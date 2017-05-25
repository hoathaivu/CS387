package ai.abstraction.strategy;

import ai.abstraction.strategy.*;

import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.*;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.ParameterSpecification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class SimpleStrategy extends AbstractionLayerAI {
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType lightType;

    private final String ruleFileName = "rules-simple.txt";
    private final String varRepresentations = "X";
    KnowledgeBase kb;
    List<Rule> rules;

    public SimpleStrategy(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
        readRuleFile();
    }

    public SimpleStrategy(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        lightType = utt.getUnitType("Light");
        readRuleFile();
    }

    public void reset() {
        super.reset();
    }

    public AI clone() {
        return new SimpleStrategy(utt, pf);
    }

    public PlayerAction getAction(int player, GameState gs) {
        //populateKnowledgeBase(gs);
        populateKnowledgeBase(player, gs);
    }

    private void readRuleFile() throws Exception {
       String all = "";
       String line = null;

       try {
           FileReader fr = new FileReader(ruleFileName);
           BufferedReader br = new BufferedReader(fr);
           while ((line = br.readLine()) != null) {
               if (line.trim().length() > 0 && line.trim().charAt(0) != '#')
                   all = all + line + "\n";
           }
           br.close();
       } catch(FileNotFoundException ex) {
           System.out.println("Unable to open file '" + ruleFileName + "'");
       } catch (IOException ex) {
           ex.printStackTrace();
       }

       all = all.replaceAll("(?m)^[ \t]*\r?\n", "");
       System.out.println(all);

       intepretRuleFile(all.split("\\r?\\n"));
    }

    //a rule must have following format: rule name + ":-" + conditions + "."
    private void intepretRuleFile(String rulesInText) {
        rules = new ArrayList<Rule>();
        for (String currentRule : rulesInText) {
            String name = currentRule.substring(0, currentRules.indexOf("(\"")).trim();
            String unitTypeToConsider = currentRule.substring(currentRule.indexOf("(\"") + 1, currentRule.indexOf("\")"));
            String conditionsString = currentRule.substring(currentRules.indexOf(":-") + 2, currentRule.indexOf(".")).trim();
            String[] conditions = conditionsString.split(",");

            //retrieve rule's patterns
            ArrayList<Term> patterns = new ArrayList<Term>();
            for (String condition : conditions) {
                //retrieve functor
                String functor = condition.substring(0, condition.indexOf("("));
                String p1 = "";
                if (functor.contains("idle"))
                    p1 = unitTypeToConsider;
                else
                    p1 = condition.substring(condition.indexOf("(") + 1, condition.indexOf(")"));
                patterns.add(new Term(functor, new String[] {"X", p1}));
            }

            //retrieve rule's effect
            ArrayList<Term> effects = new ArrayList<Term>();
            String[] splitUpperCase = name.split("(?=\\p{Upper})");
            String action = splitUpperCase[1];
            String target = "";
            if (splitUpperCase.length == 3)
                target = splitUpperCase[2];
            String functor = "a" + action.toLowerCase();
            if (target.length() != 0)
                effects.add(new Term(functor, new String[] {"X", unitTypeToConsider, target}));
            else
                effects.add(new Term(functor, new String[] {"X", unitTypeToConsider}));

            rules.add(new Rule(patterns, effects, new int[] {1}));
        }
    }

    private void populateKnowledgeBase(int player, GameState gs) {
        kb = new KnowledgeBase();
        PhysicalGameState pgs = gs.getPhysicalGameState();

        //create a table of possible units for player
        HashMap<String, Integer> ownedUnits = new HashMap<String, Integer>();
        for (UnitType ut : utt.getUnitTypes())
            ownedUnits.put(ut.name, 0);

        //create a table of resources for unit
        <String, Integer> enoughResourcesUnits = new HashMap<String, Integer>();
        for (UnitType ut : utt.getUnitTypes())
            enoughResourcesUnits.put(ut.name, 0);

        //add all available positive knowledge
        for (Unit u : pgs.getUnits())
            if (u.getPlayer() == player)
                //idle
                if (gs.getActionAssignment(u) == null)
                    kb.add(new Term("idle", new String[] {Integer.toString(player), u.getType().name}));
                //own
                if (ownedUnits.get(u.getType().name).intValue() == 0) {
                    kb.add(new Term("own", new String[] {Integer.toString(player), u.getType().name}));
                    ownedUnits.put(u.getType().name, 1);
                }
                //enoughResourcesFor
                if (gs.getPlayer(player).getResources() >= u.getType().cost)
                    if (enoughResourcesUnits.get(u.getType().name).intValue() == 0) {
                        kb.add(new Term("enoughResourcesFor", new String[] {Integer.toString(player), u.getType().name}));
                        enoughResourcesUnits.put(u.getType().name, 1);
                    }
    }

/*
    private void populateKnowledgeBase(GameState gs) {
        kb = new KnowledgeBase();
        PhysicalGameState pgs = gs.getPhysicalGameState();

        //create a table of possible units for all players
        List<HashMap<String, Integer>> playersOwnedUnits = new ArrayList<HashMap<String, Integer>>();
        HashMap<String, Integer> ownedUnits = null;
        for (int i = 0; i < pgs.getPlayers().size; i++) {
            ownedUnits = new HashMap<String, Integer>();
            for (UnitType ut : utt.getUnitTypes())
                ownedUnits.put(ut.name, 0);
            playersOwnedUnits.add(ownedUnits);
        }

        //create a table of resources for unit
        List<HashMap<String, Integer>> playersEnoughResourcesUnits = new ArrayList<HashMap<String, Integer>>();
        HashMap<String, Integer> enoughResourcesUnits = null;
        for (int i = 0; i < pgs.getPlayers().size; i++) {
            enoughResourcesUnits = new HashMap<String, Integer>();
            for (UnitType ut : utt.getUnitTypes())
                enoughResourcesUnits.put(ut.name, 0);
            playersEnoughResourcesUnits.add(enoughResourcesUnits);
        }

        //add all available positive knowledge
        for (Unit u : pgs.getUnits()) {
            //idle
            if (gs.getActionAssignment(u) == null)
                kb.add(new Term("idle", new String[] {Integer.toString(u.getPlayer()), u.getType().name}));
            //own
            if (playersOwnedUnits.get(u.getPlayer()).get(u.getType().name).intValue() == 0) {
                kb.add(new Term("own", new String[] {Integer.toString(u.getPlayer()), u.getType().name}));
                playersOwnedUnits.get(u.getPlayer()).put(u.getType().name, 1);
            }
            //enoughResourcesFor
            if (gs.getPlayer(u.getPlayer()).getResources() >= u.getType().cost)
                if (playersEnoughResourcesUnits.get(u.getPlayer()).get(u.getType().name).intValue() == 0) {
                    kb.add(new Term("enoughResourcesFor", new String[] {Integer.toString(u.getPlayer()), u.getType().name}));
                    playersEnoughResourcesUnits.get(u.getPlayer()).put(u.getType().name, 1);
                }
        }
    }
*/

    private HashMap<String, String> doUnification(List<Term> patterns, KnowledgeBase kb) {
        if (patterns.size() == 1)
            return doSimpleKBUnification(patterns.get(0), kb);

        HashMap<String, String> finalbinding = new HashMap<String, String>();
        for (Term T : patterns) {
            
        }

        return finalbinding;
    }

    private HashMap<String, String> doSimpleKBUnification(Term T1, KnowledgeBase kb) {
        if (kb == null)
            return null;

        for (Term S : kb.getFacts()) {
            String[] b = doSingleTermUnification(T1, S);
            if (bindings != null) {
                HashMap<String, String> bindings = new HashMap<String, String>();
                String[] T1p = T1.getParameters();
                for (int i = 0; i < b.length; i++)
                    if (varRepresentations.contains(T1p[i]))
                        bindings.put(T1p[i], b[i]);
                return bindings;
            }
        }

        return null;
    }

    private HashMap<String, String> doAndClauseKBUnification(Term T1, Term T2, KnowledgeBase kb) {
        if (kb == null)
            return null;

        for (Term S1 : kb.getFacts()) {
            HashMap<String, String> bindings1 = doSingleTermUnification(T1, S1);
            if (bindings1 != null) {
                Term T = applyBindings(T2, bindings1);
                for (Term S2 : kb.getFacts()) {
                    HashMap<String, String> bindings2 = doSingleTermUnification(T, S2);
                    if (bindings2 != null)
                        return bindings2;
                }
            }
        }

        return null;
    }

    /*
    *T1 may contains variables
    *T2 must not contain variables
    */
    private HashMap<String, String> doSingleTermUnification (Term T1, TermT2) {
        if (T1 == null || T2 == null)
            return null;

        if (T1.getFunctor() != T2.getFunctor())
            return null;

        String[] p1 = T1.getParameters();
        String[] p2 = T2.getParameters();
        if (p1.length != p2.length)
            return null;

        HashMap<String, String> bindings = new HashMap<String, String>();
        for (int i = 0; i < p1.length; i++)
            if (varRepresentations.contains(p1[i]))
                bindings.put(p1[i], p2[i]);
            else if (!p1[i].equals(p2[i]))
                return null;

        return bindings;
    }

    private Term applyBindings(Term T, HashMap<String, String> binding) {
        Term T1 = T.clone();

        if (!binding.isEmpty()) {
            String[] T1p = T1.getParameters();
            for (String s : binding.keySet())
                if (!Arrays.asList(T1p).contains(s))
                    return null;
                else
                    for (int i = 0; i < T1p.length; i++)
                        if (T1p[i].equals(s))
                            T1p[i] = binding.get(s);
        }

        return T1;
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
}
