package com.rain.fabricdemo.test;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class SimpleHttpServer1 implements Runnable {

    ServerSocket serverSocket;//������Socket
    public static int PORT = 8080;//����8080�˿�

    // fabric ���
    WalletDemo walletDemo;

    // ��ʼ������ Socket �߳�.
    public SimpleHttpServer1() throws Exception {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (Exception e) {
            System.out.println("�޷�����HTTP������:" + e.getLocalizedMessage());
        }
        if (serverSocket == null) System.exit(1);//�޷���ʼ������
        new Thread(this).start();
        System.out.println("HTTP server is running, port:" + PORT);

        walletDemo = new WalletDemo();
    }

    // ���з��������߳�, �����ͻ������󲢷�����Ӧ.
    public void run() {
        while (true) {
            try {
                Socket client = null;//�ͻ�Socket
                client = serverSocket.accept();//�ͻ���(������ IE �������)�Ѿ����ӵ���ǰ������
                if (client != null) {
                    System.out.println("connect to client:" + client);
                    try {
                        // ��һ�׶�: ��������
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                client.getInputStream()));

                        System.out.println("receive client message: ***************");
                        // ��ȡ��һ��, �����ַ
                        String line = in.readLine();
                        System.out.println("line: " + line);
                        String resource = line.substring(line.indexOf('/') + 1, line.lastIndexOf('/') - 5);
                        //����������Դ�ĵ�ַ
                        resource = URLDecoder.decode(resource, "UTF-8");//������ URL ��ַ
                        String method = new StringTokenizer(line).nextElement().toString();// ��ȡ���󷽷�, GET ���� POST

                        // ��ȡ������������͹������������ͷ����Ϣ
                        while ((line = in.readLine()) != null) {
//                            System.out.println(line);
                            if (line.equals("")) break;  //����β��������
                        }

                        System.out.println("end of receiving ***************");
                        System.out.println("resource:" + resource);
                        System.out.println("method: " + method);

//                        walletDemo.query();

                        client.close();
                    } catch (Exception e) {
                        System.out.println("HTTP server error:" + e.getLocalizedMessage());
                    }
                }
                //System.out.println(client+"���ӵ�HTTP������");//���������һ��,��������Ӧ�ٶȻ����
            } catch (Exception e) {
                System.out.println("HTTP server error:" + e.getLocalizedMessage());
            }
        }
    }


    //�����д�ӡ��;˵��.
    private static void usage() {
        System.out.println("Usage: java HTTPServer <port> Default port is 80.");
    }

    /**
     * �������� HTTP ������
     */
    public static void main(String[] args) throws Exception {

        try {
            if (args.length != 1) {
                usage();
            } else if (args.length == 1) {
                PORT = Integer.parseInt(args[0]);
            }
        } catch (Exception ex) {
            System.err.println("Invalid port arguments. It must be a integer that greater than 0");
        }

        new SimpleHttpServer1();   //����һ��
    }
}