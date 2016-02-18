package kbs;

import org.junit.Test;

import java.util.LinkedList;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

/**
 * Created by beardhatcode on 17/02/16.
 */
public class RuleTest {

    public LinkedList<Character> makeList(String a){
        return new LinkedList<>(a.chars().mapToObj(c -> (char)c).collect(Collectors.toList()));
    }

    public String testRule(String in, String from, String to){
        Character a[] = new Character[]{'L'};
        Rule<Character> rule = new Rule<>(makeList(from).toArray(a),makeList(to).toArray(a));
        LinkedList<Character> list = makeList(in);
        rule.apply(list);
        String out = list.stream().map(e->e.toString()).reduce((acc, e) -> acc  + e).get();
        System.out.printf("%s -> %s  (by %s -> %s )\n",in,out,from,to);
        return out;
    }

    @Test
    public void testAccept() throws Exception {
        assertEquals("lieselotteISleuk",testRule("lieselotteISNIETleuk","ISNIET","IS"));
        assertEquals("Should do nothing if not found","tandwiel",testRule("tandwiel","ISNIET","IS"));
        assertEquals("Should replace at most once","bababarGERARDlos",testRule("bababarbarbaroslos","barbaros","GERARD"));
        assertEquals("Should replace at most once","RLLLLLLLLL",testRule("LLLLLLLLLLL","LL","R"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testName() throws Exception {
        testRule("Robbert is tof","tof","awesome");
    }

    @Test
    public void testGetLut() throws Exception {

    }
}