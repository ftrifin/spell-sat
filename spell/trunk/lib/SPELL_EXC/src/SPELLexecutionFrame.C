// ################################################################################
// FILE       : SPELLexecutionFrame.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the Python frame manager
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
#include "SPELL_EXC/SPELLexecutionFrame.H"
#include "SPELL_EXC/SPELLexecutor.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLpythonError.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
// System includes ---------------------------------------------------------

// GLOBALS /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR    : SPELLexecutionFrame::SPELLexecutionFrame
//=============================================================================
SPELLexecutionFrame::SPELLexecutionFrame()
: m_modelMap()
{
    m_warmStart = NULL;
    m_error = NULL;
    m_errorLocation = "";
    m_status = EXECUTION_UNKNOWN;
    m_code = NULL;
    m_initialFrame = NULL;
    m_currentFrame = NULL;
    m_model = NULL;
    m_filename = "";
    m_definitions = NULL;
}

//=============================================================================
// DESTRUCTOR    : SPELLexecutionFrame::~SPELLexecutionFrame
//=============================================================================
SPELLexecutionFrame::~SPELLexecutionFrame()
{
	reset();
    if (m_error != NULL)
    {
        delete m_error;
    }
    if (m_warmStart != NULL)
    {
        delete m_warmStart;
    }
    if (m_code != NULL)
    {
        Py_XDECREF(m_code);
    }
    if (m_initialFrame != NULL)
    {
        Py_XDECREF(m_initialFrame);
    }

    m_discardedNames.clear();
}

