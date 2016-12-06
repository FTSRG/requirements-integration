package hu.bme.mit.requirements.dng.sandbox;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;
import org.eclipse.papyrus.sysml14.requirements.Requirement;
import org.eclipse.papyrus.sysml14.requirements.RequirementsPackage;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.util.UMLUtil.StereotypeApplicationHelper;
import org.junit.Test;

import hu.bme.mit.requirements.manager.common.MeasureSingleton;
import hu.bme.mit.requirements.manager.common.MeasurementPhase;
import hu.bme.mit.requirements.manager.common.MyPair;
import hu.bme.mit.requirements.manager.common.RequirementManager;

public class DngAdapter extends RequirementManager {
	
	private Map<java.net.URI, org.eclipse.lyo.client.oslc.resources.Requirement> artifactUriToRequirementMapping;
	private Map<org.eclipse.lyo.client.oslc.resources.Requirement, Class> requirementToClassMapping;
	
	private Map<java.net.URI, org.eclipse.lyo.client.oslc.resources.RequirementCollection> artifactUriToRequirementCollectionMapping;
	private Map<org.eclipse.lyo.client.oslc.resources.RequirementCollection, Class> requirementCollectionToClassMapping;
	
	private Map<Link, Class> relationshipBlockClasses;
	private Map<java.net.URI, org.eclipse.lyo.client.oslc.resources.Requirement> laterQueriedArtifactUriToRequirementMapping;
	private Map<org.eclipse.lyo.client.oslc.resources.Requirement, Class> laterQueriedRequirementToClassMapping;
	
	public void init() {
		requirementToClassMapping = new HashMap<org.eclipse.lyo.client.oslc.resources.Requirement, Class>();
		requirementCollectionToClassMapping = new HashMap<org.eclipse.lyo.client.oslc.resources.RequirementCollection, Class>();
		
		relationshipBlockClasses = new HashMap<Link, Class>();
		laterQueriedArtifactUriToRequirementMapping = new HashMap<java.net.URI, org.eclipse.lyo.client.oslc.resources.Requirement>();
		laterQueriedRequirementToClassMapping = new HashMap<org.eclipse.lyo.client.oslc.resources.Requirement, Class>();
		
	}
	
