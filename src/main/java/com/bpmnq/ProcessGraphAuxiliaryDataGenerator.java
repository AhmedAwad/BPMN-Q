package com.bpmnq;

import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;
import java.util.StringTokenizer;

public class ProcessGraphAuxiliaryDataGenerator
{
    private ProcessGraph theProcess;
    private List<SequenceFlow> ts;
    private List<SequenceFlowTransitiveClosure> transitiveClosure;
    
    public ProcessGraphAuxiliaryDataGenerator(ProcessGraph p)
    {
	theProcess = (ProcessGraph) p.clone();
	//Ahmed: these steps will change later
	ts = establishSequenceFlowTransitiveClosure();
	transitiveClosure = augmentClosureWithDistance(ts);
	
    }
    public ProcessGraphAuxiliaryDataGenerator(ProcessGraph p, boolean store)
    {
	this(p);
	if (store)
	    materializeTransitiveClosureWithDistance();
    }
    public List<SequenceFlowTransitiveClosure> getTransitiveClosure()
    {
	return transitiveClosure;
    }
    public List<SequenceFlow> establishSequenceFlowTransitiveClosure() 
    {
	
	List<SequenceFlow> newEdges = new ArrayList<SequenceFlow>();
	List<SequenceFlow> finalResult = new ArrayList<SequenceFlow>();

	// copy all elements in edges to the final result
	finalResult.addAll(theProcess.edges);

	do
	{
	    newEdges.clear();
	    for (SequenceFlow p : finalResult)
	    {

		GraphObject src, dst;
		src = p.getSourceGraphObject();
		dst = p.getDestinationGraphObject();
		// get successors of the successor of src
		List<GraphObject> succs =theProcess.getSuccessorsFromGraph(dst);

		for (GraphObject g:succs)
		{
		    SequenceFlow s = new SequenceFlow(src, g);
		    if (!finalResult.contains(s))
			newEdges.add(s);
		}


	    }
	    finalResult.addAll(newEdges);
	}
	while(newEdges.size() > 0);



	return finalResult;
    }
    
    public List<SequenceFlowTransitiveClosure> augmentClosureWithDistance(List<SequenceFlow> closure)
    {
	List<SequenceFlowTransitiveClosure> closureWithDistance = new ArrayList<SequenceFlowTransitiveClosure>();
	
	int distance=0;
	    for (SequenceFlow s: closure)
	    {
		// check first that there is no direct edge
		
		if (theProcess.getSuccessorsFromGraph(s.getSourceGraphObject()).contains(s.getDestinationGraphObject()))
		    distance=1;
		else
		{
		    distance=0;
		    Path p = new Path(s.getSourceGraphObject(),s.getDestinationGraphObject());
		    ProcessGraph result = evaluatePath(p);

		    if (result!=null)
		    {
			distance = result.edges.size();
		    }
		}
		SequenceFlowTransitiveClosure st = new SequenceFlowTransitiveClosure(s.getSourceGraphObject(),s.getDestinationGraphObject());
		st.setDistance(distance);
		closureWithDistance.add(st);
	    }
	
	return closureWithDistance;
    }
    private ProcessGraph evaluatePath(Path p)
    {
	ProcessGraph result=new ProcessGraph();
	List<GraphObject> intersection = Utilities.intersect(getReachableNodes(p.getSourceGraphObject()), getReachingNodes(p.getDestinationGraphObject()));

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
	
	for (GraphObject n : result.nodes)
	{
	    if (result.getSuccessorsFromGraph(n).size() == 0 && result.getPredecessorsFromGraph(n).size() == 0)
	    {
		return null;
	    }
	}
	return result;
    }
    public boolean materializeTransitiveClosureWithDistance()
    {
	if (!Utilities.isConnectionOpen())
	{
	    try
	    {
		Utilities.openConnection();
	    } catch (Exception e)
	    {
		// TODO Auto-generated catch block
		return false;
	    }
	}
	try
	{
	    for (SequenceFlowTransitiveClosure s : transitiveClosure)
	    {
		String sqlStatement="INSERT INTO \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\"(\"FRM_GAT_ID\", \"FRM_EVE_ID\", \"FRM_ACT_ID\", \"TO_GAT_ID\", \"TO_EVE_ID\", \"TO_ACT_ID\", \"MODEL_ID\", \"DISTANCE\") VALUES(";
		
		String FRM_ACT_ID="null";
		String TO_ACT_ID="null";
		String FRM_GAT_ID="null";
		String TO_GAT_ID ="null";
		String FRM_EVE_ID ="null";
		String TO_EVE_ID = "null";
		
		if (s.frmActivity != null) {
		    FRM_ACT_ID ="'"+s.frmActivity.actID+"'";
		}

		// Event as source
		else if (s.frmEvent != null) {
		    FRM_EVE_ID =  "'"+s.frmEvent.eventID+"'";

		    
		}

		//	Gateway as source
		else if (s.frmGateWay != null) {
		    FRM_GAT_ID = "'"+s.frmGateWay.gateID+"'";
		}
		
		if (s.toActivity != null) {
		    TO_ACT_ID = "'"+s.toActivity.actID+"'";
		}

		// Event as source
		else if (s.toEvent != null) {
		    TO_EVE_ID =  "'"+s.toEvent.eventID+"'";

		    
		}

		//	Gateway as source
		else if (s.toGateWay != null) {
		    TO_GAT_ID = "'"+s.toGateWay.gateID+"'";
		}
		sqlStatement += FRM_GAT_ID+","+FRM_EVE_ID+","+FRM_ACT_ID+","+TO_GAT_ID+","+TO_EVE_ID+","+TO_ACT_ID+",'"+theProcess.modelURI+"','"+s.getDistance()+"')";
		
		Utilities.getDbStatemement().execute(sqlStatement);
	    }

	} catch (SQLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return false;
	}
	
	return true;
	
    }

    public List<GraphObject> getReachableNodes(GraphObject nd)
    {
	List<GraphObject> result = new ArrayList<GraphObject>();
	for (SequenceFlow s : ts)
	{
	    if (s.getSourceGraphObject().equals(nd))
	    {
		result.add(s.getDestinationGraphObject());
	    }
	}
	return result;
    }
    public List<GraphObject> getReachingNodes(GraphObject nd)
    {
	List<GraphObject> result = new ArrayList<GraphObject>();
	for (SequenceFlow s : ts)
	{
	    if (s.getDestinationGraphObject().equals(nd))
	    {
		result.add(s.getSourceGraphObject());
	    }
	}
	return result;
    }
    public void print()
    {
	for (SequenceFlowTransitiveClosure s : transitiveClosure)
	{
	    s.print(System.out);
	}
    }
    protected List<GraphObject> handleExcludeStatement(String exec)
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
		String ID = token.substring(4);
		currentNode = theProcess.getNode(ID);
		if (currentNode != null)
		{
		    results.add(currentNode);

		}

	    }
	    else // this is an activity
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
}
