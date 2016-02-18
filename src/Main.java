import kbs.RewriteSystem;
import parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

        Parser.Element[] a = new Parser.Element[]{new Parser.Element('l',false)};
        Map<Parser.Element[], Parser.Element[]> rules = list.stream()
                .collect(Collectors.toMap(c -> c.left.toArray(a), c -> c.right.toArray(a)));

        Set<Parser.Element> elements = new HashSet<>();
        list.forEach(e -> {
            e.left.forEach(elements::add);
            e.right.forEach(elements::add);
        });

        RewriteSystem<Parser.Element> rewriteSystem = new RewriteSystem<>(rules,elements);
        rewriteSystem.apply(null);

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
