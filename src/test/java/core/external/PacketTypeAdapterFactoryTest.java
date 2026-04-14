package core.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.packet.message.MessagePacket;

class PacketTypeAdapterFactoryTest {

    @Test
    void shouldSerializeAndDeserializeMessagePacket() {
        RuntimeMembershipState membershipState = new RuntimeMembershipState();
        membershipState.assignId("sender-1");
        membershipState.assignClusterId("cluster-1");

        MessagePacket packet = new MessagePacket(membershipState, "recipient-1");
        packet.addMessageBody("payload-value");

        Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
            .create();

        String serialized = packet.toJson();
        AbstractPacket parsed = gson.fromJson(serialized, AbstractPacket.class);

        assertInstanceOf(MessagePacket.class, parsed);
        assertEquals(PacketType.MESSAGE, parsed.getPacketType());
        assertEquals("payload-value", parsed.getValueByKey("message"));
    }
}
