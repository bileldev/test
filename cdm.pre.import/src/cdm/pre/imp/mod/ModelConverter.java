package cdm.pre.imp.mod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.json.reader.JSONElelment;
import cdm.pre.imp.json.reader.JSONTreeElement;
import cdm.pre.imp.json.reader.TrafoUtils;
import cdm.pre.imp.reader.Element;
import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.PLMUtils;

public class ModelConverter {

	final private static Logger LOGGER = LogManager.getLogger(ModelConverter.class);

	/**
	 * @param jTreeElement
	 * @return
	 */
	public TreeElement createTreeElement(JSONTreeElement jTreeElement) {
		// ...
		// Missing how to set relatedelement

		Element pRelationElement = null;
		if (!PLMUtils.isFileRef(jTreeElement.getDataElement().getObjectType())) {
			pRelationElement = createReleatingElement(jTreeElement.getDataElement());
		}

		Element pElement = createElement(jTreeElement.getDataElement());

		// Setting equivalentRef
		if (PLMUtils.isFileRef(jTreeElement.getDataElement().getObjectType())) {
			// debug
			if (jTreeElement.getParent() == null) {
				LOGGER.error("Wrong JSON file: In the json model is the parent of file elment {} not defined !!",
						jTreeElement.getDataElement().getId());
			}

			pElement.getAttributes().put(IConstants.equivalentRef, jTreeElement.getParent().getDataElement().getId());
		}

		// Setting parent projectName
		if (jTreeElement.getParent() != null) {
			String parentProject = (String) jTreeElement.getParent().getDataElement().getAttributes()
					.get("ProjectName");
			if (parentProject != null) {
				pElement.setParentProjectName(parentProject);
			}
		}

		TreeElement pTreeElement = new TreeElement(pElement);
		pTreeElement.setRelatingElement(pRelationElement);

		for (JSONTreeElement jChild : jTreeElement.getChildren()) {
			TreeElement pchild = createTreeElement(jChild);
			pTreeElement.addChild(pchild);
		}

		return pTreeElement;
	}

	/**
	 * @param jElement
	 * @return
	 */
	public Element createElement(JSONElelment jElement) {
		// ...
		String pId;
		// OBID can be null like for j0NXAsm
		String obid = (String) jElement.getAttributes().get("OBID");

		// if (obid!= null && obid.equals("usr_wgub00000000FE8B72C0")){
		// LOGGER.debug("Stopp");
		// }

		/**
		 * Id used to uniquely identify the element in a delta export, because it
		 * contains elements of two structures.
		 */
		Map<String, String> userValues = getUserValuesFromJSONAttr(jElement);

		String tagName = getTagName(jElement);
		String appLabel = obid;
		// 30-01-2017 - additional attribute for Truck implementation only
		// String endItemIDs; // captures the baumuster specific end item id for the
		// baumuster children at all levels

		Map<String, String> attributes = getAttributesFromJson(jElement);

		// HashMap<String, ArrayList<MappedAttributes>> mappedElementsMap; // It is
		// filles by the mapping later
		// boolean hasPropChanges; / it is set later by the DB comaparaison
		// boolean isApplyEffectivityNone = false; // it is not really used
		// String action = null; // it is set later when comparing with the DB
		// ArrayList<ElemExprMapInfo> exprBlocksList; // set by the mapping
		// String updatedProjectName = null; // set later by he visitor

		Element plEement = new Element(tagName, attributes);
		plEement.setOBID(obid);
		plEement.setUserValues(userValues);
		plEement.setAppLabel(appLabel);

		LOGGER.debug("Convert Element obid : {}, tagName: {}", obid, tagName);

		return plEement;

	}

