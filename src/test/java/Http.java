import network.http.HttpServer;

public class Http
{
    public static void main(String[] args) throws InterruptedException
    {
        HttpServer server = new HttpServer(9000);
        server.start();
    }
}