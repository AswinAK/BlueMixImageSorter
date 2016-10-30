package bluemix;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.io.File;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class VisualRecognitionExample extends JPanel implements ActionListener {
	
	JButton openButton, sortButton;
    JTextArea log;
    JFileChooser fc;
    File myFolder = null;
	
    
    
    public VisualRecognitionExample() {
    	super(new BorderLayout());
    	
    	log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        fc = new JFileChooser();

        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        openButton = new JButton("Load Folder...");
        openButton.addActionListener(this);

        sortButton = new JButton("Sort Images");
        sortButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(sortButton);

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }
    
    @Override
	public void actionPerformed(ActionEvent e) {
		//Handle open button action.
        if (e.getSource() == openButton) {
        	myFolder = null;
            int returnVal = fc.showOpenDialog(VisualRecognitionExample.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                myFolder = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.setText("");
                log.append("Load: " + myFolder.getAbsolutePath() + "\n");
            } 
            else {
                log.append("Open command cancelled by user.\n");
            }
            log.setCaretPosition(log.getDocument().getLength());

        //Handle sort button action.
        } 
        if (e.getSource() == sortButton) {
        	if(myFolder != null) {
        		log.append("Sorting: " + myFolder.getName() + ".\n");
        		//TODO BLUEMIX
        		organizePictures();
        	}
        	else {
        		log.append("Error: No directory loaded.");
        	}
            log.setCaretPosition(log.getDocument().getLength());
        }
	}
	
	private static void createAndShowGUI() {
		JFrame frame = new JFrame("HackNC");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new VisualRecognitionExample());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
    
	private static String InputPath = "/Users/AswinAk/Desktop/BlueMixTest";

	public static void main(String[] args) {
		
	    SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                //Turn off metal's use of bold fonts
	                UIManager.put("swing.boldMetal", Boolean.FALSE); 
	                createAndShowGUI();
	            }
	        });
		
	}	
	public static void organizePictures(){
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

		for(String path: filePaths){
			ClassifyImagesOptions options = new ClassifyImagesOptions.Builder()
					.images(new File(path)).build();
			VisualClassification result = service.classify(options).execute();
			//System.out.println(result);
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
		     inputChannel = null;outputChannel = null;
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







