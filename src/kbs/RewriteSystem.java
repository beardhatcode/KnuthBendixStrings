package kbs;


import java.util.*;
import java.util.stream.Collectors;

/**
 * A rewrite systemn that can complete itself
 * @param <T> The type of the "characters" in the text
 */
public class RewriteSystem<T> {

    private final Set<Rule<T>> rules;
    private Set<Rule<T>> completeRules = null;
    private final Comparator<Collection<T>> comparator;

    /**
     * Make a rewrite system with the given comparator and ruleset
     * @param rules      A map of completeRules
     * @param comparator A comparator indicating the sort order
     */
    public RewriteSystem(Map<List<T>, List<T>> rules,Comparator<Collection<T>> comparator) {
        //Convert Rules to actual Rule's, big to small
        //Note that ordering does not belong in Rule, because a rule does not need to
        //know the ordering.
        this.rules = rules.keySet().stream().map(e -> {
            List<T> t2 = rules.get(e);
            return comparator.compare(e,t2) > 0 ? new Rule<>(e, t2) : new Rule<>(t2, e);
        }).collect(Collectors.toSet());
        this.comparator = comparator;
    }


    /**
     * Apply completeRules until a normal form is reached using the rules that set up the system
     *
     * @param pInput list to rewrite
     * @return a rewritten copy of the list
     */
    public List<T> rewrite(List<T> pInput){
        return rewriteWith(pInput,rules);
    }

    /**
     * Returns a rewriten version of the input list the input remains unchanged.
     * @param pInput the list to rewrite
     * @return a rewritten copy of the list
     */
    public List<T> getUniqueNF(List<T> pInput){
        complete();
        return rewriteWith(pInput,completeRules);
    }


    /**
     * Helper function for replacements withs {@see RewriteSystem::rewrite} and {@see getUniqueNF()}
     * @param pInput   the input to rewrite
     * @param ruleSet  the set of rules to use
     * @return a rewritten copy of the input list
     */
    private List<T> rewriteWith(List<T> pInput, Set<Rule<T>> ruleSet) {
        LinkedList<T> input= new LinkedList<>(pInput);
        boolean doneSomething = false;
        do {
            doneSomething=false;
            for (Rule<T> rule : ruleSet) {
                doneSomething=rule.apply(input)||doneSomething;
            }
        }while (doneSomething);
        return input;
    }



    /**
     * Changes the list to its rewriten version (input is changed).
     * Only considers completeRules that are already computed!
     * A call to {@see RewriteSystem::complete()}
     *
     * @param list the list to rewrite
     */
    private void changeToUniqueNF(LinkedList<T> list){
        boolean doneSomething = false;
        do {
            doneSomething=false;
            for (Rule<T> rule : completeRules) {
                doneSomething=rule.apply(list)||doneSomething;
                //Heuristic if a rule was applied, start from te beginning
                //Simple rules are in the beginning of the list due to the order of
                //the TreeSet complete rules
                if(doneSomething) break;
            }
        }while (doneSomething);
    }

    /**
     * Complete the rule system
     *
     * This is done by looking for overlap in the rules and extracting critical pairs from them. From each critical
     * pair, a new rule is made. Rules whose "from" part can be rewritten are removed, they are no longer
     * useful because we choose to always apply the new rule first.
     *
     * This is the "Knuth–Bendix completion algorithm"
     */
    public void complete() {
        if(completeRules != null) {
            return;
        }

        this.completeRules = new TreeSet<>((o1, o2) -> o1.compareTo(o2,comparator));
        this.completeRules.addAll(rules);

        //Using treesets with a special order does not speedup
        Collection<Rule<T>.CriticalPair> criticalPairs = new HashSet<>();


        Collection<Rule<T>> toProcess = new HashSet<>(completeRules);
        while (true){

            //Collect the critical pairs
            for (Rule<T> rule1 : completeRules) {
                //We only need to look at combinations with new completeRules
                for (Rule<T> rule2 : toProcess) {
                    criticalPairs.addAll(rule1.getCritical(rule2));
                    criticalPairs.addAll(rule2.getCritical(rule1));
                }
            }
            toProcess.clear(); //done with these


            //palatalise the first optimisation of the rules
            criticalPairs.parallelStream().forEach(c -> {this.changeToUniqueNF(c.to1);this.changeToUniqueNF(c.to2);});

            //No critical pairs left, we are done
            if (criticalPairs.size() == 0) break;


            for (Rule<T>.CriticalPair criticalPair : criticalPairs) {
                // check if anny new rule applies
                this.changeToUniqueNF(criticalPair.to1);
                this.changeToUniqueNF(criticalPair.to2);

                LinkedList<T> to1 = criticalPair.to1;
                LinkedList<T> to2 = criticalPair.to2;

                int compare = comparator.compare(to1, to2);

                if(compare==0) {
                    //new rule is 0 transformation after further simplification
                    continue;
                }

                List<T> big =   compare > 0 ? to1 : to2;
                List<T> small = compare < 0 ? to1 : to2;
                Rule<T> tRule = new Rule<>(big,small);

                if(completeRules.add(tRule)) {
                    //Rule was new
                    toProcess.add(tRule);
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

    /**
     * Calculate the normal forms of the system.
     *
     * This is done by completing the system and applying the completed rules to the empty sting and
     * every letter occurring in the rules that were supplied at creation time. Then we add every letter
     * occurring in the rules that were supplied at creation time to every newly found normal form.
     * this process is repeated untlil there are no new normal forms found
     *
     * @return a set of unique normal forms to which every input containing only letters that occur in the rules
     * will be reduced to using {@see getUniqueNF()}
     */
    public Set<List<T>> calcNormalForms(){
        complete();
        Set<T> elements = new HashSet<>();
        rules.forEach(e -> {
            e.getFrom().forEach(elements::add);
            e.getTo().forEach(elements::add);
        });


        Set<List<T>> baseForms = new HashSet<>();
        Paster<T> pasteSet = new Paster<>(elements);

        Set<List<T>> suggestion = new HashSet<>();
        suggestion.add(new ArrayList<>());

        Set<List<T>> newSuggestions = new HashSet<>();

        while (suggestion.size() > 0) {
            newSuggestions.clear();
            for (List<T> sugestion : suggestion) {
                List<T> clean = this.getUniqueNF(sugestion);
                if (baseForms.add(clean)) {
                    newSuggestions.add(clean);
                }
            }
            suggestion = pasteSet.paste(newSuggestions);
        }

        return baseForms;
    }


    public Set<Rule<T>> getCompleteRules() {
        complete();
        return completeRules.stream().map(Rule::new).collect(Collectors.toSet());
    }

    public Set<Rule<T>> getRules() {
        return rules.stream().map(Rule::new).collect(Collectors.toSet());
    }

}
