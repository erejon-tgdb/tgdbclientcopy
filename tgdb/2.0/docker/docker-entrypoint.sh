#!/bin/bash
#
# Copyright (c) 2018 TIBCO Software Inc.
# All rights reserved.
#       
# This script is invoked at the start of the Docker Container
#       

function printDockerBanner() {
  echo 
  echo "================================================================"
  echo "  ___ .  __   __  __ (R)   __   __        __          __   __   "
  echo "   |  | |__) /   /  \     / _\` |__)  /\  |__) |__|   |  \ |__)  "
  echo "   |  | |__) \__ \__/     \__| |  \ /¯¯\ |    |  |   |__/ |__)  "
  echo "                                                                "
  echo "                Docker Container Id `hostname`                  "
  echo "================================================================"
  echo
}

set -o pipefail  # trace ERR through pipes
set -o errtrace  # trace ERR through 'time command' and other functions
set -o errexit   ## set -e : exit the script if any statement returns a non-success return value

accept_license=
if [[ $@ =~ .*-Y.* ]] || [[ $@ =~ .*--accept-license.* ]]; then
  accept_license="-Y"
fi
INITDB_CONF_FILE=initdb.conf
TGDB_CONF_FILE=tgdb.conf

if [ ! -z $TGDB_USER_CONF_PATH ]; then
    if [ -f $TGDB_USER_CONF_PATH/initdb.conf ]; then
       cp -f $TGDB_USER_CONF_PATH/initdb.conf $TGDB_HOME/bin/userinitdb.conf
       INITDB_CONF_FILE=userinitdb.conf
    fi
    if [ -f $TGDB_USER_CONF_PATH/initdb.conf ]; then
       cp -f $TGDB_USER_CONF_PATH/tgdb.conf $TGDB_HOME/bin/usertgdb.conf
       TGDB_CONF_FILE=usertgdb.conf
    fi
fi
if [ "$1" = "tgdb" ] || [ "$1" = "-Y" ] || [ "$1" = "--accept-license" ]; then
  if [ ! -d $TGDB_DBPATH/$TGDB_DBNAME ]; then
    echo
    echo "Database $TGDB_DBNAME does not exist - TGDB database initializing..."
    echo
    tgdb -i -f "$accept_license" -c $INITDB_CONF_FILE
    if [ $? != 0 ]; then
      echo
      echo "TGDB database init failed"
      exit $?
    else
      echo 
      echo "TGDB database init successful"
      echo
    fi
  fi
  printDockerBanner
  exec tgdb -s "$accept_license" -c $TGDB_CONF_FILE
else
  exec "$@"
fi
