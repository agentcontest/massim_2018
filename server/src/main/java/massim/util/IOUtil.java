package massim.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper for some I/O operations.
 */
public abstract class IOUtil {

    /**
     * Matches all shortest occurrences of "$(...)".
     */
    private static Pattern pattern = Pattern.compile("\"\\$\\(([^$]*)??\\)\""); //

    /**
     * Tries to read a JSON object from the given file/path.
     * @param path the path of the file to read
     * @return the JSON object parsed from the string or an empty JSON object if the string was not a valid object
     * @throws IOException if the file could not be read
     */
    public static JSONObject readJSONObject(String path) throws IOException {
        try {
            return new JSONObject(readString(path));
        } catch(JSONException e){
            Log.log(Log.Level.ERROR, "Error in JSON object");
            return new JSONObject();
        }
    }

    /**
     * Reads a JSON object replacing occurrences of "$(file)" with the content of file.
     * @param path path to the root JSON file
     * @return a JSON object parsed from the file or an empty object if the string was invalid
     * @throws IOException if one of the used files could not be read
     */
    public static JSONObject readJSONObjectWithImport(String path) throws IOException {
        try{
            return new JSONObject(readWithReplace(path));
        } catch(JSONException e){
            Log.log(Log.Level.ERROR, "Invalid configuration");
        }
        return new JSONObject();
    }

    /**
     * Reads a string from the given file, replacing instances of $(path) with the contents of the file at that path.
     * Must be cycle-free to terminate.
     * $(path) entries in referenced files are handled recursively,
     * the path always being relative to the referencing file.
     * @param path the path of the root file
     * @return a string where all occurrences of $(...) have been replaced recursively
     * @throws IOException if any one file could not be read
     */
    public static String readWithReplace(String path) throws IOException {
        String text = readString(path);
        Matcher m = pattern.matcher(text);
        StringBuffer result = new StringBuffer();
        File file = new File(path).getAbsoluteFile();
        Log.log(Log.Level.DEBUG, "Reading file " + file.getAbsolutePath());
        String subPath = "";
        while(m.find()){
            try {
                subPath = file.getParent() + "/" + m.group(1);
                m.appendReplacement(result, readWithReplace(subPath));
            } catch(NullPointerException e){
                e.printStackTrace();
                Log.log(Log.Level.ERROR, "Could not insert file " + subPath + " into " + path);
            }
        }
        m.appendTail(result);
        return result.toString();
    }

    /**
     * Reads text content from a given file.
     * @param path the path to the file
     * @return a string of the file's text content
     * @throws IOException if the file could not be read
     */
    public static String readString(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    /**
     * Writes a JSON object to file.
     * @param json the JSON object to output
     * @param file the file to write to
     * @return whether writing succeeded
     */
    public static boolean writeJSONToFile(JSONObject json, File file){
        File dir = file.getParentFile();
        if(!dir.exists()) dir.mkdirs();
        try {
            FileWriter out = new FileWriter(file);
            out.write(json.toString(4));
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Writes an XML object to file.
     * @param doc the XML document to output
     * @param file the file to write to
     * @param pretty whether to
     * @return whether writing succeeded
     */
    public static boolean writeXMLToFile(Document doc, File file, boolean pretty){
        File dir = file.getParentFile();
        if(!dir.exists()) dir.mkdirs();
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            if (pretty) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            Result output = new StreamResult(file);
            Source input = new DOMSource(doc);
            transformer.transform(input, output);
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
