#include "FileSearch.h"

//******************************************************
//Parse a given string into a vector of tokens
vector<string> FileSearch::parsesearch(string search_string){
    istringstream iss(search_string); //initiializing input stream
    vector<string> tokens;  //initializing data structure for results
    
    //tokenize
    copy(istream_iterator<string>(iss),
         istream_iterator<string>(),
         back_inserter(tokens));
    return tokens; //return vector of string holting tokens
}

//******************************************************
//Look for a given word in a document, return document ID if present
bool FileSearch::searchword(string filename,string token){
    ifstream file;  //initializing file stream
    file.open(filename); //open the file
    transform(token.begin(), token.end(), token.begin(), ::tolower); //lower token
    string word;
    
    //loop throught the file
    while (file >> word)
    {
        transform(word.begin(), word.end(), word.begin(), ::tolower); //lower word
        if(token == word){
            return true; //if token = word, word is present , return true
        }
        
    }
    return false; //if not, return false
}

//******************************************************
//Look for all files that contain a given word
vector<int> FileSearch::finddocs(string token){
    vector<int> docs; //initialize data structure for results
    glob_t glob_result;
    
    //Go through all files in the database directory
    glob("database/*",GLOB_TILDE,NULL,&glob_result);
    for(unsigned int i=0; i<glob_result.gl_pathc; ++i){
        //check to see of the given word is in the directory
        bool found = searchword(glob_result.gl_pathv[i], token);
        if(found){
            //if found, push document id to the docs vector
            string path = glob_result.gl_pathv[i];
            path.erase(0,9);
            int docid = stoi(path);
            docs.push_back(docid);
        }
    }
    return docs;//return vector of doc IDs that contain the token word
}

//******************************************************
//Create inverted index for the search string
vector<vector<int> > FileSearch::inverted_index(vector<string> search_words){
    vector<vector<int> > inverted_index; //Initializing return vector
   
    //find document occurences for each search word
    for(int i = 0; i < search_words.size(); i++){
        vector<int> temp = finddocs(search_words[i]);
        inverted_index.push_back(temp); //store in inverted index
    }
    return inverted_index;//return in inverted index
}

//******************************************************
//Find the intersecting docIds
vector<int> FileSearch::intersect(vector<int> v1, vector<int> v2){
    vector<int> common; //resulting vector
    
    sort(v1.begin(),v1.end());
    sort(v2.begin(),v2.end());
    
    //find the intersection of the two vectors and store in common
    set_intersection(v1.begin(), v1.end(), v2.begin(), v2.end(),std::inserter(common,common.begin()));
    
    return common; //return the intersection
}

//******************************************************
//find commnon
 vector<int> FileSearch::find_common(vector<int> v1, vector<int> v2){
     
     //sort the vectors
     sort(v1.begin(),v1.end());
     sort(v2.begin(),v2.end());

     vector<int> common;      //initialize holder
     
     //find the common elements
     for(int i = 0; i < v1.size(); i++){
         int cur = v1[i];
         for(int j = 0; j < v2.size(); j++){
             if(cur == v2[j]){
                 common.push_back(cur); //put into common
             }
         }
     }
     return common; // return common
}
//******************************************************
//Find the intersecting docIds from the inverted index
 vector<int> FileSearch::find_relevant(vector<vector<int> > sorted_ii){
    
    vector<int> relevant;
    
    if(sorted_ii.size() == 0){
        //if there are no elements in the sorted ii retunn "No Documents relevant to Search"
        cout << "Could not find relevant documents" << endl;
    }
    else if(sorted_ii.size() == 1){
        //If there is a single vector in the sorted ii
        if(sorted_ii[0].size() < 5){
            //if the size is less than 5, return all elements
            for(int i = 0; i < sorted_ii[0].size();++i){
                relevant.push_back(sorted_ii[0].at(i));
            }
        }
        else{
            //if it is greater than 5, return the first 5 elements of sii[0]
            for(int i = 0; i < 5 ;++i){
                relevant.push_back(sorted_ii[0].at(i));
            }
        }
    }
    else{
        //intitialize common documents
        vector<int> common = find_common(sorted_ii[0], sorted_ii[1]);
        
        //check to see if common documents were found
        if(common.size() == 0){
            //if none found,
            for(int i = 0; i < sorted_ii[0].size(); i++){
                relevant.push_back(sorted_ii[0].at(i));
            }
            for(int i = 0; i < sorted_ii[1].size(); i++){
                relevant.push_back(sorted_ii[1].at(i));
            }
        }
        else{
            int index = 0;
            while(common.size()>0 && (index + 1) < sorted_ii.size()){
                //increase index
                index = index + 1;
                //push common docments to relevant
                for(int i = 0; i < common.size(); i++){
                    relevant.push_back(common[i]);
                }
                //recalculate common
                common = find_common(common, sorted_ii[index]);
            }
        }
   
    }
     
     reverse(relevant.begin(), relevant.end());
     return relevant;
}

