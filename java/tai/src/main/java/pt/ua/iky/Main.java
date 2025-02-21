package pt.ua.iky;

public class Main {

  public static void main(String[] args) {
    FCM fcm = new FCM();
    System.out.println("a: " + args[0]);
    System.out.println("k: " + args[1]);
    System.out.println("file: " + args[2]);
    boolean verbose = false;
    if (args.length > 3) {
      System.out.println("verbose?: " + args[3]);
      verbose = Boolean.parseBoolean(args[3]);
    }

    double alpha = Double.parseDouble(args[0]);
    int k = Integer.parseInt(args[1]);
    String fileName = args[2];

    fcm.runFcm(alpha, k, fileName, verbose);

  }
}