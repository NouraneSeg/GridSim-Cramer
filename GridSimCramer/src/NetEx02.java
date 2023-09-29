
// this is the Main Class for our project

import gridsim.*;
import gridsim.net.*;
import java.util.*;

public class NetEx02
{
    public static void main(String[] args)
    {
    	
    	System.out.println("╔═══════════════════════════════════════════════════════════════════════════════════════╗");
    	System.out.println("║ Welcome to our distributed computing solution to solving Cramer Systems using GridSim ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════════════╝");

        ////////////////////// recuperation des informations du systeme a resoudre //////////////////////////////////////
        Scanner sc = new Scanner(System.in);
        
        System.out.println("please enter the size of your coefficient matrix A");
        int sizeMatrix = sc.nextInt();
        int[][] matrixA = new int[sizeMatrix][sizeMatrix]; // creation de la matrice A
        
        System.out.println("please enter the values of your coefficient matrix A");
        for (int i = 0; i < sizeMatrix; i++) {
        	for (int j = 0; j < sizeMatrix; j++) {
        		matrixA[i][j] = sc.nextInt();}}
        
        System.out.println("please enter the values of the constants column B");
        int[] columnB = new int[sizeMatrix];
        for (int i = 0; i < sizeMatrix; i++) {
        	columnB[i] = sc.nextInt();}
        
        sc.close();
        
        System.out.println("Starting to solve the Cramer System ...");
        
        try
        {
            ////////////////////// Initializing the GridSim package ///////////////////////////////////////////////////
        	
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = true; // a flag that denotes whether to trace GridSim events or not.
            System.out.println("Initializing GridSim package");
            GridSim.init(num_user, calendar, trace_flag); // Initialize the GridSim package

            ////////////////////// Creating GridResource entities //////////////////////////////////////////////////////
           
            double baud_rate = 1000; // bits/sec
            double propDelay = 10;   // propagation delay in millisecond
            int mtu = 1500;          // max. transmission unit in byte
            int totalResource = 10; // number of ressources
            
            ArrayList resList = new ArrayList(totalResource); //creating list of ressources
            for (int i = 0; i < totalResource; i++)
            {
                GridResource res = createGridResource("Res_"+i, baud_rate,propDelay, mtu); //creating a ressource
                resList.add(res); // add a resource into a list
            }

            //////////////////////////// Creating grid user entities ///////////////////////////////////////////////////

            int totalGridlet = sizeMatrix+1; // number of Gridlets that will be sent to the resource

            ArrayList userList = new ArrayList(num_user); // create list of users
            for (int i = 0; i < num_user; i++)
            {
                NetUser user = new NetUser(matrixA, columnB, "User_"+i, totalGridlet, baud_rate,propDelay, mtu, trace_flag); //create a user
                userList.add(user); // add a user into a list
            }

            //////////////////////// Building the network topology among entities ///////////////////////////////////////
            
            // the topology is: user(s) --1Mb/s-- r1 --10Mb/s-- r2 --1Mb/s-- GridResource(s)

            Router r1 = new RIPRouter("router1", trace_flag);   // router 1
            Router r2 = new RIPRouter("router2", trace_flag);   // router 2

            // connect all user entities with r1 with 1Mb/s connection
            NetUser obj = null;
            for (int i = 0; i < userList.size(); i++)
            {
                FIFOScheduler userSched = new FIFOScheduler("NetUserSched_"+i);
                obj = (NetUser) userList.get(i);
                r1.attachHost(obj, userSched);
            }

            // connect all resource entities with r2 with 1Mb/s connection
            GridResource resObj = null;
            for (int i = 0; i < resList.size(); i++)
            {
                FIFOScheduler resSched = new FIFOScheduler("GridResSched_"+i);
                resObj = (GridResource) resList.get(i);
                r2.attachHost(resObj, resSched);
            }

            // then connect r1 to r2 with 10Mb/s connection
            baud_rate = 10000;
            Link link = new SimpleLink("r1_r2_link", baud_rate, propDelay, mtu);
            FIFOScheduler r1Sched = new FIFOScheduler("r1_Sched");
            FIFOScheduler r2Sched = new FIFOScheduler("r2_Sched");

            r1.attachRouter(r2, link, r1Sched, r2Sched); // attach r2 to r1

            ////////////////////////////// Starting and ending the simulation //////////////////////////////////////////////////
            GridSim.startGridSimulation();
            
            // print the result

            // print the routing tables
            r1.printRoutingTable();
            r2.printRoutingTable();

            // Print the Gridlets when simulation is over
            GridletList glList = null;
            for (int i = 0; i < userList.size(); i++)
            {
                obj = (NetUser) userList.get(i);
                glList = obj.getGridletList();
                printGridletList(glList, obj.get_name(), false);
            }

            System.out.println("\nFinished solving the system ...");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Unwanted errors happened");
        }
    }

