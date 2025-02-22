package pt.ua.iky;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public final class Common {

  private static final Logger log = Logger.getLogger(Common.class.getName());

  private Common() {
  }

  public static String readFile(String fileName) {
    ClassLoader classLoader = Common.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(fileName);
    if (inputStream == null) {
      log.log(WARNING, "file not found under resources, now trying exact path: {0}", fileName);
      try {
        FileReader reader = new FileReader(fileName);
        return readFromInputStream(reader);
      } catch (FileNotFoundException e) {
        log.log(SEVERE, "file not found: {0}", fileName);
      }
      return null;
    }
    return readFromInputStream(inputStream);
  }

  private static String readFromInputStream(InputStream inputStream) {
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
      }
    } catch (IOException e) {
      log.log(SEVERE, "error while reading the file content", e);
      return null;
    }
    return resultStringBuilder.toString();
  }

  private static String readFromInputStream(FileReader fileReader) {
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(fileReader)) {
      String line;
      while ((line = br.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
      }
    } catch (IOException e) {
      log.log(WARNING, "error while reading the file content", e);
      return null;
    }
    return resultStringBuilder.toString();
  }
}
