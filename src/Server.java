
//Store information related to server types
public class Server {

    protected String type, state;
    protected int id, curStartTime, coreCount, memory, disk, waitingJobs, runningJobs;
   
    
    protected Server(String values[]){
        type = values[0];
        id = Integer.parseInt(values[1]);
        state = values[2];
        curStartTime = Integer.parseInt(values[3]);
        coreCount = Integer.parseInt(values[4]);
        memory = Integer.parseInt(values[5]);
        disk = Integer.parseInt(values[6]);
        waitingJobs = Integer.parseInt(values[7]);
        runningJobs = Integer.parseInt(values[8]);
    } 
}
