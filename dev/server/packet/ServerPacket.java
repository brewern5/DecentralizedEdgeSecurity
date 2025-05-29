package packet;

import java.io.*;

public class ServerPacket implements Serializable {
    
    private int packetType;     // 1 for message 2 for command
    private String sender;
    private String payload;

    public ServerPacket(int packetType, String sender, String payload) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = payload;
    }

    /*          Accessor methods         */

    public int getPacketType() {
        return packetType;
    }

    public String getSender() {
        return sender;
    }

    public String payload() {
        return payload;
    }

    // Need to serialize the data since they will be sent (and read) as bytes
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(this);
        return bos.toByteArray();
    }

    // This will deserialize the data (i.e the recieved data);
    public static ServerPacket deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(bis);
        return (ServerPacket) in.readObject();  // returns the Object as a ServerPacket object rather than a generic object
    }
}
