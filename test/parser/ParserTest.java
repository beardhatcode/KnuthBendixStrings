package parser;

import org.junit.Before;
import org.junit.Test;
import parser.Parser;

import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * Test class for {@link Parser}
 */
public class ParserTest {

    private Parser parser;

    @Before
    public void setup() {
        parser = new Parser();
    }

    private String translateLHS(String str) throws ParseException {
        return parser.toString(parser.parse(str).left);
    }

    @Test
    public void element() throws ParseException {
        assertEquals("S", translateLHS("S"));
        assertEquals("S'", translateLHS("S'"));
    }

    @Test
    public void power() throws ParseException {
        assertEquals ("aaa", translateLHS("a^3"));
        assertEquals ("a'a'", translateLHS("a^-2"));
        assertEquals ("a", translateLHS("a'^-1"));
    }

    @Test
    public void reduction() throws ParseException {
        assertEquals ("", translateLHS("SS'"));
        assertEquals ("PT", translateLHS("PQ'RR'QT") );
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidPower() throws ParseException {
        translateLHS("b^0");
    }

    @Test
    public void product() throws ParseException {
        assertEquals ("abc", translateLHS("abc"));
        assertEquals ("ppqqrr", translateLHS("p^2q^2r^2"));
        assertEquals ("p", translateLHS("p^3p'^2"));
    }

    @Test
    public void parentheses() throws  ParseException {
        assertEquals ("a", translateLHS("(a)"));
        assertEquals ("abbbba'", translateLHS("(aba')^4"));
        assertEquals ("aba'b'", translateLHS("(ab)(ba)^-1"));
        assertEquals ("", translateLHS("(AB)(AB)^-1"));
    }

    @Test
    public void rhs() throws ParseException {
        Parser.Result r = parser.parse("ST=R");
        assertEquals ("ST", parser.toString(r.left));
        assertEquals ("R", parser.toString(r.right));

        r = parser.parse("((AB))=(AB)");
        assertEquals ("AB", parser.toString(r.left));
        assertEquals ("AB", parser.toString(r.right));

        r = parser.parse("ab=1");
        assertEquals ("ab", parser.toString(r.left));
        assertEquals ("", parser.toString(r.right));

        r = parser.parse("xyz");
        assertEquals ("xyz", parser.toString(r.left));
        assertEquals ("", parser.toString(r.right));
    }

    @Test(expected=ParseException.class)
    public void parseError1 () throws ParseException {
        translateLHS("p^q");
    }

    @Test(expected=ParseException.class)
    public void parseError2 () throws ParseException {
        translateLHS("((ab)=1");
    }

    @Test(expected=ParseException.class)
    public void parseError3 () throws ParseException {
        translateLHS("ST = 1");
    }

    @Test(expected=ParseException.class)
    public void parseError4 () throws ParseException {
        translateLHS("= ST");
    }


}