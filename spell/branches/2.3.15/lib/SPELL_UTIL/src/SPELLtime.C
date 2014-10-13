// ################################################################################
// FILE       : SPELLtime.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the time object
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
// Project includes --------------------------------------------------------

std::vector<long> SPELLticker::s_intime;

//=============================================================================
// CONSTRUCTOR: SPELLtime::SPELLtime
//=============================================================================
SPELLtime::SPELLtime()
{
    setCurrent();
}

//=============================================================================
// CONSTRUCTOR: SPELLtime::SPELLtime
//=============================================================================
SPELLtime::SPELLtime( unsigned long secs, bool delta )
{
    set(secs,0,delta);
}

//=============================================================================
// CONSTRUCTOR: SPELLtime::SPELLtime
//=============================================================================
SPELLtime::SPELLtime( unsigned long secs, unsigned int msecs, bool delta )
{
    set(secs,msecs,delta);
}

//=============================================================================
// CONSTRUCTOR: SPELLtime::SPELLtime
//=============================================================================
SPELLtime::SPELLtime( const SPELLtime& other )
{
    set(other.m_secs,other.m_msecs,other.m_delta);
}

//=============================================================================
// DESTRUCTOR: SPELLtime::~SPELLtime
//=============================================================================
SPELLtime::~SPELLtime()
{
    // Nothing to do
}

//=============================================================================
// METHOD: SPELLtime::operator=
//=============================================================================
SPELLtime& SPELLtime::operator=( const SPELLtime& other )
{
    if (this != &other) // protect against invalid self-assignment
    {
        m_secs = other.m_secs;
        m_msecs = other.m_msecs;
        m_delta = other.m_delta;
    }
    // by convention, always return this
    return *this;
}

//=============================================================================
// METHOD: SPELLtime::operator+
//=============================================================================
SPELLtime SPELLtime::operator+(const SPELLtime& other)
{
    SPELLtime result(0,true);
    result.set( m_secs + other.m_secs, m_msecs + other.m_msecs, m_delta && other.m_delta );
    return result;
}

//=============================================================================
// METHOD: SPELLtime::operator+=
//=============================================================================
SPELLtime& SPELLtime::operator+=(SPELLtime& other)
{
    set( m_secs + other.m_secs, m_msecs + other.m_msecs, m_delta && other.m_delta );
    return *this;
}

//=============================================================================
// METHOD: SPELLtime::operator-
//=============================================================================
SPELLtime SPELLtime::operator-(const SPELLtime& other)
{
    SPELLtime result(0,true);
    long secs = m_secs - other.m_secs;
    int msecs = m_msecs - other.m_msecs;
    result.set( (secs>0)? secs: 0, (msecs>0)? msecs: 0 );
    return result;
}

//=============================================================================
// METHOD: SPELLtime::operator-=
//=============================================================================
SPELLtime& SPELLtime::operator-=(SPELLtime& other)
{
    long secs = m_secs - other.m_secs;
    int msecs = m_msecs - other.m_msecs;
    set( (secs>0)? secs: 0, (msecs>0)? msecs: 0 );
    return *this;
}

