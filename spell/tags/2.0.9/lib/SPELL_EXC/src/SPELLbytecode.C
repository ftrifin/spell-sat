// ################################################################################
// FILE       : SPELLbytecode.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the bytecode analyzer
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
#include "SPELL_EXC/SPELLbytecode.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------
#include "opcode.h"



// Macros for accessing opcodes
#define NEXTOP()        (*next_instr++)
#define NEXTARG()       (next_instr += 2, (next_instr[-1]<<8) + next_instr[-2])
#define INSTR_OFFSET()  ((int)(next_instr - first_instr))
// Debugging macro
#define DUMP            DEBUG("[" + ISTR(info->lineno) +\
                           ":" + ISTR(info->offset) +  "]: " + OPCODES[info->opcode] \
                           + "/" + ISTR(oparg))

#define BDUMP(x)        DEBUG("------------------- Block [" + ISTR(x->start) + "->"\
                        + ISTR(x->end) + "] Line " + ISTR(x->lineno) + " Active " \
                        + (x->active ? "true" : "false"))

// Debugging purposes: opcode names
static std::string OPCODES[] =
{
    "STOP_CODE",            //0
    "POP_TOP",              //1
    "ROT_TWO",              //2
    "ROT_THREE",            //3
    "DUP_TOP",              //4
    "ROT_FOUR",             //5
    "",                     //6
    "",                     //7
    "",                     //8
    "NOP",                  //9
    "UNARY_POSITIVE",       //10
    "UNARY_NEGATIVE",       //11
    "UNARY_NOT",            //12
    "UNARY_CONVERT",        //13
    "",                     //14
    "UNARY_INVERT",         //15
    "",                     //16
    "",                     //17
    "LIST_APPEND",          //18
    "BINARY_POWER",         //19
    "BINARY_MULTIPLY",      //20
    "BINARY_DIVIDE",        //21
    "BINARY_MODULO",        //22
    "BINARY_ADD",           //23
    "BINARY_SUBTRACT",      //24
    "BINARY_SUBSCR",        //25
    "BINARY_FLOOR_DIVIDE",  //26
    "BINARY_TRUE_DIVIDE",   //27
    "INPLACE_FLOOR_DIVIDE", //28
    "INPLACE_TRUE_DIVIDE",  //29
    "SLICE",                //30
    "SLICE+1",              //31
    "SLICE+2",              //32
    "SLICE+3",              //33
    "",                     //34
    "",                     //35
    "",                     //36
    "",                     //37
    "",                     //38
    "",                     //39
    "STORE_SLICE",          //40
    "STORE_SLICE+1",        //41
    "STORE_SLICE+2",        //42
    "STORE_SLICE+3",        //43
    "",                     //44
    "",                     //45
    "",                     //46
    "",                     //47
    "",                     //48
    "",                     //49
    "DELETE_SLICE",         //50
    "DELETE_SLICE+1",       //51
    "DELETE_SLICE+2",       //52
    "DELETE_SLICE+3",       //53
    "",                     //54
    "INPLACE_ADD",          //55
    "INPLACE_SUBTRACT",     //56
    "INPLACE_MULTIPLY",     //57
    "INPLACE_DIVIDE",       //58
    "INPLACE_MODULO",       //59
    "STORE_SUBSCR",         //60
    "DELETE_SUBSCR",        //61
    "BINARY_LSHIFT",        //62
    "BINARY_RSHIFT",        //63
    "BINARY_AND",           //64
    "BINARY_XOR",           //65
    "BINARY_OR",            //66
    "INPLACE_POWER",        //67
    "GET_ITER",             //68
    "",                     //69
    "PRINT_EXPR",           //70
    "PRINT_ITEM",           //71
    "PRINT_NEWLINE",        //72
    "PRINT_ITEM_TO",        //73
    "PRINT_NEWLINE_TO",     //74
    "INPLACE_LSHIFT",       //75
    "INPLACE_RSHIFT",       //76
    "INPLACE_AND",          //77
    "INPLACE_XOR",          //78
    "INPLACE_OR",           //79
    "BREAK_LOOP",           //80
    "WITH_CLEANUP",         //81
    "LOAD_LOCALS",          //82
    "RETURN_VALUE",         //83
    "IMPORT_STAR",          //84
    "EXEC_STMT",            //85
    "YIELD_VALUE",          //86
    "POP_BLOCK",            //87
    "END_FINALLY",          //88
    "BUILD_CLASS",          //89
    "STORE_NAME",           //90
    "DELETE_NAME",          //91
    "UNPACK_SEQUENCE"       //92
    "FOR_ITER",             //93
    "",                     //94
    "",                     //95
    "STORE_ATTR",           //96
    "DELETE_ATTR",          //97
    "STORE_GLOBAL",         //98
    "DELETE_GLOBAL",        //99
    "DUP_TOPX",             //100
    "LOAD_CONST",           //101
    "LOAD_NAME",            //102
    "BUILD_TUPLE",          //103
    "BUILD_LIST",
    "BUILD_MAP",
    "LOAD_ATTR",
    "COMPARE_OP",
    "IMPORT_NAME",
    "IMPORT_FROM",
    "",
    "JUMP_FORWARD",
    "JUMP_IF_FALSE",
    "JUMP_IF_TRUE",
    "JUMP_ABSOLUTE",
    "",
    "",
    "LOAD_GLOBAL",
    "",
    "",
    "CONTINUE_LOOP",
    "SETUP_LOOP",
    "SETUP_EXCEPT",
    "SETUP_FINALLY",
    "",
    "LOAD_FAST",
    "STORE_FAST",
    "DELETE_FAST",
    "",
    "",
    "",
    "RAISE_VARARGS",
    "CALL_FUNCTION",
    "MAKE_FUNCTION",
    "BUILD_SLICE",
    "MAKE_CLOSURE",
    "LOAD_CLOSURE",
    "LOAD_DEREF",
    "STORE_DEREF",
    "",
    "",
    "CALL_FUNCTION_VAR",
    "CALL_FUNCTION_KW",
    "CALL_FUNCTION_VAR_KW",
    "EXTENDED_ARG"
};

