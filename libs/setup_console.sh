#!/bin/sh
#
# Copyright 2004-2015 Crypto-Pro. All rights reserved.
#
# This is proprietary information of
# Crypto-Pro company.
#
# Any part of this file can not be copied, 
# corrected, translated into other languages,
# localized or modified by any means,
# compiled, transferred over a network from or to
# any computer system without preliminary
# agreement with Crypto-Pro company
#
# ---------------------------------------------------
#
# This script installs CryptoPro JCP v.2.0
# 
# Usage:
#   setup_console.sh <path_to_JRE>
#
# Example:
#   setup_console.sh /usr/java/jre7
#

# how to
if [ -z "$1" ]; then
  printf "USAGE:\n"
  printf "  1. Interractive mode.\n"
  printf "    setup_console.sh path_to_JRE \n"
  printf "      Example:\n"
  printf "        setup_console.sh /usr/java/jre7 \n"
  printf "  2. Force (silent) mode.\n"
  printf "    setup_console.sh path_to_JRE -force [-ru | -en] [-install | -uninstall] [-jre <value>] [-jcp | -jcryptop | -cpssl | -cades | -ocf | -j6cf | -jcsp] [-strict_mode] [-default_provider [0|1]] [-serial_jcp <value> -serial_cpssl <value> -serial_jcsp <value>] [-rmsetting] \n"
  printf "      Examples:\n"
  printf "        1) install JCP (variant 2), cpSSL and CAdES into /usr/java/jre7 with serial number:\n"
  printf "          setup_console.bat /usr/java/jre7 -force -ru -install -jre /usr/java/jre7 -jcp -jcryptop -cpssl -cades -serial_jcp XXXXX-XXXXX-XXXXX-XXXXX-XXXXX \n"
  printf "        2) uninstall JCP from default JRE (current JRE) /usr/java/jre7 and remove all saved settings:\n"
  printf "          setup_console.bat /usr/java/jre7 -force -en -uninstall -jcp -rmsetting \n"
  printf "        3) install Java CSP into default JRE (current JRE) with serial number:\n"
  printf "          setup_console.bat /usr/java/jre7 -force -en -install -jcsp -serial_jcsp XXXXX-XXXXX-XXXXX-XXXXX-XXXXX \n"
  exit 1
fi

if [ $(id -u) != 0 ]; then
  echo "Root only accessed"
  exit 1   
  # need elevate script privileges
fi

printf "Params: $*\n"
DISTPATH="`pwd`" 

# check installer
[ -r "$DISTPATH/JCPinstGUI.jar" ] && [ -r "$DISTPATH/JCP.jar" ] && [ -r "$DISTPATH/JCPinst.jar" ] || {
  printf "Script must be in the installer folder\n"
  exit 1
}

# check JVM
JREDIR=$1
JAVACMD="$JREDIR/bin/java"
[ -x "$JAVACMD" ] || {
  printf "File not found: $JAVACMD\n"
  exit 1
}

"$JAVACMD" -version

# run installer (console)
"$JAVACMD" -Xbootclasspath/a:.:./forms_rt.jar:./JCPinst.jar:./JCPinstGUI.jar:./asn1rt.jar:./ASN1P.jar:./JCP.jar: ru.CryptoPro.Installer.InstallerConsole $*
