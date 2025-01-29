package cdm.pre.imp.batch;

import java.util.Date;

import cdm.pre.imp.reader.PLMUtils.SortedElements;

public class SnapshotInfo {
	
	private String endItemNumber;
	private String rootItemPartNo;
	
	private SortedElements sortedElems;
	
	private Date plmxmlExportDate = null;
	
	public SnapshotInfo() {
		
		this.sortedElems 	= null;
		this.rootItemPartNo = null;
		this.endItemNumber 	= null;
	}
	
	public SnapshotInfo(SortedElements sortedElems, String endItemNumber, String rootItemPartNo) {
		
		this.sortedElems 	= sortedElems;
		this.endItemNumber 	= endItemNumber;
		this.rootItemPartNo = rootItemPartNo;
	}


	public String getEndItemNumber() {
		return endItemNumber;
	}


	public void setEndItemNumber(String endItemNumber) {
		this.endItemNumber = endItemNumber;
	}


	public String getRootItemPartNo() {
		return rootItemPartNo;
	}


	public void setRootItemPartNo(String rootItemPartNo) {
		this.rootItemPartNo = rootItemPartNo;
	}


	public SortedElements getSortedElems() {
		return sortedElems;
	}


	public void setSortedElems(SortedElements sortedElems) {
		this.sortedElems = sortedElems;
	}

	public Date getPlmxmlExportDate() {
		return plmxmlExportDate;
	}

	public void setPlmxmlExportDate(Date plmxmlExportDate) {
		this.plmxmlExportDate = plmxmlExportDate;
	}
	
	
}
