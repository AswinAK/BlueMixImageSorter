package bluemix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import java.io.File;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class VisualRecognitionExample {
	private static String InputPath = "/Users/AswinAk/Desktop/BlueMixTest";

	public static void main(String[] args) {
		
		ArrayList<String> filePaths = new ArrayList<String>();
		ArrayList<Image> imageCollection =  new ArrayList<Image>();
		Set<String> uniqueCategories = new HashSet<String>();
		File file = null;
		FileChannel inputChannel,outputChannel;
		String source,dest;
		
		VisualRecognition service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
		service.setApiKey("9e3a667265d8ad82e2d01cf2502ede9727e74e96");
		
		try(Stream<Path> paths = Files.walk(Paths.get(InputPath))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		            //System.out.println(filePath.toString());
		            filePaths.add(filePath.toString());
		        }
		    });
		} 
		catch(Exception e){
			e.printStackTrace();
		}

		int i =1;
		for(String path: filePaths){
			ClassifyImagesOptions options = new ClassifyImagesOptions.Builder()
					.images(new File(path)).build();
			VisualClassification result = service.classify(options).execute();
			System.out.println("Analysis for image "+i);
			System.out.println(result);
			String category = result.getImages().get(0).getClassifiers().get(0).getClasses().get(0).getName();
			Image image = new Image(path, category);
			imageCollection.add(image);
			uniqueCategories.add(category);
		}
		
		System.out.println("Set is "+uniqueCategories);
		
		Iterator<String> setIterator  = uniqueCategories.iterator();
		
		while(setIterator.hasNext()){
			String currentCategory = setIterator.next();	
			file = new File(InputPath+"/"+currentCategory);
			if (!file.exists())
				file.mkdir();
		}
		
		for(Image img: imageCollection){
		     inputChannel = null;
			 outputChannel = null;
			 source = img.getPath();
			 dest = InputPath+"/"+img.getCategory();
			try {
				inputChannel = new FileInputStream(source).getChannel();
				outputChannel = new FileOutputStream(dest).getChannel();
				outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
				inputChannel.close();
				outputChannel.close();
			} 
			catch(Exception e){
				
			}
			
			
			
			
		}
		

	}
}