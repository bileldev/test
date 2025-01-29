package cdm.pre.imp.mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdm.pre.imp.reader.Element;
import cdm.pre.imp.reader.IConstants;

/**
 * If of the same CDI identifier j0CTModSnr/j0CTModNumber more than one
 * different version is used inside the PLMXML file, this class will use always
 * the latest one. This happens as an example if CDI was linked but the versions
 * were not updated properly.
 * 
 * @author wikeim
 * 
 */
public class ElementVersionMap {
   private Map<String, Element> versionMap = new HashMap<String, Element>();
   private boolean              doMapping;

   private boolean isCDI(final Element elem) {
	   String clazz = elem.getClazz();
	   return IConstants.j0Cdi3D.equals(clazz) || IConstants.j0Cdi2D.equals(clazz);
   }
   
   private boolean isPart(final Element elem) {
	   String clazz = elem.getClazz();
	   return IConstants.j0BrePrt.equals(clazz) || IConstants.j0PrtVer.equals(clazz) || IConstants.j0StdPrt.equals(clazz); 
   }
   private boolean isTargetObject(final Element elem) {
	   return isCDI(elem) || isPart(elem);
   }

   private String createIdent(final Element elem) {
	   if (isCDI(elem)) {
		   return elem.getUserValues().get(IConstants.j0CTModSnr) + "_" + elem.getUserValues().get(IConstants.j0CTModNumber);
	   }
	   return elem.getUserValues().get(IConstants.PartNumber);
   }

   public ElementVersionMap(final List<Element> elements, boolean doMapping) {
      this.doMapping = doMapping;
      for (Element elem : elements) {
         if (isTargetObject(elem)) {
            String ident = createIdent(elem);
            Element cachedElem = versionMap.get(ident);
            if (cachedElem == null) {
               versionMap.put(ident, elem);
            } else {
               if (checkVersion(cachedElem, elem) > 0) {
                 versionMap.put(ident, elem);
               }
            }
         }
      }
   }
   
   private int checkVersion(final Element oldElem, final Element newElem) {
	   int oldRev = 0;
       int newRev = 0;
       if (!isCDI(oldElem)) {
    	   newRev = Integer.parseInt(newElem.getUserValues().get(IConstants.Revision));
    	   oldRev = Integer.parseInt(oldElem.getUserValues().get(IConstants.Revision));
       }
      /* int newseq = Integer.parseInt(newElem.getUserValues().get(IConstants.Sequence));
       int oldseq = Integer.parseInt(oldElem.getUserValues().get(IConstants.Sequence));*/
       int newseq = 0;
       if(newElem.getUserValues().get(IConstants.Sequence) != null )
       {
    	   newseq = Integer.parseInt(newElem.getUserValues().get(IConstants.Sequence));
       }
       int oldseq = 0;
       if(oldElem.getUserValues().get(IConstants.Sequence) != null)
       {
       oldseq = Integer.parseInt(oldElem.getUserValues().get(IConstants.Sequence));
       }
       
       return Integer.valueOf(newRev*1000 + newseq).compareTo(oldRev*1000 + oldseq);
   }
   
   public boolean isMappedElem(final Element elem) {
      if (!isTargetObject(elem) || !doMapping) {
         return false;
      }
      Element cachedElem = versionMap.get(createIdent(elem));
      return checkVersion(cachedElem, elem) != 0;
   }

   public Element getLatestVersionElement(final Element elem) {
      if (!isTargetObject(elem) || !doMapping) {
         return elem;
      } else {
         return versionMap.get(createIdent(elem));
      }
   }
}
