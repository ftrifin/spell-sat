// ################################################################################
// FILE       : SPELLcontext.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the context main class
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
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_CTX/SPELLclientManager.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
#include "SPELL_CTX/SPELLdataHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Listener.H"
#include "SPELL_SDB/SPELLdatabaseFactory.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////
#define NAME std::string("[CTX] ")
#define LIST_SEPARATOR "\3"

SPELLcontext* SPELLcontext::s_instance = NULL;


//=============================================================================
// CONSTRUCTOR: SPELLcontext::SPELLcontext()
//=============================================================================
SPELLcontext::SPELLcontext()
: m_clientIPC(),
  m_listenerIPC(),
  m_dataChunker(100)
{

}

//=============================================================================
// DESTRUCTOR: SPELLcontext::~SPELLcontext()
//=============================================================================
SPELLcontext::~SPELLcontext()
{
}

//=============================================================================
// STATIC: SPELLcontext::instance()
//=============================================================================
SPELLcontext& SPELLcontext::instance()
{
	if (s_instance == NULL)
	{
		s_instance = new SPELLcontext();
	}
	return *s_instance;
}

//=============================================================================
// METHOD: SPELLcontext::start()
//=============================================================================
void SPELLcontext::start( const SPELLcontextConfiguration& config )
{
	LOG_INFO("Starting context " + getContextName() );

	// Store the configuration
	m_config = config;

	// Setup the configuration
	SPELLconfiguration::instance().loadConfig(m_config.configFile);

	// Preload the max. amount of active procedures
	std::string drvName = SPELLconfiguration::instance().getContext(m_config.contextName).getDriverName();
	SPELLdriverConfig& driverConfig = SPELLconfiguration::instance().getDriver(drvName);
	m_maxProcs = driverConfig.getMaxProcs();

	// Initialize Python support (some client requests require Python API)
    SPELLpythonHelper::instance().initialize();

	// Setup the procedure manager
	SPELLprocedureManager::instance().setup( getContextName() );

	// Setup the executor manager. This may take time to come back, if
	// there are executors to reconnect to. The listener login message will be sent after this stage.
	SPELLexecutorManager::instance().setup( getContextName() );

	// Setup the IPC interfaces to clients and listener. The listener one logs in.
	m_clientIPC.setup();
	m_listenerIPC.setup();

	LOG_INFO("Context " + getContextName() + " ready" );
}

//=============================================================================
// METHOD: SPELLcontext::stop()
//=============================================================================
void SPELLcontext::stop()
{
	LOG_INFO("Stopping context " + getContextName() );
	SPELLclientManager::instance().removeAllClients();
	SPELLexecutorManager::instance().cleanup();
	m_clientIPC.cleanup();
	SPELLexecutorManager::instance().killAll();
	m_listenerIPC.cleanup();
	// Cleanup Python API
	SPELLpythonHelper::instance().finalize();
	LOG_INFO("Context " + getContextName() + " stopped" );
}

//=============================================================================
// METHOD: SPELLcontext::getNumActiveProcedures()
//=============================================================================
unsigned int SPELLcontext::getNumActiveProcedures()
{
	return SPELLexecutorManager::instance().getNumActiveExecutors();
}

//=============================================================================
// METHOD: SPELLcontext::readyToFinish()
//=============================================================================
void SPELLcontext::readyToFinish()
{
	LOG_INFO("Context ready to finish " + getContextName() );
	m_eventFinish.set();
}

//=============================================================================
// METHOD: SPELLcontext::waitFinish()
//=============================================================================
void SPELLcontext::waitFinish()
{
	m_eventFinish.clear();
	m_eventFinish.wait();
}

