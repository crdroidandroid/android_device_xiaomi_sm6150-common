package org.lineageos.settings.device.doze.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class FileUtils {

    private FileUtils() {}

    public static boolean isFileWritable(String path) {
        return new File(path).canWrite();
    }

    public static boolean writeLine(String path, String value) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(value);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
