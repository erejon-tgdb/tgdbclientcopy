package routes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import com.tibco.tgdb.connection.TGConnection;
import com.tibco.tgdb.model.TGEdge;
import com.tibco.tgdb.model.TGEntity;
import com.tibco.tgdb.model.TGNode;
import com.tibco.tgdb.model.TGEdgeType;
import com.tibco.tgdb.query.TGResultSet;
import com.tibco.tgdb.query.TGQueryOption;

import common.AbstractGraph;

/**
 * 
 * Usage : java -cp .:<tgdb_home>/lib/tgdb-client.jar routes.OpenFlightsQuery [-noprint]
 * -noprint => Do not print routes. Just print number of routes
 * 
 */
public class OpenFlightsQuery extends AbstractGraph{

	private Scanner reader;
	private int maxStops = -1;
	private String airlines;
	private String fromAirport;
	private String toAirport;
	private boolean noPrint = false;

	OpenFlightsQuery(String[] args) throws Exception
	{
		super(args);
		for (int i=0; i<args.length; i++) {
			if (args[i].equals("-noprint"))
				noPrint = true;
		}
	}

	OpenFlightsQuery(TGConnection conn) throws Exception
	{
		super(conn);
	}

	private void readUserRequest() throws Exception
	{
		while (fromAirport == null) {
			try {
				reader = new Scanner(System.in);
				System.out.print("Enter departure airport (3-letter code) : ");
				fromAirport = reader.next("...").toUpperCase();
			} catch (InputMismatchException e) {
				continue;
			}
		}
		while (toAirport == null) {
			try {
				reader = new Scanner(System.in);
				System.out.print("Enter destination airport (3-letter code) : ");
				toAirport = reader.next("...").toUpperCase();
			} catch (InputMismatchException e) {
				continue;
			}
		}
		while (maxStops < 0) {
			try {
				reader = new Scanner(System.in);
				System.out.print("Enter number of stops : ");
				maxStops = reader.nextInt();
			} catch (InputMismatchException e) {
				continue;
			}
		}
		while (airlines == null) {
			try {
				reader = new Scanner(System.in);
				System.out.print("Enter airlines (2-letter code separated by comma) - Optional : ");
				airlines = reader.nextLine().toUpperCase();
				if (airlines.isEmpty() || airlines.matches("^..(,..)*$"))
					;
				else
					airlines = null;
			} catch (InputMismatchException e) {
				continue;
			}
		}
	}

	private boolean hasMoreRequest() throws Exception
	{
		String moreQuery = null;
		while (true) { // Want to continue ?
			try {
				reader = new Scanner(System.in);
				System.out.print("More queries ? (Y/N) : ");
				moreQuery = reader.next("[Yy]|[Nn]");
				break;
			} catch (InputMismatchException e) {
				continue;
			}
		}
		fromAirport = null;
		toAirport = null;
		maxStops = -1;
		airlines = null;

		return moreQuery.equalsIgnoreCase("Y");
	}

