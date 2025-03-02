package pt.ua.iky;

import static pt.ua.iky.FileUtil.readFile;

public class Generator {

  private FCM fcm;

  public Generator(FCM fcm) {
    this.fcm = fcm;
  }

  public void run(float alpha, int k, String fileName, String prior, boolean verbose) {
    final String content = readFile(fileName);
    fcm.setContent(content);
    fcm.generateAlphabetSet();
    fcm.generateContextAndSucceedingCharacterCounts(k);
  }
}
