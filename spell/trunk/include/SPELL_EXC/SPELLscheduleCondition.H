// ################################################################################
// FILE       : SPELLscheduleCondition.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Defines a scheduler condition
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

#ifndef __SPELL_SCHEDULE_CONDITION_H__
#define __SPELL_SCHEDULE_CONDITION_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_WRP/SPELLconfigDict.H"


/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Definition of a schedule condition
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLscheduleCondition
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
		/** Type of schedule condition */
		typedef enum ScheduleType_
		{
			/** An expression is to be evaluated */
			SCH_EXPRESSION = 0,
			/** A set of TM conditions */
			SCH_VERIFICATION,
			/** A time condition is to be fullfilled */
			SCH_TIME,
			/** Wait state is fixed until the opposite is said */
			SCH_FIXED,
			/** Prompt condition */
			SCH_PROMPT,
			/** A child procedure needs to finish */
			SCH_CHILD,
			/** No wait state set */
			SCH_NONE
		}
		SPELLscheduleType;

    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLscheduleCondition()
		: m_scheduleConfig(NULL)
		{
			type = SCH_NONE;
			expression = "";
			targetTime.set(0,0,false);
			timeout.set(0,0,false);
			period.set(0,0,false);
			message = "";
			m_tmVerificationList = NULL;
		}

		//////////////////////////////////////////////////////////////////////
		/** Assignment operator
		**////////////////////////////////////////////////////////////////////
		SPELLscheduleCondition& operator=( const SPELLscheduleCondition& other )
		{
			if (this != &other)
			{
				type = other.type;
				expression = other.expression;
				targetTime = other.targetTime;
				timeout = other.timeout;
				period = other.period;
				message = other.message;
				interval = other.interval;
				m_scheduleConfig = other.m_scheduleConfig;
				setVerification(other.m_tmVerificationList);
			}
			return *this;
		}

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Reset the condition parameters.
		**////////////////////////////////////////////////////////////////////
		void reset()
		{
			type = SCH_NONE;
			expression = "";
			targetTime.set(0,0,false);
			timeout.set(0,0,false);
			period.set(0,0,false);
			message = "";
			interval.clear();
		}

		//////////////////////////////////////////////////////////////////////
		/** Assign the tm verification list
		**////////////////////////////////////////////////////////////////////
		void setVerification( PyObject* pyList )
		{
                        if (pyList == NULL) return;
			if ( pyList == m_tmVerificationList ) return;
			SPELLsafePythonOperations ops("SPELLscheduleCondition::setVerification()");
			unsigned int items = PyList_Size( pyList );
			m_tmVerificationList = PyList_New( items );
			for(unsigned idx = 0; idx<items; idx++)
			{
				PyObject* element = PyList_GetItem( pyList,idx);
				// SetItem from lists does not incref the values
				Py_INCREF(element);
				PyList_SetItem(m_tmVerificationList,idx,element);
			}
		}

		//////////////////////////////////////////////////////////////////////
		/** Assign the schedule config
		**////////////////////////////////////////////////////////////////////
		void setConfig( PyObject* pyConf )
		{
                        if (pyConf == NULL) return;
			m_scheduleConfig.reset(pyConf);
		}

		//////////////////////////////////////////////////////////////////////
		/** Get the schedule config (borrowed)
		**////////////////////////////////////////////////////////////////////
		PyObject* getConfig()
		{
			return m_scheduleConfig.toPython();
		}

		//////////////////////////////////////////////////////////////////////
		/** Get the verification list (borrowed)
		**////////////////////////////////////////////////////////////////////
		PyObject* getVerification()
		{
			return m_tmVerificationList;
		}

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Type of condition */
		SPELLscheduleType			type;
		/** Expression to evaluate */
		std::string					expression;
		/** Target time*/
		SPELLtime					targetTime;
		/** Timeout for checks */
		SPELLtime					timeout;
		/** Period for checks */
		SPELLtime					period;
		/** Message to show */
		std::string					message;
		/** Interval for notification messages */
		std::vector<SPELLtime>	    interval;
		/** True if the PromptUser flag is set */
		bool                        promptUser;

private:
		/** TM list */
		PyObject*					m_tmVerificationList;
		/** Extra configuration to consider */
		SPELLconfigDict             m_scheduleConfig;
};

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Result of a schedule condition
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLscheduleResult
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
		/** Result types */
		typedef enum ScheduleResultType_
		{
			/** Condition fullfilled */
			SCH_SUCCESS,
			/** Condition not fullfilled */
			SCH_FAILED,
			/** Condition timed out */
			SCH_TIMEOUT,
			/** Condition evaluation aborted */
			SCH_ABORTED,
			/** Not set */
			SCH_NONE,
		}
		SPELLscheduleResultType;
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLscheduleResult()
		{
			reset();
		}

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Reset the result values.
		**////////////////////////////////////////////////////////////////////
		void reset()
		{
			type = SCH_NONE;
			error = "";
			reason = "";
		}

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Type of result */
		SPELLscheduleResultType     type;
		/** Errror message if any */
		std::string					error;
		/** Error reason if any */
		std::string                 reason;
};

/*@}*/
#endif
