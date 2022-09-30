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
#   setup_gui.sh <path_to_JRE>
#
# Example:
#   setup_gui.sh /usr/java/jre7
#
 
if [ $(id -u) != 0 ]; then
  echo "Root only accessed"
  exit 1   
  # need elevate script privileges
fi

# how to
if [ -z "$1" ]; then
  printf "USAGE:\n"
  printf "  setup_gui.sh path_to_JRE \n"
  printf "  Example:\n"
  printf "    setup_gui.sh /usr/java/jre7 \n"
  exit 1
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

# run installer (GUI)
"$JAVACMD" -Xbootclasspath/a:.:./forms_rt.jar:./JCPinst.jar:./JCPinstGUI.jar:./asn1rt.jar:./ASN1P.jar:./JCP.jar: ru.CryptoPro.Installer.InstallerForm
