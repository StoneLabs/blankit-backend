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


//***** THIS PROJECT IS NOT IN USE AND MAY NOT BE USED *****//
//***** THIS PROJECT IS NOT IN USE AND MAY NOT BE USED *****//
//***** THIS PROJECT IS NOT IN USE AND MAY NOT BE USED *****//
//***** THIS PROJECT IS NOT IN USE AND MAY NOT BE USED *****//

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class Main 
{
	public static String DBIP = "127.0.0.1";
	public static void main(String[] args)
	{		
		int Cnt = 1;
		if (args.length > 0)
			Cnt = Integer.parseInt(args[0]);
		if (args.length > 1)
			DBIP = args[1];
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));        
		String satz = "";
		
		int add;
		try{
			while ((add = br.read()) != -1)
			{
				satz += (char)add;
				if (satz.contains("[@24]"))
					break;
			}
		}catch (Exception ex) {Debug.Error("Error in input stream");}


		Connection conn = GetDBConnection();
		
		satz = satz.replace("[@24]", "");
		satz = SanitizeString(satz);
		List<String> sentences = SplitText(satz);
		Map<String, Float> sentenceSort = new TreeMap<String, Float>();
		for (int i = 0; i < sentences.size(); i++)
		{
			float value = CalculateSentenceValue(conn, sentences.get(i));
			if (value > 0)
				sentenceSort.put(sentences.get(i), value);
//			Debug.Log("ADD: " + sentences.get(i) + "[" + value + "]");
			//Map<Integer, String> sentenceSort = new TreeMap<Integer, String>();		
		}
		sentenceSort = sortByValueInvers(sentenceSort);
//		Debug.Log(""+sentenceSort.size());
		
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < Cnt && i < sentenceSort.size(); i++)
			ret.append(sentenceSort.keySet().toArray()[i] + "\n");
		System.out.println(ret.toString());
	}
	
	public static String SanitizeString(String sentence)
	{
//		sentence = sentence.replace(". ", ".");
//		sentence = sentence.replace("! ", "!");
//		sentence = sentence.replace("? ", "?");
		return sentence;
	}
	
	public static List<String> SplitText(String sentence)
	{
		List<String> ret = new ArrayList<String>();
		String[] add = sentence.split("(?<![0-9])[.]|[!?\n\r]");
		for (int i = 0; i < add.length; i++)
			ret.add(add[i] + (i == add.length - 1 ? "" : "."));
		return ret;
	}
	
	public static float CalculateSentenceValue(Connection conn, String sentence)
	{		
		String[] words = SplitSentence(sentence);
		
		if (sentence.matches(".*?[=*)(:\\]\\[/0-9]+.*?|^[A-Z].*?"))
			return -1f;
		
		long sum = 0;
		int nounC = 0;
		for (String word : words)
			if (IsWordNoun(conn, word))
			{
				nounC++;
				sum += GetWordCount(conn, word);
			}
		
	
		return (nounC > 1 ? sum/nounC : -1f);
	}
	
	public static String[] SplitSentence(String sentence)
	{
//		sentence = sentence.replace("  ", " ");
		
		String[] ret = sentence.split(" ");
		return ret;
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValueInvers( Map<K, V> map )
	{
	    List<Map.Entry<K, V>> list =
	        new LinkedList<Map.Entry<K, V>>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>()
	    {
	        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
	        {
	            return (o2.getValue()).compareTo( o1.getValue() );
	        }
	    } );
	
	    Map<K, V> result = new LinkedHashMap<K, V>();
	    for (Map.Entry<K, V> entry : list)
	    {
	        result.put( entry.getKey(), entry.getValue() );
	    }
	    return result;
	}
	
	public static boolean IsWordNoun(Connection conn, String word)
	{
		try
		{
//			Debug.Log("Checking... " + word);
			PreparedStatement stmt = conn.prepareStatement(
					"SELECT COUNT(*) AS count FROM Nomen WHERE Wort=?");
			stmt.setString(1, word);
			
			ResultSet rs = stmt.executeQuery();
			
			rs.next();
			return rs.getInt("count") == 1;
		} catch (SQLException e) {
			Debug.Error("Hier bin ich!!! FEHLER FEHLER!! \n\n" + 
			e.getMessage() + "\n\n" + e.getStackTrace());
		}
		return false;
	}
	
	public static int GetWordCount(Connection conn, String word)
	{
		try
		{
			PreparedStatement stmt = conn.prepareStatement(
				"SELECT sum(anz) AS count FROM woAnz WHERE Wort=?");
			stmt.setString(1, word);
			ResultSet rs = stmt.executeQuery();

			rs.next();
			return rs.getInt("count");
		} catch (SQLException e) {
			Debug.Error("Hier bin ich!!! FEHLER FEHLER!! \n\n" + 
			e.getMessage() + "\n\n" + e.getStackTrace());
		}
		return -1;
	}
	
	public static Connection GetDBConnection()
	{
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("jh");
		dataSource.setPassword("");
		dataSource.setDatabaseName("WC");
		//dataSource.setPort("3306");
		dataSource.setServerName(DBIP);
		Connection conn;
		try {
			conn = dataSource.getConnection();
			conn.createStatement().executeQuery("SET collation_connection = 'latin1_general_cs'");
			return conn;
			
		} catch (SQLException e) {
			Debug.Error("Hier bin ich!!! FEHLER FEHLER!! \n\n" + 
			e.getMessage() + "\n\n" + e.getStackTrace());
		}
		return null;
	}
}
