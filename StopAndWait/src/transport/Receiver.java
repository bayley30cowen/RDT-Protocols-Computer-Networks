 package transport;

public class Receiver extends NetworkHost {
     /*
     * Predefined Constant (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and Packet payload
     *
     *
     * Predefined Member Methods:
     *
     *  void startTimer(double increment):
     *       Starts a timer, which will expire in "increment" time units, causing the interrupt handler to be called.  You should only call this in the Sender class.
     *  void stopTimer():
     *       Stops the timer. You should only call this in the Sender class.
     *  void udtSend(Packet p)
     *       Sends the packet "p" into the network to arrive at other host
     *  void deliverData(String dataSent)
     *       Passes "dataSent" up to app layer. You should only call this in the Receiver class.
     *
     *  Predefined Classes:
     *
     *  NetworkSimulator: Implements the core functionality of the simulator
     *
     *  double getTime()
     *       Returns the current time in the simulator. Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().getTime()
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().printEventList()
     *
     *  Message: Used to encapsulate a message coming from the application layer
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      void setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *      String getData():
     *          returns the data contained in the message
     *
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet, which is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload):
     *          creates a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and a payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and an empty payload
     *    Methods:
     *      void setSeqnum(int seqnum)
     *          sets the Packet's sequence field to seqnum
     *      void setAcknum(int acknum)
     *          sets the Packet's ack field to acknum
     *      void setChecksum(int checksum)
     *          sets the Packet's checksum to checksum
     *      void setPayload(String payload) 
     *          sets the Packet's payload to payload
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */
    
    // Add any necessary class variables here. They can hold state information for the receiver.
    // Also add any necessary methods (e.g. checksum of a String)
    private final int ACK = 1; // Constants
    
    private int expectedNo; //Expected sequenve  number of packet, used to check if a duplicate has been recieved
    
    /* Private Methods */
    
    //Computes the sum of ASCII values of the characters in the packet payload.
    //Retruns the sum of the message characters ascii codes
    private int checkSum(String payload) {
        int total = 0;
        for (int i = 0; i < payload.length(); i++) {
            total += payload.charAt(i);
        }
        return total;
    }
    
    //Checks if the packet recieved (input) was corrupted during transmission. 
    //Done by comparing if the checksum value assigned is the same as checksum valeu calculated
    private boolean corruptCheck(Packet p) {
        return p.getChecksum() == p.getAcknum() + p.getSeqnum() + checkSum(p.getPayload());
        //Returns true if the packey has been corrupted
    }
    //Checks if the packet recieved has been recieved before (therefore a duplicate). 
    //Does this by comparing the expected sequence number with actual.
    private boolean duplicateCheck(Packet p) {
        return expectedNo != p.getSeqnum();
        //returns true only if packedt is a duplicate
    }
    
    
    // This is the constructor.  Don't touch!
    public Receiver(int entityName) {
        super(entityName);
    }

    
   
    // This method will be called once, before any of your other receiver-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the state of the receiver).
    @Override
    public void init() {
        expectedNo = 0;
    }

    // This method will be called whenever a packet sent from the sender(i.e. as a result of a udtSend() being called by the Sender ) arrives at the receiver. 
    // The argument "packet" is the (possibly corrupted) packet sent from the sender.
    @Override
    public void input(Packet packet) {
        //If tghe packet is either a duplicate or has been corrupted, send with incorrect seq number
        if (duplicateCheck(packet) || corruptCheck(packet)) {
            //modulo-2 ensures sequence number is a 1 or 0
            int sequenceNum = (expectedNo + 1) % 2;
            udtSend(new Packet(sequenceNum , ACK, (sequenceNum + ACK)));
        }
        //Otherwise if it isnt corrupt or a duplicate, deliver with correct ACK number.
        else {
            deliverData(packet.getPayload());
            udtSend(new Packet(expectedNo, ACK, (expectedNo + ACK)));
            //updates the expected sequence number.
            expectedNo = (expectedNo + 1) % 2; 
        }
    }

}