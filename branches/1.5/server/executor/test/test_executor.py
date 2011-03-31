"""
 PROJECT: SPELL

 Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.

 This file is part of SPELL.

 SPELL is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SPELL is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with SPELL. If not, see <http://www.gnu.org/licenses/>.

"""

import sys,os
from server.executor.executor import Executor
from spell.config.reader import Config
from spell.utils.log import *
LOG.showlog = False

if __name__ == "__main__":
    
    configFile = os.getenv("SPELL_HOME") + os.sep + "config" + os.sep + "test_config.xml"
    Config.instance().load(configFile)
    
    EXEC = Executor()
    
    procId = sys.argv[1]
    print "USING PROCEDURE ",procId
    ctxName = "STD"
    
    EXEC.setup( procId, ctxName, contextPort = 0, useContext = False, timeID = "TEST")


