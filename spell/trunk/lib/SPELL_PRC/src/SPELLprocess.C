// ################################################################################
// FILE       : SPELLprocess.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of process model
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
#include "SPELL_PRC/SPELLprocessManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_SYN/SPELLsyncError.H"
// System includes ---------------------------------------------------------
#include <sys/wait.h>


// GLOBALS /////////////////////////////////////////////////////////////////

#define PROCESS_READ 0
#define PROCESS_WRITE 1
#define SHELL_NOTFOUND 127

//=============================================================================
// FUNCTION   : SPELLprocessUtils
//=============================================================================
std::string SPELLprocessUtils::processStatusToString( SPELLprocessStatus status )
{
	switch(status)
	{
	case PSTATUS_FAILED:
		return "FAILED";
	case PSTATUS_FINISHED:
		return "FINISHED";
	case PSTATUS_INITIAL:
		return "INITIAL";
	case PSTATUS_KILLED:
		return "KILLED";
	case PSTATUS_RUNNING:
		return "RUNNING";
	}
	return "UNKNOWN";
}

//=============================================================================
// CONSTRUCTOR: SPELLprocess::SPELLprocess
//=============================================================================
SPELLprocess::SPELLprocess( std::string identifier, std::string command, SPELLprocessManager* mgr )
    : SPELLthread("process-" + identifier)
{
    m_pManager = mgr;
    m_notChild = false;
    m_retValue = -1;
    m_identifier = identifier;
    m_command = command;
    m_processPID = 0;
    m_status = 0;
    m_statusCode = PSTATUS_INITIAL;
    m_killIt = false;
    m_aboutToClose = false;
    m_endEvent.clear();
    DEBUG("[PRC] PROCESS " + identifier + " CREATED");
}

//=============================================================================
// CONSTRUCTOR: SPELLprocess::SPELLprocess
//=============================================================================
SPELLprocess::SPELLprocess( std::string identifier, int pid, SPELLprocessManager* mgr )
    : SPELLthread("process-" + identifier)
{
    m_pManager = mgr;
    m_notChild = true;
    m_retValue = -1;
    m_identifier = identifier;
    m_command = "";
    m_processPID = pid;
    m_status = 0;
    m_statusCode = PSTATUS_INITIAL;
    m_killIt = false;
    m_aboutToClose = false;
    m_endEvent.clear();
    DEBUG("[PRC] PROCESS " + identifier + " ATTACHED");
}

//=============================================================================
// DESTRUCTOR : SPELLprocess::~SPELLprocess
//=============================================================================
SPELLprocess::~SPELLprocess()
{
	m_endEvent.set();
    DEBUG("[PRC] PROCESS " + m_identifier + " DESTROYED");
}

//=============================================================================
// METHOD     : SPELLprocess::run()
//=============================================================================
void SPELLprocess::run()
{
    DEBUG("[PRC] Starting process monitoring");
    if (!m_notChild)
    {
		m_status = 0;
		m_statusCode = PSTATUS_INITIAL;
		m_processPID = popen( m_command );

		DEBUG("[PRC] Waiting PID");
		waitpid( m_processPID, &m_status, WNOHANG);

		// First thing, check if the command has exited already at this point
		// if the return value is 127,
		if (WIFEXITED(m_status))
		{
			m_retValue = WEXITSTATUS(m_status);
			DEBUG("[PRC] Process exited before check loop");
			if ( m_retValue == SHELL_NOTFOUND )
			{
				DEBUG("[PRC] The process has failed (no shell)");
				m_retValue = -1;
				m_statusCode = PSTATUS_FAILED;
				m_pManager->fireProcessFailed( m_identifier );
				m_endEvent.set();
				return;
			}
			else if (m_retValue != 0)
			{
				DEBUG("[PRC] The process has failed (" + ISTR(m_retValue) + ")");
				m_statusCode = PSTATUS_FAILED;
				m_pManager->fireProcessFailed( m_identifier );
				m_endEvent.set();
				return;
			}
		}
		DEBUG("[PRC] The process has started, starting check");
	    m_statusCode = PSTATUS_RUNNING;
	    m_pManager->fireProcessStarted( m_identifier );

	    bool repeat = false;
	    do
	    {
	        m_status = 0;
	        repeat = false;
	        DEBUG("[PRC] Waiting child PID " + ISTR(m_processPID) );
	        int result = waitpid( m_processPID, &m_status, WUNTRACED );
	        DEBUG("[PRC] Child PID " + ISTR(m_processPID) + " exited");
	        if (result<0)
	        {
	            m_retValue = -1;
	            m_statusCode = PSTATUS_KILLED;
	            LOG_ERROR("The process " + ISTR(m_processPID) + " has been killed [1]");
	            m_pManager->fireProcessKilled( m_identifier );
	        }
	        else if (WIFEXITED(m_status))
	        {
	            m_retValue = WEXITSTATUS(m_status);
	            DEBUG("[PRC] The process " + ISTR(m_processPID) + " has finished with exit code " + ISTR(m_retValue));
	            if (m_retValue == SHELL_NOTFOUND )
	            {
	                DEBUG("[PRC] Command not found");
	            	m_retValue = -1;
	                m_statusCode = PSTATUS_FAILED;
	                m_pManager->fireProcessFailed( m_identifier );
	            }
	            else if (m_retValue == 0)
	            {
	                m_statusCode = PSTATUS_FINISHED;
	            	m_pManager->fireProcessFinished( m_identifier, m_retValue );
	            }
	            else
	            {
	                m_statusCode = PSTATUS_FAILED;
	            	m_pManager->fireProcessFailed( m_identifier );
	            }
	        }
	        else if (WIFSIGNALED(m_status))
	        {
	            m_retValue = -1;
	            m_statusCode = PSTATUS_KILLED;
	            DEBUG("[PRC] The process " + ISTR(m_processPID) + " has been killed [2]");
	            m_pManager->fireProcessKilled( m_identifier );
	        }
	        else if (WIFSTOPPED(m_status) || WIFCONTINUED(m_status))
	        {
	            // SPELLprocess can be stopped by a signal and can continue when SIGCONT
	            // is sent.
	            repeat = true;
	        }
	        else
	        {
	            m_retValue = -1;
	            m_statusCode = PSTATUS_KILLED;
	            DEBUG("[PRC] The process " + ISTR(m_processPID) + " has been killed [3]");
	            m_pManager->fireProcessKilled( m_identifier );
	        }
	    }
	    while(repeat == true);
    }
    else
    {
        DEBUG("[PRC] Attach to process " + ISTR(m_processPID) );
        m_statusCode = PSTATUS_RUNNING;
	    m_pManager->fireProcessStarted( m_identifier );

	    bool repeat = false;
	    do
	    {
	    	if ( !SPELLutils::pathExists( "/proc/" + ISTR(m_processPID) + "/stat" ) )
	    	{
	    		if (m_aboutToClose)
	    		{
	    			DEBUG("[PRC] The attached process " + ISTR(m_processPID) + " has finished");
	    			m_pManager->fireProcessFinished( m_identifier, 0 );
	    		}
	    		else
	    		{
	    			DEBUG("[PRC] The attached process " + ISTR(m_processPID) + " has been killed [4]");
	    			m_pManager->fireProcessKilled( m_identifier );
	    		}
    			repeat = false;

	    	}
	    	else
	    	{
		    	usleep(1000);
	    	}
	    }
	    while(repeat);
    }

    DEBUG("[PRC] Process monitoring finished");
    m_endEvent.set();
    DEBUG("[PRC] Exiting monitoring thread");
}

