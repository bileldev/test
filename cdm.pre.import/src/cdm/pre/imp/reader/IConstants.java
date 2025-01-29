package cdm.pre.imp.reader;

import java.util.HashSet;
import java.util.Set;

public interface IConstants {
   static final String		CodeRule			   = "j0CodeRule";								
   static final String		location		   	   = "location";
   static final String		equivalentRef		   = "equivalentRef";   
   static final String      j0DynDiaNumber         = "j0DynDiaNumber";
   static final String      PartNumber             = "PartNumber";
   static final String      ProjectName            = "ProjectName";
   static final String      Revision               = "Revision";
   static final String      Sequence               = "Sequence";
   static final String      LifeCycleState         = "LifeCycleState";
   static final String      Nomenclature           = "Nomenclature";
   static final String      DataItemDesc           = "DataItemDesc";
   static final String      Class                  = "Class";
   static final String      j0SDiaBR               = "j0SDiaBR";
   static final String      j0SDHMod               = "j0SDHMod";
   static final String      j0SDMod                = "j0SDMod";
   static final String      j0SDSMod               = "j0SDSMod";
   static final String      j0SDPosV               = "j0SDPosV";
   static final String      j0SDPos                = "j0SDPos";
   static final String      j0SDLage               = "j0SDLage";
   static final String      j0PrtVer               = "j0PrtVer";
   static final String      j0StdPrt               = "j0StdPrt";
   static final String      j0LiePrt               = "j0LiePrt";
   static final String      j0BrePrt               = "j0BrePrt";
   static final String      j0BtePrt               = "j0BtePrt";
   static final String      j0CarCmp               = "j0CarCmp";
   static final String      j0VwCmp                = "j0VwCmp";
   static final String      j0CarBR                = "j0CarBR";
   static final String      j0Argmnt               = "j0Argmnt";
   static final String      j0Montge               = "j0Montge";

   static final String      j0CatCgr               = "j0CatCgr";
   static final String      j0CatPrt               = "j0CatPrt";
   static final String      j0CTMod                = "j0CTMod";
   static final String      j0CTMd2D               = "j0CTMd2D";
   static final String      JT                     = "JT";
   static final String      TIFF                   = "TIFF";
   static final String      j0CatDrw               = "j0CatDrw";
   static final String      j0NXPrt                = "j0NXPrt";
   static final String      j0NXAsm                = "j0NXAsm";
   static final String 		j0BauKas			   = "j0BauKas";
   static final String 		j0VisInt			   = "j0VisInt";   
   static final String      j0NXDrw                = "j0NXDrw";

   static final String      j0RelCount             = "j0RelCount";
   static final String      j0Cdi3D                = "j0Cdi3D";
   static final String      j0Cdi2D                = "j0Cdi2D";
   static final String      j0CdiAss               = "j0CdiAss";
   static final String      j2PbiDti               = "j2PbiDti";
   static final String      j2HasFil               = "j2HasFil";
   static final String      partRef                = "partRef";
   static final String      instRefs               = "instanceRefs";
   static final String      j0Creator              = "j0Creator";
   static final String      j0SecRel               = "j0SecRel";
   static final String      j0CertRel              = "j0CertRel";
   static final String      j0CPColor              = "j0CPColor";
   static final String      j0CPNumFeatureDS       = "j0CPNumFeatureDS";
   static final String      j0CPNumFeatureDZ       = "j0CPNumFeatureDZ";
   static final String      j0FdokRel              = "j0FdokRel";
   static final String      j0IsUVKZ               = "j0IsUVKZ";
   static final String      j0Tolerancing          = "j0Tolerancing";
   static final String      j0VPDIdentNum          = "j0VPDIdentNum";

   static final String      j0CPGenTol             = "j0CPGenTol";
   static final String      j0CPStatFtr            = "j0CPStatFtr";
   static final String      j0CPSurfProt           = "j0CPSurfProt";
   static final String      j0CPZDate              = "j0CPZDate";
   static final String      j0ESDCode              = "j0ESDCode";

