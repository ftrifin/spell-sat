// ################################################################################
// FILE       : SPELLerror.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL core exceptions
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

#ifndef __SPELL_ERROR_H__
#define __SPELL_ERROR_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_UTIL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Definition of all error code numbers used in SPELL applications.
 **
 ** \par Description and usage:
 **
 **  	These values are to be used as the error code identifiers
 **  	for all SPELL core exceptions.
 **
 ** \see SPELLcoreException
 **
 *////////////////////////////////////////////////////////////////////////////
typedef enum SPELLerrorCode_
{
    // SPELL error codes (Python side)
    SPELL_PYERROR_IMPORT = 0,                // Failure to import a module
    SPELL_PYERROR_INDEX  = 1,                // Wrong index in list
    SPELL_PYERROR_KEY    = 2,                // Wrong key in dict
    SPELL_PYERROR_MEMORY = 3,                // Out of memory
    SPELL_PYERROR_NAME   = 4,                // Undefined variable/func
    SPELL_PYERROR_SYNTAX = 5,                // Syntax error
    SPELL_PYERROR_TYPE   = 6,                // Wrong data type used
    SPELL_PYERROR_VALUE  = 7,                // Unexpected value
    SPELL_PYERROR_OTHER  = 8,                // Other errors (uncontrolled)

    // SPELL error codes (internal)
    SPELL_DRIVER_ERROR   = 9,
    SPELL_ERROR_UNKNOWN  = 10
}
SPELLerrorCode;


//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Root of all executor exceptions.
 **
 ** \par Description and usage:
 **
 **  	Mechanism for error handling in all SPELL modules. The basic constructor
 **  	requires an error message and an explanation of the cause. Error codes
 **  	can be used for further processing.
 **
 ** \see SPELLerrorCode
 **
 *////////////////////////////////////////////////////////////////////////////
class SPELLcoreException
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    /** Constructor.
     *
     * Sets by default the error code to unknown and the fatal error flag to
     * false.
     *
     * \param error IN: error description
     * \param reason IN: error cause or explanation.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( std::string error, std::string reason )
    {
        m_error = error;
        m_reason = reason;
        m_code = SPELL_ERROR_UNKNOWN;
        m_fatal = false;
        loggit();
    }

    //////////////////////////////////////////////////////////////////////////
    /** Const-copy constructor.
     *
     * \param original IN: exception to be copied.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( const SPELLcoreException& original )
    {
        m_error = original.m_error;
        m_reason = original.m_reason;
        m_code = original.m_code;
        m_fatal = original.m_fatal;
    }

    //////////////////////////////////////////////////////////////////////////
    /** Copy constructor.
     *
     * \param original IN: exception to be copied.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( SPELLcoreException& original )
    {
        m_error = original.m_error;
        m_reason = original.m_reason;
        m_code = original.m_code;
        m_fatal = original.m_fatal;
    }

    //////////////////////////////////////////////////////////////////////////
    /** Constructor.
     *
     * Sets by default the error code to unknown.
     *
     * \param error IN: error description.
     * \param reason IN: error cause or explanation.
     * \param fatal IN: true if the error is fatal and not recoverable.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( std::string error, std::string reason, bool fatal )
    {
        m_error = error;
        m_reason = reason;
        m_code = SPELL_ERROR_UNKNOWN;
        m_fatal = fatal;
        loggit();
    }

    //////////////////////////////////////////////////////////////////////////
    /** Constructor.
     *
     * Sets by default the fatal error flag to false.
     *
     * \param error IN: error description.
     * \param reason IN: error cause or explanation.
     * \param code IN: the associated error code.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( std::string error, std::string reason, SPELLerrorCode code )
    {
        m_error = error;
        m_reason = reason;
        m_code = code;
        m_fatal = false;
        loggit();
    }

    //////////////////////////////////////////////////////////////////////////
    /** Constructor.
     *
     * \param error IN: error description.
     * \param reason IN: error cause or explanation.
     * \param code IN: the associated error code.
     * \param fatal IN: true if the error is fatal and not recoverable.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( std::string error, std::string reason, SPELLerrorCode code, bool fatal )
    {
        m_error = error;
        m_reason = reason;
        m_code = code;
        m_fatal = fatal;
        loggit();
    }

    //////////////////////////////////////////////////////////////////////////
    /** Destructor.
     *////////////////////////////////////////////////////////////////////////
    virtual ~SPELLcoreException() throw() {};

    // METHODS ///////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    /** Obtain the error message.
     *
     * \return The error message.
     *////////////////////////////////////////////////////////////////////////
    const std::string& getError() const {
        return m_error;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Set the error message.
     *
     * \param error IN: the error message.
     *////////////////////////////////////////////////////////////////////////
    void setError( std::string error ) {
        m_error = error;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Obtain the error reason.
     *
     * \return The error cause or explanation.
     *////////////////////////////////////////////////////////////////////////
    const std::string& getReason() const {
        return m_reason;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Set the error reason.
     *
     * \param reason IN: cause or explanation of the error.
     *////////////////////////////////////////////////////////////////////////
    void setReason( std::string reason ) {
        m_reason = reason;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Obtain the error code if any.
     *
     * \return The error code of the exception.
     *////////////////////////////////////////////////////////////////////////
    SPELLerrorCode getCode() const {
        return m_code;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Check if the error is fatal (unrecoverable).
     *
     * \return True if the error is fatal.
     *////////////////////////////////////////////////////////////////////////
    bool isFatal() const {
        return m_fatal;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Convert to readable string.
     *
     * \return The error message as standard string.
     *////////////////////////////////////////////////////////////////////////
    const std::string whats() const throw()
    {
    	return std::string(what());
    }

    //////////////////////////////////////////////////////////////////////////
    /** Dump to logger.
     *////////////////////////////////////////////////////////////////////////
    void loggit() const throw()
    {
    	LOG_ERROR("EXCEPTION: " + whats());
    	std::cerr << "EXCEPTION: " + whats() << std::endl;
    }

    //////////////////////////////////////////////////////////////////////////
    /** Overrides std::exception::what.
     *
     * Provides a formatted error description message.
     *
     * \return The error message as C-string.
     *////////////////////////////////////////////////////////////////////////
    const char* what() const throw()
    {
        std::string msg = m_error;
        if (m_reason != "") msg += ": " + m_reason;
        if (m_code != SPELL_ERROR_UNKNOWN) msg += " (error code " + ISTR(m_code) + ")";
        return msg.c_str();
    }

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the error description */
		std::string     m_error;
		/** Holds the error reason */
		std::string     m_reason;
		/** Holds the error code */
		SPELLerrorCode  m_code;
		/** True if the system cannot be recovered after this error */
		bool            m_fatal;
};

/*@}*/
#endif
