import java.util.ArrayList;
public class MemorySimulator {

    public ArrayList<Job> waitingJobs;
    public ArrayList<Job> runningJobs;
    public ArrayList<Job> doneJobs;
    public ArrayList<Job> rejectedJobs;
    public ArrayList<MemoryBlock> memoryBlocks;
    public int currentTime;
    public int nextJobNumber;
    public int largestBlock;
    public boolean started;
    
    public int[][] allJobs = { //All job data [jobNumber, time, size]
        {1, 5, 5760}, {2, 4, 4190}, {3, 8, 3290}, {4, 2, 2030}, {5, 2, 2550},
        {6, 6, 6990}, {7, 8, 8940}, {8, 10, 740}, {9, 7, 3930}, {10, 6, 6890},
        {11, 5, 6580}, {12, 8, 3820}, {13, 9, 9140}, {14, 10, 420}, {15, 10, 220},
        {16, 7, 7540}, {17, 3, 3210}, {18, 1, 1380}, {19, 9, 9850}, {20, 3, 3610},
        {21, 7, 7540}, {22, 2, 2710}, {23, 8, 8390}, {24, 5, 5950}, {25, 10, 760}
    };
    public int[][] allBlocks = { // All memory blocks [blockNumber, size]
        {1, 9500}, {2, 7000}, {3, 4500}, {4, 8500}, {5, 3000},
        {6, 9000}, {7, 1000}, {8, 5500}, {9, 1500}, {10, 500}
    };
    
    // Constructor
    public MemorySimulator() {
        waitingJobs = new ArrayList<>();
        runningJobs = new ArrayList<>();
        doneJobs = new ArrayList<>();
        rejectedJobs = new ArrayList<>();
        memoryBlocks = new ArrayList<>();
        currentTime = 0;
        nextJobNumber = 0;
        largestBlock = 0;
        started = false;
    }
    
    // Set up the simulation
    public void setup() {
        // Create all memory blocks
        for (int i = 0; i < allBlocks.length; i++) {
            int blockNum = allBlocks[i][0];
            int blockSize = allBlocks[i][1];
            MemoryBlock block = new MemoryBlock(blockNum, blockSize);
            memoryBlocks.add(block);
            
            // Track largest block
            if (blockSize > largestBlock) {
                largestBlock = blockSize;
            }
        }
        started = true;
    }
    
    // Run one time tick and return what happened
    public String runOneTick() {
        String log = "";
        
        log += "=== Time " + currentTime + " ===\n";
        
        // Step 1: New job arrives
        if (nextJobNumber < allJobs.length) {
            int jobNum = allJobs[nextJobNumber][0];
            int jobTime = allJobs[nextJobNumber][1];
            int jobSize = allJobs[nextJobNumber][2];
            Job newJob = new Job(jobNum, jobTime, jobSize);
            
            log += "Job " + jobNum + " arrives (Size: " + jobSize + ", Time: " + jobTime + ")\n";
            
            // Check if job is too big
            if (jobSize > largestBlock) {
                rejectedJobs.add(newJob);
                log += "Job " + jobNum + " REJECTED - Too big!\n";
            } else {
                waitingJobs.add(newJob);
            }
            nextJobNumber++;
        }
        
        // Step 2: Process running jobs
        for (int i = 0; i < memoryBlocks.size(); i++) {
            MemoryBlock block = memoryBlocks.get(i);
            if (!block.isEmpty && block.currentJob != null) {
                // Save job reference before processing
                Job job = block.currentJob;
                job.tick();
                
                // Check if job finished
                if (job.isDone()) {
                    log += "Job " + job.jobNumber + " finished!\n";
                    log += "Block " + block.blockNumber + " is now free\n";
                    doneJobs.add(job);
                    runningJobs.remove(job);
                    block.unloadJob();
                }
            }
        }
        
        // Step 3: Try to load waiting jobs
        ArrayList<Job> jobsToRemove = new ArrayList<>();
        for (int i = 0; i < waitingJobs.size(); i++) {
            Job job = waitingJobs.get(i);
            boolean loaded = false;
            
            // Try each block (First-Fit)
            for (int j = 0; j < memoryBlocks.size(); j++) {
                MemoryBlock block = memoryBlocks.get(j);
                if (block.canFit(job)) {
                    block.loadJob(job, currentTime);
                    runningJobs.add(job);
                    jobsToRemove.add(job);
                    
                    int wasted = block.getWastedSpace();
                    double percent = (wasted * 100.0) / block.blockSize;
                    log += "Job " + job.jobNumber + " loaded into Block " + block.blockNumber + "\n";
                    log += "Wasted space: " + wasted + " (" + String.format("%.1f", percent) + "%)\n";
                    
                    loaded = true;
                    break;  // First-Fit: stop at first block that fits
                }
            }
            
            if (!loaded) {
                log += "Job " + job.jobNumber + " waiting (no block available)\n";
            }
        }
        
        // Remove jobs that were loaded
        for (int i = 0; i < jobsToRemove.size(); i++) {
            waitingJobs.remove(jobsToRemove.get(i));
        }
        
        currentTime++;
        log += "\n";
        return log;
    }
    
    // Check if simulation is done
    public boolean isDone() {
        return nextJobNumber >= allJobs.length && 
               waitingJobs.size() == 0 && 
               runningJobs.size() == 0;
    }
    
    // Getter methods for GUI
    public boolean isStarted() { return started; }
    public int getTime() { return currentTime; }
    public int getRunningCount() { return runningJobs.size(); }
    public int getWaitingCount() { return waitingJobs.size(); }
    public int getDoneCount() { return doneJobs.size(); }
    public int getRejectedCount() { return rejectedJobs.size(); }
    public ArrayList<MemoryBlock> getBlocks() { return memoryBlocks; }
}