/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.awt.*;
import java.awt.event.*;

import jvn.*;
import java.io.*;
import java.util.ArrayList;

public class Irc3 {
	public TextArea text;
	public TextField data;
	Frame frame;
	SentenceInterface sentence;

	public static void main(String argv[]) {
		try {
			int lol = 4;
			
			ArrayList<SentenceInterface> ar = new ArrayList<SentenceInterface>();
			for(int i = 0; i < lol; ++i) {
				ar.add((SentenceInterface) JvnProxy.newInstance("IRC_"+i, Sentence.class));
				if(i == 0)
					ar.get(0).read();
				if(i == 1)
					ar.get(1).write("un truc a ecrire");
			}
		} catch (Exception e) {
			System.out.println("IRC problem : " + e.getMessage());
		}
	}
}