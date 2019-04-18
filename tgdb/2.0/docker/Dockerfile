#
# Copyright (c) 2018 TIBCO Software Inc.
# All rights reserved.
#
# This is the Docker configuration file for TIBCO Graph Database
#
# Requirements:
# (1) Light Weight Kernel - preferably Alpine Linux v3.10+
# (2) Total Docker Image Size should not exceed 40MB - (Alpine + GLibC image = 12MB + TGDB = 10MB)
# (3) Stable (no RC) version of GNU C Library - preferably v2.17+
# (4) Complete Control and Knowledge of all the packages getting installed (BOM)
#
# Execute commands to set up the payload for the Docker Image:
# (1) Packages being added in APK Format: bash, bash-completion
# (2) Create a user "tibco/tibco" and execute the commands in Docker container as "tibco"
#
# Assumptions:
# (1) This is a single tenant database instance a.k.a. One customer account - One database
# (2) The underlying file system should be supported by TGDB and provides guarantee of sync-writes.

# The parent of the Docker Image
FROM frolvlad/alpine-glibc

ARG TGDB_DBNAME=my-inventory
ARG TGDB_DBPATH=/mnt/tibco/tgdb/data
ARG TGDB_LOGPATH=/mnt/tibco/tgdb/logs
ARG TGDB_VERSION=2.0
ARG TGDB_USER_CONF_PATH=/mnt/tibco/tgdb/conf

# Author / Maintainer of this file
MAINTAINER TIBCO GraphDB Engineering Team

# Metadata about the Docker Image viewed by "docker inspect"
LABEL { Vendor="TIBCO Software Inc." Copyright="2015-2018 All rights reserved." }

# Environment Variables to be set in the Docker Image viewed by "docker inspect"
ENV TGDB_HOME=/home/tibco/tgdb \
    TGDB_DBNAME=$TGDB_DBNAME \
    TGDB_DBPATH=$TGDB_DBPATH \
    TGDB_LOGPATH=$TGDB_LOGPATH \
    TGDB_VERSION=$TGDB_VERSION \
    TGDB_USER_CONF_PATH=$TGDB_USER_CONF_PATH \
    USER=tibco \
    DOCKER_REGISTRY_URL="" \
    LOG_LEVEL="debug" \
    DOCKER_HOST="unix:///var/run/docker.sock"
ENV PATH=$TGDB_HOME/bin:$PATH

# Copy local files and directories to destination folder(s) using absolute paths in the Docker Image.
COPY ["license.txt", "$TGDB_HOME/"]
COPY ["bin/tgdb*", "$TGDB_HOME/bin/"]
COPY ["examples/", "$TGDB_HOME/examples/"]
COPY ["lib/", "$TGDB_HOME/lib/"]
COPY ["docker/docker-entrypoint.sh", "$TGDB_HOME/bin/"]
COPY ["docker-tmp/initdb.conf","docker-tmp/tgdb.conf", "$TGDB_HOME/bin/"]

# Prepare for TGDB
RUN { \
    apk add --no-cache --upgrade bash bash-completion && \
    adduser -h /home/tibco -D tibco tibco && \
    mkdir -p $TGDB_HOME $TGDB_HOME/bin $TGDB_HOME/examples $TGDB_HOME/lib && \
    mkdir -p $TGDB_DBPATH $TGDB_LOGPATH && \
    mkdir -p $TGDB_USER_CONF_PATH && \
    ulimit -c unlimited ; \
    chown -R tibco:tibco $TGDB_HOME && chmod -R 755 $TGDB_HOME/ ; \
    chown -R tibco:tibco $TGDB_DBPATH && chmod -R 755 $TGDB_DBPATH/ ; \
    chown -R tibco:tibco $TGDB_LOGPATH && chmod -R 755 $TGDB_LOGPATH/ ; \
    chown -R tibco:tibco $TGDB_USER_CONF_PATH && chmod -R 755 $TGDB_USER_CONF_PATH/ ; \
}

# The base working directory for RUN, CMD, ADD, COPY, ENTRYPOINT commands
WORKDIR $TGDB_HOME/bin

# Ports that need to be exposed for external communication alternatively to be set via "docker -p"
EXPOSE 8222 8223

# Set the data / index mounts as obtained from external inputs
# Externally mount the local directories - especially for data and index files - on to the Docker Image
VOLUME $TGDB_DBPATH
VOLUME $TGDB_LOGPATH
VOLUME $TGDB_USER_CONF_PATH

# Set the default user while running Docker Image as Container
USER tibco

# Configure the container that will execute as an executable
ENTRYPOINT ["docker-entrypoint.sh"]

# By default start TGDB server
CMD ["tgdb"]