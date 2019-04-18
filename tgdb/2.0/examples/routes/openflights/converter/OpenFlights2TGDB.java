package routes.openflights.converter;

import java.util.Hashtable;

/**
 * @author Serge 
 * Map OpenFlights data to TGDB data
 * Mapping is different for airlineType, airportType and airportType$edge
 */
public interface OpenFlights2TGDB {

	/**
	 * Map OpenFlights fields to TGDB attributes
	 * @param nodeFields Openflights fields of a given entry in a data file
	 */
	void mapData(String[] nodeFields);
	
	void mapData(String[] edgeFields, OpenFlights2TGDB airport, OpenFlights2TGDB airline);

	String getType();

	Hashtable<String, String[]> getData();

}

class AirlineTypeMapper implements OpenFlights2TGDB {

	public String type = "airlineType";

	// Hold the data that goes to import csv files
	public Hashtable<String, String[]> tgdbData = new Hashtable<String, String[]>();

	private static final int AIRLINE_ID = 0;
	private static final int AIRLINE_NAME = 1;
	private static final int AIRLINE_IATA = 3;
	private static final int AIRLINE_ICAO = 4;
	private static final int AIRLINE_COUNTRY = 6;
	private static final int AIRLINE_ACTIVE = 7;

	@Override
	public void mapData(String[] fields) {

		if (fields.length != 8) {
			System.out.println("WARNING - Expecting 8 fields per airline entry but this one has " + fields.length
					+ " : Airline ID = " + fields[AIRLINE_ID]);
		} else {
			// Map OpenFlights fields to our example fields (we are not interested in
			// everything single field)
			// Note: Value '\N' for a given field means null in OpenFlights
			String[] airline = new String[7];
			airline[0] = "AIRLINE" + fields[AIRLINE_ID]; // internal import ID
			airline[1] = "AIRLINE" + fields[AIRLINE_ID]; // airline ID - Pkey
			airline[2] = fields[AIRLINE_NAME].equals("\\N") ? null : fields[AIRLINE_NAME]; // airline name
			airline[3] = fields[AIRLINE_IATA].equals("\\N") ? null : fields[AIRLINE_IATA]; // airline iata code
			airline[4] = fields[AIRLINE_ICAO].equals("\\N") ? null : fields[AIRLINE_ICAO]; // airline icao code
			airline[5] = fields[AIRLINE_COUNTRY].equals("\\N") ? null : fields[AIRLINE_COUNTRY];// airline country
			airline[6] = fields[AIRLINE_ACTIVE].equals("\"Y\"") ? "1" : "0"; // airline active

			tgdbData.put((String) airline[0], airline);
		}
	}

	@Override
	public Hashtable<String, String[]> getData() {
		return this.tgdbData;
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public void mapData(String[] fields, OpenFlights2TGDB fromNode, OpenFlights2TGDB toNode) {
		;
	}

}

class AirportTypeMapper implements OpenFlights2TGDB {

	public String type = "airportType";

	// Hold the data that goes to import csv files
	public Hashtable<String, String[]> tgdbData = new Hashtable<String, String[]>();

	private static final int AIRPORT_ID = 0;
	private static final int AIRPORT_NAME = 1;
	private static final int AIRPORT_CITY = 2;
	private static final int AIRPORT_COUNTRY = 3;
	private static final int AIRPORT_IATA = 4;
	private static final int AIRPORT_ICAO = 5;

	@Override
	public void mapData(String[] fields) {

		if (fields.length != 14) {
			System.out.println("WARNING - Expecting 14 fields per airport entry but this one has " + fields.length
					+ " : Airport ID = " + fields[AIRPORT_ID]);
		} else {
			// Map OpenFlights fields to our example fields (we are not interested in
			// everything single field)
			// Note: Value '\N' for a given field means null in OpenFlights
			String[] airport = new String[7];
			airport[0] = "AIRPORT" + fields[AIRPORT_ID]; // internal import ID
			airport[1] = "AIRPORT" + fields[AIRPORT_ID]; // airport ID - Pkey
			airport[2] = fields[AIRPORT_NAME].equals("\\N") ? null : fields[AIRPORT_NAME]; // airport name
			airport[3] = fields[AIRPORT_CITY].equals("\\N") ? null : fields[AIRPORT_CITY]; // airport city
			airport[4] = fields[AIRPORT_COUNTRY].equals("\\N") ? null : fields[AIRPORT_COUNTRY];// airport country
			airport[5] = fields[AIRPORT_IATA].equals("\\N") ? null : fields[AIRPORT_IATA]; // airport iata code
			airport[6] = fields[AIRPORT_ICAO].equals("\\N") ? null : fields[AIRPORT_ICAO]; // airport icao code

			tgdbData.put((String) airport[0], airport);
		}
	}

	@Override
	public Hashtable<String, String[]> getData() {
		return this.tgdbData;
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public void mapData(String[] fields, OpenFlights2TGDB fromNode, OpenFlights2TGDB toNode) {
		;
	}
}

class RouteTypeMapper implements OpenFlights2TGDB {

	public String type = "airportType$edges";
	int id = 1;

	// Hold the data that goes to import csv files
	public Hashtable<String, String[]> tgdbData = new Hashtable<String, String[]>();

	private static final int AIRLINE_ID = 1;
	private static final int AIRPORT_FROM = 3;
	private static final int AIRPORT_TO = 5;
	private static final int ROUTE_STOPS = 7;

	@Override
	public void mapData(String[] edgeFields, OpenFlights2TGDB airport, OpenFlights2TGDB airline) {

		if (edgeFields.length != 9) {
			System.out.println("WARNING - Expecting 9 fields per route entry but this one has " + edgeFields.length
					+ " : Airline ID = " + edgeFields[AIRLINE_ID]);
		} else if (edgeFields[ROUTE_STOPS].equals("0") 									// direct flights only
				&& (!edgeFields[AIRLINE_ID].equals("\\N")) 								// airlineID not null
				&& (!edgeFields[AIRPORT_FROM].equals("\\N")) 							// airportFrom not null
				&& (!edgeFields[AIRPORT_TO].equals("\\N"))  							// airportTo not null
				&& (airline.getData().containsKey("AIRLINE"+edgeFields[AIRLINE_ID]))	// airlineID exists in the airline.dat
				&& (airport.getData().containsKey("AIRPORT"+edgeFields[AIRPORT_FROM]))	// airportFrom exists in the airport.dat
				&& (airport.getData().containsKey("AIRPORT"+edgeFields[AIRPORT_TO])))	// airportTo exists in the airport.dat
			{
			// Map OpenFlights fields to our example fields (we are not interested in
			// every single field)
			// Note: Value '\N' for a given field means null in OpenFlights
			String[] route = new String[7];
			route[0] = id + ""; // internal edge ID
			route[1] = "1111"; // edgetype ID
			route[2] = "AIRPORT" + edgeFields[AIRPORT_FROM]; // from airport
			route[3] = "AIRPORT" + edgeFields[AIRPORT_TO]; // to airport
			route[4] = "AIRLINE" + edgeFields[AIRLINE_ID]; // airline id
			route[5] = airline.getData().get(route[4])[2]; // airline name
			route[6] = airline.getData().get(route[4])[3]; // airline iata code

			tgdbData.put((String) route[0], route);
			id++;
		}
	}

	@Override
	public Hashtable<String, String[]> getData() {
		return this.tgdbData;
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public void mapData(String[] fields) {
		;
	}
}