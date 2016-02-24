package kbs;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by beardhatcode on 24/02/16.
 */
public class PasterTest {

    @Test
    public void testPaste() throws Exception {

        Paster<Integer> pasteSet = new Paster<>(new ArrayList<>(Arrays.asList(1,2,3)));
        Set<List<Integer>> data = Arrays.asList(7,8,9).stream().map(Arrays::asList).collect(Collectors.toSet());

        data =  pasteSet.paste(data);
        Set<List<Integer>> expected = new HashSet<>();
        for (int i = 1; i <= 3; i++) {
            for (int j = 7; j <= 9; j++) {
                expected.add(Arrays.asList(j,i));
            }
        }

        assertEquals(data,expected);

    }
}