//=============================================================================
// METHOD: SPELLtime::operator<
//=============================================================================
bool SPELLtime::operator<( const SPELLtime& other ) const
{
    if (m_secs < other.m_secs) return true;
    if (m_secs == other.m_secs)
    {
        if (m_msecs < other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator>
//=============================================================================
bool SPELLtime::operator>( const SPELLtime& other ) const
{
    if (m_secs > other.m_secs) return true;
    if (m_secs == other.m_secs)
    {
        if (m_msecs > other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator>
//=============================================================================
bool SPELLtime::operator>( const unsigned long& secs ) const
{
    if (m_secs > secs) return true;
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator<
//=============================================================================
bool SPELLtime::operator<( const unsigned long& secs ) const
{
    if (m_secs < secs) return true;
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator<
//=============================================================================
bool SPELLtime::operator==( const SPELLtime& other ) const
{
    if (m_secs == other.m_secs)
    {
        if (m_msecs == other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator<
//=============================================================================
bool SPELLtime::operator==( const unsigned long& secs ) const
{
    if (m_secs == secs)
    {
        if (m_msecs == 0) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator>=
//=============================================================================
bool SPELLtime::operator>=( const SPELLtime& other ) const
{
    if (m_secs >= other.m_secs) return true;
    if (m_secs == other.m_secs)
    {
        if (m_msecs >= other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator<=
//=============================================================================
bool SPELLtime::operator<=( const SPELLtime& other ) const
{
    if (m_secs <= other.m_secs) return true;
    if (m_secs == other.m_secs)
    {
        if (m_msecs <= other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::toString
//=============================================================================
std::string SPELLtime::toString() const
{
    if (m_secs==0)
    {
        return "0";
    }
    time_t theTime = m_secs;
    struct tm* ptm = localtime(&theTime);
    if (ptm==NULL)
    {
        return "\?\?\?\?-\?\?-\?\? \?\?:\?\?:\?\?";
    }
    std::string timeStr = "";
    // 2009-12-07 16:55:12
    if (theTime >= 86400) // Long date format
    {
        timeStr += ISTR(1900+ptm->tm_year) + "-";
        std::string month = ISTR(ptm->tm_mon+1);
        if (month.length()==1) month = "0" + month;
        std::string day = ISTR(ptm->tm_mday);
        if (day.length()==1) day = "0" + day;
        timeStr += month + "-" + day + " ";
    }

    if (theTime<60) // Very short date format
    {
        timeStr = ISTR(theTime);
    }
    else
    {
        std::string hour = ISTR(ptm->tm_hour);
        if (hour.length()==1) hour = "0" + hour;
        std::string min = ISTR(ptm->tm_min);
        if (min.length()==1) min = "0" + min;
        std::string sec = ISTR(ptm->tm_sec );
        if (sec.length()==1) sec = "0" + sec;
        timeStr += hour + ":" + min + ":" + sec;
    }
    return timeStr;
}

//=============================================================================
// METHOD: SPELLtime::toString
//=============================================================================
std::string SPELLtime::toTIMEString() const
{
    if (m_secs==0)
    {
        return "0";
    }
    time_t theTime = m_secs;
    struct tm* ptm = localtime(&theTime);
    if (ptm==NULL)
    {
        return "0";
    }
    std::string timeStr = "";
    // 2009-12-07:16:55:12
    if (theTime >= 86400) // Long date format
    {
    	if (isDelta()) timeStr = "+";
        timeStr += ISTR(1900+ptm->tm_year) + "-";
        std::string month = ISTR(ptm->tm_mon+1);
        if (month.length()==1) month = "0" + month;
        std::string day = ISTR(ptm->tm_mday);
        if (day.length()==1) day = "0" + day;
        timeStr += month + "-" + day + ":";
    }

    if (theTime<60) // Very short date format
    {
        timeStr = "+000 00:00:" + ISTR(theTime);
    }
    else
    {
    	if (isDelta() && timeStr == "") timeStr = "+000 ";
        std::string hour = ISTR(ptm->tm_hour);
        if (hour.length()==1) hour = "0" + hour;
        std::string min = ISTR(ptm->tm_min);
        if (min.length()==1) min = "0" + min;
        std::string sec = ISTR(ptm->tm_sec );
        if (sec.length()==1) sec = "0" + sec;
        timeStr += hour + ":" + min + ":" + sec;
    }
    return timeStr;
}

//=============================================================================
// METHOD: SPELLtime::setCurrent
//=============================================================================
void SPELLtime::setCurrent()
{
    struct timeval tz;
    gettimeofday( &tz, NULL );
    set( tz.tv_sec, tz.tv_usec/1000, false );
}

//=============================================================================
// METHOD: SPELLtime::set
//=============================================================================
void SPELLtime::set( unsigned long secs, unsigned int msecs )
{
    set(secs,msecs,(secs<86400));
}

//=============================================================================
// METHOD: SPELLtime::set
//=============================================================================
void SPELLtime::set( unsigned long secs, unsigned int msecs, bool delta )
{
    m_secs = secs;
    m_msecs = msecs;
    m_delta = delta;
    //DEBUG("[TIME] Set time: " + ISTR(m_secs) + ", " + ISTR(m_msecs) + ", " + BSTR(m_delta))
}

//=============================================================================
// STATIC: SPELLticker::tickIn
//=============================================================================
void SPELLticker::tickIn( const std::string& function, unsigned int line )
{
    struct timeval tz;
    gettimeofday( &tz, NULL );

    long usec = tz.tv_sec * 1000000 + tz.tv_usec;
    long idx = s_intime.size();

    std::string indent('-', idx);
    int pos = function.find("::");
    int pos2 = function.rfind(" ",pos);
	std::string where = "[" + function.substr(pos2,function.size()-pos2) + ":" + ISTR(line) + "] ";

	std::cerr << std::left << std::setw(70) << where << std::left << std::setw(50) << indent << "IN  (" << idx << ")" << std::endl;
	s_intime.push_back(usec);
};

//=============================================================================
// STATIC: SPELLtime::tickOut
//=============================================================================
void SPELLticker::tickOut( const std::string& function, unsigned int line )
{
    struct timeval tz;
    gettimeofday( &tz, NULL );

    std::string indent('-', s_intime.size());

    long in = 0;
    if (s_intime.size()>0)
	{
    	in = s_intime.back();
    	s_intime.pop_back();
	}
    long idx = s_intime.size();
    long usec = tz.tv_sec * 1000000 + tz.tv_usec;
    long delta = usec - in;

    int pos = function.find("::");
    int pos2 = function.rfind(" ",pos);
	std::string where = "[" + function.substr(pos2,function.size()-pos2) + ":" + ISTR(line) + "] ";
	if (delta > 1000)
	{
		std::cerr << std::left << std::setw(70) << where << std::left << std::setw(50) << indent << "OUT (" << idx << "): " << delta/1000 << " ms." << std::endl;
	}
	else
	{
		std::cerr << std::left << std::setw(70) << where << std::left << std::setw(50) << indent << "OUT (" << idx << "): " << delta << " us." << std::endl;
	}
};
