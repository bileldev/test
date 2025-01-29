package cdm.pre.imp.mod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
//import java.util.logging.Logger;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.DateDefinitions;
import cdm.pre.imp.VersionInfo;
import cdm.pre.imp.XMLFileData;
import cdm.pre.imp.batch.BatchException;
import cdm.pre.imp.configmap.ConfigMapUtils;
import cdm.pre.imp.csvreader.RefConfigMappingObject;
import cdm.pre.imp.dbconnector.DBConnBOMInfo;
import cdm.pre.imp.dbconnector.DBConnBroker;
import cdm.pre.imp.dbconnector.DBConnDatasetInfo;
import cdm.pre.imp.dbconnector.DBConnResponse;
import cdm.pre.imp.dbconnector.DBConnSingleton;
import cdm.pre.imp.dbconnector.DBConnUtilities;
import cdm.pre.imp.map.JTFileManagerUtils;
import cdm.pre.imp.map.NXPartFileManagerUtils;
import cdm.pre.imp.map.TypeMaps;
import cdm.pre.imp.mod.decision.DuplicationRuleEvaluator;
import cdm.pre.imp.prefs.BatchPreImpConfig;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.Element;
import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.MappedAttributes;
import cdm.pre.imp.reader.ObjectInfo;
import cdm.pre.imp.reader.PLMUtils;
import cdm.pre.imp.reader.ReaderSingleton;
import cdm.pre.imp.reader.TruckException;
import cdm.pre.imp.variants.VariantsUtil;
import cdm.pre.imp.writer.BaseElement;
import cdm.pre.imp.writer.ICustom;
import cdm.pre.imp.writer.WriterUtils;
import cdm.pre.imp.xml.IndentingXMLStreamWriter;


/**
 * Builds the tree for the UI and defines the element of a tree node:<br>
 * The main purpose of this class is to<br>
 * <ul>
 * <li>Represent a structure node</li>
 * <li>Create initial or delta structure trees.</li>
 * <li>Export the structure tree to a Import XML file.</li>
 * </ul>
 * 
 * @author dump1
 * 
 */
public class TreeElement //Needed  for sendTO: implements IActionFilter 
{
	//private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");
	final private static Logger LOGGER = LogManager.getLogger(BaseElement.class);

	private String              clazz;
	private String              partName;
	private String              revision;
	private String              sequence;
	private String              obid;
	private String              project;
	private String              dynDia;
	private String              hnumber;
	private boolean 			isBomLineHasChanges =false;
	private boolean 			isEffectivityClosed =false;
	private boolean 			isTrafoHasChanges = false;
	private String tagOcc;
	private String tagBV;
	private String tagBVR;
	private String action="none";
	private String occPuid;
	private String codeRule;

	private boolean isDuplicateBOM = false;
	private boolean isBOMProcessed = false;
	/**
	 * Enum in order to identify the changes.
	 * 
	 * @author wikeim
	 * 
	 */
	public enum State {
		Missing, New, Modified
	}

	/**
	 * Enum to classify the Smaragd folder types. The folder categories like
	 * "Bauraumuntersuchung" are not yet managed.
	 * 
	 * @author wikeim
	 * 
	 */
	public enum FolderType {
		Primary, Secondary, Unknown
	}

	public static final double[]    EINHEITS_MATRIX = new double[] { 1., 0., 0., 0., 0., 1., 0., 0., 0., 0., 1., 0., 0., 0., 0., 1. };
	public static final int         CHANGE_NAME     = 1;
	public static final int         CHANGE_TRANS    = 2;
	public static final int         CHANGE_VERSION  = 4;
	public static final int         CHANGE_OBID     = 8;
	public static final int         CHANGE_PROJECT  = 16;
	public static final int         CHANGE_DYNDIA   = 32;
	public static final int         CHANGE_LCS      = 64;
	public static final int         CHANGE_HNUMBER  = 128;
	public static final int         CHANGE_LABEL  = 256;

	private Element                 element;
	private Element                 relatingElement;

	private String                  displayText;
	private String                  partNumber;
	boolean                         sorted;
	final private List<TreeElement> childs          = new ArrayList<TreeElement>();
	final private List<TreeElement> altChilds       = new ArrayList<TreeElement>();
	private TreeElement             parent;
	private State                   state;
	private int                     modType;
	private boolean                 hasChanges;

	// things that come from the relating element
	private double[]                transform;
	private int                     relCount        = 0;

	// ...........................................
	private TreeElement             modElement;
	private boolean                 filterMatch;

	private String                  refConfigName;
	private Date                    lastModifDate;
	// life cycle ..........................
	private String                  lcState;
	private String                  engDate;
	private String                  relDate;

	// .....................................
	private FolderType              folderType;
	private String                  folderDefinition;
	private int 					   maxElemNmb;

	private int 						bomLevel; 				// member variable to capture the BOM levels of the BCS elements in a truck structure
	private String endItem;
	private DBConnBroker connBrokerObj;
	private static Map<String, DBConnResponse> childLineDBResMap = new HashMap<>();
	private static Map<String, DBConnDatasetInfo> jtObjectMap = new HashMap<>(); 			// Map for storing JT information ( jtObjectMap<obid,DBConnDatasetInfo> )
	//private static Map<String, Element> jt3dModelMap = new HashMap<>(); 			// Map for storing Associated Modelfor JT information ( jt3dModelMap<refID,Element> )

	private String 					label;

	private boolean isClone = false;

