package templates;

import java.util.List;

public interface ModelExporter {

	public void exportEvaluation(ModelEvaluation evaluation, String fileName);
	
	public void exportEvaluations(List<ModelEvaluation> evaluations, String fileName);
}
