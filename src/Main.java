import kbs.RewriteSystem;
import parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main program. Reads a group presentation from standard input and
 * outputs its number of elements and the (real) time taken to compute it.
 * <p>
 * Please implement {@link #sizeOfGroup}.
 * </p>
 */
public class Main {

    private static final Comparator<Collection<Character>> SHORTLEX = (o1, o2) -> {
        if (o1.size() != o2.size()) return o1.size() - o2.size();
        Iterator<Character> iterator1 = o1.iterator();
        Iterator<Character> iterator2 = o2.iterator();
        while (true) {
            if (!iterator1.hasNext()) return 0;
            Character next1 = iterator1.next();
            Character next2 = iterator2.next();
            if (next1.equals(next2)) continue;
            return next1.compareTo(next2);
        }

    };

    /**
     * Computes the size of the group specified by the given
     * parser result.
     */
    private static int sizeOfGroup(List<Parser.Result> list) {

        //Convert to characters
        Map<List<Character>, List<Character>> rules = list.stream()
                .collect(Collectors.toMap(
                        c -> c.left.stream().map(e -> e.ch).collect(Collectors.toList()),
                        c -> c.right.stream().map(e -> e.ch).collect(Collectors.toList()))
                );

        RewriteSystem<Character> rewriteSystem = new RewriteSystem<>(rules, SHORTLEX);


        Collection<List<Character>> baseForms = rewriteSystem.calcNormalForms();

        //baseForms.stream().forEach(System.out::println);
        //rewriteSystem.getCompleteRules().stream().forEach(System.out::println);

        return baseForms.size();
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
