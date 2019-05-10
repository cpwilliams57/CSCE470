#include <iostream>
#include <string>
#include <getopt.h>
#include "FileSearch.h"
using namespace std;

//********************************************************************
//Getting arguemment from user
int main(int argc, char * argv[]) {
    
    //get command line arguement from user for number of relevant docs
    int n = 5;//default value for k
    int c;
    while ((c = getopt (argc, argv, "n:")) != -1)
        switch (c)
    {
            
        case 'n':
            n = atoi(optarg);
            break;
        case '?':
            if (optopt == 'n')
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

//********************************************************************
//Find documents relevant to the search
    
    //initialize the searcher
    FileSearch mysearch;
    
    //Recieve a search string from the user
    string search_string;
    getline(cin,search_string);
    
    //Parse the search string into tokens
    vector<string> searchtokens = mysearch.parsesearch(search_string);
    
    //create an inverted index from the search
    vector<vector<int> > invert_index = mysearch.inverted_index(searchtokens);
    
    //sort the inverted index from vector of smallest size to vector of biggest size
    //This will help with finding the most relevant documents
    sort(invert_index.begin(), invert_index.end(),
         [](const vector<int> & a, const vector<int> & b){ return a.size() < b.size(); });
    
    
    //Check inverted index for empty elements, this will help with relevance
    for(vector<vector<int>>::iterator i=invert_index.begin();i!=invert_index.end();){
        if(i->size() == 0){
            i = invert_index.erase(i);
        }
        else
            ++i;
    }
    
    //find relevant documents
    vector<int> relevant = mysearch.find_relevant(invert_index);
    
    
//********************************************************************
//display documents on the screen
    //if the size of relevant docs is less than the indicated number, return all
    if(relevant.size() < n ){
        for(int i= 0; i < relevant.size();i++){
            string file = to_string(relevant[i]);
            string cmd = "open database/" + file;
            system(cmd.c_str());
        }
    }
    else{
        for(int i= 0; i < n; i++){
            string file = to_string(relevant[i]);
            string cmd = "open database/" + file;
            system(cmd.c_str());
        }
    }
    
    return 0;
    
}
