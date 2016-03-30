import org.omg.CORBA.*;
import org.omg.CORBA.Object;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by KETKI on 10/27/2015.
 *
 */

/*
This is the ideal client class which will transfer packets equal to window size
 */
public class IdealClient extends Thread{

    private DatagramSocket client_sock;
    private int port;
    byte[] receive = new byte[1024];
    private static int from = 0;
    private static int chunksize = 700;
    private static Packet packet;
    private static ArrayList<Packet> packets;
    private static InetAddress host;
    private static String filename;
    private static String add;

    private static int ack_n = 0;
    private static int seq_n = 0;
    private static int y;


    public IdealClient(String[] args) throws Exception{
        this.filename = args[2];
        this.port = Integer.parseInt(args[3]);
        this.add = args[4];
        client_sock = new DatagramSocket();
    }

    public static void Packetlist() {
        try {
            host = InetAddress.getByName("localhost");

            FileInputStream fileInputStream = null;
            File file = new File(filename);
            byte[] bFile = new byte[(int) file.length()];

            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();

            byte[][] chunk = new byte[(int) Math.ceil(bFile.length / (double) chunksize)][chunksize];
            packets = new ArrayList<Packet>();
            for (int j = 0; j < chunk.length; j++) {
                chunk[j] = Arrays.copyOfRange(bFile, from, from + chunksize);
                from += chunksize;
                String s = new String(chunk[j]);
                packet = new Packet(seq_n, ack_n, 3, s);
                packets.add(packet);
                seq_n++;
                ack_n++;

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            ReceiveListener receive = new ReceiveListener();
            receive.t.start();

            // c is the window size
            y=0;
            for (int c = 1; c <= packets.size(); c++) {
                for (int k = y; k < y+c; k++) {
                    if(k > packets.size()-1){
                        break;
                    }
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(outputStream);

                    oos.writeObject(packets.get(k));

                    byte[] send = outputStream.toByteArray();
                    DatagramPacket send_pack = new DatagramPacket(send, send.length, host, port);
                    client_sock.send(send_pack);

                    System.out.println("Packet sent from client, Packet " + k);
                }
                System.out.println();
                receive.t.join(1000);
                y = c+y;
                if(y > packets.size()) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ReceiveListener extends Thread {

        Thread t;

        public ReceiveListener () {
            t = new Thread(this);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    DatagramPacket receive_packet = new DatagramPacket(receive, 0, receive.length);
                    client_sock.receive(receive_packet);

                    String response = new String(receive_packet.getData(), 0, receive_packet.getLength(), "UTF-8");
                    System.out.println("Response from server: " + response);

                    String number[] = response.split(":|,");
                    String seq = number[1];
                    String ack = number[3];

                    System.out.println("SEQ: " + seq + "  ACK: " + ack);
                    System.out.println();

                    seq_n = Integer.parseInt(seq);
                    ack_n = Integer.parseInt(ack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

