package evaluations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.janino.Java.ThisReference;
import org.ietf.jgss.Oid;

import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsPath;

public class VisibilityInteriorsEvaluationExposure extends VisibilityInteriorsEvaluation{

	
	
	public VisibilityInteriorsEvaluationExposure(String label, boolean onlyModifiable, boolean onlySingleFLoor) {
		super(label, true, onlyModifiable, onlySingleFLoor, false);

		isCumulator = true;
	}
			
	@Override
	public void evaluate() {
		
		this.clear();
	
		for (VisibilityInteriorsLocation location : sinks) {
			
			List<VisibilityInteriorsLocation> sources = new ArrayList<>();
						
			for (VisibilityInteriorsLocation target : location.getVisibilityPathLocations()) {
				if (!onlySingleFloor || target.getLayout() == location.getLayout()) {
					if (!onlyModifiable || target.isModifiable()) {
						if (!onlyVisible || location.getVisibilityPath(target).getLocations().size() == 2) {
							if (!sources.contains(target) && !sinks.contains(target)) {
								if (location.getDistance(target) <= maxDistance &&
									location.getConnectivityPath(target).getLength() <= maxLength) {
									sources.add(target);
								}
							}
						}
					}
				}
			}
			
			for (VisibilityInteriorsLocation source : sources) {
				
				VisibilityInteriorsPath path = location.getConnectivityPath(source);
				
				setSourcePath(location, source, path);
				setSourceValue(location, source, 1f, true);
				
				for (VisibilityInteriorsConnection connection : path.getConnections()) {
					
					VisibilityInteriorsPath connectionPath = location.getConnectivityPath(connection.getStartLocation());
									
					float visible = connectionPath.getLocations().size() == 2 ? 1f : 0f;

					addEdge(connection, 1);
					addEdgeValue(connection, visible, false);				
				}
			}
		}
								
		for (VisibilityInteriorsLocation location : sinks) {
			
			float sum = 0f;
			
			for (float value : getSourceValues(location).values()) {
				sum += value;
			}
			
			addSinkValue(location, sum);
		}
	}
}