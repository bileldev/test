/**
 * Class that encapsulates the queried information from the Teamcenter database
 * @author amit.rath
 */

package cdm.pre.imp.dbconnector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import cdm.pre.imp.reader.ReaderSingleton;


public class DBConnResponse {

	private boolean mIsExceptionThrown;																// class member that states if an exception is thrown in the code 
	
	private String mDatasetType;																	// class member that holds the dataset type to be queried
	private String eCode;

	private Vector<String> mProjIDs;																// class member that holds the project ids
	private HashMap<String, String> mMapAttrs;														// class member map that holds the attribute names and their values of the item/item rev object	
	private HashMap<String, String> mMapEffDates;													// class member map that holds the sequence and corresponding effectivity dates
	private HashMap<String, HashMap<String, DBConnBOMInfo> > mMapChildItem2BOMInfo;					// class member map --> <Child Item-ID, Map<OccPUID, DBConnBOMInfo>>
	private HashMap<String, Vector<DBConnDatasetInfo> > mMapDatasets;								// class member map that maps the dataset type to the array of instances of DBConnDatasetInfo for the item rev. object
	//private MultiValuedMap<String, HashMap<String, DBConnBOMInfo> > mMapMultiChildItem2BOMInfo;		// new multi map for capturing duplicate BOM lines
	
	/**
	 * Constructor of the class
	 * @param itemPUID - Input variable having the puid of the item
	 */
	public DBConnResponse() {
		this.mProjIDs					= new Vector<String>();
		this.mMapAttrs 					= new HashMap<String, String>();
		this.mMapEffDates				= new HashMap<String, String>();
		this.mMapChildItem2BOMInfo 		= new HashMap<String, HashMap<String, DBConnBOMInfo>>();
		this.mMapDatasets 				= new HashMap<String, Vector<DBConnDatasetInfo> >();
	//	this.mMapMultiChildItem2BOMInfo = new ArrayListValuedHashMap<>();
		this.mIsExceptionThrown 		= false;
		this.seteCode("000");
	}
	
	public boolean isExceptionThrown() {
		return mIsExceptionThrown;
	}

	public void setExceptionThrown(boolean isExceptionThrown) {
		this.mIsExceptionThrown = isExceptionThrown;
	}
	
	public String getmDatasetType() {
		return mDatasetType;
	}

	public void setmDatasetType(String mDatasetType) {
		this.mDatasetType = mDatasetType;
	}
	
	public Vector<String> getmProjIDs() {
		return mProjIDs;
	}
	
	public void setmProjIDs(String projID) {
		// checks if the project id is already part of the vector or not
		if(!this.mProjIDs.contains(projID)) {
			this.mProjIDs.add(projID);
		}
	}
	
	public HashMap<String, String> getmMapEffDates() {
		return mMapEffDates;
	}
	
	public void setmMapEffDates(String effSeq, String effDate) {
		if(this.mMapEffDates != null && !this.mMapEffDates.isEmpty()) {
			if(!this.mMapEffDates.containsKey(effDate)) {
				this.mMapEffDates.put(effSeq, effDate);
			}
		}
		else {
			this.mMapEffDates.put(effSeq, effDate);
		}
	}
	
	public HashMap<String, String> getmMapAttrs() {
		return mMapAttrs;
	}
	
	public void setmMapAttrs(String attrName, String attrVal) {
		
		if(attrName != null && attrVal != null) {
			if(this.mMapAttrs != null) {
				// checks if the map of attributes already has the attribute or not
				if(!this.mMapAttrs.containsKey(attrName)) {
					// in case not, then it puts the attribute and it's value into the map 
					this.mMapAttrs.put(attrName, attrVal);
				}
				// in case attribute is already there, then it does nothing
			}
			this.mMapAttrs.put(attrName, attrVal);
		}
	}
	
	public HashMap<String, HashMap<String, DBConnBOMInfo>> getmMapChildItem2BOMInfo() {
		return mMapChildItem2BOMInfo;
	}
	
