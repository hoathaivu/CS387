package ai.abstraction.strategy;

import java.util.ArrayList;
import java.util.List;
import ai.abstraction.strategy.Term;

public KnowledgeBase {
    List<Term> facts;

    public KnowledgeBase() {
        facts = new ArrayList<Term>();
    }

    public void add(Term t) {
        facts.add(t);
    }

    public void clear() {
        facts.clear();
    }

    public List<Term> getFacts() {
        return facts;
    }
}
