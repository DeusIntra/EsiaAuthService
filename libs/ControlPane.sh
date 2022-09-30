#!/bin/sh
#
# Copyright 2004-2008 Crypto-Pro. All rights reserved.
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
# This script starts CryptoPro JCP v.1.0 control panel
# 
# Usage:
#   ControlPane.sh <path_to_JRE>
#
# Example:
#   ControlPane.sh /usr/java/jdk1.5.0_04/jre
# 

if [ -z "$1" ]; then
  printf "USAGE:\n"
  printf "  ControlPane.sh path_to_JRE\n"
  exit 1
fi

JREDIR=$1
JAVACMD="$JREDIR/bin/java"
[ -x "$JAVACMD" ] || {
  printf "File not found: $JAVACMD\n"
  exit 1
}

"$JAVACMD" ru.CryptoPro.JCP.ControlPane.MainControlPane