//=============================================================================
// CONSTRUCTOR    : SPELLbytecode::SPELLbytecode
//=============================================================================
SPELLbytecode::SPELLbytecode( PyCodeObject* code )
    : m_code(code)
{
    DEBUG("SPELLbytecode created");
    analyze();
}

//=============================================================================
// DESTRUCTOR    : SPELLbytecode::~SPELLbytecode
//=============================================================================
SPELLbytecode::~SPELLbytecode()
{
    DEBUG("SPELLbytecode destroyed");
    LineList::iterator it;
    LineList::iterator end = m_lines.end();
    for( it = m_lines.begin(); it != end; it++ ) delete *it;
    TryBlockList::iterator tit;
    TryBlockList::iterator tend = m_tryBlocks.end();
    for( tit = m_tryBlocks.begin(); tit != tend; tit++ ) delete *tit;
}

//=============================================================================
// METHOD     : SPELLbytecode::analyze
//=============================================================================
void SPELLbytecode::analyze()
{
    assert( m_code != NULL );
    // Pointer to initial instruction (used by macros)
    unsigned char* first_instr = (unsigned char*) PyString_AS_STRING(m_code->co_code);
    // Pointer to current instruction (used by macros)
    register unsigned char* next_instr = first_instr;
    // Opcode argument
    unsigned int oparg;
    // Stores the previous line
    unsigned int prevLine = 0;

    // Will be true when there is no more bytecode to process
    bool finished = false;
    unsigned int callDepth = 0;
    // Create try block structure
	TryBlock* tb = NULL;
	// Holds the bytecode offset for except statements
	unsigned int except_offset = 0;

    while(not finished)
    {
        // Create one BLine info structure per bytecode instruction
        BLine* info = new BLine();
        // Get the instruction offset
        info->offset = INSTR_OFFSET();
        // Get the corresponding script line
        info->lineno = PyCode_Addr2Line(m_code, info->offset);
        // Obtain the opcode
        info->opcode = NEXTOP();
		// We will ignore this, for the moment
		oparg  = 0;
		if (HAS_ARG(info->opcode)) oparg = NEXTARG();

        if (( prevLine > 0 ) && ( info->lineno != prevLine ))
        {
        	info->executable = false;

        	/** \todo
			// Depending on the bytecode, either set the block as active,
			// or finish the loop (RETURN_VALUE is found at the end of the script)
			// This is maybe wrong, need to check if RETURN_VALUE is found
			// in function code objects, probably yes... */
			switch(info->opcode)
			{
			case LOAD_NAME:
				callDepth++;
				info->executable = true;
				break;
			case CALL_FUNCTION:
				callDepth--;
				info->executable = true;
				break;
			case STORE_NAME:
			case IMPORT_NAME:
			case JUMP_FORWARD:
			case JUMP_IF_FALSE:
			case JUMP_IF_TRUE:
			case JUMP_ABSOLUTE:
			case RETURN_VALUE:
			{
				info->executable = (callDepth==0);
				break;
			}
			default:
				break;
			}

            // Put the info in place
            m_lines.push_back(info);
        }

        switch(info->opcode)
        {
        case RETURN_VALUE:
			info->executable = true;
			m_lastAddr = info->offset;
        	finished = true;
        	break;
        case SETUP_EXCEPT:
			tb = new TryBlock();
			std::cerr << "START TRYBLOCK " << info->lineno << " TO OFFSET " << oparg << std::endl;
			tb->try_lineno = info->lineno;
			except_offset = info->offset + oparg + 3; // This is the real destination offset
			break;
        case END_FINALLY:
			tb->end_lineno = info->lineno;
			std::cerr << "TRYBLOCK " << tb->try_lineno << "," << tb->bexcept_lineno << "," << tb->except_lineno << "," << tb->end_lineno << std::endl;
			m_tryBlocks.push_back( tb );
			break;
		}

        // Debugging purposes
        //DUMP;

        // This should always happen between a SETUP_EXCEPT and an END_FINALLY
        if (tb && (except_offset == info->offset))
        {
        	// The last line before the except
        	tb->bexcept_lineno = prevLine;
        	// The line of the except
        	tb->except_lineno = info->lineno;
        	std::cerr << "EXCEPT " << prevLine << "," << info->lineno << std::endl;
        }

        prevLine = info->lineno;
        m_lastLine = info->lineno;
    }
}

