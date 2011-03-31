################################################################################
#
# NAME          : GetTM Demo
# DESCRIPTION   : Example about using the GetTM function
#
# AUTHOR    : Rafael Chinchilla
# FILE      : gettm.py
# DATE      : 10/10/2006 10:30
#
# SPACECRAFT: STD
# DATABASE  : N/A
#
# CATEGORY  : Demonstrations
# 
# VALIDATED : Somebody
# APPROVED  : Somebody
#
# HISTORY:
#
# Date          Revision        Author      Short description
#===============================================================================
# 01/01/2008    0.1             racc        First issue
#
################################################################################

Display("Obtaining value for FCOUNTER")
GetTM('FCOUNTER')

Display("Obtaining value for OBTIME")
GetTM('OBTIME')

Display("Obtaining value for PARAM1")
GetTM('PARAM1')

Display("Obtaining value for PARAM2")
GetTM('PARAM2', Timeout=10, OnFailure=ABORT|SKIP)

Display("Obtaining value for PARAM3")
GetTM('PARAM3', Wait=False )

Display("Obtaining value for PARAM4")
GetTM('PARAM4', Wait=True,Timeout=30)

Display("Obtaining value for invalid parameter")
GetTM('INVALID', OnFailure=SKIP)
