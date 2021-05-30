import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;

public class Client {

    //Output Strings
    private final static String AUTH = "AUTH "+System.getProperty("user.name");
    private final static String HELO = "HELO";
    private final static String OK = "OK";
    private final static String REDY = "REDY";
    private final static String SCHD = "SCHD";
    private final static String QUIT = "QUIT";

    //Input Strings
    private final static String JOBN = "JOBN"; 
    private final static String NONE = "NONE";
    private final static String GETSCAPABLE = "GETS Capable";
    private final static String GETSAVAIL = "GETS Avail";
    protected final String EJWT = "EJWT";
 

    //Socket & IO 
    private static Socket socket = null;
    private static DataInputStream inputStream = null;
    private static DataOutputStream outputStream = null;

    private static final int SERVERPORT = 50000;
    private static final String SERVERIP = "127.0.0.1";
    private static final String SERVER_XML_FILE = "ds-system.xml";
    private static final String SERVER_TAG = "server";

    //Scheduler
    private Scheduler scheduler;

    //Server States
    protected final String ACTIVE = "active";
    protected final String BOOTING = "booting";
    protected final String IDLE = "idle";
    protected final String INACTIVE = "inactive";

    //Status Codes
    private static final int ERROR = -1;
    private static final int EXIT = 1;
    private static final int SUCCESS = 0;

    public static void main(String[] args)  {
        //Create and run new client    
        try {
            Client client = new Client();
            client.run();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //Set socket and I/O streams
    private Client() throws UnknownHostException, IOException {
        socket = new Socket(SERVERIP, SERVERPORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }
    
    private void run() throws IOException {
        //Perform handshake with server
        if(connectionHandshake() != 0){
            closeConnection(ERROR);
        }
        
        //Set scheduler - Pass read XML server data and client
        scheduler = new Scheduler(getServerList(), this);
  
        writeToSocket(REDY);
        
        /*
            getJob() reads from input and passes array of data to determineAction
            determineAction() returns EXIT (1) if job is NONE, or SUCCESS (0) otherwise
            While SUCCESS, continue handling jobs until EXIT

        */
        while (determineAction(getJob()) == SUCCESS){
            writeToSocket(REDY);
        }
        closeConnection(SUCCESS);
    }

    //Perform handshake with server
    private int connectionHandshake() throws IOException {
        writeToSocket(HELO);
        String response = readFromSocket();
        if(checkResponse(OK, response) != SUCCESS) 
            return ERROR;

        writeToSocket(AUTH);
        response = readFromSocket();
        if(checkResponse(OK, response) != SUCCESS)
            return ERROR;
 
        return SUCCESS; 
    }

    protected void writeToSocket(String message) throws IOException{
        outputStream.write((message + "\n").getBytes());
        outputStream.flush();
    }

    protected String readFromSocket() throws IOException{
        String message = inputStream.readLine();
        return message.toString();
    }

    //Check received message equals expected message
    private int checkResponse(String expected, String message){
        return expected.equals(message) ? SUCCESS : ERROR;
    }

    //Return contents of input as array of data
    private String[] getJob() throws IOException {
        return readFromSocket().split(" ");
    }

    //Determine the action to perform based on 
    private int determineAction(String[] job) throws IOException {
        //First element of array contains command from server
        switch(job[0]){
            //JOBN indicates new job
            //Create job object to store data, allocate job to server and read server response
            case JOBN : Job currentJob = new Job(job);

                //Send job for scheduling then send selected server to ds-server for allocation
                Server selection = scheduler.costReduceAlgorithm(currentJob); 
                allocateJobToServer(currentJob, selection);
                break;

            case NONE:  return EXIT;
        }
        return SUCCESS;
    }

    //Get all servers capable of handling a job and return list
    protected ArrayList<Server> getCapableServers(Job currentJob) throws IOException {
        writeToSocket(GETSCAPABLE+" "+currentJob.core+ " "+currentJob.memory + " "+currentJob.disk);
        ArrayList<Server> servers = new ArrayList<>();
        String lines[] = readFromSocket().split(" ");
        writeToSocket(OK);

        for(int i = 0; i < Integer.parseInt(lines[1]); i++){
            String serverData[] = readFromSocket().split(" ");
            servers.add(new Server(serverData));
        }

        writeToSocket(OK);
        readFromSocket();
        return servers;
    }

    //Get all servers with available resources to handle a job and return list
    protected  ArrayList<Server> getAvailableServer(Job currentJob) throws IOException {
        writeToSocket(GETSAVAIL+" "+currentJob.core+ " "+currentJob.memory + " "+currentJob.disk);
        ArrayList<Server> servers = new ArrayList<>();

        String lines[] = readFromSocket().split(" ");
        writeToSocket(OK);

        //If no servers available, read '.' from socket and return empty arraylist
        if(Integer.parseInt(lines[1]) == 0){
            readFromSocket();
            return servers;
        }

        for(int i = 0; i < Integer.parseInt(lines[1]); i++){
            String serverData[] = readFromSocket().split(" ");
            Server current = new Server(serverData);
            servers.add(current);
        }

        writeToSocket(OK);
        readFromSocket();
    
        return servers;
    }


    
    //Determines algorithm to use when allocating jobs
    //Currently contains only allToLargest - More to be added in future
    private void allocateJobToServer(Job job, Server selection) throws IOException {
        writeToSocket(SCHD +" "+ job.jobID+ " " + selection.type + " " + selection.id );
        readFromSocket();
    }


    //Reads contents of server XML file and returns contents as Hashmap
    private HashMap<String,ServerTypes> getServerList()  {
        HashMap <String,ServerTypes> serverList = new HashMap<>();

        File xmFile = new File(SERVER_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmFile);

            doc.getDocumentElement().normalize();

            NodeList serverNodeList = doc.getElementsByTagName(SERVER_TAG);
            for(int i = 0; i < serverNodeList.getLength(); i++){
                Node serverNode = serverNodeList.item(i);

                if(serverNode.getNodeType() == Node.ELEMENT_NODE){
                    String type = ((Element) serverNode).getAttribute("type");
                    serverList.put(type, new ServerTypes((Element) serverNode));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serverList;
    }

    //On QUIT request or error socket is terminated and I/O streams closed
    private void closeConnection(int status) throws IOException{
        if(status == SUCCESS){
            writeToSocket(QUIT);
            String response = readFromSocket();
            checkResponse(QUIT, response);
        }
        inputStream.close();
        outputStream.close();
        socket.close();   
    }
}

           



