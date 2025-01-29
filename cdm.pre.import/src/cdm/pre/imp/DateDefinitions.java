package cdm.pre.imp;

import java.text.SimpleDateFormat;

public abstract class DateDefinitions {
   public final static SimpleDateFormat SDF         = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
   public final static SimpleDateFormat SDF_DISPLAY = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
   public final static SimpleDateFormat SDF_IMPORT  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
