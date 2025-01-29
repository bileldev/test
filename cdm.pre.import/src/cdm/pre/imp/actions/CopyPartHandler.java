package cdm.pre.imp.actions;

import cdm.pre.imp.mod.TreeElement;

public class CopyPartHandler extends CopyHandler {

   public CopyPartHandler() {
   }

   @Override
   protected String getTextToCopy(TreeElement elem) {
      return elem.getPartNumber();
   }
}
