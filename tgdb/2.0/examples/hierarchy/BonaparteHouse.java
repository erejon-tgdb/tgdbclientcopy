package hierarchy;

import com.tibco.tgdb.model.TGEntity;
import common.AbstractGraph;

import java.util.LinkedList;

/**
 * Copyright (c) 2017 TIBCO Software Inc.
 * All rights reserved.
 * <p/>
 * File name : CompleteLifeCycle.${EXT}
 * Created on: 10/23/17
 * Created by: suresh
 * <p/>
 * SVN Id: $Id$
 */


public class BonaparteHouse extends AbstractGraph {

    BuildGraph bg;
    GetAll ga;

    BonaparteHouse(String[] args) throws Exception
    {
        super(args);
        bg = new BuildGraph(this.conn);
        ga = new GetAll(this.conn);

    }

    private void setup() throws Exception
    {
        bg.build();
    }

    private void tearDown() throws Exception
    {
        LinkedList<TGEntity> entityList = new LinkedList<TGEntity>();
        ga.selectAll(entityList);
        for (TGEntity entity : entityList) {
            this.conn.deleteEntity(entity);
        }
        this.conn.commit();
    }

    public static void main(String[] args) throws Exception
    {
        BonaparteHouse bh = new BonaparteHouse(args);
        for (int i=0; i<50; i++)
        {
            System.out.printf("*********** ith Run : %d ********************\n", i);
            bh.setup();
            bh.tearDown();
        }
    }
}
