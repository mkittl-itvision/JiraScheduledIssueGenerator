package com.itvision.jira.plugin.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.scheduling.PluginScheduler;

public class IssueGeneratorImpl implements IssueGenerator, LifecycleAware {

	static final String KEY = IssueGeneratorImpl.class.getName() + "instance";
	static final String LAST_ISSUE = IssueGeneratorImpl.class.getName() + "last_issue"; 
	private static final String JOB = IssueGeneratorImpl.class.getName() + "job";
	private PluginScheduler pluginScheduler; // SAL
//	private final long everyDay = 1000 * 3600 *24;
	private final long everyDay = 1000 * 10; // 10 sek
	private Date lastRun = null;
	
	public IssueGeneratorImpl(PluginScheduler plScheduler) {
		pluginScheduler = plScheduler;
	}
	
	@Override
	public void onStart() {
		reschedule(everyDay);
	}

	@Override
	public void reschedule(long interval) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put(KEY, IssueGeneratorImpl.this);
		dataMap.put(LAST_ISSUE, new Date());
		pluginScheduler.scheduleJob(
			JOB, 
			JiraIssueScheduledTask.class, 
			dataMap, 
			new Date(), 
			interval);
		System.out.println("reschedule issue generator task");
	}

	void setLastRun(Date lastRun) {
		this.lastRun = lastRun;
	}
	
}
