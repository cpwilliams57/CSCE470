# makefile

all: kscore 

main.o: main.cpp
	g++ -g -w -std=c++14 -c main.cpp

KmeansScorer.o: KmeansScorer.h KmeansScorer.cpp
	g++ -g -w -std=c++14 -c KmeansScorer.cpp

kscore: main.o KmeansScorer.o
	g++ -g -w -std=c++14 -o kscore main.o KmeansScorer.o

clean:
	rm *.o

