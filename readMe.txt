Directory structure:
--> /kt5829/src : <--
-fcntcp.java
-Packet.java
-Server.java
-Client.java
-IdealClient.java
-IdealServer.java

--> /kt5829/doc : <--
-Documentation.docx

--> /kt5829/bld : <--
fcntcp.class
Server.class
Client.class
Packet.class
Cilent$Retransmit.class
Client$ReceiveListener.class
IdealClient$ReceiveListener.class
IdealClient.class
IdealServer.class

Checksum is implemented using CRC32 bu doing XOR of the bits in the header of each packet.

