// ################################################################################
// FILE       : SPELLpythonHelper.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the Python helper
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
//
//  This file is part of SPELL.
//
// SPELL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SPELL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SPELL. If not, see <http://www.gnu.org/licenses/>.
//
// ################################################################################

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
// System includes ---------------------------------------------------------
#include <stdarg.h>
#include "Python-ast.h"
#include "opcode.h"
#include "traceback.h"

// See setNewLine method
#undef MIN
#undef MAX
#define MIN(a, b) ((a) < (b) ? (a) : (b))
#define MAX(a, b) ((a) > (b) ? (a) : (b))


// Singleton instance
SPELLpythonHelper* SPELLpythonHelper::s_instance = 0;
// Instance lock
SPELLmutex SPELLpythonHelper::s_instanceLock;

//=============================================================================
// FUNCTION: objargs_mktuple
// DESCRIPTION: helper for creating an argument tuple from a variable argument list.
//=============================================================================
static PyObject * objargs_mktuple(va_list va)
{
    int i, n = 0;
    va_list countva = va;
    PyObject *result, *tmp;

    while (((PyObject *)va_arg(countva, PyObject *)) != NULL)
        ++n;
    result = PyTuple_New(n);
    if (result != NULL && n > 0)
    {
        for (i = 0; i < n; ++i)
        {
            tmp = (PyObject *)va_arg(va, PyObject *);
            PyTuple_SET_ITEM(result, i, tmp);
            Py_INCREF(tmp);
        }
    }
    return result;
}

//=============================================================================
// CONSTRUCTOR : SPELLpythonHelper::SPELLpythonHelper
//=============================================================================
SPELLpythonHelper::SPELLpythonHelper()
{
    m_initialized = false;
}

//=============================================================================
// DESTRUCTOR : SPELLpythonHelper::~SPELLpythonHelper
//=============================================================================
SPELLpythonHelper::~SPELLpythonHelper()
{
}

//=============================================================================
// METHOD    : SPELLpythonHelper::instance()
//=============================================================================
SPELLpythonHelper& SPELLpythonHelper::instance()
{
	SPELLmonitor m(s_instanceLock);
    if (s_instance == NULL)
    {
        s_instance = new SPELLpythonHelper();
    }
    return *s_instance;
}

//============================================================================
// METROD    : SPELLpythonHelper::importAllFrom
//============================================================================
void SPELLpythonHelper::importAllFrom( const std::string& package )
{
	LOG_INFO("Import all from " + package)
    PyObject* dict = PyDict_New();
    PyObject* module = PyImport_ImportModuleEx( const_cast<char*>(package.c_str()), dict, dict, NULL);
    if ( module == NULL )
    {
        SPELLcoreException* ex = errorToException();
        throw *ex;
    }

    PyObject* moduleDict = PyModule_GetDict( module );

    std::vector<std::string> tokens = tokenize( package, "." );

    std::vector<std::string>::iterator it;
    for( it = tokens.begin(); it != tokens.end(); it++ )
    {
        // Skip the first token
        if (it == tokens.begin()) continue;
        // Get the submodule
        module = PyDict_GetItemString( moduleDict, (*it).c_str() );
        moduleDict = PyModule_GetDict( module );
    }

    PyObject* mainModule = PyImport_AddModule("__main__");
    if (mainModule == NULL)
    {
        throw SPELLcoreException("Cannot import " + package, "Unable to access main module");
    }
    PyObject* main_dict = PyModule_GetDict(mainModule);
    if (main_dict== NULL)
    {
        throw SPELLcoreException("Cannot import " + package, "Unable to access main module dictionary");
    }

    PyObject* keyList = PyDict_Keys( moduleDict );
    int keyCount = PyList_Size( keyList );
    int count = 0;
    for( count = 0; count < keyCount; count++ )
    {
        PyObject* key = PyList_GetItem( keyList, count );
        PyObject* value = PyDict_GetItem( moduleDict, key );
        PyDict_SetItem( main_dict, key, value );
    }
    DEBUG("Imported all from module " + package)
}

//============================================================================
// METROD    : SPELLpythonHelper::getMainDict()
//============================================================================
PyObject* SPELLpythonHelper::getMainDict()
{
    PyObject* mainModule = PyImport_AddModule("__main__");
    if (mainModule == NULL)
    {
        throw SPELLcoreException("Cannot access main dictionary", "Unable to access main module");
    }
    PyObject* main_dict = PyModule_GetDict(mainModule);
    return main_dict;
}

//============================================================================
// METROD    : SPELLpythonHelper::install
//============================================================================
void SPELLpythonHelper::install( PyObject* object, const std::string& name )
{
    DEBUG("Installing object in globals: " + name)
    install(object,name,"__main__");
}

