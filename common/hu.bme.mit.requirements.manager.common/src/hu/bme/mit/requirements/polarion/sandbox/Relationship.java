package hu.bme.mit.requirements.polarion.sandbox;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.requirements.Requirement;
import org.eclipse.papyrus.sysml14.requirements.RequirementsPackage;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLUtil.StereotypeApplicationHelper;

import com.polarion.alm.ws.client.types.tracker.WorkItem;

import hu.bme.mit.requirements.manager.common.MeasureSingleton;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Stereotype;

public class Relationship {
	
	private static final String FOLLOW_UP = "follow_up";

	public Abstraction getAbstraction(Class client, Class supplier) {
		Abstraction abstraction = UMLFactory.eINSTANCE.createAbstraction();
		abstraction.getClients().add(client);
		abstraction.getSuppliers().add(supplier);
		
		// measurement
		MeasureSingleton.getInstance().abstractions += 1;
		
		return abstraction;
	}
	
	public void addAbstractionToModelAndApplyStereotype(Model model, Abstraction abstraction, String roleName) {
		if (!roleName.equals("parent")) {
			model.getPackagedElements().add(abstraction);
			EClass relationshipType = getRelationshipTypeByRoleName(roleName);
			StereotypeApplicationHelper.getInstance(null).applyStereotype(abstraction, relationshipType);
		}
	}

	private final static Map<String, EObject> roleNamesToTypes;
	static {
		roleNamesToTypes = new HashMap<String, EObject>();
		roleNamesToTypes.put(FOLLOW_UP, RequirementsPackage.eINSTANCE.getTrace());
		
	}
	
	
	public EClass getRelationshipTypeByRoleName(String role) {
		switch (role) {
		case "relates_to":
			return RequirementsPackage.eINSTANCE.getTrace();
		case "refines":
			return RequirementsPackage.eINSTANCE.getRefine();
		case "implements":
			return RequirementsPackage.eINSTANCE.getSatisfy();
		case "depends_on":
			return RequirementsPackage.eINSTANCE.getTrace();
		case "duplicates":
			return RequirementsPackage.eINSTANCE.getCopy();
		case FOLLOW_UP:
			return RequirementsPackage.eINSTANCE.getTrace();
		case "verifies":
			return RequirementsPackage.eINSTANCE.getVerify();
		}
		return RequirementsPackage.eINSTANCE.getTrace();
	}
	
	public void setContainment2(Class parent, Class child) {
		EList<Classifier> el = parent.getNestedClassifiers();
		el.add(child);
		
		// measurement
		MeasureSingleton.getInstance().containments += 1;
	}
}
