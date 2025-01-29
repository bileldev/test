package cdm.pre.imp;

import java.util.Date;
import java.util.List;

//import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import cdm.pre.imp.mod.TreeElement;
import cdm.pre.imp.mod.TreeElementFactoryFromPLMXML;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.reader.Handler;
import cdm.pre.imp.reader.PLMUtils;
import cdm.pre.imp.reader.PLMUtils.SortedElements;
import cdm.pre.imp.reader.Reader;
import cdm.pre.imp.reader.ReaderSingleton;

public class ReaderJob extends Job {

	static private Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");
	private List<TreeElement> roots;
	private String            filename;
	private Date              exportDate;
	private Handler handler;

	public ReaderJob(String filename) {
		super("Reading PLMXML file");
		logger.info("Reading PLMXML file");
		this.setPriority(Job.LONG);
		this.filename = filename;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus ret = null;
		Reader reader = new Reader();
		try 
		{
			// Reading Mapping file 
			String configMappingFilePath = PreImpConfig.getInstance().getConfigMappingFilePath();
			if(configMappingFilePath != null)
			{
				ReaderSingleton.getReaderSingleton().readMappingFile(configMappingFilePath);
			}
			handler = reader.readSnapshot(filename);
			exportDate = handler.getExportDate();
			// this call has to be modified while integrating the GUI with Truck
			roots = TreeElementFactoryFromPLMXML.createStructure(handler, false);
			ret = Status.OK_STATUS;
		} catch (Exception e) {
			ret = new Status(IStatus.ERROR, IPluginConstants.PLUGIN_ID, e.toString(), e);
		}
		return ret;
	}

	public List<TreeElement> getRoots() {
		return roots;
	}

	public Date getExportDate() {
		return exportDate;
	}
	public SortedElements getSortedElements()
	{
		SortedElements sortedElements = null;
		try
		{
			sortedElements = PLMUtils.getElements(handler.getElements());
		} 
		catch (CDMException e) 
		{
			e.printStackTrace();
		}
		return sortedElements;
	}
}
