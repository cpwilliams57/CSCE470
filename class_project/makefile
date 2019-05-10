# makefile

all: search 

main.o: main.cpp
	g++ -g -w -std=c++14 -c main.cpp

FileSearch.o: FileSearch.h FileSearch.cpp
	g++ -g -w -std=c++14 -c FileSearch.cpp

search: main.o FileSearch.o
	g++ -g -w -std=c++14 -o search main.o FileSearch.o

clean:
	rm *.o

