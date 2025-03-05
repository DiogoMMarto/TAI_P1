package pt.ua.tai.fcm;

import static java.util.logging.Level.INFO;
import static pt.ua.tai.utils.FileUtil.readFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Finite Context Model a.k.a discrete-time Markov Chain
 */
public class FCM {

  private final Logger log = Logger.getLogger(getClass().getName());
  private final Map<String, Map<Character, Integer>> contextAndSucceedingCharacterCounts = new LinkedHashMap<>();
  private final Set<Character> alphabet = new LinkedHashSet<>();
  private final List<Float> logProbabilities = new ArrayList<>();
  private String content;

  public void online(float alpha, int k, String fileName, boolean verbose) {
    content = readFile(fileName);
    if (content == null || content.isEmpty()) {
      return;
    }
    generateAlphabetSet();
    final float alphaTimesAlphabet = alpha * alphabet.size();
    float totalSum = 0.0F;
    for (int i = 0; i + k < content.length(); i++) {
      final String context = content.substring(i, i + k);
      final char nextChar = content.charAt(i + k);
      contextAndSucceedingCharacterCounts.putIfAbsent(context, new HashMap<>());
      Map<Character, Integer> charCounts = contextAndSucceedingCharacterCounts.get(context);
      int count = charCounts.merge(nextChar, 1, Integer::sum);
      final float logProb = getLogProb(alpha, charCounts, alphaTimesAlphabet, count);
      totalSum += logProb;
      logProbabilities.add(logProb);
    }

    final int totalNoOfPredictions = content.length() - k;
    final float entropy = -totalSum / totalNoOfPredictions / (float) Math.log(2);
    log.log(INFO, "Entropy: {0} bps", entropy);
    saveToCSV();

    if (verbose) {
      for (Map.Entry<String, Map<Character, Integer>> entry : contextAndSucceedingCharacterCounts.entrySet()) {
        log.info("Ctx: " + entry.getKey() + " -> " + entry.getValue());
      }
    }
  }

  private void saveToCSV() {
    try (PrintWriter writer = new PrintWriter("logProbabilities.csv")) {
      StringBuilder sb = new StringBuilder();
      for (Float logProbability : logProbabilities) {
        sb.append(logProbability).append("\n");
      }
      writer.write(sb.toString());
    } catch (IOException e) {
      log.log(Level.SEVERE, "Error writing logProbabilities to CSV", e);
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
      contextAndSucceedingCharacterCounts.putIfAbsent(context, new HashMap<>());
      Map<Character, Integer> charCounts = contextAndSucceedingCharacterCounts.get(context);
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
      for (var countsEntry : counts.entrySet()) {
        final int count = countsEntry.getValue();
        final float probability = getLogProb(alpha, counts, alphaTimesAlphabet, count);
        sumLogProb += count * (float) Math.log(probability);
      }
    }
    return sumLogProb;
  }

  private float getLogProb(float alpha, Map<Character, Integer> charCounts,
      float alphaTimesAlphabet, int count) {
    // Sum of counts for the context
    final int contextTotalCount = charCounts.values().stream().reduce(0, Integer::sum);
    // Denominator: observed count plus smoothing mass for every character
    final float denominator = contextTotalCount + alphaTimesAlphabet;
    final float probability = (count + alpha) / denominator;
    return (float) Math.log(probability);
  }

  public void setContent(String content) {
    this.content = content;
  }
}

