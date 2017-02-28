package massim.util;

import org.json.JSONObject;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper for some I/O operations.
 */
public abstract class IOUtil {

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
