package cdm.pre.imp.reader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.batch.BatchException;
import cdm.pre.imp.configmap.ConfigMapUtils;
import cdm.pre.imp.json.reader.JSONTreeElement;
import cdm.pre.imp.map.TypeMaps;
import cdm.pre.imp.mod.TreeElement;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.writer.WriterUtils;

/**
 * A class which has same utility methods.
 * 
 * @author WIKEIM
 * 
 */
public class PLMUtils {
   private static final String INSTANCE       = "Instance";
   private static final String INSTANCE_GRAPH = "InstanceGraph";
   private static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");

   /**
    * Class to sort the elements which have been created during PLMXML file parsing by their type and usage.
    * 
    * @author WIKEIM
    * 
    */
   public static class SortedElements {
      // map of all the instance elements of the snapshot
      public Map<String, Element>       instanceMap           = new HashMap<String, Element>();
      // map of all the part elements of the snapshot
      public Map<String, Part>          partMap               = new HashMap<String, Part>();
      // map of all the CdiAss elements of the snapshot
      public Map<String, Element>       compRepCdiAssMap      = new HashMap<String, Element>();
      // map of all the Cdi3D elements of the snapshot
      public Map<String, Element>       compRepCdi3DMap       = new HashMap<String, Element>();

      // map of all the Cdi2D elements of the snapshot
      public Map<String, Element>       compRepCdi2DMap       = new HashMap<String, Element>();
      // equivalent refs
      public Map<String, List<Element>> equivalantRefs        = new HashMap<String, List<Element>>();
      // stuff stored in the primary folder
      public Map<String, List<Element>> relationRefsPrimary   = new HashMap<String, List<Element>>();
      // stuff stored in the secondary folder
      public Map<String, List<Element>> relationRefsSecondary = new HashMap<String, List<Element>>();
      // there is mostly just one rootRef but it could also be possible that
      // more than one could be supplied
      public String[]                   rootRefs;
      // the name of the Smaragd reference configuration
      public String                     refConfigName;
      // needed for sendTo
      public Map<String, Element>       compoundReps          = new HashMap<String, Element>();
      public Map<String, Occurrence>    occurrences           = new HashMap<String, Occurrence>();
      public String                     rootOcc;
      // To handle j0BuaKas & j0VisInt - Wiring harness data
      public Map<String, Element>       relationRefsCompRep   = new HashMap<String, Element>();
   }

