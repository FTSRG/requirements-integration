package hu.bme.mit.requirements.dng.sandbox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.exception.JazzAuthErrorException;
import org.eclipse.lyo.client.exception.JazzAuthFailedException;
import org.eclipse.lyo.client.exception.ResourceNotFoundException;
import org.eclipse.lyo.client.exception.RootServicesException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.eclipse.lyo.client.oslc.resources.OslcQuery;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.eclipse.lyo.client.oslc.resources.Requirement;
import org.eclipse.lyo.client.oslc.resources.RequirementCollection;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;

import hu.bme.mit.requirements.manager.common.MeasureSingleton;
import hu.bme.mit.requirements.manager.common.MeasurementPhase;
import hu.bme.mit.requirements.manager.common.MyPair;
import net.oauth.OAuthException;

public class ArtifactProcessor {
	private Map<URI, Requirement> artifactUriToRequirementMapping;
	private Map<URI, RequirementCollection> artifactUriToRequirementCollectionMapping;
	
	private Properties properties;
	
	private String serverBaseAddress;
	private String baseUrl;
	private String baseUrlForAuthentication;
	
	public ArtifactProcessor() {
		artifactUriToRequirementMapping = new HashMap<URI, Requirement>();
		artifactUriToRequirementCollectionMapping = new HashMap<java.net.URI, RequirementCollection>();
		properties = new Properties();
		loadProperties();
	}
	
