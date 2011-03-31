// ################################################################################
// FILE       : SPELLwsDictDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for dictionaries
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
#include "SPELL_WS/SPELLwsDictDataHandler.H"
#include "SPELL_WS/SPELLwsDataHandlerFactory.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsDictDataHandler::SPELLwsDictDataHandler
//=============================================================================
SPELLwsDictDataHandler::SPELLwsDictDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	/** \todo Review the need of storing additional information */
	setCode( DATA_DICTIONARY );
}

//=============================================================================
// DESTRUCTOR: SPELLwsDictDataHandler::~SPELLwsDictDataHandler
//=============================================================================
SPELLwsDictDataHandler::~SPELLwsDictDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsDictDataHandler::write()
//=============================================================================
void SPELLwsDictDataHandler::write()
{
	assert( PyDict_Check(getObject()));

	PyObject* keys = PyDict_Keys( getObject() );
	unsigned int numItems = PyList_Size( keys );

	DEBUG("[DDH] Storing dictionary items: " + ISTR(numItems));

	// Store the number of items
	getStorage()->storeLong( (long) numItems );

	// Store each list item
	for( unsigned int index = 0; index < numItems; index++)
	{
		DEBUG("		[DDH] Key index " + ISTR(index));
		PyObject* key = PyList_GetItem( keys, index );
		DEBUG("		[DDH] Key to use" + PYREPR(key));
		// Handler for the key
		SPELLwsObjectDataHandler keyHandler(key);
		keyHandler.setStorage(getStorage());
		// Handler for the value
		PyObject* item = PyDict_GetItem( getObject(), key );
		SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(item);
		handler->setStorage(getStorage());

		// Store the key
		DEBUG("		[DDH] Storing key: " + PYREPR(key));
		keyHandler.write();

		// Store the item data code in order to recognise it later
		DEBUG("		[DDH] Storing data code: " + ISTR(handler->getCode()));
		handler->storeDataCode();

		// IMPORTANT in the case of lists and dictionaries, we want to be able to continue
		// the storage evenif there is a problem in the handler processing at this point.
		// If that is the case, a fake empty object will be replaced by the object being
		// processed by the handler, and the dumping of this collection will continue.
		try
		{
			// Store the value
			DEBUG("		[DDH] Storing value: " + PYREPR(item));
			handler->write();
			DEBUG("		[DDH] Storing value done");
		}
		catch(SPELLcoreException& ex)
		{
			std::string msg = "WARNING! Storage of element " + ISTR(index) + " failed: " + STR(ex.what());
			LOG_WARN(msg);
			std::cerr << msg << std::endl;
			storeFakeObject( handler->getCode() );
		}

		delete handler;
		DEBUG("     [DDH] ");
	}

	DEBUG("[DDH] Storing dictionary done");
}

//=============================================================================
// METHOD    : SPELLwsDictDataHandler::read()
//=============================================================================
void SPELLwsDictDataHandler::read()
{
	DEBUG("[DDH] Loading dictionary items");
	// Load the number of items
	unsigned int numItems = getStorage()->loadLong();
	DEBUG("[DDH] Number of items " + ISTR(numItems));

	// Create a dictionary
	PyObject* dictObject = PyDict_New();

	for( unsigned int index = 0; index < numItems; index++)
	{
		// We know that first an Object comes, as the key. So make the handler directly.
		SPELLwsObjectDataHandler keyHandler(NULL);
		keyHandler.setStorage(getStorage());
		DEBUG("		[DDH] Loading key");
		keyHandler.read();
		PyObject* key = keyHandler.getObject();
		DEBUG("		[DDH] Loaded key " + PYREPR(key));

		// Load the item code
		DEBUG("		[DDH] Loading data code");
		SPELLwsDataCode code = loadDataCode();
		DEBUG("		[DDH] Loaded data code " + ISTR(code));
		// Create an appropriate handler
		SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(code);
		handler->setStorage(getStorage());
		// Read the data
		DEBUG("		[DDH] Loading value");
		handler->read();
		DEBUG("		[DDH] Value loaded " + PYREPR(handler->getObject()));
		// Add the item to the dictionary
		PyDict_SetItem(dictObject, key, handler->getObject());
		delete handler;
		DEBUG("     [DDH] ");
	}

	DEBUG("[DDH] Dictionary loaded");

	// Set it as associated object
	setObject( dictObject );
}
