package com.itvision.jira.plugin.scheduler;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.scheduling.PluginJob;

public class JiraIssueScheduledTask implements PluginJob {

	@Override
	public void execute(Map<String, Object> jobDataMap) {
		IssueGeneratorImpl issueGenerator = (IssueGeneratorImpl) jobDataMap.get(IssueGeneratorImpl.KEY);
		Date lastIssue = (Date) jobDataMap.get(IssueGeneratorImpl.LAST_ISSUE);
		assert issueGenerator != null;
		Calendar calNow = Calendar.getInstance();
		Calendar compareCal = Calendar.getInstance();
		compareCal.setTime(lastIssue);
		compareCal.add(Calendar.MONTH, 1);
//		compareCal.add(Calendar.MINUTE, 3);
		System.out.println("executing task ...");
		System.out.println(String.format(
			"compare %1$te.%1$tm.20%1$ty %1$tH:%1$tM:%1$tS  to %2$te.%2$tm.20%2$ty %2$tH:%2$tM:%2$tS", calNow, compareCal));
		if (calNow.after(compareCal)) {
			// new issue
			System.out.println("Create the monthly Issue ....");
			// Projekt mit der ID "TP"
			ProjectManager projManager = ComponentAccessor.getProjectManager();
			Project project = projManager.getProjectObjByKey("TP");
			IssueService issueService = ComponentAccessor.getIssueService();
			IssueInputParameters inputParameters = issueService.newIssueInputParameters();
			inputParameters.setProjectId(project.getId());
			inputParameters.setIssueTypeId(getIssueTypeId(project.getIssueTypes(), "Task"));
			inputParameters.setDescription(String.format("Issue created at %1$te.%1$tm.20%1$ty %1$tH:%1$tM:%1$tS", calNow));
			ApplicationUser reportingUser = getUser(ComponentAccessor.getUserUtil().getAllApplicationUsers(), "admin");
			inputParameters.setAssigneeId(reportingUser.getKey());
			inputParameters.setReporterId(reportingUser.getKey());
			inputParameters.setSummary("A summery of the issue");
			ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(reportingUser); // statt einzuloggen ... grrrrrr
			CreateValidationResult result = issueService.validateCreate(reportingUser.getDirectoryUser(), inputParameters);
			if (result.isValid()) {
				IssueResult issueResult = issueService.create(reportingUser.getDirectoryUser(), result);
				if (!issueResult.isValid()) {
					Collection<String> errorMessages = issueResult.getErrorCollection().getErrorMessages();
					for (String errorMessage : errorMessages) {
						System.out.println(errorMessage);
					}
				}
			} else {
				System.out.println("ERROR: issue not valid");
				Collection<String> errorMessages = result.getErrorCollection().getErrorMessages();
				for (String error : errorMessages) {
					System.out.println(error);
				}
				Map<String, String> errors = result.getErrorCollection().getErrors();
				Set<String> errorKeys = errors.keySet();
				for (String errorKey : errorKeys) {
					System.out.println(errors.get(errorKey));
				}
			}
			
			jobDataMap.put(IssueGeneratorImpl.LAST_ISSUE, calNow.getTime());
			
		}
			
		issueGenerator.setLastRun(calNow.getTime());
	}

	private String getIssueTypeId(Collection<IssueType> typeList, String issueTypeName) {
		if (typeList != null) {
			for (IssueType iType : typeList) {
				if (iType.getName().equals(issueTypeName)) {
					System.out.println(String.format("found type id : %s", iType.getId()));
					return iType.getId();
				}
			}
			System.out.println("no type found");
			return null;
		} else {
			System.out.println("no type found");			
			return null;
		}
	}
	
	private ApplicationUser getUser(Collection<ApplicationUser> userList, String name) {
		if (userList != null) {
			for (ApplicationUser applUser : userList) {
				if (applUser.getName().equals(name)) {
					System.out.println(String.format("found user %s", applUser.getUsername()));
					return applUser;
				}
			}
			System.out.println("no user found");
			return null;
		}
		System.out.println("no user found");
		return null;
	}
}
