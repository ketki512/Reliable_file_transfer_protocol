
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 * Created by KETKI on 10/27/2015.
 */


/*
This is the server class which receives the packets and displays them. The server has one thread for listening to the client packets
 */
public class Server extends Thread {

    private DatagramSocket server_sock;
    private static int port ;
    private static int seq_n;
    private static int ack_n;
    private static boolean bool = true;
    private static boolean bool_1 = true;
    private static ArrayList<Packet> packet_server= new ArrayList<Packet>();
    private static String str;


    /*
    This is the constructor
     */

    public Server(String[] args) throws Exception{
        this.port = Integer.parseInt(args[2]);
        server_sock = new DatagramSocket(port);
    }

    /*
    This method detects packet loss and handles it.
     */
    public void run() {
            try {
                byte[] receive = new byte[1024];
                System.out.println("Server started..");
                while (true) {
                    DatagramPacket receive_packet = new DatagramPacket(receive, 0, receive.length);
                    server_sock.receive(receive_packet);

                    byte[] packet = receive_packet.getData();
                    ByteArrayInputStream in = new ByteArrayInputStream(packet);
                    ObjectInputStream ois = new ObjectInputStream(in);

                    try {
                            Packet object = (Packet) ois.readObject();
                            System.out.println();
                            packet_server.add(object);
                            String s = object.toString();
                            if((object.getChecksum() ^ 1) != 0){
                                System.out.println("Checksum failed!!!");
                            } else {
                                System.out.println("Checksum passed!!!");
                            }

                            System.out.println("Packet received from client " + "\n" + object);
                            System.out.println();
                            //parsing the packet and retrieving sequence and ack number
                            String number[] = s.split(":|,");
                            String seq = number[1];
                            String ack = number[3];

                            System.out.println("Packet with SEQ: " + seq + "  ACK: " + ack);

                            seq_n = Integer.parseInt(seq);
                            ack_n = Integer.parseInt(ack);

                            InetAddress IP = receive_packet.getAddress();
                            port = receive_packet.getPort();
                            if (seq_n == 4 && bool == true) {
                                try {
                                    for (int m = seq_n + 1; m <= packet_server.size(); m++) {
                                        String lost = "I got the packet with Sequence number:" + m + ", Acknowledgment number:" + ack_n;
                                        byte[] reply = lost.getBytes("UTF-8");
                                        DatagramPacket reply_lost = new DatagramPacket(reply, reply.length, IP, port);
                                        server_sock.send(reply_lost);
                                        bool = false;
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (seq_n != 5) {
                                try {
                                    if (seq_n == 4 && bool_1 == true) {
                                        ack_n++;
                                        bool_1 = false;
                                    }
                                    ack_n++;
                                    String response = "I got the packet with Sequence number:" + seq_n + ", Acknowledgment number:" + ack_n;
                                    byte[] reply = response.getBytes("UTF-8");
                                    DatagramPacket reply_packet = new DatagramPacket(reply, reply.length, IP, port);
                                    server_sock.send(reply_packet);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                ois.close();
                            }
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }

                    }
                }
                catch(Exception e){
                    e.printStackTrace();

                }
    }

}
