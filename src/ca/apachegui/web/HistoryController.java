package ca.apachegui.web;

import java.io.IOException;
import java.sql.Timestamp;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletContextAware;

import ca.apachegui.db.LogDataDao;
import ca.apachegui.virtualhosts.VirtualHost;


@RestController
@RequestMapping("/web/History")
public class HistoryController implements ServletContextAware {
	private static Logger log = Logger.getLogger(HistoryController.class);
    
	private ServletContext context;
	
    @RequestMapping(value="/Current",method=RequestMethod.GET,produces="application/json;charset=UTF-8")
	public String getHistory() throws IOException, InterruptedException {
		int numberEntries=LogDataDao.getInstance().getNumberOfEntries();
		Timestamp newestTimeStamp=LogDataDao.getInstance().getNewestTime();
		String newestTime = (newestTimeStamp==null)?"":newestTimeStamp.toString();
		Timestamp oldestTimeStamp=LogDataDao.getInstance().getOldestTime();
		String oldestTime = (oldestTimeStamp==null)?"":oldestTimeStamp.toString();
		
		JSONObject result = new JSONObject();
		result.put("identifier", "id");
		result.put("label", "name");
		
		JSONArray items = new JSONArray();
		
		JSONObject entries = new JSONObject();
		entries.put("id", Integer.toString(numberEntries));
		entries.put("numHistory", Integer.toString(numberEntries));
		entries.put("newHistory", newestTime);
		entries.put("oldHistory", oldestTime);
		
		items.put(entries);
		result.put("items", items);
		
		return result.toString();
	}
	
	@RequestMapping(method=RequestMethod.GET,params="option=getEnabled",produces="application/json;charset=UTF-8")
	public String getEnabled() throws Exception {
		
		boolean globalEnable = ca.apachegui.history.History.getGlobalEnable();
		
		JSONObject result = new JSONObject();
		
		VirtualHost enabledVirtualHosts[] = ca.apachegui.history.History.getEnabledHosts();
		
		JSONArray allEnabled = new JSONArray();
		for(VirtualHost virtualHost : enabledVirtualHosts) {
			allEnabled.put(new JSONObject(virtualHost.toJSON()));
		}
		result.put("enabled", allEnabled);
		
		JSONArray globalHosts = new JSONArray();
		if(globalEnable) {
			VirtualHost globalVirtualHosts[] = ca.apachegui.history.History.getGlobalHosts();
			
			for(VirtualHost virtualHost : globalVirtualHosts) {
				globalHosts.put(new JSONObject(virtualHost.toJSON()));
			}
		}
		result.put("global", globalHosts);
		result.put("globalEnable", globalEnable);
		
		return result.toString();
	}
	
	@RequestMapping(method=RequestMethod.GET,params="option=getDisabled",produces="application/json;charset=UTF-8")
	public String getDisabled() throws Exception {
		
		boolean globalEnable = ca.apachegui.history.History.getGlobalEnable();
		
		JSONObject result = new JSONObject();
		
		VirtualHost disabledVirtualHosts[] = ca.apachegui.history.History.getDisabledHosts();
		JSONArray allDisabled = new JSONArray();
		for(VirtualHost virtualHost : disabledVirtualHosts) {
			allDisabled.put(new JSONObject(virtualHost.toJSON()));
		}
		result.put("disabled", allDisabled);
		
		JSONArray globalHosts = new JSONArray();
		if(!globalEnable) {
			VirtualHost globalVirtualHosts[] = ca.apachegui.history.History.getGlobalHosts();

			for(VirtualHost virtualHost : globalVirtualHosts) {
				globalHosts.put(new JSONObject(virtualHost.toJSON()));
			}
		}
		result.put("global", globalHosts);
		result.put("globalEnable", globalEnable);
		
		return result.toString();
	}
	
	@RequestMapping(method=RequestMethod.POST,params="option=updateGlobal",produces="application/json;charset=UTF-8")
	public String updateGlobal(@RequestParam(value="type") String type) throws Exception {
		
		if(type.equals("enable")) {
			ca.apachegui.history.History.globalEnable(context);
		}
		
		if(type.equals("disable")) {
			ca.apachegui.history.History.globalDisable();
		}
		
		if(ca.apachegui.server.Control.isServerRunning())
		{	
			String error="";
			try
			{
				error=ca.apachegui.server.Control.restartServer();
				if(!ca.apachegui.server.Control.isServerRunning())
				{
					throw new Exception("The server could not restart");
				}
			}
			catch(Exception e)
			{
				log.error(e.getMessage(), e);
				ca.apachegui.history.History.globalEnable(context);
				if(type.equals("enable")) {
					ca.apachegui.history.History.globalDisable();
				}
				
				if(type.equals("disable")) {
					ca.apachegui.history.History.globalEnable(context);
				}
				
				throw new Exception("There was an error while trying to restart the server, the changes were reverted: " + error + " " + e.getMessage());
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("result", "success");
				
		return result.toString();
	}
	
	/** 
	{
	    "option": "enable",
	    "hosts": [{
	        "NetworkInfo": [{
	            "port": 80,
	            "address": "*"
	        }],
	        "DocumentRoot": "/var/www/html",
	        "file": "/etc/apache2/sites-enabled/000-default.conf",
	        "ServerName": "test.local"
	    }, {
	        "NetworkInfo": [{
	            "port": 80,
	            "address": "*"
	        }],
	        "DocumentRoot": "/var/www/html",
	        "file": "/etc/apache2/sites-enabled/000-default.conf",
	        "ServerName": "test.local2"
	    }]
	}
	**/
	
	@RequestMapping(value="/update",method=RequestMethod.POST,produces="application/json;charset=UTF-8") 
	public String updateNonGlobal(@RequestBody String jsonString) throws Exception {
		
		JSONObject request = new JSONObject(jsonString);
		String option = request.getString("option");
		if(option.equals("enable")) {
			//TODO check which hosts we need to enable then enable
		} 
		if(option.equals("disable")) {
			//TODO check which hosts we need to disable then disable
		}
		
		JSONObject result = new JSONObject();
		result.put("result", "success");
		
		return result.toString();
		
	}
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.context = servletContext;
	}
}
