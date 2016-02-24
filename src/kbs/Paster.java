package kbs;

import java.util.*;

/**
 * Created by beardhatcode on 24/02/16.
 */
public class Paster<T> {
    private HashSet<T> base = new HashSet<>();


    public Paster(Collection<T> base) {
        this.base.addAll(base);
    }


    public Set<List<T>> paste(Set<List<T>> current) {
        HashSet<List<T>> newSet = new HashSet<>();
        for (List<T> cur : current) {
            for (T end : base) {
                ArrayList<T> tmp = new ArrayList<>(cur.size() + 1);
                tmp.addAll(cur);
                tmp.add(end);
                newSet.add(tmp);
            }
        }
        return newSet;
    }

}
