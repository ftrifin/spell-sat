// ################################################################################
// FILE       : SPELLpythonError.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the Python error bindings
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
#include "SPELL_UTIL/SPELLpythonError.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------
#include "structmember.h"


// GLOBALS /////////////////////////////////////////////////////////////////


static PyObject* s_exc_ExecutionAborted = NULL;
static PyObject* s_exc_ExecutionTerminated = NULL;




//============================================================================
// FUNCTION        : ExecutionAborted_Init
// DESCRIPTION    : Initialized of the ExecutionAborted python object
//============================================================================
static int ExecutionAborted_Init( PyExecutionAbortedObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ExecutionAborted_Dealloc
// DESCRIPTION    : Cleanup of the ExecutionAborted python object
//============================================================================
static void ExecutionAborted_Dealloc( PyExecutionAbortedObject* self );
//============================================================================
// FUNCTION        : ExecutionAborted_New
// DESCRIPTION    : Constructor of the ExecutionAborted python object
//============================================================================
static PyObject* ExecutionAborted_New( PyTypeObject* type, PyObject* args, PyObject* kwds );



//============================================================================
// FUNCTION        : ExecutionTerminated_Init
// DESCRIPTION    : Initialized of the ExecutionTerminated python object
//============================================================================
static int ExecutionTerminated_Init( PyExecutionTerminatedObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ExecutionTerminated_Dealloc
// DESCRIPTION    : Cleanup of the ExecutionTerminated python object
//============================================================================
static void ExecutionTerminated_Dealloc( PyExecutionTerminatedObject* self );
//============================================================================
// FUNCTION        : ExecutionTerminated_New
// DESCRIPTION    : Constructor of the ExecutionTerminated python object
//============================================================================
static PyObject* ExecutionTerminated_New( PyTypeObject* type, PyObject* args, PyObject* kwds );



//============================================================================
// ExecutionAborted object member specification
//============================================================================
static PyMemberDef ExecutionAborted_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// ExecutionAborted object method specification
//============================================================================
static PyMethodDef ExecutionAborted_Methods[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL ExecutionAborted object type
//============================================================================
static PyTypeObject ExecutionAborted_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "executor.ExecutionAborted",       //tp_name
    sizeof(PyExecutionAbortedObject),  //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)ExecutionAborted_Dealloc,     //tp_dealloc
    0,                                 //tp_print
    0,                                 //tp_getattr
    0,                                 //tp_setattr
    0,                                 //tp_compare
    0,                                 //tp_repr
    0,                                 //tp_as_number
    0,                                 //tp_as_sequence
    0,                                 //tp_as_mapping
    0,                                 //tp_hash
    0,                                 //tp_call
    0,                                 //tp_str
    0,                                 //tp_getattro
    0,                                 //tp_setattro
    0,                                 //tp_as_buffer
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, //tp_flags
    "ExecutionAborted exception",      // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    ExecutionAborted_Methods,          // tp_methods
    ExecutionAborted_Members,          // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)ExecutionAborted_Init,   // tp_init
    0,                                 // tp_alloc
    ExecutionAborted_New,              // tp_new
};

//============================================================================
// ExecutionTerminated object member specification
//============================================================================
static PyMemberDef ExecutionTerminated_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// ExecutionTerminated object method specification
//============================================================================
static PyMethodDef ExecutionTerminated_Methods[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL ExecutionTerminated object type
//============================================================================
static PyTypeObject ExecutionTerminated_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "executor.ExecutionTerminated",    //tp_name
    sizeof(PyExecutionTerminatedObject),        //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)ExecutionTerminated_Dealloc,     //tp_dealloc
    0,                                 //tp_print
    0,                                 //tp_getattr
    0,                                 //tp_setattr
    0,                                 //tp_compare
    0,                                 //tp_repr
    0,                                 //tp_as_number
    0,                                 //tp_as_sequence
    0,                                 //tp_as_mapping
    0,                                 //tp_hash
    0,                                 //tp_call
    0,                                 //tp_str
    0,                                 //tp_getattro
    0,                                 //tp_setattro
    0,                                 //tp_as_buffer
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, //tp_flags
    "ExecutionTerminated exception",   // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    ExecutionTerminated_Methods,       // tp_methods
    ExecutionTerminated_Members,       // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)ExecutionTerminated_Init,// tp_init
    0,                                 // tp_alloc
    ExecutionTerminated_New,           // tp_new
};


//============================================================================
// FUNCTION        : ExecutionAborted_Init
//============================================================================
static int ExecutionAborted_Init( PyExecutionAbortedObject* self, PyObject* args, PyObject* kwds )
{
    PyObject* message=NULL;

    if (! PyArg_ParseTuple(args, "S", &message))
    {
        return -1;
    }
    if (message)
    {
        PyObject* tmp = self->message;
        Py_INCREF(message);
        self->message = message;
        Py_XDECREF(tmp);
    }
    return 0;
}

//============================================================================
// FUNCTION        : ExecutionAborted_Dealloc
//============================================================================
static void ExecutionAborted_Dealloc( PyExecutionAbortedObject* self )
{
    Py_XDECREF(self->message);
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : ExecutionAborted_New
//============================================================================
static PyObject* ExecutionAborted_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyExecutionAbortedObject* self;
    self = (PyExecutionAbortedObject*) type->tp_alloc(type,0);
    if (self != NULL)
    {
        self->message = PyString_FromString("");
        if (self->message == NULL)
        {
            Py_DECREF(self);
            return NULL;
        }

    }
    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : ExecutionTerminated_Init
//============================================================================
static int ExecutionTerminated_Init( PyExecutionTerminatedObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : ExecutionTerminated_Dealloc
//============================================================================
static void ExecutionTerminated_Dealloc( PyExecutionTerminatedObject* self )
{
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : ExecutionTerminated_New
//============================================================================
static PyObject* ExecutionTerminated_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyExecutionTerminatedObject* self;
    self = (PyExecutionTerminatedObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}








//============================================================================
// FUNCTION        : Exceptions_Install
//============================================================================
void Exceptions_Install()
{
    PyType_Ready(&ExecutionAborted_Type);
    s_exc_ExecutionAborted = (PyObject*) PyObject_New( PyExecutionAbortedObject, &ExecutionAborted_Type );
    PyType_Ready(&ExecutionTerminated_Type);
    s_exc_ExecutionTerminated = (PyObject*) PyObject_New( PyExecutionTerminatedObject, &ExecutionTerminated_Type );
}

//============================================================================
// FUNCTION        : Set_Exception_ExecutionAborted
//============================================================================
void Set_Exception_ExecutionAborted()
{
	SPELLsafePythonOperations ops;
    PyErr_SetObject( s_exc_ExecutionAborted, s_exc_ExecutionAborted );
}

//============================================================================
// FUNCTION        : Is_ExecutionAborted
//============================================================================
bool Is_ExecutionAborted( PyObject* exc )
{
    return PyObject_TypeCheck( exc, &ExecutionAborted_Type );
}

//============================================================================
// FUNCTION        : Set_Exception_ExecutionTerminated
//============================================================================
void Set_Exception_ExecutionTerminated()
{
	SPELLsafePythonOperations ops;
    PyErr_SetObject( s_exc_ExecutionTerminated, s_exc_ExecutionTerminated );
}

//============================================================================
// FUNCTION        : Is_ExecutionTerminated
//============================================================================
bool Is_ExecutionTerminated( PyObject* exc )
{
    return PyObject_TypeCheck( exc, &ExecutionTerminated_Type );
}
