package cdm.pre.imp.writer;

import java.util.HashMap;
import java.util.Map;

import cdm.pre.imp.reader.IConstants;

/**
 * Class that defines the customized elements.
 * 
 * @author dump1
 * 
 */
public interface ICustom {
   final static String              PREFIX              = "C9";
   final static String              PREFIX_UD           = "CD9";
   final static String              SPREFIX             = PREFIX.toLowerCase();
   final static String              SPREFIX_UD          = PREFIX_UD.toLowerCase();
   final static String              ATTR_MODNR          = SPREFIX + "CTModNumber";
   final static String              ATTR_CREATOR        = SPREFIX + "SmaCreator";
   final static String              ATTR_CERTIFICATION  = SPREFIX + "CertRel";
   final static String              ATTR_SECURITY       = SPREFIX + "SecRel";
   final static String              ATTR_COLOUR         = SPREFIX + "CPColor";
   final static String              ATTR_NUMFEADS       = SPREFIX + "CPNumFeatureDS";
   final static String              ATTR_NUMFEADZ       = SPREFIX + "CPNumFeatureDZ";
   final static String              ATTR_FDOC           = SPREFIX + "FdokRel";
   final static String              ATTR_ISUV           = SPREFIX + "IsUVKZ";
   final static String              ATTR_TOLERENCE      = SPREFIX + "Tolerancing";
   final static String              ATTR_VPDID          = SPREFIX + "VPDIdentNum";
   final static String              ATTR_GENTOL         = SPREFIX + "CPGenTol";
   final static String              ATTR_STATFTR        = SPREFIX + "CPStatFtr";
   final static String              ATTR_SURFPROT       = SPREFIX + "CPSurfProt";
   final static String              ATTR_ZDATE          = SPREFIX + "CPZDate";
   final static String              ATTR_ESDCODE        = SPREFIX + "ESDCode";

   final static String              ATTR_COGX           = SPREFIX + "CentreOfGravity_x";
   final static String              ATTR_COGY           = SPREFIX + "CentreOfGravity_y";
   final static String              ATTR_COGZ           = SPREFIX + "CentreOfGravity_z";

   final static String              ATTR_HEIGHT         = SPREFIX + "Height";
   final static String              ATTR_IXX            = SPREFIX + "Ixx";
   final static String              ATTR_IXY            = SPREFIX + "Ixy";
   final static String              ATTR_IXZ            = SPREFIX + "Ixz";
   final static String              ATTR_IYY            = SPREFIX + "Iyy";
   final static String              ATTR_IYZ            = SPREFIX + "Iyz";
   final static String              ATTR_IZZ            = SPREFIX + "Izz";
   final static String              ATTR_MATERIAL       = SPREFIX + "Material2";
   final static String              ATTR_PARTNUMBER     = SPREFIX + "PartNumber";
   final static String              ATTR_CREATIONDATE   = SPREFIX + "SmaCreationDate";
   final static String              ATTR_WPRG           = SPREFIX + "WeightProg";
   final static String              ATTR_WREAL          = SPREFIX + "WeightReal";
   final static String              ATTR_FFKF           = SPREFIX + "ffKf";
   final static String              ATTR_HNUMBER        = SPREFIX + "HNumber";
   final static String              ATTR_CPArea         = SPREFIX + "CPArea";
   final static String              ATTR_CPMatAL        = SPREFIX + "CPMatAL";
   final static String              ATTR_CPMatWEZ       = SPREFIX + "CPMatWEZ";
   final static String              ATTR_CPManuProc     = SPREFIX + "CPManuProc";
   final static String              ATTR_CPMatID        = SPREFIX + "CPMatID";
   final static String              ATTR_CPRemark       = SPREFIX + "CPRemark";
   final static String              ATTR_CPRevText      = SPREFIX + "CPRevText";
   final static String              ATTR_CPTrademark    = SPREFIX + "CPTrademark";
   final static String              ATTR_MstDataVern    = SPREFIX + "MasterDataVersion";
   final static String              ATTR_PartCategory   = SPREFIX + "PartCategory";
   final static String              ATTR_DMUrelevant    = SPREFIX + "DMUrelevant";
   final static String              ATTR_ZGS            = SPREFIX + "ZGSNumber";

