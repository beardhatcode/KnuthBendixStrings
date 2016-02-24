package kbs;

import com.sun.xml.internal.ws.util.StringUtils;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

/**
 * Created by beardhatcode on 17/02/16.
 */
public class RuleTest {

    public static final Character[] CHARACTERS = new Character[]{'L'};

    public LinkedList<Character> makeList(String a){
        return new LinkedList<>(a.chars().mapToObj(c -> (char)c).collect(Collectors.toList()));
    }

    public String testRule(String in, String from, String to){
        Rule<Character> rule = makeCharacterRule(from, to);
        LinkedList<Character> list = makeList(in);
        rule.apply(list);
        String out = list.stream().map(Object::toString).reduce((acc, e) -> acc  + e).get();
        System.out.printf("%s -> %s  (by %s -> %s )\n",in,out,from,to);
        return out;
    }

    private Rule<Character> makeCharacterRule(String from, String to) {
        return new Rule<>(makeList(from),makeList(to));
    }

    @Test
    public void testAccept() throws Exception {
        assertEquals("lieselotteISleuk",testRule("lieselotteISNIETleuk","ISNIET","IS"));
        assertEquals("Should do nothing if not found","tandwiel",testRule("tandwiel","ISNIET","IS"));
        assertEquals("Should replace at most once","bababarGERARDlos",testRule("bababarbarbaroslos","barbaros","GERARD"));
        assertEquals("Should replace at most once","RLLLLLLLLL",testRule("LLLLLLLLLLL","LL","R"));
        assertEquals("RSRSS",testRule("SSRRS","SSRR","RSRS"));
        assertFalse(testRule("SSRRSR","RSRS","X").contains("X"));
        assertFalse(testRule("SSSRSR","RSRS","X").contains("X"));


        assertEquals("Should handele inserts correctly","ABC-def-GHIJK",testRule("ABCDEFGHIJK","DEF","-def-"));
        assertEquals("Should handele inserts correctly","-abcdef-GHIJK",testRule("DEFGHIJK","DEF","-abcdef-"));
    }



    @Test
    public void testGetCritical() throws Exception {
        Rule<Character>.CriticalPair expected1,expected2,expected3;

        Rule<Character> a = makeCharacterRule("ABCD","P");
        Rule<Character> b = makeCharacterRule("CDEF","Q");
        Set<Rule<Character>.CriticalPair> critical = a.getCritical(b);
        assertEquals(1,critical.size());
        expected1 = a.createCriticalPair(makeList("ABCDEF"), makeList("ABQ"), makeList("PEF"));
        assertTrue(critical.contains(expected1));

        a = makeCharacterRule("ABXX","P");
        b = makeCharacterRule("XXEF","Q");
        critical = a.getCritical(b);
        assertEquals(2,critical.size());
        expected1 = a.createCriticalPair(makeList("ABXXEF"), makeList("ABQ"), makeList("PEF"));
        expected2 = a.createCriticalPair(makeList("ABXXXEF"), makeList("ABXQ"), makeList("PXEF"));
        assertTrue(critical.contains(expected1));
        assertTrue(critical.contains(expected2));

        a = makeCharacterRule("ABXX","P");
        b = makeCharacterRule("XXEF","Q");
        critical = b.getCritical(a);
        assertEquals(0,critical.size());


        a = makeCharacterRule("XXXXX","P");
        critical = a.getCritical(a);
        assertEquals(4,critical.size());
    }

}