package cdm.pre.imp.reader;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import cdm.pre.imp.DateDefinitions;
import cdm.pre.imp.configmap.ConfigMapUtils;
import cdm.pre.imp.configmap.GlobalConstMapInfo;
import cdm.pre.imp.prefs.PreferenceConstants;

/**
 * SAX handler for the SAX parser.
 * 
 * @author dump1
 * 
 */
public class Handler extends DefaultHandler {
	private static final String  INSTANCE       	= "Instance";
	private final static String  PLMXML         	= "PLMXML";
	private final static String  USER_DATA      	= "UserData";
	private final static String  USER_VALUE     	= "UserValue";
	private final static String  TRANSFORM     	= "Transform";
	private final static String  APPL_REF       	= "ApplicationRef";
//	private final static String  PART   	    	= "Part";

	private String               _userValueType 	= null;
	private String vehicleType					= null;

	private java.util.Date       exportDate;

	private StringBuilder        transformBuffer;

	private Element              currElement;
	private ArrayList<String> 	secXMLList;                                  // class member that contains the list of Part instances that refer to a secondary PLMXML file

	private List<Element>  		elements		= new ArrayList<Element>();


	private final Stack<Element> breadCrumb     	= new Stack<Element>();


	private Map<String, String>  userValues     	= new HashMap<String, String>();

	private LinkedList<String> xmlHeaderPath = new LinkedList<>();

	private ArrayList<GlobalConstMapInfo> globalConstCollection = null;






	/**
	 * Constructor for the class
	 */
	public Handler() {
		super();
		// initializes the class member map
		this.secXMLList = new ArrayList<String>();
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {

		try { 
			if (PLMXML.equals(localName)) {
				// date="2013-05-14" time="22:16:46"
				String ds = attributes.getValue("date");
				String ts = attributes.getValue("time");
				if (ds != null && ts != null) {
					try {
						exportDate = DateDefinitions.SDF_IMPORT.parse(ds + " " + ts);
					} catch (ParseException ignore) {
						// then we can use the file timestamp
					}
				}
			} else if (USER_DATA.equals(localName)) {
				userValues = new HashMap<String, String>();
				_userValueType = attributes.getValue("type");
			} else if (USER_VALUE.equals(localName)) {
				userValues.put(attributes.getValue("title"), attributes.getValue("value"));
			} else if (APPL_REF.equals(localName)) {
				currElement.setAppLabel(attributes.getValue("label"));
			} else if (TRANSFORM.equals(localName)) {
				transformBuffer = new StringBuilder();
			} else {
				Map<String, String> attrMap = new HashMap<String, String>();
				final int sz = attributes.getLength();
				for (int i = 0; i < sz; i++) {
					attrMap.put(attributes.getLocalName(i), attributes.getValue(i));
				}
				// 01-12-2016 - Amit - here there should be a check to identify all elements in the Truck XML where the partRef points to another plmxml file
				// may be the information has to be stored in the form of a static map
				// the information should be used to invoke the handler once again on those xml files before creating the tree elements
				// of the main dmu xml file
				currElement = new Element(localName, attrMap);

				// checks if the vehicle type of the input xml is Truck
				if(this.vehicleType != null) {
					if(this.vehicleType.equals(PreferenceConstants.P_FZG_TYP_LKW)) {
						// invokes the Truck specific method to populate the respective class members
						this.processTruckXMLData();
					}
				}

				elements.add(currElement);
				breadCrumb.push(currElement);
			}
			xmlHeaderPath.addLast(qName);
			  if( globalConstCollection != null )
			{
				  String headerpath = getXmlHeaderPathStr();
				  if(headerpath != null)
				  {
					  updateGlobalConstValue(headerpath,attributes);
				  }
			}

		} catch (TruckException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) {
		if (PLMXML.equals(localName)) {
			// nothing to do here
		} else if (TRANSFORM.equals(localName)) {
			// find the CompoundRep to attach the matrix
			for (int i = breadCrumb.size() - 1; i >= 0; i--) {
				Element elem = breadCrumb.get(i);
				if (IConstants.CompoundRep.equals(elem.getTagName()) || INSTANCE.equals(elem.getTagName())) {
					elem.setTransform(transformBuffer.toString());
				}
			}
		} else if (USER_DATA.equals(localName)) {
			if (_userValueType == null) {
				currElement.setUserValues(userValues);
			} else {
				currElement.setTypedUserValues(_userValueType, userValues);
			}
			String seq = userValues.get(IConstants.Sequence);
			if (seq != null) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < (3 - seq.length()); i++) {
					sb.append(0);
				}
				sb.append(seq);
				userValues.put(IConstants.Sequence, sb.toString());
			}
			_userValueType = null;
			userValues = null;
		} else if (USER_VALUE.equals(localName) || APPL_REF.equals(localName)) {
			// do nothing here
		} else {
			if("uservalue".equals(localName))
			{
				System.out.println(" it is a Project");
			}
			/* else  if(localName.equals(""))
    	  {
    		  System.out.println(" it is a ");
    	  }*/

			breadCrumb.pop();
		}
		this.xmlHeaderPath.removeLast();
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) {
		if (transformBuffer != null) {
			int i = start;
			while (i < (start + length)) {
				transformBuffer.append(ch[i++]);
			}
		}
	}