	public HashMap<String, Vector<DBConnDatasetInfo> > getmMapDatasets() {
		return mMapDatasets;
	}

	/*public void setmMapDatasets(HashMap <String, String> mapDSAttrName2Val) {
		if(this.mDatasetType != null && !this.mDatasetType.isEmpty()) {
			if(mapDSAttrName2Val != null && !mapDSAttrName2Val.isEmpty()) {
				if(this.mMapDatasets != null && !this.mMapDatasets.isEmpty()) {
					// checks if there is already an entry in the member map corresponding to the dataset type
					if(this.mMapDatasets.containsKey(this.mDatasetType)) {
						// this means the dataset type has an entry in the map
						// checks if the vector is present or not.. should always be there by first entry
						if((this.mMapDatasets.get(this.mDatasetType) != null) && !(this.mMapDatasets.get(this.mDatasetType)).isEmpty()) {
							// instantiates a new DBConnDatasetInfo object
							DBConnDatasetInfo objDSInfo = new DBConnDatasetInfo();
							// sets the puid of the parent item revision to the dataset instance
							if(this.mMapAttrs != null && !this.mMapAttrs.isEmpty() && this.mMapAttrs.containsKey(DBConnUtilities.COL_REV_PUID)) {
								objDSInfo.setmStrModelRevPUID(this.mMapAttrs.get(DBConnUtilities.COL_REV_PUID));
							}
							// sets the attribute to the instance for the attribute names and values
							objDSInfo.setmMapDatasetAttrs(mapDSAttrName2Val);
							// set the object to the class member map's vector
							this.mMapDatasets.get(this.mDatasetType).add(objDSInfo);
						}
					}
				}
				else {
					// first entry in the map
					// instantiates a DBConnDatasetInfo instance
					DBConnDatasetInfo objDSInfo = new DBConnDatasetInfo();
					objDSInfo.setmMapDatasetAttrs(mapDSAttrName2Val);
					// instantiates a temporary vector to push to the class variable
					Vector<DBConnDatasetInfo> vectDSInstances = new Vector<DBConnDatasetInfo>();
					vectDSInstances.add(objDSInfo);
					// sets the puid of the parent item revision to the dataset instance
					if(this.mMapAttrs != null && !this.mMapAttrs.isEmpty() && this.mMapAttrs.containsKey(DBConnUtilities.COL_REV_PUID)) {
						objDSInfo.setmStrModelRevPUID(this.mMapAttrs.get(DBConnUtilities.COL_REV_PUID));
					}
					// adds to the map
					this.mMapDatasets.put(this.mDatasetType, vectDSInstances);
					// clears the temporary vector
					//vectDSInstances.clear();
				}
			}
		}
	}*/
	
	
	/*
	 * re-written the method for less lines.. Krishna
	 * 
	 */
	public void setmMapDatasets(HashMap <String, String> mapDSAttrName2Val) {
		Vector<DBConnDatasetInfo> vectDSInstances;
		if(this.mDatasetType != null && !this.mDatasetType.isEmpty()) {
			
			if(mapDSAttrName2Val != null && !mapDSAttrName2Val.isEmpty()) {
				DBConnDatasetInfo objDSInfo = new DBConnDatasetInfo();
				// sets the puid of the parent item revision to the dataset instance
				if(this.mMapAttrs != null && !this.mMapAttrs.isEmpty() && this.mMapAttrs.containsKey(DBConnUtilities.COL_REV_PUID)) {
					objDSInfo.setmStrModelRevPUID(this.mMapAttrs.get(DBConnUtilities.COL_REV_PUID));
				}
				// sets the attribute to the instance for the attribute names and values
				objDSInfo.setmMapDatasetAttrs(mapDSAttrName2Val);
				
				if(this.mMapDatasets.containsKey(this.mDatasetType)) {
					// this means the dataset type has an entry in the map
					// checks if the vector is present or not.. should always be there by first entry
					if( ((this.mMapDatasets.get(this.mDatasetType) != null) && !(this.mMapDatasets.get(this.mDatasetType)).isEmpty())) {
						
						vectDSInstances = this.mMapDatasets.get(this.mDatasetType);
						vectDSInstances.add(objDSInfo);
						this.mMapDatasets.put(this.mDatasetType, vectDSInstances);
					}
				}
				else
				{
					vectDSInstances = new Vector<DBConnDatasetInfo>();
					vectDSInstances.add(objDSInfo);
					this.mMapDatasets.put(this.mDatasetType, vectDSInstances);
				}
			}
		}
	}

