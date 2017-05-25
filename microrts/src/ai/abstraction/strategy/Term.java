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

    public Term(String functorS, String[] p) {
        functor = functorIntepretation.get(functorS);
        parameters = p;
    }

    public Term(int functor, String[] p) {
        this.functor = functor;
        parameters = p;
    }

    public int getFunctor() {
        return functor;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] p) {
        parameters = p;
    }

    public Term clone() {
        Term T = new Term(functor, parameters);
        return T;
    }
}
