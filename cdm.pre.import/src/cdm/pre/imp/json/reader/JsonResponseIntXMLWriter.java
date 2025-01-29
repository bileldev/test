package cdm.pre.imp.json.reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cdm.pre.imp.DateDefinitions;
import cdm.pre.imp.VersionInfo;
import cdm.pre.imp.configmap.AttrGrpMapInfo;
import cdm.pre.imp.configmap.AttributeMapInfo;
import cdm.pre.imp.configmap.ClassMapInfo;
import cdm.pre.imp.configmap.ConfigMapUtils;
import cdm.pre.imp.configmap.DateUtils;
import cdm.pre.imp.configmap.MappingException;
import cdm.pre.imp.configmap.ReadMappingFile;
import cdm.pre.imp.dbconnector.DBConnBroker;
import cdm.pre.imp.dbconnector.DBConnResponse;
import cdm.pre.imp.map.TypeMaps;
import cdm.pre.imp.reader.MappedAttributes;
import cdm.pre.imp.reader.PLMUtils;
import cdm.pre.imp.reader.ReaderSingleton;
import cdm.pre.imp.writer.BaseElement;
import cdm.pre.imp.xml.IndentingXMLStreamWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonResponseIntXMLWriter 
{
	private final static Logger logger = LogManager.getLogger(JsonResponseIntXMLWriter.class.getName());
	
	//private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");
	static int elemNmb = 1;
	//private static Sma4UJsonLogger SMA4U_LOGGER = null;
	private boolean isDbConfigLocAvailable = false;

	public static void main(String[] args) {
		JsonResponseIntXMLWriter instance  = new JsonResponseIntXMLWriter();
		//instance.write( "D:\\ASPLM","sampleInt.xml",null,null);
	}

	/*public void setSma4ULogger(Sma4UJsonLogger logger)
	{
		this.SMA4U_LOGGER = logger;
	}*/
	private HashMap<String, ArrayList<ClassMapInfo>> mappingInfoMap;
	private String intermediateXmlFilePath;
	private String endItem;
	public void write(String pkgLoc, String xmlFileName, String configMappingFilePath, String endItem, ExpandOutputJsonObject expOutJsonObj, JSONTreeElement struct)
	{
		XMLStreamWriter streamWriter = null;
		FileOutputStream fo = null;
		BufferedWriter lstWriter = null;
		
		// SMA-140: bisher war DMU_PKW immer in effectivity gesetzt. aber jetzt soll die richtige ItemId gesetzt, wann Rückfall-Ref_config nicht verwendet.
		this.endItem = endItem;
		
		try 
		{
			intermediateXmlFilePath = pkgLoc+File.separator+xmlFileName+"_part";
			System.out.println("Intermediate XML File Location : "+intermediateXmlFilePath);
			fo = new FileOutputStream(intermediateXmlFilePath);
			streamWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(fo, "UTF-8"));
			streamWriter.writeStartDocument("UTF-8", "1.0");

			streamWriter.writeStartElement("import");
			String version = VersionInfo.getVersionNumber();
			if(version == null)
			{
				version = "";

			}


			//calling Mapping XML....
			if(configMappingFilePath == null)
			{
				configMappingFilePath = "D:/ASPLM/1.9.5/deployment/conf/CDMImporter_ASPLM_MapFile.xml";
			}
			if(configMappingFilePath != null)
			{
				ReadMappingFile mapFileReader = new ReadMappingFile(configMappingFilePath);
				if(mapFileReader != null) {
					// sets the mapping information 
					this.mappingInfoMap = mapFileReader.parseMapppingFile();
				}
			}


			streamWriter.writeAttribute("version", version);
			//String[] manifestnumber = VersionInfo.getManifestVersionNumber();
			streamWriter.writeAttribute("build", "3.108.1.v20160929-1045");

			streamWriter.writeEmptyElement("effectivity");

			streamWriter.writeAttribute("endItem", endItem);
			ReaderSingleton.getReaderSingleton().setEndItem(endItem);
			String lastModifiedDateStr = null; 

			lastModifiedDateStr = DateDefinitions.SDF.format(new Date());
			System.out.println("Last Modified Date : "+lastModifiedDateStr);

			streamWriter.writeAttribute("date", lastModifiedDateStr);


			streamWriter.writeEmptyElement("root");

			JSONElelment rootObj = getObjectData(expOutJsonObj.getRootId(),expOutJsonObj.getDataObjects());//expOutJsonObj.getDataObjects()..get();
			if(rootObj != null)
			{
				String rootPartNumber = (String) rootObj.getAttributes().get("PartNumber");
				if(rootPartNumber != null && rootPartNumber.startsWith("dummy_"))
				{
					streamWriter.writeAttribute("objectid", "");
				}
				else
				{
					streamWriter.writeAttribute("objectid", rootPartNumber);
				}

				//SMA4U_LOGGER.writeToLogFile("Root Part Number :: "+rootPartNumber,"INFO"); 
				logger.info("Root Part Number :: "+rootPartNumber); 
			}



			writeFileInfo(streamWriter, new File(pkgLoc), "PLMXML1");


			//SMA4U_LOGGER.writeToLogFile("write the project information ..........","INFO"); 
			logger.info("write the project information .........."); 
			streamWriter.writeStartElement("projects");
			Object[] projs = getProjectList(expOutJsonObj.getDataObjects(),expOutJsonObj.getRootId());
			String mainProject = (String) projs[0] ;
			if (mainProject != null) {
				streamWriter.writeAttribute("main", PLMUtils.convertProjectName(mainProject));
				//SMA4U_LOGGER.writeToLogFile("Main Project :: "+PLMUtils.convertProjectName(mainProject),"INFO"); 
				logger.info("Main Project :: "+PLMUtils.convertProjectName(mainProject)); 
			}
			for (String proj : (ArrayList<String>)projs[1]) {
				streamWriter.writeEmptyElement("P");
				streamWriter.writeAttribute("name", PLMUtils.convertProjectName(proj));
				streamWriter.writeAttribute("secProject", Boolean.toString(PLMUtils.isSecuredProject(proj)));
			}
			streamWriter.writeEndElement();


			//SMA4U_LOGGER.writeToLogFile("write the project information ..........Completed....","INFO"); 
			logger.info("write the project information ..........Completed...."); 
			// writing a Elements Section
			streamWriter.writeStartElement("elements");// start of the Elements

			if(struct != null)
			{
				//SMA4U_LOGGER.writeToLogFile("Writing Elements Section....","INFO"); 
				logger.info("Writing Elements Section...."); 
				JSONElelment rJsonTreeElm = struct.getDataElement();
				if( rJsonTreeElm != null && rJsonTreeElm.getAttributes().get("OBID") != null)
				{
					writeElement(rJsonTreeElm,streamWriter);

				}
				if(struct.getChildren() != null && struct.getChildren().size() > 0)
				{
					for(int i=0; i < struct.getChildren().size();i++)
					{
						JSONTreeElement childTreeElm = struct.getChildren().get(i);
						if(childTreeElm != null && childTreeElm.getDataElement().getAttributes().get("OBID") != null)
						{
							processChild(childTreeElm,streamWriter);
						}
					}
				}
			}

			streamWriter.writeEndElement(); 

			//SMA4U_LOGGER.writeToLogFile("Writing Elements Section Completed....","INFO"); 
			logger.info("Writing Elements Section Completed...."); 
			// Writing Connections...

			//SMA4U_LOGGER.writeToLogFile("Writing Connections Section ....","INFO");
			logger.info("Writing Connections Section ....");
			streamWriter.writeStartElement("connections"); // start of the Connections

			processJsonElementForConnections(struct,streamWriter); // Processing connections elements..

			streamWriter.writeEndElement(); // closing connections
			//SMA4U_LOGGER.writeToLogFile("Writing Connections Section Completed....","INFO"); 
			logger.info("Writing Connections Section Completed...."); 

			streamWriter.writeEndElement(); // closing xml

			//SMA4U_LOGGER.writeToLogFile("Writing Intermediate XML Completed....","INFO"); 
			logger.info("Writing Intermediate XML Completed...."); 

		}
		catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		} finally {
			if (lstWriter != null) {
				try {
					lstWriter.flush();
					lstWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if (streamWriter != null) {
				try {
					streamWriter.flush();
					streamWriter.close();
				} catch (XMLStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if (fo != null) {

				try {
					fo.flush();
					fo.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	public static boolean isFileRef(final String eClazz) {
		return getFileType(eClazz) != null;
	}
	public static String getFileType(final String eClazz) {
		return TypeMaps.FILE_TYPES.get(eClazz);
	}

	public void renameIntermediateXml(String pkgLoc, String xmlFileName)
	{
		// renaming file name to .xml_part to .xml
		Path sourcePath      =  Paths.get(intermediateXmlFilePath);
		Path destinationPath = Paths.get(pkgLoc+File.separator+xmlFileName);

		try {
			Files.move(sourcePath, destinationPath,
					StandardCopyOption.REPLACE_EXISTING);
			//SMA4U_LOGGER.writeToLogFile(" Intermediate XML File Location : "+pkgLoc+File.separator+xmlFileName,"INFO"); 
			logger.info(" Intermediate XML File Location : "+pkgLoc+File.separator+xmlFileName); 
			/* Files.copy(sourcePath, destinationPath,
					            StandardCopyOption.REPLACE_EXISTING);*/
		} catch (IOException e) {
			//moving file failed.
			e.printStackTrace();
		}
	}

	public void processJsonElementForConnections(JSONTreeElement rootTreeElm, XMLStreamWriter streamWriter) throws XMLStreamException
	{
		if(rootTreeElm != null)
		{
			JSONElelment rJsonTreeElm = rootTreeElm.getDataElement();
			if( rJsonTreeElm != null && rootTreeElm.getChildren().size() > 0 && rJsonTreeElm.getAttributes().get("OBID") != null)
			{
				ArrayList<ClassMapInfo> clsInfo = mappingInfoMap.get(rJsonTreeElm.getObjectType());
				//System.out.println("Size of the class Map Infos :: "+clsInfo.size());
				if(clsInfo == null || clsInfo.size() == 0)
				{
					//SMA4U_LOGGER.writeToLogFile("NO Mapping Element Defined for Name  :  "+rJsonTreeElm.getName()+"  Type : "+rJsonTreeElm.getObjectType()+"  OBID : "+ rJsonTreeElm.getAttributes().get("OBID"),"INFO"); 
					logger.info("NO Mapping Element Defined for Name  :  "+rJsonTreeElm.getName()+"  Type : "+rJsonTreeElm.getObjectType()+"  OBID : "+ rJsonTreeElm.getAttributes().get("OBID")); 

				}
				else
				{
					if(rJsonTreeElm.getAttributes().get("j0Invalid") != null)
					{
						Object value = rJsonTreeElm.getAttributes().get("j0Invalid");
						boolean j0Invalid;
						if(value instanceof Boolean)
						{
							//j0Invalid = Boolean.valueOf().toString(value);
							j0Invalid = ((Boolean) value).booleanValue();
							if(j0Invalid )
							{
								return;
							}
							
						}
						/*String j0Invalid = (String) rJsonTreeElm.getAttributes().get("j0Invalid");
						if(j0Invalid != null && j0Invalid.equals("true"))
						{
							return;
						}*/
					}
					
					streamWriter.writeStartElement("r");
					writeConnection(rJsonTreeElm,streamWriter);  // r


					if(rootTreeElm.getChildren() != null && rootTreeElm.getChildren().size() > 0)
					{
						for(int i=0; i < rootTreeElm.getChildren().size();i++)
						{
							JSONTreeElement childTreeElm = rootTreeElm.getChildren().get(i);
							if(childTreeElm != null && childTreeElm.getDataElement().getAttributes().get("OBID") != null)
								//if(childTreeElm != null )
							{
								if(childTreeElm.getDataElement().getAttributes().get("j0Invalid") != null)
								{
									
									Object value = childTreeElm.getDataElement().getAttributes().get("j0Invalid");
									boolean j0Invalid;
									if(value instanceof Boolean)
									{
										//j0Invalid = Boolean.valueOf().toString(value);
										j0Invalid = ((Boolean) value).booleanValue();
										if(j0Invalid )
										{
											continue;
										}
										
									}
									/*String j0Invalid = (String) childTreeElm.getDataElement().getAttributes().get("j0Invalid");
									if(j0Invalid != null && j0Invalid.equals("true"))
									{
										continue;
									}*/
								}
								writeConnectionCTag(childTreeElm.getDataElement(),streamWriter);  // c
							}
						}
					}
					//SMA4U_LOGGER.writeToLogFile("Writing Element with Partnumber :  .. Completed.. ","INFO");
					logger.info("Writing Element with Partnumber :  .. Completed.. ");
					streamWriter.writeEndElement(); // close r Tag
				}
			}
			if(rootTreeElm.getChildren() != null && rootTreeElm.getChildren().size() > 0)
			{
				for(int i=0; i < rootTreeElm.getChildren().size();i++)
				{
					JSONTreeElement childTreeElm = rootTreeElm.getChildren().get(i);
					if(childTreeElm != null)
					{

						processJsonElementForConnections(childTreeElm, streamWriter);
					}
				}
			}
		}
	}

	public void processChild(JSONTreeElement childTreeElm, XMLStreamWriter streamWriter) throws XMLStreamException
	{
		if(childTreeElm != null && childTreeElm.getDataElement().getAttributes().get("OBID") != null)
		{
			writeElement(childTreeElm.getDataElement(), streamWriter);
			if(childTreeElm.getChildren() != null && childTreeElm.getChildren().size() > 0)
			{
				for( int i =0; i < childTreeElm.getChildren().size(); i++)
				{
					processChild(childTreeElm.getChildren().get(i), streamWriter);
				}
			}
		}
	}

	public void processChildConnection(JSONTreeElement childTreeElm, XMLStreamWriter streamWriter) throws XMLStreamException
	{
		if(childTreeElm != null)
		{
			writeConnection(childTreeElm.getDataElement(), streamWriter);
			if(childTreeElm.getChildren() != null && childTreeElm.getChildren().size() > 0)
			{
				for( int i =0; i < childTreeElm.getChildren().size(); i++)
				{
					processChildConnection(childTreeElm.getChildren().get(i), streamWriter);
				}
			}
		}
	}

	public void writeElement(JSONElelment elem,  XMLStreamWriter streamWriter) throws XMLStreamException
	{
		ClassMapInfo elmClsMapInfoObj = null;
		if(streamWriter != null && elem != null)
		{

			ArrayList<ClassMapInfo> clsInfo = mappingInfoMap.get(elem.getObjectType());
			//System.out.println("Size of the class Map Infos :: "+clsInfo.size());
			if(clsInfo != null && clsInfo.size() > 0)
			{
				elmClsMapInfoObj = clsInfo.get(0);
				updateMappedElements(elem);
				//SMA4U_LOGGER.writeToLogFile("Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType() +"  OBID : "+ elem.getAttributes().get("OBID"),"INFO"); 
				logger.info("Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType() +"  OBID : "+ elem.getAttributes().get("OBID")); 
				
			}
			else 
			{
				//SMA4U_LOGGER.writeToLogFile("NO Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType()+"  OBID : "+ elem.getAttributes().get("OBID"),"INFO"); 
				logger.info("NO Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType()+"  OBID : "+ elem.getAttributes().get("OBID")); 
				return;
			}
			if(elem.getAttributes().get("j0Invalid") != null)
			{
				//if()
				//String j0Invalid = null;
				Object value = elem.getAttributes().get("j0Invalid");
				boolean j0Invalid;
				if(value instanceof Boolean)
				{
					//j0Invalid = Boolean.valueOf().toString(value);
					j0Invalid = ((Boolean) value).booleanValue();
					if(j0Invalid )
					{
						return;
					}
					
				}
				
			}
			streamWriter.writeStartElement("e");
			String  itemId = (String) elem.getAttributes().get("PartNumber");
			String  name = (String) elem.getAttributes().get("j0Nomenclature");
			String  project = (String) elem.getAttributes().get("ProjectName");
			if(itemId == null)
			{
				itemId = "";
			}
			//SMA4U_LOGGER.writeToLogFile("Writing Element With Partnumber : "+itemId,"INFO"); 
			logger.info("Writing Element With Partnumber : "+itemId); 
			String obid = (String) elem.getAttributes().get("OBID");
			if(obid == null)
			{
				obid = generateOBID(itemId,name);
			}
			if(obid == null)
			{
				obid = "OBID";
				elem.getAttributes().put("OBID", obid);
			}
			if(project == null)
			{
				project = "";
			}

			streamWriter.writeAttribute(BaseElement.ID, obid);
			streamWriter.writeAttribute(BaseElement.ELEM_NMB, Integer.toString(elemNmb));
			streamWriter.writeAttribute("smaProject", PLMUtils.convertProjectName(project));

			streamWriter.writeAttribute("cdmProject",PLMUtils.convertProjectName(project));
			boolean isDataset = isFileRef(elem.getObjectType());
			boolean isOccEff;
			if(isDataset)
			{
				isOccEff = false;
				streamWriter.writeAttribute("tcType", "Dataset");
			}
			else
			{
				streamWriter.writeAttribute("tcType", "ItemRevision");
				isOccEff = true;
			}

			String targetClsName = null;
			if(elmClsMapInfoObj != null)
			{
				targetClsName = elmClsMapInfoObj.getTrgtClassName();
			}
			if(targetClsName == null)
			{
				targetClsName = "C9Part";
			}
			streamWriter.writeAttribute("cdmType", targetClsName);

			String srcClsName = null;
			if(elmClsMapInfoObj != null)
			{
				srcClsName = elmClsMapInfoObj.getSrcClassName();
			}
			if(srcClsName == null)
			{
				srcClsName = "j0PrtVer";
			}
			streamWriter.writeAttribute("smaType", srcClsName);
			
			streamWriter.writeAttribute(BaseElement.APPLY_EFFECTIVITY, Boolean.toString(isOccEff));
			streamWriter.writeAttribute("hc", Boolean.toString(true));
			
			String itemRevId = getAttributeValue(elem,"item_revision_id");//.getMappedElementsMap().get(elmClsMapInfoObj.getTrgtClassName()).get.get("item_revision_id");
			/*if(itemRevId != null)
			{
				
			}*/
			
			 String action = "new";
			if(!isDbConfigLocAvailable())
			{
				action = "not-checked";
			}
			else
			{
				action = compareWithDB(elmClsMapInfoObj.getSrcClassName(),itemId,itemRevId,isDataset);
			}
			streamWriter.writeAttribute("action", action);



			if(elmClsMapInfoObj != null)
			{
				// Adding Attributes
				ArrayList<AttributeMapInfo> attrInfosList = elmClsMapInfoObj.getAttrInfoObjs();
				ArrayList<AttrGrpMapInfo> attrGrpObjList = elmClsMapInfoObj.getAttrGrpInfoObjs();
				//attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames();
				if(attrInfosList != null && attrInfosList.size() > 0)
				{
					for (AttributeMapInfo attributeMapInfo : attrInfosList) 
					{
						Object value = elem.getAttributes().get(attributeMapInfo.getSrcAttrNames().get(0));
						if(value != null)
						{
							if(!attributeMapInfo.getTrgtAttrName().equals("id"))
							{
								if(!attributeMapInfo.getTrgtAttrType().equals("d"))
								{
								writePropertyValue(streamWriter, attributeMapInfo.getTrgtAttrName(), value, attributeMapInfo.getTrgtAttrType(), attributeMapInfo.getTrgtAttrScope());
								}
							}

						}

					}
				}

				// adding AttrGroups
				
				{

					for(AttrGrpMapInfo attrGrpInfoObj : attrGrpObjList) {
						// get the child attribute mapping info object and see if they match to the source attribute mentioned in the
						if(attrGrpInfoObj.getAttrMapInfoObj() != null) {
							for(String srcAttrName : attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames()) {
								// checks if the source attribute is present in the user values map or not
								if(elem.getAttributes().get(srcAttrName) != null  || srcAttrName.equals(ConfigMapUtils.ATTR_OBID)) {
									String attrValue = (String) elem.getAttributes().get(srcAttrName);
									boolean isLabel = false;
									// special handling for the OBID attribute in case it is not present in the user values section of the input plmxml file

									if(attrValue != null) {
										try {
											if("dateConverter".equals(attrGrpInfoObj.getMapFuncName()))
											{
												attrValue = DateUtils.dateConverter(obid,srcAttrName, attrValue, "1970/01/01-00:00:00:000");
											}
											else 
											{
												attrValue = executeMapFunc(attrValue, attrGrpInfoObj.getMapFuncName(), attrGrpInfoObj.getMapFuncsParamValues(), 
														elem.getAttributes(), attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq(), isLabel);
											}
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
										} catch (MappingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if(attrValue != null) 
										{

											// instantiate the MappedAttributes class for the entry in the UserValues map 
											MappedAttributes mapAttrObj = new MappedAttributes(attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrName(), attrValue,
													attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrScope(), attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrType(),
													attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq());

											//elem.getAttributes().put(key, attrValue);

											if(!attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrName().equals("id"))
											{
												writePropertyValue(streamWriter, attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrName(), attrValue, attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrType(), attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrScope());
											}
											// set the newly created instance to the class member map

										}

									}
									// break out of the for loop for N:1 attribute mapping
									break;
								}

							}
						}

					}

				}

				if(elem.getObjectType().equals("JT"))
				{
					String filepath =  (String) elem.getAttributes().get("filepath");
					writePropertyValue(streamWriter, "file", filepath, "s", "ud");
				}

			}



			streamWriter.writeEndElement();
			//SMA4U_LOGGER.writeToLogFile("Writing Element with Partnumber : "+itemId+"  .. Completed.. ","INFO");
			logger.info("Writing Element with Partnumber : "+itemId+"  .. Completed.. ");
		}
		elemNmb++;
	}
	
	public static String getAttributeValue(JSONElelment element, String attrName)
	{
		String attrValue = null;
		// null check on the element object is necessary as not always the calling method does it
		
		//logger.info("Attr Name : "+attrName+" Elment OBID : "+element.getId());
		if(element != null) {
			HashMap<String, ArrayList<MappedAttributes>> mappedElementMap = element.getMappedElementsMap();
			if( mappedElementMap != null)
			{
				Object[] keys = mappedElementMap.keySet().toArray();
				if(keys != null && keys.length > 0)
				{
					for(int i = 0; i < keys.length;i++)
					{
						ArrayList<MappedAttributes> mappedAttrs = mappedElementMap.get(keys[i]);
						if( mappedAttrs != null && mappedAttrs.size() > 0)
						{
							MappedAttributes attribute = null;
							for ( int k = 0; k < mappedAttrs.size(); k++)
							{
								attribute = mappedAttrs.get(k);
								if( attribute != null && attribute.getAttrName() != null && attribute.getAttrName().equals(attrName))
								{
									attrValue =  attribute.getAttrValue();
									break;
								}
							}
						}
					}
				}
			}
			if( attrValue == null && attrName.equals("id"))
			{
				
			}
		}
		return attrValue;
	}
	
	public void updateMappedElements(JSONElelment elem)
	{

		ClassMapInfo elmClsMapInfoObj = null;
		if( elem != null)
		{

			ArrayList<ClassMapInfo> clsInfo = mappingInfoMap.get(elem.getObjectType());
			//System.out.println("Size of the class Map Infos :: "+clsInfo.size());
			if(clsInfo != null && clsInfo.size() > 0)
			{
				elmClsMapInfoObj = clsInfo.get(0);
				//SMA4U_LOGGER.writeToLogFile("Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType() +"  OBID : "+ elem.getAttributes().get("OBID"),"INFO"); 
				logger.info("Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType() +"  OBID : "+ elem.getAttributes().get("OBID")); 
			}
			else 
			{
				//SMA4U_LOGGER.writeToLogFile("NO Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType()+"  OBID : "+ elem.getAttributes().get("OBID"),"INFO");
				logger.info("NO Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType()+"  OBID : "+ elem.getAttributes().get("OBID"));
				return;
			}
			String  itemId = (String) elem.getAttributes().get("PartNumber");
			String  name = (String) elem.getAttributes().get("j0Nomenclature");
			String  project = (String) elem.getAttributes().get("ProjectName");
			if(itemId == null)
			{
				itemId = "";
			}
			//SMA4U_LOGGER.writeToLogFile("Writing Element With Partnumber : "+itemId,"INFO"); 
			logger.info("Writing Element With Partnumber : "+itemId); 
			String obid = (String) elem.getAttributes().get("OBID");
			if(obid == null)
			{
				obid = generateOBID(itemId,name);
			}
			if(obid == null)
			{
				obid = "OBID";
				elem.getAttributes().put("OBID", obid);
			}

			
			boolean isDataset = isFileRef(elem.getObjectType());
			boolean isOccEff;
			if(isDataset)
			{
				isOccEff = false;
				
			}
			else
			{
				
				isOccEff = true;
			}

			String targetClsName = null;
			if(elmClsMapInfoObj != null)
			{
				targetClsName = elmClsMapInfoObj.getTrgtClassName();
			}
			if(targetClsName == null)
			{
				targetClsName = "C9Part";
			}
			

			String srcClsName = null;
			if(elmClsMapInfoObj != null)
			{
				srcClsName = elmClsMapInfoObj.getSrcClassName();
			}
			if(srcClsName == null)
			{
				srcClsName = "j0PrtVer";
			}
			
			
			
			String action = "new";
			if(!isDbConfigLocAvailable())
			{
				action = "not-checked";
			}
			



			if(elmClsMapInfoObj != null)
			{
				// Adding Attributes
				ArrayList<AttributeMapInfo> attrInfosList = elmClsMapInfoObj.getAttrInfoObjs();
				if(attrInfosList != null && attrInfosList.size() > 0)
				{
					for (AttributeMapInfo attributeMapInfo : attrInfosList) 
					{
						Object value = elem.getAttributes().get(attributeMapInfo.getSrcAttrNames().get(0));
						if(value != null)
						{
							if(!attributeMapInfo.getTrgtAttrName().equals("id"))
							{
								//writePropertyValue(streamWriter, attributeMapInfo.getTrgtAttrName(), value, attributeMapInfo.getTrgtAttrType(), attributeMapInfo.getTrgtAttrScope());
								//System.out.println("TrgtAttrName : "+attributeMapInfo.getTrgtAttrName());
								MappedAttributes mapAttrObj = new MappedAttributes(attributeMapInfo.getTrgtAttrName(), value.toString(),
										attributeMapInfo.getTrgtAttrScope(), attributeMapInfo.getTrgtAttrType(), attributeMapInfo.isTrgtAttrReq());
								// set the newly created instance to the class member map
								elem.setMappedElementsMap(elmClsMapInfoObj.getSrcClassName(), mapAttrObj);
							}

						}

					}
				}

				// adding AttrGroups
				ArrayList<AttrGrpMapInfo> attrGrpObjList = elmClsMapInfoObj.getAttrGrpInfoObjs();
				{

					for(AttrGrpMapInfo attrGrpInfoObj : attrGrpObjList) {
						// get the child attribute mapping info object and see if they match to the source attribute mentioned in the
						if(attrGrpInfoObj.getAttrMapInfoObj() != null) {
							for(String srcAttrName : attrGrpInfoObj.getAttrMapInfoObj().getSrcAttrNames()) {
								// checks if the source attribute is present in the user values map or not
								if(elem.getAttributes().get(srcAttrName) != null  || srcAttrName.equals(ConfigMapUtils.ATTR_OBID)) {
									String attrValue = (String) elem.getAttributes().get(srcAttrName);
									boolean isLabel = false;
									// special handling for the OBID attribute in case it is not present in the user values section of the input plmxml file

									if(attrValue != null) {
										try {
											if("dateConverter".equals(attrGrpInfoObj.getMapFuncName()))
											{
												attrValue = DateUtils.dateConverter(obid, srcAttrName,attrValue, "1970/01/01-00:00:00:000");
											}
											else 
											{
												attrValue = executeMapFunc(attrValue, attrGrpInfoObj.getMapFuncName(), attrGrpInfoObj.getMapFuncsParamValues(), 
														elem.getAttributes(), attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq(), isLabel);
											}
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
										} catch (MappingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if(attrValue != null) 
										{

											// instantiate the MappedAttributes class for the entry in the UserValues map 
											MappedAttributes mapAttrObj = new MappedAttributes(attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrName(), attrValue,
													attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrScope(), attrGrpInfoObj.getAttrMapInfoObj().getTrgtAttrType(),
													attrGrpInfoObj.getAttrMapInfoObj().isTrgtAttrReq());

											elem.getAttributes().put(elmClsMapInfoObj.getSrcClassName(), attrValue);

										}
									}
									// break out of the for loop for N:1 attribute mapping
									break;
								}
							}
						}
					}
				}
			}
		}
	
	
	}

	
	public String compareWithDB(String clsName, String itemId, String itemRevID, boolean isDataset)
	{
		boolean isBVPresent = true;
		boolean isDatasetPresent = false;
		String action = "new";
		
		DBConnBroker connBrokerObj = ReaderSingleton.getReaderSingleton().getDbConnBroker();
		connBrokerObj.setmItemID(itemId);
		connBrokerObj.setmItemRevID(itemRevID);
		connBrokerObj.setmObjType(clsName+"Revision");
		connBrokerObj.setmEndItemID(ReaderSingleton.getReaderSingleton().getEndItem());

		



		DBConnResponse resp = connBrokerObj.executeDBAction(isTypeForOccEff(clsName),isBVPresent,isDatasetPresent,null);
		
		ReaderSingleton.getReaderSingleton().setMapQry2ExecuteFreq("executeDBAction");
		
		//connBrokerObj.manageDBConnect(true);

		if( resp != null && resp.isExceptionThrown())
		{
			logger.info("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemId+" Rev ID : "+itemRevID+" of Type : "+clsName+"Revision");
			action="unset";
			return action;
		}
		if( resp != null && resp.geteCode()!= null && !resp.geteCode().equals("") && resp.geteCode().equals("001"))
		{
			logger.info("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemId+" Rev ID : "+itemRevID+" of Type : "+clsName+"Revision");
			action="not-checked";
			return action;
		}
		else if(resp != null && !resp.isExceptionThrown() && resp.getmMapAttrs() != null && resp.getmMapAttrs().size() <= 1)
		{
			//logger.info("No Data Found for Item ID:  No Mapping Attributes...... : "+itemID+" Rev ID : "+itemRevID+" of Type : "+entry.getKey()+"Revision");
			//SMA4U_LOGGER.writeToLogFile("Object Not Found in Teamcenter DB. Action will be new","INFO");
			logger.info("Object Not Found in Teamcenter DB. Action will be new");
			return action;
		}
		else  if(resp != null && !resp.isExceptionThrown() && resp.getmMapAttrs() != null && resp.getmMapAttrs().size() > 1)
		{
			//SMA4U_LOGGER.writeToLogFile("Object Found in Teamcenter DB ","INFO");
			logger.info("Object Found in Teamcenter DB ");
			action="change";
			return action;
		}
		return action;
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
	public String executeMapFunc(String attrValue, String mapFuncName, HashMap<String, String> mapFuncParams, Map<String, Object> userValuesMap, 
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
										else if(paramValue.startsWith(ConfigMapUtils.PARAM_VALUE_DESIGNATOR)) {
											// this means that the value of the parameter is the value of the attribute name mentioned in the parameter value
											// in the mapping file
											// gets the actual attribute value by eliminating the trailing character
											paramValue = paramValue.substring(1);
											if(paramValue != null) {
												if(userValuesMap.containsKey(paramValue)) {
													// retrieves the value of the attribute from the user values map and stores it in the same variable
													Object paramVal = userValuesMap.get(paramValue);
													if(paramVal instanceof Long)
													{
														paramValue = ((Long)paramVal).toString();
													}
													else
													{
														paramValue = (String) userValuesMap.get(paramValue);
													}

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


	public void writeConnection(JSONElelment elem,  XMLStreamWriter streamWriter) throws XMLStreamException
	{

		ArrayList<ClassMapInfo> clsInfo = mappingInfoMap.get(elem.getObjectType());
		//System.out.println("Size of the class Map Infos :: "+clsInfo.size());
		if(clsInfo == null || clsInfo.size() == 0)
		{
			//SMA4U_LOGGER.writeToLogFile("NO Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType()+"  OBID : "+ elem.getAttributes().get("OBID"),"INFO"); 
			
			logger.info("NO Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType()+"  OBID : "+ elem.getAttributes().get("OBID")); 
			return;
		}

		String  itemId = (String) elem.getAttributes().get("PartNumber");
		String  name = (String) elem.getAttributes().get("j0Nomenclature");
		String  project = (String) elem.getAttributes().get("ProjectName");
		//SMA4U_LOGGER.writeToLogFile("Writing Element With Partnumber : "+itemId,"INFO"); 
		logger.info("Writing Element With Partnumber : "+itemId); 
		String partOBID = (String) elem.getAttributes().get("OBID");
		if(partOBID == null)
		{
			partOBID= generateOBID(itemId,name);
		}
		streamWriter.writeAttribute(BaseElement.ID, partOBID);
		streamWriter.writeAttribute(BaseElement.ELEM_NMB, Integer.toString(elemNmb));
		boolean isOccEff = isTypeForOccEff(elem.getObjectType());
		streamWriter.writeAttribute(BaseElement.APPLY_EFFECTIVITY, Boolean.toString(isOccEff));
		String clsName = (String) elem.getAttributes().get("Class");;
		boolean isDataset = isFileRef(clsName);

		if(elem.getObjectType().equals("j0Cdi3D") && isC9ModelHasJT(elem))
		{
			isDataset = isC9ModelHasJT(elem);
		}
		streamWriter.writeAttribute(BaseElement.DS_REL, Boolean.toString(isDataset));
		streamWriter.writeAttribute(BaseElement.TAG_BV, "");
		streamWriter.writeAttribute(BaseElement.TAG_BVR, "");
		String action = "new";
		if(!isDbConfigLocAvailable())
		{
			action = "not-checked";
		}
		streamWriter.writeAttribute(BaseElement.ACTION, action);

		elemNmb++;
	}

	public boolean isC9ModelHasJT(JSONElelment elem)
	{
		if (elem != null && elem.getChildren().size() > 0)
		{
			for(int i=0; i < elem.getChildren().size();i++)
			{
				if(elem.getChildren().get(i).getObjectType().equals("JT"))
				{
					return true;
				}
			}
		}
		return false;
	}
	public void writeConnectionCTag(JSONElelment elem,  XMLStreamWriter streamWriter) throws XMLStreamException
	{

		//  <c id="whchGmbwgut1-usr_wgub1Xd" relCount="4" tagOCC="AVmJuQ_H5IK_vB" action="none">
		ArrayList<ClassMapInfo> clsInfo = mappingInfoMap.get(elem.getObjectType());
		//System.out.println("Size of the class Map Infos :: "+clsInfo.size());
		if(clsInfo == null || clsInfo.size() == 0)
		{
			//SMA4U_LOGGER.writeToLogFile("NO Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType()+"  OBID : "+ elem.getAttributes().get("OBID"),"INFO"); 
			logger.info("NO Mapping Element Defined for Name  :  "+elem.getName()+"  Type : "+elem.getObjectType()+"  OBID : "+ elem.getAttributes().get("OBID")); 
			return;
		}
		
		streamWriter.writeStartElement("c");

		String  itemId = (String) elem.getAttributes().get("PartNumber");
		String  name = (String) elem.getAttributes().get("j0Nomenclature");

		Object  relCount = elem.getRelAttributes().get("j0RelCount");
		if(relCount == null)
		{
			//relCount =  elem.getAttributes().get("j0RelCount");
			relCount = "";
		}
		String partOBID = (String) elem.getAttributes().get("OBID");
		if(partOBID == null)
		{
			partOBID= generateOBID(itemId,name);
		}
		streamWriter.writeAttribute(BaseElement.ID, partOBID);
		streamWriter.writeAttribute(BaseElement.TAG_REL_COUNT, relCount.toString());
		streamWriter.writeAttribute(BaseElement.TAG_OCC, "");
		
		String action = "new";
		if(!isDbConfigLocAvailable())
		{
			action = "not-checked";
		}
		streamWriter.writeAttribute(BaseElement.ACTION, action);

		String  codeRule = (String) elem.getRelAttributes().get("j0CodeRule");


		if(codeRule != null)
		{
			//System.out.println("Code Rule : "+codeRule);
			//logger.info("Code Rule : "+codeRule);

			//streamWriter.writeStartElement("note");

			streamWriter.writeEmptyElement("note");
			streamWriter.writeAttribute("n" ,"C9CodeRule");
			streamWriter.writeAttribute("v" ,codeRule);
			streamWriter.writeAttribute("t" ,"s");
			streamWriter.writeAttribute("s" ,"occ");
		}

		// adding trafo
		streamWriter.writeEmptyElement("t");
		//<t v0="1.0" v1="0.0" v2="0.0" v3="0.0" v4="0.0" v5="1.0" v6="0.0" v7="0.0" v8="0.0" v9="0.0" v10="1.0" v11="0.0" v12="0.0" v13="0.0" v14="0.0" v15="1.0"/>
		String[] trans = TrafoUtils.generateTrafoMatrix(elem);
		/*if(trans == null )
		{
		trans = {1.0,0.0,0.0,0.0,	
						  0.0,1.0,0.0,0.0,
						  0.0,0.0,1.0,0.0,
						  0.0,0.0,0.0,1.0};
		}*/
		for (int i = 0; i < trans.length; i++) {
			streamWriter.writeAttribute("v" + Integer.toString(i), trans[i]);
		}
		/*if (child.getFolderType() != null) {
			streamWriter.writeEmptyElement("fl");
			streamWriter.writeAttribute("t", child.getFolderType().toString());
		}*/
		streamWriter.writeEndElement(); // close c Tag

	}

	
	
	
	
	public JSONElelment getObjectData(String partId, ArrayList<JSONElelment> jsonObjectsList)
	{
		if(partId != null && jsonObjectsList != null && jsonObjectsList.size() > 0)
		{
			for(int i = 0; i < jsonObjectsList.size(); i++)
			{
				if(jsonObjectsList.get(i) != null && jsonObjectsList.get(i).getId() != null && jsonObjectsList.get(i).getId().equals(partId))
				{
					return jsonObjectsList.get(i);
				}
			}
		}
		return null;
	}

	public void writeFileInfo(final XMLStreamWriter streamWriter, final File file, final String filename) throws XMLStreamException {
		if (file == null) {
			return;
		}
		SimpleDateFormat fileDF = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss S");
		streamWriter.writeStartElement("plmxmlFile");
		streamWriter.writeAttribute("file", filename);
		streamWriter.writeAttribute("absolutePath", file.getAbsolutePath());
		streamWriter.writeAttribute("fileSize", Long.toString(file.length()));
		streamWriter.writeAttribute("modifDate", fileDF.format(new Date(file.lastModified())));
		streamWriter.writeEndElement();
	}

	public void writePropertyValue(final XMLStreamWriter streamWriter, final String name, Object value, String type, String propPlace) throws XMLStreamException {
		streamWriter.writeEmptyElement("p");
		if (value == null) {
			value = "";
		}
		//logger.info("Attribute Name : "+name+" ------- Value : "+value);
		streamWriter.writeAttribute("n", name);
		streamWriter.writeAttribute("v", value.toString());

		streamWriter.writeAttribute("t", type);

		streamWriter.writeAttribute("s", propPlace);
	}


	/**
	 * processing .dat file from send to package
	 * @throws IOException 
	 */
	public HashMap<String, String> readDatFileProps(String sendToplmxmlDatFilePath) throws IOException{

		System.out.println("******************* Reading .dat file.....********************************");
		String refConfigName = null;
		HashMap<String, String> dataFileProps = null;

		if(sendToplmxmlDatFilePath != null)
		{
			dataFileProps = new HashMap<>();
			FileInputStream fileInput = new FileInputStream(sendToplmxmlDatFilePath);
			Properties properties = new Properties();
			properties.load(fileInput);
			refConfigName = (String) properties.get("ref_config_name");  
			if(refConfigName != null)
			{
				dataFileProps.put("ref_config_name", refConfigName);
			}

			String mode = (String) properties.get("mode");   
			if(mode != null)
			{
				dataFileProps.put("mode", mode);
			}
			String user = (String) properties.get("user");   
			if(user != null)
			{
				dataFileProps.put("user", user);
			}
			//item_id
			String itemId = (String) properties.get("item_id");   
			if(itemId != null)
			{
				dataFileProps.put("item_id", itemId);
			}
			//name
			String name = (String) properties.get("name");   
			if(name != null)
			{
				dataFileProps.put("name", name);
			}
			//project
			String project = (String) properties.get("project");   
			if(project != null)
			{
				dataFileProps.put("project", project);
			}
		}




		//logger.info("****************************** End of Reading .dat file.....***********************************");
		System.out.println("****************************** End of Reading .dat file.....***********************************");
		return dataFileProps;

	} 


	private Object[] getProjectList(ArrayList<JSONElelment> dataItemObjs,String rootId) {

		Object[] ret = new Object[2];
		ArrayList<String> projectsList = new ArrayList<String>();
		String mainPrj = null;



		// Amit: this logic is for Smaragd elements 
		for (JSONElelment dItemObj : dataItemObjs) {
			if(dItemObj != null )
			{
				HashMap<String, Object> attributes = dItemObj.getAttributes();
				if( attributes != null && attributes.size() > 0)
				{
					String id = dItemObj.getId();
					Object proj = attributes.get("ProjectName");
					if(id.equals(rootId))
					{
						mainPrj = (String) proj;
					}
					else
					{
						if(proj != null && !projectsList.contains(proj))
						{
							projectsList.add((String) proj);
						}
					}
				}
			}
		}

		if(mainPrj == null)
		{
			System.out.println("Main Project is not Defined...");  
		}

		ret[0] = mainPrj;
		ret[1] = projectsList;
		return ret;
	}

	public static String generateOBID(String itemId,String name)
	{
		String itemIdString = null;
		itemIdString = hash(new String[] { itemId, name });


		return itemIdString;
	}

	public static String hash(String[] values) {
		long hash = 0xCBF29CE484222325L;
		for (String s : values) {
			if (s != null) {
				hash ^= s.hashCode();
				hash *= 0x100000001B3L;
			}
		}
		return String.valueOf((hash >>> 1) + (~hash >>> 31));
	}
	
	public static boolean isTypeForOccEff(final String typeName) {
		return TypeMaps.OCCEFF_TYPES.contains(typeName);
	}

	public boolean isDbConfigLocAvailable() {
		return isDbConfigLocAvailable;
	}

	public void setDbConfigLocAvailable(boolean isDbConfigLocAvailable) {
		this.isDbConfigLocAvailable = isDbConfigLocAvailable;
	}

}
