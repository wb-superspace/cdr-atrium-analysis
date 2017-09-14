package evaluations;

import java.util.ArrayList;
import java.util.List;

import evaluations.VisibilityInteriorsEvaluation.EvaluationType;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation;

public class VisibilityInteriorsEvaluationFactory {
	
	private VisibilityInteriorsModel model;
	
	public VisibilityInteriorsEvaluationFactory(VisibilityInteriorsModel model) {		
		this.model = model;
	}

	public void createEvaluations(List<VisibilityInteriorsLocation> sinks, List<VisibilityInteriorsLocation> sources, EvaluationType type,
			boolean onlyVisible, boolean onlySingleFloor, boolean multipleSink, String label) {
		
		for (VisibilityInteriorsLocation location : model.getLocations()) {
			location.setEvaluation(null);
			location.setActive(false);
		}
		
		for (VisibilityInteriorsLocation sink : sinks ) {
		
			List<VisibilityInteriorsLocation> _sinks = new ArrayList<>();
			List<VisibilityInteriorsLocation> _sources = new ArrayList<>();
			
			if (!multipleSink)  {
				_sinks.add(sink);
			} else { 
				_sinks.addAll(sinks);
			}
			
			// TODO - move this back into eval for speed?
			
			for (VisibilityInteriorsLocation source : sources) {
				if (!source.equals(sink)) {
					if (!onlySingleFloor || source.getLayout() == sink.getLayout()) {
						if (!onlyVisible || source.getVisibilityPath(sink).getLocations().size() == 2) {
							_sources.add(source);
						}
					}
				}
			}
			
			VisibilityInteriorsEvaluation evaluation = new VisibilityInteriorsEvaluation(label, type);
			evaluation.setSinks(_sinks);
			evaluation.setSources(_sources);
			
			sink.setEvaluation(evaluation);
			sink.setActive(true);
		}
	}
}
