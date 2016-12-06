package hu.bme.mit.requirements.polarion.sandbox;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import com.polarion.alm.ws.client.WebServiceFactory;
import com.polarion.alm.ws.client.projects.ProjectWebService;
import com.polarion.alm.ws.client.session.SessionWebService;
import com.polarion.alm.ws.client.tracker.TrackerWebService;
import com.polarion.alm.ws.client.types.projects.Project;
import com.polarion.alm.ws.client.types.tracker.LinkedWorkItem;
import com.polarion.alm.ws.client.types.tracker.Module;
import com.polarion.alm.ws.client.types.tracker.WorkItem;

import hu.bme.mit.requirements.manager.common.MeasureSingleton;
import hu.bme.mit.requirements.manager.common.MeasurementPhase;
import hu.bme.mit.requirements.manager.common.MyPair;

public class WorkItemProcessor {
	private WebServiceFactory webServiceFactory;
	private SessionWebService sessionWebService;
	private TrackerWebService trackerWebService;
	private ProjectWebService projectWebService;
	private Project project;
	private String userName;
	private String userPassword;
	
	private Map<String, WorkItem> workItemCache;
	
	public void init(String baseUrl, String username, String password) {
		try {
			// measurement start
			long start = System.nanoTime();
			
			webServiceFactory = new WebServiceFactory(baseUrl);
			sessionWebService = webServiceFactory.getSessionService();
			trackerWebService = webServiceFactory.getTrackerService();
			projectWebService = webServiceFactory.getProjectService();
			
			workItemCache = new HashMap<String, WorkItem>();
			
			userName = username;
			userPassword = password;
			
			// measurement end
			long elapsedTime = System.nanoTime() - start;
			MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_PHASE1_1, elapsedTime));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Gets all the work items from Polarion Requirements.
	 * @return
	 */
	public WorkItem[] getWorkItems2() {
		WorkItem[] workItems = null;
		
		try {
			sessionWebService.logIn(userName, userPassword);
			
			String[] fields = {"id", "title", "description", "linkedWorkItems", "linkedWorkItemsDerived", "type"};
			//String luceneQueryForAllTypesOfRequirements = "type:(electricalRequirement mechanicalRequirement requirement softwareRequirement systemRequirement)";
			String luceneQuerySortBy = "type:requirement";
			String luceneQueryAllTypeOfWorkItems = "HAS_VALUE:type";
			workItems = trackerWebService.queryWorkItems(luceneQueryAllTypeOfWorkItems, luceneQuerySortBy, fields);
			return workItems;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public WorkItem[] getWorkItems3(String[] projectIds) {
		WorkItem[] workItems = null;
				
				try {
					// measurement1 start
					long start = System.nanoTime();
					
					sessionWebService.logIn(userName, userPassword);
					
					// measurement1 end
					long elapsedTime = System.nanoTime() - start;
					MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_PHASE1_2, elapsedTime));
					
					// measurement1 start
					start = System.nanoTime();
					
					String[] fields = {"id", "title", "description", "linkedWorkItems", "linkedWorkItemsDerived", "type"};
					String luceneQuerySortBy = "type:requirement";
					
					// if projectIds is empty
					String projects = "HAS_VALUE:type";
					
					if (projectIds.length == 1) {
						projects = "project.id:" + projectIds[0];
					} else {
						StringBuffer sb = new StringBuffer("project.id:(");
						for (String projectId : projectIds) {
							sb.append(projectId + " ");
						}
						sb.append(")");
						
						projects = sb.toString();
					}
					String luceneQueryAllTypeOfWorkItems = projects;
					workItems = trackerWebService.queryWorkItems(luceneQueryAllTypeOfWorkItems, luceneQuerySortBy, fields);
					
					// measurement items
					MeasureSingleton.getInstance().items += workItems.length;
					
					// measurement2 end
					elapsedTime = System.nanoTime() - start;
					MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_PHASE2, elapsedTime));
					
					return workItems;
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				
				return null;
			}
	
	public WorkItem[] getWorkItems() {
		try {
			WorkItem[] workItems = null;
			
			sessionWebService.logIn(userName, userPassword);
			String projectId = "library";

			project = projectWebService.getProject(projectId);
			
			if (project.isUnresolvable()) {
				System.err.println("Project not found:" + projectId);
			}
			
			String[] documentSpaces = trackerWebService.getDocumentSpaces(projectId);
			for (String docSpace : documentSpaces) {
				//System.out.println("docSpace: " + docSpace);
				
				Module[] modules = trackerWebService.getModules(projectId, docSpace);
				String moduleUri = modules[0].getUri();
				//System.out.println("Module uri: " + moduleUri);
				
				String[] fields = {"id", "title", "description", "linkedWorkItems", "linkedWorkItemsDerived", "type"};
				/*examples: String luceneQuery = "type:requirement AND title:function";
				String luceneQuery2 = "id:DP-292";
				String luceneQuery3 = "id:DP-345";*/
				//String luceneQueryAllTypeOfWorkItems = "HAS_VALUE:type";
				String luceneQueryForAllTypesOfRequirements = "type:(electricalRequirement mechanicalRequirement requirement softwareRequirement systemRequirement)";
				String luceneQuerySortBy = "type:requirement";
				workItems = trackerWebService.queryWorkItems(luceneQueryForAllTypesOfRequirements, luceneQuerySortBy, fields);
			}
			
			return workItems;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public LinkedWorkItem[] getBackLinkedWorkitems(String workItemURI) {
		try {
			sessionWebService.logIn(userName, userPassword);
			return trackerWebService.getBackLinkedWorkitems(workItemURI);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public WorkItem getWorkItemByUri(String workItemURI) {
		WorkItem cachedWorkItem = workItemCache.get(workItemURI);
		if (cachedWorkItem != null) {
			return cachedWorkItem;
		} else {
			try {
				// measurement start
				long start = System.nanoTime();
				
				sessionWebService.logIn(userName, userPassword);
				WorkItem workItem = trackerWebService.getWorkItemByUri(workItemURI);
				workItemCache.put(workItemURI, workItem);
				
				// measurement items
				MeasureSingleton.getInstance().items += 1;
				
				// measurement end
				long elapsedTime = System.nanoTime() - start;
				MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_QUERY_WORKITEM_BY_URI, elapsedTime));
				return workItem;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public WorkItem getWorkItemByUriWithoutCache(String workItemURI) {
		try {
			sessionWebService.logIn(userName, userPassword);
			return trackerWebService.getWorkItemByUri(workItemURI);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public WorkItem getWorkItemById(String projectId, String workitemId) {
		try {
			sessionWebService.logIn(userName, userPassword);
			return trackerWebService.getWorkItemById(projectId, workitemId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
