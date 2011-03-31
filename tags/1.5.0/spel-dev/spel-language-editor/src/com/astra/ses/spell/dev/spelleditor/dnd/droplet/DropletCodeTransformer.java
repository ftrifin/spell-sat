///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.dnd.droplet
// 
// FILE      : DropletCodeTransformer.java
//
// DATE      : 2009-10-06
//
// Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.spelleditor.dnd.droplet;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.astra.ses.spell.dev.database.impl.commanding.Command;
import com.astra.ses.spell.dev.database.impl.commanding.CommandSequence;
import com.astra.ses.spell.dev.database.impl.commanding.args.CommandArgument;
import com.astra.ses.spell.dev.database.impl.telemetry.TelemetryParameter;

public class DropletCodeTransformer {

	/** Default XML Header */
	private static final String XML_HEADER = "<?xml version=\"1.0\"?>";
	/** Root node name */
	private static final String ROOT_NODE = "droplet";
	/** Loop node */
	private static final String LOOP_NODE = "foreach";
	private static final String LOOP_TYPE_TM = "TM";
	private static final String LOOP_TYPE_TC = "TC";
	/** TM name */
	private static final String TM_NAME = "tm_name";
	/** TM description */
	private static final String TM_DESCRIPTION = "tm_desc";
	/** TC element's name */
	private static final String TC_NAME = "tc_name";
	/** TC description */
	private static final String TC_DESCRIPTION = "tc_desc";
	/** TC arguments */
	private static final String TC_ARGUMENTS = "tc_args";
	/** TC type */
	private static final String TC_TYPE = "tc_type";
	
	/***************************************************************************
	 * Constructor
	 * @param pseudoCode
	 **************************************************************************/
	public DropletCodeTransformer()
	{
	}
	
