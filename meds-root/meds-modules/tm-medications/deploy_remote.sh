#!/bin/bash

# Directory of script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Target directory
DIR_TARGET="/home/ispek/openep"

# SSH target
SSH_TARGET=""

# SCP target = (SSH_TARGET:DIR_TARGET)
SCP_TARGET=""

# Restart jar
RESTART=0

# Path to jar file
JAR_FILE=""

# Jar version
JAR_VERSION=""

# How to use script
usage="usage: $(basename "$0") [-h] -v version -t target
  options:
    -r restart    Restart jar once deployed
    -s SSH target, for instance ispek@demo1.thinkmed.marand.si
    -t set TARGET DIR (default = /home/ispek/openep)
    -h [help]
"

set -e

function copyJar() {

  local JAR_PREFIX="openep"
  local JAR_FOLDER="openep-server/target"

  echo " ⚙️⚙️ Copy Jar ⚙️⚙️ "

  # This is a bit ugly... filtering files with size... I'm bad with regex
  JAR_FILE=$(find ${SCRIPT_DIR}/${JAR_FOLDER} -type f -name "$JAR_PREFIX-*.jar" -size +50M)
  echo " 🗂🗂 Jar file -> ${JAR_FILE} 🗂🗂 "

  JAR_VERSION=$(echo ${JAR_FILE} | sed 's/.*target\/'"$JAR_PREFIX"'-\(.*\).jar.*/\1/')
  echo " 🗂🗂 Version -> ${JAR_VERSION} 🗂🗂 "

  scp -o StrictHostKeyChecking=no ${JAR_FILE} ${SCP_TARGET}

}

function copyScripts() {

  local SCRIPT_NAME="openep"

  echo " ⚙️⚙️ Copy deploy scripts ⚙️⚙️ "

  scp -o StrictHostKeyChecking=no ${SCRIPT_DIR}/deploy_local.sh ${SCP_TARGET}
  scp -o StrictHostKeyChecking=no ${SCRIPT_DIR}/${SCRIPT_NAME} ${SCP_TARGET}

}

function copyPropertyTemplates() {

  local PROPERTIES_PATH=${SCRIPT_DIR}/openep-server/src/main/resources

  echo " ⚙️⚙️ Copy property template files ⚙️⚙️ "

  scp -o StrictHostKeyChecking=no ${PROPERTIES_PATH}/application-TC-TEMPLATE.properties ${SCP_TARGET}
  scp -o StrictHostKeyChecking=no ${PROPERTIES_PATH}/application-UK-TEMPLATE.properties ${SCP_TARGET}

}

function callLocalScript() {

echo " 📞📞️ Calling local script 📞📞 "

# Run bash scripts in interactive mode, as newer Linux distributions are setting SSH's PermitUserEnvironment to no
if [[ ${RESTART} == 0 ]]; then
ssh -o StrictHostKeyChecking=no "$SSH_TARGET" "cd ${DIR_TARGET} ; bash -i " << EOF

  ./deploy_local.sh -v ${JAR_VERSION}
EOF
else
ssh -o StrictHostKeyChecking=no "$SSH_TARGET" "cd ${DIR_TARGET} ; bash -i " << EOF

  ./deploy_local.sh -v ${JAR_VERSION} -r
EOF
fi

}



while getopts rht:s: option
do
 case "${option}" in
 h) echo "$usage"
    exit
    ;;
 t) DIR_TARGET=${OPTARG}
    echo " 🎯🎯 Target is [ $DIR_TARGET ] 🎯🎯 "
    ;;
 r) echo " 🏁🏁 Restart is enabled 🏁🏁 "
    RESTART=1
    ;;
 s) SSH_TARGET=${OPTARG}
    echo " 🎯🎯 SSH target is [ $SSH_TARGET ] 🎯🎯 "
    ;;
 esac
done

# VALIDATE ARGUMENTS

if [[ -z ${DIR_TARGET} ]];
then
  echo "target [ -t ] needs to be set"
  exit 1
fi

if [[ -z ${SSH_TARGET} ]];
then
  echo "SSH target [ -s ] needs to be set"
  exit 1
fi

SCP_TARGET=${SSH_TARGET}:${DIR_TARGET}

echo " ⚙️⚙️ Deploying Meds App to [ $SCP_TARGET ] ⚙️⚙️ "

copyJar
copyScripts
copyPropertyTemplates
callLocalScript

echo " 👋👋 Done 👋👋 "
exit 0
