// ################################################################################
// FILE       : SPELLvariableMonitor.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the procedure variable monitor
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLvariableMonitor.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"

// GLOBALS ////////////////////////////////////////////////////////////////////
#define EMPTY_STRING "<empty>"

//=============================================================================
// CONSTRUCTOR : SPELLvariableMonitor::SPELLvariableMonitor
//=============================================================================
SPELLvariableMonitor::SPELLvariableMonitor( SPELLvariableChangeListener* listener,
		                                    PyFrameObject* frame,
		                                    std::set<std::string>& initialVariables)
: m_frame(frame),
  m_listener(listener),
  m_initialVariables(initialVariables)
{
	DEBUG("[VM] Created frame for code " + PYSTR(m_frame->f_code->co_name));
}

//=============================================================================
// DESTRUCTOR : SPELLvariableMonitor::~SPELLvariableMonitor
//=============================================================================
SPELLvariableMonitor::~SPELLvariableMonitor()
{
	// Do not delete! borrowed reference
	m_frame = NULL;
	// Do not delete! borrowed reference
	m_listener = NULL;
	m_variables.clear();
	DEBUG("[VM] Destroyed");
}

//=============================================================================
// METHOD: SPELLvariableMonitor::retrieveGlobalVariables()
//=============================================================================
void SPELLvariableMonitor::retrieveGlobalVariables(std::vector<SPELLvarInfo>& vars,
												   std::set<std::string> locals)
{
	DEBUG("[VM] Retrieve Globals");

	/*
	 * Once we get the bottom stack frame, we have to iterate over all the keys
	 * in the globals dictionary, and filter them agains the m_initialVariables
	 */
	PyObject* dict = m_frame->f_globals;
	PyObject* itemList = PyDict_Keys(dict);
	unsigned int numItems = PyList_Size(itemList);
	for( unsigned int index = 0; index<numItems; index++)
	{
		PyObject* key = PyList_GetItem( itemList, index );
		std::string varName = PYSSTR(key);

		// Do the following check just when the considered variables are not internal databases
		if ( (varName != DatabaseConstants::SCDB) &&
			 (varName != DatabaseConstants::GDB)  &&
			 (varName != DatabaseConstants::PROC) &&
			 (varName != DatabaseConstants::ARGS) &&
			 (varName != DatabaseConstants::IVARS))
		{
			/* If they key is contained in the initial variables set, then ignore it */
			if (m_initialVariables.find(varName) != m_initialVariables.end())
			{
				continue;
			}
		}
		/* If a variable with the same name has been retrieved in the local scope
		 * then it must be ignored */
		if (locals.find(varName) != locals.end())
		{
			continue;
		}

		// Ignore internal flags
		if (varName == "__USERLIB__") continue;

		PyObject* object = PyDict_GetItem( dict, key );

		if (!SPELLpythonHelper::instance().isInstance(object, "Database", "spell.lib.adapter.databases.database"))
		{
			if (PyCallable_Check(object)) continue;
			if (PyClass_Check(object)) continue;
			if (PyModule_Check(object)) continue;
			if (PyInstance_Check(object)) continue;
		}
		DEBUG("[VM] Processing " + varName);

		std::string type = PYSSTR( PyObject_Type(object) );
		std::string value = PYREPR( object );

		DEBUG("[VM] Type      : " + type);
		DEBUG("[VM] Value     : " + value);
		DEBUG("[VM] Global    : " + BSTR(true));
		DEBUG("[VM] Registered: " + BSTR(isRegistered(PYSSTR(key))));

		// Mark empty values (empty strings) as "<empty>"
		if (value == "") value = EMPTY_STRING;

		vars.push_back( SPELLvarInfo(varName, type, value, true, isRegistered(PYSSTR(key))) );
	}
}

