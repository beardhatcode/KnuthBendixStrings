package kbs;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by beardhatcode on 23/02/16.
 */
public class RewriteSystemTest {
    public static final Character[] CHARACTERS = new Character[]{'L'};

    Comparator<Collection<Character>> shortLex = (o1, o2) -> {
        if (o1.size() != o2.size()) return o1.size() - o2.size();
        return o1.toString().compareTo(o2.toString());
    };

    @Test
    public void testApply() throws Exception {
        Map<String,String> a= new HashMap<>();
        a.put("RRR","");
        a.put("SSS","");
        a.put("RSRS","");


        Map<List<Character>,List<Character>> b = new HashMap<>();
        a.entrySet().stream().forEach(e -> b.put( strToList(e.getKey()), strToList(e.getValue())));

        RewriteSystem<Character> characterRewriteSystem = new RewriteSystem<>(b, shortLex);
        List<Character> result = characterRewriteSystem.rewrite(strToList("RRRSRSRSSS"));
        assertEquals(Collections.EMPTY_LIST,result);
        result = characterRewriteSystem.rewrite(strToList("LOL"));
        assertEquals(strToList("LOL"),result);
    }

    @Test
    public void testComplete7() throws Exception {

        Map<String,String> a= new HashMap<>();
        a.put("RRR","");
        a.put("SSS","");
        a.put("RSRS","");


        Map<List<Character>,List<Character>> b = new HashMap<>();
        a.entrySet().stream().forEach(e -> b.put( strToList(e.getKey()), strToList(e.getValue())));

        RewriteSystem<Character> characterRewriteSystem = new RewriteSystem<>(b, shortLex);

        Set<Rule<Character>> completeRules = characterRewriteSystem.getCompleteRules();
        assertEquals(7,completeRules.size());
    }


    @Test
    public void testComplete9() throws Exception {

        Map<String,String> a= new HashMap<>();
        a.put("SSSSSSS","");
        a.put("TT","");
        a.put("SSSSTSSSSTSSSSTSSSST","");
        a.put("STSTST","");


        Map<List<Character>,List<Character>> b = new HashMap<>();
        a.entrySet().stream().forEach(e -> b.put( strToList(e.getKey()), strToList(e.getValue())));

        RewriteSystem<Character> characterRewriteSystem = new RewriteSystem<>(b, shortLex);

        Set<Rule<Character>> completeRules = characterRewriteSystem.getCompleteRules();
        assertEquals(32,completeRules.size());
    }


    private List<Character> strToList(String key) {
        ArrayList<Character> result = new ArrayList<>(key.length());
        for (Character character : key.toCharArray()) {
            result.add(character);
        }
        return result;
    }
}

