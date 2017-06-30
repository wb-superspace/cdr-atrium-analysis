package evaluations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsPath;

public class VisibilityInteriorsEvaluationVisibility extends VisibilityInteriorsEvaluation{
	
	public VisibilityInteriorsEvaluationVisibility(String label) {
		super(label);
		
		isCumulator = true;
	}

	@Override
	public void evaluate() {
		
		this.clear();
		
		Set<VisibilityInteriorsLocation> sinks = new HashSet<>(this.sinks);
										
		for (VisibilityInteriorsLocation sink : sinks) {
			
			float sum = 0f;
			
			for (List<IsovistProjectionPolygon> projectionPolygons : sink.getProjectionPolygons().values()) {
				for (IsovistProjectionPolygon projectionPolygon : projectionPolygons) {
					sum += projectionPolygon.getPolygon3DWithHoles().area();
				}
			}
			
			for (VisibilityInteriorsLocation connection : sink.getConnectivityPathLocations()) {
				
				VisibilityInteriorsPath path = sink.getConnectivityPath(connection);
				
				if (path.getConnections().size() == 1) {
					
					VisibilityInteriorsConnection c = path.getConnections().get(0); 
																		
					addEdge(c, sum);
					addEdgeValue(c, sum, false);
				}
			}
			
			addSinkValue(sink, sum);
		}
	}
}