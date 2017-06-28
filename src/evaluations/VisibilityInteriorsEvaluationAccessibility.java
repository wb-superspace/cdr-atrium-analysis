package evaluations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsPath;

public class VisibilityInteriorsEvaluationAccessibility extends VisibilityInteriorsEvaluation {

	public VisibilityInteriorsEvaluationAccessibility(String label, boolean onlyVisible, boolean onlyModifiable,
			boolean onlySingleFLoor, boolean onlyAcess) {
		super(label, onlyVisible, onlyModifiable, onlySingleFLoor, onlyAcess);

	}
	
	@Override
	public void evaluate() {

		Set<VisibilityInteriorsLocation> sources = new HashSet<>();
		Set<VisibilityInteriorsLocation> locations = new HashSet<>(sinks);
		
		this.clear();
	
		for (VisibilityInteriorsLocation location : locations) {			
			for (VisibilityInteriorsLocation target : location.getVisibilityPathLocations()) {
				if (!onlyAccess || target.isAccess()) {
					if (!onlySingleFloor || target.getLayout() == location.getLayout()) {
						if (!onlyModifiable || target.isModifiable()) {
							if (!onlyVisible || location.getVisibilityPath(target).getLocations().size() == 2) {
								if (!sources.contains(target) && !locations.contains(target)) {
									if (location.getDistance(target) <= maxDistance &&
										location.getConnectivityPath(target).getLength() <= maxLength) {
										sources.add(target);
									}
								}
							}
						}
					}
				}
			}
		}
						
		for (VisibilityInteriorsLocation source : sources) {
			
			VisibilityInteriorsPath minPath = null;
			VisibilityInteriorsLocation minSink = null;
			
			for (VisibilityInteriorsLocation location : locations) {
				
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
		
		for (VisibilityInteriorsLocation location : locations) {
			
			float average = 0f;
			
			for (float value : getSourceValues(location).values()) {
				average += value / (float) getSourceValues(location).size();
			}
			
			addSinkValue(location, average);
		}
	}
}
