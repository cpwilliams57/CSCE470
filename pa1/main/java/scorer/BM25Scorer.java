package scorer;

import ds.Document;
import ds.Query;
import utils.IndexUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Skeleton code for the implementation of a BM25 scorer in Task 2.
 */
public class BM25Scorer extends AScorer {

    /*
     *  TODO: You will want to tune these values
     */

    //using weights similar to vsm
    //After tuning, these values have been found to work the best
    double titleweight  = 0.2;
    double bodyweight = 0.1;

    // Using Weights Similar to VSM
    //After tuning, these values have been found to work the best
    double btitle = 0.2;
    double bbody = 0.1;

    //After tuning, these values have been found to work the best
    double k1 = 0.5;
    double pageRankLambda = 0.12;
    double pageRankLambdaPrime = 0.1;

    // query -> url -> document
    Map<Query,Map<String, Document>> queryDict;

    // BM25 data structures--feel free to modify these
    // ds.Document -> field -> length
    Map<Document,Map<String,Double>> lengths;

    // field name -> average length
    Map<String,Double> avgLengths;

    // ds.Document -> pagerank score
    Map<Document,Double> pagerankScores;

    /**
     * Construct a scorer.BM25Scorer.
     * @param utils Index utilities
     * @param queryDict a map of query to url to document
     */
    public BM25Scorer(IndexUtils utils, Map<Query,Map<String, Document>> queryDict) {
        super(utils);
        this.queryDict = queryDict;
        this.calcAverageLengths();
    }

    /**
     * Set up average lengths for BM25, also handling PageRank.
     */
    public void calcAverageLengths() {
        lengths = new HashMap<>();
        avgLengths = new HashMap<>(); 
        pagerankScores = new HashMap<>();

        /*
         * TODO : Your code here
         * Initialize any data structures needed, perform
         * any preprocessing you would like to do on the fields,
         * accumulate lengths of fields.
         * handle pagerank.
         *
         */


            /*
             * TODO : Your code here
             * Normalize lengths to get average lengths for
             * each field (body, title).
             */

        //******************************************************************
        //Get the lengths of each document

        double bodyLength = 0.0;
        double titleLength = 0.0;
        int docCount = 0;

            for(Map.Entry<Query,Map<String, Document>> outer : queryDict.entrySet()){
                for(Map.Entry<String,Document> inner : outer.getValue().entrySet()){

                    //Temporary Map for storage
                    Map<String,Double> tempMap = new HashMap<>();

                    //retrieve values for body and title length
                    Document tempdoc = inner.getValue();
                    double tempRank = tempdoc.page_rank;
                    bodyLength = tempdoc.body_length;
                    titleLength = tempdoc.title_length;

                    //pushing values to tempmap
                    tempMap.put("body",bodyLength);
                    tempMap.put("title",titleLength);

                    //Pushing tempmap to length
                    lengths.put(tempdoc,tempMap);
                    pagerankScores.put(tempdoc,tempRank);

                    docCount = docCount + 1;
                }
            }

        //******************************************************************
        //getting the average lengths for the field types
        double bodySum = 0.0;
        double titleSum = 0.0;
        double bodyLengthAvg = 0.0;
        double titleLengthAvg = 0.0;

        //go through each document in lengths and sum lengths for body and title;
        for(Map.Entry<Document,Map<String, Double>> outer : lengths.entrySet()){
            for(Map.Entry<String,Double> inner : outer.getValue().entrySet()){
                if(inner.getKey().equals("body")){
                    bodySum = bodySum + inner.getValue();
                }
                else{
                    titleSum = titleSum + inner.getValue();
                }
            }
        }

        //Calculate the average
        bodyLengthAvg = (bodySum / docCount);
        titleLengthAvg = (titleSum / docCount);

        //put the average in avglewngths
        avgLengths.put("body",bodyLengthAvg);
        avgLengths.put("title", titleLengthAvg);
    }

