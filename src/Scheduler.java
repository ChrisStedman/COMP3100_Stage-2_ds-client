import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Scheduler {
    private HashMap<String,ServerTypes> serverTypes;
    private Client client;

    //Set cost multiplier for algorithm
    //Higher generally means longer queues and less cost, while lower generally means shorter queues and higher costs
    private final Double COST_MULTIPLIER = 4.0;

    //Set server types
    protected Scheduler(HashMap<String,ServerTypes> serverTypes, Client client) {
        this.serverTypes = serverTypes;
        this.client = client;
    }

    //Scheduling algorithm
    //First attempt to find available powered-on server
    //If none found, find a capable sever 
    protected Server scheduleJob(Job currentJob) throws IOException{
        Server selection =  scheduleAvailable(currentJob, client.getAvailableServer(currentJob));
        
        return selection == null ? scheduleCapable(currentJob, client.getCapableServers(currentJob), client) : selection;
    }
    
    //Search available servers
    protected Server scheduleAvailable(Job job, ArrayList<Server> servers) {
        Server selected = null;

        //Find servers which are idling or active with no waiting jobs
        //These servers are ready immediately
        for(Server current: servers){
            if(current.state.equals(client.IDLE) || (current.state.equals(client.ACTIVE)) && current.waitingJobs == 0){
                selected = current;
                break;
            }
        }
        return selected;
    }

    //Find server out of all capable servers
    protected Server scheduleCapable(Job currentJob, ArrayList<Server> servers, Client client)  throws IOException{
      
        int shortestWait = Integer.MAX_VALUE;
        double currentCheapest = Double.MAX_VALUE;
        Server selection = null;
        Server inactiveSelection = null;
        
        //Iterate through all capable servers
        for(Server current: servers){
            //Get current server to be examined - store hourly rate per core in variable
            double cost = serverTypes.get(current.type).hourlyRatePerCore;

                //If the server is active or booting (must have waiting jobs as available already searched)
                if(current.state.equals(client.ACTIVE) || current.state.equals(client.BOOTING)){

                    //If hourly rate per core is cheaper or equal to - continue
                    if(cost <= currentCheapest){
                        currentCheapest = cost;
                    
                        //Get estimated wait time from server
                        client.writeToSocket(client.EJWT+" "+current.type +" "+ current.id);
                        int estWait = Integer.valueOf(client.readFromSocket());
                        
                        //If estimated wait time shorter than current shortest wait - update
                        if(estWait < shortestWait){
                            selection = current;
                            shortestWait = estWait;
                        }  
                    } 
                }
            //If server inactive and cost of running it is cheaper, then update the inactive selection
            else if(current.state.equals(client.INACTIVE) && cost < currentCheapest ){
                inactiveSelection = current;
            }
          
        }
        //If the shortest estimated wait time is x times longer than the current job's estimated run time
        //and an inactive server is available - Return inactive server - Else return the best selected active server
        //Essentially determines whether it is worth starting another server based on the expected wait time
        return shortestWait > currentJob.estRuntime * COST_MULTIPLIER && inactiveSelection != null  ? inactiveSelection : selection;
    }
}
