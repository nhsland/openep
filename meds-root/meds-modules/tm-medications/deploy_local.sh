#!/bin/bash

# Directory of script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Jar version
VERSION=""

# Restart jar
RESTART=0

# How to use script
USAGE="usage: $(basename "$0") [-h] -v version
  options:
    -v version
"
set -e

function buildVersionDir() {

  local SCRIPT_NAME="openep"
  local JAR_PREFIX="openep"

  # CREATE VERSION DIRECTORY

  VERSION_DIR=${SCRIPT_DIR}/${VERSION}
  echo " ⚙️⚙️ Creating version dir $VERSION_DIR ⚙️⚙️ "
  mkdir -p "$VERSION_DIR"

  # STOP RUNNING JAR IF RESTART IS ENABLED
  # Rewriting jar and calling stop later does not always work. This is why we need to stop it before rewrite

  if [[ ${RESTART} == 1 ]]; then

    STATUS_OUTPUT="$(./${SCRIPT_NAME} status)"
    if [[ ${STATUS_OUTPUT} == *"not running"* ]]; then
      echo " Jar not running - skipping... "
    else
      echo " Stopping jar "
      ./${SCRIPT_NAME} stop
    fi
  fi

  # MOVE JAR

  echo " ⚙️⚙️ Moving jar file ${SCRIPT_DIR}/${JAR_PREFIX}-${VERSION}.jar to ${VERSION_DIR} ⚙️⚙️ "
  mv ${SCRIPT_DIR}/${JAR_PREFIX}-${VERSION}.jar ${VERSION_DIR}

  # CREATE CONF FILE

  CONF_FILE_PATH="$VERSION_DIR/${JAR_PREFIX}-$VERSION.conf"

  if [[ ! -f ${CONF_FILE_PATH} ]]; then

      echo " ⚙️⚙️ File $CONF_FILE_PATH does not exist, creating...⚙️⚙️ "

      echo "LOG_FOLDER=$VERSION_DIR
  PID_FOLDER=$VERSION_DIR
  MODE=service" > ${CONF_FILE_PATH}
  fi

  # COPY FILES FROM LAST VERSION

  if [[ -L ${SCRIPT_DIR}/last ]]; then

    local LAST_VERSION_DIR=$( readlink ${SCRIPT_DIR}/last )

    # COPY APPLICATION PROPERTIES FROM LAST VERSION

    if [[ -f ${LAST_VERSION_DIR}/application.properties ]] && [[ ! -f ${VERSION_DIR}/application.properties ]]
    then
      echo -e "Copy application properties from ${LAST_VERSION_DIR}/application.properties to ${VERSION_DIR}"
      cp ${LAST_VERSION_DIR}/application.properties ${VERSION_DIR}
    fi

    # COPY CONF FOLDER

    if [[ -d ${LAST_VERSION_DIR}/conf ]] && [[ ! -d ${VERSION_DIR}/conf ]]
    then
      cp -rf ${LAST_VERSION_DIR}/conf ${VERSION_DIR}
    fi
  fi
}

function restart() {

  local SCRIPT_NAME="openep"

  if [[ ${RESTART} == 1 ]]; then
  echo " 🏁🏁️ Restarting jar 🏁🏁 "
  ./${SCRIPT_NAME} start ${VERSION}
  fi
}



while getopts hrv: OPTION
do
 case "${OPTION}" in
 h) echo "$USAGE"
    exit
    ;;
 r) echo " 🏁🏁 Restart is enabled 🏁🏁 "
    RESTART=1
    ;;
 v) VERSION=${OPTARG}
    echo " 🎯🎯 Version is set to $VERSION 🎯🎯 "
    ;;
 esac
done

buildVersionDir
restart

echo " 🎉🎉 Done 🎉🎉 "
exit 0
