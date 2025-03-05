package pt.ua.tai.generator.Predictor.ContextSearcher;

import java.util.Map;
import java.util.Random;

public class ContextSearcherRandom extends ContextSearcher {

  private Random random;

  public ContextSearcherRandom(Map<String, Map<Character, Integer>> countTable, int k) {
    super(countTable, k);
    random = new Random();
  }

  public ContextSearcherRandom(Map<String, Map<Character, Integer>> countTable, int k, int seed) {
    super(countTable, k);
    random = new Random(seed);
  }

  @Override
  public String getFittingContext(String context) {
    String[] keys = countTable.keySet().toArray(new String[0]);
    int n = random.nextInt(keys.length);
    return keys[n];
  }
}