//============================================================================
// METROD    : SPELLpythonHelper::install
//============================================================================
void SPELLpythonHelper::install( PyObject* object, const std::string& name, const std::string& module )
{
    PyObject* moduleObj = PyImport_AddModule( module.c_str() );
    if (moduleObj == NULL)
    {
        throw SPELLcoreException("Cannot install " + name, "Unable to access module " + module);
    }
    PyObject* dict = PyModule_GetDict(moduleObj);
    if (dict== NULL)
    {
        throw SPELLcoreException("Cannot install " + name, "Unable to access module dictionary");
    }
    if( PyDict_SetItemString( dict, name.c_str() , object) != 0)
    {
        throw SPELLcoreException("Cannot install " + name, "Unable to set object instance on dictionary");
    }
    Py_INCREF(object);
}

//=============================================================================
// METHOD    : SPELLpythonHelper::callMethod
//=============================================================================
PyObject* SPELLpythonHelper::callMethod( PyObject* obj, const std::string& method, ... )
{
    PyObject *args, *result;
    va_list vargs;

    PyObject* methodC = PyObject_GetAttr(obj, SSTRPY(method) );
    if (methodC == NULL)
    {
    	// Must clear the error from Python side, we are threating it already here
    	// with the core exception
        throw SPELLcoreException("Unable to call method " + method, "No such method");
    }

    va_start(vargs, method);
    args = objargs_mktuple(vargs);
    va_end(vargs);

    if (args != NULL)
    {
        result = PyObject_Call( methodC, args, NULL);
        if (result != NULL)
        {
            Py_INCREF(result);
        }
    }
    else
    {
        throw SPELLcoreException("Unable to call method " + method, "Error when extracting arguments");
    }
    return result;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::callFunction
//=============================================================================
PyObject* SPELLpythonHelper::callFunction( const std::string& module, const std::string& function, ... )
{
    PyObject *args, *result;
    va_list vargs;
    PyObject* functionC = getObject( module, function );
    if (functionC == NULL)
    {
        throw SPELLcoreException("Unable to call function " + function, "No such function");
    }
    va_start(vargs, function);
    args = objargs_mktuple(vargs);
    va_end(vargs);

    if (args != NULL)
    {
        assert( functionC != NULL );
        result = PyObject_Call( functionC, args, NULL);
        if (result != NULL)
        {
            Py_INCREF(result);
        }
    }
    else
    {
        throw SPELLcoreException("Unable to call method " + function, "Error when extracting arguments");
    }
    return result;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::callSpellFunction
//=============================================================================
PyObject* SPELLpythonHelper::callSpellFunction( const std::string& function, PyObject* args, PyObject* kargs )
{
    PyObject* functionC = PyDict_GetItemString( getMainDict(), function.c_str());
    PyObject* result = NULL;
    if (functionC == NULL)
    {
        throw SPELLcoreException("Unable to call language function " + function, "No such function");
    }
    result = PyObject_Call( functionC, args, kargs );
    if (result != NULL)
    {
        Py_INCREF(result);
    }
    return result;

}

//=============================================================================
// METHOD    : SPELLpythonHelper::eval
//=============================================================================
PyObject* SPELLpythonHelper::eval( const std::string& expression, bool file )
{
    PyObject* result = PyRun_String( expression.c_str(), file ? Py_file_input : Py_eval_input, getMainDict(), NULL );
    checkError();
    return result;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::getObject
//=============================================================================
PyObject* SPELLpythonHelper::getObject( const std::string& module, const std::string& object )
{
    PyObject* obj = NULL;
	PyObject* moduleObj = getModule(module);
	if (moduleObj == NULL)
	{
		throw SPELLcoreException("Unable to get object " + object, "Unable to access module " + module);
	}
	PyObject* dict = PyModule_GetDict(moduleObj);
	if (dict== NULL)
	{
		throw SPELLcoreException("Unable to get object " + object, "Unable to access module dictionary");
	}
	obj = PyDict_GetItemString(dict, object.c_str());
    return obj;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::getModule
//=============================================================================
PyObject* SPELLpythonHelper::getModule( const std::string& module )
{
    PyObject* moduleObj = NULL;
	moduleObj = PyImport_ImportModule( module.c_str() );
	if (moduleObj == NULL || moduleObj == Py_None )
	{
		throw SPELLcoreException("Unable to get module " + module, "Import failed");
	}
    return moduleObj;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::initialize
//=============================================================================
void SPELLpythonHelper::initialize()
{
    m_initialized = false;

    LOG_INFO("[PYH] Initializing Python")
    // Initialize python, builtins, main module, etc.
    Py_Initialize();
    PyEval_InitThreads();

    if (!PyEval_ThreadsInitialized())
    {
    	std::cerr << "FATAL: cannot initialize threads" << std::endl;
        throw SPELLcoreException("Unable to initialize", "Cannot initialize threads");
    }

    char* home = getenv( "SPELL_HOME" );
    char* cots = getenv( "SPELL_COTS" );
    if (home == NULL)
    {
    	std::cerr << "FATAL: SPELL_HOME not defined" << std::endl;
        throw SPELLcoreException("Unable to initialize", "SPELL_HOME variable not defined");
    }
    if (cots == NULL)
    {
    	std::cerr << "FATAL: SPELL_COTS not defined" << std::endl;
        throw SPELLcoreException("Unable to initialize", "SPELL_COTS variable not defined");
    }
    std::string homestr = home;
    std::string cotsstr = cots;

    LOG_INFO("[PYH] Initializing Python path")

    addToPath(".");
    addToPath( cotsstr +  PATH_SEPARATOR + "lib" + PATH_SEPARATOR + "python2.5" );
    addToPath( cotsstr +  PATH_SEPARATOR + "lib" + PATH_SEPARATOR + "python2.5" + PATH_SEPARATOR + "site-packages" );
    addToPath( cotsstr +  PATH_SEPARATOR + "lib" + PATH_SEPARATOR + "python2.5" + PATH_SEPARATOR + "lib-dynload" );
    addToPath( homestr );
    addToPath( homestr + PATH_SEPARATOR + "lib" );
    addToPath( homestr + PATH_SEPARATOR + "spell" );
    addToPath( homestr + PATH_SEPARATOR + "server" );

    // Initialize the system arguments to empty list (no script is executed)
    char** argv = new char*[1];
    argv[0] = "";
    PySys_SetArgv(1,argv);

    m_initialized = true;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::loadFramework()
//=============================================================================
void SPELLpythonHelper::loadFramework()
{
    importAllFrom("spell.lang.functions");
    importAllFrom("spell.lang.constants");
    importAllFrom("spell.lang.modifiers");
    importAllFrom("spell.lang.user");
    importAllFrom("spell.lib.adapter.utctime");
    importAllFrom("math");
}

//=============================================================================
// METHOD    : SPELLpythonHelper::finalize
//=============================================================================
void SPELLpythonHelper::finalize()
{
    LOG_INFO("[PYH] Cleaning up Python")
    Py_Finalize();
    m_initialized = false;
    LOG_INFO("[PYH] Python finalized")
}

//=============================================================================
// METHOD    : SPELLpythonHelper::addToPath
//=============================================================================
void SPELLpythonHelper::addToPath( const std::string& path )
{
    m_pythonPath.push_back(path);
    LOG_INFO("[PYH] Append to python path: " + path);
    std::vector<std::string>::iterator it;
    std::string libs = "";
    for( it = m_pythonPath.begin(); it != m_pythonPath.end(); it++)
    {
        if (libs.size()>0) libs += ":";
        libs += (*it);
    }
    PySys_SetPath( const_cast<char*>(libs.c_str()) );
}

//=============================================================================
// METHOD    : SPELLpythonHelper::throwSyntaxException
//=============================================================================
void SPELLpythonHelper::throwSyntaxException( const std::string& message, const std::string& reason )
{
	LOG_ERROR("SYNTAX EXCEPTION: " + message + ", " + reason);
	acquireGIL();
	PyObject* syntaxException = NULL;
	PyObject* instance = NULL;
	syntaxException = getObject( "spell.lib.exception", "SyntaxException" );
    if (syntaxException == NULL)
    {
        PyErr_Print();
        PyErr_SetString( PyExc_RuntimeError, "Unable to get spell.lib.exception.SyntaxException" );
        return;
    }
	instance = PyObject_CallFunction( syntaxException, NULL, NULL);
    if (instance == NULL)
    {
        PyErr_Print();
        PyErr_SetString( PyExc_RuntimeError, "Unable to instantiate SyntaxException" );
        return;
    }
    PyObject* msg = SSTRPY(message);
    PyObject* rea = SSTRPY(reason);
    PyObject_SetAttrString( instance, "message", msg );
    PyObject_SetAttrString( instance, "reason", rea );
    PyErr_SetObject( syntaxException, instance );
	//IMPORTANT!! DO NOT RELEASE GIL HERE, THIS TRICK WILL ALLOW
    //THE INTERPRETER TO PROCESS THE ERROR PROPERLY
}

//=============================================================================
// METHOD    : SPELLpythonHelper::throwDriverException
//=============================================================================
void SPELLpythonHelper::throwDriverException( const std::string& message, const std::string& reason )
{
	LOG_ERROR("DRIVER EXCEPTION: " + message + ", " + reason);
	acquireGIL();
	PyObject* driverException = NULL;
	PyObject* instance = NULL;
	driverException = getObject( "spell.lib.exception", "DriverException" );
    if (driverException == NULL)
    {
        PyErr_Print();
        PyErr_SetString( PyExc_RuntimeError, "Unable to get spell.lib.exception.DriverException" );
        return;
    }
	instance = PyObject_CallFunction( driverException, NULL, NULL);
    if (instance == NULL)
    {
        PyErr_Print();
        PyErr_SetString( PyExc_RuntimeError, "Unable to instantiate DriverException" );
        return;
    }
    PyObject* msg = SSTRPY(message);
    PyObject* rea = SSTRPY(reason);
    PyObject_SetAttrString( instance, "message", msg );
    PyObject_SetAttrString( instance, "reason", rea );
    PyErr_SetObject( driverException, instance );
	//IMPORTANT!! DO NOT RELEASE GIL HERE, THIS TRICK WILL ALLOW
    //THE INTERPRETER TO PROCESS THE ERROR PROPERLY
}

//=============================================================================
// METHOD    : SPELLpythonHelper::throwRuntimeException
//=============================================================================
void SPELLpythonHelper::throwRuntimeException( const std::string& message, const std::string& reason )
{
	std::string err = message + ": " + reason;
	PyErr_SetString( PyExc_RuntimeError, err.c_str() );
}

//=============================================================================
// METHOD    : SPELLpythonHelper::errorToException
//=============================================================================
SPELLcoreException* SPELLpythonHelper::errorToException()
{
    SPELLcoreException* exception = NULL;
    PyObject* err = PyErr_Occurred();
    if (err != NULL)
    {
        PyObject* ptype;
        PyObject* pvalue;
        PyObject* ptraceback;
        // Fetch the error information
        PyErr_Fetch( &ptype, &pvalue, &ptraceback );

        exception = errorToException( err, ptype, pvalue, ptraceback );
    }
    return exception;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::errorToException
//=============================================================================
SPELLcoreException* SPELLpythonHelper::errorToException( PyObject* err, PyObject* ptype, PyObject* pvalue, PyObject* ptraceback )
{
    SPELLcoreException* exception = NULL;
    DEBUG("[PYH] Fetching error information")

    // Otherwise gather error information
    std::string value = PyString_AsString( PyObject_Str(pvalue) );
    std::string proc = "???";
    int line = -1;

    std::string message = "";
    std::string reason = PYSSTR(pvalue);

    // Find out the kind of error
    if (isInstance(err, "SpellException", "spell.lib.exception"))
    {
    	exception = NULL;
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_ImportError))
    {
        message = "Import error at " + proc + ":" + ISTR(line);
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_IMPORT, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_IndexError))
    {
        message = "Index error at " + proc + ":" + ISTR(line);
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_INDEX, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_KeyError))
    {
        message = "Key error at " + proc + ":" + ISTR(line);
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_KEY, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_MemoryError))
    {
        message = "Memory error at " + proc + ":" + ISTR(line);
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_MEMORY, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_NameError))
    {
        message = "Name error at " + proc + ":" + ISTR(line);
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_NAME, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_SyntaxError))
    {
        // It is a tuple like ('invalid syntax', ('file.py', lineno, offset, 'text'))
		message = "(unknown syntax error, could not gather information)";
    	if (PyTuple_Size(pvalue)==2)
    	{
    		PyObject* data = PyTuple_GetItem(pvalue, 1);
    		if (PyTuple_Size(data)==4)
    		{
    			PyObject* filename = PyTuple_GetItem( data, 0);
    			PyObject* lineno   = PyTuple_GetItem( data, 1);
    			PyObject* text     = PyTuple_GetItem( data, 3);
    			message = "At " + PYREPR(filename) + ", line " + PYSSTR(lineno) + ": " + PYREPR(text);
    		}
    	}
        exception = new SPELLcoreException( message, "Invalid syntax", SPELL_PYERROR_SYNTAX, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_TypeError))
    {
        message = "Type error at " + proc + ":" + ISTR(line);
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_TYPE, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_ValueError))
    {
        message = "Value error at " + proc + ":" + ISTR(line);
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_VALUE, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_BaseException))
    {
        message = "Python error at " + proc + ":" + ISTR(line);
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_OTHER, false );
    }
    else
    {
        exception = new SPELLcoreException("Uncontrolled error", reason, true);
    }
    return exception;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::isInstance()
