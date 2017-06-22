package models.visibilityInteriorsModel.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdr.geometry.primitives.Point3D;
import evaluations.VisibilityInteriorsEvaluation;
import models.isovistProjectionModel3d.IsovistProjectionLocation;

public class VisibilityInteriorsLocation extends IsovistProjectionLocation {

	private VisibilityInteriorsLayout layout;
	
	private List<VisibilityInteriorsEvaluation> evaluations = new ArrayList<>();
			
	private Map<VisibilityInteriorsLocation, VisibilityInteriorsPath> connectivityPaths = new HashMap<>();
	
	private Map<VisibilityInteriorsLocation, VisibilityInteriorsPath> visibilityPaths = new HashMap<>();
	
	private List<VisibilityInteriorsPath> connectivityFlows = new ArrayList<>();
	
	private List<VisibilityInteriorsPath> visibilityFlows = new ArrayList<>();
	
	private boolean isModifiable = false;
	
	public VisibilityInteriorsLocation(Point3D point, VisibilityInteriorsLayout layout, boolean isModifiable) {
		super(point);
		
		this.layout = layout;
		this.isModifiable = isModifiable;
		
		evaluations.add(new VisibilityInteriorsEvaluation("accessibility to nodes", false, true));
		evaluations.add(new VisibilityInteriorsEvaluation("accessibility to visible", true, false));
		evaluations.add(new VisibilityInteriorsEvaluation("accessibility to all", false, false));
	}

	public VisibilityInteriorsLayout getLayout() {
		return this.layout;
	}
	
	public boolean isModifiable() {
		return this.isModifiable;
	}
				
	public void setConnectivityPath(VisibilityInteriorsLocation target, VisibilityInteriorsPath path) {
		this.connectivityPaths.put(target, path);
	}
	
	public VisibilityInteriorsPath getConnectivityPath(VisibilityInteriorsLocation target) {
		return this.connectivityPaths.get(target);
	}
	
	public Iterable<VisibilityInteriorsLocation> getConnectivityPathLocations() {
		return this.connectivityPaths.keySet();
	}
	
	public void setVisibilityPath(VisibilityInteriorsLocation target, VisibilityInteriorsPath path) {
		this.visibilityPaths.put(target, path);
	}
	
	public VisibilityInteriorsPath getVisibilityPath(VisibilityInteriorsLocation target) {
		return this.visibilityPaths.get(target);
	}
	
	public Iterable<VisibilityInteriorsLocation> getVisibilityPathLocations() {
		return this.visibilityPaths.keySet();
	}
	
	public void addConnectivityFlow(VisibilityInteriorsPath flow) {
		this.connectivityFlows.add(flow);
	}
	
	public List<VisibilityInteriorsPath> getConnectivityFlows() {
		return this.connectivityFlows;
	}
	
	public void addVisibilityFlow(VisibilityInteriorsPath flow) {
		this.visibilityFlows.add(flow);
	}
	
	public List<VisibilityInteriorsPath> getVisibilityFlows() {
		return this.visibilityFlows;
	}
	
	public List<Integer> getEvaluationIndexes() { // TODO - blehhhhhhh
		
		List<Integer> indexes = new ArrayList<>();
		
		for (int i = 0; i < this.evaluations.size(); i++) {
			indexes.add(i);
		}
		
		return indexes;
	}
			
	public VisibilityInteriorsEvaluation getEvaluation(int index) {
		
		if (!getEvaluationIndexes().contains(index)) {
			return null;
		}
		
		return this.evaluations.get(index);
	}
	
}
