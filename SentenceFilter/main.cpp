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

#include <iostream>
#include <SentenceFilter.h>

#include "mysql_connection.h"

#include <cppconn/driver.h>
#include <cppconn/exception.h>
#include <cppconn/statement.h>
#include <cppconn/prepared_statement.h>

using namespace std;

sql::Driver* driver;
sql::Connection* con;
sql::Statement* stmt;


int main( int argc, char* argv[] )
{
    srand( time( NULL ) );//init random by time
    driver = get_driver_instance();
    if(argc==3){
        con = driver->connect( argv[2], "jh", "" );
    }
    else{
        con = driver->connect( "tcp://127.0.0.1:3306", "jh", "" );
    }
    //set the charset for the current connection to avoid collisions with invalid converions

    con->setSchema( "WC" );
    sql::Statement* stmt = con->createStatement();
    stmt->execute( "SET collation_connection = 'latin1_general_cs'" );
    delete stmt;

    string full = "";
    string line;
    unsigned int i = 0;

    while( getline( cin, line ) )//read until the pipe text ends (getline ends the loop) or until you only read EOF
    {
        if( line == "EOF" )
            break;

        full += line + "\n";
        i++;
    }

    SentenceFilter wc( con );//create the class with the connection of above
    wc.readText( full );//read the text to strs

    if( argc >= 2 )//if a max number of sentences is given -> use it -> if it is a string atoi returns 0
        wc.procData( atoi( argv[1] ) );//return sentences
    else//if there is no max given, print all (0)
        wc.procData( 0 );//return sentences

    return 0;
}
