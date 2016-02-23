package kbs;

import parser.Parser;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by beardhatcode on 17/02/16.
 */
public class RewriteSystem<T> {

    private List<Rule<T>> rules;

    public RewriteSystem(Map<T[], T[]> rules, Set<T> elements) {
        //Convert Rules to acctual Rule's
        this.rules = rules.keySet().stream().map(e->new Rule<>(e,rules.get(e))).collect(Collectors.toList());
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
            for (Rule<T> rule : rules) {
                doneSomething=rule.applyAll(input)||doneSomething;
            }
        }while (doneSomething);
        return input;
    }

    public void complete(){

    }
}
