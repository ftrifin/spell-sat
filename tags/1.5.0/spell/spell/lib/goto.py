###############################################################################

"""
PACKAGE 
    spell.lib.goto 
FILE
    goto.py
    
DESCRIPTION
    Goto implementation
    
PROJECT: SPELL

 Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.

 This file is part of SPELL.

 This library is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, either
 version 3 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License and GNU General Public License (to which the GNU Lesser
 General Public License refers) along with this library.
 If not, see <http://www.gnu.org/licenses/>.
 
"""

###############################################################################
import token,tokenize
from spell.utils.log import *
from spell.lib.registry import *

__instance__ = None

__all__ = ['Step','Goto','label','GotoMgr']

###############################################################################
class DummyLabelClass(object):
    """
    DESCRIPTION:
        Description here
    """
    
    #==========================================================================
    def __getattr__(self, name):
        return None    

###############################################################################
class GotoStatementClass(object):
    """
    DESCRIPTION:
        Description here
    """

    __labels = {}
    __current = None
    
    #==========================================================================
    @staticmethod
    def call(id):
        return GotoStatementClass.__current.__getattr__(id)

    #==========================================================================
    @staticmethod
    def setCurrent(inst):
        GotoStatementClass.__current = inst

    #==========================================================================
    def __getattr__(self, id):
        #REGISTRY['CIF'].write("Jumping to Step %s" % id)
        return GotoManagerClass.goto(self.__labels[id])

    #==========================================================================
    def set(self, id, srow):
        self.__labels[id] = srow
        
    #==========================================================================
    def get(self, srow):
        return self.__labels.keys()[self.__labels.values().index(srow)]
        
    #==========================================================================
    @staticmethod
    def getCurrent():
        return GotoStatementClass.__current
    
    #==========================================================================
    def getStepLine(self, label):
        return GotoStatementClass.__labels[label]

    #==========================================================================
    def getStepTitle(self, label):
        return LabelClass.titles[label]

###############################################################################
class LabelClass(object):
    """
    DESCRIPTION:
        Description here
    """
    titles = {}
    __current = None
    
    #==========================================================================
    def __init__(self, filename = None):
        pass
    
    #==========================================================================
    @staticmethod
    def setCurrent(inst):
        LabelClass.__current = inst

    #==========================================================================
    @staticmethod
    def getCurrent():
        return LabelClass.__current

    #==========================================================================
    def set(self, id, title):
        self.titles[id] = title

    #==========================================================================
    @staticmethod
    def call(id, title):
        LabelClass.__current.set(id, title)
        # For the step-by-step feature
        # Tell the executor we are going to a step, so that it can pause if
        # sbs mode is enabled
        REGISTRY['EXEC'].stage(id,'"' + title + '"')

###############################################################################
class MissingLabelError(Exception):
    """
    DESCRIPTION:
        Raised when a goto uses a missing label
    """
    pass

