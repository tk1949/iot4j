import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;

public class CoAPClient
{
    public static void main(String[] args) throws ConnectorException, IOException
    {
        CoapClient client = new CoapClient("coap://127.0.0.1:5683/test?appKey=zq6NDc3sb6QmoQF1&appSecret=PosmJNUoMLD777Nf7tlu");
        client.get();
    }
}