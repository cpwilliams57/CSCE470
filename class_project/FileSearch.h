#ifndef FileSearch_h
#define FileSearch_h

#include <stdio.h>
#include <string>
#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <algorithm>
#include <glob.h>
#include <iterator>

using namespace std;

class FileSearch {
public:

    FileSearch(){};  //Default Constructor
    
    //Functions that will be utilized by FileSearch
    vector<string> parsesearch(string search_string); //parse the search string
    bool searchword(string filename,string token); //searches a file for a word
    vector<int> finddocs(string token); //finds all documents where a given word occurs
    vector<vector<int> > inverted_index(vector<string> search_words); //get all document postings
    vector<int> intersect(vector<int> v1, vector<int> v2); //Find intersections of document vectors
    vector<vector<int> > sort_ii(vector<vector<int> > inverted_index);//sort inverted index in increasing order
    vector<int> find_common(vector<int> v1, vector<int> v2); // intersect 2 vectors
    vector<int> find_relevant(vector<vector<int> > sorted_ii); //finds relevant docs from inv index
    
};
#endif
