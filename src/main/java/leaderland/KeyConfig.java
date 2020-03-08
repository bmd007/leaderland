package leaderland;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class KeyConfig {

    @Bean
    public RSAPublicKey publicKey(){
        return CryptographyUtil.publicKeyOf(CryptographyUtil.generateRSAKeyPair());
    }
}
