/*
    Copyright (c) 2016, David Schmidt. All rights reserved.
    This file is part of BlankIT.

    BlankIT is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BlankIT is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BlankIT.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von BlankIT.

    BlankIT ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    BlankIT wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
*/

#include "SentenceFilter.h"

SentenceFilter::SentenceFilter( sql::Connection* c )
{
    con = c;
}

int SentenceFilter::readText( string text )
{
    strs.clear();//clear maybe already stored texts

    vector<vector<string>> lstrs;//list of sentences with it's words for the current text

    boost::regex re1( "[\?!;,\()\"\':]|(?<![0-9])[.]" );//replace all kinds of special cases with single spaces to make words only seperated by one space
    string full = boost::regex_replace( text, re1, " $& " );
    boost::regex re2( "[ ]+" );
    full = boost::regex_replace( full, re2, " " );
    boost::regex re3( "([\[][\[])(.*?)([]][]])" );
    full = boost::regex_replace( full, re3, "$&$2\n" );//integrate headline into text


    vector<string> tem;
    boost::algorithm::split_regex( tem, full, boost::regex( "(?<![0-9])[.]|[!?\n\r]" ) );//split by points if there is no number before it and by ! and ?

    for( unsigned int j = 0; j < tem.size(); j++ )//split sentences into words by spaces and newlines
    {
        vector<string> tem2;
        boost::algorithm::split( tem2, tem[j], is_any_of( " \n\r" ) );
        lstrs.push_back( tem2 );//insert sentence into local list
    }

    //remove empty words and sentences
    for( unsigned int k = 0; k < lstrs.size(); k++ )
    {
        for( unsigned int j = 0; j < lstrs[k].size(); j++ )
        {
            if( lstrs[k][j].length() <= 0 )
            {
                lstrs[k].erase( lstrs[k].begin() + j );
                j--;
            }
        }

        if( lstrs[k].size() == 0 )
        {
            lstrs.erase( lstrs.begin() + k );
            k--;
        }
    }

    strs.push_back( lstrs );//add current text to global storage

    return 0;
}

string makeSentence( vector<string> sen )//make sentence of word vector
{
    string ret = "";//string to return

    for( size_t i = 0; i < sen.size(); i++ )//go through all words
    {
        ret += sen[i];//add them together, seperated by spaces -> also , because they do not belong to the word
        ret += " ";
    }

    ret += ".";//add the final dot
    return ret;
}

int SentenceFilter::procData( unsigned int maxi )
{
    sparse_hash_map<string, unsigned int> wos;//list of diferent words occuring in the text

    for( unsigned int l = 0; l < strs.size(); l++ )//go through each word -> index of text
    {
        for( unsigned int k = 0; k < strs[l].size(); k++ )//-> index of sentence
        {
            for( unsigned int j = 0; j < strs[l][k].size(); j++ )//-> index of word
                wos[strs[l][k][j]] = 0;//initialize map -> first assign 0 -> will be replaced with the correct number
        }
    }

    sql::PreparedStatement* loadAnz = con->prepareStatement( "select Wort,anz from woAnz where Wort = ?;" );
    //woAnz contains the word as string and its number of occurrences in the german Wikipedia
    //fetch the correct numbers from DB
    unsigned int su = 0;//save the sum of the number of occurrences of the words in the current text
    unsigned int si = 0;//save the amount of the words in the current text

    for( auto t = wos.begin(); t != wos.end(); ++t )//go through all words of the given text
    {
        loadAnz->setString( 1, t->first );//insert word to prepared statement
        sql::ResultSet* res = loadAnz->executeQuery();//run the query and store the (single) result

        while( res->next() )
        {
            wos[t->first] = res->getUInt( "anz" );//assign the correct number of occurrences
            su += res->getUInt( "anz" );
            si++;
        }

        delete res;//free the space of the result
    }

    delete loadAnz;//free the space of the query

    double mid = ( double )su / ( double )si;//save the median

    vector<string> validSen;//valid sentences to choose from

    for( unsigned int l = 0; l < strs.size(); l++ )//go through all sentences and text(s)
    {
        for( unsigned int k = 0; k < strs[l].size(); k++ )
        {
            unsigned int counter = 0;//count how many words of the current sentence are below the median

            for( unsigned int j = 0; j < strs[l][k].size(); j++ )
            {
                if( wos[strs[l][k][j]] <= mid )//if the word's number of occurrences is below or equal to the median, it is "accepted"
                    counter++;
            }

            //filter out sentences with 3 words or fewer and sentences where nearly all or nearly no words are "important" -> filter by percentage
            if( strs[l][k].size() > 3 && ( double )counter / ( double )strs[l][k].size() > 0.15 && ( double )counter / ( double )strs[l][k].size() < 0.85 )
            {
                string s = makeSentence( strs[l][k] );//make sentence from word vector

                if( !boost::regex_match( s.c_str(), boost::regex( ".*?[=*)(:\\]\\[/0-9]+.*?|^[^A-Z].*?" ) ) )//filter out sentences with annoying special characters
                    //filtered out: =*)(:][/0-9 or if the first character of the sentence is not a capital letter
                    validSen.push_back( s );//if the sentence fulfills all these requirements, add it to the glorious league of valid sentences ;)
            }
        }
    }

    sparse_hash_set<unsigned int> usedSen;//sentences already printed to console

    if( maxi == 0 )//if maxi is 0, all sentences will be printed
        maxi = validSen.size();

    while( usedSen.size() < maxi && usedSen.size() < validSen.size() ) //print sentences until the max number is reached or all sentences have been printed
    {
        unsigned int ra = rand() % validSen.size();//random number as index for sentence

        if( usedSen.find( ra ) == usedSen.end() ) //if index has already been used, don't use it again, try another number
        {
            cout << validSen[ra] << endl;//print sentence
            usedSen.insert( ra ); //remember that index has been used
        }
    }

    cout << "[@24]" << endl;//mark text end for FindBlank.jar

    return 0;
}
