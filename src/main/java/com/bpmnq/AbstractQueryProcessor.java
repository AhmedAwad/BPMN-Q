package com.bpmnq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import com.bpmnq.GraphObject.GraphObjectType;
//import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;






public abstract class AbstractQueryProcessor implements QueryProcessor
{
    protected ProcessGraph currentProcess;
    public boolean stopAtFirstMatch = false;
    public boolean includeEnclosingAndSplits = false;
    public boolean allowGenericShapeToEvaluateToNone=false;
    public enum ProcessorCommand {Query, Check, SemanticQuery, ComplianceQuery, ComplianceCheck, ComplianceQueryWithViolationExplanation,ComplianceCheckWithViolationExplanation};
    public ProcessorCommand procCmd;
    //  public boolean isMultiQueryMode = false;
    //  public int matchesCounter =0;

    protected final List<Match> matches = new ArrayList<Match>();
    // TODO remove static qualifier from these fields!!
    protected List<QueryGraph> finalRefinements = new ArrayList<QueryGraph>();
    protected List<QueryGraph> intermediateRefinements = new ArrayList<QueryGraph>();
    protected PrintWriter answerWriter;

    

    protected abstract void resolveGenericShape(String modelID);
    protected abstract void resolveGenericSplit(String modelID);
    protected abstract void resolveGenericJoin(String modelID);
//  protected abstract boolean checkNegativePathFromDB(GraphObject startElem, GraphObject endElem, int ModelID);
    protected abstract void resolveAnonymousActivities(String modelID);
    protected abstract boolean resolvePaths(QueryGraph query, String modelID);
    protected abstract boolean checkNegativePaths(QueryGraph query, String modelID);
    protected abstract boolean resolveConcreteNodeID(QueryGraph query, String modelID);
    protected abstract void resolveEventNode(String modelID);
    protected abstract void resolveGateWayNode(String modelID);
    protected abstract boolean checkNegativeEdges(QueryGraph qry, String modelID);
    protected abstract boolean resolveConcreteDataObjectID(QueryGraph qry, String modelID);
    protected abstract void resolveVariableDataObjects(String modelID);
    protected long extraOverhead=0;
    public long GetExtraOverheadTime()
    {
	return extraOverhead;
    }
//    protected abstract ProcessGraph updateSequenceFlowConditions(ProcessGraph result, ProcessGraph matchingProcess);
    protected Logger log = Logger.getLogger(AbstractQueryProcessor.class);
    public AbstractQueryProcessor(PrintWriter answer) {
	this.answerWriter = answer;
    }
    
    public abstract List<String> findRelevantProcessModels(QueryGraph query) throws IOException;
//    protected abstract void resolveUndirectedAssociations(int modelID, Path p);
    

    /**
     * Produces a print-out of all passed query graphs into a string
     * 
     * @param qGraphs
     *                A list of query graphs that shall be printed in a readable
     *                form
     * @return a string containing the print-out of all query graphs
     */
    protected String printGraphlist(List<QueryGraph> qGraphs)
    {
	ByteArrayOutputStream res = new ByteArrayOutputStream();
	PrintStream resPrinter = new PrintStream(res);
	for (QueryGraph queryGraph : qGraphs)
	{
	    queryGraph.print(resPrinter);
	    resPrinter.print('\n');
	}
	
	resPrinter.close();
	return res.toString();
	
    }
    private void applyQueryDirectives(QueryProcessorDirective d)
    {
	if (d == null)
	    restoreDefaultSettings();
	else
	{
	    this.stopAtFirstMatch = d.stopAtFirstMatch;
	    this.allowGenericShapeToEvaluateToNone = d.allowGenericShapeToEvaluateToNone;
	    this.includeEnclosingAndSplits = d.includeEnclosingANDSplit;
	}
    }
    
