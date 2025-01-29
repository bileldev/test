package cdm.pre.imp.writer;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cdm.pre.imp.reader.Element;
import cdm.pre.imp.reader.MappedAttributes;

public class ConfigMapModel extends BaseElement {

   public ConfigMapModel(final XMLStreamWriter streamWriter, final Element element) {
      super(streamWriter, element);
   }

   @Override
   public void writeObjectValues(final boolean hasChanges) throws XMLStreamException {
	   
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
							/* attribute = mappedAttrs.get(k);
							 if( attribute != null && attribute.getAttrValue() != null)
							 {
								 if(!attribute.getAttrName().equals("id")) {
									 writeValue(attribute.getAttrDataType(), attribute.getAttrScope(), attribute.getAttrName(),
									            attribute.getAttrValue());
								 }
							 }*/
							 
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
						 }
					 }
					 
				 }
			 }
			 //mappedElementMap.get("")
		 }
		// writerMappedLifecycleState();
	   
     /* writeValue(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.Revision, userValues.get(IConstants.Sequence));      
      // changed to handle NX drawing item id with suffix "_nx"
      if(userValues.get("Class").equals("j0Cdi2D")){
    	  if(NXPartFileManagerUtils.nxDrwgItemIdMap.containsKey(element.getId()))
    	  {
    		  writeValue(TDatatype.T_STRING, PropPlace.Item, ICustom.ItemID, NXPartFileManagerUtils.nxDrwgItemIdMap.get(element.getId())) ;
    	  }else{
    		  writeValue(TDatatype.T_STRING, PropPlace.Item, ICustom.ItemID,
    				  userValues.get(IConstants.j0CTModSnr) + "_" + userValues.get(IConstants.j0CTModNumber));
    	  }
      }else{
    	  writeValue(TDatatype.T_STRING, PropPlace.Item, ICustom.ItemID,
    			  userValues.get(IConstants.j0CTModSnr) + "_" + userValues.get(IConstants.j0CTModNumber));
      }
      writeValue(TDatatype.T_STRING, PropPlace.ItemAndRev, ICustom.ObjectName, userValues.get(IConstants.DataItemDesc));
      String creationDate = userValues.get(IConstants.CreationDate);
      if (creationDate != null) {
         writeValue(TDatatype.T_DATE, PropPlace.ItemRevision, ICustom.ATTR_CREATIONDATE, smaDateToTcDate(creationDate));
      }
      String creator = userValues.get(IConstants.j0Creator);
      if (creator != null) {
         writeValue(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CREATOR, creator);
      }

      String ctModNumber = userValues.get(IConstants.j0CTModSnr);
      if (ctModNumber != null) {
         writeValue(TDatatype.T_STRING, PropPlace.Item, ICustom.ATTR_PARTNUMBER, ctModNumber);
      }

      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_ChangeDes, userValues.get(IConstants.j0ChangeDescription));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_NomenEn, userValues.get(IConstants.j0Nomenclature_en_us));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPModTyp, userValues.get(IConstants.j0CPModTyp));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_HEIGHT, userValues.get(IConstants.j0Height));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_IXX, userValues.get(IConstants.j0Ixx));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_IXY, userValues.get(IConstants.j0Ixy));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_IXZ, userValues.get(IConstants.j0Ixz));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_IYY, userValues.get(IConstants.j0Iyy));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_IYZ, userValues.get(IConstants.j0Iyz));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_IZZ, userValues.get(IConstants.j0Izz));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_MATERIAL, userValues.get(IConstants.j0Material2));

      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_COGX, userValues.get(IConstants.j0CentreOfGravity_x));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_COGY, userValues.get(IConstants.j0CentreOfGravity_y));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_COGZ, userValues.get(IConstants.j0CentreOfGravity_z));
      writeValueLoc(TDatatype.T_DOUBLE, PropPlace.ItemRevision, ICustom.ATTR_CPArea, userValues.get(IConstants.j0CPArea));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPMatID, userValues.get(IConstants.j0CPMatID));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPMatWEZ, userValues.get(IConstants.j0CPMatWEZ));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPMatAL, userValues.get(IConstants.j0CPMatAL));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPManuProc, userValues.get(IConstants.j0CPManuProc));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPRemark, userValues.get(IConstants.j0CPRemark));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPRevText, userValues.get(IConstants.j0CPRevText));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_CPTrademark, userValues.get(IConstants.j0CPTrademark));
      writeValueLoc(TDatatype.T_STRING, PropPlace.ItemRevision, ICustom.ATTR_MstDataVern, userValues.get(IConstants.j0MasterDataVersion));

      writerLifecycleState();*/
   }
}
