import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Robbert Gurdeep Singh on 17/02/16.
 */
public class MainTest {


    @Test
    public void test10() throws Exception {
        System.setIn(new FileInputStream(new File("./res/example-10.txt")));
        Main.main(new String[]{"example-1 0.txt"});
    }
}