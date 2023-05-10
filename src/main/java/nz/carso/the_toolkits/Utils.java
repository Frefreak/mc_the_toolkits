package nz.carso.the_toolkits;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

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
}
