package pt.ua.tai;

import java.util.concurrent.Callable;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import pt.ua.tai.fcm.FCM;
import pt.ua.tai.generator.Generator;

@Command(name = "Main", mixinStandardHelpOptions = true, version = "1.0",
    description = "Runs the FCM or Generator algorithm with the given parameters.")
public final class Main implements Callable<Integer> {

  private static final Logger log = Logger.getLogger(Main.class.getName());

  @Parameters(index = "0", description = "Type of application (fcm or generator)", defaultValue = "fcm")
  private String type;

  @Option(names = {"-f", "--file"}, description = "File name", required = true)
  private String fileName;
  @Option(names = {"-v", "--verbose"}, description = "Verbose output", defaultValue = "false")
  private boolean verbose;
  @Option(names = {"-a", "--alpha"}, description = "Smoothing parameter alpha")
  private Float alpha;
  @Option(names = {"-k", "--contextWidth"}, description = "Context width")
  private Integer k;
  @Option(names = {"-p", "--prior"}, description = "Prior string for generator")
  private String prior;
  @Option(names = {"-rl", "--responseLength"}, description = "Length of the response for generator")
  private Integer responseLength;
  @Option(names = {"-nc",
      "--noChar"}, description = "Disable output char by char", defaultValue = "false")
  private boolean cmd;
  @Option(names = {"-m", "--mode"},
      description = "Mode of operation (e.g., PROBABILITY, MAX, PROBABILITYALPHA)", defaultValue = "PROBABILITY")
  private String mode;
  @Option(names = {"-sm", "--searchMode"},
      description = "Search mode (e.g., CUTFIRSTCHAR, RANDOM)", defaultValue = "CUTFIRSTCHAR")
  private String searchMode;
  @Option(names = {"-s", "--seeding"},
      description = "Seed value for random number generation")
  private Integer seed;
  @Option(names = {"-pf", "--priorFix"},
      description = "Enable prior fixing", defaultValue = "false")
  private boolean priorFix;
  @Option(names = {"-sc", "--saveCsv"},
      description = "Save online probabilities and results to CSV", defaultValue = "false")
  private boolean saveToCsv;

  public static void main(String[] args) {
    System.setProperty("java.util.logging.SimpleFormatter.format",
        "Time: %1$tT.%1$tL -> %4$s %5$s%6$s%n");
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() {
    if (type.equals("generator") || type.equals("g")) {
      validateGeneratorParams();
      log.info("Running Generator -> a: " + alpha + ", k: " + k + ", file: " + fileName);
      Generator generator = createGenerator();
      if (!cmd) {
        generator.generateToCmdChar();
      } else {
        log.info(generator.generate());
      }
    } else {
      validateFcmParams();
      log.info("Running FCM -> a: " + alpha + ", k: " + k + ", file: " + fileName);
      FCM fcm = new FCM();
      fcm.online(alpha, k, fileName, verbose, saveToCsv);
    }
    return 0;
  }

  private void validateGeneratorParams() {
    if (prior == null) {
      throw new IllegalArgumentException("Prior (-p) required for generator");
    } else if (responseLength == null) {
      throw new IllegalArgumentException("Response Length (-rl) required for generator");
    } else if (mode.equals("PROBABILITYALPHA") && alpha == null) {
      throw new IllegalArgumentException(
          "Smoothing parameter alpha (-a) required for mode PROBABILITYALPHA in generator");
    }
  }

  private void validateFcmParams() {
    if (k == null) {
      throw new IllegalArgumentException("Context width (-k) required for FCM");
    } else if (alpha == null) {
      throw new IllegalArgumentException("Smoothing parameter alpha (-a) required for FCM");
    }
  }

  private Generator createGenerator() {
    Generator generator = new Generator(prior, responseLength, fileName);
    try {
      if (k != null) {
        generator.setK(k);
      }
      if (alpha != null) {
        generator.setAlpha((double) alpha);
      }
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
    return generator;
  }

  private void runExperiments() {
    for (int f = 1; f <= 5; f++) {
      final String file = "/Users/ilker/Courses/TAI/TAI_P1/sequence" + f + ".txt";
      for (int c = 1; c <= 35; c += 1) {
        for (float a = 0.001F; a <= 1.1F; a *= 10) {
          log.info("Running FCM -> a: " + a + ", k: " + c + ", file: " + file);
          FCM fcm = new FCM();
          fcm.online(a, c, file, verbose, true);
        }
      }
    }
  }
}