//=============================================================================
// METHOD     : SPELLexecutionFrame::initialize()
//=============================================================================
void SPELLexecutionFrame::initialize( const std::string& scriptFile )
{
    m_filename = scriptFile;
    // Compile the script and create the Python frame
    // to execute bytecode
    reset();
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::restoreState
//=============================================================================
void SPELLexecutionFrame::restoreState()
{
    // If we have warmstart support
    if (m_warmStart)
    {
        DEBUG( "Starting recovery");
        // Remove the current frame if any
        if (m_initialFrame != NULL)
        {
            Py_XDECREF(m_initialFrame);
            m_initialFrame = NULL;
        }
        // Recover the frame
        m_initialFrame = m_warmStart->restoreState();
        LOG_INFO( "Recovered state at: " + PYREPR(m_initialFrame->f_code->co_filename) + ":" + ISTR(m_initialFrame->f_lineno));
        DEBUG( "Recovered state at: " + PYREPR(m_initialFrame->f_code->co_filename) + ":" + ISTR(m_initialFrame->f_lineno));
    }
    else
    {
        throw SPELLcoreException("Cannot restore state", "Warm start mechanism unavailable");
    }
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::fixState
//=============================================================================
void SPELLexecutionFrame::fixState()
{
    // If we have warmstart support
    if (m_warmStart)
    {
        DEBUG( "Starting state fix");
        // Fix the error state
        m_initialFrame = m_warmStart->fixState();
        LOG_INFO( "Fixed state at: " + PYREPR(m_initialFrame->f_code->co_filename) + ":" + ISTR(m_initialFrame->f_lineno));
        DEBUG( "Fixed state at: " + PYREPR(m_initialFrame->f_code->co_filename) + ":" + ISTR(m_initialFrame->f_lineno));
    }
    else
    {
        throw SPELLcoreException("Cannot fix state", "Warm start mechanism unavailable");
    }
}

//=============================================================================
// METHOD     : SPELLexecutionFrame::compile
//=============================================================================
void SPELLexecutionFrame::compile()
{
    assert( m_filename != "");
    LOG_INFO("[EF] Compiling script " + m_filename)
    DEBUG("[EF] Compiling procedure " + m_filename)

    // Will hold the bytecode code object
    if (m_code != NULL)
    {
        Py_XDECREF(m_code);
        m_code = NULL;
    }

    m_code = SPELLpythonHelper::instance().compile(m_filename);

	DEBUG("[EF] Compilation success");
	LOG_INFO("[EF] Compilation success");
}

//=============================================================================
// METHOD     : SPELLexecutionFrame::compileScript
//=============================================================================
PyCodeObject* SPELLexecutionFrame::compileScript( const std::string& script )
{
    LOG_INFO("[EF] Compiling user script");

	// Will hold the code object
    PyCodeObject* code = SPELLpythonHelper::instance().compileScript(script);

    LOG_INFO("[EF] Compilation success");
    return code;
}

//=============================================================================
// METHOD     : SPELLexecutionFrame::createInitialFrame
//=============================================================================
void SPELLexecutionFrame::createInitialFrame()
{
    // Obtain the main module, this is already setup by Py_Initialize
    DEBUG("[EF] Creating initial frame")

    if (m_initialFrame != NULL)
    {
        Py_DECREF(m_initialFrame);
        m_initialFrame = NULL;
    }

    m_initialFrame = SPELLpythonHelper::instance().createFrame( m_filename, m_code );

    // Force the first frame update
    if (m_currentFrame != NULL)
    {
        Py_XDECREF(m_currentFrame);
        m_currentFrame = NULL;
    }

    DEBUG("[EF] Initial frame ready");
}

//=============================================================================
// METHOD     : SPELLexecutionFrame::reset
//=============================================================================
void SPELLexecutionFrame::reset()
{
    DEBUG("[EF] Resetting");
	// Reset warmstart mechanism in case of reload
    if (m_warmStart) m_warmStart->reset();

    DEBUG("[EF] Removing analysis models");
    // Remove execution analysis models
    ModelMap::iterator mit;
    for( mit = m_modelMap.begin(); mit != m_modelMap.end(); mit++)
    {
    	delete mit->second;
    }
    m_modelMap.clear();
    m_model = NULL;

    DEBUG("[EF] Clearing breakpoints");
    // Clear all defined breakpoints
    m_breakpoints.clearBreakpoints();

    DEBUG("[EF] Clearing execution trace");
    // Remove the execution trace markers
    resetExecutionTrace();

    // Reset terminated flag
    m_terminated = false;

    DEBUG("[EF] Re-compiling");
    // Compile the script.
    compile();

    // Create the Python frame for execution
    createInitialFrame();

    DEBUG("[EF] Reset done.");
}

//=============================================================================
// METHOD     : SPELLexecutionFrame::terminate
//=============================================================================
void SPELLexecutionFrame::terminate()
{
    DEBUG("[EF] Terminating execution");
    m_terminated = true;
    // Note: we substract 3 bytes since the last two instructions are always
    // resembling a "return None" statement. We want the instruction previous
    // to the last one here.
    ModelMap::iterator end = m_modelMap.end();
    for(ModelMap::iterator it = m_modelMap.begin(); it != end; it++)
    {
    	PyFrameObject* frame = it->second->getFrame();
		int lasti = it->second->getLastAddress() -3 ;
		int lineno = it->second->getLastLine();
		DEBUG("[EF] Setting lasti " + ISTR(lasti) + " lineno " + ISTR(lineno) + " on " + PYCREPR(frame));
		frame->f_lasti = lasti;
		frame->f_lineno = lineno;
    }
}

//=============================================================================
// METHOD     : SPELLexecutionFrame::execute
//=============================================================================
const SPELLexecutionResult SPELLexecutionFrame::execute()
{
    // Delete any error remaining from previous execution
    if (m_error)
    {
        delete m_error;
        m_error = NULL;
        m_errorLocation = "";
    }
    PyFrameObject* frame = m_initialFrame;
    m_status = EXECUTION_SUCCESS;
    DEBUG("[EF] Starting execution loop, initial frame " + PYCREPR(frame) );
    while(frame!= NULL)
    {
        DEBUG("[EF] Running frame " + PYCREPR(frame) + ", restricted: " + BSTR(PyEval_GetRestricted()));
        PyEval_EvalFrame( frame );
        DEBUG("[EF] Frame finished " + PYCREPR(frame) + ", restricted: " + BSTR(PyEval_GetRestricted()));
        // Check errors
        checkRuntimeError();
        // If there is any error, stop right away. Status will be
        // updated by the function above.
        if (m_terminated || hasError()) break;
        // Go to next frame, if any
        frame = frame->f_back;
        DEBUG("[EF] Next frame " + PYCREPR(frame))
    }
    return m_status;
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::checkRuntimeError
//=============================================================================
void SPELLexecutionFrame::checkRuntimeError()
{
    // Cleanup any previous error
    if (m_error)
    {
        delete m_error;
        m_error = NULL;
        m_errorLocation = "";
    }

    // Check state of Python API
    PyObject* err = PyErr_Occurred();
    if (err != NULL)
    {
        DEBUG("[EF] Some error occured");

        PyObject* ptype;
        PyObject* pvalue;
        PyObject* ptraceback;

        // Fetch the error information
        PyErr_Fetch( &ptype, &pvalue, &ptraceback );

        DEBUG("[EF] Error is " + PYREPR(pvalue));

        if (m_terminated)
        {
            DEBUG("[EF] Process terminated");
            // Continue right away if the exception is aborted/finished type
            if (Is_ExecutionAborted(ptype))
            {
                DEBUG("[EF] Found execution aborted exception");
                m_status = EXECUTION_ABORTED;
            }
            else if (Is_ExecutionTerminated(ptype))
            {
                DEBUG("[EF] Found execution terminated exception");
                m_status = EXECUTION_TERMINATED;
            }
            PyErr_Clear();
            return;
        }

        DEBUG("[EF] Execution loop aborted due to an error");
        m_status = EXECUTION_ERROR;
        m_error = SPELLpythonHelper::instance().errorToException( err, ptype, pvalue, ptraceback );

        // Get the error location
        PyTracebackObject* tb = (PyTracebackObject*) ptraceback;
        while(tb != NULL)
        {
            m_errorLocation = PYSTR(tb->tb_frame->f_code->co_filename) + ":" + ISTR(tb->tb_lineno);
            tb = tb->tb_next;
        }
    }
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::retrieveDiscardedNames
//=============================================================================
void SPELLexecutionFrame::retrieveDiscardedNames(PyFrameObject* frame)
{
	PyObject* itemList = PyDict_Keys(frame->f_globals);
	unsigned int numItems = PyList_Size(itemList);
	DEBUG("[E] Names to discard: " + ISTR(numItems) );
	for( unsigned int index = 0; index<numItems; index++)
	{
		PyObject* key = PyList_GetItem( itemList, index );
		m_discardedNames.insert( PYSSTR(key) );
	}
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::getCurrentLine()
//=============================================================================
const unsigned int SPELLexecutionFrame::getCurrentLine()
{
	return m_currentFrame->f_lineno;
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::canSkip
//=============================================================================
const bool SPELLexecutionFrame::canSkip()
{
    SPELLsafePythonOperations ops;
    int new_lineno = -1;

    bool isSimpleLine = getModel().isSimpleLine( getCurrentLine() );
    bool isBlockStart = getModel().isBlockStart( getCurrentLine() );
    bool lineAfterTryBlock = true;

    // Special treatment for try blocks
    bool isEndTryBlock = (getModel().isInTryBlock(m_currentFrame->f_lineno) && getModel().isTryBlockEnd(m_currentFrame->f_lineno));

    if (isEndTryBlock)
    {
		DEBUG("[E] Line " + ISTR( getCurrentLine() ) + " is the last one in a try-except block");
        // The current lnotab model will find the next available line number in the
        // current code. If there is no such line it returns -1.
        new_lineno = getModel().lineAfter( getModel().tryBlockEndLine( m_currentFrame->f_lineno ) );

        // If there is no line avaiable after, dont do the go next
        if (new_lineno == -1)
		{
    		DEBUG("[E] Cannot find line aftet the try-except block");
        	lineAfterTryBlock = false;
		}
    }

    // Special condition for try blocks
    if (isEndTryBlock && !lineAfterTryBlock) return false;

	return isSimpleLine || isBlockStart;
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::goNextLine
//=============================================================================
const bool SPELLexecutionFrame::goNextLine()
{
    bool controllerContinue = true;
    SPELLsafePythonOperations ops;
    int new_lineno = -1;

    // Special treatment for try blocks
    if (getModel().isInTryBlock(m_currentFrame->f_lineno) && getModel().isTryBlockEnd(m_currentFrame->f_lineno))
    {
		DEBUG("[E] Line " + ISTR(m_currentFrame->f_lineno) + " is the last one in a try-except block");
        // The current lnotab model will find the next available line number in the
        // current code. If there is no such line it returns -1.
        new_lineno = getModel().lineAfter( getModel().tryBlockEndLine( m_currentFrame->f_lineno ) );
    }
    else
    {
        // The current lnotab model will find the next available line number in the
        // current code. If there is no such line it returns -1.
        new_lineno = getModel().lineAfter( m_currentFrame->f_lineno );
    }


    // If there is no next line in this frame
    if (new_lineno == -1)
    {
    	// If we are already in the last line of the code block
		DEBUG("[E] No next line available in frame at " + PSTR(m_currentFrame));
    	if (getModel().getLastLine()==m_currentFrame->f_lineno)
    	{
    		// Get the last instruction address
    		int lastAddress = getModel().getLastAddress();
    		// Set the instruction pointer so that we make the return statement
    		lastAddress -= 3;
    		// If we are already in the last instruction set, do nothing
    		if (m_currentFrame->f_lasti != lastAddress)
    		{
        		DEBUG("[E] Set instruction to " + ISTR(lastAddress));
    			setNewLine( m_currentFrame->f_lineno, lastAddress );
    		}
    	}
		controllerContinue = false;
    }
    else
    {
        DEBUG("[E] Next line available in frame is " + ISTR(new_lineno));
        // If there is next line, try to find the corresponding instruction offset
        int new_lasti = getModel().offset(new_lineno);
        if ( new_lasti == -1 )
        {
            DEBUG("[E] No next instruction available in frame at " + PSTR(m_currentFrame));
			controllerContinue = false;
        }
        else
        {
            DEBUG("[E] Go next line " + ISTR(new_lineno) + " at " + ISTR(new_lasti));
			controllerContinue = setNewLine( new_lineno, new_lasti );
        }
    }

    return controllerContinue;
}


//=============================================================================
// METHOD    : SPELLexecutionFrame::goLabel
//=============================================================================
const bool SPELLexecutionFrame::goLabel( const std::string& label, bool report )
{
	SPELLsafePythonOperations ops;
    DEBUG("[E] Go-to label '" + label + "'")
    std::map<std::string,unsigned int> labels = m_model->getLabels();
    std::map<std::string,unsigned int>::const_iterator it = labels.find(label);
    if ( it == labels.end() )
    {
        return false;
    }
    else
    {
        return goLine( (*it).second, report );
    }
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::goLine
//=============================================================================
const bool SPELLexecutionFrame::goLine( const int& new_lineno, bool report )
{
	SPELLsafePythonOperations ops;
    DEBUG("[E] Go-to line " + ISTR(new_lineno) + " on model " + PSTR(m_model));
    int new_lasti = m_model->offset(new_lineno);
    if ( new_lasti == -1 )
    {
        return false;
    }
    DEBUG("[E] Go new line " + ISTR(new_lineno) + " at " + ISTR(new_lasti))
    if (report)
    {
    	SPELLexecutor::instance().getCIF().write("Jump to line " + ISTR(new_lineno), LanguageConstants::SCOPE_SYS );
    }
    return setNewLine( new_lineno, new_lasti );
}

//=============================================================================
// METHOD      : SPELLexecutionFrame::programmedGoto
//=============================================================================
bool SPELLexecutionFrame::programmedGoto( const int& frameLine )
{
    int target = m_model->getTargetLine(frameLine);
    bool gotoSuccess = false;
    if (target != -1)
    {
        DEBUG("[GOTO] Programmed jump at line " + ISTR(frameLine) + " to line " + ISTR(target));
        LOG_INFO("Goto " + ISTR(target));
        gotoSuccess = goLine(target, false);
    }
    return gotoSuccess;
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::runScript
//=============================================================================
void SPELLexecutionFrame::runScript( const std::string& script )
{
	SPELLsafePythonOperations ops;
	try
    {
    	PyCodeObject* code = compileScript( std::string(script) );
    	SPELLpythonHelper::instance().loadFramework();
    	DEBUG("[EF] Executing script on frame " + PYCREPR(m_currentFrame));
    	// We need to take into account the fast locals. First the copy the fast locals to normal locals,
    	// and after the execution we UPDATE the fast locals, so that the values get updated in case of
    	// value redefinition in the script command.
    	PyFrame_FastToLocals(m_currentFrame);
    	PyEval_EvalCode(code, m_currentFrame->f_globals, m_currentFrame->f_locals);
    	PyFrame_LocalsToFast(m_currentFrame,0);
        SPELLpythonHelper::instance().checkError();
    	// See model update its information
    	getModel().update();
    }
	catch (SPELLcoreException& ex)
	{
		// Reset the error data in Python layer. We do not want the interpreter to
		// think that there is an error in the procedure execution.
		PyErr_Print();
		PyErr_Clear();
		throw ex;
	}
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::eventCall
//=============================================================================
void SPELLexecutionFrame::eventCall( PyFrameObject* frame )
{
	DEBUG("[EF] Event call on frame " + PYCREPR(frame));
	// Notify the warmstart mechanism
	if (m_warmStart)
	{
		m_warmStart->notifyCall(frame);
	}
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::eventLine
//=============================================================================
void SPELLexecutionFrame::eventLine( PyFrameObject* frame )
{
	DEBUG("[EF] Event line on frame " + PYCREPR(frame));
	// Notify the warmstart mechanism
	if (m_warmStart)
	{
		// Warmstart data shall not be built/updated in the middle of a code block
		// Otherwise we may break things when doing the iterator construction on
		// a 'for' loop, for example.
		unsigned int lineno = frame->f_lineno;
		bool doNotify = getModel().isSimpleLine(lineno) || getModel().isBlockStart(lineno);
		if (doNotify)
		{
			DEBUG("[EF] Notifying line event to warmstart");
			m_warmStart->notifyLine();
		}
	}
	// See model update its information
	getModel().update();
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::eventReturn
//=============================================================================
void SPELLexecutionFrame::eventReturn( PyFrameObject* frame )
{
	DEBUG("[EF] Event return on frame " + PYCREPR(frame));
	// Notify the warmstart mechanism
	if (m_warmStart)
	{
		m_warmStart->notifyReturn();
	}
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::eventStage
//=============================================================================
void SPELLexecutionFrame::eventStage( PyFrameObject* frame )
{
	DEBUG("[EF] Event stage on frame " + PYCREPR(frame));
	// Notify the warmstart mechanism
	if (m_warmStart)
	{
		m_warmStart->notifyStage();
	}
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::updateCurrentFrame
//=============================================================================
void SPELLexecutionFrame::updateCurrentFrame( PyFrameObject* frame )
{
	if (m_currentFrame == frame) return;

    DEBUG("[EF] Update frame: current is " + PSTR(m_currentFrame) + ", new is " + PSTR(frame));

	// Create the set if initial names to be discarded when retrieving variables
	if  (m_discardedNames.size() == 0)
	{
		retrieveDiscardedNames(frame);
	}

	// Update SPELL language definitions if required
	// Definitions list will be NULL if there is no warmstart available
	if (m_definitions != NULL)
	{
		PyObject* globals = frame->f_globals;

		PyObject* regObj = PyDict_GetItemString(globals, "REGISTRY");
		if (regObj == NULL)
		{
			DEBUG("[EF] Copying SPELL definitions");
			unsigned int numKeys = PyList_Size(m_definitions);
			for( unsigned int count = 0; count < numKeys; count++)
			{
				PyObject* key = PyList_GetItem( m_definitions, count );
				assert( key != NULL );
				PyObject* obj = PyDict_GetItem( m_currentFrame->f_globals, key );
				if (obj != NULL)
				{
					//DEBUG("   Copying " + PYREPR(key))
					PyDict_SetItem( frame->f_globals, key, obj );
					Py_INCREF(key);
					Py_INCREF(obj);
				}
			}
			DEBUG("Frame " + PSTR(frame) + "," + PYSTR(frame->f_code->co_filename) + ":" + ISTR(frame->f_lineno));
		}
		else
		{
			DEBUG("[EF] No need to copy SPELL definitions");
		}
	}

	// Update SCDB and GDB instances in locals so that they are directly available in functions
	// Do this in functions only, not in main frame
	if (m_currentFrame != NULL)
	{
		PyFrame_FastToLocals(frame);

		PyObject* scdb = PyDict_GetItemString(m_currentFrame->f_locals, "SCDB");
		PyObject* gdb = PyDict_GetItemString(m_currentFrame->f_locals, "GDB");
		PyObject* proc = PyDict_GetItemString(m_currentFrame->f_locals, "PROC");
		PyObject* args = PyDict_GetItemString(m_currentFrame->f_locals, "ARGS");

		if (scdb) PyDict_SetItemString(frame->f_locals, "SCDB", scdb);
		if (gdb) PyDict_SetItemString(frame->f_locals, "GDB", gdb);
		if (proc) PyDict_SetItemString(frame->f_locals, "PROC", proc);
		if (args) PyDict_SetItemString(frame->f_locals, "ARGS", args);

		PyFrame_LocalsToFast(frame,0);
	}

	m_currentFrame = frame;
	Py_INCREF(m_currentFrame);
	std::string filename = PYSTR(frame->f_code->co_filename);
	std::string codename = PYSTR(frame->f_code->co_name);
	std::string code_id = filename + "-" + codename;
	bool watchEnabled = SPELLexecutor::instance().getConfiguration().getWatchEnabled();

	// Generate or reuse execution model
	ModelMap::iterator mit;
	mit = m_modelMap.find(code_id);
	if (mit == m_modelMap.end())
	{
		DEBUG("[EF] Creating model for " + code_id);
		m_model = new SPELLexecutionModel(filename,
				                          frame,
										  watchEnabled,
										  m_discardedNames);
		m_modelMap.insert( std::make_pair( code_id, m_model ));

		// Validate the data analyzed by the model
		std::string errors = m_model->validateGotos();
		if (errors != "")
		{
			DEBUG("[EF] WARNING missing gotos!");
			std::string msg1 = "WARNING: found wrong 'Goto()' sentences in this procedure";
			std::string msg2 = "Please check the code, the following target labels were not found: " + errors;
			LOG_WARN(msg1);
			LOG_WARN(msg2);
			SPELLexecutor::instance().getCIF().warning(msg1, LanguageConstants::SCOPE_SYS );
			SPELLexecutor::instance().getCIF().warning(msg2, LanguageConstants::SCOPE_SYS );
			SPELLexecutor::instance().pause();
		}
	}
	else
	{
		DEBUG("[EF] Reusing model for " + code_id)
		m_model = (*mit).second;
	}

	DEBUG("[EF] Using model " + PSTR(m_model) + " with id " + code_id)
	m_model->inScope();
	DEBUG("[EF] Update finished");
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::getModel()
//=============================================================================
SPELLexecutionModel& SPELLexecutionFrame::getModel( const std::string& code_id )
{
	ModelMap::iterator mit;
	mit = m_modelMap.find(code_id);
	if (mit != m_modelMap.end())
	{
		return *(mit->second);
	}
	throw SPELLcoreException("Cannot access model", "No such identifier: " + code_id );
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::filterDictUpdated
//=============================================================================
void SPELLexecutionFrame::filterDictUpdated()
{
	DEBUG("[EF] Reading filter definitions")
	PyObject* dict = PyDict_Copy(SPELLpythonHelper::instance().getMainDict());
	m_definitions = PyDict_Keys(dict);
	DEBUG("[EF] " + ISTR(PyList_Size(m_definitions)) + " definitions read")
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::setNewLine
//=============================================================================
const bool SPELLexecutionFrame::setNewLine( const int& new_lineno, const int& new_lasti )
{
	return SPELLpythonHelper::instance().setNewLine( m_currentFrame, new_lineno, new_lasti );
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::getTraceModel()
//=============================================================================
SPELLexecutionTrace& SPELLexecutionFrame::getTraceModel( const std::string& codeId )
{
	TraceMap::iterator it = m_traceMap.find(codeId);
	m_currentTraceModel = codeId;
	if ( it == m_traceMap.end() )
	{
		DEBUG("[CSTACK] Create trace model for " + codeId );
		m_traceMap.insert( std::make_pair( std::string(codeId), SPELLexecutionTrace() ) );
		return m_traceMap.find(codeId)->second;
	}
	else
	{
		return it->second;
	}
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::getCurrentTraceModel()
//=============================================================================
SPELLexecutionTrace& SPELLexecutionFrame::getCurrentTraceModel()
{
	return getTraceModel(m_currentTraceModel);
}

//=============================================================================
// METHOD    : SPELLexecutionFrame::resetExecutionTrace()
//=============================================================================
void SPELLexecutionFrame::resetExecutionTrace()
{
	m_currentTraceModel = "";
	m_traceMap.clear();
}