	/**
	 * Populates the member map with information about the child PS Occurrences
	 * @param childItemID
	 * @param childItemPUID
	 * @param endItem
	 * @param mapAttrName2Val
	 * @param effSequence
	 * @param effDate
	 */
	public void setmMapChildItem2BOMInfo(String childItemID, String childOccPUID, String endItem, HashMap<String,String> mapAttrName2Val, String effSequence, String effDate) {
		if(!this.mMapChildItem2BOMInfo.isEmpty()) {
			if(this.mMapChildItem2BOMInfo.containsKey(childItemID)) {
				// in case the child item ID is present in the map
				if(this.mMapChildItem2BOMInfo.get(childItemID).containsKey(childOccPUID)) {
					// in case the PS Occurrence PUID is present as key in the second map
					// sets the details on the DBConnBOMInfo object
					// sets the end item details only in case they are present
					if(endItem != null && !endItem.isEmpty()) { 
						this.mMapChildItem2BOMInfo.get(childItemID).get(childOccPUID).setmMapEndItem2Eff(endItem, effSequence, effDate);
					}
					// sets the map values only in case the attribute map is not already set in the object
					/*if(this.mMapChildItem2BOMInfo.get(childItemID).get(childOccPUID).getmMapPSOccAttrs().isEmpty()) {
						this.mMapChildItem2BOMInfo.get(childItemID).get(childOccPUID).setmMapPSOccAttrs(mapAttrName2Val);
					}*/
					// 1.9.1_patch1 - removing check since the map (getmMapPSOccAttrs) will be filled with one value of occurrence note 
					// declare iterator and set the values to avoid unecessary overwrite
					for (Map.Entry<String, String> mapSet : mapAttrName2Val.entrySet()) {
						this.mMapChildItem2BOMInfo.get(childItemID).get(childOccPUID).setmMapPSOccAttrs(mapSet.getKey(), mapSet.getValue());
					}
					
				}
				else {
					// in case there is no entry for the PS Occurrence PUID in the second map
					// instantiate a DBConnBOMInfo object
					DBConnBOMInfo objBOMInfo = new DBConnBOMInfo();
					// set the members in the DBConnBOMInfo object
					if(endItem != null && !endItem.isEmpty()) { 
						objBOMInfo.setmMapEndItem2Eff(endItem, effSequence, effDate);
					}
					objBOMInfo.setmMapPSOccAttrs(mapAttrName2Val);
					// create an entry in the current class member map for the PS Occurrence PUID
					this.mMapChildItem2BOMInfo.get(childItemID).put(childOccPUID, objBOMInfo);
				}
			}
			else {
				// in case the child item ID is not present in the map, but the map is not empty
				// in case there is no entry for the PS Occurrence PUID in the second map
				// instantiate a DBConnBOMInfo object
				DBConnBOMInfo objBOMInfo = new DBConnBOMInfo();
				// set the members in the DBConnBOMInfo object
				if(endItem != null && !endItem.isEmpty()) { 
					objBOMInfo.setmMapEndItem2Eff(endItem, effSequence, effDate);
				}
				objBOMInfo.setmMapPSOccAttrs(mapAttrName2Val);
				// create a temporary map to hold the ps occurrence puid to bom info object
				HashMap<String, DBConnBOMInfo> mapPUID2BOMInfo = new HashMap<String, DBConnBOMInfo>();
				mapPUID2BOMInfo.put(childOccPUID, objBOMInfo);
				// makes the first entry into the member map#
				this.mMapChildItem2BOMInfo.put(childItemID, mapPUID2BOMInfo);
			}
		}
		else {
			// executes when the map is completely empty, i.e. this is the first entry in the map
			// in case the child item ID is not present in the map
			// in case there is no entry for the PS Occurrence PUID in the second map
			// instantiate a DBConnBOMInfo object
			DBConnBOMInfo objBOMInfo = new DBConnBOMInfo();
			// set the members in the DBConnBOMInfo object
			if(endItem != null && !endItem.isEmpty()) { 
				objBOMInfo.setmMapEndItem2Eff(endItem, effSequence, effDate);
			}
			objBOMInfo.setmMapPSOccAttrs(mapAttrName2Val);
			// create a temporary map to hold the ps occurrence puid to bom info object
			HashMap<String, DBConnBOMInfo> mapPUID2BOMInfo = new HashMap<String, DBConnBOMInfo>();
			mapPUID2BOMInfo.put(childOccPUID, objBOMInfo);
			// makes the first entry into the member map#
			this.mMapChildItem2BOMInfo.put(childItemID, mapPUID2BOMInfo);
		}
	}
	
