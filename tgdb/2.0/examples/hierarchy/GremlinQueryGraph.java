package hierarchy;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.tibco.tgdb.connection.TGConnection;
import com.tibco.tgdb.model.TGAttribute;
import com.tibco.tgdb.model.TGEntity;
import com.tibco.tgdb.gremlin.GraphTraversalSource;
import common.AbstractGraph;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

/**
 * Gremlin query for members in the House of Bonaparte graph 
 * born between the start and end years
 * and display the member attributes.
 * 
 * Usage : java GremlinQueryGraph -startyear 1900 -endyear 2000
 * 
 */
public class GremlinQueryGraph extends AbstractGraph {

	private int startYear = 1800;
    private int endYear = 1900;
	
    protected GremlinQueryGraph(String[] args) throws Exception
    {
        super(args);
        
        try {
            for (int i=0; i<args.length; i++) {
                if (args[i].equals("-startyear")) {
                    startYear = Integer.parseInt(args[i+1]);
                } else if (args[i].equals("-endyear")) {
                    endYear = Integer.parseInt(args[i+1]);
                }
            }
        } catch (NumberFormatException ex) {
            System.out.printf("Invalid year value specified\n");
            throw new Exception ("Invalid year value specified\n");
        }
        
    }

    GremlinQueryGraph(TGConnection conn) throws Exception
    {
        super(conn);
    }

    void query() throws Exception 
    {
    	System.out.printf("Querying for member born between %d and %d\n", startYear, endYear);
    	GraphTraversalSource g = EmptyGraph.instance().traversal(GraphTraversalSource.class).withRemote(conn); 
    	List<Vertex> memberList = g.V().has("houseMemberType", "yearBorn", P.between(startYear, endYear)).toList(); 

    	if (!memberList.isEmpty()) {
    		for (Object memberObj : memberList) {
    			TGEntity houseMember = (TGEntity)memberObj;
    			SimpleDateFormat simpleFormat = new SimpleDateFormat("dd MMM yyyy");
                System.out.printf("House member '%s' found\n",houseMember.getAttribute("memberName").getAsString());
                for (TGAttribute attr : houseMember.getAttributes()) {
                    if (attr.getValue() == null)
                        System.out.printf("\t%s: %s\n", attr.getAttributeDescriptor().getName(), "");
                    else
                        System.out.printf("\t%s: %s\n", attr.getAttributeDescriptor().getName(), (attr.getValue() instanceof Calendar)?(simpleFormat.format(((Calendar)attr.getValue()).getTime())):attr.getValue());
                }
    		}
    	} else {
    		System.out.printf("Querying for member born between %d and %d not found\n", startYear, endYear);
    	}
    }
	public static void main(String[] args) throws Exception {
		
		GremlinQueryGraph qg = new GremlinQueryGraph(args);
		try {
            qg.query();
        }
        finally {
		    qg.disconnect();
        }
	}
}
