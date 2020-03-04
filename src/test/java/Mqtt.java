import network.mqtt.MqttServer;

public class Mqtt
{
    public static void main(String[] args) throws InterruptedException
    {
        MqttServer server = new MqttServer();
        server.start();
    }
}