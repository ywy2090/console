package console.account;

import org.fisco.bcos.web3j.crypto.Credentials;

public class Account {

    public static final String NEW_ACCOUNT_DESC = "temporary account";

    private Credentials credentials;
    /** if this sm account */
    private boolean isSMAccount;
    /** if this account temporary account */
    private boolean isNewAccount;

    public Account(Credentials credentials) {
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public boolean isNewAccount() {
        return isNewAccount;
    }

    public void setNewAccount(boolean newAccount) {
        isNewAccount = newAccount;
    }

    public boolean isSMAccount() {
        return isSMAccount;
    }

    public void setSMAccount(boolean SMAccount) {
        isSMAccount = SMAccount;
    }
}