	/**
	 * Method that sets the values corresponding to the PSOccurrence data to output data structure 
	 * @param childItemID
	 * @param childItemPUID
	 * @param endItem
	 * @param mapAttrName2Val
	 * @param effSequence
	 * @param effDate
	 */
	@SuppressWarnings("unchecked")
	/*public void setmMultiMapChildItem2BOMInfo(String childItemID, String childItemPUID, String endItem, HashMap<String,String> mapAttrName2Val, String effSequence, String effDate) {
		if(!this.mMapMultiChildItem2BOMInfo.isEmpty()) {
			if(this.mMapMultiChildItem2BOMInfo.containsKey(childItemID)) {
				// in case the child item ID is present in the map
				Collection <HashMap<String, DBConnBOMInfo> > colBOMInfo = this.mMapMultiChildItem2BOMInfo.get(childItemID); 
				if(colBOMInfo != null && !colBOMInfo.isEmpty()) {
					colBOMInfo.iterator()
					if(((HashMap<String, DBConnBOMInfo>)colBOMInfo).containsKey(childItemPUID)) {
						if(endItem != null && !endItem.isEmpty()) {
							((HashMap<String, DBConnBOMInfo>)colBOMInfo).get(childItemPUID).setmMapEndItem2Eff(endItem, effSequence, effDate);
							//this.mMapMultiChildItem2BOMInfo.get(childItemID).get(childItemPUID).setmMapEndItem2Eff(endItem, effSequence, effDate);
						}
						// sets the map values only in case the attribute map is not already set in the object
						if(((HashMap<String, DBConnBOMInfo>)colBOMInfo).get(childItemPUID).getmMapPSOccAttrs().isEmpty()) {
							((HashMap<String, DBConnBOMInfo>)colBOMInfo).get(childItemPUID).setmMapPSOccAttrs(mapAttrName2Val);
						}
					}
				}
				else {
					// in case there is no entry for the PS Occurrence PUID in the second map
					// instantiate a DBConnBOMInfo object
					DBConnBOMInfo objBOMInfo = new DBConnBOMInfo();
					// set the members in the DBConnBOMInfo object
					if(endItem != null && !endItem.isEmpty()) { 
						objBOMInfo.setmMapEndItem2Eff(endItem, effSequence, effDate);
					}
					objBOMInfo.setmMapPSOccAttrs(mapAttrName2Val);
					// create an entry in the current class member map for the PS Occurrence PUID
					((HashMap<String, DBConnBOMInfo>) this.mMapMultiChildItem2BOMInfo.get(childItemID)).put(childItemPUID, objBOMInfo);
				}
			}
			else {
				// in case the child item ID is not present in the map, but the map is not empty
				// in case there is no entry for the PS Occurrence PUID in the second map
				// instantiate a DBConnBOMInfo object
				DBConnBOMInfo objBOMInfo = new DBConnBOMInfo();
				// set the members in the DBConnBOMInfo object
				if(endItem != null && !endItem.isEmpty()) { 
					objBOMInfo.setmMapEndItem2Eff(endItem, effSequence, effDate);
				}
				objBOMInfo.setmMapPSOccAttrs(mapAttrName2Val);
				// create a temporary map to hold the ps occurrence puid to bom info object
				HashMap<String, DBConnBOMInfo> mapPUID2BOMInfo = new HashMap<String, DBConnBOMInfo>();
				mapPUID2BOMInfo.put(childItemPUID, objBOMInfo);
				// makes the first entry into the member map#
				this.mMapMultiChildItem2BOMInfo.put(childItemID, mapPUID2BOMInfo);
			}
		}
		else {
			// executes when the map is completely empty, i.e. this is the first entry in the map
			// in case the child item ID is not present in the map
			// in case there is no entry for the PS Occurrence PUID in the second map
			// instantiate a DBConnBOMInfo object
			DBConnBOMInfo objBOMInfo = new DBConnBOMInfo();
			// set the members in the DBConnBOMInfo object
			if(endItem != null && !endItem.isEmpty()) { 
				objBOMInfo.setmMapEndItem2Eff(endItem, effSequence, effDate);
			}
			objBOMInfo.setmMapPSOccAttrs(mapAttrName2Val);
			// create a temporary map to hold the ps occurrence puid to bom info object
			HashMap<String, DBConnBOMInfo> mapPUID2BOMInfo = new HashMap<String, DBConnBOMInfo>();
			mapPUID2BOMInfo.put(childItemPUID, objBOMInfo);
			// makes the first entry into the member map#
			this.mMapMultiChildItem2BOMInfo.put(childItemID, mapPUID2BOMInfo);
		}
	}*/
	
