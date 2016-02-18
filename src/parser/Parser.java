package parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts a string to a list of group elements. Strings are of the form
 * <pre>
 *     ab^4a
 *     (ST^2)^-1SST'=1
 *     ((A^2)B)^-1=((B^2)A)^-1
 * </pre>
 * Parentheses are allowed, accents after a single letter denotes inversion (and is equivalent to ^-1). No parentheses
 * in exponents, zero exponent not allowed. Spaces are not allowed. The right hand side, if present is inverted and concatenated to the left hand side. <p>
 * <p>
 * The lists of group elements are always 'reduced': there is never a sub-list of the form X X^-1
 */
public class Parser {

   /**
     * DTO which represents a single group element.
     */
    public static class Element {
        public char ch;  // generator letter
        public boolean inverted;  // inverted if true

        public Element(char ch, boolean inverted) {
            this.ch = ch;
            this.inverted = inverted;
        }

        private boolean isInverseOf(Element other) {
            return this.ch == other.ch && this.inverted == !other.inverted;
        }

       @Override
       public String toString() {
           return ch + (inverted ? "\'":"");
       }

       @Override
       public boolean equals(Object o) {
           if (this == o) return true;
           if (o == null || getClass() != o.getClass()) return false;

           Element element = (Element) o;

           if (ch != element.ch) return false;
           return inverted == element.inverted;

       }

       @Override
       public int hashCode() {
           int result = (int) ch;
           result = 31 * result + (inverted ? 1 : 0);
           return result;
       }
   }

    // PROCESS LISTS OF ELEMENTS
    // =========================

    private static ArrayList<Element> invert(ArrayList<Element> list) {
        int size = list.size();
        if (size == 0) {
            return list;
        }

        ArrayList<Element> result = new ArrayList<>(size);
        for (int i = size - 1; i >= 0; i--) {
            Element el = list.get(i);
            result.add(new Element(el.ch, !el.inverted));
        }
        return result;
    }

    private static ArrayList<Element> concatenate(ArrayList<Element> left, ArrayList<Element> right) {
        int leftEnd = left.size();
        if (leftEnd == 0) {
            return right;
        }

        int rightSize = right.size();
        if (rightSize == 0) {
            return left;
        }

        int rightStart = 0;

        while (leftEnd > 0 && rightStart < rightSize && left.get(leftEnd-1).isInverseOf(right.get(rightStart))) {
            leftEnd--;
            rightStart++;
        }
        ArrayList<Element> result = new ArrayList<>(leftEnd + rightSize - rightStart);
        for (int i = 0; i < leftEnd; i++) {
            result.add (left.get(i));
        }
        for (int i = rightStart; i < rightSize; i++) {

            result.add (right.get(i));
        }
        return result;
    }

    /**
     * Only used for testing
     */
    static String toString (List<Element> list) {
        StringBuilder str = new StringBuilder(list.size());
        for (Element element : list) {
            str.append (element.ch);
            if (element.inverted) {
                str.append ('\'');
            }
        }
        return str.toString();
    }

    // PARSER
    // =======

    private String line; // current line being parsed

    private int pos; // current position within current line

    private int length; // length of current line

    /**
     * Parses the next element. String is known to start with a letter.
     */
    private Element element () throws ParseException {
        char name = line.charAt(pos);
        if (!Character.isLetter(name)) {
             throw new ParseException("Letter expected",pos);
        }
        pos ++;
        if (pos == length) {
            return new Element (name, false);
        }
        if (line.charAt(pos) == '\'') {
            pos ++;
            return new Element (name, true);
        } else {
            return new Element (name, false);
        }
    }

    /**
     * Parses a number (used as exponent)
     */
    private int number() throws ParseException {
        boolean negative = pos < length && line.charAt(pos) == '-';
        if (negative) {
            pos++;
        }
        int start = pos;
        while (pos < length && line.charAt(pos) >= '0' && line.charAt(pos) <= '9') {
            pos ++;
        }
        if (pos == start) {
           throw new ParseException("Number expected", pos);
        }

        int result =  Integer.parseInt(line.substring(start, pos));
        return negative ? - result : result;
    }

    /**
     * Parses a factor. Returns null if end of line is reached or '='.
     */
    private ArrayList<Element> factor() throws ParseException {
        if (pos == length || line.charAt(pos) == '=' || line.charAt(pos) == ')') {
            return null;
        }

        ArrayList<Element> result;
        if (line.charAt(pos) == '(') {
            // parenthesised expression
            pos ++;
            result = expression();
            if (pos == length ||  line.charAt(pos) != ')') {
                throw new ParseException("')' expected", pos);
            }
            pos ++;
        } else {
            result = new ArrayList<>(1);
            result.add (element());
        }

        if (pos != length && line.charAt(pos) == '^') {
            pos ++;
            int exponent = number();
            if (exponent == 0) {
                throw new IllegalArgumentException("Zero exponent not allowed");
            }

            if (exponent < 0) {
                result = invert(result);
                exponent = - exponent;
            }
            // TODO: use repeated squaring?
            ArrayList<Element> power = result;
            for (;exponent > 1; exponent--) {
                power = concatenate(power, result);
            }
            result = power;
        }

        return result;
    }

    /**
     * Parses an entire expression.
     */
    private ArrayList<Element> expression () throws ParseException {
        ArrayList<Element> result = factor();
        if (result == null) {
            throw new ParseException("Start of expression expected", pos);
        }
        ArrayList<Element> factor = factor();
        while (factor != null) {
            result = concatenate(result, factor);
            factor = factor();
        }
        return result;
    }

    /**
     * Carries the result of the {@link #parse(String)} method. Left hand side
     * and right hand side are lists of elements, representing products of group
     * elements.
     */
    public static class Result {
        /** Left hand side */
        public List<Element> left;

        /** Right hand side. May be an empty list. */
        public List<Element> right;

        private Result(List<Element> left, List<Element> right) {
            this.left = left;
            this.right = right;
        }
    }


    /**
     * Converts an input string to a {@link Result} object
     * @throws ParseException when the line does not have the correct syntax
     */
    public Result parse (String line) throws ParseException {
        this.line = line;
        this.pos = 0;
        this.length = line.length();
        List<Element>  left = expression ();
        List<Element> right= Collections.emptyList();
        if (pos == length) {
            return new Result(left, right);
        }
        if (line.charAt(pos) == '=') {
            pos ++;
            if (pos != length && line.charAt(pos) == '1') { // special case ...=1
                pos ++;
            } else {
                right = expression();
            }
        }
        if (pos==length) {
            return new Result(left,right);
        } else {
            throw new ParseException("Premature end of line", pos);
        }
    }

}
