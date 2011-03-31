'''
@author Fabio Zadrozny 
'''
import os
import sys
#make it as if we were executing from the directory above this one (so that we can use pycompletionserver
#without the need for it being in the pythonpath)
sys.argv[0] = os.path.dirname(sys.argv[0]) 
#twice the dirname to get the previous level from this file.
sys.path.insert(1, os.path.join(  os.path.dirname( sys.argv[0] )) )

try:
    import __builtin__ #@UnusedImport
    BUILTIN_MOD = '__builtin__'
except ImportError:
    BUILTIN_MOD = 'builtins'


if sys.platform.find('java') == -1:
    
    HAS_WX = False
    
    import unittest
    try:
        import importsTipper
        import inspect
    except ImportError:
        pass #Not available in jython
    
    class Test(unittest.TestCase):
    
        def p(self, t):
            for a in t:
                sys.stdout.write('%s\n' % (a,))
     
        def testImports3(self):
            tip = importsTipper.GenerateTip('os')
            ret = self.assertIn('path', tip)
            self.assertEquals('', ret[2])
    
        def testImports2(self):
            try:
                tip = importsTipper.GenerateTip('OpenGL.GLUT')
                self.assertIn('glutDisplayFunc', tip)
                self.assertIn('glutInitDisplayMode', tip)
            except ImportError:
                pass
    
        def testImports4(self):
            try:
                tip = importsTipper.GenerateTip('mx.DateTime.mxDateTime.mxDateTime')
                self.assertIn('now', tip)
            except ImportError:
                pass
            
        def testImports2a(self):
            tips = importsTipper.GenerateTip('%s.RuntimeError' % BUILTIN_MOD)
            self.assertIn('__doc__', tips)
            
        def testImports(self):
            '''
            You can print_ the results to check...
            '''
            if HAS_WX:
                tip = importsTipper.GenerateTip('wxPython.wx')
                self.assertIn('wxApp'        , tip)
                
                tip = importsTipper.GenerateTip('wxPython.wx.wxApp')
                
                try:
                    tip = importsTipper.GenerateTip('qt')
                    self.assertIn('QWidget'        , tip)
                    self.assertIn('QDialog'        , tip)
                    
                    tip = importsTipper.GenerateTip('qt.QWidget')
                    self.assertIn('rect'           , tip)
                    self.assertIn('rect'           , tip)
                    self.assertIn('AltButton'      , tip)
            
                    tip = importsTipper.GenerateTip('qt.QWidget.AltButton')
                    self.assertIn('__xor__'      , tip)
            
                    tip = importsTipper.GenerateTip('qt.QWidget.AltButton.__xor__')
                    self.assertIn('__class__'      , tip)
                except ImportError:
                    pass
                
            tip = importsTipper.GenerateTip(BUILTIN_MOD)
    #        for t in tip[1]:
    #            print_ t
            self.assertIn('object'         , tip)
            self.assertIn('tuple'          , tip)
            self.assertIn('RuntimeError'   , tip)
            self.assertIn('RuntimeWarning' , tip)
            t = self.assertIn('cmp' , tip)
            self.assertEqual('(x, y)', t[2]) #args
            
            t = self.assertIn('isinstance' , tip)
            self.assertEqual('(object, class_or_type_or_tuple)', t[2]) #args
            
            t = self.assertIn('compile' , tip)
            self.assertEqual('(source, filename, mode)', t[2]) #args
            
            t = self.assertIn('setattr' , tip)
            self.assertEqual('(object, name, value)', t[2]) #args
            
            try:
                import compiler
                compiler_module = 'compiler'
            except ImportError:
                compiler_module = 'ast'
            tip = importsTipper.GenerateTip(compiler_module) 
            if compiler_module == 'compiler':
                self.assertArgs('parse', '(buf, mode)', tip)
                self.assertArgs('walk', '(tree, visitor, walker, verbose)', tip)
                self.assertIn('parseFile'      , tip)
            else:
                self.assertArgs('parse', '(expr, filename, mode)', tip)
                self.assertArgs('walk', '(node)', tip)
            self.assertIn('parse'          , tip)
            
        def assertArgs(self, tok, args, tips):
            for a in tips[1]:
                if tok == a[0]:
                    self.assertEquals(args, a[2])
                    return
            raise AssertionError('%s not in %s', tok, tips)
    
        def assertIn(self, tok, tips):
            for a in tips[1]:
                if tok == a[0]:
                    return a
            raise AssertionError('%s not in %s' %(tok, tips))
    
        def testInspect(self):
            
            class C(object):
                def metA(self, a, b):
                    pass
            
            obj = C.metA
            if inspect.ismethod (obj):
                pass
    #            print_ obj.im_func
    #            print_ inspect.getargspec(obj.im_func)
                
            
    def suite():
        s = unittest.TestSuite()
        s.addTest(Test("testImports2"))
        unittest.TextTestRunner(verbosity=2).run(s)

if __name__ == '__main__':
    if sys.platform.find('java') == -1:
        unittest.main()
    else:
        sys.stdout.write('Not running python tests in jython -- platform: %s\n' % (sys.platform,))
    
