package massim;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Implements the random number generation (standard Java for now).
 *
 */
public abstract class RNG {

    private static Random random = new Random(System.currentTimeMillis());

    /**
     * Initializes the rng to the given seed.
     * @param seed the seed for the rng
     */
    public static synchronized void initialize(long seed){
        random = new Random(seed);
    }

    public static synchronized int nextInt(){
        return random.nextInt();
    }

    public static synchronized int nextInt(int bound){
        return random.nextInt(bound);
    }

    public static synchronized double nextDouble(){ return random.nextDouble(); }

    public static synchronized void shuffle(List<?> list){
        Collections.shuffle(list, random);
    }
}
