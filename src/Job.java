public class Job {
    protected int submitTime, jobID, estRuntime, core, memory, disk;
    
    protected Job(String[] jobInfo){
        submitTime= Integer.valueOf(jobInfo[1]);
        jobID= Integer.valueOf(jobInfo[2]);
        estRuntime= Integer.valueOf(jobInfo[3]);
        core= Integer.valueOf(jobInfo[4]);
        memory= Integer.valueOf(jobInfo[5]);
        disk= Integer.valueOf(jobInfo[6]);

    }
}