//=============================================================================
bool SPELLpythonHelper::isInstance( PyObject* object, const std::string& className, const std::string& package )
{
    PyObject* theClass = getObject(package, className);
    if (theClass == NULL) throw SPELLcoreException("Unable to evaluate instance match", "Cannot get class " + className + " in " + package, true);
    return (PyObject_IsInstance(object,theClass));
}

//=============================================================================
// METHOD    : SPELLpythonHelper::isTime
//=============================================================================
bool SPELLpythonHelper::isTime( PyObject* instance )
{
	return isInstance( instance, "TIME", "spell.lib.adapter.utctime" );
}

//=============================================================================
// METHOD    : SPELLpythonHelper::evalTime
//=============================================================================
SPELLtime SPELLpythonHelper::evalTime( PyObject* expression )
{
    if (isTime(expression))
    {
        PyObject* isRel = callMethod(expression,"isRel",NULL);
        if (isRel == Py_True)
        {
            PyObject* secs = callMethod(expression,"rel",NULL);
            return SPELLtime( PyLong_AsLong(secs), 0, true );
        }
        else
        {
            PyObject* secs = callMethod(expression,"abs",NULL);
            int seconds = 0;
            if (PyFloat_Check(secs))
            {
            	double fsecs = PyFloat_AsDouble(secs);
            	seconds = PyLong_AsLong(PyLong_FromDouble(fsecs));
            }
            else
            {
            	seconds = PyLong_AsLong(secs);
            }
            return SPELLtime( seconds, 0, false );
        }
    }
    else
    {
        throw SPELLcoreException("Unable to evaluate time", "Not a TIME instance", false);
    }
}

