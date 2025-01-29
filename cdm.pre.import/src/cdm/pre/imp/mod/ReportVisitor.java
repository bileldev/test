package cdm.pre.imp.mod;

import java.lang.reflect.InvocationTargetException;

import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.ReaderSingleton;
import cdm.pre.imp.writer.WriterUtils;

public class ReportVisitor extends AbstractVisitor {
	public void visit(TreeElement element) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (element == null) {
			return;
		}

		if (element.getClazz() == IConstants.j0Cdi3D) {

			element.setBomLevel(element.getParent().getBomLevel() + 1);
			// 19/05/2017 - Amit - adding bom count information to the Singleton member
			// retrieve the item revision ids for the BOM parent and BOM child
			String parItemRevID = null; // item revision id of the parent item revision id
			String childItemRevID = null; // item revision id of the child item revision id
			if (element.getElement() != null && element.getParent().getElement().getMappedElementsMap() != null) {
				parItemRevID = WriterUtils.getAttributeValue(element.getParent().getElement(),
						IConstants.TC_ATTR_ITEMREVID);
			}

			if (element.getElement() != null && element.getElement().getMappedElementsMap() != null) {
				childItemRevID = WriterUtils.getAttributeValue(element.getElement(), IConstants.TC_ATTR_ITEMREVID);
			}

			ReaderSingleton.getReaderSingleton().setBOMParent2ChildMap(element.getParent().getOBID(),
					element.getPartNumber(), element.getParent().getPartName(), element.getParent().getPartNumber(),
					element.getParent().getBomLevel(), parItemRevID, childItemRevID);
			
		} else if (element.getClazz() != IConstants.JT) {
			// sets the bom level on the element object
			if (element.getParent() == null) { // if (currInst.equals(IConstants.ROOT_INSTANCE) ||
												// currInst.equals(IConstants.FIRST_INSTANCE)) {
				// sets the bom level to 1 for the part object of the root instance
				element.setBomLevel(1);

				// sets the part number of the next element instance to the singleton instance
				ReaderSingleton.getReaderSingleton().setRefFzgName(element.getPartNumber());
			} else {
				// sets the BOM Level
				if (element.getParent() != null && element != null) {
					element.setBomLevel(element.getParent().getBomLevel() + 1);
				}
			}
			
			// Amit - 08/03/2017 - added code for putting it to the output log file
			// retrieve the item revision ids for the BOM parent and BOM child
			String parItemRevID = null; // item revision id of the parent item revision id
			String childItemRevID = null; // item revision id of the child item revision id

			if (element.getParent().getElement() != null && element.getParent().getElement().getMappedElementsMap() != null) {
				parItemRevID = WriterUtils.getAttributeValue(element.getParent().getElement(),
						IConstants.TC_ATTR_ITEMREVID);
			}

			if (element.getElement() != null
					&& element.getElement().getMappedElementsMap() != null) {
				childItemRevID = WriterUtils.getAttributeValue(element.getElement(),
						IConstants.TC_ATTR_ITEMREVID);
			}

			// call to set the information required for the generation of the BOM report for
			// a car
			if (TreeElementFactoryFromPLMXML.vehicleType != null
					&& TreeElementFactoryFromPLMXML.vehicleType.equals(PreferenceConstants.P_FZG_TYP_PKW)) {
				ReaderSingleton.getReaderSingleton().setBOMParent2ChildMap(element.getParent().getOBID(),
						element.getPartNumber(), element.getParent().getPartName(), element.getParent().getPartNumber(),
						element.getParent().getBomLevel(), parItemRevID, childItemRevID);
			}
			
			
		}

		visitChildren(element);

	}
}
