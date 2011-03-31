// ################################################################################
// FILE       : SPELLwsWarmStartImpl.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the warm start mechanism controller
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
#include "SPELL_WS/SPELLwsWarmStartImpl.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------


//=============================================================================
// CONSTRUCTOR: SPELLwsWarmStartImpl::SPELLwsWarmStartImpl
//=============================================================================
SPELLwsWarmStartImpl::SPELLwsWarmStartImpl()
: SPELLwarmStart()
{
	DEBUG("[WS] WarmStart created");
	m_dataFileId = "";
	m_mode = MODE_UNINIT;
	m_recursionDepth = 0;
	m_topFrame = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLwsWarmStartImpl::~SPELLwsWarmStartImpl
//=============================================================================
SPELLwsWarmStartImpl::~SPELLwsWarmStartImpl()
{
	DEBUG("[WS] WarmStart destroyed");
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::initialize()
//=============================================================================
void SPELLwsWarmStartImpl::initialize( const std::string& identifier, const SPELLwsWorkingMode& mode )
{
	m_dataFileId = identifier;
	DEBUG("[WS] Using file identifier: " + identifier);
	DEBUG("[WS] Working mode: " + ISTR(mode));
	m_mode = mode;
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::reset()
//=============================================================================
void SPELLwsWarmStartImpl::reset()
{
	DEBUG("[WS] Reset mechanism");
	while(m_frames.size()>0) removeTopFrame();
	m_recursionDepth = 0;
	m_topFrame = NULL;
	DEBUG("[WS] Reset mechanism done");
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::notifyCall()
//=============================================================================
void SPELLwsWarmStartImpl::notifyCall( PyFrameObject* newFrame )
{
	DEBUG("[WS] Notify call on " + PYCREPR(newFrame) + ", recursion depth " + ISTR(m_recursionDepth));
	// Add the frame to the list of frames
	addFrame( newFrame );
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::notifyReturn()
//=============================================================================
void SPELLwsWarmStartImpl::notifyReturn()
{
	DEBUG("[WS] Notify return");
	// Remove the frame at the top of the tree, we dont need it anymore
	removeTopFrame();
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::notifyLine()
//=============================================================================
void SPELLwsWarmStartImpl::notifyLine()
{
	DEBUG("[WS] Notify line, top frame " + PYCREPR(m_topFrame->getFrameObject()));
	// Notify the top frame to keep updated the recovery information
	m_topFrame->eventLine();
	// Perform state save if working mode is ON_LINE
	if (getWorkingMode()==MODE_ON_LINE)
	{
		saveState();
	}
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::notifyStage()
//=============================================================================
void SPELLwsWarmStartImpl::notifyStage()
{
	DEBUG("[WS] Notify stage");
	// Perform state save if working mode is ON_STAGE
	if (getWorkingMode()==MODE_ON_STEP)
	{
		saveState();
	}
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::saveState()
//=============================================================================
void SPELLwsWarmStartImpl::saveState()
{
	// Perform state save
	std::cerr << "##### SAVING WS STATE #####" << std::endl;
	unsigned int frameCount = m_frames.size();
	for( unsigned int index = 0; index < frameCount; index++)
	{
		m_frames[index]->saveState();
	}

}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::restoreState()
//=============================================================================
PyFrameObject* SPELLwsWarmStartImpl::restoreState()
{
	DEBUG("[WS] Restoring state");
	/** todo implement warmstart */
	return NULL;
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::fixState()
//=============================================================================
PyFrameObject* SPELLwsWarmStartImpl::fixState()
{
	DEBUG("[WS] Fixing state ==============================================");

	// Get the head interpreter state
	PyInterpreterState* istate = PyInterpreterState_Head();

	// Get the current thread state
	PyThreadState* oldState = PyThreadState_GET();
	DEBUG("[WS] Old state: " + PSTR(oldState));
	DEBUG("[WS] Interpreter head: " + PSTR(istate->tstate_head));
	DEBUG("[WS] Interpreter next: " + PSTR(istate->next));
	DEBUG("[WS] State recursion depth: " + ISTR(oldState->recursion_depth));
	DEBUG("[WS] State next: " + PSTR(oldState->next));

	// Create a fresh thread state
	PyThreadState* newState = PyThreadState_New(istate);
	istate->tstate_head = newState;

	newState->recursion_depth = oldState->recursion_depth;
	newState->tracing = oldState->tracing;
	newState->use_tracing = oldState->use_tracing;
	newState->tick_counter = oldState->tick_counter;
	newState->gilstate_counter = oldState->gilstate_counter;
	newState->dict = PyDict_Copy(oldState->dict);

	FrameList::iterator it;
	unsigned int frameCount = m_frames.size();
	DEBUG("[WS] Total frames to fix " + ISTR(frameCount));
	m_topFrame = NULL;
	for( unsigned int index = 0; index < frameCount; index++)
	{
		bool isHead = (index == (frameCount-1));
		DEBUG("[WS] Fix state on frame index " + ISTR(index));
		m_topFrame = m_frames[index];
		m_topFrame->fixState(newState, isHead);
	}
	DEBUG("[WS] State fixed ===============================================");

	return m_topFrame->getFrameObject();
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::addFrame()
//=============================================================================
void SPELLwsWarmStartImpl::addFrame( PyFrameObject* frame )
{
	// Dont actually add it if it is the head (this happens after fixing the state)
	if ( m_topFrame == NULL || frame != m_topFrame->getFrameObject() )
	{
		DEBUG("[WS] Adding frame manager for " + PYCREPR(frame));
		m_topFrame = new SPELLwsFrame( m_dataFileId, m_frames.size(), frame, getWorkingMode() );
		m_frames.push_back(m_topFrame);
		m_recursionDepth++;
	}
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::removeTopFrame()
//=============================================================================
void SPELLwsWarmStartImpl::removeTopFrame()
{
	DEBUG("[WS] Removing top frame manager");
	// Delete the top frame
	delete m_topFrame;
	// Remove the list element
	FrameList::iterator it = m_frames.end();
	it--; //Point to the last frame
	m_frames.pop_back();
	// Get the new top frame now
	it = m_frames.end();
	it--; //Point to the last frame
	m_topFrame = (*it);
	m_recursionDepth--;
}
