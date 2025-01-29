package cdm.pre.imp.writer;

/**
 * Enum which defines where to store an attribute.
 * 
 * @author wikeim
 * 
 */
public enum PropPlace {
   // place the attribute only at the Item
   Item,
   // place the attribute only at the ItemRevision
   ItemRevision,
   // place the Item at the Item and ItemRevision
   ItemAndRev,
   // don't know where to place the Item
   Undef,
   // place the attribute at the Form (Not Yet Detailled Any Used!)
   Form
}
