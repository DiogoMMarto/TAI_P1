package pt.ua.iky;

import java.util.logging.Logger;

public final class Main {

  private static final Logger log = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    System.setProperty("java.util.logging.SimpleFormatter.format",
        "Time: %1$tT.%1$tL -> %4$s %5$s%6$s%n");
    log.info("a: " + args[0] + ", k: " + args[1] + ", file: " + args[2]);

    boolean verbose = false;
    if (args.length > 3) {
      log.info("verbose?: " + args[3]);
      verbose = Boolean.parseBoolean(args[3]);
    }
    double alpha = Double.parseDouble(args[0]);
    int k = Integer.parseInt(args[1]);
    String fileName = args[2];

    FCM fcm = new FCM();
    fcm.runFcm(alpha, k, fileName, verbose);
  }
}