   /**
    * Create a sorted list of the elements given as the parameter <code>pElements</code>. The sort criteria and which
    * elements should be returned in the {@link SortedElement} object is controlled by the preferences and the tagName
    * and other criteria of the individual element.
    * 
    * @param pElements
    * @return
    * @throws CDMException
    */
   public static SortedElements getElements(List<Element> pElements) throws CDMException {
      final SortedElements ret = new SortedElements();
      final boolean disable2D = PreImpConfig.getInstance().isDisable2D();
      final boolean disableSecFolder = PreImpConfig.getInstance().isDisableSecFolder();
      final boolean ignoreInvalidSmaDia = PreImpConfig.getInstance().isIgnoreInvalidSmaDia();
      final Set<String> invalidPartBlackList = new HashSet<String>();
      final Set<String> invalidInstanceBlackList = new HashSet<String>();
      Part currPart = null;
      
      //R1 filter invlid SMA
      if (ignoreInvalidSmaDia) {
         for (Element element : pElements) {
            if (element.getUserValues() != null) {
               String invalid = element.getUserValues().get(IConstants.j0Invalid);
               if ("+".equals(invalid)) {
                  invalidPartBlackList.add(element.getId());
               }
            }
         }
         for (Element element : pElements) {
            if (INSTANCE.equals(element.getTagName())) {
               String partRef = element.getAttributes().get(IConstants.partRef);
               if (invalidPartBlackList.contains(partRef)) {
                  invalidInstanceBlackList.add(element.getId());
               }
            }
         }
      }

      
      for (Element element : pElements) {
         final String tagName = element.getTagName();
         
         if (tagName.equals("Part")) {
            if (ignoreInvalidSmaDia && invalidPartBlackList.contains(element.getId())) {
               continue;
            }
            Part part = new Part();
            part.element = element;
            currPart = part;
            ret.partMap.put(element.getId(), part);
            String instanceRefs = element.getAttributes().get(IConstants.instRefs);
            if (instanceRefs != null) {
               String[] refs = instanceRefs.split(" ");
               part.refs = new ArrayList<String>();
               for (int i = 0; i < refs.length; i++) {
                  if (ignoreInvalidSmaDia && invalidInstanceBlackList.contains(refs[i])) {
                     continue;
                  }
                  part.refs.add(refs[i]);
               }
            }
         } else if (tagName.equals("Relation")) {
            String subType = element.getAttributes().get("subType");
            if (subType == null) {
               // we are not interested in any other relations
               continue;
            }
            String ooo = WriterUtils.getAttributeValue(element, "id");
			 /*if(ooo != null && ooo.equals("usr_wgub0000000319DA1138"))
			 {
				 System.out.println("CompReps for Part : "+ooo);
				 logger.info("CompReps for Part : "+strObid);
				// System.out.println("CompReps : "+part.compReps);
				// logger.info("CompReps : "+part.compReps);
			 }*/
            final boolean primary = !subType.equals(IConstants.j2PbiDtS);
            
            //final boolean primary = subType.equals(IConstants.j2PbiDtS);
            
            // check if secondary folders should be skipped
            if (disableSecFolder && !primary) {
               continue;
            }

            String relatedRefs = element.getAttributes().get(IConstants.relatedRefs);
            if (relatedRefs != null) {
               String[] related = relatedRefs.split(" ");
               if (related.length != 2) {
                  throw new CDMException("dcx:Relation '" + element.getId() + "' for subType " + subType
                        + " contains not expected relatedRefs");
               }

               Map<String, List<Element>> relationRefs = primary ? ret.relationRefsPrimary : ret.relationRefsSecondary;
               String partRef = related[0];
               List<Element> elems = relationRefs.get(partRef);
               if (elems == null) {
                  elems = new ArrayList<Element>();
                  relationRefs.put(partRef, elems);
               }
               elems.add(element);
               // To handle j0BuaKas & j0VisInt - Wiring harness data
               String compRep = related[1];
               if(ret.relationRefsCompRep.containsKey(compRep)){
	               List<Element> fileRefs = ret.equivalantRefs.get(related[1]);
	               if (fileRefs == null) {
	                  fileRefs = new ArrayList<Element>();
	                  ret.equivalantRefs.put(related[0], fileRefs);
	               }
	               fileRefs.add(ret.relationRefsCompRep.get(compRep));
               }
            }
         } else if (tagName.equals(INSTANCE)) {
        	 
        	 // Validation of roots for cyclic structure...
        	 
        	
        	 
            String partRef = element.getAttributes().get(IConstants.partRef);
            if (ignoreInvalidSmaDia && invalidInstanceBlackList.contains(partRef)) {
               continue;
            }
            element.getAttributes().put(IConstants.partRef, partRef);   // why is this line required ?
            ret.instanceMap.put(element.getId(), element);
         } else if (tagName.equals(INSTANCE_GRAPH)) {
            ret.rootRefs = element.getAttributes().get("rootRefs").split(" ");
         } else if (tagName.equals("CompoundRep")) {
            if (currPart.compReps == null) {
               currPart.compReps = new ArrayList<String>();
            }
            currPart.compReps.add(element.getId());
            ret.compoundReps.put(element.getId(), element);
        
            if (IConstants.j0Cdi3D.equals(element.getClazz())) {
            	//logger.info("Reader OBID : "+element.getAppLabel());
            	ReaderSingleton.getReaderSingleton().addAppLabel(element.getAppLabel());
               ret.compRepCdi3DMap.put(element.getId(), element);
            } else if (IConstants.j0Cdi2D.equals(element.getClazz()) && !disable2D) {
            	
               ret.compRepCdi2DMap.put(element.getId(), element);
            } else if (IConstants.j0CdiAss.equals(element.getClazz())) {
               ret.compRepCdiAssMap.put(element.getId(), element);
            } else if(IConstants.j0BauKas.equals(element.getClazz()) || IConstants.j0VisInt.equals(element.getClazz())){ 
            	// To handle j0BuaKas & j0VisInt - Wiring harness data
            	ret.relationRefsCompRep.put(element.getId(),element);
            }else {
               String eqRef = element.getAttributes().get("equivalentRef");
               if (eqRef != null) {
            	  
                  // sometimes the Class user value is not available for JTs
                  if (element.getUserValues().get(IConstants.Class) == null) {
                     element.getUserValues().put(IConstants.Class, element.getAttributes().get("format"));
                  }
                  List<Element> fileRefs = ret.equivalantRefs.get(eqRef);
                  if (fileRefs == null) {
                     fileRefs = new ArrayList<Element>();
                     ret.equivalantRefs.put(eqRef, fileRefs);
                  }
                  fileRefs.add(element);
               }
            }
         } else if (tagName.equals(IConstants.Header)) {
            if (element.getUserValues() != null) {
               ret.refConfigName = element.getUserValues().get(IConstants.REFCONFIG);
            }
         } else if ("Occurrence".equals(tagName)) {
            if (ret.rootOcc == null) {
               ret.rootOcc = element.getId();
            }
            ret.occurrences.put(element.getId(), new Occurrence(element.getId(), element.getAttributes().get("occurrenceRefs"), element
                  .getAttributes().get("instanceRefs"), element.getAttributes().get("representationRefs")));
         }
      }
      return ret;
   }