//=============================================================================
// METHOD: SPELLvariableMonitor::retrieveLocalVariables()
//=============================================================================
void SPELLvariableMonitor::retrieveLocalVariables(std::vector<SPELLvarInfo>& vars)
{
	DEBUG("[VM] Retrieve Locals");

	/*
	 * Bottom stack frame is discarded,
	 * as globals and locals are the same dictionary
	 */
	if (m_frame->f_back == NULL) return;

	/*
	 * Get the names defined in the current code, including arguments
	 */
	std::vector<std::string> varNames = retrieveNames();

	/*
	 * Iterate over the locals dictionary, retrieving the names contained in
	 * varNames
	 */
	PyFrame_FastToLocals(m_frame);
	PyObject* dict = m_frame->f_locals;
	DEBUG("[VM] Frame: " + PYCREPR(m_frame));
	for( unsigned int index = 0; index< varNames.size(); index++)
	{
		std::string varName = varNames[index];
		PyObject* pyVarName = SSTRPY(varName);
		if (PyDict_Contains( dict, pyVarName ))
		{
			PyObject* object = PyDict_GetItem( dict, pyVarName );

			if (!SPELLpythonHelper::instance().isInstance(object, "Database", "spell.lib.adapter.databases.database"))
			{
				if (PyCallable_Check(object)) continue;
				if (PyClass_Check(object)) continue;
				if (PyModule_Check(object)) continue;
				if (PyInstance_Check(object)) continue;
			}
			DEBUG("[VM] Processing " + varName);
			std::string type = PYSSTR( PyObject_Type(object) );
			DEBUG("[VM] Type      : " + type);
			std::string value = PYREPR( object );
			DEBUG("[VM] Value     : " + value);
			DEBUG("[VM] Global    : " + BSTR(false));
			DEBUG("[VM] Registered: " + BSTR(isRegistered(varName)));

			// Mark empty values (empty strings) as "<empty>"
			if (value == "") value = EMPTY_STRING;

			vars.push_back( SPELLvarInfo(varName, type, value, false, isRegistered(varName)) );
		}
	}
	PyFrame_LocalsToFast(m_frame,0);
}

