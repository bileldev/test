package cdm.pre.imp.mod;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.MappedElements;
import cdm.pre.imp.writer.WriterUtils;

public class MappingVisitor extends AbstractVisitor {
	private final static Logger LOGGER = LogManager.getLogger(MappingVisitor.class.getName());
	
	public void visit(TreeElement element) throws NoSuchMethodException, SecurityException, ClassNotFoundException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (element == null) {
			return;
		}

		LOGGER.debug(String.format("Visit %s", element.getElement().getId()));
		
		/////////// DEBUGGING /////////////////////////
		//if (element.getPartName().equals("A0999064802_FP002")) {
		//	LOGGER.debug("debugging");
		//}
		//if (element.getElement().getOBID()!= null && element.getElement().getOBID().equals("usr_wgub00000000FE8B72C0")){
		//	LOGGER.debug("Stopp");
		//}
		
		// call to implement target data model mapping on the child Tree element
		MappedElements mapElem = new MappedElements();

		// sets the mapped element to the Tree Element instance
		element.setElement(mapElem.processElementForMapping(element.getElement()));

		// TODO: this don´t belongs to mapping normally should be done when converting
		// JSON model to PLMXML Model.
		if (element.getClazz() != IConstants.j0Cdi3D && element.getClazz() != IConstants.JT) {
			// 24-02-2017 - call to populate the OBID to the tree element instance once the
			// mapping is done
			if (element.getElement() != null && element.getElement().getMappedElementsMap() != null) {
				// String strItemID = WriterUtils.getAttributeValue(nextElement.getElement(),
				// "item_id");
				String obid = WriterUtils.getAttributeValue(element.getElement(), "id");
				if (obid != null) {
					// sets the OBID on the element object of the parent tree element object
					element.getElement().setOBID(obid);
					// sets the OBID on the tree element object
					element.setOBID(obid);
					// compareWithDBData(nextElement.getElement());
				}
				else
					LOGGER.info("No OBID set for element {} !", element.getElement().getId());
			}
		}

		visitChildren(element);

	}
}