//=============================================================================
// METHOD: SPELLcontext::openExecutor
//=============================================================================
SPELLipcMessage SPELLcontext::openExecutor( const SPELLipcMessage& msg, SPELLclient* controllingClient )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	DEBUG( NAME + "Requested opening new executor");

	// Check maximum amount of procs per context (driver specific)
	if (m_maxProcs == getNumActiveProcedures())
	{
		LOG_ERROR(NAME + "Cannot open: maximum number of procedures reached");
	    resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_OPEN_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot open executor");
		resp.set( MessageField::FIELD_REASON, "Maximum amount of active procedures reached (" + ISTR(m_maxProcs) + ")" );
		return resp;
	}

	std::string theInstanceId = "";
	std::string theParentInstanceId = "";
	if (msg.hasField( MessageField::FIELD_SPROC_ID ))
	{
		theInstanceId = msg.get( MessageField::FIELD_SPROC_ID );
		theParentInstanceId = msg.get( MessageField::FIELD_PROC_ID );
	}
	else
	{
		theInstanceId = msg.get( MessageField::FIELD_PROC_ID );
	}

	SPELLexecutorConfiguration config(theInstanceId, SPELLutils::fileTimestamp() );

	config.setArguments(msg.get( MessageField::FIELD_ARGS ));
	config.setCondition(msg.get( MessageField::FIELD_CONDITION ));
	std::string cMode = msg.get( MessageField::FIELD_GUI_MODE );
	config.setClientMode(SPELLdataHelper::clientModeFromString(cMode));
	std::string oMode = msg.get( MessageField::FIELD_OPEN_MODE );
	config.setOpenMode(SPELLdataHelper::openModeFromString(oMode));
	config.setParentInstanceId(theParentInstanceId);

	DEBUG("		- Instance Id : " + config.getInstanceId());
	DEBUG("		- Time Id     : " + config.getTimeId());
	DEBUG("		- Proc Id     : " + config.getProcId());
	DEBUG("		- Instance num: " + ISTR(config.getInstanceNum()));
	DEBUG("		- Arguments   : " + config.getArguments());
	DEBUG("		- Condition   : " + config.getCondition());
	DEBUG("		- Client mode : " + cMode);
	DEBUG("		- Open mode   : " + oMode);
	DEBUG("		- Parent      : " + config.getParentInstanceId());

	try
	{
		SPELLexecutorManager::instance().startExecutor( config, controllingClient );
		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_OPEN_EXEC, msg );

		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(config.getInstanceId());
		SPELLexecutorStatus initialStatus = exec->getStatus();

		int clientKey = -1;
		if (controllingClient)
		{
			SPELLclientManager::instance().setExecutorController( controllingClient, exec );
			clientKey = controllingClient->getClientKey();
		}
		else
		{
			config.setClientMode(CLIENT_MODE_UNKNOWN);
		}
		// Notify other clients
		notifyExecutorOperation( config.getInstanceId(),
								 theParentInstanceId,
								 clientKey,
								 config.getClientMode(),
								 initialStatus,
								 EXEC_OP_OPEN,
								 config.getCondition() );

	}
	catch( SPELLcoreException& err )
	{
	    resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_OPEN_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot open executor");
		resp.set( MessageField::FIELD_REASON, err.what() );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::closeExecutor( const SPELLipcMessage& msg )
{
	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested closing executor " + instanceId );
	SPELLipcMessage resp = VOID_MESSAGE;
	try
	{
		// Check if there is a parent to notify
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor( instanceId );
		std::string parentInstanceId = exec->getParentInstanceId();

		DEBUG( NAME + "Removing monitoring clients");
		// Unsubscribe any monitoring client
		std::list<int> mclients = SPELLclientManager::instance().getMonitoringClientsKeys( instanceId );
		std::list<int>::iterator it;
		for( it = mclients.begin(); it != mclients.end(); it++ )
		{
			SPELLclient* client = SPELLclientManager::instance().getClient(*it);
			SPELLclientManager::instance().stopMonitorExecutor( client, exec );
		}

		DEBUG( NAME + "Removing controlling client");
		SPELLclient* client = exec->getControllingClient();
		if (client)
		{
			SPELLclientManager::instance().removeExecutorController( client, exec, true );
		}

		DEBUG( NAME + "Closing executor");
		SPELLexecutorManager::instance().closeExecutor( instanceId );

		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_CLOSE_EXEC, msg );

		DEBUG( NAME + "Notifying operation");
		// Notify other clients
		notifyExecutorOperation( instanceId, parentInstanceId,
				                 msg.getKey(),
				                 CLIENT_MODE_UNKNOWN,
				                 STATUS_UNINIT,
				                 EXEC_OP_CLOSE, "");
	}
	catch( SPELLcoreException& err )
	{
	  	resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_CLOSE_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot close executor");
		resp.set( MessageField::FIELD_REASON, err.what() );
	}
	DEBUG( NAME + "Request to close executor finished");
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::killExecutor( const SPELLipcMessage& msg )
{
	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested killing executor " + instanceId );
	SPELLipcMessage resp = VOID_MESSAGE;
	try
	{
		// Check if there is a parent to notify first
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor( instanceId );
		std::string parentInstanceId = exec->getParentInstanceId();

		// Unsubscribe any monitoring client
		std::list<int> mclients = SPELLclientManager::instance().getMonitoringClientsKeys( instanceId );
		std::list<int>::iterator it;
		for( it = mclients.begin(); it != mclients.end(); it++ )
		{
			SPELLclient* client = SPELLclientManager::instance().getClient(*it);
			SPELLclientManager::instance().stopMonitorExecutor( client, exec );
		}

		SPELLclient* client = exec->getControllingClient();
		if (client)
		{
			SPELLclientManager::instance().removeExecutorController( client, exec, false );
		}

		SPELLexecutorManager::instance().killExecutor( instanceId );

		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_KILL_EXEC, msg );

		// Notify other clients
		notifyExecutorOperation( instanceId, parentInstanceId,
				                 msg.getKey(),
				                 CLIENT_MODE_UNKNOWN,
				                 STATUS_UNINIT,
				                 EXEC_OP_KILL, "");
	}
	catch( SPELLcoreException& err )
	{
	  	resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_ATTACH_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot kill to executor");
		resp.set( MessageField::FIELD_REASON, "Cannot find executor " + instanceId );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::recoverExecutor
//=============================================================================
SPELLipcMessage SPELLcontext::recoverExecutor( const SPELLipcMessage& msg, SPELLclient* controllingClient )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	DEBUG( NAME + "Requested recovering executor");

	// Check maximum amount of procs per context (driver specific)
	if (m_maxProcs == getNumActiveProcedures())
	{
	    resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_RECOVER_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot recover executor");
		resp.set( MessageField::FIELD_REASON, "Maximum amount of active procedures reached (" + ISTR(m_maxProcs) + ")" );
		return resp;
	}

	SPELLexecutorConfiguration config(msg.get( MessageField::FIELD_PROC_ID ), SPELLutils::fileTimestamp());

	config.setClientMode(CLIENT_MODE_CONTROL);
	config.setOpenMode((SPELLopenMode)(OPEN_MODE_VISIBLE | OPEN_MODE_BLOCKING));
	config.setRecoveryFile(msg.get( MessageField::FIELD_FILE_NAME ));

	LOG_INFO("RECOVER FILE " + config.getRecoveryFile() );

	DEBUG("		- Instance Id : " + config.getInstanceId());
	DEBUG("		- Time Id     : " + config.getTimeId());
	DEBUG("		- Proc Id     : " + config.getProcId());
	DEBUG("		- Instance num: " + ISTR(config.getInstanceNum()));
	DEBUG("		- Arguments   : " + config.getArguments());
	DEBUG("		- Condition   : " + config.getCondition());
	DEBUG("		- Client mode : " + SPELLdataHelper::clientModeToString(config.getClientMode()));
	DEBUG("		- Open mode   : " + SPELLdataHelper::openModeToString(config.getOpenMode()));
	DEBUG("		- Parent      : " + config.getParentInstanceId());

	try
	{
		checkRecoveryFiles( config.getRecoveryFile() );

		SPELLexecutorManager::instance().recoverExecutor( config, controllingClient );
		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_RECOVER_EXEC, msg );

		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(config.getInstanceId());
		SPELLexecutorStatus initialStatus = exec->getStatus();

		int clientKey = controllingClient->getClientKey();
		SPELLclientManager::instance().setExecutorController( controllingClient, exec );

		// Notify other clients
		notifyExecutorOperation( config.getInstanceId(),
				                 config.getParentInstanceId(),
								 clientKey,
								 config.getClientMode(),
								 initialStatus,
								 EXEC_OP_OPEN,
								 config.getCondition() );

	}
	catch( SPELLcoreException& err )
	{
	    resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_RECOVER_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot recover executor");
		resp.set( MessageField::FIELD_REASON, err.what() );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::checkRecoveryFiles( const std::string& filename )
{
	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);

	std::string wsDataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ws");
	std::string arDataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ar");

	bool wsdFound = false;
	bool wssFound = false;
	bool wspFound = false;
	bool arfFound = false;

	std::list<std::string> files = SPELLutils::getFilesInDir(wsDataDir);
	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string completeFilename = *it;
		std::string path = wsDataDir + PATH_SEPARATOR + completeFilename;
		if (completeFilename.find(filename) != std::string::npos)
		{
			if (!wssFound && + (completeFilename.find(".wss") != std::string::npos))
			{
				wssFound = true;
				if (SPELLutils::fileSize(path)==0)
				{
					THROW_EXCEPTION("Recovery file check failed", "WSS data file has zero bytes", SPELL_ERROR_WSTART);
				}
			}
			else if (!wsdFound && + (completeFilename.find(".wsd") != std::string::npos))
			{
				wsdFound = true;
				if (SPELLutils::fileSize(path)==0)
				{
					THROW_EXCEPTION("Recovery file check failed", "WSD data file has zero bytes", SPELL_ERROR_WSTART);
				}
			}
			if (!wspFound && + (completeFilename.find(".wsp") != std::string::npos))
			{
				wspFound = true;
				if (SPELLutils::fileSize(path)==0)
				{
					THROW_EXCEPTION("Recovery file check failed", "WSP data file has zero bytes", SPELL_ERROR_WSTART);
				}
			}
		}
	}
	if (!wssFound || !wsdFound || !wspFound)
	{
		THROW_EXCEPTION("Recovery file check failed", "Warmstart files missing", SPELL_ERROR_WSTART);
	}

	files = SPELLutils::getFilesInDir(arDataDir);
	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string completeFilename = *it;
		std::string path = arDataDir + PATH_SEPARATOR + completeFilename;
		if (completeFilename == filename + ".ASRUN")
		{
			arfFound = true;
			if (SPELLutils::fileSize(path)==0)
			{
				THROW_EXCEPTION("Recovery file check failed", "ASRUN file has zero bytes: '" + completeFilename + "'", SPELL_ERROR_WSTART);
			}
			break;
		}
	}
	if (!arfFound)
	{
		THROW_EXCEPTION("Recovery file check failed", "ASRUN file missing: '" + arDataDir + PATH_SEPARATOR + filename + ".ASRUN'", SPELL_ERROR_WSTART);
	}
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getProcedureList( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested list of procedures");
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLprocedureManager::instance().refresh();
	SPELLprocedureManager::ProcList list = SPELLprocedureManager::instance().getProcList();

	std::string listStr = "";
	SPELLprocedureManager::ProcList::const_iterator it;
	for( it = list.begin(); it != list.end(); it++)
	{
		if (listStr != "") listStr += LIST_SEPARATOR;
		listStr += *it;
	}
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_PROC_LIST, msg );
	resp.set( MessageField::FIELD_PROC_LIST, listStr );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getProcedureRecoveryList( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested list of procedure recovery files");
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);
	std::string dataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ws");

	std::list<std::string> files = SPELLutils::getFilesInDir(dataDir);
	std::list<std::string> wsfiles;
	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string filename = *it;
		if (filename.find(".wsp") != std::string::npos )
		{
			wsfiles.push_back(filename);
		}
	}

	std::string listStr = "";
	for(std::list<std::string>::const_iterator it = wsfiles.begin(); it != wsfiles.end(); it++)
	{
		if (listStr != "") listStr += LIST_SEPARATOR;
		listStr += *it;
	}
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_RECOVERY_LIST, msg );
	resp.set( MessageField::FIELD_FILE_LIST, listStr );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::listFiles( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	std::string dir = msg.get(MessageField::FIELD_DIR_NAME);

	DEBUG( NAME + "Requested list of files in directory " + dir);

	std::string fileList = "";
	if (SPELLutils::pathExists(dir))
	{
		std::list<std::string> files = SPELLutils::getFilesInDir(dir);
		std::list<std::string>::iterator it;
		for( it = files.begin(); it != files.end(); it++ )
		{
			if (fileList.size()>0) fileList += LIST_SEPARATOR;
			fileList += *it;
		}
		files = SPELLutils::getSubdirs(dir);
		for( it = files.begin(); it != files.end(); it++ )
		{
			if (fileList.size()>0) fileList += LIST_SEPARATOR;
			fileList += "+" + *it;
		}
	}
	else
	{
		LOG_ERROR("Path does not exist, cannot list files: '" + dir + "'");
	}
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_LIST_FILES, msg );
	resp.set( MessageField::FIELD_FILE_LIST, fileList );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::listDataDirectories( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested list of data directories");
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);
	std::list<std::string> dataLocations = ctxConfig.getLocations();

	std::list<std::string>::iterator it;
	std::string dirList = "";
	// To avoid duplicated location paths (may happen in config)
	std::list<std::string> alreadyProcessed;
	for(it = dataLocations.begin(); it != dataLocations.end(); it++)
	{
		std::string locationPath = ctxConfig.getLocationPath(*it);
		if ( std::find( alreadyProcessed.begin(), alreadyProcessed.end(), locationPath ) == alreadyProcessed.end() )
		{
			if (dirList.size()>0) dirList += LIST_SEPARATOR;
			dirList += SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + locationPath;
			alreadyProcessed.push_back(locationPath);
			DEBUG( "    - " + SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + locationPath);
		}
	}

	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_LIST_DATADIRS, msg );
	resp.set( MessageField::FIELD_FILE_LIST, dirList );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::deleteRecoveryFiles()
