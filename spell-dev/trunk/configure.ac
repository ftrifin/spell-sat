################################################################################
#                                                                              # 
#  FILE       : configure.ac                                                   #
#  DESCRIPTION: automake and autoconf configuration file, and source for the   #
#               'configure' script. Describes the structure and organization   #
#               of all makefiles to be generated in order to build SPELL-DEV.  #
#                                                                              #
#  LICENSE ------------------------------------------------------------------  #
#                                                                              #
#  Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.               #
#                                                                              #
#  This file is part of SPELL.                                                 #
#                                                                              #
#  This library is free software: you can redistribute it and/or modify it     #
#  under the terms of the GNU Lesser General Public License as published by    #
#  the Free Software Foundation, either version 3 of the License, or (at your  #
#  option) any later version.                                                  #
#                                                                              #
#  This library is distributed in the hope that it will be useful, but WITHOUT # 
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or       #
#  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public        #
#  License for more details.                                                   #
#                                                                              #
#  You should have received a copy of the GNU Lesser General Public License    #
#  and GNU General Public License (to which the GNU Lesser General Public      #
#  License refers) along with this library. If not, see                        #
#  <http://www.gnu.org/licenses/>.                                             #
#                                                                              #
################################################################################

AC_INIT([SPELL-DEV], [2.0], [])
AM_INIT_AUTOMAKE([1.9 tar-pax -Wall -Werror foreign])
AC_CONFIG_HEADERS([config.h])


#------------------------------------------------------------------------------# 
# Checks for basic standard programs
#------------------------------------------------------------------------------# 
AC_PROG_INSTALL
AC_PROG_MAKE_SET


#------------------------------------------------------------------------------#
# Conditional: check the platform in order to know which scripts to use
#------------------------------------------------------------------------------#
AM_CONDITIONAL(PLATFORM_LINUX, test `uname -s`=="Linux")

#------------------------------------------------------------------------------#
# Conditional: check rsync availability
#------------------------------------------------------------------------------#
AM_CONDITIONAL(HAVE_RSYNC, test `which rsync`) 

#------------------------------------------------------------------------------#
# WITH argument to indicate the path to the Eclipse installation used to 
# compile the RCP applications. Shall be of version 3.5 or higher.
#------------------------------------------------------------------------------#
AC_ARG_WITH([eclipse],
            [AS_HELP_STRING([--with-eclipse=PATH],[eclipse installation used to compile SPELL DEV])],
            [ 
              echo "using eclipse at ${withval}"
              if [[ ! -d ${withval} ]]
              then
                  AC_MSG_ERROR([Unable to find the eclipse installation: ${withval}])
              fi
              with_eclipse_inst=${withval}
            ],
            [])

WITH_ECLIPSE=
AS_IF([ test "x$with_eclipse_inst" != x],
      [AC_SUBST([WITH_ECLIPSE],[${with_eclipse_inst}])],
      [])

AS_IF([ test ""=""] ,
      [
          if [[ -z "$WITH_ECLIPSE" ]]
          then
              AC_MSG_ERROR([Must provide an eclipse installation path (--with-eclipse) to compile the SPELL-DEV])
          fi
      ],
      [])


#------------------------------------------------------------------------------#
# Specification of all the makefiles to be generated.
#------------------------------------------------------------------------------#
AC_CONFIG_FILES([Makefile \
                 rcp/Makefile\
                 doc/Makefile\
				 config/Makefile\
				 scripts/Makefile\
                 scripts/linux/Makefile\
				 scripts/win32/Makefile\
                 data/Makefile\
                 src/Makefile\
               ])
AC_OUTPUT()

