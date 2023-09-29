package exemple;

import gridsim.*;
import gridsim.net.*;
import eduni.simjava.*;
import java.util.*;


/**
 * This class basically sends one or more messages to the other
 * entity over a link. Then, it waits for an ack.
 * Finally, before finishing the simulation, it pings the other
 * entity.
 */
public class NetUser extends GridSim
{
    private int myID_;          // my entity ID
    private String name_;       // my entity name
    private String destName_;   // destination name
    private int destID_;        // destination id
    Scanner scanner = new Scanner(System.in);
   
    /** Custom tag that denotes sending a message */
    public static final int SEND_MSG = 1;


    /**
     * Creates a new NetUser object
     * @param name      this entity name
     * @param destName  the destination entity's name
     * @param link      the physical link that connects this entity to destName
     * @throws Exception    This happens when name is null or haven't
     *                      initialized GridSim.
     */
    public NetUser(String name, String destName, Link link) throws Exception
    {
        super(name, link);

        // get this entity name from Sim_entity
        this.name_ = super.get_name();

        // get this entity ID from Sim_entity
        this.myID_ = super.get_id();

        // get the destination entity name
        destName_ = destName;
    }

    /**
     * The core method that handles communications among GridSim entities.
     */
    public void body()
    {
        int packetSize = 500;   // packet size in bytes
        int size = 1;           // number of packets sent
        int i = 0;

        // get the destination entity ID
        this.destID_ = GridSim.getEntityId(destName_);
    int n;
    System.out.println("donnez la taille de matrice");
    n = scanner.nextInt();
   
    int[][] matrice = implementMatrice(n);
    int[] vecteur = implementVecteur(n);
   
    IO_data matriceData = new IO_data(matrice, packetSize, destID_);
        super.send(super.output, GridSimTags.SCHEDULE_NOW,
                   NetUser.SEND_MSG, matriceData);
        ////////////////////////////////////////////////////////
   
       Object message = null;
       for (i = 0; i <= n; i++)
       {
           // waiting for incoming event in the Input buffer
           message = super.receiveEventObject();
           if(message == "start") {
          startCalcule(matrice,vecteur,n,packetSize);  
           }else {
          System.out.println(message);
           }
       }    
        ////////////////////////////////////////////////////////
        InfoPacket pkt = null;


        pkt = super.pingBlockingCall(destID_, size);

        // print the result
        System.out.println("\n-------- " + name_ + " ----------------");
        System.out.println(pkt);
        System.out.println("-------- " + name_ + " ----------------\n");

        ////////////////////////////////////////////////////////
        // sends back denoting end of simulation
        super.send(destID_, GridSimTags.SCHEDULE_NOW,
                   GridSimTags.END_OF_SIMULATION);

        ////////////////////////////////////////////////////////
        // shut down I/O ports
        shutdownUserEntity();
        terminateIOEntities();

        System.out.println(this.name_ + ":%%%% Exiting body() at time " +
                           GridSim.clock() );
    }

private void startCalcule(int[][] matrice, int[] vecteur,int n,int packetSize) {
// TODO Auto-generated method stub

            for(int i = 0; i < n;i++) {
            int[][] matCopy = matriceCopy(matrice);
                    for(int j = 0;j < vecteur.length;j++) {
                    matCopy[j][i] = vecteur[j];
                    }
                    IO_data data = new IO_data(matCopy, packetSize, destID_);
                    //sends through Output buffer of this entity
                    super.send(super.output, GridSimTags.SCHEDULE_NOW,
                             NetUser.SEND_MSG, data);
            }
}

private int[][] matriceCopy(int[][] matrice) {
// TODO Auto-generated method stub
int[][] matCopy =  new int[matrice.length][matrice.length];
        for (int i = 0; i < matrice.length; i++) {
            for (int j = 0; j < matrice.length; j++) {
            matCopy[i][j] = matrice[i][j];
            }
       }
return matCopy;
}

private int[] implementVecteur(int n) {
// TODO Auto-generated method stub
int[] vecteur = new int[n];
    for(int c = 0;c < n;c++) {
System.out.println("donnez vecteur[" + c + "] :");
vecteur[c] = scanner.nextInt();
    }
return vecteur;
}

private int[][] implementMatrice(int n) {
// TODO Auto-generated method stub
int[][] matrice = new int[n][n];
    for(int c = 0;c < n;c++) {
    for(int j = 0;j < n;j++) {
    System.out.println("donnez matrice[" + c + "][" + j + "]: ");
    matrice[c][j] = scanner.nextInt();
    }
    }
return matrice;
}

} // end class
