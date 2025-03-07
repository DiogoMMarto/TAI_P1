package pt.ua.tai.generator;

import static pt.ua.tai.utils.FileUtil.readFile;
import static pt.ua.tai.utils.FileUtil.readTxtFileToString;

import java.io.IOException;
import pt.ua.tai.generator.Enum.Mode;
import pt.ua.tai.generator.Enum.Search;
import pt.ua.tai.generator.Predictor.ContextSearcher.ContextSearcher;
import pt.ua.tai.generator.Predictor.ContextSearcher.ContextSearcherPopularSuffix;
import pt.ua.tai.generator.Predictor.ContextSearcher.ContextSearcherRandom;
import pt.ua.tai.generator.Predictor.Predictor;
import pt.ua.tai.generator.Predictor.PredictorMax;
import pt.ua.tai.generator.Predictor.PredictorProb;
import pt.ua.tai.generator.Predictor.PredictorProbAlpha;
import pt.ua.tai.generator.Processor.Processor;
import pt.ua.tai.generator.Processor.ProcessorProbAlpha;

public class Generator {

  private final String prior;
  private final int responseLength;
  private final String fileName;
  private int k;
  private double alpha;
  private Mode mode;
  private Search searchMode;
  private boolean seeding;
  private int seed;
  private boolean priorFixing;

  private String content;
  private Processor processor;
  private Predictor predictor;


  public Generator(String prior, int responseLength, String fileName) {
    this.responseLength = responseLength;
    this.fileName = fileName;
    this.prior = prior;
    k = 5;
    alpha = 0.01;
    mode = Mode.PROBABILITY;
    searchMode = Search.CUTFIRSTCHAR;
    seeding = false;
    priorFixing = false;
  }

  public void setK(int k) throws Exception {
    if (k < 1) {
      throw new Exception("Invalid k value:" + k);
    }
    this.k = k;
  }

  public void setAlpha(Double alpha) throws Exception {
    if (alpha <= 0) {
      throw new Exception("Invalid alpha value:" + alpha);
    }
    this.alpha = alpha;
  }

  public void setMode(String mode) throws Exception {
    this.mode = Mode.valueOf(mode);
  }

  public void setSearchMode(String searchMode) throws Exception {
    this.searchMode = Search.valueOf(searchMode);
  }

  public void seeding(int seed) {
    seeding = true;
    this.seed = seed;
  }

  public void priorFixing() {
    priorFixing = true;
  }


  public void init() {
    //read file to content string
    /*
    try {
      content = readTxtFileToString(this.fileName);
    } catch (IOException e) {
      throw new RuntimeException("File:" + this.fileName + " Not Found!");
    }*/
    content = readFile(this.fileName);
    initProcessor();
  }

  private void initProcessor() {
    switch (mode) {
      case MAX:
        processor = new Processor(content, k);
        break;
      case PROBABILITY:
        processor = new Processor(content, k);
        break;
      case PROBABILITYALPHA:
        processor = new ProcessorProbAlpha(content, k, alpha);
        break;
    }
  }

  private void initPredictor() {
    ContextSearcher contextSearcher;
    switch (searchMode) {
      case RANDOM:
        if (seeding) {
          contextSearcher = new ContextSearcherRandom(processor.getCountTable(), k, seed);
        } else {
          contextSearcher = new ContextSearcherRandom(processor.getCountTable(), k);
        }
        break;
      case CUTFIRSTCHAR:
        contextSearcher = new ContextSearcherPopularSuffix(processor.getCountTable(), k);
        break;
      default:
        contextSearcher = new ContextSearcherPopularSuffix(processor.getCountTable(), k);
        break;
    }
    switch (mode) {
      case MAX:
        predictor = new PredictorMax(contextSearcher, processor.getCountTable(), responseLength, k);
        break;
      case PROBABILITY:
        predictor = new PredictorProb(contextSearcher, processor.getCountTable(), responseLength,
            k);
        break;
      case PROBABILITYALPHA:
        if (seeding) {
          predictor = new PredictorProbAlpha(contextSearcher,
              ((ProcessorProbAlpha) processor).getProbTable(), responseLength, k, seed);
        } else {
          predictor = new PredictorProbAlpha(contextSearcher,
              ((ProcessorProbAlpha) processor).getProbTable(), responseLength, k);
        }
        break;
    }
    if (priorFixing) {
      predictor.feedPrior(fixPrior(prior));
    } else {
      try {
        validatePrior(prior);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      predictor.feedPrior(prior);
    }
  }

  public String generate() {
    preprocessing();
    initPredictor();
    return predictor.generateResponse();
  }

  public void generateToCmdString() {
    System.out.println(generate());
  }

  public void generateToCmdChar() {
    preprocessing();
    initPredictor();
    //generate response
    predictor.generateResponseToCmd();
  }

  public void preprocessing() {
    //generate counter table
    //generate counter table for lower k case Search.CUTFIRSTCHAR ToDo
    processor.process();
  }

  public void validatePrior(String prior) throws Exception {
    if (prior.length() != k || !processor.getCountTable().containsKey(prior)) {
      throw new Exception("Invalid prior!");
    }
  }

  private String fixPrior(String prior) {
    if (prior.length() > k) {
      return prior.substring(prior.length() - k);
    } else if (prior.length() < k) {
      StringBuilder fixedPrior = new StringBuilder(prior);
      do {
        fixedPrior.insert(0, ' ');
      } while (fixedPrior.length() < k);
      return fixedPrior.toString();
    }
    return prior;
  }
}
