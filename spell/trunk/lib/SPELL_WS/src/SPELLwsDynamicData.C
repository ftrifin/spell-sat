// ################################################################################
// FILE       : SPELLwsDynamicData.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the dynamic runtime data manager
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
#include "SPELL_WS/SPELLwsDynamicData.H"
#include "SPELL_WS/SPELLwsDictDataHandler.H"
#include "SPELL_WS/SPELLwsListDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------

//=============================================================================
/** Fake object required for valuestack management */
typedef struct
{
	PyObject_HEAD;
	long 			it_index;
	PyListObject* 	it_seq;
}
ListIteratorMirror;
//=============================================================================


//=============================================================================
// CONSTRUCTOR: SPELLwsDynamicData::SPELLwsDynamicData
//=============================================================================
SPELLwsDynamicData::SPELLwsDynamicData( const std::string& persisFile, unsigned int depth, PyFrameObject* frame, const SPELLwsWorkingMode& mode )
: m_mode(mode),
  m_frame(frame),
  m_persistentFile(persisFile + "_" + ISTR(depth) + ".WSD")
{
	DEBUG("[DYN] Created dynamic data manager for frame " + PYCREPR(m_frame));
	if (mode != MODE_ON_HOLD)
	{
		m_storage = new SPELLwsStorage(m_persistentFile, SPELLwsStorage::MODE_WRITE);
	}
	else
	{
		m_storage = NULL;
	}
}

