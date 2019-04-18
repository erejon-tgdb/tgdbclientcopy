package hierarchy;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.tibco.tgdb.connection.TGConnection;
import com.tibco.tgdb.model.TGAttribute;
import com.tibco.tgdb.model.TGEdge;
import com.tibco.tgdb.model.TGEntity;
import com.tibco.tgdb.model.TGKey;
import com.tibco.tgdb.model.TGNode;
import common.AbstractGraph;

/**
 * Search for a member in the House of Bonaparte graph 
 * and display the member attributes and children
 * 
 * Usage : java SearchGraph -memberName "Carlo Bonaparte" 
 * 
 */
public class SearchGraph extends AbstractGraph
{
	private String memberName = "Napoleon Bonaparte";

	SearchGraph(String[] args) throws Exception
	{
		super(args);
		for (int i=0; i<args.length; i++) {
			if (args[i].equals("-memberName"))
				memberName = args[i+1];
		}
	}

	SearchGraph(TGConnection conn) throws Exception
	{
		super(conn);
	}

	private void search() throws Exception
	{
		TGKey houseKey = gof.createCompositeKey("houseMemberType");

		houseKey.setAttribute("memberName", memberName);
		System.out.printf("Searching for member '%s'...\n",memberName);
		TGEntity houseMember = conn.getEntity(houseKey, null);
		if (houseMember != null) {
			SimpleDateFormat simpleFormat = new SimpleDateFormat("dd MMM yyyy");
			System.out.printf("House member '%s' found\n",houseMember.getAttribute("memberName").getAsString());
			for (TGAttribute attr : houseMember.getAttributes()) {
				if (attr.getValue() == null)
					System.out.printf("\t%s: %s\n", attr.getAttributeDescriptor().getName(), "");
				else
					System.out.printf("\t%s: %s\n", attr.getAttributeDescriptor().getName(), (attr.getValue() instanceof Calendar)?(simpleFormat.format(((Calendar)attr.getValue()).getTime())):attr.getValue());
			}
			for (TGEdge relation : ((TGNode)houseMember).getEdges(TGEdge.DirectionType.Directed)) { // Directed == child
				TGNode[] vertices = relation.getVertices();
				TGNode fromMember = vertices[0];
				TGNode toMember = vertices[1];
				if (fromMember == houseMember) {
					System.out.printf("\tchild: %s\n", toMember.getAttribute("memberName").getAsString());
				}
			}
		} else {
			System.out.printf("House member '%s' not found", memberName);
		}
	}

	public static void main(String[] args) throws Exception {

		SearchGraph sg = new SearchGraph(args);
		try {
			sg.search();
		}
		finally {
			sg.disconnect();
		}

	}
}
