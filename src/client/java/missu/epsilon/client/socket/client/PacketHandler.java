package missu.epsilon.client.socket.client;

public interface PacketHandler {

    void onMessage(String sender,String message);

    void onDisconnected(String message);

    void onConnected();

    String getInGameUsername();

}
