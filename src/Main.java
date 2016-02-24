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
        Map<List<Parser.Element>, List<Parser.Element>> rules = list.stream()
                .collect(Collectors.toMap(c -> c.left, c -> c.right));

        Set<Parser.Element> elements = new HashSet<>();
        list.forEach(e -> {
            e.left.forEach(elements::add);
            e.right.forEach(elements::add);
        });

        RewriteSystem<Parser.Element> rewriteSystem = new RewriteSystem<>(rules,(o1, o2) -> {
            if(o1.size() != o2.size()) return o1.size() - o2.size();
            Iterator<Parser.Element> iterator1 = o1.iterator();
            Iterator<Parser.Element> iterator2 = o2.iterator();
            while ( true ) {
                Parser.Element next1 = iterator1.next();
                Parser.Element next2 = iterator2.next();
                if(next1 == null && next2 == null) return 0;
                if(next1==null) return -1;
                if(next2==null) return 1;
                if(next1.hashCode() == next2.hashCode()) continue;
                return next1.hashCode() - next2.hashCode();
            }

        });
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
