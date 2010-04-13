"""
FILE: console.py
PACKAGE: spell.utils.console

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
from log import *
import os

################################################################################
class CommandSet(object):

    console = None
    
    #===========================================================================
    def __init__(self):
        self.console = None
        
    #===========================================================================
    def setConsole(self, console):
        self.console = console        
        
    #===========================================================================
    def doCommand(self, cmd):
        cmdMethod = "command_" + cmd[0]
        if hasattr(self,cmdMethod):
            method = getattr(self,cmdMethod)
            method(cmd)
        else:
            self.console.error("Unrecognised command: " + cmd[0]) 

################################################################################
class Console(object):
    
    working = True
    command = ""
    commandSet = None
    startup = None
    cmdError = False
    inPrompt = False
    promptText = "[SPELL]"
    
    #===========================================================================    
    def __init__(self, commandSet) :
        self.working = True
        self.command = ""
        self.commandSet = commandSet
        self.commandSet.setConsole(self)
        self.startup = None
        self.cmdError = False
        self.inPrompt = False
        LOG.showlog = False
  
    #===========================================================================    
    def setStartupScript(self, ssfile):
        self.startup = ssfile
        if not os.path.exists(ssfile):
            raise BaseException("Cannot find startup script: " + repr(file))
  
    #===========================================================================    
    def start(self):
        self.clear()
        if self.startup is not None:
            self.info("Executing startup script: " + repr(self.startup))
            f = file(self.startup)
            lines = f.readlines()
            for line in lines:
                self.command = line
                self.process()
                if self.cmdError:
                    self.info("Startup script aborted") 
                    break
            if not self.cmdError:
                self.info("Startup script finished")
        while self.working:
            self.prompt()
            self.process()
        self.info("Quiting")
            
    #===========================================================================    
    def process(self):
        if self.command is None or self.command == "": return
        cmd = self.command.lower()
        cmd = cmd.split()
        try:
            self.commandSet.doCommand(cmd)
        except BaseException,ex:
            msg = ex.message
            if msg:
                self.error("Command " + cmd[0] + " failed: " + msg)
            else:
                if type(ex)!=BaseException: 
                    self.error("Command " + cmd[0] + " failed: " + repr(ex))
                else:
                    self.error("Command " + cmd[0] + " failed.")
                    
        self.cmdError = False
            
    #===========================================================================    
    def prompt(self):
        try:
            self.inPrompt = True
            self.command = raw_input( self.promptText + "> " )
            self.inPrompt = False
        except:
            self.command = None

    #===========================================================================    
    def write(self, msg, continuePrompt = True):
        if self.inPrompt: print
        print msg
        if continuePrompt and self.inPrompt: print self.promptText + ">",

    #===========================================================================    
    def info(self, msg, continuePrompt = True ):
        if self.inPrompt: print
        print "[*]",msg
        if continuePrompt and self.inPrompt: print self.promptText + ">",
        
    #===========================================================================    
    def error(self, msg, continuePrompt = True):
        if self.inPrompt: print
        print "[!]",msg
        self.cmdError = True
        if continuePrompt and self.inPrompt: print self.promptText + ">",

    #===========================================================================    
    def clear(self, continuePrompt = True):
        os.system(['clear','cls'][os.name == 'nt'])
        if continuePrompt and self.inPrompt: print self.promptText + ">",
        
    #===========================================================================    
    def input(self, msg):
        try:
            resp = raw_input(msg)
        except:
            resp = ""
        return resp
    
