package com.bpmnq.compliancechecker;

//import java.io.File;

import java.io.IOException;

import java.io.PrintWriter;

import java.util.List;

import org.apache.log4j.Logger;
import org.ontoware.rdf2go.util.ModelUtils;

import com.bpmnq.Association;
import com.bpmnq.DataObject;
import com.bpmnq.GraphObject;
import com.bpmnq.MemoryQueryProcessor;
import com.bpmnq.Path;
import com.bpmnq.ProcessGraph;
import com.bpmnq.QueryGraph;
import com.bpmnq.AbstractQueryProcessor;
import com.bpmnq.UndirectedAssociation;

import com.bpmnq.Utilities;
import com.bpmnq.Association.AssociaitonType;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.Path.PathEvaluation;

import static com.bpmnq.Path.TemporalType.*;
import com.bpmnq.finitestatemachine.FiniteStateMachine;
import com.bpmnq.petrinet.GraphReducer;
import com.bpmnq.petrinet.PetriNet;
import com.bpmnq.petrinet.PetriNetGenerator;
import com.bpmnq.petrinet.Place;
import com.bpmnq.petrinet.Transition;

/**
 * this class encapsulates all work on
 * 1 - processing the query
 * 2 - reducing the result process graph
 * 3 - generating the temporal expression
 * 4 - writing the final .smv file and invoking model checker
 * 5 - appending the result to the result file of the query answer.
 *
 * @author Ahmed Awad
 */
public final class ModelChecker {
    public static final int RET_NET_COMPLIES = 1;
    public static final int RET_NET_DOESNT_COMPLY = 0;
    public static final int RET_NO_NET_MATCHES = -1;
    public static final int RET_NET_HAS_DEADLOCK = -2;
    public static final int RET_NET_IS_UNBOUNDED = -3;
    public boolean writeNonCompliantMatchingProcessResult; // to write the result of structural match of pattern but not compliant
    public boolean writeCompliantProcessResult; // to write the structurally matching and compliant results
    private Logger log = Logger.getLogger(ModelChecker.class);
    
    private TemporalQueryGraph myTQGraph;
    private ProcessGraph myPGraph;
    private GraphReducer graphReducer;
    private PetriNetGenerator petriNetGen;
    private FiniteStateMachine fsm;
    public AbstractQueryProcessor queryProc;
    private PrintWriter answerWriter;
    private void init()
    {
	writeCompliantProcessResult = true;
	writeNonCompliantMatchingProcessResult = false;
    }
    public ModelChecker(TemporalQueryGraph tqg,PrintWriter out) {
	myTQGraph = tqg;
	myPGraph = new ProcessGraph();
	graphReducer = null;
	petriNetGen = null;
	fsm = null;
	answerWriter = out;
	queryProc = new MemoryQueryProcessor(out);
	init();
    }
    public ModelChecker()
    {
	myTQGraph = null;
	myPGraph = new ProcessGraph();
	graphReducer = null;
	petriNetGen = null;
	fsm = null;
	queryProc = new MemoryQueryProcessor(null);
	init();
    }
    public void setQueryGraph(TemporalQueryGraph tq)
    {
	myTQGraph = tq;
    }
    private QueryGraph prepareQuery()
    {
	QueryGraph preparedQuery = (QueryGraph)myTQGraph.clone();
	int nsize = myTQGraph.nodes.size();
	GraphObject start = null;
	GraphObject end = null;
	boolean startEventFound = false;
	boolean endEventFound = false;
	for (int i = 0 ; i < nsize;i++)
	{
	    GraphObject currentGraph = myTQGraph.nodes.get(i); 
	    if (currentGraph.type == GraphObjectType.EVENT 
		    && currentGraph.type2.endsWith("1"))
	    {
		start = currentGraph;
		startEventFound = true;
		//break;
	    }
	    if (currentGraph.type == GraphObjectType.EVENT 
		    && currentGraph.type2.endsWith("3"))
	    {
		end = currentGraph;
		endEventFound = true;
		//break;
	    }
	}
	if (start == null) // no start event found
	{
	    start = new GraphObject();
	    start.setID("0"); // it is required to resolve it
//	    start.setName("");
	    start.type = GraphObjectType.EVENT;
	    start.type2 = GraphObject.EventType.START.asType2String();
	}
	if (end == null) // no start event found
	{
	    end = new GraphObject();
	    end.setID("0"); // it is required to resolve it
//	    end.setName("");
	    end.type = GraphObjectType.EVENT;
	    end.type2 = GraphObject.EventType.END.asType2String();
	}
	for (Path currPath : myTQGraph.paths)
	{
	    if (currPath.getTemporalTag() == PRECEDES)
	    {
		if (!startEventFound)
		{
		    preparedQuery.add(start);
		    startEventFound = true;
		}
		preparedQuery.add(new Path(start, currPath.getDestinationGraphObject(), ""));
	    }
	    if (currPath.getTemporalTag() == LEADS_TO)
	    {
		if (!endEventFound)
		{
		    preparedQuery.add(end);
		    endEventFound = true;
		}
		preparedQuery.add(new Path(currPath.getSourceGraphObject(),end, ""));
	    }
	}
	
	// Added on 19.08.2009
	for (Path p : preparedQuery.paths)
	    p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
//	System.out.println("########## Implied structural query ############");
//	preparedQuery.print(System.out);
//	System.out.println("########## Implied structural query ############");
//	if (myTQGraph.associations.size() > 0)
//	{
//	    Path pp = new Path(start,end);
//	    pp.setPathEvaluaiton(PathEvaluation.CYCLIC);
//	    DataObject dob = new DataObject("@D");
//	    dob.setState("?");
//	    UndirectedAssociation uda = new UndirectedAssociation(dob.originalNode(),pp);
//	    preparedQuery.add(pp);
//	    preparedQuery.add(dob);
//	    preparedQuery.addDataPathAssociation(uda);
//	}
	// just remove behavioral associaitons from prepared query
	boolean found = true;
	while (preparedQuery.associations.size() > 0 && found)
	{
	    found = false;
	    for (Association ass : preparedQuery.associations)
	    {
		if (ass.assType == AssociaitonType.Behavioral)
		{
		    preparedQuery.associations.remove(ass);
		    found = true;
		    break;
		}

	    }
	}
	return preparedQuery;

    }

