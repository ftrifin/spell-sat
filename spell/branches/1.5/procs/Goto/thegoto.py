################################################################################
#
# NAME          : Goto Demo
# DESCRIPTION   : Example for using the goto feature
#
# AUTHOR    : Rafael Chinchilla
# FILE      : thegoto.py
# REVISION  : 1.0
# DATE      : 10/10/2006 10:30
#
# SPACECRAFT: A2B
# DATABASE  : N/A
#
# CATEGORY  : Goto
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

count = 0

Step('S1',"Step title")

Display(str(count))

count = count + 1

if count<10: 
    Goto('S1')

Display("FINISH")
