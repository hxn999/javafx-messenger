package com.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileLineCounter {
    public static long countLines(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)).count();
    }
}
