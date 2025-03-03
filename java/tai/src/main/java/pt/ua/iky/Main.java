package pt.ua.iky;

import java.util.concurrent.Callable;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import pt.ua.Generator.Generator;

@Command(name = "Main", mixinStandardHelpOptions = true, version = "1.0",
    description = "Runs the FCM algorithm with the given parameters.")
public final class Main implements Callable<Integer> {

  private static final Logger log = Logger.getLogger(Main.class.getName());

  //  @Option(names = {"-t","--type"}, description = "Type of application (fcm or generator)", defaultValue = "fcm")
  @Parameters(index = "0", description = "Type of application (fcm or generator)", defaultValue = "fcm")
  private String type;
  @Option(names = {"-p", "--prior"}, description = "Prior string for generator")
  private String prior;
  @Option(names = {"-rl", "--responseLength"}, description = "Length of the response for generator")
  private Integer responseLength;
  @Option(names = {"-c", "--char"}, description = "Output char by char", defaultValue = "true")
  private boolean cmd;
  @Option(names = {"-a", "--alpha"}, description = "Alpha value", required = true)
  private float alpha;
  @Option(names = {"-k", "--context"}, description = "Context width", required = true)
  private int k;
  @Option(names = {"-f", "--file"}, description = "File name", required = true)
  private String fileName;
  @Option(names = {"-v", "--verbose"}, description = "Verbose output", defaultValue = "false")
  private boolean verbose;
  @Option(names = {"-m", "--mode"}, description = "Mode of operation (e.g., PROBABILITY, MAX, PROBABILITYALPHA)", defaultValue = "PROBABILITY")
  private String mode;
  @Option(names = {"-sm", "--searchMode"}, description = "Search mode (e.g., CUTFIRSTCHAR, RANDOM)", defaultValue = "CUTFIRSTCHAR")
  private String searchMode;
  @Option(names = {"-s", "--seeding"}, description = "Seed value for random number generation", required = false)
  private Integer seed;
  @Option(names = {"-pf", "--priorFix"}, description = "Enable prior fixing", defaultValue = "false")
  private boolean priorFix;

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
      /*
      log.info("Running Generator -> a: " + alpha + ", k: " + k + ", file: " + fileName);
      Generator generator = new Generator(fcm);
      generator.run(alpha, k, fileName, prior, verbose);*/
      log.info("Running Generator -> a: " + alpha + ", k: " + k + ", file: " + fileName);
      if (prior==null){
        throw new RuntimeException("Prior (-p) required for generator");
      }
      if (responseLength==null){
        throw new RuntimeException("Response Length (-rl) required for generator");
      }
      Generator generator = new Generator(prior,responseLength,fileName);
        try {
          generator.setK(k);
          generator.setAlpha((double) alpha);
          generator.setMode(mode);
          generator.setSearchMode(searchMode);
          if (seed != null) {
            generator.seeding(seed);
          }
          if (priorFix) {
            generator.priorFixing();
          }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        generator.init();
      if(cmd){
        generator.generateToCmdChar();
      }else {
        System.out.println(generator.generate());
      }
    } else {
      log.info("Running FCM -> a: " + alpha + ", k: " + k + ", file: " + fileName);
      fcm.online(alpha, k, fileName, verbose);
    }
    return 0;
  }
}