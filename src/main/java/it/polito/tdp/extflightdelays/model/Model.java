package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	private Graph <Airport, DefaultWeightedEdge> grafo;
	private Map <Integer, Airport> idMap;
	
	public Model() {
		this.idMap= new HashMap <Integer, Airport>();
	}
	public void creaGrafo(double x) {
		this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		ExtFlightDelaysDAO dao= new ExtFlightDelaysDAO();
		dao.loadAllAirports(idMap);
		Graphs.addAllVertices(this.grafo, idMap.values());
		for (Connessione c: dao.getConnessione(idMap,x)) {
				Graphs.addEdge(this.grafo, c.getPartenza(), c.getArrivo(), c.getPeso());
		
	}
}
	public int nVertici() {
		return this.grafo.vertexSet().size();	
	}
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	public String archi() {
		String result="";
		for(DefaultWeightedEdge e: grafo.edgeSet()) {
			result+="("+grafo.getEdgeSource(e).getAirportName()+ ", "+ grafo.getEdgeTarget(e).getAirportName()+", "+ grafo.getEdgeWeight(e)+")\n";
		}
	return result.substring(0, result.length()-1);
		
	}
	
}

