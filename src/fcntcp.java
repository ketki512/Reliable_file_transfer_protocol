/**
 * Created by KETKI on 11/14/2015.
 */

/*
This is the main class to run the program client and ServerLossy.
 */
    public class fcntcp {
        public static void main (String args[]) {
            try {
                if(args[0].equals("-l") && args[1].equals("-c") && args[2].equals("-f")) {
                    Client client = new Client(args);
                    client.Packetlist();
                    Thread thread = new Client(args);
                    new Thread(thread).start();
                }
                else if(args[0].equals("-l") && args[1].equals("-s")) {
                    Thread thread = new Server(args);
                    new Thread(thread).start();
                }

                else if(args[0].equals("-c") && args[1].equals("-f")) {
                    IdealClient cli = new IdealClient(args);
                    cli.Packetlist();
                    Thread thread = new IdealClient(args);
                    new Thread(thread).start();
                    }
                else if (args[0].equals("-s")) {
                    Thread thread = new IdealServer(args);
                    new Thread(thread).start();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

