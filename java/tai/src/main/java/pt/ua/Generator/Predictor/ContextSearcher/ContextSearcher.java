package pt.ua.Generator.Predictor.ContextSearcher;

import java.util.Map;

public abstract class ContextSearcher {
    protected Map<String, Map<Character, Integer>> countTable;
    protected int k;

    public ContextSearcher(Map<String, Map<Character, Integer>> countTable, int k) {
        this.countTable = countTable;
        this.k=k;
    }

    public abstract String getFittingContext(String context) throws Exception;
}
