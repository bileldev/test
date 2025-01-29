package cdm.pre.imp.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Occurrence {

   private String       id;

   private List<String> instanceRefs       = new ArrayList<String>();
   private List<String> occurrenceRefs     = new ArrayList<String>();
   private List<String> representationRefs = new ArrayList<String>();

   public Occurrence(final String id, final String sOccurrenceRefs, final String sInstanceRefs,
         final String sRepresentationRefs) {
      this.id = id;
      if (sOccurrenceRefs != null && !"".equals(sOccurrenceRefs)) {
         String[] occRefs = sOccurrenceRefs.split(" ");
         occurrenceRefs.addAll(Arrays.asList(occRefs));
      }

      if (sInstanceRefs != null && !"".equals(sInstanceRefs)) {
         String[] instRefs = sInstanceRefs.split(" ");
         for (String instRef : instRefs) {
            instanceRefs.add(instRef.substring(1));
         }
      }

      if (sRepresentationRefs != null && !"".equals(sRepresentationRefs)) {
         String[] repRefs = sRepresentationRefs.split(" ");
         for (String repRef : repRefs) {
            representationRefs.add(repRef.substring(1));
         }
      }
   }

   public String getId() {
      return id;
   }

   public List<String> getInstanceRefs() {
      return instanceRefs;
   }

   public List<String> getOccurrenceRefs() {
      return occurrenceRefs;
   }

   public List<String> getRepresentationRefs() {
      return representationRefs;
   }
}
