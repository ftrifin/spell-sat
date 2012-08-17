// ################################################################################
// FILE       : SPELLexecutorConfiguration.H
// DATE       : Apr 11, 2011
// PROJECT    : SPELL
// DESCRIPTION: Configuration for the executor process
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

#ifndef __SPELL_EXECUTOR_CONFIG_H__
#define __SPELL_EXECUTOR_CONFIG_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
#include "SPELL_CTX/SPELLclientMode.H"
#include "SPELL_CTX/SPELLopenMode.H"
// Project includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLutils.H"

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLexecutorConfiguration
{
public: //--------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLexecutorConfiguration( const std::string& instanceId, const std::string& timeId, bool reconnecting = false )
		{
			int idx = instanceId.find("#");
			fProcId = instanceId.substr(0, idx);
			fTimeId = timeId;
			fInstanceNum = STRI(instanceId.substr(idx+1,instanceId.size()-idx));
			fInstanceId = instanceId;
			fArguments = "";
			fCondition = "";
			fClientMode = CLIENT_MODE_UNKNOWN;
			fOpenMode = OPEN_MODE_UNKNOWN;
			fParentInstanceId = "";
			fConfigFile = "";
			fContextName = "";
			fRecoveryFile = "";
			fIpcPort = 0;
			fPID = 0;
			fReconnecting = reconnecting;
		}

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLexecutorConfiguration() {};

		//////////////////////////////////////////////////////////////////////
		/** Copy constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLexecutorConfiguration( const SPELLexecutorConfiguration& copy )
		{
			fProcId = copy.fProcId;
			fTimeId = copy.fTimeId;
			fInstanceNum = copy.fInstanceNum;
			fInstanceId = copy.fInstanceId;
			fArguments = copy.fArguments;
			fCondition = copy.fCondition;
			fClientMode = copy.fClientMode;
			fOpenMode = copy.fOpenMode;
			fParentInstanceId = copy.fParentInstanceId;
			fConfigFile = copy.fConfigFile;
			fContextName = copy.fContextName;
			fRecoveryFile = copy.fRecoveryFile;
			fIpcPort = copy.fIpcPort;
			fPID = copy.fPID;
			fReconnecting = copy.fReconnecting;
		}

	// METHODS ///////////////////////////////////////////////////////////////

		std::string getProcId() const { return fProcId; };
		std::string getTimeId() const { return fTimeId; };
		std::string getInstanceId() const { return fInstanceId; };
		unsigned int getInstanceNum() const { return fInstanceNum; };

		pid_t getPID() const { return fPID; };
		int getIpcPort() const { return fIpcPort; };
		bool isReconnecting() const { return fReconnecting; }

		std::string getArguments() const { return fArguments; };
		std::string getCondition() const { return fCondition; };
		SPELLclientMode getClientMode() const { return fClientMode; };
		SPELLopenMode getOpenMode() const { return fOpenMode; };
		std::string getParentInstanceId() const { return fParentInstanceId; };
		std::string getConfigFile() const { return fConfigFile; };
		std::string getContextName() const { return fContextName; };
		std::string getRecoveryFile() const { return fRecoveryFile; };

		void setPID( pid_t pid ) { fPID = pid; };
		void setIpcPort( int ipcPort ) { fIpcPort = ipcPort; };
		void setArguments( const std::string& arguments ) { fArguments = arguments; };
		void setCondition( const std::string& condition ) { fCondition = condition; };
		void setClientMode( const SPELLclientMode& mode ) { fClientMode = mode; };
		void setOpenMode( const SPELLopenMode& mode ) { fOpenMode = mode; };
		void setParentInstanceId( const std::string& parentInstanceId ) { fParentInstanceId = parentInstanceId; };
		void setConfigFile( const std::string& configFile ) { fConfigFile = configFile; };
		void setContextName( const std::string& ctxName ) { fContextName = ctxName; };
		void setRecoveryFile( const std::string& recoveryFile ) { fRecoveryFile = recoveryFile; };

private: //-------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// STATIC ////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////
	// DATA MEMBERS //////////////////////////////////////////////////////////

		std::string     fProcId;
		unsigned int    fInstanceNum;
		int fIpcPort;
		pid_t fPID;
		bool fReconnecting;
		std::string		fInstanceId;
		std::string     fTimeId;
		std::string     fArguments;
		std::string     fCondition;
		SPELLclientMode fClientMode;
		SPELLopenMode   fOpenMode;
		std::string     fParentInstanceId;
		std::string     fConfigFile;
		std::string     fContextName;
		std::string		fRecoveryFile;
};

/*@}*/
#endif
