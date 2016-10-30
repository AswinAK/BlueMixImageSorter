package bluemix;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class BlueMixImageSort extends JPanel implements ActionListener {

	JButton openButton, sortButton;
	JTextArea log;
	JFileChooser fc;
	private static File InputPath = null;
	private static String FILE_SEPARATOR;

	public BlueMixImageSort() {
		super(new BorderLayout());

		log = new JTextArea(5, 20);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);

		fc = new JFileChooser();
		// Only want to load folders of images
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		openButton = new JButton("Load Folder...");
		openButton.addActionListener(this);

		sortButton = new JButton("Sort Images");
		sortButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);
		buttonPanel.add(sortButton);
		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Handle open button action.
		if (e.getSource() == openButton) {
			InputPath = null;
			int returnVal = fc.showOpenDialog(BlueMixImageSort.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				InputPath = fc.getSelectedFile();
				log.setText("");
				log.append("Loaded: " + InputPath.getAbsolutePath() + "\n");
			} else {
				log.append("Open command cancelled by user.\n");
			}
			log.setCaretPosition(log.getDocument().getLength());
		}
		// Handle sort button action.
		if (e.getSource() == sortButton) {
			if (InputPath != null) {
				log.append("Sorting: " + InputPath.getName() + "...\n");
				organizePictures();
			} else {
				log.append("Error: No directory loaded.\n");
			}
			log.append("\nDone.\n");
			log.setCaretPosition(log.getDocument().getLength());
			try {
				String[] old = InputPath.list();
				for (int i = 0; i < old.length; i++) {
					File myFile = new File(InputPath, old[i]);
					if (!myFile.isDirectory()) {
						myFile.delete();
					}
				}
				Desktop.getDesktop().open(new File(InputPath.getAbsolutePath()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private static void createAndShowGUI() {
		String os = System.getProperty("os.name");
		if (os.startsWith("Windows")) {
			FILE_SEPARATOR = "\\";
		} else {
			FILE_SEPARATOR = "/";
		}
		JFrame frame = new JFrame("HackNC");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new BlueMixImageSort());

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public void organizePictures() {
		ArrayList<String> filePaths = new ArrayList<String>();
		ArrayList<Image> imageCollection = new ArrayList<Image>();
		Set<String> uniqueCategories = new HashSet<String>();
		log.append("\nRunning complex Image processing algorithims...\n");
		VisualRecognition service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
		service.setApiKey("9e3a667265d8ad82e2d01cf2502ede9727e74e96");

		try (Stream<Path> paths = Files.walk(Paths.get(InputPath.getAbsolutePath()))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					filePaths.add(filePath.toString());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String path : filePaths) {
			ClassifyImagesOptions options = new ClassifyImagesOptions.Builder().images(new File(path)).build();
			VisualClassification result = service.classify(options).execute();
			if (result != null) {
				String category = result.getImages().get(0).getClassifiers().get(0).getClasses().get(0).getName();
				Image image = new Image(path, category);
				imageCollection.add(image);
				uniqueCategories.add(category);
				log.append("Found: " + category + "\n");
				iterateCategories(uniqueCategories, imageCollection);
			} else {
				log.append("No files in the folder");
			}
		}
	}

	public void iterateCategories(Set<String> uniqueCategories, ArrayList<Image> imageCollection) {
		Iterator<String> setIterator = uniqueCategories.iterator();
		while (setIterator.hasNext()) {
			String currentCategory = setIterator.next();
			File file = new File(InputPath + "/" + currentCategory);
			if (!file.exists())
				file.mkdir();
		}
		byte[] buffer = new byte[1024];
		int length;
		for (Image img : imageCollection) {
			Path p = Paths.get(img.getPath());
			File input = new File(img.getPath());
			File output = new File(InputPath + "\\" + img.getCategory() + "\\" + p.getFileName().toString());
			try {
				InputStream instream = new FileInputStream(input);
				OutputStream outstream = new FileOutputStream(output);
				while ((length = instream.read(buffer)) > 0) {
					outstream.write(buffer, 0, length);
				}
				instream.close();
				outstream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
