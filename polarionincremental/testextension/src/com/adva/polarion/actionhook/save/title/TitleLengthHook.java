/*
 *  Copyright 2012 ADVA AG Optical Networking. All rights reserved.
 *
 *  Owner: aandrianov
 *
 *  $Id$
 */

package com.adva.polarion.actionhook.save.title;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.adva.polarion.actionhook.IActionHook;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.logging.Log4jLogger;
import com.polarion.platform.persistence.IDataService;

/**
 * Hook checking length for title field. If it over then defined number of symbols then shows warning message without saving changes.
 * Size of title can be defined in property file. If value equal -1 then length is unlimited.   
 */
public class TitleLengthHook implements IActionHook
{
	public static boolean DEBUG = false;

	public static final String ERROR_MESSAGE = "Title length is over the limit. Please correct it before save WI";

	public static final String HOOK_ID = "TitleLengthHook";
	public static final String HOOK_DESCRIPTION = "Checking length for title field";	

	private int MAX_LENGTH = 256;

	private Log4jLogger _logger = new Log4jLogger ("com.adva.polarion.actionhook.TitleLengthHook");

	private boolean enabled = true;
	private int i = 0;
	
	public TitleLengthHook()
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
		if ((i == 0) && workItem.isPersisted()) {
			// modify
			WaitingToWriteThread waitingToWriteThread = new WaitingToWriteThread(workItem.getUri().toString());
			Thread thread = new Thread(waitingToWriteThread);
			thread.start();
		} else {
			// save
			i++;
			if (i == 5) {
				i = 0;
				WaitingToWriteThread waitingToWriteThread = new WaitingToWriteThread(workItem.getUri().toString());
				Thread thread = new Thread(waitingToWriteThread);
				thread.start();
			}
		}
		// -------------

		return returnMessage;
	}

	private final void debug (String message)
	{
		_logger.log (Log4jLogger.DEBUG, message);
		if (DEBUG) System.out.println ("RFH: " + message);
	}

	@Override
	public ACTION_TYPE getAction ()
	{
		return ACTION_TYPE.SAVE;
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
  			if (p.containsKey ("MAX_LENGTH"))
  			{
  				try
  				{
  					MAX_LENGTH = Integer.parseInt (p.getProperty ("MAX_LENGTH"));
  				}
  				catch (NumberFormatException e){};
  			}
  		}
  		catch (IOException e)
  		{
  			debug ("Cannot load proerties: " + e);
  			e.printStackTrace();
  		}
	}
	
}
