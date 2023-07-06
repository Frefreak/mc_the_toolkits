package nz.carso.the_toolkits;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utils {
    private static final Logger logger = LogManager.getLogger();
    public static Boolean saveFile(String content, String path) {
        // Get the Minecraft game folder location
        File gameDir = Minecraft.getInstance().gameDirectory;
        File ourDir = Paths.get(gameDir.getAbsolutePath(), Constants.MOD_ID).toFile();
        if (!ourDir.exists()) {
            boolean ok = ourDir.mkdir();
            if (!ok) {
                logger.error("make mod folder failed");
                return false;
            }
        }

        // Create a new JSON file in the game folder
        File jsonFile = Paths.get(ourDir.getAbsolutePath(), path).toFile();

        // Write the JSON data to the file
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static byte[] compressString(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static String decompressString(byte[] compressed) throws IOException {
        if (compressed == null || compressed.length == 0) {
            return "";
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8)) {
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;
            while ((bytesRead = inputStreamReader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, bytesRead);
            }
            return stringBuilder.toString();
        }
    }
    public static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
