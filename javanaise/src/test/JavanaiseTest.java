package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.junit.Test;

import irc.Sentence;
import irc.SentenceInterface;
import jvn.JvnProxy;
import jvn.JvnServerImpl;

public class JavanaiseTest {

	@Test
	public final void testSaturation1() {
		try {
			ArrayList<SentenceInterface> ar = new ArrayList<SentenceInterface>();
			for(int i = 0; i <= 3; ++i) {
				ar.add((SentenceInterface) JvnProxy.newInstance("tS1_"+i, Sentence.class));
			}
			String obtainedOutput = JvnServerImpl.jvnGetServer().getHashmap().keySet().toString();
			String expectedOutput = "[tS1_3, tS1_2, tS1_1]";
			assertEquals(obtainedOutput, expectedOutput);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public final void testSaturation2() {
		try {
			ArrayList<SentenceInterface> ar = new ArrayList<SentenceInterface>();
			for(int i = 0; i <= 3; ++i) {
				ar.add((SentenceInterface) JvnProxy.newInstance("tS1_"+i, Sentence.class));
				if(i == 0)
					ar.get(0).read();
				if(i == 1)
					ar.get(1).write("text");
			}
			String obtainedOutput = JvnServerImpl.jvnGetServer().getHashmap().keySet().toString();
			String expectedOutput = "[tS1_3, tS1_1, tS1_0]";
			assertEquals(obtainedOutput, expectedOutput);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
