package cdm.pre.imp.mod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.map.TypeMaps;
import cdm.pre.imp.mod.TreeElement.FolderType;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.Element;
import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.MappedElements;
import cdm.pre.imp.reader.Occurrence;
import cdm.pre.imp.reader.PLMUtils;
import cdm.pre.imp.reader.PLMUtils.SortedElements;
import cdm.pre.imp.reader.Part;
import cdm.pre.imp.reader.ReaderSingleton;
import cdm.pre.imp.writer.WriterUtils;

public abstract class AbstractTreeElementFactoy {
	protected static String vehicleType;    				// class member that captures the vehicle type being imported

	
	// member map copied from the BatchImportUtil java file
	// private static HashMap<String, TreeElement> secXMLFile2ChildTreeElemMap = new HashMap<String, TreeElement>();
	private SortedElements sortedElements;

	protected Map<String, TreeElement> idTreeMap = new HashMap<String, TreeElement>();

	private final static Logger logger = LogManager.getLogger(AbstractTreeElementFactoy.class.getName());


	protected AbstractTreeElementFactoy(final SortedElements sortedElements) {
		this.sortedElements = sortedElements;
	}


	public static TreeElement createNewRoot()
	{

		// create a dummy hash map for the attributes of the config item object
		HashMap<String, String> dmyAttrMap = new HashMap<String, String>();

		TreeElement cfgTreeElem = null;
		// increments the counter
		//++counter;
		// maps the required attributes to corresponding values
		dmyAttrMap.put("id", "dummy_root" );
		dmyAttrMap.put("name", "dummy_root_name" );
		// instantiate a Part object for each entry in the map for the representation for C9CfgTruck item
		Element cfgTrckElem = new Element("Part", dmyAttrMap);
		// sets the required attributes for the user values map for the element object
		if(cfgTrckElem != null) {
			cfgTrckElem.setAppLabel("dummy_root_label" );
			cfgTrckElem.setUserValues("Class", IConstants.TRUCK_CLASS_BCS);
			cfgTrckElem.setUserValues("Revision", "0001");
			cfgTrckElem.setUserValues("Sequence", "001");
			cfgTrckElem.setUserValues("Nomenclature", "dummy_root_nomenclature");
			cfgTrckElem.setUserValues("PartNumber", "dummy_root_partnumber");
			if(ReaderSingleton.getReaderSingleton().getBcsProjectName() != null) {
				cfgTrckElem.setUserValues("Project", ReaderSingleton.getReaderSingleton().getBcsProjectName());
			}
			// populates the mapped element and mapped attributes for the element object
			/*	MappedAttributes attrMap = new MappedAttributes("item_id", "dummy_root_id" , "i", "s", true);
				cfgTrckElem.setMappedElementsMap(IConstants.TRUCK_CLASS_C9TRCKCFG, attrMap);
				attrMap = null;
				attrMap = new MappedAttributes("object_name", "dummy_root_objectname" , "i", "s", true);
				cfgTrckElem.setMappedElementsMap(IConstants.TRUCK_CLASS_C9TRCKCFG, attrMap);
				attrMap = null;
				attrMap = new MappedAttributes("item_revision_id", "0001.001", "r", "s", true);
				cfgTrckElem.setMappedElementsMap(IConstants.TRUCK_CLASS_C9TRCKCFG, attrMap);
				attrMap = null;
				attrMap = new MappedAttributes("id", "dummy_root_id", "r", "s", true);
				cfgTrckElem.setMappedElementsMap(IConstants.TRUCK_CLASS_C9TRCKCFG, attrMap);
				// 01-02-2017 : setting the end item information for the configuration item 
				if(ReaderSingleton.getReaderSingleton().getRefFzgName() != null) {
					cfgTrckElem.setEndItemIDs(ReaderSingleton.getReaderSingleton().getRefFzgName(), true);
				}*/
			// instantiate the part object
			Part cfgTrckPart = new Part();
			// assigns the element object to the part object
			cfgTrckPart.element = cfgTrckElem;
			// instantiates the corresponding Tree Element instance
			cfgTreeElem = new TreeElement(cfgTrckElem);
			// logic to change the hierarchy in roots.get(0)
			/*if(cfgTreeElem != null) {
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
				}*/
		}
		return cfgTreeElem;

	}

