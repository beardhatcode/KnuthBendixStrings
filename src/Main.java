import parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main program. Reads a group presentation from standard input and
 * outputs its number of elements and the (real) time taken to compute it.
 * <p>
 * Please implement {@link #sizeOfGroup}.
 * </p>
 */
public class Main {

    /**
     * Computes the size of the group specified by the given
     * parser result.
     */
    private static int sizeOfGroup(List<Parser.Result> list) {
        // this implementation simply returns the number of relations
        return list.size();
    }

    public static void main(String[] args) throws IOException, ParseException {
        try (InputStreamReader isr = new InputStreamReader(System.in);
             BufferedReader reader = new BufferedReader(isr)) {
            long currentTime = System.currentTimeMillis();
            Parser parser = new Parser();

            // cannot use Java 8 streams because parse throws exception
            String line = reader.readLine();
            List<Parser.Result> parsed = new ArrayList<>();
            while (line != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    parsed.add(parser.parse(line));
                }
                line = reader.readLine();
            }

            int size = sizeOfGroup(parsed);
            long time = System.currentTimeMillis() - currentTime;
            System.out.printf("%9d %6d.%03d s\n", size, time/1000, time%1000);
        }
    }


}
