package kbs;

import java.util.*;

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
        this.from = from.clone();
        this.to = to.clone();

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
                if (i + start >= from.length || from[i] != from[i + start]) {
                    miss = i;
                    break;
                }
            }
            if (miss == 0) {
                lut[start + 1] = start + 1;
            } else {
                for (int i = same + 1; i <= miss && i+start<lut.length; i++) {
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
                    iter.previous();//next .next() is same
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


    public Set<CriticalPair> getCritical(Rule<T> other){
        T[] f1 = this.from;
        T[] f2 = other.from;
        T[] t1 = this.to;
        T[] t2 = other.to;

        Set<CriticalPair> result= new HashSet<>();
        int start = 0;
        int cur = 0;

        for(int overlap = Math.min(f1.length,f2.length); overlap > 0; overlap--){
            boolean ok = true;
            for (int i = 0; i < overlap && ok; i++) {
                if(!f2[i].equals(f1[f1.length - overlap + i])){
                    ok = false;
                }
            }
            if(ok){
                //Construct from part
                LinkedList<T> critFrom = new LinkedList<>();
                for(int i  = 0; i<f1.length ; i++){
                    critFrom.add(f1[i]);
                }
                for(int i = overlap; i < f2.length; i++){
                    critFrom.add(f2[i]);
                }

                //Construct result by applying "this" first
                LinkedList<T> critTo1 = new LinkedList<>();

                for(int i = 0; i< t1.length ; i++){
                    critTo1.add(t1[i]);
                }
                for(int i = overlap; i < f2.length; i++){
                    critTo1.add(f2[i]);
                }

                //Construct result by applying "other" first
                LinkedList<T> critTo2 = new LinkedList<>();
                for(int i  = 0; i<f1.length - overlap ; i++){
                    critTo2.add(f1[i]);
                }
                for(int i = 0; i < t2.length; i++){
                    critTo2.add(t2[i]);
                }
                result.add(new CriticalPair(critFrom,critTo1,critTo2));
            }
        }


        return result;
    }

    CriticalPair createCriticalPair(LinkedList<T> from, LinkedList<T> to1, LinkedList<T> to2){
        return new CriticalPair( from,  to1, to2);
    }

    public class CriticalPair{
        public final LinkedList<T> from;
        public final LinkedList<T> to1;
        public final LinkedList<T> to2;

        public CriticalPair(LinkedList<T> from, LinkedList<T> to1, LinkedList<T> to2) {
            if(from == null  || to1 ==null || to2 == null){
                throw new IllegalArgumentException("from, to1 and t2 must not be null");
            }

            this.from = from;
            this.to1 = to1;
            this.to2 = to2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CriticalPair that = (CriticalPair) o;

            if (!from.equals(that.from)) return false;
            return (to1.equals(that.to1) && to2.equals(that.to2)) || (to1.equals(that.to2) && to2.equals(that.to1));
        }

        @Override
        public int hashCode() {
            int result = from.hashCode();
            result = 31 * result + to1.hashCode() + to2.hashCode();
            return result;
        }
    }

}
