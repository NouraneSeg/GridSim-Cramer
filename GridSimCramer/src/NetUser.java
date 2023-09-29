
// This class basically creates Gridlets and submits them to particular GridResources in the network topology.

import java.util.*;
import gridsim.*;
import gridsim.net.*;
import gridsim.util.SimReport;

class NetUser extends GridSim
{
    private int myId_;      // my entity ID
    private String name_;   // my entity name
    private GridletList list_;          // list of submitted Gridlets
    private GridletList receiveList_;   // list of received Gridlets
    private SimReport report_;  // logs every events

    NetUser(int[][] A, int[] B,String name, int totalGridlet, double baud_rate, double delay, int MTU, boolean trace_flag) throws Exception
    {
        super( name, new SimpleLink(name+"_link",baud_rate,delay, MTU) );

        this.name_ = name;
        this.receiveList_ = new GridletList();
        this.list_ = new GridletList();

        // creates a report file
        if (trace_flag == true) {
            report_ = new SimReport(name);
        }

        // Gets an ID for this entity
        this.myId_ = super.getEntityId(name);
        write("Creating a grid user entity with name = " + name + ", and id = " + this.myId_);

        // Creates a list of Gridlets or Tasks for this grid user
        write(name + ":Creating " + totalGridlet +" Gridlets");
        this.createGridlet(myId_, totalGridlet, A, B);
    }
    
    List<Thread> threads = new ArrayList<>();
    Thread thread1;
    private int detA; 
    int i = 0 ;
    int[] X ;
    
