/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact: 
 *
 * Authors: 
 */

package test;

import java.awt.*;
import java.awt.event.*;

import jvn.*;
import java.io.*;
import java.util.ArrayList;

import irc.Sentence;
import irc.SentenceInterface;

public class TestStress {
	public TextArea text;
	public TextField data;
	Frame frame;
	SentenceInterface sentence;

	public static void main(String argv[]) {
		try {
			int stress = 100000000;
			SentenceInterface resource = (SentenceInterface) JvnProxy.newInstance("IRC", Sentence.class);
			long t1 = System.nanoTime();
			for(int i = 0; i < stress; ++i) {
				resource.write("" + i);
			}
			long t2 = System.nanoTime();
			System.out.println((t2 - t1) / 1000000 );
			System.out.println(resource.read());
		} catch (Exception e) {
			System.out.println("IRC problem : " + e.getMessage());
		}
	}
}