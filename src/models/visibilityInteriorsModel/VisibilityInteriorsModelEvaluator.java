package models.visibilityInteriorsModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import evaluations.VisibilityEvaluationField;
import templates.Model;
import templates.ModelEvaluation;
import templates.ModelEvaluator;

public class VisibilityInteriorsModelEvaluator implements ModelEvaluator{

	public enum EvaluationType {
		VISIBILITY,
		DISCOVERABILITY,
		ACCESSIBILITY
	}
	
	HashMap<String, ModelEvaluation> evaluations = new HashMap<>();
	BidiMap<EvaluationType, String> labels = new DualHashBidiMap<>();
			
	public VisibilityInteriorsModelEvaluator() {
		
		this.addEvaluation(new VisibilityEvaluationField(), EvaluationType.VISIBILITY);
		this.addEvaluation(new VisibilityEvaluationField(), EvaluationType.DISCOVERABILITY);
		this.addEvaluation(new VisibilityEvaluationField(), EvaluationType.ACCESSIBILITY);
	}
		
	@Override
	public ModelEvaluation evaluateModel(Model model, ModelEvaluation evaluation) {
		evaluation.clear();
		evaluation.evaluate(model);
		return evaluation;
	}
	
	public void addEvaluation(ModelEvaluation evaluation, EvaluationType type) {
		this.addEvaluation(evaluation);
		this.labels.put(type, evaluation.getLabel());
	}
	
	@Override
	public void addEvaluation(ModelEvaluation evaluation) {
		evaluations.put(evaluation.getLabel(), evaluation);
	}
	
	@Override
	public List<String> getEvaluationLabels() {
		return new ArrayList<>(evaluations.keySet());
	}
	
	public ModelEvaluation getEvaluation(EvaluationType type) {
		return this.getEvaluation(this.labels.get(type));
	}
	
	@Override
	public ModelEvaluation getEvaluation(String label) {
		return this.evaluations.get(label);
	}
	
	public EvaluationType getType(String label) {
		return labels.getKey(label);
	}
				
}
