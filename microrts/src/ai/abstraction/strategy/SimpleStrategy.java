package ai.abstraction.strategy;

import ai.abstraction.strategy.*;

import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.*;
import ai.abstraction.*;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashMap;

public class SimpleStrategy extends AbstractionLayerAI {
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType lightType;
    UnitType baseType;
    UnitType barracksType;

    private final String ruleFileName = "rules-simple.txt";
    private final ArrayList<String> varRepresentations = new ArrayList<String>(Arrays.asList("X", "Y", "Z"));
    private HashMap<String, Integer> unitCount;
    KnowledgeBase kb;
    List<Rule> rules;

    private String debugOutput;

    public SimpleStrategy(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
        readRuleFile();
    }

    public SimpleStrategy(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        lightType = utt.getUnitType("Light");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        readRuleFile();
    }

    private void readRuleFile() {
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

       intepretRuleFile(all.split("\\r?\\n"));
    }

    //a rule must have following format: rule name + ":-" + conditions + "."
    private void intepretRuleFile(String[] rulesInText) {
        rules = new ArrayList<Rule>();
        for (String currentRule : rulesInText) {
            String name = currentRule.substring(0, currentRule.indexOf("(\"")).trim();
            String unitTypeToConsider = currentRule.substring(currentRule.indexOf("(\"") + 2, currentRule.indexOf("\")"));
            String conditionsString = currentRule.substring(currentRule.indexOf(":-") + 2, currentRule.indexOf(".")).trim();
            String[] conditions = conditionsString.split(",");

            //retrieve rule's patterns
            ArrayList<Term> patterns = new ArrayList<Term>();
            for (String condition : conditions) {
                //retrieve functor
                String functor = "";
                if (condition.indexOf("(") != -1)
                    functor = condition.substring(0, condition.indexOf("(")).trim();
                else
                    functor = condition.trim();
                String p1 = "";
                if (functor.equals("idle"))
                    p1 = unitTypeToConsider;
                else
                    p1 = condition.substring(condition.indexOf("(\"") + 2, condition.indexOf("\")"));
                patterns.add(new Term(functor, new String[] {p1}));
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
                effects.add(new Term(functor, new String[] {unitTypeToConsider, target}));
            else
                effects.add(new Term(functor, new String[] {unitTypeToConsider}));

            rules.add(new Rule(patterns, effects, new int[] {1}));
        }
    }

    public PlayerAction getAction(int player, GameState gs) {
        debugOutput = "";
        debugOutput = debugOutput + "Get action for player " + Integer.toString(player) + "\n";
        populateKnowledgeBase(player, gs);
        boolean b = doRuleBasedSystemIteration(player, gs);
        debugOutput+="\n";

        if (b)
            System.out.println(debugOutput);

        return translateActions(player, gs);
    }

    private void populateKnowledgeBase(int player, GameState gs) {
        debugOutput+="Populate KnowledgeBase\n";
        kb = new KnowledgeBase();
        PhysicalGameState pgs = gs.getPhysicalGameState();

        //create a table of possible units for player
        HashMap<String, Integer> ownedUnits = new HashMap<String, Integer>();
        for (UnitType ut : utt.getUnitTypes())
            ownedUnits.put(ut.name, 0);

        //create a table of resources for unit
        HashMap<String, Integer> enoughResourcesUnits = new HashMap<String, Integer>();
        for (UnitType ut : utt.getUnitTypes())
            enoughResourcesUnits.put(ut.name, 0);

        //add all available positive knowledge
        for (Unit u : pgs.getUnits())
            if (u.getPlayer() == player) {
                //idle
                if (gs.getActionAssignment(u) == null) {
                    debugOutput = debugOutput + "\tFound " + u.getType().name + " idle\n";
                    kb.add(new Term("idle", new String[] {u.getType().name}));
                }
                //own
                if (ownedUnits.get(u.getType().name).intValue() == 0) {
                    debugOutput = debugOutput + "\tOwn " + u.getType().name + "\n";
                    kb.add(new Term("own", new String[] {u.getType().name}));
                    ownedUnits.put(u.getType().name, 1);
                }
            }
        for (UnitType ut : utt.getUnitTypes())
            //enoughResourcesFor
            if (gs.getPlayer(player).getResources() >= ut.cost && !ut.isResource)
                if (enoughResourcesUnits.get(ut.name).intValue() == 0) {
                    debugOutput = debugOutput + "\tEnough Resource For " + ut.name + "\n";
                    kb.add(new Term("enoughResourcesFor", new String[] {ut.name}));
                    enoughResourcesUnits.put(ut.name, 1);
                }
    }

    private boolean doRuleBasedSystemIteration(int player, GameState gs) {
        debugOutput+="Do RuleBasedSystemIteration\n";
        debugOutput+="\tFound matching rule(s):\n";
        List<Rule> firedRules = new ArrayList<Rule>();
        for (Rule r : rules) {
            HashMap<String, String> bindings = doUnification(r.getPatterns(), kb);
            if (bindings != null) {
                instantiate(r, bindings);
                firedRules.add(r);
                debugOutput = debugOutput + "\tUnit type: " + r.getEffects().get(0).getParameters()[0] + " - Action code: " + r.getEffects().get(0).getFunctor() + "\n";
            }
        }
        List<Rule> toExecuteRules = arbitrate(firedRules, player, gs);
        return execute(toExecuteRules, player, gs);
    }

    private HashMap<String, String> doUnification(List<Term> patterns, KnowledgeBase kb) {
        if (patterns.size() == 1)
            return doSimpleKBUnification(patterns.get(0), kb);

        HashMap<String, String> finalbindings = new HashMap<String, String>();
        HashMap<String, String> bindings = null;
        Term T = null;
        for (Term t : patterns) {
            T = applyBindings(t, finalbindings);
            if (T.getFunctor() < 0)
                bindings = doSimpleNegationKBUnification(T, kb);
            else
                bindings = doSimpleKBUnification(T, kb);
            if (bindings == null)
                return null;
            finalbindings.putAll(bindings);
        }

        return finalbindings;
    }

    private HashMap<String, String> doSimpleKBUnification(Term T1, KnowledgeBase kb) {
        if (kb == null)
            return null;

        for (Term S : kb.getFacts()) {
            HashMap<String, String> bindings = doSingleTermUnification(T1, S);
            if (bindings != null)
                return bindings;
        }

        return null;
    }

    private HashMap<String, String> doSimpleNegationKBUnification(Term T1, KnowledgeBase kb) {
        if (kb == null)
            return null;

        Term T = new Term(0 - T1.getFunctor(), new String[] {});
        for (Term S : kb.getFacts()) {
            HashMap<String, String> bindings = doSingleTermUnification(T, S);
            if (bindings != null)
                return null;
        }

        return new HashMap<String, String>();
    }

    /*
    *T1 may contains variables
    *T2 must not contain variables
    */
    private HashMap<String, String> doSingleTermUnification (Term T1, Term T2) {
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
                if (Arrays.asList(T1p).contains(s))
                    for (int i = 0; i < T1p.length; i++)
                        if (T1p[i].equals(s))
                            T1p[i] = binding.get(s);
        }

        return T1;
    }

