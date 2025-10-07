import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class BestFitMemorySimulator {

    public ArrayList<Job> waitingJobs;
    public ArrayList<Job> runningJobs;
    public ArrayList<Job> doneJobs;
    public ArrayList<Job> rejectedJobs;
    public ArrayList<MemoryBlock> memoryBlocks;
    public int currentTime;
    public int nextJobNumber;
    public int largestBlock;
    public boolean started;
    // Metrics (same as MemorySimulator(which implements First-Fit))
    public long queueLengthSum;
    public int queueLengthMax;
    public int queueLengthSamples;
    public long totalWaitTime;
    public int jobsStartedCount;
    public long internalFragSumBytes;
    public long internalFragDenomBytes;
    public int internalFragSamples;
    
    public int[][] allJobs = { //All job data [jobNumber, time, size] - will be sorted by size
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
    public BestFitMemorySimulator() {
        waitingJobs = new ArrayList<>();
        runningJobs = new ArrayList<>();
        doneJobs = new ArrayList<>();
        rejectedJobs = new ArrayList<>();
        memoryBlocks = new ArrayList<>();
        currentTime = 0;
        nextJobNumber = 0;
        largestBlock = 0;
        started = false;
        queueLengthSum = 0;
        queueLengthMax = 0;
        queueLengthSamples = 0;
        totalWaitTime = 0;
        jobsStartedCount = 0;
        internalFragSumBytes = 0;
        internalFragDenomBytes = 0;
        internalFragSamples = 0;
    }
    
    // Set up the simulation
    public void setup() {
        // Sort allJobs by size (third element) in ascending order for best-fit allocation
        Arrays.sort(allJobs, new Comparator<int[]>() {
            @Override
            public int compare(int[] job1, int[] job2) {
                return Integer.compare(job1[2], job2[2]); // Compare by size (third element)
            }
        });
        
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
            newJob.arrivalTime = currentTime;
            
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
                block.timeUsedTicks++;
                
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
        
        // Step 3: Try to load waiting jobs using BEST-FIT algorithm
        // record queue length snapshot before allocation
        queueLengthSum += waitingJobs.size();
        queueLengthSamples++;
        if (waitingJobs.size() > queueLengthMax) queueLengthMax = waitingJobs.size();
        ArrayList<Job> jobsToRemove = new ArrayList<>();
        for (int i = 0; i < waitingJobs.size(); i++) {
            Job job = waitingJobs.get(i);
            boolean loaded = false;
            
            // Best-Fit: Find the smallest block that can fit this job
            MemoryBlock bestBlock = null;
            int smallestWaste = Integer.MAX_VALUE;
            
            for (int j = 0; j < memoryBlocks.size(); j++) {
                MemoryBlock block = memoryBlocks.get(j);
                if (block.canFit(job)) {
                    int wastedSpace = block.blockSize - job.memoryNeeded;
                    if (wastedSpace < smallestWaste) {
                        smallestWaste = wastedSpace;
                        bestBlock = block;
                    }
                }
            }
            
            // If we found a suitable block, load the job
            if (bestBlock != null) {
                bestBlock.loadJob(job, currentTime);
                runningJobs.add(job);
                jobsToRemove.add(job);
                totalWaitTime += job.waitTime;
                jobsStartedCount++;
                
                int wasted = bestBlock.getWastedSpace();
                double percent = (wasted * 100.0) / bestBlock.blockSize;
                log += "Job " + job.jobNumber + " loaded into Block " + bestBlock.blockNumber + " (BEST-FIT)\n";
                log += "Wasted space: " + wasted + " (" + String.format("%.1f", percent) + "%)\n";
                
                loaded = true;
            }
            
            if (!loaded) {
                log += "Job " + job.jobNumber + " waiting (no suitable block available)\n";
            }
        }
        
        // Remove jobs that were loaded
        for (int i = 0; i < jobsToRemove.size(); i++) {
            waitingJobs.remove(jobsToRemove.get(i));
        }
        
        // Step 4: Measure internal fragmentation for this tick
        long wastedThisTick = 0;
        long occupiedCapacityThisTick = 0;
        for (int i = 0; i < memoryBlocks.size(); i++) {
            MemoryBlock block = memoryBlocks.get(i);
            if (!block.isEmpty && block.currentJob != null) {
                wastedThisTick += block.getWastedSpace();
                occupiedCapacityThisTick += block.blockSize;
            }
        }
        if (occupiedCapacityThisTick > 0) {
            internalFragSumBytes += wastedThisTick;
            internalFragDenomBytes += occupiedCapacityThisTick;
            internalFragSamples++;
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

    // Extended statistics string
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Jobs Completed: ").append(getDoneCount()).append("\n");
        sb.append("Jobs Rejected: ").append(getRejectedCount()).append("\n");
        sb.append("Total Time: ").append(getTime()).append(" ticks\n");
        double throughput = getTime() > 0 ? (getDoneCount() * 1.0) / getTime() : 0.0;
        sb.append(String.format("Throughput: %.3f jobs/tick\n", throughput));

        double avgQueueLen = queueLengthSamples > 0 ? (queueLengthSum * 1.0) / queueLengthSamples : 0.0;
        sb.append(String.format("Avg waiting queue length: %.2f (max %d)\n", avgQueueLen, queueLengthMax));

        double avgWaitTime = jobsStartedCount > 0 ? (totalWaitTime * 1.0) / jobsStartedCount : 0.0;
        sb.append(String.format("Avg waiting time in queue: %.2f ticks\n", avgWaitTime));

        // Internal fragmentation
        double avgWastedBytes = internalFragSamples > 0 ? (internalFragSumBytes * 1.0) / internalFragSamples : 0.0;
        double avgFragPct = (internalFragDenomBytes > 0) ? (internalFragSumBytes * 100.0) / internalFragDenomBytes : 0.0;
        sb.append(String.format("Avg internal fragmentation: %.0f bytes (%.2f%%) per active tick\n", avgWastedBytes, avgFragPct));

        // Storage utilization by time usage
        int blocks = memoryBlocks.size();
        int neverUsed = 0, lightUsed = 0, moderateUsed = 0, heavyUsed = 0;
        long totalUsedTicks = 0;
        for (int i = 0; i < blocks; i++) {
            MemoryBlock b = memoryBlocks.get(i);
            totalUsedTicks += b.timeUsedTicks;
            if (b.timesAssigned == 0) {
                neverUsed++;
            }
            double timeFrac = getTime() > 0 ? (b.timeUsedTicks * 1.0) / getTime() : 0.0;
            if (timeFrac >= 0.8) heavyUsed++;
            else if (timeFrac >= 0.2) moderateUsed++;
            else if (timeFrac > 0.0) lightUsed++;
        }
        double overallUtilPct = (getTime() > 0 && blocks > 0) ? (totalUsedTicks * 100.0) / (getTime() * blocks) : 0.0;
        sb.append(String.format("Avg block time utilization: %.2f%%\n", overallUtilPct));
        if (blocks > 0) {
            sb.append(String.format("Partitions never used: %.1f%% (%d/%d)\n", neverUsed * 100.0 / blocks, neverUsed, blocks));
            sb.append(String.format("Partitions lightly used (<20%% time): %.1f%% (%d/%d)\n", lightUsed * 100.0 / blocks, lightUsed, blocks));
            sb.append(String.format("Partitions moderately used (20-80%% time): %.1f%% (%d/%d)\n", moderateUsed * 100.0 / blocks, moderateUsed, blocks));
            sb.append(String.format("Partitions heavily used (>=80%% time): %.1f%% (%d/%d)\n", heavyUsed * 100.0 / blocks, heavyUsed, blocks));
        }
        return sb.toString();
    }
}
