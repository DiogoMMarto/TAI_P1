package pt.ua.tai.generator.Predictor.ContextSearcher;

import java.util.List;
import java.util.Map;

public class ContextSearcherPopSuffixAllK extends ContextSearcherPopularSuffix{
    List<Map<String, Map<Character, Integer>>> subCountTables;

    public ContextSearcherPopSuffixAllK(Map<String, Map<Character, Integer>> countTable, int k, List<Map<String, Map<Character, Integer>>> subCountTables) {
        super(countTable, k);
        this.subCountTables =subCountTables;
    }

    @Override
    public String getFittingContext(String context) throws Exception {
        if (countTable.containsKey(context)) {
            return context;
        }
        for(int i = subCountTables.size()-1; i>=0; i--){
            Map<String, Map<Character, Integer>> subProbTable= subCountTables.get(i);
            String auxContext=context.substring(context.length()-(i+1));
            if(subProbTable.containsKey(auxContext)){
                return auxContext;
            }
        }
        throw new Exception("Context: "+context+" and subcontexts not found!");
    }

    @Override
    public Map<Character, Integer> getEntries(String context) {
        if(context.length()> subCountTables.size()){
            return countTable.get(context);
        }
        return subCountTables.get(context.length()-1).get(context);
    }

    @Override
    public Map<Character, Double> getProbEntries(String context, Map<String, Map<Character, Double>> probTable, List<Map<String, Map<Character, Double>>> subProbTables) {
        if(context.length()> subProbTables.size()){
            return probTable.get(context);
        }
        return subProbTables.get(context.length()-1).get(context);
    }
}
