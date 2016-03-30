
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by KETKI on 10/27/2015.
 */

/*
This is the ideal server class which will receive without packet loss
 */
public class IdealServer extends Thread {

    private DatagramSocket server_sock;
    private int port ;
    private static int seq_n;
    private static int ack_n;
    private static String str;

    public IdealServer(String[] args) throws Exception{
        this.port = Integer.parseInt(args[1]);
        server_sock = new DatagramSocket(port);
    }

    public void run() {
        try{
            byte[] receive = new byte[1024];
            System.out.println("Server started..");
            while (true) {
                DatagramPacket receive_packet = new DatagramPacket(receive, 0,receive.length);
                server_sock.receive(receive_packet);

                byte[] packet = receive_packet.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(packet);
                ObjectInputStream ois = new ObjectInputStream(in);

                try {
                    Packet object = (Packet)ois.readObject();
                    System.out.println();
                    String s = object.toString();
                    if((object.getChecksum() ^ 1) != 0){
                        System.out.println("Checksum failed!!!");
                    } else {
                        System.out.println("Checksum passed!!!");
                    }

                    System.out.println("Packet received from client "+"\n" + object);
                    System.out.println();
                    //parsing the packet and retrieving sequence and ack number
                    String number[] = s.split(":|,");
                    String seq = number[1];
                    String ack = number[3];

                    System.out.println("Packet with SEQ: " +seq + "  ACK: "+ack);

                    seq_n = Integer.parseInt(seq);
                    ack_n = Integer.parseInt(ack);

                    InetAddress IP = receive_packet.getAddress();
                    port = receive_packet.getPort();
                    ack_n++;
                    String response = "I got the packet with Sequence number:" + seq_n + ", Acknowledgment number:" + ack_n;
                    byte[] reply = response.getBytes("UTF-8");
                    DatagramPacket reply_packet = new DatagramPacket(reply, reply.length, IP, port);
                    server_sock.send(reply_packet);


                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
