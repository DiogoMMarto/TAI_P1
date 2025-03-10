package pt.ua.tai.generator.Processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessorProbAlphaAllK extends ProcessorAllK{
    private double alpha;
    private Map<String, Map<Character, Double>> probTable;
    private List<Map<String, Map<Character, Double>>> subProbTables;

    public ProcessorProbAlphaAllK(String content, int k, double alpha) {
        super(content, k);
        probTable=new HashMap<>();
    }

    @Override
    public void process() {
        super.process();
        for (String context : super.countTable.keySet()) {
            Map<Character, Integer> temp = countTable.get(context);

            int sum = 0;
            for (Integer integer : temp.values()) {
                sum += integer;
            }
            double tot = sum + alpha * getAlphabet().size();

            Map<Character, Double> tempProb = new HashMap<>();
            for (Character c : getAlphabet()) {
                Double probability = (temp.getOrDefault(c, 0) + alpha) / tot;
                tempProb.put(c, probability);
            }
            probTable.put(context, tempProb);
        }

        for (int i = 0; i < k; i++) {
            for(String context : super.getSubCountTables().get(i).keySet()){
                Map<Character, Integer> temp = super.getSubCountTables().get(i).get(context);

                int sum = 0;
                for (Integer integer : temp.values()) {
                    sum += integer;
                }
                double tot = sum + alpha * getAlphabet().size();
            }
        }
    }

    public Map<String, Map<Character, Double>> getProbTable() {
        return probTable;
    }

    public List<Map<String, Map<Character, Double>>> getSubProbTables(){
        return subProbTables;
    }
}