	public String getXmlHeaderPathStr()
	{
		String headerPathStr = null;
		Object[] array = xmlHeaderPath.toArray();

		for (int i = 0; i < xmlHeaderPath.size(); i++) 
		{
			if( i == 0)
			{
				headerPathStr = (String) array[i];
			}
			else
			{
				headerPathStr = headerPathStr+"##"+array[i];
			}

		}
		return headerPathStr;

	}

	public void updateGlobalConstValue(String xmlHeaderPath, Attributes attributes )
	{

	//	boolean isGlobalConst = false;
		for (GlobalConstMapInfo globalConstElement : globalConstCollection) 
		{
			if( globalConstElement != null && globalConstElement.getVarXMLPath() != null && globalConstElement.getVarXMLPath().equals(xmlHeaderPath))
			{
				
				if( globalConstElement.getVarName().equals(globalConstElement.getVarXMLAttr()))
				{
					//if( attributes.getLocalName(i).equals(globalConstElement.getVarXMLAttr()) )
					{
						globalConstElement.setValue(attributes.getValue(globalConstElement.getVarXMLAttr()));
						break;
					}
				}
				else
				{
					if( attributes.getValue(globalConstElement.getVarXMLAttr()).equals(globalConstElement.getVarName()) )
					{
						globalConstElement.setValue(attributes.getValue(globalConstElement.getValue()));
						break;
					}
				}
				
				
				/*final int sz = attributes.getLength();
				for (int i = 0; i < sz; i++)
				{
					if( attributes.getLocalName(i).equals(globalConstElement.getVarXMLAttr()) && globalConstElement.getVarName().equals(globalConstElement.getVarXMLAttr()))
					{
						globalConstElement.setValue(attributes.getValue(i));
						break;
					}
					else if( attributes.getLocalName(i).equals(globalConstElement.getVarXMLAttr()) )
					{
						isVarNameEqual = false;
						continue;
					}
					
					if( !isVarNameEqual)
					{
						if( attributes.getLocalName(i).equals(globalConstElement.getValue()) )
						{
							globalConstElement.setValue(attributes.getValue(i));
							break;
						}
					}
					

					
					if( globalConstElement.getVarName().equals(globalConstElement.getVarXMLAttr()))
					{
						if( attributes.getLocalName(i).equals(globalConstElement.getVarXMLAttr()) )
						{
							globalConstElement.setValue(attributes.getValue(i));
							break;
						}
					}
					else
					{
						if( attributes.getLocalName(i).equals(globalConstElement.getVarXMLAttr()) )
						{
							isGlobalConst = true;
							continue;
						}
						if( attributes.getLocalName(i).equals(globalConstElement.getValue()) && isGlobalConst )
						{
							globalConstElement.setValue(attributes.getValue(i));
							break;
						}
						
					}
				}*/
				break;
			}
		}
	}

	/*public void mappConfigElements()
   {
	   String xmlSchemaPath = PreImpConfig.getInstance().getConfigMappingFilePath();
	   if( xmlSchemaPath != null)
	   {
		 //  System.out.println("Mapping file path 1: " + xmlSchemaPath);
		   mappedElems = new MappedElements(xmlSchemaPath);
		   mappedElems.readMappingInfo();
		   List<Element>  updatedElements       = new ArrayList<Element>();
		   for( int i = 0; i < elements.size(); i++)
		   {
			   Element element = elements.get(i);
			   try {
				   mappedElems.processElementForMapping(element);
				   updatedElements.add(element);
			   } catch (NoSuchMethodException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (SecurityException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (ClassNotFoundException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (IllegalAccessException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (IllegalArgumentException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (InvocationTargetException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   }
		   }
		   if(updatedElements.size() > 0)
		   {
			   elements = updatedElements;
			   //updatedElements.clear();
		   }
	   }
   }*/
	public List<Element> getElements() {
		// Reading Mapping Schema XML file
		//String xmlSchemaPath = "D:\\MyData\\02_Projects\\02_AS-PLM\\PreImporter\\conf\\TestMapping.xml";
		/*if(!isElementsMapped)
	  {
		  mappConfigElements();
		  isElementsMapped = true;
	  }*/
		return elements;
	}

