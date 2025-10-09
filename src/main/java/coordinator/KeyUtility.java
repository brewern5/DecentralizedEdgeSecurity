// KeyUtility.java
package coordinator;

import java.security.interfaces.RSAPrivateKey;
import java.security.*;
import java.security.interfaces.RSAPublicKey;

public class KeyUtility {
    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    // We choose a secure, high-bit key size for the Coordinator's private key.
    private static final int KEY_SIZE = 4096; 
    private static final String ALGORITHM = "RSA";
    
    // Alias/Name to reference the key material
    public static final String KEY_ALIAS = "CoordinatorSigningKey"; 

    /**
     * Initializes the Coordinator's RSA KeyPair for JWT signing.
     * In a production environment, this would load keys from a secure file/vault.
     * Here, we generate them once upon startup for simulation.
     */
    public static void initializeKeys() throws Exception {
        // If keys are null, generate a new pair.
        if (privateKey == null || publicKey == null) {
            // Register Bouncy Castle as a Security Provider (needed for certain environments)
            if (Security.getProvider("BC") == null) {
                // NOTE: BouncyCastleProvider is part of the dependency you added
                // Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                // Assuming it's already registered or loaded by the system for now. 
            }
            
            // Generate the KeyPair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(KEY_SIZE);
            KeyPair pair = keyGen.generateKeyPair();
            
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();

            System.out.println("COORDINATOR KEY MANAGER: Successfully generated new " + KEY_SIZE + "-bit RSA KeyPair.");
        }
    }

    /**
     * Retrieves the Coordinator's Private Key, used for signing JWTs.
     * This key must NEVER be shared.
     */
    public static PrivateKey getPrivateKey() {
        if (privateKey == null) {
            throw new IllegalStateException("Private Key has not been initialized. Run initializeKeys() first.");
        }
        return privateKey;
    }

    /**
     * Retrieves the Coordinator's Public Key, used by Servers/Nodes for verification.
     * This key is SAFE to share.
     */
    public static PublicKey getPublicKey() {
        if (publicKey == null) {
            throw new IllegalStateException("Public Key has not been initialized. Run initializeKeys() first.");
        }
        return publicKey;
    }
}