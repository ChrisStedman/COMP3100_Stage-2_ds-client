import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Scheduler {
    HashMap<String,ServerTypes> serverTypes;

    protected Scheduler(HashMap<String,ServerTypes> serverTypes) {
        this.serverTypes = serverTypes;
    }

    protected Server scheduleJob(Job currentJob, Client client) throws IOException{
        Server selection =  scheduleAvailable(currentJob, client.getAvailableServer(currentJob));
        
        return selection == null ? scheduleCapable(currentJob, client.getCapableServers(currentJob), client) : selection;
    }
    
    protected Server scheduleAvailable(Job job, ArrayList<Server> servers) {
        Server selected = null;
        for(Server current: servers){
            if(current.state.equals("idle") || (current.state.equals("active")) && current.waitingJobs == 0){
               
                selected = current;
                break;
            }
        }
        return selected;
    }

    protected Server scheduleCapable(Job currentJob, ArrayList<Server> servers, Client client)  throws IOException{
      
        int shortestWait = Integer.MAX_VALUE;
        double currentCheapest = Double.MAX_VALUE;
        Server selection = null;
        
        for(int i = servers.size()-1; i >=0; i--){
            Server current = servers.get(i);
            double cost = serverTypes.get(current.type).hourlyRatePerCore;

           
                if(current.state.equals("active") || current.state.equals("booting")){
                    if(cost <= currentCheapest){
                        currentCheapest = cost;
              
               
                    client.writeToSocket("EJWT "+current.type +" "+ current.id);
                    int estWait = Integer.valueOf(client.readFromSocket());
                    shortestWait = Integer.MAX_VALUE;
                    
                    if(estWait < shortestWait){
                        selection = current;
                        shortestWait = estWait;
                    }  

                }

                }
            else if(current.state.equals("inactive") && shortestWait > currentJob.estRuntime *5){
                selection = current;
                break;
            }
      
    }
        return selection != null ? selection : servers.get(servers.size()-1);
        

    }
}
