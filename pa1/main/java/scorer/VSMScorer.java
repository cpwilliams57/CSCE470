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
 * Skeleton code for the implementation of a
 * Cosine Similarity scorer in Task 1.
 */
public class VSMScorer extends AScorer {

    /* Tune
     * the weights for each field.
     */

    //title weight should be greater than the body weight
    //From testing several different parameters, these values give the best ndcg score
    double titleweight  = 0.3;
    double bodyweight = 0.1;

    /**
     * Construct a Cosine Similarity scorer.
     * @param utils Index utilities to get term/doc frequencies
     */
    public VSMScorer(IndexUtils utils) {
        super(utils);
    }

    /**
     * Get the net score for a query and a document.
     * @param tfs the term frequencies
     * @param q the ds.Query
     * @param tfQuery the term frequencies for the query
     * @param d the ds.Document
     * @return the net score
     */
    public double getNetScore(Map<String, Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {

        /*
         *
         * See Equation 1 in the handout regarding the net score
         * between a query vector and the term score vectors
         * for a document.
         */
        //qvq · ((ct ·tfdt) + (cb ·tfdb))

        ////***********************************************
        //populating two maps for body and title
        //Initialize variables
        double score = 0.0;
        double weight = 0.0;
        Map<String,Double> bodyMap = new HashMap<>();
        Map<String,Double> titleMap = new HashMap<>();

        //populate tilemap and query map
        for(String word: q.queryWords){
            bodyMap.put(word,0.0);
            titleMap.put(word,0.0);
        }

        for(Map.Entry<String,Map<String,Double>> outerEntry : tfs.entrySet()){
            //go through title and body elements
            for(Map.Entry<String,Double> innerEntry : outerEntry.getValue().entrySet()) {
                //Getting key of inner entry
                String qword = innerEntry.getKey();
                if(outerEntry.getKey().equals("body")){
                    weight = bodyweight * innerEntry.getValue();
                    bodyMap.put(qword,weight);
                }
                else{
                    weight = titleweight * innerEntry.getValue();
                    titleMap.put(qword,weight);
                }
            }
        }

        double t = 0.0;
        double b = 0.0;
        double sum = 0.0;
        ////***********************************************
        //Add the two maops together
        Map<String,Double> sumTB = new HashMap<>();
        for(String qword : q.queryWords){
            t = titleMap.get(qword);
            b = bodyMap.get(qword);
            sum = t + b;
            sumTB.put(qword,sum);
        }

        ////***********************************************
        //get the dot product of the query vec and sums.
        for(String qword : q.queryWords){
            score += score + tfQuery.get(qword) * sumTB.get(qword);
        }

        return score;
    }

    /**
     * Normalize the term frequencies.
     * @param tfs the term frequencies
     * @param d the ds.Document
     * @param q the ds.Query
     */
    public void normalizeTFs(Map<String,Map<String, Double>> tfs, Document d, Query q) {
        //

        //* //

        //* Note that we should use the length of each field

        //* for term frequency normalization as discussed in the assignment handout.

        //

        ////***********************************************

        Map<String, Map<String,Double>> tempTfs = new HashMap<>(); //tempory tfs holder
        double totalDocs = utils.totalNumDocs(); //Total number of documents in the collection
        double bodyLength = d.body_length;
        double titleLength = d.title_length;
        double normLength;

        //Go through body and title
        for(Map.Entry<String,Map<String,Double>> outerEntry : tfs.entrySet()){
            //go through title and body elements
            for(Map.Entry<String,Double> innerEntry : outerEntry.getValue().entrySet()){
                //Getting key of inner entry
                String qword = innerEntry.getKey();
                //creating temporary hashmap for storage of values
                HashMap tempmap = new HashMap();
                if(outerEntry.getKey() == "body"){
                    //calculating normalized doc frequency
                    normLength = (innerEntry.getValue() / bodyLength);
                    tempmap.put(qword,normLength);
                    tempTfs.put("body",tempmap);
                }
                else{
                    normLength = (innerEntry.getValue() / titleLength);
                    tempmap.put(qword,normLength);
                    tempTfs.put("title",tempmap);
                }
            }
        }


        //Assigning tfs to our tfs with normalized values;
        tfs = tempTfs;
        ////***********************************************

    }

    /**
     * Write the tuned parameters of vsmSimilarity to file.
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
                    "titleweight", "bodyweight"
            };
            double[] values = {
                    this.titleweight, this.bodyweight
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
    /** Get the similarity score between a document and a query.
     * @param d the ds.Document
     * @param q the ds.Query
     * @return the similarity score.
     */
    public double getSimScore(Document d, Query q) {
        Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
        this.normalizeTFs(tfs, d, q);
        Map<String,Double> tfQuery = getQueryFreqs(q);

        // Write out tuned vsmSimilarity parameters
        // This is only used for grading purposes.
        // You should NOT modify the writeParaValues method.
        writeParaValues("vsmPara.txt");
        return getNetScore(tfs,q,tfQuery,d);
    }
}
