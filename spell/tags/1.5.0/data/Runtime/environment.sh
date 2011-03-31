#!/bin/bash
###############################################################################
# COPYRIGHT SES-ASTRA (c) 2007,2008
# 
# FILE: Environment setup for SPELL
#
# This environment setup is prepared for PRODUCTION installations. No
# subversion or other COTS are loaded but Python.
#
# AUTHOR: R. Chinchilla, F. Bouleau
# DATE: 27-Nov-2008
###############################################################################

if [[ -z "$SPELL_COTS" ]]
then
    export SPELL_COTS=$SPELL_HOME/cots
fi

# COTS
[[ ! -d $SPELL_COTS ]] && export SPELL_COTS=${SPELL_HOME}/cots-linux

# Runtime data
RUNTIME=$SPELL_HOME/data/Runtime
# The libraries we have to add
PYLIBS="plat-linux2 lib-tk lib-dynload site-packages"

#===============================================================================
# Setup main environment variables
#===============================================================================

# Find the python COTS
PY_COTS=`find $SPELL_COTS -maxdepth 1 -iname "python*" | xargs basename`
PYHOME=$SPELL_COTS/$PY_COTS
echo "[*] Using python: $PYHOME"

# Export the environment variables for COTS
export PATH=$SPELL_HOME/bin:$PYHOME/bin:$PATH
export LD_LIBRARY_PATH=$PYHOME/lib:$PYHOME/lib/python2.5/:$PYHOME/lib/python2.5/lib-dynload/:$LD_LIBRARY_PATH

#===============================================================================
# Extra COTS libraries
#===============================================================================
LIB_DIRS=`find ${SPELL_COTS} -type d -name "lib"`
for LDIR in $LIB_DIRS
do
    echo "[*] Append library: $LDIR"
    export LD_LIBRARY_PATH=${LDIR}:${LD_LIBRARY_PATH}
done


#===============================================================================
# Update python path
#===============================================================================
echo "[*] Setting up python path"

export PYTHON=${PYHOME}/bin/python

if [ ! -e $PYTHON ] 
then
    export PYTHON=`which python`
    export PYTHONPATH=${PYTHONPATH}:${SPELL_HOME}/spell/:${SPELL_HOME}/server:${SPELL_HOME}
else
    # The base python path
    PYLBASE=`find $PYHOME/lib -maxdepth 1 -mindepth 1 -type d -name "*python*"`
    PYPKG=${PYHOME}/lib:$PYLBASE
    # Now append the libraries
    for PLIB in $PYLIBS
    do
        PYPKG=${PYPKG}:${PYLBASE}/$PLIB
    done
    export PYTHONPATH=${PYPKG}:${SPELL_HOME}/spell/:${SPELL_HOME}/server:${SPELL_HOME}
fi

echo "[*] Python path: $PYTHONPATH"

#===============================================================================
# Other environment variables
#===============================================================================

export TK_LIBRARY=""
export TCL_LIBRARY=""
export PYTHONCASEOK=""

echo "[*] Environment ready"