	private void findRoutes() throws Exception
	{
		String queryString = null;
		String traverseString = null;
		String endString = null;
		TGResultSet resultSet = null;

		TGQueryOption option = TGQueryOption.createQueryOption();
		option.setTraversalDepth(maxStops + 1);

		// Search for airline ID first
		String[] airlineCode = null;
		String[][] airlineIDName = null;
		if (!airlines.equals("")) {
			airlineCode = airlines.split(",");
			queryString = "@nodetype = 'airlineType'";
			for(int i=0; i<airlineCode.length; i++) {
				if (i==0)
					queryString = queryString + " and (iataCode = '" +airlineCode[i]+ "'";
				else
					queryString = queryString + " or iataCode = '" +airlineCode[i]+ "'";
			}
			queryString = queryString + ");";
			resultSet = conn.executeQuery(queryString, null);


			if (resultSet != null) {
				airlineIDName = new String[resultSet.count()][2];
				while (resultSet.hasNext()) {
					TGEntity airline = resultSet.next();
					airlineIDName[resultSet.getPosition()][0] = airline.getAttribute("airlineID").getAsString();
					airlineIDName[resultSet.getPosition()][1] = airline.getAttribute("name").getAsString();
				}
			} else {
				System.out.printf("\nWarning : Airlines %s not found\n", airlines);
				airlineIDName = new String[0][2];
			}
		}
		else
			airlineIDName = new String[0][2];

		// Build and execute query
		queryString = "@nodetype = 'airportType' and iataCode = '" + fromAirport + "';";
		traverseString = "@edgetype = 'routeType' and @isfromedge = 1";
		for (int i=0; i<airlineIDName.length; i++) {
			if (i==0)
				traverseString = traverseString + " and (@edge.airlineID = '" + airlineIDName[i][0] + "'";
			else
				traverseString = traverseString + " or @edge.airlineID = '" + airlineIDName[i][0] + "'";
			if (i==airlineIDName.length-1)
				traverseString = traverseString + ")";

		}
		traverseString = traverseString + " and @degree <= " + (maxStops + 1) + ";";
		endString = "@tonodetype = 'airportType' and @tonode.iataCode = '" + toAirport + "';";

		System.out.println("\nYour Query Parameters : ");
		System.out.println("\tStart condition : " + queryString);
		System.out.println("\tTraversal condition : " + traverseString);
		System.out.println("\tEnd condition : " + endString);
		System.out.println("Your Query Options : ");
		System.out.println("\tTraversal depth : " + option.getTraversalDepth());
		System.out.println("\tEdge limit : " + option.getEdgeLimit());
		System.out.println("\tPrefetch size : " + option.getPrefetchSize());

		System.out.print("\nSearching routes from '" + fromAirport + "' to '" + toAirport + "' with " + maxStops + " stop(s)");
		for (int i=0; i<airlineIDName.length; i++) {
			if (i==0)
				System.out.print(" via airlines '"+airlineIDName[i][1]+"'");
			else
				System.out.print(", '"+airlineIDName[i][1]+"'");
		}
		System.out.println(" ...");

		long startServerProc = System.currentTimeMillis();
		resultSet = conn.executeQuery(queryString, null, traverseString, endString, option);
		long endServerProc = System.currentTimeMillis();
		
        long startClientProc = System.currentTimeMillis();
		// Store routes, airports, airlines in there
		HashMap<String, String> airlineGlossary = new HashMap<String, String>();
		HashMap<String, String> airportGlossary = new HashMap<String, String>();
		int routeCount = 0;
		StringBuilder routeSoFar = new StringBuilder(100);
		if (!noPrint) 
			System.out.println("\nRoutes:");
		
		// Go through resultSet and build routes
		if (resultSet != null) {
			while (resultSet.hasNext()) {
				TGEntity airport0 = resultSet.next();
				String airportCode0 = (airport0).getAttribute("iataCode").getAsString();
				String airportName0 = (airport0).getAttribute("name").getAsString();
				List<Object[]> initRoute = new ArrayList<Object[]>();
				initRoute.add(new Object[] { airport0, new StringBuilder("[" + airportCode0 + "]") });
				Iterator<Object[]> routeIter = initRoute.iterator();
				airportGlossary.put(airportCode0, airportName0);
				List<Object[]> tempRoutes = null;
				
				// Iterate through stops and build routes and store them
				for (int i = 0; i <= maxStops; i++) {
					tempRoutes = new ArrayList<Object[]>();
					while (routeIter.hasNext()) {
						Object[] route = routeIter.next();
						TGNode currentAirport = (TGNode)route[0];
						StringBuilder currentRoute = (StringBuilder)route[1];
						Collection<TGEdge> flights = currentAirport.getEdges(null, TGEdge.Direction.Outbound);  // follow outbound flights only
						Iterator<TGEdge> flightsIter = flights.iterator();
						
						while (flightsIter.hasNext()) {
							TGEdge flight = flightsIter.next();
							TGNode nextAirport = flight.getVertices()[1];
							String nextAirportIataCode = nextAirport.getAttribute("iataCode").getAsString();
							String airlineIataCode = flight.getAttribute("iataCode").getAsString();
							String airlineName = flight.getAttribute("name").getAsString();
							String nextAirportName = nextAirport.getAttribute("name").getAsString();
							if (i==maxStops && !nextAirportIataCode.equals(toAirport)) {
								; // Dead route. No need to store
							}
							else {
								routeSoFar = new StringBuilder(currentRoute + "----(" + airlineIataCode + ")---->["+ nextAirportIataCode + "]");
								airportGlossary.put(nextAirportIataCode, nextAirportName);
								airlineGlossary.put(airlineIataCode, airlineName);
								if (nextAirportIataCode.equals(toAirport)) { // Final route. Print
									routeCount++;
									if (!noPrint)
										System.out.println(routeSoFar);
								}
								else { // Temporary route. Store
									tempRoutes.add(new Object[] { nextAirport, routeSoFar });
								}
							}
						}
					}
					routeIter = tempRoutes.iterator(); // re-init airport list for next stop
				}
			}
			
			// Print airports, airlines
			if (!noPrint) {
				System.out.println("\nAirports:");
				for (Entry<String, String> airport : airportGlossary.entrySet()) {
					System.out.println("[" + airport.getKey() + "] : " + airport.getValue());
				}
				System.out.println("\nAirlines:");
				for (Entry<String, String> airline : airlineGlossary.entrySet()) {
					System.out.println("(" + airline.getKey() + ") : " + airline.getValue());
				}
			}
			System.out.println("\nSUMMARY");
			System.out.println("-------");
			System.out.println("Routes : "+routeCount);
			System.out.println("Airports : "+airportGlossary.size());
			System.out.println("Airlines : "+airlineGlossary.size());
			long endClientProc = System.currentTimeMillis();
			System.out.println("\nServer Processing Time : " + (endServerProc - startServerProc) + " msec");
			System.out.println("Client Processing Time : " + (endClientProc - startClientProc) + " msec");

		}
		else {
			System.out.printf("\nNo route found from '" + fromAirport + "' to '" + toAirport + "'\n");
		}
		System.out.println("\n**************************************************\n");
		
	}
	
	public static void main(String[] args) throws Exception
	{
		OpenFlightsQuery ofq = new OpenFlightsQuery(args);
		try {
			do {
				ofq.readUserRequest();
				ofq.findRoutes();
			} while (ofq.hasMoreRequest());
		} 
		finally {
			ofq.disconnect();
			ofq.reader.close();
		}
	}
}
