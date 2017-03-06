package massim.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Listens for text inputs. If started, nothing else should read from {@link System#in}.
 * To wait until the next empty line is submitted, threads can synchronize and wait on this object, e.g.
 * {@code synchronized (inputManager) {try {inputManager.wait();} catch (InterruptedException ignored) {}}}
 */
public class InputManager {

    private LinkedBlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private Scanner scanner = new Scanner(System.in);
    private boolean stopped = false;

    /**
     * Starts listening on standard input
     */
    public void start() {
        Thread scanningThread = new Thread(() -> {
            String line;
            while (!stopped) {
                try {
                    line = scanner.nextLine().replace("\n", "");
                } catch (IllegalStateException e) {
                    stopped = true;
                    break;
                }
                Log.log(Log.Level.NORMAL, "You typed: " + line);
                if (line.equals("")) {
                    // notify all threads waiting for an empty line (also known as ENTER)
                    synchronized (this) {
                        InputManager.this.notifyAll();
                    }
                } else {
                    inputQueue.offer(line);
                }
            }
        });
        scanningThread.start();
    }

    /**
     * Stops listening for new inputs
     */
    public void stop(){
        this.stopped = true;
        scanner.close();
    }

    /**
     * Retrieves all inputs made since the last call to this method.
     * Empty lines are ignored (since they are used to notify waiting threads).
     * @return a list of all those inputs
     */
    public synchronized List<String> takeInputs(){
        List<String> inputs = new ArrayList<>(inputQueue.size());
        inputQueue.drainTo(inputs);
        return inputs;
    }
}
