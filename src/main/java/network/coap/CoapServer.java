package network.coap;

import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.elements.tcp.netty.TcpServerConnector;

import java.net.InetSocketAddress;

public class CoapServer extends org.eclipse.californium.core.CoapServer
{
    private final int     port;
    private final boolean tcp;

    public CoapServer()
    {
        this.port = 5683;
        this.tcp  = false;
    }

    public CoapServer(int port)
    {
        this.port = port;
        this.tcp  = false;
    }

    public CoapServer(boolean tcp)
    {
        this.port = 5683;
        this.tcp  = tcp;
    }

    public CoapServer(int port, boolean tcp)
    {
        this.port = port;
        this.tcp  = tcp;
    }

    @Override
    public synchronized void start()
    {
        NetworkConfig config = NetworkConfig.getStandard();
        InetSocketAddress bindToAddress = new InetSocketAddress(port);
        {
            CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
            builder.setInetSocketAddress(bindToAddress);
            builder.setNetworkConfig(config);
            addEndpoint(builder.build());
        }
        if (tcp)
        {
            TcpServerConnector connector = new TcpServerConnector(bindToAddress, config.getInt(NetworkConfig.Keys.TCP_WORKER_THREADS), config.getInt(NetworkConfig.Keys.TCP_CONNECTION_IDLE_TIMEOUT));
            CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
            builder.setConnector(connector);
            builder.setNetworkConfig(config);
            addEndpoint(builder.build());
        }

        super.start();
    }

    @Override
    public CoapServer add(Resource... resources)
    {
        super.add(resources);
        return this;
    }
}