// ################################################################################
// FILE       : SPELLpyDatabaseMethods.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of database Python bindings
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
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Local includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLpyDatabaseObject.H"
#include "SPELL_SDB/SPELLdatabaseFactory.H"
// System includes ---------------------------------------------------------

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////


//============================================================================
// FUNCTION        : SPELLpyDatabase_Init
//============================================================================
int SPELLpyDatabase_Init( PyDatabaseObject* self, PyObject* args, PyObject* kwds )
{
	std::string name = "";
	std::string file = "";
	std::string ext = "";
	std::string type = self->ob_type->tp_name;
	if (PyTuple_Size(args)!=3)
	{
		THROW_SYNTAX_EXCEPTION_NR("Cannot initialize database", "Expected database name, file and default extension");
		return -1;
	}
	PyObject* pyName = PyTuple_GetItem(args,0);
	PyObject* pyFile = PyTuple_GetItem(args,1);
	PyObject* pyExt  = PyTuple_GetItem(args,2);
	if ((!PyString_Check(pyName))||(!PyString_Check(pyName))||(!PyString_Check(pyName)))
	{
		THROW_SYNTAX_EXCEPTION_NR("Cannot initialize database", "All arguments shall be strings");
		return -1;
	}
	name = PYSTR( pyName );
	file = PYSTR( pyFile );
	ext  = PYSTR( pyExt );
	self->__pdb = SPELLdatabaseFactory::instance().createDatabase(type, name, file, ext);
	if (self->__pdb == NULL)
	{
		THROW_RUNTIME_EXCEPTION_NR("Cannot initialize database", "Unsupported type '" + type + "'");
		return -1;
	}
    return 0;
}

//============================================================================
// FUNCTION        : SPELLpyDatabase_Dealloc
//============================================================================
void SPELLpyDatabase_Dealloc( PyDatabaseObject* self )
{
	delete self->__pdb;
	self->__pdb = NULL;
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : SPELLpyDatabase_New
//============================================================================
PyObject* SPELLpyDatabase_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyDatabaseObject* self;
    self = (PyDatabaseObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Create
//============================================================================
PyObject* SPELLpyDatabase_Create( PyObject* self, PyObject* args )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			((PyDatabaseObject*)self)->__pdb->create();
			Py_RETURN_NONE;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot create database", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot create database", "Unknown processing error");
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot create database", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Load
//============================================================================
PyObject* SPELLpyDatabase_Load( PyObject* self, PyObject* args )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			((PyDatabaseObject*)self)->__pdb->load();
			Py_RETURN_NONE;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot load database", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot load database", "Unknown processing error");
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot load database", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Reload
//============================================================================
PyObject* SPELLpyDatabase_Reload( PyObject* self, PyObject* args )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			((PyDatabaseObject*)self)->__pdb->reload();
			Py_RETURN_NONE;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot reload database", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot reload database", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot reload database", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Id
//============================================================================
PyObject* SPELLpyDatabase_Id( PyObject* self, PyObject* args )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			std::string id = ((PyDatabaseObject*)self)->__pdb->id();
			PyObject* pyId = SSTRPY(id);
			Py_INCREF(pyId);
			return pyId;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database name", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database name", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot get database name", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Commit
//============================================================================
PyObject* SPELLpyDatabase_Commit( PyObject* self, PyObject* args )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			((PyDatabaseObject*)self)->__pdb->commit();
			Py_RETURN_NONE;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot commit database", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot create database", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot commit database", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Set
//============================================================================
PyObject* SPELLpyDatabase_Set( PyObject* self, PyObject* args )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			if (PyTuple_Size(args)!=2)
			{
				THROW_SYNTAX_EXCEPTION("Cannot set database item", "Expected a key name and a value");
			}
			PyObject* key = PyTuple_GetItem(args,0);
			PyObject* value = PyTuple_GetItem(args,1);
			if ((!PyString_Check(key))&&(!PyLong_Check(key))&&(!PyInt_Check(key))&&(!PyFloat_Check(key)))
			{
				THROW_SYNTAX_EXCEPTION("Cannot set database item", "Key must be string or number");
			}
			((PyDatabaseObject*)self)->__pdb->set(key,value);
			Py_RETURN_NONE;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot set database item", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot set database item", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot set database item", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Get
//============================================================================
PyObject* SPELLpyDatabase_Get( PyObject* self, PyObject* args )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			if (PyTuple_Size(args)!=1)
			{
				THROW_SYNTAX_EXCEPTION("Cannot get database item", "Expected a key name");
			}
			PyObject* key = PyTuple_GetItem(args,0);
			if ((!PyString_Check(key))&&(!PyLong_Check(key))&&(!PyInt_Check(key))&&(!PyFloat_Check(key)))
			{
				THROW_SYNTAX_EXCEPTION("Cannot get database item", "Key must be a string or number");
			}
			PyObject* value = ((PyDatabaseObject*)self)->__pdb->get(key);
			return value;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database item", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database item", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot get database item", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Keys
