package pt.ua.tai.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHandler {


    public String readTxtFileToString(String fileName) throws IOException {
        return Files.readString(Paths.get(fileName));
    }
}
