public class MemoryBlock {
    public int blockNumber;         // Block ID (1-10)
    public int blockSize;           // How big this block is
    public Job currentJob;          // Job using this block (
    public boolean isEmpty;         
    
    
    public MemoryBlock(int number, int size) { //constructor 
        this.blockNumber = number;
        this.blockSize = size;
        this.currentJob = null;
        this.isEmpty = true;
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
            
            // If job finished, unload it
            if (currentJob.isDone()) {
                unloadJob();
                return true;  // Job completed
            }
        }
        return false;  // Job still running
    }
}