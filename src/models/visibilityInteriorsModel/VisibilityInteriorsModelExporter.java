package models.visibilityInteriorsModel;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import evaluations.EvaluationField;
import templates.ModelEvaluation;
import templates.ModelExporter;

public class VisibilityInteriorsModelExporter implements ModelExporter{

	@Override
	public void exportEvaluation(ModelEvaluation evaluation, String fileName) {
		
		System.out.println("writing evaluation to file...");
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(((EvaluationField)evaluation).serialize());
		
		try {
			FileWriter writer = new FileWriter(fileName + ".json");  
			writer.write(json);  
			writer.close();      
		} catch (IOException e) {  
			e.printStackTrace();  
		}
		
		System.out.println("...done");
		
	}

	@Override
	public void exportEvaluations(List<ModelEvaluation> evaluations, String fileName) {

		System.out.println("writing evaluations to file...");
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		Map<Object, Object> serilizedEvaluations = new HashMap<>();
		
		for (ModelEvaluation evaluation : evaluations) {
			
			if (evaluation instanceof EvaluationField) {
				
				Map<Object, Object> serialized = ((EvaluationField) evaluation).serialize();
				
				for (Object key : serialized.keySet()) {
					serilizedEvaluations.put(key, serialized.get(key));
				}
			} 
		}
		
		String json = gson.toJson(serilizedEvaluations);
		
		try {
			FileWriter writer = new FileWriter(fileName + ".json");  
			writer.write(json);  
			writer.close();      
		} catch (IOException e) {  
			e.printStackTrace();  
		}
		
		System.out.println("...done");
		
	}

}
