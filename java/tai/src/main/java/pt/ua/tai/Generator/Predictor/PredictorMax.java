package pt.ua.tai.Generator.Predictor;

import pt.ua.tai.Generator.Predictor.ContextSearcher.ContextSearcher;

import java.util.Map;

public class PredictorMax extends Predictor{


    public PredictorMax(ContextSearcher contextSearcher, Map<String, Map<Character, Integer>> table, int responseSize, int k) {
        super(contextSearcher, responseSize, k);
        setTable(table);
    }

    @Override
    protected char chooseChar() {
        int max = 0;
        char c=' ';
        for (Map.Entry<Character, Integer> entry : entries.entrySet()) {
            if (entry.getValue() > max) {
                c = entry.getKey();
            }
        }
        return c;
    }
}
