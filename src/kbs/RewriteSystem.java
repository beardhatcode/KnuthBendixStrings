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



    public Set<Rule<T>.CriticalPair> complete() {
        Set<Rule<T>.CriticalPair> criticalPairs = new HashSet<>();
        Set<Rule<T>> toProcess = new HashSet<>(rules);
        while (true){
            for (Rule<T> rule1 : rules) {
                for (Rule<T> rule2 : toProcess) {
                    criticalPairs.addAll(rule1.getCritical(rule2));
                    criticalPairs.addAll(rule2.getCritical(rule1));
                }
            }
            toProcess.clear();

            if (criticalPairs.size() == 0) break;
            int useless = 0;
            int added = 0;
            for (Rule<T>.CriticalPair criticalPair : criticalPairs) {
                List<T> to1 = this.apply(criticalPair.to1);
                List<T> to2 = this.apply(criticalPair.to2);

                int compare = comparator.compare(to1, to2);


                if(compare==0) {
                    useless++;
                    continue; //same after further simplification
                }

                List<T> big = compare > 0 ? to1 : to2;
                List<T> small = compare < 0 ? to1 : to2;
                Rule<T> tRule = new Rule<>(big,small);

                //System.out.println("Optimizable: "+rules.stream().filter(tRule::canOptimize).count());


                if(rules.add(tRule)) {
                    System.out.println(" --> "+tRule);
                    toProcess.add(tRule);
                    added++;
                }

            }
            for (Rule<T> toProces : toProcess) {
                rules.removeIf(toProces::canOptimize);
            }

            criticalPairs.clear();
            rules.stream().map(e->e.toString()).sorted((o1, o2) -> o1.length() - o2.length()).forEach(System.out::println);

        }
        rules.stream().map(e->e.toString()).sorted((o1, o2) -> o1.length() - o2.length()).forEach(System.out::println);
        return criticalPairs;
    }
}
