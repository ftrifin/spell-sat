// ################################################################################
// FILE       : SPELLpythonError.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Python error bindings
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

#ifndef __SPELL_PYERROR_H__
#define __SPELL_PYERROR_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Project includes --------------------------------------------------------


/** \addtogroup SPELL_UTIL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Python binding object for "Execution Aborted" exceptions thrown
 **  from the C++ side. This exception is used when aborting the procedure
 **  execution programatically.
 **
 ** \par Description and usage:
 **
 **		This object allows the C++ layer throwing exceptions which are
 **		detected by the Python interpreter code.
 **
**////////////////////////////////////////////////////////////////////////////
typedef struct PyExecutionAbortedObject_
{
    PyObject_HEAD;
    PyObject*    message;
}
PyExecutionAbortedObject;

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Python binding object for "Execution Terminated" exceptions thrown
 **  from the C++ side. This exception is used when forcing the procedure
 **  execution to terminate programatically.
 **
 ** \par Description and usage:
 **
 **		This object allows the C++ layer throwing exceptions which are
 **		detected by the Python interpreter code.
 **
**////////////////////////////////////////////////////////////////////////////
typedef struct PyExecutionTerminatedObject_
{
    PyObject_HEAD;
}
PyExecutionTerminatedObject;

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief C-API function to install the exception bindings on the Python layer.
 **
 ** \par Description and usage:
 **
 **		Used from the main function of programs which need exception injection
 **		features.
 **
**////////////////////////////////////////////////////////////////////////////
void Exceptions_Install();

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief C-API function to inject the Execution Aborted exception on the
 ** Python layer.
 **
 ** \par Description and usage:
 **
 **		Used from the SPELL controller to provoke abort conditions.
 **
**////////////////////////////////////////////////////////////////////////////
void Set_Exception_ExecutionAborted();

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief C-API function to check if the Execution Aborted exception has
 ** been injected in the Python layer.
 **
 ** \param obj IN: the Python object representing a detected exception.
 **
 ** \par Description and usage:
 **
 **		Used from the SPELL controller to check abort conditions.
 **
**////////////////////////////////////////////////////////////////////////////
bool Is_ExecutionAborted( PyObject* obj );

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief C-API function to inject the Execution Terminated exception on the
 ** Python layer.
 **
 ** \par Description and usage:
 **
 **		Used from the SPELL controller to provoke termination conditions.
 **
**////////////////////////////////////////////////////////////////////////////
void Set_Exception_ExecutionTerminated();

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief C-API function to check if the Execution Terminated exception has
 ** been injected in the Python layer.
 **
 ** \param obj IN: the Python object representing a detected exception.
 **
 ** \par Description and usage:
 **
 **		Used from the SPELL controller to check termination conditions.
 **
**////////////////////////////////////////////////////////////////////////////
bool Is_ExecutionTerminated( PyObject* obj );

/*@}*/
#endif
