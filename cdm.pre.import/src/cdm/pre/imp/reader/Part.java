package cdm.pre.imp.reader;

import java.util.List;

/**
 * Class the represents a &lt;Part&gt; element of PLMXML file.
 * 
 * @author wikeim
 * 
 */
public class Part {
   // The part element.
   public Element      element;
   // The list of instance references.
   public List<String> refs;
   // The list of compound references
   public List<String> compReps;
}
