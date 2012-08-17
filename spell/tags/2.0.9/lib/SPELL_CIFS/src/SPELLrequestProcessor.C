// ################################################################################
// FILE       : SPELLrequestProcessor.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the message request processor
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
#include "SPELL_CIFS/SPELLrequestProcessor.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLlog.H"
using namespace PythonConstants;
using namespace LanguageConstants;
using namespace LanguageModifiers;
// System includes ---------------------------------------------------------



//=============================================================================
// CONSTRUCTOR: SPELLrequestProcessor::SPELLrequestProcessor
//=============================================================================
SPELLrequestProcessor::SPELLrequestProcessor()
{
    DEBUG("[CIF] Created request processor");
}

//=============================================================================
// DESTRUCTOR: SPELLrequestProcessor::~SPELLrequestProcessor
//=============================================================================
SPELLrequestProcessor::~SPELLrequestProcessor()
{
    DEBUG("[CIF] Destroyed request processor");
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processMessageForChild
//=============================================================================
void SPELLrequestProcessor::processMessageForChild( SPELLipcMessage* msg )
{
    std::string procId = msg->get(MessageField::FIELD_PROC_ID);

    if (SPELLexecutor::instance().getChildManager().hasChild() &&
    	SPELLexecutor::instance().getChildManager().getChildId() == procId)
    {
        if (msg->getId() == ContextMessages::MSG_EXEC_OP)
        {
            std::string operation = msg->get(MessageField::FIELD_EXOP);
            if (operation == MessageValue::DATA_EXOP_CLOSE)
            {
                SPELLexecutor::instance().getChildManager().notifyChildClosed();
            }
            else if ( operation == MessageValue::DATA_EXOP_KILL )
            {
                SPELLexecutor::instance().getChildManager().notifyChildKilled();
            }
        }
        else if (msg->getType() == MSG_TYPE_ERROR)
        {
            std::string childError = msg->get(MessageField::FIELD_ERROR);
            std::string childErrorReason = msg->get(MessageField::FIELD_REASON);
            SPELLexecutor::instance().getChildManager().notifyChildError( childError, childErrorReason );
        }
    }
    else
    {
        LOG_ERROR("[E] Unexpected message to a child: " + procId);
    }
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processGetConfig
//=============================================================================
void SPELLrequestProcessor::processGetConfig( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    DEBUG("[CIF] Request to get executor config");
    response->setId(ExecutorMessages::RSP_GET_CONFIG);
    SPELLexecutorConfig& config = SPELLexecutor::instance().getConfiguration();
    response->set(ExecutorConstants::RunInto, config.getRunInto() ? True : False);
    std::stringstream buffer;
    buffer << config.getExecDelay();
    response->set(ExecutorConstants::ExecDelay, buffer.str());
    response->set(ExecutorConstants::ByStep, config.getByStep() ? True : False);
    response->set(ExecutorConstants::BrowsableLib, config.getBrowsableLib() ? True : False);
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processSetConfig
//=============================================================================
void SPELLrequestProcessor::processSetConfig( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    DEBUG("[CIF] Request to change executor config");
    response->setId(ExecutorMessages::RSP_SET_CONFIG);
    std::string runInto = msg->get(ExecutorConstants::RunInto);
    std::string execDelay = msg->get(ExecutorConstants::ExecDelay);
    std::string byStep = msg->get(ExecutorConstants::ByStep);
    std::string browsableLib = msg->get(ExecutorConstants::BrowsableLib);
    SPELLexecutor::instance().setRunInto( (runInto == True) );
    SPELLexecutor::instance().setExecDelay( atoi( execDelay.c_str() ));
    SPELLexecutor::instance().setByStep( (byStep == True) );
    SPELLexecutor::instance().setBrowsableLib( (browsableLib == True) );
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processSetBreakpoint
//=============================================================================
void SPELLrequestProcessor::processSetBreakpoint( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    DEBUG("[CIF] Request to set breakpoint");
    // Update the repsonse message
    response->setId(ExecutorMessages::RSP_SET_BREAKPOINT);
    // Retrieve the information from the message
    std::string codeId = msg->get(MessageField::FIELD_BREAKPOINT_PROC);
    std::string targetLine = msg->get(MessageField::FIELD_BREAKPOINT_LINE);
    std::string bpType = msg->get(MessageField::FIELD_BREAKPOINT_TYPE);
    SPELLbreakpointType bp = breakpointTypeFromString(bpType);
    // Perform the action
    SPELLexecutor::instance().setBreakpoint(codeId, atoi(targetLine.c_str()), bp);
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processClearBreakpoints
//=============================================================================
void SPELLrequestProcessor::processClearBreakpoints( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    DEBUG("[CIF] Request to clear breakpoints for the given code");
    // Update the response message
    response->setId(ExecutorMessages::RSP_CLEAR_BREAKPOINT);
    // Perform the action in the executor
    SPELLexecutor::instance().clearBreakpoints();
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processNotificationForChild
//=============================================================================
void SPELLrequestProcessor::processNotificationForChild( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    std::string procId = msg->get(MessageField::FIELD_PROC_ID);

    if (msg->get(MessageField::FIELD_DATA_TYPE) == MessageValue::DATA_TYPE_STATUS)
    {
        if (SPELLexecutor::instance().getChildManager().hasChild() &&
        	SPELLexecutor::instance().getChildManager().getChildId() == procId)
        {
            response->setId(msg->getId());
            std::string status = msg->get(MessageField::FIELD_EXEC_STATUS);
            SPELLexecutorStatus childStatus = StringToStatus(status);
            if (childStatus == STATUS_ERROR)
            {
                std::string childError = msg->get(MessageField::FIELD_ERROR);
                std::string childErrorReason = msg->get(MessageField::FIELD_REASON);
                LOG_INFO("[CIF] Error info: " + childError + ": " + childErrorReason);
                SPELLexecutor::instance().getChildManager().notifyChildError( childError, childErrorReason );
            }
            else if (childStatus == STATUS_ABORTED)
            {
                std::string childError = "Child execution did not finish";
                std::string childErrorReason = "Execution was aborted";
                SPELLexecutor::instance().getChildManager().notifyChildError( childError, childErrorReason );
            }
            else
            {
                SPELLexecutor::instance().getChildManager().notifyChildStatus( childStatus );
            }
            LOG_INFO("[CIF] Child status: " + status );
        }
        else
        {
            LOG_ERROR("[CIF] No such child procedure: " + procId);
        }
    }
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processGetVariables
//=============================================================================
void SPELLrequestProcessor::processGetVariables( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    // Update the response message
    response->setId(ExecutorMessages::RSP_VARIABLE_NAMES);

    std::string whichOnes = msg->get( MessageField::FIELD_VARIABLE_GET );

    std::vector<SPELLvarInfo> vars;

    if (whichOnes == MessageValue::AVAILABLE_ALL)
	{
    	vars = SPELLexecutor::instance().getVariableManager().getAllVariables();
	}
    else if (whichOnes == MessageValue::AVAILABLE_GLOBALS)
	{
    	vars = SPELLexecutor::instance().getVariableManager().getGlobalVariables();
	}
    else if (whichOnes == MessageValue::AVAILABLE_LOCALS)
	{
    	vars = SPELLexecutor::instance().getVariableManager().getLocalVariables();
	}
    else if (whichOnes == MessageValue::REGISTERED_ALL)
	{
    	vars = SPELLexecutor::instance().getVariableManager().getRegisteredVariables();
	}
    else if (whichOnes == MessageValue::REGISTERED_GLOBALS)
	{
    	vars = SPELLexecutor::instance().getVariableManager().getRegisteredGlobalVariables();
	}
    else if (whichOnes == MessageValue::REGISTERED_LOCALS)
	{
    	vars = SPELLexecutor::instance().getVariableManager().getRegisteredLocalVariables();
	}

	std::string names = "";
	std::string types = "";
	std::string values = "";
	std::string globals = "";
	std::string registereds = "";

	for(unsigned int index = 0; index<vars.size(); index++)
	{
		if (names != "")
		{
			names += ",,";
			types += ",,";
			values += ",,";
			globals += ",,";
			registereds += ",,";
		}
		names += vars[index].varName;
		types += vars[index].varType;
		values += vars[index].varValue;
		globals += vars[index].isGlobal ? "True" : "False";
		registereds += vars[index].isRegistered ? "True" : "False";
	}

	response->set( MessageField::FIELD_VARIABLE_NAME, names );
	response->set( MessageField::FIELD_VARIABLE_TYPE, types );
	response->set( MessageField::FIELD_VARIABLE_VALUE, values );
	response->set( MessageField::FIELD_VARIABLE_GLOBAL, globals );
	response->set( MessageField::FIELD_VARIABLE_REGISTERED, registereds );
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processVariableWatch
//=============================================================================
void SPELLrequestProcessor::processVariableWatch( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    // Update the response message
    response->setId(ExecutorMessages::RSP_VARIABLE_WATCH);

    std::string name = msg->get( MessageField::FIELD_VARIABLE_NAME );
    std::string value;
    std::string type;
    bool global = (msg->get( MessageField::FIELD_VARIABLE_GLOBAL ) == "True");

    SPELLvarInfo info(name,type,value,global,false);
	bool success = SPELLexecutor::instance().getVariableManager().registerVariable( info );

	if (success)
	{
		response->set( MessageField::FIELD_VARIABLE_VALUE, info.varValue );
		response->set( MessageField::FIELD_VARIABLE_TYPE, info.varType );
	}
	else
	{
		response->set( MessageField::FIELD_VARIABLE_VALUE, "???" );
		response->set( MessageField::FIELD_VARIABLE_TYPE, "???" );
	}
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processVariableNoWatch
//=============================================================================
void SPELLrequestProcessor::processVariableNoWatch( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    // Update the response message
    response->setId(ExecutorMessages::RSP_VARIABLE_NOWATCH);

    std::string name = msg->get( MessageField::FIELD_VARIABLE_NAME );
    bool global = (msg->get( MessageField::FIELD_VARIABLE_GLOBAL ) == "True");

    SPELLvarInfo info(name,"","",global,true);
	SPELLexecutor::instance().getVariableManager().unregisterVariable( info );
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processWatchNothing
//=============================================================================
void SPELLrequestProcessor::processWatchNothing( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    // Update the response message
    response->setId(ExecutorMessages::RSP_WATCH_NOTHING);

	SPELLexecutor::instance().getVariableManager().unregisterAll();
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processChangeVariable
//=============================================================================
void SPELLrequestProcessor::processChangeVariable( SPELLipcMessage* msg, SPELLipcMessage* response )
{
    // Update the response message
    response->setId(ExecutorMessages::RSP_CHANGE_VARIABLE);

    std::string name = msg->get( MessageField::FIELD_VARIABLE_NAME );
    std::string valueExpression = msg->get( MessageField::FIELD_VARIABLE_VALUE );
    bool global = (msg->get( MessageField::FIELD_VARIABLE_GLOBAL ) == "True");

    // Type and registration flag are not important here
    SPELLvarInfo info(name,"",valueExpression,global,false);

	SPELLexecutor::instance().getVariableManager().changeVariable( info );
}