   static final String      j0Height               = "j0Height";
   static final String      j0Ixx                  = "j0Ixx";
   static final String      j0Ixy                  = "j0Ixy";
   static final String      j0Ixz                  = "j0Ixz";
   static final String      j0Iyy                  = "j0Iyy";
   static final String      j0Iyz                  = "j0Iyz";
   static final String      j0Izz                  = "j0Izz";
   static final String      j0Material2            = "j0Material2";
   static final String      CreationDate           = "CreationDate";
   static final String      j0CTModNumber          = "j0CTModNumber";
   static final String      j0HNumber              = "j0HNumber";

   static final String      j0WeightProg           = "j0WeightProg";
   static final String      j0WeightReal           = "j0WeightReal";
   static final String      j0ffKf                 = "j0ffKf";
   static final String      j0CTModSnr             = "j0CTModSnr";

   static final String      Instance               = "Instance";
   static final String      Part                   = "Part";
   static final String      CompoundRep            = "CompoundRep";
   static final String      j0SmaDia2GeoPos        = "j0SmaDia2GeoPos";
   static final String      j0ZbuNomenclature      = "j0ZbuNomenclature";
   static final String      j0ZbuType              = "j0ZbuType";
   static final Object      j0PartCategory         = "j0PartCategory";
   static final Object      j0Invalid              = "j0Invalid";

   // Added for date attributes
   
   
   static final String      j0EngDateEffectiveFrom = "j0EngDateEffectiveFrom";
   static final String      j0RelDateEffectiveFrom = "j0RelDateEffectiveFrom";
   static final String      relatedRefs            = "relatedRefs";
   static final String      LcsReleased            = "LcsReleased";
   static final String      j0LcsEng               = "j0LcsEng";
   static final String      name                   = "name";
   static final String      j2PbiDtS               = "j2PbiDtS";
   static final String      j2PbiDtP               = "j2PbiDtP";
   static final String      j0DocumentType         = "j0DocumentType";

   static final String      j0CentreOfGravity_x    = "j0CentreOfGravity_x";
   static final String      j0CentreOfGravity_y    = "j0CentreOfGravity_y";
   static final String      j0CentreOfGravity_z    = "j0CentreOfGravity_z";
   static final String      j0CPArea               = "j0CPArea";
   static final String      j0CPMatAL              = "j0CPMatAL";
   static final String      j0CPMatWEZ             = "j0CPMatWEZ";
   static final String      j0CPManuProc           = "j0CPManuProc";
   static final String      j0CPMatID              = "j0CPMatID";
   static final String      j0CPRemark             = "j0CPRemark";
   static final String      j0CPRevText            = "j0CPRevText";
   static final String      j0CPTrademark          = "j0CPTrademark";
   static final String      j0MasterDataVersion    = "j0MasterDataVersion";
   static final String      j0DMUrelevant          = "j0DMUrelevant";
   static final String      j0ZGSNumber            = "j0ZGSNumber";
   static final String      j0ChangeDescription    = "j0ChangeDescription";
   static final String      j0Nomenclature_en_us   = "j0Nomenclature_en_us";
   static final String      j0LieferantSnr         = "j0LieferantSnr";
   static final String      j0CPModTyp             = "j0CPModTyp";
   static final String      j0SDNomenclature_en_us = "j0Nomenclature_en_us";
   static final String      j0AusArtCR             = "j0AusArtCR";
   static final String      j0CodeRule             = "j0CodeRule";
   static final String      j0Steer                = "j0Steer";
   static final String      j0CPAuthor             = "j0CPAuthor";
   static final String      j0CPDecor              = "j0CPDecor";
   static final String      j0CPDepartment         = "j0CPDepartment";
   static final String      j0CPFunctInstr         = "j0CPFunctInstr";
   static final String      j0CPGenTol2            = "j0CPGenTol2";
   static final String      j0CPGenTol3            = "j0CPGenTol3";
   static final String      j0MasterDataMethod     = "j0MasterDataMethod";
   static final String      j0BasePartnumber       = "j0BasePartnumber";
   static final String      j0TheftRel             = "j0TheftRel";
   static final String      j0EeSigMark            = "j0EeSigMark";
   static final String      j0EeSwSec              = "j0EeSwSec";
   static final String      j0Settled              = "j0Settled";
   static final String      Header                 = "Header";
   static final String      REFCONFIG              = "REFCONFIG";
   static final Object      LDisplayedName         = "LDisplayedName";
   static final Object      j0RevSeq               = "j0RevSeq";
   static final Object      j0Nomenclature         = "j0Nomenclature";
   
