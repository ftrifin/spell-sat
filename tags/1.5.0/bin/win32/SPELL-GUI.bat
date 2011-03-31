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

set RCP_HOME=%SPELL_HOME%\rcp\plugins
set LAUNCHER=%RCP_HOME%\equinox-launcher.jar
set GUI_RCP_CONFIG=%SPELL_HOME%\spel-gui\bin\configuration\win32

REM check the java version
set TEMPFILE=%TEMP%\%TIME::=_%
java -version 2>%TEMPFILE%
for /f "eol=; tokens=*" %%i in (%TEMPFILE%) do call :extractver %%i
del %TEMPFILE%
if not "%javaver%" == "1.6" goto :FAIL2

if "%1" == "" start java -jar "%LAUNCHER%" -showsplash "%SPELL_HOME%\spel-gui\bin\splash.bmp" -configuration "%GUI_RCP_CONFIG%" -os win32 -arch x86 -ws win32 -clean -data @none -consoleLog -config "%SPELL_CONFIG%\gui\config.xml"
if not "%1" == "" start java -jar "%LAUNCHER%" -showsplash "%SPELL_HOME%\spel-gui\bin\splash.bmp" -configuration "%GUI_RCP_CONFIG%" -os win32 -arch x86 -ws win32 -clean -data @none -consoleLog %* 

endlocal

REM ###########################################################################
goto :EOF

REM Fail if SPELL_HOME not defined ############################################
:FAIL
echo SPELL_HOME environment variable is not defined!
goto :EOF

REM Fail if java version is not correct #######################################
:FAIL2
echo Java Runtime Environment version 1.6.X is required!
goto :EOF

REM Extract the Java version ##################################################
:extractver
set mystring=%*
set preamble=%mystring:~0,12%
if not "%preamble%" == "java version" goto :EOF
set javaver=%mystring:~14,-6%
goto :EOF