//=============================================================================
// METHOD     : SPELLbytecode::isExecutable
//=============================================================================
bool SPELLbytecode::isExecutable( unsigned int lineNo ) const
{
	LineList::const_iterator it;
	LineList::const_iterator end = m_lines.end();
	for(it = m_lines.begin(); it != end; it++)
	{
		if ((*it)->lineno == lineNo)
		{
			return (*it)->executable;
		}
	}
	return false;
}

//=============================================================================
// METHOD     : SPELLbytecode::isInTryBlock
//=============================================================================
bool SPELLbytecode::isInTryBlock( unsigned int lineNo ) const
{
	TryBlockList::const_iterator it;
	TryBlockList::const_iterator end = m_tryBlocks.end();
	for(it = m_tryBlocks.begin(); it != end; it++)
	{
		if ( ((*it)->try_lineno < lineNo) && ((*it)->except_lineno > lineNo) )
		{
			return true;
		}
	}
	return false;
}

//=============================================================================
// METHOD     : SPELLbytecode::isTryBlockEnd
//=============================================================================
bool SPELLbytecode::isTryBlockEnd( unsigned int lineNo ) const
{
	TryBlockList::const_iterator it;
	TryBlockList::const_iterator end = m_tryBlocks.end();
	for(it = m_tryBlocks.begin(); it != end; it++)
	{
		if ( ((*it)->try_lineno < lineNo) && ((*it)->except_lineno > lineNo) )
		{
			if ((*it)->bexcept_lineno == lineNo) return true;
			return false;
		}
	}
	return false;
}

//=============================================================================
// METHOD     : SPELLbytecode::tryBlockEnd
//=============================================================================
int SPELLbytecode::tryBlockEndLine( unsigned int lineNo ) const
{
	TryBlockList::const_iterator it;
	TryBlockList::const_iterator end = m_tryBlocks.end();
	for(it = m_tryBlocks.begin(); it != end; it++)
	{
		if ( ((*it)->try_lineno < lineNo) && ((*it)->except_lineno > lineNo) )
		{
			return (*it)->end_lineno;
		}
	}
	return -1;
}
