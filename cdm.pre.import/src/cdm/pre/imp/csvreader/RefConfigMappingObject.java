package cdm.pre.imp.csvreader;

public class RefConfigMappingObject 
{
	private String refConfig = null;  // Name of the exported Reference Configuration from Smaragd as stated in the exported PLMXML file
	private String variant = null;   // Short representation of the reference configuration name. This would be value that is set to the variant option value.
	private String parentRefConfig = null; // BelongsToRefConfig - Name of the SUM configuration (column – RefConfig) to which the 100% Reference Configuration or the Montage belongs.
	private String topNode =  null; // Item ID of the custom VD object which would be the top BOM parent in the E-BOM.
	private String optionName = null;
	
	private RefConfigMappingObject parentRefConfigMapObj = null;
	
	
	public void setRefConfig(String refConfig)
	{
		this.refConfig = refConfig;
	}
	
	public String getRefConfig()
	{
		return this.refConfig;
	}
	
	public void setVariant(String variant)
	{
		this.variant = variant;
	}
	public String getVariant()
	{
		if(variant.equals("-") && topNode != null)
		{
			String[] split = topNode.split("-");
			if( split != null && split.length == 2)
			{
				variant = split[1];
			}
		}
		return this.variant;
	}
	
	public void setParentRefConfig(String parentRefConfig)
	{
		this.parentRefConfig = parentRefConfig;
	}
	public String getParentRefConfig()
	{
		return this.parentRefConfig;
	}
	
	public void setTopNode(String topNode)
	{
		this.topNode = topNode;
	}
	
	public String getTopNode()
	{
		return this.topNode;
	}
	
	public RefConfigMappingObject getParentRefConfigMapObj() {
		return parentRefConfigMapObj;
	}

	public void setParentRefConfigMapObj(RefConfigMappingObject parentRefConfigMapObj) {
		this.parentRefConfigMapObj = parentRefConfigMapObj;
	}

	
	public String getOptionName() {
		return optionName;
	}

	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}

	public void print()
	{
		System.out.println("**************************START*************************");
		System.out.println("Reference Configuration Name : "+refConfig);
		System.out.println("Variant :"+variant);
		System.out.println("Parent Ref Configuration : "+parentRefConfig);
		System.out.println("Top Node : "+topNode);
		System.out.println("**************************END*************************");
	}
}
