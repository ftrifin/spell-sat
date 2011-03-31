// ################################################################################
// FILE       : SPELLdatabase.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of SPELL database
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
// Local includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLdatabase.H"
// System includes ---------------------------------------------------------

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLdatabase::SPELLdatabase()
//=============================================================================
SPELLdatabase::SPELLdatabase( const std::string& name, const std::string& filename, const std::string& defExt )
{
	m_name = name;
	m_filename = filename;
	m_defaultExt = defExt;
}

//=============================================================================
// DESTRUCTOR: SPELLdatabase::~SPELLdatabase()
//=============================================================================
SPELLdatabase::~SPELLdatabase()
{
	ValueMap::const_iterator it;
	for( it = m_values.begin(); it != m_values.end(); it++)
	{
		Py_XDECREF(it->second);
	}
	m_values.clear();
}

//=============================================================================
// METHOD: SPELLdatabase::keys()
//=============================================================================
std::vector<PyObject*> SPELLdatabase::keys()
{
	ValueMap::const_iterator it;
	std::vector<PyObject*> keys;
	for( it = m_values.begin(); it != m_values.end(); it++)
	{
		std::string key = it->first;
		PyObject* pyKey = strToKey(key);
		keys.push_back(pyKey);
	}
	return keys;
}

//=============================================================================
// METHOD: SPELLdatabase::size()
//=============================================================================
unsigned int SPELLdatabase::size()
{
	return m_values.size();
}

//=============================================================================
// METHOD: SPELLdatabase::get()
//=============================================================================
PyObject* SPELLdatabase::get( PyObject* key )
{
	std::string keyStr = keyToStr(key);
	ValueMap::iterator it = m_values.find(keyStr);
	if (it != m_values.end())
	{
		return it->second;
	}
	return NULL;
}

//=============================================================================
// METHOD: SPELLdatabase::set()
//=============================================================================
void SPELLdatabase::set( PyObject* key, PyObject* value )
{
	std::string keyStr = keyToStr(key);
	// When value is NULL, the item deletion mechanism is used
	if ((value == NULL)&&(m_values.find(keyStr) != m_values.end()))
	{
		ValueMap::iterator it = m_values.find(keyStr);
		Py_XDECREF(it->second);
		m_values.erase(it);
	}
	else
	{
		Py_INCREF(value);
		m_values.insert( std::make_pair( keyStr, value ) );
	}
}

//=============================================================================
// METHOD: SPELLdatabase::hasKey()
//=============================================================================
bool SPELLdatabase::hasKey( PyObject* key )
{
	std::string keyStr = keyToStr(key);
	ValueMap::iterator it = m_values.find(keyStr);
	return (it != m_values.end());
}

//=============================================================================
// METHOD: SPELLdatabase::create()
//=============================================================================
void SPELLdatabase::create()
{
	throw SPELLdatabaseError("create() not implemented");
}

//=============================================================================
// METHOD: SPELLdatabase::repr()
//=============================================================================
std::string SPELLdatabase::repr()
{
	std::string result = "{";
	ValueMap::const_iterator it;
	for( it = m_values.begin(); it != m_values.end(); it++)
	{
		std::string key = it->first;
		PyObject* pyKey = strToKey(key);
		if (result != "{") result += ",";
		result += PYREPR(pyKey) + ":" + PYREPR(it->second);
	}
	result += "}";
	return result;
}

//=============================================================================
// METHOD: SPELLdatabase::str()
//=============================================================================
std::string SPELLdatabase::str()
{
	return repr();
}

//=============================================================================
// METHOD: SPELLdatabase::commit()
//=============================================================================
void SPELLdatabase::commit()
{
	throw SPELLdatabaseError("commit() not implemented");
}

//=============================================================================
// METHOD: SPELLdatabase::load()
//=============================================================================
void SPELLdatabase::load()
{
	throw SPELLdatabaseError("load() not implemented");
}

//=============================================================================
// METHOD: SPELLdatabase::reload()
//=============================================================================
void SPELLdatabase::reload()
{
	throw SPELLdatabaseError("reload() not implemented");
}

//=============================================================================
// METHOD: SPELLdatabase::id()
//=============================================================================
std::string SPELLdatabase::id()
{
	return m_name;
}

//=============================================================================
// METHOD: SPELLdatabase::keyToStr()
//=============================================================================
std::string SPELLdatabase::keyToStr( PyObject* key )
{
	std::string keyStr = PYSTR(PyObject_Str(key));
	if (PyLong_Check(key)||PyInt_Check(key))
	{
		keyStr = "%I%" + keyStr;
	}
	else if (PyFloat_Check(key))
	{
		keyStr = "%F%" + keyStr;

	}
	return keyStr;
}

//=============================================================================
// METHOD: SPELLdatabase::strToKey()
//=============================================================================
PyObject* SPELLdatabase::strToKey( const std::string& keyStr )
{
	PyObject* pyKey = NULL;
	if (keyStr.find("%I%") != std::string::npos )
	{
		std::string theKey = keyStr.substr(3,keyStr.size()-3);
		pyKey = PyLong_FromString( (char*)theKey.c_str(), NULL,0);
	}
	else if (keyStr.find("%F%") != std::string::npos )
	{
		std::string theKey = keyStr.substr(3,keyStr.size()-3);
		pyKey = PyFloat_FromString( SSTRPY(theKey), NULL);
	}
	else
	{
		pyKey = SSTRPY(keyStr);
	}
	return pyKey;
}
