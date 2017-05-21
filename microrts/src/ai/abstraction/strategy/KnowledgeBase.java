package ai.abstraction.strategy;

import java.util.ArrayList;
import java.util.List;
import ai.abstraction.strategy.Term;

public KnowledgeBase {
    List<Term> facts;

    public KnowledgeBase() {
        facts = new ArrayList<Term>();
    }

    public add(Term t) {
        facts.add(t);
    }

    public clear() {
        facts.clear();
    }
}
