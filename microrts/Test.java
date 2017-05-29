import java.io.*;

public class Test {
    public static void main(String [] args) {
       String all = "";
       String line = null;
       String ruleFileName = "rules-simple.txt";

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
       String[] rulesInText = all.split("\\r?\\n");
       System.out.println("readRuleFile output:\n" + all + "\n");

       int count = 0;
       for (String currentRule : rulesInText) {
            count++;
            System.out.println("Rule " + Integer.toString(count));
            String name = currentRule.substring(0, currentRule.indexOf("(\"")).trim();
            String unitTypeToConsider = currentRule.substring(currentRule.indexOf("(\"") + 2, currentRule.indexOf("\")"));
            String conditionsString = currentRule.substring(currentRule.indexOf(":-") + 2, currentRule.indexOf(".")).trim();
            String[] conditions = conditionsString.split(",");

            System.out.println("\tName: " + name);
            System.out.println("\tunitTypeToConsider: " + unitTypeToConsider);
            System.out.println("\tconditionsString: " + conditionsString);

            System.out.println("\tRule's patterns:");
            //retrieve rule's patterns
            for (String condition : conditions) {
                System.out.print("\t\t");
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
                System.out.println("functor: " + functor + " - p1: " + p1);
            }

            System.out.println("\tRule's effects:");
            //retrieve rule's effect
            String[] splitUpperCase = name.split("(?=\\p{Upper})");
            String action = splitUpperCase[1];
            String target = "";
            if (splitUpperCase.length == 3)
                target = splitUpperCase[2];
            String functor = "a" + action.toLowerCase();
            if (target.length() != 0)
                System.out.println("\t\tfunctor: " + functor + " - p1: " + unitTypeToConsider + " - p2: " + target);
            else
                System.out.println("\t\tfunctor: " + functor + " - p1: " + unitTypeToConsider);
        }
    }
}
