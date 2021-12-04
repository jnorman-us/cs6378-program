package project3;

import node.NodeID;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Random;

public class Application {
    private File criticalSectionOutput;
    private FileWriter output;

    private DLock lock;
    private Random random;

    private int avgInterRequestDelay;
    private int avgCSExecutionTime;
    private int numCSRequests;

    public Application(NodeID id, String configFile, int aird, int acset, int ncsr) {
        try {
            String directoryPath = "./output";
            File outputDirectory = new File(directoryPath);
            if(!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            criticalSectionOutput = new File(String.format("%s/%s.txt", directoryPath, id.toString()));
            criticalSectionOutput.createNewFile();
        } catch(IOException e) {
            e.printStackTrace();
        }

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
            try {
                criticalSection(request_i, randomCSExecutionTime);
            } catch(IOException e) {
                e.printStackTrace();
            }
            lock.unlock();
        }
        // lock.close();
        System.out.println("Done!\n\n\n\n\n\n");
    }

    public void criticalSection(int i, int executionTime) throws IOException {
        output = new FileWriter(criticalSectionOutput, i != 0);
        System.out.println("ENTER");
        String timeStart = LocalTime.now().toString();
        sleep(executionTime);
        String timeEnd = LocalTime.now().toString();
        System.out.println("EXIT");
        output.write(timeStart + "-" + timeEnd + ": " + lock.getID() + " (" + (i + 1) + "/" + numCSRequests + ")\n");
        output.close();
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