//=============================================================================
// DESTRUCTOR: SPELLwsDynamicData::~SPELLwsDynamicData
//=============================================================================
SPELLwsDynamicData::~SPELLwsDynamicData()
{
	DEBUG("[DYN] Destroyed dynamic data manager for frame " + PYCREPR(m_frame));
	/** IMPORTANT if this frame manager is destroyed we do not need the persistent data
	    anymore, destroy the files */
	remove( m_persistentFile.c_str() );
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::reset()
//=============================================================================
void SPELLwsDynamicData::reset()
{
	m_storage->reset();
	m_iBlocks.clear();
	// IMPORTANT do not DECREF objects in ValueStack or FastLocals, since they
	// are borrowed references managed by Python layer.
	m_valueStack.clear();
	m_fastLocals.clear();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::update()
//=============================================================================
void SPELLwsDynamicData::update()
{
	// We need to keep a copy of these these data structures, since the Python interpreter code
	// unwinds the stack and the try-blocks in case of exceptions.
	updateTryBlocks();
	updateValueStack();
	updateFastLocals();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::recover()
//=============================================================================
void SPELLwsDynamicData::recover()
{
	// We need to re-create these data structures, since the Python interpreter code
	// unwinds the stack and the try-blocks in case of exceptions.
	recoverTryBlocks();
	recoverValueStack();
	recoverFastLocals();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::save()
//=============================================================================
void SPELLwsDynamicData::save()
{
	storeTryBlocks();
	storeValueStack();
	storeFastLocals();
	storeGlobals();
	storeLocals();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::restore()
//=============================================================================
void SPELLwsDynamicData::restore()
{
	loadTryBlocks();
	loadValueStack();
	loadFastLocals();
	loadGlobals();
	loadLocals();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::updateTryBlocks()
//=============================================================================
void SPELLwsDynamicData::updateTryBlocks()
{
	// If there are tryblocks defined in the frame
	unsigned int numTryBlocks = m_frame->f_iblock;
	if (numTryBlocks>0)
	{
		DEBUG("[DYN] Updating try blocks on frame " + PYCREPR(m_frame));

		// If there is a different number of blocks than before, update. We assume they dont change once created...
		if ( numTryBlocks > m_iBlocks.size())
		{
			PyTryBlock block = m_frame->f_blockstack[m_frame->f_iblock-1];
			m_iBlocks.push_back(block);
			DEBUG("[DYN] 	add block: [" + ISTR(block.b_type) + "," + ISTR(block.b_handler) + "," + ISTR(block.b_level) + "]");
		}
		// Less blocks...
		else if ((unsigned int) m_frame->f_iblock < m_iBlocks.size())
		{
			// Just throw it away
			TryBlocks::iterator it = m_iBlocks.end(); it--;
			m_iBlocks.erase(it);
			DEBUG("[DYN] 	remove block");
		}
		DEBUG("[DYN] Updating try blocks done");
	}
	// Take into account the case of going out loops and try blocks ( the frame
	// does not have blocks defined, so we must clear ours )
	else if (m_iBlocks.size()>0)
	{
		// No need to delete or decref
		m_iBlocks.clear();
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::recoverTryBlocks()
//=============================================================================
void SPELLwsDynamicData::recoverTryBlocks()
{
	// If there were blocks defined...
	if (m_iBlocks.size()>0)
	{
		DEBUG("[DYN] 	Restoring try blocks on frame " + PYCREPR(m_frame));
		TryBlocks::iterator it;
		unsigned int count = 0;
		for( it = m_iBlocks.begin(); it != m_iBlocks.end(); it++)
		{
			PyTryBlock& block = (*it);
			DEBUG("[DYN] 		block " + ISTR(count) + ": [" + ISTR(block.b_type) + "," + ISTR(block.b_handler) + "," + ISTR(block.b_level) + "]");
			PyFrame_BlockSetup( m_frame, block.b_type, block.b_handler, block.b_level );
			count++;
		}
		m_frame->f_iblock = m_iBlocks.size();
		DEBUG("[DYN] 	Restoring try blocks done");
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeTryBlocks()
//=============================================================================
void SPELLwsDynamicData::storeTryBlocks()
{
	// Store tryblocks first. Size of the list:
	m_storage->storeLong( m_iBlocks.size() );
	// Store the blocks now, if any
	TryBlocks::iterator bit;
	for( bit = m_iBlocks.begin(); bit != m_iBlocks.end(); bit++ )
	{
		m_storage->storeLong( (*bit).b_type );
		m_storage->storeLong( (*bit).b_handler );
		m_storage->storeLong( (*bit).b_level );
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadTryBlocks()
//=============================================================================
void SPELLwsDynamicData::loadTryBlocks()
{

}

//=============================================================================
// METHOD    : SPELLwsDynamicData::updateValueStack()
//=============================================================================
void SPELLwsDynamicData::updateValueStack()
{
	// If there are stack values defined
	if ( (m_frame->f_stacktop != NULL) && (m_frame->f_valuestack != m_frame->f_stacktop) )
	{
		int stackCount = m_frame->f_stacktop - m_frame->f_valuestack;

		// Add a new item to the stack information
		if ((unsigned int) stackCount > m_valueStack.size())
		{
			DEBUG("[DYN] Updating value stack on frame " + PYCREPR(m_frame));

			PyObject** p;
			p = m_frame->f_valuestack;

			PyTypeObject* type = (PyTypeObject*) PyObject_Type(*p);
			if (STR(type->tp_name)=="listiterator" )
			{
				// Get the iterator characteristics
				ListIteratorMirror* iterator = (ListIteratorMirror*) (*p);
				DEBUG("[DYN] 	add list iterator: [" + ISTR(iterator->it_index) + "," + PYCREPR(iterator->it_seq) + "]");

				// Create a copy
				PyObject* newIterator = PyObject_GetIter( (PyObject*)iterator->it_seq );
				newIterator->ob_refcnt = (*p)->ob_refcnt;
				ListIteratorMirror* itm = (ListIteratorMirror*)newIterator;
				itm->it_index = iterator->it_index;
				// IMPORTANT this is a borrowed reference
				DEBUG("[DYN] 	stored iterator: [" + ISTR(itm->it_index) + "," + PYCREPR(itm->it_seq) + "]");
				m_valueStack.push_back(newIterator);
			}
			else
			{
				DEBUG("[DYN] Adding to valuestack, unprocessed: " + PYREPR(*p));
				// IMPORTANT this is a borrowed reference
				m_valueStack.push_back(*p);
			}

			DEBUG("[DYN] Updating value stack done");
		}
		// Less items
		else if ((unsigned int) stackCount < m_valueStack.size())
		{
			// Throw it away...
			ObjectList::iterator it = m_valueStack.end(); it--;
			DEBUG("[DYN] 	remove value stack object");
			// IMPORTANT do not DECREF objects in ValueStack or FastLocals, since they
			// are borrowed references managed by Python layer.
			m_valueStack.erase(it);
		}
		// Check item changes
		else if (stackCount>0)
		{
			PyObject** p;
			p = m_frame->f_valuestack;
			for( unsigned int index = 0; index < m_valueStack.size(); index++)
			{
				if (*p == NULL) break;
				DEBUG("[DYN] 	checking value stack object: " + PYREPR(*p));
				PyTypeObject* type = (PyTypeObject*) PyObject_Type(*p);
				if (STR(type->tp_name)=="listiterator" )
				{
				    ListIteratorMirror* stackIterator = (ListIteratorMirror*) (*p);
				    ListIteratorMirror* storedIterator = (ListIteratorMirror*) m_valueStack[index];
				    storedIterator->it_index = stackIterator->it_index;
					DEBUG("[DYN] 	update iterator: [" + ISTR(stackIterator->it_index) + "," + PYCREPR(stackIterator->it_seq) + "]");
				}
			}
		}
	}
	// No values defined in the frame, ensure we dont keep them either
	else if (m_valueStack.size() > 0)
	{
		// IMPORTANT do not DECREF objects in ValueStack or FastLocals, since they
		// are borrowed references managed by Python layer.
		m_valueStack.clear();
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::recoverValueStack()
//=============================================================================
void SPELLwsDynamicData::recoverValueStack()
{
	// If there are values to recover
	if (m_valueStack.size()>0)
	{
		DEBUG("[DYN] 	Restoring value stack on frame " + PYCREPR(m_frame));
		PyObject** stack_pointer = m_frame->f_valuestack;

		for( unsigned int count = 0; count<m_valueStack.size(); count++)
		{
			PyObject* stackObject = m_valueStack[count];
			DEBUG("[DYN] 		add value stack object " + PYCREPR(stackObject));
			PyTypeObject* type = (PyTypeObject*) PyObject_Type(stackObject);
			DEBUG("[DYN] Type is " + STR(type->tp_name));
			if (STR(type->tp_name)=="listiterator" )
			{
				ListIteratorMirror* stackIterator = (ListIteratorMirror*) (stackObject);
				DEBUG("[DYN] 	      restoring iterator: [" + ISTR(stackIterator->it_index) + "," + PYCREPR(stackIterator->it_seq) + "]");
				PyObject* newIterator = PyObject_GetIter( (PyObject*)stackIterator->it_seq );
				newIterator->ob_refcnt = stackObject->ob_refcnt;
				*stack_pointer++ = newIterator;
			}
			else
			{
				*stack_pointer++ = stackObject;
			}
		}
		m_frame->f_stacktop = stack_pointer--;
		m_frame->f_code->co_stacksize = m_valueStack.size();
		DEBUG("[DYN] 	Restoring value stack done");
	}
	// Otherwise define the top as the same value as the bottom of the stack
	else
	{
		m_frame->f_stacktop = m_frame->f_valuestack;
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeValueStack()
//=============================================================================
void SPELLwsDynamicData::storeValueStack()
{
	// Store the value stack. Size of the list first:
	m_storage->storeLong( m_valueStack.size() );
	ObjectList::iterator it;
	for( it = m_valueStack.begin(); it != m_valueStack.end(); it++)
	{
		PyTypeObject* type = (PyTypeObject*) PyObject_Type(*it);
		if (STR(type->tp_name)=="listiterator" )
		{
		    // Store the identifier tag
		    PyObject* mod_str  = STRPY("<LIST-ITERATOR>");
		    SPELLwsObjectDataHandler marker(mod_str);
		    marker.setStorage(m_storage);

		    // The iterator characteristics
		    ListIteratorMirror* iterator = (ListIteratorMirror*) (*it);
		    // The list
		    SPELLwsListDataHandler list( (PyObject*) iterator->it_seq );
		    list.setStorage(m_storage);

		    // Store the marker
		    marker.write();
		    // The current index
		    m_storage->storeLong(iterator->it_index);
		    // Store the list
		    list.write();

		}
		else
		{
		    SPELLwsObjectDataHandler handler( *it );
		    handler.setStorage(m_storage);
		    handler.write();
		}
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadValueStack()
//=============================================================================
void SPELLwsDynamicData::loadValueStack()
{

}

//=============================================================================
// METHOD    : SPELLwsDynamicData::updateFastLocals()
//=============================================================================
void SPELLwsDynamicData::updateFastLocals()
{
	int numLocals = m_frame->f_code->co_nlocals-1;
	if (numLocals<0) numLocals = 0;
        if ((numLocals>0) && ((unsigned int)numLocals != m_fastLocals.size()))
	{
		PyObject** lplus = m_frame->f_localsplus;
		for ( int count = 0; count<numLocals-1; count++) lplus++;
		DEBUG("      add fast local: " + PYREPR(*lplus));
		// IMPORTANT  this is a borrowed reference
		m_fastLocals.push_back(*lplus);
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::recoverFastLocals()
//=============================================================================
void SPELLwsDynamicData::recoverFastLocals()
{
	if (m_fastLocals.size()>0)
	{
		 for( unsigned int count = 0; count<m_fastLocals.size(); count++)
		 {
			 PyObject* obj = m_fastLocals[count];
			 DEBUG("      recover fast local " + PYREPR(obj));
			 m_frame->f_localsplus[count] = obj;
		 }
		 m_frame->f_code->co_nlocals = m_fastLocals.size()+1;
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeFastLocals()
//=============================================================================
void SPELLwsDynamicData::storeFastLocals()
{
	// Store the fast locals. Size of list first
	m_storage->storeLong( m_fastLocals.size() );
	ObjectList::iterator it;
	for( it = m_fastLocals.begin(); it != m_fastLocals.end(); it++)
	{
		 m_storage->storeObjectOrNone(*it);
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadFastLocals()
//=============================================================================
void SPELLwsDynamicData::loadFastLocals()
{

}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeGlobals()
//=============================================================================
void SPELLwsDynamicData::storeGlobals()
{
	storeDictionary( m_frame->f_globals );
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadGlobals()
//=============================================================================
void SPELLwsDynamicData::loadGlobals()
{
	m_frame->f_globals = loadDictionary();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeLocals()
//=============================================================================
void SPELLwsDynamicData::storeLocals()
{
	storeDictionary( m_frame->f_locals );
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadLocals()
//=============================================================================
void SPELLwsDynamicData::loadLocals()
{
	m_frame->f_locals = loadDictionary();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeDictionary()
//=============================================================================
void SPELLwsDynamicData::storeDictionary( PyObject* dictionary )
{
	SPELLwsDictDataHandler handler(dictionary);
	handler.setStorage(m_storage);
	handler.write();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadDictionary()
//=============================================================================
PyObject* SPELLwsDynamicData::loadDictionary()
{
	SPELLwsDictDataHandler handler(NULL);
	handler.setStorage(m_storage);
	handler.read();
	return handler.getObject();
}
