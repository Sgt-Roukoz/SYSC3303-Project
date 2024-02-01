import java.io.File;

/**
 * Dummy class
 */

public class Main {

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        Floor floor = new Floor("Floor 1", scheduler, 1, false, true);
        floor.processFile(new File("test.txt"));
    }
}
