package hu.bme.mit.requirements.polarion.sandbox;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.util.UMLUtil.StereotypeApplicationHelper;
import org.junit.Test;

import com.polarion.alm.ws.client.types.tracker.LinkedWorkItem;
import com.polarion.alm.ws.client.types.tracker.WorkItem;

import hu.bme.mit.requirements.manager.common.MeasureSingleton;
import hu.bme.mit.requirements.manager.common.MeasurementPhase;
import hu.bme.mit.requirements.manager.common.MyPair;
import hu.bme.mit.requirements.manager.common.RequirementManager;

import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;
import org.eclipse.papyrus.sysml14.requirements.Requirement;
import org.eclipse.papyrus.sysml14.requirements.RequirementsPackage;

public class PolarionAdapter extends RequirementManager {
	private String baseUrl;

	private Map<WorkItem, Class> workItemToClassMapping;
	private Map<String, WorkItem> workItemUriToWorkItemMapping;
	private Map<WorkItem, Class> laterQueriedWorkItemToClassMapping;
	private Map<String, WorkItem> laterQueriedWorkItemUriToWorkItemMapping;
	
	private Properties properties;

	public void init() {
		workItemToClassMapping = new HashMap<WorkItem, Class>();
		workItemUriToWorkItemMapping = new HashMap<String, WorkItem>();
		laterQueriedWorkItemToClassMapping = new HashMap<WorkItem, Class>();
		laterQueriedWorkItemUriToWorkItemMapping = new HashMap<String, WorkItem>();
		
		properties = new Properties();
		loadProperties();
	}
	
	/**
	 * Loads the properties from config.properties.
	 */
	private void loadProperties() {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream("resources/polarionreqconfig.properties");
			properties.load(inputStream);
			baseUrl = properties.getProperty("server_address") + "/polarion/ws/services/";
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
	/**
	 * Processes all the work items from Polarion Requirements and saves it to a papyrus specific sysml model.
	 */
	@Test
	public void processWorkItemsFromPolarionRequirements(String modelName, String[] projectIds) {
		String outputUmlFileName = modelName;
		umlFileName = outputUmlFileName;
		init();
		initModel(outputUmlFileName);

		WorkItemProcessor workItemProcessor = new WorkItemProcessor();
		workItemProcessor.init(baseUrl, properties.getProperty("username"), properties.getProperty("userpassword"));
		WorkItem[] workItems = workItemProcessor.getWorkItems3(projectIds);

		if (workItems == null) {
			System.err.println("Workitems null.");
			fail();
		}
		
		// measurement1 start
		long start = System.nanoTime();
		
		convertWorkItemsToSysMLSpecificObjects(workItems, workItemToClassMapping, workItemUriToWorkItemMapping);

		for (Entry<WorkItem, Class> entry : workItemToClassMapping.entrySet()) {
			// iterates through the work items' linked work items
			LinkedWorkItem[] linkedItems = entry.getKey().getLinkedWorkItems();
			if (linkedItems == null) {
				System.out.println("No linked items found.");
				continue;
			}

			for (LinkedWorkItem linkedItem : linkedItems) {
				// check if the linkedItem is in the store
				WorkItem linkedItemAsWorkItem = workItemUriToWorkItemMapping.get(linkedItem.getWorkItemURI());
				// check if the linkedItem is in the later queried store
				if (linkedItemAsWorkItem == null) {
					linkedItemAsWorkItem = laterQueriedWorkItemUriToWorkItemMapping.get(linkedItem.getWorkItemURI());
				}

				Class parent = null;
				Class child = entry.getValue();

				// if the linked work item already exists in the store, then create the relationship
				if (linkedItemAsWorkItem != null) {
					parent = workItemToClassMapping.get(linkedItemAsWorkItem);
				} else {
					// linked item does not exist in the store, it is necessary to get it and create it
					linkedItemAsWorkItem = workItemProcessor.getWorkItemByUri(linkedItem.getWorkItemURI());
					WorkItem[] laterQueriedWorkItem = new WorkItem[1];
					laterQueriedWorkItem[0] = linkedItemAsWorkItem;

					convertWorkItemsToSysMLSpecificObjects(laterQueriedWorkItem, laterQueriedWorkItemToClassMapping,
							laterQueriedWorkItemUriToWorkItemMapping);

					parent = laterQueriedWorkItemToClassMapping.get(linkedItemAsWorkItem);
				}

				// set the relationship
				Relationship relationship = new Relationship();
				if (linkedItem.getRole().getId().equals("parent")) {
					relationship.setContainment2(parent, child);
				} else {
					Abstraction abstraction = relationship.getAbstraction(child, parent);
					relationship.addAbstractionToModelAndApplyStereotype(model, abstraction,
							linkedItem.getRole().getId());
				}
			}
		}
		
		// measurement1 end
		long elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_PHASE3, elapsedTime));
		
