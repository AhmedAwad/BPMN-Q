package com.bpmnq;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.bpmnq.GraphObject.GraphObjectType;

public class ProcessGraphAuxiliaryData
{
    private ProcessGraphAuxiliaryDataGenerator pgadg;
    private ProcessGraph theProcess;
    private Map<GraphObject,List<GraphObject>> transitiveClosure;
    private Map<GraphObject,List<GraphObject>> transitiveClosureInverse;
    private Map<SequenceFlowTransitiveClosure,Integer> distances;
    private Map<Integer, List<SequenceFlowTransitiveClosure>> distances2;
    private Map<String,List<GraphObject>> gatewaysByType;
    private Map<GraphObject.GraphObjectType, List<GraphObject>> nodesByType;
    private Map<String,List<GraphObject>> eventsByType;
    private Map<GraphObject, Integer> farthestDistance;
    private Map<GraphObject, Integer> farthestDistanceInverse;
    private long processLoadingTime=0;
    public List<GraphObject> getNodesWithDistanceFromSource(GraphObject src, int distance)
    {
	List<GraphObject> result = new ArrayList<GraphObject>(10);
	List<SequenceFlowTransitiveClosure> sfs = distances2.get(new Integer(distance));
	for (SequenceFlowTransitiveClosure sftc : sfs)
	{
	    if (sftc.getSourceGraphObject().equals(src))
	    {
		result.add(sftc.getDestinationGraphObject());
	    }
	}
	return result;
    }
    public GraphObject getNodeByID(String id)
    {
	return theProcess.getNodeByID(id);
    }
    public List<GraphObject> getNodesWithDistanceFromDestination(GraphObject dst, int distance)
    {
	List<GraphObject> result = new ArrayList<GraphObject>(10);
	List<SequenceFlowTransitiveClosure> sfs = distances2.get(new Integer(distance));
	for (SequenceFlowTransitiveClosure sftc : sfs)
	{
	    if (sftc.getDestinationGraphObject().equals(dst))
	    {
		if (!result.contains(sftc.getSourceGraphObject()))
		    result.add(sftc.getSourceGraphObject());
	    }
	}
	return result;
    }
    public List<GraphObject> getFarthestNodesFromSource(GraphObject src)
    {
	Integer d = farthestDistance.get(src);
	
	return getNodesWithDistanceFromSource(src, d.intValue());
    }
    public List<GraphObject> getFarthestNodesFromDestination(GraphObject dst)
    {
	Integer d = farthestDistanceInverse.get(dst);
	
	return getNodesWithDistanceFromDestination(dst, d.intValue());
	
    }
    public List<GraphObject> getPredecessors(GraphObject dst)
    {
	return getNodesWithDistanceFromDestination(dst, 1);
    }
    public List<GraphObject> getSuccessors(GraphObject src)
    {
	return getNodesWithDistanceFromSource(src, 1);
    }
    public int getDistance(GraphObject src, GraphObject dst)
    {
	SequenceFlowTransitiveClosure sftc = new SequenceFlowTransitiveClosure(src, dst);
	
	Integer d = distances.get(sftc);
	if (d == null)
	    d = new Integer(0);
	return d.intValue();
    }
    public List<GraphObject> getFarthestNodesFromSourceByType(GraphObject src, GraphObjectType t, String type2)
    {
	List<SequenceFlowTransitiveClosure> sss = new ArrayList<SequenceFlowTransitiveClosure>(distances.keySet().size());
	int maxDistanceType = 0;
	for (SequenceFlowTransitiveClosure sftc : distances.keySet())
	{
	    if (sftc.getSourceGraphObject().equals(src) && sftc.getDestinationGraphObject().type==t && sftc.getDestinationGraphObject().type2.equals(type2))
	    {
		sss.add(sftc);
		if (sftc.getDistance() > maxDistanceType)
		    maxDistanceType = sftc.getDistance();
	    }
	}
	List<GraphObject> result = new ArrayList<GraphObject>(sss.size());
	
	
	for(SequenceFlowTransitiveClosure sftc: sss)
	{
	    if (sftc.getDistance()== maxDistanceType)
		result.add(sftc.getDestinationGraphObject());
	}
	
	
	return result;
    }
    public ProcessGraphAuxiliaryData(ProcessGraph p)
    {
	theProcess = (ProcessGraph) p.clone();
	if (Utilities.isConnectionOpen())
	{
	    
	    
	    
	    if (!isAuxiliaryDataMaterialized(theProcess.modelURI))
	    {
		pgadg = new ProcessGraphAuxiliaryDataGenerator(theProcess, true);
		loadAuxiliaryData(pgadg.getTransitiveClosure());
	    }
	    else // auxiliary data is present
	    {
		loadAuxiliaryDataFromDB(theProcess.modelURI);
	    }
	}
	else // there is no database connection
	{
	    
	    // of course no auxiliary data is materialized
	    pgadg = new ProcessGraphAuxiliaryDataGenerator(theProcess, false);
	    loadAuxiliaryData(pgadg.getTransitiveClosure());
	}
	
    }
    public ProcessGraphAuxiliaryData(String processURI)
    {
	theProcess = new ProcessGraph();
	// check whether we have access to the database
	long startloading;
	if (Utilities.isConnectionOpen())
	{
	    startloading= System.currentTimeMillis();
	    
	    theProcess.loadModelFromOryxRepository(processURI);
	    if (theProcess.nodes.size()==0)//maybe the process is not yet loaded to oryx db repository
	    {
		theProcess.loadFromOryx(processURI);// we have to load directly from Oryx
		theProcess.saveToDB();

	    }
	    processLoadingTime = System.currentTimeMillis() - startloading;
	    if (!isAuxiliaryDataMaterialized(processURI))
	    {
		pgadg = new ProcessGraphAuxiliaryDataGenerator(theProcess, true);
		loadAuxiliaryData(pgadg.getTransitiveClosure());
	    }
	    else // auxiliary data is present
	    {
		loadAuxiliaryDataFromDB(processURI);
	    }
	}
	else // there is no database connection
	{
	    startloading= System.currentTimeMillis();
	    theProcess.loadFromOryx(processURI);
	    processLoadingTime = System.currentTimeMillis() - startloading;
	    // of course no auxiliary data is materialized
	    pgadg = new ProcessGraphAuxiliaryDataGenerator(theProcess, false);
	    loadAuxiliaryData(pgadg.getTransitiveClosure());
	}
    }
    public long getProcessLoadingTime()
    {
	return processLoadingTime;
    }
    private void init()
    {

	distances = new HashMap<SequenceFlowTransitiveClosure, Integer>(theProcess.nodes.size()*theProcess.nodes.size());
	distances2 = new HashMap<Integer, List<SequenceFlowTransitiveClosure>>(theProcess.nodes.size()*theProcess.nodes.size());
	transitiveClosure = new HashMap<GraphObject, List<GraphObject>>(theProcess.nodes.size());
	transitiveClosureInverse = new HashMap<GraphObject, List<GraphObject>>(theProcess.nodes.size());
	farthestDistance = new HashMap<GraphObject, Integer>(theProcess.nodes.size());
	farthestDistanceInverse = new HashMap<GraphObject, Integer>(theProcess.nodes.size());
	nodesByType = new HashMap<GraphObject.GraphObjectType, List<GraphObject>>(theProcess.nodes.size());
	gatewaysByType = new HashMap<String, List<GraphObject>>(theProcess.getGateways("").size());
	eventsByType = new HashMap<String, List<GraphObject>>(theProcess.getEvents(1).size()+theProcess.getEvents(2).size()+theProcess.getEvents(3).size());
    }
    private void loadAuxiliaryDataFromDB(String processURI)
    {
	init();
	String sqlStatement="SELECT \"FRM_GAT_ID\", \"FRM_EVE_ID\", \"FRM_ACT_ID\", \"TO_GAT_ID\", \"TO_EVE_ID\", \"TO_ACT_ID\", \"DISTANCE\" FROM \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\"   WHERE \"MODEL_ID\"='"+processURI+"'";
	Statement stt;
	try
	{
	    stt = Utilities.connection.createStatement(
	    	    ResultSet.TYPE_SCROLL_INSENSITIVE,
	    	    ResultSet.CONCUR_UPDATABLE);
	    ResultSet tmp = stt.executeQuery(sqlStatement);
	    while(tmp.next())
	    {
		String frmGatID,frmEveID,frmActID,toGatID,toEveID,toActID;
		int dist;
		
		frmGatID = tmp.getString(1);
		frmEveID = tmp.getString(2);
		frmActID = tmp.getString(3);
		
		toGatID = tmp.getString(4);
		toEveID = tmp.getString(5);
		toActID = tmp.getString(6);
		
		dist = tmp.getInt(7);
		
		GraphObject source,dest;
		if (frmGatID != null)
		{
		    source = theProcess.getNodeByID(frmGatID);
		}
		else if (frmEveID != null)
		{
		    source = theProcess.getNodeByID(frmEveID);
		}
		else
		    source = theProcess.getNodeByID(frmActID);
		
		
		if (toGatID != null)
		{
		    dest = theProcess.getNodeByID(toGatID);
		}
		else if (toEveID != null)
		{
		    dest = theProcess.getNodeByID(toEveID);
		}
		else
		    dest = theProcess.getNodeByID(toActID);
		
		SequenceFlowTransitiveClosure sftc = new SequenceFlowTransitiveClosure(source, dest);
		sftc.setDistance(dist);
		handleSequenceFlowTransitiveClosure(sftc);
		
	    }
	    loadNodeCategorization();
	} catch (SQLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
    }
    private void loadNodeCategorization()
    {
	for (GraphObject n :theProcess.nodes)
	{
	    handleGraphObjectType(n, n.type);
	}
    }
    private void handleGraphObjectType(GraphObject n, GraphObjectType t)
    {
	List<GraphObject> act = nodesByType.get(t);
	if (act == null)
	{
	    act = new ArrayList<GraphObject>(theProcess.getActivities().size());
	}
	act.add(n);
	nodesByType.put(t, act);
	// special handling of gateways
	if (n.type == GraphObjectType.GATEWAY)
	{
	    List<GraphObject> gats = gatewaysByType.get(n.type2);
	    if (gats == null)
	    {
		gats = new ArrayList<GraphObject>(theProcess.getGateways(n.type2).size());
	    }
	    gats.add(n);
	    gatewaysByType.put(n.type2, gats);
	}
	else if (n.type == GraphObjectType.EVENT)
	{
	    String pos = n.type2.substring(n.type2.length()-1, n.type2.length());
	    List<GraphObject> events = eventsByType.get(pos);
	    if (events == null)
	    {
		events = new ArrayList<GraphObject>(theProcess.getEvents(Integer.parseInt(pos)).size());
	    }
	    events.add(n);
	    eventsByType.put(pos, events);
	}
    }
    public List<GraphObject> getGatewaysByType(String type2)
    {
	return (gatewaysByType.get(type2)==null) ? new ArrayList<GraphObject>(0): gatewaysByType.get(type2);
    }
    
    public List<GraphObject> getNodesByType(GraphObjectType t)
    {
	return (nodesByType.get(t)==null) ? new ArrayList<GraphObject>(0): nodesByType.get(t);
    }
    public List<GraphObject> getEventsByType(String pos) // "1" start, "2" intermediate and "3" end events
    {
	return (eventsByType.get(pos)==null) ? new ArrayList<GraphObject>(0): eventsByType.get(pos);
    }
    private void loadAuxiliaryData(List<SequenceFlowTransitiveClosure> closure)
    {
	init();
	for (SequenceFlowTransitiveClosure sftc:closure)
	{
	    handleSequenceFlowTransitiveClosure(sftc);
	}
	loadNodeCategorization();
    }
    private void handleSequenceFlowTransitiveClosure(
	    SequenceFlowTransitiveClosure sftc)
    {
	distances.put(sftc, sftc.getDistance());
	
	List<SequenceFlowTransitiveClosure> sfs = distances2.get(new Integer(sftc.getDistance()));
	if (sfs == null)
	{
	sfs = new ArrayList<SequenceFlowTransitiveClosure>(theProcess.nodes.size());
	
	}
	//Anyway, put the data back
	sfs.add(sftc);
	distances2.put(new Integer(sftc.getDistance()),sfs);
	
	
	List<GraphObject> clsr = transitiveClosure.get(sftc.getSourceGraphObject());
	if (clsr == null)
	{
	clsr = new ArrayList<GraphObject>(theProcess.nodes.size());
	
	}
	//Anyway, put the data back
	clsr.add(sftc.getDestinationGraphObject());
	transitiveClosure.put(sftc.getSourceGraphObject(),clsr);
	
	// inverse transitive closure
	List<GraphObject> clsrInverse = transitiveClosureInverse.get(sftc.getDestinationGraphObject());
	if (clsrInverse == null)
	{
	clsrInverse = new ArrayList<GraphObject>(theProcess.nodes.size());
	
	}
	//Anyway, put the data back
	clsrInverse.add(sftc.getSourceGraphObject());
	transitiveClosureInverse.put(sftc.getDestinationGraphObject(),clsrInverse);
	
	
	// farthest distance
	Integer dd = farthestDistance.get(sftc.getSourceGraphObject());
	if (dd == null)
	{
	    dd = new Integer(sftc.getDistance());
	    farthestDistance.put(sftc.getSourceGraphObject(),dd);
	
	}
	else  if (dd.intValue() < sftc.getDistance())
	{
	    dd = new Integer(sftc.getDistance());
	    farthestDistance.put(sftc.getSourceGraphObject(),dd);
	}
	// farthest distance inverse
	dd = farthestDistanceInverse.get(sftc.getDestinationGraphObject());
	if (dd == null)
	{
	    dd = new Integer(sftc.getDistance());
	    farthestDistanceInverse.put(sftc.getDestinationGraphObject(),dd);
	
	}
	else  if (dd.intValue() < sftc.getDistance())
	{
	    dd = new Integer(sftc.getDistance());
	    farthestDistanceInverse.put(sftc.getDestinationGraphObject(),dd);
	}
	
    }
    
    private boolean isAuxiliaryDataMaterialized(String processURI)
    {
	String sqlStatement="SELECT COUNT(*)  FROM \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\"   WHERE \"MODEL_ID\"='"+processURI+"'";
	Statement stt;
	try
	{
	    stt = Utilities.connection.createStatement(
	    	    ResultSet.TYPE_SCROLL_INSENSITIVE,
	    	    ResultSet.CONCUR_UPDATABLE);
	    ResultSet tmp = stt.executeQuery(sqlStatement);
	    tmp.next();
	    int cnt = tmp.getInt(1);
	    return cnt!=0;
	} catch (SQLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	
	return false;
    }
    public void print()
    {
	for (GraphObject g : transitiveClosure.keySet())
	{
	    System.out.println(g.toString() + transitiveClosure.get(g).toString());
	}
	for (SequenceFlowTransitiveClosure sftc : distances.keySet())
	{
	    System.out.println(sftc.toString());
	}
    }
    public ProcessGraph evaluatePath(Path p)
    {
	ProcessGraph result=new ProcessGraph();
	List<GraphObject> reachableFromSource = getReachableNodes(p.getSourceGraphObject());
	List<GraphObject> reachingDestination = getReachingNodes(p.getDestinationGraphObject());
//	List<GraphObject> intersection = Utilities.intersect(getReachableNodes(p.getSourceGraphObject()), getReachingNodes(p.getDestinationGraphObject()));
	List<GraphObject> intersection = Utilities.intersect(reachableFromSource, reachingDestination);
	List<GraphObject> excludes = handleExcludeStatement(p.exclude);
	intersection.removeAll(excludes);
	// check the nodes in the exclude element.
	// now add the source and destination if they are not there
	if (!intersection.contains(p.getSourceGraphObject()))
	{
	    intersection.add(p.getSourceGraphObject());
	}
	if (!intersection.contains(p.getDestinationGraphObject()))
	{
	    intersection.add(p.getDestinationGraphObject());
	}
	result.nodes.addAll(intersection);
	for (GraphObject s: result.nodes)
	    for(GraphObject d: result.nodes)
	    {
		if (theProcess.getSuccessorsFromGraph(s).contains(d))
		{
		    result.add(new SequenceFlow(s, d));
		}
	    }
	// check connectivity
	return checkConnectivity(p, result);
    }

    private ProcessGraph checkConnectivity(Path p, ProcessGraph result)
    {
	List<GraphObject> toRemove = new ArrayList<GraphObject>();
	boolean descrepancies = false;
	do
	{
	    descrepancies = false;
	    for (GraphObject n : result.nodes)
	    {
		if (n.equals(p.getSourceGraphObject()) && result.getSuccessorsFromGraph(n).size() == 0)
		{
		    return null;
		}
		else if (n.equals(p.getDestinationGraphObject()) && result.getPredecessorsFromGraph(n).size() == 0)
		{
		    return null;
		}
		else if (!n.equals(p.getSourceGraphObject()) && !n.equals(p.getDestinationGraphObject()) && (result.getSuccessorsFromGraph(n).size() == 0 || result.getPredecessorsFromGraph(n).size() == 0))
		{
		    descrepancies = true;
		    toRemove.add(n);
		}
		
		
	    }
	 // remove all nodes and all edges with that node from the process graph
	    result.nodes.removeAll(toRemove);
	    for (GraphObject o : toRemove)
	    {
		result.removeEdgesWithDestination(o);
		result.removeEdgesWithSource(o);
	    }
	    toRemove.clear();
	    
	}
	while(descrepancies);
	
	return result;
    }
    private List<GraphObject> getReachableNodes(GraphObject sourceGraphObject)
    
    {
	List<GraphObject> result = new ArrayList<GraphObject>();
	List<GraphObject> cc = transitiveClosure.get(sourceGraphObject);
	if (cc==null) 
	    return result;
	for (GraphObject o : cc)
	{
	    try
	    {
		if (!result.contains(o))
		    result.add(o.clone());
	    } catch (CloneNotSupportedException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return result;
    }
    private List<GraphObject> getReachingNodes(
	    GraphObject destinationGraphObject)
    {
	
//	return transitiveClosureInverse.get(destinationGraphObject) ==null? new ArrayList<GraphObject>(0):transitiveClosureInverse.get(destinationGraphObject);
	List<GraphObject> result = new ArrayList<GraphObject>();
	List<GraphObject> cc = transitiveClosureInverse.get(destinationGraphObject);
	if (cc==null) 
	    return result;
	for (GraphObject o : cc)
	{
	    try
	    {
		if (!result.contains(o))
		    result.add(o.clone());
	    } catch (CloneNotSupportedException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return result;
    }
    public List<GraphObject> handleExcludeStatement(String exec)
    {
	List<GraphObject> results= new ArrayList<GraphObject>();
	StringTokenizer stk = new StringTokenizer(exec,",");
//	StringTokenizer stk2;
	String token;
	
	while(stk.hasMoreTokens())
	{
	    token = stk.nextToken();
	    if (token.startsWith("\""))
		token = token.substring(1, token.length());
	    if (token.endsWith("\""))
		token = token.substring(0, token.length()-1);
	    token = token.trim();
	    if (token.equals("XORJOIN"))
	    {
		for (GraphObject nd:theProcess.getGateways("XOR JOIN"))
		{
		    results.add(nd);

		}
	    }
	    else if (token.equals("XORSPLIT"))
	    {
		for (GraphObject nd:theProcess.getGateways("XOR SPLIT"))
		{
		    results.add(nd);

		}

	    }
	    else if (token.equals("ANDJOIN"))
	    {
		for (GraphObject nd:theProcess.getGateways("AND JOIN"))
		{
		    results.add(nd);

		}
	    }
	    else if (token.equals("ANDSPLIT"))
	    {
		for (GraphObject nd:theProcess.getGateways("AND SPLIT"))
		{
		    results.add(nd);

		}
	    }
	    else if (token.equals("ORJOIN"))
	    {
		for (GraphObject nd:theProcess.getGateways("OR JOIN"))
		{
		    results.add(nd);

		}
	    }
	    else if (token.equals("ORSPLIT"))
	    {
		for (GraphObject nd:theProcess.getGateways("OR SPLIT"))
		{
		    results.add(nd);

		}
	    }
	    else if (token.startsWith("GAT") || token.startsWith("EVE")|| token.startsWith("ACT"))
	    {
		GraphObject currentNode = new GraphObject();
//		String ID = token.substring(3);
		currentNode = theProcess.getNode(token);
		if (currentNode != null)
		{
		    results.add(currentNode);

		}

	    }
	    else if (token.startsWith("#")) // this is an id
	    {
		GraphObject currentNode = new GraphObject();
		
		currentNode = theProcess.getNodeByID(token);
		if (currentNode != null)
		{
		    results.add(currentNode);

		}
		
	    }
	    else // this is an activity label
	    {
		GraphObject currentNode = new GraphObject();
		
		currentNode = theProcess.getActivity(token);
		if (currentNode != null)
		{
		    results.add(currentNode);

		}
	    }
	}
//	System.out.println("##### Exclude statement "+results.toString());
	return results;
    }
    public String getProcessID()
    {
	return theProcess.modelURI;
    }
    public boolean pathExists(GraphObject source, GraphObject destination)
    {
	List<GraphObject> clsr = getReachableNodes(source);
	return clsr.contains(destination);
    }
    public boolean acyclicPathExists(GraphObject source, GraphObject destination)
    {
	List<GraphObject> preds = getPredecessors(source);
	// add the ids of the preds to the exclude statement
	
	List<GraphObject> clsr = getReachableNodes(source);
	
	clsr.removeAll(preds);
	if (!clsr.contains(source))
	    clsr.add(source);
	if (!clsr.contains(destination))
	    clsr.add(destination);
	
	ProcessGraph result = new ProcessGraph();
	result.nodes.addAll(clsr);
	for (GraphObject s: result.nodes)
	    for(GraphObject d: result.nodes)
	    {
		if (theProcess.getSuccessorsFromGraph(s).contains(d))
		{
		    result.add(new SequenceFlow(s, d));
		}
	    }
	// check connectivity
	
	for (GraphObject n : result.nodes)
	{
	    if (result.getSuccessorsFromGraph(n).size() == 0 && result.getPredecessorsFromGraph(n).size() == 0)
	    {
		return false;
	    }
	    if (n.getID().equals(source.getID()))
	    {
		if (result.getSuccessorsFromGraph(n).size() ==0)
		    return false;
	    }
	    else if (n.getID().equals(destination.getID()))
	    {
		if (result.getPredecessorsFromGraph(n).size() ==0)
		    return false;
	    }
	    
	    
	}
	// check that the source has at least one outgoing edge
	
	
	return true;
    }
    
    public ProcessGraph getProcessModel()
    {
	if (theProcess ==null)
	    return null;
	return (ProcessGraph) theProcess.clone();
//	return theProcess;
    }
}
