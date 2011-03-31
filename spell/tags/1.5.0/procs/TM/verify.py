################################################################################
#
# NAME          : Verify Demo
# DESCRIPTION   : Example for using the Verify function
#
# AUTHOR    : Rafael Chinchilla
# FILE      : verify.py
# REVISION  : 1.0
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

basicStep1 = [ 'FCOUNTER', lt, 30 ]
basicStep2 = [ 'PARAM1', gt, 0 ]
basicStep3 = [ 'PARAM2', gt, 0 ]
basicStep4 = [ 'PARAM3', lt,0 ]
basicStep5 = [ 'PARAM4', gt, 0, {ValueFormat:RAW} ]

complexStep = ['PARAM4', eq, 'ON', {Timeout:5,OnFailure:ABORT|SKIP} ]

simpleSet = [ basicStep1 ]
complexSet = [ basicStep1, complexStep, basicStep2 ]
complexSet2 = [ basicStep1, 
                basicStep2,
                basicStep3,
                basicStep4,
                basicStep5 ]

Verify( simpleSet )

Verify( complexSet, Timeout=500, OnFailure=SKIP )

Verify( complexSet2 )

Display('Finished')

