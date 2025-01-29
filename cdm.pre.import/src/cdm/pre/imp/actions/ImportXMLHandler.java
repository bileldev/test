package cdm.pre.imp.actions;

import org.eclipse.core.commands.ExecutionEvent;

import cdm.pre.imp.batch.BatchException;
import cdm.pre.imp.reader.TruckException;

public class ImportXMLHandler extends BaseHandler {

   public ImportXMLHandler() {
   }

   @Override
   public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
      try {
		getStructView(event).writeImportXML();
	} catch (BatchException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (TruckException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      return null;
   }
}
