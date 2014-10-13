// ################################################################################
// FILE       : SPELLdatabase.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of SPELL database
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
// Local includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLdatabase.H"
#include "SPELL_WRP/SPELLconstants.H"
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
	m_dict = PyDict_New();
	Py_XINCREF(m_dict);
}

//=============================================================================
// DESTRUCTOR: SPELLdatabase::~SPELLdatabase()
//=============================================================================
SPELLdatabase::~SPELLdatabase()
{
	clearValues();
	Py_XDECREF(m_dict);
}

//=============================================================================
// METHOD: SPELLdatabase::clearValues()
//=============================================================================
void SPELLdatabase::clearValues()
{
	PyObject* keys = PyDict_Keys(m_dict);
	Py_XINCREF(keys);
	int numKeys = PyList_Size(keys);
	for( int index = 0; index < numKeys; index++)
	{
		PyObject* key = PyList_GetItem(keys,index);
		Py_XINCREF(key);
		PyObject* value = PyDict_GetItem(m_dict, key);
		PyDict_DelItem(m_dict,key);
		Py_XDECREF(value);
		Py_XDECREF(key);
	}
	Py_XDECREF(keys);
	PyDict_Clear(m_dict);
	m_formats.clear();
}

//=============================================================================
// METHOD: SPELLdatabase::keys()
//=============================================================================
std::vector<PyObject*> SPELLdatabase::keys()
{
	std::vector<PyObject*> keys;
	PyObject* pyKeys = PyDict_Keys(m_dict);
	Py_XINCREF(pyKeys);
	int numKeys = PyList_Size(pyKeys);
	for( int index = 0; index < numKeys; index++)
	{
		PyObject* key = PyList_GetItem(pyKeys,index);
		Py_XINCREF(key);
		keys.push_back(key);
	}
	Py_XDECREF(pyKeys);
	return keys;
}

//=============================================================================
// METHOD: SPELLdatabase::keysStr()
//=============================================================================
std::vector<std::string> SPELLdatabase::keysStr()
{
	std::vector<std::string> keys;
	PyObject* pyKeys = PyDict_Keys(m_dict);
	Py_XINCREF(pyKeys);
	int numKeys = PyList_Size(pyKeys);
	for( int index = 0; index < numKeys; index++)
	{
		PyObject* key = PyList_GetItem(pyKeys,index);
		Py_XINCREF(key);
		keys.push_back( PYSSTR(key) );
		Py_XDECREF(key);
	}
	Py_XDECREF(pyKeys);
	return keys;
}

//=============================================================================
// METHOD: SPELLdatabase::size()
//=============================================================================
unsigned int SPELLdatabase::size()
{
	return PyDict_Size(m_dict);
}

//=============================================================================
// METHOD: SPELLdatabase::get()
//=============================================================================
PyObject* SPELLdatabase::get( PyObject* key )
{
	return PyDict_GetItem(m_dict,key);
}

//=============================================================================
// METHOD: SPELLdatabase::getStr()
//=============================================================================
std::string SPELLdatabase::getStr( const std::string& key )
{
	PyObject* pyKey = SSTRPY(key);
	std::string str = "None";
	if (PyDict_Contains(m_dict,pyKey))
	{
		FormatMap::iterator fit = m_formats.find(key);
		if (fit == m_formats.end())
		{
			str = PYSSTR( PyDict_GetItem(m_dict,pyKey));
		}
		else
		{
			int intValue = PyLong_AsLongLong(PyDict_GetItem(m_dict,pyKey));
			if (fit->second == LanguageConstants::HEX)
			{
				str = SPELLutils::hexstr(intValue);
				str = SPELLutils::toUpper(str);
				str = "0x" + str;
			}
			else if (fit->second == LanguageConstants::BIN)
			{
				str = SPELLutils::binstr(intValue);
				str = "0b" + str;
			}
			else if (fit->second == LanguageConstants::OCT)
			{
				str = SPELLutils::octstr(intValue);
				str = "0" + str;
			}
			else
			{
				str = ISTR(intValue);
			}
		}
	}
	return str;
}

//=============================================================================
// METHOD: SPELLdatabase::set()
//=============================================================================
void SPELLdatabase::set( PyObject* key, PyObject* value, const std::string& format )
{
	std::string keyStr = keyToStr(key);
	// When value is NULL, the item deletion mechanism is used
	if ((value == NULL)&&(PyDict_Contains(m_dict,key)))
	{
		FormatMap::iterator fit = m_formats.find(keyStr);
		Py_XDECREF(PyDict_GetItem(m_dict,key));
		PyDict_DelItem(m_dict,key);
		if (fit != m_formats.end())
		{
			m_formats.erase(fit);
		}
	}
	else
	{
		if (format != "")
		{
			m_formats.insert( std::make_pair(keyStr,format));
		}
		Py_INCREF(value);
		PyDict_SetItem(m_dict,key,value);
	}
}

//=============================================================================
// METHOD: SPELLdatabase::hasKey()
//=============================================================================
bool SPELLdatabase::hasKey( PyObject* key )
{
	return PyDict_Contains(m_dict,key);
}

//=============================================================================
// METHOD: SPELLdatabase::create()
//=============================================================================
void SPELLdatabase::create()
{
	THROW_EXCEPTION("Cannot create database", "Not implemented", SPELL_ERROR_LANGUAGE);
}

//=============================================================================
// METHOD: SPELLdatabase::repr()
//=============================================================================
std::string SPELLdatabase::repr()
{
	return PYREPR(m_dict);
}

//=============================================================================
// METHOD: SPELLdatabase::str()
//=============================================================================
std::string SPELLdatabase::str()
{
	return PYSSTR(m_dict);
}

//=============================================================================
// METHOD: SPELLdatabase::commit()
//=============================================================================
void SPELLdatabase::commit()
{
	THROW_EXCEPTION("Cannot commit database", "Not implemented", SPELL_ERROR_LANGUAGE);
}

//=============================================================================
// METHOD: SPELLdatabase::load()
//=============================================================================
void SPELLdatabase::load()
{
	THROW_EXCEPTION("Cannot load database", "Not implemented", SPELL_ERROR_LANGUAGE);
}

//=============================================================================
// METHOD: SPELLdatabase::reload()
//=============================================================================
void SPELLdatabase::reload()
{
	THROW_EXCEPTION("Cannot reload database", "Not implemented", SPELL_ERROR_LANGUAGE);
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
