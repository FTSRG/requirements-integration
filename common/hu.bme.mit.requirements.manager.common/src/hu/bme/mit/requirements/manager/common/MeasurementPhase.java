package hu.bme.mit.requirements.manager.common;

public class MeasurementPhase {
	
	public static final String PHASE0 = "Phase 0 - model initialization - ";
	
	public static final String POLARION_PHASE1_1 = "Phase 1_1 - polarion server init";
	public static final String POLARION_PHASE1_2 = "Phase 1_2 - polarion server login";
	public static final String POLARION_PHASE2 = "Phase 2 - polarion query work items";
	public static final String POLARION_PHASE3 = "Phase 3 - polarion convert work items to papyrus requirement model";
	public static final String POLARION_PHASE4 = "Phase 4 - polarion save papyrus requirement model";
	public static final String POLARION_PHASE5_1 = "Phase 5_1 - incremental query - convert work item to papyrus model";
	public static final String POLARION_PHASE5_2 = "Phase 5_2 - incremental query - save changed model";
	
	public static final String POLARION_QUERY_WORKITEM_BY_URI = "polarion query work item by uri";
	public static final String POLARION_QUERY_WORKITEM_BY_URI_WITHOUT_CACHE = "Phase 5_1_0 - polarion query wok item by uri without cache";
	
	public static final String POLARION_ENTIRE_PROCESS = "polarion entire process";
	
	public static final String DNG_PHASE1 = "Phase 1 - DNG server init, login";
	public static final String DNG_PHASE2 = "Phase 2 - dng query artifacts";
	public static final String DNG_PHASE3 = "Phase 3 - dng convert work items to papyrus requirement model";
	public static final String DNG_PHASE4 = "Phase 4 - dng save papyrus requirement model";
	
	public static final String DNG_REQCOLL_QUERY = "Phase 2_2 Query RequirementCollection";
	public static final String DNG_REQ_QUERY = "Phase 2_3 Query Requirement";
	public static final String DNG_OSLC_QUERY_RESULT = "Phase 2_1 OSLC Query Result";
	
	public static final String DNG_ENTIRE_PROCESS = "dng entire process";
}