    /**
     * Get the net score.
     * @param tfs the term frequencies
     * @param q the ds.Query
     * @param tfQuery
     * @param d the ds.Document
     * @return the net score
     */
    public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {

        double score = 0.0;

        /*
         * TODO : Your code here
         * Use equation 3 first and then equation 4 in the writeup to compute the overall score
         * of a document d for a query q.
         */

        //******************************************************************
        //equation 3
        //Variable initialization
        Map<String,Double> bodyelms = new HashMap<>();
        bodyelms = tfs.get("body");
        Map<String,Double> titleelms = new HashMap<>();
        titleelms = tfs.get("title");

        Map<String,Double> wdt = new HashMap<>();
        double bodyCalc = 0.0;
        double titleCalc = 0.0;
        double wdtCalc;

        //caclulating wdt and storing in a map
       if(bodyelms != null ){
           for(Map.Entry<String,Double> elm : bodyelms.entrySet()){
               String word = elm.getKey();
               bodyCalc = bodyweight * elm.getValue();

               if(titleelms.containsKey(word)){
                   titleCalc = titleweight * (titleelms.get(word));
               }

               wdtCalc = bodyCalc + titleCalc;
               wdt.put(word,wdtCalc);
           }

       }


        //******************************************************************
        //equation 4 values
        double idf = 0.0;
        double denom = 0.0;

        //get pagerank of document d
        double pgrnk = pagerankScores.get(d);

        //possibly tune
       // double v = Math.log(pageRankLambdaPrime + pgrnk);
        double v = pgrnk / (pageRankLambdaPrime + pgrnk);

        double totalDocs = utils.totalNumDocs();
        double docFreq = 0.0;
        double wdtval = 0.0;

        //calculating final score based oon equation provided in the pdf
        for(Map.Entry<String,Double> elm : wdt.entrySet()){
            //get the idf of the term
            docFreq = utils.docFreq(elm.getKey());
            idf = Math.log(totalDocs / docFreq);
            //wdt
            wdtval = elm.getValue();
            //calc K1 + Wdt
            denom = k1 + elm.getValue();
            score = score + ((wdtval / denom) * idf) + (pageRankLambda*v);
        }

        return score;
    }

    /**
     * Do BM25 Normalization.
     * @param tfs the term frequencies
     * @param d the ds.Document
     * @param q the ds.Query
     */
    public void normalizeTFs(Map<String,Map<String, Double>> tfs, Document d, Query q) {
        /*
         * TODO : Your code here
         * Use equation 2 in the writeup to normalize the raw term frequencies
         * in fields in document d.
         */

        //Temporary hashmap for storage
        Map<String, Map<String, Double>> temptfs = new HashMap<>();
        //Calculate average lengths of bidy for the training set
        double avgBodyLength = avgLengths.get("body");
        //Calculate Average lenth of the titles for the training set
        double avgTitleLength = avgLengths.get("title");

        //Initiializing variables that will be used
        double bodyLength = d.body_length;
        double titleLength = d.title_length;
        double normlength = 0.0;
        double denom1 = 0.0;
        double denom2 = 0.0;


        //iterate through all elements in tfs
        for(Map.Entry<String, Map<String,Double>> outer: tfs.entrySet()){
            //Go through the term values in tfs
            String field = outer.getKey();
            for(Map.Entry<String,Double> inner : outer.getValue().entrySet()){
                //Get raw term frequency
                Double rawtf = inner.getValue();
                //Get the term
                String term = inner.getKey();
                //Check if title or field value
                if(field.equals("body")){
                    //calculating normalized freq for body
                    denom1 = 1 - bbody;
                    denom2 = bbody * (bodyLength / avgBodyLength);
                    normlength = (rawtf / (denom1 + denom2));
                }
                else{
                    //calculating normalized freq for title
                    denom1 = 1 - btitle;
                    denom2 = btitle * (bodyLength / avgBodyLength);
                    normlength = (rawtf / (denom1 + denom2));
                }
                //putting our normalized values in temptfs
                Map<String,Double> tempmap = new HashMap<>();
                tempmap.put(term,normlength);
                temptfs.put(field,tempmap);
            }
        }

        //setting tfs to temp tfs
        tfs = temptfs;

    }

    /**
     * Write the tuned parameters of BM25 to file.
     * Only used for grading purpose, you should NOT modify this method.
     * @param filePath the output file path.
     */
    private void writeParaValues(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            String[] names = {
                    "titleweight", "bodyweight", "btitle",
                    "bbody", "k1", "pageRankLambda", "pageRankLambdaPrime"
            };
            double[] values = {
                    this.titleweight, this.bodyweight, this.btitle,
                    this.bbody, this.k1, this.pageRankLambda,
                    this.pageRankLambdaPrime
            };
            BufferedWriter bw = new BufferedWriter(fw);
            for (int idx = 0; idx < names.length; ++ idx) {
                bw.write(names[idx] + " " + values[idx]);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    /**
     * Get the similarity score.
     * @param d the ds.Document
     * @param q the ds.Query
     * @return the similarity score
     */
    public double getSimScore(Document d, Query q) {
        Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
        this.normalizeTFs(tfs, d, q);
        Map<String,Double> tfQuery = getQueryFreqs(q);

        // Write out the tuned BM25 parameters
        // This is only used for grading purposes.
        // You should NOT modify the writeParaValues method.
        writeParaValues("bm25Para.txt");
        return getNetScore(tfs,q,tfQuery,d);
    }

}
