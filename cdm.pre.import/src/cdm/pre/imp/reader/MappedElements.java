package cdm.pre.imp.reader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import cdm.pre.imp.configmap.*;
import cdm.pre.imp.writer.WriterUtils;

/**
 * Applies the mapping information read from the mapping file to the list of <Element> objects
 * read from the input SMARAGD PLMXML file.
 * As output it generates a structure with the target class and attribute values filled as required
 * for creating the intermediate XML file.
 * 
 * @author amit.rath
 * 
 */

public class MappedElements {
	private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");


	/** 
	 * class member variable that holds the absolute path to the mapping file
	 */
	//private String mappingFilePath;					

	/**
	 * class member that holds the output of the mapping file parse operation
	 */
	//private HashMap<String, ArrayList<ClassMapInfo>> mappingInfoMap;	


	/**
	 * member that holds the output of the logical combination of the mapping info with the data read from the PLMXML file
	 * The key of the Map is the name of the Target Data Model class name
	 * The value of the Map is an array list of the instances of the MappedAttributes class
	 */
	//private HashMap<String, ArrayList<MappedAttributes>> mappedElementsMap;

	/**
	 * Constructor
	 */
	public MappedElements() {

	}

	/**
	 * Getter for the member holding the absolute file path to the mapping XML file.
	 * @return : Path to the mapping XML file.
	 */
	/*public String getMappingFilePath() {
		return mappingFilePath;
	}

	public void setMappingFilePath(String mappingFilePath) {
		this.mappingFilePath = mappingFilePath;
	}*/

	/*public HashMap<String, ArrayList<ClassMapInfo>> getMappingInfoMap() {
	//	return mappingInfoMap;
	}*/

	/*public void setMappedElementsMap(String trgtClassName, MappedAttributes mapAttrObj) {

		// checks if the member map already has an entry for the target class
		if(this.mappedElementsMap.containsKey(trgtClassName)) {
			// insert the object into the class member map
			this.mappedElementsMap.get(trgtClassName).add(mapAttrObj);
		}
		// executes if there is no entry for the target class in the member map
		else {
			// creates an entry for the target class in the member map and then assigns the MappedAttribute instance to the map 
			this.mappedElementsMap.put(trgtClassName, new ArrayList<MappedAttributes>());
			this.mappedElementsMap.get(trgtClassName).add(mapAttrObj);
		}

		this.mappedElementsMap	= new HashMap<String, ArrayList<MappedAttributes>>();
		this.mappedElementsMap.put(trgtClassName, new ArrayList<MappedAttributes>());
		this.mappedElementsMap.get(trgtClassName).add(mapAttrObj);
	}*/

	/*public HashMap<String, ArrayList<MappedAttributes>> getMappedElementsMap() {
		return this.mappedElementsMap;
	}

	public void setMappedElementsMapToNull() {
		this.mappedElementsMap.clear();
	}*/

	/**
	 * Method that processes each Element instance to map them to TEAMCENTER class and attributes
	 * Method in turn calls methods that handle specific elements from the input SMARAGD PLMXML file
	 * @param elemObj : Input Element class instance
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public Element processElementForMapping(Element elemObj) throws NoSuchMethodException, SecurityException, ClassNotFoundException,
	IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		try {

			String srcClassName = null;				// variable that holds the source class name for the current Element object

			if(elemObj != null) {

				String oo = WriterUtils.getAttributeValue(elemObj, "id");
				if(oo == null)
				{
					oo=elemObj.getAppLabel();
				}
				//logger.info("In processElementForMapping () Method :::::  ---------------- OBID :"+oo);
				// 07-02-2016 code added to check if mapping information already exists on the element object or not
				if(elemObj.getMappedElementsMap().isEmpty()) {
					if(elemObj.getUserValues() != null) {
						if(elemObj.getUserValues().containsKey(IConstants.Class)) {
							srcClassName = elemObj.getUserValues().get(IConstants.Class);
						}
					}
					else {
						throw new MappingException("User Values Map is null for the Element instance with ID: " + elemObj.getId() + " Exception is thrown in Class: " +
								this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false, 
								ConfigMapUtils.LOG_TYPE_WARNING);
					}
					if(srcClassName != null) {
						// checks is there is already an existing instance of the ObjectInfo class for the srcClassName
						/*if(ReaderSingleton.getReaderSingleton().getObjInfoInst(srcClassName) == null) {
							// instantiates the ObjectInfo class and sets it to the ReaderSingleton instance
							ObjectInfo objInfoInst = new ObjectInfo(srcClassName);
							if(objInfoInst != null) {
								// increments the object count
								objInfoInst.setSmaObjCount();
								ReaderSingleton.getReaderSingleton().setObjInfoList(objInfoInst);
							}
						}
						// increments the counter on the existing ObjectInfo instance
						else {
							ReaderSingleton.getReaderSingleton().getObjInfoInst(srcClassName).setSmaObjCount();
						}*/


