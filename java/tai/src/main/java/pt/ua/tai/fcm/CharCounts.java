package pt.ua.tai.fcm;

import java.util.HashMap;
import java.util.Map;

public final class CharCounts {

  private final Map<Character, Integer> counts = new HashMap<>(1, 1);
  private int totalCount = 0;

  int increment(char c) {
    int newCount = counts.merge(c, 1, Integer::sum);
    totalCount++;
    return newCount;
  }

  Map<Character, Integer> getCounts() {
    return counts;
  }

  int getTotalCount() {
    return totalCount;
  }
}
