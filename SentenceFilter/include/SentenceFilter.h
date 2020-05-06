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

#ifndef SENTENCEFILTER_H
#define SENTENCEFILTER_H

#include <iostream>

#include <boost/regex.hpp>
#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string/regex.hpp>

#include <google/sparse_hash_set>
#include <google/sparse_hash_map>

#include "mysql_connection.h"
#include <cppconn/driver.h>
#include <cppconn/exception.h>
#include <cppconn/statement.h>
#include <cppconn/prepared_statement.h>


using namespace std;
using namespace boost::algorithm;
using namespace boost;


using google::sparse_hash_map;
using google::sparse_hash_set;

class SentenceFilter {
    public:
        SentenceFilter( sql::Connection* c );//contructor gets connection to mysql DB
        int readText( string text );//read a text from a long plain string
        int procData( unsigned int maxi );//process data stored in strs, giving the max number of sentences to return (0=all)
    protected:
    private:
        vector<vector<vector<string>>> strs;//[text][satz][wort] -> scalable -> multiple texts at once
        sql::Connection* con;//store that connection in class
};

#endif // SENTENCEFILTER_H
