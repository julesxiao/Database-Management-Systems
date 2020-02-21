package hw4;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.Tuple;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool which check that the transaction has the appropriate
 * locks to read/write the page.
 */
public class BufferPool {
    /** Bytes per page, including header. */
    public static final int PAGE_SIZE = 4096;

    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;
    
    private int pageSize = 0;
    
    private ArrayList<HeapPage> heapPages = new ArrayList<HeapPage>();
    
    // transactions: <transaction_id, locks>
    private HashMap<Integer, ArrayList<Lock>> transactions = new HashMap<Integer, ArrayList<Lock>>(); 

    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        // your code here
    	this.pageSize = numPages;
    }

    private class Lock {
    	/** each lock has 3 parameters:
        * @param lockTableId the ID of the table with the requested page
        * @param lockPid the ID of the requested page
        * @param lockPer the requested permissions on the page
        */
    	private int lockTableId = 0;
    	private int lockPid = 0;
    	private Permissions lockPer = null;

    	public Lock(int lockPageId, int lockTid,Permissions lockP) {
			/**
			 * Creates a new lock that can keep track of the lock info
			 */
    		this.lockPer = lockP;
    		 
    		this.lockTableId = lockTid;
    		this.lockPid = lockPageId;
    	}
    	
    	/**
    	 * Returns a page Id for this lock
    	 */
    	public int getPid() {
    		return this.lockPid;
    	}
    	
    	public int getTableId() {
    		return this.lockTableId;
    	}
    	
    	/**
    	 * Returns requested permissions to a lock.
    	 * Private constructor with two static objects READ_ONLY and READ_WRITE that
    	 * represent the two levels of permission.
    	 */
    	public Permissions getPermissionType() {
    		return this.lockPer;
    	}
    }
    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param tableId the ID of the table with the requested page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public HeapPage getPage(int tid, int tableId, int pid, Permissions perm)
        throws Exception {
        // your code here
    	// check if that lock is held by another transaction
    	if(checkBlock(tid, pid, tableId)) {
    		transactionComplete(tid, false);
    		return null;
    	}
    	// acquire a lock
		acquireLock(tid, pid, tableId, perm);
    	HeapPage heapPage = Database.getCatalog().getDbFile(tableId).readPage(pid);

    	if (heapPages.size() == this.pageSize) {
    		evictPage();
    		heapPages.add(heapPage);
            return heapPage;
    	}

    	for(int i = 0; i<heapPages.size();i++) {
    		if (heapPages.get(i).getId() == pid) {
    			return heapPages.get(i);
    		}
    	}

    	heapPages.add(heapPage);
        return heapPage;
    }
    
    public boolean checkBlock(int tid, int pid, int tableId) 
    	throws Exception{
    	ArrayList<Lock> currentTransction = new ArrayList<Lock>();
	    	for (int transctionKey : transactions.keySet()) {
	    		if (transctionKey != tid) {
	    			currentTransction = transactions.get(transctionKey);
	    			for(int i = 0;i< currentTransction.size();i++) {
	    				int eachPid = currentTransction.get(i).getPid();
	    				int eachTid = currentTransction.get(i).getTableId();
	    				if (eachPid == pid && eachTid == tableId) {
		    				// lock is held by another transaction
		    				return true;
		    			}
	    			}
	    		}
	    	}
	    // there is no lock on the requested page
    	return false;
    }
    
    public void acquireLock(int tid, int pid, int tableId, Permissions perm) {	
    	Lock e = new Lock(pid,tableId,perm);
    	for (int transctionKey : transactions.keySet()) {
    		if (transctionKey == tid) {
    			transactions.get(tid).add(e);
    			return;
    		}
    	}
    	ArrayList<Lock> locks = new ArrayList<Lock>();
    	locks.add(e);
    	transactions.put(tid, locks);
    	return;
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param tableID the ID of the table containing the page to unlock
     * @param pid the ID of the page to unlock
     */
    public  void releasePage(int tid, int tableId, int pid) {
        // your code here
    	ArrayList<Lock> currentTransction = new ArrayList<Lock>();
    	for (int transctionKey : transactions.keySet()) {
    		if (transctionKey == tid) {
    			currentTransction = transactions.get(tid);
	    		for (int i = 0;i<currentTransction.size();i++) {
	    			Lock eachLock = currentTransction.get(i);
	    			if (eachLock.getPid() == pid && eachLock.getTableId() == tableId) {
	    				transactions.get(tid).remove(eachLock);
	    			}
	    		}
    		}
    	}	
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public   boolean holdsLock(int tid, int tableId, int pid) {
        // your code here
    	ArrayList<Lock> currentTransction = new ArrayList<Lock>();
    	for (int transctionKey : transactions.keySet()) {
    		if (transctionKey == tid) {
    			currentTransction = transactions.get(tid);
    			for(int i = 0;i< currentTransction.size();i++) {
    				int eachPid = currentTransction.get(i).getPid();
    				int eachTid = currentTransction.get(i).getTableId();
    				if (eachPid == pid && eachTid == tableId) {
	    				return true;
	    			}
    			}
    		}
    	}	
        return false;
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction. If the transaction wishes to commit, write
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public   void transactionComplete(int tid, boolean commit)
        throws IOException {
        // your code here
    	ArrayList<Lock> currentTransaction = new ArrayList<Lock>();
    	// releasedTransaction:<table_id,page_id>
    	HashMap<Integer, Integer> releasedTransactions = new HashMap<Integer, Integer>(); 
    	for (int transctionKey : transactions.keySet()) {
    		if (transctionKey == tid) {
    			currentTransaction = transactions.get(tid);
	    		for(int i = 0;i< currentTransaction.size();i++) {
    				int eachPid = currentTransaction.get(i).getPid();
    				int eachTid = currentTransaction.get(i).getTableId();
    				releasedTransactions.put(eachTid, eachPid);
    			}
	    		break;
    		}
    	}	
    	transactions.remove(tid);
    	if(commit != true) {
    		for(int eachTableId:releasedTransactions.keySet()) {  
				for(int i = 0; i < heapPages.size();i++) {
					HeapPage eachPage = heapPages.get(i);
            		if (eachPage.getId() == releasedTransactions.get(eachTableId) && eachPage.getTableId() == eachTableId) {
            			heapPages.remove(eachPage);
            		}
            	}

    		}	
    		return;
    	}
    	else {
    		for(int eachTableId:releasedTransactions.keySet()) {
    			flushPage(eachTableId, releasedTransactions.get(eachTableId));	
    		}
    		return;
    	}   
    }

    /**
     * Add a tuple to the specified table behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to. May block if the lock cannot 
     * be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public  void insertTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
    	if(checkBlock(tid, t.getId(), tableId)) {
    		throw new Exception("insertTuple fail");
    	}
    	//check if the requested page is READ_ONLY
    	ArrayList<Lock> currentTransaction = new ArrayList<Lock>();
    	for (int transctionKey : transactions.keySet()) {
    		if (transctionKey == tid) {
    			currentTransaction = transactions.get(tid);
    			for(int i = 0;i< currentTransaction.size();i++) {
    				int eachPid = currentTransaction.get(i).getPid();
    				int eachTid = currentTransaction.get(i).getTableId();
    				Permissions eachPerm = currentTransaction.get(i).getPermissionType();
    				if (eachPid == t.getPid() && eachTid == tableId && eachPerm == Permissions.READ_ONLY) {
	    				throw new Exception("insertTupleFail");
	    			}
    			}
	    		
    		}
    	}	
    	
    	// acquire a lock
    	acquireLock(tid, t.getId(), tableId, Permissions.READ_WRITE);
   
    	getPage(tid, tableId, t.getPid(), Permissions.READ_WRITE).addTuple(t);
    	getPage(tid, tableId, t.getPid(), Permissions.READ_WRITE).updateIsDirty(true);
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from. May block if
     * the lock cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty.
     *
     * @param tid the transaction adding the tuple.
     * @param tableId the ID of the table that contains the tuple to be deleted
     * @param t the tuple to add
     */
    public  void deleteTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
    	if(checkBlock(tid, t.getId(), tableId)) {
    		throw new Exception("deadlock");
    	}
    	// acquire a lock
    	acquireLock(tid, t.getId(), tableId, Permissions.READ_WRITE);
    	    	
    	getPage(tid, tableId, t.getPid(), Permissions.READ_WRITE).deleteTuple(t);
    	getPage(tid, tableId, t.getPid(), Permissions.READ_WRITE).updateIsDirty(true);
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void flushPage(int tableId, int pid) throws IOException {
        // your code here
    	for(int i = 0;i<heapPages.size();i++) {
    		if(heapPages.get(i).getTableId() == tableId && heapPages.get(i).getId() == pid) {
    			//Flushes the page to disk
    			Database.getCatalog().getDbFile(tableId).writePage(heapPages.get(i));
    			//update dirty pages
    			heapPages.get(i).updateIsDirty(false);
    		}
    	}
    }

    private synchronized  void evictPage() throws Exception {
        // your code here
    	for(int i = 0;i<heapPages.size();i++) {
    		if(heapPages.get(i).isDirty() != true) {
    			heapPages.remove(i);
    			return;
    		}
    	}
    	throw new Exception("evictPage fail");
    }
}