###############################################################################
class GotoClass(object):
    """
    DESCRIPTION:
        Description here
    """
    __goto_lbl = None
    __goto_var = None
    __labels = None
    __filename = None
    __gotoStatement = None
    __stepStatement = None
    srow = None

    #==========================================================================
    def __init__(self, filename):
        self.srow = None
        if self.__filename == filename:
            return
        LOG("Parsing " + repr(filename))
        self.__filename = filename
        self.__gotoStatement = GotoStatementClass()
        self.__stepStatement = LabelClass()
        self.parseModule()

    #==========================================================================
    def getGoto(self):
        return self.__gotoStatement
    
    #==========================================================================
    def getInitLine(self):
        if "INIT" in self.__labels:
            return self.__labels["INIT"]
        return None

    #==========================================================================
    def getStep(self):
        return self.__stepStatement
    
    #==========================================================================
    def parseModule(self):
        """
        DESCRIPTION:
            Nothing
        ARGUMENTS:
            Nothing
        RETURNS:
            Nothing
        RAISES:
            Nothing
        """
        self.__labels = {}
        self.__goto_lbl = {}
        self.__goto_var = {}
        lblIter = 0

        goto_lbl = [(token.NAME, 'goto'), (token.OP, '.')]
        goto_var = [(token.NAME, 'goto'), (token.OP, '*')]
        label = [(token.NAME, 'step'), (token.OP, '.')]
        label2 = [(token.NAME, 'Step'), (token.OP, '(')]
    
        window = [(None, ''), (None, '')]
        for tokenType, tokenString, (srow, scol), (erow, ecol), line \
                in tokenize.generate_tokens(open(self.__filename, 'r').readline):
            if lblIter == 1:
                self.__stepStatement.set(myTokenString, tokenString)
                lblIter = 0
            elif lblIter > 1:
                lblIter = lblIter - 1
            elif window == goto_lbl:
                self.__goto_lbl[srow] = tokenString
            elif window == goto_var:
                self.__goto_var[srow] = tokenString
            elif window == label:
                self.__labels[tokenString] = srow
                myTokenString = tokenString
                self.__gotoStatement.set(myTokenString, srow)
                lblIter = 2
            elif window == label2:
                myTokenString = tokenString.strip("'\"")
                self.__labels[myTokenString] = srow
                self.__gotoStatement.set(myTokenString, srow)
                lblIter = 2
            window = [window[1], (tokenType, tokenString)]

    #==========================================================================
    @staticmethod
    def trace_dispatch(frame, event, arg):
        """
        DESCRIPTION:
            Nothing
        ARGUMENTS:
            Nothing
        RETURNS:
            Nothing
        RAISES:
            Nothing
        """
        if frame is not None:
            mygoto = REGISTRY['GOTOMGR'].create( frame.f_code.co_filename )
            return mygoto.trace(frame, event, arg)
        
        return None

    #==========================================================================
    @staticmethod
    def reset(filename):
        REGISTRY['GOTOMGR'].reset(filename)
    
    #==========================================================================
    def trace(self, frame, event, arg):
        """
        DESCRIPTION:
            Nothing
        ARGUMENTS:
            Nothing
        RETURNS:
            Nothing
        RAISES:
            Nothing
        """
        if self.srow is not None:
            LOG("GOING TO LINE " + str(self.srow))
            frame.f_lineno = self.srow
            self.srow = None
                
        ## OBSOLETE: old-style gotos
        #
        #linelbl = ''

        #if self.__goto_lbl.has_key(frame.f_lineno):
        #    linelbl = self.__goto_lbl[frame.f_lineno]
        #elif self.__goto_var.has_key(frame.f_lineno):
        #    linelbl = eval(self.__goto_var[frame.f_lineno], frame.f_globals, frame.f_locals)
    
        #if linelbl <> '':
        #    if not self.__labels.has_key(linelbl):
        #        raise MissingLabelError, "Missing label: %s" % linelbl
        #    frame.f_lineno = self.__labels[linelbl]
    
        return self.trace

###############################################################################
class GotoManagerClass(object):

    """
    DESCRIPTION:
        Manages the goto class instances. An instance per python file must be
        created.
    """

    # Stores the goto class instances for each file    
    __gotoInstances = {}
    __current = None
    __currentFileName = None
    
    #==========================================================================
    def __init__(self):
        REGISTRY['GOTOMGR'] = self
        self.__gotoInstances = {}

    #==========================================================================
    @staticmethod
    def instance():
        global __instance__
        if __instance__ is None:
            __instance__ = GotoManagerClass()
        return __instance__

    #==========================================================================
    def reset(self, filename = None):
        if filename is None:
            LOG("Clearing all goto instances")
            self.__gotoInstances.clear()
            GotoManagerClass.__currentFileName = None
            GotoManagerClass.__current = None
        else:
            LOG("Reseting goto for " + filename)
            if self.__gotoInstances.has_key(filename):
                del self.__gotoInstances[filename]
                LOG("Instance removed")
        
    #==========================================================================
    @staticmethod
    def goto(row):
        # For the step-by-step feature
        # Tell the executor we are going to a step, so that it can pause if
        # sbs mode is enabled
        titles = LabelClass.getCurrent().titles
        id     = GotoStatementClass.getCurrent().get(row)
        REGISTRY['EXEC'].stage(id,titles.get(id))
        GotoManagerClass.__current.srow = row
    
    #==========================================================================
    def getStepLine(self, label):
        return GotoStatementClass.getCurrent().getStepLine(label)
        
    #==========================================================================
    def getStepTitle(self, label):
        return GotoStatementClass.getCurrent().getStepTitle(label)
        
    #==========================================================================
    def create(self, filename):
        global Goto, Step, label

        if GotoManagerClass.__currentFileName == filename:
            return GotoManagerClass.__current
        
        inst = self.__gotoInstances.get(filename)
         
        if not inst:
            LOG("Creating new goto instance for " + filename)
            inst = GotoClass( filename )
            self.__gotoInstances[filename] = inst
        
        GotoStatementClass.setCurrent(inst.getGoto())
        LabelClass.setCurrent(inst.getStep())
        GotoManagerClass.__current = inst
        GotoManagerClass.__currentFileName = filename
        
        return inst

###############################################################################
Step = LabelClass.call
Goto = GotoStatementClass.call
label = Step
###############################################################################
GotoMgr = GotoManagerClass
