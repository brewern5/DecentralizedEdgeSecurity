package core.external;

import com.google.gson.TypeAdapterFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import core.external.providers.AckResponseSubtypeProvider;
import core.external.providers.ErrorResponseSubtypeProvider;
import core.external.providers.InitalizationPacketSubtypeProvider;
import core.external.providers.InitializationResponseSubtypeProvider;
import core.external.providers.KeepAlivePacketSubtypeProvider;
import core.external.providers.MessagePacketSubtypeProvider;
import core.external.providers.PeerListReqPacketSubtypeProvider;
import core.external.providers.PeerListResPacketSubtypeProvider;
import core.packet.AbstractPacket;

/**
 * Central factory for registering packet subtype mappings with Gson.
 *
 * <p>Subtype providers are loaded through ServiceLoader so packet classes can be
 * added without editing this file.
 */
public class PacketTypeAdapterFactory {

    private static final List<PacketSubtypeProvider> SUBTYPE_PROVIDERS = loadProviders();

    private PacketTypeAdapterFactory() {
    }

    private static List<PacketSubtypeProvider> loadProviders() {
        List<PacketSubtypeProvider> providers = new ArrayList<>();

        ServiceLoader<PacketSubtypeProvider> loader = ServiceLoader.load(PacketSubtypeProvider.class);
        for (PacketSubtypeProvider provider : loader) {
            providers.add(provider);
        }

        // Built-ins are a fallback for environments where service files are not resolved.
        registerBuiltIns(providers);

        providers.sort(Comparator.comparing(PacketSubtypeProvider::typeLabel));
        return Collections.unmodifiableList(providers);
    }

    private static void registerBuiltIns(List<PacketSubtypeProvider> providers) {
        registerIfMissing(providers, new InitalizationPacketSubtypeProvider());
        registerIfMissing(providers, new InitializationResponseSubtypeProvider());
        registerIfMissing(providers, new KeepAlivePacketSubtypeProvider());
        registerIfMissing(providers, new ErrorResponseSubtypeProvider());
        registerIfMissing(providers, new AckResponseSubtypeProvider());
        registerIfMissing(providers, new PeerListReqPacketSubtypeProvider());
        registerIfMissing(providers, new PeerListResPacketSubtypeProvider());
        registerIfMissing(providers, new MessagePacketSubtypeProvider());
    }

    private static void registerIfMissing(List<PacketSubtypeProvider> providers, PacketSubtypeProvider candidate) {
        for (PacketSubtypeProvider existing : providers) {
            if (existing.typeLabel().equals(candidate.typeLabel())) {
                return;
            }
        }
        providers.add(candidate);
    }

    private static RuntimeTypeAdapterFactory<AbstractPacket> buildFactory(boolean maintainTypeField) {
        RuntimeTypeAdapterFactory<AbstractPacket> factory = maintainTypeField
            ? RuntimeTypeAdapterFactory.of(AbstractPacket.class, "packetType", true)
            : RuntimeTypeAdapterFactory.of(AbstractPacket.class, "packetType");

        for (PacketSubtypeProvider provider : SUBTYPE_PROVIDERS) {
            factory = factory.registerSubtype(provider.packetClass(), provider.typeLabel());
        }

        return factory;
    }

    /**
     * Creates a runtime adapter with packetType-based subtype mapping.
     */
    public static TypeAdapterFactory create() {
        return buildFactory(false);
    }

    /**
     * Creates a runtime adapter that preserves the packetType field during deserialization.
     */
    public static TypeAdapterFactory createWithTypeField() {
        return buildFactory(true);
    }
}
