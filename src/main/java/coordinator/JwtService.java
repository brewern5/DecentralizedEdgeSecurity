// JwtService.java
package coordinator;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWTCreator;
import java.util.Date;
import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtService {

    // Token expiration is set to a short duration (e.g., 30 minutes)
    private static final long EXPIRATION_TIME = 1800000; // 30 minutes in milliseconds
    private static final String ISSUER = "Coordinator";

    /**
     * Generates a signed JWT for a node, embedding critical network identifiers (claims).
     * This is the core of the Third-Party Based Identity model.
     *
     * @param nodeId        The ID of the node requesting the token (Subject).
     * @param serverId      The ID of the server the node is attached to (Claim).
     * @param clusterId     The ID of the local cluster (Claim).
     * @param targetNodeId  Optional: The ID of the specific peer this token is for (Audience Claim).
     * @return              The signed JWT string.
     */
    public String generateToken(String nodeId, String serverId, String clusterId, String targetNodeId) throws Exception {
        
        // 1. Load Signing Key from the Utility Class
        PrivateKey privateKey = KeyUtility.getPrivateKey();
        PublicKey publicKey = KeyUtility.getPublicKey();
        
        // Use RSA256 for asymmetric signing with the private key
        Algorithm algorithm = Algorithm.RSA256(
            (java.security.interfaces.RSAPublicKey) publicKey, 
            (java.security.interfaces.RSAPrivateKey) privateKey
        );

        // Calculate Expiration Time
        Date issuedAt = new Date();
        Date expiresAt = new Date(System.currentTimeMillis() + EXPIRATION_TIME);

        // 2. Build the Token and Inject Claims
        JWTCreator.Builder builder = JWT.create()
            .withIssuer(ISSUER)
            .withSubject(nodeId)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            
            // CRITICAL NETWORK IDENTIFIERS (Required by verification plan)
            .withClaim("server_id", serverId)
            .withClaim("cluster_id", clusterId);
            
        // 3. Conditional Audience Claim (Audience = Target Peer ID)
        if (targetNodeId != null && !targetNodeId.isEmpty()) {
            builder.withAudience(targetNodeId); 
        }

        // 4. Sign and return the token
        return builder.sign(algorithm);
    }
}