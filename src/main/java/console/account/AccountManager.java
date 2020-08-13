package console.account;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManager {

    private static final Logger logger = LoggerFactory.getLogger(AccountManager.class);

    public AccountManager()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
                    NoSuchProviderException {
        Credentials credentials = AccountTool.newAccount();
        logger.info(" AccountManager newAccount: {}", credentials.getAddress());
        setCurrentAccount(credentials);
    }

    /** Current account used to sign transaction */
    private Credentials currentAccount;
    /** account mapper used to save all accounts loaded */
    private Map<String, Credentials> accountMap = new ConcurrentHashMap<>();

    public Credentials getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Credentials currentAccount) {
        this.currentAccount = currentAccount;
    }

    public boolean isAccountExist(String account) {
        return accountMap.get(account) != null;
    }

    public boolean addAccount(Credentials credentials) {
        Credentials credentials1 = accountMap.putIfAbsent(credentials.getAddress(), credentials);
        return credentials1 != null;
    }

    public Map<String, Credentials> getAccountMap() {
        return accountMap;
    }

    public String[] accountAddresses() {
        return accountMap.keySet().toArray(new String[0]);
    }

    public void setAccountMap(Map<String, Credentials> accountMap) {
        this.accountMap = accountMap;
    }
}
