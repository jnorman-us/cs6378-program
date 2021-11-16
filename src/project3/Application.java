package project3;

import node.NodeID;

import java.util.Random;

public class Application {
    private DLock lock;
    private Random random;

    private int avgInterRequestDelay;
    private int avgCSExecutionTime;
    private int numCSRequests;

    public Application(NodeID id, String configFile, int aird, int acset, int ncsr) {
        lock = new DLock(id, configFile);
        random = new Random();

        avgInterRequestDelay = aird;
        avgCSExecutionTime = acset;
        numCSRequests = ncsr;
    }

    public void run() {
        for(int request_i = 0; request_i < numCSRequests; request_i ++) {
            int randomRequestDelay = generateRandomWithAverage(avgInterRequestDelay);
            int randomCSExecutionTime = generateRandomWithAverage(avgCSExecutionTime);

            sleep(randomRequestDelay);

            lock.lock();
            criticalSection(randomCSExecutionTime);
            lock.unlock();
        }
        lock.close();
    }

    public void criticalSection(int executionTime) {
        System.out.println("Entering Critical Section");

        sleep(executionTime);
        // TODO do something more meaningful here, maybe to aide in testing
        // that Mutual Exclusion is enforced through timestamps
        // e.g. 11/15/2021 12:00:34.123

        System.out.println("Exiting Critical Section");
    }

    public int generateRandomWithAverage(int average) {
        double standardDeviation = average / 3; // idk, here's some uniform distribution around mean
        double randomGaussian = Math.abs(random.nextGaussian(average, standardDeviation));
        return (int) Math.round(randomGaussian);
    }

    public void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
