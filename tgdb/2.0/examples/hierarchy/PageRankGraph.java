package hierarchy;

import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

import com.tibco.tgdb.connection.TGConnection;
import com.tibco.tgdb.gremlin.GraphTraversalSource;
import common.AbstractGraph;

/**
 * PageRank for members in the House of Bonaparte graph 
 * This requires a GPU-enabled TGDB server.
 * See Release Notes for configuration.
 * 
 * Usage : java PageRankGraph
 * 
 */
public class PageRankGraph extends AbstractGraph {
	
    protected PageRankGraph(String[] args) throws Exception
    {
        super(args); 
    }

    PageRankGraph(TGConnection conn) throws Exception
    {
        super(conn);
    }

    void pageRank() throws Exception
    {
    	GraphTraversalSource g = EmptyGraph.instance().traversal(GraphTraversalSource.class).withRemote(conn);
    	List<Map<String,Object>> valueList = g.V().pageRank().valueMap("@pagerank", "memberName").toList();	
    	 
       	for (Object value : valueList) {
    		System.out.println(value);
    	}
    	
    }
	public static void main(String[] args) throws Exception {
		
		PageRankGraph qg = new PageRankGraph(args);
		try {
            qg.pageRank();
        }
        finally {
		    qg.disconnect();
        }
	}
}
