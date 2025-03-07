package pt.ua.tai.generator.Predictor.ContextSearcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContextSearcherPopularSuffix extends ContextSearcher {

  public ContextSearcherPopularSuffix(Map<String, Map<Character, Integer>> countTable, int k) {
    super(countTable, k);
  }

  @Override
  public String getFittingContext(String context) throws Exception {
    //immediate exit for context that already exists
    if (countTable.containsKey(context)) {
      return context;
    }
    //retrieve all possible contexts that have equal sufix
    //reduce size of suffix until if any matching
    List<String> possibleContexts = new ArrayList<>();
    int aux = 1;
    while (possibleContexts.size() == 0) {
      if (aux == k) {
        throw new Exception("Context search "+context+": No matches!");
      }
      for (String key : countTable.keySet()) {
        if (key.substring(aux).equals(context.substring(aux))) {
          possibleContexts.add(key);
        }
      }
      aux++;
    }
    //Choose the context "more popular", with max of sum of all values
    int maxSum = 0;
    for (String key : possibleContexts) {
      int currentSum = countTable.get(key).values().stream().reduce(0, Integer::sum);
      if (currentSum > maxSum) {
        maxSum = currentSum;
        context = key;
      }
    }
    return context;
  }
}
