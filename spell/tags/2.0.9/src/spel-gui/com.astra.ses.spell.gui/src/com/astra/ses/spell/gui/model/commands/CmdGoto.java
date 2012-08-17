package com.astra.ses.spell.gui.model.commands;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.dialogs.GotoDialog;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;

public class CmdGoto extends AbstractHandler
{

	/** Command id */
	public static final String	ID	       = "com.astra.ses.spell.gui.commands.Goto";

	/** Proc id command argument */
	public static final String	ARG_PROCID	= "procId";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		GotoDialog dialog = new GotoDialog(Display.getCurrent()
		        .getActiveShell());
		Object result = CommandResult.NO_EFFECT;
		// Process the returned value
		if (dialog.open() == Window.OK)
		{
			String target = dialog.getTarget();
			if (target != null && (!target.isEmpty()))
			{
				if (dialog.isLabel())
				{
					HashMap<String, String> args = new HashMap<String, String>();
					args.put(CmdGotoLabel.ARG_PROCID,
					        event.getParameter(ARG_PROCID));
					args.put(CmdGotoLabel.ARG_LABEL, target);
					result = CommandHelper.execute(CmdGotoLabel.ID, args);
				}
				else
				{
					HashMap<String, String> args = new HashMap<String, String>();
					args.put(CmdGotoLine.ARG_PROCID,
					        event.getParameter(ARG_PROCID));
					args.put(CmdGotoLine.ARG_LINENO, target);
					result = CommandHelper.execute(CmdGotoLine.ID, args);
				}
			}
		}
		return result;
	}

}
