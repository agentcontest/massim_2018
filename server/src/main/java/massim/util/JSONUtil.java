package massim.util;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper for some JSON operations.
 */
public abstract class JSONUtil {

    public static void writeToFile(JSONObject json, File file){
        File dir = file.getParentFile();
        if(!dir.exists()) dir.mkdirs();
        try {
            FileWriter out = new FileWriter(file);
            out.write(json.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
