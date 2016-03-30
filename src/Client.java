import org.omg.CORBA.*;
import org.omg.CORBA.Object;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by KETKI on 10/27/2015.
 *
 */


/*
This class creates a Client to send packets to the server and receiver acknowledgements
 */
public class Client extends Thread{

    private DatagramSocket client_sock;
    private int port;
    byte[] receive = new byte[1024];
    private static int from = 0;
    private static int chunksize = 512;
    private static Packet packet;
    private static ArrayList<Packet> packets;
    private static InetAddress host;
    private static String add;

    private static int ack_n = 0;
    private static int seq_n = 0;
    private static int y;
    private static int k;
    private static int dup_ack=0;
    private static int cwnd;
    private static int sshthresh=0;
    private static String filename;

    ReceiveListener receives;

    /*
    This is the constructor
     */
    public Client(String [] args) throws Exception{
        this.filename = args[3];
        this.port = Integer.parseInt(args[4]);
        this.add = args[5];
        client_sock = new DatagramSocket();
    }

    /*
    This method reads the sample text file to send to the server. It further divides this file
    into chunks i.e small packets of 512 bytes each and places each of these chunks in a data structure.
     */

    public static void Packetlist() {
        try {
            host = InetAddress.getByName(add);

            FileInputStream fileInputStream = null;
            File file = new File(filename);
            byte[] bFile = new byte[(int) file.length()];

            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();

            byte[][] chunk = new byte[(int) Math.ceil(bFile.length / (double) chunksize)][chunksize];
            packets = new ArrayList<Packet>();

            for (int j = 0; j < chunk.length; j++) {
                if(from + chunksize > bFile.length){
                    int difference = ((from+chunksize)- bFile.length);
                    chunk[j] = Arrays.copyOfRange(bFile, 0, difference);
                    String s = new String(chunk[j]);
                    packet = new Packet(seq_n, ack_n, 3, s);
                    packets.add(packet);
                    seq_n++;
                    ack_n++;
                }
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
/*
The run method of Client thread
 */
    public void run() {
        send_packet();
    }

    /*
    This method is used to send packets to the server and it starts the listener thread which listens
    for acknowledgements from the server. Here we increase the congestion window size every time till we detect a packet loss.
    Further we follow the slow start mechanism and fast retransmit.
     */
    public synchronized void send_packet() {
        try{
            receives = new ReceiveListener();
            receives.t.start();

            // cwnd is the window size
            y = 0;
            for (cwnd = 1; cwnd <= packets.size();) {
                for (k = y; k < (y + cwnd); k++) {
                    if (k > packets.size() - 1) {
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
                receives.t.join(1000);
                y = cwnd + y;
                if(y > packets.size()) {
                    break;
                }
                if (sshthresh == cwnd) {
                    System.out.println("sshthresh = c");
                    cwnd =cwnd+(1/cwnd);
                    y=cwnd+y;
                }
                else{
                    cwnd++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    This class receives for the acknowledgements from the server on a different thread to avoid conflicts. It displays the
    response from the server and we know whether the packet is received or lost in the network.
     */

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
                    String number[] = response.split(":|,");
                    String seq = number[1];
                    String ack = number[3];
                    seq_n = Integer.parseInt(seq);
                    ack_n = Integer.parseInt(ack);

                    if(seq_n > ack_n) {
                        dup_ack++;
                        if(dup_ack ==3) {
                            System.out.println("PACKET LOST, three duplicate acks received!!!!");
                            Retransmit r = new Retransmit();
                            r.retransmit.start();
                        }

                        else{
                            System.out.println("PACKET LOST due to extensive timeout !!!!");
                            Retransmit r = new Retransmit();
                            r.retransmit.start();
                        }
                    }

                        System.out.println("Response from server: " + response);

                        System.out.println("SEQ: " + seq + "  ACK: " + ack);
                        System.out.println();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    This class is used to retransmit a packet which was lost. It uses a different thread to retransmit. It also detects
    Duplicate acks or extensive timeout and retransmits accordingly. Here we set the sshthresh value.
     */

    class Retransmit extends Thread {

        Thread retransmit;

        public Retransmit() {
            retransmit = new Thread(this);
        }

        @Override
        public synchronized void run () {
            try {
                System.out.println("Retransmitting the lost packet ");
                System.out.println();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);

                oos.writeObject(packets.get(4));
                System.out.println("Packet sent from client, Packet 4");

                byte[] send = outputStream.toByteArray();
                DatagramPacket send_pack = new DatagramPacket(send, send.length, host, port);
                client_sock.send(send_pack);
                sshthresh = cwnd / 2;
                cwnd = 1;

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

