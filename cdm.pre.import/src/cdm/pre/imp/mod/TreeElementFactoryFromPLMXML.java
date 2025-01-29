package cdm.pre.imp.mod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.Handler;
import cdm.pre.imp.reader.Occurrence;
import cdm.pre.imp.reader.PLMUtils;
import cdm.pre.imp.reader.PLMUtils.SortedElements;

/**
 * Builds the tree which is a structure of {@link TreeElement}s. The input for such kind of structure is in general a
 * PLMXML file.
 * 
 * @author dump1
 * 
 */
public class TreeElementFactoryFromPLMXML extends AbstractTreeElementFactoy {
	private final static Logger logger = LogManager.getLogger(TreeElementFactoryFromPLMXML.class.getName());

	private static Handler handler;						// class member that has the parser handler object
	
	public TreeElementFactoryFromPLMXML(final SortedElements sortedElements) {
		super(sortedElements);
	}


	/**
	 * Builds the tree from the parsed PLMXML elements.
	 * 
	 * @param handler
	 *           The handler which was used for parsing.
	 * @return A list of all the root nodes.
	 * @throws CDMException
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */

	// redundant method defintion to ensure compatibility with the GUI code.. must be removed in the GUI integration phase
	public static List<TreeElement> createStructure(final Handler handler, boolean isSecXML) throws CDMException, NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException {
		if(handler != null) {
			TreeElementFactoryFromPLMXML.handler = handler;
		}

		// 06-12-2016 - removed the handler argument and made it as a class member
		return createStructure(true, isSecXML);
	}
	
	/**
	 * Builds the tree from the parsed PLMXML elements.
	 * 
	 * @param handler
	 *           The handler which was used for parsing.
	 * @param parseAltHierarchy
	 *           Turn of alternative hierarchy parsing.
	 * @return A list of all the root nodes.
	 * @throws CDMException
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static List<TreeElement> createStructure(final boolean parseAltHierarchy, boolean isSecXML) throws CDMException, NoSuchMethodException, 
	SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException {

		TreeElement dummyRoot = null;
		// sets the vehicle type to the current object
		if(TreeElementFactoryFromPLMXML.handler.getVehicleType() != null) {
			TreeElementFactoryFromPLMXML.vehicleType = TreeElementFactoryFromPLMXML.handler.getVehicleType();
		}

		SortedElements sortedElements = PLMUtils.getElements(handler.getElements());
		
		// TODO : move it to upper class
		TreeElementFactoryFromPLMXML factory = new TreeElementFactoryFromPLMXML(sortedElements);
		List<TreeElement> ret = new ArrayList<TreeElement>();
		if (sortedElements.rootRefs != null) {
			if(sortedElements.rootRefs.length > 1)
			{
				logger.info(" No of Roots in the PLMXML : "+ sortedElements.rootRefs.length);
				System.out.println(" No of Roots in the PLMXML : "+ sortedElements.rootRefs.length);
				dummyRoot = createNewRoot();
				ret.add(dummyRoot);
			}
			for (String root : sortedElements.rootRefs) {
				logger.info("Handling Root : "+root);
				
				TreeElement treeElem = factory.createGraph(root, null, isSecXML);
				if (treeElem != null) {
					if(treeElem.getClazz().equals("j0SupNum"))
					{
						System.out.println("j0SupNum");
					}
					if(dummyRoot != null)
					{
						dummyRoot.addChild(treeElem);
					}
					else
					{
						ret.add(treeElem);
						logger.debug(String.format("Creating root %s ", treeElem.getElement().getId()));
					}
					//ret.add(treeElem);
				}
			}
		}
		if (!ret.isEmpty()) {
			if(sortedElements.refConfigName != null) {
				ret.get(0).setRefConfigName(sortedElements.refConfigName);
			}
			// loop for the Truck DMU XML file which does not have the <Header> section
			if(TreeElementFactoryFromPLMXML.handler.getVehicleType() != null) {
				if(TreeElementFactoryFromPLMXML.handler.getVehicleType().equals(PreferenceConstants.P_FZG_TYP_LKW)) {
					// retrieve the root node and it's part number 
					if(ret.get(0).getPartNumber() != null) {
						// sets the part number as the reference configuration name
						ret.get(0).setRefConfigName(ret.get(0).getPartNumber());
					}
				}
			}
		}
		if (parseAltHierarchy) {
			if (!sortedElements.occurrences.isEmpty()) {
				Occurrence altHier = sortedElements.occurrences.get(sortedElements.rootOcc);
				List<String> roots = altHier.getOccurrenceRefs();
				for (String root : roots) {
					Occurrence rootOcc = sortedElements.occurrences.get(root);
					TreeElement rootTree = factory.idTreeMap.get(rootOcc.getInstanceRefs().get(0));
					factory.createAltHierachy(rootOcc, rootTree);
				}
			}
		}
		
		// mapping
		logger.debug(String.format("Start visiting Mapping to TC ..."));
		for (TreeElement root : ret) {
			addMappingToTC(root);
		}

		// set project
		logger.debug(String.format("Start visiting Setting project ..."));
		for (TreeElement root : ret) {
			addProjects(root);
		}

		// reporting
		logger.debug(String.format("Start visiting Reporting ..."));
		for (TreeElement root : ret) {
			//addReporting(root);
		}
		
		return ret;
	}

}
