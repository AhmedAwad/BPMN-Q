package com.bpmnq;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class EfficientQueryProcessorWithPathIndex extends
	EfficientQueryProcessor
{
    protected static final int NO_PATH_INDEX_ENTRY = 1;
    protected static final int PATH_ENTRY_POSITIVE = 2;
    protected static final int PATH_ENTRY_NEGATIVE = 3;
    protected static final int PATH_ENTRY_NEGATIVE_SUBSET = 4;
    protected StringBuffer nodeList;
    protected HashMap<Path, String> pathIndex;
//    protected HashMap<Path, String> newPathIndexEntries;
    
    public EfficientQueryProcessorWithPathIndex(PrintWriter writer)
    {
	super(writer);
	pathIndex = new HashMap<Path, String>();
//	newPathIndexEntries = new HashMap<Path, String>();
    }
    protected void loadPathIndex(String modelID)
    {
	pathIndex.clear();
	String sqlStatement="SELECT \"SUB_GRAPH_NODES\", \"SOURCE\", \"TARGET\", \"EXCLUDED_NODES\"" +
			"FROM \"BPMN_GRAPH\".\"ORYX_PATHS\" WHERE \"Model_ID\"='"+modelID+"'";
	Statement stt;
	try
	{
	    stt = Utilities.connection.createStatement(
	    	    ResultSet.TYPE_SCROLL_INSENSITIVE,
	    	    ResultSet.CONCUR_UPDATABLE);
	    ResultSet tmp = stt.executeQuery(sqlStatement);
	    while(tmp.next())
	    {
		String source,target,subgraphNodes,excludes;
		
		
		source = tmp.getString(2);
		target = tmp.getString(3);
		subgraphNodes = tmp.getString(1);
		
		excludes = tmp.getString(4);
		
		GraphObject sourceObject, targetObject;
		
		try
		{
		    sourceObject = pgad.getNodeByID(source).clone();
		    targetObject = pgad.getNodeByID(target).clone();
			if (sourceObject != null && targetObject != null)
			{
			    Path p = new Path(sourceObject,targetObject,excludes);
//			    System.out.println("Has code of path "+p.toString() +" is :"+p.hashCode());
			    pathIndex.put(p, subgraphNodes);
			}
		} catch (CloneNotSupportedException e)
		{
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
		
		
	    }
	   
	} catch (SQLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
    }
    @Override
    protected boolean resolvePaths(QueryGraph query, String modelID)
    {
	if (query.paths.size() ==0) 
	    return true;
	refreshModel(modelID);

	//System.out.println("CALLING RESOLVE PATHS");
	boolean pathFound = false;
	

	ProcessGraph currentPath;
	Path currentEdgep;

	while (query.paths.size() > 0)
	{

	    currentEdgep = query.paths.remove(0);
	    
	    if (query.pathEdgeHasDependency(currentEdgep))
	    {
	    	query.paths.add(currentEdgep);
	    	continue;
	    }

	    

	    // Lookup path index
	     nodeList=new StringBuffer();
	     // here we have to resolve all labels in the exclude property to node ids
	    List<GraphObject> exludes = pgad.handleExcludeStatement(currentEdgep.exclude);
	    String nodelistIDs = getNodesIDsList(exludes);
	    currentEdgep.exclude = nodelistIDs;
	    int pathIndexResult = lookupPathIndex(currentEdgep);
	    if (pathIndexResult == NO_PATH_INDEX_ENTRY)
	    {
		currentPath = pgad.evaluatePath(currentEdgep);
		
		// we have to update the index for the upcoming lookups
	    }
	    else if (pathIndexResult == PATH_ENTRY_NEGATIVE || pathIndexResult == PATH_ENTRY_NEGATIVE_SUBSET)
	    {
		currentPath = null;
		// we have to update the index
	    }
	    else // there is an exact entry for the path
	    {
		currentPath = loadPrecomputedPath(currentEdgep);
		// we have to load the answer from the index
	    }
	    
	    if (currentPath == null || currentPath.nodes.size()==0)
	    {
		query.addErrorLog("Evaluation of "+currentEdgep.toString() +" failed !");
		// update the path index
		updatePathIndex(currentEdgep,currentPath, pathIndexResult);
		return false;
	    }
	    
	    // we have to remove this matched edge
	    // insert nodes and edges from the matched path
	    // call it one more time
	    //		query.paths.remove(currente);
	    pathFound = true;
	    //update the Path index

	    // Optimization step added on 7th July 2007
	    handleEvaluatedPath(query, currentPath, currentEdgep);

	    // we have to update all exclude path statements
	    query.updateExcludeExpression(currentEdgep.label, currentPath.nodes.toString().replace("[", "").replace("]", "").replace("GAT", "").replace("ACT", "").replace("EVE",""));
	    updatePathIndex(currentEdgep,currentPath, pathIndexResult);
	    break;
	}
	if (pathFound) 
	    return resolvePaths(query, modelID);
	
	return false;
    }
    protected void refreshModel(String modelID)
    {
	if (pgad == null || !(modelID.equals(pgad.getProcessID())))
	{
	    long startLoading = System.currentTimeMillis();
	    pgad = new ProcessGraphAuxiliaryData(modelID);
	    currentProcess = pgad.getProcessModel();
	    loadPathIndex(modelID);
	    long loadingTime = (System.currentTimeMillis() - startLoading);
	   
	    extraOverhead += loadingTime;
	    System.out.println("Loading model: "+modelID+" took "+(loadingTime) +" MS");
	}
	   
    }
    
    private int lookupPathIndex(Path currentEdgep)
    {
	// precaution step
	currentEdgep.exclude = currentEdgep.exclude.replace(" ", "");
	// there might be cases where 
	String subgraphNodes = pathIndex.get(currentEdgep);
	if (subgraphNodes == null) // no path index entry
	{
	    if (currentEdgep.exclude.length() == 0)
	    {
		return NO_PATH_INDEX_ENTRY;
		
	    }
	    else
	    {
		// look for inexact matches
		Set<Path> indexPaths = pathIndex.keySet();
		boolean entryFound = false;
		for (Path p : indexPaths)
		{
		    
		    if (p.getSourceGraphObject().equals(currentEdgep.getSourceGraphObject())
			   && p.getDestinationGraphObject().equals(currentEdgep.getDestinationGraphObject())
			   && p.exclude.length() > 0
			   && Utilities.isSubset(currentEdgep.exclude, p.exclude))
		    {
			
			String subgraphNodes2 = pathIndex.get(p);
			if (subgraphNodes2.length() == 0) // negative answer subset
			{
			    entryFound = true;
			    return PATH_ENTRY_NEGATIVE_SUBSET;
			    
			}
			
		    }
		}
		if (!entryFound)
		    return NO_PATH_INDEX_ENTRY;
	    }
	}
	else if (subgraphNodes.length() == 0)
	{
	    return PATH_ENTRY_NEGATIVE;
	}
	else
	{
	    nodeList = new StringBuffer();
	    nodeList.append(subgraphNodes);
	    
	    if (!nodeList.toString().contains(currentEdgep.getSourceGraphObject().getID()))
		nodeList.append(","+currentEdgep.getSourceGraphObject().getID());
	    if (!nodeList.toString().contains(currentEdgep.getDestinationGraphObject().getID()))
		nodeList.append(","+currentEdgep.getDestinationGraphObject().getID());
	    return PATH_ENTRY_POSITIVE;

	}
	return 0;

    }
//	List<GraphObject> exclueds = pgad.handleExcludeStatement(currentEdgep.exclude);
//	StringBuilder sqlStatement = new StringBuilder();
//	StringBuilder sqlStatement2 = new StringBuilder();
//	sqlStatement.append("SELECT \"SUB_GRAPH_NODES\" " +
//			"FROM \"BPMN_GRAPH\".\"ORYX_PATHS\" WHERE ");
//	sqlStatement2.append("SELECT \"SUB_GRAPH_NODES\", \"Model_ID\", \"EXCLUDED_NODES\"   " +
//	"FROM \"BPMN_GRAPH\".\"ORYX_PATHS\" WHERE ");
//	
//	// filter with source
//	sqlStatement.append(" \"SOURCE\" ='"+currentEdgep.getSourceGraphObject().getID()+"'");
//	sqlStatement2.append(" \"SOURCE\" ='"+currentEdgep.getSourceGraphObject().getID()+"'");
//	// filter with destination
//	sqlStatement.append(" AND \"TARGET\" ='"+currentEdgep.getDestinationGraphObject().getID()+"'");
//	sqlStatement.append(" AND (1=1 ");
//	
//	sqlStatement2.append(" AND \"TARGET\" ='"+currentEdgep.getDestinationGraphObject().getID()+"'");
//	sqlStatement2.append(" AND (1=1 ");
//	// first look for exact match
	
	
	// loop over excludes
//	if (exclueds.size() == 0)
//	{
//	    // we have to be sure that the EXCLUDED nodes field is empty
//	    sqlStatement.append(" AND \"EXCLUDED_NODES\"=''");
//	    sqlStatement2.append(" AND \"EXCLUDED_NODES\"=''");
//	}
//	else
//	{
//	    // we have nodes to exclude
//	    String listOfIDs = getNodesIDsList(exclueds);
//	    sqlStatement.append(" AND \"BPMN_GRAPH\".\"IS_SUBSET\"('"+listOfIDs+"',\"EXCLUDED_NODES\")='Y'");
//	    sqlStatement2.append(" AND \"BPMN_GRAPH\".\"IS_SUBSET\"('"+listOfIDs+"',\"EXCLUDED_NODES\")='Y'");
//	    sqlStatement2.append(" AND \"BPMN_GRAPH\".\"IS_SUBSET\"(\"EXCLUDED_NODES\",'"+listOfIDs+"')='Y'");
//	}
//	// loop over excludes
//	sqlStatement.append(" )");
//	sqlStatement2.append(")");
	// exact match entry
//	sqlStatement2.append("SELECT \"BPMN_GRAPH\".get_path_with_excluded_nodes_exact('"+currentEdgep.getSourceGraphObject().getID()+"','"+currentEdgep.getDestinationGraphObject().getID()+"'," +
//			"'"+pgad.getProcessID()+"','"+(exclueds.size() == 0? "":getNodesIDsList(exclueds))+"')");
//	
//	sqlStatement.append("SELECT \"BPMN_GRAPH\".get_path_with_excluded_nodes_none_exact('"+currentEdgep.getSourceGraphObject().getID()+"','"+currentEdgep.getDestinationGraphObject().getID()+"'," +
//		"'"+pgad.getProcessID()+"','"+(exclueds.size() == 0? "":getNodesIDsList(exclueds))+"')");
//	try
//	{
//	    Statement stt = Utilities.connection.createStatement(
//	    	    ResultSet.TYPE_SCROLL_INSENSITIVE,
//	    	    ResultSet.CONCUR_UPDATABLE);
//	    ResultSet tmp = stt.executeQuery(sqlStatement2.toString());
//	    
//	    while(tmp.next())
//	    {
//		String subgraphNodes;
//		
//		subgraphNodes = tmp.getString(1);
//		nodeList = new StringBuffer();
//		nodeList.append(subgraphNodes);
//		if (nodeList.length() > 0)
//		{
//		    nodeList.append(","+currentEdgep.getSourceGraphObject().getID());
//		    nodeList.append(","+currentEdgep.getDestinationGraphObject().getID());
//		    return PATH_ENTRY_POSITIVE;
//		}
//		else // there is an entry with empty subgraph nodes
//		{
//		    return PATH_ENTRY_NEGATIVE;
//		}
//	    }
//	    // no match found
//	    if (exclueds.size() == 0)
//		return NO_PATH_INDEX_ENTRY;
//	    else
//	    {
//		// lookup for  cases where  a subset of the exlcuded nodes are known to have no matches
//		// i.e., there is an entry in the index with the same source and destination,but with a subset of the excluded nodes
//		// and the subgraph nodes is empty. At this case, we conclude that the check for the path will fail and thus no need to 
//		// compute it.
//		ResultSet tmp2 = stt.executeQuery(sqlStatement.toString());
//		
//		// here there could be more than one entry
//		while(tmp2.next())
//		{
//		    String subgraphNodes;
//
//		    subgraphNodes = tmp2.getString(1);
//		    nodeList = new StringBuffer(subgraphNodes);
//		    
////		    if (nodeList.length() > 0)
////		    {
////			nodeList +=","+currentEdgep.getSourceGraphObject().getID();
////			nodeList += ","+currentEdgep.getDestinationGraphObject().getID();
////			return PATH_ENTRY_POSITIVE;
////		    }
////		    else // there is an entry with empty subgraph nodes for a subset of the excluded nodes
//		    if (nodeList.length() == 0)
//		    {
//			// we know that the path evaluation fails
//			// However, we have to tell BPMN-Q to insert a specific entry for that path
//			return PATH_ENTRY_NEGATIVE;
//		    }
//		}
//		// we reach here meaning that we cannot decide for emptiness of the path
//		// thus, we have to instruct BPMN-Q to compute it
//		return NO_PATH_INDEX_ENTRY;
//	    }
//	} catch (SQLException e)
//	{
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	    return NO_PATH_INDEX_ENTRY;
//	}
//	return 0;
//    }
    
    
    private void updatePathIndex(Path currentEdgep, ProcessGraph currentPath, int pathIndexResult)
    {
	// no action needs to be taken in the following two cases
	if (pathIndexResult == PATH_ENTRY_NEGATIVE || pathIndexResult ==  PATH_ENTRY_POSITIVE)
	    return;
	
	
	// TODO Auto-generated method stub
	String excludedNodesIDsList = (currentEdgep.exclude == null || currentEdgep.exclude.length() == 0) ? "" :
	    getNodesIDsList(pgad.handleExcludeStatement(currentEdgep.exclude));
	// update in-memory index
	String subgraphNodes = (currentPath == null ? "":getNodesIDsList(currentPath.nodes));
	pathIndex.put(currentEdgep,subgraphNodes );
	
	
	StringBuilder insertStatement = new StringBuilder();
	insertStatement.append("INSERT INTO \"BPMN_GRAPH\".\"ORYX_PATHS\"(\"SOURCE\", \"TARGET\", \"Model_ID\", \"EXCLUDED_NODES\",\"SUB_GRAPH_NODES\")" +
			" VALUES('"+currentEdgep.getSourceGraphObject().getID()+"','"+currentEdgep.getDestinationGraphObject().getID()+
			"','"+pgad.getProcessID()+"','"+ excludedNodesIDsList+"','");
	
	if (pathIndexResult == NO_PATH_INDEX_ENTRY) // we have to reflect what is in the currentPath process
	{
	    if (currentPath == null || currentPath.nodes.size() == 0)
	    {
		insertStatement.append("')");
	    }
	    else // there is a positive entry to be added
	    {
		String listOfNodeID = getNodesIDsList(currentPath.nodes);
		// clean the list from ID of souce and destination nodes
//		listOfNodeID=listOfNodeID.replace(currentEdgep.getSourceGraphObject().getID(), "");
//		listOfNodeID=listOfNodeID.replace(currentEdgep.getDestinationGraphObject().getID(), "");
//		listOfNodeID=listOfNodeID.replace(",,", ",");
		insertStatement.append(listOfNodeID+"')");
	    }
	}
	else if (pathIndexResult == PATH_ENTRY_NEGATIVE_SUBSET) // we have to insert an empty exact match entry in the index
	{
	    insertStatement.append("')");
	}
	
	 // update the ORYX_PATHS table
	try
	{
	    Utilities.getDbStatemement().execute(insertStatement.toString());
	} catch (SQLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
    }
    private ProcessGraph loadPrecomputedPath(Path currentEdgep)
    {
	ProcessGraph result = new ProcessGraph();
	String[] nodeIDs = nodeList.toString().split(",");
	ProcessGraph inspected = pgad.getProcessModel();
	for (String s : nodeIDs)
	{
	    GraphObject nd = inspected.getNodeByID(s);
	    if (nd != null)
		try
		{
		    result.nodes.add(nd.clone());
		} catch (CloneNotSupportedException e)
		{
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	for (GraphObject s: result.nodes)
	    for(GraphObject d: result.nodes)
	    {
		if (inspected.getSuccessorsFromGraph(s).contains(d))
		{
		    result.add(new SequenceFlow(s, d));
		}
	    }
	return result;
    }
    
    
    @Override
    protected List<GraphObject> eleminateNodesViolatingPathConstraints(List<GraphObject> possibilities, QueryGraph query, GraphObject evaluatedNode)
    {
	possibilities = super.eleminateNodesViolatingPathConstraints(possibilities, query, evaluatedNode);
	// now cycle over the path edges that have exclude statement set to some reference
	List<GraphObject> toRemove = new ArrayList<GraphObject>(possibilities.size());
	List<GraphObject> pathSuccessors = query.getPathSuccessorsFromQueryGraph(evaluatedNode);
	for (GraphObject g : pathSuccessors)
	{
	    if (!g.isResolved())
		continue;
	    String exl = query.getPathExcludeStatement(evaluatedNode, g);
	    if (exl != null && exl.length() > 0)
	    {
		String[] exludes = exl.split(",");
		boolean containdUnresolvedNodes = false;
		for (String s: exludes)
		{
		    if (!s.startsWith("#"))
		    {
			containdUnresolvedNodes = true;
			break;
		    }
		}
		if (containdUnresolvedNodes)
		{
		    // we can not actuall check it right now
		    continue;
		}
	    }
	    for (GraphObject possibility : possibilities)
	    {
		Path p = new Path(possibility,g,exl);
		int check = lookupPathIndex(p);
		if (check == PATH_ENTRY_NEGATIVE || check == PATH_ENTRY_NEGATIVE_SUBSET)
		{
		    toRemove.add(possibility);
		}
		else if (check == NO_PATH_INDEX_ENTRY && !pgad.pathExists(possibility, g)) // we have no decisive answer from the index
		    toRemove.add(possibility);
		
		
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	}
	List<GraphObject> pathPredecessors = query.getPathPredecessorsFromQueryGraph(evaluatedNode);
	toRemove.clear();
	for (GraphObject g : pathPredecessors)
	{
	    if (!g.isResolved())
		continue;
	    String exl = query.getPathExcludeStatement(g, evaluatedNode);
	    if (exl != null && exl.length() > 0)
	    {
		String[] exludes = exl.split(",");
		boolean containdUnresolvedNodes = false;
		for (String s: exludes)
		{
		    if (!s.startsWith("#"))
		    {
			containdUnresolvedNodes = true;
			break;
		    }
		}
		if (containdUnresolvedNodes)
		{
		    // we can not actuall check it right now
		    continue;
		}
	    }
	    for (GraphObject possibility : possibilities)
	    {
		Path p = new Path(g,possibility,exl);
		int check = lookupPathIndex(p);
		if (check == PATH_ENTRY_NEGATIVE || check == PATH_ENTRY_NEGATIVE_SUBSET)
		{
		    toRemove.add(possibility);
		}
		else if (!pgad.pathExists(g,possibility))
		    toRemove.add(possibility);
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	}
	return possibilities;
	
//	List<GraphObject> resolvedNodes = new ArrayList<GraphObject>(query.nodes.size());
//	for (GraphObject g: query.nodes)
//	{
//	    if (g.isResolved())
//		resolvedNodes.add(g);
//	}
//	if (resolvedNodes.size()==0)
//	    return possibilities;// there is no way to tell how to reduce possibilities
//	
//	// now get those nodes that are path predecessors or path successors of a resolved node
//	List<GraphObject> toRemove = new ArrayList<GraphObject>(possibilities.size());
//	
//	for (GraphObject g : resolvedNodes)
//	{
//	    
//	    List<GraphObject> pathPredecessors = query.getPathPredecessorsFromQueryGraph(g);
//	    if (pathPredecessors.contains(evaluatedNode))
//	    {
//		String exl = query.getPathExcludeStatement(evaluatedNode, g);
//		if (exl != null && exl.length() > 0)
//		{
//		    // check the path index
//		    String[] exludes = exl.split(",");
//		    boolean containdUnresolvedNodes = false;
//		    for (String s: exludes)
//		    {
//			if (!s.startsWith("#"))
//			{
//			    containdUnresolvedNodes = true;
//			    break;
//			}
//		    }
//		    if (containdUnresolvedNodes)
//		    {
//			// we can not actuall check it right now
//			continue;
//		    }
//		}
//		
//		// make sure that the path has no exclude property
//		
//		for (GraphObject possibility : possibilities)
//		{
//		    
//			Path p = new Path(possibility,g,exl);
//			int check = lookupPathIndex(p);
//			if (check == PATH_ENTRY_NEGATIVE || check == PATH_ENTRY_NEGATIVE_SUBSET)
//			{
//			    toRemove.add(possibility);
//			}
//			else if (!pgad.pathExists(possibility, g)) // we have no decisive answer from the index
//			    toRemove.add(possibility);
//		   
//		}
//	    }
//	    possibilities.removeAll(toRemove);
//	    toRemove.clear();
//	    if (possibilities.size()==0)
//		    return possibilities;
//	    
//	    
//	    List<GraphObject> pathSuccessors = query.getPathSuccessorsFromQueryGraph(g);
//	    if (pathSuccessors.contains(evaluatedNode))
//	    {
//		String exl = query.getPathExcludeStatement( g,evaluatedNode);
//		if (exl != null && exl.length() > 0)
//		{
//		    // check the path index
//		    String[] exludes = exl.split(",");
//		    boolean containdUnresolvedNodes = false;
//		    for (String s: exludes)
//		    {
//			if (!s.startsWith("#"))
//			{
//			    containdUnresolvedNodes = true;
//			    break;
//			}
//		    }
//		    if (containdUnresolvedNodes)
//		    {
//			// we can not actuall check it right now
//			continue;
//		    }
//		}
//		for (GraphObject possibility : possibilities)
//		    
//		{
//		    Path p = new Path(g,possibility,exl);
//		    int check = lookupPathIndex(p);
//		    if (check == PATH_ENTRY_NEGATIVE || check == PATH_ENTRY_NEGATIVE_SUBSET)
//		    {
//			toRemove.add(possibility);
//		    }
//		    else if (!pgad.pathExists( g,possibility)) // we have no decisive answer from the index
//			toRemove.add(possibility);
//		}
//		    
//	    }
//	    possibilities.removeAll(toRemove);
//	    toRemove.clear();
//	    if (possibilities.size()==0)
//		    return possibilities;
//	}
//	return possibilities;
    }
    /* (non-Javadoc)
     * @see com.bpmnq.EfficientQueryProcessor#eleminateNodesViolatingNegativePathConstraints(java.util.List, com.bpmnq.QueryGraph, com.bpmnq.GraphObject)
     */
    @Override
    protected List<GraphObject> eleminateNodesViolatingNegativePathConstraints(
	    List<GraphObject> possibilities, QueryGraph query,
	    GraphObject evaluatedNode)
    {
	// TODO Auto-generated method stub
//	return super.eleminateNodesViolatingNegativePathConstraints(possibilities,
//		query, evaluatedNode);
	
	// now cycle over the path edges that have exclude statement set to some reference
	List<GraphObject> toRemove = new ArrayList<GraphObject>(possibilities.size());
	List<GraphObject> pathSuccessors = query.getNegativePathSuccessorsFromQueryGraph(evaluatedNode);
	String exl;
	for (GraphObject g : pathSuccessors)
	{
	    if (!g.isResolved())
		continue;
	    
	   
	    
	    for (GraphObject possibility : possibilities)
	    {
		exl="";
		List<GraphObject> possibilityPreds = pgad.getPredecessors(possibility);
		exl = getNodesIDsList(possibilityPreds);
		Path p = new Path(possibility,g,exl);
		int check = lookupPathIndex(p);
		if (check == PATH_ENTRY_POSITIVE)
		{
		    toRemove.add(possibility);
		}
		else if (check == NO_PATH_INDEX_ENTRY || check == PATH_ENTRY_NEGATIVE_SUBSET)
		{
		    ProcessGraph pg =pgad.evaluatePath(p);
		   
		    if (pg !=null)
		    {
			toRemove.add(possibility);
			
		    }
		    // update the index
		   
		    
		    
		    updatePathIndex(p, pg, check);
		}
		
		
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	}
	List<GraphObject> pathPredecessors = query.getNegativePathPredecessorsFromQueryGraph(evaluatedNode);
	toRemove.clear();
	for (GraphObject g : pathPredecessors)
	{
	    if (!g.isResolved())
		continue;
	    
	    
	    for (GraphObject possibility : possibilities)
	    {
		exl ="";
		List<GraphObject> possibilityPreds = pgad.getPredecessors(g);
		exl = getNodesIDsList(possibilityPreds);
		Path p = new Path(g,possibility,exl);
		int check = lookupPathIndex(p);
		if (check == PATH_ENTRY_POSITIVE)
		{
		    toRemove.add(possibility);
		}
		else if (check == NO_PATH_INDEX_ENTRY || check == PATH_ENTRY_NEGATIVE_SUBSET)
		{
		    ProcessGraph pg =pgad.evaluatePath(p);
		   
		    if (pg != null)
		    {
			toRemove.add(possibility);
			
		    }
		    // update the index
		   
		    
		    
		    updatePathIndex(p, pg, check);
		}
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	}
	
	
	
	
	
	return possibilities;
    }
    /* (non-Javadoc)
     * @see com.bpmnq.EfficientQueryProcessor#prepareSelectStatement(com.bpmnq.QueryGraph)
     */
    @Override
    protected StringBuilder preparePocessGraphsFilterStatement(QueryGraph query)
    {
	// TODO Auto-generated method stub
	StringBuilder parentStatement = super.preparePocessGraphsFilterStatement(query);
	// here add further filtration 
	// now add further constraints on the negative paths
	// Added on 10.05.2011 to support filtering by path/negative path and negative edges on the closure of sequence flow
	
	// check paths with exclude statement
	for(Path currentEdge : query.paths)
	{
	    // just handle pure path edges without any exclude statements
	    // in the version with path index, we can handle that
	    if (!(currentEdge.exclude !=null && currentEdge.exclude.length() > 0))
		continue;
	    if (currentEdge.exclude.contains("?")) // this is dynamic exclude and cannot contribute 
		continue;
	    // here only paths that have exclude statements
	    if (currentEdge.frmActivity != null && currentEdge.toActivity != null)
	    {
		if (!currentEdge.frmActivity.name.startsWith("@") && !currentEdge.toActivity.name.startsWith("@") 
			&& !currentEdge.frmActivity.name.startsWith("$#") && !currentEdge.toActivity.name.startsWith("$#")
			&& !currentEdge.frmActivity.name.startsWith("?")  && !currentEdge.toActivity.name.startsWith("?")) 
		{
		    parentStatement.append(" and not exists (select 1 from \"BPMN_GRAPH\".\"ORYX_PATHS\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination");
		    parentStatement.append(" where \"SOURCE\" =source.\"ID\" and \"TARGET\" = destination.\"ID\"");
		    parentStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    parentStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"BPMN_GRAPH\".\"ORYX_PATHS\".\"Model_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\"");
		    
		    parentStatement.append(" AND \"BPMN_GRAPH\".\"IS_SUBSET\"(\"BPMN_GRAPH\".get_id_list_for_activity_labels('"+currentEdge.exclude+"',\"BPMN_GRAPH\".\"ORYX_PATHS\".\"Model_ID\"),\"EXCLUDED_NODES\")='Y'");
		    //parentStatement.append(" AND \"BPMN_GRAPH\".\"IS_SUBSET\"(\"EXCLUDED_NODES\",\"BPMN_GRAPH\".get_id_list_for_activity_labels('"+currentEdge.exclude+"',\"BPMN_GRAPH\".\"ORYX_PATHS\".\"Model_ID\"))='Y'");
		    parentStatement.append(" AND length(\"BPMN_GRAPH\".\"ORYX_PATHS\".\"SUB_GRAPH_NODES\") <= 1)");
		    
//		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination");
//		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
//		    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
//		    if (currentEdge.frmActivity.actID.equals(currentEdge.toActivity.actID))
//			filterStatement.append(" and source.\"ID\" = destination.\"ID\"");
//		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
//		    filterStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
//		   
//		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
	    }
	}
	
	for(SequenceFlow currentEdge : query.negativePaths)
	{
	    if (currentEdge.frmActivity != null && currentEdge.toActivity != null)
	    {
		if (!currentEdge.frmActivity.name.startsWith("@") && !currentEdge.toActivity.name.startsWith("@") 
			&& !currentEdge.frmActivity.name.startsWith("$#") && !currentEdge.toActivity.name.startsWith("$#")
			&& !currentEdge.frmActivity.name.startsWith("?")  && !currentEdge.toActivity.name.startsWith("?"))
		{
		    parentStatement.append(" and not exists (select 1 from \"BPMN_GRAPH\".\"ORYX_PATHS\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination");
		    parentStatement.append(" where \"SOURCE\" =source.\"ID\" and \"TARGET\" = destination.\"ID\"");
		    parentStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    parentStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"BPMN_GRAPH\".\"ORYX_PATHS\".\"Model_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\"");
		    parentStatement.append(" AND \"BPMN_GRAPH\".\"IS_SUBSET\"(\"BPMN_GRAPH\".get_predecessor_of_activity_node(source.\"ID\",\"BPMN_GRAPH\".\"ORYX_PATHS\".\"Model_ID\"),\"EXCLUDED_NODES\")='Y'");
		    parentStatement.append(" AND length(\"BPMN_GRAPH\".\"ORYX_PATHS\".\"SUB_GRAPH_NODES\") > 1)");
//		    parentStatement.append(" AND \"BPMN_GRAPH\".\"IS_SUBSET\"(\"EXCLUDED_NODES\",'"+listOfIDs+"')='Y'");
		
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
	    }
	    
	}
	return parentStatement;
    }
    
}
