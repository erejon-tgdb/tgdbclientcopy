package routes.openflights.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Hashtable;

/**
 * @author Serge
 * Parse the OpenFlights data files : airlines.dat - airports.dat - routes.dat
 * and generate the TGDB import files : airlineType.csv - airportType.csv - airportType$edges.csv
 */
public class MainConverter {

	private static final String CSV_DELIMITER = ",";
	private static final String CSV_NEWLINE = "\n";
	private static final String SOURCE_DIR = System.getProperty("SOURCE_DIR", "routes/openflights/resources");
	private static final String IMPORT_DIR = System.getProperty("IMPORT_DIR", "routes/import");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws IOException {
		
		FilenameFilter csvFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".csv")) {
					return true;
				} else {
					return false;
				}
			}
		};
		
		File importDir = new File(IMPORT_DIR);
		// create IMPORT_DIR if does not exist
		if (!importDir.exists()) { 
			if (!importDir.mkdirs())
				throw new IOException("Cannot create import directory : " + importDir);
		// if IMPORT_DIR exists, delete all csv files inside it
		} else { 
			File[] files;
			if (((files = importDir.listFiles(csvFilter)).length) > 0)
				for (File f : files) {
					if (!f.delete())
						throw new IOException("Cannot delete existing import file : " + f.getAbsolutePath());
				}
		}

		// Create the various mappers
		OpenFlights2TGDB airline = new AirlineTypeMapper();
		OpenFlights2TGDB airport = new AirportTypeMapper();
		OpenFlights2TGDB route = new RouteTypeMapper();

		// Parse OpenFlights data files
		readOpenFlightsDataFile(SOURCE_DIR + "/airlines.dat", airline);
		readOpenFlightsDataFile(SOURCE_DIR + "/airports.dat", airport);
		readOpenFlightsDataFile(SOURCE_DIR + "/routes.dat", route, airport, airline);

		System.out.println("--------------------------------------\n");
		
		// Generate TGDB import files
		writeTGDBImportFile(IMPORT_DIR + "/" + airline.getType() +".csv", airline);
		writeTGDBImportFile(IMPORT_DIR + "/" + airport.getType() +".csv", airport);
		writeTGDBImportFile(IMPORT_DIR + "/" + route.getType() +".csv", route);
	}

	/**
	 * Parse OpenFlights data file and store data in the specified mapper
	 * @param resourcePath data file to parse
	 * @param mapperType mapper for specific TGDB desc
	 * @throws IOException cannot read file
	 */
	public static void readOpenFlightsDataFile(String resourcePath, OpenFlights2TGDB mapperType) throws IOException {
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
		if (is == null) 
			throw new IOException("Error : could not read '" + resourcePath + "'");
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
		System.out.println("### Parsing OpenFlights data : " + resourcePath + " ...");
		String line = "";
		while ((line = fileReader.readLine()) != null) {
			String[] fields = line.split(CSV_DELIMITER);
			mapperType.mapData(fields);
		}
		System.out.println("### Successfully parsed OpenFlights data : Total " + mapperType.getType() + " processed = "
				+ mapperType.getData().size() + "\n");
	}
	
	public static void readOpenFlightsDataFile(String resourcePath, OpenFlights2TGDB mapperType, OpenFlights2TGDB mapperType2, OpenFlights2TGDB mapperType3) throws IOException {
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
		if (is == null) 
			throw new IOException("Error : could not read '" + resourcePath + "'");
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
		System.out.println("### Parsing OpenFlights data : " + resourcePath + " ...");
		String line = "";
		while ((line = fileReader.readLine()) != null) {
			String[] fields = line.split(CSV_DELIMITER);
			mapperType.mapData(fields, mapperType2, mapperType3);
		}
		System.out.println("### Successfully parsed OpenFlights data : Total " + mapperType.getType() + " processed = "
				+ mapperType.getData().size() + "\n");
	}

	/**
	 * Generate TGDB import file with data from the specified mapper
	 * @param importFile file to generate
	 * @param mapperType entries to write to import file
	 * @throws IOException cannot write file
	 */
	public static void writeTGDBImportFile(String importFile, OpenFlights2TGDB mapperType) throws IOException {
		System.out.println("### Generating TGDB import file for " + mapperType.getType() + " ...");
		FileWriter fileWriter = new FileWriter(importFile);
		Hashtable<String, String[]> tgdbData = mapperType.getData();
		for (String key : tgdbData.keySet()) {
			for (int i = 0; i < tgdbData.get(key).length; i++) {
				fileWriter.append(tgdbData.get(key)[i]);
				if (i < tgdbData.get(key).length - 1)
					fileWriter.append(CSV_DELIMITER);
				else
					fileWriter.append(CSV_NEWLINE);
			}
		}
		fileWriter.flush();
		fileWriter.close();
		System.out.println("### Successfully generated TGDB import file : " + new File(importFile).getCanonicalPath() + "\n");
	}
}
