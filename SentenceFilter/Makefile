projdir = /home/dvs23/projects/BlankIT/SentenceFilter/
all:
	mkdir -p obj
	mkdir -p obj/Release
	mkdir -p obj/Release/src
	mkdir -p bin
	mkdir -p bin/Release
	g++ -Wall -fexceptions -O3 -std=c++11 -Iinclude -c $(projdir)main.cpp -o obj/Release/main.o
	g++ -Wall -fexceptions -O3 -std=c++11 -Iinclude -c $(projdir)src/SentenceFilter.cpp -o obj/Release/src/SentenceFilter.o
	g++  -o bin/Release/SentenceFilter obj/Release/main.o obj/Release/src/SentenceFilter.o  -s -lmysqlcppconn -lboost_regex -pthread -Wl,--no-as-needed -ldl  /usr/lib/libboost_regex.a
	
debug:
	mkdir -p obj
	mkdir -p obj/Debug
	mkdir -p obj/Debug/src
	mkdir -p bin
	mkdir -p bin/Debug
	g++ -w -std=c++11 -g -Wall -fexceptions -Iinclude -I/usr/include -c $(projdir)main.cpp -o obj/Debug/main.o
	g++ -w -std=c++11 -g -Wall -fexceptions -Iinclude -I/usr/include -c $(projdir)src/SentenceFilter.cpp -o obj/Debug/src/SentenceFilter.o
	g++  -o bin/Debug/SentenceFilter obj/Debug/main.o obj/Debug/src/SentenceFilter.o  -lmysqlcppconn -lboost_regex -pthread -Wl,--no-as-needed -ldl  /usr/lib/libboost_regex.a