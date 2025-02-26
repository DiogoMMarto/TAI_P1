package pt.ua.iky;

import static pt.ua.iky.FileUtil.readFile;

public class Generator extends FCM {

  public void run(float alpha, int k, String fileName, String prior, boolean verbose) {
    final String content = readFile(fileName);
    setContent(content);
    generateAlphabetSet();
    generateContextAndSucceedingCharacterCounts(k);
    
  }
}
