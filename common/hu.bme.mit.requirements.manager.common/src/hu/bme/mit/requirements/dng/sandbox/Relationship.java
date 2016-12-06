package hu.bme.mit.requirements.dng.sandbox;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.sysml14.requirements.RequirementsPackage;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.util.UMLUtil.StereotypeApplicationHelper;

import hu.bme.mit.requirements.manager.common.MeasureSingleton;

public class Relationship {
	public void setContainment(Class parent, Class child) {
		EList<Classifier> el = parent.getNestedClassifiers();
		el.add(child);
		
		// measurement
		MeasureSingleton.getInstance().containments += 1;
	}
	
	public Abstraction getAbstraction(Class client, Class supplier) {
		Abstraction abstraction = UMLFactory.eINSTANCE.createAbstraction();
		abstraction.getClients().add(client);
		abstraction.getSuppliers().add(supplier);
		
		// measurement
		MeasureSingleton.getInstance().abstractions += 1;
		
		return abstraction;
	}
	
	public void addAbstractionToModelAndApplyStereotype(Model model, Abstraction abstraction, String roleName) {
		if (!roleName.equals("decomposes")) {
			model.getPackagedElements().add(abstraction);
			EClass relationshipType = getRelationshipTypeByRoleName(roleName);
			StereotypeApplicationHelper.getInstance(null).applyStereotype(abstraction, relationshipType);
		} else {
			System.out.println("Can't be decomposes. Use containment.");
		}
	}
	
	public EClass getRelationshipTypeByRoleName(String role) {
		switch (role) {
		case "elaboratedBy":
			return RequirementsPackage.eINSTANCE.getRefine();
		case "specifiedBy":
			return RequirementsPackage.eINSTANCE.getRefine();
		case "affectedBy":
			return RequirementsPackage.eINSTANCE.getTrace();
		case "trackedBy":
			return RequirementsPackage.eINSTANCE.getTrace();
		case "implementedBy":
			return RequirementsPackage.eINSTANCE.getSatisfy();
		case "validatedBy":
			return RequirementsPackage.eINSTANCE.getVerify();
		case "satisfiedBy":
			return RequirementsPackage.eINSTANCE.getSatisfy();
		case "constrainedBy":
			return RequirementsPackage.eINSTANCE.getTrace();
		}
		return RequirementsPackage.eINSTANCE.getTrace();
	}
}
