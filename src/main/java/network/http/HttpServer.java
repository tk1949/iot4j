package network.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public final class HttpServer
{
    private final int port;
    private final int boss;
    private final int work;

    private EventLoopGroup  bossGroup;
    private EventLoopGroup  workGroup;
    private ServerBootstrap bootstrap;

    public HttpServer()
    {
        this.port = 80;
        this.boss = Runtime.getRuntime().availableProcessors() + 1;
        this.work = this.boss;
    }

    public HttpServer(int port)
    {
        this.port = port;
        this.boss = Runtime.getRuntime().availableProcessors() + 1;
        this.work = this.boss;
    }

    public HttpServer(int port, int boss, int work)
    {
        this.port = port;
        this.boss = boss;
        this.work = work;
    }

    public void start() throws InterruptedException
    {
        bossGroup = new NioEventLoopGroup(boss);
        workGroup = new NioEventLoopGroup(work);
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                 .channel(NioServerSocketChannel.class)
                 .childHandler(new ChannelInitializer<SocketChannel>()
                 {
                     public void initChannel(SocketChannel ch)
                     {
                         ch.pipeline().addLast(
                                 new ReadTimeoutHandler(60, TimeUnit.SECONDS),
                                 new WriteTimeoutHandler(60, TimeUnit.SECONDS),
                                 new HttpServerCodec(),
                                 new HttpObjectAggregator(65536),
                                 new CorsHandler(
                                         CorsConfigBuilder
                                         .forAnyOrigin()
                                         .allowNullOrigin()
                                         .allowCredentials()
                                         .build()
                                 ),
                                 ResponseHandler.INSTANCE
                         );
                     }
                 })
                 .bind(port)
                 .sync();
    }

    public void stop()
    {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
        bossGroup = null;
        workGroup = null;
        bootstrap = null;
    }

    @ChannelHandler.Sharable
    private static class ResponseHandler extends SimpleChannelInboundHandler<HttpObject>
    {
        private static final ResponseHandler INSTANCE = new ResponseHandler();

        private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
        {
            if (msg instanceof HttpRequest)
            {
                HttpRequest req = (HttpRequest) msg;

                boolean keepAlive = HttpUtil.isKeepAlive(req);

                FullHttpResponse response =
                        new DefaultFullHttpResponse(
                                req.protocolVersion(),
                                OK,
                                Unpooled.wrappedBuffer(CONTENT)
                        );

                response.headers()
                        .set(CONTENT_TYPE, TEXT_PLAIN)
                        .setInt(CONTENT_LENGTH, response.content().readableBytes());

                if (keepAlive)
                {
                    if (! req.protocolVersion().isKeepAliveDefault())
                    {
                        response.headers().set(CONNECTION, KEEP_ALIVE);
                    }
                }
                else
                {
                    response.headers().set(CONNECTION, CLOSE);
                }

                ChannelFuture f = ctx.write(response);

                if (! keepAlive)
                {
                    f.addListener(ChannelFutureListener.CLOSE);
                }

                ctx.flush();
            }
        }
    }
}