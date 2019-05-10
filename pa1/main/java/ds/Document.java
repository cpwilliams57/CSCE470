package ds;

import java.util.List;
import java.util.Map;

/**
 * The class is used to store useful information for a document.
 * You can also write the document to a string for debugging.
 */
public class Document {

    public String url;
    public String title = null;

    // term -> [list of positions]
    public Map<String, List<Integer>> body_hits = null;
    public int body_length = 0;
    public int title_length = 0;
    public int page_rank = 0;

    // debug string for you to debug your implementation
    public String debugStr = "";

    /**
     * Constructs a document with a String url.
     * @param url the url associated with a document.
     */
    public Document(String url) {
        this.url=url;
    }

    /**
     * Returns a String representation of a ds.Document.
     * @return the String of fields representing a ds.Document
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");
        result.append("url: "+ url + NEW_LINE);
        if (title != null) result.append("title: " + title + NEW_LINE);
        if (body_hits != null) result.append("body_hits: " + body_hits.toString() + NEW_LINE);
        if (body_length != 0) result.append("body_length: " + body_length + NEW_LINE);
        if (title_length != 0) result.append("title_length: " + title_length + NEW_LINE);
        if (page_rank != 0) result.append("page_rank: " + page_rank + NEW_LINE);
        return result.toString();
    }
}
