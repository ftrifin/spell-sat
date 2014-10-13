// ################################################################################
// FILE       : SPELLnotifications.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Notification data structures
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

#ifndef __SPELL_NOTIFICATIONS_H__
#define __SPELL_NOTIFICATIONS_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Local includes ----------------------------------------------------------

// Project includes --------------------------------------------------------

/** \addtogroup SPELL_CIF */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
//============================================================================
/** Notification types */
typedef enum NotifyType_
{
    NOTIFY_ITEM,
    NOTIFY_VALUE,
    NOTIFY_VERIFICATION,
    NOTIFY_EXECUTION,
    NOTIFY_SYSTEM,
    NOTIFY_TIME
}
NotificationType;
//============================================================================

//============================================================================

//============================================================================
/** Notification information structure */
typedef struct ItemNotification_
{
    NotificationType       type;
    std::string            name;
    std::string            value;
    std::string            status;
    std::string            comment;
    std::string            time;
    std::string            stack;
}
ItemNotification;
//============================================================================

/*@}*/
#endif
