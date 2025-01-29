package cdm.pre.imp.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
   private final static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

   @Override
   public String format(LogRecord record) {
      StringBuilder ret = new StringBuilder(df.format(new Date(record.getMillis())) + " - ");
      if (record.getThrown() != null) {
         StringWriter w = new StringWriter();
         record.getThrown().printStackTrace(new PrintWriter(w));
         ret.append(w.toString());
      } else {
         ret.append(record.getMessage());
      }
      ret.append("\n");
      return ret.toString();
   }

}
