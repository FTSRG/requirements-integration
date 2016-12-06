package hu.bme.mit.requirements.manager.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.papyrus.junit.utils.rules.HouseKeeper;
import org.eclipse.papyrus.sysml14.util.SysMLResource;
import org.eclipse.papyrus.uml.tools.utils.PackageUtil;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.util.UMLUtil;

public abstract class RequirementManager {
	protected HouseKeeper houseKeeper;
	protected ResourceSet resourceSet;
	protected Model model;
	protected String umlFileName;
	protected String modelName = "RootElement";
	
	public abstract void createRequirementModel(String modelName, String[] projectIds);
	
	public abstract void doIncrementalModelUpdate();
	
	/**
	 * Initialize an empty model with the necessary profiles.
	 * @param umlFileName_
	 */
	public void initModel(String umlFileName_) {
		// measurement
		long start = System.nanoTime();
		
		houseKeeper = new HouseKeeper();
		resourceSet = houseKeeper.createResourceSet();
		model = SysMLResource.createSysMLModel(resourceSet, umlFileName_, modelName);

		applySysml14PackageImports(model);
		
		// measurement
		long elapsedTime = System.nanoTime() - start;
		MeasureSingleton.getInstance().timeStorage.add(new MyPair(MeasurementPhase.PHASE0 + umlFileName_, elapsedTime));
	}
	
	/**
	 * Applies SysML1.4 specific package imports.
	 * @param model
	 */
	public void applySysml14PackageImports(Model model) {
		// ---------------------------------- based on
		// org.eclipse.papyrus.sysml14.ui.CreateSysML14ModelCommand.java
		// ----------------------------------
		Package umlPrimitiveTypes = (Package) PackageUtil.loadPackage(
				URI.createURI(UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI), model.eResource().getResourceSet());
		if (umlPrimitiveTypes != null) {
			PackageImport pi = UMLFactory.eINSTANCE.createPackageImport();
			pi.setImportedPackage(umlPrimitiveTypes);
			model.getPackageImports().add(pi);
		}

		Package sysmlLibrary = (Package) PackageUtil.loadPackage(URI.createURI(SysMLResource.LIBRARY_PATH),
				model.eResource().getResourceSet());
		if (sysmlLibrary != null) {
			PackageImport pi = UMLFactory.eINSTANCE.createPackageImport();
			pi.setImportedPackage(sysmlLibrary);
			model.getPackageImports().add(pi);
		}
		// ---------------------------------------------------------------------------------------------------------------------------------------------
	}
	
	// ----------- Based on
		// org.eclipse.papyrus.sysml.utils.SysMLTestResources.java -----------
		
		/**
		 * Saves the given resource to a papyrus specific sysml model.
		 * @param resourceSet
		 * @param package_
		 * @param uri
		 */
		public void save(ResourceSet resourceSet, org.eclipse.uml2.uml.Package package_, URI uri) {
			Resource resource = resourceSet.getResource(uri, true);
			EList<EObject> contents = resource.getContents();

			contents.add(package_);

			for (Iterator<?> allContents = UMLUtil.getAllContents(package_, true, false); allContents.hasNext();) {

				EObject eObject = (EObject) allContents.next();

				if (eObject instanceof Element) {
					contents.addAll(((Element) eObject).getStereotypeApplications());
				}
			}

			try {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, Boolean.TRUE);
				resource.save(options);
			} catch (IOException ioe) {
				// err
			}
		}

		public void registerResourceFactories() {
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
					UMLResource.Factory.INSTANCE);
		}

}
