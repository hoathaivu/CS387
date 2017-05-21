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

/*
Predefined predicates' values:
1 - idle
2 - own(UnitType)
3 - enoughResourcesFor(UnitType)
4 - ~idle
5 - ~own(UnitType)
6 - ~enoughResourcesFor(UnitType)
*/

public class SimpleStrategy extends AbstractionLayerAI {
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType lightType;

    String[] rulesInText;
    private final String ruleFileName = "rules-simple.txt";
    KnowledgeBase kb;

    public SimpleStrategy(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
    }

    public SimpleStrategy(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        lightType = utt.getUnitType("Light");
    }

    public void reset() {
        super.reset();
    }

    public AI clone() {
        return new LightRush(utt, pf);
    }

    public PlayerAction getAction(int player, GameState gs) {

    }

    public void readRuleFile() throws Exception {
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

       rulesInText = all.split("\\r?\\n");
    }

    //a rule must have following format: rule name + ":-" + conditions + "."
    public void intepretRuleFile() {
        for (String currentRule : rulesInText) {
            String name = currentRules.substring(0, currentRules.indexOf(":-")).trim();
            String conditionsString = currentRules.substring(currentRules.indexOf(":-") + 2, currentRule.indexOf(".")).trim();
            String[] conditions = conditionsString.split(",");
            ArrayList<Term> patterns = new ArrayList<Term>();
            for (String condition : conditions) {
                condition = condition.trim();
                if (condition.charAt(0) == '~')
            }
        }
    }

   public void populateKnowledgeBase(GameState gs) {
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
                kb.add(new Term(1, u.getPlayer(), u.getType().name));
            //own
            if (playersOwnedUnits.get(u.getPlayer()).get(u.getType().name).intValue() == 0) {
                kb.add(new Term(2, u.getPlayer(), u.getType().name));
                playersOwnedUnits.get(u.getPlayer()).put(u.getType().name, 1);
            }
            //enoughResourcesFor
            if (gs.getPlayer(u.getPlayer()).getResources() >= u.getType().cost)
                if (playersEnoughResourcesUnits.get(u.getPlayer()).get(u.getType().name).intValue() == 0) {
                    kb.add(new Term(3, u.getPlayer(), u.getType().name));
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
