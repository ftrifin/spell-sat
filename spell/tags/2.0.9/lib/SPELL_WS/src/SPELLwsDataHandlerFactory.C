// ################################################################################
// FILE       : SPELLwsDataHandlerFactory.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler factory
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
#include "SPELL_WS/SPELLwsDataHandlerFactory.H"
#include "SPELL_WS/SPELLwsClassDataHandler.H"
#include "SPELL_WS/SPELLwsInstanceDataHandler.H"
#include "SPELL_WS/SPELLwsDictDataHandler.H"
#include "SPELL_WS/SPELLwsListDataHandler.H"
#include "SPELL_WS/SPELLwsObjectDataHandler.H"
#include "SPELL_WS/SPELLwsNoneDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLerror.H"
// System includes ---------------------------------------------------------

//=============================================================================
// STATIC : SPELLwsDataHandlerFactory::createDataHandler()
//=============================================================================
SPELLwsDataHandler* SPELLwsDataHandlerFactory::createDataHandler( PyObject* object )
{
	assert(object != NULL);

	SPELLwsDataHandler* handler = NULL;

	if (PyClass_Check(object))
	{
		handler = new SPELLwsClassDataHandler(object);
	}
	else if (PyInstance_Check(object))
	{
		handler = new SPELLwsInstanceDataHandler(object);
	}
	else if (PyDict_Check(object))
	{
		handler = new SPELLwsDictDataHandler(object);
	}
	else if (PyList_Check(object))
	{
		handler = new SPELLwsListDataHandler(object);
	}
	else if ( Py_None == object )
	{
		handler = new SPELLwsNoneDataHandler();
	}
	else if (PyLong_Check(object) || PyInt_Check(object) || PyString_Check(object) || PyFloat_Check(object) || PyCode_Check(object) )
	{
		handler = new SPELLwsObjectDataHandler(object);
	}
	else
	{
		throw SPELLcoreException("Cannot create handler", "Unknown handler type: " + PYREPR(object));
	}
	return handler;
}

//=============================================================================
// STATIC : SPELLwsDataHandlerFactory::createDataHandler()
//=============================================================================
SPELLwsDataHandler* SPELLwsDataHandlerFactory::createDataHandler( SPELLwsDataCode code )
{
	SPELLwsDataHandler* handler = NULL;

	switch(code)
	{
	case DATA_BYTECODE:
	case DATA_GENERIC:
		handler = new SPELLwsObjectDataHandler(NULL);
		break;
	case DATA_CLASS:
		handler = new SPELLwsClassDataHandler(NULL);
		break;
	case DATA_INSTANCE:
		handler = new SPELLwsInstanceDataHandler(NULL);
		break;
	case DATA_DICTIONARY:
		handler = new SPELLwsDictDataHandler(NULL);
		break;
	case DATA_LIST:
		handler = new SPELLwsListDataHandler(NULL);
		break;
	case DATA_NONE:
		handler = new SPELLwsNoneDataHandler();
		break;
	default:
		throw SPELLcoreException("Cannot create handler", "Unknown handler type: " + ISTR(code));
	}

	return handler;
}
