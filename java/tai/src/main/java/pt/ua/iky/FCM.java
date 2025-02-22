package pt.ua.iky;

import static java.util.logging.Level.INFO;
import static pt.ua.iky.Common.readFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class FCM {

  private final Logger log = Logger.getLogger(getClass().getName());

  public float run(float alpha, int k, String fileName, boolean verbose) {
    final String content = readFile(fileName);
    if (content == null || content.isEmpty()) {
      return 0F;
    }
    final Set<Character> alphabet = getAlphabetSet(content);
    final Map<String, Map<Character, Integer>> contextCounts = getContextCharacterCounts(k,
        content);
    final int totalNoOfPredictions = content.length() - k;
    float sumLogProb = calculateWeightedLogSum(alpha, alphabet, contextCounts);
    // Average (per prediction) negative log probability converted into bits
    final float entropy = -sumLogProb / totalNoOfPredictions / (float) Math.log(2);
    log.log(INFO, "Entropy: {0} bps", entropy);

    if (verbose) {
      for (Map.Entry<String, Map<Character, Integer>> entry : contextCounts.entrySet()) {
        log.info("Ctx: " + entry.getKey() + " -> " + entry.getValue());
      }
    }
    return entropy;
  }

  /**
   * Creates the map in the format of "CONTEXT" -> {"A": 1, "B":2}
   *
   * @param k       context width
   * @param content full text
   * @return map of "CONTEXT" -> {"A": 1, "B":2}
   */
  private Map<String, Map<Character, Integer>> getContextCharacterCounts(int k,
      String content) {
    final Map<String, Map<Character, Integer>> contextCounts = new LinkedHashMap<>();
    for (int i = 0; i + k < content.length(); i++) {
      final String context = content.substring(i, i + k);
      final char nextChar = content.charAt(i + k);
      // Initialize map if necessary
      contextCounts.putIfAbsent(context, new HashMap<>());
      Map<Character, Integer> charCounts = contextCounts.get(context);
      // Increment count for this character
      charCounts.merge(nextChar, 1, Integer::sum);
    }
    return contextCounts;
  }

  /**
   * Creates an alphabet from the symbols used in the content
   *
   * @param content full text
   * @return set of symbols
   */
  private Set<Character> getAlphabetSet(String content) {
    final Set<Character> alphabet = new LinkedHashSet<>();
    for (final char c : content.toCharArray()) {
      alphabet.add(c);
    }
    return alphabet;
  }

  /**
   * Compute the weighted sum of log probabilities for each (context, nextChar) pair: count *
   * log((count + alpha) / (totalCount_for_context + alpha*alphabet.size()))
   */
  private float calculateWeightedLogSum(float alpha, Set<Character> alphabet,
      Map<String, Map<Character, Integer>> contextCounts) {
    float sumLogProb = 0.0F;
    final float alphaTimesAlphabet = alpha * alphabet.size();
    for (Map.Entry<String, Map<Character, Integer>> entry : contextCounts.entrySet()) {
      Map<Character, Integer> counts = entry.getValue();
      // Sum of counts for the context
      final int contextTotalCount = counts.values().stream().mapToInt(Integer::intValue).sum();
      // Denominator: observed count plus smoothing mass for every character
      final float denominator = contextTotalCount + alphaTimesAlphabet;
      for (char c : alphabet) {
        final Integer count = counts.get(c);
        if (count != null) {
          final float probability = (count + alpha) / denominator;
          sumLogProb += count * (float) Math.log(probability);
        }
      }
    }
    return sumLogProb;
  }

}
