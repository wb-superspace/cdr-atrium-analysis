package evaluations;

import java.util.List;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsPath;

public class VisibilityInteriorsEvaluationVisibility extends VisibilityInteriorsEvaluation{

	public VisibilityInteriorsEvaluationVisibility(String label, boolean onlyVisible, boolean onlyModifiable,
			boolean onlySingleFLoor) {
		super(label, onlyVisible, onlyModifiable, onlySingleFLoor, false);
		
		isCumulator = true;
	}
	
	@Override
	public void evaluate() {
		
		this.clear();
										
		for (VisibilityInteriorsLocation location : sinks) {
			
			float sum = 0f;
			
			for (List<IsovistProjectionPolygon> projectionPolygons : location.getProjectionPolygons().values()) {
				for (IsovistProjectionPolygon projectionPolygon : projectionPolygons) {
					sum += projectionPolygon.getPolygon3DWithHoles().area();
				}
			}
			
			for (VisibilityInteriorsLocation connection : location.getConnectivityPathLocations()) {
				
				VisibilityInteriorsPath path= location.getConnectivityPath(connection);
				
				if (path.getConnections().size() == 1) {
					
					VisibilityInteriorsConnection c = path.getConnections().get(0); 
																		
					addEdge(c, sum);
					addEdgeValue(c, sum, false);
				}
			}
			
			addSinkValue(location, sum);
		}
	}
}