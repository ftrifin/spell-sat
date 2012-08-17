// ################################################################################
// FILE       : SPELLwsFrame.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the frame data manager
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
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_WS/SPELLwsFrame.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------


//=============================================================================
/** For debugging purposes only */
void DumpFrameInfo( PyFrameObject* frame )
{
	std::cerr << "=============================================" << std::endl;
	std::cerr << "Last instruction   " << frame->f_lasti << std::endl;
	std::cerr << "Last line          " << frame->f_lineno << std::endl;
	std::cerr << "Previous frame     " << PYCREPR(frame->f_back) << std::endl;
	std::cerr << "Thread state       " << PSTR(frame->f_tstate) << std::endl;
	std::cerr << "Try blocks count   " << frame->f_iblock << std::endl;
	std::cerr << "Try blocks         " << PSTR(frame->f_blockstack) << std::endl;
	std::cerr << "Value stack        " << PSTR(frame->f_valuestack) << std::endl;
	std::cerr << "Stack top          " << PSTR(frame->f_stacktop) << std::endl;
	std::cerr << "Stack count        " << (frame->f_stacktop - frame->f_valuestack) << std::endl;
	std::cerr << "Fast locals        " << (frame->f_code->co_nlocals-1) << std::endl;
	std::cerr << "=============================================" << std::endl;
}
//=============================================================================


//=============================================================================
// CONSTRUCTOR: SPELLwsFrame::SPELLwsFrame
//=============================================================================
SPELLwsFrame::SPELLwsFrame( const std::string& persisFile, unsigned int depth, PyFrameObject* frame, const SPELLwsWorkingMode& mode )
: m_frame(frame),
  m_static(persisFile,depth,frame,mode),
  m_dynamic(persisFile,depth,frame,mode)
{
	DEBUG("[FRM] Created manager for frame " + PYCREPR(m_frame));
	m_lastInstruction = frame->f_lasti;
	m_lastLine = frame->f_lineno;
}

//=============================================================================
// CONSTRUCTOR: SPELLwsFrame::SPELLwsFrame
//=============================================================================
SPELLwsFrame::SPELLwsFrame( const std::string& persisFile, unsigned int depth, const SPELLwsWorkingMode& mode )
: m_frame(NULL),
  m_static(persisFile,depth,NULL,mode),
  m_dynamic(persisFile,depth,NULL,mode)
{
	/** \todo frame recovery */
}

//=============================================================================
// DESTRUCTOR: SPELLwsFrame::~SPELLwsFrame
//=============================================================================
SPELLwsFrame::~SPELLwsFrame()
{
	DEBUG("[FRM] Destroyed manager for frame " + PYCREPR(m_frame));
	// IMPORTANT if this frame manager is destroyed we do not need the persistent data
	// anymore, destroy the files
}

//=============================================================================
// METHOD    : SPELLwsFrame::eventLine()
//=============================================================================
void SPELLwsFrame::eventLine()
{
	//DumpFrameInfo( m_frame );

	// On a line event we need to keep the latest instruction and line number used
	// so that we can reapply them after a recovery.
	m_lastInstruction = m_frame->f_lasti;
	m_lastLine = m_frame->f_lineno;

	DEBUG("[FRM] Frame " + PYCREPR(m_frame) + ": INS(" + ISTR(m_lastInstruction) + "), LIN(" + ISTR(m_lastLine) + ")");

	// Update the tracked dynamic data of the frame
	m_dynamic.update();

	DEBUG("[FRM] Update on line event finished");
}

//=============================================================================
// METHOD    : SPELLwsFrame::fixState()
//=============================================================================
void SPELLwsFrame::fixState( PyThreadState* newState, bool isHead )
{
	DEBUG("[FRM] Fix state on frame " + PYCREPR(m_frame) + ", head=" + BSTR(isHead));
	// This is required due to how the Python evaluation loop works. The
	// instruction interesting for us is the one after the function call, if
	// the frame is no the head of the tree.
	if (isHead)
	{
		DEBUG("[FRM] Set instruction as head");
		m_lastInstruction--;
	}
	else
	{
		DEBUG("[FRM] Set instruction as intermediate");
		DEBUG("[FRM] Original instruction was " + ISTR(m_lastInstruction));
		DEBUG("[FRM] Last line was " + ISTR(m_lastLine));

		std::string filename = PYSTR(m_frame->f_code->co_filename);
		std::string codename = PYSTR(m_frame->f_code->co_name);
		std::string code_id = filename + "-" + codename;
		int nextLine = SPELLexecutor::instance().getFrame().getModel(code_id).lineAfter(m_lastLine);
		DEBUG("[FRM] Next line is " + ISTR(nextLine));
		int nextInstruction = SPELLexecutor::instance().getFrame().getModel(code_id).offset(nextLine);
		m_lastInstruction = nextInstruction -1; // Will position it in the lastLine but POP_TOP instr.
		DEBUG("[FRM] Set instruction to: " + ISTR(m_lastInstruction));
	}

	DEBUG("[FRM] Applying: INS(" + ISTR(m_lastInstruction) + "), LIN(" + ISTR(m_lastLine) + ") on frame " + PYCREPR(m_frame));

	// Reset the frame values
	m_frame->f_lasti = m_lastInstruction;
	m_frame->f_lineno = m_lastLine;
	m_frame->f_tstate = newState;
	m_frame->f_stacktop = m_frame->f_valuestack;

	// Recover the dynamic data and update the frame
	DEBUG("[FRM] Recovering dynamic data");
	m_dynamic.recover();

	// Connect the head with the thread state (the head is the frame going to
	// be executed after recovery)
	if (isHead)
	{
		newState->frame = m_frame;
	}

	//DumpFrameInfo( m_frame );
	DEBUG("[FRM] State fixed on frame " + PYCREPR(m_frame));
}

//=============================================================================
// METHOD    : SPELLwsFrame::reset()
//=============================================================================
void SPELLwsFrame::reset()
{
	m_dynamic.reset();
}

//=============================================================================
// METHOD    : SPELLwsFrame::saveState()
//=============================================================================
void SPELLwsFrame::saveState()
{
	m_dynamic.save();
}
