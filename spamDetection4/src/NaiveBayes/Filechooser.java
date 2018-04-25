package NaiveBayes;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

public class Filechooser {
	private JFrame mainFrame;
	private JLabel headerLabel;
	private JLabel statusLabel;
	private JPanel controlPanel;

	HashMap<String, Word> words = new HashMap<String, Word>();
	BufferedWriter out;

	public Filechooser() {
		prepareGUI();
	}

	public static void main(String[] args) {
		Filechooser swingControlDemo = new Filechooser();
		swingControlDemo.showFileChooserDemo();
	}

	private void prepareGUI() {
		mainFrame = new JFrame("Java Swing Examples");
		mainFrame.setSize(400, 400);
		mainFrame.setLayout(new GridLayout(3, 1));

		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		headerLabel = new JLabel("", JLabel.CENTER);
		// statusLabel = new JLabel("",JLabel.CENTER);
		// statusLabel.setSize(350,100);

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());

		mainFrame.add(headerLabel);
		mainFrame.add(controlPanel);
		// mainFrame.add(statusLabel);
		mainFrame.setVisible(true);
	}

	private void showFileChooserDemo() {
		headerLabel.setText("SPAM HAM message Detection");
		final JFileChooser fileDialog = new JFileChooser();
		JButton showFileDialogButton = new JButton("Open File to test");

		showFileDialogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fileDialog.showOpenDialog(mainFrame);
				String path;

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fileDialog.getSelectedFile();
					path = file.getAbsolutePath();
					System.out.println(path);

					try {
						train("train.txt");
						//filter("testFile.txt");
						filter(path);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					System.out.println("successfull");

					// statusLabel.setText("File Selected :" + file.getName());
				} else {
					// statusLabel.setText("Open command cancelled by user." );
					System.out.println("Open command cancelled by user.");
				}
			}
		});
		controlPanel.add(showFileDialogButton);
		mainFrame.setVisible(true);
	}

	public void train(String input) throws IOException {
		int totalSpamCount = 0;
		int totalHamCount = 0;
		BufferedReader in = new BufferedReader(new FileReader(input));
		String line = in.readLine();
		while (line != null) {
			if (!line.equals("")) {
				String type = line.split("\t")[0];
				String sms = line.split("\t")[1];
				for (String word : sms.split(" ")) {
					word = word.replaceAll("\\W", "");
					word = word.toLowerCase();
					Word w = null;
					if (words.containsKey(word)) {
						w = (Word) words.get(word);
					} else {
						w = new Word(word);
						words.put(word, w);
					}
					if (type.equals("ham")) {
						w.countHam();
						totalHamCount++;
					} else if (type.equals("spam")) {
						w.countSpam();
						totalSpamCount++;
					}
				}
			}
			line = in.readLine();
		}
		in.close();

		for (String key : words.keySet()) {
			words.get(key).calculateProbability(totalSpamCount, totalHamCount);
		}
	}

	// Takes the text to be analyzes as input, and produces predictions by form of
	// 'spam' or 'ham'
	public void filter(String inputFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(inputFile));
		this.out = new BufferedWriter(new FileWriter("predictions.txt"));
		String line = in.readLine();
		while (line != null) {
			if (!line.equals("")) {
				ArrayList<Word> sms = makeWordList(line);
				boolean isSpam = calculateBayes(sms);
				/*if (isSpam == true)
					this.out.write("spam");
				else if (isSpam == false)
					this.out.write("ham");*/
				if (isSpam == true) {
					//this.out.write("spam");
					System.out.println("This message is spam");
				}		
				else if (isSpam == false) {
					//this.out.write("ham");
					System.out.println("This message is ham");
				}
					
			}
			this.out.newLine();
			line = in.readLine();
		}
		this.out.close();
		in.close();
	}

	// make an arraylist of all words in an sms, set probability of spam to 0.4 if
	// word is not known
	public ArrayList<Word> makeWordList(String sms) {
		ArrayList<Word> wordList = new ArrayList<Word>();
		for (String word : sms.split(" ")) {
			word = word.replaceAll("\\W", "");
			word = word.toLowerCase();
			Word w = null;
			if (words.containsKey(word)) {
				w = (Word) words.get(word);
			} else {
				w = new Word(word);
				w.setProbOfSpam(0.40f);
			}
			wordList.add(w);
		}
		return wordList;
	}

	// Applying Bayes rule and calculating probability of ham or spam. Return true
	// if spam, false if ham
	public boolean calculateBayes(ArrayList<Word> sms) {
		//float probabilityOfPositiveProduct = 1.0f;
		//float probabilityOfNegativeProduct = 1.0f;
		float probabilitySpam = 1.0f;
		float probabilityHam = 1.0f;
		for (int i = 0; i < sms.size(); i++) {
			Word word = (Word) sms.get(i);
			//probabilityOfPositiveProduct *= word.getProbOfSpam();
			//probabilityOfNegativeProduct *= (1.0f - word.getProbOfSpam());
			probabilitySpam*= word.getProbOfSpam();
			probabilityHam*=word.getProbOfHam();
		}
		float probability=probabilitySpam/probabilityHam;
		//float probability=probabilitySpam/(probabilityHam+probabilitySpam);
		//float probOfSpam = probabilityOfPositiveProduct / (probabilityOfPositiveProduct + probabilityOfNegativeProduct);
		/*if (probOfSpam > 0.9f)
			return true;
		else
			return false;*/
		if (probability > 0.75f)
			return true;
		else
			return false;
	}
}
