/*
 * Copyright © 2018 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl;

import com.apollocurrency.aplwallet.apl.crypto.Crypto;
import com.apollocurrency.aplwallet.apl.util.Convert;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import util.WalletRunner;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.apollocurrency.aplwallet.apl.TestData.TEST_LOCALHOST;
import static org.slf4j.LoggerFactory.getLogger;
import static util.TestUtil.atm;

public class TestDataGenerator {
        private static final Logger LOG = getLogger(TestDataGenerator.class);

    private static NodeClient client = new NodeClient();

    public static void main(String[] args) {
        WalletRunner runner = new WalletRunner(true);
        try {
            runner.run();
            TestAccount acc1 = generateAccount("Acc1");
            System.out.println("Pass:" + acc1.getSecretPhrase());
            System.out.println("Acc:" + acc1.getRS());
//            fundAcc(acc1, new TestAccount("test1"), 100);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            runner.shutdown();
        }
    }

    public static Map<TestAccount, List<JSONTransaction>> generateChatsForAccount(TestAccount acc) throws Exception {
        Map<TestAccount, List<JSONTransaction>> chatAccounts = new HashMap<>();

        TestAccount randomAcc1 = generateAccount("randomAcc1_");
        TestAccount randomAcc2 = generateAccount("randomAcc2_");
        TestAccount randomAcc3 = generateAccount("randomAcc3_");
        Thread acc1Thread = new Thread(() -> {
            try {
                fundAcc(randomAcc1, acc, 10L);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, "randomAcc1 chat generation");
        Thread acc2Thread = new Thread(() -> {
            try {
                fundAcc(randomAcc2, acc, 10L);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, "randomAcc2 chat generation");
        Thread acc3Thread = new Thread(() -> {
            try {
                fundAcc(randomAcc3, acc, 10L);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, "randomAcc3 chat generation");
        acc1Thread.start();
        acc2Thread.start();
        acc3Thread.start();
        acc1Thread.join();
        acc2Thread.join();
        acc3Thread.join();
        chatAccounts.put(randomAcc1, generateChatTransactions(acc, randomAcc1, 2, 2, false));

        chatAccounts.put(randomAcc2, generateChatTransactions(acc, randomAcc2, 3, 4, false));

        chatAccounts.put(randomAcc3, generateChatTransactions(acc, randomAcc3, 0, 1, true));

        return chatAccounts;
    }

    public static JSONTransaction generateChatTransaction(TestAccount sender, TestAccount recipient) throws IOException, ParseException {
        return client.sendChatTransaction(TEST_LOCALHOST, "Test message", sender.getSecretPhrase(), 60, Convert.rsAccount(recipient.getId()), atm(1));
    }

    public static List<JSONTransaction> generateChatTransactions(TestAccount sender, TestAccount recipient, int numberOfReceivedTransactions,
                                                                 int numberOfSentTransactions, boolean wait) throws Exception {
        List<JSONTransaction> transactions = new ArrayList<>();
        LOG.debug("Generating chat transaction for account: {} and {}", sender.getRS(), recipient.getRS());
        for (int i = 0; i < Math.min(numberOfReceivedTransactions, numberOfSentTransactions); i++) {
            transactions.add(generateChatTransaction(sender, recipient));
            JSONTransaction tr = generateChatTransaction(recipient, sender);
            transactions.add(tr);
        }
        TestAccount s = numberOfReceivedTransactions > numberOfSentTransactions ? recipient : sender;
        TestAccount r = numberOfReceivedTransactions > numberOfSentTransactions ? sender : recipient;
        for (int i = Math.min(numberOfReceivedTransactions, numberOfSentTransactions); i < Math.max(numberOfReceivedTransactions,
                numberOfSentTransactions); i++) {
            JSONTransaction tr = generateChatTransaction(s, r);
            transactions.add(tr);
        }
        if (wait) {
            LOG.debug("Waiting confirmations for chat transactions");
            waitForConfirmation(transactions);
        }
        return transactions;
    }

    public static void fundAcc(TestAccount account, TestAccount fundingAcc, long amount) throws Exception {
        JSONTransaction jsonTransaction = client.sendMoneyTransaction(TEST_LOCALHOST, fundingAcc.getSecretPhrase(),
                Convert.toHexString(account.getPublicKey()),60,
                Convert.rsAccount(account.getId()), atm(amount), atm(1L));
        LOG.debug("Funding acc: {} to {}", fundingAcc.getRS(), account.getRS());
        waitForConfirmation(jsonTransaction);

    }

    private static boolean waitForConfirmation(JSONTransaction transaction) throws Exception {
        while (true) {
            JSONTransaction receivedTransaction = client.getTransaction(TEST_LOCALHOST, transaction.getFullHash());
            if (receivedTransaction != null && receivedTransaction.getNumberOfConfirmations() >= 0) {
                return true;
            }
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private static boolean waitForConfirmation(List<JSONTransaction> transactions) {
        for (JSONTransaction tr : transactions) {
            while (true) {
                try {
                    JSONTransaction receivedTransaction = client.getTransaction(TEST_LOCALHOST, tr.getFullHash());
                    TimeUnit.SECONDS.sleep(1);
                    if (receivedTransaction != null && receivedTransaction.getNumberOfConfirmations() >= 0) {
                        break;
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException(e.toString(), e);
                }
            }
        }
        return false;
    }
    public static TestAccount generateAccount(String namePrefix) {
        String secretPhrase = generateSecretPhrase();
        byte[] publicKey = Crypto.getPublicKey(secretPhrase);
        long id = Account.getId(publicKey);
        String name = namePrefix + Crypto.getSecureRandom().nextInt();
        return new TestAccount(id, publicKey, name, secretPhrase);
    }
    public static JSONTransaction saveAcc(TestAccount account) throws Exception {
            JSONTransaction jsonTransaction = client.setAccountInfo(TEST_LOCALHOST, account.getName(), account.getSecretPhrase(), 60, atm(1));
            waitForConfirmation(jsonTransaction);
        return jsonTransaction;
    }

    public static String generateSecretPhrase() {
        return "test" + new Random().nextInt();
    }
}