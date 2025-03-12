package pt.ua.tai.fcm;

import static java.util.logging.Level.INFO;
import static pt.ua.tai.utils.FileUtil.readTxtFileToString;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
  private final Map<String, CharCounts> contextAndSucceedingCharacterCounts = new HashMap<>();
  private final Set<Character> alphabet = new LinkedHashSet<>();
  private final List<Float> logProbabilities = new LinkedList<>();
  private String content;

  public void online(float alpha, int k, String fileName, boolean verbose, boolean saveToCsv) {
    try {
      content = readTxtFileToString(fileName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    generateAlphabetSet();
    final float alphaTimesAlphabet = alpha * alphabet.size();
    log.info("Alphabet size: " + alphabet.size());
    double totalSum = 0.0F;
    final int contentLength = content.length();
    final char[] contentChars = content.toCharArray();
    char[] contextWindow = Arrays.copyOf(contentChars, k);
    for (int i = 0; i + k < contentLength; i++) {
      final String context = String.valueOf(contextWindow);
      final char nextChar = contentChars[i + k];
      CharCounts charCounts = contextAndSucceedingCharacterCounts.computeIfAbsent(
          context, key -> new CharCounts());
      int count = charCounts.increment(nextChar);
      float logProb = getLogProb(alpha, charCounts, alphaTimesAlphabet, count);
      totalSum += logProb;
      if (saveToCsv) {
        logProbabilities.add(logProb);
      }
      System.arraycopy(contextWindow, 1, contextWindow, 0, k - 1);
      contextWindow[k - 1] = nextChar;
    }
    final int totalNoOfPredictions = content.length() - k;
    final double entropy = -totalSum / totalNoOfPredictions / Math.log(2);
    log.log(INFO, "Entropy: {0} bps", entropy);
    printMemoryUsage();
    if (saveToCsv) {
      saveLogProbsToCsv(fileName.substring(fileName.lastIndexOf('/') + 1), alpha, k);
      appendToAllResultsCSV(fileName.substring(fileName.lastIndexOf('/') + 1), alpha, k, entropy);
    }
    if (verbose) {
      for (Map.Entry<String, CharCounts> entry : contextAndSucceedingCharacterCounts.entrySet()) {
        log.info("Ctx: " + entry.getKey() + " -> " + entry.getValue());
      }
    }
  }

  private float getLogProb(float alpha, CharCounts charCounts, float alphaTimesAlphabet,
      int count) {
    final int contextTotalCount = charCounts.getTotalCount() - 1;
    final float denominator = contextTotalCount + alphaTimesAlphabet;
    final float probability = (count - 1 + alpha) / denominator;
    return (float) Math.log(probability);
  }

  /**
   * Creates the context and succeeding character and count Map
   *
   * @param k context width
   */
  private void generateContextAndSucceedingCharacterCounts(int k) {
    final int contentLength = content.length();
    StringBuilder contextBuilder = new StringBuilder(content.substring(0, k));
    for (int i = 0; i + k < content.length(); i++) {
      final String context = contextBuilder.toString();
      final char nextChar = content.charAt(i + k);
      CharCounts charCounts = contextAndSucceedingCharacterCounts.computeIfAbsent(
          context, key -> new CharCounts());
      charCounts.increment(nextChar);
      if (i + k + 1 < contentLength) {
        contextBuilder.deleteCharAt(0).append(content.charAt(i + k));
      }
    }
  }

  /**
   * Generate an alphabet from the symbols used in the content
   */
  private void generateAlphabetSet() {
    for (int i = 0; i < content.length(); i++) {
      alphabet.add(content.charAt(i));
    }
  }

  /**
   * Compute the weighted sum of log probabilities for each (context, nextChar) pair: count *
   * log((count + alpha) / (totalCount_for_context + alpha*alphabet.size()))
   */
  private float calculateWeightedLogSum(float alpha) {
    float sumLogProb = 0.0F;
    final float alphaTimesAlphabet = alpha * alphabet.size();
    for (Map.Entry<String, CharCounts> entry : contextAndSucceedingCharacterCounts.entrySet()) {
      CharCounts charCounts = entry.getValue();
      for (var countsEntry : charCounts.getCounts().entrySet()) {
        final int count = countsEntry.getValue();
        final float probability = getLogProb(alpha, charCounts, alphaTimesAlphabet, count);
        sumLogProb += count * (float) Math.log(probability);
      }
    }
    return sumLogProb;
  }

  private void printMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
    log.log(INFO, "Used memory: {0} MB", usedMemory / (1024 * 1024));
  }

  private void saveLogProbsToCsv(String fileName, float alpha, int k) {
    StringBuilder sb = new StringBuilder();
    sb.append("logProbabilities").append("\n");
    try (FileWriter f = new FileWriter("logProb_" + fileName + "_"
        + Float.parseFloat(String.format("%.5f", alpha)) + "_" + k + ".csv");
        PrintWriter writer = new PrintWriter(f)) {
      for (Float logProbability : logProbabilities) {
        sb.append(logProbability).append("\n");
      }
      writer.write(sb.toString());
    } catch (IOException e) {
      log.log(Level.SEVERE, "Error writing logProbabilities to CSV", e);
    }
  }

  private void appendToAllResultsCSV(String fileName, float alpha, int k, double entropy) {
    StringBuilder sb = new StringBuilder();
    try (FileWriter f = new FileWriter("allResults.csv", true);
        PrintWriter writer = new PrintWriter(f)) {
      sb.append(fileName).append(',').append(Float.parseFloat(String.format("%.5f", alpha)))
          .append(',').append(k).append(',').append(entropy).append("\n");
      writer.write(sb.toString());
    } catch (IOException e) {
      log.log(Level.SEVERE, "Error writing to allResults.csv", e);
    }
  }
}

