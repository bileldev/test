package cdm.pre.imp.writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Effectivity {
   // 2012/11/23/22/10/35
   private String fromDate;
   private String toDate;

   public String getFromDate() {
      return fromDate;
   }

   public String getToDate() {
      return toDate;
   }

   public Effectivity(final String fromDate, final String toDate) {
      this.toDate = toDate;
      this.fromDate = fromDate;
   }

   public void writeEffectivity(final XMLStreamWriter streamWriter) throws XMLStreamException {
      if (fromDate != null) {
         streamWriter.writeAttribute("fromDate", fromDate);
      }
      if (toDate != null) {
         streamWriter.writeAttribute("toDate", toDate);
      }
   }
}