    public int checkModelWithReduction(String modelID) {
	myPGraph = new ProcessGraph();
	ProcessGraph reducedPGraph;
	myPGraph = queryProc.runQueryAgainstModel(prepareQuery(), modelID);
	if (myPGraph.nodes.size() > 0) {
	    graphReducer = new GraphReducer(myPGraph, myTQGraph.generateKeepList(modelID));
	    reducedPGraph = graphReducer.getReducedGraph();
	    return doWork(reducedPGraph, modelID);
	}
	return RET_NO_NET_MATCHES; // Query didn't find a match
    }

    public int checkModelWithoutReduction(String modelID)
    {
	myPGraph = new ProcessGraph();
	QueryGraph prepared = prepareQuery();
	System.out.println("ZZZZZZZZZZZZZZZZZZZZZZ Implied Structural Query ");
	prepared.print(System.out);
	System.out.println("ZZZZZZZZZZZZZZZZZZZZZZ Implied Structural Query ");
	myPGraph = queryProc.runQueryAgainstModel(prepared, modelID);
	
	   
	if (myPGraph.nodes.size() > 0)
	{
	    //myPGraph = normalize(myPGraph);
	    myPGraph.modelURI = modelID;
	    System.out.println("##########################Match Query to Process Model "+modelID);
	    myPGraph.print(System.out);
	    System.out.println("##########################Match Query to Process Model "+modelID);
	    myPGraph.loadFromOryx(modelID); // this is just a workaround for presentation purpose at ICSOC
	    int result = doWork(myPGraph, modelID);
	    if (answerWriter !=null)
	    {
		
		if (result == RET_NET_HAS_DEADLOCK)
		{
		    myPGraph.exportXML(answerWriter,"<match>pattern</match>\n<diagnosis>matching has a deadlock</diagnosis>");
		}
		else if (result == RET_NET_IS_UNBOUNDED)
		{
		    myPGraph.exportXML(answerWriter,"<match>pattern</match>\n<diagnosis>matching is unbounded</diagnosis>");
		}
		else if (result == RET_NET_COMPLIES)
		{
		    if (writeCompliantProcessResult)
			myPGraph.exportXML(answerWriter,"<match>pattern</match>\n<diagnosis>complies</diagnosis>");
		}
		else if (result == RET_NET_DOESNT_COMPLY)
		{
		    if (writeNonCompliantMatchingProcessResult)
			myPGraph.exportXML(answerWriter,"<match>pattern</match>\n<diagnosis>does not comply</diagnosis>");
		}
		
	    }
	    
	    
	    return result;
	}
//	if (answerWriter != null)
//	{
//	    answerWriter.println("<diagnosis>no match</diagnosis>");
//	    answerWriter.println("</query-result>");
//	}
	return RET_NO_NET_MATCHES;
    }

