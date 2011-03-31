'''
Refactoring tests.
'''

import os
import sys
#make it as if we were executing from the directory above this one (so that we can use pycompletionserver
#without the need for it being in the pythonpath)
sys.argv[0] = os.path.dirname(sys.argv[0]) 
#twice the dirname to get the previous level from this file.
sys.path.insert(1, os.path.join(  os.path.dirname( sys.argv[0] )) )


import unittest
try:
    import refactoring
except ImportError:
    pass #That's ok -- not available in jython

#===============================================================================
# delete
#===============================================================================
def delete(filename):
    '''Removes filename, or does nothing if the file doesn't exist.
    '''
    try:
        os.remove(filename)
    except OSError:
        pass

#================================================================================
# createFile       
#================================================================================
def createFile(filename, contents='', flag='w'):
    '''Creates the given filename with the given contents.
    '''
    f = open(filename, flag)
    f.write(contents)
    f.close()

FILE = 'temporary_file.py'

def getInitialFile():
    s = \
'''
class C:
    def a(self):
        a = 2
        b = 3
        c = a+b #this should be refactored.
        return c
c = C()
'''
    return s


def getRenameRefactored():
    s = \
'''
class G:
    def a(self):
        a = 2
        b = 3
        c = a+b #this should be refactored.
        return c
c = G()
'''
    return s
    
class Test(unittest.TestCase):

        
    def getRefactoredFile(self):
        s = \
'''
class C:
    def a(self):
        a = 2
        b = 3
        c = self.plusMet(a, b) #this should be refactored.
        return c

    def plusMet(self, a, b):
        return a+b
c = C()
'''
        return s

    def setUp(self):
        unittest.TestCase.setUp(self)
        createFile(FILE, getInitialFile())

    def tearDown(self):
        unittest.TestCase.tearDown(self)
        delete(FILE)
    
    def testExtractMethod(self):
        r = refactoring.Refactoring()
        s = r.extractMethod(FILE, 5+1, 12, 5+1, 12+3, 'plusMet')

        f = file(FILE, 'r')
        contents = f.read()
        f.close()

        self.assertEquals(self.getRefactoredFile(), contents)

    def testRename(self):
        r = refactoring.Refactoring()
        s = r.renameByCoordinates(FILE, 1+1, 6, 'G')

        f = file(FILE, 'r')
        contents = f.read()
        f.close()

        self.assertEquals(getRenameRefactored(), contents)

#    def testRename2(self):
#        r = refactoring.Refactoring()
#        s = r.renameByCoordinates(FILE, 7+1, 4, 'G')
#
#        f = file(FILE, 'r')
#        contents = f.read()
#        f.close()
#
#        print_ contents
#        self.assertEquals(getRenameRefactored(), contents)

    def testFind(self):
        r = refactoring.Refactoring()
        s = r.findDefinition(FILE, 7+1, 4)
        
        s = s.replace('[(','').replace(')]','').split(',')
        self.assert_( s[0].endswith('temporary_file.py'))
        self.assertEquals('2', s[1])   #line
        self.assertEquals('6', s[2])   #col
        self.assertEquals('100', s[3]) #accuracy

        createFile(FILE, getFindFile())
        s = r.findDefinition(FILE, 5+1, 2)
        s1 = r.findDefinition(FILE, 5+1, 3)
        s2 = r.findDefinition(FILE, 5+1, 4)
        self.assert_(s == s1 == s2)

def getFindFile():
    s = \
'''
class C:
    def aaa(self):
        return 0
c = C()
c.aaa()
'''
    return s
        
if __name__ == '__main__':
    try:
        refactoring
    except NameError:
        sys.stdout.write('Not running python tests in platform: %s (unable to import refactoring)\n' % (sys.platform,))

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    