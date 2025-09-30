import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MemorySimulatorGUI extends JFrame {

    public JPanel leftPanel;           
    public JPanel centerPanel;         
    public JTextArea logArea;          
    public JScrollPane logScroll;
    public JLabel timeLabel;
    public JLabel runningLabel;
    public JLabel waitingLabel;
    public JLabel doneLabel;
    public JLabel rejectedLabel;
    public JLabel algorithmLabel;  // New label to show current algorithm
    public JButton startFirstFitButton;  // Renamed and updated
    public JButton startBestFitButton;   // New button for best-fit
    public JButton pauseButton;
    public JButton stepButton;
    public Timer timer;
    public ArrayList<BlockPanel> blockPanels;
    public MemorySimulator firstFitSim;      // First-fit simulator
    public BestFitMemorySimulator bestFitSim; // Best-fit simulator
    public Object currentSim;  // Reference to currently active simulator
    public String currentAlgorithm;  // Track which algorithm is running
    
  
    public MemorySimulatorGUI() {
        firstFitSim = new MemorySimulator();
        bestFitSim = new BestFitMemorySimulator();
        currentSim = null;
        currentAlgorithm = "None";
        blockPanels = new ArrayList<>();
        
        // Window settings
        setTitle("Memory Allocation Simulator - First-Fit vs Best-Fit");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create all parts
        makeLeftPanel();
        makeCenterPanel();
        makeLogPanel();
        
        // Add to window
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(logScroll, BorderLayout.SOUTH);
        
        // Make block visuals
        makeBlockPanels();
        
        // Make timer (2 seconds per tick)
        timer = new Timer(2000, e -> {
            step();
            if (isCurrentSimDone()) {
                timer.stop();
                startFirstFitButton.setEnabled(true);
                startBestFitButton.setEnabled(true);
                pauseButton.setEnabled(false);
                logArea.append("\n=== SIMULATION COMPLETE ===\n");
                showStats();
            }
        });
        
        // Show window
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Create left control panel
    public void makeLeftPanel() {
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        leftPanel.setPreferredSize(new Dimension(220, 400));
        
        // Create labels
        algorithmLabel = new JLabel("Algorithm: None");
        algorithmLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        timeLabel = new JLabel("Time: 0");
        runningLabel = new JLabel("Running: 0");
        waitingLabel = new JLabel("Waiting: 0");
        doneLabel = new JLabel("Done: 0");
        rejectedLabel = new JLabel("Rejected: 0");
        
        // Create buttons
        startFirstFitButton = new JButton("Start First-Fit");
        startBestFitButton = new JButton("Start Best-Fit");
        pauseButton = new JButton("Pause");
        stepButton = new JButton("Step");
        pauseButton.setEnabled(false);
        stepButton.setEnabled(false);
    
        
        // Button actions
        startFirstFitButton.addActionListener(e -> startFirstFit());
        startBestFitButton.addActionListener(e -> startBestFit());
        pauseButton.addActionListener(e -> pause());
        stepButton.addActionListener(e -> step());
        
        // Add everything
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(algorithmLabel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(timeLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(runningLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(waitingLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(doneLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(rejectedLabel);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(startFirstFitButton);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(startBestFitButton);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(pauseButton);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(stepButton);
        leftPanel.add(Box.createVerticalGlue());
    }
    
    // Create center panel for memory blocks
    public void makeCenterPanel() {
        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 5, 10, 10));  // 2 rows, 5 columns
        centerPanel.setBorder(BorderFactory.createTitledBorder("Memory Blocks"));
        centerPanel.setBackground(Color.LIGHT_GRAY);
    }
    
    // Create log area at bottom
    public void makeLogPanel() {
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log"));
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    
    // Create visual blocks
    public void makeBlockPanels() {
        int[][] blocks = {
            {1, 9500}, {2, 7000}, {3, 4500}, {4, 8500}, {5, 3000},
            {6, 9000}, {7, 1000}, {8, 5500}, {9, 1500}, {10, 500}
        };
        
        for (int i = 0; i < blocks.length; i++) {
            BlockPanel bp = new BlockPanel(blocks[i][0], blocks[i][1]);
            blockPanels.add(bp);
            centerPanel.add(bp);
        }
    }
    
    // Start First-Fit simulation
    public void startFirstFit() {
        // Reset simulation if needed
        if (currentSim != null) {
            resetSimulation();
        }
        
        currentSim = firstFitSim;
        currentAlgorithm = "First-Fit";
        algorithmLabel.setText("Algorithm: First-Fit");
        
        if (!firstFitSim.isStarted()) {
            firstFitSim.setup();
            logArea.append("=== FIRST-FIT SIMULATION STARTED ===\n");
            logArea.append("=====================================\n");
        }
        
        startSimulation();
    }
    
    // Start Best-Fit simulation
    public void startBestFit() {
        // Reset simulation if needed
        if (currentSim != null) {
            resetSimulation();
        }
        
        currentSim = bestFitSim;
        currentAlgorithm = "Best-Fit";
        algorithmLabel.setText("Algorithm: Best-Fit");
        
        if (!bestFitSim.isStarted()) {
            bestFitSim.setup();
            logArea.append("=== BEST-FIT SIMULATION STARTED ===\n");
            logArea.append("====================================\n");
        }
        
        startSimulation();
    }
    
    // Common start logic
    private void startSimulation() {
        timer.start();
        startFirstFitButton.setEnabled(false);
        startBestFitButton.setEnabled(false);
        pauseButton.setEnabled(true);
        stepButton.setEnabled(true);
    }
    
    // Reset simulation state
    private void resetSimulation() {
        timer.stop();
        firstFitSim = new MemorySimulator();
        bestFitSim = new BestFitMemorySimulator();
        logArea.setText("");
        
        // Clear visual blocks
        for (BlockPanel panel : blockPanels) {
            panel.clearJob();
        }
        
        updateDisplay();
    }
    
    // Pause button clicked
    public void pause() {
        timer.stop();
        startFirstFitButton.setEnabled(true);
        startBestFitButton.setEnabled(true);
        pauseButton.setEnabled(false);
    }
    
    // Step button clicked (or timer tick)
    public void step() {
        if (currentSim == null) return;
        
        String log = "";
        if (currentSim instanceof MemorySimulator) {
            log = ((MemorySimulator) currentSim).runOneTick();
        } else if (currentSim instanceof BestFitMemorySimulator) {
            log = ((BestFitMemorySimulator) currentSim).runOneTick();
        }
        
        logArea.append(log);
        updateDisplay();
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    // Check if current simulation is done
    private boolean isCurrentSimDone() {
        if (currentSim == null) return true;
        
        if (currentSim instanceof MemorySimulator) {
            return ((MemorySimulator) currentSim).isDone();
        } else if (currentSim instanceof BestFitMemorySimulator) {
            return ((BestFitMemorySimulator) currentSim).isDone();
        }
        return true;
    }
    
    // Update all displays
    public void updateDisplay() {
        if (currentSim == null) {
            timeLabel.setText("Time: 0");
            runningLabel.setText("Running: 0");
            waitingLabel.setText("Waiting: 0");
            doneLabel.setText("Done: 0");
            rejectedLabel.setText("Rejected: 0");
            return;
        }
        
        // Update labels based on current simulator
        if (currentSim instanceof MemorySimulator) {
            MemorySimulator sim = (MemorySimulator) currentSim;
            timeLabel.setText("Time: " + sim.getTime());
            runningLabel.setText("Running: " + sim.getRunningCount());
            waitingLabel.setText("Waiting: " + sim.getWaitingCount());
            doneLabel.setText("Done: " + sim.getDoneCount());
            rejectedLabel.setText("Rejected: " + sim.getRejectedCount());
            
            // Update blocks
            ArrayList<MemoryBlock> blocks = sim.getBlocks();
            updateBlockPanels(blocks);
        } else if (currentSim instanceof BestFitMemorySimulator) {
            BestFitMemorySimulator sim = (BestFitMemorySimulator) currentSim;
            timeLabel.setText("Time: " + sim.getTime());
            runningLabel.setText("Running: " + sim.getRunningCount());
            waitingLabel.setText("Waiting: " + sim.getWaitingCount());
            doneLabel.setText("Done: " + sim.getDoneCount());
            rejectedLabel.setText("Rejected: " + sim.getRejectedCount());
            
            // Update blocks
            ArrayList<MemoryBlock> blocks = sim.getBlocks();
            updateBlockPanels(blocks);
        }
        
        repaint();
    }
    
    // Helper method to update block panels
    private void updateBlockPanels(ArrayList<MemoryBlock> blocks) {
        for (int i = 0; i < blocks.size() && i < blockPanels.size(); i++) {
            MemoryBlock block = blocks.get(i);
            BlockPanel panel = blockPanels.get(i);
            
            if (!block.isEmpty) {
                panel.setJob(block.currentJob);
            } else {
                panel.clearJob();
            }
        }
    }
    
    // Show final stats
    public void showStats() {
        String stats = "\n=== FINAL STATISTICS ===\n";
        stats += "Algorithm Used: " + currentAlgorithm + "\n";
        
        if (currentSim instanceof MemorySimulator) {
            MemorySimulator sim = (MemorySimulator) currentSim;
            stats += "Jobs Completed: " + sim.getDoneCount() + "\n";
            stats += "Jobs Rejected: " + sim.getRejectedCount() + "\n";
            stats += "Total Time: " + sim.getTime() + " ticks\n";
        } else if (currentSim instanceof BestFitMemorySimulator) {
            BestFitMemorySimulator sim = (BestFitMemorySimulator) currentSim;
            stats += "Jobs Completed: " + sim.getDoneCount() + "\n";
            stats += "Jobs Rejected: " + sim.getRejectedCount() + "\n";
            stats += "Total Time: " + sim.getTime() + " ticks\n";
        }
        
        stats += "========================\n";
        logArea.append(stats);
    }
    
    // Panel for one memory block
    class BlockPanel extends JPanel {
        public int blockNum;
        public int blockSize;
        public Job currentJob;
        public boolean hasJob;
        
        public BlockPanel(int num, int size) {
            this.blockNum = num;
            this.blockSize = size;
            this.hasJob = false;
            this.currentJob = null;
            setPreferredSize(new Dimension(120, 80));
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        }
        
        public void setJob(Job job) {
            this.currentJob = job;
            this.hasJob = true;
            repaint();
        }
        
        public void clearJob() {
            this.currentJob = null;
            this.hasJob = false;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Choose color
            Color bgColor;
            if (hasJob && currentJob != null) {
                // Red to green based on progress
                float done = 1.0f - ((float)currentJob.timeLeft / currentJob.totalTime);
                int red = (int)(255 * (1.0f - done));
                int green = (int)(255 * done);
                bgColor = new Color(red, green, 0);
            } else {
                bgColor = Color.LIGHT_GRAY;
            }
            
            // Fill background
            g.setColor(bgColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw text
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            
            // Block info
            String line1 = "Block " + blockNum;
            String line2 = "Size: " + blockSize;
            g.setColor(Color.BLACK);
            g.drawString(line1, (getWidth() - fm.stringWidth(line1)) / 2, 20);
            g.drawString(line2, (getWidth() - fm.stringWidth(line2)) / 2, 35);
            
            // Job info
            if (hasJob && currentJob != null) {
                // Pick text color based on background
                float done = 1.0f - ((float)currentJob.timeLeft / currentJob.totalTime);
                if (done > 0.5f) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                
                String line3 = "Job " + currentJob.jobNumber;
                String line4 = currentJob.timeLeft + "/" + currentJob.totalTime;
                g.drawString(line3, (getWidth() - fm.stringWidth(line3)) / 2, 50);
                g.drawString(line4, (getWidth() - fm.stringWidth(line4)) / 2, 65);
            } else {
                g.setColor(Color.DARK_GRAY);
                String line3 = "FREE";
                g.drawString(line3, (getWidth() - fm.stringWidth(line3)) / 2, 55);
            }
        }
    }
    
    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MemorySimulatorGUI();
        });
    }
}