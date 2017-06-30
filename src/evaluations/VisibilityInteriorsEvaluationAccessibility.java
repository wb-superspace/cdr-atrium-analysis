package evaluations;

import java.util.HashSet;
import java.util.Set;

import models.VisibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsPath;

public class VisibilityInteriorsEvaluationAccessibility extends VisibilityInteriorsEvaluation {

	public VisibilityInteriorsEvaluationAccessibility(String label) {
		super(label);
	}

	@Override
	public void evaluate() {

		Set<VisibilityInteriorsLocation> sources = new HashSet<>();
		Set<VisibilityInteriorsLocation> sinks = new HashSet<>(this.sinks);
		
		this.clear();
	
		for (VisibilityInteriorsLocation sink : sinks) {			
			for (VisibilityInteriorsLocation source : this.sources) {
				if (!sink.equals(source)) {
					if (sink.getDistance(source) <= maxDistance &&
						sink.getConnectivityPath(source).getLength() <= maxLength) {
						sources.add(source);
					}	
				}		
			}
		}
						
		for (VisibilityInteriorsLocation source : sources) {
			
			VisibilityInteriorsPath minPath = null;
			VisibilityInteriorsLocation minSink = null;
			
			for (VisibilityInteriorsLocation location : sinks) {
				
				VisibilityInteriorsPath path = location.getConnectivityPath(source);
				
				if (path == null) {					
					continue;
				
				} else if (minPath == null || path.getLength() < minPath.getLength()) {
					minPath = path;
					minSink = location;
				}
			}
						
			if (minPath == null) {							
				continue; 
			}
						
			setSourcePath(minSink, source, minPath);
			setSourceValue(minSink, source, minPath.getAccessibility(), true);
			
			for (VisibilityInteriorsConnection connection : minPath.getConnections()) {
				
				VisibilityInteriorsPath connectionPath = minSink.getConnectivityPath(connection.getStartLocation());
				
				addEdge(connection, 1);
				addEdgeValue(connection, connectionPath.getAccessibility(), false);			
			}
		}
		
		for (VisibilityInteriorsLocation sink : sinks) {
			
			float average = 0f;
			
			for (float value : getSourceValues(sink).values()) {
				average += value / (float) getSourceValues(sink).size();
			}
			
			addSinkValue(sink, average);
		}
	}
}
