#include <iostream>
#include <glob.h>
#include <fstream>
#include <stdio.h>
#include <getopt.h>
#include <iomanip>
#include "KmeansScorer.h"
using namespace std;

//**************************************************************
int main(int argc, char *argv[]) {

    //command line option for choosing number of clusters
    int k = 5;//default value for k
    int e = 3;// default value for e
    int c;
    while ((c = getopt (argc, argv, "k:e:")) != -1)
        switch (c)
    {
            
        case 'k':
            k = atoi(optarg);
            break;
        case 'e':
            e = atoi(optarg);
            break;
        case '?':
            if (optopt == 'k' || optopt == 'e')
                fprintf(stderr, "Option -%c requires an argument.\n", optopt);
            else if (isprint (optopt))
                fprintf (stderr, "Unknown option `-%c'.\n", optopt);
            else
                fprintf (stderr,
                         "Unknown option character `\\x%x'.\n",
                         optopt);
            return 1;
        default:
            abort ();
    }
    
    cout << "Number of clusters indicated " << k << endl; 

//**************************************************************
    // **** Part 1 : Building Normalized document representations for training set ****
    //Initialize the kscorer
    KmeansScorer kscore;
    cout << "K-scorer created..." << endl;
    
    //create a vector containing bag of wor representations for all documents
    cout << "Documents represented as bow vectors..." << endl;
    vector<map<string,int> > alldocs = kscore.makebowvec();
    //normalize each element of alldocs using euclidean normalization
    cout << "Documents normalized using euclidean normalization..." << endl;
    vector<map<string,double> > normdocs = kscore.normalize(alldocs);
    
//**************************************************************
    
    //**** Part 2 : Centroid Initialization ****
    //Find arethmetic mean for each document
    cout << "calculating means of each document..." << endl;
   
    //dont forget about this
    vector<pair<double,int> > docmeans = kscore.computemeans(normdocs);
    
    //choose arbitrary number of clusters (ranging from 2 - 20)
    cout << "Choosing initital centroids an initial vector assignment..." << endl;
    
    //initial centroid assignment, mean and cent #
    pair< vector < pair<double,int> >, vector< pair<double,int> > > init = kscore.init_cent(k, docmeans);
    vector<pair<double,int> > cent_assign = init.first;
    
//**************************************************************
    //Part 3
    //Reassignment of vectors: assign each document in the collection to
    //its closest centroid.
   
    cout << "Initial Assignment of documents to centroids" << endl;
    //initial vector assignment mean and doc #
    vector<pair<double,int> > vec_assign = init.second;
    
    
//**************************************************************
    //Part 4
    //Recomputation of centroids: calculate new centroids for clusters.
    
    for(int i = 0; i < e; i ++){
        //recomputing centroid assignment
        vector<pair<double,int> > doc_cents = kscore.reassign(cent_assign, vec_assign);
        //recomputing centroids
        vector<pair<double,int> > new_cents = kscore.calc_cents(doc_cents);
        cent_assign = new_cents;
    }
    
    //printing resulting centroid assignment to output document
    kscore.print_results();
    
    return 0;
}
