################################################################################
#
# NAME          : Prompt features
# DESCRIPTION   : Example for using the prompt functions
#
# AUTHOR    : Rafael Chinchilla
# FILE      : prompt.py
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

# Prompt with default sorting, alpha answer
x = Prompt('Message', ['Option1','Option2'], Type=LIST|ALPHA )
Display("Prompt result: " + str(x))

x = Prompt("Basic prompt")
Display("Prompt result: " + str(x))

x = Prompt("Basic prompt, type OK", OK )
Display("Prompt result: " + str(x))

x = Prompt("Basic prompt, type OK/CANCEL", OK_CANCEL )
Display("Prompt result: " + str(x))

x = Prompt("Basic prompt, type YES", YES )
Display("Prompt result: " + str(x))

x = Prompt("Basic prompt, type NO", NO )
Display("Prompt result: " + str(x))

x = Prompt("Basic prompt, type YES/NO", YES_NO )
Display("Prompt result: " + str(x))

x = Prompt("Basic prompt, type NUM", NUM )
Display("Prompt result: " + str(x))

x = Prompt("Basic prompt, type ALPHA", ALPHA )
Display("Prompt result: " + str(x))

x = Prompt("Basic prompt, sorted OPTIONS", ['1:Option 1','2:Option 2','3:Option 3'], {Type:LIST})
Display("Prompt result: " + str(x))