    private void instantiate(Rule r, HashMap<String, String> bindings) {
        for (String s : bindings.keySet())
            if (varRepresentations.contains(s))
                r.replaceVariable(s, bindings.get(s));
            else
                System.out.println("instantiate - How the heck did it get here? pair: " + s + " - " + bindings.get(s));
    }

    //for now, only check if there's enough unit and resource for each rule
    private List<Rule> arbitrate(List<Rule> rulesToConsider, int player, GameState gs) {
        debugOutput+="\tRemove rule(s):\n";
        HashMap<String, ArrayList<Unit>> unitsOccupied = new HashMap<String, ArrayList<Unit>>();
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int availableResource = gs.getPlayer(player).getResources();

        int i = 0;
        while (i < rulesToConsider.size()) {
            List<Term> effects = rulesToConsider.get(i).getEffects();
            String unitType = effects.get(0).getParameters()[0];
            int temp = availableResource;
            for (Term t : effects)
                if (t.getFunctor() == 11 || t.getFunctor() == 12)
                    temp-=utt.getUnitType(t.getParameters()[1]).cost;

            boolean isGood = true;
            if (temp >= 0) {
                if (!unitsOccupied.containsKey(unitType))
                    unitsOccupied.put(unitType, new ArrayList<Unit>());

                isGood = false;
                for (Unit u : pgs.getUnits())
                    if (u.getType().name.equals(unitType) && u.getPlayer() == player && !unitsOccupied.get(unitType).contains(u)) {
                        unitsOccupied.get(unitType).add(u);
                        isGood = true;
                        break;
                    }
            } else
                isGood = false;

            if (!isGood) {//no unit available to carry out the rule
                debugOutput = debugOutput + "\tUnit type: " + unitType + " - Action code: " + Integer.toString(rulesToConsider.get(i).getEffects().get(0).getFunctor()) + "\n"; 
                rulesToConsider.remove(i);
            } else {
                availableResource = temp;
                i++;
            }
        }

        return rulesToConsider;
    }