						// initialization for the instances of ClassMapInfo
						ArrayList<ClassMapInfo> mapInfoObjs = new ArrayList<ClassMapInfo> ();;
						if(ReaderSingleton.getReaderSingleton().getMappingInfoMap() != null) {
							// checks if the source class obtained from the Element object is present in the mapping XML file or not
							if(ReaderSingleton.getReaderSingleton().getMappingInfoMap().containsKey(srcClassName)) {
								// retrieves the array of ClassMapInfo objects for the particular source class name
								mapInfoObjs = ReaderSingleton.getReaderSingleton().getMappingInfoMap().get(srcClassName);
								if(mapInfoObjs != null) {

									// this code is not needed as the OBID attribute would already be present in the user values of the element object
									//elemObj.setUserValues(ConfigMapUtils.ATTR_OBID, elemObj.getOBID());	

									// code added specifically for the BCS elements in the Truck DMU xml file
									if(elemObj.getClazz() != null) {
										// checks if the source class of the element object is BCS
										if(elemObj.getClazz().equals(IConstants.TRUCK_CLASS_BCS)) {
											// inserts the label value to the user values map of the element object
											if(elemObj.getAppLabel() != null) {
												elemObj.getUserValues().put(ConfigMapUtils.ATTR_OBID, elemObj.getAppLabel());
											}
										}
									}

									// handles the location attribute of the bulk file from the input PLMXML file
									if(srcClassName.equals("JT") || srcClassName.equals("j0CatPrt")) {
										if(elemObj.getAttributes() != null) {
											if(elemObj.getAttributes().containsKey(ConfigMapUtils.ATTR_LOCATION)) {
												elemObj.setUserValues(ConfigMapUtils.ATTR_LOCATION, elemObj.getAttributes().get(ConfigMapUtils.ATTR_LOCATION));
											}
										}
									}

									// check if there is any mapping function attached at the class to class mapping level.
									// if so, then execute the same and determine which of the map info objects should be considered for the mapping of the 
									// element object. for now it is assumed that a mapping function at the class level only serves to ensure whether in a 1:N
									// mapping if the target class is valid for the source class given a certain condition is true
									mapInfoObjs = this.executeClassMapping(mapInfoObjs, elemObj.getUserValues());

									// call to process the data in the User Values map member of the Element instance
									if(mapInfoObjs != null && mapInfoObjs.size() > 0) {
										if(!mapInfoObjs.isEmpty()) {
											this.mapUserValues(mapInfoObjs, elemObj);

											if(mapInfoObjs.get(0).getElemExprInfoObjs() != null)
											{

												processElemExpr(elemObj,mapInfoObjs.get(0).getElemExprInfoObjs());

											}
										}
									}
									// warning message thrown in case no target mapping class can be resolved for the source class
									else {
										throw new MappingException("Source Class Name: " + srcClassName + " provided in the Element object with ID: " 
												+ elemObj.getId() +
												" cannot be mapped to a target Teamcenter class as a result of the execution of class mapping function"
												+ " Exception is thrown in Class: " + 
												this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false,
												ConfigMapUtils.LOG_TYPE_WARNING);
									}
									// call to process the data in the Attributes map member of the Element instance
									// for the moment commenting this out as not needed - 28.10.2016 - Amit
									//this.mapAttributes(mapInfoObjs, elemObj.getAttributes());



								}
							}
							else {
								throw new MappingException("Source Class Name: " + srcClassName + " provided in the Element object with ID: " + elemObj.getId() +
										" is not present" + " in the Mapping File. Exception is thrown in Class: " + 
										this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false,
										ConfigMapUtils.LOG_TYPE_WARNING);
							}

						}
					}
					else {
						throw new MappingException("Source Class Name not provided in the Element object with ID: " + elemObj.getId() + " Exception is thrown in Class: " + 
								this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false, 
								ConfigMapUtils.LOG_TYPE_ERROR);
					}
					// increments the counter for the target TcUA object type in the ReaderSingleton member map
					/*if(elemObj.getMappedElementsMap() != null) {
						// iterates through the set of mapped to TcUA object types
						for(String tcObjType : elemObj.getMappedElementsMap().keySet()) {
							// here it is assumed that the ReaderSingleton class member should already be populated for the corresponding Smaragd
							// class name
							if(ReaderSingleton.getReaderSingleton().getObjInfoInst(srcClassName) != null) {
								ReaderSingleton.getReaderSingleton().getObjInfoInst(srcClassName).setTcType2CountMap(tcObjType);
							}

						}
					} */
				}
			}
			else {
				throw new MappingException("Empty instance of Element object being used for mapping to target data model. Exception is thrown in Class: " +
						this.getClass().getName() + "at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false,
						ConfigMapUtils.LOG_TYPE_WARNING);
			}
			// catch clause to handle exceptions encountered in the mapping of individual Element instances, so that they are not propagated 
			// further upward
		} catch (MappingException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// returns the element object with the class member map populated with the mapped attributes and it's corresponding values
		return elemObj;
	}

	/**
	 * Method that reads each of the source attribute names in the input map and looks for the corresponding attribute in the target data model
	 * from the mapping info and stores the value against this target attribute against the target class in the class member map  
	 * @param mapInfoObjs	: ArrayList of ClassMapInfo objects that correspond to the source class of the Element object	
	 * @param userValuesMap : Map containing the attribute names and corresponding values as read from the <UserValue> element of the PLMXML file
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public void mapUserValues(ArrayList<ClassMapInfo> mapInfoObjs, Element elemObj) throws MappingException, NoSuchMethodException, SecurityException, 
	ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		// loops through the mapping information for the particular source class and maps the values in the Element object to the corresponding target data model classes
		// and attributes
		if(mapInfoObjs != null && elemObj.getUserValues() != null) {
			// loop should traverse the list of attribute mapping since the mandatory condition can be checked
			for (ClassMapInfo mapInfoObj : mapInfoObjs) {
				// retrieves the list of the attribute map info objects
				// checks if the ClassMapInfo instance has AttributeInfo instances or not
				if(!(mapInfoObj.getAttrInfoObjs().isEmpty())) {
					for(AttributeMapInfo attrMapInfObj : mapInfoObj.getAttrInfoObjs()) {
						// assumption is that in case of the attribute mapping there can be one and only one source attribute
						// find the source attribute in the user values map which corresponds to the current attribute map info object
						if(attrMapInfObj.getSrcAttrNames().size() == 1) {
							String attrValue = null;
							if(elemObj.getUserValues().containsKey(attrMapInfObj.getSrcAttrNames().get(0))) {
								// get the value of the source attribute from the user values map
								attrValue = elemObj.getUserValues().get(attrMapInfObj.getSrcAttrNames().get(0));
								if(attrValue != null) {
									// instantiate the MappedAttributes class for the entry in the UserValues map 
									MappedAttributes mapAttrObj = new MappedAttributes(attrMapInfObj.getTrgtAttrName(), attrValue,
											attrMapInfObj.getTrgtAttrScope(), attrMapInfObj.getTrgtAttrType(), attrMapInfObj.isTrgtAttrReq());
									// set the newly created instance to the class member map
									elemObj.setMappedElementsMap(mapInfoObj.getTrgtClassName(), mapAttrObj);
								} 
								else {
									// in case the source attribute value is missing from the PLMXML file, then checks the isRequired flag
									// for the attribute from the attribute mapping info object.
									if (attrMapInfObj.isTrgtAttrReq() == true) {
										throw new MappingException("Value of mandatory attribute: " 
												+ attrMapInfObj.getSrcAttrNames().get(0)
												+ " is missing from the input PLMXML file for the Element object with ID: " + elemObj.getId() 
												+ " Exception thrown in Class: " 
												+ this.getClass().getName()
												+ " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false,
												ConfigMapUtils.LOG_TYPE_ERROR);
									}	
								}
							}	
							else {
								// in case the source attribute value is missing from the PLMXML file, then checks the isRequired flag
								// for the attribute from the attribute mapping info object.
								if (attrMapInfObj.isTrgtAttrReq() == true) {
									throw new MappingException("Value of mandatory attribute: " 
											+ attrMapInfObj.getSrcAttrNames().get(0)
											+ " is missing from the input PLMXML file for the Element object with ID: " + elemObj.getId() 
											+ " Exception thrown in Class: " 
											+ this.getClass().getName()
											+ " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false,
											ConfigMapUtils.LOG_TYPE_ERROR);
								}	
							}
						}
						else {
							throw new MappingException("Invalid number of source attribute names specified in the mapping xml file."
									+ " There should only be one entry for the source attribute name. Exception is thrown in the class:"
									+ this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), 
									false, ConfigMapUtils.LOG_TYPE_ERROR);
						}
					}
				}
				// loops through the attribute group information  

				if(!mapInfoObj.getAttrGrpInfoObjs().isEmpty()) { 
					for(AttrGrpMapInfo attrGrpInfoObj : mapInfoObj.getAttrGrpInfoObjs()) {
						// get the child attribute mapping info object and see if they match to the source attribute mentioned in the
						// attribute map.
						if(attrGrpInfoObj.getAttrMapInfoObj() != null) {
							for(String srcAttrName : attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames()) {
								// checks if the source attribute is present in the user values map or not
								if(elemObj.getUserValues().containsKey(srcAttrName) || srcAttrName.equals(ConfigMapUtils.ATTR_OBID)) {
									String attrValue = elemObj.getUserValues().get(srcAttrName);
									boolean isLabel = false;
									// special handling for the OBID attribute in case it is not present in the user values section of the input plmxml file
									if( attrValue == null && srcAttrName.equals(ConfigMapUtils.ATTR_OBID))
									{
										attrValue = elemObj.getAppLabel();
										isLabel = true;
									}
									if(attrValue != null) {
										// executes the mapping function associated with the attribute group element in the mapping file
										/*if("dateConverter".equals( attrGrpInfoObj.getMapFuncName()) && elemObj.getUserValues().get("PartNumber").equals("A3026180076"))
										{
											System.out.println("welcome");
										}*/
										//System.out.println("attrValue : "+attrValue+" attrGrpInfoObj.getMapFuncName() : "+attrGrpInfoObj.getMapFuncName()+"attrGrpInfoObj.getMapFuncsParamValues() : "+attrGrpInfoObj.getMapFuncsParamValues()+" \n elemObj.getUserValues() : "+elemObj.getUserValues()+" \n attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq() : "+attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq()+" isLabel : "+isLabel);
										attrValue = this.executeMapFunc(attrValue, attrGrpInfoObj.getMapFuncName(), attrGrpInfoObj.getMapFuncsParamValues(), 
												elemObj.getUserValues(), attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq(), isLabel);
										if(attrValue != null) 
										{
											if(attrGrpInfoObj.getMapFuncName().equals("getFormObjectName"))
											{
												attrValue = attrValue+"_"+mapInfoObj.getTrgtClassName()+"/001";
											}
											// instantiate the MappedAttributes class for the entry in the UserValues map 
											MappedAttributes mapAttrObj = new MappedAttributes(attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrName(), attrValue,
													attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrScope(), attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrType(),
													attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq());
											// set the newly created instance to the class member map
											elemObj.setMappedElementsMap(mapInfoObj.getTrgtClassName(), mapAttrObj);
										}
										else {
											// in case the source attribute value is missing from the PLMXML file, then checks the isRequired flag
											// for the attribute from the attribute mapping info object.
											// special handling in case the attribute is OBID, since it is critical to the operation of the pre-importer
											if (attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq() == true) {
												if(attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames().get(0).equals(ConfigMapUtils.ATTR_OBID)) {
													throw new MappingException("Value of mandatory attribute: " 
															+ attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames().get(0)
															+ " is missing from the input PLMXML file for the Element object with ID: " + elemObj.getId() 
															+ " Exception thrown in Class: " 
															+ this.getClass().getName()
															+ " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), true,
															ConfigMapUtils.LOG_TYPE_OBID_ERROR);
												}
												else {
													throw new MappingException("Value of mandatory attribute: " 
															+ attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames().get(0)
															+ " is missing from the input PLMXML file for the Element object with ID: " + elemObj.getId() 
															+ " Exception thrown in Class: " 
															+ this.getClass().getName()
															+ " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false,
															ConfigMapUtils.LOG_TYPE_ERROR);
												}
											}	
										}
									}
									else {
										// in case the source attribute value is missing from the PLMXML file, then checks the isRequired flag
										// for the attribute from the attribute mapping info object.
										if (attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq() == true) {
											if(attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames().get(0).equals(ConfigMapUtils.ATTR_OBID)) {
												throw new MappingException("Value of mandatory attribute: " 
														+ attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames().get(0)
														+ " is missing from the input PLMXML file for the Element object with ID: " + elemObj.getId() 
														+ " Exception thrown in Class: " 
														+ this.getClass().getName()
														+ " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), true,
														ConfigMapUtils.LOG_TYPE_OBID_ERROR);
											}
											else {
												throw new MappingException("Value of mandatory attribute: " 
														+ attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames().get(0)
														+ " is missing from the input PLMXML file for the Element object with ID: " + elemObj.getId() 
														+ " Exception thrown in Class: " 
														+ this.getClass().getName()
														+ " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false,
														ConfigMapUtils.LOG_TYPE_ERROR);
											}
										}	
									}
									// break out of the for loop for N:1 attribute mapping
									break;
								}
								else {
									// in case the source attribute value is missing from the PLMXML file, then checks the isRequired flag
									// for the attribute from the attribute mapping info object.
									if (attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq() == true) {
										if(attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames().get(0).equals(ConfigMapUtils.ATTR_OBID)) {
											throw new MappingException("Value of mandatory attribute: " 
													+ attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames().get(0)
													+ " is missing from the input PLMXML file for the Element object with ID: " + elemObj.getId() 
													+ " Exception thrown in Class: " 
													+ this.getClass().getName()
													+ " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), true,
													ConfigMapUtils.LOG_TYPE_OBID_ERROR);
										}
										else {
											throw new MappingException("Value of mandatory attribute: " 
													+ attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames().get(0)
													+ " is missing from the input PLMXML file for the Element object with ID: " + elemObj.getId() 
													+ " Exception thrown in Class: " 
													+ this.getClass().getName()
													+ " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false,
													ConfigMapUtils.LOG_TYPE_ERROR);
										}
									}	
								}
							}
						}
						else {
							throw new MappingException("No attribute mapping information is available for the <AttrGrp> information in the mapping xml file."
									+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(),
									false, ConfigMapUtils.LOG_TYPE_ERROR);
						}
					}
				}
			}
		}
	}

	/**
	 * Method that executes the mapping function based on the mapping function information provided in the <AttrGrp> element in the mapping XML file
	 * @param attrValue			: Value as read from the input SMARAGD PLMXML file
	 * @param mapFuncName		: Name of the custom mapping function
	 * @param mapFuncParams		: Map of the Parameter names and their values
	 * @param userValuesMap		: Map of the attribute names and values read from the <UserValue> element of the SMARAGD PLMXML file
	 * @param isAttrReq			: Mandatory nature of the target attribute 
	 * @return					: Attribute value obtained after the execution of the mapping function
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 * @throws ClassNotFoundException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException
	 * @throws MappingException 
	 */
	public String executeMapFunc(String attrValue, String mapFuncName, HashMap<String, String> mapFuncParams, Map<String, String> userValuesMap, 
			boolean isAttrReq, boolean isLabel) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, 
	InvocationTargetException, MappingException {

		String mappedAttrVal	= null;				// mapped value for the attribute after the execution of the mapping function

		// retrieve the class instance that holds all the mapping functions
		if(attrValue != null && mapFuncName != null && (!mapFuncParams.isEmpty())) {
			Object[] mapFuncArgs	= null;				// input array of parameters for invoking the mapping function

			// assumes all the mapping functions are defined in the ConfigMapUtils class as static methods
			Method methObj = this.getMappingFuncObj(mapFuncName);
			if(methObj != null) {
				// need to get the method parameter names so as to map them to the correct value being read from the mapping xml file
				if(methObj.getParameterCount() > 0) {
					int paramCount	= 0;				// counter for the parameters
					if(!mapFuncParams.isEmpty()) {
						// assigning the size to the parameter array for method invocation
						mapFuncArgs = new String[mapFuncParams.size()];
						// retrieves the list of parameters from the method object
						Parameter[] paramObjs = methObj.getParameters();
						if(paramObjs != null) {
							String paramName	= null;				// name of the parameter read from the method instance
							String paramValue	= null;				// value of the parameter read from the mapping file
							for(Parameter paramObj : paramObjs) {
								paramName = paramObj.getName();
								// search for the parameter name from the mapping XML parameter map
								if(mapFuncParams.containsKey(paramName)) {
									// get the parameter value read from the mapping file
									paramValue = mapFuncParams.get(paramName);
									// assigns the value to the array and increments the counter
									if(paramValue != null) {
										// block that resolves the parameter value in case it has to be retrieved from the user values map
										if(isLabel == true) {
											if(paramName.equals(ConfigMapUtils.ATTR_LABEL)) {
												paramValue = attrValue;
											}
										}
										else if( paramValue.startsWith(ConfigMapUtils.GLOBALPARAM_VALUE_DESIGNATOR))
										{
											String gConstVarName = paramValue.substring(2);
											
											
											paramValue = getGlobalConstantValue(gConstVarName);
											/*if( gConstValue != null)
											{
												paramattrValuesMap.put((String) key, gConstValue);
											}
											else
											{
												// GlobalConst is not defined in Mapping file - @gConstVarName
											}*/
										}
										else if(paramValue.startsWith(ConfigMapUtils.PARAM_VALUE_DESIGNATOR)) {
											// this means that the value of the parameter is the value of the attribute name mentioned in the parameter value
											// in the mapping file
											// gets the actual attribute value by eliminating the trailing character
											paramValue = paramValue.substring(1);
											if(paramValue != null) {
												if(userValuesMap.containsKey(paramValue)) {
													// retrieves the value of the attribute from the user values map and stores it in the same variable
													paramValue = userValuesMap.get(paramValue);
													// null check for the parameter value in case it is null in the input Smaragd PLMXML file
													if(paramValue == null && isAttrReq == true && !mapFuncName.equals("genWeldItemID")) {
														//throws an exception for a required attribute
														throw new MappingException("Mandatory mapping attribute: " + paramName +  " is missing from the mapping xml file."
																+ " Exception thrown in Class: " + this.getClass().getName() + " at Line: " + 
																Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
													}
												}

											}
										}
										// assigns the param value to the array of input arguments to the mapping function
										mapFuncArgs[paramCount] = paramValue;
										++paramCount;
									}
									else {
										// throws the exception in case the target attribute is a required attribute
										if(isAttrReq == true) {
											throw new MappingException("Mandatory mapping attribute: " + paramName +  " is missing from the mapping xml file."
													+ " Exception thrown in Class: " + this.getClass().getName() + " at Line: " + 
													Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
										}
									}
								}
								else {
									throw new MappingException("Parameter Name: " + paramName + " not found in the mapping XML file for the mapping "
											+ "function:  " + mapFuncName + ". Exception thrown in the Class: " + this.getClass().getName() + " at Line: "
											+ Thread.currentThread().getStackTrace()[2].getLineNumber(), false, ConfigMapUtils.LOG_TYPE_ERROR);
								}

							}
						}
					}
					else {
						throw new MappingException("Parameters missing in the mapping xml file for the mapping function: "
								+ mapFuncName + ". Exception thrown in the Class: " + this.getClass().getName() + " at Line: "
								+ Thread.currentThread().getStackTrace()[2].getLineNumber(), false, ConfigMapUtils.LOG_TYPE_ERROR);
					}
				}
				// invokes the mapping function
				if(mapFuncArgs != null) {
					Object retObj  = methObj.invoke(null, mapFuncArgs);
					if(retObj != null) {
						// gets the String representation. Here it is assumed that the output of all the mapping functions must be a string
						mappedAttrVal = retObj.toString();

					}
				}
			}
		}
		return mappedAttrVal;	
	}

	/*public void getValueFromMappedAttributes()
	{

	}*/

	/**
	 * Method that returns the Method instance for the mapping function name provided as input to the method. Assumption here is that all the mapping 
	 * functions are defined in the ConfigMapUtils class
	 * @param mapFuncName	: Name of the mapping function whose method instance needs to be ascertained
	 * @return				: Instance of the Method class corresponding to the mapping function name 
	 * @throws ClassNotFoundException
	 */
	public Method getMappingFuncObj(String mapFuncName) throws ClassNotFoundException, NoSuchMethodException {

		Method methObj = null;

		Class<?> c = Class.forName("cdm.pre.imp.configmap.ConfigMapUtils"); 
		Method[] methObjs = c.getDeclaredMethods();
		// loops through all the declared methods of the class and compares the name of each with the input mapping function name
		for(Method metObj : methObjs) {
			if(metObj.getName().equals(mapFuncName)) {
				// assigns the object for the mapping function to the return variable
				methObj = metObj;
				break;
			}
		}
		// returns the method object to the calling method
		return methObj;		
	}

	/**
	 * Method instantiates the ReadMappingFile class and invokes the parseMappingFile method to retrieve the mapping information 
	 * from the mapping XML file
	 * @throws MappingException
	 */
	/*public void readMappingInfo() throws MappingException {

		ReadMappingFile mapInfoObj = new ReadMappingFile(this.mappingFilePath);

		 // invokes the call to read the mapping information
		 if(mapInfoObj != null) {
			this.mappingInfoMap = mapInfoObj.parseMapppingFile();
		 }
		 if(this.mappingInfoMap == null) {
			 throw new MappingException("No mapping information available from the mapping file. Exception is thrown in Class: " + 
					 this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);

		 }
	}*/

	/**
	 * Method that executes the class mapping function and based on the return value of the mapping function method, keeps or removes the target class
	 * from the mapInfoObjs array.
	 * @param mapInfoObjs				: Array list of ClassMapInfo objects for a particular source class
	 * @param userValuesMap				: Map of attributes and values in the <UserValue> element in the source PLMXML file
	 * @return							: Array list of ClassMapInfo that have to be considered for a given source class
	 * @throws ClassNotFoundException	
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public ArrayList<ClassMapInfo> executeClassMapping(ArrayList<ClassMapInfo> mapInfoObjs, Map<String, String> userValuesMap) 
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		boolean hasClassMapping = false;						// boolean variable that states whether any of the mapping objects has class level mapping func or not
		// define the return variable for the method
		ArrayList<ClassMapInfo> dmyMapInfo = new ArrayList<ClassMapInfo>();

		if(mapInfoObjs != null) {
			// loops through the mapping info objects
			for(ClassMapInfo mapInfoObj : mapInfoObjs) {
				// checks if the class mapping has a mapping function or not
				if(mapInfoObj.getMapFuncName() != null) {
					boolean isTrgtClass = false;
					// setting the local flag to true
					hasClassMapping = true;
					// in case there is a mapping function, then it invokes the method to execute the mapping function
					Object retObj = this.executeClassMapFunc(mapInfoObj, userValuesMap);
					if( retObj != null)
					{
						if( retObj instanceof Boolean)
						{
							isTrgtClass = (boolean) retObj;
						}
					}
					// checks the value of the boolean as returned by the mapping function.
					// if the boolean value is true, then keep the mapInfoObj in the array list else remove it
					if(isTrgtClass) {
						// removes the map info object from the array list
						dmyMapInfo.add(mapInfoObj);
					}
				}
			}
		}
		// handling of the return array in the case that there is no class mapping function defined, then the mapping info objects correspond
		// to 
		if(!hasClassMapping) {
			// re-assignment to original value
			dmyMapInfo = mapInfoObjs;
		}
		return dmyMapInfo;
	}

	/**
	 * Method that executes the custom mapping function by retrieving the function parameter values from the mapping file.
	 * @param mapInfoObj				: Instance of the ClassMapInfo class
	 * @param userValuesMap				: User Values map as read from the source PLMXML file
	 * @return							: boolean value denoting whether the ClassMapInfo object should be considered for the source class
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	/*public boolean executeClassMapFunc(ClassMapInfo mapInfoObj, Map<String, String> userValuesMap) 
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		boolean isTrgtClass = false;

		// block that retrieves the mapping function object, 
		if(mapInfoObj != null) {

			// no check done here on the mapping function name as it is already done in the invoking method
			Method methObj = this.getMappingFuncObj(mapInfoObj.getMapFuncName());
			if(methObj != null) {

				Object[] mapFuncArgs	= null;				// input array of parameters for invoking the mapping function

				// need to get the method parameter names so as to map them to the correct value being read from the mapping xml file
				if(methObj.getParameterCount() > 0) {
					int paramCount	= 0;							// counter for the parameters

					// checks if the mapping parameter information is provided in the mapping file or not
					if(mapInfoObj.getMapFuncsParamValues() != null) {
						// assigning the size to the parameter array for method invocation
						mapFuncArgs = new String[mapInfoObj.getMapFuncsParamValues().size()];
						// retrieves the list of parameters from the method object
						Parameter[] paramObjs = methObj.getParameters();
						// iterates through the parameter objects to get the 
						if(paramObjs != null) {
							String paramName 	= null;				// name of the custom mapping function parameter as got from the method object
							String paramValue 	= null;				// value for the parameter to be passed to the mapping function 

							for(Parameter paramObj : paramObjs) {
								// retrieves the parameter name
								paramName = paramObj.getName();
								// checks if the parameter name is present in the mapping file or not
								if(paramName != null) { 
									if(mapInfoObj.getMapFuncsParamValues().containsKey(paramName)) {
										// retrieves the parameter value from the information read from the mapping file
										paramValue = mapInfoObj.getMapFuncsParamValues().get(paramName);
										if(paramValue != null) {
											if(paramValue.startsWith(ConfigMapUtils.PARAM_VALUE_DESIGNATOR)) {
												// this means that the value of the parameter is the value of the attribute name mentioned in the parameter value
												// in the mapping file
												// gets the actual attribute value by eliminating the trailing character
												paramValue = paramValue.substring(1);
												if(paramValue != null) {
													if(userValuesMap.containsKey(paramValue)) {
														// retrieves the value of the attribute from the user values map and stores it in the same variable
														paramValue = userValuesMap.get(paramValue);
														if(paramValue == null) {
															new MappingException("Parameter value missing for the parameter: " + paramName + " for the mapping function:" +
																	mapInfoObj.getMapFuncName() + " in the mapping file. Exception found in Class: " + 
																	this.getClass().getName() + " at Line No: "+ Thread.currentThread().getStackTrace()[2].getLineNumber(), 
																	true, ConfigMapUtils.LOG_TYPE_ERROR);
														}
													}
												}
											}
										}
										else {
											// throw exception to exit
											new MappingException("Parameter value missing for the parameter: " + paramName + " for the mapping function:" +
													mapInfoObj.getMapFuncName() + " in the mapping file. Exception found in Class: " + 
													this.getClass().getName() + " at Line No: "+ Thread.currentThread().getStackTrace()[2].getLineNumber(), 
													true, ConfigMapUtils.LOG_TYPE_ERROR);
										}
									}
									else {
										// throw exception to exit
										new MappingException("Parameter name missing for the parameter: " + paramName + " for the mapping function:" +
												mapInfoObj.getMapFuncName() + " in the mapping file. Exception found in Class: " + this.getClass().getName() + " at Line No: "
												+ Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
									}
									// assigns the param value to the array of input arguments to the mapping function
									// value can be as read directly from the mapping file or after processing 
									mapFuncArgs[paramCount] = paramValue;
									++paramCount;
								}
							}
						}
					}
					else {
						// throws an exception that exits the pre-importer because of a critical error in the mapping file
						throw new MappingException("Mapping Function Parameter information missing from the mapping file for the mapping function: " 
								+ mapInfoObj.getMapFuncName() + " .Exception thrown in Class: " + this.getClass().getName() + " at Line No: "
								+ Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
				}

				// invokes the mapping function
				if(mapFuncArgs != null) {
					Object retObj  = methObj.invoke(null, mapFuncArgs);
					if(retObj != null) {
						// gets the String representation. Here it is assumed that the output of all the mapping functions must be a string

						if( retObj instanceof String)
						{
							String weldPointType = (String) retObj;
							System.out.println("Weld Point Type : "+weldPointType);
						}
						else
						{
							isTrgtClass = (boolean) retObj;
						}
					}
				}
			}
		}
		return isTrgtClass;
	}*/

	public Object executeClassMapFunc(ClassMapInfo mapInfoObj, Map<String, String> userValuesMap) 
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Object isTrgtClass = false;

		// block that retrieves the mapping function object, 
		if(mapInfoObj != null) {

			// no check done here on the mapping function name as it is already done in the invoking method
			Method methObj = this.getMappingFuncObj(mapInfoObj.getMapFuncName());
			if(methObj != null) {

				Object[] mapFuncArgs	= null;				// input array of parameters for invoking the mapping function

				// need to get the method parameter names so as to map them to the correct value being read from the mapping xml file
				if(methObj.getParameterCount() > 0) {
					int paramCount	= 0;							// counter for the parameters

					// checks if the mapping parameter information is provided in the mapping file or not
					if(mapInfoObj.getMapFuncsParamValues() != null) {
						// assigning the size to the parameter array for method invocation
						mapFuncArgs = new String[mapInfoObj.getMapFuncsParamValues().size()];
						// retrieves the list of parameters from the method object
						Parameter[] paramObjs = methObj.getParameters();
						// iterates through the parameter objects to get the 
						if(paramObjs != null) {
							String paramName 	= null;				// name of the custom mapping function parameter as got from the method object
							String paramValue 	= null;				// value for the parameter to be passed to the mapping function 

							for(Parameter paramObj : paramObjs) {
								// retrieves the parameter name
								paramName = paramObj.getName();
								// checks if the parameter name is present in the mapping file or not
								if(paramName != null) { 
									if(mapInfoObj.getMapFuncsParamValues().containsKey(paramName)) {
										// retrieves the parameter value from the information read from the mapping file
										paramValue = mapInfoObj.getMapFuncsParamValues().get(paramName);
										if(paramValue != null) {
											if(paramValue.startsWith(ConfigMapUtils.PARAM_VALUE_DESIGNATOR)) {
												// this means that the value of the parameter is the value of the attribute name mentioned in the parameter value
												// in the mapping file
												// gets the actual attribute value by eliminating the trailing character
												paramValue = paramValue.substring(1);
												if(paramValue != null) {
													if(userValuesMap.containsKey(paramValue)) {
														// retrieves the value of the attribute from the user values map and stores it in the same variable
														paramValue = userValuesMap.get(paramValue);
														if(paramValue == null) {
															new MappingException("Parameter value missing for the parameter: " + paramName + " for the mapping function:" +
																	mapInfoObj.getMapFuncName() + " in the mapping file. Exception found in Class: " + 
																	this.getClass().getName() + " at Line No: "+ Thread.currentThread().getStackTrace()[2].getLineNumber(), 
																	true, ConfigMapUtils.LOG_TYPE_ERROR);
														}
													}
												}
											}
										}
										else {
											// throw exception to exit
											new MappingException("Parameter value missing for the parameter: " + paramName + " for the mapping function:" +
													mapInfoObj.getMapFuncName() + " in the mapping file. Exception found in Class: " + 
													this.getClass().getName() + " at Line No: "+ Thread.currentThread().getStackTrace()[2].getLineNumber(), 
													true, ConfigMapUtils.LOG_TYPE_ERROR);
										}
									}
									else {
										// throw exception to exit
										new MappingException("Parameter name missing for the parameter: " + paramName + " for the mapping function:" +
												mapInfoObj.getMapFuncName() + " in the mapping file. Exception found in Class: " + this.getClass().getName() + " at Line No: "
												+ Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
									}
									// assigns the param value to the array of input arguments to the mapping function
									// value can be as read directly from the mapping file or after processing 
									mapFuncArgs[paramCount] = paramValue;
									++paramCount;
								}
							}
						}
					}
					else {
						// throws an exception that exits the pre-importer because of a critical error in the mapping file
						throw new MappingException("Mapping Function Parameter information missing from the mapping file for the mapping function: " 
								+ mapInfoObj.getMapFuncName() + " .Exception thrown in Class: " + this.getClass().getName() + " at Line No: "
								+ Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
				}

				// invokes the mapping function
				if(mapFuncArgs != null) 
				{
					Object retObj  = methObj.invoke(null, mapFuncArgs);
					if(retObj != null) {
						// gets the String representation. Here it is assumed that the output of all the mapping functions must be a string

						if( retObj instanceof String)
						{
							// [Amit] - commented out the below line. To be cross-checked and removed later
							//String weldPointType = (String) retObj;
							//System.out.println("Weld Point Type : "+weldPointType);
						}

						isTrgtClass =  retObj;

					}
				}
			}
		}
		return isTrgtClass;
	}


	public void processElemExpr(Element elemObj, ArrayList<ElemExprMapInfo> elemExprMapInfoList)
	{
		for (ElemExprMapInfo elemExprMapInfo : elemExprMapInfoList) 
		{
			HashMap<String, String> attrValuesMap = elemExprMapInfo.getElemExprAttrValues();
			if(attrValuesMap != null && attrValuesMap.size() > 0)
			{
				Object[] keys = attrValuesMap.keySet().toArray();
				for (Object key : keys) {
					String value = attrValuesMap.get(key);
					if( value != null)
					{
						if( value.startsWith(ConfigMapUtils.GLOBALPARAM_VALUE_DESIGNATOR))
						{
							String gConstVarName = value.substring(2);
							/*ArrayList<GlobalConstMapInfo> globalConstCollection = ReaderSingleton.getReaderSingleton().getGlobalConstCollection();
							if( globalConstCollection != null && globalConstCollection.size() > 0)
							{
								for (GlobalConstMapInfo globalConstMapInfo : globalConstCollection) 
								{
									if(globalConstMapInfo.getVarName().equals(varName))
									{
										attrValuesMap.put((String) key, globalConstMapInfo.getValue());
										break;
									}
								}
							}*/
							
							String gConstValue = getGlobalConstantValue(gConstVarName);
							if( gConstValue != null)
							{
								attrValuesMap.put((String) key, gConstValue);
							}
							else
							{
								// GlobalConst is not defined in Mapping file - @gConstVarName
							}
						}
						else if( value.startsWith(ConfigMapUtils.PARAM_VALUE_DESIGNATOR))
						{
							String varName = value.substring(1);
							if(varName != null) 
							{
								if(elemObj.getUserValues().containsKey(varName))
								{
									attrValuesMap.put((String) key, elemObj.getUserValues().get(varName));;
								}
							}
						}


					}
				}
			}
		}

		elemObj.setExprBlocksList(elemExprMapInfoList);
	}
	
	public String getGlobalConstantValue(String paramName)
	{
		String globalConstValue = null;
		ArrayList<GlobalConstMapInfo> globalConstCollection = ReaderSingleton.getReaderSingleton().getGlobalConstCollection();
		if( globalConstCollection != null && globalConstCollection.size() > 0)
		{
			for (GlobalConstMapInfo globalConstMapInfo : globalConstCollection) 
			{
				if(globalConstMapInfo.getVarName().equals(paramName))
				{
					globalConstValue =  globalConstMapInfo.getValue();
					break;
				}
			}
		}
		return globalConstValue;
	}


}
