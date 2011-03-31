// ################################################################################
// FILE       : SPELLprocess.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of process model
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
// CONSTRUCTOR: SPELLprocess::SPELLprocess
//=============================================================================
SPELLprocess::SPELLprocess( std::string identifier, std::string command, SPELLprocessManager* mgr )
    : SPELLthread("process-" + identifier)
{
    m_pManager = mgr;
    m_retValue = -1;
    m_identifier = identifier;
    m_command = command;
    m_processPID = 0;
    m_killIt = false;
    m_endEvent.clear();
    DEBUG("[PRC] PROCESS " + identifier + " CREATED");
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

    m_status = 0;
    m_processPID = popen( m_command );

    DEBUG("[PRC] Waiting PID");
    waitpid( m_processPID, &m_status, WNOHANG);

    // First thing, check if the command has exited already at this point
    // if the return value is 127,
    if (WIFEXITED(m_status))
    {
        m_retValue = WEXITSTATUS(m_status);
        DEBUG("[PRC] Process exited before check loop");
        if ( m_retValue == SHELL_NOTFOUND)
        {
            DEBUG("[PRC] The process has failed");
            m_retValue = -1;
            m_pManager->fireProcessFailed( m_identifier );
            m_endEvent.set();
            return;
        }
    }
    DEBUG("[PRC] The process has started, starting check");
    m_pManager->fireProcessStarted( m_identifier );
    bool repeat = false;
    do
    {
        m_status = 0;
        repeat = false;
        DEBUG("[PRC] Waiting PID #2");
        int result = waitpid( m_processPID, &m_status, WUNTRACED );
        DEBUG("[PRC] Waiting PID exited");
        if (result<0)
        {
            m_retValue = -1;
            DEBUG("[PRC] The process has been killed");
            m_pManager->fireProcessKilled( m_identifier );
        }
        else if (WIFEXITED(m_status))
        {
            m_retValue = WEXITSTATUS(m_status);
            DEBUG("[PRC] The process has finished with exit code " + ISTR(m_retValue));
            if (m_retValue == SHELL_NOTFOUND )
            {
                DEBUG("[PRC] Command not found");
            	m_retValue = -1;
                m_pManager->fireProcessFailed( m_identifier );
            }
            else
            {
            	m_pManager->fireProcessFinished( m_identifier, m_retValue );
            }
        }
        else if (WIFSIGNALED(m_status))
        {
            m_retValue = -1;
            DEBUG("[PRC] The process has been killed");
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
            DEBUG("[PRC] The process has been killed");
            m_pManager->fireProcessKilled( m_identifier );
        }
    }
    while(repeat == true);
    DEBUG("[PRC] Process monitoring finished");
    m_endEvent.set();
    DEBUG("[PRC] Exiting monitoring thread");
}

//=============================================================================
// METHOD     : SPELLprocess::kill()
//=============================================================================
void SPELLprocess::kill()
{
    m_killIt = true;
    DEBUG("[PRC] Sending KILL signal");
    ::kill( m_processPID, SIGKILL );
}

//=============================================================================
// METHOD     : SPELLprocess::wait()
//=============================================================================
int SPELLprocess::wait()
{
    DEBUG("[PRC] (wait) Waiting process to finish");
    try{ join(); } catch(SPELLsyncError& err) {;};
    DEBUG("[PRC] (wait) Process finished with code " + ISTR(m_retValue));
    return m_retValue;
}

//=============================================================================
// METHOD     : SPELLprocess::popen()
//=============================================================================
pid_t SPELLprocess::popen( std::string command )
{
    int p_stdin[2], p_stdout[2];
    pid_t pid;

    if (pipe(p_stdin) != 0 || pipe(p_stdout) != 0)
    {
        throw SPELLcoreException("Cannot launch process", "Failed to setup standard pipes");
    }

    pid = fork();

    if (pid < 0)
    {
        return pid;
    }
    else if (pid == 0)
    {
        close(p_stdin[PROCESS_WRITE]);
        dup2(p_stdin[PROCESS_READ], PROCESS_READ);
        close(p_stdout[PROCESS_READ]);
        dup2(p_stdout[PROCESS_WRITE], PROCESS_WRITE);
        execl("/bin/sh", "sh", "-c", command.c_str(), NULL);
        exit(1);
    }

    close(p_stdin[PROCESS_WRITE]);
    close(p_stdout[PROCESS_READ]);

    return pid;
}

