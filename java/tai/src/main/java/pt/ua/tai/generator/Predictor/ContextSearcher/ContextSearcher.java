package pt.ua.tai.generator.Predictor.ContextSearcher;

import java.util.List;
import java.util.Map;

public abstract class ContextSearcher {

  protected Map<String, Map<Character, Integer>> countTable;
  protected int k;

  public ContextSearcher(Map<String, Map<Character, Integer>> countTable, int k) {
    this.countTable = countTable;
    this.k = k;
  }

  public abstract String getFittingContext(String context) throws Exception;

  public abstract Map<Character, Integer> getEntries(String context);

  public abstract Map<Character, Double> getProbEntries(String context, Map<String, Map<Character, Double>> probTable, List<Map<String, Map<Character, Double>>> subProbTables);
}
