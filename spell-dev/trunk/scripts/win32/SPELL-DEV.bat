@echo off
REM ##############################################################################
REM # COPYRIGHT SES-ASTRA (c) 2007,2008
REM # 
REM # AUTHOR: R. Chinchilla, F. Bouleau
REM # DATE: 08/02/2008
REM ###############################################################################

setlocal

if "%SPELL_DATA%" == "" set SPELL_DATA=%SPELL_HOME%\data
if "%SPELL_COTS%" == "" set SPELL_COTS=%SPELL_HOME%\cots
if "%SPELL_CONFIG%" == "" set SPELL_CONFIG=%SPELL_HOME%\config
if "%SPELL_SYS_DATA%" == "" set SPELL_SYS_DATA=%SPELL_HOME%\data
if "%SPELL_LOG%" == "" set SPELL_LOG=%SPELL_HOME%\log

echo SPELL home  : %SPELL_HOME%
echo SPELL data  : %SPELL_DATA%
echo SPELL config: %SPELL_CONFIG%
echo SPELL cots  : %SPELL_COTS%

set JRE="%SPELL_COTS%\JRE_1.6.0_07\"
PATH "%JRE%\bin";"%JRE%\lib";%PATH%

echo "SPELL home is " %SPELL_HOME%

set RCP_HOME=%SPELL_HOME%\rcp\plugins
set LAUNCHER=%RCP_HOME%\equinox-launcher.jar
set DEV_HOME=%SPELL_HOME%\spel-dev
set DEV_RCP_CONFIG=%DEV_HOME%\configuration\win32

REM # java process set errorlevel to 1 when asking for restarting the application (switch workspace or restart)
:runit
java -jar "%LAUNCHER%" -showsplash "%DEV_HOME%\splash.bmp" -configuration "%DEV_RCP_CONFIG%" -os win32 -arch x86 -ws win32 -clean -consoleLog -vm "%JRE%\bin\client\jvm.dll" 
if errorlevel 1 goto :runit

endlocal