//============================================================================
PyObject* SPELLpyDatabase_Keys( PyObject* self, PyObject* args )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			std::vector<PyObject*> keys = ((PyDatabaseObject*)self)->__pdb->keys();
			PyObject* list = PyList_New(keys.size());
			Py_INCREF(list);
			std::vector<PyObject*>::const_iterator it;
			unsigned int index = 0;
			for( it = keys.begin(); it != keys.end(); it++ )
			{
				PyList_SetItem(list,index,*it);
				index++;
			}
			return list;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database keys", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database keys", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot get database keys", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Repr
//============================================================================
PyObject* SPELLpyDatabase_Repr( PyObject* self )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			std::string repr = ((PyDatabaseObject*)self)->__pdb->repr();
			PyObject* pyRepr = SSTRPY(repr);
			Py_INCREF(pyRepr);
			return pyRepr;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database representation", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database representation", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot get database representation", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_Str
//============================================================================
PyObject* SPELLpyDatabase_Str( PyObject* self )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			std::string str = ((PyDatabaseObject*)self)->__pdb->str();
			PyObject* pyStr = SSTRPY(str);
			Py_INCREF(pyStr);
			return pyStr;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database representation", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get database representation", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION("Cannot get database representation", "Database type not supported");
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_DictLength
//============================================================================
Py_ssize_t SPELLpyDatabase_DictLength( PyObject* self )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			return ((PyDatabaseObject*)self)->__pdb->size();
		}
		catch(SPELLdatabaseError& err)
		{
	        THROW_RUNTIME_EXCEPTION_NR( "Cannot get database size", err.getError() );
			return -1;
		}
		catch(...)
		{
	        THROW_RUNTIME_EXCEPTION_NR( "Cannot get database size", "Unknown processing error" );
	        return -1;
		}
	}
	else
	{
        THROW_RUNTIME_EXCEPTION_NR( "Cannot get database size", "Database type not supported" );
		return -1;
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_DictSubscript
//============================================================================
PyObject* SPELLpyDatabase_DictSubscript( PyObject* self, PyObject* key )
{
	PyDatabaseObject* db = ((PyDatabaseObject*)self);
	if (db->__pdb != NULL)
	{
		try
		{
			if ((!PyString_Check(key))&&(!PyLong_Check(key))&&(!PyInt_Check(key))&&(!PyFloat_Check(key)))
			{
				THROW_RUNTIME_EXCEPTION( "Cannot get item", "Key must be a string or number");
			}
			if (db->__pdb->hasKey(key))
			{
				PyObject* value = db->__pdb->get(key);
				return value;
			}
			else
			{
				THROW_RUNTIME_EXCEPTION("Cannot get item", "Database does not contain that key: " + PYREPR(key));
			}
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get item", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION("Cannot get item", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION( "Cannot get item", "Database type not supported" );
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_DictAssSub
//============================================================================
int SPELLpyDatabase_DictAssSub( PyObject* self, PyObject* key, PyObject* value )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			if ((!PyString_Check(key))&&(!PyLong_Check(key))&&(!PyInt_Check(key))&&(!PyFloat_Check(key)))
			{
				THROW_RUNTIME_EXCEPTION_NR( "Cannot set item", "Key must be a string or number");
				return -1;
			}
			((PyDatabaseObject*)self)->__pdb->set(key,value);
			return 0;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION_NR( "Cannot set item", err.getError() );
			return -1;
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION_NR( "Cannot set item", "Unknown processing error");
	        return -1;
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION_NR( "Cannot set item", "Database type not supported" );
		return -1;
	}
}

//============================================================================
// FUNCTION       : SPELLpyDatabase_HasKey
//============================================================================
PyObject* SPELLpyDatabase_HasKey( PyObject* self, PyObject* args )
{
	if (((PyDatabaseObject*)self)->__pdb != NULL)
	{
		try
		{
			if (PyTuple_Size(args)!=1)
			{
				THROW_SYNTAX_EXCEPTION("Cannot check key existence", "Expected a dictionary key");
			}
			PyObject* key = PyTuple_GetItem(args,0);
			if ((!PyString_Check(key))&&(!PyLong_Check(key))&&(!PyInt_Check(key))&&(!PyFloat_Check(key)))
			{
				THROW_SYNTAX_EXCEPTION( "Cannot check key", "Key must be a string or number" );
			}
			if (((PyDatabaseObject*)self)->__pdb->hasKey(key))
			{
				Py_RETURN_TRUE;
			}
			Py_RETURN_FALSE;
		}
		catch(SPELLdatabaseError& err)
		{
			THROW_RUNTIME_EXCEPTION( "Cannot check key", err.getError() );
		}
		catch(...)
		{
			THROW_RUNTIME_EXCEPTION( "Cannot check key", "Unknown processing error" );
		}
	}
	else
	{
		THROW_RUNTIME_EXCEPTION( "Cannot check key", "Database type not supported" );
	}
}
