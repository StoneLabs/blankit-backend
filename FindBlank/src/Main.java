/*
    Copyright (c) 2016, Levy Ehrstein. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * Basic Word class containing fundamental informations used in the analysation process of
 * the algorithm. 
 * 
 * This is a DOC
 */
class Word
{
	private String 	m_word		;
	private boolean m_noun		;
	private int 	m_frequency	;
	
	public void SetWord			(String  newVal) { this.m_word 		= newVal; }
	public void SetNoun			(boolean newVal) { this.m_noun 		= newVal; }
	public void SetFrequency	(int     newVal) { this.m_frequency = newVal; }
	
	public String	GetWord()		{ return m_word; 		}
	public boolean	IsNoun()		{ return m_noun; 		}
	public int		GetFrequency()	{ return m_frequency; 	}
	
	
	public Word(String word, boolean Noun, int frequency)
	{
		this.m_word 		= word;
		this.m_noun 		= Noun;
		this.m_frequency 	= frequency;
	}
}


public class Main 
{
	//Database ip
	private static String DBIP = "127.0.0.1";
	
	/**
	 * Main class.
	 *
	 * @param  args	Arg0: 'quatschFaktor' describing how accurate the algorithm chooses the words to blank out.
	 * 				Arg1: Database IP (Standart localhost)
	 */
	public static void main(String[] args)
	{
		int Fq = 1;
		if (args.length > 0)	Fq 	 = Integer.parseInt(args[0]);
		if (args.length > 1)	DBIP = args[1];
		
		Connection conn = GetDBConnection();
		
		BufferedReader inputBiffer = 
				new BufferedReader(new InputStreamReader(System.in));
		
		String inputSentence = "#WAITING_FOR_INPUT";
		
		for (;;) //Till break;
		{
			try		{ inputSentence = inputBiffer.readLine(); } 
			catch 	( IOException e ) 
			{ Debug.Error("Invalid input stream!");	}
			
			if (inputSentence.contains("[@24]"))
				break;
			
			List<Word> 	words	= AnalyseSentence	(conn, inputSentence);
			String 		ret		= StripOutAndConcat	(words, Fq			);
			
			if (ret != null)
				System.out.println(ret + "[@23]");
		}
	}
	
	/**
	 * Splits sentences in word lists.
	 *
	 * @param  sentence  One sentence represented as a String.
	 * @return	Returns a String array containing the individual words if the sentence.
	 */
	public static String[] SplitSentence(String sentence)
	{ return sentence.split(" "); }
	
	/**
	 * Reads the sentence in a List of Words containing information about the role in the sentence and
	 * the frequency of the word.
	 *
	 * @param  	conn  Database connection object
	 * @param	sentence	The sentence represented as a single String
	 * @return	The sentence represented as a List of Words (OBJECT)
	 */
	public static List<Word> AnalyseSentence(Connection conn, String sentence)
	{		
		String[] 	parts = SplitSentence(sentence);
		List<Word> 	words = new ArrayList<Word>();
				
		for (int i = 0; i < parts.length; i++)
		{
			String part = parts[i];
			
			boolean noun 		= IsWordNoun(conn, part);
			int 	frequency 	= noun ? GetWordCount(conn, part) : -1;
			
			Word word = new Word(part, noun, frequency);
			words.add(word);
		}
		return words;
	}
	

	/**
	 * Calculates and blanks out the most important part of the sentence.
	 *
	 * @param  	words  The input sentence represented as a Word (OBJECT) List
	 * @param	Fq	QuatschFaktor
	 * @return	The sentence represented as a single String in BlankProtocoll.
	 */
	public static String StripOutAndConcat(List<Word> words, int Fq)
	{
		//Below: index of word in list by frequency
		Map<Integer, Integer> nounsFQMap = new HashMap<Integer, Integer>();
		
		for (int i = 1; i < words.size(); i++) //Start with 1, so first word cannot be taken!
			if (words.get(i).IsNoun()) 
					nounsFQMap.put(i, 
							words.get(i).GetFrequency());
		
		if (nounsFQMap.size() == 0) return null;
		
		nounsFQMap = sortByValue(nounsFQMap);
		
		int maxTake 	= nounsFQMap.size();
		int selection 	= (int)Math.floor(Math.random() * Math.min(maxTake, Fq));
		int index 		= (int)nounsFQMap.keySet().toArray()[selection];
		
		words.get(index).SetWord(
				"[@22]" + words.get(index).GetWord()
				); 
		
		
		//CONCAT
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < words.size(); i++)
		{
			boolean lastWord = i == words.size()-1;
			sb.append(words.get(i).GetWord() + (lastWord ? "" : " "));
		}
		