	private void loadProperties() {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream("resources/dngconfig.properties");
			properties.load(inputStream);
			serverBaseAddress = properties.getProperty("server_address") + ":" + properties.getProperty("server_port");
			baseUrl = serverBaseAddress + "/rm";
			baseUrlForAuthentication = serverBaseAddress + "/jts";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Map<URI, Requirement> getArtifactUriToRequirementMapping() {
		return artifactUriToRequirementMapping;
	}

	public Map<URI, RequirementCollection> getArtifactUriToRequirementCollectionMapping() {
		return artifactUriToRequirementCollectionMapping;
	}
	
	public Requirement getRequirementFromDNG(URI uri) {
		JazzRootServicesHelper helper;
		JazzFormAuthClient client;
		Requirement requirement = null;
		try {
			helper = new JazzRootServicesHelper(baseUrl, OSLCConstants.OSLC_RM_V2 );
			client = helper.initFormClient(properties.getProperty("username"),
											properties.getProperty("userpassword"), baseUrlForAuthentication);
			
			if (client.login() == HttpStatus.SC_OK) {
				ClientResponse clientResponse = client.getResource(uri.toString(), OslcMediaType.APPLICATION_RDF_XML); 
				requirement = clientResponse.getEntity(Requirement.class);
			}
		} catch (RootServicesException e) {
			e.printStackTrace();
		} catch (JazzAuthFailedException e) {
			e.printStackTrace();
		} catch (JazzAuthErrorException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return requirement;
	}
	
	public RequirementCollection getRequirementCollectionFromDNG(URI uri) {
		JazzRootServicesHelper helper;
		JazzFormAuthClient client;
		RequirementCollection requirementCollection = null;
		try {
			helper = new JazzRootServicesHelper(baseUrl, OSLCConstants.OSLC_RM_V2);
			client = helper.initFormClient(properties.getProperty("username"), properties.getProperty("userpassword"), baseUrlForAuthentication);
			
			if (client.login() == HttpStatus.SC_OK) {
				ClientResponse clientResponse = client.getResource(uri.toString(), OslcMediaType.APPLICATION_RDF_XML); 
				requirementCollection = clientResponse.getEntity(RequirementCollection.class);
			}
		} catch (RootServicesException e) {
			e.printStackTrace();
		} catch (JazzAuthFailedException e) {
			e.printStackTrace();
		} catch (JazzAuthErrorException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return requirementCollection;
	}

	public void test(String[] projectNames) {
		int noPaging = 0;
		try {
			// measurement1 start
			long start = System.nanoTime();
			
			JazzRootServicesHelper helper = new JazzRootServicesHelper(baseUrl, OSLCConstants.OSLC_RM_V2);
			JazzFormAuthClient client = helper.initFormClient(properties.getProperty("username"),
																properties.getProperty("userpassword"), baseUrlForAuthentication);
			// measurement1 end
			long elapsedTime = System.nanoTime() - start;
			MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_PHASE1, elapsedTime));
			
			// measurement2 start
			long start2 = System.nanoTime();
			
			for (String projectName : projectNames) {
				if (client.login() == HttpStatus.SC_OK) {
					// measurement5 start
					long start5 = System.nanoTime();
					
					String serviceProviderUrl = client.lookupServiceProviderUrl(helper.getCatalogUrl(), projectName);
					String queryCapability = client.lookupQueryCapability(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE);
					
					// remove configuration url
					StringBuffer strb = new StringBuffer(queryCapability);
					strb.delete(queryCapability.indexOf("&vvc"), strb.length());
					queryCapability = strb.toString();
					
					OslcQueryParameters queryParams = new OslcQueryParameters();
					queryParams = new OslcQueryParameters();
					queryParams.setPrefix("dcterms=<http://purl.org/dc/terms/>,oslc_rm=<http://open-services.net/ns/rm#>");
					queryParams.setSelect("dcterms:title,dcterms:identifier,dcterms:description");
					OslcQuery query = new OslcQuery(client, queryCapability, noPaging, queryParams);
					OslcQueryResult result = query.submit();
					
					// measurement5 end
					long elapsedTime5 = System.nanoTime() - start5;
					MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_OSLC_QUERY_RESULT, elapsedTime5));
					
					int resultsSize = result.getMembersUrls().length;
					
					// measurement6
					MeasureSingleton.getInstance().items += resultsSize;
					
					//System.out.println("\nResultSize: " + resultsSize + " ------------------------------\n");
					
					//System.out.println(projectName + ":");
					String[] membersUrls = result.getMembersUrls();			
					
					Iterator<Requirement> reqIt = result.getMembers(Requirement.class).iterator();
					int membersUrlsCounter = 0;
					while (reqIt.hasNext()) {
						Requirement req = reqIt.next();
						RequirementCollection reqCol = null;
						java.net.URI[] rdfTypes = req.getRdfTypes();
						for (java.net.URI rdfType : rdfTypes) {
							String type = rdfType.toString().split("#")[1];
							if (type.equalsIgnoreCase("requirementcollection")) {
								// measurement3 start
								long start3 = System.nanoTime();
								
								ClientResponse clientResponse = client.getResource(req.getAbout().toString(), OslcMediaType.APPLICATION_RDF_XML); 
								reqCol = clientResponse.getEntity(RequirementCollection.class);
								artifactUriToRequirementCollectionMapping.put(reqCol.getAbout(), reqCol);
								
								// measurement3 end
								long elapsedTime3 = System.nanoTime() - start3;
								MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_REQCOLL_QUERY, elapsedTime3));
								
								break;
							}
						}
						
						if (reqCol == null) {
							if (artifactUriToRequirementMapping.get(req.getAbout()) == null) {
								// BUG miatt le kell kerni megint...
								
								// measurement4 start
								long start4 = System.nanoTime();
								
								ClientResponse clientResponse = client.getResource(req.getAbout().toString(), OslcMediaType.APPLICATION_RDF_XML); 
								Requirement requirement = clientResponse.getEntity(Requirement.class);
								artifactUriToRequirementMapping.put(requirement.getAbout(), requirement);
								
								// measurement4 end
								long elapsedTime4 = System.nanoTime() - start4;
								MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_REQ_QUERY, elapsedTime4));
							}
						}
					}
				}
			}
			
			// measurement2 end
			long elapsedTime2 = System.nanoTime() - start2;
			MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_PHASE2, elapsedTime2));
			
		} catch (RootServicesException e) {
			e.printStackTrace();
		} catch (JazzAuthFailedException e) {
			e.printStackTrace();
		} catch (JazzAuthErrorException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			e.printStackTrace();
		}
	}
}
