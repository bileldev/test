package cdm.pre.imp;

//import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import cdm.pre.imp.mod.IProgFeedback;
import cdm.pre.imp.reader.PLMUtils;

public class WriterJob extends Job {
   private final XMLFileData xmlFileData;
   static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");

   public WriterJob(final XMLFileData xmlFileData) {
      super("Write Import XML");
      logger.info("Job Creating...");
      this.xmlFileData = xmlFileData;
      setUser(true);
   }

   @Override
   protected IStatus run(final IProgressMonitor monitor) {
      IStatus ret = null;
      try {
    	  xmlFileData.getRoot().writeItemXMLFile(xmlFileData, new IProgFeedback() {
    		  @Override
    		  public void worked(int work) {
    			  monitor.worked(work);
    		  }

    		  @Override
    		  public void done() {
    			  monitor.done();
    		  }

    		  @Override
    		  public void beginTask(String name, int totalWork) {
    			  //logger.info("Task started..");
    			  monitor.beginTask(name, totalWork);
    		  }

    		  @Override
    		  public boolean isCanceled() {
    			  //	logger.info("Job Cancelled");
    			  return monitor.isCanceled();
    		  }
    	  });
    	  ret = Status.OK_STATUS;
      } catch (Exception e) {
    	  ret = new Status(IStatus.ERROR, IPluginConstants.PLUGIN_ID, e.toString(), e);
      }

      // added code for printing out the object count.
      PLMUtils.printObjCountReport();

      // prints out a complete BOM report
     // PLMUtils.printBOMReport();
     
      return ret;
   }
   public XMLFileData getXMLFileData()
   {
	   return xmlFileData;
   }
}