   /**
    * Checks if a Smaragd class name is naming a SmaDia2 class.
    * 
    * @param eClazz
    *           The Smaragd class name.
    * @return <code>true</code> if it is naming a SmaDia2 class.
    */
   public static boolean isDiaElement(final String eClazz) {
      return eClazz.startsWith("j0SD");
   }

   /**
    * Checks if a Smaragd class name is naming a Smaragd class which is used to manage file objects.
    * 
    * @param eClazz
    *           The name of the Smaragd class.
    * @return <code>If the Smaragd class name is naming class of Smaragd that manages file objects.
    */
   public static boolean isFileRef(final String eClazz) {
	  return TypeMaps.getFileType(eClazz) != null;
   }

   /**
    * Checks by the name if a project name is naming a secured project.<br>
    * <b>Attention: The extensions for secured projects my vary. It would be better in the future to determine if a
    * project is a secured project based on a Smaragd export list.</b>
    * 
    * @param projectName
    *           The TcUA project name.
    * @return <code>true</code> if secured.
    */
   public static boolean isSecuredProject(String projectName) {
      projectName = projectName.toLowerCase();
      return projectName.contains("@sec") || projectName.contains("@guard") || projectName.contains("design")
            || projectName.contains("@conf") || projectName.endsWith("sec");
   }
   
   public static boolean isCommonProject(String projectName) {
	      projectName = projectName.toLowerCase();
	      return projectName.equals("multipleuse") || projectName.equals("dc_general") || projectName.equals("globalparts");
	   }

   private static final Pattern             projNamePattern = Pattern.compile("(.+?)@(.+)");
   private static final Map<String, String> projNames       = new HashMap<String, String>();

   /**
    * This creates the TcUA Project name out of a Smaragd project name because in Smaragd certain characters in project
    * names are not allowed.
    * 
    * @param projectName
    *           The Smaragd project name.
    * @return The TcUA project name.
    */
   public static String convertProjectName(final String projectName) {
      String ret = null;
      synchronized (projNames) {
         ret = projNames.get(projectName);
         if (ret == null) {
            Matcher m = projNamePattern.matcher(projectName);
            if (m.matches()) {
               ret = m.group(1) + "_" + m.group(2) + "_";
            } else {
               ret = projectName;
            }
            projNames.put(projectName, ret);
         }
      }
      return ret;
   }
   
