package kbs;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by beardhatcode on 17/02/16.
 */
public class RewriteSystem<T> {

    private Set<Rule<T>> rules;
    private Set<Rule<T>> completeRules = null;
    private Comparator<Collection<T>> comparator;

    /**
     * Make a rewritesystem with the given comparator and ruleset
     * @param rules      A map of completeRules
     * @param comparator A comparator indicating the sort order
     */
    public RewriteSystem(Map<List<T>, List<T>> rules,Comparator<Collection<T>> comparator) {
        //Convert Rules to actual Rule's, big to small
        //Note that ordering does not belong in Rule, because a rule does not need to
        //know the ordering.
        this.rules = rules.keySet().stream().map(e -> {
            List<T> t1 = e;
            List<T> t2 = rules.get(t1);
            return comparator.compare(t1,t2) > 0 ? new Rule<>(t1, t2) : new Rule<>(t2, t1);
        }).collect(Collectors.toSet());
        this.comparator = comparator;
    }




    /**
     * Apply completeRules until a normal form is reached
     *
     * @param pInput
     * @return
     */
    public List<T> rewrite(List<T> pInput){
        return rewriteWith(pInput,rules);
    }

    public List<T> getNormForm(List<T> pInput){
        complete();
        return rewriteWith(pInput,completeRules);
    }

    private List<T> rewriteWith(List<T> pInput, Set<Rule<T>> r) {
        LinkedList<T> input= new LinkedList<>(pInput);
        boolean doneSomething = false;
        do {
            doneSomething=false;
            for (Rule<T> rule : r) {
                doneSomething=rule.apply(input)||doneSomething;
            }
        }while (doneSomething);
        return input;
    }


    public void complete() {
        if(completeRules != null) {
            return;
        }

        this.completeRules = new HashSet<>();
        this.completeRules.addAll(rules);

        Set<Rule<T>.CriticalPair> criticalPairs = new HashSet<>();
        Set<Rule<T>> toProcess = new HashSet<>(completeRules);
        while (true){

            //Find th critical pairs
            for (Rule<T> rule1 : completeRules) {
                //We only need to look at combinations with new completeRules
                for (Rule<T> rule2 : toProcess) {
                    criticalPairs.addAll(rule1.getCritical(rule2));
                    criticalPairs.addAll(rule2.getCritical(rule1));
                }
            }
            toProcess.clear(); //done with these

            //No critical pairs left
            if (criticalPairs.size() == 0) break;

            int useless = 0;
            int added = 0;
            for (Rule<T>.CriticalPair criticalPair : criticalPairs) {
                List<T> to1 = this.getNormForm(criticalPair.to1);
                List<T> to2 = this.getNormForm(criticalPair.to2);

                int compare = comparator.compare(to1, to2);

                if(compare==0) {
                    //new rule is 0 transformation after further simplification
                    useless++;
                    continue;
                }

                List<T> big =   compare > 0 ? to1 : to2;
                List<T> small = compare < 0 ? to1 : to2;
                Rule<T> tRule = new Rule<>(big,small);

                if(completeRules.add(tRule)) {
                    //Rule was new
                    toProcess.add(tRule);
                    added++;
                }

            }

            //Rules of which the "from" part can be rewritten can be removed, if the new rule
            //the rule can never be applied because the new rule will rewrite it first (we choose this)
            for (Rule<T> toProces : toProcess) {
                completeRules.removeIf(toProces::canOptimize);
            }

            //Reuse the critical pairs set
            criticalPairs.clear();

        }

    }

    public Set<Rule<T>> getCompleteRules() {
        complete();
        return completeRules.stream().map(Rule::new).collect(Collectors.toSet());
    }

    public Set<Rule<T>> getRules() {
        return rules.stream().map(Rule::new).collect(Collectors.toSet());
    }
}
