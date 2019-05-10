#include "KmeansScorer.h"
#include <glob.h>
#include <math.h>
#include <iomanip>




//**************************************************************
//choose initial centroids and initial vector assignment
void KmeansScorer::print_results(){
    //initialize output stream
    ofstream o_file;
    o_file.open("pred.txt");
    for(int i = 0; i < docnum_centnum.size(); i++){
        o_file << setfill('0') << setw(5) << docnum_centnum[i].first << "\t" << docnum_centnum[i].second << endl;
    }
    o_file.close();
}

//**************************************************************
//choose initial centroids and initial vector assignment
 pair< vector<pair<double,int>> , vector<pair<double,int> > > KmeansScorer::init_cent(int k, vector<pair<double,int> > docvals){
    
    vector<pair<double,int> > centroids;
    vector<pair<double,int>> cent_assignt;
     
     
    //divide doc vals into K sections, select first doc in the section as an arbitrary centroid
    int itr_val = (docvals.size() / k) + 1;
    int d = 1;
    int cent;
    
    
     //initial selection of centroids and vector assignment
     for(int i = 0; i < docvals.size(); i++){
         if ((i % itr_val) == 0){
            cent = docvals[i].second;
             //produce a vector of pairs contiainting centroid mean and centroid #
            centroids.push_back(pair<double, int>(docvals[i].first,d));
            cout << "centroid # " << d << ", Doc # " << docvals[i].second << endl;
            d = d + 1;
        }
         //produce a vector of pairs contiainting doc mean and doc #
         cent_assignt.push_back(pair<double,int>(docvals[i].first,docvals[i].second));
         docnum_centnum.push_back(pair<int,int>(docvals[i].second, d -1));
    }
     
     //return the assigned vectoers
     return pair< vector < pair<double,int> >, vector< pair<double,int> > >(centroids,cent_assignt);
}

//**************************************************************
//compute arethmetic mean of each normalized document
  vector<pair<double,int> > KmeansScorer::reassign(vector<pair<double,int> > cents,vector<pair<double,int> > docs){
    
    vector<pair<int,int>> holder;
    
    //doc value, cent assignmnet
    vector<pair<double,int>> doc_cents;
    
    //find centroid nerewst to each document
    for(int i = 0; i < docs.size(); i++){
        double curr_dist = sqrt((docs[i].first * docs[i].first) - ((cents[0].first) * cents[0].first));
        double next_dist;
        int cent_num = 1;
        for(int j = 0; j < docs.size(); j++){
            next_dist = sqrt((docs[i].first * docs[i].first) - ((cents[j].first) * cents[j].first));
            if(next_dist < curr_dist){
                curr_dist = next_dist;
                cent_num = cents[j].second;
            }
        }
        
        //put into the temporary holder
        holder.push_back(pair<int,int>(docs[i].second,cent_num));
        
        //put into the  new_cents holder
        doc_cents.push_back(pair<double,int>(docs[i].first,cent_num));
        
    }
    
    docnum_centnum = holder;
      
      //returns doc values with assigned centroid
    return doc_cents;
}

//**************************************************************
//compute new centroids
 vector<pair<double,int> > KmeansScorer::calc_cents(vector<pair<double,int> > doc_cents){
    
    //given value and centnum
    
    vector<pair<double,int> > new_cents;
   
    double avg = 0;
    double sum = 0;
    int count = 1;
    
    for(int i = 0; i < doc_cents.size(); i++){
        
        int cur = doc_cents[i].second;
        int next = doc_cents[i + 1].second;
        sum = sum + doc_cents[i].first;
        
        if(cur != next){
            avg = sum / count;
            new_cents.push_back(pair<double,int>(avg,cur));
            avg = 0;
            sum = 0;
            count = 0;
        }
        count ++;
    }
    
     return new_cents;
}

//**************************************************************
//compute arethmetic mean of each normalized document
vector<pair<double,int> > KmeansScorer::computemeans(vector<map<string, double> > normdocs){
    vector<pair<double,int> > means;
    
    //calculate mean of every document
    for(int i = 0; i < normdocs.size(); i++){
        double sum = 0;
        double docsize = normdocs[i].size();
        for( auto& entry: normdocs[i]){
            sum = sum + entry.second;
        }
        double mean = sum / docsize;
        means.push_back(pair<double,int>(mean,i));
    }
    
    //sort the map from least to greatest mean
    sort(means.begin(), means.end(), [](auto &left, auto &right) {
        return left.first < right.first;
    });
    
    return means;
}

//**************************************************************
//normalize bag of words representations for all docs
vector<map<string,double> > KmeansScorer::normalize(vector<map<string,int> > alldocs){
    
    vector<map<string,double> > normalized;
    //loop throgh the vector
    for(int i = 0; i < alldocs.size(); i++){
        //initialize values for normalization calculation
        double docsize = alldocs[i].size();
        map<string,double> temp;
        //loop through each map in the vector and normalize the elements
        for( auto& entry: alldocs[i]){
            double normval = ((entry.second) * (entry.second))/(sqrt(docsize));
            string word = entry.first;
            temp.insert(pair<string,double>(word,normval));
        }
        normalized.push_back(temp);
    }
    
    return normalized;
}


//**************************************************************
//This function iterates through the data directory and creates
//a vector of bag of words for each file;
vector<map <string,int> > KmeansScorer::makebowvec(){
    vector<map <string,int> > bowvec;
    glob_t glob_result;
    glob("data/train/*",GLOB_TILDE,NULL,&glob_result);
    for(unsigned int i=0; i<glob_result.gl_pathc; ++i){
        map<string,int> temp = makebow(glob_result.gl_pathv[i]);
        bowvec.push_back(temp);
    }
    return bowvec;
}

//**************************************************************
//This function locates individual words within a document
string KmeansScorer:: getNextToken(istream &in)
{
    char c;
    string ans="";
    c=in.get();
    while(!isalpha(c) && !in.eof())
    {
        c=in.get();
    }
    while(isalpha(c))
    {
        ans.push_back(tolower(c));
        c=in.get();
    }
    return ans;
}

//**************************************************************
//This function creates the bag of words vector
map<string,int> KmeansScorer::makebow(string file){
    
    map<string,int> bow;
    ifstream fin(file);
    string word;
    string empty ="";
    while((word=getNextToken(fin))!=empty )
    ++bow[word];
    return bow;
}
