// ################################################################################
// FILE       : SPELLexecutorModel.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Information of a given executor
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

#ifndef __SPELL_EXEC_MODEL_H__
#define __SPELL_EXEC_MODEL_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// System includes -----------------------------------------------------------
// Project includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_SYN/SPELLmutex.H"
// Local includes ------------------------------------------------------------
#include "SPELL_CTX/SPELLexecutorConfiguration.H"
#include "SPELL_CTX/SPELLopenMode.H"

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
class SPELLexecutor;
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Executor information class
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLexecutorModel
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLexecutorModel( const SPELLexecutorConfiguration& config );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLexecutorModel();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		SPELLexecutorStatus getStatus();

		int getIpcKey() { return m_ipcKey; };
		int getIpcPort() { return m_ipcPort; };
		pid_t getPID() { return m_PID; };

		std::string getProcId() { return m_procId; };
		std::string getInstanceId() { return m_instanceId; };
		unsigned int getInstanceNum() { return m_instanceNum; };
		std::string getParentInstanceId() { return m_parentProcId; };
		std::string getArguments() { return m_arguments; };
		std::string getCondition() { return m_condition; };
		SPELLopenMode getOpenMode() { return m_openMode; };
		std::string getAsRunFilename() { return m_arFileName; }
		std::string getLogFilename() { return m_logFileName; };
		std::string getWsFilename() { return m_wsFileName; };
		std::string getConfigFile() { return m_configFile; };
		std::string getContextName() { return m_contextName; };
		std::string getTimeId() { return m_timeId; };

		void setIpcPort( int port ) { m_ipcPort = port; };
		void setPID( pid_t pid ) { m_PID = pid; };
		void setAsRunFilename( const std::string& filename ) { m_arFileName = filename; };
		void setWsFilename( const std::string& filename ) { m_wsFileName = filename; };
		void setLogFilename( const std::string& filename ) { m_logFileName = filename; };
		void setError( const std::string& error, const std::string& reason )
		{
			m_errorMsg = error;
			m_errorReason = reason;
		}

		std::string getErrorMessage() { return m_errorMsg; };
		std::string getErrorReason() { return m_errorReason; };

		//int getControllingClient TODO
		//Add controlling client

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

		friend class SPELLexecutor;

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		void setIpcKey( int ipcKey ) { m_ipcKey = ipcKey; };
		void setStatus( const SPELLexecutorStatus& status );

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

		int					m_ipcKey;
		pid_t               m_PID;
		int                 m_ipcPort;
		std::string         m_procId;
		std::string         m_instanceId;
		std::string         m_timeId;
		unsigned int        m_instanceNum;
		SPELLexecutorStatus m_status;
		std::string         m_parentProcId;
		std::string         m_arguments;
		SPELLopenMode		m_openMode;
		std::string         m_condition;
		std::string	        m_arFileName;
		std::string	        m_logFileName;
		std::string			m_wsFileName;
		std::string         m_configFile;
		SPELLmutex			m_lock;
		std::string			m_errorMsg;
		std::string			m_errorReason;
		std::string         m_contextName;
};

/*@}*/
#endif
