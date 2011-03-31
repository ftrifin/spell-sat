################################################################################
#
# NAME          : Commanding features
# DESCRIPTION   : Example for using the commanding functions
#
# AUTHOR    : Rafael Chinchilla
# DATE      : 10/10/2006 10:30
#
# SPACECRAFT: STD
# FILE      : commanding.py
# DATABASE  : N/A
#
# CATEGORY  : Demonstrations
#
# VALIDATED : Somebody
# APPROVED  : Somebody
#
# HISTORY:
#
# Date      Revision        Author      Short description
#===============================================================================
# 01/01/2008    0.1         racc            First issue
#
################################################################################


Display("Begin commanding demo")

TCSAMPLE = BuildTC('TCSAMPLE')

#-------------------------------------------------------------------------------
if Prompt("Inject single command"):
    Send( command = TCSAMPLE, Timeout=10 )
    Display("Command sent")

#-------------------------------------------------------------------------------
if Prompt("Inject commands as group"):
    Send( group = [TCSAMPLE,TCSAMPLE,TCSAMPLE] , Timeout=60 )
    Display("Group sent")

#-------------------------------------------------------------------------------
if Prompt("Group TryAll demo"):
    Send( group = [TCSAMPLE,'xxx',TCSAMPLE] , TryAll=True,Timeout=60 )
    Display("Group sent")

#-------------------------------------------------------------------------------
if Prompt("Inject commands as blocked group"):
    Send( group = [TCSAMPLE,TCSAMPLE,TCSAMPLE] , Timeout=60,Block=True )
    Display("Block sent")

#-------------------------------------------------------------------------------
if Prompt("Inject command sequence"):
    Send( sequence = 'TCSEQUENCE' , Timeout=60 )
    Display("Sequence sent")

#-------------------------------------------------------------------------------
if Prompt("Single command failure"):
    Send( command = "TC_FAIL", Timeout=70,OnFailure=SKIP|REPEAT)

#-------------------------------------------------------------------------------
if Prompt("Send and Verify"):
    tc = BuildTC('TCSAMPLE')
    steps = [[ 'FCOUNTER', lt, 30, {Timeout:500} ]]
    SendAndVerify( command = tc, 
                   config={Timeout:70,OnFailure:SKIP|REPEAT}, 
                   verify = steps )

#-------------------------------------------------------------------------------
if Prompt("Send group and Verify"):
    tc1 = BuildTC('TCSAMPLE')
    tc2 = BuildTC('TC_FAIL')
    tc3 = BuildTC('TCSAMPLE')
    steps = [[ 'FCOUNTER', lt, 30 ]]
    SendAndVerify( group = [tc1,tc2,tc3], 
                   config={Timeout:70,OnFailure:SKIP|REPEAT,TryAll:True}, 
                   verify = steps )

