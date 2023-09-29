package exemple;

import java.util.*;
import gridsim.*;
import gridsim.net.*;
import gridsim.util.SimReport;
import eduni.simjava.*;


/**
 * This class handles incoming requests and sends back an ack.
 * In addition, this class logs every activities.
 */
public class Test extends GridSim
{
    private int myID_;          // my entity ID
    private String name_;       // my entity name
    private String destName_;   // destination name
    private int destID_;        // destination id
    private SimReport report_;  // logs every activity

    /**
     * Creates a new NetUser object
     * @param name      this entity name
     * @param destName  the destination entity's name
     * @param link      the physical link that connects this entity to destName
     * @throws Exception    This happens when name is null or haven't
     *                      initialized GridSim.
     */
    public Test(String name, String destName, Link link) throws Exception
    {
        super(name, link);

        // get this entity name from Sim_entity
        this.name_ = super.get_name();

        // get this entity ID from Sim_entity
        this.myID_ = super.get_id();

        // get the destination entity name
        this.destName_ = destName;
       
        // logs every activity. It will automatically create name.csv file
        report_ = new SimReport(name);
        report_.write("Creates " + name);
    }

    /**
     * The core method that handles communications among GridSim entities.
     */
    public void body()
    {
        // get the destination entity ID
        this.destID_ = GridSim.getEntityId(destName_);
        //Integer det = null;
        int [][] tableau = null;
        boolean status = false;
        int packetSize = 500;   // packet size in bytes
        Sim_event ev = new Sim_event();     // an event
       
        // a loop waiting for incoming events
        while ( Sim_system.running() )
        {
            // get the next event from the Input buffer
            super.sim_get_next(ev);
           
            // if an event denotes end of simulation
            if (ev.get_tag() == GridSimTags.END_OF_SIMULATION)
            {
                System.out.println();
                write(super.get_name() + ".body(): exiting ...");
                break;
            }
           
            // if an event denotes another event type
            else if (ev.get_tag() == NetUser.SEND_MSG)
            {
               
            if(status == false) {
            tableau = (int[][]) ev.get_data();
                    // sends back an ack
                    IO_data data = new IO_data("start", packetSize, destID_);
                    // sends through Output buffer of this entity
                    super.send(super.output, GridSimTags.SCHEDULE_NOW,
                               NetUser.SEND_MSG, data);
                    status = true;
            //System.out.print(tableau);
            }else {
            int[][] tab = (int[][]) ev.get_data();
            float result = determinant(tab) / determinant(tableau);
            IO_data data = new IO_data(result, packetSize, destID_);
                    // sends through Output buffer of this entity
                    super.send(super.output, GridSimTags.SCHEDULE_NOW,
                               NetUser.SEND_MSG, data);
            //System.out.println(result);
            }
            }
           
            // handle a ping requests. You need to write the below code
            // for every class that extends from GridSim or GridSimCore.
            // Otherwise, the ping functionality is not working.
            else if (ev.get_tag() ==  GridSimTags.INFOPKT_SUBMIT)
            {
                processPingRequest(ev);                
            }
        }

        ////////////////////////////////////////////////////////
        // shut down I/O ports
        shutdownUserEntity();
        terminateIOEntities();

        // don't forget to close the file
        if (report_ != null) {
            report_.finalWrite();
        }
       
        System.out.println(this.name_ + ":%%%% Exiting body() at time " +
                           GridSim.clock() );
    }
    public static int determinant(int[][] matrix) {
        int n = matrix.length;
        int det = 0;
       
        if (n == 1) {
            return matrix[0][0];
        } else if (n == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        } else {
            for (int i = 0; i < n; i++) {
                int[][] subMatrix = new int[n - 1][n - 1];
                for (int j = 1; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        if (k < i) {
                            subMatrix[j - 1][k] = matrix[j][k];
                        } else if (k > i) {
                            subMatrix[j - 1][k - 1] = matrix[j][k];
                        }
                    }
                }
                det += matrix[0][i] * Math.pow(-1, i) * determinant(subMatrix);
            }
        }
        return det;
    }
    /**
     * Handles ping request
     * @param ev    a Sim_event object
     */
    private void processPingRequest(Sim_event ev)
    {
        InfoPacket pkt = (InfoPacket) ev.get_data();
        pkt.setTag(GridSimTags.INFOPKT_RETURN);
        pkt.setDestID( pkt.getSrcID() );

        // sends back to the sender
        super.send(super.output, GridSimTags.SCHEDULE_NOW,
                   GridSimTags.INFOPKT_RETURN,
                   new IO_data(pkt,pkt.getSize(),pkt.getSrcID()) );
    }
   
    /**
     * Prints out the given message into stdout.
     * In addition, writes it into a file.
     * @param msg   a message
     */
    private void write(String msg)
    {
        System.out.println(msg);
        if (report_ != null) {
            report_.write(msg);
        }        
    }
   
} // end class

