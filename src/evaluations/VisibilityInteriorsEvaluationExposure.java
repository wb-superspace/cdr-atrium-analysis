package evaluations;

import java.util.HashSet;
import java.util.Set;

import models.VisibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsPath;

public class VisibilityInteriorsEvaluationExposure extends VisibilityInteriorsEvaluation{

				
	public VisibilityInteriorsEvaluationExposure(String label) {
		super(label);
		
		isCumulator = true;
	}

	@Override
	public void evaluate() {
		
		Set<VisibilityInteriorsLocation> sinks = new HashSet<>(this.sinks);
		
		this.clear();
	
		for (VisibilityInteriorsLocation sink : sinks) {
			
			Set<VisibilityInteriorsLocation> sources = new HashSet<>();
						
			for (VisibilityInteriorsLocation source : this.sources) {
				if (!sink.equals(source)) {
					if (sink.getDistance(source) <= maxDistance &&
						sink.getConnectivityPath(source).getLength() <= maxLength) {
						sources.add(source);
					}	
				}
			}
			
			for (VisibilityInteriorsLocation source : sources) {
				
				VisibilityInteriorsPath path = sink.getConnectivityPath(source);
				
				setSourcePath(sink, source, path);
				setSourceValue(sink, source, 1f, true);
				
				for (VisibilityInteriorsConnection connection : path.getConnections()) {
					
					VisibilityInteriorsPath connectionPath = sink.getConnectivityPath(connection.getStartLocation());
									
					float visible = connectionPath.getLocations().size() == 2 ? 1f : 0f;

					addEdge(connection, 1);
					addEdgeValue(connection, visible, false);				
				}
			}
		}
								
		for (VisibilityInteriorsLocation sink : sinks) {
			
			float sum = 0f;
			
			for (float value : getSourceValues(sink).values()) {
				sum += value;
			}
			
			addSinkValue(sink, sum);
		}
	}
}