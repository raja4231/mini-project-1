import java.util.*;
import java.util.concurrent.*;

public class CloudNodeMonitor {
    static class CloudNode implements Runnable {
        private final String nodeId;
        private volatile boolean isHealthy = true;
        private final Random random = new Random();
        private volatile boolean running = true;

        public CloudNode(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeId() {
            return nodeId;
        }

        public boolean isHealthy() {
            return isHealthy;
        }

        public void heal() {
            isHealthy = true;
            System.out.println("[Self-Healing] " + nodeId + " has been restarted.");
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(2000);
                    if (random.nextInt(10) > 7) {
                        isHealthy = false;
                        System.out.println("[Fault Detected] " + nodeId + " has failed.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("[Shutdown] " + nodeId + " stopped.");
        }
    }

    static class MonitorService implements Runnable {
        private final List<CloudNode> nodes;
        private volatile boolean running = true;

        public MonitorService(List<CloudNode> nodes) {
            this.nodes = nodes;
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(3000);
                    for (CloudNode node : nodes) {
                        if (!node.isHealthy()) {
                            System.out.println("[Monitor] Restarting " + node.getNodeId() + "...");
                            node.heal();
                        } else {
                            System.out.println("[Monitor] " + node.getNodeId() + " is healthy.");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("[Shutdown] Monitor stopped.");
        }
    }

    public static void main(String[] args) {
        List<CloudNode> nodes = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool();

        // Create and start cloud nodes
        for (int i = 1; i <= 3; i++) {
            CloudNode node = new CloudNode("Node-" + i);
            nodes.add(node);
            executor.submit(node);
        }

        // Start monitoring service
        MonitorService monitor = new MonitorService(nodes);
        executor.submit(monitor);

        // Run simulation for 30 seconds
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Shutdown all services
        System.out.println("\n[System] Shutting down simulation...");
        for (CloudNode node : nodes) {
            node.stop();
        }
        monitor.stop();
        executor.shutdownNow();
    }
}