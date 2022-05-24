package com.rain.fabricdemo.test;

import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.gateway.impl.ContractImpl;
import org.hyperledger.fabric.gateway.impl.GatewayImpl;
import org.hyperledger.fabric.gateway.impl.TransactionImpl;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.common.Common.Block;
import org.hyperledger.fabric.sdk.*;
import org.omg.CORBA.TRANSACTION_REQUIRED;

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
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class Hello {

    // connection.json ��Ӧ������������Ϣ
    private static final Path NETWORK_CONFIG_PATH = Paths.get("src", "main", "resources", "connection.json");
    // ֤���λ��
    private static final Path credentialPath = Paths.get("src", "main", "resources", "crypto-config",
            "peerOrganizations", "org1.example.com", "users", "Admin@org1.example.com", "msp");

    private static void query(Network network) {
        try {
            // �õ�smart contract
            Contract contract = network.getContract("mycc");
            // ��ѯ�˱���Ϣ
            byte[] queryA = contract.evaluateTransaction("query", "a");
            System.out.println("A: " + new String(queryA, StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("QueryLedger Error");
            e.printStackTrace();
        }
    }

    public static void queryFromPeer(Network network) {
        try {
            GatewayImpl gateway = (GatewayImpl) network.getGateway();
            Channel channel = network.getChannel();

            QueryByChaincodeRequest queryByChaincodeRequest = gateway.getClient().newQueryProposalRequest();
            queryByChaincodeRequest.setChaincodeName("mycc");
            queryByChaincodeRequest.setFcn("query");
            queryByChaincodeRequest.setArgs("a");

            Collection<Peer> peerSet = channel.getPeers();
            Collection<Peer> endorserSet = new LinkedList<>();
            for (Peer peer : peerSet) {
                if (peer.getName().equals("peer0.org2.example.com")) {
                    endorserSet.add(peer);
                }
                //System.out.println(peer.getName());
            }
            // System.out.println(endorserSet.toString());

            Collection<ProposalResponse> proposalResponses = channel.queryByChaincode(queryByChaincodeRequest, endorserSet);
            for (ProposalResponse prores : proposalResponses) {
                String result = prores.getProposalResponse().getResponse().getPayload().toStringUtf8();
                System.out.printf("Result from %s: %s\n", prores.getPeer().getName(), result);
            }

        } catch (Exception e) {
            System.out.println("QueryLedger Error");
            e.printStackTrace();
        }
    }

    public static void invokeFromPeer(Network network) {
        try {
            // �õ�smart contract
            Contract contract = network.getContract("mycc");
            Transaction transaction = contract.createTransaction("invoke");

            Collection<Peer> peerSet = network.getChannel().getPeers();
            Collection<Peer> endorserSet = new LinkedList<>();
            for (Peer peer : peerSet) {
                if (peer.getName().equals("peer0.org2.example.com")) {
                    endorserSet.add(peer);
                }
//                endorserSet.add(peer);
            }
            // ע�ⱳ������Ƿ��������������configtx.yaml�����ļ��е��������Ƿ����
            /* configtx.yaml
            * Application: &ApplicationDefaults
                Organizations:
                Policies:
                    ...
                    Endorsement:
                        #Type: ImplicitMeta
                        # Rule: "MAJORITY Endorsement"
                        Type: Signature
                        Rule: "OR('Org1MSP.peer', 'Org2MSP.peer')"
            **/
            transaction.setEndorsingPeers(endorserSet);

            transaction.submit("a", "b", "10");
        } catch (Exception e) {
            System.out.println("Invoke Error");
            e.printStackTrace();

        }
    }

    private static void queryLedger(Network network) {
        try {
            // 1.����ʱ������ͨ��ϵͳ�����ѯָ���������
//            Contract contract = network.getContract("qscc");
//            byte[] blockBytes = contract.evaluateTransaction("GetBlockByNumber", "mychannel", "0");
//            Block block = Block.parseFrom(blockBytes);
            // System.out.println(block.getHeader().getDataHash());

            // 2.��ʼ��ʱ�������õ����еĿ�
            Channel channel = network.getChannel();
            BlockchainInfo channelInfo = channel.queryBlockchainInfo();
            for (int current = 0; current < channelInfo.getHeight(); current++) {
                BlockInfo blockInfo = channel.queryBlockByNumber(current);
                long blockNumber = blockInfo.getBlockNumber();

                System.out.println("---------------------------------------");
                System.out.printf("Block %d: data hash() is %s \n", blockNumber, Hex.encodeHexString(blockInfo.getDataHash()));

                // �����õ����е�ÿ�ʽ���
                int count = 0;
                for (BlockInfo.EnvelopeInfo env : blockInfo.getEnvelopeInfos()) {
                    count++;
                    System.out.printf("Transaction %d: transaction id: %s\n", count, env.getTransactionID());
                    System.out.printf("Transaction %d: ValidationCode: %s\n", count, env.getValidationCode());
                    System.out.printf("Transaction %d: channel id: %s\n", count, env.getChannelId());
                    System.out.printf("Transaction %d: transaction timestamp: %tB %<te,  %<tY  %<tT %<Tp\n", count, env.getTimestamp());
                    System.out.printf("Transaction %d: type id: %s\n", count, env.getType());
                    System.out.printf("Transaction %d: nonce : %s\n", count, Hex.encodeHexString(env.getNonce()));
                    System.out.printf("Transaction %d: submitter mspid: %s,  certificate: %s\n", count, env.getCreator().getMspid(), env.getCreator().getId());
                    if (env.getType() == BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE) {
                        System.out.printf("Transaction %d: isValid: %b\n", count, env.isValid());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("QueryLedger Error");
            e.printStackTrace();
        }
    }

    private static void lightNodeBlockWalker(Network network) {
        try {
            // 1.����ʱ������ͨ��ϵͳ�����ѯָ���������
//            Contract contract = network.getContract("qscc");
//            byte[] blockBytes = contract.evaluateTransaction("GetBlockByNumber", "mychannel", "0");
//            Block block = Block.parseFrom(blockBytes);
            // System.out.println(block.getHeader().getDataHash());

            // 2.��ʼ��ʱ�������õ����еĿ�
            Channel channel = network.getChannel();
//            Collection<Peer> peers = channel.getPeers();
//            for (Peer peer : peers) {
//                if (!peer.getName().equals("peer2.org1.example.com")) {
//
//                }
//            }

            BlockchainInfo channelInfo = channel.queryBlockchainInfo();
            for (int current = 0; current < channelInfo.getHeight(); current++) {
                BlockInfo blockInfo = channel.queryBlockByNumber(current);
                long blockNumber = blockInfo.getBlockNumber();

                System.out.println("---------------------------------------");
                System.out.printf("Block %d: previous hash is %s \n", blockNumber, Hex.encodeHexString(blockInfo.getPreviousHash()));
                System.out.printf("Block %d: data hash is %s \n", blockNumber, Hex.encodeHexString(blockInfo.getDataHash()));
            }
        } catch (Exception e) {
            System.out.println("light");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        X509Certificate certificate = readX509Certificate(credentialPath.resolve(Paths.get("signcerts", "Admin@org1.example.com-cert.pem")));
        PrivateKey privateKey = getPrivateKey(credentialPath.resolve(Paths.get("keystore", "priv_sk")));

        // ����һ��Ǯ���������н�����������Ҫ��identities
        Wallet wallet = Wallets.newInMemoryWallet();
        // Path walletDir = Paths.get("wallet");
        // Wallet wallet = Wallets.newFileSystemWallet(walletDir);
        wallet.put("user", Identities.newX509Identity("Org1MSP", certificate, privateKey));

        // ����������������Ҫ��gateway connection������Ϣ
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, "user")
                .networkConfig(NETWORK_CONFIG_PATH);

        // ����Gateway����
        try (Gateway gateway = builder.connect()) {
            // ����channel
            Network network = gateway.getNetwork("mychannel");

            // ��ͨ��ѯ
            // query(network);

            // ͨ��ϵͳ�����ѯ�˱��е�����
//            queryLedger(network);

            // ָ��peer��ѯ
            queryFromPeer(network);

            // ָ��peer�ύ����
//            invokeFromPeer(network);

            // ��ѯ��ڵ��ϵ�����
            // lightNodeBlockWalker(network);
        } catch (Exception e) {
            System.out.println("Main Error!");
            e.printStackTrace();
        }

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
