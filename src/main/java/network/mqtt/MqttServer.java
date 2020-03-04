package network.mqtt;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;

public final class MqttServer
{
    private final int port;
    private final int boss;
    private final int work;

    private EventLoopGroup  bossGroup;
    private EventLoopGroup  workGroup;
    private ServerBootstrap bootstrap;

    public MqttServer()
    {
        this.port = 1883;
        this.boss = Runtime.getRuntime().availableProcessors() + 1;
        this.work = this.boss;
    }

    public MqttServer(int port)
    {
        this.port = port;
        this.boss = Runtime.getRuntime().availableProcessors() + 1;
        this.work = this.boss;
    }

    public MqttServer(int port, int boss, int work)
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
                                 MqttEncoder.INSTANCE,
                                 new MqttDecoder(),
                                 new IdleStateHandler(45, 0, 0, TimeUnit.SECONDS),
                                 MessageHandler.INSTANCE
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
    private static class MessageHandler extends SimpleChannelInboundHandler<MqttMessage>
    {
        private static final MessageHandler INSTANCE = new MessageHandler();

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg)
        {
            switch (msg.fixedHeader().messageType())
            {
                case CONNECT:
                    MqttConnAckMessage connAck = new MqttConnAckMessage(
                            new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                            new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false)
                    );
                    ctx.writeAndFlush(connAck);
                    break;
                case PINGREQ:
                    MqttMessage pingResp = new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0));
                    ctx.writeAndFlush(pingResp);
                    break;
                case DISCONNECT:
                    ctx.close();
                    break;
                default:
                    System.out.println("Unexpected message type: " + msg.fixedHeader().messageType());
                    ReferenceCountUtil.release(msg);
                    ctx.close();
            }
        }
    }
}