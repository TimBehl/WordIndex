import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WordIndex {

	private PersistentArray hashIndex;

	private RandomAccessFile indexFile;

	private ListFile wordLists;
	//String: Word
	//Link: Offset of next entry in list
	//Value: Offset to the URL list for this word
	private ListFile urlLists;
	//String: URL
	//Link: Offset of next entry in list
	//Value: Number of times word is in URL
	private static final String WORDLIST_NAME = ".wordlist.bin";
	private static final String URLLIST_NAME = ".urllist.bin";
	private static final String HASHINDEX_NAME = ".hashindex.bin";

	public static void initialize(String indexName, long indexSize){
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(indexName + ".bin", "rw");
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ListFile.initialize(indexName + WORDLIST_NAME);
		ListFile.initialize(indexName + URLLIST_NAME);
		PersistentArray.initialize(indexName + HASHINDEX_NAME, (int) indexSize, -1);
	}

	public static void delete(String indexName){
		File file = new File(indexName + ".bin");
		File wordLists = new File(indexName + WORDLIST_NAME);
		File urlLists = new File(indexName + URLLIST_NAME);
		File hashIndex = new File(indexName + HASHINDEX_NAME);
		file.delete();
		wordLists.delete();
		urlLists.delete();
		hashIndex.delete();
	}

	public WordIndex(String indexName){
		try {
			indexFile = new RandomAccessFile(indexName + ".bin", "rw");
			this.wordLists = new ListFile(indexName + WORDLIST_NAME);
			this.urlLists = new ListFile(indexName + URLLIST_NAME);
			this.hashIndex = new PersistentArray(indexName + HASHINDEX_NAME);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void close(){
		try {
			indexFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void add(String word, String url){
		if(hashIndex.get(getHashIndex(word)) == -1){
			//New Entry to HashIndex
			long urlOffset = urlLists.newEntry(new Entry(url, 1, -1));
			long wordOffset = wordLists.newEntry(new Entry(word, urlOffset, -1));
			hashIndex.set(getHashIndex(word), wordOffset);
		}else{
			if(getWordOffset(word) == -1){
				//Existing Hash, New Word
				Entry lastWord = getLastWord(getHashIndex(word));
				long urlOffset = urlLists.newEntry(new Entry(url, 1, -1));
				long wordOffset = wordLists.newEntry(new Entry(word, urlOffset, -1));
				lastWord.setLink(wordOffset);
				wordLists.putEntry(getWordOffset(lastWord.getString()), lastWord);
			}else{
				if(getUrlOffset(word, url) == -1){
					//Existing Word, New URL
					Entry lastUrl = getLastUrl(word);
					long urlOffset = urlLists.newEntry(new Entry(url, 1, -1));
					lastUrl.setLink(urlOffset);
					urlLists.putEntry(getUrlOffset(word, url), lastUrl);
				}else{
					//Errythang is herr
					Entry urlEntry = urlLists.getEntry(getUrlOffset(word, url));
					urlEntry.setValue(urlEntry.getValue() + 1);
					urlLists.putEntry(getUrlOffset(word, url), urlEntry);
				}
			}
		}
	}

	public Iterator<UrlEntry> getUrls(String word){
		List<UrlEntry> urls = new ArrayList<UrlEntry>();
		Entry lookingEntry = urlLists.getEntry(wordLists.getEntry(getWordOffset(word)).getValue());
		if(lookingEntry.getLink() == -1){
			urls.add(new UrlEntry(lookingEntry.getString(), lookingEntry.getValue()));
		}else{
			while(lookingEntry.getLink() != -1){
				urls.add(new UrlEntry(lookingEntry.getString(), lookingEntry.getValue()));
				lookingEntry = urlLists.getEntry(lookingEntry.getLink());
			}
			urls.add(new UrlEntry(lookingEntry.getString(), lookingEntry.getValue()));
		}
		return urls.iterator();
	}
	
	private int getHashIndex(String word){
		return (int) Math.abs(word.hashCode() % hashIndex.getLength());
	}
	
	private long getWordOffset(String word){
		long listOffset = hashIndex.get(getHashIndex(word));
		if(listOffset == -1){
			return -1;
		}else{
			Entry newEntry, oldEntry;
			newEntry = wordLists.getEntry(listOffset);
			if(newEntry.getString().equalsIgnoreCase(word)){
				return listOffset;
			}else{
				while(newEntry.getLink() == -1){
					oldEntry = newEntry;
					newEntry = wordLists.getEntry(newEntry.getLink());
					if(newEntry.getString().equalsIgnoreCase(word)){
						return oldEntry.getLink();
					}
				}
			}
		}
		return -1;
	}
	
	private long getUrlOffset(String word, String url){
		long listOffset = wordLists.getEntry(getWordOffset(word)).getValue();
		Entry oldEntry;
		Entry newEntry = urlLists.getEntry(listOffset);
		if(newEntry.getString().equalsIgnoreCase(url)){
			return listOffset;
		}else{
			while(newEntry.getLink() != -1){
				oldEntry = newEntry;
				newEntry = urlLists.getEntry(newEntry.getLink());
				if(newEntry.getString().equalsIgnoreCase(url)){
					return oldEntry.getLink();
				}
			}
		}
		return -1;
	}
	
	private Entry getLastWord(int index){
		Entry returnVal = wordLists.getEntry(hashIndex.get(index));
		while(returnVal.getLink() != -1){
			returnVal = wordLists.getEntry(returnVal.getLink());
		}
		return returnVal;
	}
	
	private Entry getLastUrl(String word){
		Entry returnVal = urlLists.getEntry(wordLists.getEntry(getWordOffset(word)).getValue());
		while(returnVal.getLink() != -1){
			returnVal = urlLists.getEntry(returnVal.getLink());
		}
		return returnVal;
	}
}