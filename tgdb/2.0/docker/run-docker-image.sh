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
# Arguments:
#    $* = all necessary arguments that need to be passed to 'docker run'
#
# Usage:
#    PRODUCT_DIRECTORY=<......> VERSION=<...> ./run-docker-image.sh
#
# This can be invoked by the build process after building the product deliverables.
#
# Assumptions:
#    <1> The expected folder structure for Docker Build is as follows:
#        {PRODUCT_DIRECTORY}                                  ==> set via environment variable or CURRENT DIRECTORY (default)
#        {PRODUCT_DIRECTORY}/docker                           ==> The folder where all docker image related files are located
#        {PRODUCT_DIRECTORY}/docker/build-docker-image.sh     ==> The file that helps build a custom docker image for TGDB product
#        {PRODUCT_DIRECTORY}/docker/Dockerfile	              ==> set via environment variable or 'Dockerfile' (default)
#        {PRODUCT_DIRECTORY}/docker/initdb.template           ==> The template for server initialization configuration
#        {PRODUCT_DIRECTORY}/docker/prepare-conf.awk	      ==> The AWK script that generates custom TGDB configurations
#        {PRODUCT_DIRECTORY}/docker/run-docker-image.sh       ==> This file
#        {PRODUCT_DIRECTORY}/docker/start-tgdb.bash 	      ==> The start-up script for Docker Container
#        {PRODUCT_DIRECTORY}/docker/tgdb-default.txt          ==> The defaults for all configuration parameters
#        {PRODUCT_DIRECTORY}/docker/tgdb.template             ==> The template for server runtime configuration
#        {PRODUCT_DIRECTORY}/docker/variables.txt             ==> User defined 'custom' values for configuration parameters
#    <2> The end user(s) must be ensuring that Docker environment is setup locally  with appropriate pre-requisites as documented
#    <3> The end user(s) must be ensuring that a locally built TGDB Docker image is already available and accessible

#set -x
set -o pipefail  # trace ERR through pipes
set -o errtrace  # trace ERR through 'time command' and other functions
set -o errexit   ## set -e : exit the script if any statement returns a non-success return value

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
	VERSION=1.1
fi

# Check if User has specified path for user defined configuration files
if [[ ! -z ${USER_CONFIG_DIRECTORY} ]] ; then
   echo "User given path for configuration files : $USER_CONFIG_DIRECTORY"
fi

# Use the supplied registry or a default value
if [ "${DOCKER_REGISTRY}" == "" ]; then
	DOCKER_REGISTRY=localhost:5000
fi

## TODO: Uncomment following 5 blocks after the final decision is made/agreed upon with legal
## Check whether Docker environment/executable is setup and accessible or not
#docker_exec=`docker --version | awk '{print $3}'`
#if [[ ! -z ${docker_exec} ]] ; then
#	echo "Please make sure DOCKER is installed locally"
#	exit 1
#fi
#
## Check whether Docker registry is setup and accessible or not
#registry=`env | grep DOCKER_REGISTRY`
#if [[ ! -z ${registry} ]] ; then
#	echo "Please make sure DOCKER REGISTRY environment variable is set correctly"
#	exit 1
#fi
#
## Use the supplied registry or a default value
#if [ "${DOCKER_REGISTRY}" == "" ]; then
#	DOCKER_REGISTRY=localhost:5000
#fi
#
## Check if the Docker image for TGDB exists in the registry
#tgdb_image=`docker images | grep tgdb-${VERSION} | awk '{ print $3 }'`
#if [[ ! -z ${tgdb_image} ]] ; then
#	echo "Unable to find the TGDB Docker Image ... Please make sure you have access to appropriate TGDB Docker Image"
#	echo "Alternatively please contact TIBCO TGDB Support on how to build it locally"
#	exit 1
#fi
#
#echo "Stopping and removing existing TGDB Docker container, present if any"
#existing_container_id=`docker ps -a | grep tibco/tgdb-${VERSION} | awk '{print $1}'`
#if [[ ! -z ${existing_container_id} ]] ; then
#	echo "Found an existing container for '"tibco/tgdb-${VERSION}"' with ID: "$existing_container_id && \
#	echo "Stopping container ID: "$existing_container_id && docker stop $existing_container_id && \
#	echo "Removing container ID: "$existing_container_id && docker rm $existing_container_id
#fi

# TODO: Pass environment variables via '-e' switch
# TODO: Mount additional data / index directories via '-v' switch
echo "Setting up Instance and Server Configuration"
#docker run -it --rm --name tgdb-2.0 --memory 6g --memory-swap 8g \
docker run -it -d --name tgdb-2.0 -v $USER_CONFIG_DIRECTORY:/mnt/tibco/tgdb/conf --memory 6g --memory-swap 8g \
    -p 8222:8222 -p 8223:8223 \
    -u tibco tibco/tgdb-${VERSION} \
     $*|| exit $?

echo "Docker Container for TGDB Database Server has been configured, is running and listening on port 8222 ..."