   /**
  	 * generates the Truck hierarchy by adding the additional Truck Configuration item element for every C/D-BM
  	 * under the Truck vehicle. 
  	 * @param roots
  	 * @return : returns the modified structure tree with the additional element
  	 * @throws BatchException 
  	 */
  	public static List<TreeElement> generateTruckHierarchy(List<TreeElement> roots) throws BatchException {
  		
  		// assumes all Truck related checks are done
  		if(roots != null) {
  			// gets the root node and checks if the type is a C9Truck
  			if(roots.get(0) != null) {
  				int counter = 0;						// counter for the id of the config elements
  				
  				// checks the top level element in the roots hierarchy to be mapped to a C9Truck  
  				
  				if(roots.get(0).getElement().getMappedElementsMap().containsKey(IConstants.TRUCK_CLASS_C9TRCK) ) {
  				
  			// Krishna : Added truck changes for daimler truck POC TRUCK_CLASS_DBM
  			//	if(roots.get(0).getElement().getMappedElementsMap().containsKey(IConstants.TRUCK_CLASS_C9TRCK) || roots.get(0).getElement().getMappedElementsMap().containsKey(IConstants.TRUCK_CLASS_DBM)) {
  					// iterate through the list of child C/D-BMs 
  					if(ReaderSingleton.getReaderSingleton().getPart2EffIDMap() != null) {
  						for(Map.Entry<String, String> mapEntry : ReaderSingleton.getReaderSingleton().getPart2EffIDMap().entrySet()) {
  							// create a dummy hash map for the attributes of the config item object
  							HashMap<String, String> dmyAttrMap = new HashMap<String, String>();
  							// increments the counter
  							++counter;
  							// maps the required attributes to corresponding values
  							dmyAttrMap.put("id", "cfg" + String.valueOf(counter));
  							dmyAttrMap.put("name", "CFG_" + mapEntry.getValue());
  							// instantiate a Part object for each entry in the map for the representation for C9CfgTruck item
  							Element cfgTrckElem = new Element("Part", dmyAttrMap);
  							// sets the required attributes for the user values map for the element object
  							if(cfgTrckElem != null) {
  								cfgTrckElem.setAppLabel("CFG_" + mapEntry.getValue());
  								cfgTrckElem.setUserValues("Class", IConstants.TRUCK_CLASS_BCS);
  								cfgTrckElem.setUserValues("Revision", "0001");
  								cfgTrckElem.setUserValues("Sequence", "001");
  								cfgTrckElem.setUserValues("Nomenclature", "CFG_" + mapEntry.getValue());
  								cfgTrckElem.setUserValues("PartNumber", "CFG_" + mapEntry.getValue());
  								if(ReaderSingleton.getReaderSingleton().getBcsProjectName() != null) {
  									cfgTrckElem.setUserValues("Project", ReaderSingleton.getReaderSingleton().getBcsProjectName());
  								}
  								// populates the mapped element and mapped attributes for the element object
  								MappedAttributes attrMap = new MappedAttributes("item_id", "CFG_" + mapEntry.getValue(), "i", "s", true);
  								cfgTrckElem.setMappedElementsMap(IConstants.TRUCK_CLASS_C9TRCKCFG, attrMap);
  								attrMap = null;
  								attrMap = new MappedAttributes("object_name", "CFG_" + mapEntry.getValue(), "i", "s", true);
  								cfgTrckElem.setMappedElementsMap(IConstants.TRUCK_CLASS_C9TRCKCFG, attrMap);
  								attrMap = null;
  								attrMap = new MappedAttributes("item_revision_id", "0001.001", "r", "s", true);
  								cfgTrckElem.setMappedElementsMap(IConstants.TRUCK_CLASS_C9TRCKCFG, attrMap);
  								attrMap = null;
  								attrMap = new MappedAttributes("id", "CFG_" + mapEntry.getValue(), "r", "s", true);
  								cfgTrckElem.setMappedElementsMap(IConstants.TRUCK_CLASS_C9TRCKCFG, attrMap);
  								// 01-02-2017 : setting the end item information for the configuration item 
  								if(ReaderSingleton.getReaderSingleton().getRefFzgName() != null) {
  									cfgTrckElem.setEndItemIDs(ReaderSingleton.getReaderSingleton().getRefFzgName(), true);
  								}
  								// instantiate the part object
  								Part cfgTrckPart = new Part();
  								// assigns the element object to the part object
  								cfgTrckPart.element = cfgTrckElem;
  								// instantiates the corresponding Tree Element instance
  								TreeElement cfgTreeElem = new TreeElement(cfgTrckElem);
  								// logic to change the hierarchy in roots.get(0)
  								if(cfgTreeElem != null) {
  									// get exact child Tree Element for the entry in the singleton map
  									TreeElement BMChild = roots.get(0).findChild(mapEntry.getKey());
  									// stores the children temporarily in the local placeholder
  									if(BMChild != null) {
  										// removes the link between the C9Truck and the BM tree element
  										roots.get(0).removeChild(mapEntry.getKey());
  										// adds the removed BM child as the child to the Config Item Tree element
  										cfgTreeElem.addChild(BMChild);
  										// adds the config tree element as child to the root C9 element
  										roots.get(0).addChild(cfgTreeElem);
  										
  									}
  								}
  							}
  						}
  					}
  				}
  				else {
  					throw new BatchException("No top level C9Truck object found in the runtime BOM structure. Exception thrown in Class: " 
  						+ "PLMUtils at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(),
						true, ConfigMapUtils.LOG_TYPE_ERROR);
  				}
  			}
  		}
  		return roots;
  	}
  	