//=============================================================================
// METHOD    : SPELLpythonHelper::evalTime
//=============================================================================
SPELLtime SPELLpythonHelper::evalTime( const std::string& expression )
{
	PyObject* theClass = getObject("spell.lib.adapter.utctime", "TIME");
	PyObject* args = PyTuple_New(1);
	PyObject* pyStr = SSTRPY(expression);
	PyTuple_SetItem(args,0,pyStr);
	PyObject* instance = PyObject_Call( theClass, args, NULL );

    if (instance == NULL)
    {
        throw SPELLcoreException("Unable to evaluate time", "Input was: '" + expression + "'", false);
    }

    return evalTime(instance);
}

//=============================================================================
// METHOD    : SPELLpythonHelper::beginSafeAllowThreads
//=============================================================================
PyThreadState* SPELLpythonHelper::beginSafeAllowThreads()
{
	PyThreadState *tstate = PyThreadState_GET();
	if (tstate != NULL)
	{
		tstate = PyEval_SaveThread();
	}
	return tstate;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::endSafeAllowThreads
//=============================================================================
void SPELLpythonHelper::endSafeAllowThreads( PyThreadState* tstate )
{
	if (tstate != NULL)
	{
		PyEval_RestoreThread(tstate);
	}
}

//=============================================================================
// METHOD    : SPELLpythonHelper::acquireGIL();
//=============================================================================
PyGILState_STATE SPELLpythonHelper::acquireGIL()
{
	return PyGILState_Ensure();
}

//=============================================================================
// METHOD    : SPELLpythonHelper::releaseGIL();
//=============================================================================
void SPELLpythonHelper::releaseGIL( PyGILState_STATE state )
{
	PyGILState_Release(state);
}

//=============================================================================
// METHOD    : SPELLpythonHelper::readProcedureFile()
//=============================================================================
std::string SPELLpythonHelper::readProcedureFile( const std::string& filename )
{
	// Holds the source code
	std::string source = "";
    std::ifstream file;
    file.open( filename.c_str() );
    if (!file.is_open())
    {
        throw SPELLcoreException("Cannot parse file " + filename, "Unable to open");
    }
    // Open the file for read only
    try
    {
	    while(!file.eof())
	    {
	        std::string line = "";
	        std::getline(file,line);
	        source += line + "\n";
	    }
    }
    catch(...)
    {
    	file.close();
    	throw;
    }
	file.close();

	replace( source, "\r\n", "\n" );

    return source;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::compile();
//=============================================================================
PyCodeObject* SPELLpythonHelper::compile( const std::string& filePath )
{
    DEBUG("[PYH] Compiling procedure " + filePath)

    // Will hold the AST preprocessed bytecode
    mod_ty ast;
    // Will hold the resulting code object
    PyCodeObject* code = NULL;
    // Compiler flags set to default
    PyCompilerFlags flags;
    flags.cf_flags = PyCF_SOURCE_IS_UTF8;
    // The arena is required for compilation process
    PyArena *arena = NULL;

    try
    {
        // The arena is required for compilation process
        arena = PyArena_New();
        if(arena == NULL)
        {
            throw SPELLcoreException("Unable to compile script", "Could not create arena");
        }

        // Read the source code
        std::string source = readProcedureFile( filePath );
    	if (source == "")
    	{
    		throw SPELLcoreException("Unable to compile script", "Cannot read source code");
    	}

        // Compile the script to obtain the AST code
        ast = PyParser_ASTFromString( source.c_str(), filePath.c_str(), Py_file_input, &flags, arena);
        if (ast == NULL) // Could not get ast
        {
            SPELLcoreException* exception = errorToException();
            if (exception != NULL)
            {
                exception->setError( "Unable to compile script: " + exception->getError() );
                throw *exception;
            }
            else
            {
                throw SPELLcoreException("Unable to compile script", "Could not get AST code");
            }
        }

        // Construct the code object from AST
        code = PyAST_Compile(ast, filePath.c_str(), &flags, arena);
        if (code == NULL)
        {
            SPELLcoreException* exception = errorToException();
            if (exception != NULL)
            {
                exception->setError( "Unable to compile script: " + exception->getError() );
                throw *exception;
            }
            else
            {
                throw SPELLcoreException("Unable to compile script", "Could not compile the code");
            }
        }
    }
    catch(SPELLcoreException& ex)
    {
        PyArena_Free(arena);
        code = NULL;
        throw ex;
    }
    // Cleanup after compilation
    PyArena_Free(arena);

    DEBUG("[PYH] Compilation success");
    return code;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::compileScript();
//=============================================================================
PyCodeObject* SPELLpythonHelper::compileScript( const std::string& source )
{
    DEBUG("[PYH] Compiling source code script");

	// Will hold the code object
    PyCodeObject* code = NULL;
    // Will hold the AST preprocessed bytecode
    mod_ty ast;
    // Compiler flags set to default
    PyCompilerFlags flags;
    flags.cf_flags = 0;
    // The arena is required for compilation process
    PyArena *arena = NULL;

    try
    {
        // The arena is required for compilation process
        arena = PyArena_New();
        if(arena == NULL)
        {
            throw SPELLcoreException("Unable to compile script", "Could not create arena");
        }

        // Compile the script to obtain the AST code
        ast = PyParser_ASTFromString( source.c_str(), "<string>", Py_file_input, &flags, arena);
        if (ast == NULL) // Could not get ast
        {
            SPELLcoreException* exception = errorToException();
            if (exception != NULL)
            {
                exception->setError( "Unable to compile script: " + exception->getError() );
                throw *exception;
            }
            else
            {
                throw SPELLcoreException("Unable to compile script", "Could not get AST code");
            }
        }

        // Construct the code object from AST
        code = PyAST_Compile(ast, "<string>", &flags, arena);
        if (code == NULL)
        {
            SPELLcoreException* exception = errorToException();
            if (exception != NULL)
            {
                exception->setError( "Unable to compile script: " + exception->getError() );
                throw *exception;
            }
            else
            {
                throw SPELLcoreException("Unable to compile script", "Could not compile the code");
            }
        }
    }
    catch(SPELLcoreException& ex)
    {
        PyArena_Free(arena);
        code = NULL;
        throw ex;
    }
    // Cleanup after compilation
    PyArena_Free(arena);
    DEBUG("[PYH] Compilation success");
    return code;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::setNewLine();
//=============================================================================
bool SPELLpythonHelper::setNewLine( PyFrameObject* frame, const int& new_lineno, const int& new_lasti )
{
	DEBUG("[PYH] Changing line number on frame " + PYCREPR(frame));

	/////////////////////////////////////////////////////////
    // THIS IS NASTY: copied from Python 2.5 implementation
	/////////////////////////////////////////////////////////

    int new_iblock = 0;            /* The new value of f_iblock */

    char* code = NULL;             /* The bytecode for the frame... */
    Py_ssize_t code_len = 0;       /* ...and its length */
    int min_addr = 0;              /* Scanning the SETUPs and POPs */
    int max_addr = 0;              /* (ditto) */

    int delta_iblock = 0;          /* (ditto) */
    int min_delta_iblock = 0;      /* (ditto) */
    int min_iblock = 0;            /* (ditto) */

    int f_lasti_setup_addr = 0;    /* Policing no-jump-into-finally */
    int new_lasti_setup_addr = 0;  /* (ditto) */
    int blockstack[CO_MAXBLOCKS];  /* Walking the 'finally' blocks */
    int in_finally[CO_MAXBLOCKS];  /* (ditto) */
    int blockstack_top = 0;        /* (ditto) */
    int setup_op = 0;              /* (ditto) */

    // Fail if the line comes before the start of the code block.
    if (new_lineno < frame->f_code->co_firstlineno)
    {
        LOG_ERROR("[PYH] Unable to set new line: line comes before start of code block");
        return false;
    }

    /* We're now ready to look at the bytecode. */
    PyString_AsStringAndSize(frame->f_code->co_code, &code, &code_len);
    min_addr = MIN(new_lasti, frame->f_lasti);
    max_addr = MAX(new_lasti, frame->f_lasti);

    /* You can't jump onto a line with an 'except' statement on it -
     * they expect to have an exception on the top of the stack, which
     * won't be true if you jump to them.  They always start with code
     * that either pops the exception using POP_TOP (plain 'except:'
     * lines do this) or duplicates the exception on the stack using
     * DUP_TOP (if there's an exception type specified).  See compile.c,
     * 'com_try_except' for the full details.  There aren't any other
     * cases (AFAIK) where a line's code can start with DUP_TOP or
     * POP_TOP, but if any ever appear, they'll be subject to the same
     * restriction (but with a different error message). */
    if (code[new_lasti] == DUP_TOP || code[new_lasti] == POP_TOP)
    {
        LOG_ERROR("[PYH] Unable to set new line: cannot go inside a try/except block");
        return false;
    }

    /* You can't jump into or out of a 'finally' block because the 'try'
     * block leaves something on the stack for the END_FINALLY to clean
     * up.  So we walk the bytecode, maintaining a simulated blockstack.
     * When we reach the old or new address and it's in a 'finally' block
     * we note the address of the corresponding SETUP_FINALLY.  The jump
     * is only legal if neither address is in a 'finally' block or
     * they're both in the same one.  'blockstack' is a stack of the
     * bytecode addresses of the SETUP_X opcodes, and 'in_finally' tracks
     * whether we're in a 'finally' block at each blockstack level. */
    f_lasti_setup_addr = -1;
    new_lasti_setup_addr = -1;
    memset(blockstack, '\0', sizeof(blockstack));
    memset(in_finally, '\0', sizeof(in_finally));
    blockstack_top = 0;
    int addr = 0;
    for (addr = 0; addr < code_len; addr++)
    {
        unsigned char op = code[addr];
        switch (op)
        {
        case SETUP_LOOP:
        case SETUP_EXCEPT:
        case SETUP_FINALLY:
            blockstack[blockstack_top++] = addr;
            in_finally[blockstack_top-1] = 0;
            break;
        case POP_BLOCK:
            assert(blockstack_top > 0);
            setup_op = code[blockstack[blockstack_top-1]];
            if (setup_op == SETUP_FINALLY)
            {
                in_finally[blockstack_top-1] = 1;
            }
            else {
                blockstack_top--;
            }
            break;
        case END_FINALLY:
            /* Ignore END_FINALLYs for SETUP_EXCEPTs - they exist
             * in the bytecode but don't correspond to an actual
             * 'finally' block.  (If blockstack_top is 0, we must
             * be seeing such an END_FINALLY.) */
            if (blockstack_top > 0)
            {
                setup_op = code[blockstack[blockstack_top-1]];
                if (setup_op == SETUP_FINALLY)
                {
                    blockstack_top--;
                }
            }
            break;
        }

        /* For the addresses we're interested in, see whether they're
         * within a 'finally' block and if so, remember the address
         * of the SETUP_FINALLY. */
        if (addr == new_lasti || addr == frame->f_lasti)
        {
            int i = 0;
            int setup_addr = -1;
            for (i = blockstack_top-1; i >= 0; i--)
            {
                if (in_finally[i])
                {
                    setup_addr = blockstack[i];
                    break;
                }
            }
            if (setup_addr != -1)
            {
                if (addr == new_lasti)
                {
                    new_lasti_setup_addr = setup_addr;
                }
                if (addr == frame->f_lasti)
                {
                    f_lasti_setup_addr = setup_addr;
                }
            }
        }
        if (op >= HAVE_ARGUMENT)
        {
            addr += 2;
        }
    }
    /* Verify that the blockstack tracking code didn't get lost. */
    assert(blockstack_top == 0);

    /* After all that, are we jumping into / out of a 'finally' block? */
    if (new_lasti_setup_addr != f_lasti_setup_addr)
    {
        LOG_ERROR("[PYH] Unable to set new line: cannot go outside the try/except block");
        return false;
    }

    /* Police block-jumping (you can't jump into the middle of a block)
     * and ensure that the blockstack finishes up in a sensible state (by
     * popping any blocks we're jumping out of).  We look at all the
     * blockstack operations between the current position and the new
     * one, and keep track of how many blocks we drop out of on the way.
     * By also keeping track of the lowest blockstack position we see, we
     * can tell whether the jump goes into any blocks without coming out
     * again - in that case we raise an exception below. */
    delta_iblock = 0;
    for (addr = min_addr; addr < max_addr; addr++)
    {
        unsigned char op = code[addr];
        switch (op)
        {
        case SETUP_LOOP:
        case SETUP_EXCEPT:
        case SETUP_FINALLY:
            delta_iblock++;
            break;
        case POP_BLOCK:
            delta_iblock--;
            break;
        }

        min_delta_iblock = MIN(min_delta_iblock, delta_iblock);
        if (op >= HAVE_ARGUMENT)
        {
            addr += 2;
        }
    }

    /* Derive the absolute iblock values from the deltas. */
    min_iblock = frame->f_iblock + min_delta_iblock;
    if (new_lasti > frame->f_lasti)
    {
        /* Forwards jump. */
        new_iblock = frame->f_iblock + delta_iblock;
    }
    else
    {
        /* Backwards jump. */
        new_iblock = frame->f_iblock - delta_iblock;
    }

    /* Are we jumping into a block? */
    if (new_iblock > min_iblock)
    {
        LOG_ERROR("[PYH] Unable to set new line: cannot jump into a block");
        return false;
    }

    /* Pop any blocks that we're jumping out of. */
    while (frame->f_iblock > new_iblock)
    {
        PyTryBlock *b = &frame->f_blockstack[--frame->f_iblock];
        while ((frame->f_stacktop - frame->f_valuestack) > b->b_level)
        {
            PyObject *v = (*--frame->f_stacktop);
            Py_DECREF(v);
        }
    }
    /* Finally set the new f_lineno and f_lasti and return OK. */
    frame->f_lineno = new_lineno;
    frame->f_lasti = new_lasti;
    DEBUG("[PYH] Set frame lineno " + ISTR(new_lineno) + ":" + ISTR(new_lasti));
    return true;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::createFrame();
//=============================================================================
PyFrameObject* SPELLpythonHelper::createFrame( const std::string& filename, PyCodeObject* code )
{
	DEBUG("[PYH] Creating frame for code " + PYCREPR(code));

	PyFrameObject* frame = NULL;

	PyObject* mainModule = PyImport_AddModule("__main__");

    if (mainModule == NULL)
    {
        throw SPELLcoreException("Unable to create frame", "Cannot access main module");
    }

    // Do it only the first time
    PyObject* mainDict = PyModule_GetDict(mainModule);

    if (mainDict== NULL)
    {
        throw SPELLcoreException("Unable to create frame", "Cannot access main dictionary");
    }

    // Set the file name if necessary
    if (PyDict_GetItemString(mainDict, "__file__") == NULL)
    {
        PyObject* f = SSTRPY(filename);
        if (f == NULL)
        {
            return NULL;
        }
        if (PyDict_SetItemString(mainDict, "__file__", f) < 0)
        {
            Py_DECREF(f);
            return NULL;
        }
        Py_DECREF(f);
    }
    DEBUG("[PYH] Frame creation");
    frame = PyFrame_New( PyThreadState_Get(), code, mainDict, mainDict );
    Py_INCREF(frame);
    return frame;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::checkError
//=============================================================================
void SPELLpythonHelper::checkError()
{
    PyObject* err = PyErr_Occurred();
    if (err != NULL)
    {
        std::cerr << std::endl << "===============================" << std::endl;
        //PyErr_Print();
        PyObject* ptype;
        PyObject* pvalue;
        PyObject* ptraceback;
        PyErr_Fetch( &ptype, &pvalue, &ptraceback );
        std::cerr << "TYPE : " << PYREPR(ptype) << std::endl;
        std::cerr << "VALUE: " << PYREPR(pvalue) << std::endl;
        if (ptraceback)
        {
            std::cerr << "Traceback: " << std::endl;
    		PyTracebackObject* traceback = (PyTracebackObject*) ptraceback;
        	while(traceback != NULL)
        	{
        		std::cerr << "at " << PYREPR(traceback->tb_frame->f_code->co_filename) << ":" << traceback->tb_lineno << std::endl;
        		traceback = traceback->tb_next;
        	}
        }
        std::cerr << "===============================" << std::endl << std::endl;
        // Parse the exception to give better information about the error on python side
        PyObject* spellException = SPELLpythonHelper::instance().getObject("spell.lib.exception", "SpellException");
        if (PyObject_IsInstance( pvalue, spellException ))
        {
            PyObject* msg = PyObject_GetAttrString( pvalue, "message" );
            PyObject* rea = PyObject_GetAttrString( pvalue, "reason" );
            std::string message = "<unknown error>";
            std::string reason  = "";
            if (msg != NULL)
            {
                message = PYSSTR(msg);
            }
            if (rea != NULL )
            {
                reason = PYSSTR(rea);
            }
            /** \todo decide the fatal flag value */
            std::cerr << "RAISING INTERNAL DRIVER EXCEPTION" << std::endl;
            throw SPELLcoreException("Driver error: " + message, reason, SPELL_DRIVER_ERROR );
        }
        else
        {
            std::cerr << "RAISING PYTHON ERROR EXCEPTION" << std::endl;
            throw SPELLcoreException("Python error", PYSSTR(pvalue));
        }
    }
}
