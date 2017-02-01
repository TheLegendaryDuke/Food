
package com.aaronsapp;

import com.aaronsapp.timers.DailyTimer;
import com.backendless.logging.LogBuffer;
import com.backendless.logging.Logger;
import com.backendless.servercode.IBackendlessBootstrap;


public class Bootstrap implements IBackendlessBootstrap
{
            
  @Override
  public void onStart()
  {
//	  LogBuffer.getInstance().setLogReportingPolicy( 1, 0 );
//		Logger log = Logger.getLogger(DailyTimer.class);
//		log.debug("test");
	  try {
		new DailyTimer().execute("v1");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		//e.printStackTrace();
	}
    // add your code here
  }
    
  @Override
  public void onStop()
  {
    // add your code here
  }
    
}
        