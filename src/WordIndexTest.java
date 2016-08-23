import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;

import org.junit.Test;

public class WordIndexTest {
/*
 * Add new word to new URL
 * Add existing word to new URL
 * Add existing word to existing URL
 * Add new word to existing URL
 * 
 * These are the cases to test
 */
	@Test
	public void test() {
		//Initialization Test
		String filename = "unitTestIndex";
		WordIndex.delete(filename);
		File wordFile = new File(filename + ".bin");
		File wordListFile = new File(filename + ".wordlist.bin");
		File urlListFile = new File(filename + ".urllist.bin");
		File hashListFile = new File(filename + ".hashindex.bin");
		assertFalse(wordFile.exists());
		assertFalse(wordListFile.exists());
		assertFalse(urlListFile.exists());
		assertFalse(hashListFile.exists());
		WordIndex.initialize(filename, 4);
		assertTrue(wordFile.exists());
		assertTrue(wordListFile.exists());
		assertTrue(urlListFile.exists());
		assertTrue(hashListFile.exists());
		
		String wordOne = "cat";
		String wordTwo = "dog";
		String urlOne = "cat.com";
		String urlTwo = "dog.com";
		WordIndex testWordIndex = new WordIndex(filename);
		
		//New Word to New Url
		testWordIndex.add(wordOne, urlOne);
		//Existing Word to New Url
		testWordIndex.add(wordOne, urlTwo);
		//New Word to Existing Url
		testWordIndex.add(wordTwo, urlOne);
		//Existing Word to Existing Url
		testWordIndex.add(wordOne, urlOne);
		
		//Word Two should only be on Url One once
		//Word One should be on Url One twice, and Url Two once
		Iterator<UrlEntry> wordOneIt = testWordIndex.getUrls(wordOne);
		while(wordOneIt.hasNext()){
			UrlEntry entry = wordOneIt.next();
			System.out.println(wordOne);
			System.out.println("Url: " + entry.getUrl());
			System.out.println("Count: " + entry.getCount() + "\n");
		}
		Iterator<UrlEntry> wordTwoIt = testWordIndex.getUrls(wordTwo);
		while(wordTwoIt.hasNext()){
			UrlEntry entry = wordTwoIt.next();
			System.out.println(wordTwo);
			System.out.println("Url: " + entry.getUrl());
			System.out.println("Count: " + entry.getCount() + "\n");
		}
		
		WordIndex.delete(filename);
	}
}