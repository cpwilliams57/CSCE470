#ifndef KmeansScorer_h
#define KmeansScorer_h
#include <stdio.h>
#include <iterator>
#include <iostream>
#include <fstream>
#include <map>
#include <string>
#include <vector>
using namespace std;

class KmeansScorer {
public:
    
    //global variable for stroing current assignment.
    vector<pair<int,int>> docnum_centnum;
   
    void print_results();
    
    //Default costructor
    KmeansScorer(){};
    
    //Helps with parsing the file
    string getNextToken(istream &in);
    
    //creates bag of words representation for a single file
    map<string,int> makebow(string file);
    
    //Normalizes the bag of words vector
    vector<map<string,double> > normalize(vector<map<string,int> > alldocs);
    
    //make a single vector of all bow representations
    vector< map<string,int> > makebowvec();
    
    //compute document means
    vector<pair<double,int> > computemeans(vector< map<string,double> > normdocs);
    
    //choose initial centroids and initial vector assignment
    pair< vector<pair<double,int>>, vector<pair<double,int> > > init_cent(int k, vector<pair<double,int> > docvals);
    
    //assign documents to a centroid
     vector<pair<double,int> > reassign(vector<pair<double,int> > cents,vector<pair<double,int> > docs);
    
    //recalculate centroids
     vector<pair<double,int> > calc_cents( vector<pair<double,int> > doc_cents);
    
   
};

#endif /* KmeansScorer_hpp */
