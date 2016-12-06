/*
 *  Copyright 2012 ADVA AG Optical Networking. All rights reserved.
 *
 *  Owner: aandrianov
 *
 *  $Id$
 */

package com.adva.polarion.actionhook.delete.assignee;


import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import com.adva.polarion.actionhook.IActionHook;
import com.polarion.alm.projects.model.IUser;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.logging.Log4jLogger;
import com.polarion.platform.persistence.IDataService;
import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.model.IPObjectList;

/**
 * WorkItemActionInterceptor delete hook for control that only assignee user can delete WI
 * If Wi is unassigned then it can be removed or not dependent on value property ENABLE_DELETE_UNASSIGNEE 
 */
public class OnlyAssigneeCanDeleteHook implements IActionHook
{
	public static boolean DEBUG = false;

	public static final String ERROR_MESSAGE = "Only assignee user can delete WI!";
	
	public static final String HOOK_ID = "OnlyAssigneeCanDeleteHook";
	public static final String HOOK_DESCRIPTION = "Control that only assignee user can delete WI";	

	private Log4jLogger _logger = new Log4jLogger ("com.adva.polarion.actionhook.OnlyAssigneeCanDeleteHook");

	private boolean enableDeleteUnassignee = true;

	private boolean enabled = true;
	
	public OnlyAssigneeCanDeleteHook()
	{
		super ();
		loadProperties();
	}

	@Override
	public void init (IDataService dataService)
	{
	}
	
	@Override
	public String processAction (IWorkItem workItem)
	{
		String returnMessage = null;
		// ------------- modified part
		WaitingToWriteThread waitingToWriteThread = new WaitingToWriteThread(workItem.getUri().toString());
		Thread thread = new Thread(waitingToWriteThread);
		thread.start();
		// -------------

		return returnMessage;
	}

	private final void debug (String message)
	{
		_logger.log (Log4jLogger.DEBUG, message);
		if (DEBUG) System.out.println ("OACD: " + message);
	}

	@Override
	public ACTION_TYPE getAction ()
	{
		return ACTION_TYPE.DELETE;
	}

	@Override
	public String getName ()
	{
		return HOOK_ID;
	}

	@Override
	public String getDescription ()
	{
		return HOOK_DESCRIPTION;
	}

	@Override
	public boolean isEnabled ()
	{
		return enabled ;
	}

	@Override
	public void setEnabled (boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * Load properties
	 */
	private void loadProperties()
	{
    InputStream in = this.getClass().getClassLoader().getResourceAsStream (HOOK_ID + ".properties");
    if (in != null)
  		try
  		{
  			Properties p = new Properties();
  			p.load (in);
  			if (p.containsKey ("ENABLE_DELETE_UNASSIGNEE"))
  				enableDeleteUnassignee = Boolean.parseBoolean (p.getProperty ("ENABLE_DELETE_UNASSIGNEE"));
  		}
  		catch (IOException e)
  		{
  			debug ("Cannot load proerties: " + e);
  			e.printStackTrace();
  		}
	}

}
