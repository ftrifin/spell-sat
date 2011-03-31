"""
 PROJECT: SPELL

 Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.

 This file is part of SPELL.

 SPELL is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SPELL is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with SPELL. If not, see <http://www.gnu.org/licenses/>.

"""


import sys,os,inspect

def function_A():
    frame = inspect.currentframe()
    print "    -----------------------------------------------------------------"
    print "    FUNCTION A: print global var"
    print "    -----------------------------------------------------------------"
    print "    FRAME  :",frame
    print "    GLOBALS:",frame.f_globals.keys()
    print "    LOCALS :",frame.f_locals.keys()
    print "   ",A
    print "    -----------------------------------------------------------------"

def function_B():
    global A
    print "    -----------------------------------------------------------------"
    print "    FUNCTION B: cannot modify a global var which is not global in ctx"
    print "    -----------------------------------------------------------------"
    frame = inspect.currentframe()
    print "    FRAME  :",frame
    print "    GLOBALS:",frame.f_globals.keys()
    print "    LOCALS :",frame.f_locals.keys()
    exec "A=2" in frame.f_globals,frame.f_locals
    print "    GLOBALS:",frame.f_globals.keys()
    print "    LOCALS :",frame.f_locals.keys()
    print "   ",A
    assert(A==1)
    print "    -----------------------------------------------------------------"

def function_C():
    print "    -----------------------------------------------------------------"
    print "    FUNCTION C: modify a GLOBAL var if we declare it as global"
    print "    -----------------------------------------------------------------"
    frame = inspect.currentframe()
    print "    FRAME  :",frame
    print "    GLOBALS:",frame.f_globals.keys()
    print "    LOCALS :",frame.f_locals.keys()
    exec "global A;A=2" in frame.f_globals,frame.f_locals
    print "    GLOBALS:",frame.f_globals.keys()
    print "    LOCALS :",frame.f_locals.keys()
    print "   ",A
    assert(A==2)
    print "    -----------------------------------------------------------------"

def function_D():
    print "    -----------------------------------------------------------------"
    print "    FUNCTION D: we can declare a new GLOBAL B variable"
    print "    -----------------------------------------------------------------"
    frame = inspect.currentframe()
    print "    FRAME  :",frame
    print "    GLOBALS:",frame.f_globals.keys()
    print "    LOCALS :",frame.f_locals.keys()
    exec "global B;B=2" in frame.f_globals,frame.f_locals
    print "    GLOBALS:",frame.f_globals.keys()
    print "    LOCALS :",frame.f_locals.keys()
    print "   ",B
    assert(B==2)
    print "    -----------------------------------------------------------------"

def function_E():
    print "    -----------------------------------------------------------------"
    print "    FUNCTION E: we can declare a LOCAL C variable without global"
    print "    -----------------------------------------------------------------"
    frame = inspect.currentframe()
    print "    FRAME  :",frame
    print "    GLOBALS:",frame.f_globals.keys()
    print "    LOCALS :",frame.f_locals.keys()
    exec "C=2" in frame.f_globals,frame.f_locals
    print "    GLOBALS:",frame.f_globals.keys()
    print "    LOCALS :",frame.f_locals.keys()
    print "   ",C
    assert(C==2)
    print "    -----------------------------------------------------------------"

print "---------------------------------------------------------------------"
frame = inspect.currentframe()
print "FRAME  :",frame
print "GLOBALS:",frame.f_globals.keys()
print "LOCALS :",frame.f_locals.keys()
print "---------------------------------------------------------------------"

exec "A=1" in frame.f_globals,frame.f_locals

print "---------------------------------------------------------------------"
frame = inspect.currentframe()
print "FRAME  :",frame
print "GLOBALS:",frame.f_globals.keys()
print "LOCALS :",frame.f_locals.keys()
print "---------------------------------------------------------------------"

function_A()

print "GA:",A

function_B()

print "GA:",A

function_C()

print "GA:",A

function_D()

assert(B==2)
print "GA:",A
print "GB:",B

function_E()

print "---------------------------------------------------------------------"
frame = inspect.currentframe()
print type(frame.f_code.co_names)
print "FRAME  :",frame
print "GLOBALS:",frame.f_globals.keys()
print "LOCALS :",frame.f_locals.keys()
print "---------------------------------------------------------------------"