   // defines for the DMU XML file processing
   static final String		PLMXML_FILE_EXTENSION  = ".plmxml";
   static final String		PLMXML_FILE_SEPARATOR  = "#";
   static final String 		TRUCK_CLASS_BCS		   = "BCS";
   static final String 		TRUCK_CLASS_C9TRCK	   = "C9Truck";
   static final String 		TRUCK_CLASS_C9TRCKCFG  = "C9TruckCfgItem";
   static final String 		TRUCK_CLASS_CBM		   = "CB9CBM";
   static final String 		TRUCK_CLASS_DBM		   = "CB9SBProd";
   static final String      ATTR_BOM_LEVEL		   = "bomLevel";
   static final String 		PREFIX_EFF_ID		   = "EI";
   static final String 		PREFIX_CFG_ID		   = "CFG";
   static final String      ROOT_INSTANCE		   = "rootInstance";
   static final String 		FIRST_INSTANCE		   = "inst1";
   static final String 		TCATTR_ITEM_ID		   = "item_id";
   
   // tc type defines for Truck
   public static final String TC_TYPE_TRUCK			= "C9Truck";
   public static final String TC_TYPE_CBM			= "CB9CBM";
   public static final String TC_TYPE_DBM			= "CB9SBProd";
   public static final String TC_TYPE_NAVMOD		= "CB9SBGrp";
   public static final String TC_TYPE_FUNCMOD		= "CB9SBUmf";
   public static final String TC_TYPE_POS			= "CB9SBPos";
   public static final String TC_TYPE_LIPOS			= "CB9LiPos";
   public static final String CBM_IDENT_PREFIX		= "C";
   public static final String DBM_IDENT_PREFIX		= "D";
   
   public static final String TC_TYPE_C9CarBRVD 	= "C9CarBRVD";
   
   // tc attribute defines
   public static final String TC_ATTR_ITEMREVID		= "item_revision_id";
   
   // project prefixes for Truck projects
   static final String 		PROJ_PREFIX_SFTP	   = "S3";
   static final String 		PROJ_PREFIX_NGA		   = "A6";
   static final String 		PROJ_PREFIX_ACTROS	   = "A3";
   static final String 		PROJ_PREFIX_FUSO	   = "F2";
   
   // project names for Trucks
   static final String 		PROJ_SFTP		   	   = "SFTP";
   static final String 		PROJ_NGA			   = "NGA";
   static final String 		PROJ_ACTROS		   	   = "ACTROS";
   static final String 		PROJ_FUSO			   = "FUSO";
  
   
   // defines for the intermediate xml file defines
   static final String 	    INTXML_ENDITEM_ATTR	   	= "endItem"; 
   static final String 	    INTXML_DATE_ATTR	   	= "date"; 
   static final String 		INTXML_ENDITEM_SEP		= ";";
   
   static final String 	    INTXML_VARIANT_ITEM_ID 		= "Item"; 
   static final String 	    INTXML_VARIANT_TYPE	   		= "type"; 
   static final String 		INTXML_VARIANT_VALUE		= "value";
   static final String 		INTXML_VARIANT_REMOVE		= "remove";
   static final String 		INTXML_VARIANT_REMOVE_TYPE	= "remove_type";
   
   
   
   
   

   // the native CAD formats like CATPart, NX files or CATIA V4 files.
   static final Set<String> NATIVE_FORMATS         = new HashSet<String>();

   class Init {
      static {
         NATIVE_FORMATS.add(IConstants.j0CatPrt);
         NATIVE_FORMATS.add(IConstants.j0CatDrw);
         NATIVE_FORMATS.add(IConstants.j0NXPrt);
         NATIVE_FORMATS.add(IConstants.j0CTMod);
         NATIVE_FORMATS.add(IConstants.j0CTMd2D);
      }
   }

   /**
    * To call the static block.
    */
   final static Init init = new Init();
}
