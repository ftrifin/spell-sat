###############################################################################

"""
PACKAGE 
    spell.lib.adapter.tm
FILE
    tm.py
    
DESCRIPTION
    TM interface definition. Provides functions to access telemetry data.

PROJECT: SPELL

 Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.

 This file is part of SPELL.

 This library is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, either
 version 3 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License and GNU General Public License (to which the GNU Lesser
 General Public License refers) along with this library.
 If not, see <http://www.gnu.org/licenses/>.
 
"""

###############################################################################

#*******************************************************************************
# SPELL imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lib.registry import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.notification import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from tm_item import TmItemClass
from verifier import TmVerifierClass
from config import Configurable
from constants.core import COMP_SYMBOLS
from result import TmResult
from interface import Interface

#*******************************************************************************
# System imports
#*******************************************************************************
import time,string,thread,sys

###############################################################################
# Module import definition

__all__ = ['TmInterface']

INTERFACE_DEFAULTS = {  OnFailure:ABORT | SKIP | RECHECK | CANCEL, 
                        ValueFormat:ENG, 
                        Timeout:15, 
                        Retries:2,
                        Tolerance:0,
                        Wait:False,
                        PromptUser:True,
                        OnFalse:PROMPT,
                        OnTrue:NOPROMPT,
                        OnSkip:True,
			            IgnoreCase:False }

EPSILON = 2.1e-5