   final static String              ATTR_CPDecor        = SPREFIX + "CPDecor";
   final static String              ATTR_CPDepartment   = SPREFIX + "CPDepartment";
   final static String              ATTR_CPAuthor       = SPREFIX + "CPAuthor";
   final static String              ATTR_CPFunctInstr   = SPREFIX + "CPFunctInstr";
   final static String              ATTR_CPGenTol2      = SPREFIX + "CPGenTol2";
   final static String              ATTR_CPGenTol3      = SPREFIX + "CPGenTol3";
   final static String              ATTR_MSTDTAMTD      = SPREFIX + "MasterDataMethod";
   final static String              ATTR_BSPRTNUM       = SPREFIX + "BasePartnumber";
   final static String              ATTR_ESIGMRK        = SPREFIX + "EeSigMark";
   final static String              ATTR_ESWSEC         = SPREFIX + "EeSwSec";
   final static String              ATTR_SETTLED        = SPREFIX + "Settled";
   final static String              ATTR_CPZDATE        = SPREFIX + "CPZDate";
   final static String              ATTR_THEFTREL       = SPREFIX + "TheftRel";
   final static String              ATTR_ChangeDes      = SPREFIX + "ChangeDescription";
   final static String              ATTR_NomenEn        = SPREFIX + "Nomenclature_en";
   final static String              ATTR_LieferantSnr   = SPREFIX + "LieferantSnr";
   final static String              ATTR_CPModTyp       = SPREFIX + "CPModTyp";
   final static String              ATTR_DYNDIANR       = SPREFIX_UD + "DynDiaNumber";
   final static String              ATTR_GEOPOS         = SPREFIX_UD + "SmaDia2GeoPos";
   final static String              DirectModel         = "DirectModel";
   final static String              CATPart             = "CATPart";
   final static String              Model               = "catia";
   final static String              CGR                 = "CATCache";
   final static String              TIF                 = "TIF";
   final static String              CATDrawing          = "CATDrawing";
   final static String              UGMASTER            = "UGMASTER";
   final static String              BAUKAS              = PREFIX + "BauKas";
   final static String              VISINT              = PREFIX + "VisInt";
   // Added for date attributes
   final static String              ATTR_ENGDATEEFF     = SPREFIX + "EngDateEffectiveFrom";
   final static String              ATTR_RELDATEEFF     = SPREFIX + "RelDateEffectiveFrom";
   // the date to be used if we get an invalid TcUA date value from Smaragd
   final static String              DEFAULT_DATE        = "1970/01/01/00/00/00";
   final static Map<String, String> TYPE_MAP            = new HashMap<String, String>();

   final static String              ATTR_INVALID        = SPREFIX_UD + "Invalid";
   final static String              ATTR_SDNOMENCLATURE = SPREFIX_UD + "Nomenclature_en";
   final static String              ATTR_AUSARTCR       = SPREFIX_UD + "AusArtCR";
   final static String              ATTR_CODERULE       = SPREFIX_UD + "CodeRule";
   final static String              ATTR_STEER          = SPREFIX_UD + "Steer";

   /**
    * Class to initialize the type map.
    * 
    * @author dump1
    * 
    */
   class Init {
      static {
         TYPE_MAP.put(IConstants.j0PrtVer, PREFIX + "Part");
         TYPE_MAP.put(IConstants.j0StdPrt, PREFIX + "StdPart");
         TYPE_MAP.put(IConstants.j0BrePrt, PREFIX + "FlexPart");
         TYPE_MAP.put(IConstants.j0LiePrt, PREFIX + "SupplierPart");
         TYPE_MAP.put(IConstants.j0Cdi3D, PREFIX + "Model");
         TYPE_MAP.put(IConstants.j0Cdi2D, PREFIX + "Drawing");
         TYPE_MAP.put(IConstants.j0BtePrt, PREFIX + "InternalPart");
         TYPE_MAP.put(IConstants.j0CarCmp, PREFIX + "CarCmp");
         TYPE_MAP.put(IConstants.j0CarBR, PREFIX + "CarBR");
         TYPE_MAP.put(IConstants.j0VwCmp, PREFIX + "VwCmp");
         TYPE_MAP.put(IConstants.j0Argmnt, PREFIX + "Arrangement");
         TYPE_MAP.put(IConstants.j0Montge, PREFIX + "Montge");

         TYPE_MAP.put(IConstants.j0SDiaBR, PREFIX_UD + "SDiaBR");
         TYPE_MAP.put(IConstants.j0SDHMod, PREFIX_UD + "SDHMod");
         TYPE_MAP.put(IConstants.j0SDMod, PREFIX_UD + "SDMod");
         TYPE_MAP.put(IConstants.j0SDSMod, PREFIX_UD + "SDSMod");
         TYPE_MAP.put(IConstants.j0SDiaBR, PREFIX_UD + "SDiaBR");
         TYPE_MAP.put(IConstants.j0SDPos, PREFIX_UD + "SDPos");
         TYPE_MAP.put(IConstants.j0SDPosV, PREFIX_UD + "SDPosV");
         TYPE_MAP.put(IConstants.j0SDLage, PREFIX_UD + "SDLage");
      }
   }

   /**
    * To call the static block.
    */
   final static Init   init         = new Init();

   static final String Item         = "Item";
   static final String ItemRevision = "ItemRevision";
   static final String ItemID       = "item_id";
   static final String ObjectName   = "object_name";
   static final String Revision     = "item_revision_id";
}
