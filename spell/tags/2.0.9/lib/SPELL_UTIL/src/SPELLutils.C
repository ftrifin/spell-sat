// ################################################################################
// FILE       : SPELLutils.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the utilities
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
#include "SPELL_UTIL/SPELLerror.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------
#include <dirent.h>
#include <iomanip>
#include <sys/stat.h>


//============================================================================
// FUNCTION : itostr
//============================================================================
std::string itostr( long i )
{
    char buffer[500];
    sprintf( buffer, "%ld", i );
    return buffer;
};

//============================================================================
// FUNCTION : tokenize
//============================================================================
std::vector<std::string> tokenize( const std::string& str, const std::string& delimiters )
{
    std::vector<std::string> tokens;

    // Skip delimiters at the beginning
    std::string::size_type lastPos = str.find_first_not_of(delimiters,0);

    // Find first non-delimiter
    std::string::size_type pos = str.find_first_of(delimiters, lastPos);

    while( std::string::npos != pos || std::string::npos != lastPos )
    {
        // Found a token, add it to the vector
        tokens.push_back( str.substr( lastPos, pos - lastPos ));
        // Skip delimiters
        lastPos = str.find_first_not_of(delimiters, pos);
        // Find next non-delimiter
        pos = str.find_first_of(delimiters, lastPos);
    }
    return tokens;
}

//============================================================================
// FUNCTION : tokenized
//============================================================================
std::vector<std::string> tokenized( const std::string& str, const std::string& delimiters )
{
    std::vector<std::string> tokens;
    std::string::size_type delimPos = 0, tokenPos = 0, pos = 0;

    if(str.length()<1) return tokens;
    while(1)
    {
        delimPos = str.find_first_of(delimiters, pos);
        tokenPos = str.find_first_not_of(delimiters, pos);

        if(std::string::npos != delimPos)
        {
            if(std::string::npos != tokenPos)
            {
                if(tokenPos<delimPos)
                {
                    tokens.push_back(str.substr(pos,delimPos-pos));
                }
                else
                {
                    tokens.push_back("");
                }
            }
            else
            {
                tokens.push_back("");
            }
            pos = delimPos+1;
        }
        else
        {
            if(std::string::npos != tokenPos)
            {
                tokens.push_back(str.substr(pos));
            }
            else
            {
                tokens.push_back("");
            }
            break;
        }
    }
    return tokens;
}

//============================================================================
// FUNCTION : trim
//============================================================================
void trim( std::string& str )
{
    std::string::size_type pos = str.find_last_not_of(' ');
    if(pos != std::string::npos)
    {
        str.erase(pos + 1);
        pos = str.find_first_not_of(' ');
        if(pos != std::string::npos) str.erase(0, pos);
    }
    else
    {
        str.erase(str.begin(), str.end());
    }
}

//============================================================================
// FUNCTION : trim
//============================================================================
void trim( std::string& str, std::string characters )
{
    std::string::size_type pos;
    while(true)
    {
        pos = str.find_first_of(characters);
        if (pos != std::string::npos)
        {
            str.erase(pos);
        }
        else
        {
            break;
        }
    }
}

//============================================================================
// FUNCTION : replace
//============================================================================
void replace( std::string& str, std::string original, std::string newstr )
{
    std::string::size_type pos;
    while(true)
    {
        pos = str.find(original);
        if (pos != std::string::npos)
        {
            str.replace(pos, original.size(), newstr.c_str());
        }
        else
        {
            break;
        }
    }
}

//============================================================================
// FUNCTION : resolvePath
//============================================================================
std::string resolvePath( const std::string& path )
{
    std::string resolved = path;
    std::vector<std::string> tokens = tokenize( resolved, PATH_SEPARATOR );
    std::vector<std::string>::iterator it;
    for ( it = tokens.begin(); it != tokens.end(); it++)
    {
        if ( (*it).find_first_of("$") != std::string::npos)
        {
            std::string varname = (*it).substr(1, (*it).size()-1);
            char* value = getenv( varname.c_str() );
            if (value == NULL) throw SPELLcoreException("Cannot resolve path", "Cannot find variable " + (*it));
            (*it) = value;
        }
    }
    resolved = "";
    for ( it = tokens.begin(); it != tokens.end(); it++)
    {
        if (resolved.size()>0) resolved += "/";
        resolved += (*it);
    }
    return resolved;
}

//============================================================================
// FUNCTION:    basePath
//============================================================================
std::string basePath( const std::string& path )
{
    std::size_t pos = path.find_last_of( PATH_SEPARATOR );
    if ( pos != std::string::npos )
    {
        return path.substr(0, pos);
    }
    else
    {
        return path;
    }
}

//============================================================================
// FUNCTION : pathExists
//============================================================================
bool pathExists( const std::string& path )
{
    struct stat st;
    return (stat( path.c_str(),&st) == 0);
}

//============================================================================
// FUNCTION : isDirectory
//============================================================================
bool isDirectory( const std::string& path )
{
    struct stat buf;
    bool isDir = false;
    if (stat(path.c_str(),&buf) == 0)
    {
        isDir = S_ISDIR( buf.st_mode );
    }
    return isDir;
}

//============================================================================
// FUNCTION : isFile
//============================================================================
bool isFile( const std::string& path )
{
    struct stat buf;
    bool isFile = false;
    if (stat(path.c_str(),&buf) == 0)
    {
        isFile = S_ISREG( buf.st_mode );
    }
    return isFile;
}


