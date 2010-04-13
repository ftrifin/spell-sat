################################################################################
#
# NAME          : Injection
# DESCRIPTION   : Example for using the TM/EV injection functions
#
# AUTHOR    : Rafael Chinchilla
# FILE      : injection.py
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

Display("Injecting parameters E001 and E002", WARNING )

SetGroundParameter([
                   [ 'E001', 1, True ],
                   [ 'E002', 2 ]
                   ])

Event("Changed ground parameters", WARNING)

Display("Finished")
