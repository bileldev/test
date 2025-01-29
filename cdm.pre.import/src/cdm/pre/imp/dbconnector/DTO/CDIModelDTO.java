package cdm.pre.imp.dbconnector.DTO;

/**
 * DTO for the parent of JT.
 */
public class CDIModelDTO {

	private String modSnr; //j0CTModSnr
	private String modNumber; // j0CTModNumber
	private String obid;
	private String sequence; // Sequence
	
	public CDIModelDTO(String modSnr, String modNumber, String obid, String sequence) {
		super();
		this.modSnr = modSnr;
		this.modNumber = modNumber;
		this.obid = obid;
		this.sequence = sequence;
	}

	public String getModSnr() {
		return modSnr;
	}
	
	public String getModNumber() {
		return modNumber;
	}
	
	public String getOBID() {
		return obid;
	}
	
	public String getSequence() {
		return sequence;
	}
}
