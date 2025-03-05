package pt.ua.tai.utils;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public final class FileUtil {

  private static final Logger log = Logger.getLogger(FileUtil.class.getName());

  private FileUtil() {
  }

  public static String readFile(String fileName) {
    InputStream resourceFileInputStream = getInputStreamOfResourceFile(fileName);
    if (resourceFileInputStream == null) {
      return readExternalFile(fileName);
    }
    return readFromInputStream(resourceFileInputStream);
  }

  private static InputStream getInputStreamOfResourceFile(String fileName) {
    ClassLoader classLoader = FileUtil.class.getClassLoader();
    return classLoader.getResourceAsStream(fileName);
  }

  private static String readExternalFile(String fileName) {
    try {
      log.log(WARNING, "file not found under resources, now trying exact path: {0}", fileName);
      FileReader reader = new FileReader(fileName);
      return readFromFileReader(reader);
    } catch (FileNotFoundException e) {
      log.log(SEVERE, "file not found: {0}", fileName);
      return null;
    }
  }

  private static String readFromInputStream(InputStream inputStream) {
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      readFromBufferedReader(br, resultStringBuilder);
    } catch (IOException e) {
      log.log(SEVERE, "error while reading the file content using input stream", e);
      return null;
    }
    return resultStringBuilder.toString();
  }

  private static String readFromFileReader(FileReader fileReader) {
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(fileReader)) {
      readFromBufferedReader(br, resultStringBuilder);
    } catch (IOException e) {
      log.log(WARNING, "error while reading the file content using file reader", e);
      return null;
    }
    return resultStringBuilder.toString();
  }

  private static void readFromBufferedReader(BufferedReader br, StringBuilder resultStringBuilder)
      throws IOException {
    String line;
    while ((line = br.readLine()) != null) {
      resultStringBuilder.append(line).append("\n");
    }
  }
}
