package templates;

import java.util.List;

public interface ModelEvaluator {

	public ModelEvaluation evaluateModel(Model model, ModelEvaluation evaluation);
	
	public void addEvaluation(ModelEvaluation evaluation);
	
	public List<String> getEvaluationLabels();
	
	public ModelEvaluation getEvaluation(String label);
}