		// measurement2 start
		start = System.nanoTime();
		
		save(resourceSet, model, URI.createURI(outputUmlFileName));
		
		// measurement2 end
		elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_PHASE4, elapsedTime));
	}
	
	/**
	 * Converts work items to SysML specific objects. (e.g.: Class, Requirement, Block)
	 * @param workItems
	 * @param workItemToClassMapping_
	 * @param workItemUriToWorkItemMapping_
	 */
	public void convertWorkItemsToSysMLSpecificObjects(WorkItem[] workItems,
			Map<WorkItem, Class> workItemToClassMapping_, Map<String, WorkItem> workItemUriToWorkItemMapping_) {
		// converts work items to sysml specific objects and stores it
		for (WorkItem workItem : workItems) {
			if (workItem.getType().getId().toLowerCase().contains("requirement")) {
				String className = workItem.getTitle();
				Class requirementClass = model.createOwnedClass(className, false);
				Requirement requirement = (Requirement) StereotypeApplicationHelper.getInstance(null)
						.applyStereotype(requirementClass, RequirementsPackage.eINSTANCE.getRequirement());
				requirement.setId(workItem.getId() + " ## " + workItem.getUri());
				String description = "";
				
				if (workItem.getDescription() != null) {
					description = workItem.getDescription().getContent();
				}
				
				requirement.setText(description);

				workItemToClassMapping_.put(workItem, requirementClass);
				workItemUriToWorkItemMapping_.put(workItem.getUri(), workItem);
			} else {
				Class blockClass = model.createOwnedClass(workItem.getId() + " ## " + workItem.getTitle() + " ## " + workItem.getUri(), false);
				StereotypeApplicationHelper.getInstance(null).applyStereotype(blockClass,
						BlocksPackage.eINSTANCE.getBlock());

				workItemToClassMapping_.put(workItem, blockClass);
				workItemUriToWorkItemMapping_.put(workItem.getUri(), workItem);
			}
		}
	}
	
	public void convertWorkItemsToSysMLSpecificObjects2(WorkItem[] workItems,
			Map<String, Class> workItemUriToClassMapping_, Map<String, WorkItem> workItemUriToWorkItemMapping_) {
		// converts work items to sysml specific objects and stores it
		for (WorkItem workItem : workItems) {
			if (workItem.getType().getId().toLowerCase().contains("requirement")) {
				String className = workItem.getTitle();
				Class requirementClass = model.createOwnedClass(className, false);
				Requirement requirement = (Requirement) StereotypeApplicationHelper.getInstance(null)
						.applyStereotype(requirementClass, RequirementsPackage.eINSTANCE.getRequirement());
				requirement.setId(workItem.getId() + " ## " + workItem.getUri());
				String description = "";
				
				if (workItem.getDescription() != null) {
					description = workItem.getDescription().getContent();
				}
				
				requirement.setText(description);

				workItemUriToClassMapping_.put(workItem.getUri(), requirementClass);
				workItemUriToWorkItemMapping_.put(workItem.getUri(), workItem);
			} else {
				Class blockClass = model.createOwnedClass(workItem.getId() + " ## " + workItem.getTitle() + " ## " + workItem.getUri(), false);
				StereotypeApplicationHelper.getInstance(null).applyStereotype(blockClass,
						BlocksPackage.eINSTANCE.getBlock());

				workItemUriToClassMapping_.put(workItem.getUri(), blockClass);
				workItemUriToWorkItemMapping_.put(workItem.getUri(), workItem);
			}
		}
	}

	public void testIncrementalInMemory(ResourceSet resourceSet, Model model, URI uriForModel) {
		WorkItemProcessor workItemProcessor = new WorkItemProcessor();
		workItemProcessor.init(baseUrl, properties.getProperty("username"), properties.getProperty("userpassword"));
		
		WaitingToWriteThread waitingToWriteThread = new WaitingToWriteThread(this);
		Thread thread = new Thread(waitingToWriteThread);
		
		thread.setDaemon(false);
		thread.start();
		
		// create one map from the other two
		Map<String, WorkItem> globalWorkItemUriToWorkItemMapping = new HashMap<String, WorkItem>();
		
		Map<String, Class> globalWorkItemUriToClassMapping = new HashMap<String, Class>();
		
		for (Entry<WorkItem, Class> entry: workItemToClassMapping.entrySet()) {
			globalWorkItemUriToClassMapping.put(entry.getKey().getUri(), entry.getValue());
		}
		
		for (Entry<WorkItem, Class> entry : laterQueriedWorkItemToClassMapping.entrySet()) {
			globalWorkItemUriToClassMapping.put(entry.getKey().getUri(), entry.getValue());
		}
		
		globalWorkItemUriToWorkItemMapping.putAll(workItemUriToWorkItemMapping);
		globalWorkItemUriToWorkItemMapping.putAll(laterQueriedWorkItemUriToWorkItemMapping);
		
		BlockingQueue<String[]> tasks = waitingToWriteThread.getTasks();

		while (true) {
			String[] take = null;
			try {
				take = tasks.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (take[0].equals("stop")) break;
			
			// measurement1 start
			long start = System.nanoTime();
			
			String uri = take[1];
			WorkItem workItemInMap = globalWorkItemUriToWorkItemMapping.get(uri);
			
			if (take[0].equals("save")) {
				// if work item does not exist in the model, fetch it, create it, and set it's relationships
				if (workItemInMap == null) {
					// measurement start
					long startWithoutCache = System.nanoTime();
					
					// measurement items
					MeasureSingleton.getInstance().items += 1;
					
					// fetch it
					WorkItem workItemByUri = workItemProcessor.getWorkItemByUriWithoutCache(uri);
					
					// measurement end
					long elapsedTimeWithoutCache = System.nanoTime() - startWithoutCache;
					MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_QUERY_WORKITEM_BY_URI_WITHOUT_CACHE, elapsedTimeWithoutCache));
					
					WorkItem[] workItemByUriArray = new WorkItem[1];
					workItemByUriArray[0] = workItemByUri;
					// create it
					convertWorkItemsToSysMLSpecificObjects2(workItemByUriArray, globalWorkItemUriToClassMapping/*globalWorkItemToClassMapping*/, globalWorkItemUriToWorkItemMapping);
					// set relationships
					LinkedWorkItem[] linkedItems = workItemByUri.getLinkedWorkItems();
					LinkedWorkItem[] linkedItemsDerived = workItemByUri.getLinkedWorkItemsDerived();
					
					Class child = globalWorkItemUriToClassMapping.get(uri);
					// get it from model
					Class parent = null;
					
					if (linkedItems != null) {
						for (LinkedWorkItem linkedItem : linkedItems) {
							// get it from in memory model
							WorkItem linkedWorkItem = globalWorkItemUriToWorkItemMapping.get(linkedItem.getWorkItemURI());
							//parent = globalWorkItemToClassMapping.get(linkedWorkItem);
							parent = globalWorkItemUriToClassMapping.get(linkedWorkItem.getUri());
							
							// set the relationship
							Relationship relationship = new Relationship();
							if (linkedItem.getRole().getId().equals("parent")) {
								relationship.setContainment2(parent, child);
							} else {
								Abstraction abstraction = relationship.getAbstraction(child, parent);
								relationship.addAbstractionToModelAndApplyStereotype(model, abstraction,
										linkedItem.getRole().getId());
							}
						}
					}
					
					if (linkedItemsDerived != null) {
						// roles inverted
						parent = globalWorkItemUriToClassMapping.get(uri);
						for (LinkedWorkItem linkedItemDerived : linkedItemsDerived) {
							// get it from model
							WorkItem linkedWorkItem = globalWorkItemUriToWorkItemMapping.get(linkedItemDerived.getWorkItemURI());
							child = globalWorkItemUriToClassMapping.get(linkedWorkItem.getUri());
							
							// set the relationship
							Relationship relationship = new Relationship();
							if (linkedItemDerived.getRole().getId().equals("parent")) {
								relationship.setContainment2(parent, child);
							} else {
								Abstraction abstraction = relationship.getAbstraction(child, parent);
								relationship.addAbstractionToModelAndApplyStereotype(model, abstraction,
										linkedItemDerived.getRole().getId());
							}
						}
					}
				} else {
					// measurement start
					long startWithoutCache2 = System.nanoTime();
					
					// measurement items
					MeasureSingleton.getInstance().items += 1;
					
					// if work item exists in the model, then compare it's attributes and relationships, then update if necessary
					WorkItem changedWorkItemFromPolarion = workItemProcessor.getWorkItemByUriWithoutCache(uri);
					
					// measurement end
					long elapsedTimeWithoutCache2 = System.nanoTime() - startWithoutCache2;
					MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_QUERY_WORKITEM_BY_URI_WITHOUT_CACHE, elapsedTimeWithoutCache2));
					
					Class existingClass = globalWorkItemUriToClassMapping.get(workItemInMap.getUri());

					Requirement req = null;
					Block block = null;
					if (workItemInMap.getType().getId().toLowerCase().contains("requirement")) {
						req = (Requirement) existingClass.getStereotypeApplications().get(0);
					} else {
						block = (Block) existingClass.getStereotypeApplications().get(0);
					}
					// compare attributes
					if (workItemInMap.getId().equals(changedWorkItemFromPolarion.getId()) == false) {
						workItemInMap.setId(changedWorkItemFromPolarion.getId());
						if (req != null) {
							req.setId(changedWorkItemFromPolarion.getId() + " ## " + changedWorkItemFromPolarion.getUri());
						} else {
							// new id, old title
							existingClass.setName(changedWorkItemFromPolarion.getId() + " ## " + workItemInMap.getTitle()  + " ## " + changedWorkItemFromPolarion.getUri());
						}

					}
					
					if (req != null) {
						if (changedWorkItemFromPolarion.getDescription() != null) {
							if (workItemInMap.getDescription() == null) {
								workItemInMap.setDescription(changedWorkItemFromPolarion.getDescription());
								req.setText(changedWorkItemFromPolarion.getDescription().getContent());
							} else {
								if (workItemInMap.getDescription().getContent().equals(changedWorkItemFromPolarion.getDescription().getContent()) == false) {
									workItemInMap.setDescription(changedWorkItemFromPolarion.getDescription());
									req.setText(changedWorkItemFromPolarion.getDescription().getContent());
								}
							}
						}
					}
					
					if (workItemInMap.getTitle().equals(changedWorkItemFromPolarion.getTitle()) == false) {
						workItemInMap.setTitle(changedWorkItemFromPolarion.getTitle());
						if (req != null) {
							existingClass.setName(changedWorkItemFromPolarion.getTitle());
						} else {
							existingClass.setName(workItemInMap.getId() + " ## " + changedWorkItemFromPolarion.getTitle()  + " ## " + changedWorkItemFromPolarion.getUri());
						}
					}
					
					// compare relationships
					LinkedWorkItem[] linkedWorkItems = changedWorkItemFromPolarion.getLinkedWorkItems();
					LinkedWorkItem[] linkedWorkItemsDerived = changedWorkItemFromPolarion.getLinkedWorkItemsDerived();
					
					Class child = globalWorkItemUriToClassMapping.get(uri);
					// get it from model
					Class parent = null;

					for (org.eclipse.uml2.uml.Relationship relationships : child.getRelationships()) {
						EcoreUtil.delete(relationships, true);
					}
					
					// create all relationships
					if (linkedWorkItems != null) {
						for (LinkedWorkItem linkedItem : linkedWorkItems) {
							// get it from in memory model
							WorkItem linkedWorkItem = globalWorkItemUriToWorkItemMapping.get(linkedItem.getWorkItemURI());
							parent = globalWorkItemUriToClassMapping.get(linkedWorkItem.getUri());
							
							// set the relationship
							Relationship relationship = new Relationship();
							if (linkedItem.getRole().getId().equals("parent")) {
								relationship.setContainment2(parent, child);
							} else {
								Abstraction abstraction = relationship.getAbstraction(child, parent);
								relationship.addAbstractionToModelAndApplyStereotype(model, abstraction,
										linkedItem.getRole().getId());
							}
						}
					}
					
					if (linkedWorkItemsDerived != null) {
						// roles inverted
						parent = globalWorkItemUriToClassMapping.get(uri);
						
						for (LinkedWorkItem linkedItemDerived : linkedWorkItemsDerived) {
							// get it from model
							WorkItem linkedWorkItem = globalWorkItemUriToWorkItemMapping.get(linkedItemDerived.getWorkItemURI());
							//child = globalWorkItemToClassMapping.get(linkedWorkItem);
							child = globalWorkItemUriToClassMapping.get(linkedWorkItem.getUri());
							
							// set the relationship
							Relationship relationship = new Relationship();
							if (linkedItemDerived.getRole().getId().equals("parent")) {
								relationship.setContainment2(parent, child);
							} else {
								Abstraction abstraction = relationship.getAbstraction(child, parent);
								relationship.addAbstractionToModelAndApplyStereotype(model, abstraction,
										linkedItemDerived.getRole().getId());
							}
						}
					}
					
				}
			} else if (take[0].equals("delete")) {
				EList<EObject> appliedStereotypes = globalWorkItemUriToClassMapping.get(workItemInMap.getUri()).getStereotypeApplications();
				EList<org.eclipse.uml2.uml.Relationship> relationships = globalWorkItemUriToClassMapping.get(workItemInMap.getUri()).getRelationships();
				for (org.eclipse.uml2.uml.Relationship relationship : relationships) {
					EList<Stereotype> relationshipAppliedStereotypes = relationship.getAppliedStereotypes();
					for (Stereotype relationshipStereotype : relationshipAppliedStereotypes) {
						EcoreUtil.delete(relationshipStereotype);
					}
					EcoreUtil.delete(relationship);
				}
				for (EObject appliedStereotype: appliedStereotypes) {
					EcoreUtil.delete(appliedStereotype);
				}
				EcoreUtil.delete(globalWorkItemUriToClassMapping.get(workItemInMap.getUri()));
				globalWorkItemUriToClassMapping.remove(workItemInMap.getUri());
				globalWorkItemUriToWorkItemMapping.remove(uri);
			}
			
			// measurement1 end
			long elapsedTime = System.nanoTime() - start;
			MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_PHASE5_1, elapsedTime));
			
			// measurement2 start
			start = System.nanoTime();
			
			save(resourceSet, model, uriForModel);
			
			// measurement2 end
			elapsedTime = System.nanoTime() - start;
			MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.POLARION_PHASE5_2, elapsedTime));
		}
	}
	
	@Override
	public void createRequirementModel(String modelName, String[] projectIds) {
		processWorkItemsFromPolarionRequirements(modelName, projectIds);
	}

	@Override
	public void doIncrementalModelUpdate() {
		testIncrementalInMemory(resourceSet, model, URI.createURI(umlFileName));
	}
}
