import java.io.Serializable;

/**
 * Created by KETKI on 11/7/2015.
 */

/*
This class creates a TCP Packet with Sequence number, acknowledgement number, checksum, Payload

 */
public class Packet implements Serializable {
    private int seqnumber;
    private int acknumber;
    private int checksum;
    private String payload;

    /*
    Constructor
     */
    public Packet(int seq, int ack, int check, String Payload) {
        seqnumber = seq;
        acknumber = ack;
        checksum = check;
        payload =Payload;
    }

    public Packet (int seq, int ack) {
        seqnumber = seq;
        acknumber = ack;
    }

    public boolean setSeqnum(int seq_n) {
        seqnumber = seq_n;
        return true;
    }

    public boolean setAcknum(int ack_n) {
        acknumber = ack_n;
        return true;
    }

    public boolean setChecksum(int chk_n) {
        checksum = chk_n;
        return true;
    }

    public boolean setPayload(String Payload) {
            payload = Payload;
            return true;
    }

    public int getSeqnum() {
        return seqnumber;
    }

    public int getAcknum() {
        return acknumber;
    }

    public int getChecksum() {
        return checksum;
    }

    public String getPayload() {
        return payload;
    }

    /*
    This is the method to send a response string
     */

    public String toString() {
        return("Sequence number:" + seqnumber + "," + "  Acknowledgement number:" + acknumber + "," + "   Checksum:" +
                checksum + "," + "  Payload: " + payload);
    }
}