	@Test
	public void test(String modelName, String[] projectNames) {
		String outputUmlFileName = modelName;
		umlFileName = outputUmlFileName;
		init();
		initModel(outputUmlFileName);
		
		ArtifactProcessor artifactProcessor = new ArtifactProcessor();
		artifactProcessor.test(projectNames);
		artifactUriToRequirementMapping = artifactProcessor.getArtifactUriToRequirementMapping();
		artifactUriToRequirementCollectionMapping = artifactProcessor.getArtifactUriToRequirementCollectionMapping();
		
		// measurement1 start
		long start = System.nanoTime();
		
		createRequirementClasses(artifactUriToRequirementMapping, requirementToClassMapping);
		createRequirementCollectionClasses(artifactUriToRequirementCollectionMapping, requirementCollectionToClassMapping);
		
		for (Entry<org.eclipse.lyo.client.oslc.resources.RequirementCollection, Class> entry : requirementCollectionToClassMapping.entrySet()) {
			java.net.URI[] uses = entry.getKey().getUses();
			for (java.net.URI link : uses) {
				Class parent = entry.getValue();
				Class child = null;
				org.eclipse.lyo.client.oslc.resources.Requirement requirement = artifactUriToRequirementMapping.get(link);
				// if the contained requirement does not exist in the storage/mapping, then get it from DNG and add it to mappings
				if (requirement == null) {
					requirement = artifactProcessor.getRequirementFromDNG(link);
					
					if (requirement == null) {
						System.out.println("requirement fetching error, uri: " + link.toString());
						continue;
					}
					
					addRequirementToMappings(requirement, artifactUriToRequirementMapping, requirementToClassMapping);
				}
				
				child = requirementToClassMapping.get(requirement);
				
				// set the relationship
				Relationship relationship = new Relationship();
				relationship.setContainment(parent, child);
			}
		}
		
		for (Entry<org.eclipse.lyo.client.oslc.resources.Requirement, Class> entry : requirementToClassMapping.entrySet()) {
			
			for (Link link : entry.getKey().getAffectedBy()) {
				// BLOCK, constraint miatt
				setRelationship(link, entry.getValue(), "affectedBy");
			}
			for (Link link : entry.getKey().getConstrainedBy()) {
				// Requirement, alapbol nincs link type
				Relationship relationship = new Relationship();
				Class parent = requirementToClassMapping.get(artifactUriToRequirementMapping.get(link.getValue()));
				if (parent == null) {
					parent = laterQueriedRequirementToClassMapping.get(laterQueriedArtifactUriToRequirementMapping.get(link.getValue()));
				}
				Class child = entry.getValue();
				
				org.eclipse.lyo.client.oslc.resources.Requirement requirement = null;
				// if this Block does not exist, then create it
				if (parent == null) {
					requirement = artifactProcessor.getRequirementFromDNG(link.getValue());
					
					if (requirement == null) {
						System.out.println("requirement fetching error, uri: " + link.toString());
						continue;
					}
					
					addRequirementToMappings(requirement, laterQueriedArtifactUriToRequirementMapping, laterQueriedRequirementToClassMapping);
					parent = requirementToClassMapping.get(requirement);
					if (parent == null) {
						parent = laterQueriedRequirementToClassMapping.get(requirement);
					}
				}
				
				Abstraction abstraction = relationship.getAbstraction(child, parent);
				relationship.addAbstractionToModelAndApplyStereotype(model, abstraction, "constrainedBy");
			}
			// containment (nestedClassifier)
			for (Link link : entry.getKey().getDecomposedBy()) {
				// BLOCK, nincs ilyen oslc-s link tipus alapbol
				Class parent = getBlockClassForRelationship(link);
				Class child = entry.getValue();
				
				// set the relationship
				Relationship relationship = new Relationship();
				relationship.setContainment(parent, child);
			}
			for (Link link : entry.getKey().getElaboratedBy()) {
				// BLOCK kiprobaltam, nem lehet req-req kozott
				setRelationship(link, entry.getValue(), "elaboratedBy");
			}
			for (Link link : entry.getKey().getImplementedBy()) {
				// BLOCK, constraint miatt
				setRelationship(link, entry.getValue(), "implementedBy");
			}
			
			for (Link link : entry.getKey().getSatisfiedBy()) {
				// Requirement lehet
				Relationship relationship = new Relationship();
				Class parent = requirementToClassMapping.get(artifactUriToRequirementMapping.get(link.getValue()));
				if (parent == null) {
					parent = laterQueriedRequirementToClassMapping.get(laterQueriedArtifactUriToRequirementMapping.get(link.getValue()));
				}
				Class child = entry.getValue();
				
				org.eclipse.lyo.client.oslc.resources.Requirement requirement = null;
				// if this Block does not exist, then create it
				if (parent == null) {
					requirement = artifactProcessor.getRequirementFromDNG(link.getValue());
					
					if (requirement == null) {
						System.out.println("requirement fetching error, uri: " + link.toString());
						continue;
					}
					
					addRequirementToMappings(requirement, laterQueriedArtifactUriToRequirementMapping, laterQueriedRequirementToClassMapping);
					parent = requirementToClassMapping.get(requirement);
					if (parent == null) {
						parent = laterQueriedRequirementToClassMapping.get(requirement);
					}
				}
				
				Abstraction abstraction = relationship.getAbstraction(child, parent);
				relationship.addAbstractionToModelAndApplyStereotype(model, abstraction, "satisfiedBy");
			}
			for (Link link : entry.getKey().getSpecifiedBy()) {
				// BLOCK, constraint miatt
				setRelationship(link, entry.getValue(), "specifiedBy");
			}
			for (Link link : entry.getKey().getTrackedBy()) {
				// BLOCK, constraint miatt
				setRelationship(link, entry.getValue(), "trackedBy");
			}
			for (Link link : entry.getKey().getValidatedBy()) {
				// BLOCK, constraint miatt
				setRelationship(link, entry.getValue(), "validatedBy");
			}
		}
		
		// measurement1 end
		long elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_PHASE3, elapsedTime));
		
		// measurement2 start
		long start2 = System.nanoTime();
		
		save(resourceSet, model, URI.createURI(outputUmlFileName));
		
		// measurement2 end
		long elapsedTime2 = System.nanoTime() - start2;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.DNG_PHASE4, elapsedTime2));
	}
	
	public void setRelationship(Link link, Class classForRequirement, String relationShipType) {
		Relationship relationship = new Relationship();
		Class parent = relationshipBlockClasses.get(link);
		Class child = classForRequirement;
		
		// if this Block does not exist, then create it
		if (parent == null) {
			parent = getBlockClassForRelationship(link);
		}
		
		Abstraction abstraction = relationship.getAbstraction(child, parent);
		relationship.addAbstractionToModelAndApplyStereotype(model, abstraction, relationShipType);
	}
	
	public void addRequirementToMappings(org.eclipse.lyo.client.oslc.resources.Requirement requirementToPutInToMap,
											Map<java.net.URI, org.eclipse.lyo.client.oslc.resources.Requirement> requirementMapping,
											Map<org.eclipse.lyo.client.oslc.resources.Requirement, Class> requirementClassMapping) {
		String className = requirementToPutInToMap.getTitle();
		Class requirementClass = model.createOwnedClass(className, false);
		Requirement requirement = (Requirement) StereotypeApplicationHelper.getInstance(null).applyStereotype(requirementClass, RequirementsPackage.eINSTANCE.getRequirement());
		requirement.setId(requirementToPutInToMap.getIdentifier() + " ## " + requirementToPutInToMap.getAbout().toString());
		requirement.setText(requirementToPutInToMap.getDescription());
		
		requirementMapping.put(requirementToPutInToMap.getAbout(), requirementToPutInToMap);
		requirementClassMapping.put(requirementToPutInToMap, requirementClass);
	}
	
	public Class getBlockClassForRelationship(Link link) {
		Class blockClass = model.createOwnedClass("id" + " ## " + "name" + " ## " + link.getValue().toString(), false);
		StereotypeApplicationHelper.getInstance(null).applyStereotype(blockClass, BlocksPackage.eINSTANCE.getBlock());
		
		relationshipBlockClasses.put(link, blockClass);
		
		return blockClass;
	}
	
	public Class getRequirementClassForRelationship(org.eclipse.lyo.client.oslc.resources.Requirement requirementToConvert) {
		String className = requirementToConvert.getTitle();
		Class requirementClass = model.createOwnedClass(className, false);
		Requirement requirement = (Requirement) StereotypeApplicationHelper.getInstance(null).applyStereotype(requirementClass, RequirementsPackage.eINSTANCE.getRequirement());
		requirement.setId(requirementToConvert.getIdentifier() + " ## " + requirementToConvert.getAbout().toString());
		requirement.setText(requirementToConvert.getDescription());
		
		return requirementClass;
	}
	
	public void createRequirementClasses(Map<java.net.URI, org.eclipse.lyo.client.oslc.resources.Requirement> requirementMapping, Map<org.eclipse.lyo.client.oslc.resources.Requirement, Class> requirementClassMapping) {
		for (Entry<java.net.URI, org.eclipse.lyo.client.oslc.resources.Requirement> entry : requirementMapping.entrySet()) {
			String className = entry.getValue().getTitle();
			Class requirementClass = model.createOwnedClass(className, false);
			Requirement requirement = (Requirement) StereotypeApplicationHelper.getInstance(null).applyStereotype(requirementClass, RequirementsPackage.eINSTANCE.getRequirement());
			requirement.setId(entry.getValue().getIdentifier() + " ## " + entry.getValue().getAbout().toString());
			requirement.setText(entry.getValue().getDescription());
			
			requirementClassMapping.put(entry.getValue(), requirementClass);
		}
	}
	
	public void createRequirementCollectionClasses(Map<java.net.URI, org.eclipse.lyo.client.oslc.resources.RequirementCollection> requirementCollectionMapping, Map<org.eclipse.lyo.client.oslc.resources.RequirementCollection, Class> requirementCollectionClassMapping) {
		for (Entry<java.net.URI, org.eclipse.lyo.client.oslc.resources.RequirementCollection> entry : requirementCollectionMapping.entrySet()) {
			Class blockClass = model.createOwnedClass(entry.getValue().getIdentifier() + " ## " + entry.getValue().getTitle() + " ## " + entry.getValue().getAbout().toString(), false);
			StereotypeApplicationHelper.getInstance(null).applyStereotype(blockClass, BlocksPackage.eINSTANCE.getBlock());
			
			requirementCollectionClassMapping.put(entry.getValue(), blockClass);
		}
	}
	
	/**
	 * projectIds = project names
	 */
	@Override
	public void createRequirementModel(String modelName, String[] projectIds) {
		test(modelName, projectIds);
	}

	@Override
	public void doIncrementalModelUpdate() {
		// TODO Auto-generated method stub
		
	}
}
