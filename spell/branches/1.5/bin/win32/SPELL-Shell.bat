@echo off
REM ###########################################################################
REM # Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
REM #
REM # This file is part of SPELL.
REM #
REM # SPELL is free software: you can redistribute it and/or modify
REM # it under the terms of the GNU General Public License as published by
REM # the Free Software Foundation, either version 3 of the License, or
REM # (at your option) any later version.
REM #
REM # SPELL is distributed in the hope that it will be useful,
REM # but WITHOUT ANY WARRANTY; without even the implied warranty of
REM # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
REM # GNU General Public License for more details.
REM #
REM # You should have received a copy of the GNU General Public License
REM # along with SPELL. If not, see <http://www.gnu.org/licenses/>.
REM #
REM # FILE: Startup script for SPEL Server
REM #
REM # DATE: 24/11/2008
REM #
REM ###############################################################################

setlocal

if "%SPELL_HOME%" == "" goto :FAIL

if "%SPELL_DATA%" == "" set SPELL_DATA=%SPELL_HOME%\data
if "%SPELL_CONFIG%" == "" set SPELL_CONFIG=%SPELL_HOME%\config
if "%SPELL_SYS_DATA%" == "" set SPELL_SYS_DATA=%SPELL_HOME%\data
if "%SPELL_LOG%" == "" set SPELL_LOG=%SPELL_HOME%\log

echo SPELL home  : %SPELL_HOME%
echo SPELL data  : %SPELL_DATA%
echo SPELL config: %SPELL_CONFIG%

set TK_LIBRARY=
set TCL_LIBRARY=
set PYTHONCASEOK=
 
set PYTHONPATH=%SPELL_HOME%
set PYTHONPATH=%PYTHONPATH%;%SPELL_HOME%\server
set PYTHONPATH=%PYTHONPATH%;%SPELL_HOME%\spell

pushd .
cd %SPELL_HOME%
python -i "%SPELL_HOME%\spell\spell\lang\shell.py" -c "%SPELL_HOME%\config\server\server.xml" -n "STD"
popd

endlocal

REM ###########################################################################
goto :EOF

REM Fail if SPELL_HOME not defined ############################################
:FAIL
echo SPELL_HOME environment variable is not defined!
goto :EOF