	void accept(IModelVisitor visitor) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		visitor.visit(this);
	}

	/**
	 * setter for the obid member variable 
	 * @param obid : input value passed by the calling method
	 */
	public void setOBID(String obid) {
		this.obid = obid;
	}

	/**
	 * getter for the member variable
	 * @return : value of the bom level member variable
	 */
	public int getBomLevel() {
		return bomLevel;
	}

	/**
	 * setter for the member variable
	 * @param bomLevel : input value for the bom level member
	 */
	public void setBomLevel(int bomLevel) {
		this.bomLevel = bomLevel;
	}

	public void setLastModifDate(Date lastModifDate) {
		this.lastModifDate = lastModifDate;
	}

	public boolean hasAltHier() {
		return !altChilds.isEmpty();
	}

	public List<TreeElement> getAltChilds() {
		return altChilds;
	}

	public Date getLastModifDate() {
		return lastModifDate;
	}

	public String getRefConfigName() {
		return refConfigName;
	}

	public void setRefConfigName(String refConfigName) {
		this.refConfigName = refConfigName;
	}

	public Element getRelatingElement() {
		return relatingElement;
	}

	public String getRelCount() {
		if (relCount < 1) {
			return null;
		}
		return Integer.toString(relCount);
	}

	/**
	 * Used to set the relating element, which is used when computing the
	 * differences.
	 * 
	 * @param relatingElement
	 *           The relating element.
	 */
	public void setRelatingElement(Element relatingElement) {
		if (relatingElement != null) {
			this.relatingElement = relatingElement;
			String trans = relatingElement.getTransform();
			if (trans != null) {
				setTransform(trans);
			}
			if (relatingElement.getUserValues() != null) {
				String relCount = relatingElement.getUserValues().get(IConstants.j0RelCount);
				if (relCount != null) {
					try {
						this.relCount = Integer.parseInt(relCount);
					} catch (NumberFormatException ignore) {
						// can be ignored
					}
				}
			}
		}
	}

	public boolean isHasChanges() {
		return hasChanges;
	}

	public TreeElement getParent() {
		return parent;
	}

	public boolean isFilterMatch() {
		return filterMatch;
	}

	public void setFilterMatch(boolean filterMatch) {
		this.filterMatch = filterMatch;
	}

	public void clearFilterMatch() {
		filterMatch = false;
		for (TreeElement child : getChilds()) {
			child.clearFilterMatch();
		}
	}

	public State getState() {
		return state;
	}

	public boolean hasChanges() {
		return hasChanges;
	}

	/* public String getOBID() {
      return obid;
   }*/

	public String getOBID() {
		obid = WriterUtils.getAttributeValue(element, "id");

		return obid;
	}

	public String getProject() {
		return project;
	}

	public void setTransform(String trans) {
		if (trans != null) {
			trans = trans.replaceAll("\\s+", " ").replaceAll("(\r|\n)", "").trim();
			transform = new double[16];
			String[] sValues = trans.split(" ");
			for (int i = 0; i < sValues.length; i++) {
				transform[i] = Double.parseDouble(sValues[i]);
			}
		} else {
			transform = null;
		}
	}

	public double[] getTransform() {
		return transform;
	}

	public void setTransform(double[] transform) {
		this.transform = transform;
	}

	private void markParentForChanges() {
		if (state == null) {
			hasChanges = true;
		}
		if (parent != null) {
			parent.markParentForChanges();
		}
	}

	public boolean isFirstPart() {
		boolean ret = false;
		if (parent != null) {
			String parClazz = parent.getClazz();
			ret = IConstants.j0SDLage.equals(parClazz) || IConstants.j0SDPosV.equals(parClazz);
		}
		return ret;
	}

	private void setState(State state) {
		this.state = state;
		if (parent != null)
			markParentForChanges();
	}

	//setting RelCount for sendto PLMXML cdi3d objects
	public void setRelCount(String relcount){
		this.relCount = Integer.parseInt(relcount);
	}

	/**
	 * Special clone method to create a copy of the TreeElement if a new tree
	 * structure is created because of a delta calculation.
	 * 
	 * @return The cloned TreeElement object.
	 */
	protected TreeElement copy() {
		TreeElement ret = new TreeElement();
		ret.displayText = displayText;
		ret.element = element;
		ret.relatingElement = relatingElement;
		ret.clazz = clazz;
		ret.partNumber = partNumber;
		ret.relCount = relCount;
		ret.revision = revision;
		ret.sequence = sequence;
		ret.partName = partName;
		ret.obid = obid;
		ret.project = project;
		ret.dynDia = dynDia;
		ret.hnumber = hnumber;
		ret.lcState = lcState;
		ret.engDate = engDate;
		ret.relDate = relDate;
		ret.folderType = folderType;
		ret.folderDefinition = folderDefinition;
		ret.refConfigName = refConfigName;
		if (transform != null) {
			ret.transform = new double[transform.length];
			for (int i = 0; i < transform.length; i++) {
				ret.transform[i] = transform[i];
			}
		}
		ret.label = label;
		return ret;
	}

	private void copyStructure(TreeElement struct) {
		for (TreeElement child : struct.getChilds()) {
			TreeElement cp = child.copy();
			cp.state = state;
			addChild(cp);
			cp.copyStructure(child);
		}
	}

	public FolderType getFolderType() {
		return folderType;
	}

	public String getFolderDefinition() {
		return folderDefinition;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public String getClazz() {
		return clazz;
	}

	public String getPartName() {
		return partName;
	}

	public String getRevision() {
		return revision;
	}

	public String getSequence() {
		return sequence;
	}

	private TreeElement() {
	}

	public TreeElement(Element element) {
		this(element, null, null);
	}

	public TreeElement(Element element, FolderType folderType, String folderDefinition) {
		this.element = element;
		this.folderType = folderType;
		this.folderDefinition = folderDefinition;
		Map<String, String> userValues = element.getUserValues();
		clazz = element.getClazz();

		
		if (clazz == null) {
			LOGGER.trace("class is not defined for element ");
		}


		if (IConstants.j0Cdi3D.equals(clazz) || IConstants.j0Cdi2D.equals(clazz)) {
			String modSnr = userValues.get(IConstants.j0CTModSnr);
			String modNmb = userValues.get(IConstants.j0CTModNumber);
			if (modSnr != null && modNmb != null) {
				partNumber = userValues.get(IConstants.j0CTModSnr) + "_" + userValues.get(IConstants.j0CTModNumber);
			} else {
				partNumber = userValues.get(IConstants.LDisplayedName);
			}
			if (element.getTransform() != null) {
				setTransform(element.getTransform());
			}
			displayText = partNumber;
		} else if (PLMUtils.isFileRef(clazz)) {
			displayText = userValues.get(IConstants.DataItemDesc);
			if (displayText == null) {
				String name = element.getAttributes().get("name");
				if (name != null) {
					int pos = name.indexOf(',');
					if (pos > -1) {
						name = name.substring(0, pos);
					}
					displayText = name;
				}
			}
			//  partNumber = element.getOBID();
			partNumber =  WriterUtils.getAttributeValue(element, "id");
		} else if (clazz.startsWith("j0Alp")) {
			displayText = userValues.get(IConstants.Nomenclature);
			partNumber = userValues.get(IConstants.PartNumber);
		} else {
			String dynDiaNmb = userValues.get(IConstants.j0DynDiaNumber);
			dynDia = dynDiaNmb;
			displayText = dynDiaNmb != null ? userValues.get(IConstants.j0DynDiaNumber) : userValues.get(IConstants.PartNumber);
			partNumber = userValues.get(IConstants.PartNumber);
		}


		if (displayText == null) {
			// try LDisplayedName
			displayText = userValues.get(IConstants.LDisplayedName);
			if (displayText == null) {
				displayText = element.getAttributes().get("name");
			}

			if (displayText == null) {
				LOGGER.error("DisplayText is not available for element: " + element.getId());
			}
		}
		revision = userValues.get(IConstants.Revision);
		sequence = userValues.get(IConstants.Sequence);
		if (revision == null && sequence == null) {
			String j0RevSeq = userValues.get(IConstants.j0RevSeq);
			if (j0RevSeq != null) {
				int pos = j0RevSeq.indexOf('.');
				if (pos > -1) {
					revision = j0RevSeq.substring(0, pos);
					sequence = j0RevSeq.substring(pos + 1);
				} else {
					sequence = j0RevSeq.trim();
				}
			}
		}
		partName = userValues.get(IConstants.Nomenclature);
		if (partName == null) {
			partName = userValues.get(IConstants.j0Nomenclature);
		}
		project = userValues.get(IConstants.ProjectName);
		lcState = userValues.get(IConstants.LifeCycleState);
		engDate = userValues.get(IConstants.j0EngDateEffectiveFrom);
		relDate = userValues.get(IConstants.j0RelDateEffectiveFrom);
		hnumber = userValues.get(IConstants.j0HNumber);

		label = getElement().getAppLabel();

		// this method call is changed to calculate the obid value from the WriterUtils call
		//obid = element.getOBID();

		//obid = WriterUtils.getAttributeValue(element, "id");

		//if(clazz)

	}

	public void addChild(final TreeElement child) {
		addChild(child, false);
	}

	public void addChild(final TreeElement child, final boolean beginning) {
		if (beginning) {
			childs.add(0, child);
		} else {
			childs.add(child);
		}
		child.parent = this;
		if (child.element.getParentProjectName() == null) {
			child.element.setParentProjectName(element.getUserValues().get(IConstants.ProjectName));
		}
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setElement(Element elemObj) {
		this.element = elemObj;
	}

	public Element getElement() {
		return element;
	}

	public String getDynDia() {
		return dynDia;
	}

	public String getHnumber() {
		return hnumber;
	}

	/**
	 * Returns the children of the current tree. This method should be used
	 * instead of accessing the {@link TreeElement#childs} directly because it
	 * returns the children sorted based on the {@link TreeElement#relCount}
	 * attribute.
	 * 
	 * @return The sorted list of the tree element's children.
	 */
	public List<TreeElement> getChilds() {
		List<TreeElement> ret = childs;

		if (!sorted) {
			Collections.sort(ret, new Comparator<TreeElement>() {

				@Override
				public int compare(TreeElement o1, TreeElement o2) {
					int ret = o1.displayText.compareTo(o2.displayText);
					if (ret == 0) {
						ret = new Integer(o1.relCount).compareTo(o2.relCount);
					}
					return ret;
				}
			});
			sorted = true;
		}

		return ret;
	}

	public boolean isOccurrence() {
		return !PLMUtils.isFileRef(element.getClazz());
	}

	/**
	 * Compares two structures.
	 * 
	 * @param elems1
	 *           The main structure.
	 * @param elems2
	 *           The structure to which the main structure should be compared to.
	 * @param treeElement
	 *           The root element of the structure which contains the comparison
	 *           result. If this is <code>null</code> <code>list</code> must be
	 *           not <code>null</code>.
	 * @param list
	 *           A list of root elements if the root elements of
	 *           <code>elem1</code> and <code>elem2</code> are different.
	 */
	private static void compareStruct(final List<TreeElement> elems1, List<TreeElement> elems2, TreeElement treeElement,
			List<TreeElement> list) {
		Set<TreeElement> elem1Matched = new HashSet<TreeElement>();
		Set<TreeElement> elem2Matched = new HashSet<TreeElement>();
		if( treeElement != null)
		{
			String o2OBID = WriterUtils.getAttributeValue(treeElement.getElement(), "id");
			if(o2OBID != null && o2OBID.equals("99999013_DUMMY"))
			{
				//System.out.println("compareStruct : OBID : "+o2OBID);
				LOGGER.info("compareStruct : OBID : "+o2OBID);
			}

		}


		for (TreeElement elem1 : elems1) {
			if (!elem1Matched.contains(elem1)) {
				for (TreeElement elem2 : elems2) {
					if (!elem2Matched.contains(elem2)) {
						TreeElement comp = elem1.compareStruct(elem2);
						if (comp != null) {

							if (treeElement != null) { 
								treeElement.addChild(comp);
								if (comp.hasChanges()) {
									// treeElement.hasChanges = true;
									treeElement.markParentForChanges();
								} else if (comp.getState() != null) {
									comp.markParentForChanges();
								}
							} else {
								list.add(comp);
							}
							elem1Matched.add(elem1);
							elem2Matched.add(elem2);
							break;
						}
					}
				}
			}
		}

		if (elem1Matched.size() < elems1.size()) {
			for (TreeElement elem1 : elems1) {
				if (!elem1Matched.contains(elem1)) {
					TreeElement missElem = elem1.copy();
					if (treeElement != null) {
						treeElement.addChild(missElem);
					} else {
						list.add(missElem);
					}
					missElem.setState(State.New);
					missElem.copyStructure(elem1);
				}

			}
		}

		if (elem2Matched.size() < elems2.size()) {
			for (TreeElement elem2 : elems2) {
				if (!elem2Matched.contains(elem2)) {
					TreeElement missElem = elem2.copy();
					if (treeElement != null) {
						treeElement.addChild(missElem);
					} else {
						list.add(missElem);
					}
					missElem.setState(State.Missing);
					missElem.copyStructure(elem2);
				}
			}
		}
	}

	protected TreeElement compareStruct(TreeElement treeElement) {	   
		TreeElement ret = compareElement(treeElement);
		if (ret != null) {
			final List<TreeElement> childs1 = getChilds();
			final List<TreeElement> childs2 = treeElement.getChilds();
			compareStruct(childs1, childs2, ret, null);
		}

		return ret;
	}

	public static List<TreeElement> compareRoots(List<TreeElement> roots1, List<TreeElement> roots2) {
		List<TreeElement> ret = new ArrayList<TreeElement>();
		compareStruct(roots1, roots2, null, ret);
		return ret;
	}

	private boolean attrComp(String a1, String a2) {
		if (a1 == null && a2 != null) {
			return false;
		}

		if (a1 == null && a2 == null) {
			return true;
		}
		return a1.equals(a2);
	}

	/**
	 * Compares the tree element with another tree element.
	 * 
	 * @param o2
	 *           The tree element to compare with.
	 * @return A tree element that is a copy of <code>this</code> with additional
	 *         comparison information.
	 */
	private TreeElement compareElement(TreeElement o2) {
		TreeElement ret = null;


		/*if(clazz != null && clazz.equals("j0Cdi3D"))
		{
			System.out.println("C9Model Element..");
		}*/
		//	String o2OBID = WriterUtils.getAttributeValue(o2.getElement(), "id");
		/*if( o2OBID != null && o2OBID.equals("usr_wgub0000000024C001C6"))
		{
			System.out.println("usr_wgub0000000024C001C6");
		}
		if(partNumber != null && partNumber.equals("A0009907483"))
		{
			System.out.println("compareElement :: partNumber :: "+partNumber);
		}*/
		if (partNumber != null && clazz != null && clazz.equals(o2.clazz) && partNumber.equals(o2.partNumber)) {
			// if (relCount != o2.relCount) {
			// return ret;
			// }
			// same element
			ret = copy();
			if (!attrComp(sequence, o2.getSequence())) {
				ret.modType |= CHANGE_VERSION;
			}

			if (!attrComp(revision, o2.getRevision())) {
				ret.modType |= CHANGE_VERSION;
			}

			if ((ret.modType & CHANGE_VERSION) == 0
					&& (!attrComp(lcState, o2.lcState) || !attrComp(engDate, o2.engDate) || !attrComp(relDate, o2.relDate))) {
				ret.modType |= CHANGE_LCS;
			}

			if (!attrComp(partName, o2.getPartName())) {
				ret.modType |= CHANGE_NAME;
			}

			if (!attrComp(project, o2.getProject())) {
				ret.modType |= CHANGE_PROJECT;
			}

			if (!attrComp(obid, o2.getOBID())) {
				ret.modType |= CHANGE_OBID;
			}

			if (!attrComp(dynDia, o2.getDynDia())) {
				ret.modType |= CHANGE_DYNDIA;
			}
			if (!attrComp(hnumber, o2.getHnumber())) {
				ret.modType |= CHANGE_HNUMBER;
			}

			if (transform != null && o2.getTransform() != null) {
				for (int i = 0; i < transform.length; i++) {
					if (!compareDouble(transform[i], o2.getTransform()[i])) {
						ret.modType |= CHANGE_TRANS;
						break;
					}
				}
			} else if (transform == null && o2.getTransform() != null || transform != null && o2.getTransform() == null) {
				ret.modType |= CHANGE_TRANS;
			}
			if (ret.modType != 0) {
				ret.setState(State.Modified);
				ret.modElement = o2;
			}

		}
		else if(clazz != null && clazz.equals("JT"))
		{
			//System.out.println("Part Number : "+partNumber);



			ret = copy();
			if (!attrComp(label, o2.getElement().getAppLabel())) {
				ret.modType |= CHANGE_LABEL;
			}

			if (ret.modType != 0) {
				ret.setState(State.Modified);
				ret.modElement = o2;
			}
		}
		/*{
			System.out.println("Type : "+o2.getClazz());
		}*/

		return ret;
	}

	private static DecimalFormat df = new DecimalFormat("#.#######");

	boolean compareDouble(double v1, double v2) {
		String sv1 = df.format(v1);
		String sv2 = df.format(v2);
		return sv1.equals(sv2);
	}

	/**
	 * Returns the transformation matrix stored as double array as a
	 * {@link String}.
	 * 
	 * @param trans
	 *           The matrix to be converted.
	 * @return The matrix values concatenated with blanks as a single
	 *         {@link String} value.
	 */
	private String createTransText(double[] trans) {
		StringBuilder ret = new StringBuilder();
		if (trans != null) {
			for (double d : trans) {
				if (ret.length() > 0) {
					ret.append(" ");
				}
				ret.append(df.format(d));
			}
		}
		return ret.toString();
	}

	private static String getLCS(final TreeElement elem) {
		if (elem.lcState != null) {
			return IConstants.j0LcsEng.equals(elem.lcState) ? "Engineering" : "Released";
		}
		if (elem.relDate != null) {
			return "Released";
		} else if (elem.engDate != null) {
			return "Engineering";
		}
		return "";
	}

	public String getModificationText() {
		StringBuilder ret = new StringBuilder();
		if ((modType & CHANGE_NAME) != 0) {
			ret.append("Partname:" + partName + " - " + modElement.getPartName());
		}

		if ((modType & CHANGE_VERSION) != 0) {
			if (ret.length() > 0) {
				ret.append("\n");
			}
			ret.append("Version: " + revision + "." + sequence + " - " + modElement.getRevision() + "." + modElement.getSequence());
		}

		if ((modType & CHANGE_PROJECT) != 0) {
			if (ret.length() > 0) {
				ret.append("\n");
			}
			ret.append("Project: " + project + " - " + modElement.project);
		}
		if ((modType & CHANGE_TRANS) != 0) {
			if (ret.length() > 0) {
				ret.append("\n");
			}
			ret.append("Trans.: " + createTransText(transform)).append("\n").append(createTransText(modElement.getTransform()));
		}

		if ((modType & CHANGE_DYNDIA) != 0) {
			if (ret.length() > 0) {
				ret.append("\n");
			}
			ret.append("DynDia: " + dynDia + " - " + modElement.getDynDia());
		}

		if ((modType & CHANGE_HNUMBER) != 0) {
			if (ret.length() > 0) {
				ret.append("\n");
			}
			ret.append("HNumber: " + hnumber + " - " + modElement.getHnumber());
		}

		if ((modType & CHANGE_LCS) != 0) {
			if (ret.length() > 0) {
				ret.append("\n");
			}

			ret.append("LCS: " + getLCS(this) + " - " + getLCS(modElement));
		}
		return ret.toString();
	}

	//@Override
	public boolean testAttribute(Object target, String name, String value) {
		boolean ret = false;
		if ("hasChanges".equals(name)) {
			ret = Boolean.toString(hasChanges).equals(value);
		} else if ("isFilterMatch".equals(name)) {
			ret = Boolean.toString(filterMatch).equals(value);
		}
		return ret;
	}

	public int getModType() {
		return modType;
	}

	/**
	 * This method is used to decide if the element's parent allows changes
	 * without the need of having a new version.<br>
	 * This can be:
	 * <ul>
	 * <li>Certain SmaDia2 elements:</li>
	 * <li>Because some of these elements are managed by variants or the GeoPos
	 * marker is effectivity controlled.</li>
	 * <li>CDI elements.</li>
	 * <li>The creation of TIFF or JTs can happen later in Smaragd because of
	 * certain batch operation and may not to be executed during check in.</li>
	 * 
	 * @return <code>true</code> if the parent's content can change without
	 *         creating a new revision.
	 */
	private boolean checkParentForVariantCfg() {
		boolean ret = false;
		if (parent != null) {
			String parClazz = parent.getClazz();
			ret = TypeMaps.isTypeForOccEff(parClazz) || IConstants.j0Cdi3D.equals(parClazz) || IConstants.j0Cdi2D.equals(parClazz);
		}
		return ret;
	}

	/**
	 * Creates an {@link CDMException} for a {@link TreeElement} object which is
	 * missing when exporting the relations. The method puts the details of the
	 * tree element details into the exception.
	 * 
	 * @param treeElem
	 *           The {@link TreeElement} this exception has to be created for.
	 * @return The exception.
	 */
	private CDMException createMissingElementEx(TreeElement treeElem) {
		StringBuilder sb = new StringBuilder();
		sb.append("No object was defined for the OBID ").append(treeElem.getOBID());
		sb.append("\nDetails: ").append(treeElem.getDisplayText());
		return new CDMException(sb.toString());
	}

	/**
	 * Write some basic file information to the Import XML file for debugging
	 * purposes.
	 * 
	 * @param streamWriter
	 *           The {@link XMLStreamWriter} implementation.
	 * @param file
	 *           The {@link java.io.File} object.
	 * @param filename
	 *           The name of the file used by the application.
	 * @throws XMLStreamException
	 */
	private void writeFileInfo(final XMLStreamWriter streamWriter, final File file, final String filename) throws XMLStreamException {
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

	public void writeItemXMLFile(final XMLFileData xmlFileData, final IProgFeedback feedback) throws IOException, XMLStreamException,
	CDMException {
		writeItemXMLFile(xmlFileData, feedback, false);
	}

	private String getStopClass() {
		String ret = null;
		String stopClass = PreImpConfig.getPlainInstance().getStopClass();
		if (stopClass != null && !"".equals(stopClass)) {
			ret = stopClass;
		}
		return ret;
	}

	/**
	 * This method creates a XML File which is consumed by the cdm.tc.import.prod
	 * tool. The format of the XML File is:
	 * 
	 * <pre>
	 * &lt;xml version=&quot;1.0&quot encoding=&quot;utf-8&quot;&gt;
	 * &lt;import version&quot;tool-version&quot;&gt;
	 * &lt;settings ... preferences used to generate the XML File ... &gt;
	 * &lt;effectivity endItem=&quot;end item name&quot; date=&quot;effectivity date&quot;/&gt;
	 * &lt;plmxmlFile file=&quot;PLMXML1&quot; absolutePath=&quot;the absolute file name of the PLMXML file&quot; fileSize=&quot;the file size&quot; modifDate=&quot;the modification date&quot;&gt;&lt;/plmxmlFile&gt;
	 * &lt;projects main=&quot;the root nodes Smaragd project&quot;&gt;
	 *   &lt;P name=&quot;project name of object with is part of the structure&quot; secProject=&quot;if it is a secured project&quot;/&gt;
	 *   ...
	 * &lt;/projects&gt;
	 * &lt;elements&gt;
	 * this describes an ItemRevision
	 * &lt;e id=&quot;Smaragd OBID&quot; elemNmb=&quot;unique internal number&quot; smaProject=&quot;Smaragd project name&quot; cdmProject=&quot;TcUA project name&quot; tcType=&quot;TcUA foundation type&quot; cdmType=&quot;CDM type&quot; smaType=&quot;Smaragd type&quot; ae=&quot;if a revision effectivity has to be applied&quot; hc=&quot;if there are any modification of the object&quot;&gt;
	 *  &lt;p n=&quot;CDM name of the attribubte&quot; v=&quot;attribute value&quot; t=&quot;attribute type&quot; s=&quot;storage location&quot;/&gt;
	 * &lt;/e&gt;
	 * ...
	 * &lt;/elements&gt;
	 * &lt;relations&gt;
	 * this describes a relation (PSBOMViewRevision)
	 * &lt;r id=&quot;Smaragd OBID of the parent&quot; elemNmb=&quot;internal number&quot; ae=&quot;if an occurrence effectivity has to be applied&quot; dsRel=&quot;if it is a dataset relation&quot;&gt;
	 *  this describes a child
	 *  &lt;c id=&quot;Smaragd OBID of the child&quot; relCount=&quot;the Smaragd relation count&quot;&gt;
	 *    the transformation matrix which needs to be added when relating the elements
	 *    &lt;t v0=&quot;1.0&quot; v1=&quot;0.0&quot; v2=&quot;0.0&quot; v3=&quot;0.0&quot; v4=&quot;0.0&quot; v5=&quot;1.0&quot; v6=&quot;0.0&quot; v7=&quot;0.0&quot; v8=&quot;0.0&quot; v9=&quot;0.0&quot; v10=&quot;1.0&quot; v11=&quot;0.0&quot; v12=&quot;0.0&quot; v13=&quot;0.0&quot; v14=&quot;0.0&quot; v15=&quot;1.0&quot;/&gt;
	 *    &lt;fl t=&quot;If it is a dataset relation this element give info of the type of folder the dataset is attached to in Smaragd&quot;/&gt;
	 *  &lt;/c&gt;
	 *  ...
	 * &lt;/relations&gt;
	 * </pre>
	 * 
	 * @param xmlFileData
	 *           The object that holds the information about the input files.
	 * @param feedback
	 *           Callback for the progress indicator.
	 * @param batchMode
	 *           If <code>true</code> XML file creation is run in batch mode.
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws CDMException
	 * @throws TruckException
	 */
	@SuppressWarnings("unchecked")
	public void writeItemXMLFile(final XMLFileData xmlFileData, final IProgFeedback feedback, boolean batchMode) throws IOException,
	XMLStreamException, CDMException, BatchException {
		endItem = xmlFileData.getEndItem();
		Set<String> unchanged = new HashSet<String>();
		walkElements(unchanged);

		List<Element> elements = new ArrayList<Element>();
		List<TreeElement> relations = new ArrayList<TreeElement>();
		Set<String> uniqueElems = new HashSet<String>();
		Set<String> uniqueRels = new HashSet<String>();
		childLineDBResMap = new HashMap<String, DBConnResponse>();
		jtObjectMap = new HashMap<String, DBConnDatasetInfo>(); 

		// long t = System.currentTimeMillis();
		getElementsForXML(elements, relations, uniqueElems, uniqueRels, xmlFileData.isDelta());


		// parse the versions of the CDI elements and find the latest one
		final int maxCounter = elements.size() + relations.size();
		if (!batchMode) {
			feedback.beginTask("Writing XML", maxCounter);
		}

		LOGGER.info("******************** Writing Intermediate XML.... *****************************");
		XMLStreamWriter streamWriter = null;
		FileOutputStream fo = null;
		FileOutputStream foLst = null;
		BufferedWriter lstWriter = null;
		final ElementVersionMap versMap = new ElementVersionMap(elements, true);
		try {
			fo = new FileOutputStream(xmlFileData.getFileloc());
			if (PreImpConfig.getInstance().isRefNameIncluded()) {
				foLst = new FileOutputStream(xmlFileData.getFileloc().replace(".xml", "") + ".lst");
				lstWriter = new BufferedWriter(new OutputStreamWriter(foLst, "UTF-8"));
			}

			streamWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(fo, "UTF-8"));
			streamWriter.writeStartDocument("UTF-8", "1.0");

			streamWriter.writeStartElement("import");
			String version = VersionInfo.getVersionNumber();
			if(version == null)
			{
				version = "";
				LOGGER.warn("version number is not available. Please define Version.properties");

			}

			streamWriter.writeAttribute("version", version);
			String[] manifestnumber = VersionInfo.getManifestVersionNumber();

			if (manifestnumber != null)
				streamWriter.writeAttribute("build", manifestnumber[0]);

			// 6/6/2017 - Amit - added the check so that different methods are called based on the batch mode value
			if(batchMode == false) {
				PreImpConfig.getInstance().writeSettings(streamWriter);
			}
			else if(batchMode == true) {
				BatchPreImpConfig.getInstance().writeSettings(streamWriter);
			}
			streamWriter.writeEmptyElement("effectivity");
			endItem = xmlFileData.getEndItem();
			if(endItem == null)
			{
				endItem = "";
				LOGGER.warn("endItem is not available. Please validate the  PLMXML file");
			}
			else
			{
				LOGGER.info("End Item : "+ endItem);
			}
			streamWriter.writeAttribute("endItem", endItem);
			String lastModifiedDateStr = null; 

			String preInputMode = ReaderSingleton.getReaderSingleton().getInputMode();
			/*if (preInputMode != null && preInputMode.equals(PreferenceConstants.P_MODE_SENDTO_WITHOUT_REF_CONFIG)) 
			{
				lastModifiedDateStr = "0000/00/00/00/00/00";
			}
			else
			{
				lastModifiedDateStr = DateDefinitions.SDF.format(lastModifDate);
			}*/
			lastModifiedDateStr = DateDefinitions.SDF.format(lastModifDate);
			System.out.println("Last Modified Date : "+lastModifiedDateStr);

			//String values = ReaderSingleton.getReaderSingleton().getGlobalConstValue("CHILD_BLACK_LIST_PROJECTS");
			//System.out.println("CHILD_BLACK_LIST_PROJECTS : "+values);
			streamWriter.writeAttribute("date", lastModifiedDateStr);
			LOGGER.info("Last Modified Date  : "+ lastModifiedDateStr);
			//	09/12/2016 Amit- modifications to the write of the effectivity element to include a list of effectivities in case of a DMU truck xml input file
			if(ReaderSingleton.getReaderSingleton().getFzgTypeName() != null) {
				// checks if the pre-importer is running for a truck dmu xml file or not
				if(ReaderSingleton.getReaderSingleton().getFzgTypeName().equals(PreferenceConstants.P_FZG_TYP_LKW)) {
					// null check for the member map that holds the list of effectivity ids that need to be created for the truck structure
					if(ReaderSingleton.getReaderSingleton().getPart2EffIDMap() != null) {
						// iterate through the map and print the required information to the intermediate xml file
						for(Map.Entry<String, String> mapEntry : ReaderSingleton.getReaderSingleton().getPart2EffIDMap().entrySet()) {
							streamWriter.writeEmptyElement("effectivity");

							//-- Krishna -- Temorary fix for removing endItem Attribute  --------------------------------------------- commenting the below line
							streamWriter.writeAttribute(IConstants.INTXML_ENDITEM_ATTR, mapEntry.getValue());
							streamWriter.writeAttribute(IConstants.INTXML_DATE_ATTR, DateDefinitions.SDF.format(lastModifDate));
						}
					}
				}
			}


			if(ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs() != null) 
			{
				LOGGER.info("Writing Varaint Block in Header Section .........."); 
				// iterate through the map and print the required information to the intermediate xml file
				ArrayList<RefConfigMappingObject> sumRefConfigList = ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs();
				for(int i = 0; i< sumRefConfigList.size();i++)
				{
					streamWriter.writeEmptyElement("variant");
					streamWriter.writeAttribute(IConstants.INTXML_VARIANT_ITEM_ID, sumRefConfigList.get(i).getTopNode());
					streamWriter.writeAttribute(IConstants.INTXML_VARIANT_TYPE, sumRefConfigList.get(i).getOptionName());
					streamWriter.writeAttribute(IConstants.INTXML_VARIANT_VALUE, sumRefConfigList.get(i).getVariant());
					if(sumRefConfigList.get(i).getOptionName() != null)
					{
						boolean removeState = VariantsUtil.getRemoveState(sumRefConfigList.get(i).getOptionName());

						streamWriter.writeAttribute(IConstants.INTXML_VARIANT_REMOVE, Boolean.toString(removeState));
					}

					if(ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM) && sumRefConfigList.get(i).getOptionName().equals("car"))
					{
						String gValue = ReaderSingleton.getReaderSingleton().getGlobalConstValue(VariantsUtil.GLOBAL_VARIABLE_VARIANT_REMOVE_CAR_TYPE);
						if(gValue != null && !gValue.equals(""))
						{
							streamWriter.writeAttribute(IConstants.INTXML_VARIANT_REMOVE_TYPE, gValue);
						}
					}
					else if(sumRefConfigList.get(i).getOptionName().equals("cfg"))
					{
						String gValue = ReaderSingleton.getReaderSingleton().getGlobalConstValue(VariantsUtil.GLOBAL_VARIABLE_VARIANT_REMOVE_CFG_TYPE);
						if(gValue != null && !gValue.equals(""))
						{
							streamWriter.writeAttribute(IConstants.INTXML_VARIANT_REMOVE_TYPE, gValue);
						}
					}
				}
			}

			streamWriter.writeEmptyElement("root");

			if(xmlFileData.getRoot().partNumber.startsWith("dummy_"))
			{
				streamWriter.writeAttribute("objectid", "");
			}
			else
			{
				streamWriter.writeAttribute("objectid", (xmlFileData.getRoot().partNumber));
			}


			writeFileInfo(streamWriter, xmlFileData.getPlmxmlFile1(), "PLMXML1");
			writeFileInfo(streamWriter, xmlFileData.getPlmxmlFile2(), "PLMXML2");

			LOGGER.info("write the project information .........."); 
			streamWriter.writeStartElement("projects");
			Object[] projs = getProjectList(elements);
			String mainProject = projs[0] != null ? projs[0].toString() : element.getUserValues().get(IConstants.ProjectName);
			if (mainProject != null) {
				streamWriter.writeAttribute("main", PLMUtils.convertProjectName(mainProject));
			}
			for (String proj : (Set<String>) projs[1]) {
				streamWriter.writeEmptyElement("P");
				streamWriter.writeAttribute("name", PLMUtils.convertProjectName(proj));
				streamWriter.writeAttribute("secProject", Boolean.toString(PLMUtils.isSecuredProject(proj)));
			}
			streamWriter.writeEndElement();

			LOGGER.info("write the project information ..........Completed...."); 
			// ........................................

			/** Start : This change is for renaming part file as its item_id **/
			LOGGER.info("Renaming part file as its item_id..");
			//Map<String, Element> j0Elements = new HashMap<String, Element>();
			Map<String, String> partFileaNameMap = new HashMap<String, String>();
			ArrayList<Element> danglingj0nxdraw = new ArrayList<>();
			Map<String, String> jtFileaNameMap = new HashMap<String, String>();
			/**
			 * Start : Handling j0CdiAss part number as it does not have "j0CTModSnr" & "j0CTModNumber"
			 * This map is used in further code while renaming send-to package files
			 */
			Map<String, String> j0CdiAssMap = new HashMap<String, String>();         

			for (TreeElement rel : relations) {
				String currentClass = rel.getClazz();
				TreeElement parent = new TreeElement();
				String partNumber = null;
				if (currentClass.contains(IConstants.j0CdiAss)) {
					parent = rel.getParent();
					if (parent != null /* && parent.getClazz().equals(IConstants.j0PrtVer) */) {
						partNumber = parent.getPartNumber();
						if(partNumber !=null){
							j0CdiAssMap.put(rel.obid, partNumber);
						}
					}
				}        	 
			}
			/**
			 * End : Handling j0CdiAss part number as it does not have "j0CTModSnr" & "j0CTModNumber"
			 */
			// checks for the pre-population of the Singleton map
			if(ReaderSingleton.getReaderSingleton().getId2ElemObjMap() != null) {
				for (Element elem : elements) 
				{

					//logger.info("pre-population - App Label :: "+elem.getAppLabel());

					Map<String, String> elemAttributes = elem.getAttributes();
					Map<String, String> elemUserValues = elem.getUserValues();
					if (elemAttributes.containsKey("location") && elemAttributes.get("location") == null) {
						LOGGER.error("location is not defined for element {} !", elem.getId());
					}

					if (elemAttributes.containsKey("location") && elemAttributes.get("location").toString().contains(".prt")) {
						Element refElem = ReaderSingleton.getReaderSingleton().getId2ElemObjMap().get(elemAttributes.get("equivalentRef"));
						if (refElem!=null && refElem.getUserValues().containsKey(IConstants.j0CTModNumber)) {

							String item_id = refElem.getUserValues().get(IConstants.j0CTModSnr) + "_"
									+ refElem.getUserValues().get(IConstants.j0CTModNumber);
							if ((item_id != null) && 
									(elemUserValues.containsKey("Class") 
											&& elemUserValues.get("Class").toString().equalsIgnoreCase("j0NXDrw"))){
								partFileaNameMap.put((String) elemAttributes.get("location"), item_id);
								NXPartFileManagerUtils.nxDrwgItemIdMap.put(refElem.getId(), item_id+"_nx");
							}else if (item_id != null){
								partFileaNameMap.put((String) elemAttributes.get("location"), item_id); 
							}

						} else if(refElem!=null){

							String item_id = refElem.getUserValues().get(IConstants.j0CTModSnr);
							if ((item_id != null) && 
									(elemUserValues.containsKey("Class") 
											&& elemUserValues.get("Class").toString().equalsIgnoreCase("j0NXDrw"))){
								partFileaNameMap.put((String) elemAttributes.get("location"), item_id);
								NXPartFileManagerUtils.nxDrwgItemIdMap.put(refElem.getId(), item_id+"_nx");
							}else if (item_id != null){
								partFileaNameMap.put((String) elemAttributes.get("location"), item_id); 
							}
							else {
								if(j0CdiAssMap.get(WriterUtils.getAttributeValue(refElem, "id")) != null){
									partFileaNameMap.put((String) elemAttributes.get("location"), j0CdiAssMap.get(WriterUtils.getAttributeValue(refElem, "id")));
								}               
							}

						}else{
							danglingj0nxdraw.add(elem);
							LOGGER.info("Reference Element for NX Element: " + elem.getId() + " is not loaded or null");
						}
					}

					// Adding JT ItemId Into Map
					if (elemAttributes.containsKey("location") && elemAttributes.get("location").toString().contains(".jt")) {
						Element jtElem = ReaderSingleton.getReaderSingleton().getId2ElemObjMap().get(elemAttributes.get("equivalentRef"));

						if (jtElem!=null && jtElem.getUserValues().containsKey(IConstants.j0CTModNumber)) {

							String item_id = jtElem.getUserValues().get(IConstants.j0CTModSnr) + "_"
									+ jtElem.getUserValues().get(IConstants.j0CTModNumber);
							if ((item_id != null) && 
									(elemUserValues.containsKey("Class") 
											&& elemUserValues.get("Class").toString().equalsIgnoreCase("JT"))){
								jtFileaNameMap.put((String) elemAttributes.get("location"), item_id);
							}else if (item_id != null){
								jtFileaNameMap.put((String) elemAttributes.get("location"), item_id); 
							}

						} else if(jtElem!=null){

							String item_id = jtElem.getUserValues().get(IConstants.j0CTModSnr);
							if ((item_id != null) && 
									(elemUserValues.containsKey("Class") 
											&& elemUserValues.get("Class").toString().equalsIgnoreCase("JT"))){
								jtFileaNameMap.put((String) elemAttributes.get("location"), item_id);
							}else if (item_id != null){
								jtFileaNameMap.put((String) elemAttributes.get("location"), item_id); 
							}
							else {
								/* if(j0CdiAssMap.get(jtElem.getOBID()) != null){
	                 		 jtFileaNameMap.put((String) elemAttributes.get("location"), j0CdiAssMap.get(jtElem.getOBID()));*/
								if(j0CdiAssMap.get(WriterUtils.getAttributeValue(jtElem, "id")) != null){
									jtFileaNameMap.put((String) elemAttributes.get("location"), j0CdiAssMap.get(WriterUtils.getAttributeValue(jtElem, "id")));
								}               
							}

						}else{
							danglingj0nxdraw.add(elem);
							LOGGER.info("Reference Element for the JT element: " + elem.getId() + " is not loaded or null");
						}
					}
				}
			}
			else {
				throw new BatchException("Severe Error: No JT or PRT elements are processed. Exiting the Pre-Importer.", true, ConfigMapUtils.LOG_TYPE_ERROR);
			}

			NXPartFileManagerUtils.setPartFileNameMap(partFileaNameMap);
			JTFileManagerUtils.setJTFileNameMap(jtFileaNameMap);
			LOGGER.info("Renaming part file as its item_id.. completed..");
			/** End : This change is for renaming part file as its item_id End **/
			LOGGER.info("Writing elements");
			streamWriter.writeStartElement("elements");
			ReaderSingleton.getReaderSingleton().addOBIDActionLogList("**********************************Element Section***************************************************************");
			// t = System.currentTimeMillis();
			int counter = 1;
			int elemNmb = 1;
			for (Element elem : elements) {
				if (!versMap.isMappedElem(elem)) {

					// 2017-02-23 - Amit: added implementation to check for the presence of mapped attribute information before calling the write to the intermediate xml file
					if(elem.getMappedElementsMap() != null && elem.getMappedElementsMap().size() > 0) {




						boolean bIsUnchanged = false;
						//  boolean applyEffectivity = !xmlFileData.isDelta() ? true : unchanged.contains(elem.getOBID()) ? false : true;
						bIsUnchanged =  unchanged.contains(WriterUtils.getAttributeValue(elem, "id"));
						boolean applyEffectivity = !xmlFileData.isDelta() ? true : unchanged.contains(WriterUtils.getAttributeValue(elem, "id")) ? false : true;
						// 2019.01.21 [Amit] - removing P_MODE_SENDTO_WITHOUT_REF_CONFIG from the if condition as this mode has an end item associated
						if (preInputMode != null && (preInputMode.equals(PreferenceConstants.P_MODE_SENDTO_2_WITHOUT_REF_CONFIG) )) 
						{
							applyEffectivity = false;
						}
						if(!danglingj0nxdraw.contains(elem))
							/*try {
	            			mappedElems.processElementForMapping(elem);
	            			elem.setMappedElementsMap(mappedElems.getMappedElementsMap());
	            		} catch (NoSuchMethodException e) {
	            			// TODO Auto-generated catch block
	            			e.printStackTrace();
	            		} catch (SecurityException e) {
	            			// 

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
	            		}*/
							if(elemNmb == 1 && ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs() != null && ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs().size() > 0)
							{

								streamWriter.writeStartElement("e");
								streamWriter.writeAttribute(BaseElement.ID, VariantsUtil.generateElementIDForTopNode(ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs().get(0)));
								streamWriter.writeAttribute(BaseElement.ELEM_NMB, Integer.toString(elemNmb));
								streamWriter.writeAttribute("smaProject", "");

								streamWriter.writeAttribute("cdmProject","");
								streamWriter.writeAttribute("tcType", "ItemRevision");
								streamWriter.writeAttribute("cdmType", IConstants.TC_TYPE_C9CarBRVD);
								streamWriter.writeAttribute("smaType", "");
								streamWriter.writeAttribute(BaseElement.APPLY_EFFECTIVITY, Boolean.toString(applyEffectivity));
								streamWriter.writeAttribute("hc", Boolean.toString(hasChanges));
								boolean avFlag = true;
								String varCond = VariantsUtil.buildVariantCondition(ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs());
								if( varCond != null)
								{
									streamWriter.writeAttribute("av", Boolean.toString(avFlag));
									streamWriter.writeAttribute("var", varCond);
								}

								VariantsUtil.addParameters(streamWriter, ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs().get(0));
								streamWriter.writeEndElement();
								elemNmb++;

							}

						LOGGER.info("CLASS NAME : "+elem.getAppLabel()+" END ITEM : "+ xmlFileData.getEndItem());
						//LOGGER.info("ALL attributes : ");
						//printMappedAttrs(elem.getMappedElementsMap());

						if( xmlFileData.isDelta() && elem.getClazz().equals("JT")) {
							if(!bIsUnchanged)
								elem.writeElement(streamWriter, lstWriter, applyEffectivity, elemNmb++, mainProject, xmlFileData.getEndItem());
						}
						else{

							elem.writeElement(streamWriter, lstWriter, applyEffectivity, elemNmb++, mainProject, xmlFileData.getEndItem());
						}

						// supplies the object count information to the Singleton member
						if(elem.getClazz() != null) {
							// checks is there is already an existing instance of the ObjectInfo class for the srcClassName
							if(ReaderSingleton.getReaderSingleton().getObjInfoInst(elem.getClazz()) == null) {
								// instantiates the ObjectInfo class and sets it to the ReaderSingleton instance
								ObjectInfo objInfoInst = new ObjectInfo(elem.getClazz());
								if(objInfoInst != null) {

									// increments the object count
									objInfoInst.setSmaObjCount();
									ReaderSingleton.getReaderSingleton().setObjInfoList(objInfoInst);

								}
							}
							// increments the counter on the existing ObjectInfo instance
							else {
								ReaderSingleton.getReaderSingleton().getObjInfoInst(elem.getClazz()).setSmaObjCount();
							}
							// count for the Teamcenter type
							if(ReaderSingleton.getReaderSingleton().getObjInfoInst(elem.getClazz()) != null) {
								if(elem.getMappedElementsMap() != null && elem.getMappedElementsMap().keySet().size() == 1) {
									String tcTypeName = (String) (elem.getMappedElementsMap().keySet().toArray())[0];
									ReaderSingleton.getReaderSingleton().getObjInfoInst(elem.getClazz()).setTcType2CountMap(tcTypeName);
								}
							}


						}
					}

				}

				if (!batchMode) {
					if (feedback.isCanceled()) {
						return;
					}
					if (counter++ % 10 == 0) {
						feedback.worked(10);
					}
				}
			}

			// end of the print of the elements section
			streamWriter.writeEndElement(); // elements

			LOGGER.info("Writing elements completed.");
			ReaderSingleton.getReaderSingleton().addOBIDActionLogList("**********************************End of Element Section***************************************************************");
			ReaderSingleton.getReaderSingleton().addOBIDActionLogList("**********************************Connections Section***************************************************************");

			streamWriter.writeStartElement("connections");
			LOGGER.info("Writing connections..");

			for (TreeElement relation : relations) {

				if (versMap.isMappedElem(relation.element)) {
					continue;
				}
				if((relation.element.getMappedElementsMap() == null) || (relation.element.getMappedElementsMap().isEmpty()))
					continue;
				//logger.info("Relation clazz "+relation.clazz);
				String stopClass = getStopClass();
				if (stopClass != null && stopClass.equals(relation.element.getClazz())) {
					continue;
				}
				if (!batchMode) {
					if (feedback.isCanceled()) {
						return;
					}
				}
				List<TreeElement> validChildren = new ArrayList<TreeElement>();
				List<TreeElement> validDatasets = new ArrayList<TreeElement>();

				//				logger.info("<r> < "+WriterUtils.getAttributeValue(versMap.getLatestVersionElement(relation.element), "id")+" >");
				for (TreeElement child : relation.getChilds()) {
					if((child.element.getMappedElementsMap() == null) || (child.element.getMappedElementsMap().isEmpty()))
						 continue;
					
					List<TreeElement> tElems = child.getChilds();
					if(!(child.getClazz().equals(IConstants.j0BauKas) || child.getClazz().equals(IConstants.j0VisInt)))
					{
						if(!tElems.isEmpty() && tElems.size()==1){
							if (child.getState() != State.Missing 
									&& child.element.isValidForXML() 
									&& !tElems.get(0).getClazz().equals(IConstants.j0NXDrw)) { /* Condition added to exclude NX drawing */
								validChildren.add(child);
							}
						}else{
							if (child.getState() != State.Missing 
									&& child.element.isValidForXML()
									&& ! (PreImpConfig.getInstance().isDisable2D() && child.getClazz().equals(IConstants.j0NXDrw))) { // Condition to add NxDraw in connection only when Disable2d element is off
								validChildren.add(child);
							}
						}
					}            	
					if(relation.getClazz().equals(IConstants.j0PrtVer) &&
							(child.getClazz().equals(IConstants.j0BauKas) || child.getClazz().equals(IConstants.j0VisInt))){
						validDatasets.add(child);
					}

				}
				if (!batchMode) {
					if (counter++ % 10 == 0) {
						feedback.worked(10);
					}
					if (counter == maxCounter) {
						feedback.worked(counter % 10);
					}
				}
				boolean isOccEff = TypeMaps.isTypeForOccEff(relation.getClazz());

				// 2019.01.21 [Amit] - removing the condition check for P_MODE_SENDTO_WITHOUT_REF_CONFIG as this mode should have an end item associated with it 
				if (preInputMode != null && (preInputMode.equals(PreferenceConstants.P_MODE_SENDTO_2_WITHOUT_REF_CONFIG))) 
				{
					isOccEff = false;
				}
				if (validChildren.isEmpty() && !isOccEff) {
					continue;
				}            

				/* To handle NX Drawing  -- Starts here
				 * Creating new connection for NX drawing, 
				 * This creates a connection such that NX drawing will become a parent of its actual parent
				 */

				if (relation.getClazz().contains(IConstants.j0Cdi2D)) {
					LOGGER.info("Handling NX Drawing ...");

					List<TreeElement> drawingChilds = relation.getChilds();

					if(drawingChilds.size()==1 && drawingChilds.get(0).getClazz().equals(IConstants.j0NXDrw)){
						streamWriter.writeStartElement("r");
						TreeElement parentTreeElm = relation.getParent();
						if(parentTreeElm.getPartNumber().equals(xmlFileData.getRoot().partNumber)){                    	
							String sDatFileLoc = null;
							sDatFileLoc = xmlFileData.getFileloc().replace(".xml", ".dat");
							//sDatFileLoc = NXPartFileManagerUtils.getPlmXmlFileLoc().replace(".xml", ".dat");
							if(sDatFileLoc!=null){
								writeAssemblyID4NxDrawing(sDatFileLoc,relation.getPartNumber());
							}
						}

						String drawingObId = relation.getOBID(); 
						// String drawingObId = relation.getOBID(WriterUtils.getAttributeValue(relation, "id"));

						if ((BaseElement.ID != null) && (drawingObId != null)){
							streamWriter.writeAttribute(BaseElement.ID, drawingObId);
						}

						if (!uniqueElems.contains(relation.getOBID())) {
							throw createMissingElementEx(relation);
						}
						streamWriter.writeAttribute(BaseElement.ELEM_NMB, Integer.toString(elemNmb++));
						streamWriter.writeAttribute(BaseElement.APPLY_EFFECTIVITY, Boolean.toString(isOccEff));
						streamWriter.writeAttribute(BaseElement.DS_REL, Boolean.toString(false));


						if(ReaderSingleton.getReaderSingleton().isDbConnect())
						{

							String tagBV = relation.getTagBV();//WriterUtils.getAttributeValue(relation.getElement(), BaseElement.TAG_BV);
							String tagBVR = relation.getTagBVR();//WriterUtils.getAttributeValue(relation.getElement(), BaseElement.TAG_BVR);

							String action = "none";
							if(tagBV == null || tagBV.equals(""))
							{
								tagBV ="";
								action = "new";
							}
							if(tagBVR == null|| tagBVR.equals(""))
							{
								tagBVR ="";
								action = "new";
							}
							if(action.equals("none")&& (relation.isBomLineHasChanges || relation.getAction().equals("change")))
							{
								action = "change";
								LOGGER.info("Test ::: Bomline has some changes : Action : "+action);

							}
							streamWriter.writeAttribute(BaseElement.TAG_BV, tagBV);
							streamWriter.writeAttribute(BaseElement.TAG_BVR, tagBVR);

							ReaderSingleton.getReaderSingleton().addOccConnWriterLogList(WriterUtils.getAttributeValue(relation.getElement(), "item_id")+" : "+WriterUtils.getAttributeValue(relation.getElement(), "item_revision_id")+" : "+tagBV+" : "+tagBVR+"\n");

							streamWriter.writeAttribute(BaseElement.ACTION, action);
							ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(relation.getElement(), "id")+" : "+action);
							//streamWriter.writeAttribute(BaseElement.ACTION, getAction());
						}
						else
						{
							streamWriter.writeAttribute(BaseElement.ACTION, "not-checked");
							ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(relation.getElement(), "id")+" : "+"not-checked");
						}
						streamWriter.writeStartElement("c");
						// streamWriter.writeAttribute(BaseElement.ID, versMap.getLatestVersionElement(parentTreeElm.element).getOBID());
						streamWriter.writeAttribute(BaseElement.ID, WriterUtils.getAttributeValue(versMap.getLatestVersionElement(parentTreeElm.element), "id"));
						String relCnt = parentTreeElm.getRelCount();
						if (relCnt == null) {
							relCnt = "1";
						}
						streamWriter.writeAttribute("relCount", relCnt);

						if(ReaderSingleton.getReaderSingleton().isDbConnect())
						{
							//String tagOcc = relation.getTagOcc();//WriterUtils.getAttributeValue(relation.getElement(), "tagOCC");
							String tagOcc = parentTreeElm.getTagOcc();//versMap.getLatestVersionElement(parentTreeElm.element).get;
							String action = "none";
							if(tagOcc == null || tagOcc.equals(""))
							{
								tagOcc ="";
								action ="new";
							}
							streamWriter.writeAttribute(BaseElement.TAG_OCC, tagOcc);
							//streamWriter.writeAttribute(BaseElement.TAG_BVR, tagBVR);
							//streamWriter.writeAttribute(BaseElement.ACTION, action);
							//if(ReaderSingleton.getReaderSingleton().getOccConnWriteLogger() != null) {
							//ReaderSingleton.getReaderSingleton().getOccConnWriteLogger().write(WriterUtils.getAttributeValue(relation.getElement(), "item_id")+" : "+tagOcc+"\n");
							//System.out.println("Mapping info Map Size"+ReaderSingleton.getReaderSingleton().getMappingInfoMap().size());
							//}
							streamWriter.writeAttribute(BaseElement.ACTION, action);
							ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(relation.getElement(), "id")+" : "+action);
						}
						// only write trafo and folder type if it is an occurrence
						if (parentTreeElm.isOccurrence()) {
							// Have to write default transformation matrix
							double[] trans = EINHEITS_MATRIX;
							streamWriter.writeEmptyElement("t");
							for (int i = 0; i < trans.length; i++) {
								streamWriter.writeAttribute("v" + Integer.toString(i), Double.toString(trans[i]));
							}
							if (parentTreeElm.getFolderType() != null) {
								streamWriter.writeEmptyElement("fl");
								streamWriter.writeAttribute("t", parentTreeElm.getFolderType().toString());
							}
						}
						streamWriter.writeEndElement(); // C   
						//streamWriter.writeAttribute(BaseElement.ACTION, "test");

						streamWriter.writeEndElement(); // R            	
					}                 
				}
				/* To Handle NX Drawing -- Ends here */

				ArrayList<RefConfigMappingObject> refConfigMapObjList = ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs();
				if(refConfigMapObjList != null && refConfigMapObjList.size() > 0 && xmlFileData.getRoot().getOBID().equals(relation.getOBID()))
				{
					streamWriter.writeStartElement("r");

					streamWriter.writeAttribute(BaseElement.ID, VariantsUtil.generateElementIDForTopNode(refConfigMapObjList.get(0)));
					streamWriter.writeAttribute(BaseElement.ELEM_NMB, Integer.toString(elemNmb++));
					streamWriter.writeAttribute(BaseElement.APPLY_EFFECTIVITY, Boolean.toString(isOccEff));


					if( TypeMaps.isTypeForCarVariantCondition(IConstants.TC_TYPE_C9CarBRVD))
					{
						streamWriter.writeAttribute("av", Boolean.toString(true));
					}
					streamWriter.writeAttribute(BaseElement.DS_REL, Boolean.toString(false));

					streamWriter.writeStartElement("c");
					streamWriter.writeAttribute(BaseElement.ID, relation.getOBID());


					streamWriter.writeAttribute("relCount", "1");

					if(ReaderSingleton.getReaderSingleton().isDbConnect())
					{
						String tagOcc =relation.getTagOcc();// WriterUtils.getAttributeValue(relation.getElement(), "tagOCC");
						String action ="none";
						if(tagOcc == null || tagOcc.equals(""))
						{
							tagOcc ="";
							action = "new";
						}
						streamWriter.writeAttribute(BaseElement.TAG_OCC, tagOcc);
						/*if(ReaderSingleton.getReaderSingleton().getOccConnWriteLogger() != null) {
							//ReaderSingleton.getReaderSingleton().getOccConnWriteLogger().write(WriterUtils.getAttributeValue(relation.getElement(), "item_id")+" : "+tagOcc+"\n");
							//System.out.println("Mapping info Map Size"+ReaderSingleton.getReaderSingleton().getMappingInfoMap().size());
						}*/
						streamWriter.writeAttribute(BaseElement.ACTION, action);
						ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(relation.getElement(), "id")+" : "+action);

						//streamWriter.writeAttribute(BaseElement.ACTION, getAction());
					}
					else
					{
						streamWriter.writeAttribute(BaseElement.ACTION, "not-checked");
						ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(relation.getElement(), "id")+" : "+"not-checked");
					}
					if (xmlFileData.getRoot().isOccurrence()) {
						double[] trans = xmlFileData.getRoot().getTransform();
						if (trans == null) {
							trans = EINHEITS_MATRIX;
						}
						streamWriter.writeEmptyElement("t");
						for (int i = 0; i < trans.length; i++) {
							streamWriter.writeAttribute("v" + Integer.toString(i), Double.toString(trans[i]));
						}
					}
					streamWriter.writeEndElement();
					streamWriter.writeEndElement();
				}

				String currentClass = relation.getClazz();
				if(currentClass.equals(IConstants.j0Cdi3D) )
				{
					if(!validChildren.isEmpty())
					{
						TreeElement valChild = validChildren.get(0);
						if(valChild.getClazz().equals("JT"))
						{
							boolean bIsUnchanged = false;
							//  boolean applyEffectivity = !xmlFileData.isDelta() ? true : unchanged.contains(elem.getOBID()) ? false : true;
							bIsUnchanged =  unchanged.contains(WriterUtils.getAttributeValue(valChild.getElement(), "id"));

							if(xmlFileData.isDelta() && bIsUnchanged)
							{
								continue;
							}
						}
					}
				}


				if(validChildren.isEmpty())
				{
					continue;
				}

				boolean first = true;
				boolean isDataset = false;

				Map<String, String> childPartElemMapForRelCount = new HashMap<>();



				HashMap<String,HashMap<String, DBConnBOMInfo>> obIdDBConnBomInfoMap = new HashMap<>(); // obid - DB Connections Map
				HashMap<String,HashMap<String, DBConnBOMInfo>> obIdRelCountDBConnBomInfoMap = new HashMap<>(); // obid - DB Connections Map
				HashMap<String, DBConnBOMInfo> relCountDBConnBomInfoMap = new HashMap<>();
				HashMap<String,HashMap<String, String>> obIdOccActionMap = new HashMap<>(); // OBID - OccurenceAction Map
				HashMap<String, TreeElement> obIdElementMap = new HashMap<>(); // OBID - TreeElement Map
				HashMap<String, HashMap<String, TreeElement>> obIdRelCountElementMap = new HashMap<>(); // OBID - TreeElement Map

				ArrayList<TreeElement> relationChilds = new ArrayList<>(); 
				ArrayList<String> tagEndItemList = new ArrayList<>();
				String pOBID = WriterUtils.getAttributeValue(relation.getElement(), "id");

				for (TreeElement child : validChildren) 
				{


					if (!uniqueElems.contains(child.getOBID())) {
						LOGGER.info("Not in uniqElements List : "+child.getOBID());
						throw createMissingElementEx(child);
						//continue;
					}

					isDataset = PLMUtils.isFileRef(child.getElement().getClazz());
					/*if (first) 
					{
						first = false;
						streamWriter.writeAttribute(BaseElement.DS_REL, Boolean.toString(PLMUtils.isFileRef(child.getElement().getClazz())));
					}*/

					String childOBID = WriterUtils.getAttributeValue(versMap.getLatestVersionElement(child.element), "id");
					if( childOBID == null)
					{
						continue;
					}

					/*if("usr_wgub0000000160E97848".equals(WriterUtils.getAttributeValue(versMap.getLatestVersionElement(relation.element), "id")))
					{
						System.out.println("Processing Duplicate bomlines for Parent BOMLine : "+WriterUtils.getAttributeValue(versMap.getLatestVersionElement(relation.element), "id"));
					}*/

					//  Reading Duplicate Lines
					DBConnResponse dbConnResp = childLineDBResMap.get(relation.getOBID());
					String tagOcc = null ;
					String childAction = "none";
					String tagEndItem = null;
					if(dbConnResp != null )
					{
						HashMap<String, HashMap<String, DBConnBOMInfo>> childBomLinesMap = dbConnResp.getmMapChildItem2BOMInfo();
						if(childBomLinesMap != null && childBomLinesMap.size() > 0)
						{
							String itemID = WriterUtils.getAttributeValue(child.getElement(), "item_id");
							if( itemID != null)
							{
								HashMap<String, DBConnBOMInfo> childLineFromDB = childBomLinesMap.get(itemID);
								HashMap<String, String> duplicateBOMMap = new HashMap<>();
								if(childLineFromDB != null && childLineFromDB.size() > 0)
								{
									//	logger.info("Evaluting Duplicate BOMLines for Child BOMLine : <"+WriterUtils.getAttributeValue(versMap.getLatestVersionElement(child.element), "id")+" > < "+itemID+" >");
									/*System.out.println("Evaluting Refeerence Configurations for Child BOMLine : "+WriterUtils.getAttributeValue(versMap.getLatestVersionElement(child.element), "id")+" Item ID : "+itemID);*/


									childAction = "none";
									String chObid = WriterUtils.getAttributeValue(versMap.getLatestVersionElement(child.element), "id");
									if(childLineFromDB.size() > 1)
									{
										boolean matchfound = false;
										//SMA-218 D29
										HashMap<String, String> OccurenceActionMap =  new HashMap<>();
										boolean isOccurrenceActionMapFound = false;
										//System.out.println("No.of Children : "+childLineFromDB.keySet().size());
										HashMap<String, HashMap<String, DBConnBOMInfo>> dbRelCntOccBOMInfoMap = getChildLinesByRelCount(childLineFromDB);
										HashMap<String, DBConnBOMInfo> occBomInfoMap = null;
										occBomInfoMap = dbRelCntOccBOMInfoMap.get(child.getRelCount());
										if(occBomInfoMap != null && occBomInfoMap.size() > 0)
										{
											//break; Call duplicate BOM and Action calculation Method
											LOGGER.info(String.format("Calculate duplicated BOMS under %s ...", itemID));
											HashMap<String, String> occActionMapCalc = calculateDuplicateBOMsAction(occBomInfoMap);
											
											for (Entry <String, String> e : occActionMapCalc.entrySet()) {
												if (e.getValue() != null && e.getValue().equals("none")) {
													e.setValue(child.getAction());
												}
											}

											for (String key : occBomInfoMap.keySet()) 
											{
												DBConnBOMInfo dda1 = occBomInfoMap.get(key);


												//DBConnBOMInfo dda1 = childLineFromDB.get(key);
												if(dda1 != null)
												{
													if(obIdOccActionMap.get(childOBID) != null)
													{
														OccurenceActionMap = obIdOccActionMap.get(childOBID);

														isOccurrenceActionMapFound = true;
													}
													childAction = "none";
													HashMap<String, String> occAttrMap = dda1.getmMapPSOccAttrs();
													if(occAttrMap != null && occAttrMap.size() > 0)
													{
														//if(key.equals("$lSt8flO6PkLuC"))
														childAction = occActionMapCalc.get(key);
														if (null  == childAction) {
															childAction = "none";
															LOGGER.warn(String.format("While getting duplicated BOMs calculation result is a an occurence not found.\n  OBID: %s \n  occTag: %s \n  relcount: %s", child.getOBID(), key, child.getRelCount()));
															LOGGER.info(String.format("  Duplicate BOM calculation result: %s", occActionMapCalc.toString()));
															LOGGER.info(String.format("  BOMView occurences: %s", occBomInfoMap.keySet().toString()));
														}
														

														setAction(childAction);
														if(isOccurrenceActionMapFound )
														{
															if(occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT).equals(child.getRelCount()))
															{
																OccurenceActionMap.put(key, childAction);
															}

														}
														else if(!isOccurrenceActionMapFound )
														{
															OccurenceActionMap.put(key, childAction);
														}
														duplicateBOMMap.put(occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT),key);
														//	logger.info("Occ ID : "+key+" Rel Count : "+occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT));


													}
												}
												/*System.out.println("Action : "+action);
												logger.info("Action : "+action);*/

											}
										}
										else
										{
											// SMA-186
											childAction = "new";
											setAction(childAction);
										}

										obIdDBConnBomInfoMap.put(chObid, childLineFromDB);
										obIdElementMap.put(chObid, child);
										HashMap<String,TreeElement> map = null;

										if(obIdRelCountElementMap != null )
										{
											if(obIdRelCountElementMap.get(chObid) != null)
											{
												map = obIdRelCountElementMap.get(chObid);
												map.put(child.getRelCount(), child);
												obIdRelCountElementMap.put(chObid, map);
											}
											else
											{
												map = new HashMap<>();
												map.put(child.getRelCount(), child);
												obIdRelCountElementMap.put(chObid, map);
											}
										}

										//SMA-218 D29
										if(obIdOccActionMap.get(childOBID) != null)
										{
											OccurenceActionMap = obIdOccActionMap.get(childOBID);
										}
										obIdOccActionMap.put(chObid, OccurenceActionMap);
										
										// dirty workaround:  occurence mit eine relcount, die nicht in DB existirert,
										// soll weiter berabeitet werden, um in der Liste relationChilds hinzugefügt zu werden. sonst werden sie XML nicht geschreiben.
										if (dbRelCntOccBOMInfoMap.get(child.getRelCount()) != null) {
											continue;
										}
										
									}
									else
									{
										tagOcc =(String) childLineFromDB.keySet().toArray()[0];
										DBConnBOMInfo dda1 = childLineFromDB.get(tagOcc);
										
										// compare DB relCount with input relCount
										HashMap<String, String> occAttrMap = dda1.getmMapPSOccAttrs();
										if(occAttrMap != null && occAttrMap.size() > 0) {
											if(!occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT).equals(child.getRelCount())) {
												tagOcc = null;
											}
										}

										if(dda1.getmMapEndItem2Eff().size() > 0)
										{
											Object[] endItem2effMapKeys = dda1.getmMapEndItem2Eff().keySet().toArray();
											for (Object refCName : endItem2effMapKeys)
											{
												tagEndItemList.add((String) refCName);
											}
										}
									}


								}else {
									LOGGER.info(String.format("Item id %s not existing in input. Occurences with same current endItem will be closed", itemID));
									
								}
							}
						}
						// writing into intermediate 


					}

					if(isDataset)
					{
						LOGGER.info("Action for Dataset changes : ");
						String tagDataset = WriterUtils.getAttributeValue(child.getElement(), "tagDataset");
						if( tagDataset == null)
						{
							tagDataset = "";
							childAction = "new";
							LOGGER.info("Action for Dataset changes : No tag : Action Markes as new");
						}
						else
						{
							childAction = "none";
							LOGGER.info("Action for Dataset changes : dataset tag found : Action Markes as none");
						}

					}
					else 
					{
						//tagOcc = WriterUtils.getAttributeValue(child.getElement(), "tagOCC");
						if(tagOcc == null || tagOcc.equals(""))
						{
							tagOcc ="";
							childAction = "new";
						}

						// changes for missing occurence action....

						if(childAction.equals("none")&& !child.isBomLineHasChanges )
						{
							childAction = "none";
							LOGGER.info(childOBID+" ::Action = "+childAction +" :: BomLine Has No Changes.. So action marked as none");
						}

						else if(childAction.equals("none")&& child.isBomLineHasChanges)
						{
							childAction = "change";
							LOGGER.info(childOBID+" :: Action = "+childAction +" :: BomLine Has occurecne effectivty changes ..");
						}

						else if(relation.getElement().getAction() != null && !relation.getElement().getAction().equals("") && relation.getElement().getAction().equals("not-checked"))
						{
							childAction = "not-checked";
						}

						else if(!childAction.equals("not-checked"))
						{
							if(tagOcc == null || tagOcc.equals("") )
							{
								childAction = "new";
							}
							else
							{
								if(tagEndItemList.size() > 0 &&  tagEndItemList.contains(this.endItem))
								{
									childAction = "none";
								}
								else if(childAction.equals("none") && !isOccEff)
								{
									childAction = "none";
									//logger.info("Action = "+action +" :: BomLine Has Changes ..End Item : "+this.endItem+" not assigned to the Bomline ");
								}
								else
								{
									childAction = "change";
									LOGGER.info(childOBID+" :: Action = "+childAction +" :: BomLine Has Changes ..End Item : "+this.endItem+" not assigned to the Bomline ");
								}
							}
						}
					}
					child.setAction(childAction);
					child.setTagOcc(tagOcc);
					relationChilds.add(child);

				}

				// Writing r element...



				streamWriter.writeStartElement("r");


				// to handle j0CdiAss in connections start
				/**
				 * If there is j0CdiAss, it has to take it's part details
				 */
				//	String currentClass = relation.getClazz();

				// 12-12-2016 - Truck implementation only - sets the current baumuster id to the singleton instance
				if(ReaderSingleton.getReaderSingleton().getFzgTypeName() != null) {
					// checks the input vehicle type and asserts it is a Truck
					if(ReaderSingleton.getReaderSingleton().getFzgTypeName().equals(PreferenceConstants.P_FZG_TYP_LKW)) {
						// checks the mapped class of the current relation instance and asserts if it is a C/D-BM
						if(relation.getElement().getMappedElementsMap().containsKey(IConstants.TRUCK_CLASS_CBM) ||
								relation.getElement().getMappedElementsMap().containsKey(IConstants.TRUCK_CLASS_DBM))
						{
							//sets the current C/D-BM end item id to the singleton instance
							if(ReaderSingleton.getReaderSingleton().getPart2EffIDMap() != null) {
								if(ReaderSingleton.getReaderSingleton().getPart2EffIDMap().containsKey(relation.getPartNumber())) {
									ReaderSingleton.getReaderSingleton().setCurrBaumsterID(
											ReaderSingleton.getReaderSingleton().getPart2EffIDMap().get(relation.getPartNumber()));
								}
							}
						}
					}
				}

				TreeElement parentTreeElem1 = null;// new TreeElement();
				String parentObId = null;
				if (currentClass.contains(IConstants.j0CdiAss)) {
					parentTreeElem1 = relation.getParent();

					if (parentTreeElem1 != null) {
						//parentObId = parentTreeElem1.getElement().getOBID();
						parentObId = WriterUtils.getAttributeValue(parentTreeElem1.getElement(), "id");     
					}

				} else
					parentObId = relation.getOBID();
				if( parentObId == null)
				{
					System.out.println("No Relation ");
					LOGGER.info("No Relation ");
				}
				if ((BaseElement.ID != null) && (parentObId != null))
					streamWriter.writeAttribute(BaseElement.ID, parentObId);
				// to handle j0CdiAss in connections end
				if (!uniqueElems.contains(relation.getOBID())) {
					throw createMissingElementEx(relation);
				}

				streamWriter.writeAttribute(BaseElement.ELEM_NMB, Integer.toString(elemNmb++));
				streamWriter.writeAttribute(BaseElement.APPLY_EFFECTIVITY, Boolean.toString(isOccEff));
				//if(refConfigMapObjList != null && refConfigMapObjList.size() > 0 &&  TypeMaps.isTypeForCarVariantCondition(relation.getClazz()))
				if(refConfigMapObjList != null && refConfigMapObjList.size() > 0 )
				{
					if(isOccEff || TypeMaps.isTypeForCarVariantCondition(relation.getClazz()))
					{
						streamWriter.writeAttribute("av", Boolean.toString(true));
					}
				}
				/*else if(refConfigMapObjList != null && refConfigMapObjList.size() > 0 &&  TypeMaps.isTypeForCarVariantCondition(relation.getClazz()))
					//if(refConfigMapObjList != null && refConfigMapObjList.size() > 0 && isOccEff)
					{
						streamWriter.writeAttribute("av", Boolean.toString(true));
					}*/
				if (validChildren.isEmpty()) {
					streamWriter.writeAttribute(BaseElement.DS_REL, Boolean.toString(false));
				}

				if(ReaderSingleton.getReaderSingleton().isDbConnect())
				{
					//String tagBV = WriterUtils.getAttributeValue(relation.getElement(), BaseElement.TAG_BV);
					//String tagBVR = WriterUtils.getAttributeValue(relation.getElement(), BaseElement.TAG_BVR);

					String tagBV = relation.getTagBV();//WriterUtils.getAttributeValue(relation.getElement(), BaseElement.TAG_BV);
					String tagBVR = relation.getTagBVR();//WriterUtils.getAttributeValue(relation.getElement(), BaseElement.TAG_BVR);
					String action = "none";
					if(tagBV == null || tagBV.equals(""))
					{
						tagBV ="";
						action = "new";
					}
					if(tagBVR == null|| tagBVR.equals(""))
					{
						tagBVR ="";
						action = "new";
					}
					if(action.equals("none")&& relation.isBomLineHasChanges)
					{
						action = "change";
						LOGGER.info(parentObId+" Test ::: Bomline has some changes : Action : "+action);
					}


					// validating changes of children for action traversal....


					streamWriter.writeAttribute(BaseElement.TAG_BV, tagBV);
					streamWriter.writeAttribute(BaseElement.TAG_BVR, tagBVR);

					ReaderSingleton.getReaderSingleton().addOccConnWriterLogList(WriterUtils.getAttributeValue(relation.getElement(), "item_id")+" : "+WriterUtils.getAttributeValue(relation.getElement(), "item_revision_id")+" : "+tagBV+" : "+tagBVR+"\n");



					if(currentClass.equals(IConstants.j0Cdi3D) )
					{
						if(!validChildren.isEmpty())
						{
							TreeElement valChild = validChildren.get(0);
							isDataset = PLMUtils.isFileRef(valChild.getElement().getClazz());

							if(ReaderSingleton.getReaderSingleton().isDbConnect())
							{

								if(isDataset)
								{
									String tagDataset = WriterUtils.getAttributeValue(valChild.getElement(), "tagDataset");
									if( tagDataset == null)
									{
										tagDataset = "";
										action = "new";
									}
									else
									{
										action = "none";
									}
									//streamWriter.writeAttribute(BaseElement.TAG_DATASET, tagDataset);
								}
							}
						}
					}


					if(relationChilds != null && relationChilds.size() > 0 )
					{
						//logger.info("Parent : action changes : Default action : "+relation.getAction());
						LOGGER.info( "Validatng child bomlines for parent action changes..");
						for (TreeElement validChild : relationChilds)
						{
							//	if(validChild.getClazz().equals("j0SDPosV") || validChild.getClazz().equals("CD9SDPosV") || validChild.getClazz().equals("j0PrtVer") || validChild.getClazz().equals("C9Part"))
							

							Element relElement = validChild.getRelatingElement();

							if (relElement != null) {
								String codeRule = relElement.getUserValues().get("j0CodeRule");

								if (codeRule != null) {
									String codeRuleDB = validChild.getCodeRule();
									if (codeRuleDB == null) {
										if (validChild.getAction().equals("new")) {
											action = "new";
											// continue;
										} else {
											action = "change";
											LOGGER.info(validChild.getOBID()
													+ " Test ::: CodeRule has some changes : Action : " + action);
											break;
										}
										validChild.setAction(action);
									} else if (!codeRuleDB.equals(codeRule)) {
										action = "change";
										LOGGER.info(validChild.getOBID()
												+ " Test ::: CodeRule has some changes : Action : " + action);
										validChild.setAction(action);
									}
								}
							}
							
							if(validChild.getAction().equals("none") )
							{
								action = "none";
								continue;
							}
							else if(validChild.getAction().equals("new") )
							{
								action = "new";
								continue;
							}
							else
							{
								action = "change";
								LOGGER.info(validChild.getOBID()+" Test ::: Bomline has some changes : Action : "+action);
								break;
							}
						}
					}

					String itemID1 = WriterUtils.getAttributeValue(element, "item_id");

					String itemRevID1 = WriterUtils.getAttributeValue(element, "item_revision_id");
					LOGGER.info(" Parent : < "+parentObId+" > < "+itemID1+" > < "+itemRevID1+" Action : "+action+" > Before...");
					if (obIdElementMap.size() > 0 && !action.equals("change")) {
						Object[] obidKeys = obIdElementMap.keySet().toArray();
						LOGGER.info("Validating duplicate bomlines for parent action changes..");
						for (Object obid : obidKeys) {
							// child = obIdElementMap.get(obid);
							// Object[] bomInfoKeys = obIdDBConnBomInfoMap.keySet().toArray();
							// for (Object bomInfoKey : bomInfoKeys) {
							HashMap<String, DBConnBOMInfo> bomLineFromDB = obIdDBConnBomInfoMap.get(obid);
							Object[] bomlineKeys = bomLineFromDB.keySet().toArray();

							/*
							 * commented below piece of code to stop propagating changes form children to
							 * parent..
							 * 
							 */
							
							
							for (Object bomlineKey : bomlineKeys) {
								DBConnBOMInfo dda1 = bomLineFromDB.get(bomlineKey);
								if (dda1 != null) {
									if (obIdOccActionMap.get(obid) != null && obIdOccActionMap.get(obid).get((String) bomlineKey) == null) {
										LOGGER.trace(String.format("Found a DB Occurence %s not existing in input for the element %s", (String) bomlineKey, obid ));
										List<String> endItems = getEndItems(dda1);
										if (endItems.contains(endItem)) {
											LOGGER.trace(String.format("Since the Input endItem %s belongs to bomline endItems %s then remove action is set   ", endItem, endItems.toString()));
											
											obIdOccActionMap.get(obid).put((String) bomlineKey, "remove");
											action = "change";
										}else {
											LOGGER.trace(String.format("Since the Input endItem %s don´t belongs to bomline endItems %s then none action is set   ", endItem, endItems.toString()));

											obIdOccActionMap.get(obid).put((String) bomlineKey, "none");
										}										
									}
								}
							}
							if (action != "change") {
								if (obIdOccActionMap.get(obid) != null) {
									if (obIdOccActionMap.get(obid).values().contains("delete") || obIdOccActionMap.get(obid).values().contains("change")) {
										action = "change";
									} else if (obIdOccActionMap.get(obid).values().stream()
											.allMatch(v -> v.equals("new"))) {
										action = "new";
									}
								}
							}
							
						}
					}
					//validating note property for updating parent Action



					//logger.info("Action for Parent after Validating child bomline changes : "+action);

					// 	<parent element OBID> <Item-ID><Rev-ID><action value><cause of action value>
					String itemID = WriterUtils.getAttributeValue(element, "item_id");

					String itemRevID = WriterUtils.getAttributeValue(element, "item_revision_id");
					LOGGER.info("Parent: < "+parentObId+" > < "+itemID+" > < "+itemRevID+" Action : "+action+" >");

					if((isOccEff && action.equals("none")) )
					{
						DBConnResponse parentElmDBResp = childLineDBResMap.get(parentObId);
						if(parentElmDBResp != null && parentElmDBResp.getmMapEffDates() != null)
						{
							HashMap<String, String> parentEffDatesMap = parentElmDBResp.getmMapEffDates();
							if(parentEffDatesMap != null && isEffectivityClosed(parentEffDatesMap.get(1)))
							{
								action = "change";
								LOGGER.info(parentObId+" Test ::: Bomline has some changes with respect to effectivity : Action : "+action);
							}
						}

					}

					if (!currentClass.equals(IConstants.j0Cdi3D)) { // fix the issue where an existing CDI3D has no new JT but the action is set to new

						if (tagBV != null && !tagBV.equals("") && tagBVR != null && !tagBVR.equals("")) {
							if (action.equals("change") || action.equals("new")) {
								action = "change";
								LOGGER.info(parentObId
										+ " Test ::: Child Bomlines has some changes in effectivity or bomline changes or changes in Trafos : Action : "
										+ action);
							} else {
								action = "none";
							}
						} else if ((tagBV == null || tagBV.equals("")) && (tagBVR == null || tagBVR.equals(""))) {
							action = "new";
						}
					}
					streamWriter.writeAttribute(BaseElement.ACTION, action);
					ReaderSingleton.getReaderSingleton().addOBIDActionLogList(parentObId+" : "+action);


					//streamWriter.writeAttribute(BaseElement.ACTION, action);
					//streamWriter.writeAttribute(BaseElement.ACTION, getAction());
				}
				else
				{
					streamWriter.writeAttribute(BaseElement.ACTION, "not-checked");
					ReaderSingleton.getReaderSingleton().addOBIDActionLogList(parentObId+" : "+"not-checked");
				}

				isDataset = PLMUtils.isFileRef(relation.getElement().getClazz());
				if (first) 
				{
					first = false;

					if(currentClass.equals(IConstants.j0Cdi3D) )
					{
						if(!validChildren.isEmpty())
						{
							TreeElement valChild = validChildren.get(0);
							//isDataset = PLMUtils.isFileRef(valChild.getElement().getClazz());
							streamWriter.writeAttribute(BaseElement.DS_REL, Boolean.toString(PLMUtils.isFileRef(valChild.getElement().getClazz())));
						}
					}
					else{
						streamWriter.writeAttribute(BaseElement.DS_REL, Boolean.toString(PLMUtils.isFileRef(relation.getElement().getClazz())));
					}


				}

				//logger.info("Writing Child BOMlines....  ");
				if(relationChilds != null && relationChilds.size() > 0 )
				{
					for (TreeElement treeElement : relationChilds) {
						LOGGER.info("Test :::: Writing child BOMlines.... OBID : "+treeElement.getOBID()+ " Action : "+ treeElement.getAction());
						addBOMChildConnection(streamWriter, relation, treeElement, isOccEff, versMap, xmlFileData, tagEndItemList);
					}
				}

				//// write Duplicate BOMs here...
				if( obIdElementMap.size() > 0)
				{

					LOGGER.info("Writing Duplicate BOMlines....");
					Object[] obidKeys = obIdElementMap.keySet().toArray();
					TreeElement inputChild;
					for (Object obid : obidKeys) 
					{
						inputChild = obIdElementMap.get(obid);
						//logger.info("Writing Duplicate BOMlines for : "+obid);
						//Object[] bomInfoKeys = obIdDBConnBomInfoMap.keySet().toArray();
						//	for (Object bomInfoKey : bomInfoKeys) {
						HashMap<String, DBConnBOMInfo> bomLineFromDB = obIdDBConnBomInfoMap.get(obid);
						HashMap<String, TreeElement> map = obIdRelCountElementMap.get(obid);
						Object[] bomlineKeys = bomLineFromDB.keySet().toArray();
						for (Object bomlineKey : bomlineKeys) {
							DBConnBOMInfo dda1 = bomLineFromDB.get(bomlineKey);
							if(dda1 != null)
							{
								HashMap<String, String> occAttrMap = dda1.getmMapPSOccAttrs();
								TreeElement child;
								if(map.get(occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT)) != null)
								{
									child = map.get(occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT));
									//System.out.println("Child :: NULL");
									child.setClone(false);
								}else {
									// dirty Workaround: occurence, die keine entsprechende XML Element haben, sollen nicht vergliechen werden.
									// clone input child
									child = inputChild; // (TreeElement) inputChild.clone();
									child.setClone(true);
								}
								
								if(child != null)
								{
									if((String)bomlineKey != null)
									{
										child.setTagOcc((String)bomlineKey);
									}
									LOGGER.info(child.getOBID()+" Test ::: Writing Duplicate child .... Action  "+ child.getAction());
									LOGGER.trace(String.format("Occurences for OBID %s are : %s", obid, obIdOccActionMap.get(obid).toString()));
									addChildConnections(streamWriter, child, isOccEff, versMap, relation,(String)bomlineKey,occAttrMap,xmlFileData,obIdOccActionMap.get(obid));
								}
							}
						}
					}
				}
				//logger.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+WriterUtils.getAttributeValue(element, "item_id")+" > < "+WriterUtils.getAttributeValue(element, "item_rev_id")+" > < Action : "+element.getAction());
				LOGGER.info("End Of <r > < "+WriterUtils.getAttributeValue(relation.getElement(), "id")+" > " );
				streamWriter.writeEndElement(); // R

				/* Dataset handling for part -- starts here */
				if(relation.getClazz().equalsIgnoreCase(IConstants.j0PrtVer) && !validDatasets.isEmpty()){
					streamWriter.writeStartElement("r");
					String parentObId1 = relation.getOBID();
					if ((BaseElement.ID != null) && (parentObId1 != null)){
						streamWriter.writeAttribute(BaseElement.ID, parentObId1);
					}
					streamWriter.writeAttribute(BaseElement.ELEM_NMB, Integer.toString(elemNmb++));
					streamWriter.writeAttribute(BaseElement.APPLY_EFFECTIVITY, Boolean.toString(isOccEff));
					Boolean relationtag = true;
					String rAction ="";
					for (TreeElement dataset : validDatasets) {  
						if (relationtag) {
							relationtag = false;
							isDataset= PLMUtils.isFileRef(dataset.getElement().getClazz());
							streamWriter.writeAttribute(BaseElement.DS_REL, Boolean.toString(PLMUtils.isFileRef(dataset.getElement().getClazz())));
						}


						streamWriter.writeStartElement("c");
						streamWriter.writeAttribute(BaseElement.ID, WriterUtils.getAttributeValue(versMap.getLatestVersionElement(dataset.element), "id"));

						TreeElement datsetParent = dataset.getParent();
						String relCnt = datsetParent.getRelCount();
						if (relCnt == null) {
							relCnt = "1";
						}
						streamWriter.writeAttribute("relCount", relCnt);        
						if(ReaderSingleton.getReaderSingleton().isDbConnect())
						{
							String action = "none";
							if(isDataset)
							{
								String tagDataset = WriterUtils.getAttributeValue(dataset.getElement(), "tagDataset");
								if( tagDataset == null)
								{
									tagDataset = "";
									action = "new";
								}
								else
								{
									action = "none";
								}
								streamWriter.writeAttribute(BaseElement.TAG_DATASET, tagDataset);
							}
							else 
							{
								if(tagOcc == null || tagOcc.equals(""))
								{
									tagOcc ="";
									action = "new";
								}

								streamWriter.writeAttribute(BaseElement.TAG_OCC, tagOcc);
								/*if(ReaderSingleton.getReaderSingleton().getOccConnWriteLogger() != null) {
									//ReaderSingleton.getReaderSingleton().getOccConnWriteLogger().write(WriterUtils.getAttributeValue(relation.getElement(), "item_id")+" : "+tagOcc+"\n");
									//System.out.println("Mapping info Map Size"+ReaderSingleton.getReaderSingleton().getMappingInfoMap().size());
								}*/
							}
							if(rAction != null && !rAction.equals("change"))
							{
								rAction = action;
							}
							LOGGER.info("Test ::: Dataset changes : Action : "+action);
							streamWriter.writeAttribute(BaseElement.ACTION, action);
							ReaderSingleton.getReaderSingleton().addOBIDActionLogList("\t"+WriterUtils.getAttributeValue(dataset.getElement(), "id")+" : "+action);
						}
						else
						{
							streamWriter.writeAttribute(BaseElement.ACTION, "not-checked");
							ReaderSingleton.getReaderSingleton().addOBIDActionLogList("\t"+WriterUtils.getAttributeValue(dataset.getElement(), "id")+" : "+"not-checked");
						}
						streamWriter.writeEndElement(); // C
					}

					if(rAction.equals(""))
					{
						rAction = "none";
					}
					streamWriter.writeEndElement(); // R

				} /* Dataset handling for part -- ends here */
			}

			setMaxElemNmb(elemNmb);
			streamWriter.writeEndElement(); // connections
			LOGGER.info("Writing connections completed.");
			ReaderSingleton.getReaderSingleton().addOBIDActionLogList("**********************************End of Connections Section***************************************************************");

			streamWriter.writeEndElement(); // import
			streamWriter.writeEndDocument();
			if (!batchMode) {
				feedback.done();
			}
		} catch (Exception e) {
			LOGGER.error("Failure while writing XML file", e);
		} finally {
			if (lstWriter != null) {
				lstWriter.flush();
				lstWriter.close();
			}
			if (streamWriter != null) {
				streamWriter.flush();
				streamWriter.close();
			}
			if (fo != null) {
				fo.flush();
				try {
					fo.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	private void setClone(boolean isClone) {
		this.isClone = isClone;
		
	}

	public boolean isClone() {
		return isClone;
	}
	
	private List<String> getEndItems(DBConnBOMInfo DBbomInfo) {
		
		List<String> endItemsList = new ArrayList<>();
		if (DBbomInfo == null) {
			return endItemsList;
		}
		
		HashMap<String, HashMap<String, String>> endItem2effMap = DBbomInfo.getmMapEndItem2Eff();
		if( endItem2effMap == null || endItem2effMap.size() == 0){
			return endItemsList;
		}
		
		Object[] endItem2effMapKeys = endItem2effMap.keySet().toArray();
		for (Object refCName : endItem2effMapKeys)
		{
			endItemsList.add((String) refCName);

		}
		
		return endItemsList;
	}

	private HashMap<String, String> calculateDuplicateBOMsAction(HashMap<String, DBConnBOMInfo> occBomInfoMap) 
	{
		LOGGER.debug("Start calculating duplicated BOMS... "+occBomInfoMap.size());
		
		// TODO Auto-generated method stub
		HashMap<String, Integer> occurenceNoOfEndItems =  new HashMap<>();
		HashMap<String, ArrayList<String>> occurenceEndItemsListMap =  new HashMap<>();
		String occurence = null;
		int noOfEndItems = 0;
		ArrayList<String> maxEndItemList = new ArrayList<>();
		//System.out.println("No of duplicate BOMs : "+occBomInfoMap.size() );
		HashMap<String, String> occrenceActionMap = new HashMap<>();

		LOGGER.info("No of Duplicate BOMs : "+occBomInfoMap.size());

		if(occBomInfoMap.size() == 1)
		{
			//System.out.println("Welcome:::");
			Object[] keys = occBomInfoMap.keySet().toArray();
			String key = (String) keys[0];
			/*if(key.equals("XKQt8fa06PkLuC"))
			{
				System.out.println("Key : "+key);
			}*/

			DBConnBOMInfo dda1 = occBomInfoMap.get(key);
			HashMap<String, HashMap<String, String>> endItem2effMap = dda1.getmMapEndItem2Eff();
			if( endItem2effMap != null && endItem2effMap.size() > 0)
			{
				Object[] endItem2effMapKeys = endItem2effMap.keySet().toArray();
				HashMap<String, String> effectivityDatesMap ;
				ArrayList<String> refConfigList = new ArrayList<>();
				for (Object refCName : endItem2effMapKeys)
				{
					refConfigList.add((String) refCName);

				}
				if(refConfigList.size() == 1 &&  refConfigList.contains(endItem))
				{
					//occoccurence = key;
					effectivityDatesMap = endItem2effMap.get(endItem);
					if(isEffectivityClosed(effectivityDatesMap.get(1)))
					{
						occrenceActionMap.put(key, "change");
						LOGGER.info("Test ::: Effectivity closed so  : Action : "+action);
					}
					else
					{
						occrenceActionMap.put(key, "none");
					}


				}
				else if(refConfigList.size() == 1 &&  !refConfigList.contains(endItem))
				{
					occrenceActionMap.put(key, "change");
					LOGGER.info("Test ::: End Item not SET so  : Action : "+action);
				}
				else if(refConfigList.size() >= 1 &&  !refConfigList.contains(endItem))
				{
					occrenceActionMap.put(key, "change");
					LOGGER.info("Test ::: End Item not SET so  : Action : "+action);
				}
				else if(refConfigList.size() >= 1 &&  refConfigList.contains(endItem))
				{
					effectivityDatesMap = endItem2effMap.get(endItem);
					if(isEffectivityClosed(effectivityDatesMap.get(1)))
					{
						occrenceActionMap.put(key, "change");
						LOGGER.info("Test ::: Effectivity closed so  : Action : "+action);
					}
					else
					{
						occrenceActionMap.put(key, "none");
					}
				}
			}
			else
			{
				occrenceActionMap.put(key, "change");
				LOGGER.info("Test ::: End Item not SET so  : Action : "+action);
			}
			//occurenceNoOfEndItems.put(key, refConfigList.size());
			//occurenceEndItemsListMap.put(key, refConfigList);
		}
		else 
		{
			new DuplicationRuleEvaluator().evaluateRules(occBomInfoMap, endItem, occrenceActionMap);
			
			/*
			for (String key : occBomInfoMap.keySet()) 
			{
				DBConnBOMInfo dda1 = occBomInfoMap.get(key);



				HashMap<String, HashMap<String, String>> endItem2effMap = dda1.getmMapEndItem2Eff();
				if( endItem2effMap != null && endItem2effMap.size() > 0)
				{
					Object[] endItem2effMapKeys = endItem2effMap.keySet().toArray();
					ArrayList<String> refConfigList = new ArrayList<>();
					for (Object refCName : endItem2effMapKeys)
					{
						refConfigList.add((String) refCName);

					}
					if(refConfigList.size() > noOfEndItems)
					{
						occurence = key;
						noOfEndItems = refConfigList.size();
						maxEndItemList = refConfigList;
					}
					occurenceNoOfEndItems.put(key, refConfigList.size());
					occurenceEndItemsListMap.put(key, refConfigList);
				}
				//else
				//{
				//	occrenceActionMap.put(key, "delete");
				//}
			}
			if(occurence != null )
			{
				LOGGER.info("End Items ( most no in BOM Line ) : "+maxEndItemList);
				if(maxEndItemList != null && maxEndItemList.contains(endItem))
				{
					occrenceActionMap.put(occurence, "none");
				}
				else if(maxEndItemList != null && !maxEndItemList.contains(endItem))
				{
					occrenceActionMap.put(occurence, "change");
				}
			}
			
			//if(occurenceNoOfEndItems.size() > 0)
			{
				for (String key :  occBomInfoMap.keySet()) 
				{
					if(occrenceActionMap.get(key)== null)
					{
						ArrayList<String> endItemsList = new ArrayList<>();
						//System.out.println(endItemsList+"  :::: "+ maxEndItemList);

						DBConnBOMInfo dda1 = occBomInfoMap.get(key);



						HashMap<String, HashMap<String, String>> endItem2effMap = dda1.getmMapEndItem2Eff();
						//System.out.println(endItemsList);
						if( endItem2effMap != null && endItem2effMap.size() > 0)
						{
							Object[] endItem2effMapKeys = endItem2effMap.keySet().toArray();
							//ArrayList<String> refConfigList = new ArrayList<>();
							for (Object refCName : endItem2effMapKeys)
							{
								endItemsList.add((String) refCName);

							}

						}
						else
						{
							occrenceActionMap.put(key, "delete");
						}

						if(endItemsList != null && endItemsList.size() > 0)
						{
							LOGGER.info("END Items : "+ endItemsList);
							if(endItemsList.contains(endItem) && isEndItemSubset(maxEndItemList, endItemsList))
							{
								
								occrenceActionMap.put(key, "delete");
							}
							else if(!endItemsList.contains(endItem) && isEndItemSubset(maxEndItemList, endItemsList))
							{
								occrenceActionMap.put(key, "delete");
							}
							else if(endItemsList.contains(endItem) && !isEndItemSubset(maxEndItemList, endItemsList))
							{
								occrenceActionMap.put(key, "remove");
							}
							else if(!endItemsList.contains(endItem) && !isEndItemSubset(maxEndItemList, endItemsList))
							{
								occrenceActionMap.put(key, "none");
							}
						}
					}


				}
			}	*/
		}
		//}

		//occrenceActionMap.forEach((key, value) -> System.out.println(key + ":" + value));
		//System.out.println("Max No.of EndItems : "+noOfEndItems+"  Occurence : "+occurence);
		//System.out.println("*************************************************************************************************");
		return occrenceActionMap;
	}

	public boolean isEffectivityClosed(String dblastModDate)
	{
		boolean isEffectivityClosed = false;
		//String dblastModDate = dbConnDatasetInfo.getmMapDatasetAttrs().get("mod_date");
		if(dblastModDate != null && !dblastModDate.equals(""))
		{
			//System.out.println("Last Modified Date :: "+dblastModDate);


			SimpleDateFormat dbDateSimpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dbDateValObj;
			try {
				dbDateValObj = dbDateSimpleFormat.parse(dblastModDate);
				Date today = new Date();
				Date preDateValObj = dbDateSimpleFormat.parse(today.toString());
				if (dbDateValObj != null && preDateValObj != null)
				{
					if(dbDateValObj.before(preDateValObj))
					{
						isEffectivityClosed = true;
					}
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



		}
		return isEffectivityClosed;
	}
	public boolean isEndItemSubset(ArrayList<String> maxEndList, ArrayList<String> duplBomEndItemList)
	{
		boolean isEndItemListSubset =true;;
		for(int i =0; i < duplBomEndItemList.size();i++)
		{
			//if(!duplBomEndItemList.get(i).equals(maxEndList.get(i)))
			if(!maxEndList.contains(duplBomEndItemList.get(i)))
			{
				isEndItemListSubset = false;
				//System.out.println("Is Subset : "+isEndItemListSubset);
				return isEndItemListSubset;
			}
		}
		if(isEndItemListSubset && duplBomEndItemList.size() >=maxElemNmb)
		{
			isEndItemListSubset = true;
		}
		//System.out.println("Is Subset : "+isEndItemListSubset);
		return isEndItemListSubset;
	}

	private HashMap<String, HashMap<String, DBConnBOMInfo>> getChildLinesByRelCount(HashMap<String, DBConnBOMInfo> childLines) 
	{
		HashMap<String, HashMap<String, DBConnBOMInfo>>  relCntBomInfoMap = new HashMap<>();
		if(childLines != null && childLines.size() > 0)
		{
			Set<String> keys = childLines.keySet();
			for (String key : keys) 
			{
				DBConnBOMInfo dda1 = childLines.get(key);
				if(dda1 != null)
				{
					HashMap<String, String> occAttrMap = dda1.getmMapPSOccAttrs();
					if(occAttrMap != null && occAttrMap.size() > 0)
					{
						//System.out.println( "Rel Count from DB : "+ occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT)+" ::::: RefCount from PLMXML : "+child.getRelCount());
						//logger.info( "Rel Count from DB : "+ occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT)+" ::::: RefCount from PLMXML : "+child.getRelCount());
						String dbRelCnt = occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT);
						HashMap<String, DBConnBOMInfo> occurenceBomInfoMap;
						if(dbRelCnt != null )
						{
							if(relCntBomInfoMap.get(dbRelCnt) == null)
							{
								occurenceBomInfoMap = new HashMap<>();
								occurenceBomInfoMap.put(key, dda1);

							}
							else
							{
								occurenceBomInfoMap = relCntBomInfoMap.get(dbRelCnt);
								occurenceBomInfoMap.put(key, dda1);

							}
							relCntBomInfoMap.put(dbRelCnt, occurenceBomInfoMap);
						}
					}
				}
			}
		}
		return relCntBomInfoMap;
	}




	// , xmlFileData xmlFileData, String , occAttrMap occAttrMap,
	public void addChildConnections(XMLStreamWriter streamWriter, TreeElement child,boolean isOccEff, ElementVersionMap versMap,TreeElement parentTreeElement, String occurence, HashMap<String, String> occAttrMap, XMLFileData xmlFileData, HashMap<String, String> occMap) throws XMLStreamException
	{
		String childAction = occMap.get(occurence);
		if (null  == childAction) { // SMA-186: element will be deleted
			childAction = "remove";
			LOGGER.error(String.format("The following child occurence is not found in the list of occurences with relCount values as in the incoming model. It will be ignored  .\n  OBID: %s \n  occTag: %s \n  relcount: %s", child.getOBID(), occurence, child.getRelCount()));
		}

		streamWriter.writeStartElement("c");
		streamWriter.writeAttribute(BaseElement.ID, WriterUtils.getAttributeValue(versMap.getLatestVersionElement(child.element), "id"));


		// 12-12-2016 - Truck implementation only
		// checks if the current input being processed is Truck or not
		if(ReaderSingleton.getReaderSingleton().getFzgTypeName() != null) {
			if(ReaderSingleton.getReaderSingleton().getFzgTypeName().equals(PreferenceConstants.P_FZG_TYP_LKW)) {
				// checks if the current element being processed is not the top level truck object
				if(!child.getElement().getMappedElementsMap().containsKey(IConstants.TRUCK_CLASS_C9TRCK)) {

					// writes out the end item information that is stored in the associated Element instance
					if(Boolean.toString(isOccEff).equals("true")) {
						if(child.getElement().getEndItemIDs() != null && !child.getElement().getEndItemIDs().isEmpty()) {
							//-- Krishna -- Temorary fix for removing endItem Attribute  --------------------------------------------- commenting the below line
							streamWriter.writeAttribute(IConstants.INTXML_ENDITEM_ATTR, child.getElement().getEndItemIDs());
						}
					}
				}
			}
		}

		boolean childOccEff = isOccEff;
		ArrayList<RefConfigMappingObject> refConfigMapObjList = ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs();

		if(refConfigMapObjList != null && refConfigMapObjList.size() > 0 && xmlFileData.getRoot().getOBID().equals(parentTreeElement.getOBID()))
		{
			childOccEff = true;
		}
		if(refConfigMapObjList != null && refConfigMapObjList.size() > 0 && childOccEff)
		{
			String varCond = VariantsUtil.createVarConditionForConnectionFromGlobalConstant( ICustom.TYPE_MAP.get(parentTreeElement.getClazz()), ICustom.TYPE_MAP.get(child.getClazz()), refConfigMapObjList);
			if(varCond != null && !varCond.equals(""))
			{
				streamWriter.writeAttribute("var", varCond);
			}
		}

		//String relCnt = child.getRelCount(); 
		String relCnt = occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT);
		if (relCnt == null) {
			relCnt = "1";
		}
		//	logger.info("relCount :: "+relCnt);
		streamWriter.writeAttribute("relCount", relCnt);

		if(ReaderSingleton.getReaderSingleton().isDbConnect())
		{
			boolean isDataset = PLMUtils.isFileRef(child.getElement().getClazz());
			if(isDataset)
			{
				String tagDataset = WriterUtils.getAttributeValue(child.getElement(), "tagDataset");
				if( tagDataset == null)
				{
					tagDataset = "";
					childAction = "new";
				}
				else
				{
					childAction = "none";
				}
				streamWriter.writeAttribute(BaseElement.TAG_DATASET, tagDataset);
				child.setAction(action);
			}
			else
			{
				String occTag = child.getTagOcc();
				if(occTag == null || occTag.equals(""))
				{
					occTag ="";
					childAction = "new";
				}
				streamWriter.writeAttribute(BaseElement.TAG_OCC, occTag);
				//if(ReaderSingleton.getReaderSingleton().getOccConnWriteLogger() != null) {
				/*if(occurence.equals("1If9rR1u6PkLuC"))
					{
						System.out.println("Welcome");
					}*/
				//ReaderSingleton.getReaderSingleton().getOccConnWriteLogger().write(WriterUtils.getAttributeValue(parentTreeElement.getElement(), "item_id")+" : "+WriterUtils.getAttributeValue(parentTreeElement.getElement(), "item_revision_id")+" : "+WriterUtils.getAttributeValue(child.getElement(), "item_id")+" : "+relCnt+" : "+occurence+"\n");
				ReaderSingleton.getReaderSingleton().addOccConnWriterLogList(WriterUtils.getAttributeValue(parentTreeElement.getElement(), "item_id")+" : "+WriterUtils.getAttributeValue(parentTreeElement.getElement(), "item_revision_id")+" : "+WriterUtils.getAttributeValue(child.getElement(), "item_id")+" : "+relCnt+" : "+occurence+"\n");
				String trafoChanges = "";
				if(!child.isClone && child.isTrafoHasChanges())
				{
					trafoChanges = "Trafo has changes";
					LOGGER.info(child.getOBID()+" Test ::: Writing Duplicate child BOMlines.... Updated Action Due to  Trafo has changes "+ action);
				}
				LOGGER.info("Child : < "+WriterUtils.getAttributeValue(child.getElement(), "id")+" > < "+occTag+" > < "+WriterUtils.getAttributeValue(child.getElement(), "item_id")+" > < "+trafoChanges+" > < "+child.getRelCount()+" > < "+relCnt+" > <Action : "+occMap.get(occurence)+" >");
				//System.out.println("Mapping info Map Size"+ReaderSingleton.getReaderSingleton().getMappingInfoMap().size());
				//} 1.9.1_p001
				child.setAction(childAction);
			}

			String strOccMapVal = occMap.get(occurence);
			if( strOccMapVal == null)
			{
				strOccMapVal = "none"; // SMA-186: element will be ignored.  Duplicated logic check which is already done on the method top
			}
			//streamWriter.writeAttribute(BaseElement.ACTION, occMap.get(occurence));

			child.setAction(strOccMapVal);
			String codeRule = null;
			String codeRuleFromDB = null;
			if(ReaderSingleton.getReaderSingleton().isDbConnect())
			{
				//String action = "none";

				//	if(child.getClazz().equals("j0SDPosV") || child.getClazz().equals("CD9SDPosV") || child.getClazz().equals("j0PrtVer") || child.getClazz().equals("C9Part"))
				//{

					//logger.info("Reading code Rule for updating the action "); ujogEnbwgut1-usr_wgubUUJ


					if (!child.isClone && !childAction.equals("delete") && !childAction.equals("remove")
							&& !childAction.equals("change")) {
						Element relElement = child.getRelatingElement();
						if (relElement != null) {
							codeRule = relElement.getUserValues().get("j0CodeRule");

							if (codeRule != null) {
								codeRuleFromDB = child.getCodeRule();
								if (codeRuleFromDB == null) {
									if (child.getAction().equals("new")) {
										childAction = "new";
										// continue;
									} else {
										childAction = "change";
										LOGGER.info(child.getOBID()
												+ " Test ::: Writing Duplicate child BOMlines.... Updated Action Due to  CodeRule changes "
												+ childAction);
									}
									child.setAction(childAction);
									
								} else if (!codeRuleFromDB.equals(codeRule)) {
									childAction = "change";
									child.setAction(childAction);
									LOGGER.info(child.getOBID()
											+ " Test :::: Writing Duplicate child BOMlines.... Updated Action Due to  CodeRule changes "
											+ childAction);
									
								}
								// logger.info("Action : "+action+"After reading code Rule ");
							}
						}

					}
				//}
				//streamWriter.writeAttribute(BaseElement.ACTION, child.getAction());
				//ReaderSingleton.getReaderSingleton().addOBIDActionLogList("\t"+WriterUtils.getAttributeValue(child.getElement(), "id")+" : "+child.getAction());
			}


			streamWriter.writeAttribute(BaseElement.ACTION, child.getAction());
			//logger.info("Action : "+action);
			ReaderSingleton.getReaderSingleton().addOBIDActionLogList("\t"+WriterUtils.getAttributeValue(child.getElement(), "id")+" : "+strOccMapVal);
		}
		else
		{
			streamWriter.writeAttribute(BaseElement.ACTION, "not-checked");
			ReaderSingleton.getReaderSingleton().addOBIDActionLogList("\t"+WriterUtils.getAttributeValue(child.getElement(), "id")+" : "+"not-checked");
		}

		// adding Code rule...


		// writing Coderule tag ...

		Element relElement = child.getRelatingElement();

		if(relElement != null)
		{
			/*	if("ujogEnbwgut1-usr_wgubUUJ".equals((WriterUtils.getAttributeValue(child.getElement(), "id"))))
			{
				System.out.println("Welcome::");
			}*/
			codeRule =relElement.getUserValues().get("j0CodeRule");

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
		}


		if (child.isOccurrence()) {
			double[] trans = child.getTransform();
			if (trans == null) {
				trans = EINHEITS_MATRIX;
			}
			streamWriter.writeEmptyElement("t");
			for (int i = 0; i < trans.length; i++) {
				streamWriter.writeAttribute("v" + Integer.toString(i), Double.toString(trans[i]));
			}
			if (child.getFolderType() != null) {
				streamWriter.writeEmptyElement("fl");
				streamWriter.writeAttribute("t", child.getFolderType().toString());
			}
		}
		streamWriter.writeEndElement(); // C
		//System.out.println("Writing Connections : End <c> element ");
		//	logger.info("Writing Connections : End <c> element ");
	}

	public int getMaxElemNmb() {
		return maxElemNmb;
	}

	public void setMaxElemNmb(int maxElemNmb) {
		this.maxElemNmb = maxElemNmb;
	}

	/**
	 * Collect the elements which don't have any changes.
	 * 
	 * @param unchanged
	 *           The set of not changed elements.
	 */
	private void walkElements(Set<String> unchanged) {

		if (state == null && !unchanged.contains(element)) {
			// unchanged.add(element.getOBID()); 
			unchanged.add(WriterUtils.getAttributeValue(element, "id"));
		}
		for (TreeElement child : getChilds()) {
			child.walkElements(unchanged);
		}
	}

	/**
	 * Collect the elements which should be written to the Import XML file. This
	 * method is doing the most complicated job, because it has to find out the
	 * content which has changes and the content that doesn't has changes but
	 * needs to be part of the Import XML file for reference.
	 * 
	 * @param elements
	 *           The elements that have to go into import XML file.
	 * @param relations
	 *           The connections that should go into the import XML file.
	 * @param unchanged
	 *           The unchanged objects which are used to find out if the new
	 *           state is just because of the added relation.
	 * @param uniqueElems
	 *           The set to track the elements which have been already added to
	 *           the {@code elements} list.
	 * @param uniqueRels
	 *           The set to track the connections which have been already added
	 *           to the {@code relations} list.
	 * @param delta
	 *           {@code true} if the delta is written to the XML file.
	 */
	private void getElementsForXML(final List<Element> elements, final List<TreeElement> relations, final Set<String> uniqueElems,
			final Set<String> uniqueRels, final boolean delta) {
		class AddElementHepler {
			void add(final TreeElement treeElement) {

				if (!uniqueRels.contains(treeElement.getOBID())) {
					uniqueRels.add(treeElement.getOBID());
					validateChildBOMLines(treeElement);

					List<TreeElement> children = treeElement.getChilds();
					if(children != null && children.size() > 0)
					{
						//System.out.println("No of Children :: "+children.size());
					}


					//relations.add(treeElement);

					if(treeElement.getOBID() != null  && treeElement.getParent() != null && treeElement.getParent().getElement().getMappedElementsMap() == null)
					{
						System.out.println("Welcome :::  "+treeElement.getOBID() +"    Parent Clazz :: "+treeElement.getParent().getClazz());
						//continue;
					}
					relations.add(treeElement);
				}else {
					if (treeElement.getOBID() != null)
					LOGGER.debug("Relation {} already exists", treeElement.getOBID());
				}
			}

			void add(final Element element) {
				// if (!uniqueElems.contains(element.getOBID())) { ;  
				String tObid =WriterUtils.getAttributeValue(element, "id");


				if(tObid == null)
				{
					tObid = element.getOBID();
				}

				// Fix by krishna -- 9thMarch 2020
				if(tObid == null){
					tObid = element.getAppLabel();
				}
				//System.out.println("before adding uniqueElems List : "+tObid);
				//logger.info("before adding uniqueElems List : "+tObid);


				//if (!uniqueElems.contains(WriterUtils.getAttributeValue(element, "id")) || !uniqueElems.contains(element.getOBID())) {
				if (!uniqueElems.contains(tObid) ) {
					if(ReaderSingleton.getReaderSingleton().isDbConnect())
					{
						compareWithDBData(element);
					}

					elements.add(element);
					// 01-03-2017 populates the Singleton map that stores the PLMXML id value against the Element instance
					ReaderSingleton.getReaderSingleton().setId2ElemObjMap(element.getId(), element);
					//uniqueElems.add(element.getOBID());
					String dd = WriterUtils.getAttributeValue(element, "id");
					if(dd == null){
						dd = element.getOBID();
					}
					uniqueElems.add(dd);
				}else {
					LOGGER.debug("Relation {} already exists", tObid);
				}
			}

			boolean contains(final Element element) {
				//return uniqueElems.contains(element.getOBID());
				String dd = WriterUtils.getAttributeValue(element, "id");
				if(dd == null){
					dd = element.getOBID();
				}


				//logger.info("In contains Method : "+dd +" in uniqueElems list : "+uniqueElems.contains(dd));
				return uniqueElems.contains(dd);
			}
		}


		final AddElementHepler helper = new AddElementHepler();
		if (delta) {
			String dd = WriterUtils.getAttributeValue(element, "id");
			if(dd == null){
				dd = element.getOBID();
			}

			if(dd == null){
				dd = element.getAppLabel();
			}



			if (state != null) {

				switch (state) {
				case Missing:
					if (checkParentForVariantCfg()) {
						if (parent.state != State.Missing) {
							helper.add(parent.element);
							helper.add(parent);
						}
					}
					break;
				case New:
					// check if this is only a new relation
					helper.add(element);
					helper.add(this);
					if (checkParentForVariantCfg()) {
						helper.add(parent.element);
						helper.add(parent);
					}
					break;
				case Modified:
					// if the element has a trafo change, then check the parent, it
					// must be a Lage object
					if ((modType & CHANGE_TRANS) != 0 && IConstants.j0SDLage.equals(parent.getClazz())) {
						helper.add(parent.element);
						helper.add(parent);
					} else if ((modType & CHANGE_VERSION) != 0) {
						helper.add(element);
						helper.add(this);
					} else if ((modType & CHANGE_NAME) != 0 || (modType & CHANGE_LCS) != 0 || (modType & CHANGE_HNUMBER) != 0
							|| (modType & CHANGE_PROJECT) != 0) {
						helper.add(element);
					}
					if (parent != null && parent.state == State.Modified && (parent.modType & CHANGE_VERSION) != 0) {
						helper.add(element);
					}
					else if (parent != null && parent.state == State.Modified && (parent.modType & CHANGE_DYNDIA) != 0) {
						helper.add(element);
					}
					break;
				}
			} else {
				// check if the parent has a version change


				if (parent != null && parent.state == State.Modified && (parent.modType & CHANGE_VERSION) != 0) {
					helper.add(element);
				} else if (checkParentForVariantCfg()) {
					if (helper.contains(parent.element)) {
						helper.add(element);
					} else {
						for (TreeElement treeChild : parent.getChilds()) {

							//if (treeChild.getState() == State.Missing || treeChild.getState() == State.New || treeChild.getState() == State.Modified  ) {
							if (treeChild.getState() == State.Missing || treeChild.getState() == State.New ) {
								helper.add(element);
								break;
							}
						}
					}
				}
			}
		} else {
			helper.add(element);
			if(helper != null && this != null)
			{
				try{
					helper.add(this);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println(e.getLocalizedMessage());
				}
			}
		}
		String stopClass = getStopClass();

		boolean processChildren = stopClass == null || !stopClass.equals(element.getClazz());
		if (processChildren) {
			for (TreeElement child : getChilds()) {

				child.getElementsForXML(elements, relations, uniqueElems, uniqueRels, delta);
			}
		}
	}

	private Object[] getProjectList(List<Element> elements) {

		Object[] ret = new Object[2];
		Set<String> list = new HashSet<String>();
		String mainPrj = null;

		// check if the input vehicle type is a DMU Truck
		if(ReaderSingleton.getReaderSingleton().getFzgTypeName() != null) {
			// executes a different logic for setting the main project for the truck structure
			if(ReaderSingleton.getReaderSingleton().getFzgTypeName().equals(PreferenceConstants.P_FZG_TYP_LKW)) {
				if(ReaderSingleton.getReaderSingleton().getBcsProjectName() != null) {
					mainPrj = ReaderSingleton.getReaderSingleton().getBcsProjectName(); 
				}
			}
		}

		// Amit: this logic is for Smaragd elements 
		for (Element element : elements) {
			if(ReaderSingleton.getReaderSingleton().getFzgTypeName() != null) {
				// checks for a non-dmu truck xml file 
				if(ReaderSingleton.getReaderSingleton().getFzgTypeName() == PreferenceConstants.P_FZG_TYP_PKW) {	
					if (mainPrj == null && (IConstants.j0SDiaBR.equals(element.getClazz()) || IConstants.j0SDHMod.equals(element.getClazz()))) {
						mainPrj = element.getUserValues().get(IConstants.ProjectName);
					}
				}
			}
			// assignment for all elements other than BCS elements
			//if(!element.getClazz().equals(IConstants.TRUCK_CLASS_BCS)) {
			String proj = element.getUserValues().get(IConstants.ProjectName);

			if (proj != null && !list.contains(proj)) {
				list.add(proj);
			}
			//}
		}

		if(mainPrj == null)
		{
			LOGGER.warn("Main Project is not Defined...");  
		}

		ret[0] = mainPrj;
		ret[1] = list;
		return ret;
	}

	/**
	 * Returns the statistics of the structure.
	 * 
	 * @return A 1 dimensional array with the length of 3.<br>
	 *         int index 0 = number of occurrences<br>
	 *         int index 1 = number of occurrences with changes<br>
	 *         int index 2 = number of elements
	 */
	public int[] getStatistics() {
		int[] ret = new int[3];
		Set<String> elemOBIDs = new HashSet<String>();
		getStatistics(ret, elemOBIDs);
		ret[2] = elemOBIDs.size();
		return ret;
	}

	/**
	 * Calculates the statistics of the structure.
	 * 
	 * @param stat
	 *           Array of size 2 with stat[0] == number of occurrences, stat[1]
	 *           == number of occurrences with modifications.
	 * @param elemOBIDs
	 *           To count the number of elements.
	 */
	private void getStatistics(int[] stat, Set<String> elemOBIDs) {
		stat[0]++;
		if (state != null) {
			if (state == State.Modified) {
				// modifications are related to single parts, so count them just
				// one time
				// if (!elemOBIDs.contains(element.getOBID())) {
				if (!elemOBIDs.contains(WriterUtils.getAttributeValue(element, "id"))) {
					stat[1]++;
				}
			} else {
				stat[1]++;
			}
		}
		//elemOBIDs.add(element.getOBID());
		elemOBIDs.add(WriterUtils.getAttributeValue(element, "id"));
		for (TreeElement child : getChilds()) {
			child.getStatistics(stat, elemOBIDs);
		}
	}

	/**
	 * Write the assembly id into DAT file, in case drawing handling through sendto package
	 * 
	 * @param sDatFileLoc
	 * 			- DAT file name with the location         
	 * @param assemblyId
	 *            - Assembly ID to be written in the DAT file
	 */
	private void writeAssemblyID4NxDrawing(String sDatFileLoc, String assemblyId){
		try{
			StringBuilder datFileOutput = new StringBuilder();   
			FileReader datFileReader =  new FileReader(sDatFileLoc);
			BufferedReader readDatFile = null;  
			boolean assemblyIdExists = false;
			if(datFileReader!=null){
				readDatFile = new BufferedReader(datFileReader);   
				String line = readDatFile.readLine();
				while(line!=null){
					if(line.contains("assembly_id")){
						assemblyIdExists = true;
						datFileOutput.append("assembly_id="+assemblyId+"\n");
					}else{
						datFileOutput.append(line+"\n");
					}
					line = readDatFile.readLine();
				}
				if(!assemblyIdExists)
					datFileOutput.append("assembly_id="+assemblyId+"\n");
				datFileReader.close();
			}

			FileWriter datFileWriter =  new FileWriter(sDatFileLoc);
			if(datFileWriter!=null 
					&& datFileOutput!=null){
				BufferedWriter writeAssemblyID = new BufferedWriter(datFileWriter);
				writeAssemblyID.write(datFileOutput.toString());
				writeAssemblyID.close();
			}

		}catch(Exception e){
			LOGGER.error(e.getMessage());		   
		}
	}

	public int writeTruckConfigItemElement(XMLStreamWriter streamWriter, int elemNmb) throws XMLStreamException, TruckException {

		if(ReaderSingleton.getReaderSingleton().getPart2EffIDMap() != null) {
			// iterate through the member map of the singleton
			for(Map.Entry<String, String> mapEntry : ReaderSingleton.getReaderSingleton().getPart2EffIDMap().entrySet()) {

				streamWriter.writeStartElement("e");
				streamWriter.writeAttribute(BaseElement.ID, IConstants.TRUCK_CLASS_C9TRCKCFG + "_" + mapEntry.getValue());
				streamWriter.writeAttribute(BaseElement.ELEM_NMB, Integer.toString(elemNmb++));
				streamWriter.writeAttribute("smaProject", ReaderSingleton.getReaderSingleton().getBcsProjectName());
				streamWriter.writeAttribute("tcType", "Item");
				streamWriter.writeAttribute("cdmType", IConstants.TRUCK_CLASS_C9TRCKCFG);
				streamWriter.writeAttribute("smaType", "");
				streamWriter.writeAttribute(BaseElement.APPLY_EFFECTIVITY, String.valueOf(true));
				streamWriter.writeAttribute("hc", Boolean.toString(true));
				streamWriter.writeEndElement();			   
			}
		}
		else {
			throw new TruckException("No information exists for baumuster to end item mapping: Exception thrown in Class: " + this.getClass().getName()
					+ " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), false, ConfigMapUtils.LOG_TYPE_ERROR);
		}

		return elemNmb;
	}

	public int writeTruckCfgItemConnection(XMLStreamWriter streamWriter, int elemNmb) {

		return elemNmb;

	}

	public void compareWithDBData( Element element)
	{
		long timeBefore	= 0L;
		long timeAfter	= 0L;

		boolean isBVPresent = true;
		boolean isDatasetPresent = false;
		String datasetType=null;

		if( element != null )
		{



			if(DBConnSingleton.getInstance().getmDBConnInst() == null)
			{
				//logger.info("EXCEPTION HAS BEEN ENCOUNTERED WHILE CREATING DB CONNECTION ");
				element.setAction("not-checked");
				//argumentsMap.put(PreferenceConstants.P_DBCONNECT,"false");
				return;
			}

			if(element.getClazz().equals("JT"))
			{
				//System.out.println("Class Name : "+element.getClazz());
				LOGGER.info("Processing object of Type : "+element.getClazz());
				if(jtObjectMap != null && jtObjectMap.size() > 0 )
				{

					String equivalentRef = element.getAttributes().get("equivalentRef");

					if(equivalentRef != null && jtObjectMap.get(equivalentRef) != null)
					{
						DBConnDatasetInfo datasetInfo = jtObjectMap.get(equivalentRef);
						String jtDBObjObid = datasetInfo.getmMapDatasetAttrs().get("object_desc");
						String jtObidPlmxml = WriterUtils.getAttributeValue(element, "id");
						if( jtDBObjObid == null && jtObidPlmxml != null )
						{

						}
						else if( jtDBObjObid != null && jtObidPlmxml != null )
						{
							ReaderSingleton.getReaderSingleton().addMapDbObjectSum("DirectModel");
							if(WriterUtils.getAttributeValue(element, "datasetm_puid") == null)
							{
								String datasetPUID = datasetInfo.getmMapDatasetAttrs().get("dataset_puid");
								MappedAttributes datasetPUIDAttrObj = new MappedAttributes("tagDataset", datasetInfo.getmMapDatasetAttrs().get("dataset_puid"),
										"ud", "s", true);
								//datasetPUIDAttrObj.setModified(true);
								element.getMappedElementsMap().get("DirectModel").add(datasetPUIDAttrObj);
							}
							if(!jtObidPlmxml.equals(jtDBObjObid))
							{
								element.setHasPropChanges(true);
								//Element jt3dmodelElem = jt3dModelMap.get(equivalentRef);
								/*if(jt3dmodelElem != null)
								{
									jt3dmodelElem.setHasPropChanges(true);
									jt3dmodelElem.setAction("change");
									logger.info("JT has changed :  jtObidPlmxml : "+jtObidPlmxml+ " jtDBObjObid : "+jtDBObjObid );
									logger.info("JT has change for 3dModel of  Part number : "+WriterUtils.getAttributeValue(jt3dmodelElem, "item_id"));
								}*/
							}
						}
					}
				}
			}
			Map<String, ArrayList<MappedAttributes>> map = element.getMappedElementsMap();
			if( map != null && map.size() > 0)
			{
				for (Entry<String, ArrayList<MappedAttributes>> entry : map.entrySet()) {
					String itemID = WriterUtils.getAttributeValue(element, "item_id");
					if( itemID == null)
						return;
					/*if(itemID.equals("C223    16"))
					{
						System.out.println("Welcome");
					}*/
					String itemRevID = WriterUtils.getAttributeValue(element, "item_revision_id");	
					endItem = ReaderSingleton.getReaderSingleton().getEndItem();
					try
					{

						LOGGER.info("Processing object of Type (TC Type) : "+entry.getKey()+"Revision  - (SMARAGD Type) : "+element.getClazz()+" Part number : "+itemID);
						//logger.info("Processing object of Type (SMARAGD Type) : "+element.getClazz());
						boolean isC9Model = false;
						if("C9Model".equals(entry.getKey()) )
						{
							isBVPresent = false;
							isDatasetPresent = true;
							datasetType="DirectModel";
							isC9Model = true;

							/*	if( itemID.equals("N910112008000_2"))
							{
								System.out.println("Welcome : ");
							}*/
						}


						// Calling Database & comparing with plmxml values.. preparing Delta..

						connBrokerObj = ReaderSingleton.getReaderSingleton().getDbConnBroker();
						connBrokerObj.setmItemID(itemID);
						connBrokerObj.setmItemRevID(itemRevID);
						connBrokerObj.setmObjType(entry.getKey()+"Revision");
						connBrokerObj.setmEndItemID(ReaderSingleton.getReaderSingleton().getEndItem());

						// logs the start time for each call
						timeBefore = System.currentTimeMillis();

						DBConnResponse resp = connBrokerObj.executeDBAction(TypeMaps.isTypeForOccEff(element.getClazz()),isBVPresent,isDatasetPresent,datasetType);
						timeAfter = System.currentTimeMillis();
						ReaderSingleton.getReaderSingleton().setMapQry2ExecTime("executeDBAction", (timeAfter-timeBefore));
						ReaderSingleton.getReaderSingleton().setMapQry2ExecuteFreq("executeDBAction");
						timeBefore = 0L;
						timeAfter = 0L;
						//connBrokerObj.manageDBConnect(true);

						if( resp != null && resp.isExceptionThrown())
						{
							LOGGER.error("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemID+" Rev ID : "+itemRevID+" of Type : "+entry.getKey()+"Revision");
							element.setAction("unset");
							element.setDBExceptionThrown(true);
							return;
						}
						
						if( resp != null && resp.geteCode()!= null && !resp.geteCode().equals("") && resp.geteCode().equals("001"))
						{
							LOGGER.error("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemID+" Rev ID : "+itemRevID+" of Type : "+entry.getKey()+"Revision");
							element.setAction("not-checked");
							return;
						}
						else if(resp != null && !resp.isExceptionThrown() && resp.getmMapAttrs() != null && resp.getmMapAttrs().size() <= 1)
						{
							//logger.info("No Data Found for Item ID:  No Mapping Attributes...... : "+itemID+" Rev ID : "+itemRevID+" of Type : "+entry.getKey()+"Revision");
							LOGGER.info("Object Not Found in Teamcenter DB. Action will be new");
							return;
						}
						else  if(resp != null && !resp.isExceptionThrown() && resp.getmMapAttrs() != null && resp.getmMapAttrs().size() > 1)
						{
							LOGGER.info("Object Found in Teamcenter DB ");
							//return;
						}

						ReaderSingleton.getReaderSingleton().addMapDbObjectSum(entry.getKey());
						//logger.info();
						//resp.printAttrMap();
						//logger.info("***** Start of Values from DATABASE *******");
						if(resp == null)
						{
							LOGGER.info("***** NO Map Attributes *******");
							LOGGER.info(itemID+" Rev ID : "+itemRevID+" of Type : "+entry.getKey()+"Revision");
						}
						LOGGER.info(itemID+" Rev ID : "+itemRevID+" of Type : "+entry.getKey()+"Revision");
						//System.out.println(itemID+" Rev ID : "+itemRevID+" of Type : "+entry.getKey()+"Revision");
						//resp.getmMapAttrs().forEach((key, value) -> System.out.println(key + ":" + value));
						//resp.getmMapAttrs().forEach((key, value) -> logger.info(key + ":" + value));
						//logger.info("***** END of Values from DATABASE *******");
						String item_puid = resp.getmMapAttrs().get("item_puid");
						if(item_puid != null && WriterUtils.getAttributeValue(element, "tagItem") == null)
						{
							MappedAttributes itemPUIDAttrObj = new MappedAttributes("tagItem", resp.getmMapAttrs().get("item_puid"),
									"i", "s", true);
							//itemPUIDAttrObj.setModified(true);
							element.getMappedElementsMap().get(entry.getKey()).add(itemPUIDAttrObj);
						}
						String rev_puid = resp.getmMapAttrs().get("rev_puid");
						if(rev_puid != null && WriterUtils.getAttributeValue(element, "tagItemRev") == null)
						{
							MappedAttributes itemRevPUIDAttrObj = new MappedAttributes("tagItemRev", resp.getmMapAttrs().get("rev_puid"),
									"r", "s", true);
							//itemRevPUIDAttrObj.setModified(true);
							element.getMappedElementsMap().get(entry.getKey()).add(itemRevPUIDAttrObj);
						}
						//logger.info("***** Start of Values from PLMXML *******");
						for (MappedAttributes attr : entry.getValue()) 
						{

							if(attr.getAttrName().equals("object_desc"))
							{
								//System.out.println("object_desc : "+attr.getAttrValue());
								LOGGER.info("object_desc : "+attr.getAttrValue());
							}
							if( attr.getAttrDataType().equals("d"))
							{
								String dbdateVal = resp.getmMapAttrs().get(attr.getAttrName());
								if(dbdateVal != null && !dbdateVal.equals(""))
								{
									SimpleDateFormat dbDateSimpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									Date dbDateValObj = dbDateSimpleFormat.parse(dbdateVal);//new SimpleDateFormat(DateDefinitions.SDF);
									String attrDateVal = attr.getAttrValue();
									Date attrDateValObj = DateDefinitions.SDF.parse(attrDateVal);//new SimpleDateFormat(DateDefinitions.SDF);

									//	System.out.println("attrDateVal :"+attrDateVal+"  --- dbdateVal : "+dbdateVal);
									long diff = attrDateValObj.getTime() - dbDateValObj.getTime();

									int diffhours = (int) (diff / (60 * 60 * 1000));
									if(diffhours < 0 || diffhours > 2)
									{
										//logger.info("Property Value changes :  Attribute Name : "+attr.getAttrName()+" PLMXML Value : "+attrDateValObj+" DB Value : "+dbDateValObj);
										LOGGER.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+itemID+" > < "+itemRevID+" > < PLMXML: "+attr.getAttrName()+" : "+attrDateValObj+" > < DB: "+attr.getAttrName()+dbDateValObj+" >");
										attr.setModified(true);
										if(!element.isHasPropChanges())
										{
											element.setHasPropChanges(true);
										}
									}
								}

							}
							else if( attr.getAttrDataType().equals("b") )
							{
								String dbBoolVal = resp.getmMapAttrs().get(attr.getAttrName());


								String attrBoolVal = attr.getAttrValue();

								//System.out.println("attrBoolVal :"+attrBoolVal+"  --- dbBoolVal : "+dbBoolVal);
								if(dbBoolVal != null && attrBoolVal != null)
								{
									if(dbBoolVal.equals("0") && attrBoolVal.equals("+") ||  dbBoolVal.equals("1") && attrBoolVal.equals("-"))
									{
										//logger.info("Property Value changes :  Attribute Name : "+attr.getAttrName()+" PLMXML Value : "+attrBoolVal+" DB Value : "+dbBoolVal);
										//<element OBID> <Item-ID><Rev-ID><action value><db property value><plmxml property value>
										LOGGER.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+itemID+" > < "+itemRevID+" > < PLMXML: "+attr.getAttrName()+" : "+attrBoolVal+" > < DB: "+attr.getAttrName()+" : "+dbBoolVal+" >");
										attr.setModified(true);
										if(!element.isHasPropChanges())
										{
											element.setHasPropChanges(true);
										}
									}
								}
								else 
								{
									attr.setModified(true);
									if(!element.isHasPropChanges())
									{
										element.setHasPropChanges(true);
									}
									if(dbBoolVal == null)
									{
										dbBoolVal = "";
									}
									//logger.info("Property Value not in DB :  Attribute Name : "+attr.getAttrName()+" PLMXML Value : "+attrBoolVal+" DB Value : ");
									LOGGER.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+itemID+" > < "+itemRevID+" > < PLMXML: "+attr.getAttrName()+" : "+attrBoolVal+" > < DB: "+attr.getAttrName()+" : "+dbBoolVal+" >");

								}
							}
							else if( attr.getAttrDataType().equals("f") )
							{
								String dbFloatVal = resp.getmMapAttrs().get(attr.getAttrName());


								String attrFloatVal = attr.getAttrValue();

								//	System.out.println("attrFloatVal :"+attrFloatVal+"  --- dbFloatVal : "+dbFloatVal);
								if(dbFloatVal != null && attrFloatVal != null)
								{
									if(Float.valueOf(dbFloatVal)== Float.valueOf(attrFloatVal))
									{
										//logger.info("Property Value changes :  Attribute Name : "+attr.getAttrName()+" PLMXML Value : "+attrFloatVal+" DB Value : "+dbFloatVal);
										LOGGER.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+itemID+" > < "+itemRevID+" > < PLMXML: "+attr.getAttrName()+" : "+attrFloatVal+" > < DB: "+attr.getAttrName()+" : "+dbFloatVal+" >");

										attr.setModified(true);
										if(!element.isHasPropChanges())
										{
											element.setHasPropChanges(true);
										}
									}
								}
								else 
								{
									attr.setModified(true);
									if(!element.isHasPropChanges())
									{
										element.setHasPropChanges(true);
									}
									if(dbFloatVal == null )
									{
										dbFloatVal = "";
										//logger.info("Property Value not in DB :  Attribute Name : "+attr.getAttrName()+" PLMXML Value : "+attrFloatVal+" DB Value : ");
										LOGGER.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+itemID+" > < "+itemRevID+" > < PLMXML: "+attr.getAttrName()+" : "+attrFloatVal+" > < DB: "+attr.getAttrName()+" : "+dbFloatVal+" >");
									}
								}
							}
							else 
							{

								if(  attr.getAttrDataType().equals("s") && (attr.getAttrName().equals("c9PartNumber") || attr.getAttrName().equals("tagItem") || attr.getAttrName().equals("tagOCC") || attr.getAttrName().equals("tagItemRev") || attr.getAttrName().equals("item_id")  || attr.getAttrName().equals("id") || attr.getAttrName().equals("item_revision_id")))  
								{
									continue;
								}
								else if (isC9Model && attr.getAttrName().equals("c9Material2"))
								{
									continue;
								}
								else if( resp.getmMapAttrs().get(attr.getAttrName()) != null && !attr.getAttrValue().equals(resp.getmMapAttrs().get(attr.getAttrName())))
								{
									//logger.info("Property Value changes :  Attribute Name : "+attr.getAttrName()+" PLMXML Value : "+attr.getAttrValue()+" DB Value : "+resp.getmMapAttrs().get(attr.getAttrName()));
									LOGGER.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+itemID+" > <"+itemRevID+" > < PLMXML: "+attr.getAttrName()+" : "+attr.getAttrValue()+" > < DB: "+attr.getAttrName()+" : "+resp.getmMapAttrs().get(attr.getAttrName())+" >");
									attr.setModified(true);
									if(!element.isHasPropChanges())
									{
										element.setHasPropChanges(true);
									}
								}
								else if( resp.getmMapAttrs().get(attr.getAttrName()) == null && attr.getAttrValue() != null)
								{
									//logger.info("Property Value changes :  Attribute Name : "+attr.getAttrName()+" PLMXML Value : "+attr.getAttrValue()+" No values set in DB ");
									LOGGER.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+itemID+" > < "+itemRevID+" > < PLMXML: "+attr.getAttrName()+" : "+attr.getAttrValue()+" >");

									attr.setModified(true);
									if(!element.isHasPropChanges())
									{
										element.setHasPropChanges(true);
									}
								}

							}
							/*if(element.isHasPropChanges())
							{
								logger.info("***** Has properties change : *******");
							}*/
						}
						if(!element.isHasPropChanges())
						{
							LOGGER.info("Object Found in Teamcenter DB but no changes detected. Action will be none");
						}
						//	logger.info("***** End of Values from PLMXML *******");

						if(isC9Model)
						{
							Vector<DBConnDatasetInfo> datasetInfoVec = resp.getmMapDatasets().get(datasetType);
							/*if(datasetInfoVec != null)
							{

								if( itemID.equals("N910112008000_2"))
								{
									System.out.println("Welcome : ");
								}
								System.out.println("No of Datasets  ::: "+datasetInfoVec.size()+" for ITEM ID : "+itemID);
								logger.info("No of Datasets  ::: "+datasetInfoVec.size()+" for ITEM ID : "+itemID);
							}*/
							if(datasetInfoVec != null && datasetInfoVec.size() > 0)
							{
								DBConnDatasetInfo latestDBConnDatasetInfo = null;
								String lastModDate =  null;
								SimpleDateFormat dbDateSimpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								for (DBConnDatasetInfo dbConnDatasetInfo : datasetInfoVec) 
								{
									if(dbConnDatasetInfo != null && dbConnDatasetInfo.getmMapDatasetAttrs() != null && dbConnDatasetInfo.getmMapDatasetAttrs().size() > 0)
									{
										String dblastModDate = dbConnDatasetInfo.getmMapDatasetAttrs().get("mod_date");
										if(dblastModDate != null && !dblastModDate.equals(""))
										{
											//System.out.println("Last Modified Date :: "+dblastModDate);

											if( lastModDate == null)
											{
												lastModDate = dblastModDate;	
												latestDBConnDatasetInfo = dbConnDatasetInfo;
											}
											else
											{
												Date dbDateValObj = dbDateSimpleFormat.parse(dblastModDate);
												Date preDateValObj = dbDateSimpleFormat.parse(lastModDate);
												if (dbDateValObj != null && preDateValObj != null)
												{
													if(dbDateValObj.after(preDateValObj))
													{
														lastModDate = dblastModDate;	
														latestDBConnDatasetInfo = dbConnDatasetInfo;
													}
												}
											}

										}
									}
								}
								//for (DBConnDatasetInfo dbConnDatasetInfo : datasetInfoVec) 
								//	{
								if(latestDBConnDatasetInfo != null && latestDBConnDatasetInfo.getmMapDatasetAttrs() != null && latestDBConnDatasetInfo.getmMapDatasetAttrs().size() > 0)
								{
									String jtObjObid = latestDBConnDatasetInfo.getmMapDatasetAttrs().get("object_desc");
									if( jtObjObid != null )
									{
										//logger.info("C9Model OBID : "+jtObjObid);
										String refID = element.getAttributes().get("id");
										if(refID != null){
											jtObjectMap.put(refID, latestDBConnDatasetInfo);
											//jt3dModelMap.put(refID,element);
										}
									}
								}
								//}
							}
						}

						if(resp.getmMapEffDates() != null)
						{
							HashMap<String, String> effcDates = resp.getmMapEffDates();
							if(effcDates.size() == 2)
							{
								//element.setApplyEffectivityNone(true);
								//isEffectivityClosed(effcDates.get(1));
							}
						}


						//	resp.print();
						//childBomlineMaps = resp.getmMapChildItem2BOMInfo();
						childLineDBResMap.put(WriterUtils.getAttributeValue(element, "id"), resp);
						String action = "none";
						if(item_puid == null && rev_puid == null)
						{
							action = "new";
						}
						if((item_puid != null || rev_puid != null) && element.isHasPropChanges())
						{
							action = "change";
							//logger.info("Object Found in Teamcenter DB but no changes detected. Action will be none");
						}
						LOGGER.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+WriterUtils.getAttributeValue(element, "item_id")+" > < "+WriterUtils.getAttributeValue(element, "item_revision_id")+" > < Action : "+action);

					}
					catch (Exception e) 
					{
						System.out.println("Error Message :: "+e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
	}


	public void validateChildBOMLines(TreeElement treeElement)
	{
		if(treeElement != null)
		{
			if(childLineDBResMap != null && childLineDBResMap.size() > 0)
			{
				DBConnResponse dbConnResp = childLineDBResMap.get(treeElement.getOBID());

				if(dbConnResp != null ) 
				{
					HashMap<String, HashMap<String, DBConnBOMInfo>> childBomLinesMap = dbConnResp.getmMapChildItem2BOMInfo();
					boolean ischildBomModified = false;
					if(treeElement.getChilds() != null && treeElement.getChilds().size() > 0)
					{
						for(int i=0; i < treeElement.getChilds().size(); i++)
						{
							String itemID = WriterUtils.getAttributeValue(treeElement.getChilds().get(i).getElement(), "item_id");
							String itemRevID = WriterUtils.getAttributeValue(treeElement.getChilds().get(i).getElement(), "item_revision_id");
							if( itemID != null )
							{
								HashMap<String, DBConnBOMInfo> childLineFromDB = childBomLinesMap.get(itemID);
								boolean matchfound = false;
								if(childLineFromDB != null)
								{
									Object[] ddaList =  childLineFromDB.keySet().toArray();
									DBConnBOMInfo dda = null;
									String ddaOccPuId = null;
									boolean isParentOccEff = TypeMaps.isTypeForOccEff(treeElement.getClazz());

									for (Object object : ddaList) 
									{
										if( object != null )
										{
											if(childLineFromDB.get(object) != null)
											{
												//logger.info("Welcome : Child Rel Count : "+childLineFromDB.get(object).getmMapPSOccAttrs().get(DBConnUtilities.COL_OCC_RELCOUNT));
												String dbRelCnt = childLineFromDB.get(object).getmMapPSOccAttrs().get(DBConnUtilities.COL_OCC_RELCOUNT);
												childLineFromDB.get(object).getmMapPSOccAttrs().get(DBConnUtilities.COL_OCC_RELCOUNT);
												String plmxmlRelCnt = treeElement.getChilds().get(i).getRelCount();
												if((dbRelCnt != null && dbRelCnt.equals(plmxmlRelCnt)))
												{
													dda = childLineFromDB.get(object);//(DBConnBOMInfo) object;
													ddaOccPuId = (String) object;
													//logger.info("Trafo from PLMXML : "+treeElement.getChilds().get(i).getTransform());
													double[] t = treeElement.getChilds().get(i).getTransform();
													break;
												}
											}
										}
									}
									if(dda != null)
									{
										//System.out.println("Item ID : "+itemID+" ::: "+dda.getmMapPSOccAttrs().get(DBConnUtilities.COL_OCC_RELCOUNT)+ " : Welcome : Rel Count : "+treeElement.getChilds().get(i).getRelCount());
										//logger.info("Item ID : "+itemID+" ::: "+dda.getmMapPSOccAttrs().get(DBConnUtilities.COL_OCC_RELCOUNT)+ " : Welcome : Rel Count : "+treeElement.getChilds().get(i).getRelCount());

										HashMap<String, String> occAttrMap = dda.getmMapPSOccAttrs();

										//TODO: rename
										String action = "none";
										
										LOGGER.info("ITEM ID ::::: "+itemID);
										if(treeElement.getChilds().get(i).getTransform() != null )
										{
											String[] trafo = new String[]{"v1","v2","v3","v4","v5","v6","v7","v8","v9","v10","v11","v12","v13","v14","v15","v16"};
											int k = 0;

											for (String tr : trafo) {
												double transFormVal = treeElement.getChilds().get(i).getTransform()[k];
												int decimalPlaces = 0;
												if (occAttrMap.get(tr) != null) {
													// SMA-205
													decimalPlaces = 16;
													transFormVal = Precision.round(transFormVal, decimalPlaces);

													// logger.info(tr+" ---
													// "+Double.valueOf(occAttrMap.get(tr)).doubleValue() +" :: "+
													// transFormVal);
												}

												// if(occAttrMap.get(tr) != null &&
												// Double.valueOf(occAttrMap.get(tr)).doubleValue() !=
												// treeElement.getChilds().get(i).getTransform()[k])
												if (occAttrMap.get(tr) != null && Double.valueOf(occAttrMap.get(tr))
														.doubleValue() != transFormVal) {
													// SMA-205
													ischildBomModified = true;
													action = "change";

													// logger.info("Trafo matrix has some changes..."+tr+" DB Value :
													// "+occAttrMap.get(tr)+" plmxml Value :
													// "+treeElement.getChilds().get(i).getTransform()[k]);
													LOGGER.info("Test ::: Trafo has some changes : Action : " + action);
													LOGGER.info("< " + obid + " > < " + itemID + " > < " + itemRevID
															+ " > :: Trafo Matrix has some changes... " + tr + " < "
															+ treeElement.getChilds().get(i).getTransform()[k] + " > < "
															+ occAttrMap.get(tr) + " >");
													treeElement.getChilds().get(i)
															.setTrafoHasChanges(ischildBomModified);
													break;
												}
												k++;
											}
										}
										HashMap<String, ArrayList<MappedAttributes>> maaaap = treeElement.getChilds().get(i).getElement().getMappedElementsMap();
										Object[] maaaapKeys = maaaap.keySet().toArray();
										
										// save code rule for later comparaison when addng the bom!
										String dbCodeRule = occAttrMap.get(DBConnUtilities.COL_OCC_CODERULE);
										if (dbCodeRule!=null) {
											treeElement.getChilds().get(i).setCodeRule(dbCodeRule);
										}
										
										Element relElement = treeElement.getChilds().get(i).getRelatingElement();
										String codeRule = relElement.getUserValues().get("j0CodeRule");
										if (codeRule != null && !codeRule.isEmpty()) { // CodeRule soll nicht entfernt werden
											if (dbCodeRule == null || !dbCodeRule.equals(codeRule)) {
												ischildBomModified = true;
												action = "change";
											}
										}
										
										String occTag = occAttrMap.get("occ_puid");
										String relCount = occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT);
										if(occTag == null)
										{
											if(ddaOccPuId != null )
											{
												occTag =ddaOccPuId;
											}
											else
											{
												occTag = "";
											}

										}


										ArrayList<MappedAttributes> valMap = treeElement.getChilds().get(i).getElement().getMappedElementsMap().get(maaaapKeys[0]);
										//logger.info("Occerence Tag :: "+occTag);
										String occObid = WriterUtils.getAttributeValue(treeElement.getChilds().get(i).getElement(), "Id");
										if(occObid == null)
										{
											occObid = "";
										}
										//if(ReaderSingleton.getReaderSingleton().getOccConnLogger() != null) {

										//ReaderSingleton.getReaderSingleton().getOccConnLogger().write(WriterUtils.getAttributeValue(treeElement.getElement(), "item_id")+" : "+WriterUtils.getAttributeValue(treeElement.getElement(), "item_revision_id")+" : "+itemID +" : "+relCount+" : "+occTag+"\n"); // relCount
										ReaderSingleton.getReaderSingleton().addOccConnReaderLogList(WriterUtils.getAttributeValue(treeElement.getElement(), "item_id")+" : "+WriterUtils.getAttributeValue(treeElement.getElement(), "item_revision_id")+" : "+itemID +" : "+relCount+" : "+occTag+"\n");
										//System.out.println("Mapping info Map Size"+ReaderSingleton.getReaderSingleton().getMappingInfoMap().size());
										//}
										MappedAttributes tagOccMappAttr = new MappedAttributes("tagOCC", occTag,"r", "s", false);
										valMap.add(tagOccMappAttr);

										treeElement.getChilds().get(i).getElement().getMappedElementsMap().put(((String) maaaapKeys[0]), valMap);
										treeElement.getChilds().get(i).setTagOcc(occTag);

										HashMap<String, HashMap<String, String>> endItem2effMap = dda.getmMapEndItem2Eff();

										//HashMap<String, String> occAttrMap = dda1.getmMapPSOccAttrs();
										if(occAttrMap != null && occAttrMap.size() > 0)
										{
											if(occAttrMap.get(DBConnUtilities.COL_OCC_RELCOUNT).equals(treeElement.getChilds().get(i).getRelCount()))
											{


												//	HashMap<String, HashMap<String, String>> endItem2effMap = dda1.getmMapEndItem2Eff();
												if (endItem2effMap != null && endItem2effMap.size() > 0) {
													Object[] endItem2effMapKeys = endItem2effMap.keySet().toArray();
													// HashMap<String, String> effectivityDatesMap =
													// endItem2effMap.get(endItem2effMapKeys[0]);
													ArrayList<String> refConfigList = new ArrayList<>();
													for (Object refCName : endItem2effMapKeys) {
														refConfigList.add((String) refCName);

													}

													if (refConfigList.size() > 0 && refConfigList.contains(endItem)) {
														HashMap<String, String> effectivityDatesMap = endItem2effMap
																.get(endItem);
														if (isEffectivityClosed(effectivityDatesMap.get(1))) {
															action = "change";
															setEffectivityClosed(true);
															ischildBomModified = true;
															LOGGER.info("Test ::: effectivity closed :   Action : "
																	+ action);
															// adding logs
														}
													} else {
														action = "change";
														ischildBomModified = true;
														LOGGER.info("Test :::  END Item not SET : Action : " + action);
													}

												}
												else
												{
													if(isParentOccEff || ischildBomModified)
													{
														action = "change";
														LOGGER.info("Test ::: effectivity changes & END Item not SET : Action : "+action);
														ischildBomModified = true;
													}

												}
											}

										}
										treeElement.getChilds().get(i).setAction(action);
										treeElement.getChilds().get(i).setBomLineHasChanges(ischildBomModified);

									}else
									{   //TODO: commit in nächste version
										LOGGER.info(String.format("Child with itemId %s and relcount %s is not found in TC", itemID, treeElement.getChilds().get(i).getRelCount()));
										//treeElement.getChilds().get(i).setAction("new");
										//treeElement.setBomLineHasChanges(true);
									}
								}
								else
								{
                                    //TODO: commit in nächste version 
									LOGGER.info(String.format("Child with itemId %s and relcount %s is not found in TC", itemID, treeElement.getChilds().get(i).getRelCount()));
									//treeElement.getChilds().get(i).setAction("new");
									//treeElement.setBomLineHasChanges(true);
									
									treeElement.getChilds().get(i).setAction(action);
									treeElement.getChilds().get(i).setBomLineHasChanges(ischildBomModified);
								}
							}
						}
					}
					//	System.out.println("Welcome ::::: TreeElement.Chidren : "+treeElement.getChilds().size()+" childBomLines :: "+childBomLinesMap.size());
					HashMap<String, ArrayList<MappedAttributes>> maaaap = treeElement.getElement().getMappedElementsMap();
					Object[] maaaapKeys = maaaap.keySet().toArray();

					ArrayList<MappedAttributes> valMap = treeElement.getElement().getMappedElementsMap().get(maaaapKeys[0]);

					String bvTag = dbConnResp.getmMapAttrs().get("bv_puid");
					if(bvTag == null)
					{
						bvTag ="";
					}


					MappedAttributes tagBvMappAttr = new MappedAttributes("tagBV", bvTag,"r", "s", false);
					//logger.info("BomView Tag (bv_puid)"+bvTag);

					//valMap.add(tagBvMappAttr);
					treeElement.setTagBV(bvTag);

					String bvrTag = dbConnResp.getmMapAttrs().get("bvr_puid");
					if(bvrTag == null)
					{
						bvrTag ="";
					}

					if(bvTag != null & !bvrTag.equals(""))
					{
						ReaderSingleton.getReaderSingleton().addOccConnReaderLogList(WriterUtils.getAttributeValue(treeElement.getElement(), "item_id")+" : "+WriterUtils.getAttributeValue(treeElement.getElement(), "item_revision_id")+" : "+bvTag+" : "+bvrTag+"\n");
					}
					MappedAttributes tagBvrMappAttr = new MappedAttributes("tagBVR", bvrTag,"r", "s", false);

					//logger.info("BomViewRevision Tag (bvr_puid)"+bvrTag);
					//valMap.add(tagBvrMappAttr);
					treeElement.setTagBVR(bvrTag);
					//treeElement.getElement().getMappedElementsMap().put(((String) maaaapKeys[0]), valMap);
				}
			}
		}
	}

	// XMLStreamWriter streamWriter, TreeElement child,boolean isOccEff, ElementVersionMap versMap,TreeElement parentTreeElement, String occurence, HashMap<String, String> occAttrMap, XMLFileData xmlFileData, HashMap<String, String> occMap

	public void addBOMChildConnection(XMLStreamWriter streamWriter, TreeElement relation, TreeElement child, boolean isOccEff,ElementVersionMap versMap, XMLFileData xmlFileData, ArrayList<String> tagEndItemList) throws XMLStreamException
	{
		streamWriter.writeStartElement("c");
		/*if("Alqk0ggwgut1-usr_wgubwJi".equals(WriterUtils.getAttributeValue(versMap.getLatestVersionElement(child.element), "id")))
		{
			System.out.println("Welcom : ");
		}*/


		streamWriter.writeAttribute(BaseElement.ID, WriterUtils.getAttributeValue(versMap.getLatestVersionElement(child.element), "id"));

		// 12-12-2016 - Truck implementation only
		// checks if the curr'nt input being processed is Truck or not
		if(ReaderSingleton.getReaderSingleton().getFzgTypeName() != null) {
			if(ReaderSingleton.getReaderSingleton().getFzgTypeName().equals(PreferenceConstants.P_FZG_TYP_LKW)) {
				// checks if the current element being processed is not the top level truck object
				if(!child.getElement().getMappedElementsMap().containsKey(IConstants.TRUCK_CLASS_C9TRCK)) {

					// writes out the end item information that is stored in the associated Element instance
					if(Boolean.toString(isOccEff).equals("true")) {
						if(child.getElement().getEndItemIDs() != null && !child.getElement().getEndItemIDs().isEmpty()) {
							//-- Krishna -- Temorary fix for removing endItem Attribute  --------------------------------------------- commenting the below line
							streamWriter.writeAttribute(IConstants.INTXML_ENDITEM_ATTR, child.getElement().getEndItemIDs());
						}
					}
				}
			}
		}

		ArrayList<RefConfigMappingObject> refConfigMapObjList = ReaderSingleton.getReaderSingleton().getSumRefConfigMapObjs();
		boolean childOccEff = isOccEff;
		if(refConfigMapObjList != null && refConfigMapObjList.size() > 0 && xmlFileData.getRoot().getOBID().equals(relation.getOBID()))
		{
			childOccEff = true;
		}
		if(refConfigMapObjList != null && refConfigMapObjList.size() > 0 && childOccEff)
		{
			String varCond = VariantsUtil.createVarConditionForConnectionFromGlobalConstant( ICustom.TYPE_MAP.get(relation.getClazz()), ICustom.TYPE_MAP.get(child.getClazz()), refConfigMapObjList);
			if(varCond != null && !varCond.equals(""))
			{
				streamWriter.writeAttribute("var", varCond);
			}
		}

		String relCnt = child.getRelCount();
		if (relCnt == null) {
			relCnt = "1";
		}
		streamWriter.writeAttribute("relCount", relCnt);

		boolean isDataset = PLMUtils.isFileRef(child.getElement().getClazz());
		if(isDataset)
		{
			String tagDataset = WriterUtils.getAttributeValue(child.getElement(), "tagDataset");
			if(tagDataset == null)
			{
				tagDataset = "";
			}
			streamWriter.writeAttribute(BaseElement.TAG_DATASET, tagDataset);
		}
		else
		{
			//tagOcc = WriterUtils.getAttributeValue(child.getElement(), "tagOCC");
			tagOcc = child.getTagOcc();
			if(tagOcc == null || tagOcc.equals(""))
			{
				tagOcc = "";
				child.setAction("new");
			}
			/*if(ReaderSingleton.getReaderSingleton().getOccConnWriteLogger() != null) {
				//ReaderSingleton.getReaderSingleton().getOccConnLogger().write(WriterUtils.getAttributeValue(versMap.getLatestVersionElement(child.element), "id")+" : "+tagOcc+"\n");
				//System.out.println("Mapping info Map Size"+ReaderSingleton.getReaderSingleton().getMappingInfoMap().size());
			}*/  



			streamWriter.writeAttribute(BaseElement.TAG_OCC, tagOcc);
			//if(ReaderSingleton.getReaderSingleton().getOccConnWriteLogger() != null) {

			//ReaderSingleton.getReaderSingleton().getOccConnWriteLogger().write(WriterUtils.getAttributeValue(relation.getElement(), "item_id")+" : "+WriterUtils.getAttributeValue(relation.getElement(), "item_revision_id")+" : "+WriterUtils.getAttributeValue(child.getElement(), "item_id")+" : "+relCnt+" : "+tagOcc+"\n");
			ReaderSingleton.getReaderSingleton().addOccConnWriterLogList(WriterUtils.getAttributeValue(relation.getElement(), "item_id")+" : "+WriterUtils.getAttributeValue(relation.getElement(), "item_revision_id")+" : "+WriterUtils.getAttributeValue(child.getElement(), "item_id")+" : "+relCnt+" : "+tagOcc+"\n");
			//System.out.println("Mapping info Map Size"+ReaderSingleton.getReaderSingleton().getMappingInfoMap().size());


			//}
		}
		String codeRule = null;
		String codeRuleFromDB = null;
		if(ReaderSingleton.getReaderSingleton().isDbConnect())
		{



			Element relElement = child.getRelatingElement();

			if(relElement != null)
			{
				codeRule =relElement.getUserValues().get("j0CodeRule");

				if(codeRule != null)
				{
					codeRuleFromDB = child.getCodeRule();
					if(codeRuleFromDB == null)
					{
						if(child.getAction().equals("new") )
						{
							action = "new";
							//continue;
						}
						else
						{
							action = "change";
						}
						
						child.setAction(action);
					}
					else  if(!codeRuleFromDB.equals(codeRule)) 
					{
						action = "change";
						LOGGER.info(child.getOBID()+" Test ::: Action marked as change due to CodeRule: "+ action);
						
						child.setAction(action);
					}
					//	logger.info("Action : "+action+"After reading code Rule ");
				}
			}

			if(child.getAction().equals("none") && child.isEffectivityClosed())
			{
				action = "change";
				LOGGER.info(child.getOBID()+" Test ::: Action marked as change ::  effectivity closed : "+ action);
				child.setAction(action);
			}
			streamWriter.writeAttribute(BaseElement.ACTION, child.getAction());
			ReaderSingleton.getReaderSingleton().addOBIDActionLogList("\t"+WriterUtils.getAttributeValue(child.getElement(), "id")+" : "+child.getAction());
		}
		else
		{
			streamWriter.writeAttribute(BaseElement.ACTION, "not-checked");
			ReaderSingleton.getReaderSingleton().addOBIDActionLogList("\t"+WriterUtils.getAttributeValue(child.getElement(), "id")+" : "+"not-checked");
		}
		String trafoChanges = ""; 
		if(child.isTrafoHasChanges())
		{
			trafoChanges = "Trafo has changes";
			LOGGER.info("Test ::: Writing child BOMlines.... updated Action due to Trafo has changes: "+ action);
		}
		if(codeRule ==null)
		{
			codeRule = "No Changes in Trafo";
		}
		if(codeRuleFromDB== null)
			codeRuleFromDB = "";
		LOGGER.info("Child : < "+WriterUtils.getAttributeValue(child.getElement(), "id")+" > < "+tagOcc+" > < "+WriterUtils.getAttributeValue(child.getElement(), "item_id")+" > <"+trafoChanges+" > <"+child.getRelCount()+" > <"+relCnt+" > <"+codeRule+" > <"+codeRuleFromDB+" > < Action : "+action+" >");
		// writing code rule to connections...

		//if(child.getClazz().equals("j0SDPosV") || child.getClazz().equals("CD9SDPosV") || child.getClazz().equals("j0PrtVer") || child.getClazz().equals("C9Part"))
		{


			Element relElement = child.getRelatingElement();

			if(relElement != null)
			{
				/*	if("ujogEnbwgut1-usr_wgubUUJ".equals((WriterUtils.getAttributeValue(child.getElement(), "id"))))
				{
					System.out.println("Welcome::");
				}*/
				codeRule =relElement.getUserValues().get("j0CodeRule");

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
			}
			//String codeRule = WriterUtils.getAttributeValue(child.getRelatingElement(), "j0CodeRule");

		}


		// only write trafo and folder type if it is an occurrence
		if (child.isOccurrence()) {
			double[] trans = child.getTransform();
			if (trans == null) {
				trans = EINHEITS_MATRIX;
			}
			streamWriter.writeEmptyElement("t");
			for (int i = 0; i < trans.length; i++) {
				streamWriter.writeAttribute("v" + Integer.toString(i), Double.toString(trans[i]));
			}
			if (child.getFolderType() != null) {
				streamWriter.writeEmptyElement("fl");
				streamWriter.writeAttribute("t", child.getFolderType().toString());
			}
		}
		streamWriter.writeEndElement(); // C

	}



	/**
	 * method that finds a particular bom child based on the part number attribute
	 * @param partNumber : input part number attribute to search the bom childs
	 * @return
	 */
	public TreeElement findChild(String partNumber) {

		TreeElement child = null;

		if(partNumber != null && this.childs != null) {
			for(TreeElement inst : this.childs) {

				if(inst.getPartNumber() != null) {
					if(inst.getPartNumber().equals(partNumber))	{
						child = inst;
						break;
					}
				}
			}
		}
		return child;

	}

	/**
	 * method that removes a particular bom child from the current tree element's childs list
	 * @param partNumber : identifier to search for the particular bom child to remove
	 */
	public void removeChild(String partNumber) {

		if(partNumber != null && this.childs != null) {
			for(int iInx=0; iInx < this.childs.size(); ++iInx) {
				if(this.childs.get(iInx).getPartNumber() != null) {
					if(this.childs.get(iInx).getPartNumber().equals(partNumber))	{
						this.childs.remove(iInx);
						break;
					}
				}
			}
		}	
	}

	/**
	 * setter for the part number member variable
	 * @param partNumber : input value to set to the member variable
	 */
	public void setPartNumber(String partNumber) {

		if(partNumber != null) {
			this.partNumber = partNumber;
		}

	}

	public boolean isBomLineHasChanges() {
		return isBomLineHasChanges;
	}

	public void setBomLineHasChanges(boolean isBomLineHasChanges) {
		this.isBomLineHasChanges = isBomLineHasChanges;
	}

	public String getTagOcc() {
		return tagOcc;
	}

	public void setTagOcc(String tagOcc) {
		this.tagOcc = tagOcc;
	}

	public String getTagBV() {
		return tagBV;
	}

	public void setTagBV(String tagBV) {
		this.tagBV = tagBV;
	}

	public String getTagBVR() {
		return tagBVR;
	}

	public void setTagBVR(String tagBVR) {
		this.tagBVR = tagBVR;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isDuplicateBOM() {
		return isDuplicateBOM;
	}

	public void setDuplicateBOM(boolean isDuplicateBOM) {
		this.isDuplicateBOM = isDuplicateBOM;
	}

	public boolean isBOMProcessed() {
		return isBOMProcessed;
	}

	public void setBOMProcessed(boolean isBOMProcessed) {
		this.isBOMProcessed = isBOMProcessed;
	}

	public String getOccPuid() {
		return occPuid;
	}

	public void setOccPuid(String occPuid) {
		this.occPuid = occPuid;
	}

	public String getCodeRule() {
		return codeRule;
	}

	public void setCodeRule(String codeRule) {
		this.codeRule = codeRule;
	}

	public boolean isTrafoHasChanges() {
		return isTrafoHasChanges;
	}

	public void setTrafoHasChanges(boolean isTrafoHasChanges) {
		this.isTrafoHasChanges = isTrafoHasChanges;
	}

	public boolean isEffectivityClosed() {
		return isEffectivityClosed;
	}

	public void setEffectivityClosed(boolean isEffectivityClosed) {
		this.isEffectivityClosed = isEffectivityClosed;
	}
	public void printMappedAttrs(HashMap<String, ArrayList<MappedAttributes>> mappAttrsMap)
	{
		if(mappAttrsMap != null && mappAttrsMap.size()  > 0)
		{
			String[] keys = (String[]) mappAttrsMap.keySet().toArray();
			if(keys != null && keys.length > 0)
			{
				for (String key : keys) {
					ArrayList<MappedAttributes> mapAttrsList = mappAttrsMap.get(key);
					if(mapAttrsList != null && mapAttrsList.size() > 0)
					{
						for (MappedAttributes mappedAttribute : mapAttrsList) {
							if(mappedAttribute != null)
							{
								LOGGER.info("Attribute Name : "+mappedAttribute.getAttrName()+"    ---- Value :: "+mappedAttribute.getAttrValue());
							}
						}
					}
					
				}
			}
		}
	}
}
