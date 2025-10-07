public class MemoryBlock {
    public int blockNumber;         // Block ID (1-10)
    public int blockSize;           // How big this block is
    public Job currentJob;          // Job using this block (
    public boolean isEmpty;         
    // Metrics
    public int timeUsedTicks;       // how many ticks this block was occupied
    public int timesAssigned;       // how many jobs have been assigned to this block
    public int maxUtilization;      // track max utilization percent seen (0-100)
    
    
    public MemoryBlock(int number, int size) { //constructor 
        this.blockNumber = number;
        this.blockSize = size;
        this.currentJob = null;
        this.isEmpty = true;
        this.timeUsedTicks = 0;
        this.timesAssigned = 0;
        this.maxUtilization = 0;
    }
    public boolean canFit(Job job) { //checks to see if a job can enter a memoryBlock
        if (isEmpty && job.memoryNeeded <= blockSize){ // if the block is empty and the jobs memory is less that the block size, it returns true
            return true;
        }else {
            return false;
        }
    }
    public void loadJob(Job job, int currentTime) { //loads a job into a memoryBlock
        this.currentJob = job;
        this.isEmpty = false;
        job.start(currentTime, this.blockNumber);
        this.timesAssigned++;
        // update max utilization
        int util = (int)Math.round((job.memoryNeeded * 100.0) / blockSize);
        if (util > maxUtilization) maxUtilization = util;
    }
    public void unloadJob() { //deallocates a job from memory
        if (currentJob != null) {
            currentJob.finish();
            this.currentJob = null;
            this.isEmpty = true;
        }
    }
    public int getWastedSpace() { // calculating the internal fragmentation
        if (!isEmpty && currentJob != null) {
            return blockSize - currentJob.memoryNeeded;
        }
        return 0;
    }
    public boolean processJob() { //uses the tick method to process the job for a unit of time
        if (!isEmpty && currentJob != null) {
            currentJob.tick();
            this.timeUsedTicks++; // count utilization time
            
            // If job finished, unload it
            if (currentJob.isDone()) {
                unloadJob();
                return true;  // Job completed
            }
        }
        return false;  // Job still running
    }
}