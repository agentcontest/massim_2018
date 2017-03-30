package massim.util;

import java.io.IOException;
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
        new Thread(() -> {
            String line;
            while (!stopped) {
                try {
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine().replace("\n", "");
                        if (line.equals("")) {
                            // notify all threads waiting for an empty line (also known as ENTER)
                            synchronized (this) {
                                InputManager.this.notifyAll();
                            }
                        } else {
                            Log.log(Log.Level.NORMAL, "You typed: " + line);
                            inputQueue.add(line);
                        }
                    } else
                        Thread.sleep(200);
                } catch (IllegalStateException e) {
                    stopped = true;
                    break;
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    /**
     * Stops listening for new inputs
     */
    public void stop(){
        this.stopped = true;
        try {
            System.in.close();
        } catch (IOException ignored) {}
    }

    /**
     * @return the most recent input - blocks until one is available if the input queue is empty
     * @throws InterruptedException if interrupted while waiting
     */
    public String take() throws InterruptedException {
        return inputQueue.take();
    }

    /**
     * @return true if at least one input is buffered and has not been taken yet
     */
    public boolean hasInput() {
        return inputQueue.size() > 0;
    }
}
