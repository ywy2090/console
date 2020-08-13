package console.account;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountTool {

    private static final Logger logger = LoggerFactory.getLogger(AccountTool.class);

    public static boolean isGMAccount(Credentials credentials) {
        return true;
    }

    /** @return */
    public static Credentials newAccount() {
        Credentials credentials = GenCredential.create();
        logger.info(
                " newAccount: {}, encryptType: {}",
                credentials.getAddress(),
                EncryptType.encryptType);
        return credentials;
    }

    /**
     * @param accountPath
     * @param password
     * @return
     * @throws UnrecoverableKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws NoSuchProviderException
     * @throws CertificateException
     * @throws IOException
     */
    public static Credentials loadAccount(String accountPath, String password)
            throws UnrecoverableKeyException, InvalidKeySpecException, NoSuchAlgorithmException,
                    KeyStoreException, NoSuchProviderException, CertificateException, IOException {
        ECKeyPair keyPair = null;

        if (accountPath.endsWith("p12")) {
            // p12
            P12Manager p12Manager = new P12Manager();
            p12Manager.setP12File(accountPath);
            p12Manager.setPassword(password);
            p12Manager.load();
            keyPair = p12Manager.getECKeyPair();
        } else {
            // pem
            PEMManager pem = new PEMManager();
            pem.setPemFile(accountPath);
            pem.load();
            keyPair = pem.getECKeyPair();
        }

        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));
        logger.info(
                " loadAccount accountFile: {}, address: {}", accountPath, credentials.getAddress());

        return credentials;
    }

    /**
     * @param
     * @param credentials
     */
    public static void saveAccount(Credentials credentials, String dirPath) {
        /*
        String fileName = credentials.getAddress() + (isGMAccount(credentials)? "_gm.pem" : ".pem");
        String path = dirPath + File.pathSeparator + fileName;
        logger.info(" saveAccount, account: {}, path: {}", credentials.getAddress(), path);

        ECPrivateKeySpec secretKeySpec =
                new ECPrivateKeySpec(credentials.getEcKeyPair().getPrivateKey(), ECCParams.ecNamedCurveSpec);
        BCECPrivateKey bcecPrivateKey =
                new BCECPrivateKey("ECDSA", secretKeySpec, BouncyCastleProvider.CONFIGURATION);
                */
        return;
    }
}
