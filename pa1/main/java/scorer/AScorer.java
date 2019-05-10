package scorer;

import ds.Document;
import ds.Query;
import jdk.jfr.Frequency;
import utils.IndexUtils;

import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


/**
 * An abstract class for a scorer.
 * Needs to be extended by each specific implementation of scorers.
 */
public abstract class AScorer {

    // Map: term -> idf
    IndexUtils utils;


    // Various types of term frequencies that you will need
    String[] TFTYPES = {"title","body"};

    /**
     * Construct an abstract scorer with a map of idfs.
     * @param utils index utility functions like map of idf scores
     */
    public AScorer(IndexUtils utils) {
        this.utils = utils;
    }

    /**
     * You can implement your own function to whatever you want for debug string
     * The following is just an example to include page information in the debug string
     * The string will be forced to be 1-line and truncated to only include the first 200 characters
     */
    public String getDebugStr(Document d, Query q)
    {
        return "Pagerank: " + Integer.toString(d.page_rank);
    }

    /**
     * Score each document for each query.
     * @param d the ds.Document
     * @param q the ds.Query
     */
    public abstract double getSimScore(Document d, Query q);

    /**
     * Get frequencies for a query.
     * @param q the query to compute frequencies for
     */
    public Map<String,Double> getQueryFreqs(Query q) {

        // queryWord -> term frequency
        Map<String,Double> tfQuery = new HashMap<>();
        Map<String, Double> rawQF = new HashMap<>();
        /*
         *
         * Compute the raw term frequencies
         * Additionally weight each of the terms using the idf value
         * of the term in the query (we use the provided text corpus to 
         * determine how many documents contain the query terms, which is stored
         * in this.idfs).
         * Multiply raw query frequency by IDF for document frequency
         */

        double idf = 0.0;
        double docFreq = 0.0;
        double totalDocs = utils.totalNumDocs();


        //*********************************************

        //Getting raw query frequency
        for(String w: q.queryWords){
            //Getting the query grequency for a query term
            Double n = tfQuery.get(w);
            n = (n == null) ? 1 : ++n;
            rawQF.put(w,n);
        }

        //Accounting for IDF
        for(Map.Entry<String,Double> qfelm: rawQF.entrySet()){
            //getting the document frequency for a certain term
            docFreq = utils.docFreq(qfelm.getKey());
            //Calculating idf for the term
            idf = Math.log(totalDocs / docFreq);
            //pushing term and IDF to tfquery map
            tfQuery.put(qfelm.getKey(),idf);
        }

        //*********************************************

        return tfQuery;
    }


    /*
     *
     * Include any initialization and/or parsing methods
     * that you may want to perform on the ds.Document fields
     * prior to accumulating counts.
     * See the ds.Document class in ds.Document.java to see how
     * the various fields are represented.
     */


    /**
     * Accumulate the various kinds of term frequencies
     * for the fields (title, body).
     * You can override this if you'd like, but it's likely
     * that your concrete classes will share this implementation.
     * @param d the ds.Document
     * @param q the ds.Query
     */
    public Map<String,Map<String, Double>> getDocTermFreqs(Document d, Query q) {

        // Map from tf type (field) -> queryWord -> score
        Map<String, Map<String, Double>> tfs = new HashMap<>();

        /*
         *
         * Initialize any variables needed
         */

        //modifying the doc title to a usable for for comparison
        Query title = new Query(d.title);

        for (String queryWord : q.queryWords) {
            /*
             *
             * Loop through query terms and accumulate term frequencies.
             * Note: you should do this for each type of term frequencies,
             * i.e. for each of the different fields.
             * Don't forget to lowercase the query word.
             */

            double titleFreq = 0.0;

            //***********************************************
            Map<String, Double> titleWord = new HashMap<>();
            Map<String, Double> bodyWord = new HashMap<>();
            Map<String, Double> tempTitle = new HashMap<>();

            for (String word : title.queryWords) {
                tempTitle.put(word, titleFreq);
            }

            //Going through the title and seeing if querywords are present

            //For every word in the title, getting raw tf
            for (String wordInTitle : title.queryWords) {
                //check if the current title word is equal to the query word
                if (wordInTitle.equals(queryWord)) { // if it is equal
                    //getting current freq of word
                    titleFreq = tempTitle.get(queryWord);
                    //increasing freq by 1
                    titleFreq = titleFreq + 1;

                }
            }

            //push frequency into tfs vector
            titleWord.put(queryWord, titleFreq);
            tfs.put("title", titleWord);

            //push body frequency into tfs vector
            if (d.body_hits != null)
                if (d.body_hits.containsKey(queryWord)) {
                    double n = d.body_hits.get(queryWord).size();
                    bodyWord.put(queryWord, n);
                    tfs.put("body", bodyWord);
                }
        }

        return tfs;
    }

}