	protected void createAltHierachy(final Occurrence parentOcc, final TreeElement parentTree) {
		List<String> childOccIds = parentOcc.getOccurrenceRefs();
		for (String childOccId : childOccIds) {
			if (childOccId.startsWith("d_")) {
				// skip the sendTo specific node
				continue;
			}
			Occurrence childOcc = sortedElements.occurrences.get(childOccId);
			if (!childOcc.getInstanceRefs().isEmpty()) {
				String instId = childOcc.getInstanceRefs().get(childOcc.getInstanceRefs().size() - 1);
				TreeElement childTree = idTreeMap.get(instId);
				// childTree = childTree.copy();
				if (childTree != null) { // happens for example if non Geo Pos are ignored
					parentTree.getAltChilds().add(childTree);
					if (!childOcc.getRepresentationRefs().isEmpty()) {
						for (String repId : childOcc.getRepresentationRefs()) {
							TreeElement repTree = idTreeMap.get(repId);
							if (repTree != null) { // happens if an pre 14.1 PLMXML is used
								TreeElement parTree = repTree.getParent();
								// this is just an approximated assignment, because the logic of the alternative hierarchy is
								// different here
								if (!parTree.getAltChilds().contains(repTree)) {
									parTree.getAltChilds().add(repTree);
								}
								if (parTree != childTree) { // do this if it is only a CDI
									childTree.getAltChilds().add(parTree);
								}
							}
						}
					}
					createAltHierachy(childOcc, childTree);
				}
			}
		}
	}