    protected ProcessGraph updateSequenceFlowConditions(ProcessGraph result, ProcessGraph matchingProcess)
    {
	for (SequenceFlow ed : result.edges)
	{
	    for (SequenceFlow ed2: matchingProcess.edges)
		// do not use the equal method here
		if (ed.getSourceGraphObject().equals(ed2.getSourceGraphObject()) && ed.getDestinationGraphObject().equals(ed2.getDestinationGraphObject()))
		    ed.arcCondition = ed2.arcCondition;
	}
	return result;
    }
    public boolean testQueryAgainstModel(QueryGraph rslt, String modelID, 
	    ProcessGraph resultGraph)
    {
	// Added on 19.08.09 to give more flexibility with the query processing
	applyQueryDirectives(rslt.getProcessorDirectives());
	resultGraph = null;
	this.intermediateRefinements.clear();
	this.finalRefinements.clear();
	// things should go in this order, to reduce cost from step to the next
	log.info("Testing Query against Model " + modelID);
	
	if (!resolveConcreteNodeID(rslt, modelID))
	    return false;
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	
	if (!resolveConcreteDataObjectID(rslt, modelID))
	    return false;
	
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	
//	this.intermediateRefinements.add(rslt);
	
	resolveEventNode(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
//	log.debug("refined graphs before resolveGatewayNode:\n" + printGraphlist(intermediateRefinements));
	System.out.println("refined graphs before resolveGatewayNode: "+intermediateRefinements.size());
	resolveGateWayNode(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
//	log.debug("refined graphs after resolveGatewayNode:\n" + printGraphlist(intermediateRefinements));
	System.out.println("refined graphs after resolveGatewayNode: "+intermediateRefinements.size());
	resolveAnonymousActivities(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	// added for testing an enhanced version of resolve generic shapes
	resolveGenericSplit(modelID);
	System.out.println("refined graphs after resolveGenericShape: "+intermediateRefinements.size());
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	resolveGenericJoin(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	
	for (QueryGraph qg : this.intermediateRefinements)
	    qg.establishCommonBinding();

	resolveGenericShape(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	resolveVariableDataObjects(modelID);
//	 Extra step added on 18.11.2009 to handle resolution of anonymous activities and dataobjects
	for (QueryGraph q : this.finalRefinements)
	{
	    if (validateDataObjectAssociations(q))
		this.intermediateRefinements.add(q);
	}
	// Filter the queries for unique ones only
//	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	if (intermediateRefinements.size() > 0)
	{
	    //this.finalRefinements.add(intermediateRefinements.remove(0));
	    for (QueryGraph aa : intermediateRefinements)
	    {
		// check edges
		boolean add = checkUnresolvedEdges(aa);
		
		if (add)
		    finalRefinements.add(aa);
	    }
	}
	log.info("Total number of resolved query graphs for model " + modelID + " is " + this.finalRefinements.size());

	ProcessGraph matchingGraphtemp = new ProcessGraph();
	matchingGraphtemp.modelURI = modelID;
	boolean matched = false;

	int modelCount = 1;
	for (QueryGraph refinedQGraph : this.finalRefinements)
	{
	    log.info("Checking query resolution number:" + modelCount++);
	    boolean solution2 = checkNegativeEdges(refinedQGraph, modelID);
	    if (solution2)
		solution2 = checkNegativePaths(refinedQGraph, modelID);
	    if (solution2)
		solution2 = resolvePaths(refinedQGraph, modelID);
	    if (solution2)
	    {
		matched = true;
		log.info("Match Found Model number " + modelID);

		// add the refined query graph which matches to matchingGraphTemp
		for (int y = 0 ; y < refinedQGraph.nodes.size(); y++)
		    matchingGraphtemp.add(refinedQGraph.nodes.get(y));
		for (int y = 0; y < refinedQGraph.edges.size(); y++)
		    matchingGraphtemp.union(refinedQGraph.edges.get(y));
	    }
	    for(DataObject dob : refinedQGraph.dataObjs)
		matchingGraphtemp.add(dob);
	    for(Association ass : refinedQGraph.associations)
		matchingGraphtemp.add(ass);
	    updateSequenceFlowConditions(matchingGraphtemp, currentProcess);
	    for (String s: refinedQGraph.getInfoLogs())
		log.info(s);
	    for (String s: refinedQGraph.getErrorLogs())
		log.error(s);
	    if (stopAtFirstMatch && matched)
		break;
	}
	
	if (matched) 
	{
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    PrintStream matchPrinter = new PrintStream(bos);
	    matchingGraphtemp.print(matchPrinter);
	    log.info("Matching part of model " + matchingGraphtemp.modelURI + " looks as follows:\n" 
		    + bos.toString());
	    answerWriter.println("<query-result>");
	    matchingGraphtemp.exportXML(answerWriter);
	    answerWriter.println("</query-result>");
	    resultGraph = new ProcessGraph();
	    resultGraph.modelURI = matchingGraphtemp.modelURI;
	    resultGraph.nodes.addAll(matchingGraphtemp.nodes);
	    resultGraph.edges.addAll(matchingGraphtemp.edges);
	    resultGraph.dataObjs.addAll(matchingGraphtemp.dataObjs);
	    resultGraph.associations.addAll(matchingGraphtemp.associations);
	} 

	return matched;
    }
    private boolean checkUnresolvedEdges(QueryGraph aa)
    {
	boolean add = true;
	for (SequenceFlow s : aa.edges)
	{
	    if (s.frmActivity != null && s.frmActivity.actID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.frmGateWay != null && s.frmGateWay.gateID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.frmEvent != null && s.frmEvent.eventID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toActivity != null && s.toActivity.actID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toGateWay != null && s.toGateWay.gateID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toEvent != null && s.toEvent.eventID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    
	}
	for (SequenceFlow s : aa.negativeEdges)
	{
	    if (s.frmActivity != null && s.frmActivity.actID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.frmGateWay != null && s.frmGateWay.gateID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.frmEvent != null && s.frmEvent.eventID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toActivity != null && s.toActivity.actID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toGateWay != null && s.toGateWay.gateID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toEvent != null && s.toEvent.eventID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    
	}
	for (Path s : aa.paths)
	{
	    if (s.frmActivity != null && s.frmActivity.actID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.frmGateWay != null && s.frmGateWay.gateID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.frmEvent != null && s.frmEvent.eventID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toActivity != null && s.toActivity.actID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toGateWay != null && s.toGateWay.gateID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toEvent != null && s.toEvent.eventID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    
	}
	for (SequenceFlow s : aa.negativePaths)
	{
	    if (s.frmActivity != null && s.frmActivity.actID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.frmGateWay != null && s.frmGateWay.gateID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.frmEvent != null && s.frmEvent.eventID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toActivity != null && s.toActivity.actID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toGateWay != null && s.toGateWay.gateID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    if (s.toEvent != null && s.toEvent.eventID.startsWith("-"))
	    {
		add = false;
		break;
	    }
	    
	}
	return add;
    }
    public ProcessGraph runQueryAgainstModel(QueryGraph q, ProcessGraph p)
    {
	// TODO Auto-generated method stub
	currentProcess = (ProcessGraph) p.clone();
	return runQueryAgainstModel(q, currentProcess.modelURI);
    }
    public ProcessGraph runQueryAgainstModel(QueryGraph rslt, String modelID)
    {
	applyQueryDirectives(rslt.getProcessorDirectives());
	this.intermediateRefinements.clear();
	this.finalRefinements.clear();
	ProcessGraph tmp = new ProcessGraph();
	tmp.modelURI = modelID;
	boolean solution2;
	// things should go in this order, to reduce cost from step to the next
	if (!resolveConcreteNodeID(rslt, modelID))
		return tmp;
	// This step added on 02.09.2009 to handle multiple occurrence of nodes within a model
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	
	if (!resolveConcreteDataObjectID(rslt, modelID))
	    return tmp;
//	this.intermediateRefinements.add(rslt);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	
	resolveEventNode(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	resolveGateWayNode(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	
	resolveAnonymousActivities(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	
	
	// added for testing an enhanced version of resolve generic shapes
	resolveGenericSplit(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	resolveGenericJoin(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	
//	this step is added on 28 july 2008
	
	for (QueryGraph qg : this.intermediateRefinements)
	    qg.establishCommonBinding();

	resolveGenericShape(modelID);
	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	resolveVariableDataObjects(modelID);
//	 Extra step added on 18.11.2009 to handle resolution of anonymous activities and dataobjects
	for (QueryGraph q : this.finalRefinements)
	{
	    if (validateDataObjectAssociations(q))
		this.intermediateRefinements.add(q);
	}
//	 Filter the queries for unique ones only
//	this.intermediateRefinements.addAll(this.finalRefinements);
	this.finalRefinements.clear();
	if (intermediateRefinements.size() > 0)
	{
	    //this.finalRefinements.add(intermediateRefinements.remove(0));
	    for (QueryGraph aa : intermediateRefinements)
	    {
		// check edges
		boolean add = checkUnresolvedEdges(aa);
		
		if (add)
		    finalRefinements.add(aa);
	    }
	}
	log.info("Total number of resolved query graphs for model " + modelID + " are " + this.finalRefinements.size());
	tmp = new ProcessGraph();

	// Optimization step added on 7th July 2007
	int sz72 = this.finalRefinements.size();
	for(int m =0; m < sz72;m++)
	{
	    log.info("Check query resolution number: " + (m+1));
	    solution2 = checkNegativeEdges(this.finalRefinements.get(m),modelID);
	    if (solution2)
		solution2 = checkNegativePaths(this.finalRefinements.get(m),modelID);
	    if (solution2)
		solution2 = resolvePaths(this.finalRefinements.get(m),modelID);
	    if (solution2)
	    {

		log.info("Match found model number " + modelID);

		for (int y = 0 ; y < this.finalRefinements.get(m).nodes.size();y++)
		    tmp.add(this.finalRefinements.get(m).nodes.get(y));
		for (int y = 0; y < this.finalRefinements.get(m).edges.size();y++)
		    tmp.union(this.finalRefinements.get(m).edges.get(y));
		
		for (int y = 0 ; y < this.finalRefinements.get(m).dataObjs.size();y++)
		    tmp.add(this.finalRefinements.get(m).dataObjs.get(y));
		for (int y = 0; y < this.finalRefinements.get(m).associations.size();y++)
		    tmp.add(this.finalRefinements.get(m).associations.get(y));
	    }
	}
	updateSequenceFlowConditions(tmp, currentProcess);
	return tmp;
    }
    protected boolean validateDataObjectAssociations(QueryGraph query)
    {
	for (DataObject dob : query.dataObjs)
	{
	    List<GraphObject> procActivities = currentProcess.getReadingActivities(dob,(dob.getState().startsWith("?") || dob.getState().startsWith("@") ) ? "" : dob.getState());
	    List<GraphObject> queryActivities = query.getReadingActivities(dob,(dob.getState().startsWith("?") || dob.getState().startsWith("@") ) ? "" : dob.getState());
	    if (!procActivities.containsAll(queryActivities))
		return false;
	    procActivities = currentProcess.getUpdatingActivities(dob,(dob.getState().startsWith("?") || dob.getState().startsWith("@") ) ? "" : dob.getState());
	    queryActivities = query.getUpdatingActivities(dob,(dob.getState().startsWith("?") || dob.getState().startsWith("@") ) ? "" : dob.getState());
	    if (!procActivities.containsAll(queryActivities))
		return false;

	}
	return true;
    }
    public List<String> processQuery(QueryGraph qry)
    {
	applyQueryDirectives(qry.getProcessorDirectives());
//	Map<Integer, ProcessGraph> matchedModels = new HashMap<Integer, ProcessGraph>();
	List<String> matchedModels = new ArrayList<String>();
	List<String> filterResult;
	try
	{
	    long startTime = System.currentTimeMillis();
	    filterResult = findRelevantProcessModels(qry);
	    this.extraOverhead+= (System.currentTimeMillis() - startTime);
	} catch (IOException e)
	{
	    log.error("Database error, Cannot retrieve model numbers. Cannot continue - Aborting!", e);
	    return matchedModels;
	}

	answerWriter.println("<query-result>");
	for (String procModelNo : filterResult)
	{
	    this.intermediateRefinements.clear();
	    this.finalRefinements.clear();
	    QueryGraph rslt = (QueryGraph)qry.clone();
	    //log.debug("Testing Model:" + procModelNo);
	    ProcessGraph matchingProcGraph = null;
	    if (testQueryAgainstModel(rslt, procModelNo.toString(), matchingProcGraph))
		matchedModels.add(procModelNo);
//		matchedModels.put(procModelNo, matchingProcGraph);
		
	}
	answerWriter.println("</query-result>");

	return matchedModels;
    }

    public List<Match> processMultiQuery(QueryGraph qry)
    {
	// Added on 19.08.209 to give more flexibility to the query processing
	applyQueryDirectives(qry.getProcessorDirectives());
	List<String> filterResult;
	try
	{
	    filterResult = findRelevantProcessModels(qry);
	} catch (IOException e)
	{
	    log.error("Database error, Cannot retrieve model numbers. Cannot continue - Aborting!", e);
	    return new ArrayList<Match>(0);
	}

	matches.clear();

	for (String modelNo : filterResult)
	{
	    this.intermediateRefinements.clear();
	    this.finalRefinements.clear();
	    QueryGraph rslt = (QueryGraph)qry.clone();
	    ProcessGraph pGraph = runQueryAgainstModel(rslt, modelNo.toString());
	    if (pGraph.nodes.size() > 0)
	    {
		Match mc = new Match();
		mc.matchGraph = pGraph;
		mc.matchGraph.modelURI = modelNo;
		mc.queryMatchRate = qry.getMatchRate();
		mc.matchedQuery = qry;
		matches.add(mc);

	    }
	}
	return matches;

    }
    public void printMessage(String msg)
    {
	this.answerWriter.println(msg);
    }
    public void restoreDefaultSettings()
    {
	this.stopAtFirstMatch = false;
	this.allowGenericShapeToEvaluateToNone = true;
	this.includeEnclosingAndSplits = false;
    }
    public ProcessGraph getInspectedProcess()
    {
	return (ProcessGraph) currentProcess.clone();
    }
    protected List<DataObject> getIncomingNonAnonymousDataObjects(QueryGraph q, GraphObject nd)
    {
	List<DataObject> result = new ArrayList<DataObject>();
	List<Association> ass = q.getIncomingAssociation(nd);
	for (Association as : ass)
	{
	    if (as.frmDataObject != null)
		if (as.frmDataObject.isResolved())
		    result.add(as.frmDataObject);
	}
	return result;
    }
    protected List<DataObject> getOutgoingNonAnonymousDataObjects(QueryGraph q, GraphObject nd)
    {
	List<DataObject> result = new ArrayList<DataObject>();
	List<Association> ass = q.getOutgoingAssociation(nd);
	for (Association as : ass)
	{
	    if (as.toDataObject != null)
		if (as.toDataObject.isResolved())
		    result.add(as.toDataObject);
	}
	return result;
    }
    protected boolean containsUnresolvedNode(List<GraphObject> nds)
    {
	for (GraphObject nd : nds)
	    if (!nd.isResolved()) return true;
	return false;
    }
    
    protected QueryGraph handleRefineToNode(QueryGraph query, GraphObject currentNode, GraphObject intersectionNode)
    {
	QueryGraph refinement = (QueryGraph)query.clone();

	refinement.remove(currentNode);
	refinement.addInfoLog("Generic Node "+ currentNode.getName() + " has been bound to " + intersectionNode.toString() );
	// Added on 25.8.2010
	intersectionNode.setBoundQueryObjectID(currentNode.getID());
	refinement.add(intersectionNode);
	if (intersectionNode.type == GraphObjectType.ACTIVITY)
	    refinement.forbiddenActivityIDs.append(", " + intersectionNode.getID()); 
	else if (intersectionNode.type == GraphObjectType.GATEWAY)
	    refinement.forbiddenGatewayIDs.append(", " + intersectionNode.getID());
	else if (intersectionNode.type == GraphObjectType.EVENT)
	    refinement.forbiddenEventIDs.append(", " + intersectionNode.getID());

	refinement.updateNegativeEdgesWithDestination(currentNode, intersectionNode);
	refinement.updateNegativePathsWithDestination(currentNode, intersectionNode);
	refinement.updateNegativeEdgesWithSource(currentNode, intersectionNode);
	refinement.updateNegativePathsWithSource(currentNode, intersectionNode);

	refinement.updateEdgesWithDestination(currentNode, intersectionNode);
	refinement.updateEdgesWithSource(currentNode, intersectionNode);

	refinement.updatePathsWithDestination(currentNode, intersectionNode);
	refinement.updatePathsWithSource(currentNode, intersectionNode);

//	added on 9th of July 2008 
	refinement.updateExcludeExpression(currentNode.getName(), intersectionNode.getID());

	refinement.updateAssociationsFromFlowObject(currentNode, intersectionNode);
	refinement.updateAssociationsToFlowObject(currentNode, intersectionNode);

	refinement.updateUnDirectedAssociationPathSource(currentNode,intersectionNode);
	refinement.updateUnDirectedAssociationPathDestination(currentNode,intersectionNode);
	return refinement;
    }

    protected QueryGraph handleRefineToNone(QueryGraph query, GraphObject currentNode)
    {
	QueryGraph refinement = (QueryGraph)query.clone();
	refinement.remove(currentNode);
//	All negative edges and paths incoming and outgoing must be removed
	refinement.removeNegativeEdgesWithDestination(currentNode);
	refinement.removeNegativeEdgesWithSource(currentNode);
	refinement.removeNegativePathsWithDestination(currentNode);
	refinement.removeNegativePathsWithSource(currentNode);
	// TODO : how to handle the undirect association in case of removing a path
	refinement.removeAssociationFromFlowObject(currentNode);
	refinement.removeAssociationToFlowObject(currentNode);

	List<GraphObject> epreds,esuccs,ppreds,psuccs;

	epreds = refinement.getPredecessorsFromQueryGraph(currentNode);
	ppreds = refinement.getPathPredecessorsFromQueryGraph(currentNode);

	esuccs = refinement.getSuccessorsFromGraph(currentNode);
	psuccs = refinement.getPathSuccessorsFromQueryGraph(currentNode);

	refinement.removeEdgesWithDestination(currentNode);
	refinement.removeEdgesWithSource(currentNode);

	for (GraphObject nd1 : epreds)
	{
	    for (GraphObject nd2 : esuccs)
	    {
		refinement.addEdge(nd1, nd2);
	    }
	    for (GraphObject nd3 : psuccs)
	    {
		refinement.add(new Path(nd1, nd3, refinement.getPathExcludeStatement(currentNode, nd3),refinement.getPathLabel(currentNode, nd3)));
	    }
	}

	for (GraphObject nd1 : ppreds)
	{
	    for (GraphObject nd2 : esuccs)
	    {
		refinement.add(new Path(nd1, nd2,refinement.getPathExcludeStatement(nd1, currentNode),refinement.getPathLabel(nd1,currentNode)));
	    }
	    for (GraphObject nd3 : psuccs)
	    {
	    	refinement.add(new Path(nd1, nd3, refinement.getPathExcludeStatement(nd1, currentNode)+","+ refinement.getPathExcludeStatement(currentNode, nd3),refinement.getPathLabel(nd1,currentNode)));
	    	refinement.updateExcludeExpression(refinement.getPathLabel(currentNode,nd3), refinement.getPathLabel(nd1,currentNode));
	    }
	}

	refinement.removePathsWithDestination(currentNode);
	refinement.removePathsWithSource(currentNode);
	refinement.updateExcludeExpression(currentNode.getName(), "");
	return refinement;

    }

}
