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

import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import java.lang.Thread;

import java.text.SimpleDateFormat;

/**
 * Debug enum containing the different debug levels used by Debug class
 */
enum DebugLevel
{
	MINIMAL, LOW, MEDIUM, HIGH;
}


/**
 * Main Debug class containing a collection of debug methods.
 */
public class Debug
{
    private static void out(String prefix, String text, DebugLevel debugLevel)
    {
    	StringBuilder sb = new StringBuilder();
    	Formatter formatter = new Formatter(sb, Locale.US);
    	
        switch (debugLevel)
        {
        	case LOW:
        		formatter.format("[%s@%s]: %s", prefix, GetCallerClassName(), text);
        		break;
        	case MEDIUM:
        		formatter.format("[%s@%s@%s]: %s", prefix, GetCallerMethodeName(),
        												   GetCallerClassName(), text);
        		break;
        	case HIGH:
        		formatter.format("[%s@%s@%s@%s.%-4d]: %s", prefix, GetCallerMethodeName(),
															  	   GetCallerClassName()  , 
															  	   GetCallerFileName()   , 
															  	   GetCallerLineNumber() ,
															  	   text);
        		break;
        	default:
        		formatter.format("[%s]: %s", prefix, text);
        		break;
        }
        
        System.out.println(sb.toString());
        formatter.close();
    }
    

	/**
	 * Splits sentences in word lists.
	 *
	 * @param  sentence  One sentence represented as a String.
	 * @return	Returns a String array containing the individual words if the sentence.
	 */
    public static void Clear()
    {
        System.out.print("\f");
    }
    
    public static void Seperator()
    {
    	System.out.println();
    }
    public static void Log(String text) 
    { 
        out("DEBUG"		, text, DebugLevel.MINIMAL); 
    }    
    public static void Warning(String text)  
    { 
        out("WARNING"	, text, DebugLevel.LOW); 
    } 
    public static void Error(String text) 
    { 
        out("ERROR"		, text, DebugLevel.HIGH); 
        System.exit(1);
    } 
    public static void Todo()
    { 
        out("TODO"		, "TODO CALLED IN CODE", DebugLevel.HIGH); 
    } 

    private static String GetCallerMethodeName(){ return GetStackTraceElement().getMethodName() ; }
    private static String GetCallerClassName() 	{ return GetStackTraceElement().getClassName() 	; }
    private static String GetCallerFileName() 	{ return GetStackTraceElement().getFileName() 	; }
    private static int GetCallerLineNumber() 	{ return GetStackTraceElement().getLineNumber()	; }
    
    private static StackTraceElement GetStackTraceElement()
    {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Debug.class.getName())  &&
                ste.getClassName().indexOf("java.lang.Thread")!=0) {
                return ste;
            }
        }
        return null;
    }
    public static String GetCurrentTimeStamp() 
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");//"yyyy-MM-dd HH:mm:ss.SSS"
        return sdf.format(new Date()); //now = new Date();
    }
}
