################################################################################
#
# NAME          : Database Demo
# DESCRIPTION   : Example for using the Spacecraft database
#
# AUTHOR    : Rafael Chinchilla
# FILE      : database.py
# DATE      : 3/06/2008 10:30
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

Display( "Number of database keys: " + str(len(SCDB.keys())) )

keys = SCDB.keys()
for key in keys:
    Display( key + " = " + str(SCDB[key]) )
    time.sleep(0.5)

mmd = LoadDictionary( 'mmd://STD_EW001' )

for key in mmd.keys():
    Display( key + " = " + str(mmd[key]) )
    time.sleep(0.5)

