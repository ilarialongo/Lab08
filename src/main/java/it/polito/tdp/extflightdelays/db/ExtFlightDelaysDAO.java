package it.polito.tdp.extflightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.extflightdelays.model.Airline;
import it.polito.tdp.extflightdelays.model.Airport;
import it.polito.tdp.extflightdelays.model.Connessione;
import it.polito.tdp.extflightdelays.model.Flight;

public class ExtFlightDelaysDAO {

	public List<Airline> loadAllAirlines() {
		String sql = "SELECT * from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Airline(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRLINE")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public void loadAllAirports(Map <Integer, Airport> idMap) {
		String sql = "SELECT * FROM airports";
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				if (!idMap.containsKey(rs.getInt("ID"))) {
				Airport airport = new Airport(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRPORT"),
						rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"), rs.getDouble("LATITUDE"),
						rs.getDouble("LONGITUDE"), rs.getDouble("TIMEZONE_OFFSET"));
				idMap.put(airport.getId(), airport);
				}
			}
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Flight> loadAllFlights() {
		String sql = "SELECT * FROM flights";
		List<Flight> result = new LinkedList<Flight>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Flight flight = new Flight(rs.getInt("ID"), rs.getInt("AIRLINE_ID"), rs.getInt("FLIGHT_NUMBER"),
						rs.getString("TAIL_NUMBER"), rs.getInt("ORIGIN_AIRPORT_ID"),
						rs.getInt("DESTINATION_AIRPORT_ID"),
						rs.getTimestamp("SCHEDULED_DEPARTURE_DATE").toLocalDateTime(), rs.getDouble("DEPARTURE_DELAY"),
						rs.getDouble("ELAPSED_TIME"), rs.getInt("DISTANCE"),
						rs.getTimestamp("ARRIVAL_DATE").toLocalDateTime(), rs.getDouble("ARRIVAL_DELAY"));
				result.add(flight);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	/* public double calcolaDistanzaMedia (int aPartenza, int aDestinazione) {
		String sql= "SELECT AVG (DISTANCE) AS media FROM flights AS F WHERE (f.ORIGIN_AIRPORT_ID=? AND f.DESTINATION_AIRPORT_ID=?) OR (f.ORIGIN_AIRPORT_ID=? AND f.DESTINATION_AIRPORT_ID=?)";
		double media=0.0;
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, aPartenza);
			st.setInt(2, aDestinazione);
			st.setInt(3, aDestinazione);
			st.setInt(4, aPartenza);
			ResultSet rs = st.executeQuery();
			if(rs.next()) {
				media= rs.getDouble("media");
			}
			conn.close();
			return media;
			
		}
			catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Errore connessione al database");
				throw new RuntimeException("Error Connection Database");
			}
	}
	
public List<Connessione> getConnessione(Map <Integer, Airport> idMap, double x) {
	String sql= "SELECT DISTINCT ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID FROM flights";
	List <Connessione> connessioni= new ArrayList<>();
	try {
		Connection conn = ConnectDB.getConnection();
		PreparedStatement st = conn.prepareStatement(sql);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
			boolean flag=true;
			Connessione c= new Connessione(idMap.get(rs.getInt("ORIGIN_AIRPORT_ID")), idMap.get(rs.getInt("DESTINATION_AIRPORT_ID")), this.calcolaDistanzaMedia(rs.getInt("ORIGIN_AIRPORT_ID"), rs.getInt("DESTINATION_AIRPORT_ID")));
			if (connessioni.isEmpty()) {
				connessioni.add(c);
			}
			else {
				for (Connessione cTemp: connessioni) {
					if ((cTemp.getPartenza().getId()==c.getArrivo().getId()) && (cTemp.getArrivo().getId()==c.getPartenza().getId())) {
						flag=false;
					}
				}
				
				if (flag==true) {
					if (c.getPeso()>=x) {
					connessioni.add(c);
					}
				}
			}
		}
		conn.close();
		return connessioni;	
	}
	catch (SQLException e) {
		e.printStackTrace();
		System.out.println("Errore connessione al database");
		throw new RuntimeException("Error Connection Database");
	}
	
}*/

public List<Connessione> getConnessione(Map <Integer, Airport> idMap, double x) {
	String sql= "SELECT DISTINCT ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID, AVG(DISTANCE) AS media FROM flights AS f GROUP BY ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID";
	List <Connessione> connessioni= new ArrayList<>();
	try {
		Connection conn = ConnectDB.getConnection();
		PreparedStatement st = conn.prepareStatement(sql);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
			boolean flag=true;
			Connessione c= new Connessione(idMap.get(rs.getInt("ORIGIN_AIRPORT_ID")), idMap.get(rs.getInt("DESTINATION_AIRPORT_ID")), rs.getDouble("media"));
			if (connessioni.isEmpty()) {
				connessioni.add(c);
			}
			else {
				for (Connessione cTemp: connessioni) {
					if ((cTemp.getPartenza().getId()==c.getArrivo().getId()) && (cTemp.getArrivo().getId()==c.getPartenza().getId())) {
						flag=false;
						int i= connessioni.indexOf(cTemp);
						connessioni.get(i).setPeso((c.getPeso()+cTemp.getPeso())/2);
						
					}
				}
				
				if (flag==true) {
					if (c.getPeso()>=x) {
					connessioni.add(c);
					}
				}
			}
		}
		conn.close();
		return connessioni;	
	}
	catch (SQLException e) {
		e.printStackTrace();
		System.out.println("Errore connessione al database");
		throw new RuntimeException("Error Connection Database");
	}
	
	
}

	
}
