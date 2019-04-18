Readme Contents:
================================================================================
Product Name	 : TIBCO(R) Graph Database - Docker Edition
Release Version	 : 2.0.0
Release Date	 : November 2018

================================================================================
Introduction 

TIBCO(R) Graph Database is a digital database whose organization is based on 
the graph/network model of the data. A graph is a collection of nodes and edges. 
The nodes of a graph may represent entity, and an edge represents a 
relationship between the nodes. TIBCO(R) Graph Database provides ACID 
transaction for the graph data model. It can persist graph data on a storage 
medium (magnetic or solid state) that has random access capabilities.

This README provides instructions and information on how to embed and use 
TIBCO(R) Graph Database within a Docker container. Please note that by embedding
database within a Docker container, it becomes a single tenant database instance.

================================================================================
Supported Platforms

    CentOS
       6.x, 7.x 64-bit on x86-64
    Red Hat Enterprise Linux Server
       6.x, 7.x 64-bit on x86-64

================================================================================
Supported Third-party Software (Dependency)

    A light weight Linux Kernel - Alpha Linux 3.10+
    Stable (no RC) build of GNU C library - Preferably 2.17+
    Linux utilities

================================================================================
Minimum Hardware Requirements

    Free Disk Space:   10 MB
    Runtime Memory:    4 GB
    File System Types: We support device-mapper for xfs, ext4, btrfs on the Host
                       O/S that provides guarantee of sync-writes. Please note
                       that Docker Overlay File System is NOT supported.

================================================================================
Documentation

Documentation on https://docs.tibco.com is updated more frequently than any 
documentation that might be included with the product. To ensure that you are
accessing the latest available help topics, please visit TIBCO Documentation site.

================================================================================
Frequently Asked Questions (FAQ)

(Q#1) How To Build a TGDB Docker Image? (An optional feature)
(A#1) This feature is available ONLY for Linux 64bit platform.

(1) Once the product is installed e.g. /home/tibco/tgdb/<VERSION>, please make 
    sure you have ‘docker’ subfolder in ‘/home/tibco/tgdb/<VERSION>’
(2) Please make sure you have ‘docker’ environment installed and usable.
    - Please refer ‘https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-centos-7’ or equivalent.
(3) Navigate to ‘docker’ folder e.g. ‘cd /home/tibco/tgdb/<VERSION>/docker’
(4) Edit ‘variables.txt’ file to set custom values for your TGDB Server 
    Initialization and runtime configuration parameters.
(5) Execute the script ‘build-docker-image.sh’ as shown below 
    (for product version 1.1 and installation directory as /home/tibco/tgdb)

    Prompt#> clear && docker images && \
        > PRODUCT_DIRECTORY=/home/tibco/tgdb VERSION=1.1 ./build-docker-image.sh && \
        > docker images

(6) On successful completion of command above, you should see a docker image 
    ‘tgdb-1.1’ (or ‘tgdb-<VERSION>’ in the list as follows:

    tibco/tgdb-1.1      latest      35a8cff9a2f0      1 seconds ago      48.3MB


(Q#2) How do I execute TGDB Docker Image in a Docker Container?
(A#2) This assumes that you already have successfully built a TGDB Docker Image in 
    your local environment.

(1) Navigate to ‘docker’ folder e.g. ‘cd /home/tibco/tgdb/<VERSION>/docker’
(2) Execute the script ‘run-docker-image.sh’ as shown below 
    (for product version 1.1 and installation directory as /home/tibco/tgdb,
    -Y option indicates that you accept the end user license agreement)

    Prompt#> clear && docker ps -a && \
        > PRODUCT_DIRECTORY=/home/tibco/tgdb VERSION=1.1 ./run-docker-image.sh -Y && \
        > sleep 30 && docker ps -a

    If you want to specify external configuration files,
    set USER_CONFIG_DIRECTORY=<path to directory containing external conf files> while running the script.
    ( Make sure to set dbPath = /mnt/tibco/tgdb/data in the respective conf files as per variables.txt)


    For example:

    prompt#> PRODUCT_DIRECTORY=/home/tibco/tgdb VERSION=2.0 USER_CONFIG_DIRECTORY=/home/centos/user_conf ./run-docker-image.sh –Y

    This will pick up configuration files(initdb.conf,tgdb.conf) from  directory user_conf

(3) On successful completion of command above, you should see a docker 
    container ‘tgdb-1.1’ (or tgdb-<VERSION>’ in the list as follows:

    CONTAINER ID        IMAGE            COMMAND                  CREATED                  
        STATUS                    PORTS                              NAMES
    35a8cff9a2f0        tibco/tgdb-1.1   "./start-tgdb.bash"      45 minutes ago      
        Up 45 minutes             0.0.0.0:8222-8223->8222-8223/tcp   tgdb-1.1


(Q#3) How do I make sure whether the Docker container and TGDB Server inside is 
    working properly or not?
(A#3) This assumes that you already have successfully built a TGDB Docker Image 
    and attempted to create a Docker Container for it. There are few simple 
    commands as listed below that you can use to check/verify whether the Docker 
    Container is functioning properly or not.

(1) Get the appropriate <CONTAINER ID> via ‘docker ps -a’ - as shown above
(2) Use ‘docker exec <CONTAINER ID> ls -ltR /home/tibco/tgdb/data’ 
    to check whether a database subfolder and associated files are present or not
(3) Use ‘docker exec <CONTAINER ID> cat /home/tibco/tgdb/bin/log/tgdb_<YOUR_DBNAME>.log’ 
    to see if there are any errors during creation of your custom DB
(4) Use ‘docker exec <CONTAINER ID> cat /home/tibco/tgdb/bin/log/tgdb_initdb.log’ 
    to see if there are any errors during TGDB server initialisation based 
    on your custom configuration
(5) Use ‘docker exec <CONTAINER ID> cat /home/tibco/tgdb/bin/initdb.conf’ 
    to verify whether your custom configuration values are being used or not
(6) Use ‘docker exec <CONTAINER ID> cat /home/tibco/tgdb/bin/tgdb.conf’ 
    to verify whether your custom runtime configuration values are being used or not


(Q#4) What all components other than TGDB product - Docker Image is dependent on?
(A#4) All such components are documented in Docker build file ‘Dockerfile’ 
    located in ‘/home/tibco/tgdb/<VERSION>/docker’ and they are:

(1) A light weight Linux Kernel - Alpha Linux 3.10+
(2) Stable (no RC) build of GNU C library - Preferably 2.17+ 
    - Change to latest version of ‘glibc-<???>-x86_64.pkg.tar.xz’ if needed
(3) Linux utilities such as bash, wget, and which to ease the dev efforts

================================================================================
TIBCO Product Support

Check the TIBCO Product Support web site at https://support.tibco.com for 
product information that was not available at release time. Entry to this site 
requires a username and password. If you do not have one, you can request one. 
You must have a valid maintenance or support contract to use this site.

================================================================================
Copyright

Copyright (c) 2016-2018, TIBCO Software Inc. All rights reserved.
TIBCO Software Inc. Confidential Information
