################################################################################
#
# NAME          : WaitFor Demo
# DESCRIPTION   : Example for WaitFor function
#
# AUTHOR    : Rafael Chinchilla
# FILE      : waitfor.py
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

WaitFor( ['FCOUNTER', eq, 1], {Delay:15} )
Display('Frame counter is 1')

WaitFor( 30, Interval=[10,5] )

WaitFor( Delay=5 )

WaitFor( Until=NOW+"+00:00:10" )


