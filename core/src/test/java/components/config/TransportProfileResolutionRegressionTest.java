package components.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import components.coordinator.config.CoordinatorConfig;
import components.node.config.NodeConfig;
import components.server.config.ServerConfig;

/**
 * Regression tests for transport profile selection behavior in component config loaders.
 *
 * Verifies profile resolution precedence:
 * 1) explicit des.transport.profile
 * 2) fallback from transport.mode (LORA -> lora)
 * 3) default to ip
 */
class TransportProfileResolutionRegressionTest {

    private static final String PROFILE_KEY = "des.transport.profile";
    private static final String MODE_KEY = "transport.mode";

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty(PROFILE_KEY);
        System.clearProperty(MODE_KEY);
    }

    @Test
    void resolveConfigProfile_defaultsToIpWhenNoOverrides() throws Exception {
        for (Class<?> configClass : configClasses()) {
            assertEquals("ip", invokeResolveConfigProfile(configClass));
        }
    }

    @Test
    void resolveConfigProfile_derivesLoraFromTransportModeWhenProfileMissing() throws Exception {
        System.setProperty(MODE_KEY, "LORA");

        for (Class<?> configClass : configClasses()) {
            assertEquals("lora", invokeResolveConfigProfile(configClass));
        }
    }

    @Test
    void resolveConfigProfile_profilePropertyTakesPrecedenceOverMode() throws Exception {
        System.setProperty(PROFILE_KEY, "ip");
        System.setProperty(MODE_KEY, "LORA");

        for (Class<?> configClass : configClasses()) {
            assertEquals("ip", invokeResolveConfigProfile(configClass));
        }
    }

            @Test
            void resolveDefaultConfigPath_usesLoraWhenDerivedFromMode() throws Exception {
            System.setProperty(MODE_KEY, "LORA");

            assertEquals(
                "config/lora/node_config/nodeConfig.properties",
                invokeStaticString(NodeConfig.class, "resolveDefaultConfigPath")
            );
            assertEquals(
                "config/lora/server_config/serverConfig.properties",
                invokeStaticString(ServerConfig.class, "resolveDefaultConfigPath")
            );
            assertEquals(
                "config/lora/coordinator_config/coordinatorConfig.properties",
                invokeStaticString(CoordinatorConfig.class, "resolveDefaultConfigPath")
            );
            }

            @Test
            void resolveInstanceConfigPath_usesIpWhenModeMissing() throws Exception {
            String instanceId = "node1";

            assertEquals(
                "config/ip/node_config/nodeConfig_" + instanceId + ".properties",
                invokeStaticStringWithArg(NodeConfig.class, "resolveInstanceConfigPath", instanceId)
            );
            assertEquals(
                "config/ip/server_config/serverConfig_" + instanceId + ".properties",
                invokeStaticStringWithArg(ServerConfig.class, "resolveInstanceConfigPath", instanceId)
            );
            assertEquals(
                "config/ip/coordinator_config/coordinatorConfig_" + instanceId + ".properties",
                invokeStaticStringWithArg(CoordinatorConfig.class, "resolveInstanceConfigPath", instanceId)
            );
            }

    @Test
    void resolveConfigProfile_normalizesExplicitProfileCaseAndWhitespace() throws Exception {
        System.setProperty(PROFILE_KEY, "  LoRa  ");
        System.setProperty(MODE_KEY, "IP");

        for (Class<?> configClass : configClasses()) {
            assertEquals("lora", invokeResolveConfigProfile(configClass));
        }
    }

    @Test
    void resolveConfigProfile_handlesMixedCaseTransportMode() throws Exception {
        System.setProperty(MODE_KEY, "  lOrA  ");

        for (Class<?> configClass : configClasses()) {
            assertEquals("lora", invokeResolveConfigProfile(configClass));
        }
    }

    private static List<Class<?>> configClasses() {
        return List.of(NodeConfig.class, ServerConfig.class, CoordinatorConfig.class);
    }

    private static String invokeResolveConfigProfile(Class<?> targetClass) throws Exception {
        Method method = targetClass.getDeclaredMethod("resolveConfigProfile");
        method.setAccessible(true);
        return (String) method.invoke(null);
    }

    private static String invokeStaticString(Class<?> targetClass, String methodName) throws Exception {
        Method method = targetClass.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (String) method.invoke(null);
    }

    private static String invokeStaticStringWithArg(Class<?> targetClass, String methodName, String arg) throws Exception {
        Method method = targetClass.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, arg);
    }
}