		return sb.toString();
	}
	
	//BELOW CURRENTLY NOT IN USE
	public static Map<Integer, Integer> SortMapByKey(Map<Integer, Integer> orgMap)
	{
		TreeMap<Integer, Integer> treeMap = new TreeMap<Integer, Integer>();

		for (int i = 0; i < orgMap.size(); i++)
		{
			Integer index = (Integer) orgMap.keySet().toArray()[i];
			treeMap.put(index, orgMap.get(index));
		}
		return treeMap;
	}
	//ABOVE CURRENTLY NOT IS USE


	/**
	 * Sorts a Map by its value set using default comperator logic.
	 *
	 * @param  	cmap	THe input map to be sorted
	 * @return	The sorted map
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> 
    	sortByValue( Map<K, V> map )
	{
	    List<Map.Entry<K, V>> list =
	        new LinkedList<Map.Entry<K, V>>( map.entrySet());
	    
	    Collections.sort( list, 
		    new Comparator<Map.Entry<K, V>>()
		    {
		        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
		        {
		            return (o1.getValue()).compareTo( o2.getValue() );
		        }
		    } 
	    );
	
	    Map<K, V> result = new LinkedHashMap<K, V>();
	    
	    for (Map.Entry<K, V> entry : list)
	    {
	        result.put( entry.getKey(), entry.getValue() );
	    }
	    
	    return result;
	}


	/**
	 * Check whether or not a word is known to be a noun.
	 *
	 * @param  	conn  Database connection object
	 * @param	word	The word to be checked
	 * @return	Whether or not the word is noun
	 */
	public static boolean IsWordNoun(Connection conn, String word)
	{
		try
		{
			PreparedStatement stmt = conn.prepareStatement(SQL_countInNounTable);
			stmt.setString(1, word);
			
			ResultSet rs = stmt.executeQuery();
			
			rs.next();
			return rs.getInt("count") == 1;
			
		} catch (SQLException e) 
		{ Debug.Error(e.getMessage()); }
		return false;
	}

	/**
	 * Function to measure word frequency
	 *
	 * @param  	conn  Database connection object
	 * @param	word	The word which frequency shall be counted
	 * @return	The frequency value of the DB
	 */
	public static int GetWordCount(Connection conn, String word)
	{
		try
		{
			PreparedStatement stmt = conn.prepareStatement(SQL_getWordFrequency);
			stmt.setString(1, word);
			
			ResultSet rs = stmt.executeQuery();

			rs.next();
			return rs.getInt("count");

		} catch (SQLException e) 
		{ Debug.Error(e.getMessage()); }
		return -1;
	}

	/**
	 * Function to establish a new DB connection.
	 *
	 * @return	The initialized database connection object
	 */
	public static Connection GetDBConnection()
	{
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser			("jh");
		dataSource.setPassword		(""	 );
		dataSource.setDatabaseName	("WC");
		dataSource.setServerName	(DBIP);
		
		try {
			Connection conn;
			conn = dataSource.getConnection();
			
			conn.createStatement().executeQuery(SQL_setCollationConnection);
			return conn;
		} catch (SQLException e) 
		{ Debug.Error(e.getMessage()); }
		return null;
	}
	
	//Below: MySQL commands using in code above
	public static final String SQL_countInNounTable 		= "SELECT COUNT(*) AS count FROM Nomen WHERE Wort=?";
	public static final String SQL_getWordFrequency 		= "SELECT sum(anz) AS count FROM woAnz WHERE Wort=?";
	public static final String SQL_setCollationConnection 	= "SET collation_connection = 'latin1_general_cs'"	;
	
	
	
}
