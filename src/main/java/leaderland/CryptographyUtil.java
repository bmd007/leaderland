package leaderland;

import org.bouncycastle.util.io.pem.PemObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class CryptographyUtil {

    static {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator kpg = kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return kpg.genKeyPair();
//            byte[] publicKey = kp.getPublic().getEncoded();
//            byte[] privateKey = kp.getPrivate().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveKeyPair(KeyPair keyPair){

    }

    public static byte[] publicKeyToPem(PublicKey publicKey){
        var public_key = new PemObject("PUBLIC KEY", publicKey.getEncoded());
        return public_key.getContent();
    }

    public static RSAPublicKey publicKeyOf(KeyPair keys) {
        return (RSAPublicKey) keys.getPublic();
    }

    public static RSAPrivateKey privateKeyOf(KeyPair keys) {
        return (RSAPrivateKey) keys.getPrivate();
    }
}
