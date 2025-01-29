/**
 * Class that holds the list of object types (Smaragd and TcUA) and their corresponding counts. 
 * @author amit.rath
 */

package cdm.pre.imp.reader;

import java.util.HashMap;

public class ObjectInfo {
	
	private String smaClsName;
	
	private int smaObjCount;
	
	private HashMap<String, Integer> tcType2CountMap;

	private int changeObjCout;

	private int newObjCout;

	private int noOfObjectFoundInDB;

	/**
	 * Parameterized Constructor
	 * @param smaClsName
	 */
    public ObjectInfo(String smaClsName) {
    	this.smaClsName = smaClsName;
    	this.tcType2CountMap = new HashMap<String, Integer>();
    	this.incChangeObjCout(0);
    	this.incNewObjCout(0);
    	this.incNoOfObjectFoundInDB(0);
    }
	
    /**
     * getter for the Smaragd class name
     * @return : returns the Smaragd class name for the instance
     */
	public String getSmaClsName() {
		return smaClsName;
	}
	
	/**
	 * setter for the Smaragd class name member
	 * @param smaClsName : input Smaragd class name
	 */
	public void setSmaClsName(String smaClsName) {
		this.smaClsName = smaClsName;
	}

	/** getter for the Smaragd object count
	 * @return : returns the Smaragd object count class member
	 */
	public int getSmaObjCount() {
		return smaObjCount;
	}

	/**
	 * setter for the Smaragd object count class member. Increments the class member for every invocation 
	 */
	public void setSmaObjCount() {
		this.smaObjCount = this.smaObjCount + 1;
	}

	/**
	 * 
	 * @return
	 */
	public HashMap<String, Integer> getTcType2CountMap() {
		return tcType2CountMap;
	}

	/**
	 * 
	 * @param tcType2CountMap
	 */
	public void setTcType2CountMap(String tcClassName) {
		if(this.tcType2CountMap != null) {
			if(this.tcType2CountMap.containsKey(tcClassName)) {
				this.tcType2CountMap.put(tcClassName, (this.tcType2CountMap.get(tcClassName) + 1));
			}
			else {
				this.tcType2CountMap.put(tcClassName, 1);
			}
		}
		else {
			this.tcType2CountMap.put(tcClassName, 1);
		}
	}

	public int getNoOfObjectFoundInDB() {
		return noOfObjectFoundInDB;
	}

	public void incNoOfObjectFoundInDB(int noOfObjectFoundInDB) {
		this.noOfObjectFoundInDB = noOfObjectFoundInDB;
	}

	public int getNewObjCout() {
		return newObjCout;
	}

	public void incNewObjCout(int newObjCout) {
		this.newObjCout = newObjCout;
	}

	public int getChangeObjCout() {
		return changeObjCout;
	}

	public void incChangeObjCout(int changeObjCout) {
		this.changeObjCout = changeObjCout;
	}
	
}
