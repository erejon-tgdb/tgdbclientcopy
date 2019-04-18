package hierarchy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.tibco.tgdb.model.TGEntity;
import com.tibco.tgdb.model.TGKey;
import common.AbstractGraph;


/**
 * For a given member of the House, update the attributes
 * 
 * Usage : java UpdateGraph [options]
 * 
 *  where options are:
 *   -memberName <memberName> Required. Member name - "Napoleon Bonaparte"
 *   -crownName <crownName>   Optional. Name while reigning - "Napoleon XVIII"
 *   -crownTitle <crownTitle> Optional. Title while reigning - "King of USA"    
 *   -houseHead <houseHead>   Optional. Head of the house - true or false
 *   -yearBorn <yearBorn>     Optional. Year of birth - 2004
 *   -yearDied <yearDied>     Optional. Year of death - 2016 or null if still alive
 *   -reignStart <reignStart> Optional. Date reign starts (format dd MMM yyyy) - 20 Jan 2008 or null if never reigned
 *   -reignEnd <reignEnd>     Optional. Date reign ends (format dd MMM yyyy) - 08 Nov 2016 or null if never reigned or still reigning
 *   
 *  For instance to update the house member named "Napoleon Bonaparte" :
 *  java UpdateGraph -memberName "Napoleon Bonaparte" -crownName "Napoleon XVIII" -crownTitle "King of USA" -yearDied null -reignEnd "31 Jan 2016"
 *
 */
public class UpdateGraph extends AbstractGraph
{
	private String memberName = null;
	private String crownName = null;
	private String crownTitle = null;
	private String yearBorn = null;
	private String yearDied = null;
	private String reignStart = null;
	private String reignEnd= null;
	private String houseHead = null;

	UpdateGraph(String[] args) throws Exception
	{
		super(args);
		parseArgs(args);

	}

	private void parseArgs(String[] args) throws Exception {
		for (int i=0; i<args.length; i++) {
			if (args[i].equals("-memberName"))
				memberName = args[i+1];
			else if (args[i].equals("-crownName"))
				crownName = args[i+1];
			else if (args[i].equals("-crownTitle"))
				crownTitle = args[i+1];
			else if (args[i].equals("-yearBorn"))
				yearBorn = args[i+1];
			else if (args[i].equals("-yearDied"))
				yearDied = args[i+1];
			else if (args[i].equals("-houseHead"))
				houseHead = args[i+1];
			else if (args[i].equals("-reignStart")) 
				reignStart = args[i+1];
			else if (args[i].equals("-reignEnd")) 
				reignEnd = args[i+1];
		}
	}

	private void update() throws Exception
	{
		TGKey houseKey = gof.createCompositeKey("houseMemberType");

		houseKey.setAttribute("memberName", memberName);
		System.out.printf("Searching for member '%s'...\n",memberName);
		TGEntity houseMember = conn.getEntity(houseKey, null);
		if (houseMember != null) {
			System.out.printf("House member '%s' found\n",houseMember.getAttribute("memberName").getAsString());
			if (crownName != null)
				houseMember.setAttribute("crownName", crownName);
			if (crownTitle != null)
				houseMember.setAttribute("crownTitle", crownTitle);
			if (houseHead != null)
				houseMember.setAttribute("houseHead", Boolean.parseBoolean(houseHead));
			if (yearBorn != null)
				houseMember.setAttribute("yearBorn", Integer.parseInt(yearBorn));
			if (yearDied != null) {
				if (yearDied.equals("null"))
					houseMember.setAttribute("yearDied", null);
				else
					houseMember.setAttribute("yearDied", Integer.parseInt(yearDied));
			}
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy",Locale.ENGLISH);
			sdf.setLenient(false);
			Calendar calReignStart = Calendar.getInstance();
			if (reignStart != null) {
				if (reignStart.equals("null"))
					houseMember.setAttribute("reignStart", null);
				else {
					try {
						calReignStart.setTime(sdf.parse(reignStart));
						houseMember.setAttribute("reignStart", calReignStart);
					}
					catch (ParseException e) {
						throw new Exception("Member update failed - Wrong parameter: -reignStart format should be \"dd MMM yyyy\"");
					}
				}
			}
			Calendar calReignEnd = Calendar.getInstance();
			if (reignEnd != null) {
				if (reignEnd.equals("null"))
					houseMember.setAttribute("reignEnd", null);
				else {
					try {
						calReignEnd.setTime(sdf.parse(reignEnd));
						houseMember.setAttribute("reignEnd", calReignEnd);
					}
					catch (ParseException e) {
						throw new Exception("Member update failed - Wrong parameter: -reignEnd format should be \"dd MMM yyyy\"");
					}
				}
			}

			conn.updateEntity(houseMember);
			conn.commit();
			System.out.printf("House member '%s' updated successfully\n", memberName);
		}
		else {
			System.out.printf("House member '%s' not found\n", memberName);
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		UpdateGraph ug = new UpdateGraph(args);
		try {
			ug.update();
		}
		finally {
			ug.disconnect();
		}
	}
}