	/**
	 * Internal method which is called recursively in order to create the tree.
	 * 
	 * @param currInst
	 *           The current PLMXML instance element.
	 * @param sortedElements
	 *           The sorted element class.
	 * @param parent
	 *           The parent element in the tree to be build.
	 * @return The newly created {@link TreeElement} object.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws CDMException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	protected TreeElement createGraph(final String currInst, final TreeElement parent, boolean isSecXML)
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException,
			IOException, CDMException {

		// removed the final qualifier from the partRef variable - 06/12/2016

		String partRef = null;
		TreeElement nextElement = null;
		String partClass = null;
		TreeElement root = null;
		Part part = null;
        
		final Element instance = sortedElements.instanceMap.get(currInst);
		logger.debug(String.format("Retrieve instance %s from the parser pool (sortedElements)", currInst));

		// boolean isCylicStructre = false;
		if (instance != null) {
			partRef = instance.getAttributes().get(IConstants.partRef);

		}

		// sendTo creates a kind of dummy element which has to be skipped
		final boolean dummySendToElement = partRef.startsWith("d_");

		// final boolean dummySendToElement = f);

		if (partRef != null) {
			part = sortedElements.partMap.get(partRef);
			logger.debug(String.format("Retrieve part %s from the parser pool (sortedElements)", partRef));
			// System.out.println("Part Ref : "+partRef); part.element != null &&
			// part.element.getClazz() != null &&
			if (part != null && part.element != null && part.element.getClazz() != null
					&& part.element.getClazz().equals("j0SupNum")) {
				// System.out.println("Object Type : j0SupNum");
				return null;
			}
			
			TreeElement pp = parent;
			int p = 0;

			boolean isCylicStructre = false;
			while (pp != null && !isCylicStructre) { //TODO think about to replace it
				if (pp.getOBID() != null && pp.getOBID().equals(part.element.getUserValues().get("OBID")))
				// if(pp.getElement().getId().equals(instance.getId())) OBID
				{
					logger.info("Found cylic structure with  : " + part.element.getUserValues().get("PartNumber")
							+ " SKIPPING processing the Element");
					isCylicStructre = true;
					break;
				}

				pp = pp.getParent();
				p++;
			}
			if (isCylicStructre) {
				return null;
			}
		}

		// truck structure
		if (part != null) {
			partClass = part.element.getClazz();
			logger.debug(String.format("Part class:  %s", partClass));

			// not using non geo pos marked positions
			if (PreImpConfig.getInstance().isOnlyGeoPos()) {
				if (IConstants.j0SDPos.equals(partClass)
						&& "-".equals(part.element.getUserValues().get(IConstants.j0SmaDia2GeoPos))) {
					return null;
				}
			}

			// it has to be checked if the ROOT_TYPE check is still needed
			// if ((parent != null) || TypeMaps.ROOT_TYPES.contains(partClass)) {
			if (dummySendToElement) {
				// skip the element that is only contained in SendTo PLMXML
				nextElement = parent;
			} else {
				nextElement = new TreeElement(part.element);

				if (TreeElementFactoryFromPLMXML.vehicleType != null && partClass != null) {
					ReaderSingleton.getReaderSingleton().setRefFzgName(nextElement.getPartNumber());
					/*
					 * Moved to report visitor
					// sets the bom level on the element object 
					if (currInst.equals(IConstants.ROOT_INSTANCE) || currInst.equals(IConstants.FIRST_INSTANCE)) {
						// sets the bom level to 1 for the part object of the root instance
						nextElement.setBomLevel(1);

						// sets the part number of the next element instance to the singleton instance
						ReaderSingleton.getReaderSingleton().setRefFzgName(nextElement.getPartNumber());
					} else {
						// sets the BOM Level
						if (parent != null && nextElement != null) {
							nextElement.setBomLevel(parent.getBomLevel() + 1);
						}
					}
					*/
				}

				/* moved to mapping visitor
				 * 
				// call to implement target data model mapping on the child Tree element
				MappedElements mapElem = new MappedElements();
				if (mapElem != null && nextElement != null) {
					// sets the mapped element to the Tree Element instance
					nextElement.setElement(mapElem.processElementForMapping(nextElement.getElement()));

					
					// 24-02-2017 - call to populate the OBID to the tree element instance once the
					// mapping is done
					if (nextElement.getElement() != null && nextElement.getElement().getMappedElementsMap() != null) {
						// String strItemID = WriterUtils.getAttributeValue(nextElement.getElement(),
						// "item_id");
						String obid = WriterUtils.getAttributeValue(nextElement.getElement(), "id");
						if (obid != null) {
							// sets the OBID on the element object of the parent tree element object
							nextElement.getElement().setOBID(obid);
							// sets the OBID on the tree element object
							nextElement.setOBID(obid);
							// compareWithDBData(nextElement.getElement());
						}
					}
				} */

				/*
				 * Moved to visitor
				 * 
				if (nextElement != null && parent != null) {
					String parentProject = parent.getProject();
					if (parentProject == null) {
						parentProject = parent.getElement().getUpdatedProjectName();
					}
					String instanceProject = nextElement.getElement().getUserValues().get(IConstants.ProjectName);
					if (parentProject != null && instanceProject != null) {
						if (PLMUtils.isSecuredProject(instanceProject) || PLMUtils.isCommonProject(instanceProject)) {
							nextElement.getElement().setUpdatedProjectName(instanceProject);
						} else {
							if (instanceProject.equals(parentProject)) {
								if (parent.getElement().getUpdatedProjectName() != null
										&& parent.getElement().getUpdatedProjectName().contains("_COP")) {
									nextElement.getElement()
											.setUpdatedProjectName(parent.getElement().getUpdatedProjectName());
								} else {
									nextElement.getElement().setUpdatedProjectName(instanceProject);
								}
							} else if (!instanceProject.equals(parentProject)) {
								if (parent.getElement().getUpdatedProjectName() == null) {
									if (PLMUtils.isSecuredProject(parentProject)
											|| PLMUtils.isCommonProject(parentProject)) {
										nextElement.getElement().setUpdatedProjectName(instanceProject);
									} else if (parent.getProject() != null) {
										nextElement.getElement().setUpdatedProjectName(parent.getProject() + "_COP");
									} else {
										nextElement.getElement().setUpdatedProjectName(instanceProject);
									}
								}

								else if (parent.getElement().getUpdatedProjectName().endsWith("_COP")) {
									nextElement.getElement()
											.setUpdatedProjectName(parent.getElement().getUpdatedProjectName());
								} else {
									if (PLMUtils.isSecuredProject(parent.getElement().getUpdatedProjectName())
											|| PLMUtils.isCommonProject(parent.getElement().getUpdatedProjectName())) {
										nextElement.getElement().setUpdatedProjectName(instanceProject);
									} else {
										nextElement.getElement().setUpdatedProjectName(
												parent.getElement().getUpdatedProjectName() + "_COP");
									}
								}
							}
						}
					}
				}
				
				*/

				idTreeMap.put(currInst, nextElement);
				nextElement.setRelatingElement(instance);
				if (parent != null) {
					// condition check to find if the parent is mapped to a Teamcenter object type
					if ((parent.getElement() != null) && (parent.getElement().getMappedElementsMap() != null)) {
						parent.addChild(nextElement);
						logger.debug(String.format("Add part %s to parent %s", nextElement.getElement().getAttributes().get("id"), parent.getElement().getAttributes().get("id")));
						logger.debug(String.format("Part %s OBID: %s", nextElement.getElement().getAttributes().get("id"), nextElement.getOBID()));

						/*
						 * Moved to Report visitor
						// Amit - 08/03/2017 - added code for putting it to the output log file
						// retrieve the item revision ids for the BOM parent and BOM child
						String parItemRevID = null; // item revision id of the parent item revision id
						String childItemRevID = null; // item revision id of the child item revision id

						if (parent.getElement() != null && parent.getElement().getMappedElementsMap() != null) {
							parItemRevID = WriterUtils.getAttributeValue(parent.getElement(),
									IConstants.TC_ATTR_ITEMREVID);
						}

						if (nextElement.getElement() != null
								&& nextElement.getElement().getMappedElementsMap() != null) {
							childItemRevID = WriterUtils.getAttributeValue(nextElement.getElement(),
									IConstants.TC_ATTR_ITEMREVID);
						}

						// call to set the information required for the generation of the BOM report for
						// a car
						if (TreeElementFactoryFromPLMXML.vehicleType != null
								&& TreeElementFactoryFromPLMXML.vehicleType.equals(PreferenceConstants.P_FZG_TYP_PKW)) {
							ReaderSingleton.getReaderSingleton().setBOMParent2ChildMap(parent.getOBID(),
									nextElement.getPartNumber(), parent.getPartName(), parent.getPartNumber(),
									parent.getBomLevel(), parItemRevID, childItemRevID);
						}
						*/
					}
				} else {
					root = nextElement;
				}
			}

			FolderType folderType = TreeElement.FolderType.Primary;
			if (dummySendToElement) {
				if (instance.getClazz().equals("j2PbiDtP")) {
					folderType = TreeElement.FolderType.Primary;
				} else if (instance.getClazz().equals("j2PbiDtS")) {
					folderType = TreeElement.FolderType.Primary;
				} else if (instance.getClazz().equals("j2pPrdRv")) {
					folderType = TreeElement.FolderType.Primary;
				} else {
					folderType = TreeElement.FolderType.Unknown;
				}
			}

			// if (TypeMaps.GEO_TYPES.contains(partClass) || dummySendToElement &&
			// part.compReps != null) {
			if (TypeMaps.GEO_TYPES.contains(partClass) || dummySendToElement && part.compReps != null) {
				addCDIs(folderType, sortedElements, part, nextElement,
						dummySendToElement ? instance.getTransform() : null,
						dummySendToElement ? instance.getUserValues().get(IConstants.j0RelCount) : null);
				if (!dummySendToElement) {
					addCDIs(TreeElement.FolderType.Secondary, sortedElements, part, nextElement, null, null);
				}
			}

			// logger.info("Part ID : "+part.element.getId());
			if (part.refs != null) {

				for (String instId : part.refs) {
					// null check and empty string check required as the instance refs attribute is
					// failing for some elements (Smaragd data not available !!!!) in the truck dmu
					// xml file
					if (instId != null && instId != "") {
						TreeElement r = createGraph(instId, nextElement, false);
						if (r != null) {
							root = r;
						}
					}
				}
			}
		}

		return root;
	}

	private void addFilesToCDI(final TreeElement cdiTreeElement, final List<Element> fileRefs, final String transform) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (fileRefs != null) {
			for (final Element fileRef : fileRefs) {
				String clazz = fileRef.getClazz();
				if (PLMUtils.isFileRef(clazz)) {
					//System.out.println("fileRef : "+fileRef.getAppLabel());
					//logger.info("fileRef : "+fileRef.getAppLabel());
					TreeElement forTree = new TreeElement(fileRef);
					// 11/12/2016 - added implementation for mapping file elements to target data model
					/*
					 * moved to visitor
					MappedElements mapElems = new MappedElements();
					// null checks for mapped elements and current tree element object
					if(mapElems != null && forTree != null) {
						// executes the mapping on the file tree elements
						forTree.setElement(mapElems.processElementForMapping(forTree.getElement()));
					}*/
					
					idTreeMap.put(fileRef.getId(), forTree);
					forTree.setTransform(transform);
					cdiTreeElement.addChild(forTree, IConstants.NATIVE_FORMATS.contains(clazz));
					
					logger.debug(String.format("Create JT %s and add it to CDI %s ", fileRef.getId(), cdiTreeElement.getElement().getId()));

					
					//JuOCmFfWusr_wgub34426619
					
					/*if( forTree != null  && cdiTreeElement != null)
					{
						String parentProject = cdiTreeElement.getProject(); 
						if(parentProject == null)
						{
							parentProject = cdiTreeElement.getElement().getUpdatedProjectName();
						}
						String instanceProject = forTree.getElement().getUserValues().get(IConstants.ProjectName);
						if(parentProject != null && instanceProject != null)
						{
							if(PLMUtils.isSecuredProject(instanceProject) || PLMUtils.isCommonProject(instanceProject))
							{
								forTree.getElement().setUpdatedProjectName(instanceProject);
							}
							else 
							{
								if(instanceProject.equals(parentProject))
								{
									String project = cdiTreeElement.getElement().getUpdatedProjectName();
									if( project== null)
									{
										project = instanceProject;
									}
									forTree.getElement().setUpdatedProjectName(project);
								}
								else if(!instanceProject.equals(parentProject))
								{
									if(cdiTreeElement.getElement().getUpdatedProjectName()== null)
									{
										//forTree.getElement().setUpdatedProjectName(instanceProject);
										if(PLMUtils.isSecuredProject(parentProject) || PLMUtils.isCommonProject(parentProject) )
										{
											forTree.getElement().setUpdatedProjectName(instanceProject);
										}
										else if(cdiTreeElement.getProject()!= null)
										{
											forTree.getElement().setUpdatedProjectName(cdiTreeElement.getProject()+"_COP");
										}
										else
										{
											forTree.getElement().setUpdatedProjectName(instanceProject);
										}
									}

									else if(cdiTreeElement.getElement().getUpdatedProjectName().endsWith("_COP"))
									{
										forTree.getElement().setUpdatedProjectName(cdiTreeElement.getElement().getUpdatedProjectName());
									}
									else 
									{
										if(PLMUtils.isSecuredProject(cdiTreeElement.getElement().getUpdatedProjectName()) || PLMUtils.isCommonProject(cdiTreeElement.getElement().getUpdatedProjectName()) )
										{
											forTree.getElement().setUpdatedProjectName(instanceProject);
										}
										else 
										{
											forTree.getElement().setUpdatedProjectName(cdiTreeElement.getElement().getUpdatedProjectName()+"_COP");
										}

									}

								}
							}


						}
						else if(instanceProject == null )
						{
							if(cdiTreeElement.getElement().getUpdatedProjectName()== null)
							{
								if(PLMUtils.isSecuredProject(parentProject) || PLMUtils.isCommonProject(parentProject) )
								{
									forTree.getElement().setUpdatedProjectName(instanceProject);
								}
								else if(cdiTreeElement.getParent().getElement().getUpdatedProjectName()!= null)
								{
									forTree.getElement().setUpdatedProjectName(cdiTreeElement.getParent().getElement().getUpdatedProjectName());
								}
								else if(cdiTreeElement.getProject()!= null)
								{
									forTree.getElement().setUpdatedProjectName(cdiTreeElement.getProject());
								}
							}
							else
							{
								forTree.getElement().setUpdatedProjectName(cdiTreeElement.getElement().getUpdatedProjectName());
							}
						}
					}
					*/

				}
			}
		}
	}

	/**
	 * Adds a cdi element the to a tree element.
	 * 
	 * @param folderType
	 *           The folder type used for adding the cdi.
	 * @param sortedElements
	 *           The sortedElements object.
	 * @param part
	 *           The part of the PLMXML file.
	 * @param nextElement
	 *           The {@link TreeElement} the cdi has to be added to.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	private void addCDIs(final TreeElement.FolderType folderType, final SortedElements sortedElements, final Part part,
			final TreeElement nextElement, final String transform, final String relcount) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// create the model elements
		String partId = part.element.getId();
		logger.debug(String.format("Looking for CDI in : %s", partId));
        
		List<Element> reps = null;
		if (sortedElements.relationRefsPrimary.isEmpty() && sortedElements.relationRefsSecondary.isEmpty() && part.compReps != null) {
			// get it from the part, this is the case when we process a PLMXML file from sendTo
			if (folderType != TreeElement.FolderType.Secondary) {
				// if folder information is not part of the PLMXML only add CDIs for primary or unknown folder types
				boolean cdiCreated = false;
				
				
				for (String compRepId : part.compReps) {
					
					TreeElement[] cdis = createCDI(folderType, sortedElements, nextElement, null, compRepId, null);
					if (!cdiCreated && (cdis[0] != null || cdis[1] != null) || cdis[2] != null) {
						if (cdis[0] != null && cdis[0].getTransform() == null) {
							cdis[0].setTransform(transform);
							//handle relcount for cdi3d objects in sendto package
							if (cdis[0].getRelCount() == null && relcount != null ){
								cdis[0].setRelCount(relcount);
							}
						}
						cdiCreated = true;
					}
				}
				if (!cdiCreated) {
					List<Element> fileList = new ArrayList<Element>();
					for (String compRepId : part.compReps) {
						fileList.add(sortedElements.compoundReps.get(compRepId));
					}
					addFilesToCDI(nextElement, fileList, transform);
				}
			}
		} else {
			List<Element> oo = sortedElements.relationRefsPrimary.get(partId);
			reps = folderType == TreeElement.FolderType.Primary ? sortedElements.relationRefsPrimary.get(partId)
					: sortedElements.relationRefsSecondary.get(partId);
			if (reps != null) {
				for (Element rep : reps) {
					String repId = rep.getAttributes().get(IConstants.relatedRefs).split(" ")[1];
					String folderDefinition = rep.getUserValues().get(IConstants.j0DocumentType);

					createCDI(folderType, sortedElements, nextElement, rep, repId, folderDefinition);
				}
			}
		}
	}

	private TreeElement[] createCDI(final TreeElement.FolderType folderType, final SortedElements sortedElements,
			final TreeElement nextElement, Element rep, String repId, String folderDefinition)
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		
		TreeElement[] ret = new TreeElement[4];

		Element cdi3D = sortedElements.compRepCdi3DMap.get(repId);
		if (cdi3D != null) {
			TreeElement cdi3DTree = new TreeElement(cdi3D, folderType, folderDefinition);
			
			/* Moved to visitor
			 * // 10/12/2016 - added implementation for mapping elements for the cdi elements
			 
			MappedElements mapElems = new MappedElements();
			if (mapElems != null && cdi3DTree != null) {

				cdi3DTree.setElement(mapElems.processElementForMapping(cdi3DTree.getElement()));
			}*/
			
			idTreeMap.put(repId, cdi3DTree);

			if (cdi3DTree != null && nextElement != null) {
				// 19/05/2017 - Amit - set the bomb level to the cdi3D object
				
				nextElement.addChild(cdi3DTree);
				
				logger.debug(String.format("Create CDI3D %s and attach it to part %s", repId, nextElement.getElement().getId()));
				
				/*
				cdi3DTree.setBomLevel(nextElement.getBomLevel() + 1);
				// 19/05/2017 - Amit - adding bom count information to the Singleton member
				// retrieve the item revision ids for the BOM parent and BOM child
				String parItemRevID = null; // item revision id of the parent item revision id
				String childItemRevID = null; // item revision id of the child item revision id
				if (nextElement.getElement() != null && nextElement.getElement().getMappedElementsMap() != null) {
					parItemRevID = WriterUtils.getAttributeValue(nextElement.getElement(),
							IConstants.TC_ATTR_ITEMREVID);
				}

				if (cdi3DTree.getElement() != null && cdi3DTree.getElement().getMappedElementsMap() != null) {
					childItemRevID = WriterUtils.getAttributeValue(cdi3DTree.getElement(),
							IConstants.TC_ATTR_ITEMREVID);
				}

				ReaderSingleton.getReaderSingleton().setBOMParent2ChildMap(nextElement.getOBID(),
						cdi3DTree.getPartNumber(), nextElement.getPartName(), nextElement.getPartNumber(),
						nextElement.getBomLevel(), parItemRevID, childItemRevID);
				*/
			}
			cdi3DTree.setRelatingElement(rep);
			List<Element> fileRefs = sortedElements.equivalantRefs.get(cdi3D.getId());
			if (fileRefs != null) {
				addFilesToCDI(cdi3DTree, fileRefs, null);
			}
			ret[0] = cdi3DTree;

			/*if (cdi3DTree != null && nextElement != null) {
				String parentProject = nextElement.getProject();
				if (parentProject == null) {
					parentProject = nextElement.getElement().getUpdatedProjectName();
				}
				String instanceProject = cdi3DTree.getElement().getUserValues().get(IConstants.ProjectName);

				if (parentProject != null && instanceProject != null) {
					if (PLMUtils.isSecuredProject(instanceProject) || PLMUtils.isCommonProject(instanceProject)) {
						cdi3DTree.getElement().setUpdatedProjectName(instanceProject);
					} else {
						if (instanceProject.equals(parentProject)) {
							if (nextElement.getElement().getUpdatedProjectName() != null
									&& nextElement.getElement().getUpdatedProjectName().contains("_COP")) {
								cdi3DTree.getElement()
										.setUpdatedProjectName(nextElement.getElement().getUpdatedProjectName());
							} else {
								cdi3DTree.getElement().setUpdatedProjectName(instanceProject);
							}
						} else if (!instanceProject.equals(parentProject)) {
							if (nextElement.getElement().getUpdatedProjectName() == null) {
								cdi3DTree.getElement().setUpdatedProjectName(instanceProject);

								if (PLMUtils.isSecuredProject(parentProject)
										|| PLMUtils.isCommonProject(parentProject)) {
									cdi3DTree.getElement().setUpdatedProjectName(instanceProject);
								} else if (nextElement.getProject() != null) {
									cdi3DTree.getElement().setUpdatedProjectName(nextElement.getProject() + "_COP");
								} else {
									cdi3DTree.getElement().setUpdatedProjectName(instanceProject);
								}
							}

							else if (nextElement.getElement().getUpdatedProjectName().endsWith("_COP")) {

								cdi3DTree.getElement()
										.setUpdatedProjectName(nextElement.getElement().getUpdatedProjectName());
							} else {
								if (PLMUtils.isSecuredProject(nextElement.getElement().getUpdatedProjectName())
										|| PLMUtils.isCommonProject(nextElement.getElement().getUpdatedProjectName())) {
									// AMIT - modified for 1.91
									cdi3DTree.getElement().setUpdatedProjectName(instanceProject);
								} else {
									// nextElement.getElement().setUpdatedProjectName(nextElement.getElement().getUpdatedProjectName()+"_COP");
									// AMIT - modified for 1.91
									cdi3DTree.getElement().setUpdatedProjectName(
											nextElement.getElement().getUpdatedProjectName() + "_COP");
								}
							}
						}
					}
				}
			}*/
		}

		Element cdi2D = sortedElements.compRepCdi2DMap.get(repId);
		if (cdi2D != null) {
			TreeElement cdi2DTree = new TreeElement(cdi2D, folderType, folderDefinition);
			idTreeMap.put(repId, cdi2DTree);

			if (cdi2DTree != null && nextElement != null) {
				// 19/05/2017 - Amit - set the bom level to the cdi2D object
				///cdi2DTree.setBomLevel(nextElement.getBomLevel() + 1);
				nextElement.addChild(cdi2DTree);
				
				logger.debug(String.format("Create CDI2D %s and attach it to part %s", repId, nextElement.getElement().getId()));

				/*
				// retrieve the item revision ids for the BOM parent and BOM child
				String parItemRevID = null; // item revision id of the parent item revision id
				String childItemRevID = null; // item revision id of the child item revision id

				if (nextElement.getElement() != null && nextElement.getElement().getMappedElementsMap() != null) {
					parItemRevID = WriterUtils.getAttributeValue(nextElement.getElement(),
							IConstants.TC_ATTR_ITEMREVID);
				}

				if (cdi2DTree.getElement() != null && cdi2DTree.getElement().getMappedElementsMap() != null) {
					childItemRevID = WriterUtils.getAttributeValue(cdi2DTree.getElement(),
							IConstants.TC_ATTR_ITEMREVID);
				}

				// 19/05/2017 - Amit - adding bom count information to the Singleton member
				ReaderSingleton.getReaderSingleton().setBOMParent2ChildMap(nextElement.getOBID(),
						cdi2DTree.getPartNumber(), nextElement.getPartName(), nextElement.getPartNumber(),
						nextElement.getBomLevel(), parItemRevID, childItemRevID);
						*/
			}

			nextElement.addChild(cdi2DTree); //TODO why adding it again?
			addFilesToCDI(cdi2DTree, sortedElements.equivalantRefs.get(cdi2D.getId()), null);
			ret[1] = cdi2DTree;
		}
		;

		Element cdiAss = sortedElements.compRepCdiAssMap.get(repId);
		if (cdiAss != null) {
			TreeElement cdiAssTree = new TreeElement(cdiAss, folderType, folderDefinition);
			idTreeMap.put(repId, cdiAssTree);

			if (cdiAssTree != null && nextElement != null) {
				// 19/05/2017 - Amit - set the bom level to the cdiAss object
				///cdiAssTree.setBomLevel(nextElement.getBomLevel() + 1);
				nextElement.addChild(cdiAssTree);
				
				logger.debug(String.format("Create CDIAss %s and attach it to part %s", repId, nextElement.getElement().getId()));

				/*
				// retrieve the item revision ids for the BOM parent and BOM child
				String parItemRevID = null; // item revision id of the parent item revision id
				String childItemRevID = null; // item revision id of the child item revision id

				if (nextElement.getElement() != null && nextElement.getElement().getMappedElementsMap() != null) {
					parItemRevID = WriterUtils.getAttributeValue(nextElement.getElement(),
							IConstants.TC_ATTR_ITEMREVID);
				}

				if (cdiAssTree.getElement() != null && cdiAssTree.getElement().getMappedElementsMap() != null) {
					childItemRevID = WriterUtils.getAttributeValue(cdiAssTree.getElement(),
							IConstants.TC_ATTR_ITEMREVID);
				}

				// 19/05/2017 - Amit - adding bom count information to the Singleton member
				ReaderSingleton.getReaderSingleton().setBOMParent2ChildMap(nextElement.getOBID(),
						cdiAssTree.getPartNumber(), nextElement.getPartName(), nextElement.getPartNumber(),
						nextElement.getBomLevel(), parItemRevID, childItemRevID);
				*/
			}

			nextElement.addChild(cdiAssTree);  //TODO why adding it again?
			addFilesToCDI(cdiAssTree, sortedElements.equivalantRefs.get(cdiAss.getId()), null);
			ret[2] = cdiAssTree;
		}
		// To handle j0BuaKas & j0VisInt - Wiring harness data
		// 19-05-2017 - Amit the bom count logic has not been implemented for this
		// !!!!!!!
		Element partRefs = sortedElements.relationRefsCompRep.get(repId);
		if (partRefs != null && partRefs.isValidWiringHarness()) {
			TreeElement partRefsTree = new TreeElement(partRefs, folderType, folderDefinition);
			idTreeMap.put(repId, partRefsTree);
			nextElement.addChild(partRefsTree);
			addFilesToCDI(partRefsTree, sortedElements.equivalantRefs.get(partRefs.getId()), null);
			ret[3] = partRefsTree;
		}
		return ret;
	}
	
	protected static void addReporting(TreeElement root) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		IModelVisitor reportVisitor = new ReportVisitor();		
		root.accept(reportVisitor);		
	}


	protected static void addMappingToTC(TreeElement root) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		IModelVisitor mappingVisitor = new MappingVisitor();		
		root.accept(mappingVisitor);	
		
	}
	
	protected static void addProjects(TreeElement root) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		IModelVisitor projectVisitor = new ProjectVisitor();		
		root.accept(projectVisitor);
		
	}
	
	
}
