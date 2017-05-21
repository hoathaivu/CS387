package ai.abstraction.strategy;

class Term {
    int functor;
    int player;
    String[] parameters;

    public Term(int functor) {
        this.functor = functor;
        parameters = new String[0];
    }

    public Term(int functor, int player, String p1) {
        this.functor = functor;
        this.player = player;
        parameters = new String[] {p1};
    }

    public int getFunctor() {
        return functor;
    }

    public int getPlayer() {
        return player;
    }

    public String[] getParameters() {
        return parameters;
    }
}
