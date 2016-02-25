import kbs.Paster;
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

    private static final Comparator<Collection<Parser.Element>> SHORTLEX = (o1, o2) -> {
        if (o1.size() != o2.size()) return o1.size() - o2.size();
        Iterator<Parser.Element> iterator1 = o1.iterator();
        Iterator<Parser.Element> iterator2 = o2.iterator();
        while (true) {
            boolean hasNext1 = iterator1.hasNext();
            boolean hasNext2 = iterator2.hasNext();
            if (!hasNext1 && !hasNext2) return 0;
            if (!hasNext1) return -1;
            if (!hasNext2) return 1;
            Parser.Element next1 = iterator1.next();
            Parser.Element next2 = iterator2.next();
            if (next1.hashCode() == next2.hashCode()) continue;
            return next1.hashCode() - next2.hashCode();
        }

    };

    /**
     * Computes the size of the group specified by the given
     * parser result.
     */
    private static int sizeOfGroup(List<Parser.Result> list) {
        // this implementation simply returns the number of relations

        Map<List<Parser.Element>, List<Parser.Element>> rules = list.stream()
                .collect(Collectors.toMap(c -> c.left, c -> c.right));

        Set<Parser.Element> elements = new HashSet<>();
        list.forEach(e -> {
            e.left.forEach(elements::add);
            e.right.forEach(elements::add);
        });


        RewriteSystem<Parser.Element> rewriteSystem = new RewriteSystem<>(rules, SHORTLEX);

        Set<List<Parser.Element>> baseForms = new HashSet<>();
        Paster<Parser.Element> pasteSet = new Paster<>(elements);

        Set<List<Parser.Element>> sugestions = new HashSet<>();
        sugestions.add(new ArrayList<>());

        Set<List<Parser.Element>> newSugestions = new HashSet<>();

        while (sugestions.size() > 0) {
            newSugestions.clear();
            for (List<Parser.Element> sugestion : sugestions) {
                List<Parser.Element> clean = rewriteSystem.getNormForm(sugestion);
                if (baseForms.add(clean)) {
                    System.out.println("Base: "+clean);
                    newSugestions.add(clean);
                }
            }
            sugestions = pasteSet.paste(newSugestions);
        }


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
