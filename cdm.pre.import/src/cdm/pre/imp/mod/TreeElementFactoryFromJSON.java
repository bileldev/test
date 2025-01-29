package cdm.pre.imp.mod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.json.reader.JSONTreeElement;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.PLMUtils.SortedElements;

public class TreeElementFactoryFromJSON extends AbstractTreeElementFactoy {
	private final static Logger logger = LogManager.getLogger(TreeElementFactoryFromJSON.class.getName());

	//private static final TreeElementFactoryFromJSON OBJ = new TreeElementFactoryFromJSON();
	
	private JSONTreeElement jsonModel;	
	
	
	/**
	 * 
	 */
	public TreeElementFactoryFromJSON(final SortedElements sortedElements, final JSONTreeElement jsonModel) {
		super(sortedElements);
		this.jsonModel = jsonModel;
	}
	
	 
	/*
	 * public static TreeElementFactoryFromJSON getInstance() { return OBJ; }
	 */
	
	public static List<TreeElement> createStructure(final boolean parseAltHierarchy, boolean isSecXML, JSONTreeElement jsonModel, String endItem) throws CDMException, NoSuchMethodException, 
	SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException {

		//TreeElement dummyRoot = null;
		
		// sets the vehicle type to the current object
		TreeElementFactoryFromPLMXML.vehicleType = PreferenceConstants.P_FZG_TYP_PKW;
		
		List<TreeElement> ret = new ArrayList<TreeElement>();
		
		// single root in json is expected
		TreeElement pRoot =  new ModelConverter().createTreeElement(jsonModel);
		ret.add(pRoot);
		
		// In the PLMXML it is taken from the attributes of PLMXML element. In Json it is missing.
		// that is why it will set it manually.
		pRoot.setLastModifDate(new Date());
		
		// Set endItem
		pRoot.setRefConfigName(endItem);
		
		//TreeElementFactoryFromJSON factory = new TreeElementFactoryFromJSON(sortedElements, jsonModel);
		
		
		
		// TODO : move it to upper class
		/*
		
		if (sortedElements.rootRefs != null) {
			if(sortedElements.rootRefs.length > 1)
			{
				logger.info(" No of Roots in the PLMXML : "+ sortedElements.rootRefs.length);
				System.out.println(" No of Roots in the PLMXML : "+ sortedElements.rootRefs.length);
				dummyRoot = createNewRoot();
				ret.add(dummyRoot);
			}
			for (String root : sortedElements.rootRefs) {
				logger.info("Root : "+root);
				
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
					}
					//ret.add(treeElem);
				}
			}
		}
		*/
		
	/*	if (!ret.isEmpty()) {
			if(endItem != null) {
				ret.get(0).setRefConfigName(endItem);
			}
		}*/
		
		// Alternate hierarchy (n PLMXML Occurence are used instead of instance) won´t be handled in SMA4U
		/*
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
		}*/
		
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
			// addReporting(root);
		}
		
		return ret;
	}

}