	public Element createReleatingElement(JSONElelment jElement) {
		// relAttributes
		// "Class"
		// "j0RelCount"
		// j0CodeRule

		Element pRelationElement = new Element("", null);

		// read MATRXxx attribute from JElement to Transform attribut of relatingElement
		// (new Element) which will be added to the parent.
		String transform_json = Arrays.toString(TrafoUtils.generateTrafoMatrix(jElement));

		String tranform_plmxml = convertTransformFormat(transform_json);
		pRelationElement.setTransform(tranform_plmxml);

		Map<String, String> userValues = getUserValuesFromJSONRelatingAttr(jElement);
		pRelationElement.setUserValues(userValues);

		// String codeRule = (String) jElement.getRelAttributes().get("j0CodeRule");
		// pRelationElement.getUserValues().put(IConstants.CodeRule, codeRule);

		// String relCount = (String) jElement.getRelAttributes().get("j0RelCount");
		// pRelationElement.getUserValues().put(IConstants.Relcount, relCount);

		return pRelationElement;

	}

	/**
	 * Convert the transform from json format to PLMXML format JSON format: [1.0,0
	 * 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0]
	 * PLMXML format: 1 0 0 0 0 1 0 0 0 0 1 0 0 0 -0.5000000000000000000 1
	 *
	 * @param transform_json
	 * @return
	 */
	private String convertTransformFormat(String transform_json) {
		String ret = transform_json.replace(",", "").replace("[", "").replace("]", "");
		return ret;
	}

	/**
	 * 
	 * location: when "Class" : "JT" then "filepath" -> location equivalenRef: when
	 * "Class" : "JT" then id of the parent element -> equivalenRef id partRef
	 * instanceRef name: "DisplayedName" -> name
	 * 
	 * @param jElement
	 * @return
	 */
	public Map<String, String> getAttributesFromJson(JSONElelment jEelement) {
		Map<String, String> attrs = new HashMap<>();
		if (jEelement != null) {
			// TODO creat a constant file for json ids

			attrs.put(IConstants.name, (String) jEelement.getAttributes().get("DisplayedName"));

			// attrs.put("id",(String) jTreeEelement.getAttributes().get("id"));
			attrs.put("id", jEelement.getId());

			if (PLMUtils.isFileRef(jEelement.getObjectType())) {
				if (jEelement.getAttributes().containsKey("filepath"))
					attrs.put(IConstants.location, (String) jEelement.getAttributes().get("filepath"));
			}
			/*
			 * else { String insatanceRef = getInstanceRefFromJson(jTreeEelement);
			 * if(insatanceRef != null) { attrs.put("instanceRefs", insatanceRef); }
			 * attrs.put("partRef",jTreeEelement.getDataElement().getId()); }
			 */
		}

		return attrs;
	}

	public String getInstanceRefFromJson(JSONTreeElement jTreeEelement) {
		String instaceRefStr = null;
		if (jTreeEelement != null & jTreeEelement.getChildren().size() > 0) {
			for (JSONTreeElement children : jTreeEelement.getChildren()) {
				if (children != null) {
					if (instaceRefStr == null) {
						instaceRefStr = children.getDataElement().getId();
					} else {
						instaceRefStr = instaceRefStr + " " + children.getDataElement().getId();
					}
				}
			}
		}
		return instaceRefStr;
	}

	private String getTagName(JSONElelment jElement) {
		String clazz = (String) jElement.getAttributes().get(IJSONConstants.CLASS);
		if (PLMUtils.isFileRef(clazz)) {
			return IConstants.CompoundRep;
		} else if (IConstants.j0Cdi3D.equals(clazz) || IConstants.j0Cdi2D.equals(clazz)) {
			return IConstants.CompoundRep;
		}

		return IConstants.Part;
	}

	public Map<String, String> getUserValuesFromJSONAttr(JSONElelment jElem) {
		jElem.getAttributes().entrySet().removeIf(entry -> entry.getKey().getClass().isArray());

		Map<String, String> ret = jElem.getAttributes().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

		return ret;

	}

	public Map<String, String> getUserValuesFromJSONRelatingAttr(JSONElelment jElem) {
		jElem.getRelAttributes().entrySet().removeIf(entry -> entry.getKey().getClass().isArray());

		Map<String, String> ret = jElem.getRelAttributes().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

		return ret;

	}

}
