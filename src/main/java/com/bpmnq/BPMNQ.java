package com.bpmnq;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;



import java.io.FileWriter;
import java.io.IOException;


import java.io.PrintWriter;

import java.net.URI;

//import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;


//import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.themis.ir.eTVSMOntology;

import quicktime.app.actions.NextImageAction;
//import org.apache.log4j.PropertyConfigurator;

import com.bpmnq.compliancechecker.BusinessContext;
import com.bpmnq.compliancechecker.ComplianceViolationExplanator;
import com.bpmnq.compliancechecker.ModelChecker;
import com.bpmnq.compliancechecker.TemporalLogicQuerySolver;
import com.bpmnq.compliancechecker.TemporalQueryGraph;
import com.bpmnq.finitestatemachine.FiniteStateMachine;
//import com.bpmnq.finitestatemachine.FiniteStateMachine;
//import com.bpmnq.petrinet.PetriNetGenerator;
//import com.bpmnq.petrinet.PetriNet;
import com.bpmnq.petrinet.PetriNet;
import com.bpmnq.petrinet.PetriNetGenerator;
import com.bpmnq.queryexpander.ETVSMLoader;
import com.bpmnq.queryexpander.QueryExpander;
import com.bpmnq.AbstractQueryProcessor.ProcessorCommand;
import com.bpmnq.Association.AssociaitonType;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.Path.PathEvaluation;
import com.bpmnq.Path.TemporalType;
import com.bpmnq.QueryGraphBuilderRDF.RdfSyntax;
import com.complianceviolationresolution.ComplianceResolver.ViolationType;
import com.pst.ProcessStructureTree;
import com.pst.Node.NodeType;

/**
 * Main entry point to the BPMN-Q processor. 
 * Parses command-line options and invokes the corresponding routines.
 *
 * @author Ahmed Awad
 */
public final class BPMNQ
{
    private Logger log = Logger.getLogger(BPMNQ.class);
    private PrintWriter answerWriter;
    private AbstractQueryProcessor qProcessor;
    
    public static void main(String arg[]) throws Exception {
	System.out.println("BPMN-Q Version 1.3 \nCopyright (c) Ahmed Awad & Steffen Ryll 2007-2011");
	System.out.println("Last update: 20.04.2011");
	System.out.println("Free available memory: " + Runtime.getRuntime().freeMemory()/1024/1024 + " MB ");
	// init utilities
	Utilities util = Utilities.getInstance();
	
	

	BPMNQ bpmnq = new BPMNQ();
	long startTime, endTime;
	startTime = System.currentTimeMillis();

	// we have received a command line parameter with an .xml file containing a query
	if (arg.length > 0) {
	    System.out.println("Using command line argument");
	    final String command = arg[0].toUpperCase();

	    if (command.equals("QUERY"))
	    {
		String fileName = arg[1];
		if (arg.length > 2)
		{
		    bpmnq.doQuery(fileName, true);
		}
		else
		    bpmnq.doQuery(fileName,false);
	    }
	    else if (command.equals("OQUERY"))
	    {
		String fileName = arg[1];
		
		bpmnq.doOryxQuery(fileName);
	    }
	    else if (command.equals("MODEL"))
	    {
		String fileName = arg[1];
		bpmnq.doModel(fileName);
	    }
	    else if (command.equals("CHECK"))
	    {
		String fileName = arg[2];
		String modelId = arg[1];
		if (arg.length > 3)
		{
		    bpmnq.doCheck(fileName, modelId,true);
		}
		else
		    bpmnq.doCheck(fileName, modelId,false);
	    }
	    else if (command.equals("OCHECK"))
	    {
		String fileName = arg[2];
		String modelId = arg[1];
		bpmnq.doOryxCheck(fileName, modelId);
	    }
	    else if (command.equals("SEMANTIC_QUERY"))
	    {
		String fileName = arg[2];
		float threshold = Float.parseFloat(arg[1]);
		bpmnq.doSemanticQuery(fileName, threshold);
	    }
	    else if (command.equals("COMPLIANCE_QUERY"))
	    {
		String fileName = arg[1];
		bpmnq.doComplianceQuery(fileName);
	    }
	    else if (command.equals("GENERATE_TEMPORAL_EXPRESSION"))
	    {
		String fileName = arg[1];
		bpmnq.doGenerateTemporalExpression(fileName);
	    }
	    else if (command.equals("COMPLIANCE_CHECK"))
	    {
		String fileName = arg[2];
		String modelId = arg[1];
		bpmnq.doComplianceCheck(fileName, modelId);
	    } else if (command.equals("WRITE_PROPERTIES")) {
		util.writeProperties();
	    }
	    else if (command.equals("COMPLIANCE_VIOLATION_QUERY"))
	    {
		String fileName = arg[1];
		bpmnq.doComplianceViolationQuery(fileName);
	    }
	    
	    else if (command.equals("COMPLIANCE_VIOLATION_CHECK"))
	    {
		String fileName = arg[2];
		String modelId = arg[1];
		bpmnq.doComplianceViolationCheck(fileName, modelId);
	    }
	    else if (command.equals("OCOMPLIANCE_VIOLATION_CHECK"))
	    {
		String fileName = arg[2];
		String modelId = arg[1];
		bpmnq.doOryxComplianceViolationCheck(fileName, modelId);
	    }
	    else if (command.equals("OCOMPLIANCE_VIOLATION_QUERY"))
	    {
		String fileName = arg[1];
		bpmnq.doOryxComplianceViolationQuery(fileName);
	    }
	    else if (command.equals("RELOAD_ORYX_REP_TO_ONTOLOGY"))
	    {
		bpmnq.doReloadOryxRepositoryToETVSMOntology();
	    }
	    else if(command.equals("UPLOAD_ORYX_TO_DB"))
		bpmnq.doUploadOryxModelsToDB();
	    else
	    {
		System.out.println("Command not understood. Exiting.");
		System.exit(-1);
	    }

	} else {
	    bpmnq.doDefaultCase();		
	}

	endTime = System.currentTimeMillis();
	System.out.println("Total time = "+ (endTime - startTime) );
	System.out.println("Done...");
	bpmnq.cleanup();

    }
    