    private boolean execute(List<Rule> rulesToExecute, int player, GameState gs) {
        debugOutput+="\tFinal conclusion:\n";
        PhysicalGameState pgs = gs.getPhysicalGameState();
        List<Integer> reservedPositions = new LinkedList<Integer>();
        List<Unit> usedUnits = new ArrayList<Unit>();
        boolean executed = false;
        for (Rule r : rulesToExecute)
            for (Term t : r.getEffects()) {
                int functor = t.getFunctor();
                String[] p = t.getParameters();
                switch(functor) {
                    case 10: //amove
                        executed = true;
                        break;
                    case 11: //abuild
                        //Assumptions: - the unit can build the type
                        //             - only build one
                        //             - Strings contain correct information
                        //             - resources are unchanged from last check
                        String unitToBuild = p[0];
                        String unitToBeBuilded = p[1];
                        debugOutput = debugOutput + "\t" + unitToBuild + " builds a " + unitToBeBuilded + "\n";
                        for (Unit u : pgs.getUnits())
                            if (u.getType() == utt.getUnitType(unitToBuild) && u.getPlayer() == player && gs.getActionAssignment(u) == null) 
                                if (!usedUnits.contains(u)) {
                                    usedUnits.add(u);
                                    buildIfNotAlreadyBuilding(u, utt.getUnitType(unitToBeBuilded), u.getX(), u.getY(), reservedPositions, gs.getPlayer(player), pgs);
                                    executed = true;
                                }
                        break;
                    case 12: //atrain. 
                        //Assumptions: - the unit can train the type
                        //             - only train one
                        //             - Strings contain correct information
                        //             - resources are unchanged from last check
                        String unitToTrain = p[0];
                        String unitToBeTrained = p[1];
                        debugOutput = debugOutput + "\t" + unitToTrain + " trains a " + unitToBeTrained + "\n";
                        for (Unit u : pgs.getUnits())
                            if (u.getType() == utt.getUnitType(unitToTrain) && u.getPlayer() == player && gs.getActionAssignment(u) == null) 
                                if (!usedUnits.contains(u)) {
                                    usedUnits.add(u);
                                    train(u, utt.getUnitType(unitToBeTrained));
                                    executed = true;
                                }
                        break;
                    case 13: //aharvest
                        //Assumptions: - the unit can harvest
                        //             - Strings contain correct information
                        //             - harvest will be done to closest resource and stored in closest stockpile
                        String unitToHarvest = p[0];
                        debugOutput = debugOutput + "\t" + unitToHarvest + " harvest\n";
                        for (Unit u : pgs.getUnits())
                            if (u.getType() == utt.getUnitType(unitToHarvest) && u.getPlayer() == player && gs.getActionAssignment(u) == null)
                                if (!usedUnits.contains(u)) {
                                    usedUnits.add(u);
                                    //code within this if statement is copied from LightRush.java
                                    Unit closestBase = null;
                                    Unit closestResource = null;
                                    int closestDistance = 0;
                                    for (Unit u2 : pgs.getUnits()) {
                                        if (u2.getType().isResource) {
                                            int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                                            if (closestResource == null || d < closestDistance) {
                                                closestResource = u2;
                                                closestDistance = d;
                                            }
                                        }
                                    }

                                    closestDistance = 0;
                                    for (Unit u2 : pgs.getUnits()) {
                                        if (u2.getType().isStockpile && u2.getPlayer() == player) {
                                            int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                                            if (closestBase == null || d < closestDistance) {
                                                closestBase = u2;
                                                closestDistance = d;
                                            }
                                        }
                                    }
 
                                    if (closestResource != null && closestBase != null) {
                                        AbstractAction aa = getAbstractAction(u);
                                        executed = true;
                                        if (aa instanceof Harvest) {
                                            Harvest h_aa = (Harvest)aa;
                                            if (h_aa.getTarget() != closestResource || h_aa.getBase()!=closestBase) harvest(u, closestResource, closestBase);
                                        } else {
                                            harvest(u, closestResource, closestBase);
                                        }
                                    }
                                }
                        break;
                    case 14: //aattack
                        //Assumptions: - the unit can attack
                        //             - Strings contain correct information
                        //             - attack will be done to closest enemy
                        String unitToAttack = p[0];
                        debugOutput = debugOutput + "\t" + unitToAttack + " attacks ";
                        for (Unit u : pgs.getUnits())
                            if (u.getType() == utt.getUnitType(unitToAttack) && u.getPlayer() == player && gs.getActionAssignment(u) == null)
                                if (!usedUnits.contains(u)) {
                                    usedUnits.add(u);
                                    //code copied from LightRush.java
                                    Unit closestEnemy = null;
                                    int closestDistance = 0;
                                    for (Unit u2 : pgs.getUnits()) {
                                        if (u2.getPlayer() >= 0 && u2.getPlayer() != player) {
                                            int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                                            if (closestEnemy == null || d < closestDistance) {
                                                closestEnemy = u2;
                                                closestDistance = d;
                                            }
                                        }
                                    }

                                    if (closestEnemy != null) {
                                        debugOutput = debugOutput + closestEnemy.getType().name + "\n";
//                                      System.out.println("LightRushAI.meleeUnitBehavior: " + u + " attacks " + closestEnemy);
                                        attack(u, closestEnemy);
                                        executed = true;
                                    }
                                }
                        break;
                    case 15: //aidle
                        //Assumptions: - the unit can attack
                        //             - Strings contain correct information
                        String unitToIdle = p[0];
                        for (Unit u : pgs.getUnits())
                            if (u.getType() == utt.getUnitType(unitToIdle) && u.getPlayer() == player && gs.getActionAssignment(u) == null)
                                if (!usedUnits.contains(u)) {
                                    usedUnits.add(u);
                                    idle(u);
                                    executed = true;
                                    break;
                                }
                            break;
                    default:
                        System.out.println("execute - What is this?!? functor: " + Integer.toString(functor));
                }
            }

        return executed;
    }

    public void reset() {
        super.reset();
    }

    public AI clone() {
        return new SimpleStrategy(utt, pf);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
}
