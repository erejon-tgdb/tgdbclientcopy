==================================================================
 TIBCO(R) Graph Database
 Copyright (c) 20016-2018 TIBCO Software Inc. All rights reserved.
------------------------------------------------------------------ 
 Openflights Routes
==================================================================

This example demonstrates how to import openflights data into the 
database and then find routes between two airports

1) Edit <tgdb_home>/examples/routes/initdb.conf
In [import] section, update 'dir' parameter to point to the import folder : 
<tgdb_home>/examples/routes/import
For instance: dir=/home/tibco/tgdb/1.1/examples/routes/import

2) Init database and import flights data:
<tgdb_home>/bin/tgdb -i- f -c <tgdb_home>/examples/routes/initdb.conf

3) Start database
<tgdb_home>/bin/tgdb -s -c <tgdb_home>/examples/routes/tgdb.conf 

4) Launch an Admin console and connect to the server
<tgdb_home>/bin/tgdb-admin

Execute a 'show types' command and make sure entries are present
admin@localhost:8222> show types
 T  SysId     Name                  #Entries
 N  9225      airlineType           6162
 N  9227      airportType           7184
 E  1041      routeType             65601
3 type(s) returned.

5) Compile: javac -cp <tgdb_home>/lib/tgdb-client.jar <tgdb_home>/examples/routes/OpenFlightsQuery.java

6) Execute OpenFlightsQuery program and follow the instructions on prompt :
java -cp .:<tgdb_home>/lib/tgdb-client.jar routes.OpenFlightsQuery [-noprint]
option 'noprint' just prints the summary instead of all routes. 
Use only if number of routes is high (stops > 2)