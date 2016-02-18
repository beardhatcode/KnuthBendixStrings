package kbs;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.Consumer;

/**
 * Rule of the Rewrite system.
 * String finding algorithm of Knuth-Moris-Pratt is used on linked list to ease working with changing lengths.
 * @author  Robbert Gurdeep Singh
 * @todo handle longer to than from ?
 */
public class Rule<T>  {
    private T from[];
    private T to[];
    private int lut[];

    /**
     * Make a Rule for the rule system
     *
     * *Note*: Rules must not make thing bigger (in the strict sense)
     * And calculate the Knuth-Moris-Pratt Shift table.
     *
     * @param from An array of elements that should be replaced
     * @param to The array by witch the from array should be replaced
     */
    public Rule(T[] from, T[] to) {
        this.from = from;
        this.to = to;

        if (from.length < to.length){
            throw new IllegalArgumentException("The length of the taget should be larger then the source");
        }

        makeKMP();
    }

    /**
     * Make the Knuth-Moris-Pratt Shift table. (helper function)
     */
    private void makeKMP() {
        lut = new int[from.length];
        lut[0] = 1;
        lut[1] = 1;

        int start = 1;
        int same = 0;
        while (start < from.length - 1) {
            int miss = from.length;
            for (int i = 0; i < from.length; i++) {
                if (from[i] != from[i + start]) {
                    miss = i;
                    break;
                }
            }

            if (miss == 0) {
                lut[start + 1] = start + 1;
            } else {
                for (int i = same + 1; i <= miss; i++) {
                    lut[start + i] = start;
                }
            }
            start += lut[miss];

            same = (miss == 0 ? 0 : miss - lut[miss]);
        }
    }


    /**
     * Apply the rule to the given LinkedList. (Will be applied to the given reference)
     * @param input to be rewitten
     * @return true if something has been replaced
     */
    public boolean apply(LinkedList<T> input) {
        ListIterator<T> iter = input.listIterator();
        return applyOnIter(iter);
    }

    /**
     * Apply the rule to the given LinkedList multiple times (Will be applied to the given reference)
     * ! NOT GUARANTEED THAT EVERYTHING WILL BE REPLACED
     * @todo relace everything
     * @param ts
     * @return
     */
    public boolean applyAll(LinkedList<T> ts) {
        boolean hasDone = false;
        ListIterator<T> iter = ts.listIterator();
        while(iter.hasNext()) {
            hasDone =  applyOnIter(iter) || hasDone;
        }

        return applyOnIter(iter);
    }


    /**
     * Execute the replace
     *
     * Works by iterating over the list until a match is found and then using the iterator to
     * replace the match.
     *
     * @param iter active iterator on the linked list
     * @return
     */
    private boolean applyOnIter(ListIterator<T> iter) {
        //Look for an occurrence
        int curPos = 0;
        while (iter.hasNext()) {
            T cur = iter.next();
            if (cur.equals(from[curPos])) {
                curPos++;
                if (curPos == from.length) break;
            } else {
                if (curPos != 0) {
                    //Knuth-Moris-Pratt
                    curPos = curPos - lut[curPos];
                    iter.previous();
                }
            }

        }

        if (curPos != from.length) {
            //No match was found
            return false;
        }

        //Replace the occurrence backwards and remove excess characters
        for (int i = 1; i <= from.length; i++) {
            //move cursor backwards, if first itteration -> same as last next()
            iter.previous();
            if (to.length - i >= 0) {
                iter.set(to[to.length - i]);
            } else {
                iter.remove();
            }
        }

        return true;
    }

}