//============================================================================
// FUNCTION : getFilesInDir
//============================================================================
std::list<std::string> getFilesInDir( const std::string& path )
{
    DIR *dirp;
    struct dirent *dp;

    std::list<std::string> files;

    dirp = opendir( path.c_str() );
    while ( (dp = readdir(dirp)) != NULL )
    {
    	if (dp->d_type & DT_REG)
    	{
    		files.push_back(dp->d_name);
    	}
    	// IMPORTANT: in some filesystems, the type may be impossible
    	// to get with readdir. In these cases we need to use stat().
    	else if (dp->d_type == DT_UNKNOWN)
    	{
			if (isFile( path + PATH_SEPARATOR + std::string(dp->d_name)))
			{
				files.push_back(dp->d_name);
			}
    	}
    	// Ignore other types like symbolic links, sockets, etc.
    }
    closedir(dirp);
    return files;
}

//============================================================================
// FUNCTION : getSubdirs
//============================================================================
std::list<std::string> getSubdirs( const std::string& path )
{
    DIR* dirp;
    struct dirent *dp;

    std::string dot = ".";
    std::string pdot = "..";
    std::string svn = ".svn";
    std::list<std::string> subdirs;

    dirp = opendir( path.c_str() );
    while ( (dp = readdir(dirp)) != NULL )
    {
    	if (dp->d_type & DT_DIR)
    	{
            if ( dp->d_name != dot && dp->d_name != pdot && dp->d_name != svn )
            {
            	subdirs.push_back(dp->d_name);
            }
    	}
    	// IMPORTANT: in some filesystems, the type may be impossible
    	// to get with readdir. In these cases we need to use stat().
    	else if (dp->d_type == DT_UNKNOWN)
        {
    		if ( isDirectory( path + PATH_SEPARATOR + std::string(dp->d_name) ) )
    		{
				if ( dp->d_name != dot && dp->d_name != pdot && dp->d_name != svn )
				{
					subdirs.push_back(dp->d_name);
				}
    		}
        }
    	// Ignore other types like symbolic links, sockets, etc.
    }
    closedir(dirp);
    return subdirs;
}

//============================================================================
// FUNCTION:    timestamp
//============================================================================
std::string timestamp()
{
    time_t t;
    time(&t);
    struct tm* theTime;

    theTime = gmtime( &t );

    std::string year  = ISTR( 1900 + theTime->tm_year);
    std::string month = ISTR(theTime->tm_mon+1);
    if (month.size()==1) month = "0" + month;
    std::string day   = ISTR(theTime->tm_mday);
    if (day.size()==1) day = "0" + day;
    std::string hours = ISTR(theTime->tm_hour);
    if (hours.size()==1) hours = "0" + hours;
    std::string mins = ISTR(theTime->tm_min);
    if (mins.size()==1) mins = "0" + mins;
    std::string secs = ISTR(theTime->tm_sec);
    if (secs.size()==1) secs = "0" + secs;

    // 2009-09-01 12:49:54
    return year + "-" + month + "-" + day + " " + hours + ":" +
           mins + ":" + secs;
}

//============================================================================
// FUNCTION:    fileTimestamp
//============================================================================
std::string fileTimestamp()
{
    time_t t;
    time(&t);
    struct tm* theTime;

    theTime = gmtime( &t );

    std::string year  = ISTR( 1900 + theTime->tm_year);
    std::string month = ISTR(theTime->tm_mon+1);
    if (month.size()==1) month = "0" + month;
    std::string day   = ISTR(theTime->tm_mday);
    if (day.size()==1) day = "0" + day;
    std::string hours = ISTR(theTime->tm_hour);
    if (hours.size()==1) hours = "0" + hours;
    std::string mins = ISTR(theTime->tm_min);
    if (mins.size()==1) mins = "0" + mins;
    std::string secs = ISTR(theTime->tm_sec);
    if (secs.size()==1) secs = "0" + secs;

    // 2009-09-01_124954
    return year + "-" + month + "-" + day + "_" + hours + mins + secs;
}

//============================================================================
// FUNCTION:    envVar
//============================================================================
void envVar( const std::string& name )
{
    char* value = getenv(name.c_str());
    if (value == NULL)
    {
        std::cerr << "* " << std::setw(15) << name << ": (NOT DEFINED)" << std::endl;
    }
    else
    {
        std::cerr << "* " << std::setw(15) << name << ": " << value << std::endl;
    }
}

//============================================================================
// FUNCTION:    showEnvironment
//============================================================================
void showEnvironment()
{
    std::cerr << "==============================================================================" << std::endl;
    std::cerr << "     SPELL environment configuration" << std::endl;
    std::cerr << "==============================================================================" << std::endl;
    envVar("SPELL_HOME");
    envVar("SPELL_COTS");
    envVar("SPELL_DATA");
    envVar("SPELL_CONFIG");
    envVar("SPELL_SYS_DATA");
    envVar("SPELL_LOG");
    envVar("SPELL_PROCS");
    std::cerr << "==============================================================================" << std::endl;
}

//============================================================================
// FUNCTION:    getSPELL_HOME()
//============================================================================
std::string getSPELL_HOME()
{
	char* value = getenv("SPELL_HOME");
	if (value == NULL)
	{
	    throw SPELLcoreException("Unable to obtain SPELL_HOME", "Variable not defined in environment");
	}
	return value;
}

//============================================================================
// FUNCTION:    getSPELL_DATA()
//============================================================================
std::string getSPELL_DATA()
{
	char* value = getenv("SPELL_DATA");
	if (value == NULL)
	{
	    return getSPELL_HOME() + PATH_SEPARATOR + "data";
	}
	return value;
}

//============================================================================
// FUNCTION:    getSPELL_CONFIG()
//============================================================================
std::string getSPELL_CONFIG()
{
	char* value = getenv("SPELL_CONFIG");
	if (value == NULL)
	{
	    return getSPELL_HOME() + PATH_SEPARATOR + "config";
	}
	return value;
}
