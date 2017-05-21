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
       System.out.println(all);
    }
}
