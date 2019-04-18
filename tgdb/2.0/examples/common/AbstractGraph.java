package common;
import com.tibco.tgdb.connection.TGConnection;
import com.tibco.tgdb.connection.TGConnectionFactory;
import com.tibco.tgdb.model.*;

/**
 * Copyright (c) 2017 TIBCO Software Inc.
 * All rights reserved.
 * <p/>
 * File name : AbstractExample.${EXT}
 * Created on: 10/23/17
 * Created by: suresh
 * <p/>
 * SVN Id: $Id$
 */


public abstract class AbstractGraph {

    String url = "tcp://127.0.0.1:8222";
    String user = "scott";
    String pwd = "scott";
    protected TGConnection conn = null;
    protected TGGraphObjectFactory gof = null;

    protected AbstractGraph(String[] args) throws Exception
    {
        parseArgs(args);
        connect();
    }

    protected AbstractGraph(TGConnection conn) throws Exception
    {
        this.conn = conn;
        initConnection();
    }

    private void parseArgs(String[] args) throws Exception
    {
        if (args == null) return;
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("-url")) url = args[i+1];
            if (args[i].equals("-user")) user = args[i+1];
            if (args[i].equals("-pwd")) pwd = args[i+1];
        }
        return;
    }

    private void connect() throws Exception
    {
        conn = TGConnectionFactory.getInstance().createConnection(url, user, pwd, null);
        conn.connect();
        initConnection();
    }

    private void initConnection() throws Exception
    {
        gof = conn.getGraphObjectFactory();
        if (gof == null) {
            throw new Exception("Graph object not found");
        }

        conn.getGraphMetadata(true);
    }


    protected void disconnect()
    {
        try {
            if (conn != null)
                conn.disconnect();
            conn = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
