package ai.abstraction.strategy;

import ai.abstraction.strategy.Term;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class Rule {
    List<Term> patterns;
    List<Term> effects;
    int[] effectTypes;
    
    public Rule(List<Term> p, List<Term> e, int[] et) {
        patterns = p;
        effects = e;
        effectTypes = et;
    }

    public List<Term> getPatterns() {
        return patterns;
    }

    public List<Term> getEffects() {
        return effects;
    }

    public int[] getEffectTypes() {
        return effectTypes;
    }

    public void replaceVariable(String var, String val) {
        for (Term t : patterns)
            t.replaceVariable(var, val);
        for (Term t : effects)
            t.replaceVariable(var, val);
    }
}
