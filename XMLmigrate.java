/*
 * XMLmigrate
 * 
 * A schema migration tool for XML regression test files
 * 
 * @author Peter Winter
 * @version 1.1
 *
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class XMLmigrate {
	
	// Interne Variablen, nicht aendern!
	private String inputFileName = "";
	private String outputFileName = "";
	private String actionsMigrateFile = "actions.migrate";
	private Scanner scanner = new Scanner("");
	private String currentLine = "";
	private String currentAction = "";
	private List<String> fileInMemoryList = new ArrayList<String>();
	private List<String> actionsInMemoryList = new ArrayList<String>();


	/**
	 * Erstellt ein Objekt vom Typ XMLmigrate, welches eine Input- und Output-Datei fuer die Regressionstest-Daten benoetigt.
	 * @param inputFile Die XML Input-Datei
	 * @param outputFile Die XML Output-Datei
	 */
	XMLmigrate(String inputFile, String outputFile) {

		inputFileName = inputFile;
		outputFileName = outputFile;
		
		readInputFileIntoMemory();

		readActionsMigrateFileIntoMemory();
	}
	
	/**
	 * Erstellt ein Objekt vom Typ XMLmigrate. Die Daten werden in der Input-Datei nach den Aenderungen direkt ueberschrieben
	 * @param inputFile Die Input- und zugleich auch Output-Datei
	 */
	XMLmigrate(String inputFile) {
		
		this(inputFile, inputFile);
		
	}
	

	/**
	 * Liest die gesamte Input-Datei in den Hauptspeicher ein.
	 */
	public void readInputFileIntoMemory() {
		
		// Oeffne die input Datei
		try {
			scanner = new Scanner( new File(inputFileName) );
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		// Packe die Datei in die Liste im Hauptspeicher
		while (scanner.hasNextLine()) {

			fileInMemoryList.add(scanner.nextLine());

		}

	}
	

	/**
	 * Liest die actions.migrate Datei in den Hauptspeicher ein.
	 */
	public void readActionsMigrateFileIntoMemory() {
		
		// Oeffne die input Datei
		try {
			scanner = new Scanner( new File(actionsMigrateFile) );
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		// Packe die Datei in die Liste im Hauptspeicher
		while (scanner.hasNextLine()) {

			currentLine = scanner.nextLine();
			
			// Webb Zeile nicht auskommentiert, fuege sie als action hinzu
			if (!currentLine.startsWith("//")) {
				actionsInMemoryList.add(currentLine);
			}

		}

	}
	

	/**
	 * Fuehrt die actions aus der actions.migrate Datei aus
	 */
	public void execActions() {

		String[] action = new String[10];
		
		Iterator<String> actionsIterator = actionsInMemoryList.iterator();
    	
    	while (actionsIterator.hasNext()) {
    		
    		currentAction = actionsIterator.next();
    		
    		action = currentAction.split(","); 
    		
    		if (action[0].equals("addAttribute")) {
    			addAttribute(action[1], action[2], action[3], action[4]);
    		} else if (action[0].equals("removeAttribute")) {
    			removeAttribute(action[1], action[2], action[3]);
    		} else if (action[0].equals("renameAttribute")) {
    			renameAttribute(action[1], action[2], action[3], action[4]);
    		} else if (action[0].equals("changeAttributeValue")) {
    			changeAttributeValue(action[1], action[2], action[3], action[4]);
    		} else if (action[0].equals("renameTable")) {
    			renameTable(action[1], action[2], action[3]);
    		} else if (action[0].equals("renameSchema")) {
    			renameSchema(action[1], action[2]);
    		} else if (action[0].equals("deleteTable")) {
    			deleteTable(action[1], action[2]);
    		} else if (action[0].equals("deleteSchema")) {
    			deleteSchema(action[1]);
    		} else {
    			System.out.println("Keine gueltige action!");
    		}
    		
    	}
		

	}
	

	/**
	 * Liest die geparsten Zeilen aus der Liste im Hauptspeicher und schreibt sie in die Output-Datei
	 */
	public void writeXMLtoDisk() {
		
        try {
        	
        	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFileName)));
        	Iterator<String> listIterator = fileInMemoryList.iterator();
        	
        	while (listIterator.hasNext()) {

                bw.write(listIterator.next());

                // Neue Zeile explizit im UNIX-Format setzen, nicht mit der Systemvariable newLine();
                bw.write("\n");

        	}
        	
        	bw.close();
        	
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}


	/**
	 * Fuegt ein Attribut mit Wert zu einer bestimmten Tabelle hinzu
	 * @param schema Der Name des Schemas
	 * @param table Der Name der Tabelle
	 * @param attribute Der Name des Attributs
	 * @param value Der Wert des Attributs
	 */
	public void addAttribute(String schema, String table, String attribute, String value) {
		
		List<String> tempList = new ArrayList<String>();
		
    	Iterator<String> listIterator = fileInMemoryList.iterator();

		// Hier beginnt das eigentliche Parsen
		while (listIterator.hasNext()) {

			currentLine = listIterator.next();

			// Wenn die Zeile den Namen des Schemas und der Tabelle, jedoch nicht das Attribut enthaelt, springe rein und fuege es mit Wert hinzu
			if (currentLine.contains("<" + schema + "." + table + " ") && !currentLine.contains(attribute + "=")) {
				
				// Fuege das Attribut mit Wert hinzu
				currentLine = currentLine.replace("/>", " " + attribute + "=\"" + value + "\"/>");
				
				System.out.println("Changed line:\n" + currentLine);

			}

			tempList.add(currentLine);

		}

		// Biege die Referenz von der fileInMemoryList auf die veraenderte, nun aktuelle tempList um
		fileInMemoryList = tempList;

	}		

	
	/**
	 * Entfernt ein Attribut mit Wert aus einer bestimmten Tabelle
	 * @param schema Der Name des Schemas
	 * @param table Der Name der Tabelle
	 * @param attribute Der Name des Attributs
	 */
	public void removeAttribute(String schema, String table, String attribute) {

		List<String> tempList = new ArrayList<String>();
		
		Iterator<String> listIterator = fileInMemoryList.iterator();
		
		// Hier beginnt das eigentliche Parsen
		while (listIterator.hasNext()) {

			currentLine = listIterator.next();
			
			// Wenn die Zeile den Namen des Schemas, der Tabelle und des zu veraendernden Attributs enthaelt, springe rein und entferne das Attribut mit Wert
			if (currentLine.contains("<" + schema + "." + table + " ") && currentLine.contains(attribute + "=")) {
				
				// Benenne das Attribut um
				currentLine = currentLine.replaceAll(" " + attribute + "=\".*?\"", "");
				
				System.out.println("Changed line:\n" + currentLine);

			}

			tempList.add(currentLine);
			
		}
		
		// Biege die Referenz von der fileInMemoryList auf die veraenderte, nun aktuelle tempList um
		fileInMemoryList = tempList;

	}
	
	
	/**
	 * Benennt ein Attribut in einer bestimmten Tabelle um
	 * @param schema Der Name des Schemas
	 * @param table Der Name der Tabelle
	 * @param attributeOld Der alte Name des Attributs
	 * @param attributeNew Der neue Name des Attributs
	 */
	public void renameAttribute(String schema, String table, String attributeOld, String attributeNew) {

		List<String> tempList = new ArrayList<String>();
		
		Iterator<String> listIterator = fileInMemoryList.iterator();
		
		// Hier beginnt das eigentliche Parsen
		while (listIterator.hasNext()) {

			currentLine = listIterator.next();
			
			// Wenn die Zeile den Namen des Schemas, der Tabelle und des zu veraendernden Attributs enthaelt, springe rein und benenne das Attribut um
			if (currentLine.contains("<" + schema + "." + table + " ") && currentLine.contains(attributeOld + "=")) {
				
				// Benenne das Attribut um
				currentLine = currentLine.replace(attributeOld, attributeNew);
				
				System.out.println("Changed line:\n" + currentLine);

			}

			tempList.add(currentLine);
			
		}
		
		// Biege die Referenz von der fileInMemoryList auf die veraenderte, nun aktuelle tempList um
		fileInMemoryList = tempList;

	}

	
	/**
	 * Aendert den Wert eines bestimmten Attributs in einer bestimmten Tabelle
	 * @param schema Der Name des Schemas
	 * @param table Der Name der Tabelle
	 * @param attribute Der Name des Attributs
	 * @param newValue Der neue Wert des Attributs
	 */
	public void changeAttributeValue(String schema, String table, String attribute, String newValue) {

		List<String> tempList = new ArrayList<String>();
		
		Iterator<String> listIterator = fileInMemoryList.iterator();
		
		// Hier beginnt das eigentliche Parsen
		while (listIterator.hasNext()) {

			currentLine = listIterator.next();
			
			// Wenn die Zeile den Namen des Schemas, der Tabelle und des zu veraendernden Attributs enthaelt, springe rein und aendere den Wert
			if (currentLine.contains("<" + schema + "." + table + " ") && currentLine.contains(attribute + "=")) {
				
				// Benenne das Attribut um
				currentLine = currentLine.replaceAll(" " + attribute + "=\".*?\"", " " + attribute + "=\"" + newValue + "\"");
				
				System.out.println("Changed line:\n" + currentLine);

			}

			tempList.add(currentLine);
			
		}
		
		// Biege die Referenz von der fileInMemoryList auf die veraenderte, nun aktuelle tempList um
		fileInMemoryList = tempList;

	}

	
	/**
	 * Benennt eine bestimme Tabelle um.
	 * @param schema Der Name des Schemas
	 * @param tableOld Der alte Name der Tabelle
	 * @param tableNew Der neue Name der Tabelle
	 */
	public void renameTable(String schema, String tableOld, String tableNew) {

		List<String> tempList = new ArrayList<String>();
		
		Iterator<String> listIterator = fileInMemoryList.iterator();
		
		// Hier beginnt das eigentliche Parsen
		while (listIterator.hasNext()) {

			currentLine = listIterator.next();
			
			// Wenn die Zeile den Namen des Schemas, der Tabelle und des zu veraendernden Attributs enthaelt, springe rein und aendere den Wert
			if (currentLine.contains("<" + schema + "." + tableOld + " ")) {
				
				// Benenne das Attribut um
				currentLine = currentLine.replace("<" + schema + "." + tableOld + " ", "<" + schema + "." + tableNew + " ");
				
				System.out.println("Changed line:\n" + currentLine);

			}

			tempList.add(currentLine);
			
		}
		
		// Biege die Referenz von der fileInMemoryList auf die veraenderte, nun aktuelle tempList um
		fileInMemoryList = tempList;

	}

	
	/**
	 * Benennt ein bestimmtes Schema um
	 * @param schemaOld Der alte Name des Schemas
	 * @param schemaNew Der neue Name des Schemas
	 */
	public void renameSchema(String schemaOld, String schemaNew) {

		List<String> tempList = new ArrayList<String>();
		
		Iterator<String> listIterator = fileInMemoryList.iterator();
		
		// Hier beginnt das eigentliche Parsen
		while (listIterator.hasNext()) {

			currentLine = listIterator.next();
			
			// Wenn die Zeile den Namen des Schemas enthaelt, springe rein und aendere ihn
			if (currentLine.contains("<" + schemaOld + ".")) {
				
				// Benenne das Attribut um
				currentLine = currentLine.replace("<" + schemaOld + ".", "<" + schemaNew + ".");
				
				System.out.println("Changed line:\n" + currentLine);

			}

			tempList.add(currentLine);
			
		}
		
		// Biege die Referenz von der fileInMemoryList auf die veraenderte, nun aktuelle tempList um
		fileInMemoryList = tempList;

	}
	
	
	/**
	 * Loescht eine bestimmte Tabelle
	 * @param schema Der Name des Schemas
	 * @param table Der Name der Tabelle
	 */
	public void deleteTable(String schema, String table) {

		List<String> tempList = new ArrayList<String>();
		
		Iterator<String> listIterator = fileInMemoryList.iterator();
		
		// Hier beginnt das eigentliche Parsen
		while (listIterator.hasNext()) {

			currentLine = listIterator.next();
			
			// Wenn die Zeile den Namen des Schemas enthaelt, springe rein und aendere ihn
			if (!currentLine.contains("<" + schema + "." + table + " ")) {
				
				// Fuege die Zeile beim Durchlaufen nur wieder hinzu, wenn der Name des Schemas und der Tabelle NICHT in Zeile vorkommt
				tempList.add(currentLine);

			}

		}
		
		// Biege die Referenz von der fileInMemoryList auf die veraenderte, nun aktuelle tempList um
		fileInMemoryList = tempList;

	}
	

	/**
	 * Loescht ein bestimmtes Schema
	 * @param schema Der Name des Schemas
	 */
	public void deleteSchema(String schema) {

		List<String> tempList = new ArrayList<String>();
		
		Iterator<String> listIterator = fileInMemoryList.iterator();
		
		// Hier beginnt das eigentliche Parsen
		while (listIterator.hasNext()) {

			currentLine = listIterator.next();
			
			// Wenn die Zeile den Namen des Schemas enthaelt, ueberspringe Sie (= Loeschen) beim Hinzufuegen zur Temp-Liste
			if (!currentLine.contains("<" + schema + ".")) {
				
				// Fuege die Zeile beim Durchlaufen nur wieder hinzu, wenn der Name des Schemas und der Tabelle NICHT in Zeile vorkommt
				tempList.add(currentLine);

			}

		}
		
		// Biege die Referenz von der fileInMemoryList auf die veraenderte, nun aktuelle tempList um
		fileInMemoryList = tempList;

	}
	
	
	/**
	 * Test-Funktion um die Funktionalitaet der Klasse im Standalone-Betrieb zu verifizieren.
	 * @param args
	 */
	public static void main(String[] args) {

		//if (args.length == 1) {
			//inputFileName = args[0];
		//}
		
		// Zeitstempel um Verarbeitungsgeschwindigkeit zu pruefen
		long timeNow;
		long timeAfter;

		timeNow = System.currentTimeMillis();
		
		XMLmigrate migrate = new XMLmigrate("full_data.xml", "full_data_new.xml");
		migrate.execActions();
		migrate.writeXMLtoDisk();
		
		timeAfter = System.currentTimeMillis();
		
		System.out.println("Execution time: " + ((timeAfter - timeNow) / 1000) + " second(s)");
		
	  }

}
