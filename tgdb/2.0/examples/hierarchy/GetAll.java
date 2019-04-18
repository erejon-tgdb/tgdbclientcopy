/**
 * Copyright (c) 2017 TIBCO Software Inc.
 * All rights reserved.
 * <p/>
 * File name : GetAll.java
 * Created on: 8/11/17
 * Created by: suresh
 * <p/>
 * SVN Id: $Id$
 */
package hierarchy;



import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.tibco.tgdb.connection.TGConnection;
import com.tibco.tgdb.model.TGAttribute;
import com.tibco.tgdb.model.TGEdge;
import com.tibco.tgdb.model.TGEntity;
import com.tibco.tgdb.model.TGKey;
import com.tibco.tgdb.model.TGNode;
import com.tibco.tgdb.query.TGQueryOption;
import com.tibco.tgdb.query.TGResultSet;
import common.AbstractGraph;

/**
 * Search for a member in the House of Bonaparte graph
 * and display the member attributes and children
 *
 * Usage : java GetAll [-url url] [-user user] [-pwd pwd]
 *
 */
public class GetAll extends AbstractGraph {


    public GetAll(String args[]) throws Exception
    {
        super(args);
    };

    public GetAll(TGConnection conn) throws Exception
    {
        super(conn);
    }

    protected int selectAll(List<TGEntity> entityList) throws Exception
    {
        String queryString = "@nodetype = 'houseMemberType';";
        TGQueryOption opt = TGQueryOption.createQueryOption();
        opt.setTraversalDepth(1);
        opt.setEdgeLimit(1);
        opt.setPrefetchSize(1000);
        TGResultSet resultSet = conn.executeQuery(queryString, opt);
        while (resultSet.hasNext())
        {
            entityList.add(resultSet.next());
        }
        return resultSet.count();
    }

    protected void displayAll() throws Exception
    {
        int count = 0;
        int nf = 0;

        TGKey houseKey = gof.createCompositeKey("houseMemberType");
        for (Object[] member : BuildGraph.houseMemberData) {
            String memberName = member[0].toString();
            houseKey.setAttribute("memberName", memberName);
            System.out.printf("Searching for member '%s'...\n", memberName);
            TGEntity houseMember = conn.getEntity(houseKey, null);
            if (houseMember != null) {
                ++count;
                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd MMM yyyy");
                System.out.printf("House member '%s' found\n", houseMember.getAttribute("memberName").getAsString());
                for (TGAttribute attr : houseMember.getAttributes()) {
                    if (attr.getValue() == null)
                        System.out.printf("\t%s: %s\n", attr.getAttributeDescriptor().getName(), "");
                    else
                        System.out.printf("\t%s: %s\n", attr.getAttributeDescriptor().getName(), (attr.getValue() instanceof Calendar) ? (simpleFormat.format(((Calendar) attr.getValue()).getTime())) : attr.getValue());
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
                System.out.printf("House member '%s' not found\n", memberName);
                ++nf;
            }
        }
        System.out.printf("Number of entities found :%d\n", count);
        System.out.printf("length of HouseMemberData:%d\n", BuildGraph.houseMemberData.length);
        System.out.printf("Number of entities Not found :%d\n", nf);
        return;
    }


    public static void main(String[] args) throws Exception
    {
        GetAll getAll = new GetAll(args);
        try {
            getAll.displayAll();
        }
        finally {
            getAll.disconnect();
        }
    }
}