    //////////////////////////// creating a grid ressource /////////////////////////////////////////////////////////
    private static GridResource createGridResource(String name,double baud_rate, double delay, int MTU)
    {

        MachineList mList = new MachineList(); // creating the machines list
        int mipsRating = 377;
        mList.add( new Machine(0, 4, mipsRating));   // creating and adding the first Machine to the list
        mList.add( new Machine(1, 4, mipsRating));   // Second Machine
        mList.add( new Machine(2, 2, mipsRating));   // Third Machine

        String arch = "Sun Ultra";     
        String os = "Solaris";       
        double time_zone = 9.0;        
        double cost = 3.0;       
        // Creating a ResourceCharacteristics object that stores properties of a Grid resource
        ResourceCharacteristics resConfig = new ResourceCharacteristics( 
        		arch, os, mList, ResourceCharacteristics.TIME_SHARED,time_zone, cost); 

        long seed = 11L*13*17*19*23+1;
        double peakLoad = 0.0;     
        double offPeakLoad = 0.0;    
        double holidayLoad = 0.0;     
        LinkedList Weekends = new LinkedList();
        Weekends.add(new Integer(Calendar.SATURDAY));
        Weekends.add(new Integer(Calendar.SUNDAY));
        LinkedList Holidays = new LinkedList();
        
        // creating a GridResource object
        GridResource gridRes = null;
        try
        {
            // creates a GridResource with a link
            gridRes = new GridResource(name,
                new SimpleLink(name + "_link", baud_rate, delay, MTU),
                seed, resConfig, peakLoad, offPeakLoad, holidayLoad,
                Weekends, Holidays);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("creating one Grid resource (name: " + name +
                " - id: " + gridRes.get_id() + ")");

        return gridRes;
    }

    ////////////////////////////////// Printing the Gridlet objects //////////////////////////////////////////////////
    private static void printGridletList(GridletList list, String name,boolean detail)
    {
        int size = list.size();
        Gridlet gridlet = null;

        String indent = "    ";
        System.out.println();
        System.out.println("============= OUTPUT for " + name + " ==========");
        System.out.println("Gridlet ID" + indent + "STATUS" + indent + "Resource ID" + indent + "Cost");

        // a loop to print the overall result
        int i = 0;
        for (i = 0; i < size; i++)
        {
            gridlet = (Gridlet) list.get(i);
            System.out.print(indent + gridlet.getGridletID() + indent
                    + indent);

            System.out.print( gridlet.getGridletStatusString() );

            System.out.println( indent + indent + gridlet.getResourceID() +
                    indent + indent + gridlet.getProcessingCost() );
        }

        if (detail == true)
        {
            // a loop to print each Gridlet's history
            for (i = 0; i < size; i++)
            {
                gridlet = (Gridlet) list.get(i);
                System.out.println( gridlet.getGridletHistory() );

                System.out.print("Gridlet #" + gridlet.getGridletID() );
                System.out.println(", length = " + gridlet.getGridletLength()
                        + ", finished so far = " +
                        gridlet.getGridletFinishedSoFar() );
                System.out.println("======================================\n");
            }
        }
    }

} // end of class