  	/**
  	 * Prints the missing OBIDs
  	 */
  	public static void printMissingOBID() {
		ArrayList<String> getCdi3dListWriter = ReaderSingleton.getReaderSingleton().getCdi3dListWriter();//.addWriterAppLabel(element.getAppLabel());
		ArrayList<String> getCdi3dList = ReaderSingleton.getReaderSingleton().getCdi3dList();
		
		logger.info("*********************** Missing OBID****************************** ");
		if( getCdi3dListWriter != null && getCdi3dListWriter.size() > 0)
		{
			if(getCdi3dList != null && getCdi3dList.size() > 0)
			{
				for (String cdi3d : getCdi3dList) 
				{
					if(cdi3d != null && !getCdi3dListWriter.contains(cdi3d))
					{
						logger.info("Missing OBID : "+cdi3d);
					}
				}
			}
		}
		logger.info("*********************** END OF Missing OBID****************************** ");
	}
  	
  	/**
  	 * prints out the individual Smaragd object count and the corresponding TcUA object type count
  	 * the count corresponds to the count after the delta count 
  	 */
  	public static void printObjCountReport() {
  		// null check for the Singleton member
  		if(ReaderSingleton.getReaderSingleton().getObjInfoList() != null) {
  			logger.info("**************************************************************");
  			logger.info("*******************OBJECT COUNT REPORT************************");
  			logger.info("                                                            ");
  			HashMap<String, Integer> dbMap = ReaderSingleton.getReaderSingleton().getMapDbObjectSum();
  			int count =0;
  			for(ObjectInfo objInfoInst : ReaderSingleton.getReaderSingleton().getObjInfoList()) {
  				if(objInfoInst != null) {
  					logger.info("(SmaPLMXML) SMARAGD TYPE: " + objInfoInst.getSmaClsName() + " ------> " + objInfoInst.getSmaObjCount());
  					if(objInfoInst.getTcType2CountMap() != null) {
  						// iterates through the map and prints out the individual child tc object names and their respective counts
  						for(Map.Entry<String, Integer> entry : objInfoInst.getTcType2CountMap().entrySet()) {
  							logger.info("(SmaPLMXML) TcUA TYPE: " + entry.getKey() + " ------> " + entry.getValue());
  							if(dbMap.get(entry.getKey()) != null){
  								logger.info("(Database) TcUA TYPE: " + entry.getKey() + " ------> " + dbMap.get(entry.getKey()));
  								count = count+dbMap.get(entry.getKey());
  							}
  						}
  					}
  					logger.info("                                                            ");
  				}
  			}
  			logger.info("*******************(Database) OBJECT COUNT : "+count+" ************************");
  			logger.info("*******************OBJECT COUNT REPORT************************");
  			logger.info("**************************************************************");
  		}
  	}
  	
