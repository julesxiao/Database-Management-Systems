package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A heap file stores a collection of tuples. It is also responsible for managing pages.
 * It needs to be able to manage page creation as well as correctly manipulating pages
 * when tuples are added or deleted.
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {
	
	public static final int PAGE_SIZE = 4096;
	
	private File file;
	private TupleDesc type;
	private int numPages;
	/**
	 * Creates a new heap file in the given location that can accept tuples of the given type
	 * @param f location of the heap file
	 * @param types type of tuples contained in the file
	 */
	public HeapFile(File f, TupleDesc type) {
		//your code here
		this.file = f;
		this.type = type;
		this.numPages = (int) (this.getFile().length()/HeapFile.PAGE_SIZE);
	}
	
	public File getFile() {
		//your code here
		return this.file;
	}
	
	public TupleDesc getTupleDesc() {
		//your code here
		return this.type;
	}
	
	/**
	 * Creates a HeapPage object representing the page at the given page number.
	 * Because it will be necessary to arbitrarily move around the file, a RandomAccessFile object
	 * should be used here.
	 * @param id the page number to be retrieved
	 * @return a HeapPage at the given page number
	 */
	public HeapPage readPage(int id) {
		//your code here
		//reference: https://examples.javacodegeeks.com/core-java/io/randomaccessfile/java-randomaccessfile-example/
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(this.getFile().getPath(), "r");
			file.seek(id * HeapFile.PAGE_SIZE);
			byte[] bytes = new byte[HeapFile.PAGE_SIZE];
	        file.read(bytes);
	        file.close();
	        return new HeapPage(id, bytes, this.getId());
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns a unique id number for this heap file. Consider using
	 * the hash of the File itself.
	 * @return
	 */
	public int getId() {
		//your code here
		return this.getFile().hashCode();
	}
	
	/**
	 * Writes the given HeapPage to disk. Because of the need to seek through the file,
	 * a RandomAccessFile object should be used in this method.
	 * @param p the page to write to disk
	 */
	public void writePage(HeapPage p) {
		//your code here
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(this.getFile().getPath(), "rw");
			file.seek(p.getId() * HeapFile.PAGE_SIZE);
	        file.write(p.getPageData());
	        file.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Adds a tuple. This method must first find a page with an open slot, creating a new page
	 * if all others are full. It then passes the tuple to this page to be stored. It then writes
	 * the page to disk (see writePage)
	 * @param t The tuple to be stored
	 * @return The HeapPage that contains the tuple
	 * @throws Exception 
	 */
	public HeapPage addTuple(Tuple t) throws Exception {
		//your code here
		for (int i = 0; i < this.getNumPages(); i++) {
			HeapPage heapPage = this.readPage(i);
			if (!heapPage.pageOccupied()) {
				try {
					heapPage.addTuple(t);
					this.writePage(heapPage);
					return heapPage;
				} catch (Exception e) {
					throw e;
				}
			}
		}
		try {
			HeapPage heapPage = new HeapPage(this.getNumPages(), new byte[HeapFile.PAGE_SIZE], this.getId());
			heapPage.addTuple(t);
			this.writePage(heapPage);
			this.numPages++;
			return heapPage;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method will examine the tuple to find out where it is stored, then delete it
	 * from the proper HeapPage. It then writes the modified page to disk.
	 * @param t the Tuple to be deleted
	 * @throws Exception 
	 */
	public void deleteTuple(Tuple t) throws Exception{
		//your code here
		HeapPage heapPage = readPage(t.getPid());
		try {
			heapPage.deleteTuple(t);
			this.writePage(heapPage);
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Returns an ArrayList containing all of the tuples in this HeapFile. It must
	 * access each HeapPage to do this (see iterator() in HeapPage)
	 * @return
	 */
	public ArrayList<Tuple> getAllTuples() {
		//your code here
		ArrayList<Tuple> list = new ArrayList<>();
		for (int i = 0; i < this.getNumPages(); i++) {
			HeapPage heapPage = this.readPage(i);
			heapPage.iterator().forEachRemaining(tuple -> list.add(tuple));
		}
		return list;
	}
	
	/**
	 * Computes and returns the total number of pages contained in this HeapFile
	 * @return the number of pages
	 */
	public int getNumPages() {
		//your code here
		return this.numPages;
	}
}
