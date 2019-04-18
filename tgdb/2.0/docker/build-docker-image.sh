#!/bin/bash
#
# Copyright (c) 2018 TIBCO Software Inc.
# All rights reserved.
#
# File name : build-docker-image.sh
# Created on: Oct 09, 2017
# Created by: achavan
#
# This is the build script to build a Docker Image for TIBCO Graph Database
#
# Usage:
#    PRODUCT_DIRECTORY=<......> VERSION=<...> ./build-docker-image.sh
#       
# This can be invoked by the build process after building the product deliverables.
#
# Assumptions:
#    <1> The expected folder structure for Docker Build is as follows:
#        {PRODUCT_DIRECTORY}                                  ==> set via environment variable or CURRENT DIRECTORY (default)
#        {PRODUCT_DIRECTORY}/docker                           ==> The folder where all docker image related files are located
#        {PRODUCT_DIRECTORY}/docker/build-docker-image.sh     ==> This file
#        {PRODUCT_DIRECTORY}/docker/Dockerfile	              ==> set via environment variable or 'Dockerfile' (default)
#        {PRODUCT_DIRECTORY}/docker/initdb.template           ==> The template for server initialization configuration
#        {PRODUCT_DIRECTORY}/docker/prepare-conf.awk	      ==> The AWK script that generates custom TGDB configurations
#        {PRODUCT_DIRECTORY}/docker/run-docker-image.sh       ==> The file to start docker containeronce docker image is built
#        {PRODUCT_DIRECTORY}/docker/start-tgdb.bash 	      ==> The start-up script for Docker Container
#        {PRODUCT_DIRECTORY}/docker/tgdb-default.txt          ==> The defaults for all configuration parameters
#        {PRODUCT_DIRECTORY}/docker/tgdb.template             ==> The template for server runtime configuration
#        {PRODUCT_DIRECTORY}/docker/variables.txt             ==> User defined 'custom' values for configuration parameters
#    <2> The end user(s) must be ensuring that Docker environment is setup locally  with appropriate pre-requisites as documented
#    <3> The format of 'variables.txt' (along with a sample copy) will be available for reference in TGDB documentation
        
set -x
set -o pipefail  # trace ERR through pipes
set -o errtrace  # trace ERR through 'time command' and other functions
set -o errexit   ## set -e : exit the script if any statement returns a non-success return value
        
# Make sure the custom values for parameters are set by customer(s)/end-user(s)
if [[ ! -f "variables.txt" ]] ; then
	echo "Please specify all custom values for configuration parameters in 'variables.txt' file"
	echo "The format of 'variables.txt' file is <NAME>=<VALUE>."
	echo "The possible configuration parameters are:"
	echo "<1> TGDB_DBNAME, <2> TGDB_DBPATH, <3> TGDB_SEGSIZE, <4> TGDB_DATASEGCNT"
	echo "<5> TGDB_DATAPGSZ, <6> TGDB_TXTPGSZ, <7> TGDB_BLOBPGSZ, <8> TGDB_IDXSGCNT"
	echo "<9> TGDB_IDXPGSZ, <10> TGDB_RUNTIME_MEMORY, <11> TGDB_SHARED_MEMORY"
	echo "<12> TGDB_TXNPROCESSOR_CNT, <13> TGDB_QRYPROCESSOR_CNT, <14> TGDB_QRYPROCESSOR_QDEPTH"
	echo "<15> TGDB_TXNPROCESSOR_QDEPTH, <16> TGDB_REDO_QDEPTH, <17> TGDB_LOG_LEVEL"
	echo "<18> TGDB_LOG_LOCATION, <19> TGDB_LOG_FILE_SIZE, <20> TGDB_LOG_FILE_COUNT"
	exit 1
fi

# Check what the product directory is set to
if [ -z "${PRODUCT_DIRECTORY}" ]; then
    echo "Please set PRODUCT_DIRECTORY environment variable to point where the TGDB product is installed"
    exit 1
fi

# Set the default location for the PRODUCT_DIRECTORY environment variable if it is set to EMPTY
if [ "${PRODUCT_DIRECTORY}" == "" ]; then
	PRODUCT_DIRECTORY=.
fi

# Set the default for VERSION 
if [ "${VERSION}" == "" ]; then
	VERSION=2.0
fi

# Use the supplied registry or a default value
if [ "${DOCKER_REGISTRY}" == "" ]; then
	DOCKER_REGISTRY=localhost:5000
fi

# Set the default for DOCKERFILE name 
if [ "${DOCKERFILE}" == "" ]; then
	DOCKERFILE=Dockerfile
fi

# Ensure you are in accurate 'docker' folder with all the scripts available
DOCKER_DIRECTORY=${PRODUCT_DIRECTORY}/${VERSION}/docker
cp -r ${DOCKER_DIRECTORY} ${PRODUCT_DIRECTORY}/${VERSION}/docker-tmp
if [ ! -f "${DOCKER_DIRECTORY}/prepare-conf.awk" ]; then
    echo "Please make sure you are in appropriate ('docker') sub-folder that has utilities to build Docker image"
    exit 1
fi

# Generate configuration files based on the user input values
cd ${PRODUCT_DIRECTORY}/${VERSION}/docker-tmp && \
echo "Generating TGDB Server Initialization configuration ... initdb.conf" && \
awk -f prepare-conf.awk initdb.template tgdb-default.txt variables.txt && \
echo "Generating TGDB Server Runtime Configuration ... tgdb.conf" && \
awk -f prepare-conf.awk tgdb.template tgdb-default.txt variables.txt && \
echo "" && \
echo "Moving custom configration files - initdb.conf, tgdb.conf - to appropriate product 'bin' folder" && \
echo ""

# Grab few properties from variables.txt that Docker will make use of
TGDB_DBNAME=`cat variables.txt | grep "TGDB_DBNAME" | cut -d'=' -f2`
TGDB_DBPATH=`cat variables.txt | grep "TGDB_DBPATH" | cut -d'=' -f2`
TGDB_LOGPATH=`cat variables.txt | grep "TGDB_LOG_LOCATION" | cut -d'=' -f2`

# Execute all the following commands in product directory
cd ${PRODUCT_DIRECTORY}/${VERSION} && \
# Build TGDB docker image
docker build --rm -f ${DOCKER_DIRECTORY}/${DOCKERFILE} -t tibco/tgdb-${VERSION} --build-arg TGDB_VERSION="$VERSION" --build-arg TGDB_DBNAME="$TGDB_DBNAME" --build-arg TGDB_DBPATH="$TGDB_DBPATH" --build-arg TGDB_LOGPATH="$TGDB_LOGPATH" .

rm -rf ${PRODUCT_DIRECTORY}/${VERSION}/docker-tmp
echo "Successfully build TGDB Docker Image 'tibco/tgdb-${VERSION}'"