//=============================================================================
// METHOD: SPELLvariableMonitor::retrieveNames()
//=============================================================================
std::vector<std::string> SPELLvariableMonitor::retrieveNames()
{
	std::vector<std::string> varNames;

	PyObject* varList = m_frame->f_code->co_names;
	DEBUG("[VM] CO_NAMES   : " + PYREPR(varList));
	unsigned int numVars = PyTuple_Size(varList);

	/*
	 * co_varnames contains the names of the local variables
	 * (starting with the argument names)
	 */
	varList = m_frame->f_code->co_varnames;
	DEBUG("[VM] CO_VARNAMES: " + PYREPR(varList));
	numVars = PyTuple_Size(varList);
	for( unsigned int index = 0; index<numVars; index++)
	{
		PyObject* varName = PyTuple_GetItem( varList, index );
		varNames.push_back( PYSSTR( varName ) );
	}

	varList = m_frame->f_code->co_freevars;
	DEBUG("[VM] CO_FREEVARS : " + PYREPR(varList));

	varList = m_frame->f_code->co_cellvars;
	DEBUG("[VM] CO_CELLVARS : " + PYREPR(varList));

	return varNames;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getAllVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableMonitor::getAllVariables()
{
	SPELLsafePythonOperations ops("SPELLvariableMonitor::getAllVariables()");
	std::vector<SPELLvarInfo> vars;
	// We need to retrieve the function arguments and other locals, which are only stored in
	// fast locals by default
	PyFrame_FastToLocals(m_frame);

	/*
	 * Get locals
	 */
	retrieveLocalVariables(vars);

	/*
	 * Get the name of the found locals to filter them while retrieving
	 * the locals
	 */
	std::set<std::string> locals;
	/*
	 * TODO maybe we should filter globals variables whose names are the same as
	 * variables found in the local scope
	 * To do this, uncomment the following loop
	 */
	/*for (unsigned int index = 0; index< vars.size(); index++)
	{
		SPELLvarInfo var = vars[index];
		locals.insert(var.varName);
	}*/

	/*
	 * Get globals
	 */
	retrieveGlobalVariables(vars, locals);

	return vars;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getRegisteredVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableMonitor::getRegisteredVariables()
{
	std::vector<SPELLvarInfo> vars;
	VarMap::iterator it;
	for( it = m_variables.begin(); it != m_variables.end(); it++)
	{
		vars.push_back( it->second );
		DEBUG("[VM] Registered variable: " + it->second.varName );
	}
	return vars;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getAllVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableMonitor::getGlobalVariables()
{
	SPELLsafePythonOperations ops("SPELLvariableMonitor::getGlobalVariables()");
	std::vector<SPELLvarInfo> vars;

	std::set<std::string> locals;

	retrieveGlobalVariables( vars, locals );

	return vars;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getAllVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableMonitor::getLocalVariables()
{
	SPELLsafePythonOperations ops("SPELLvariableMonitor::getLocalVariables()");
	std::vector<SPELLvarInfo> vars;

	// We need to retrieve the function arguments and other locals, which are only stored in
	// fast locals by default
	PyFrame_FastToLocals(m_frame);

	retrieveLocalVariables( vars );

	return vars;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getAllVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableMonitor::getRegisteredGlobalVariables()
{
	std::vector<SPELLvarInfo> vars;
	VarMap::iterator it;
	for( it = m_variables.begin(); it != m_variables.end(); it++)
	{
		if (it->second.isGlobal)
		{
			vars.push_back( it->second );
			DEBUG("[VM] Registered variable: " + it->second.varName );
		}
	}
	return vars;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getAllVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableMonitor::getRegisteredLocalVariables()
{
	std::vector<SPELLvarInfo> vars;
	VarMap::iterator it;
	for( it = m_variables.begin(); it != m_variables.end(); it++)
	{
		if (!it->second.isGlobal)
		{
			vars.push_back( it->second );
			DEBUG("[VM] Registered variable: " + it->second.varName );
		}
	}
	return vars;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::registerVariable()
//=============================================================================
bool SPELLvariableMonitor::registerVariable( SPELLvarInfo& var )
{
	SPELLsafePythonOperations ops("SPELLvariableMonitor::registerVariable()");

	if ((m_frame->f_globals)&&(var.isGlobal))
	{
		var.varValue = PYSSTR( PyDict_GetItemString( m_frame->f_globals, var.varName.c_str() ));
		if (var.varValue == "") var.varValue = EMPTY_STRING;
		var.varType = PYSSTR( PyObject_Type( PyDict_GetItemString( m_frame->f_globals, var.varName.c_str() )) );
		var.isGlobal = true;
		var.isRegistered = true;
		m_variables.insert( std::make_pair( var.varName, var ) );
		DEBUG("[VM] Registered global variable: " + var.varName + ", current value: " + var.varValue );
		return true;
	}
	else if ((m_frame->f_locals)&&(!var.isGlobal))
	{
		// We need to retrieve the function arguments and other locals, which are only stored in
		// fast locals by default
		PyFrame_FastToLocals(m_frame);
		var.varValue = PYSSTR( PyDict_GetItemString( m_frame->f_locals, var.varName.c_str() ));
		if (var.varValue == "") var.varValue = EMPTY_STRING;
		var.varType = PYSSTR( PyObject_Type( PyDict_GetItemString( m_frame->f_locals, var.varName.c_str() )) );
		var.isGlobal = true;
		var.isRegistered = true;
		m_variables.insert( std::make_pair( var.varName, var ) );
		DEBUG("[VM] Registered local variable: " + var.varName + ", current value: " + var.varValue );
		return true;
	}
	var.varValue = "???";
	var.varType = "???";
	var.isRegistered = false;
	DEBUG("[VM] No such variable: " + var.varName);
	return false;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::unregisterVariable()
//=============================================================================
void SPELLvariableMonitor::unregisterVariable( SPELLvarInfo& var )
{
	VarMap::iterator it = m_variables.find(var.varName);
	if (it != m_variables.end())
	{
		m_variables.erase(it);
		DEBUG("[VM] Unregistered variable: " + var.varName );
		var.isRegistered = false;
	}
}

//=============================================================================
// METHOD: SPELLvariableMonitor::changeVariable()
//=============================================================================
void SPELLvariableMonitor::changeVariable( SPELLvarInfo& var )
{
	SPELLsafePythonOperations ops("SPELLvariableMonitor::changeVariable()");

	DEBUG("[VM] Request changing variable " + var.varName);

	// Evaluate the value for the variable
	PyObject* value = NULL;
	// If varValue is '<empty>' or empty string, do not try to evaluate it,
	// directly assign Python empty string
	if ((var.varValue == EMPTY_STRING)||(var.varValue == ""))
	{
		value = STRPY("");
	}
	else
	{
		// Build assignment expression. We need to check first, if there
		// are double quotes, convert them to single quotes
		SPELLutils::replace(var.varValue, "\"", "'");
		DEBUG("[VM] Evaluating value expression: " + var.varValue );
		// Check value correctness and evaluate it
		value = SPELLpythonHelper::instance().eval(var.varValue, false);
	}

	if ((m_frame->f_globals)&&(var.isGlobal))
	{
		DEBUG("[VM] Setting " + var.varName + " to " + PYREPR(value) + " in globals");
		PyDict_SetItemString( m_frame->f_globals, var.varName.c_str(), value );
	}
	else if ((m_frame->f_locals)&&(!var.isGlobal))
	{
		DEBUG("[VM] Setting " + var.varName + " to " + PYREPR(value) + " in locals");
		// Update locals from fast locals first
		PyFrame_FastToLocals(m_frame);
		PyDict_SetItemString( m_frame->f_locals, var.varName.c_str(), value );
		PyFrame_LocalsToFast(m_frame,0);
	}
	var.varValue = PYSSTR(value);
	if (var.varValue == "") var.varValue = EMPTY_STRING;

	// Update the variable if it is registered, and notify to listeners
	VarMap::iterator it = m_variables.find(var.varName);
	if (it != m_variables.end())
	{
		std::vector<SPELLvarInfo> changed;
		it->second.varValue = var.varValue;
		DEBUG("[VM] Variable changed by user: " + var.varName + ", current value: " + var.varValue );
		changed.push_back(it->second);
		m_listener->variableChanged( changed );
	}
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getVariableRef
//=============================================================================
PyObject* SPELLvariableMonitor::getVariableRef( const std::string& name )
{
	if (PyDict_Contains(m_frame->f_globals, SSTRPY(name)))
	{
		return PyDict_GetItemString(m_frame->f_globals, name.c_str());
	}
	return NULL;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::unregisterAll()
//=============================================================================
void SPELLvariableMonitor::unregisterAll()
{
	m_variables.clear();
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getVariable()
//=============================================================================
void SPELLvariableMonitor::getVariable( SPELLvarInfo& var )
{
	VarMap::iterator it = m_variables.find(var.varName);
	if (it != m_variables.end())
	{
		var.varValue = it->second.varValue;
		if (var.varValue == "") var.varValue = EMPTY_STRING;
		var.varType = it->second.varType;
		var.isGlobal = it->second.isGlobal;
		var.isRegistered = true;
	}
	else
	{
		var.varValue = "???";
		var.varType = "???";
		var.isGlobal = false;
		var.isRegistered = false;
	}
}

//=============================================================================
// METHOD: SPELLvariableMonitor::isRegistered()
//=============================================================================
bool SPELLvariableMonitor::isRegistered( const std::string& varName )
{
	VarMap::iterator it = m_variables.find(varName);
	return (it != m_variables.end());
}

//=============================================================================
// METHOD: SPELLvariableMonitor::analyze()
//=============================================================================
void SPELLvariableMonitor::analyze()
{
	std::vector<SPELLvarInfo> changed;
	bool copyFast = true;

	VarMap::iterator it;
	for( it = m_variables.begin(); it != m_variables.end(); it++ )
	{
		std::string varName = it->first;
		std::string currentValue = "";
		std::string lastValue = "";

		if ( (it->second.isGlobal) && (m_frame->f_globals) )
		{
			PyObject* pyCurrentValue = PyDict_GetItemString( m_frame->f_globals, varName.c_str() );
			currentValue = PYSSTR(pyCurrentValue);
			lastValue = it->second.varValue;
		}
		else if ( (!it->second.isGlobal) && (m_frame->f_locals) )
		{
			if (copyFast)
			{
				copyFast = false;
				// We need to retrieve the function arguments and other locals, which are only stored in
				// fast locals by default
				PyFrame_FastToLocals(m_frame);
			}
			PyObject* pyCurrentValue = PyDict_GetItemString( m_frame->f_locals, varName.c_str() );
			currentValue = PYSSTR(pyCurrentValue);
			lastValue = it->second.varValue;
		}

		if (lastValue != currentValue)
		{
			if (currentValue == "") currentValue == EMPTY_STRING;
			DEBUG("[VM] Variable change: " + varName + ", current value: " + currentValue + ", previous: " + it->second.varValue );
			it->second.varValue = currentValue;
			changed.push_back(it->second);
		}
	}
	if (changed.size()>0)
	{
		m_listener->variableChanged( changed );
	}
}
