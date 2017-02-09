package com.aaronsapp.timers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.backendless.Backendless;
import com.backendless.logging.LogBuffer;
import com.backendless.logging.Logger;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.servercode.annotation.BackendlessTimer;
    
/**
* DailyTimer is a timer.
* It is executed according to the schedule defined in Backendless Console. The
* class becomes a timer by extending the TimerExtender class. The information
* about the timer, its name, schedule, expiration date/time is configured in
* the special annotation - BackendlessTimer. The annotation contains a JSON
* object which describes all properties of the timer.
*/
@BackendlessTimer("{'startDate':1486702800000,'frequency':{'schedule':'daily','repeat':{'every':1}},'timername':'daily'}")
public class DailyTimer extends com.backendless.servercode.extension.TimerExtender
{
    
  @Override
  public void execute( String appVersionId ) throws Exception
  {
	String where = "expire < ";
	Date cur = Calendar.getInstance().getTime();
	where += new SimpleDateFormat("MM/dd/yyyy").format(cur);
	LogBuffer.getInstance().setLogReportingPolicy( 1, 0 );
	Logger log = Logger.getLogger(DailyTimer.class);
//	log.debug(String.valueOf(cur));
	BackendlessDataQuery q = new BackendlessDataQuery();
	q.setWhereClause(where);
    ArrayList<Map> res = new ArrayList(Backendless.Persistence.of("offers").find(q).getCurrentPage());
//    log.debug(String.valueOf(res.size()));
    for(Map m : res) {
    	Backendless.Persistence.of( "offers" ).remove(m);
    	log.debug("deleted expired offer " + m.get("objectId").toString());
    }
  }
    
}
        