###############################################################################
class TmInterface(Configurable, Interface):

    """
    This class provides the TM management interface. Feature methods shall
    be overriden by driver concrete interfaces.
    """

    __tmParameters = {}
    __verifiers = []
    __verifTable = []
    __verifMutex = None
    __useConfig = {}

    #===========================================================================
    def __init__(self):
        Interface.__init__(self, "TM")
        Configurable.__init__(self)
        self.__tmParameters = {}
        self.__verifiers = []
        self.__verifTable = []
        self.__verifMutex = thread.allocate_lock()
        self.__ctxName = None
        LOG("Created")
        
    #===========================================================================
    def __getitem__(self, key):
        # If the parameter mnemonic is composed of several words:
        words = key.split()
        mnemonic = key
        description = None
        if len(words)>1 and words[0].upper() == 'T':
            mnemonic = words[1]
            description = ' '.join(words[2:])
        else:
            mnemonic = key
            description = key

        if not mnemonic in self.__tmParameters.keys():
            LOG("Creating TM item for " + mnemonic)
            LOG("Description: " + repr(description))
            item = self._createTmItem(mnemonic,description)
            self.__tmParameters[mnemonic] = item
        else:
            item = self.__tmParameters.get(mnemonic)
        return item

    #===========================================================================
    def refreshConfig(self):
        ctxConfig = self.getContextConfig()
        languageDefaults = ctxConfig.getInterfaceConfig(self.getInterfaceName())
        if languageDefaults:
            INTERFACE_DEFAULTS.update(languageDefaults)
        self.setConfig( INTERFACE_DEFAULTS )
        LOG("Configuration loaded", level = LOG_CNFG )

    #===========================================================================
    def _createTmItem(self, mnemonic, description = ""):
        return TmItemClass(self, mnemonic, description)

    #===========================================================================
    def setup(self, ctxConfig, drvConfig):
        LOG("Setup TM adapter interface")
        self.storeConfig(ctxConfig, drvConfig)
        self.refreshConfig()

    #===========================================================================
    def cleanup(self):
        LOG("Cleanup TM adapter interface")

    #===========================================================================
    def eq(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Description

        Compare two entities and test if their values are equal.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.eq( <tm item>, <value> )
            
            Compares the default value of given TM item against the given value. 
            Default interface configuration is used.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            
            <value> may be a constant, variable, or tm item

        ------------------------------------------------------------------------
        Syntax #2:
            TM.eq( <tm item>, <value>, {config} )
            
            Same as syntax #1, but using a particular configuration for the
            comparison instead of the interface defaults.

        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - ValueFormat:RAW/ENG           Specify the tm item value to use 
              
            - Timeout:<float>               Timeout for tm item value check
              
            - Wait:True/False               Wait for tm item updates or use
                                            the current value
                                            
            - Retries:<int>                 Number of retries for failed comps.
            
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_eq
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[eq], config = useConfig )        
            
    #===========================================================================
    def neq(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Description

        Compare two entities and test if their values are different.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.neq( <tm item>, <value> )
            
            Compares the default value of given TM item against the given value. 
            Default interface configuration is used.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            
            <value> may be a constant, variable, a tm item

        ------------------------------------------------------------------------
        Syntax #2:
            TM.neq( <tm item>, <value>, {config} )
            
            Same as syntax #1, but using a particular configuration for the
            comparison instead of the interface defaults.

        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - ValueFormat:RAW/ENG           Specify the tm item value to use 
              
            - Timeout:<float>               Timeout for tm item value check
              
            - Wait:True/False               Wait for tm item updates or use
                                            the current value
                                            
            - Retries:<int>                 Number of retries for failed comps.
            
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_neq
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[neq], config = useConfig )        
    
    #===========================================================================
    def lt(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Description

        Compare two entities and test if the 1st value is less than the 2nd.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.lt( <tm item>, <value> )
            
            Compares the default value of given TM item against the given value. 
            Default interface configuration is used.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            
            <value> may be a constant, variable, or tm item

        ------------------------------------------------------------------------
        Syntax #2:
            TM.lt( <tm item>, <value>, {config} )
            
            Same as syntax #1, but using a particular configuration for the
            comparison instead of the interface defaults.

        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - ValueFormat:RAW/ENG           Specify the tm item value to use 
              
            - Timeout:<float>               Timeout for tm item value check
              
            - Wait:True/False               Wait for tm item updates or use
                                            the current value
                                            
            - Retries:<int>                 Number of retries for failed comps.
            
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_lt
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[lt], config = useConfig )        
    
    #===========================================================================
    def le(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Description

        Compare two entities and test if the 1st value is less thant or equal
        to the second.  
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.le( <tm item>, <value> )
            
            Compares the default value of given TM item against the given value. 
            Default interface configuration is used.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            
            <value> may be a constant, variable, or tm item

        ------------------------------------------------------------------------
        Syntax #2:
            TM.le( <tm item>, <value>, {config} )
            
            Same as syntax #1, but using a particular configuration for the
            comparison instead of the interface defaults.

        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - ValueFormat:RAW/ENG           Specify the tm item value to use 
              
            - Timeout:<float>               Timeout for tm item value check
              
            - Wait:True/False               Wait for tm item updates or use
                                            the current value
                                            
            - Retries:<int>                 Number of retries for failed comps.
            
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_le
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[le], config = useConfig )        
    
    #===========================================================================
    def gt(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Description

        Compare two entities and test if the 1st value is greater than the 2nd.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.gt( <tm item>, <value> )
            
            Compares the default value of given TM item against the given value. 
            Default interface configuration is used.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            
            <value> may be a constant, variable, or tm item

        ------------------------------------------------------------------------
        Syntax #2:
            TM.gt( <tm item>, <value>, {config} )
            
            Same as syntax #1, but using a particular configuration for the
            comparison instead of the interface defaults.

        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - ValueFormat:RAW/ENG           Specify the tm item value to use 
              
            - Timeout:<float>               Timeout for tm item value check
              
            - Wait:True/False               Wait for tm item updates or use
                                            the current value
                                            
            - Retries:<int>                 Number of retries for failed comps.
            
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_gt
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[gt], config = useConfig )        
    
    #===========================================================================
    def ge(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Description

        Compare two entities and test if the first value is greater than or 
        equal to the second.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.ge( <tm item>, <value> )
            
            Compares the default value of given TM item against the given value. 
            Default interface configuration is used.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            
            <value> may be a constant, variable, or tm item

        ------------------------------------------------------------------------
        Syntax #2:
            TM.ge( <tm item>, <value>, {config} )
            
            Same as syntax #1, but using a particular configuration for the
            comparison instead of the interface defaults.

        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - ValueFormat:RAW/ENG           Specify the tm item value to use 
              
            - Timeout:<float>               Timeout for tm item value check
              
            - Wait:True/False               Wait for tm item updates or use
                                            the current value
                                            
            - Retries:<int>                 Number of retries for failed comps.
            
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_ge
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[ge], config = useConfig )        
    
    #===========================================================================
    def between(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Description

        Compare two entities and test if the first value is between the second
        and the third values.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.between( <tm item>, <value>, <value> )
            
            Compares the default value of given TM item against the given values. 
            Default interface configuration is used.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            
            <value> may be a constant, variable, or tm item

        ------------------------------------------------------------------------
        Syntax #2:
            TM.between( <tm item>, <value>, <value>, {config} )
            
            Same as syntax #1, but using a particular configuration for the
            comparison instead of the interface defaults.

        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - ValueFormat:RAW/ENG           Specify the tm item value to use 
              
            - Timeout:<float>               Timeout for tm item value check
              
            - Wait:True/False               Wait for tm item updates or use
                                            the current value
                                            
            - Retries:<int>                 Number of retries for failed comps.

            - Strict:True/False             Strict comparison flag
            
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        lvalue_item = args[1]
        gvalue_item = args[2]
        cFunc = self.__c_btw
        return self.__bcomparator(param, lvalue_item, gvalue_item, cFunc, COMP_SYMBOLS[bw], config = useConfig)
    
    #===========================================================================
    def not_between(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Description

        Compare two entities and test if the first value is NOT between the second
        and the third values.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.not_between( <tm item>, <value>, <value> )
            
            Compares the default value of given TM item against the given values. 
            Default interface configuration is used.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            
            <value> may be a constant, variable, or tm item

        ------------------------------------------------------------------------
        Syntax #2:
            TM.not_between( <tm item>, <value>, <value>, {config} )
            
            Same as syntax #1, but using a particular configuration for the
            comparison instead of the interface defaults.

        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - ValueFormat:RAW/ENG           Specify the tm item value to use 
              
            - Timeout:<float>               Timeout for tm item value check
              
            - Wait:True/False               Wait for tm item updates or use
                                            the current value
                                            
            - Retries:<int>                 Number of retries for failed comps.

            - Strict:True/False             Strict comparison flag
            
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        lvalue_item = args[1]
        gvalue_item = args[2]
        cFunc = self.__c_nbtw
        return self.__bcomparator(param, lvalue_item, gvalue_item, cFunc, COMP_SYMBOLS[nbw], config = useConfig)
                  
    #===========================================================================
    def refresh(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Description

        Refresh the given TM item or all stored TM items.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.refresh( <tm item> )
            
            Refresh the value of the corresponding tm item.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            
        ------------------------------------------------------------------------
        Syntax #2:
            TM.refresh( <tm item>, {config} )
            
            Same as syntax #1, but using a particular configuration for the
            comparison instead of the interface defaults.

        ------------------------------------------------------------------------
        Syntax #3:
            TM.refresh()
            
            Refresh all stored tm items. See syntax #1.

        ------------------------------------------------------------------------
        Syntax #4:
            TM.refresh( {config} )
            
            Refresh all stored tm items. See syntax #2.

        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - Timeout:<float>               Timeout for tm item value check
              
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        if len(args)==0:
            for param in self.__tmParameters:
                param = self.__tmParameters[param] 
                self.__refreshItemNotify(param, useConfig)
        else:
            param = args[0]
            if type(param)==dict:
                for param in self.__tmParameters:
                    param = self.__tmParameters[param] 
                    self.__refreshItemNotify(param, useConfig)
            else:                         
                if type(param) == str: param = self.__tmParameters[param] 
                self.__refreshItemNotify(param, useConfig)

    #===========================================================================
    def inject(self, *args, **kargs ):
        """
        ------------------------------------------------------------------------
        Description

        Inject values for the given tm items into the GCS.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.inject( <tm item>, <value> )
            
            Inject the value of the corresponding tm item using default
            configuration.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            <value> may be a constant, variable, or tm item.
            
        ------------------------------------------------------------------------
        Syntax #2:
            TM.inject( <tm item>, <value>, {config} )
            
            Inject the value of the corresponding tm item using specific
            configuration.
            
        ------------------------------------------------------------------------
        Syntax #3:
            TM.inject( [ tm item list ] )
            
            Inject a group of tm items with their corresponding values. The
            list format shall be
            
            [ [ <tm_item>, <value>, {config} ], ... ]
            
            Where the config dictionary is optional. This configuration may
            be used to determine the format of the value injected, for example.
            <tm item> may be a TM parameter name, or a tm item instance.
            <value> may be a constant, variable or tm item.

        ------------------------------------------------------------------------
        Syntax #4:
            TM.inject( [ tm item list ], {config} )
            
            Same as Syntax #3, but applying a specific configuration for
            all tm item injections. Specific item configurations (see #3)
            override the global specific configuration.
            
        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - Timeout:<float>               Timeout for tm injection
            - ValueFormat:ENG/RAW           Which tm value to use in injection
                                            when using a tm item
            - Radix:HEX/OCT/BIN/DEC         Radix for values
            - Units:<str>                   Units for values
            - ValueType:LONG/STRING/FLOAT
                        DOUBLE/SHORT/USHORT
                        ULONG/CHAR/BOOLEAN
                        TIME (default LONG) Type for values
              
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
        if len(args)==0 and len(kargs)==0:
            raise SyntaxException("No arguments given")

        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        result = False
        if type(args[0])==list:
            injectionList = args[0]
            for item in injectionList:
                itemConfig = {}
                itemConfig.update(useConfig)
                param = item[0]
                value = item[1]
                if type(item[-1])==dict:
                    itemConfig.update(item[-1])
                if type(param)==str:
                    param = self[param]
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.name(), value, NOTIF_STATUS_PR, "Injecting value")
                result = self._injectItem( param, value, itemConfig )
                if result == True:
                    REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.name(), value, NOTIF_STATUS_OK)
                else:
                    REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.name(), value, NOTIF_STATUS_FL)
        else:
            # Single parameter injection
            if type(args[0])==str:
                param = self[args[0]]
            # Value
            value = args[1]
            # Update with specific configuration if any
            if type(args[-1])==dict:
                useConfig.update(args[-1])
            # Inject the parameter
            REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.name(), value, NOTIF_STATUS_PR, "Injecting value")
            result = self._injectItem( param, value, useConfig )
            if result == True:
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.name(), value, NOTIF_STATUS_OK)
            else:
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.name(), value, NOTIF_STATUS_FL)
                
        return result

    #===========================================================================
    def verify(self, *args, **kargs ):
        """
        ------------------------------------------------------------------------
        Description

        Perform a TM verification. Return True if the verification is 
        successful, False otherwise.
        
        ------------------------------------------------------------------------
        Syntax #1:
            TM.verify( <tm item>, <comparison>, <value> )
            
            Perform a verification with default configuration
            
            <tm item> may be a TM parameter name, or a tm item instance.
            <comparison> is the comparison operator.
            <value> may be a constant, variable or tm item.
            
        ------------------------------------------------------------------------
        Syntax #2:
            TM.verify( <tm item>, <comparison>, <value>, {config} )
            
            Same as #1, but using specific configuration
            
        ------------------------------------------------------------------------
        Syntax #3:
            TM.verify( [ step list ] )
            
            Carry out a list of verifications. The given list shall be like
            
            [ [ <tm_item>, <comparison>, <value>, {config} ], ... ]
            
            Where the config dictionary is optional. This configuration may
            be used to determine the way the tm item value is extracted, for 
            example.
            
            <tm item> may be a TM parameter name, or a tm item instance.
            <comparison> is the comparison operator.
            <value> may be a constant, variable or tm item.

        ------------------------------------------------------------------------
        Syntax #4:
            TM.verify( [ step list ], {config} )
            
            Same as Syntax #3, but applying a specific configuration for
            all tm verifications. Specific item configurations (see #3)
            override the global specific configuration.
            
        ------------------------------------------------------------------------
        Configuration
        
        Possible configuration modifiers are:
        
            - Timeout:<float>               Timeout for tm injection
            - ValueFormat:ENG/RAW           Which tm value to use 
              
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """

        if len(args)==0 and len(kargs)==0:
            raise SyntaxException("No arguments given")
        
        # Obtain global verification config
        self.__useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        # Input cases ----------------------------------------------------------
        numArgs = len(args)
        
        # If the user gives the steps using 'verify =' 
        if numArgs==0:
            # No steps are given as a normal parameter, but the user may be
            # using the keyword "verify" to pass them.
            if kargs.has_key('verify'):
                verificationSteps = kargs.get('verify')
                LOG("Retrieved definition from named argument")
            else:
                raise SyntaxException("Malformed verification: no steps given")
        else:
            # The user is either passing a simple verification step givin TM parameter,
            # comparison operator, and valueitem, or passing a list of verification
            # steps. In addition to this, if the user passes a config dictionary,
            # it is already processed by buildConfig(), thus we shall remove it.
            
            # Check if the last element of the tuple is a dictionary
            if type(args[-1])==dict:
                iargs = args[0:-1]
            else:
                iargs = args
            
            # Now, if the length of the remaining tuple is one, it means that
            # a single step or a list of steps are given. If the length of
            # the tuple is more than one, the user passed the verification
            # parameters without square brackets.
            numArgs = len(iargs)
            if numArgs > 1:
                # Create a list of steps, with a single step containing the
                # passed parameters
                if numArgs < 3:
                    raise SyntaxException("Malformed condition")
                verificationSteps = [ [ item for item in args ] ]
                LOG("Built single step list")
            else:
                # Get rid of the tuple and get the first element only
                iargs = iargs[0]
                # Here we have a list. To distinguish between a single step or a 
                # list of steps, just check if the first element is a list 
                # (a step) or not
                if type(iargs[0])==list:
                    verificationSteps = iargs
                    LOG("Using direct definition")
                else:
                    if len(iargs)<3:
                        raise SyntaxException("Malformed condition")
                    verificationSteps = [ iargs ] 
                    LOG("Built list of steps" + repr(iargs))
            
        try:
            self._operationStart()
            
            # Prepare verifiers
            self.__prepareVerification(verificationSteps)
    
            # Start verifiers
            #TODO review
            self.__startVerifiers()
            
            # Wait all verifications to be finished
            self.__waitVerifiers()
            
            # Check overall result
            overallResult,errors = self.__checkVerifiers()
    
            # If there is a failure somewhere, raise the exception
            someError = False
            whichError = ""
            for ed in errors:
                if ed[1] is not None:
                    someError = True
                    if len(whichError)>0: whichError += ","
                    whichError += ed[0].split()[-1]
            if someError:
                raise DriverException("Verification failed", "Could not evaluate all TM conditions (check messages)")
            
        finally:
            self._operationEnd()
        
        return overallResult
    
    #===========================================================================
    def updateVerificationStatus(self, verifier):
        LOG("Verification status: " + verifier.name + "=" + verifier.value + "," + verifier.status) 
        if self.__useConfig.has_key(Notify):
            if not self.__useConfig.get(Notify): return
        self.__verifMutex.acquire()
        entry = self.__verifTable[verifier.step]
        entry[1] = verifier.value
        entry[2] = verifier.status
        entry[3] = verifier.reason
        entry[4] = verifier.updtime
        names  = ""
        values = ""
        status = ""
        reason = ""
        times  = ""
        for entry in self.__verifTable:
            if len(names)>0: 
                names  = names + ITEM_SEP
                values = values + ITEM_SEP
                status = status + ITEM_SEP
                reason = reason + ITEM_SEP
                times  = times + ITEM_SEP
            names = names + entry[0]
            values = values + entry[1]
            status = status + entry[2]
            times = times + entry[4]
            if len(entry[3])==0:
                reason = reason + " "
            else:
                reason = reason + entry[3]
            
        REGISTRY['CIF'].notify( NOTIF_TYPE_VERIF, names, values, status, reason, times)
        self.__verifMutex.release()
    
    #===========================================================================
    def setLimit(self, *args, **kargs ):
        
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        #TODO implement more flexible input
        if len(args)!=3: 
            raise SyntaxException("Expected parameter, limit name and value")

        param = args[0]
        limit = args[1]
        value = args[2]

        if type(param)==str:
            param = self[param]

        if type(limit)!=str:
            raise SyntaxException("Expected a limit name")
        
        return self._setLimit( param, limit, value, useConfig)

    #===========================================================================
    def getLimit(self, *args, **kargs ):

        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        #TODO implement more flexible input
        if len(args)!=2: 
            raise SyntaxException("Expected parameter, and limit name")

        param = args[0]
        limit = args[1]

        if type(param)==str:
            param = self[param]

        if type(limit)!=str:
            raise SyntaxException("Expected a limit name")
        
        return self._getLimit( param, limit, useConfig)

    #===========================================================================
    def getLimits(self, *args, **kargs ):

        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        #TODO implement more flexible input
        if len(args)!=1: 
            raise SyntaxException("Expected parameter")

        param = args[0]

        if type(param)==str:
            param = self[param]

        return self._getLimits( param, useConfig)

    #===========================================================================
    def setLimits(self, *args, **kargs ):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        if len(args)!=2: 
            raise SyntaxException("Expected parameter and limits values")

        param = args[0]
        limits = args[1]

        if type(param)==str:
            param = self[param]

        return self._setLimits( param, limits, useConfig )

    #===========================================================================
    def loadLimits(self, *args, **kargs ):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        limitsList = args[0]
        return self._loadLimits( limitsList, useConfig )
               
    #===========================================================================
    def _refreshItem(self, param, config = {} ):
        # SHALL RETURN eng,raw,status
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _injectItem(self, param, value, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _setLimit(self, param, limit, value, config ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _getLimit(self, param, limit, config ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _getLimits(self, param, config ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _setLimits(self, param, limits, config ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _loadLimits(self, param, limits, config ):
        """
        param: TM item class or string with the param name
        limits: dictionary containing the limit definitions with LoYel, Midpoint, Expected, etc
        """
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 
               
    #===========================================================================
    def _operationStart(self):
        LOG("TM interface started operation")

    #===========================================================================
    def _operationEnd(self):
        LOG("TM interface finished  operation")
               
    #===========================================================================
    def __refreshItemNotify(self, param, useConfig):
        try:
            doNotify = useConfig.get(Notify) == True 
            if doNotify:
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.name(), "???", NOTIF_STATUS_PR, "" )
            self.__refreshItemValidity(param, useConfig)
            if doNotify:
                if useConfig.get(ValueFormat) == ENG:
                    value = repr(param._engValue)
                else:
                    value = repr(param._rawValue)
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.name(), value, NOTIF_STATUS_OK, "" )
        except DriverException,ex:
            if doNotify:
                reason = ex.message
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.name(), "???", NOTIF_STATUS_FL, reason )
            raise ex

    #===========================================================================
    def __refreshItemValidity(self, param, config):
        value,validity = self._refreshItem(param, config)
        if not validity:
            raise DriverException("Parameter invalid")
        return [value,validity]
    
    #===========================================================================
    def __calccounter(self, retries):
        counter = retries
        if counter < 1: 
            counter = 1
        return counter
        
    #===========================================================================
    def __timeiter(self, value, config):
        comp = value
        if isinstance(value, TmItemClass):
            comp,status = self.__refreshItemValidity( value, config )
        return comp

    #===========================================================================
    def __comparator(self, param, value_item, cFunc, symbol, config = {} ):
        
        # If no configuration is given, use the interface defaults
        if len(config)==0:
            config = self.getConfig()

        # Retrieve configuration parameters
        retries = config.get(Retries)
        tolerance = config.get(Tolerance)
        ignoreCase = config.get(IgnoreCase)

        # Retrieve initial values
        if type(param)==str:
            param = self[param]
        
        # Prepare comparison tools
        counter = self.__calccounter(retries)
        comp = self.__timeiter(value_item, config)

        # In the first iteration the user config will be used. In the rest
        # of retries, wait will be true in any case.
        firstCheck = True

        LOG("### Starting TM comparison")
        comparisonResult = False

        # Perform comparisons        
        while counter > 0: 
            LOG("### (" + str(counter) + ") Retrieving " + param.name() + " comparison value")
            cvalue,status = self.__refreshItemValidity(param, config)

            LOG("### (" + str(counter) + ") Comparing " + repr(param.name()) + "=" + repr(cvalue) + " against " + repr(comp) +\
                ", iteration " + str(counter) + ", tolerance " + str(tolerance))
            LOG("### (" + str(counter) + ") Comparison config: " + repr(config))
            if cFunc(cvalue,comp,tolerance,ignoreCase):
                LOG("### (" + str(counter) + ") Comparison success") 
                comparisonResult = True
                break
            else:
                LOG("### (" + str(counter) + ") Comparison failed")
                comparisonResult = False
                
            if firstCheck: 
                config[Wait] = True
                firstCheck = False
            if config.has_key(Notify) and config[Notify] == True:
                LOG("### (" + str(counter) + ") Notify comparison retry")
                val = symbol + str(comp)
                reason = "Value is " + str(cvalue) + ", retrying comparison (" + str(counter) + " left)"
                if config.has_key("STEP_ID"):
                    step = config.get("STEP_ID")
                    for verifier in self.__verifiers:
                        if verifier.step == step:
                            verifier.value = val
                            verifier.reason = reason
                            #REGISTRY['CIF'].write( "Retrying comparison for " + param.name() + " (" + str(counter) + " retries left)", {Severity:WARNING})
                            self.updateVerificationStatus(verifier)
                            break
                else:
                    REGISTRY['CIF'].notify( NOTIF_TYPE_VERIF, param.name(), val, NOTIF_STATUS_PR, reason )

            comp = self.__timeiter(value_item, config)
            counter = counter - 1

        LOG("### Finished TM comparison: " + repr(comparisonResult))

        return comparisonResult

    #===========================================================================
    def __bcomparator(self, param, lvalue_item, gvalue_item, cFunc, symbol, config = {} ):
        # If no configuration is given, use the interface defaults
        if len(config)==0:
            config = self.getConfig()

        # Retrieve configuration parameters
        retries = config.get(Retries)
        strict = config.get(Strict)
        tolerance = config.get(Tolerance)

        # Retrieve initial values
        if type(param)==str:
            param = self[param]
        cvalue,status = self.__refreshItemValidity(param, config)

        counter = self.__calccounter(retries)
        comp_lt = self.__timeiter(lvalue_item, config)
        comp_gt = self.__timeiter(gvalue_item, config)
        
        while counter > 0: 
            if cFunc(cvalue,comp_lt, comp_gt, strict, tolerance): return True
            cvalue,status = self.__refreshItemValidity(param, config)
            comp_lt = self.__timeiter(lvalue_item, config)
            comp_gt = self.__timeiter(gvalue_item, config)
            counter = counter - 1
            if config.has_key(Notify) and config[Notify] == True:
                val = symbol + " " + str(comp_lt) + "," + str(comp_gt)
                reason = "Value is " + str(cvalue) + ", retrying comparison (" + str(counter) + ")"
                if config.has_key("STEP_ID"):
                    step = config.get("STEP_ID")
                    for verifier in self.__verifiers:
                        if verifier.step == step:
                            verifier.value = val
                            verifier.reason = reason
                            self.updateVerificationStatus(verifier)
                            break
                else:
                    REGISTRY['CIF'].notify( NOTIF_TYPE_VERIF, param.name(), val, NOTIF_STATUS_PR, reason )
        return False

    #===========================================================================
    def __c_eq(self, cvalue, comp, tolerance = 0, ignoreCase = False):
        
        #-----------------------------------------------------------------------
        # String comparisons (tolerance does not apply)
        if type(cvalue) == str:
            if ignoreCase:
                return (cvalue.upper() == comp.upper())
            else:
                return cvalue == comp
        #-----------------------------------------------------------------------
        # Numeric comparisons (ignoreCase does not apply)
        else:
            if tolerance<0:
                raise DriverException("Error: cannot accept negative tolerance")
            elif tolerance>0:
                return self.__c_btw(cvalue, comp-tolerance, comp+tolerance, False)
            else: 
                if type(cvalue)==float and type(comp)==float:
                    dnm = max(abs(cvalue),abs(comp))
                    if not dnm > 0: dnm = 1 
                    return ( abs(cvalue-comp) / dnm ) < EPSILON
                else:
                    return (cvalue == comp)
        
    #===========================================================================
    def __c_neq(self, cvalue, comp, tolerance = 0, ignoreCase = False): 

        #-----------------------------------------------------------------------
        # String comparisons (tolerance does not apply)
        if type(cvalue) == str:
            if ignoreCase:
                return (cvalue.upper() != comp.upper())
            else:
                return cvalue != comp
        #-----------------------------------------------------------------------
        # Numeric comparisons (ignoreCase does not apply)
        else:
            if tolerance<0:
                raise DriverException("Error: cannot accept negative tolerance")
            elif tolerance>0:
                return self.__c_nbtw(cvalue, comp-tolerance, comp+tolerance, False)
            else: 
                if type(cvalue)==float and type(comp)==float:
                    dnm = max(abs(cvalue),abs(comp))
                    if not dnm > 0: dnm = 1 
                    return ( abs(cvalue-comp) / dnm ) > EPSILON
                else:
                    return (cvalue != comp)
    
    #===========================================================================
    def __c_lt(self, cvalue, comp, tolerance = 0, ignoreCase = False): 
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if tolerance<0:
            raise DriverException("Error: cannot accept negative tolerance")
        else: 
            return (cvalue < comp + tolerance)
        
    #===========================================================================
    def __c_le(self, cvalue, comp, tolerance = 0, ignoreCase = False): 
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if tolerance<0:
            raise DriverException("Error: cannot accept negative tolerance")
        else: 
            return (cvalue <= comp + tolerance)
        
    #===========================================================================
    def __c_gt(self, cvalue, comp, tolerance = 0, ignoreCase = False): 
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if tolerance<0:
            raise DriverException("Error: cannot accept negative tolerance")
        else: 
            return (cvalue > comp - tolerance)
        
    #===========================================================================
    def __c_ge(self, cvalue, comp, tolerance = 0, ignoreCase = False): 
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if tolerance<0:
            raise DriverException("Error: cannot accept negative tolerance")
        else: 
            return (cvalue >= comp - tolerance)
                
    #===========================================================================
    def __c_btw(self, cvalue, lcomp, gcomp, strict, tolerance = 0):
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if tolerance<0:
            raise DriverException("Error: cannot accept negative tolerance")
        else:
            if strict:
                return (lcomp - tolerance < cvalue < gcomp + tolerance)
            else:
                return (lcomp - tolerance <= cvalue <= gcomp + tolerance)
            
    #===========================================================================
    def __c_nbtw(self, cvalue, lcomp, gcomp, strict, tolerance = 0): 
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if tolerance<0:
            raise DriverException("Error: cannot accept negative tolerance")
        else:
            if strict:
                return (lcomp + tolerance > cvalue) or (gcomp - tolerance < cvalue)
            else:
                return (lcomp + tolerance >= cvalue) or (gcomp - tolerance <= cvalue)

    #===========================================================================
    def __checkComparsionArgs(self, args, kargs):
        if len(args)==0 and len(kargs)==0:
            raise SyntaxException("Received no parameters")
        
    #===========================================================================
    def __resetVerification(self):
        self.__verifTable = []
        for v in self.__verifiers[:]:
            self.__verifiers.remove(v)
                    
    #===========================================================================
    def __prepareVerification(self, verificationSteps):
        self.__resetVerification()
        stepCount = 0
        REGISTRY['CIF'].write( "Verifying telemetry conditions" )
        for step in verificationSteps:
            message = "    " + str(stepCount) + ": Parameter " + repr(step[0]) +\
                      " " + COMP_SYMBOLS[step[1]] + " " + repr(step[2])
            # Used for ternary operators
            if len(step) == 4:
                message = message + " and " + repr(step[3])
            REGISTRY['CIF'].write(message)
            verifier = TmVerifierClass( self, stepCount, step, self.__useConfig )
            self.__verifiers.append( verifier )
            self.__verifTable.append([verifier.name,verifier.value,
                                      verifier.status,verifier.reason,verifier.updtime])
            stepCount = stepCount + 1

    #===========================================================================
    def __startVerifiers(self):
        for v in self.__verifiers: v.start()

    #===========================================================================
    def __waitVerifiers(self):
        while True:
            time.sleep(0.2)
            someAlive = False
            for v in self.__verifiers:
                if v.isAlive():
                    someAlive = True
                    break
            if not someAlive: return

    #===========================================================================
    def __checkVerifiers(self):
        someWrong = False
        overallResult = TmResult() 
        errors = []
        for v in self.__verifiers:
            defn = v.getDefinition()
            keyName = str(defn[0]) + ": " + defn[1][0]
            overallResult[keyName] = (not v.failed)
            errors.append( [ keyName, v.error ] ) 
            if v.failed:
                reason = v.reason
                stepNum = str(defn[0])
                message = "Verification " + stepNum + " failed. "
                message += reason + "."
                REGISTRY['CIF'].write( message , {Severity:ERROR} )
                someWrong = True
        if not someWrong: 
            REGISTRY['CIF'].write( "Verifications succeeded" )
        return overallResult,errors

