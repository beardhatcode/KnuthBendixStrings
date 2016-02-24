package kbs;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by beardhatcode on 17/02/16.
 */
public class RewriteSystem<T> {

    private Set<Rule<T>> rules;
    private Comparator<Collection<T>> comparator;

    public RewriteSystem(Map<List<T>, List<T>> rules,Comparator<Collection<T>> comparator) {
        //Convert Rules to acctual Rule's
        this.rules = rules.keySet().stream().map(e->new Rule<>(e,rules.get(e))).collect(Collectors.toSet());
        this.comparator = comparator;
    }




    /**
     * Apply rules until normal form
     * @todo test
     * @param pInput
     * @return
     */
    public List<T> apply(List<T> pInput){
        LinkedList<T> input= new LinkedList<>(pInput);
        boolean doneSomething = false;
        do {
            doneSomething=false;
            for (Rule<T> rule : rules) {
                doneSomething=rule.apply(input)||doneSomething;
            }
        }while (doneSomething);
        return input;
    }

    public void complete(){

    }
}
