import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ListFile {
	
	private RandomAccessFile listFile;
	
	public static void initialize(String listFileName){
		try {
			RandomAccessFile listFile = new RandomAccessFile(listFileName, "rw");
			listFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static void delete(String listFileName){
    	File file = new File(listFileName);
    	file.delete();
    }

    public ListFile(String listFileName){
    	try {
			listFile = new RandomAccessFile(listFileName, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }

    public void close(){
    	try {
			listFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public long newEntry(Entry entry){
    	try {
    		long returnVal = listFile.length();
			listFile.seek(returnVal);
			listFile.writeLong(entry.getString().length());
			listFile.writeChars(entry.getString());
			listFile.writeLong(entry.getValue());
			listFile.writeLong(entry.getLink());
			return returnVal;
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return -1;
    }

    public Entry getEntry(long offset){
    	try {
    		listFile.seek(offset);
			long strLength = listFile.readLong();
			String entryString = "";
			for(int i = 0; i < strLength; i++){
				entryString += listFile.readChar();
			}
			return new Entry(entryString, listFile.readLong(), listFile.readLong());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }

    public void putEntry(long offset, Entry entry){
    	try {
			listFile.seek(offset);
			listFile.writeLong(entry.getString().length());
			listFile.writeChars(entry.getString());
			listFile.writeLong(entry.getValue());
			listFile.writeLong(entry.getLink());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
}