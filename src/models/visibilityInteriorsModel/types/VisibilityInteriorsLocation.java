package models.visibilityInteriorsModel.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdr.geometry.primitives.Point3D;
import evaluations.VisibilityInteriorsEvaluation;
import evaluations.VisibilityInteriorsEvaluationAccessibility;
import evaluations.VisibilityInteriorsEvaluationExposure;
import evaluations.VisibilityInteriorsEvaluationVisibility;
import models.isovistProjectionModel3d.IsovistProjectionLocation;

public class VisibilityInteriorsLocation extends IsovistProjectionLocation {

	private VisibilityInteriorsLayout layout;
	
	private Map<Character, VisibilityInteriorsEvaluation> evaluations = new HashMap<>();
			
	private Map<VisibilityInteriorsLocation, VisibilityInteriorsPath> connectivityPaths = new HashMap<>();
	
	private Map<VisibilityInteriorsLocation, VisibilityInteriorsPath> visibilityPaths = new HashMap<>();
	
	private List<VisibilityInteriorsPath> connectivityFlows = new ArrayList<>();
	
	private List<VisibilityInteriorsPath> visibilityFlows = new ArrayList<>();
	
	private boolean isModifiable = false;
	private boolean isAccess = false;
	private boolean isActive = false;
	
	public VisibilityInteriorsLocation(Point3D point, VisibilityInteriorsLayout layout, boolean isModifiable, boolean isAccess) {
		super(point);
		
		this.layout = layout;
		this.isModifiable = isModifiable;
		this.isAccess = isAccess;
	}

	public VisibilityInteriorsLayout getLayout() {
		return this.layout;
	}
	
	public boolean isModifiable() {
		return this.isModifiable;
	}
	
	public boolean isAccess() {
		return this.isAccess;
	}
	
	public void setActive(boolean active) {
		this.isActive = active;
	}
	
	public boolean isActive() {
		return this.isActive;
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
	
	public void addEvaluation(Character key, VisibilityInteriorsEvaluation evaluation) {
		this.evaluations.put(key, evaluation);
	}
	
	public List<VisibilityInteriorsEvaluation> getEvaluations() {
		return new ArrayList<>(this.evaluations.values());
	}
			
	public VisibilityInteriorsEvaluation getEvaluation(Character key) {
		return this.evaluations.get(key);
	}
	
}
