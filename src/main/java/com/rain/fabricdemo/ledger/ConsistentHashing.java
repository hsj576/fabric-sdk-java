package com.rain.fabricdemo.ledger;

import java.util.*;

    public class ConsistentHashing {
        // ����ڵ�
        private Set<String> physicalNodes = new TreeSet<String>() {
            {
                add("smallbank");
                add("smallbank_1_shard");
                add("smallbank_2_shard");
                add("smallbank_3_shard");
                add("smallbank_4_shard");
                add("smallbank_5_shard");
                add("smallbank_6_shard");
                add("smallbank_7_shard");
                add("smallbank_8_shard");
                add("smallbank_9_shard");
                add("smallbank_10_shard");
                add("smallbank_11_shard");
                add("smallbank_12_shard");
                add("smallbank_13_shard");
                add("smallbank_14_shard");
                add("smallbank_15_shard");
            }
        };
        //����ڵ�
        private final int VIRTUAL_COPIES = 50000; // ����ڵ�������ڵ�ĸ��Ʊ���
        private TreeMap<Long, String> virtualNodes = new TreeMap<>(); // ��ϣֵ => ����ڵ�
        // 32λ�� Fowler-Noll-Vo ��ϣ�㷨
        // https://en.wikipedia.org/wiki/Fowler�CNoll�CVo_hash_function
        private static Long FNVHash(String key) {
            final int p = 16777619;
            Long hash = 2166136261L;
            for (int idx = 0, num = key.length(); idx < num; ++idx) {
                hash = (hash ^ key.charAt(idx)) * p;
            }
            hash += hash << 13;
            hash ^= hash >> 7;
            hash += hash << 3;
            hash ^= hash >> 17;
            hash += hash << 5;

            if (hash < 0) {
                hash = Math.abs(hash);
            }
            return hash;
        }
        // ��������ڵ㣬��������ڵ�ӳ���
        public ConsistentHashing() {
            for (String nodeIp : physicalNodes) {
                addPhysicalNode(nodeIp);
            }
        }
        // �������ڵ�
        public void addPhysicalNode(String nodeIp) {
            for (int idx = 0; idx < VIRTUAL_COPIES; ++idx) {
                long hash = FNVHash(nodeIp + "#" + idx);
                virtualNodes.put(hash, nodeIp);
            }
        }
        // ɾ������ڵ�
        public void removePhysicalNode(String nodeIp) {
            for (int idx = 0; idx < VIRTUAL_COPIES; ++idx) {
                long hash = FNVHash(nodeIp + "#" + idx);
                virtualNodes.remove(hash);
            }
        }
        // ���Ҷ���ӳ��Ľڵ�
        public String getServer(String object) {
            long hash = FNVHash(object);
            SortedMap<Long, String> tailMap = virtualNodes.tailMap(hash); // ���д��� hash �Ľڵ�
            Long key = tailMap.isEmpty() ? virtualNodes.firstKey() : tailMap.firstKey();
            return virtualNodes.get(key);
        }
        // ͳ�ƶ�����ڵ��ӳ���ϵ
        public void dumpObjectNodeMap(String label, int objectMin, int objectMax) {
            // ͳ��
            Map<String, Integer> objectNodeMap = new TreeMap<>(); // IP => COUNT
            for (int object = objectMin; object <= objectMax; ++object) {
                String nodeIp = getServer(Integer.toString(object));
                Integer count = objectNodeMap.get(nodeIp);
                objectNodeMap.put(nodeIp, (count == null ? 0 : count + 1));
            }
            // ��ӡ
            double totalCount = objectMax - objectMin + 1;
            System.out.println("======== " + label + " ========");
            for (Map.Entry<String, Integer> entry : objectNodeMap.entrySet()) {
                long percent = (int) (100 * entry.getValue() / totalCount);
                System.out.println("IP=" + entry.getKey() + ": RATE=" + percent + "%");
            }
        }

//        public static void main(String[] args) {
//            ConsistentHashing ch = new ConsistentHashing();
//            for(int i=1;i<10;i++){
//
//                String server = ch.getServer(String.valueOf(i));
//                System.out.println("test_sample_"+i+" is routed to "+server);
//            }
//
//            // ��ʼ���
//            ch.dumpObjectNodeMap("Information", 0, 65536);
//
//
//            // �������ڵ�
//           // ch.addPhysicalNode("abstore_4_shard");
//            //ch.dumpObjectNodeMap("�������ڵ�", 0, 65536);
//        }

    }