	/***************************************************************************
	 * Transform the pseudoCode into Spell code with the given 
	 * tm and tc elements
	 * @param tm
	 * @param tc
	 * @return
	 **************************************************************************/
	public String transform(String code, TelemetryParameter[] tm, Command[] tc)
	{
		/*
		 *  Droplet code is converted to xml code by putting a xml header
		 *  and a root node
		 */
		String xml = transformToXml(code);
		String result = "";
		/*
		 * Now parse the xml code
		 */
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			StringReader in = new StringReader(xml);
			Document xmlDroplet = db.parse(new InputSource(in));
			NodeList nodeList = xmlDroplet.getElementsByTagName(ROOT_NODE);
			if (nodeList.getLength() != 1)
			{
				// There is more than one droplet element in the xml
				return "";
			}
			nodeList = nodeList.item(0).getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				Node node = nodeList.item(i);
				result += processNode(node, tm, tc, -1, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**************************************************************************
	 * 
	 * @param node
	 * @param tm
	 * @param tc
	 * @param currentTM
	 * @param currentTC
	 * @return
	 *************************************************************************/
	private String processNode(Node node, 
			TelemetryParameter[] tm, 
			Command[] tc,
			int currentTM,
			int currentTC
			)
	{
		String result = "";
		switch (node.getNodeType())
		{
			case Node.TEXT_NODE:
				result = processTextNode(node);
				break;
			case Node.ELEMENT_NODE:
				result = processElementNode(node, tm, tc, currentTM, currentTC);
				break;
			default: 
				break;
		}
		return result;
	}
	
	/***************************************************************************
	 * 
	 * @param node
	 * @return
	 *************************************************************************/
	private String processTextNode(Node node)
	{
		return node.getTextContent();
	}
	
	/***************************************************************************
	 * 
	 * @param node
	 * @param tm
	 * @param tc
	 * @return
	 **************************************************************************/
	private String processElementNode(
			Node node, 
			TelemetryParameter[] tm, 
			Command[] tc,
			int currentTM,
			int currentTC
			)
	{
		String result = "";
		String name = node.getNodeName();
		int tmIndex = Math.max(0, currentTM);
		int tcIndex = Math.max(0, currentTC);
		if (name.equals(LOOP_NODE))
		{
			result = processTMLoop(node, tm, tc, currentTM, currentTC);
		}
		else if (name.equals(TM_NAME))
		{
			TelemetryParameter param = tm[tmIndex];
			result = param.getName();
		}
		else if (name.equals(TM_DESCRIPTION))
		{
			TelemetryParameter param = tm[tmIndex];
			result = param.getDescription();
		}
		else if (name.equals(TC_NAME))
		{
			Command comm = tc[tcIndex];
			result = comm.getName();
		}
		else if (name.equals(TC_DESCRIPTION))
		{
			Command comm = tc[tcIndex];
			result = comm.getDescription();
		}
		else if (name.equals(TC_ARGUMENTS))
		{
			Command comm = tc[tcIndex];
			result = getArgumentsList(comm);
		}
		else if (name.equals(TC_TYPE))
		{
			Command comm = tc[tcIndex];
			result = getCommandType(comm);
		}
		return result;
	}
	
	/****************************************************************************
	 * Process a loop node
	 * @param loopNode
	 * @param tm
	 * @param tc
	 * @param currentTM
	 * @param currentTC
	 * @return
	 ***************************************************************************/
	private String processTMLoop(
			Node loopNode,
			TelemetryParameter[] tm,
			Command[] tc,
			int currentTM,
			int currentTC
			)
	{
		String result = "";
		NamedNodeMap attributes = loopNode.getAttributes();
		String loopType = LOOP_TYPE_TM;
		if (attributes.getNamedItem("type") != null)
		{
			loopType = attributes.getNamedItem("type").getNodeValue();
		}
		String loopSeparator = "";
		if (attributes.getNamedItem("separator") != null)
		{
			loopSeparator = attributes.getNamedItem("separator").getNodeValue();
		}
		NodeList nodes = loopNode.getChildNodes();
		int nodesCount = nodes.getLength();
		if (loopType.equals(LOOP_TYPE_TM))
		{
			for (int i = 0; i < tm.length; i++)
			{
				for (int j = 0; j < nodesCount; j++)
				{
					Node node = nodes.item(j);
					result += processNode(node, tm, tc, i, currentTC);
				}
				if (i < tm.length - 1)
				{
					result += loopSeparator;
				}
			}
		}
		else if (loopType.equals(LOOP_TYPE_TC))
		{
			for (int i = 0; i < tc.length; i++)
			{
				for (int j = 0; j < nodesCount; j++)
				{
					Node node = nodes.item(j);
					result += processNode(node, tm, tc, currentTM, i);
				}
				if (i < tc.length - 1)
				{
					result += loopSeparator;
				}
			}
		}

		return result;
	}
	
	/****************************************************************************
	 * Transform the command arguments into a Spell language list
	 * @param comm
	 * @return
	 ***************************************************************************/
	private String getArgumentsList(Command comm)
	{
		String result = null;
		CommandArgument[] args = comm.getArguments();
		if (args.length> 0)
		{
			result = "args=[";
			int i = 0;
			for (CommandArgument arg : args)
			{
				result += "['"+ arg.getName() +"', "+ arg.getDefaultValue() +"]";
				if (i < args.length - 1)
				{
					result += ",";
				}
				i++;
			}
			result += "]";
		}
		return result;
	}
	
	/***************************************************************************
	 * Determine which command type is being processed and generate the code
	 * @return
	 **************************************************************************/
	private String getCommandType(Command comm)
	{
		String commandType = "command";
		if (comm instanceof CommandSequence)
		{
			commandType = "sequence";
		}
		return commandType;
	}
	
	/****************************************************************************
	 * 
	 * @param dropletCode
	 * @return
	 ***************************************************************************/
	private String transformToXml(String dropletCode)
	{
		String dropletStart = "<" + ROOT_NODE + ">";
		String dropletEnd = "</" + ROOT_NODE + ">";
		dropletCode = dropletCode.replaceAll(
				"<" + TM_NAME + ">",
				"<" + TM_NAME + " />");
		dropletCode = dropletCode.replaceAll(
				"<" + TM_DESCRIPTION + ">",
				"<" + TM_DESCRIPTION + " />");
		dropletCode = dropletCode.replaceAll(
				"<" + TC_NAME + ">",
				"<" + TC_NAME + " />");
		dropletCode = dropletCode.replaceAll(
				"<" + TC_DESCRIPTION + ">",
				"<" + TC_DESCRIPTION + " />");
		dropletCode = dropletCode.replaceAll(
				"<" + TC_ARGUMENTS + ">",
				"<" + TC_ARGUMENTS + " />");
		dropletCode = dropletCode.replaceAll(
				"<" + TC_TYPE + ">",
				"<" + TC_TYPE + " />");
		return XML_HEADER + dropletStart + dropletCode + dropletEnd;
	}
}