	/**
	 * Method that processes the query output and fill ups the members of the class
	 * @param vQryOutput - input vector whose each individual element is a map. Each entry in the map 
	 * represents a retrieved row from the query and whose key is the column name and value is the column value. 
	 * The complete vector represents the result set of the Teamcenter SQL query.
	 * @param endItemID - Item ID of the end item. Needed for the retrieval of relevant effective dates' details
	 */
	public void processObjQueryOutput(Vector<HashMap<String, String> > vQryOutput, String endItemID) {
		
		// the vector should be iterated. it is to be noted that the vector holds the query output corresponding to a single
		// item, item revision
		if(vQryOutput != null && !vQryOutput.isEmpty()) {
			
			Iterator<HashMap<String, String>> itrVect = vQryOutput.iterator();
			while(itrVect.hasNext()) {
				String dateSeq = null;						// temporary variable for holding the date sequence attribute value
				String effDate = null;						// temporary variable for holding the effective date attribute value
				String eItemID = null;						// temporary variable for holding the end item id
				
				// each iteration of the vector is a map - each map represents one row of data returned by the query
				for(Map.Entry<String, String> entryMap : (itrVect.next()).entrySet()) {
					// each map contains a series of key-value pairs which represent column names and corresponding attributes got from the query
					// checks for the special columns that need to fed to the pre-defined member variables
					/*if(entryMap.getKey().equals("c9SmaCreationDate"))
					{
						System.out.println("Sample Trest...:::  "+entryMap.getKey());
					}*/
					if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_DATE_SEQ)) {
						dateSeq = entryMap.getValue(); 
					}
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_EFF_DATE)) {
						effDate = entryMap.getValue();
					}
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_PROJ_ID)) {
						this.setmProjIDs(entryMap.getValue());
					}
					// retrieves the end item id
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_ENDITEM_ID)) {
						eItemID = entryMap.getValue();
					}
					else {
						this.setmMapAttrs(entryMap.getKey(), entryMap.getValue());
					}	
				}		
				// sets the date seqeunce and effective date to the member map only in case the corresponding end item id matches the input end item id
				if(dateSeq != null && effDate != null && eItemID != null && eItemID.equals(endItemID)) {
					// checks if the same sequence already exists in the member map for the same end item
					if(!this.mMapEffDates.isEmpty()) {
						if(this.mMapEffDates.containsKey(dateSeq)) {
							// check for the later date
							String laterDate = null;
							laterDate = DBConnUtilities.getLaterDate(this.mMapEffDates.get(dateSeq), effDate);
							if(!laterDate.isEmpty() && this.mMapEffDates.get(dateSeq).equals(laterDate) == false) {
								// in case the later date is the input date
								this.mMapEffDates.remove(dateSeq);
								this.mMapEffDates.put(dateSeq, laterDate);
							}
						}
						else {
							// case when the sequence id is not in the map
							this.mMapEffDates.put(dateSeq, effDate);					}
					}
					else {
						this.setmMapEffDates(dateSeq, effDate);
					}
				}
			}
		}
	}
	
	/**
	 * Method processes the query output of the PS Occurrence queries
	 * Each call should populate information about a singular PS Occurrence
	 * @param vQryOutput
	 */
	public void processOccQueryOutput(String queryName, Vector<HashMap<String, String> > vQryOutput) {
		
		// the vector should be iterated. it is to be noted that the vector holds the query output corresponding to a single
		// item, item revision
		if(vQryOutput != null && !vQryOutput.isEmpty()) {
			
			Iterator<HashMap<String, String>> itrVect = vQryOutput.iterator();
			while(itrVect.hasNext()) {
				String dateSeq = null;						// temporary variable for holding the date sequence attribute value
				String effDate = null;						// temporary variable for holding the effective date attribute value
				String eItemID = null;						// temporary variable for holding the end item id#
				String childItemID = null;					// temporary variable for holding the item id of the BOM child item
				String childPUID = null;					// temporary variable for holding the puid of the BOM child item
				String childOccPUID = null;					// temporary variable for holding the puid of the PS Occurrence
				String childRelCount = null;				// temporary variable for holding the occurrence note - C9RelCount
				String childCodeRule = null;				// temporary variable for holding the occurrence note - C9CodeRule
				String occNoteType = null;					// temporary variable for holding the occurrence note type
				String occNoteValue = null;					// temporary variable for holding the occurrence note value
				
				HashMap<String, String> mapOccAttrName2Val;	// holds the Occurrence attribute name to value map
				
				mapOccAttrName2Val = new HashMap<String, String>();
				
				// each iteration of the vector is a map - each map represents one row of data returned by the query
				for(Map.Entry<String, String> entryMap : (itrVect.next()).entrySet()) {
					// each map contains a series of key-value pairs which represent column names and corresponding attributes got from the query
					// checks for the special columns that need to fed to the pre-defined member variables
					if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_DATE_SEQ)) {
						dateSeq = entryMap.getValue(); 
					}
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_EFF_DATE)) {
						effDate = entryMap.getValue();
					}
					// retrieves the end item id
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_ENDITEM_ID)) {
						eItemID = entryMap.getValue();
					}
					// the BOMView PUID is saved to the map of the DBConnResponse Object
					else if (entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_BV_PUID)) {
						this.setmMapAttrs(entryMap.getKey(), entryMap.getValue());
					}
					// the BOMView Revision puid is saved to the map of the DBConnResponse Object
					else if (entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_BVR_PUID)) {
						this.setmMapAttrs(entryMap.getKey(), entryMap.getValue());
					}
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_CHILD_ITEMID)) {
						childItemID = entryMap.getValue();
					}
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_CHILD_PUID)) {
						childPUID = entryMap.getValue();
					}
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_CHILDOCC_PUID)) {
						childOccPUID = entryMap.getValue();
					}
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_NOTE_TYPE)) {
						occNoteType = entryMap.getValue();
					}
					else if(entryMap.getKey() != null && entryMap.getKey().equals(DBConnUtilities.COL_NOTE_VALUE)) {
						occNoteValue = entryMap.getValue();
					}
					else {
						// sets the PS Occurrence attribute name and value (e.g. trafo, relcount, etc)
						mapOccAttrName2Val.put(entryMap.getKey(), entryMap.getValue());
					}
					
					// 1.9.1_patch1 - Resolution of the occurrence note type and value
					if(occNoteType != null && !occNoteType.isEmpty() && occNoteValue != null && !occNoteValue.isEmpty()) {
						if(occNoteType.equals(DBConnUtilities.COL_OCC_CODERULE)) {
							childCodeRule = occNoteValue;
						}
						else if(occNoteType.equals(DBConnUtilities.COL_OCC_RELCOUNT)) {
							childRelCount = occNoteValue;
						}
					}
				}
				
				// set the occurrence note attributes in the map
				if(childRelCount != null && !childRelCount.isEmpty()) {
					mapOccAttrName2Val.put(DBConnUtilities.COL_OCC_RELCOUNT, childRelCount);
				}
				if(childCodeRule != null && !childCodeRule.isEmpty()) {
					mapOccAttrName2Val.put(DBConnUtilities.COL_OCC_CODERULE, childCodeRule);
				}
				
				// based on queried information, end item information is provided to the this object
				if(queryName != null && queryName.equals(DBConnUtilities.QRY_PSOCC_WITHEFF)) {
					if(childItemID != null && !childItemID.isEmpty() && childPUID != null && !childPUID.isEmpty() && eItemID != null && !eItemID.isEmpty()) {
						//this.setmMapChildItem2BOMInfo(childItemID, childPUID, eItemID, mapOccAttrName2Val, dateSeq, effDate);
						this.setmMapChildItem2BOMInfo(childItemID, childOccPUID, eItemID, mapOccAttrName2Val, dateSeq, effDate);
					}
				}
				else if (queryName != null && queryName.equals(DBConnUtilities.QRY_PSOCC_WOEFF)) {
					if(childItemID != null && !childItemID.isEmpty() && childPUID != null && !childPUID.isEmpty()) {
						this.setmMapChildItem2BOMInfo(childItemID, childOccPUID, null, mapOccAttrName2Val, dateSeq, effDate);
					}
				}
			}
		}
	}
	
	/**
	 * This method processes the database retrieved information for the Dataset and sets the required values to the DBConnResponse class members 
	 * @param queryName
	 * @param vQryOutput
	 */
	public void processDatasetQueryOutput(String queryName, Vector<HashMap<String, String> > vQryOutput) {
		// the vector should be iterated. it is to be noted that the vector holds the query output corresponding to a single item, item revision
		if(vQryOutput != null && !vQryOutput.isEmpty()) {
			
			HashMap <String, String> mapDSAttrName2Val;					// map of dataset attribute name to value map
			
			
			
			Iterator<HashMap<String, String>> itrVect = vQryOutput.iterator();
			while(itrVect.hasNext()) {
				// each iteration of the vector is a map - each map entry represents one row of data returned by the query
				mapDSAttrName2Val = new HashMap<String, String>();
				for(Map.Entry<String, String> entryMap : (itrVect.next()).entrySet()) {
					// inserts the retrieved attribute name to value from the query to the local map
					mapDSAttrName2Val.put(entryMap.getKey(), entryMap.getValue());
				}
				if(!mapDSAttrName2Val.isEmpty()) {
					this.setmMapDatasets(mapDSAttrName2Val);
				}
			}
		}
	}
	
	public void printAttrMap()
	{
		this.mMapAttrs.forEach((key, value) -> System.out.println(key + ":" + value));
	}

	public String geteCode() {
		return eCode;
	}

	public void seteCode(String eCode) {
		this.eCode = eCode;
	}
	
	public String toString() {
		
		StringBuilder output = new StringBuilder();
		
		if(!isExceptionThrown() && getmMapAttrs() != null && getmMapAttrs().size() > 1)
		{
			output.append("\nObject Found in Teamcenter DB");
			output.append("\n\n--Attributes:--");
			this.mMapAttrs.forEach((key, value) -> output.append("\n"+key + ":" + value));
		}

		return  output.toString();
	}

}