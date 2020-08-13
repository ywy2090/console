package console.account;

import console.common.HelpInfo;
import java.util.Map;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountImpl implements AccountInterface {

    private static final Logger logger = LoggerFactory.getLogger(AccountImpl.class);

    private AccountManager accountManager;

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void loadAccount(String[] params) {
        if (params.length < 2) {
            HelpInfo.promptHelp("loadAccount");
            return;
        }

        if (params.length > 4) {
            HelpInfo.promptHelp("loadAccount");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.loadAccountHelp();
            return;
        }

        String accountPath = params[1];
        try {
            Credentials credentials = AccountTool.loadAccount(accountPath, "");
            accountManager.addAccount(credentials);
            System.out.println(
                    "load "
                            + accountPath
                            + " successfully, account address: "
                            + credentials.getAddress());
            System.out.println();
        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println("load " + accountPath + " failed, error: " + e.getMessage());
            System.out.println();
        }
    }

    @Override
    public void switchAccount(String[] params) {
        if (params.length < 2) {
            HelpInfo.promptHelp("switchAccount");
            return;
        }

        if (params.length > 3) {
            HelpInfo.promptHelp("switchAccount");
            return;
        }

        if ("-h".equals(params[1]) || "--help".equals(params[1])) {
            HelpInfo.switchAccountHelp();
            return;
        }

        String account = params[1];
        try {
            account = Numeric.prependHexPrefix(account);
            Map<String, Credentials> accountMap = accountManager.getAccountMap();
            Credentials credentials = accountMap.get(account);
            if (credentials == null) {
                System.out.println("account:" + account + " not exist.");
                System.out.println("account list:");
                for (String address : accountMap.keySet()) {
                    System.out.println("\t " + address);
                }
            } else {
                accountManager.setCurrentAccount(credentials);
                System.out.println("switch to account: " + account + " successfully.");
            }
            System.out.println();

        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println("load " + account + " failed, error: " + e.getMessage());
            System.out.println();
        }
    }

    @Override
    public void listAccount(String[] params) {
        if ((params.length > 1) && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.listAccountHelp();
            return;
        }

        Map<String, Credentials> accountMap = accountManager.getAccountMap();
        System.out.println("account list:");
        for (String address : accountMap.keySet()) {
            System.out.println("\t " + address);
        }
        System.out.println();
    }

    @Override
    public void saveAccount(String[] params) {
        // saveAccount account accountSavePath
    }

    @Override
    public void newAccount(String[] params) {

        if ((params.length > 1) && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.newAccountHelp();
            return;
        }

        try {
            Credentials credentials = AccountTool.newAccount();
            accountManager.addAccount(credentials);
            System.out.println(
                    "create account successfully, account address:" + credentials.getAddress());
            System.out.println();
        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println("newAccount failed, error: " + e.getMessage());
            System.out.println();
        }
    }
}
