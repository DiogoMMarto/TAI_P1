package pt.ua.iky;

import java.util.concurrent.Callable;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "Main", mixinStandardHelpOptions = true, version = "1.0",
    description = "Runs the FCM algorithm with the given parameters.")
public final class Main implements Callable<Integer> {

  private static final Logger log = Logger.getLogger(Main.class.getName());

  //  @Option(names = {"-t","--type"}, description = "Type of application (fcm or generator)", defaultValue = "fcm")
  @Parameters(index = "0", description = "Type of application (fcm or generator)", defaultValue = "fcm")
  private String type;
  @Option(names = {"-p", "--prior"}, description = "Prior string for generator", defaultValue = "")
  private String prior;
  @Option(names = {"-a", "--alpha"}, description = "Alpha value", required = true)
  private float alpha;
  @Option(names = {"-k", "--context"}, description = "Context width", required = true)
  private int k;
  @Option(names = {"-f", "--file"}, description = "File name", required = true)
  private String fileName;
  @Option(names = {"-v", "--verbose"}, description = "Verbose output", defaultValue = "false")
  private boolean verbose;

  public static void main(String[] args) {
    System.setProperty("java.util.logging.SimpleFormatter.format",
        "Time: %1$tT.%1$tL -> %4$s %5$s%6$s%n");
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() {
    FCM fcm = new FCM();
    if (type.equals("generator") || type.equals("g")) {
      log.info("Running Generator -> a: " + alpha + ", k: " + k + ", file: " + fileName);
      Generator generator = new Generator(fcm);
      generator.run(alpha, k, fileName, prior, verbose);
    } else {
      log.info("Running FCM -> a: " + alpha + ", k: " + k + ", file: " + fileName);
      fcm.online(alpha, k, fileName, verbose);
    }
    return 0;
  }
}