    ///// The core method that handles communications among GridSim entities
    public void body()
    {
        // wait for a little while for about 3 seconds.
        // This to give a time for GridResource entities to register their
        // services to GIS (GridInformationService) entity.
        super.gridSimHold(3.0);
        LinkedList resList = super.getGridResourceList();

        // initialises all the containers
        int totalResource = resList.size();
        int resourceID[] = new int[totalResource];
        String resourceName[] = new String[totalResource];

        // a loop to get all the resources available
        int i = 0;
        for (i = 0; i < totalResource; i++)
        {
            // Resource list contains list of resource IDs
            resourceID[i] = ( (Integer) resList.get(i) ).intValue();

            // get their names as well
            resourceName[i] = GridSim.getEntityName( resourceID[i] );
        }

        ////////////////////////////////////////////////
        // SUBMIT Gridlets

        // determines which GridResource to send to
        int index = i;
        if (index >= totalResource) {
            index = 0;
        }

        // sends all the Gridlets
        Gridlet gl = null;
        boolean success;
        for (i = 0; i < threads.size(); i++)
        {
            gl = (Gridlet) list_.get(i);
            write(name_ + "Sending Gridlet #" + i + " to " + resourceName[i]);

            // For even number of Gridlets, send without an acknowledgement
            // whether a resource has received them or not.

            // Start all threads
            Thread thread = threads.get(i);
            thread.start();
            
                // by default - send without an ack
                success = super.gridletSubmit(gl, resourceID[i]);

            // For odd number of Gridlets, send with an acknowledgement
        }
        for (Thread thread : threads) {
            try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    	System.out.println("╔═══════════════════════════════════════════════════════════════════════════════════════╗");
    	System.out.println("║               le vecteur des solutions X = "+Arrays.toString(X)+"                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════════════╝");

        ////////////////////////////////////////////////////////
        // RECEIVES Gridlets back

        // hold for few period - few seconds since the Gridlets length are
        // quite huge for a small bandwidth
        super.gridSimHold(5);

        // receives the gridlet back
        for (i = 0; i < list_.size(); i++)
        {
            gl = (Gridlet) super.receiveEventObject();  // gets the Gridlet
            receiveList_.add(gl);   // add into the received list

            write(name_ + ": Receiving Gridlet #" +
                  gl.getGridletID() + " at time = " + GridSim.clock() );
        }

        ////////////////////////////////////////////////////////
        // ping functionality
        InfoPacket pkt = null;
        int size = 500;

        // There are 2 ways to ping an entity:
        // a. non-blocking call, i.e.
        //super.ping(resourceID[index], size);    // (i)   ping
        //super.gridSimHold(10);        // (ii)  do something else
        //pkt = super.getPingResult();  // (iii) get the result back

        // b. blocking call, i.e. ping and wait for a result
        pkt = super.pingBlockingCall(resourceID[index], size);

        // print the result
        write("\n-------- " + name_ + " ----------------");
        write(pkt.toString());
        write("-------- " + name_ + " ----------------\n");

        ////////////////////////////////////////////////////////
        // shut down I/O ports
        shutdownUserEntity();
        terminateIOEntities();

        // don't forget to close the file
        if (report_ != null) {
            report_.finalWrite();
        }

        write(this.name_ + ": sending and receiving of Gridlets" +
              " complete at " + GridSim.clock() );
    }

    /**
     * Gets a list of received Gridlets
     * @return a list of received/completed Gridlets
     */
    public GridletList getGridletList() {
        return receiveList_;
    }

    /**
     * This method will show you how to create Gridlets
     * @param userID        owner ID of a Gridlet
     * @param numGridlet    number of Gridlet to be created
     * @throws InterruptedException 
     */

    
    private void createGridlet(int userID, int numGridlet, int[][] A, int[] B) throws InterruptedException
    {
        int data = 5000;
        int compteurGridlet = 0;
        
            // Creates a Gridlet
            Gridlet g = new Gridlet(compteurGridlet, data, data, data);
            Runnable taskg = new Runnable() {
                @Override
            	public void run() {
                    detA = Determinant(A);
                    System.out.println("########## detA"+detA+" ##########");
            	}};
            compteurGridlet++;
            g.setUserID(userID);
            this.list_.add(g);
            
            thread1 = new Thread(taskg);
            threads.add(thread1);
            	
            	// Create a list of tasks
                List<Runnable> tasks = new ArrayList<>();
                
                // Add tasks to the list dynamically
                int n = A.length;
                X = new int[n];
                i=0;
                for(i=0;i<n;i++) {
                	Gridlet g1 = new Gridlet(compteurGridlet, data, data, data);
                    final int taskId = i;
                    tasks.add(new Runnable() {
                        @Override
                        public void run() {
                                    int[][] Ai = new int[n][n];
                                    for (int j = 0; j < n; j++) {
                                        for (int k = 0; k < n; k++) {
                                            if (k == taskId) {
                                                Ai[j][k] = B[j];
                                            } else {
                                                Ai[j][k] = A[j][k];
                                            }
                                        }
                                    }
                                    
                                    int detAi = Determinant(Ai);
                                    System.out.println("########## detAi"+detAi+" ##########");
									X[taskId] = detAi / detA;
                                    System.out.println("########## X"+taskId+"="+X[taskId]+" ##########");
                                
                            }
                    });
                    compteurGridlet++;
                    g1.setUserID(userID);
                    this.list_.add(g1);
                }
                
                // Create threads for all tasks
                for (Runnable task : tasks) {
                    Thread thread = new Thread(task);
                    threads.add(thread);
                }
               
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
    
    public static int Determinant(int[][] matrix) {
    	int n = matrix.length;
        int determinant = 0;
        
        if (n == 1) {
            determinant = matrix[0][0];
        } else if (n == 2) {
            determinant = (matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0]);
        } else {
            for (int i = 0; i < n; i++) {
                int[][] subMatrix = new int[n-1][n-1];
                
                for (int j = 1; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        if (k < i) {
                            subMatrix[j-1][k] = matrix[j][k];
                        } else if (k > i) {
                            subMatrix[j-1][k-1] = matrix[j][k];
                        }
                    }
                }
                
                determinant += matrix[0][i] * Math.pow(-1, i) * Determinant(subMatrix);
            }
        }
        
        return determinant;
    }
    
    public void MatricePrinter(int[][] matrice) {
            for (int i = 0; i < matrice.length; i++) {
                for (int j = 0; j < matrice[i].length; j++) {
                    System.out.print(matrice[i][j] + " ");
                }
                System.out.println(); // passer à la ligne suivante
            }
    }

} // end class

