package com.rain.fabricdemo.test;

import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Peer;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.EnumSet;
import java.util.concurrent.TimeoutException;

public class WalletDemo {
    // connection.json ��Ӧ������������Ϣ
    private static final Path NETWORK_CONFIG_PATH = Paths.get("src", "main", "resources", "connection-lightnode.json");
    // ֤���λ��
    private static final Path credentialPath = Paths.get("src", "main", "resources", "crypto-config",
            "peerOrganizations", "org1.example.com", "users", "Admin@org1.example.com", "msp");

    Wallet wallet;
    Gateway gateway;
    Network network;
    Contract contract;

    public void query() {
        try {
            // ��ѯ�˱���Ϣ
            byte[] queryA = contract.evaluateTransaction("query", "a");
            System.out.println("A: " + new String(queryA, StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("QueryLedger Error");
            e.printStackTrace();
        }
    }

    public void invoke() {
        try {
            byte[] invokeResult = contract.createTransaction("invoke")
                    .setEndorsingPeers(network.getChannel().getPeers(EnumSet.of(Peer.PeerRole.ENDORSING_PEER)))
                    .submit("b", "a", "1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public WalletDemo() throws Exception {
        X509Certificate certificate = readX509Certificate(credentialPath.resolve(Paths.get("signcerts", "Admin@org1.example.com-cert.pem")));
        PrivateKey privateKey = getPrivateKey(credentialPath.resolve(Paths.get("keystore", "priv_sk")));

        // ����һ��Ǯ���������н�����������Ҫ��identities
        wallet = Wallets.newInMemoryWallet();
        // Path walletDir = Paths.get("wallet");
        // Wallet wallet = Wallets.newFileSystemWallet(walletDir);
        wallet.put("user", Identities.newX509Identity("Org1MSP", certificate, privateKey));

        // ����������������Ҫ��gateway connection������Ϣ
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, "user")
                .networkConfig(NETWORK_CONFIG_PATH);

        // ����Gateway����
        gateway = builder.connect();
        // ����channel
        network = gateway.getNetwork("mychannel");
        // �õ�smart contract
        contract = network.getContract("mycc");
    }

    private static X509Certificate readX509Certificate(final Path certificatePath) throws IOException, CertificateException {
        try (Reader certificateReader = Files.newBufferedReader(certificatePath, StandardCharsets.UTF_8)) {
            return Identities.readX509Certificate(certificateReader);
        }
    }

    private static PrivateKey getPrivateKey(final Path privateKeyPath) throws IOException, InvalidKeyException {
        try (Reader privateKeyReader = Files.newBufferedReader(privateKeyPath, StandardCharsets.UTF_8)) {
            return Identities.readPrivateKey(privateKeyReader);
        }
    }
}
