################################################################################
#
# NAME          : Config Demo
# DESCRIPTION   : Example for using the configuration functions
#
# AUTHOR    : Rafael Chinchilla
# FILE      : config_params.py
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
# Date      Revision        Author      Short description
#===============================================================================
# 01/01/2008    0.1         racc            First issue
#
################################################################################

###############################################################################

Display("Obtaining variable value: XXXX")

value = GetResource("XXXX")

Display("XXXX value is " + str(value))