//=============================================================================
// METHOD     : SPELLprocess::kill()
//=============================================================================
void SPELLprocess::kill()
{
	if (m_statusCode == PSTATUS_RUNNING)
	{
		m_killIt = true;
		DEBUG("[PRC] Sending KILL signal to process " + ISTR(m_processPID));
		::kill( m_processPID, SIGKILL );
	}
	else
	{
		DEBUG("[PRC] Cannot kill a process which is not running");
	}
}

//=============================================================================
// METHOD     : SPELLprocess::wait()
//=============================================================================
int SPELLprocess::wait()
{
	if (m_statusCode == PSTATUS_RUNNING)
	{
		DEBUG("[PRC] (wait) Waiting process to finish");
		try{ join(); } catch(SPELLsyncError& err) {;};
		DEBUG("[PRC] (wait) Process finished with code " + ISTR(m_retValue));
	}
	else
	{
		DEBUG("[PRC] Cannot wait for a process which is not running");
	}
	return m_retValue;
}

//=============================================================================
// METHOD     : SPELLprocess::popen()
//=============================================================================
pid_t SPELLprocess::popen( std::string command )
{
	/** \todo define and use the pipes for process output */
    int p_stdin[2], p_stdout[2];
    pid_t pid;

    if (pipe(p_stdin) != 0 || pipe(p_stdout) != 0)
    {
        THROW_EXCEPTION("Cannot launch process", "Failed to setup standard pipes", SPELL_ERROR_PROCESS);
    }

	DEBUG("[PRC] POpen prefork");
    pid = fork();

    if (pid < 0)
    {
        return pid;
    }
    else if (pid == 0)
    {
    	DEBUG("[PRC] POpen in exec");
        close(p_stdin[PROCESS_WRITE]);
        //dup2(p_stdin[PROCESS_READ], PROCESS_READ);
        close(p_stdout[PROCESS_READ]);
        //dup2(p_stdout[PROCESS_WRITE], PROCESS_WRITE);
        if (execl("/bin/sh", "sh", "-c", command.c_str(), NULL) != 0)
		{
        	THROW_EXCEPTION("Unable to start process", "Errno " + ISTR(errno), SPELL_ERROR_PROCESS);
		}
    	DEBUG("[PRC] POpen in exec done");
        exit(1);
    }

    close(p_stdin[PROCESS_WRITE]);
    close(p_stdout[PROCESS_READ]);

    return pid;
}

//=============================================================================
// METHOD     : SPELLprocess::getInfo()
//=============================================================================
SPELLprocessInfo SPELLprocess::getInfo()
{
    SPELLprocessInfo info;

    info.m_processPID = this->m_processPID;
    info.m_identifier = this->m_identifier;
    info.m_command = this->m_command;
    info.m_retValue = this->m_retValue;
    info.m_status = this->m_status;
    info.m_statusCode = this->m_statusCode;
    info.m_killIt = this->m_killIt;

    return info;
}

