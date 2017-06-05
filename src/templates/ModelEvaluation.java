package templates;

import java.util.List;

public interface ModelEvaluation {
	
	enum ValueType {
		INTERPOLATE,
		VALUE,
		COUNT,
		PERCENTAGE,
		TEXT
	}
	
	public void evaluate(Model model) ;
	
	public void clear();
	
	public List<String> getValueLabels() ; 
	
	public String getUnit(String label) ;
	
	public ValueType getValueType(String label) ;
		
	public String getLabel() ;	
}
