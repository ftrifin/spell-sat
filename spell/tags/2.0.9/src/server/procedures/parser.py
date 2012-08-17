###################################################################################
## MODULE     : procedures.parser
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Procedure header parser
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## This component is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## This software is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with SPELL. If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################

#*******************************************************************************
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from procedure import PROC_LOADABLE
from properties import *

#*******************************************************************************
# System Imports
#*******************************************************************************
import os,string
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
class ParseError(BaseException): pass
class NotLoadable(BaseException): pass
 
#*******************************************************************************
# Module globals
#*******************************************************************************

NOTLOAD = 'NOT-LOADABLE'

################################################################################
class HeaderParser(object):
    """
    DESCRIPTION:
        
    """
    
    # Holds the read properties
    __properties = {}
    
    # Holds the file name
    __fileName = None
    
    
    #==========================================================================
    def __init__(self, k1L, kNL ):
        LOG("Created")
        self.__fileName = None
        self.__properties = {}
        # Get the property keys passed from the proc manager
        # Force all keys to be in lowercase
        if (len(k1L)>0): PROPERTIES_1L = map(string.lower,k1L)
        if (len(kNL)>0): PROPERTIES_NL = map(string.lower,kNL)

    #==========================================================================
    def properties(self):
        return self.__properties

    #==========================================================================
    def parseFile(self, filename, ignoreLoadable = False ):
        if not os.path.exists(filename):
            raise ParseError("Cannot find file: " + repr(filename))

        # Clear the properties
        self.__properties = {}
        # Default
        self.__properties[PROC_LOADABLE] = True

        # Open the file
        f = file(filename)
        
        # This flag is true when we are inside the header area
        inHeader = False
        # This flag is true when we are building a multi-line property
        multiline = None
        for line in f.readlines():
            # Discard null/empty lines
            if len(line)>0:
                # We are interested in comments only
                isComment = (line[0]=='#')
                if isComment and not inHeader:
                    # This marks the 'entering header' condition
                    # Previous comments are ignored
                    if self.__isLimitLine(line):
                        inHeader = True
                elif isComment and inHeader:
                    # This marks the 'exiting header' condition
                    if self.__isLimitLine(line):
                        if len(self.__properties)==0:
                            raise ParseError("No properties found")
                        else:
                            return
                    else: # Rest of the comment lines fall here
                        # Search for a keyword in the line

                        # Single-line properties first
                        try:
                            key = self.__getKeyword(line, ignoreLoadable, multiLine = False  )
                            if key:
                                multiline = None
                                idx = line.find(":")
                                property = line[idx+1:].strip()
                                self.__properties[key] = property
                                # The key is processed, continue to next line
                                continue
                        except NotLoadable:
                            #LOG("Not loadable file: " + filename)
                            self.__properties[PROC_LOADABLE] = False
                            continue
                        # Now multi-line properties
                        key = self.__getKeyword(line, ignoreLoadable, multiLine = True )
                        if key:
                            # If this is the first line of the property
                            if not self.__properties.has_key(key):
                                idx = line.find(":")
                                pvalue = line[idx+1:].strip()
                                multiline = key
                                self.__properties[key] = pvalue
                            continue
                        # Continue with a multi-line property
                        elif multiline is not None:
                            pvalue = self.__properties.get(multiline)
                            # Remove the spaces and comment symbols
                            cvalue = line.strip('#').strip()
                            if len(pvalue.strip())>0:
                                pvalue = pvalue + '\n' + cvalue
                            else:
                                pvalue = pvalue + cvalue
                            self.__properties[multiline] = pvalue
                            
        if len(self.__properties)==0:
            raise ParseError("No properties found")

    #==========================================================================
    def __isLimitLine(self, line):
        l = len(line.strip('#\n\r'))
        return ((l==0) and len(line.strip('\n\r'))>1)

    #==========================================================================
    def __getKeyword(self, line, ignoreLoadable, multiLine = False ):
        # If we do not ignore the NOT-LOADABLE keyword, those procedures
        # containing that keyword in the header will not be shown to 
        # the user.
        if not ignoreLoadable:
            if line.find(NOTLOAD)>0: raise NotLoadable("This file cannot be loaded")
        # Use one key dictionary or the other depending on wether the property
        # is multiline or not
        if multiLine:
            dict = PROPERTIES_NL
        else:
            dict = PROPERTIES_1L
        for k in dict:
            # Convert the line for the comparison
            line = string.lower(line)
            if line.find(k)==2:
                return k
        return None

###############################################################################
if __name__ == "__main__":
    
    p = HeaderParser();
    p.parseFile("header.txt")   
    
    for k in p.properties().keys():
        print "----------------------------"
        print k
        print p.properties().get(k)
        print "----------------------------"
