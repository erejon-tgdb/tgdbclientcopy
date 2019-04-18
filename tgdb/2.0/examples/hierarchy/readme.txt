==================================================================
 TIBCO(R) Graph Database
 Copyright (c) 20016-2018 TIBCO Software Inc. All rights reserved.
------------------------------------------------------------------ 
 House of Bonaparte
==================================================================

This example demonstrates how to build a graph with the API
and perform search and queries on objects in the graph.
Refer to hierarchy.pdf for full graph description.

1) Initialize database:
<tgdb_home>/bin/tgdb -i- f -c <tgdb_home>/examples/hierarchy/initdb.conf

2) Start database
<tgdb_home>/bin/tgdb -s -c <tgdb_home>/examples/hierarchy/tgdb.conf 

3) Compile all the java classes. For instance
javac -cp <tgdb_home>/lib/tgdb-client.jar <tgdb_home>/examples/hierarchy/BuildGraph.java
javac -cp <tgdb_home>/lib/tgdb-client.jar:<tgdb_home>/lib/tgdb-gremlin-mincore.jar <tgdb_home>/examples/hierarchy/GremlinQueryGraph.java

4) Build graph 
java -cp <tgdb_home>/lib/tgdb-client.jar hierarchy.BuildGraph 

5) Search graph
java -cp <tgdb_home>/lib/tgdb-client.jar hierarchy.SearchGraph -memberName "Napoleon Bonaparte"

6) Query graph. Query for members born between the start and end years. For instance
java -cp <tgdb_home>/lib/tgdb-client.jar hierarchy.QueryGraph -startyear 1900 -endyear 2000

7) Gremlin Query. Same as #6 but Gremlin-style query
java -cp <tgdb_home>/lib/tgdb-client.jar:<tgdb_home>/lib/tgdb-gremlin-mincore.jar hierarchy.GremlinQueryGraph -startyear 1900 -endyear 2000

8) PageRank analysis. Requires NVIDIA CUDA's graph analytics package nvGraph. See Release Notes for config
java -cp <tgdb_home>/lib/tgdb-client.jar:<tgdb_home>/lib/tgdb-gremlin-mincore.jar hierarchy.PageRankGraph

9) Update Graph. Update a given member of the house. For instance
java -cp <tgdb_home>/lib/tgdb-client.jar hierarchy.UpdateGraph -memberName "Napoleon Bonaparte" -crownTitle "King of USA" -yearDied null -reignEnd "31 Jan 2016" 