    private void doReloadOryxRepositoryToETVSMOntology()
    {
	ETVSMLoader eloader = new ETVSMLoader();
	
	OryxMemoryQueryProcessor omqp = new OryxMemoryQueryProcessor(this.answerWriter,"http://localhost:8080/backend/poem");

//	try
//	{
//	    List<String> models = omqp.findRelevantModelsMemory(null);
	    List<String> models = new ArrayList<String>(1);
	    models.add("http://localhost:8080/backend/poem/model/1028/rdf");
	    models.add("http://localhost:8080/backend/poem/model/1027/rdf");
	    for (String mdl : models)
	    {
		eloader.loadOryxModel(mdl);
	    }
//	} catch (IOException e)
//	{
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	} 
    }

    public BPMNQ() throws IOException
    {
	String answerFilename = Utilities.makeOutputfilePath(Utilities.config.getProperty("bpmnq.outputfile", "answer.xml"));
	try
	{
	    this.answerWriter = new PrintWriter(new BufferedWriter(
	    	new FileWriter(answerFilename)));
	    this.qProcessor = new MemoryQueryProcessor(this.answerWriter);
	    

	    // in an ideal world, answerWriter should be closed properly before terminating
	} catch (IOException e)
	{
	    log.error("Could not open file " + answerFilename + " for writing answer.", e);
	    throw e;
	}
 
    }
    
    public void cleanup() throws SQLException
    {
	this.answerWriter.close();
	Utilities.closeConnection();
    }

    /**
     * @param modelFileName
     */
    public void doGenerateTemporalExpression(String modelFileName) {
	//QueryGraphBuilderXML dd = new QueryGraphBuilderXML(modelFileName);
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	QueryGraph qry;
	try
	{
	    qry = dd.buildGraph();
	    TemporalQueryGraph tqry = new TemporalQueryGraph(); 
    	    for (GraphObject nd: qry.nodes )
    	    {
    		tqry.add(nd);
    	    }
    	    tqry.dataObjs.addAll(qry.dataObjs);
    	    for (Path p : qry.paths)
    	    {
    		if (p.label.toUpperCase().equals("PRECEDES"))
    		    tqry.addPrecedesPath(p.getSourceGraphObject(), p.getDestinationGraphObject(),p.exclude);
    		else if (p.label.toUpperCase().equals("LEADSTO"))
    		    tqry.addLeadsToPath(p.getSourceGraphObject(), p.getDestinationGraphObject(),p.exclude);
    	    }
    	    tqry.associations.addAll(qry.associations);
    	    for (Association a : tqry.associations)
    		if (a.frmDataObject != null)
    		    a.frmDataObject.normalize();
    		else if (a.toDataObject != null)
    		    a.toDataObject.normalize();
    	    System.out.println("Temporal Expression: ");
    	    BusinessContext bc = new BusinessContext();
    	    bc.loadContradictingStates();
    	    for (String s : tqry.getTemporalExpressions(bc))
    	    {
    		System.out.println(s.replace("  ", " ").replace("true & ", "").replace("G(!false) &", ""));//.replace("G(!false) ", ""));
    	    }
	}
	
	catch(FileFormatException e)
	{
	    log.error("The provided query file has syntax errors. Cannot proceed.", e);
	}
	
	
    }

    /**
     * @param modelFileName
     * @param threshold
     */
    public void doSemanticQuery(String modelFileName, float threshold) {
	handleDBConnection();
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	QueryGraph qry = null;
	try {
	    qry = dd.buildGraph();
	} catch (FileFormatException e) {
	    e.printStackTrace();
	}
	qry.print(System.out);
	QueryExpander qe = new QueryExpander(this.answerWriter);
	qe.runSemanticQuery(qry, threshold);
    }

    /**
     * @param modelFileName
     * @param modelId
     */
    public void doCheck(String modelFileName, String modelId,boolean allowEvalToNone) {
	handleDBConnection();
	QueryGraph qry = null;
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	try
	{
	    qry = dd.buildGraph();
	}
	catch(FileFormatException e)
	{
	    log.error("The provided query file has syntax errors. Cannot proceed.", e);
	}
	
	log.info("Checking model " + modelId);
//	Utilities.initialQueryGraphs.clear();
//	Utilities.isMultiQueryMode = false;
	this.qProcessor.stopAtFirstMatch = false;
	this.qProcessor.includeEnclosingAndSplits = false;
	this.qProcessor.allowGenericShapeToEvaluateToNone = allowEvalToNone;
	this.qProcessor.testQueryAgainstModel(qry, modelId, null);

    }

    /**
     * @param modelFileName
     */
    public void doModel(String modelFileName) {
//	ProcessGraphBuilderXML dd = new ProcessGraphBuilderXML(modelFileName);
//	ProcessGraph prs = dd.buildGraph();
//	prs.saveToDB();
    }

    /**
     * @param modelFileName
     * @param wo
     */
    private void handleDBConnection()
    {
	try { 
	    if (!Utilities.isConnectionOpen() && Utilities.QUERY_PROCESSOR_TYPE.toUpperCase().equals("DATABASE")) {
		Utilities.openConnection();
		System.out.println("Connection established with repository...");
	    }
	}  
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
    public void doQuery(String modelFileName,boolean allowGenEvalToNone) {
	
	handleDBConnection();
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	
	QueryGraph qry = null;
	try {
	    qry = dd.buildGraph();
	} catch (FileFormatException e) {
	    e.printStackTrace();
	}

	log.info("Querying models");
//	Utilities.initialQueryGraphs.clear();
//	Utilities.isMultiQueryMode = false;
	this.qProcessor.stopAtFirstMatch = false;
	this.qProcessor.includeEnclosingAndSplits = false;
	this.qProcessor.allowGenericShapeToEvaluateToNone = allowGenEvalToNone;
	this.qProcessor.processQuery(qry);

    }
    public void doOryxQuery(String modelFileName)
    {
	
	    handleDBConnection();
	
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	QueryGraph qry = null;
	try {
	    qry = dd.buildGraph();
	} catch (FileFormatException e) {
	    e.printStackTrace();
	}
	qry.print(System.out);
	log.info("Querying models");
	this.qProcessor = new EfficientQueryProcessorWithPathIndex(answerWriter);
//	this.qProcessor = new EfficientQueryProcessor(this.answerWriter);
//	this.qProcessor = new OryxMemoryQueryProcessor(this.answerWriter);
	this.qProcessor.stopAtFirstMatch = false;
	this.qProcessor.includeEnclosingAndSplits = false;
	this.qProcessor.allowGenericShapeToEvaluateToNone = false;
	long startProcessing = System.currentTimeMillis();
	qry.setAllowGenericShapeToEvaluateToNone(false);
	qry.setAllowIncludeEnclosingAndSplitDirective(false);
	this.qProcessor.processQuery(qry);
	System.out.println("Querying time :"+ ((System.currentTimeMillis() -startProcessing)-this.qProcessor.GetExtraOverheadTime()) + " MS");
	
    }
    public void doOryxCheck(String modelFileName, String modelID)
    {
	handleDBConnection();
	QueryGraph qry = null;
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	try
	{
	    qry = dd.buildGraph();
	}
	catch(FileFormatException e)
	{
	    log.error("The provided query file has syntax errors. Cannot proceed.", e);
	}
	
	log.info("Checking model " + modelID);
	
	qry.getResolvedVersion();
	qry.print(System.out);
	
//	this.qProcessor = new EfficientQueryProcessor(this.answerWriter);
	this.qProcessor = new EfficientQueryProcessorWithPathIndex(answerWriter);
//	this.qProcessor = new OryxMemoryQueryProcessor(this.answerWriter);
	this.qProcessor.allowGenericShapeToEvaluateToNone = false;
//	Utilities.initialQueryGraphs.clear();
//	Utilities.isMultiQueryMode = false;
	this.qProcessor.stopAtFirstMatch = false;
	this.qProcessor.includeEnclosingAndSplits = false;
	this.qProcessor.allowGenericShapeToEvaluateToNone = false;
	long startProcessing = System.currentTimeMillis();
	qry.setAllowGenericShapeToEvaluateToNone(false);
	qry.setAllowIncludeEnclosingAndSplitDirective(false);
	
	ProcessGraph result=null;
	//= this.qProcessor.runQueryAgainstModel(qry, modelID);
	if (this.qProcessor.testQueryAgainstModel(qry, modelID, result) && result !=null)
		result.print(System.out);
	
	System.out.println("Querying time :"+ (System.currentTimeMillis() -startProcessing) + " MS");
    }
    public void doComplianceQuery(String modelFileName) {
	handleDBConnection();
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	QueryGraph qry = null;
	try {
	    qry = dd.buildGraph();
	} catch (FileFormatException e) {
	    e.printStackTrace();
	}
	
	
	TemporalQueryGraph tqry = qry.getTemporalQueryGraph();
	ModelChecker mc = new ModelChecker(tqry,answerWriter);
	List<String> mdls =null;
	try{
	   mdls = this.qProcessor.findRelevantProcessModels(qry);
	}
	catch(IOException sle)
	{
	    log.error("Could not determine process models relevant to this query.", sle);
	}
	mc.queryProc.procCmd = ProcessorCommand.ComplianceQuery;
	answerWriter.println("<query-result>");
	for (int i = 0; i < mdls.size();i++)
	{
	    System.out.println("Checking model "+ mdls.get(i));
	    int result = mc.checkModelWithoutReduction(mdls.get(i));
	    if (result == -3)
		System.out.println("Petri Net generated from model "+ mdls.get(i) + " is unbounded. Inspection is not possible");
	    else if (result == -2)
		System.out.println("Model "+ mdls.get(i) + " suffers from a deadlock. Inspection is not possible");
	    else if (result ==-1)
	    {
		System.out.println("Query didnt find a match -> Does not comply :( ...");
//		this.qProcessor.printMessage("Model " + mdls.get(i) +" is not compliant");
	    }
	    else if (result == 1)
	    {
		System.out.println("Complies :) ...");
//		this.qProcessor.printMessage("Model " + mdls.get(i) +" is compliant");
	    }
	    else
	    {
		System.out.println("Does not Comply :( ...");
//		this.qProcessor.printMessage("Model " + mdls.get(i) +" is not compliant");
	    }

	}
	
	answerWriter.println("</query-result>");
    }
    
    
    
    public void doComplianceCheck(String modelFileName, String modelId) {
	handleDBConnection();
	
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	QueryGraph qry = null;
	try {
	    qry = dd.buildGraph();
	} catch (FileFormatException e) {
	    e.printStackTrace();
	}

	TemporalQueryGraph tqry = qry.getTemporalQueryGraph();

	ModelChecker mc = new ModelChecker(tqry,answerWriter);

	System.out.println("Checking model "+ modelId);
	mc.queryProc.procCmd = ProcessorCommand.ComplianceCheck;
	answerWriter.println("<query-result>");
	
	int result = mc.checkModelWithoutReduction(modelId);
	if (result == ModelChecker.RET_NET_IS_UNBOUNDED)
	    System.out.println("Petri Net generated from model "+ modelId + " is unbounded. Inspection is not possible");
	else if (result == ModelChecker.RET_NET_HAS_DEADLOCK)
	    System.out.println("Model "+ modelId + " suffers from a deadlock. Inspection is not possible");
	else if (result == ModelChecker.RET_NO_NET_MATCHES)
	{
	    System.out.println("Query didnt find a match -> Does not comply :( ...");
//	    this.qProcessor.printMessage("Model " + modelId +" is not compliant");
	}
	else if (result == ModelChecker.RET_NET_COMPLIES)
	{
	    System.out.println("Complies :) ...");
//	    this.qProcessor.printMessage("Model " + modelId +" is compliant");
	}
	else
	{
	    System.out.println("Does not Comply :( ...");
//	    this.qProcessor.printMessage("Model " + modelId +" is not compliant");
	}
	answerWriter.println("</query-result>");
    }
    public void doComplianceViolationCheck(String modelFileName, String modelId) {
	handleDBConnection();
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	QueryGraph qry = null;
	try {
	    qry = dd.buildGraph();
	} catch (FileFormatException e) {
	    e.printStackTrace();
	}

	TemporalQueryGraph tqry = qry.getTemporalQueryGraph();
	ModelChecker mc = new ModelChecker(tqry,answerWriter);

	System.out.println("Checking model "+ modelId);
	mc.queryProc.procCmd = ProcessorCommand.ComplianceCheck;
	answerWriter.println("<query-result>");
	boolean generateAntiPattern = false;
	int result = mc.checkModelWithoutReduction(modelId);
	if (result == ModelChecker.RET_NET_IS_UNBOUNDED)
	    System.out.println("Petri Net generated from model "+ modelId + " is unbounded. Inspection is not possible");
	else if (result == ModelChecker.RET_NET_HAS_DEADLOCK)
	    System.out.println("Model "+ modelId + " suffers from a deadlock. Inspection is not possible");
	else if (result == ModelChecker.RET_NO_NET_MATCHES)
	{
	    generateAntiPattern = true;
	    System.out.println("Query didnt find a match -> Does not comply :( ...");
//	    this.qProcessor.printMessage("Model " + modelId +" is not compliant");
	}
	else if (result == ModelChecker.RET_NET_COMPLIES)
	{
	    System.out.println("Complies :) ...");
//	    this.qProcessor.printMessage("Model " + modelId +" is compliant");
	}
	else
	{
	    generateAntiPattern = true;
	    System.out.println("Does not Comply :( ...");
//	    this.qProcessor.printMessage("Model " + modelId +" is not compliant");
	}
	if (generateAntiPattern)
	{
	    List<QueryGraph> antiPatterns = tqry.generateAntiPatternQueries();
	    ProcessGraph matchAntiPattern=null;
	    for (QueryGraph q : antiPatterns)
	    {
		matchAntiPattern = qProcessor.runQueryAgainstModel(q, modelId);
		if(matchAntiPattern.nodes.size() > 0)
		{
		    matchAntiPattern.modelURI = modelId;
		    matchAntiPattern.exportXML(answerWriter,"<match>antipattern</match>\n<diagnosis>violation scenario</diagnosis>");
		}
	    }
	   
	}
	answerWriter.println("</query-result>");
	
	
    }
    public void doOryxComplianceViolationCheck(String modelFileName, String modelId) {
	handleDBConnection();
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	QueryGraph qry = null;
	try {
	    qry = dd.buildGraph();
	} catch (FileFormatException e) {
	    e.printStackTrace();
	}

	TemporalQueryGraph tqry = qry.getTemporalQueryGraph();
	tqry.print(System.out);
	ModelChecker mc = new ModelChecker(tqry,answerWriter);
	qProcessor = new OryxMemoryQueryProcessor(answerWriter);
	qProcessor.includeEnclosingAndSplits = true;
	System.out.println("Checking model "+ modelId);
	mc.queryProc = new OryxMemoryQueryProcessor(answerWriter);
	mc.queryProc.procCmd = ProcessorCommand.ComplianceCheck;
	answerWriter.println("<query-result>");
	boolean generateAntiPattern = false;
	int result = mc.checkModelWithoutReduction(modelId);
	if (result == ModelChecker.RET_NET_IS_UNBOUNDED)
	    System.out.println("Petri Net generated from model "+ modelId + " is unbounded. Inspection is not possible");
	else if (result == ModelChecker.RET_NET_HAS_DEADLOCK)
	    System.out.println("Model "+ modelId + " suffers from a deadlock. Inspection is not possible");
	else if (result == ModelChecker.RET_NO_NET_MATCHES)
	{
	    generateAntiPattern = true;
	    System.out.println("Query didnt find a match -> Does not comply :( ...");
//	    this.qProcessor.printMessage("Model " + modelId +" is not compliant");
	}
	else if (result == ModelChecker.RET_NET_COMPLIES)
	{
	    System.out.println("Complies :) ...");
//	    this.qProcessor.printMessage("Model " + modelId +" is compliant");
	}
	else
	{
	    generateAntiPattern = true;
	    System.out.println("Does not Comply :( ...");
//	    this.qProcessor.printMessage("Model " + modelId +" is not compliant");
	}
	if (generateAntiPattern)
	{
	    // Handling data and control flow rules 17.11.2009
//	    ComplianceViolationExplanator cve = new ComplianceViolationExplanator(modelId);
	    ComplianceViolationExplanator cve = new ComplianceViolationExplanator(mc.getProcessGraph());
	    List<QueryGraph> antiPatterns = cve.explainViolation(tqry);
//	    List<QueryGraph> antiPatterns = tqry.generateAntiPatternQueries();
	    ProcessGraph matchAntiPattern=null;
	    for (QueryGraph q : antiPatterns)
	    {
		q.setAllowIncludeEnclosingAndSplitDirective(false);
		matchAntiPattern = qProcessor.runQueryAgainstModel(q, modelId);
		if(matchAntiPattern.nodes.size() > 0)
		{
		    matchAntiPattern.modelURI = modelId;
		    matchAntiPattern.exportXML(answerWriter,"<match>antipattern</match>\n<diagnosis>violation scenario</diagnosis>");
		}
	    }
	   
	}
	answerWriter.println("</query-result>");
	
	
    }
    protected GraphBuilder getGraphBuilderFor(String fileName) {
	GraphBuilder result;
	URI fileUri = new File(fileName).toURI();

	String file = fileName.toLowerCase();
	try {
	    if (file.endsWith("xml")) {
		result = new QueryGraphBuilderXML(fileName);
	    } else if (file.endsWith("rdf")){
		result = new QueryGraphBuilderRDF(fileUri, RdfSyntax.RDF_XML);
	    } else if (file.endsWith("xhtml")) {
		result = new QueryGraphBuilderRDF(fileUri, RdfSyntax.eRDF);
	    } else {
		throw new IllegalArgumentException("Unsupported file type for query. You may use XML, RDF or XHTML (eRDF) files.");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-9);
	    return null;
	}

	return result;
    }
    public void doOryxComplianceViolationQuery(String modelFileName)
    {
	this.qProcessor = new OryxMemoryQueryProcessor(this.answerWriter);
	doComplianceViolationQuery(modelFileName);
    }
    public void doComplianceViolationQuery(String modelFileName) {
	
	handleDBConnection();
	GraphBuilder dd = getGraphBuilderFor(modelFileName);
	QueryGraph qry = null;
	try {
	    qry = dd.buildGraph();
	} catch (FileFormatException e) {
	    e.printStackTrace();
	}
	
	TemporalQueryGraph tqry = qry.getTemporalQueryGraph();
	
	List<String> mdls =null;
	try{
	   mdls = this.qProcessor.findRelevantProcessModels(qry);
	}
	catch(IOException sle)
	{
	    log.error("Could not determine process models relevant to this query.", sle);
	}
	ModelChecker mc = new ModelChecker(tqry,answerWriter);
	mc.queryProc = this.qProcessor;
	mc.queryProc.procCmd = ProcessorCommand.ComplianceQueryWithViolationExplanation;
	answerWriter.println("<query-result>");
	boolean generateAntiPattern = false;
	List<String> compliantModels = new ArrayList<String>();
	for (int i = 0; i < mdls.size();i++)
	{
	    System.out.println("Checking model "+ mdls.get(i));
	    
	    int result = mc.checkModelWithoutReduction(mdls.get(i));
	    if (result == ModelChecker.RET_NET_IS_UNBOUNDED)
		System.out.println("Petri Net generated from model "+ mdls.get(i) + " is unbounded. Inspection is not possible");
	    else if (result == ModelChecker.RET_NET_HAS_DEADLOCK)
		System.out.println("Model "+ mdls.get(i) + " suffers from a deadlock. Inspection is not possible");
	    else if (result == ModelChecker.RET_NO_NET_MATCHES)
	    {
		generateAntiPattern = true;
		System.out.println("Query didnt find a match -> Does not comply :( ...");
//		this.qProcessor.printMessage("Model " + modelId +" is not compliant");
	    }
	    else if (result == ModelChecker.RET_NET_COMPLIES)
	    {
		System.out.println("Complies :) ...");
		compliantModels.add(mdls.get(i));
//		this.qProcessor.printMessage("Model " + modelId +" is compliant");
	    }
	    else
	    {
		generateAntiPattern = true;
		System.out.println("Does not Comply :( ...");
//		this.qProcessor.printMessage("Model " + modelId +" is not compliant");
	    }
	}
	if (generateAntiPattern)
	{
	    List<QueryGraph> antiPatterns = tqry.generateAntiPatternQueries();
	    ProcessGraph matchAntiPattern=null;
	    for (QueryGraph q : antiPatterns)
	    {
		try
		{
		    List<String> mdls2 = qProcessor.findRelevantProcessModels(q);
		    for (String x : mdls2)
		    {
			if (compliantModels.contains(x))
			    continue;
			matchAntiPattern = qProcessor.runQueryAgainstModel(q, x);
			if(matchAntiPattern.nodes.size() > 0)
			{
			    matchAntiPattern.modelURI = x;
			    matchAntiPattern.exportXML(answerWriter,"<match>antipattern</match>\n<diagnosis>violation scenario</diagnosis>");
			}
		    }
		} 
		catch (IOException e)
		{
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}


	    }

	}

	answerWriter.println("</query-result>");
	
    }
    
    protected void aboutPST()
    {
	System.out.println("Testing default case");
	com.pst.Node nd[];
	nd = new com.pst.Node[13];
	for (int i = 0; i < nd.length;i++)
	    nd[i] = new com.pst.Node();
	
	com.pst.ProcessStructureTree pst=  new ProcessStructureTree();
	pst.loadTreeFromFile("C:\\BPMNQ_Result\\test\\pst6.xml");
	pst.print(System.out);
	nd[0] = pst.getOccurrencesOfLabel("go to checkout").get(0);
	nd[8] = pst.getOccurrencesOfLabel("notify customer").get(0);
//	nd[8].setLabel("Send goods");
//	nd[8].setNodeType(NodeType.Activity);
	
	com.pst.Node source, destination;
	source = nd[0];
	destination = nd[8];
	BusinessContext bc = new BusinessContext();
	com.complianceviolationresolution.ComplianceResolver cr 
	= new com.complianceviolationresolution.ComplianceResolver(pst,bc);
	ViolationType vt;
//	List<List<String>> eff = cr.generateCumulativeEffect(nd[0], null) ;
	com.pst.ProcessStructureTree compTree=null;
	do
	{
	    vt= cr.getViolationTypeOfLeadsTo(source, destination);
	    
	    if (vt == ViolationType.DifferentBranches)
	    {
		System.out.println("Violation of Rule : "+source.toString() +" leads to "+ destination.toString()+" is of type DIFFERENT BRANCHES");
		compTree=null;
		compTree = cr.resolveDifferentBranchesViolation(source, destination);
	    }
	    else if (vt == ViolationType.InverseOrder)
	    {
		System.out.println("Violation of Rule : "+source.toString() +" leads to "+ destination.toString()+" is of type INVERSE ORDER");
		compTree = null;
		compTree = cr.resolveInverseOrderViolation(source, destination);
	    }
	    else if (vt == ViolationType.LackofActivity)
	    {
		System.out.println("Violation of Rule : "+source.toString() +" leads to "+ destination.toString()+" is of type LACK OF ACTIVITY");
		compTree=null;
		compTree = cr.resolveLackOfActivityViolation(source, destination);
	    }
	    else if (vt == ViolationType.splittingChoice)
	    {
		System.out.println("Violation of Rule : "+source.toString() +" leads to "+ destination.toString()+" is of type SPLITTING CHOICE");
		compTree=null;
		compTree = cr.resolveSplittingChoiceViolation(source, destination);
	    }
	    else
	    {
		System.out.println("No violation to the Rule : "+source.toString() +" leads to "+ destination.toString());
	    }
	    if (compTree != null && vt != ViolationType.none)
	    {
		System.out.println("Tree after resolving violation:");
//		compTree.normalizeTree();
		compTree.print(System.out);
//		break;
	    }
	    else
	    {
		System.out.println("It is not possible to resolve that violation, terminating");
		break;
	    }
	    if (compTree.getOccurrencesOfLabel(destination.getLabel()).size() > 0)
		    destination = compTree.getOccurrencesOfLabel(destination.getLabel()).get(0);
	}while(vt != ViolationType.none);
	if (compTree != null)
	{
	    compTree.normalizeTree();
	    System.out.println("Final Tree is:");
	    compTree.print(System.out);
	}
    }
    public void doDefaultCase3()
    {
	ProcessGraph p = new ProcessGraph();
	String modelID ="http://localhost:8080/backend/poem/model/248/rdf";
	p.loadFromOryx(modelID);
	PetriNetGenerator png = new PetriNetGenerator(p);
	png.generatePTNetFromProcessGraph();
	FiniteStateMachine fsm = new FiniteStateMachine(p);
	//String pnLolanetFilename = "c:/bpmnq_result/temp/lolaspec50.apnn";
	String pnLolanetFilename = "/Users/ahmedawad/Documents/development/lolaspec248.net";
	try
	{
//	    png.getPetriNet().writeAPNNNFile(pnLolanetFilename);
	    //png.getReadableNet().writeAPNNNFile(pnLolanetFilename);
	    
	    png.getReadableNet().writeLoLANetFile(pnLolanetFilename);
//	    return;
	}
	catch(IOException ioe)
	{
	    System.out.println(ioe.getMessage());
	}
//	
//	Utilities.callLoLA("lola", pnLolanetFilename);
	
	//String fsmLolanetFilename = Utilities.makeTempfilePath("lolaspec50.graph");
	String fsmLolanetFilename = "/Users/ahmedawad/Documents/development/lolaspec248.graph";
	String nuSmvFilename = "/Users/ahmedawad/Documents/development/lolaspec248.smv";
	
	try
	{
//	    if (!p.modelURI.equals(modelID))
//	    {
		
		log.debug("Loading the behavioral model...");
		fsm.loadStateFromLOLAStateFile(fsmLolanetFilename);
		log.debug("Generating the Kripke structure...");
//	    }
	    fsm.writeNuSMVSpecToFile(nuSmvFilename, "");
	    fsm.writeDotFile(nuSmvFilename.replace("smv", "dot"));
	} catch (IOException e)
	{
	    log.error("unrecoverable error when calling external tools");
	    
	}
    }
    public void doDefaultCase1()
    {
	handleDBConnection();
	Map<QueryGraph,String> queryMatches = new HashMap<QueryGraph,String>(); 
	
	GraphBuilder gBuilder = getGraphBuilderFor("C:/Query1.rdf");
        QueryGraph query = null;
        try {
            query = gBuilder.buildGraph();
            
            // Added for Debugging
//            System.out.print("####### Servlet path");
//            System.out.println((String) this.getServletContext().getRealPath("."));
            
            System.out.println("########################################## QUERY #################################");
//            log.info("########################################## QUERY #################################");
//            log.info(query.toString());
            query.print(System.out);
            System.out.println("########################################## QUERY #################################");
        } catch (FileFormatException e) {
            e.printStackTrace();
            
        }
        AbstractQueryProcessor qProcessor;
        qProcessor = new OryxMemoryQueryProcessor(answerWriter); 
        TemporalQueryGraph tqry = query.getTemporalQueryGraph();
	ModelChecker mc = new ModelChecker(tqry,answerWriter);
	mc.queryProc = new OryxMemoryQueryProcessor(answerWriter);
	List<String> mdls =null;
	try{
	   mdls = qProcessor.findRelevantProcessModels(query);
	}
	catch(IOException sle)
	{
	    log.error("Could not determine process models relevant to this query.", sle);
	}
	mc.queryProc.procCmd = ProcessorCommand.ComplianceQuery;
	answerWriter.println("<query-result>");
//	System.out.println("<query-result>");
	
	for (int i = 0; i < mdls.size();i++)
	{
		boolean generateAntiPattern = false;
		try
		{
		    	System.out.println("Check Pattern Against Model "+mdls.get(i));
//			System.out.println("############# Checking model "+mdls.get(i));
			int result = mc.checkModelWithoutReduction(mdls.get(i));
			System.out.println("############# Model Checker Result is "+result );
			if (result == ModelChecker.RET_NO_NET_MATCHES)
			{
				generateAntiPattern = true;
//				System.out.println("Query didnt find a match -> Does not comply :( ...");

			}
			else if (result == ModelChecker.RET_NET_DOESNT_COMPLY)
			{
				generateAntiPattern = true;
//				System.out.println("Does not Comply :( ...");

			}
			if (generateAntiPattern)
			{
				ComplianceViolationExplanator cve = new ComplianceViolationExplanator(mdls.get(i));
				
				List<QueryGraph> antiPatterns =  cve.explainViolation(tqry);// tqry.generateAntiPatternQueries();
				
				for (QueryGraph q : antiPatterns)
				{
//					System.out.println("########################################## ANTI PATTERN QUERY #################################");
//					q.print(System.out);
//					System.out.println("########################################## ANTI PATTERN QUERY #################################");
					// this has to be changed
					List<String> mdls2 = qProcessor.findRelevantProcessModels(q);
					
					for (String s : mdls2)
					{
						boolean insert = false;
						ProcessGraph matchAntiPattern=null;
						QueryGraph cln = (QueryGraph) q.clone();
						matchAntiPattern = qProcessor.runQueryAgainstModel(cln, s);
						if(matchAntiPattern.nodes.size() > 0)
						{
							
							boolean qFound= false;
							for (QueryGraph qq : queryMatches.keySet())
							{
							    
							    if (qq.getSignature().containsAll(q.getSignature()))
							    {
								qFound = true;
								String mdlls = queryMatches.get(qq);
								if (!mdlls.contains(s))
								{
								    mdlls +=","+s;
								    queryMatches.put(qq, mdlls);
								    insert = true;
								}
							    }
							    else
							    {
								System.out.println("Map query Signature "+qq.getSignature().toString());
								System.out.println("Investigated query signature "+q.getSignature().toString());
							    }
							}
							if (!qFound)
							{
								insert = true;
								queryMatches.put(q, s);
							}
//							System.out.println("############################### Insert value is "+insert);
							if (insert)
							{
								matchAntiPattern.modelURI = s;
								matchAntiPattern.exportXML(answerWriter,"<match>antipattern</match>\n<diagnosis>violation scenario</diagnosis>");
								matchAntiPattern.print(System.out);
//								System.out.println("<match>antipattern</match>\n<diagnosis>violation scenario</diagnosis>");
							}
							
						}
						else
						{
							System.out.println("Anti Pattern Query didnt find a match");
						}
					}
				}

			}
		}
		catch(Exception e)
		{
			answerWriter.println("<Exception>"+e.getMessage()+"</Exception>");
		}

	}
	
	answerWriter.println("</query-result>");
	System.out.println("########################################## PROCESS COMPLIANCE QUERY #################################");
    }
    public void doUploadOryxModelsToDB()
    {
	handleDBConnection();
	ProcessGraph p = new ProcessGraph();
	PrintWriter ans = new PrintWriter (System.out);
	OryxMemoryQueryProcessor qp= new OryxMemoryQueryProcessor(ans);
	ProcessGraphAuxiliaryData pgad;
	long totalProcessingTime=0;
//	try
//	{
	    
//	    List<String> relevant = qp.findRelevantModelsMemory(null);
	    List<String> relevant = new ArrayList<String>(1);
	    relevant.add("http://localhost:8080/backend/poem/model/1028/rdf");
	    relevant.add("http://localhost:8080/backend/poem/model/1027/rdf");
	    System.out.println("Processing a total of "+relevant.size()+" models");
//	    System.exit(0);
	    int nexTD = 1027;
	    for (String r : relevant)
	    {
		if (r.contains("913"))
		    continue;
		else if (r.contains("895"))
		    continue;
		else if (r.contains("621"))
		    continue;
		else if (r.contains("546"))
		    continue;
		try
		{
		    System.out.println("Processing model "+r);
		    p.loadFromOryx(r);
		    // just a trick
		    if (p.modelURI.contains("1028"))
			p.modelURI = p.modelURI.replace("1028", "2001");
		    if (p.modelURI.contains("1027"))
			p.modelURI = p.modelURI.replace("1028", "2000");
		    p.saveToDB();
//		    System.out.println("Model "+p.modelURI +" was saved to the database");
		    long startprocesisng = System.currentTimeMillis();
		    pgad = new ProcessGraphAuxiliaryData(p);
		    totalProcessingTime += (System.currentTimeMillis() - startprocesisng);
		    
//		    System.out.println("Closure index of model "+p.modelURI +" was constructed");
		    // duplicate the data
		    p.modelURI = "http://localhost:8080/backend/poem/model/"+nexTD+"/rdf";
		    p.saveToDB();
//		    System.out.println("Model "+p.modelURI +" was saved to the database");
		    startprocesisng = System.currentTimeMillis();
		    pgad = new ProcessGraphAuxiliaryData(p);
		    totalProcessingTime += (System.currentTimeMillis() - startprocesisng);
//		    System.out.println("Closure index of model "+p.modelURI +" was constructed");
		    nexTD++;
		    
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		}
	    }
	    System.out.println("Processing time of closure calculation is: "+totalProcessingTime +" MS");
	}
//	catch(IOException ioe)
//	{
//	    System.out.println(ioe.getMessage());
//	}
//    }
    public void doDefaultCase9()
    {
	handleDBConnection();
	ProcessGraphAuxiliaryData pgad = new ProcessGraphAuxiliaryData("http://localhost:8080/backend/poem/model/23/rdf");
	pgad.print();
    }
    public void doDefaultCase8()
    {
	handleDBConnection();
	ProcessGraph p = new ProcessGraph();
	p.loadFromOryx("http://localhost:8080/backend/poem/model/137/rdf");
	ProcessGraphAuxiliaryDataGenerator pa = new ProcessGraphAuxiliaryDataGenerator(p);
	pa.print();
//	List<SequenceFlow> closure = pa.establishSequenceFlowTransitiveClosure();
//	
//	for (SequenceFlow e : closure)
//	{
//	    e.print(System.out);
//	}
    }
    public void doDefaultCase4()
    {
	handleDBConnection();
	
	 ORYXModelXMLParser xp=new ORYXModelXMLParser();
	 
	 xp.createModel("http://localhost:8080/backend/poem/model/255/rdf");
//	 xp.createModel("http://adage.cse.unsw.edu.au:9090/backend/poem/model/48/rdf");
//	 xp.createModel("http://oryx-project.org/backend/poem/model/11745/rdf");
	 PartialProcessModel ppm = xp.getPartialProcessModel();
	 
	 ppm.print();
//	 System.exit(0);
	 this.qProcessor = new OryxMemoryQueryProcessor(this.answerWriter);
	 this.qProcessor.stopAtFirstMatch = false;
	 this.qProcessor.includeEnclosingAndSplits = false;
	 this.qProcessor.allowGenericShapeToEvaluateToNone = false;
	 ProcessGraph result = ppm.evaluatePPM(this.qProcessor);
	 //result.print(System.out);
	 PrintWriter writer = new PrintWriter(System.out);
	 result.exportXMLDetailed(writer);
	 writer.flush();
	 writer.close();
    }
    public void doDefaultCase5()
    {
	// Check performance of lola versus dssmc
	long startTimeLola, endTimeLola, startTimeDss, endTimeDss, startTimeNuSMV, endTimeNuSMV;
	String lolaCmd = "C:\\test\\lola\\lola --analysis=spec2.ctl lolaspec50.net";
	String dssCmd = "C:\\test\\dss\\zbddmc --net-file=net50.apnn --Ctl-file=spec2.ctl";
	String nusmvCmd = "C:\\test\\nusmv\\nusmv lolaspec50.smv";
	
	try
	{
	    // calling lola 
	    startTimeLola = System.currentTimeMillis();
	    Process lolaProc = Runtime.getRuntime().exec(lolaCmd);
	    
//	    BufferedReader in = new BufferedReader(new InputStreamReader(
//		    lolaProc.getInputStream()));
//	    String currentLine = null;
//	    while ((currentLine = in.readLine()) != null)
//		System.out.println(currentLine);
	    lolaProc.waitFor();
	    endTimeLola = System.currentTimeMillis();
	    
	    System.out.println("Lola took: "+(endTimeLola-startTimeLola ) +"ms");
	    
	    startTimeDss = System.currentTimeMillis();
	    Process dssProc = Runtime.getRuntime().exec(dssCmd);
	    dssProc.waitFor();
	    endTimeDss = System.currentTimeMillis();
	    
	    
	    System.out.println("DssMc took: "+(endTimeDss-startTimeDss) +"ms");
	    
	    startTimeNuSMV = System.currentTimeMillis();
	    Process nusmvProc = Runtime.getRuntime().exec(nusmvCmd);
	    nusmvProc.waitFor();
	    endTimeNuSMV = System.currentTimeMillis();
	    
	    
	    System.out.println("NuSMV took: "+(endTimeNuSMV-startTimeNuSMV) +"ms");
	    
	    
	    
	    // calling dss
	} catch (IOException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (InterruptedException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
    }
    private void doDefaultCase6()
    {
	GraphObject x1,x2,x3,x4,x5,x6;
	x1 = new GraphObject();
	x1.setName("A");
	x1.type = GraphObjectType.ACTIVITY;
	x1.type2 = "";
	x1.setID("1");

	x2 = new GraphObject();
	x2.setName("B");
	x2.type = GraphObjectType.ACTIVITY;
	x2.type2 = "";
	x2.setID("2");
	
	x3 = new GraphObject();
	x3.setName("C");
	x3.type = GraphObjectType.ACTIVITY;
	x3.type2 = "";
	x3.setID("3");
	
	x4 = new GraphObject();
	x4.setName("D");
	x4.type = GraphObjectType.ACTIVITY;
	x4.type2 = "";
	x4.setID("4");
	
	x5 = new GraphObject();
	x5.setName("E");
	x5.type = GraphObjectType.ACTIVITY;
	x5.type2 = "";
	x5.setID("5");
	
	x6 = new GraphObject();
	x6.setName("F");
	x6.type = GraphObjectType.ACTIVITY;
	x6.type2 = "";
	x6.setID("6");
	
	List<GraphObject> excluded = new ArrayList<GraphObject>();
	excluded.add(x2);
	
	ProcessGraph p = new ProcessGraph();
	p.add(x1);p.add(x2);p.add(x3);p.add(x4);p.add(x5);p.add(x6);
	p.addEdge(x1, x2);
	p.addEdge(x2,x3);
	p.addEdge(x2,x4);
	p.addEdge(x3,x5);
	p.addEdge(x4,x6);
	p.addEdge(x5, x6);
	p.addEdge(x6,x3);
	
	ProcessGraphAuxiliaryDataGenerator sftc = new ProcessGraphAuxiliaryDataGenerator(p);
	
	
    }
    private void doDefaultCase7()
    {
	GraphBuilder dd = getGraphBuilderFor("/Users/ahmedawad/Documents/My Writing/Design by Selection/Coop With Remco/Experiments/Queries/PPM4Q4.RDF");
	QueryGraph qry = null;
	try {
	    qry = dd.buildGraph();
	    FileWriter f0 = new FileWriter("/Users/ahmedawad/Documents/My Writing/Design by Selection/Coop With Remco/Experiments/Queries/PPM4Q4.xml");
	    PrintWriter writer = new PrintWriter(f0);
	    qry.exportXML(writer);
	    f0.flush();
	    f0.close();
	} catch (FileFormatException e) {
	    e.printStackTrace();
	} catch (IOException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    private void doDefaultCase10()
    {
	File folder = new File("/Users/ahmedawad/Downloads/From Oryx BPMN2.0 Processes/");
	File[] listOfFiles = folder.listFiles();

	for (int i = 0; i < listOfFiles.length; i++) {
	    if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".rdf")) {
		System.out.println("File " + listOfFiles[i].getPath());
		try
		{
		    
		    String fileName = listOfFiles[i].getName().replace(".rdf", "");
//		    BufferedReader in = new BufferedReader(new FileReader(listOfFiles[i].getPath()));
		    FileInputStream fstream = new FileInputStream(listOfFiles[i].getPath());
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		        BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    FileOutputStream fstreamOut = new FileOutputStream(fileName+"-.rdf");
		    DataOutputStream out = new DataOutputStream(fstreamOut);
		    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		      // Print the content on the console
		      System.out.println (strLine);
		      strLine = strLine.replace("bpmn2.0", "bpmn1.1");
		      strLine = strLine.replace("StartNone", "Start");
		      strLine = strLine.replace("EndNone", "End");
		      bw.write(strLine);
		    }
		    out.flush();
		    out.close();
		    in.close();
		} catch (Exception e)
		{
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		System.out.println("File " + listOfFiles[i].getName());
	    } else if (listOfFiles[i].isDirectory()) {
		System.out.println("Directory " + listOfFiles[i].getName());
	    }
	}

    }
    public void doDefaultCase() 
    {
	doDefaultCase4();
//	handleDBConnection();
//	ProcessGraph p = new ProcessGraph();
//	PrintWriter ans = new PrintWriter (System.out);
//	OryxMemoryQueryProcessor qp= new OryxMemoryQueryProcessor(ans);
//	
//	GraphObject src;
//	src = new GraphObject();
//	src.setID("-1");
//	src.setName("A");
//	src.type = GraphObjectType.ACTIVITY;
//	GraphObject dst;
//	dst = new GraphObject();
//	dst.setID("-1");
//	dst.setName("B");
//	dst.type = GraphObjectType.ACTIVITY;
////	dst.type2 = "3";
//	Path pa = new Path(src,dst,"",TemporalType.LEADS_TO);
////	pa.setPathEvaluaiton(PathEvaluation.ACYCLIC);
//	TemporalQueryGraph q = new TemporalQueryGraph();
//	q.add(src);
//	q.add(dst);
//	q.add(pa);
////	q.setAllowIncludeEnclosingAndSplitDirective(true);
//	ComplianceViolationExplanator cve = new ComplianceViolationExplanator("http://localhost:8080/backend/poem/model/47/rdf");
//	List<QueryGraph> antiPatterns = cve.explainViolation(q);
//	for (QueryGraph g : antiPatterns)
//	{
//	    p = qp.runQueryAgainstModel(g, "http://localhost:8080/backend/poem/model/47/rdf");
//	    p.print(System.out);
//	}
//	
//	try
//	{
//	    List<String> relevant = qp.findRelevantModelsMemory(null);
//	    for (String r : relevant)
//	    {
//		System.out.println("Processing model "+r);
//		p.loadFromOryx(r);
//		p.saveToDB();
//	    }
//	}
//	catch(IOException ioe)
//	{
//	    System.out.println(ioe.getMessage());
//	}

//	try
//	{
//	    String model = "imported-2282";
//	    String id = "oryx_F6D54AE0-EFE1-418E-A7C4-59D1F9CE42D2";
//	    URL processuri = new URL("http://xenodot.hpi:5984/"+model+"/_design/bpmnq/_view/node_type_2?key=[\""+id+"\"]");
//	    BufferedReader in = new BufferedReader(new InputStreamReader(processuri.openStream()));
//	    String line ;
//	    line = in.readLine();
//	    
//	    while(line!= null)
//	    {
//		if (line.contains("\"key\":[\""+id+"\"]"))
//		{
//		    Pattern p = Pattern.compile("value\":\\[\\\".*\\]");
//		    Matcher m = p.matcher(line);
//		    
//		    if(m.find())
//		    {
//			String match = m.group();
//			System.out.println(match.substring(match.indexOf("[\"")+2,match.indexOf("\"]")));
//			
//		    }
//		    
//		}
//		line = in.readLine();
//	    }
//	}
//	catch(MalformedURLException mfe)
//	{
//	    System.out.println(mfe.getMessage());
//	}
//	catch(IOException ioe)
//	{
//	    System.out.println(ioe.getMessage());
//	}
//	OryxMemoryQueryProcessor oqp = new OryxMemoryQueryProcessor(this.answerWriter);
//	ProcessGraph resultGraph= new ProcessGraph();
//	oqp.testQueryAgainstModel(null, "http://localhost:8080/backend/poem/model/16/rdf", resultGraph);
//	
//	ProcessGraph p = new ProcessGraph();
//	p.loadModelFromRepository("25");
//	PetriNetGenerator pn = new PetriNetGenerator(p);
//	pn.generatePTNetFromProcessGraph();
//	pn.writePetriNetToLOLAFile(Utilities.TEMP_DIRECTORY+"/25.net");
//	List<String> result = Utilities.callLoLA(Utilities.LOLA_PATH, Utilities.TEMP_DIRECTORY+"/25.net");
//	for (String s : result)
//	    System.out.println(s);
	
//	QueryGraph q = new QueryGraph();
//	try
//	{
//	    InputStream rdfXmlInput =  new FileInputStream("C:/behavioralQuery2.rdf");
//	    String baseUri = "BPMN-Q"; //erdfFile.toURI().toString();
//
//	    QueryGraphBuilderRDF testable = new QueryGraphBuilderRDF();
//	    testable.setRdfInput(rdfXmlInput, RdfSyntax.RDF_XML, baseUri);
//            q = testable.buildGraph();
//            q.print(System.out);
//            
//	}
//	catch(FileNotFoundException fnf)
//	{
//	    System.out.println(fnf.getMessage());
//	    
//	}
//	catch(IOException e)
//        {
//            System.out.println(e.getMessage());
//        }
//        catch(FileFormatException fe)
//        {
//            System.out.println(fe.getMessage());
//        }
	//InputStream rdfXmlInput = getClass().getResourceAsStream("c:/deadlockQuery.rdf");
        
            
        
        
	
//	ProcessGraph p = new ProcessGraph();
//	p.loadFromOryx("http://localhost:8080/backend/poem/model/15/rdf");
//	p.print(System.out);
	
	
	
    }
///////////////////////////// OLD PARTS FROM THE DO DEFAULT CASE //////////////////////////////////////////
/// PST
//    BusinessContext bc = new BusinessContext();
//	bc.fullLoad();
//	com.complianceviolationresolution.Planner pl = new com.complianceviolationresolution.Planner(bc);
//	
//	List<String> initState = new ArrayList<String>();
//	for (String s : bc.getAllDataObjectStates())
//	{
//	    if (s.contains("initial"))
//		initState.add(s);
//	}
//	List<String> goalState = new ArrayList<String>();
//	goalState.add("EXECUTED Send-goods");
//	ProcessStructureTree pst = pl.findPlan(initState, goalState);
//	pst.print(System.out);

//	Preparing the tree
//	com.pst.Node nd[];
//	nd = new com.pst.Node[13];
//	for (int i = 0; i < nd.length;i++)
//	    nd[i] = new com.pst.Node();
//	nd[0].setCondition("true");
//	nd[0].setNodeType(com.pst.Node.NodeType.Sequence);
//	nd[1].setLabel("Go to checkout");
//	nd[1].setNodeType(com.pst.Node.NodeType.Activity);
//	nd[2].setLabel("Provide shipping address");
//	nd[2].setNodeType(com.pst.Node.NodeType.Activity);
//	
//	nd[3].setNodeType(com.pst.Node.NodeType.XChoice);
//	
//	nd[4].setLabel("Prepare goods");
//	nd[4].setNodeType(com.pst.Node.NodeType.Activity);
//	nd[5].setLabel("Send goods");
//	nd[5].setNodeType(com.pst.Node.NodeType.Activity);
//////	
//	nd[6].setCondition("PAYMENT_METHOD_credit");
//	nd[6].setNodeType(com.pst.Node.NodeType.Sequence);
//	nd[7].setLabel("Provide credit card data");
//	nd[7].setNodeType(com.pst.Node.NodeType.Activity);
//	nd[8].setLabel("Pay by credit card");
//	nd[8].setNodeType(com.pst.Node.NodeType.Activity);
//////	
//	nd[9].setCondition("PAYMENT_METHOD_bank");
//	nd[9].setNodeType(com.pst.Node.NodeType.Sequence);
//	nd[10].setLabel("Provide bank data");
//	nd[10].setNodeType(com.pst.Node.NodeType.Activity);
//	nd[11].setLabel("Pay by bank transfer");
//	nd[11].setNodeType(com.pst.Node.NodeType.Activity);
//	nd[12].setLabel("Notify customer");
//	nd[12].setNodeType(com.pst.Node.NodeType.Activity);
////	
//	com.pst.ProcessStructureTree pst = new ProcessStructureTree(nd[0]);
//	pst.insertNode(nd[1], nd[0], 1);
//	pst.insertNode(nd[2], nd[0], 2);
//	pst.insertNode(nd[3], nd[0], 3);
//	pst.insertNode(nd[4], nd[0], 4);
//	pst.insertNode(nd[5], nd[0], 5);
//	
//	pst.insertNode(nd[6], nd[3]);
//	pst.insertNode(nd[9], nd[3]);
//	pst.insertNode(nd[7], nd[6],1);
//	pst.insertNode(nd[8], nd[6],2);
//	
//	pst.insertNode(nd[10], nd[9],1);
//	pst.insertNode(nd[11], nd[9],2);
//	pst.insertNode(nd[12], nd[9],3);
//	pst.print(System.out);
////	Preparing the business context
//	BusinessContext bc = new BusinessContext();
//	bc.fullLoad();
//	
//
////	Preparing the compliance violation resolver
//	com.complianceviolationresolution.ComplianceResolver cr 
//		= new com.complianceviolationresolution.ComplianceResolver(pst,bc);
//	String cumeffect = cr.generateCumulativeEffect(nd[1], null);
//	System.out.println("Cummulative effect : "+ cumeffect);
//	com.pst.Node source, destination;
//	source = nd[8];
//	destination = nd[4];
//	ViolationType vt= cr.getViolationTypeOfLeadsTo(source, destination);
//	do
//	{
//	    com.pst.ProcessStructureTree compTree=null;
//	    if (vt == ViolationType.DifferentBranches)
//	    {
//		System.out.println("Violation of Rule : "+source.toString() +" leads to "+ destination.toString()+" is of type DIFFERENT BRANCHES");
//		compTree = cr.resolveDifferentBranchesViolation(source, destination);
//	    }
//	    else if (vt == ViolationType.InverseOrder)
//	    {
//		System.out.println("Violation of Rule : "+source.toString() +" leads to "+ destination.toString()+" is of type INVERSE ORDER");
//	    }
//	    else if (vt == ViolationType.LackofActivity)
//	    {
//		System.out.println("Violation of Rule : "+source.toString() +" leads to "+ destination.toString()+" is of type LACK OF ACTIVITY");
//		compTree = cr.resolveLackOfActivityViolation(source, destination);
//	    }
//	    else if (vt == ViolationType.splittingChoice)
//	    {
//		System.out.println("Violation of Rule : "+source.toString() +" leads to "+ destination.toString()+" is of type SPLITTING CHOICE");
//		compTree = cr.resolveSplittingChoiceViolation(source, destination);
//	    }
//	    else
//	    {
//		System.out.println("No violation to the Rule : "+source.toString() +" leads to "+ destination.toString());
//	    }
//	    if (compTree != null)
//	    {
//		System.out.println("Tree after resolving violation:");
//		compTree.print(System.out);
//	    }
//	    
//	    vt= cr.getViolationTypeOfLeadsTo(source, destination);
//	}while(vt != ViolationType.none);

//    nd[0].setCondition("true");
//	nd[0].setNodeType(com.pst.Node.NodeType.Sequence);
//	nd[1].setLabel("Go to checkout");
//	nd[1].setNodeType(com.pst.Node.NodeType.Activity);
//	nd[2].setLabel("Provide shipping address");
//	nd[2].setNodeType(com.pst.Node.NodeType.Activity);
//	
//	nd[3].setNodeType(com.pst.Node.NodeType.Parallel);
//	
//	nd[4].setLabel("Prepare goods");
//	nd[4].setNodeType(com.pst.Node.NodeType.Activity);
//	nd[5].setLabel("Send goods");
//	nd[5].setNodeType(com.pst.Node.NodeType.Activity);
//	
//	nd[6].setCondition("true");
//	nd[6].setNodeType(com.pst.Node.NodeType.Sequence);
//	nd[7].setLabel("Provide credit card data");
//	nd[7].setNodeType(com.pst.Node.NodeType.Activity);
//	nd[8].setLabel("Pay by credit card");
//	nd[8].setNodeType(com.pst.Node.NodeType.Activity);
//	
//	nd[9].setCondition("true");
//	nd[9].setNodeType(com.pst.Node.NodeType.Sequence);
//	
//	
//	com.pst.ProcessStructureTree pst = new com.pst.ProcessStructureTree(nd[0]);
//	pst.insertNode(nd[1], nd[0], 1);
//	pst.insertNode(nd[3], nd[0], 2);
//	pst.insertNode(nd[2], nd[0], 3);
//	pst.insertNode(nd[5], nd[0], 4);
//	pst.insertNodeBasic(nd[6], nd[3]);
//	pst.insertNodeBasic(nd[9], nd[3]);
//	pst.insertNode(nd[7], nd[9],1);
//	pst.insertNode(nd[8], nd[9],2);
//	
//	pst.insertNode(nd[4], nd[6],1);
//	
	
//	ProcessGraph test = new ProcessGraph();
////	FiniteStateMachine fsm;
////	fsm = new FiniteStateMachine();
//	test.loadModelFromRepository("69");
//	
////	PetriNetGenerator png = new PetriNetGenerator(test);
//	TemporalQueryGraph tq = new TemporalQueryGraph();
//	
//	GraphObject nd = new GraphObject(),nd1= new GraphObject(),nd2 = new GraphObject();
//	nd.setName("Open Correspondent Account");
//	nd.type = GraphObjectType.ACTIVITY;
//	
//	nd2.setName("@");
//	nd2.type = GraphObjectType.ACTIVITY;
//	
//	nd1.setName("Open Correspondent Account");
//	nd1.type = GraphObjectType.ACTIVITY;
//	
//	
//	
//	Path p = new Path(nd2,nd);
//	p.temporalTag = TemporalType.PRECEDES;
//	
//	
//	GraphObject nd3,nd4,nd5;
//	nd3 = new GraphObject();
//	nd3.setName("Rating");
//	nd3.type = GraphObjectType.DATAOBJECT;
//	
//	
//
//	
//	
//	nd5 = new GraphObject();
//	nd5.setName("Risk");
//	nd5.type = GraphObjectType.DATAOBJECT;
//	nd5.type2 ="low";
//	DataObject dob4 = new DataObject();
//	dob4.name = "Risk";
//	dob4.setState("low");
//	
//	
//	Association as4 = new Association(nd2,nd5);
//	as4.assType = AssociaitonType.Structural;
//	
//	tq.add(nd);
//	
//	tq.add(nd2);
//	
//	tq.add(dob4);
//	tq.add(as4);
//	
//	tq.add(p);
//	
//
//	
//	
//	
//	ComplianceViolationExplanator cve= new ComplianceViolationExplanator("69");
//	
//	List<QueryGraph> antiPatterns = cve.explainViolation(tq);
//	for(QueryGraph q :antiPatterns)
//	    q.print(System.out);
//	TemporalLogicQuerySolver tlqs = new TemporalLogicQuerySolver(bc,mc);
//	Map<String,String> result;
//	result = tlqs.resolveConditionalPrecedesQuery(tq, "69");
//	Iterator it = result.keySet().iterator();
//	while(it.hasNext())
//	{
//	    String key = (String) it.next();
//	    System.out.println("States of "+key +" are "+result.get(key));
//	}
//	png.generatePTNetFromProcessGraph();
//	png.getPetriNet().writeLoLANetFile("c://BPMNQ_Result/temp/model69_star.net");
//	Utilities.callLoLA(Utilities.LOLA_PATH, "c://BPMNQ_Result/temp/model69_star.net");
//
//	String fsmLolanetFilename = Utilities.makeTempfilePath("model69_star.graph");
//	String nuSmvFilename = Utilities.makeTempfilePath("lolaspec"+"69"+".smv");
//	try
//	{
//	    fsm = new FiniteStateMachine();
//	    log.debug("Loading the behavioral model...");
//	    fsm.loadStateFromLOLAStateFile(fsmLolanetFilename);
//	    log.debug("Generating the Kripke structure...");
//	    fsm.writeNuSMVSpecToFile(nuSmvFilename, "");
//	} catch (IOException e)
//	{
//	    log.error("unrecoverable error when calling external tools");
//	    //return RET_NET_DOESNT_COMPLY;
//	    System.err.println(e.getMessage());
//	}
//	
//	PetriNet pn = png.
//	FiniteStateMachine fsm = new FiniteStateMachine();
//	try
//	{
//	    fsm.loadStateFromLOLAStateFile("C:/BPMNQ_Result/temp/testMultipleDataConditionProduction2.graph");
//	    fsm.writeNuSMVSpecToFile("C:/BPMNQ_Result/temp/testMultipleDataConditionProduction2.smv", "");
//	} catch (IOException e)
//	{
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}
	
	
//	ProcessGraph p = new ProcessGraph();
//	p.loadModelFromRepository(65);
//	PetriNetGenerator png = new PetriNetGenerator(p);
//	png.generatePTNetFromProcessGraph();
//	//this.getClass().
//	png.getPetriNet().writeLoLANetFile("C:/BPMNQ_Result/temp/full_lolaspec165.net");
//	FiniteStateMachine fsm = new FiniteStateMachine();
//	try
//	{
//	    fsm.loadStateFromLOLAStateFile("C:\\BPMNQ_Result\\temp\\lolaspec63.graph");
//	    fsm.writeNuSMVSpecToFile("C:\\BPMNQ_Result\\temp\\lolaspec63.smv", "G(executed__1 -> F(executed__72))");
//	}
//	catch(IOException io)
//	{
//	    
//	}
//	System.out.println(Utilities.getParallelsOfActivity("ACT283"));
//	ProcessGraph p = new ProcessGraph();
//	p.loadModelFromRepository(61);
//	p.print(System.out);
//	p.addNode(new GraphObject(1,"",GraphObjectType.EVENT,"1"));
//	p.addNode(new GraphObject(2,"A",GraphObjectType.ACTIVITY,""));
//	p.addNode(new GraphObject(3,"B",GraphObjectType.ACTIVITY,""));
//	p.addNode(new GraphObject(4,"",GraphObjectType.EVENT,"3"));
//	p.addEdge(new GraphObject(1,"",GraphObjectType.EVENT,"1"), new GraphObject(2,"A",GraphObjectType.ACTIVITY,""));
//	p.addEdge(new GraphObject(2,"A",GraphObjectType.ACTIVITY,""), new GraphObject(3,"B",GraphObjectType.ACTIVITY,""));
//	p.addEdge(new GraphObject(3,"B",GraphObjectType.ACTIVITY,""),new GraphObject(4,"",GraphObjectType.EVENT,"3") );
//	p.addDataObject(new DataObject(1,"do1",""));
//	p.addDataObject(new DataObject(2,"do2",""));
////	p.addDataObject(new DataObject(3,"do2","state_5"));
//	p.addAssociation(new GraphObject(1,"do1",GraphObjectType.DATAOBJECT,"state_1"), new GraphObject(2,"A",GraphObjectType.ACTIVITY,""));
////	p.addAssociation(new GraphObject(1,"do1",GraphObjectType.DATAOBJECT,"state_1"), new GraphObject(3,"B",GraphObjectType.ACTIVITY,""));
////	p.addAssociation(new GraphObject(1,"do1",GraphObjectType.DATAOBJECT,"state_2"), new GraphObject(3,"B",GraphObjectType.ACTIVITY,""));
//	p.addAssociation(new GraphObject(2,"do2",GraphObjectType.DATAOBJECT,"state_5"), new GraphObject(3,"B",GraphObjectType.ACTIVITY,""));
//	p.addAssociation(new GraphObject(2,"A",GraphObjectType.ACTIVITY,""), new GraphObject(1,"do1",GraphObjectType.DATAOBJECT,"state_2"));
//	p.addAssociation(new GraphObject(2,"A",GraphObjectType.ACTIVITY,""), new GraphObject(1,"do1",GraphObjectType.DATAOBJECT,"state_1"));
//	
//	PetriNetGenerator pn = new PetriNetGenerator(p);
//	pn.generatePTNetFromProcessGraph();
//	pn.getPetriNet().print();
//	pn.getPetriNet().writeLoLANetFile("C://test.net");
//	FiniteStateMachine fsm = new FiniteStateMachine();
//	try
//	{
//	fsm.loadStateFromLOLAStateFile("C://test.graph");
//	fsm.writeNuSMVSpecToFile("c://test.nsmv","");
//	}
//	catch(IOException e)
//	{
//		
//	}
//	p.loadModelFromRepository(60);
//	PathBuilder pb = new PathBuilder();
//
//	GraphObject startElem, endElem;
//	startElem = new GraphObject();
//	endElem = new GraphObject();
//	startElem.setId(139);
//	startElem.setName("");
//	startElem.type= GraphObjectType.EVENT;
//	startElem.type2= "1";
//	endElem.setId(579);
//	endElem.setName("Open Correspondent Account");
//	endElem.type= GraphObjectType.ACTIVITY;
//	endElem.type2 = "";
//	
//	List<ProcessGraph> result = pb.buildPathsAcyclicShortest(  startElem,endElem, p);
////	if (result.size() > 0) result.remove(0);
//	for (ProcessGraph f : result)
//		System.out.println(f.nodes.toString());
//	
//	System.out.println(result.size());
//	

//	pb.buildAllPaths(p);
//	TemporalQueryGraph qry = new TemporalQueryGraph();
//	QueryGraphBuilderXML dd;
//	QueryProcessor wo = new QueryProcessor();
//	try
//	{
//	dd = new QueryGraphBuilderXML("C:\\Query.xml");
//	qry = dd.buildGraph();
//	qry.print();

//	}
//	catch(Exception e)
//	{
//	System.err.println(e.getMessage());
//	}
	// System.out.println(Utilities.getParallelsOfActivity("ACT40"));
	// Testing the calculation of transitive closure

//	ProcessGraph p = new ProcessGraph();

//	GraphObject s1,sp1,a,sp2,b,c,j1,d,j2,e1;
//	s1 = new GraphObject();
//	s1.type = GraphObject.GraphObjectType.EVENT;
//	s1.type2="1";
////	s1.setId(1);
//	s1.setName("Start event");
//
//	qry.add(s1);

//	sp1 = new GraphObject();
//	sp1.type = GraphObject.GraphObjectType.GATEWAY;
//	sp1.type2="AND SPLIT";
//	sp1.setId(2);
//	sp1.setName("AND SPLIT 1");

//	p.addNode(sp1);

//	a = new GraphObject();
//	a.type = GraphObject.GraphObjectType.ACTIVITY;
//	a.type2="";
////	a.setId(3);
//	a.setName("A");
//	qry.add(a);
////	p.addNode(a);
//
////	sp2 = new GraphObject();
////	sp2.type = GraphObject.GraphObjectType.GATEWAY;
////	sp2.type2="AND SPLIT";
////	sp2.setId(4);
////	sp2.setName("AND SPLIT 2");
//
////	//p.addNode(sp2);
//
//	b = new GraphObject();
//	b.type = GraphObject.GraphObjectType.ACTIVITY;
//	b.type2="";
////	b.setId(5);
//	b.setName("B");
//	qry.add(b);
////	p.addNode(b);
//
//	c = new GraphObject();
//	c.type = GraphObject.GraphObjectType.ACTIVITY;
//	c.type2="";
////	c.setId(6);
//	c.setName("C");
//	qry.add(c);
//	Path p1,p2,p3;
//	p1 = new Path(s1,a,"C",TemporalType.LEADS_TO);
//	p2 = new Path(a,b,"",TemporalType.LEADS_TO);
//	p3 = new Path(b,c,"D",TemporalType.PRECEDES);
//	qry.add(p1);
//	qry.add(p2);
//	qry.add(p3);
//	List<QueryGraph> result = qry.generateAntiPatternQueries();
//	for (QueryGraph q:result)
//	    q.print(System.out);
//	p.addNode(c);

//	j1 = new GraphObject();
//	j1.type = GraphObject.GraphObjectType.GATEWAY;
//	j1.type2="AND JOIN";
//	j1.setId(7);
//	j1.setName("AND JOIN 1");

//	p.addNode(j1);


//	d = new GraphObject();
//	d.type = GraphObject.GraphObjectType.ACTIVITY;
//	d.type2="";
//	d.setId(8);
//	d.setName("D");

//	p.addNode(d);

//	j2 = new GraphObject();
//	j2.type = GraphObject.GraphObjectType.GATEWAY;
//	j2.type2="AND JOIN";
//	j2.setId(9);
//	j2.setName("AND JOIN 2");

//	p.addNode(j2);


//	e1 = new GraphObject();
//	e1.type = GraphObject.GraphObjectType.EVENT;
//	e1.type2="3";
//	e1.setId(10);
//	e1.setName("End event");

//	p.addNode(e1);

//	p.addEdge(s1,sp1);
//	p.addEdge(sp1,a);
//	//p.addEdge(sp1,sp2);
//	p.addEdge(sp1,b);
//	p.addEdge(sp1,c);
//	p.addEdge(b,j1);
//	p.addEdge(c,j1);
//	p.addEdge(j1,d);
//	p.addEdge(d,j2);
//	p.addEdge(a,j2);
//	p.addEdge(j2, e1);
//	ProcessGraph rStar = Utilities.calculateTransitiveClosure(p);
//	System.out.println("Sequence transitive closure");
//	rStar.print();


//	ProcessGraph p2 = new ProcessGraph();

//	p2 .nodes.addAll(p.nodes);

//	p2.addEdge(a, sp2);
//	p2.addEdge(sp2,a);
//	p2.addEdge(b, c);
//	p2.addEdge(c,b);
//	p2.addEdge(a,d);
//	p2.addEdge(a,j1);
//	p2.addEdge(a,c);
//	p2.addEdge(a,b);
//	p2.addEdge(d,a);
//	p2.addEdge(j1,a);
//	p2.addEdge(c,a);
//	p2.addEdge(b, a);



	//rStar = Utilities.calculateTransitiveClosure(p2);

	//System.out.println("Parallel transitive closure");
	//rStar.print();



	/*			//wo.init();
    			//wo.loadModel(1);
    			ProcessGraph Paths;
    			GraphObject start,end;

    			start = new GraphObject();
    			start.id =13;
    			start.name = "Start";
    			start.type1 = "Event";
    			start.type2 = "";

    			end = new GraphObject();
    			end.id = 14;
    			end.name = "End 1";
    			end.type1 = "Event";
    			end.type2 = "";
    			//Vector<GraphObject> rslt = wo.getSuccessorsFromDB(start,1);
    			Paths = wo.findPathFromDB(start,end,9);
    			//System.out.println("Total Paths found:"+Paths.size());
    			//ProcessGraph x ;
    			//for (int j = 0; j < Paths.size();j++)
    			//{
    				//System.out.println("ProcessGraph "+(j+1));
    				//x = Paths.elementAt(j);
    				for (int i =0; i < Paths.nodes.size();i++)
    					System.out.println("Element "+ Paths.nodes.elementAt(i).name);
    				Paths.print();
    			//QueryGraph qry = new QueryGraph();
    			GraphObject x1,x2,x3,x4,x5,x6;
    			x1 = new GraphObject();
    			x1.name="Get approval from finance director";
    			x1.type1 = "Activity";
    			x1.type2 = "";
    			x1.id = 196;

    			x2 = new GraphObject();
    			x2.id = 0;
    			x2.name = "";
    			x2.type1 = "Event";
    			x2.type2 = "1";

    			x3 = new GraphObject();
    			x3.name = "C";
    			x3.type1 = "Activity";
    			x3.type2="";
    			x3.id = 27;

    			x4 = new GraphObject();
    			x4.name = "D";
    			x4.type1 = "Activity";
    			x4.type2="";
    			x4.id = 56;

    			x5 = new GraphObject();
    			x5.name="Prepare cheque for ANZ bank";
    			x5.type1 = "Activity";
    			x5.type2 = "";
    			x5.id = 195;

    			x6 = new GraphObject();
    			x6.name="Open Account";
    			x6.type1 = "Activity";
    			x6.type2 = "";
    			x6.id = 81;

    			prs = new ProcessGraph();
    			int modelnum = 40;
    			prs.loadFromDB(modelnum);
    			PetriNetGenerator png = new PetriNetGenerator(prs);
    			png.printProcess();
    			System.out.println("Starting generation of petri net...");
    			png.generatePTNetFromProcessGraph();

    			//png.printPetriNet();
    			png.writePetriNetToLOLAFile("C://completelolaspec"+modelnum+".net");
    			Utilities.callLoLA(Utilities.LOLA_PATH, "C://completelolaspec"+modelnum+".net");

    			ArrayList<GraphObject> kl;
    			kl = new ArrayList<GraphObject>(2);
    			kl = prs.getStartupNodes();
	 */	

//	QueryProcessor wo = new QueryProcessor();
//	GraphObject xx,xy,xz;
//	xx = new GraphObject();
//	xx.setName("Obtain Customer Info");
//	xx.type = GraphObjectType.ACTIVITY;
//	xx.setId(0);//93;

//	xy = new GraphObject();
//	xy.setName("Verify Customer ID");
//	xy.type = GraphObjectType.ACTIVITY;

//	xy.setId(0);//88;

//	xz = new GraphObject();
//	xz.setName("Open Account");
//	xz.type = GraphObjectType.ACTIVITY;
//	xz.setId(0);
//	/*			qry = new QueryGraph();
//	qry.addNode(xx);
//	qry.addNode(xy);
//	qry.addPath(xx, xy, "");
//	ETVSMLoader el = new ETVSMLoader();
//	el.loadModel(41);
//	el.loadModel(42);
//	el.loadModel(10);
//	el.loadModel(12);
//	el.loadModel(13);
//	el.loadModel(17);
//	el.loadModel(23);
//	el.loadModel(25);
//	el.loadModel(26);
//	el.loadModel(27);
//	el.loadModel(39);
//	el.loadModel(3);
//	el.loadModel(8);
//	qe.runSemanticQuery(qry);
//	*/			
//	TemporalQueryGraph tqr = new TemporalQueryGraph();
//	tqr.addNode(xx);
//	tqr.addNode(xy);
//	tqr.addNode(xz);
//	tqr.addPrecedesPath(xx, xz, "");
//	tqr.addPrecedesPath(xy,xz,"");
//	ArrayList<Integer> mdls = wo.findRelevantProcessModels(tqr);

//	System.out.println(tqr.toString());
//	for (int i = 0; i < mdls.size();i++)
//	{
//	ModelChecker mc = new ModelChecker (tqr);
//	int result = mc.checkModelWithReduction(mdls.get(i).intValue());
//	if (result == -3)
//	System.out.println("Petri Net generated from model "+ mdls.get(i).intValue() + " is unbounded. Inspection is not possible");
//	else if (result == -2)
//	System.out.println("Model "+ mdls.get(i).intValue() + " suffers from a deadlock. Inspection is not possible");
//	else if (result ==-1)
//	System.out.println("Query didnt find a match -> Does not comply :( ...");

//	else if (result == 1)
//	System.out.println("Complies :) ...");
//	else
//	System.out.println("Does not Comply :( ...");
//	}


//	xy = new GraphObject();
//	xy.setName("Verify Customer ID");
//	xy.type = GraphObjectType.ACTIVITY;

//	xy.setId(0);//88;

//	xz = new GraphObject();
//	xz.setName("Open Account");
//	xz.type = GraphObjectType.ACTIVITY;
//	xz.setId(0);
	/*			qry = new QueryGraph();
    			qry.addNode(xx);
    			qry.addNode(xy);
    			qry.addPath(xx, xy, "");
    			ETVSMLoader el = new ETVSMLoader();
    			el.loadModel(41);
    			el.loadModel(42);
    			el.loadModel(10);
    			el.loadModel(12);
    			el.loadModel(13);
    			el.loadModel(17);
    			el.loadModel(23);
    			el.loadModel(25);
    			el.loadModel(26);
    			el.loadModel(27);
    			el.loadModel(39);
    			el.loadModel(3);
    			el.loadModel(8);
    			qe.runSemanticQuery(qry);
	 */			
//	TemporalQueryGraph tqr = new TemporalQueryGraph();
//	tqr.addNode(xx);
//	tqr.addNode(xy);
//	tqr.addNode(xz);
//	tqr.addPrecedesPath(xx, xz, "");
//	tqr.addPrecedesPath(xy,xz,"");
//	List<Integer> mdls = wo.findRelevantProcessModels(tqr);

//	System.out.println(tqr.toString());
//	for (int i = 0; i < mdls.size();i++)
//	{
//	ModelChecker mc = new ModelChecker (tqr);
//	int result = mc.checkModelWithReduction(mdls.get(i).intValue());
//	if (result == ModelChecker.RET_NET_IS_UNBOUNDED)
//	System.out.println("Petri Net generated from model "+ mdls.get(i).intValue() + " is unbounded. Inspection is not possible");
//	else if (result == ModelChecker.RET_NET_HAS_DEADLOCK)
//	System.out.println("Model "+ mdls.get(i).intValue() + " suffers from a deadlock. Inspection is not possible");
//	else if (result == ModelChecker.RET_NO_NET_MATCHES)
//	System.out.println("Query didnt find a match -> Does not comply :( ...");

//	else if (result == ModelChecker.RET_NET_COMPLIES)
//	System.out.println("Complies :) ...");
//	else
//	System.out.println("Does not Comply :( ...");
//	}


	/*			kl.add(xy);
    			kl.add(xx);
    			GraphReducer gr = new GraphReducer(prs,kl);
    			png = new PetriNetGenerator(gr.getReducedGraph());
    			png.printProcess();
    			png.generatePTNetFromProcessGraph();
    			png.writePetriNetToLOLAFile("C://reducedlolaspec"+modelnum+".net");
    			// call lola to generate the graph file
    			Utilities.callLoLA(Utilities.LOLA_PATH, "c:\\reducedlolaspec"+modelnum+".net");

    			// now lola terminated and generated the graph file
    			// we read the graph file and generate the smv input file
    			FiniteStateMachine fsm = new FiniteStateMachine();
    			fsm.loadStateFromLOLAStateFile("C:\\reducedlolaspec"+modelnum+".graph");
    			fsm.print();
    			fsm.writeNuSMVSpecToFile("c:\\reducedlolaspec"+modelnum+".smv");

    			ArrayList<String> rslt = Utilities.callNuSMV(Utilities.NUSMV_PATH,"c:\\reducedlolaspec"+modelnum+".smv" );


    		     BufferedReader err = 
    	               new BufferedReader (  
    	                     new InputStreamReader ( NuSMVProc.getErrorStream (  )  )  ) ; 
    	     while  (  ( currentLine = err.readLine (  )  )  != null )  
    	       System.out.println ( currentLine ) ; 


    			System.out.println("Generation of petri net completed...");

    			FiniteStateMachine fsm = new FiniteStateMachine();
    			fsm.loadStateFromLOLAStateFile("C:\\reducedlolaspec"+modelnum+".graph");
    			fsm.print();
    			fsm.writeNuSMVSpecToFile("c:\\reducedlolaspec"+modelnum+".smv");

    			fsm.loadStateFromLOLAStateFile("C://lolaspec"+modelnum+".graph");
    			fsm.writeNuSMVSpecToFile("c:\\lolaspec"+modelnum+".smv");
    //			prs.addNode(x1);
    //			prs.addNode(x2);
    //			prs.addNode(x3);
    //			prs.addNode(x4);
    //			prs.addNode(x5);
    //			prs.addNode(x6);
    //			prs.addEdge(x1, x2);
    //			prs.addEdge(x2, x3);
    //			prs.addEdge(x3, x4);
    //			prs.addEdge(x4, x5);
    //			prs.addEdge(x5, x6);
    			GraphReducer gr;
    			ArrayList<GraphObject> kl;
    			kl = new ArrayList<GraphObject>(2);

    			kl.add(x1);
    //			kl.add(x6);
    			kl.add(x5);

    			//prs.print();

    			qry.addNode(x1);
    			qry.addNode(x2);
    	//		qry.addNode(x3);
    	//		qry.addNode(x6);
    			qry.addNode(x5);
    //			qry.addEdge(x1, x4);
    //			qry.addEdge(x4, x2);
    //			qry.addNegativeEdge(x4,x5);
    			qry.addPath(x2, x1,"");
    			qry.addPath(x2, x5,"");
    //			//qry.addPath(x4, x2,"");
    //			System.out.println("Temporal Expression: " + qry.generateTemporalExpression());
    	//		
    	//		//qry.addEdge(x3, x1);
    	//		//qry.addEdge(x2, x3);
    	//		//qry.addPath(x1, x3);
    	//		//qry.addPath(x4, x2);
    	//		//qry.addPath(x1, x2);
    	//		//qry.addNegativePath(x1, x3);
    	//		//ProcessGraph Paths;
    	//		//Paths = wo.findPathFromDB(x5, x4, 12);
    	//		//Paths.print();
    	//		//wo.processQuery(qry);
    			prs = new ProcessGraph();
    			prs = wo.runQueryAgainstModel(qry, 26);
    			System.out.println("Original graph");
    			prs.print();
    			kl.addAll(prs.getStartupNodes());
    			gr = new GraphReducer(prs,kl);
    			System.out.println("Reduced graph");
    			gr.getReducedGraph().print();
    	//		//}
    	//		
    	//		for(int i=1;i <=13;i++)
    	//		{
    	//			//wo.checkAndJoinLoopDeadlock(i);
    	//			//wo.checkDirectXORSplitANDJoinDeadlock(i);
    	//		}
    			//wo.processQuery(DeadlockDetector.checkAndJoinLoopDeadlock());

    			//wo.processQuery(DeadlockDetector.checkDirectXORSplitANDJoinDeadlock());
    			//wo.processQuery(DeadlockDetector.checkIndirectXORSplitANDJoinDeadlock());
    			//Utilities.buildAllPathsOfModel(1);
    //			ProcessGraph p = new ProcessGraph();
    //			p.loadFromDB(6);
    //			PathAnalyzer pa = new PathAnalyzer(p);
    //			pa.distributeTokens();
    //			pa.print();
	 */

///////////////////////////// END OF OLD PARTS FROM DO THE DEFAULT CASE ///////////////////////////////////
}