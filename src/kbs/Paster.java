package kbs;

import java.util.*;

/**
 * Class that creates every possible outcome of adding a letter of the baseset to the given set of lists
 * Created by Robbert Gurdeep Singh on 24/02/16.
 */
public class Paster<T> {
    private final HashSet<T> base = new HashSet<>();


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