//=============================================================================
SPELLipcMessage SPELLcontext::deleteRecoveryFiles( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested delete recovery files");
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);

	std::string fileName = msg.get(MessageField::FIELD_FILE_NAME);
	fileName = fileName.substr(0,fileName.length()-4);

	// Delete warmstart files
	std::string wsDataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ws");
	std::list<std::string> files = SPELLutils::getFilesInDir(wsDataDir);

	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string completeFilename = *it;
		if (completeFilename.find(fileName) != std::string::npos)
		{
			std::string path = wsDataDir + PATH_SEPARATOR + completeFilename;
			if (SPELLutils::pathExists(path))
			{
				SPELLutils::deleteFile(path);
			}
		}
	}

	// Delete Asrun files
	std::string arDataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ar");

	files = SPELLutils::getFilesInDir(arDataDir);

	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string completeFilename = *it;
		if (completeFilename.find(fileName) != std::string::npos)
		{
			std::string path = arDataDir + PATH_SEPARATOR + completeFilename;
			if (SPELLutils::pathExists(path))
			{
				SPELLutils::deleteFile(path);
			}
		}
	}

	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_DELETE_RECOVERY, msg );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getExecutorList( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested list of executors");
	SPELLipcMessage resp = VOID_MESSAGE;
	SPELLexecutorManager::ExecList list = SPELLexecutorManager::instance().getExecutorList();
	std::string listStr = "";
	SPELLexecutorManager::ExecList::const_iterator it;
	for( it = list.begin(); it != list.end(); it++)
	{
		if (listStr != "") listStr += LIST_SEPARATOR;
		DEBUG("    found " + *it);
		listStr += *it;
	}
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_EXEC_LIST, msg );
	resp.set( MessageField::FIELD_EXEC_LIST, listStr );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getProcedureProperties( const SPELLipcMessage& msg )
{
	std::string procId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested procedure properties for " + procId);
	SPELLipcMessage resp = VOID_MESSAGE;
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_PROC_PROP, msg );
	SPELLprocedure::PropertyKeys keys = SPELLprocedureManager::instance().getPropertyKeys( procId );
	SPELLprocedure::PropertyKeys::iterator it;
	for( it = keys.begin(); it != keys.end(); it++)
	{
		std::string key = *it;
		const std::string value = SPELLprocedureManager::instance().getProperty( procId, key );
		key = SPELLutils::toLower(key);
		resp.set( key, value );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getExecutorInfo( const SPELLipcMessage& msg )
{
	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	SPELLipcMessage resp = SPELLipcHelper::createResponse( ContextMessages::RSP_EXEC_INFO, msg);
	DEBUG( NAME + "Requested executor information: " + instanceId);
	SPELLexecutorManager::instance().buildExecutorInfo( instanceId, resp );
	SPELLclientManager::instance().completeMonitoringInfo( instanceId, resp );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::attachExecutor( const SPELLipcMessage& msg, SPELLclient* client )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	std::string clientModeStr = msg.get( MessageField::FIELD_GUI_MODE );
	DEBUG( NAME + "Requested attaching to executor: " + instanceId + " in mode " + clientModeStr );

	SPELLclientMode mode = SPELLdataHelper::clientModeFromString( clientModeStr );

	try
	{
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(instanceId);

		if (mode == CLIENT_MODE_CONTROL)
		{
			if (exec->hasControllingClient())
			{
				LOG_ERROR("Cannot attach, already controlled: " + instanceId );
				resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_ATTACH_EXEC, msg );
				resp.set( MessageField::FIELD_ERROR, "Cannot attach to executor in controlling mode");
				resp.set( MessageField::FIELD_REASON, "Executor is already controlled" );
				resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
			}
			else
			{
				// Set the procedure controlling client on the client model
				client->addProcedure(instanceId, CLIENT_MODE_CONTROL);
				// Set the procedure controlling client on the executor model
				LOG_INFO("Client " + ISTR(client->getClientKey()) + " controlling executor " + instanceId);
				exec->setControllingClient( client );
				resp = SPELLipcHelper::createResponse( ContextMessages::RSP_ATTACH_EXEC, msg );
				// Add the executor information
				SPELLexecutorManager::instance().buildExecutorInfo( instanceId, resp );
				// Add the GUI list
				SPELLclientManager::instance().completeMonitoringInfo( instanceId, resp );
				// Notify other clients
				notifyExecutorOperation( instanceId, exec->getParentInstanceId(),
						                 msg.getKey(),
						                 CLIENT_MODE_CONTROL,
						                 exec->getStatus(),
						                 EXEC_OP_ATTACH, "");
			}
		}
		else
		{
			// Set the procedure controlling client on the client model
			client->addProcedure(instanceId, CLIENT_MODE_MONITOR);

			SPELLexecutorStatus st = exec->getStatus();

			LOG_INFO("Client " + ISTR(client->getClientKey()) + " monitoring executor " + instanceId);

			resp = SPELLipcHelper::createResponse( ContextMessages::RSP_ATTACH_EXEC, msg );
			// Add the executor information
			SPELLexecutorManager::instance().buildExecutorInfo( instanceId, resp );
			// Add the GUI list
			SPELLclientManager::instance().completeMonitoringInfo( instanceId, resp );
			// Monitor the executor
			SPELLclientManager::instance().startMonitorExecutor( client, exec );
			// Notify other clients
			notifyExecutorOperation( instanceId, exec->getParentInstanceId(),
									 msg.getKey(),
									 CLIENT_MODE_MONITOR,
									 st, EXEC_OP_ATTACH, "");
		}
	}
	catch( SPELLcoreException& ex )
	{
		LOG_ERROR("Cannot attach: " + std::string(ex.what()) );
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_ATTACH_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot attach to executor");
		resp.set( MessageField::FIELD_REASON, ex.what());
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::clientLost( int clientKey )
{
	DEBUG("Client " + ISTR(clientKey) + " lost, stopping procedures");
	SPELLclient* client = SPELLclientManager::instance().getClient( clientKey );
	if (client)
	{
		// Remove the client from all procedures associated to it
		SPELLclient::ProcedureList procs = client->getProcedures();
		SPELLclient::ProcedureList::iterator it;
		for ( it = procs.begin(); it != procs.end(); it++ )
		{
			try
			{
				SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor( it->first );
				if (it->second == CLIENT_MODE_CONTROL )
				{
					SPELLclientManager::instance().removeExecutorController(client, exec, true);
					LOG_WARN("Client " + ISTR(clientKey) + " stop controlling executor " + exec->getModel().getInstanceId());
				}
				else
				{
					SPELLclientManager::instance().stopMonitorExecutor(client, exec);
					LOG_INFO("Client " + ISTR(clientKey) + " stop monitoring executor " + exec->getModel().getInstanceId());
				}
			}
			catch(SPELLcoreException& ex){};
		}
		DEBUG("Client " + ISTR(clientKey) + " lost, stopping procedures done");
	}
	else
	{
		LOG_ERROR("Unable to stop procedures for client " + ISTR(clientKey) + " client not found");
	}
	// Now remove the client from the client manager
	SPELLclientManager::instance().clientLost(clientKey);
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::executorLost( const std::string& instanceId )
{
	try
	{
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor( instanceId );

		// Remove the controlling client about the loss
		SPELLclient* client = exec->getControllingClient();
		if (client)
		{
			SPELLclientManager::instance().removeExecutorController( client, exec, false );
		}

		// Remove all monitoring clients
		std::list<int> mkeys = SPELLclientManager::instance().getMonitoringClientsKeys( instanceId );
		std::list<int>::iterator it;
		for( it = mkeys.begin(); it != mkeys.end(); it++ )
		{
			SPELLclient* mclient = SPELLclientManager::instance().getClient(*it);
			SPELLclientManager::instance().stopMonitorExecutor(mclient, exec);
		}

		// Notify all GUIs in the system
		notifyExecutorOperation( instanceId, exec->getParentInstanceId(),
		     			         -1, CLIENT_MODE_UNKNOWN, STATUS_ERROR,
								 EXEC_OP_CRASH, "" );

		// Mark the model to be removed (cannot delete now, since this operation is
		// triggered from the process model itself
		SPELLexecutorManager::instance().clearExecutor( instanceId );
	}
	catch(SPELLcoreException& ex){};
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::detachExecutor( const SPELLipcMessage& msg, SPELLclient* client )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested detaching from executor: " + instanceId );

	SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(instanceId);

	try
	{
		LOG_INFO("Client " + ISTR(client->getClientKey()) + " detached from executor " + instanceId);
		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_DETACH_EXEC, msg);

		if (exec->getControllingClient() == client)
		{
			SPELLclientManager::instance().removeExecutorController( client, exec, true );
		}
		else
		{
			SPELLclientManager::instance().stopMonitorExecutor( client, exec );
		}

		// Notify other clients
		notifyExecutorOperation( instanceId, exec->getParentInstanceId(), msg.getKey(),
				                 CLIENT_MODE_UNKNOWN,
				                 exec->getStatus(),
				                 EXEC_OP_DETACH, "");
	}
	catch( SPELLcoreException& ex )
	{
		LOG_ERROR("Cannot detach: " + std::string(ex.what()) );
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_DETACH_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot detach from executor");
		resp.set( MessageField::FIELD_REASON, ex.what() );
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::removeControl( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested remove control from executor: " + instanceId );

	SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(instanceId);

	try
	{
		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_REMOVE_CONTROL, msg);
		SPELLclient* client = exec->getControllingClient();

		if (client)
		{
			LOG_INFO("Client " + ISTR(client->getClientKey()) + " removed from executor " + instanceId);
			SPELLclientManager::instance().removeExecutorController( client, exec, true );
			// Notify other clients
			notifyExecutorOperation( instanceId, exec->getParentInstanceId(), msg.getKey(),
					                 CLIENT_MODE_UNKNOWN,
					                 exec->getStatus(),
					                 EXEC_OP_DETACH, "");
		}

	}
	catch( SPELLcoreException& ex )
	{
		LOG_ERROR("Cannot remove control: " + std::string(ex.what()) );
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_REMOVE_CONTROL, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot remove control");
		resp.set( MessageField::FIELD_REASON, ex.what() );
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getClientInfo( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;
	std::string clientKeyStr = msg.get( MessageField::FIELD_GUI_KEY );

	if (clientKeyStr != "")
	{
		std::vector<std::string> tok = SPELLutils::tokenize( clientKeyStr, ":" );
		int clientKey = -1;
		if (tok.size()==2) clientKey = STRI( tok[1] );
		SPELLclient* client = SPELLclientManager::instance().getClient(clientKey);
		if (client)
		{
			resp = SPELLipcHelper::createResponse( ContextMessages::RSP_CLIENT_INFO, msg);
			resp.set( MessageField::FIELD_GUI_KEY, ISTR(clientKey) );
			resp.set( MessageField::FIELD_GUI_MODE, "" ); // to be removed
			resp.set( MessageField::FIELD_HOST, client->getClientHost() );
		}
		else
		{
			resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_CLIENT_INFO, msg );
			resp.set( MessageField::FIELD_ERROR, "Cannot get client information");
			resp.set( MessageField::FIELD_REASON, "No such client: " + clientKeyStr );
			resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
		}
	}
	else
	{
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_CLIENT_INFO, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot get client information");
		resp.set( MessageField::FIELD_REASON, "No client key given" );
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getInstanceId( const SPELLipcMessage& msg )
{
    // Create the login message
    std::string requestId = msg.getId();
    SPELLipcMessage resp = VOID_MESSAGE;

	DEBUG( NAME + "Requested new instance id");
	std::string procId = msg.get( MessageField::FIELD_PROC_ID );
	try
	{
		// Ensure that the procedure id exists
		SPELLprocedureManager::instance().getProcName(procId);
		std::string instanceId = SPELLexecutorManager::instance().getInstanceId( procId );
		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_INSTANCE_ID, msg );
		resp.set( MessageField::FIELD_INSTANCE_ID, instanceId );
	}
	catch(SPELLcoreException& ex)
	{
		resp = SPELLipcHelper::createErrorResponse(ContextMessages::RSP_INSTANCE_ID, msg);
		resp.set(MessageField::FIELD_ERROR, "Cannot get instance number");
		resp.set(MessageField::FIELD_REASON, "Procedure does not exist: '" + procId + "'");
		resp.set(MessageField::FIELD_FATAL, "true");
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getProcedureCode( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;
	DEBUG( NAME + "Requested procedure source code");
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_PROC_CODE, msg);
	std::string procId = msg.get( MessageField::FIELD_PROC_ID );
	SPELLipcDataChunk::DataList data;

	if (msg.get( MessageField::FIELD_CHUNK ) != "")
	{
		int chunkNo = STRI( msg.get( MessageField::FIELD_CHUNK ) );
		DEBUG( NAME + "Get code chunk " + ISTR(chunkNo));
		data = m_dataChunker.getChunk( procId, chunkNo );
		int totalChunks = m_dataChunker.getSize( procId );
		if (chunkNo == (totalChunks-1)) m_dataChunker.endChunks(procId);
		resp.set( MessageField::FIELD_CHUNK, ISTR(chunkNo) );
		resp.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
	}
	else
	{
		SPELLprocedureSourceCode source = SPELLprocedureManager::instance().getSourceCode( procId );
		std::vector<std::string> lines = source.getSourceCodeLines();
		int totalChunks = m_dataChunker.startChunks( procId, lines );
		resp.set( MessageField::FIELD_CHUNK, "0" );

		DEBUG( NAME + "Start chunks " + ISTR(totalChunks));

		if (totalChunks == 0)
		{
			resp.set( MessageField::FIELD_TOTAL_CHUNKS, "0");
			data = source.getSourceCodeLines();
		}
		else
		{
			resp.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
			data = m_dataChunker.getChunk( procId, 0 );
		}
	}
	std::string dataStr = "";
	dataStr = SPELLdataHelper::sourceToString(data);
	resp.set( MessageField::FIELD_PROC_CODE, dataStr );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getServerFilePath( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	if (msg.hasField(MessageField::FIELD_PROC_ID))
	{
		std::string procId = msg.get( MessageField::FIELD_PROC_ID );
		DEBUG( NAME + "Requested server file for " + procId );

		if (msg.hasField( MessageField::FIELD_SERVER_FILE_ID))
		{
			std::string fileTypeStr = msg.get( MessageField::FIELD_SERVER_FILE_ID );
			std::string path = "";

			SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(procId);

			SPELLserverFile file = SPELLdataHelper::serverFileFromString( fileTypeStr );
			if (file == FILE_ASRUN)
			{
				path = exec->getModel().getAsRunFilename();
			}
			else if (file == FILE_LOG)
			{
				path = exec->getModel().getLogFilename();
			}

			resp = SPELLipcHelper::createResponse( ContextMessages::RSP_SERVER_FILE_PATH, msg );
			resp.set( MessageField::FIELD_FILE_PATH, path );
		}
		else
		{
			LOG_ERROR("Cannot provide file path: missing procedure identifier");
			resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_SERVER_FILE_PATH, msg );
			resp.set( MessageField::FIELD_ERROR, "Cannot provide file path");
			resp.set( MessageField::FIELD_REASON, "Missing file type");
		}
	}
	else
	{
		LOG_ERROR("Cannot provide file path: missing procedure identifier");
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_SERVER_FILE_PATH, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot provide file path");
		resp.set( MessageField::FIELD_REASON, "Missing procedure identifier");
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::dumpInterpreterInfo( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;
	std::string instanceId = "";

	instanceId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested dump interpreter info for " + instanceId );

	SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(instanceId);

	if (exec)
	{
		SPELLipcMessage copy(msg);
		copy.setType( MSG_TYPE_ONEWAY );
		copy.setId( ExecutorMessages::MSG_DUMP_INTERPRETER );
		exec->sendMessageToExecutor( &copy );
		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_DUMP_INTERPRETER, msg);
	}
	else
	{
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_DUMP_INTERPRETER, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot dump interpreter information");
		resp.set( MessageField::FIELD_REASON, "Executor not found" );
	}

	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcDataChunk::DataList SPELLcontext::getServerFileData( const std::string& filename )
{
	SPELLipcDataChunk::DataList data;
	std::ifstream fileis;
	if (!SPELLutils::pathExists(filename))
	{
		THROW_EXCEPTION("Cannot obtain file '" + filename + "'", "File not found", SPELL_ERROR_FILESYSTEM);
	}
    fileis.open( filename.c_str() );
    if (!fileis.is_open())
    {
        THROW_EXCEPTION("Cannot obtain file '" + filename + "'", "Unable to open", SPELL_ERROR_FILESYSTEM);
    }
    do
    {
        std::string line = "";
        std::getline(fileis,line);
        if (line != "") data.push_back(line);
    }
    while(!fileis.eof());
    fileis.close();
    return data;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcDataChunk::DataList SPELLcontext::getInputFileData( SPELLdatabase* db )
{
	SPELLipcDataChunk::DataList data;
	std::vector<std::string> keys = db->keysStr();
	std::vector<std::string>::iterator it;
	for( it = keys.begin(); it != keys.end(); it++ )
	{
		std::string value = db->getStr(*it);
		SPELLutils::trim(value);
		std::string line = *it + VARIABLE_PROPERTY_SEPARATOR + value;
		if (data.size()>0)
		{
			line = VARIABLE_SEPARATOR + line;
		}
		data.push_back(line);
	}
    return data;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getInputFile( const SPELLipcMessage& msg )
{
	SPELLipcMessage response = SPELLipcHelper::createResponse( ContextMessages::RSP_INPUT_FILE, msg );

	std::string filename = msg.get(MessageField::FIELD_FILE_PATH);

	DEBUG( NAME + "Request to get input file '" + filename + "'");

	SPELLipcDataChunk::DataList data;

	if (msg.get( MessageField::FIELD_CHUNK ) != "")
	{
		int chunkNo = STRI( msg.get( MessageField::FIELD_CHUNK ) );
		DEBUG("Get dictionary contents chunk " + ISTR(chunkNo));
		data = m_dataChunker.getChunk( filename, chunkNo );
		int totalChunks = m_dataChunker.getSize( filename );
		if (chunkNo == (totalChunks-1))
		{
			DEBUG( NAME + "End chunks" );
			m_dataChunker.endChunks(filename);
		}
		response.set( MessageField::FIELD_CHUNK, ISTR(chunkNo) );
		response.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
		response.set( MessageField::FIELD_DICT_CONTENTS, SPELLdataHelper::linesToString(data) );
		DEBUG("Given chunk " +ISTR(chunkNo));
	}
	else
	{
		if (!SPELLutils::pathExists(filename))
		{
			THROW_EXCEPTION("Cannot obtain file '" + filename + "'", "File not found", SPELL_ERROR_FILESYSTEM);
		}

		// Find out the file location
		SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);

		std::list<std::string> locations = ctxConfig.getLocations();

		std::string correspondingLocation = "";
		for(std::list<std::string>::iterator it = locations.begin(); it != locations.end(); it++ )
		{
			std::string lpath = ctxConfig.getLocationPath(*it);
			// Append the data dir
			lpath = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + lpath;

			if (filename.find(lpath) == 0)
			{
				correspondingLocation = *it;
				break;
			}
		}
		if (correspondingLocation == "")
		{
			THROW_EXCEPTION("Cannot find appropriate location path for '" + filename + "'", "Unknown location", SPELL_ERROR_FILESYSTEM);
		}

		DEBUG( NAME + "Corresponding location is " + correspondingLocation);

		int idx = filename.rfind(".")+1;
		std::string ext = filename.substr(idx,filename.size()-idx);
		std::string type = ctxConfig.getLocationType(correspondingLocation);

		DEBUG( NAME + "Input file type is " + type + ", extension " + ext);

		// Once the location path is obtained, use the appropriate parser for the file
		SPELLdatabase* db = SPELLdatabaseFactory::instance().createDatabase( type, filename, filename, ext );

		if (db == NULL)
		{
			THROW_EXCEPTION("Cannot load input file '" + filename + "'", "Unable to create database parser (" + type + ")", SPELL_ERROR_FILESYSTEM);
		}

		DEBUG( NAME + "Loading database");

		// May throw exceptions in parsing or setup errors
		db->load();

		data = getInputFileData( db );

		int totalChunks = m_dataChunker.startChunks( filename, data );
		response.set( MessageField::FIELD_CHUNK, "0" );

		// Chunk if needed
		if (totalChunks != 0)
		{
			DEBUG("Input file contents needs chunk: " + ISTR(totalChunks));

			response.set( MessageField::FIELD_CHUNK, "0" );

			data = m_dataChunker.getChunk( filename, 0 );

			response.set( MessageField::FIELD_DICT_CONTENTS, SPELLdataHelper::linesToString(data) );
			response.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
		}
		else
		{
			DEBUG("No need for chunk");
			response.set( MessageField::FIELD_TOTAL_CHUNKS, "0");
			response.set( MessageField::FIELD_DICT_CONTENTS, SPELLdataHelper::linesToString(data) );
		}
	}

	DEBUG( NAME + "Input file '" + filename + "' obtained");

    return response;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::notifyExecutorOperation(
							  const std::string& instanceId,
							  const std::string& parentInstanceId,
							  int clientKey,
		                      const SPELLclientMode& clientMode,
		                      const SPELLexecutorStatus& status,
		                      const SPELLexecutorOperation& operation,
		                      const std::string& condition,
		                      const std::string& errorMessage,
		                      const std::string& errorReason )
{
	DEBUG( NAME + "Notify executor operation " + SPELLdataHelper::executorOperationToString(operation));
	SPELLipcMessage notification( ContextMessages::MSG_EXEC_OP );
	notification.setSender("CTX");
	notification.setReceiver("CLT");
	notification.setType(MSG_TYPE_ONEWAY);
	notification.set( MessageField::FIELD_PROC_ID, instanceId );
	notification.set( MessageField::FIELD_PARENT_PROC, parentInstanceId );
	notification.set( MessageField::FIELD_EXOP, SPELLdataHelper::executorOperationToString(operation) );
	notification.set( MessageField::FIELD_GUI_KEY, ISTR(clientKey) );
	notification.set( MessageField::FIELD_GUI_MODE, SPELLdataHelper::clientModeToString(clientMode) );
	notification.set( MessageField::FIELD_EXEC_STATUS, SPELLdataHelper::executorStatusToString(status) );
	notification.set( MessageField::FIELD_CONDITION, condition );
	if (errorMessage != "")
	{
		DEBUG( NAME + "Append error information: " + errorMessage + ":" + errorReason);
		notification.set( MessageField::FIELD_ERROR, errorMessage );
		notification.set( MessageField::FIELD_REASON, errorReason );
	}

	// Notify to clients
	SPELLclientManager::instance().notifyClients( notification );

	// Notify to parent proc if needed
	if (parentInstanceId != "")
	{
		DEBUG( NAME + "Notify to parent executor: " + parentInstanceId );
		try
		{
			SPELLexecutor* parentExec = SPELLexecutorManager::instance().getExecutor( parentInstanceId );

			// Status notifications for executors need to be requests, not oneway
			if ( operation == EXEC_OP_STATUS)
			{
				notification.setType( MSG_TYPE_NOTIFY );
				notification.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_STATUS );
				parentExec->sendRequestToExecutor( notification );
			}
			else
			{
				parentExec->sendMessageToExecutor( notification );
			}
		}
		catch( SPELLcoreException& ex )
		{
			LOG_ERROR("Cannot notify parent executor: " + std::string(ex.what()));
		}
	}
	DEBUG( NAME + "Notify executor operation done");
}