    private String normalizeModelID(String ModelID)
    {
	if (ModelID.startsWith("http"))
	{
	    return ModelID.substring(ModelID.indexOf("model/")+6, ModelID.indexOf("/rdf"));
	}
	else
	    return ModelID;
    }
   
    private int doWork(ProcessGraph p,String modelID)
    {
	long startTime, endTime;
	petriNetGen = new PetriNetGenerator(p);
	petriNetGen.generatePTNetFromProcessGraph();
	PetriNet testDead = new PetriNet();

	for (Place pp : petriNetGen.getPetriNet().myPlaces)
	    testDead.addPlace(pp);

	for (Transition tt : petriNetGen.getPetriNet().myTransitions)
	    testDead.addTransition(tt);

	testDead.shortCircuit();
	log.debug("Testing for deadlock...");
	String scLolanetFilename =Utilities.makeTempfilePath("shortcircuitlolaspec"+normalizeModelID(modelID)+".net");
	try
	{
	    testDead.writeLoLANetFile(scLolanetFilename);
	    
	}
	catch(IOException e)
	{
	    if (answerWriter != null)
		answerWriter.println("<Exception>"+e.getMessage()+"</Exception>");
	}
	scLolanetFilename = "\""+scLolanetFilename+"\"";
	//net is unbounded
	List<String> rslt ;
	rslt = Utilities.callLoLA(Utilities.LOLA_PATH_DEADLOCK, scLolanetFilename);
	for (String resString : rslt)
	{
	    if (resString.toUpperCase().contains("DEAD STATE FOUND")) {
		return RET_NET_HAS_DEADLOCK;
	    }
	}

	// test boundedness
	log.debug("Testing for unboundedness...");
	String pnLolanetFilename = Utilities.makeTempfilePath("lolaspec"+normalizeModelID(modelID)+".net");
	
	try
	{
	    petriNetGen.writePetriNetToLOLAFile(pnLolanetFilename);
	} catch (IOException e)
	{
	    if (answerWriter != null)
		answerWriter.println("<Exception>"+e.getMessage()+"</Exception>");
	}
	pnLolanetFilename = "\""+pnLolanetFilename+"\"";
	rslt = Utilities.callLoLA(Utilities.LOLA_PATH_BOUNDED, pnLolanetFilename);
	for (String resString : rslt)
	{
	    if (resString.toUpperCase().contains("NET IS UNBOUNDED")) {
		return RET_NET_IS_UNBOUNDED;
	    }
	}
	// call lola to generate the state file.
	// First check whether the net suffers from anomalies
	// FIXME Why is the result ignored??
	log.debug("Generating the reachability graph...");
	rslt = Utilities.callLoLA(Utilities.LOLA_PATH, pnLolanetFilename);
	log.debug(" Lola state space building result");
	for (String resString : rslt)
	{
	    log.debug(resString);
	}
	String fsmLolanetFilename = Utilities.makeTempfilePath("lolaspec"+normalizeModelID(modelID)+".graph");
	String nuSmvFilename = Utilities.makeTempfilePath("lolaspec"+normalizeModelID(modelID)+".smv");
	try
	{
	    fsm = new FiniteStateMachine(p);
	    log.debug("Loading the behavioral model...");
	    fsm.loadStateFromLOLAStateFile(fsmLolanetFilename);
	    log.debug("Generating the Kripke structure...");
	    fsm.writeNuSMVSpecToFile(nuSmvFilename, myTQGraph.toString());
	    
	} catch (IOException e)
	{
	    log.error("unrecoverable error when calling external tools");
	    return RET_NET_DOESNT_COMPLY;
	}
	System.out.println("Calling NuSMV...");
	startTime = System.currentTimeMillis();
	nuSmvFilename = " \""+nuSmvFilename+"\"";
	System.out.println("Nusmv command "+Utilities.NUSMV_PATH+nuSmvFilename);
	rslt = Utilities.callNuSMV(Utilities.NUSMV_PATH, nuSmvFilename);
	endTime = System.currentTimeMillis();
	log.info("NuSMV took "+(endTime-startTime)+" milliseconds");

	for (String resString : rslt)
	{
	    System.out.println("Numsv "+resString);
	    if (resString.toUpperCase().contains("FALSE")) {
		log.info(resString);
		return RET_NET_DOESNT_COMPLY;
	    }
	}
	
	return RET_NET_COMPLIES;
    }
    private int doWork2(ProcessGraph p,String modelID, String formula)
    {
	long startTime, endTime;
	if (!p.modelURI.equals(modelID))
	{
	    petriNetGen = new PetriNetGenerator(p);
	    petriNetGen.generatePTNetFromProcessGraph();
	}
	String pnLolanetFilename = Utilities.makeTempfilePath("lolaspec"+normalizeModelID(modelID)+".net");
	List<String> rslt ;
//	PetriNet testDead = new PetriNet();
//
//	for (Place pp : petriNetGen.getPetriNet().myPlaces)
//	    testDead.addPlace(pp);
//
//	for (Transition tt : petriNetGen.getPetriNet().myTransitions)
//	    testDead.addTransition(tt);
//
//	testDead.shortCircuit();
//	log.debug("Testing for deadlock...");
//	String scLolanetFilename = Utilities.makeTempfilePath("shortcircuitlolaspec"+modelID+".net");
//	testDead.writeLoLANetFile(scLolanetFilename);
//
//	//net is unbounded
//	
//	rslt = Utilities.callLoLA(Utilities.LOLA_PATH_DEADLOCK, scLolanetFilename);
//	for (String resString : rslt)
//	{
//	    if (resString.toUpperCase().contains("DEAD STATE FOUND")) {
//		return RET_NET_HAS_DEADLOCK;
//	    }
//	}
//	log.debug("No dead states found");
//	System.out.println("No dead states found");
//	// test boundedness
//	log.debug("Testing for unboundedness...");
//	
//	petriNetGen.writePetriNetToLOLAFile(pnLolanetFilename);
//	rslt = Utilities.callLoLA(Utilities.LOLA_PATH_BOUNDED, pnLolanetFilename);
//	for (String resString : rslt)
//	{
//	    if (resString.toUpperCase().contains("NET IS UNBOUNDED")) {
//		return RET_NET_IS_UNBOUNDED;
//	    }
//	}
//	log.debug("Net is bounded");
//	System.out.println("Net is bounded");
	// call lola to generate the state file.
	// First check whether the net suffers from anomalies
	// FIXME Why is the result ignored??
	if (!p.modelURI.equals(modelID))
	{
	    log.debug("Generating the reachability graph...");
	    rslt = Utilities.callLoLA(Utilities.LOLA_PATH, pnLolanetFilename);
	}
	String fsmLolanetFilename = Utilities.makeTempfilePath("lolaspec"+normalizeModelID(modelID)+".graph");
	String nuSmvFilename = Utilities.makeTempfilePath("lolaspec"+normalizeModelID(modelID)+".smv");
	
	try
	{
//	    if (!p.modelURI.equals(modelID))
//	    {
		fsm = new FiniteStateMachine(this.myPGraph);
		log.debug("Loading the behavioral model...");
		fsm.loadStateFromLOLAStateFile(fsmLolanetFilename);
		log.debug("Generating the Kripke structure...");
//	    }
	    fsm.writeNuSMVSpecToFile(nuSmvFilename, formula);
	} catch (IOException e)
	{
	    log.error("unrecoverable error when calling external tools");
	    return RET_NET_DOESNT_COMPLY;
	}
	nuSmvFilename = " \""+nuSmvFilename+"\"";
	System.out.println("Calling NuSMV "+Utilities.NUSMV_PATH+" "+nuSmvFilename);
	startTime = System.currentTimeMillis();
	rslt = Utilities.callNuSMV(Utilities.NUSMV_PATH, nuSmvFilename);
	endTime = System.currentTimeMillis();
	log.info("NuSMV took "+(endTime-startTime)+" milliseconds");

	for (String resString : rslt)
	{
	    System.out.println("Result "+resString);
	    if (resString.toUpperCase().contains("FALSE")) {
		log.info(resString);
		return RET_NET_DOESNT_COMPLY;
	    }
	}
	
	return RET_NET_COMPLIES;
    }
    public int checkModelAgainstFormula(String modelID,String formula)
    {
	
	//myPGraph = queryProc.runQueryAgainstModel(prepareQuery(), modelID);
	if (!myPGraph.modelURI.equals(modelID))
	    if (modelID.startsWith("http"))
		myPGraph.loadFromOryx(modelID);
	    else
		myPGraph.loadModelFromRepository(modelID);
	
	if (myPGraph.nodes.size() > 0)
	{
	    return doWork2(myPGraph, modelID,formula);
	}
	return RET_NO_NET_MATCHES;
    }
    public void setProcessGraph(ProcessGraph p)
    {
	myPGraph = (ProcessGraph) p.clone();
    }
    public ProcessGraph getProcessGraph()
    {
	return (ProcessGraph) myPGraph.clone();
    }
}
