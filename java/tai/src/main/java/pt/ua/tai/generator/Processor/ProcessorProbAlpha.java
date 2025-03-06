package pt.ua.tai.generator.Processor;

import java.util.HashMap;
import java.util.Map;

public class ProcessorProbAlpha extends Processor {

  private double alpha;
  private Map<String, Map<Character, Double>> probTable;


  public ProcessorProbAlpha(String content, int k, double alpha) {
    super(content, k);
    this.alpha = alpha;
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

  }

  public Map<String, Map<Character, Double>> getProbTable() {
    return probTable;
  }
}
