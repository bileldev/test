package cdm.pre.imp.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.ReaderSingleton;
import cdm.pre.imp.writer.ICustom;

public final class TypeMaps {
	public final static Map<String, String> FILE_TYPES   = new HashMap<String, String>();
	public final static Set<String>         OCCEFF_TYPES = new HashSet<String>();
	public final static Set<String>          GEO_TYPES    = new HashSet<String>();
	public final static Set<String>          ROOT_TYPES   = new HashSet<String>();
	private final static Set<String>         VARIANT_CFG_TYPES = new HashSet<String>();
	private final static Set<String>         VARIANT_CAR_TYPES = new HashSet<String>();

	static {
		// File types
		FILE_TYPES.put(IConstants.JT, ICustom.DirectModel);
		FILE_TYPES.put(IConstants.j0CatPrt, ICustom.CATPart);
		FILE_TYPES.put(IConstants.j0CatCgr, ICustom.DirectModel);
		FILE_TYPES.put(IConstants.j0CTMod, ICustom.Model);
		FILE_TYPES.put(IConstants.j0CTMd2D, ICustom.Model);
		FILE_TYPES.put(IConstants.j0CatDrw, ICustom.CATDrawing);
		FILE_TYPES.put(IConstants.TIFF, ICustom.TIF);
		FILE_TYPES.put(IConstants.j0NXAsm, ICustom.UGMASTER);
		FILE_TYPES.put(IConstants.j0NXPrt, ICustom.UGMASTER);
		FILE_TYPES.put(IConstants.j0NXDrw, ICustom.UGMASTER);
		FILE_TYPES.put(IConstants.j0BauKas, ICustom.BAUKAS);
		FILE_TYPES.put(IConstants.j0VisInt, ICustom.VISINT);

		// Geo types
		GEO_TYPES.add(IConstants.j0BrePrt);
		GEO_TYPES.add(IConstants.j0PrtVer);
		GEO_TYPES.add(IConstants.j0StdPrt);
		GEO_TYPES.add(IConstants.j0LiePrt);
		GEO_TYPES.add(IConstants.j0CarBR);
		GEO_TYPES.add(IConstants.j0Argmnt);

		// Root types
		ROOT_TYPES.add(IConstants.j0SDiaBR);
		ROOT_TYPES.add(IConstants.j0SDHMod);
		ROOT_TYPES.add(IConstants.j0VwCmp);
		ROOT_TYPES.add(IConstants.j0CarBR);
		ROOT_TYPES.add(IConstants.j0PrtVer);
		ROOT_TYPES.add(IConstants.j0Argmnt);
		ROOT_TYPES.add(IConstants.j0Montge);

		// OccEff types
		OCCEFF_TYPES.add(IConstants.j0SDPos);
		OCCEFF_TYPES.add(IConstants.j0SDPosV);
		OCCEFF_TYPES.add(IConstants.j0SDSMod);
		OCCEFF_TYPES.add(IConstants.j0SDMod);
		OCCEFF_TYPES.add(IConstants.j0SDHMod);
		OCCEFF_TYPES.add(IConstants.j0SDLage);
		OCCEFF_TYPES.add(IConstants.j0Montge);
		//OCCEFF_TYPES.add(IConstants.j0PrtVer);

		// added for Truck implementation
		OCCEFF_TYPES.add(IConstants.TRUCK_CLASS_BCS);
		OCCEFF_TYPES.add(IConstants.j0CarBR);
		OCCEFF_TYPES.add(IConstants.j0VwCmp);
		OCCEFF_TYPES.add(IConstants.j0CarCmp);

		// Variant Types - cfg 
		VARIANT_CFG_TYPES.add(IConstants.j0SDPos);
		VARIANT_CFG_TYPES.add(IConstants.j0SDPosV);
		VARIANT_CFG_TYPES.add(IConstants.j0SDSMod);
		VARIANT_CFG_TYPES.add(IConstants.j0SDMod);
		VARIANT_CFG_TYPES.add(IConstants.j0SDHMod);
		VARIANT_CFG_TYPES.add(IConstants.j0SDLage);
		VARIANT_CFG_TYPES.add(IConstants.j0Montge);
		VARIANT_CFG_TYPES.add(IConstants.j0PrtVer); 
		VARIANT_CFG_TYPES.add(IConstants.j0SDiaBR);
		VARIANT_CFG_TYPES.add(IConstants.j0StdPrt);

		// Variant Types - car

		VARIANT_CAR_TYPES.add(IConstants.j0SDiaBR);
		VARIANT_CAR_TYPES.add(IConstants.j0Montge);
		VARIANT_CAR_TYPES.add(IConstants.j0SDPos);
	}

	public static String getFileType(final String eClazz) {
		/*if(eClazz != null && eClazz.equals("j0BrePrt"))
		{
			System.out.println("j0BrePrt");
		}*/
		if (PreImpConfig.getInstance().isJTOnly()) {
			if (IConstants.JT.equals(eClazz)) {
				return FILE_TYPES.get(eClazz);
			}
			return null;
		} else {
			return FILE_TYPES.get(eClazz);
		}
	}

	public static boolean isTypeForOccEff(final String typeName) {
		String preInputMode = ReaderSingleton.getReaderSingleton().getInputMode();
		// 2019.01.21 [Amit] - changed from P_MODE_SENDTO_WITHOUT_REF_CONFIG to P_MODE_SENDTO_2_WITHOUT_REF_CONFIG
		if (preInputMode != null && preInputMode.equals(PreferenceConstants.P_MODE_SENDTO_2_WITHOUT_REF_CONFIG))
		{
			return false;
		}
		if (PreImpConfig.getInstance().isIgnoreInvalidSmaDia()) {
			return OCCEFF_TYPES.contains(typeName);
		}
		return IConstants.j0SDSMod.equals(typeName) || IConstants.j0SDPos.equals(typeName)
				|| IConstants.j0SDPosV.equals(typeName) || IConstants.j0SDLage.equals(typeName)
				|| IConstants.j0Montge.equals(typeName) || IConstants.TRUCK_CLASS_BCS.equals(typeName);
	}

	public static boolean isTypeForVariantCondition(final String typeName) {
		if (PreImpConfig.getInstance().isIgnoreInvalidSmaDia()) {
			return VARIANT_CFG_TYPES.contains(typeName);
		}
		return IConstants.j0SDSMod.equals(typeName) || IConstants.j0SDPos.equals(typeName)
				|| IConstants.j0SDPosV.equals(typeName) || IConstants.j0SDLage.equals(typeName)
				|| IConstants.j0Montge.equals(typeName) || IConstants.TRUCK_CLASS_BCS.equals(typeName);
	}

	public static boolean isTypeForCarVariantCondition(final String typeName) {
		return VARIANT_CAR_TYPES.contains(typeName);
	}
}
