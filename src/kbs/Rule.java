package kbs;

import java.util.*;

/**
 * Rule of the Rewrite system.
 * String finding algorithm of Knuth-Moris-Pratt is used on linked list to ease working with changing lengths.
 * @author  Robbert Gurdeep Singh
 * @todo handle longer to than from ?
 */
public class Rule<T> {
    private List<T> from;
    private List<T> to;
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
    public Rule(List<T> from, List<T> to) {
        this.from = new ArrayList<>(from);
        this.to = new ArrayList<>(to);
        makeKMP();
    }

    public Rule(Rule<T> other) {
        this(other.from,other.to);
    }

    /**
     * Make the Knuth-Moris-Pratt Shift table. (helper function)
     */
    private void makeKMP() {
        int size = from.size();
        lut = new int[size];
        lut[0] = 1;
        lut[1] = 1;
        int start = 1;
        int same = 0;
        while (start < size - 1) {
            int miss =  size;
            for (int i = 0; i < size; i++) {
                if (i + start >= size || from.get(i) != from.get(i + start)) {
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
        int length = from.size();
        while (iter.hasNext()) {
            T cur = iter.next();
            if (cur.equals(from.get(curPos))) {
                curPos++;
                if (curPos == length) break;
            } else {
                if (curPos != 0) {
                    //Knuth-Moris-Pratt
                    curPos = curPos - lut[curPos];
                    iter.previous();//next .next() is same
                }
            }

        }

        if (curPos != length) {
            //No match was found
            return false;
        }

        //Replace the occurrence backwards and remove excess characters
        for (int i = 1; i <= length; i++) {
            //move cursor backwards, if first itteration -> same as last next()
            iter.previous();
            if (to.size() - i >= 0) {
                iter.set(to.get(to.size() - i));
            } else {
                iter.remove();
            }
        }
        for (int i = 0; i < to.size() - length; i++) {
            iter.add(to.get(i));
        }

        return true;
    }


    public Set<CriticalPair> getCritical(Rule<T> other){
        ArrayList<T> f1 = this.from;
        ArrayList<T> f2 = other.from;
        ArrayList<T> t1 = this.to;
        ArrayList<T> t2 = other.to;

        Set<CriticalPair> result= new HashSet<>();
        int start = 0;
        int cur = 0;

        for(int overlap = Math.min(f1.size(), f2.size()); overlap > 0; overlap--){
            boolean ok = true;
            for (int i = 0; i < overlap && ok; i++) {
                if(!f2.get(i).equals(f1.get(f1.size() - overlap + i))){
                    ok = false;
                }
            }
            if(ok){
                //Construct from part
                LinkedList<T> critFrom = new LinkedList<>();
                for(int i = 0; i< f1.size(); i++){
                    critFrom.add(f1.get(i));
                }
                for(int i = overlap; i < f2.size(); i++){
                    critFrom.add(f2.get(i));
                }

                //Construct result by applying "this" first
                LinkedList<T> critTo1 = new LinkedList<>();

                for(int i = 0; i< t1.size(); i++){
                    critTo1.add(t1.get(i));
                }
                for(int i = overlap; i < f2.size(); i++){
                    critTo1.add(f2.get(i));
                }

                //Construct result by applying "other" first
                LinkedList<T> critTo2 = new LinkedList<>();
                for(int i = 0; i< f1.size() - overlap ; i++){
                    critTo2.add(f1.get(i));
                }
                for(int i = 0; i < t2.size(); i++){
                    critTo2.add(t2.get(i));
                }
                if(!critTo1.equals(critTo2)) {
                    result.add(new CriticalPair(critFrom, critTo1, critTo2));
                }
            }
        }


        return result;
    }


    /**
     * find out if this Rule can optimize the given rule
     * @param other
     * @return
     */
    public boolean canOptimize(Rule<T> other){
        if(this.equals(other)) return false;

        ListIterator<T> iter = other.from.listIterator();
        //Look for an occurrence
        int curPos = 0;
        int length = from.size();
        while (iter.hasNext()) {
            T cur = iter.next();
            if (cur.equals(from.get(curPos))) {
                curPos++;
                if (curPos == length) return true;
            } else {
                if (curPos != 0) {
                    //Knuth-Moris-Pratt
                    curPos = curPos - lut[curPos];
                    iter.previous();//next .next() is same
                }
            }

        }

        return false;
    }


    public boolean isEquiv(){
        return to.equals(from);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule<?> rule = (Rule<?>) o;

        if (!from.equals(rule.from)) return false;
        return to.equals(rule.to);

    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Rule{" +
                  from.toString().replaceAll("[ ,\\[\\]]","") +
                " -> " + to.toString().replaceAll("[ ,\\[\\]]","") +
                '}';
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

        @Override
        public String toString() {
            return "CriticalPair{" +
                    "from=" + from +
                    ", to1=" + to1 +
                    ", to2=" + to2 +
                    '}';
        }
    }

}
