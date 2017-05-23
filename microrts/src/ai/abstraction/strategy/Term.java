package ai.abstraction.strategy;

class Term {
    int functor;
    String[] parameters;
    private static final HashMap<String, Integer> functorIntepretation = initializeFunctorIntepretation();

    private static HashMap<String, Integer> initializeFunctorIntepretation() {
        functorIntepretation = new HashMap<String, Integer>();
        functorIntepretation.put("idle", 1);
        functorIntepretation.put("~idle", -1);
        functorIntepretation.put("own", 2);
        functorIntepretation.put("~own", -2);
        functorIntepretation.put("enoughResourcesFor", 3);
        functorIntepretation.put("~enoughResourcesFor", -3);

        //possible actions
        functorIntepretation.put("amove", 10);
        functorIntepretation.put("atrain", 11);
        functorIntepretation.put("abuild", 12);
        functorIntepretation.put("aharvest", 13);
        functorIntepretation.put("aattack", 14);
        functorIntepretation.put("aidle", 15);

        return functorIntepretation;
    }

    public Term(String functorS) {
        functor = functorIntepretation.get(functorS);
        parameters = new String[0];
    }

    //status (e.g. idle, enoughResourcesFor) or actions whose targets are determined by system
    public Term(String functorS, String player, String p1) {
        functor = functorIntepretation.get(functorS);
        parameters = new String[] {player, p1};
    }

    //action (e.g. attack) whose targets are determined by player
    public Term(String functorS, String player, String p1, String p2) {
        functor = functorIntepretation.get(functorS);
        parameters = new String[] {player, p1, p2};
    }

    public int getFunctor() {
        return functor;
    }

    public String[] getParameters() {
        return parameters;
    }
}
