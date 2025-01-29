package cdm.pre.imp.dbconnector.DTO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DTO for Jt.
 */
public class JTDTO{
	final private static Logger LOGGER = LogManager.getLogger(JTDTO.class);
	
	private CDIModelDTO parent;
	private String OBID;
	private String nomenclature; //j0Nomenclature
	private String fileSize; //j0FileSize
	private String sequence; //Sequence
	
	/* Should be filled: 
	   		True: exists in ASPLM
	   		False: don´t esists in ASPLM
	 */
	 
	private boolean inASPLM;

	public JTDTO(CDIModelDTO parent, String obid, String nomenclature, String fileSize, String sequence) {
		super();
		this.parent = parent;
		this.OBID = obid;
		this.nomenclature = nomenclature;
		this.fileSize = fileSize;
		this.sequence = sequence;
		inASPLM = false;
	}
	
	public boolean isInASPLM() {
		return inASPLM;
	}

	public void setInASPLM(boolean inASPLM) {
		LOGGER.debug(String.format("The jt with OBID: %s and j0Nomenclature :%S is in ASPLM", OBID, nomenclature));
		
		this.inASPLM = inASPLM;
	}
	
	public CDIModelDTO getParent() {
		return parent;
	}
	
	public String getOBID() {
		return OBID;
	}
	
	public String getNomenclature() {
		return nomenclature;
	}
	
	public String getFileSize() {
		return fileSize;
	}
	
	public String getSequence() {
		return sequence;
	}

}