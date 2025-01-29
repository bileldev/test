package cdm.pre.imp.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cdm.pre.imp.reader.Element;
import cdm.pre.imp.reader.MappedAttributes;

public class ConfigMapItem extends BaseElement {

   public ConfigMapItem(final XMLStreamWriter streamWriter, final Element element) {
      super(streamWriter, element);
   }

   @Override
   public void writeObjectValues(final boolean hasChanges) throws XMLStreamException {
      // writer the attributes
	   
	  
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
						 if( attribute != null && attribute.getAttrValue() != null)
						 {
							 if(attribute.getAttrName().equals("id")) {
								 continue;
							 }
							 if(attribute.getAttrName().equals("tagOCC")) {
								 continue;
							 }
							 if(element.getAction().equals("none"))
							 {
								 if( attribute.isAttrReq() )
								 {
									 writeValue(attribute.getAttrDataType(), attribute.getAttrScope(), attribute.getAttrName(),
											 attribute.getAttrValue());
								 }
							 }
							 else if(element.getAction().equals("change"))
							 {
								 if( attribute.isAttrReq() || attribute.isModified() )
								 {
									 writeValue(attribute.getAttrDataType(), attribute.getAttrScope(), attribute.getAttrName(),
											 attribute.getAttrValue());
								 }
							 }
							 else 
							 {
								 writeValue(attribute.getAttrDataType(), attribute.getAttrScope(), attribute.getAttrName(),
										 attribute.getAttrValue());
							 }
								 
						 }
						 
						 
						 /*
						 attribute = mappedAttrs.get(k);
						 if( attribute != null && attribute.getAttrValue() != null)
						 {
							 if(!attribute.getAttrName().equals("id")) {
								 if(element.isHasPropChanges() )
								 {
									 if( attribute.isAttrReq() || attribute.isModified())
									 {
										 writeValue(attribute.getAttrDataType(), attribute.getAttrScope(), attribute.getAttrName(),
												 attribute.getAttrValue());
									 }
								 }
								 else  if(!element.isHasPropChanges() && element.isApplyEffectivityNone())
								 {
									 if( attribute.isAttrReq() || attribute.isModified())
									 {
										 writeValue(attribute.getAttrDataType(), attribute.getAttrScope(), attribute.getAttrName(),
												 attribute.getAttrValue());
									 }
								 }
								 else
								 {
									 writeValue(attribute.getAttrDataType(), attribute.getAttrScope(), attribute.getAttrName(),
											 attribute.getAttrValue());
								 }
							 }
						 }
					 */}
				 }
				 
			 }
		 }
	 }
	   
    /*  writeValue(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.Revision,
            userValues.get(IConstants.Revision) + "." + userValues.get(IConstants.Sequence));
      String dynDia = userValues.get(IConstants.j0DynDiaNumber);
      if (dynDia != null) {
         writeValue(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_DYNDIANR, dynDia);
      }
      writeValue(TDatatype.T_STRING, PropPlace.Item, ICustom.ItemID, userValues.get(IConstants.PartNumber));
      writeValue(TDatatype.T_STRING, PropPlace.ItemAndRev, ICustom.ObjectName, userValues.get(IConstants.Nomenclature));
      String geoPos = userValues.get(IConstants.j0SmaDia2GeoPos);
      if (geoPos != null) {
         writeValue(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_GEOPOS, "+".equals(geoPos) ? "Y" : "N");
      }
      String creationDate = userValues.get(IConstants.CreationDate);
      if (creationDate != null) {
         writeValue(TDatatype.T_DATE, PropPlace.ItemRevision, ICustom.ATTR_CREATIONDATE, smaDateToTcDate(creationDate));
      }
      String creator = userValues.get(IConstants.j0Creator);
      if (creator != null) {
         writeValue(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CREATOR, creator);
      }

      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_ChangeDes, userValues.get(IConstants.j0ChangeDescription));
      String eClass = element.getClazz();

      if (eClass.contentEquals(IConstants.j0PrtVer) || eClass.contentEquals(IConstants.j0StdPrt)
            || eClass.contentEquals(IConstants.j0CarCmp) || eClass.contentEquals(IConstants.j0BtePrt)
            || eClass.contentEquals(IConstants.j0LiePrt) || eClass.contentEquals(IConstants.j0BrePrt)) {
         writeValueLoc(TDatatype.T_STRING, PropPlace.Item, ICustom.ATTR_PARTNUMBER, userValues.get(IConstants.PartNumber));
         writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_WPRG, userValues.get(IConstants.j0WeightProg));
         writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_WREAL, userValues.get(IConstants.j0WeightReal));
         writeValueLoc(TDatatype.T_BOOL, PropPlace.ItemRevision, ICustom.ATTR_DMUrelevant, userValues.get(IConstants.j0DMUrelevant));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_ZGS, userValues.get(IConstants.j0ZGSNumber));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_NomenEn, userValues.get(IConstants.j0Nomenclature_en_us));
      }

      if (eClass.contentEquals(IConstants.j0SDiaBR) || eClass.contentEquals(IConstants.j0SDHMod)
            || eClass.contentEquals(IConstants.j0SDMod) || eClass.contentEquals(IConstants.j0SDSMod)
            || eClass.contentEquals(IConstants.j0SDPos) || eClass.contentEquals(IConstants.j0SDPosV)
            || eClass.contentEquals(IConstants.j0SDLage)) {
         String invalid = userValues.get(IConstants.j0Invalid);
         if (invalid != null) {
            if ("+".equals(invalid)) {
               invalid = "Y";
            } else if ("-".equals(invalid)) {
               invalid = "N";
            }
            writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_INVALID, invalid);
         }
      }
      if(eClass.contentEquals(IConstants.j0SDPosV)) {
    	  writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_AUSARTCR, userValues.get(IConstants.j0AusArtCR));
    	  writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CODERULE, userValues.get(IConstants.j0CodeRule));
    	  writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_STEER, userValues.get(IConstants.j0Steer));
      }

      if (eClass.contentEquals(IConstants.j0PrtVer) || eClass.contentEquals(IConstants.j0StdPrt)) {
         writeValueLoc(TDatatype.T_BOOL, PropPlace.ItemRevision, ICustom.ATTR_CERTIFICATION, userValues.get(IConstants.j0CertRel));
         writeValueLoc(TDatatype.T_BOOL, PropPlace.ItemRevision, ICustom.ATTR_SECURITY, userValues.get(IConstants.j0SecRel));
      }

      if (eClass.contentEquals(IConstants.j0PrtVer)) {
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_SURFPROT, userValues.get(IConstants.j0CPSurfProt));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_HNUMBER, userValues.get(IConstants.j0HNumber));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_PartCategory, userValues.get(IConstants.j0PartCategory));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPAuthor, userValues.get(IConstants.j0CPAuthor));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_COLOUR, userValues.get(IConstants.j0CPColor));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPDecor, userValues.get(IConstants.j0CPDecor));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPDepartment, userValues.get(IConstants.j0CPDepartment));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPFunctInstr, userValues.get(IConstants.j0CPFunctInstr));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_GENTOL, userValues.get(IConstants.j0CPGenTol));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPGenTol2, userValues.get(IConstants.j0CPGenTol2));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPGenTol3, userValues.get(IConstants.j0CPGenTol3));
         writeValueLoc(TDatatype.T_INTEGER, PropPlace.ItemRevision, ICustom.ATTR_NUMFEADS, userValues.get(IConstants.j0CPNumFeatureDS));
         writeValueLoc(TDatatype.T_INTEGER, PropPlace.ItemRevision, ICustom.ATTR_NUMFEADZ, userValues.get(IConstants.j0CPNumFeatureDZ));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_STATFTR, userValues.get(IConstants.j0CPStatFtr));
         writeValueLoc(TDatatype.T_DATE, PropPlace.ItemRevision, ICustom.ATTR_CPZDATE, userValues.get(IConstants.j0CPZDate));
         writeValueLoc(TDatatype.T_BOOL, PropPlace.ItemRevision, ICustom.ATTR_ESDCODE, userValues.get(IConstants.j0ESDCode));
         writeValueLoc(TDatatype.T_BOOL, PropPlace.ItemRevision, ICustom.ATTR_FDOC, userValues.get(IConstants.j0FdokRel));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_ISUV, userValues.get(IConstants.j0IsUVKZ));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_MSTDTAMTD, userValues.get(IConstants.j0MasterDataMethod));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_TOLERENCE, userValues.get(IConstants.j0Tolerancing));
         writeValueLoc(TDatatype.T_INTEGER, PropPlace.ItemRevision, ICustom.ATTR_VPDID, userValues.get(IConstants.j0VPDIdentNum));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_BSPRTNUM, userValues.get(IConstants.j0BasePartnumber));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_THEFTREL, userValues.get(IConstants.j0TheftRel));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_TOLERENCE, userValues.get(IConstants.j0Tolerancing));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_ESIGMRK, userValues.get(IConstants.j0EeSigMark));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_ESWSEC, userValues.get(IConstants.j0EeSwSec));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_SETTLED, userValues.get(IConstants.j0Settled));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_MATERIAL, userValues.get(IConstants.j0Material2));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPMatWEZ, userValues.get(IConstants.j0CPMatWEZ));
         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPMatAL, userValues.get(IConstants.j0CPMatAL));
      }

      if (eClass.contentEquals(IConstants.j0PrtVer) || eClass.contentEquals(IConstants.j0StdPrt)
            || eClass.contentEquals(IConstants.j0BrePrt)) {

         writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_FFKF, userValues.get(IConstants.j0ffKf));
      }
      if (eClass.contentEquals(IConstants.j0LiePrt))  {

    	  writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_LieferantSnr, userValues.get(IConstants.j0LieferantSnr));
      }*/
	 //writerMappedLifecycleState();
   }
}