  	public static void printDBObjCountReport() {
  		// null check for the Singleton member
  		if(ReaderSingleton.getReaderSingleton().getMapDbObjectSum() != null) {
  			logger.info("**************************************************************");
  			logger.info("*******************DATABASE OBJECTS COUNT REPORT************************");
  			logger.info("                                                            ");
  			HashMap<String, Integer> dbMap = ReaderSingleton.getReaderSingleton().getMapDbObjectSum();
  			int count =0;
  			for(Map.Entry<String, Integer> entry : dbMap.entrySet()) {
					logger.info("TcUA TYPE: " + entry.getKey() + " ------> " + entry.getValue());
					count = count+entry.getValue();
				}
  			logger.info("*******************TOTAL NO OF OBJECTS FOUND IN DATABASE : "+count+" ************************");
  			
  			//logger.info("*******************OBJECT COUNT REPORT************************");
  			logger.info("**************************************************************");
  		}
  	}
  	
  	
  	/**
  	 * prints out the count of the bom children for each bom parent in the BOM structure
  	 */
  	public static void printBOMCountReport() {
  		// null check for the Singleton member
  		if(ReaderSingleton.getReaderSingleton().getBOMParent2ChildMap() != null) {
  			logger.info("***************************************************************");
  			logger.info("********************BOM COUNT REPORT***************************");
  			logger.info("                                                            ");
  			// declare a map iterator for the Singleton map
  			for(Map.Entry<Integer, TreeMap<String, BOMInfo>> entry : ReaderSingleton.getReaderSingleton().getBOMParent2ChildMap().entrySet()) {
  				logger.info("BOM LEVEL ------> " + entry.getKey());
  				for(Map.Entry<String, BOMInfo> mapEntry : entry.getValue().entrySet()) {
  					logger.info("BOM PARENT PARTNO. ------> " + mapEntry.getValue().getBomParentID());
  					logger.info("----------------------------------------->   CHILD COUNT IS ------> " + mapEntry.getValue().getBomChildOBID().size());
  				}
  			}
  			logger.info("                                                            ");
  			logger.info("********************BOM COUNT REPORT***************************");
  			logger.info("***********************************T***************************");
  		}
  	}
  	
  	/**
  	 * method that prints out the child part numbers for each of the parent bom lines in the stru
  	 */
  	public static void printBOMReport() {
  		
  		String cdmLogPath = null;									// path to the CDM_IMPORTER_LOG env variable
  		
  		// open the BOM report file
  		try {
  			Map<String, String> envMap = System.getenv();
  			if(envMap != null && !envMap.isEmpty()) {
  				cdmLogPath = envMap.get("CDM_IMPORTER_LOG");
  			}
  			
  			if(cdmLogPath != null) {
  			
	  			PrintWriter printWriterObj = new PrintWriter(cdmLogPath + "\\" + ReaderSingleton.getReaderSingleton().getRefFzgName()
	  			+ "_bomContentReport.txt");
	
	  			// null check for the Singleton member
	  			if(ReaderSingleton.getReaderSingleton().getBOMParent2ChildMap() != null) {
	  				printWriterObj.println("***************************************************************");
	  				printWriterObj.println("********************BOM CONTENT REPORT*************************");
	  				printWriterObj.println("                                                            ");
	  				// declare a map iterator for the Singleton map
	  				for(Map.Entry<Integer, TreeMap<String, BOMInfo>> entry : ReaderSingleton.getReaderSingleton().getBOMParent2ChildMap().entrySet()) {
	  					for(Map.Entry<String, BOMInfo> mapEntry : entry.getValue().entrySet()) {
	  						
	  						/*if( mapEntry.getValue().getBomParentID().equals("A6SE-967603001A")) {
	  							System.out.println("Here");
	  						}*/
	  						
	  						// writes out the line for a truck parent item
	  						if(mapEntry.getValue().getTrckBMEndItem() != null && !mapEntry.getValue().getTrckBMEndItem().isEmpty()) {
	  							printWriterObj.println("#" + entry.getKey() + "$" + mapEntry.getValue().getBomParentID() + "/" + mapEntry.getValue().getBomParRevID()
	  									+ "$" + mapEntry.getValue().getTrckBMEndItem());
	  						}
	  						else {
	  							printWriterObj.println("#" + entry.getKey() + "$" + mapEntry.getValue().getBomParentID() + "/" + mapEntry.getValue().getBomParRevID());
	  						}
	  						
	  						// iterate and print out the part numbers of the children
	  						for(Map.Entry<String, String> chldRevEntry : mapEntry.getValue().getBomChildOBID().entrySet()) {
	  							printWriterObj.println("									" + chldRevEntry.getKey() + "/" + chldRevEntry.getValue());
	  						}
	  					}
	  				}
	  				printWriterObj.println("                                                            ");
	  				printWriterObj.println("********************BOM CONTENT REPORT*************************");
	  				printWriterObj.println("***************************************************************");
	  			}
	  			printWriterObj.close();
  			}
  		} catch (FileNotFoundException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
  	}

}
