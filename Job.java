public class Job {
    public int jobNumber;           
    public int totalTime;           // How long job needs to run
    public int timeLeft;            
    public int memoryNeeded;        // How much memory this job needs
    public String status;           // "WAITING", "RUNNING", or "DONE"
    public int blockNumber;         
    public int startTime;           //when job started running
    
    //Constrsuctor for a new job
    public Job(int jobNum, int time, int memory) {
        this.jobNumber = jobNum;
        this.totalTime = time;
        this.timeLeft = time;
        this.memoryNeeded = memory;
        this.status = "WAITING";
        this.blockNumber = -1;
        this.startTime = -1;
    }
    public void tick() {  //tick is a time unit
        if (status.equals("RUNNING") && timeLeft > 0) { //checks if the job status is running and the time left is greater than 0
            timeLeft--;
            if (timeLeft == 0) {
                status = "DONE";  //changes the job status to DONE
            }
        }
    }
    public void start(int currentTime, int block) { // method to start a job, takes in the current tick time and the assigned memory block
        this.status = "RUNNING";
        this.startTime = currentTime;
        this.blockNumber = block;
    }
    public void finish() {
        this.status = "DONE";
        this.blockNumber = -1;
    }
    public boolean isDone() {  //checks is a job has finished
        return status.equals("DONE");
    }
}