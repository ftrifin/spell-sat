// ################################################################################
// FILE       : SPELLwsStorage.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the storage model
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
// Local includes ----------------------------------------------------------
#include "SPELL_WS/SPELLwsStorage.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------
#include "marshal.h"



#define LATEST_MARSHAL_VERSION 2

//=============================================================================
// CONSTRUCTOR: SPELLwsStorage::SPELLwsStorage
//=============================================================================
SPELLwsStorage::SPELLwsStorage( std::string filename, Mode mode )
{
	//m_traceFilename = filename + "_trace";
	//m_trace.open(m_traceFilename.c_str(), std::ios_base::out );
	//m_traceCounter = 0;

    m_filename = filename;
    m_mode = mode;
    // Prefer to use stdio/FILE for this due to the Python marshal
    // API functions
    switch(mode)
    {
    case MODE_READ:
        DEBUG("[*] Open persistent file in read mode: " + filename)
        m_file = fopen( filename.c_str(), "r" );
        break;
    case MODE_WRITE:
        DEBUG("[*] Open persistent file in write mode: " + filename)
        m_file = fopen( filename.c_str(), "w+" );
        break;
    case MODE_UNINIT:
        THROW_EXCEPTION("Unable to setup persistent storage", "Mode is not set", SPELL_ERROR_WSTART);
        break;
    }
    if (m_file == NULL)
    {
        LOG_ERROR("Unable to setup persistent storage, cannot open file: '" + filename + "'");
    }
}

//=============================================================================
// DESTRUCTOR: SPELLwsStorage::~SPELLwsStorage
//=============================================================================
SPELLwsStorage::~SPELLwsStorage()
{
	m_trace.close();
    if (m_file != NULL)
    {
        DEBUG("[*] Close persistent file: " + m_filename)
        fclose(m_file);
        m_file = NULL;
    }
}

//=============================================================================
// METHOD    : SPELLwsStorage::getMode
//=============================================================================
SPELLwsStorage::Mode SPELLwsStorage::getMode()
{
    return m_mode;
}

//=============================================================================
// METHOD    : SPELLwsStorage::reset()
//=============================================================================
void SPELLwsStorage::reset()
{
    if (m_file == NULL)
    {
        return;
    }
    DEBUG("[*] Reset persistent file");
    fclose(m_file);
    if (m_mode == MODE_READ)
    {
        m_file = fopen( m_filename.c_str(), "r" );
    }
    else
    {
        m_file = fopen( m_filename.c_str(), "w+" );
    }
    if (m_file == NULL)
    {
        THROW_EXCEPTION("Unable to reset persistent storage", "Cannot open file: '" + m_filename + "'", SPELL_ERROR_FILESYSTEM);
    }
}

//=============================================================================
// METHOD    : SPELLwsStorage::reset()
//=============================================================================
void SPELLwsStorage::reset( const SPELLwsStorage::Mode& mode )
{
    m_mode = mode;
    reset();
}

//=============================================================================
// METHOD    : SPELLwsStorage::storeObject
//=============================================================================
void SPELLwsStorage::storeObject( PyObject* object )
{
	if (m_file == NULL) return;
    if (object == NULL)
    {
        THROW_EXCEPTION("Unable to store object", "Null reference given", SPELL_ERROR_WSTART);
    }
    if (m_mode == MODE_READ)
    {
        THROW_EXCEPTION("Unable to store object", "Initialized in read mode", SPELL_ERROR_WSTART);
    }

    std::cerr << "STORE\t" << PYREPR(object) << "\t[" << PYREPR(PyObject_Type(object)) << "]" << std::endl;

    // Marshal the object to the persistent storage file
    PyMarshal_WriteObjectToFile( object, m_file, LATEST_MARSHAL_VERSION );

    // Ensure there is no internal Python error
    SPELLpythonHelper::instance().checkError();

    fflush(m_file);
}

//=============================================================================
// METHOD    : SPELLwsStorage::storeLong
//=============================================================================
void SPELLwsStorage::storeLong( long value )
{
	if (m_file == NULL) return;
    if (m_mode == MODE_READ)
    {
        THROW_EXCEPTION("Unable to store long value", "Initialized in read mode", SPELL_ERROR_WSTART);
    }

    std::cerr << "STORE\t" << value << "\t[long]" << std::endl;

    // Marshal the object to the persistent storage file
    PyMarshal_WriteLongToFile( value, m_file, LATEST_MARSHAL_VERSION );

    // Ensure there is no internal Python error
    SPELLpythonHelper::instance().checkError();

    fflush(m_file);
}

//=============================================================================
// METHOD    : SPELLwsStorage::storeObjectOrNone
//=============================================================================
void SPELLwsStorage::storeObjectOrNone( PyObject* object )
{
	if (m_file == NULL) return;
    if (m_mode == MODE_READ)
    {
        THROW_EXCEPTION("Unable to store object", "Initialized in read mode", SPELL_ERROR_WSTART);
    }
    if (object == NULL)
    {

        //m_trace << m_traceCounter++ << "\tSTORE\t(NONE)\t[None]" << std::endl;

        // Marshal None to the file
        PyMarshal_WriteObjectToFile( Py_None, m_file, LATEST_MARSHAL_VERSION );
        SPELLpythonHelper::instance().checkError();
    }
    else
    {
        storeObject(object);
    }
}

//=============================================================================
// METHOD    : SPELLwsStorage::loadObject
//=============================================================================
PyObject* SPELLwsStorage::loadObject()
{
	if (m_file == NULL) return NULL;
    if (m_mode == MODE_WRITE)
    {
        THROW_EXCEPTION("Unable to load object", "Initialized in write mode", SPELL_ERROR_WSTART);
    }
    PyObject* obj = (PyObject*) PyMarshal_ReadObjectFromFile(m_file);

    std::cerr << "LOAD\t" << PYREPR(obj) << "\t[" << PYREPR(PyObject_Type(obj)) << "]" << std::endl;

    SPELLpythonHelper::instance().checkError();

    if (obj != NULL) Py_INCREF(obj);

    return obj;
}

//=============================================================================
// METHOD    : SPELLwsStorage::loadLong
//=============================================================================
long SPELLwsStorage::loadLong()
{
	if (m_file == NULL) return -1;
    if (m_mode == MODE_WRITE)
    {
        THROW_EXCEPTION("Unable to load long value", "Initialized in write mode", SPELL_ERROR_WSTART);
    }
    long value = PyMarshal_ReadLongFromFile(m_file);

    std::cerr << "LOAD\t" << value << "\t[long]" << std::endl;

    SPELLpythonHelper::instance().checkError();

    return value;
}
