package ai.abstraction.strategy;

import ai.abstraction.strategy.KnowledgeBase;
import ai.abstraction.strategy.Term;

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
import java.util.ArrayList;

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
        populateKnowledgeBase(gs);
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
                patterns.add(new Term(functor, "X", p1));
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
                effects.add(new Term(functor, "X", unitTypeToConsider, target));
            else
                effects.add(new Term(functor, "X", unitTypeToConsider));

            rules.add(new Rule(patterns, effects, new int[] {1}));
        }
    }

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
                kb.add(new Term("idle", Integer.toString(u.getPlayer()), u.getType().name));
            //own
            if (playersOwnedUnits.get(u.getPlayer()).get(u.getType().name).intValue() == 0) {
                kb.add(new Term("own", Integer.toString(u.getPlayer()), u.getType().name));
                playersOwnedUnits.get(u.getPlayer()).put(u.getType().name, 1);
            }
            //enoughResourcesFor
            if (gs.getPlayer(u.getPlayer()).getResources() >= u.getType().cost)
                if (playersEnoughResourcesUnits.get(u.getPlayer()).get(u.getType().name).intValue() == 0) {
                    kb.add(new Term("enoughResourcesFor", Integer.toString(u.getPlayer()), u.getType().name));
                    playersEnoughResourcesUnits.get(u.getPlayer()).put(u.getType().name, 1);
                }
        }
   }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
}
