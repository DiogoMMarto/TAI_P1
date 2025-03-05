package pt.ua.tai.generator.Predictor;

import java.util.Map;
import java.util.Random;
import pt.ua.tai.generator.Predictor.ContextSearcher.ContextSearcher;

public class PredictorProbAlpha extends Predictor {

  protected Map<String, Map<Character, Double>> probTable;
  private Random random;


  public PredictorProbAlpha(ContextSearcher contextSearcher,
      Map<String, Map<Character, Double>> table, int responseSize, int k) {
    super(contextSearcher, responseSize, k);
    probTable = table;
    random = new Random();
  }

  public PredictorProbAlpha(ContextSearcher contextSearcher,
      Map<String, Map<Character, Double>> table, int responseSize, int k, int seed) {
    super(contextSearcher, responseSize, k);
    probTable = table;
    random = new Random(seed);
  }

  @Override
  protected char chooseChar() {
    Map<Character, Double> tempProb = probTable.get(context);
    double randomValue = random.nextDouble();
    double cumulativeSum = 0;

    for (Map.Entry<Character, Double> entry : tempProb.entrySet()) {
      cumulativeSum += entry.getValue();
      if (randomValue < cumulativeSum) {
        return entry.getKey();
      }
    }
    return ' ';
  }
}