	/* public List<Element> getElements() {
	   // Reading Mapping Schema XML file
	   //String xmlSchemaPath = "D:\\MyData\\02_Projects\\02_AS-PLM\\PreImporter\\conf\\TestMapping.xml";
	   String xmlSchemaPath = PreImpConfig.getInstance().getConfigMappingFilePath();
	   if( xmlSchemaPath != null)
	   {
		   System.out.println("Mapping file path 1: " + xmlSchemaPath);
		   mappedElems = new MappedElements(xmlSchemaPath);
		   mappedElems.readMappingInfo();
		   List<Element>  updatedElements       = new ArrayList<Element>();
		   for( int i = 0; i < elements.size(); i++)
		   {
			   Element element = elements.get(i);
			   try {
				   mappedElems.processElementForMapping(element);
				   updatedElements.add(element);
			   } catch (NoSuchMethodException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (SecurityException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (ClassNotFoundException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (IllegalAccessException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (IllegalArgumentException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   } catch (InvocationTargetException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   }
		   }
		   if(updatedElements.size() > 0)
		   {
			   elements = updatedElements;
			   //updatedElements.clear();
		   }
	   }
	   return elements;
   }*/

	public Date getExportDate() {
		return exportDate;
	}

	/**
	 * getter for the member map
	 * @return : value of the part2SecXML map value
	 */
	public ArrayList<String> getSecXMLList() {
		return this.secXMLList;
	}

	/**
	 * Setter for the member map. Checks for the existence of values for the input variables and
	 * also checks if the input file path variable exists or not
	 * @param partObj		: Part object 
	 * @param secXMLPath	: path to the secondary DMU xml file
	 */
	public void setSecXMLList(String secXMLPath) {
		if(secXMLPath != null) {
			if(new File(secXMLPath).exists()) {
				this.secXMLList.add(secXMLPath);
			}
		}
	}

	/**
	 * Getter for the vehicle type member variable
	 * @return
	 */
	public String getVehicleType() {
		return vehicleType;
	}

	/**
	 * Setter for the vehicle type member variable
	 * @param vehicleType : input type for the vehicle type
	 */
	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

	/**
	 * Method that takes care of populating the truck specific information in to the element object and the relevant members of the Handler class
	 * @throws TruckException 
	 * 
	 */
	public void processTruckXMLData() throws TruckException {

		// checks if the current element object is null or not
		if(this.currElement != null) {
			// checks if the element is an Instance element or not
			if(this.currElement.getTagName().equals(INSTANCE)) {
				// checks if the partRef attribute is present in the Instance element or not
				if(this.currElement.getAttributes().containsKey(IConstants.partRef)) {
					// retrieves the value of the partRef attribute and checks if the value contains the path to a valid xml file
					String secXMLFileName = this.currElement.getAttributes().get(IConstants.partRef);
					// processes the string value and gets the actual absolute file path to the secondary xml file
					if(secXMLFileName.contains(IConstants.PLMXML_FILE_EXTENSION)) {
						String [] tmpArray = secXMLFileName.split(IConstants.PLMXML_FILE_SEPARATOR);
						if(tmpArray != null) {
							if(tmpArray.length == 2) {
								secXMLFileName = tmpArray[0];
								// assumes that the secondary file is in the same directory as the main plmxml file and hence appends this path to the name of the secondary plmxml file
								tmpArray = null;
								if(!new File(secXMLFileName).exists()) {
									// child PLMXML file not found
									throw new TruckException("Child PLMXML with the absolute path: " + secXMLFileName + " not found. "
											+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " 
											+ Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
								}
								// populates the member map
								else {
									this.secXMLList.add(secXMLFileName);
								}
							}
							else {
								// improperly formatted value for the partRef attribute which contains the plmxml file extension
								throw new TruckException("Improperly formatted partRef value:  " + secXMLFileName + " present in the main truck PLMXML file" 
										+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " 
										+ Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
							}
						}
					}
				}
			}	
		}
	}

	public ArrayList<GlobalConstMapInfo> getGlobalConstCollection() {
		return globalConstCollection;
	}

	public void setGlobalConstCollection(ArrayList<GlobalConstMapInfo> globalConstCollection) {
		this.globalConstCollection = globalConstCollection;
	}
}
