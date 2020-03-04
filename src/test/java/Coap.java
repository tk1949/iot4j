import network.coap.CoapServer;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Coap
{
    public static void main(String[] args)
    {
        CoapServer server = new CoapServer();
        server.start();
        server.add(
                new CoapResource("test")
                {
                    @Override
                    public void handleGET(CoapExchange exchange)
                    {
                        System.out.println(this);
                        super.handlePOST(exchange);
                    }
                }
        );
    }
}