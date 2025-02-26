package pt.ua.iky;

import static java.util.logging.Level.INFO;
import static pt.ua.iky.FileUtil.readFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Finite Context Model a.k.a discrete-time Markov Chain
 */
public class FCM {

  private final Logger log = Logger.getLogger(getClass().getName());
  private final Map<String, Map<Character, Integer>> contextAndSucceedingCharacterCounts = new LinkedHashMap<>();
  private final Set<Character> alphabet = new LinkedHashSet<>();
  private String content;

  public void run(float alpha, int k, String fileName, boolean verbose) {
    content = readFile(fileName);
    if (content == null || content.isEmpty()) {
      return;
    }
    generateAlphabetSet();
    generateContextAndSucceedingCharacterCounts(k);
    final int totalNoOfPredictions = content.length() - k;
    float sumLogProb = calculateWeightedLogSum(alpha);
    // Average (per prediction) negative log probability converted into bits
    final float entropy = -sumLogProb / totalNoOfPredictions / (float) Math.log(2);
    log.log(INFO, "Entropy: {0} bps", entropy);

    if (verbose) {
      for (Map.Entry<String, Map<Character, Integer>> entry : contextAndSucceedingCharacterCounts.entrySet()) {
        log.info("Ctx: " + entry.getKey() + " -> " + entry.getValue());
      }
    }
  }

  /**
   * Creates the context and succeeding character and count Map
   *
   * @param k context width
   */
  protected void generateContextAndSucceedingCharacterCounts(int k) {
    for (int i = 0; i + k < content.length(); i++) {
      final String context = content.substring(i, i + k);
      final char nextChar = content.charAt(i + k);
      // Initialize map if necessary
      contextAndSucceedingCharacterCounts.putIfAbsent(context, new HashMap<>());
      Map<Character, Integer> charCounts = contextAndSucceedingCharacterCounts.get(context);
      // Increment count for this character
      charCounts.merge(nextChar, 1, Integer::sum);
    }
  }

  /**
   * Generate an alphabet from the symbols used in the content
   */
  protected void generateAlphabetSet() {
    for (int i = 0; i < content.length(); i++) {
      alphabet.add(content.charAt(i));
    }
  }

  /**
   * Compute the weighted sum of log probabilities for each (context, nextChar) pair: count *
   * log((count + alpha) / (totalCount_for_context + alpha*alphabet.size()))
   */
  protected float calculateWeightedLogSum(float alpha) {
    float sumLogProb = 0.0F;
    final float alphaTimesAlphabet = alpha * alphabet.size();
    for (Map.Entry<String, Map<Character, Integer>> entry : contextAndSucceedingCharacterCounts.entrySet()) {
      Map<Character, Integer> counts = entry.getValue();
      // Sum of counts for the context
      final int contextTotalCount = counts.values().stream().reduce(0, Integer::sum);
      // Denominator: observed count plus smoothing mass for every character
      final float denominator = contextTotalCount + alphaTimesAlphabet;
      for (var countsEntry : counts.entrySet()) {
        final int count = countsEntry.getValue();
        final float probability = (count + alpha) / denominator;
        sumLogProb += count * (float) Math.log(probability);
      }
    }
    return sumLogProb;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
