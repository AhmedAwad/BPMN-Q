package com.bpmnq;
//currently this processor does not support the exclusion of enclosing AND Split
//this processor supports negative paths with excludes
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.Path.PathEvaluation;
import com.bpmnq.Path.TemporalType;
import com.sun.tools.javac.util.Paths;

public class EfficientQueryProcessor extends AbstractQueryProcessor
{
   
    protected ProcessGraphAuxiliaryData pgad;
    
    
    protected void refreshModel(String modelID)
    {
	if (pgad == null || !(modelID.equals(pgad.getProcessID())))
	{
	    long startLoading = System.currentTimeMillis();
	    pgad = new ProcessGraphAuxiliaryData(modelID);
	    currentProcess = pgad.getProcessModel();
	    long loadingTime = ((System.currentTimeMillis() - startLoading)-pgad.getProcessLoadingTime());
	    extraOverhead += loadingTime;
	    System.out.println("Loading model: "+modelID+" took "+(loadingTime) +" MS");
	}
	   
    }
    public EfficientQueryProcessor(PrintWriter answer)
    {
	super(answer);
	currentProcess = new ProcessGraph();
	
    }
    @Override
    protected void resolveGateWayNode(String modelID) 
    {
	
	
	if (this.intermediateRefinements.size() == 0) 
	    return;

	refreshModel(modelID);
	
	while (this.intermediateRefinements.size() > 0)
	{
//	    System.out.println("Intermediate query list size ="+intermediateRefinements.size());
//	    System.out.println("Final query list size ="+finalRefinements.size());
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0);

	    boolean queryHasBeenRefined = false;
	    //ArrayList<ArrayList<QueryGraph>> refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    List<QueryGraph> refinedQueries = new ArrayList<QueryGraph>();
	    
	    QueryGraph refinement;
	    GraphObject currentNode;

	    List<GraphObject> resolvedGateways = new ArrayList<GraphObject>();

	    int nodesSize = query.nodes.size();
	    for (int nodeNum = 0; nodeNum < nodesSize; nodeNum++)
	    {
		currentNode = query.nodes.get(nodeNum);
		if (currentNode.type != GraphObjectType.GATEWAY) 
		    continue;
		if (currentNode.isResolved()) 
		    continue;
		if (currentNode.type2.startsWith("GENERIC")) 
		    continue;

		queryHasBeenRefined = true;

		// try to find a gateway in this process model, where either successor or predecessor 
		// match a node in the query that has been resolved already 
		
		
		for (GraphObject gateway : pgad.getGatewaysByType(currentNode.type2))
		{
		    boolean contradictsModel = false;
		    if (query.forbiddenGatewayIDs.toString().contains(gateway.getID()))
			continue;

		    List<GraphObject> predsInQuery = query.getPredecessorsFromQueryGraph(currentNode);
		    List<GraphObject> predsInModel = pgad.getPredecessors(gateway);
		    

		    for (GraphObject pre : predsInQuery)
		    {
			if (pre.isResolved() && !predsInModel.contains(pre))
			{
			    contradictsModel = true;
			    break;
			}
//			for (GraphObject preP : predsInModel)
//			{
//			if (pre.type == )
//			}

		    }

		    
		    List<GraphObject> succsInQuery = query.getSuccessorsFromQueryGraph(currentNode);
		    List<GraphObject> succsInModel = pgad.getSuccessors(gateway);
		    
		    for (GraphObject suc : succsInQuery)

		    {
			if (suc.isResolved() && !succsInModel.contains(suc))
			{
			    contradictsModel = true;
			    break;
			}
//			for (GraphObject preP : succsInModel)
//			{
//			if (pre.type == )
//			}

		    }
		    if (!contradictsModel)
			resolvedGateways.add(gateway);
		}
		// here we should add an enhancement step to reduce the number of possible resolved gateways
		// this should be invoked when there are no successors or predecessors from the query graph
		// At that point, we have to look for other nodes in the query graph where we can benefit from to reduce 
		// the number of generated intermediate query graphs
		resolvedGateways = reducePossibilities(resolvedGateways, query, currentNode);
		for (GraphObject nd : resolvedGateways)
		{
		    // we have to create a refinement for each event id
		    currentNode.setBoundQueryObjectID(currentNode.getID());
//		    currentNode.setID(nd.getID());
		    //currentNode.name = lrs.getString("name");
		    refinement = (QueryGraph)query.clone();
		    refinement.nodes.get(nodeNum).setBoundQueryObjectID(currentNode.getBoundQueryObjectID());
		    refinement.addInfoLog("GateWay " + currentNode.getName() + " was bound to gateway ID " + nd.getID());
		    
		    refinement.nodes.get(nodeNum).setID(nd.getID());
		    refinement.nodes.get(nodeNum).setName(nd.getName());

		    refinement.forbiddenGatewayIDs.append(", " + nd.getID());

		    //query.nodes.get(i).id = lrs.getInt("id");
		    // we have to update all paths, edges in which this node is incident
		    refinement.updateEdgesWithDestination(currentNode,nd);
		    refinement.updateEdgesWithSource(currentNode,nd);
		    // updated on 6.1.2011 to fix a big all update methods should be replaced with ID counterparts
		    refinement.updatePathsWithDestinationID(currentNode,refinement.nodes.get(nodeNum));
		    refinement.updatePathsWithSourceID(currentNode,refinement.nodes.get(nodeNum));

//		    DO the same thing with negatives
		    refinement.updateNegativeEdgesWithDestination(currentNode,nd);
		    refinement.updateNegativeEdgesWithSource(currentNode,nd);
		    refinement.updateNegativePathsWithDestination(currentNode,nd);
		    refinement.updateNegativePathsWithSource(currentNode,nd);


		    // added on 9th of July 2008 
		    refinement.updateExcludeExpression(currentNode.getName(), nd.getID());

		    refinement.updateUnDirectedAssociationPathSource(currentNode,refinement.nodes.get(nodeNum));
		    refinement.updateUnDirectedAssociationPathDestination(currentNode,refinement.nodes.get(nodeNum));

		    refinedQueries.add(refinement);
		}
		//for (int u =0; u < refinedQueries.size();u++)
		if (refinedQueries.size()> 0)
		{
		    for (QueryGraph qg : refinedQueries)
			if (!this.intermediateRefinements.contains(qg))
			    this.intermediateRefinements.add(0, qg);
		}
		else if (this.intermediateRefinements.size() == 0 && this.finalRefinements.size()==0)
		{
		    // at any time an event fails to be resolved
		    // the whole query is terminated with failure.
		    log.error("GateWay "+ currentNode.getName() +" failed to find a binding");
		    log.error("Terminating the query processing against model " + modelID + " due to unbound objects");
		    this.intermediateRefinements.clear();
		    this.finalRefinements.clear();
		    return;
		}

		break;
	    }
	    // here we have a list of versions for each event node
	    // we have to have the cartesian product
	    // the order is inherint in the order of lists of refined queries

	    if (!queryHasBeenRefined && !this.finalRefinements.contains(query))
		this.finalRefinements.add(query);
	    

	}//;

    }
    @Override
    protected void resolveGenericShape(String modelID)
    {
	if (this.intermediateRefinements.size()==0)
	    return;

	refreshModel(modelID);
	List<GraphObject> AllInnerNodes=null;

	do
	{
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();
//	    System.out.println("RESOLVE GENERIC Intermediate query list size ="+intermediateRefinements.size());
//	    String shapeMatch ="";
	    boolean queryHasBeenRefined = false;
	    List<QueryGraph> refinedQueries;
	    //refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    refinedQueries = new ArrayList<QueryGraph>();
	    QueryGraph refinement=null;
	    List<GraphObject> succsFromQueryGraph;
	    List<GraphObject> predsFromQueryGraph;
	    //ArrayList<GraphObject> pathSuccsFromQueryGraph;
	    //ArrayList<GraphObject> pathPredsFromQueryGraph;
	    List<GraphObject> intersectionResult= new ArrayList<GraphObject>();
	    List<GraphObject> tmp;
	    //SequenceFlow currentEdge;
	    GraphObject currentNode;


	    //System.out.println("CALLING RESOLVE EDGES");
	    int i;
	    // Optimization step added on 7th of July 2007
	    int sz3 = query.nodes.size();
	    for ( i = 0; i < sz3 ;i++)
		// End of optimization step
	    {

		currentNode = query.nodes.get(i);
		if (currentNode.type == GraphObjectType.ACTIVITY && currentNode.type2.equals("GENERIC SHAPE"))
		{

		    predsFromQueryGraph = query.getPredecessorsFromQueryGraph(currentNode);
		    succsFromQueryGraph = query.getSuccessorsFromQueryGraph(currentNode);
		    if (predsFromQueryGraph.size()> 0)
		    {
			tmp = pgad.getSuccessors(predsFromQueryGraph.get(0));
//			tmp = currentProcess.getSuccessorsFromGraph(predsFromQueryGraph.get(0));
			intersectionResult = Utilities.intersect(tmp, tmp);
			// update the common bindings
//			for (GraphObject intNode : intersectionResult)
//			query.updateSuccCommonBinding(predsFromQueryGraph.get(0).toString(), currentNode.toString(), intNode.toString());

			int sz4 = predsFromQueryGraph.size();
			for (int j =1; j < sz4 && intersectionResult.size() > 0;j++)
			    // end of optimization step
			{
			    tmp = pgad.getSuccessors(predsFromQueryGraph.get(j));
//			    for (GraphObject intNode : tmp)
//			    query.updateSuccCommonBinding(predsFromQueryGraph.get(j).toString(), currentNode.toString(), intNode.toString());

			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			}
			
			if (intersectionResult.size() == 0 && containsUnresolvedNode(predsFromQueryGraph))
			{
			    if (AllInnerNodes == null)
				{
				    AllInnerNodes = pgad.getNodesByType(GraphObjectType.ACTIVITY);
				    AllInnerNodes.addAll(pgad.getNodesByType(GraphObjectType.EVENT));
				    AllInnerNodes.addAll(pgad.getNodesByType(GraphObjectType.GATEWAY));
				    
				}
			    intersectionResult.clear();
			    try
			    {
				for (GraphObject j : AllInnerNodes)

				    intersectionResult.add((GraphObject) j.clone());
			    } catch (CloneNotSupportedException e)
			    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }

			}
			else if (intersectionResult.size()== 0) // a variable node fails to resolve so terminate this query
			    return ; 
			
			
			
		    }
		    if (succsFromQueryGraph.size() >0)
		    {
			tmp = pgad.getPredecessors(succsFromQueryGraph.get(0));
			if (intersectionResult.size() >0)
			    // as i reach here with intersection result empty, this means i didnt find predecessors in query graph
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			else
			    intersectionResult = Utilities.intersect(tmp, tmp);
//			for (GraphObject intNode : intersectionResult)
//			query.updatePredCommonBinding(succsFromQueryGraph.get(0).toString(), currentNode.toString(), intNode.toString());

			//	iterate to find the common nodes
			// optimization step added on 7th July 2007
			int sz5 = succsFromQueryGraph.size();
			for (int j =1; j < sz5 && intersectionResult.size() > 0;j++)
			    // End of optimization step
			{
			    tmp = pgad.getPredecessors(succsFromQueryGraph.get(j));
//			    for (GraphObject intNode : tmp)
//			    query.updatePredCommonBinding(succsFromQueryGraph.get(j).toString(), currentNode.toString(), intNode.toString());
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			}
			if (intersectionResult.size() == 0 && containsUnresolvedNode(succsFromQueryGraph))
			{
			    if (AllInnerNodes == null)
			    {
				AllInnerNodes = pgad.getNodesByType(GraphObjectType.ACTIVITY);
				AllInnerNodes.addAll(pgad.getNodesByType(GraphObjectType.EVENT));
				AllInnerNodes.addAll(pgad.getNodesByType(GraphObjectType.GATEWAY));
			    }
			    intersectionResult.clear();
			    try
			    {
				for (GraphObject j : AllInnerNodes)

				    intersectionResult.add((GraphObject) j.clone());
			    } catch (CloneNotSupportedException e)
			    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			}
			else if (intersectionResult.size()== 0) // a variable node fails to resolve so terminate this query
			    return ; 
			
		    }
		    // no sequence flow edges at all either incoming or outgoing
		    if (predsFromQueryGraph.size()==0 && succsFromQueryGraph.size()==0)
			// we have to try out all activities
		    { // this parent needs to be calculated once for all invocations
			// as it is a constant per instance
			if (AllInnerNodes == null)
			{
			    AllInnerNodes = pgad.getNodesByType(GraphObjectType.ACTIVITY);
			    AllInnerNodes.addAll(pgad.getEventsByType("2"));
			    AllInnerNodes.addAll(pgad.getNodesByType(GraphObjectType.GATEWAY));
			}


			intersectionResult.clear();
			try
			{
			    for (GraphObject j : AllInnerNodes)

				intersectionResult.add((GraphObject) j.clone());
			} catch (CloneNotSupportedException e)
			{
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
		    }


		    //return;
		    queryHasBeenRefined = true;
		 // Apply reduction of possibilities
		    intersectionResult = reducePossibilities(intersectionResult, query, currentNode);

		    for (GraphObject intersectionNode : intersectionResult)
		    {	
			if ((query.forbiddenActivityIDs.toString().contains(intersectionNode.getID()) && intersectionNode.type == GraphObjectType.ACTIVITY) ||
				(query.forbiddenGatewayIDs.toString().contains(intersectionNode.getID()) && intersectionNode.type == GraphObjectType.GATEWAY) ||
				(query.forbiddenEventIDs.toString().contains(intersectionNode.getID()) && intersectionNode.type == GraphObjectType.EVENT)
			)
			    intersectionNode.setName(intersectionNode.getName() + "$$");


		    }
		    boolean matchFound = false;
		    for ( GraphObject intersectionNode : intersectionResult)
		    {

			// for each of the suggested matched create a new 
			// instance of the query graph
			// 
			if (intersectionNode.getName().endsWith("$$")) 
			{
				if (query.genericJoinBindings.toString().contains(String.valueOf(intersectionNode.getID())))
						continue;
				if (query.genericSplitBindings.toString().contains(String.valueOf(intersectionNode.getID())))
						continue;
				//			    we have to apply the resolve to nothing principle
			    if (query.lookupPredBindings(currentNode.toString(), intersectionNode.toString()))
				continue;
			    if (query.lookupSuccBindings(currentNode.toString(), intersectionNode.toString()))
				continue;
//			    shapeMatch += intersectionNode.toString() +",";
			    if (allowGenericShapeToEvaluateToNone)
				refinement = handleRefineToNone(query, currentNode);



			}
			else
			{
			    refinement = handleRefineToNode(query, currentNode, intersectionNode);
			    for (GraphObject nn : predsFromQueryGraph)
			    {
				query.updateSuccCommonBinding(nn.toString(), currentNode.toString(), intersectionNode.toString());
				refinement.updateSuccCommonBinding(nn.toString(), currentNode.toString(), intersectionNode.toString());
			    }
			    for (GraphObject nn : succsFromQueryGraph)
			    {
				query.updatePredCommonBinding(nn.toString(), currentNode.toString(), intersectionNode.toString());
				refinement.updatePredCommonBinding(nn.toString(), currentNode.toString(), intersectionNode.toString());
			    }
			}
			
//			shapeMatch += intersectionNode.toString() +",";
			if (refinement != null)
			{
			    matchFound = true;
			    refinedQueries.add(refinement);
			}
			

		    }
		    if (!matchFound)
		    {
			//this is a new addition to resolve the node to nothing 28 7 2008
			if (allowGenericShapeToEvaluateToNone)
			    refinedQueries.add(handleRefineToNone(query, currentNode));

		    }
		    if (refinedQueries.size()> 0)
			for (QueryGraph qg : refinedQueries)
			    if (!this.intermediateRefinements.contains(qg))
				this.intermediateRefinements.add(0, qg);

		    break;
		}
	    }
	    if (!queryHasBeenRefined && !this.finalRefinements.contains(query))
		this.finalRefinements.add(query);

	}
	while (this.intermediateRefinements.size() > 0);

	
    }
    @Override
    protected void resolveGenericSplit(String modelID)
    {
	resolveGenericSplitJoin(modelID,"SPLIT");
    }
    @Override
    protected void resolveGenericJoin(String modelID)
    {
	resolveGenericSplitJoin(modelID,"JOIN");
    }
    protected void resolveGenericSplitJoin(String modelID, String tt)
    {
	if (this.intermediateRefinements.size() == 0) 
	    return;
	refreshModel(modelID);
	List<GraphObject> AllGateWayNodes=null;
	do
	{
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	    boolean queryHasBeenRefined = false;
	    List<QueryGraph> refinedQueries;
	    //refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    refinedQueries = new ArrayList<QueryGraph>();
	    QueryGraph refinement;
	    List<GraphObject> succsFromQueryGraph;
	    List<GraphObject> predsFromQueryGraph;
	    //ArrayList<GraphObject> pathSuccsFromQueryGraph;
	    //ArrayList<GraphObject> pathPredsFromQueryGraph;
	    List<GraphObject> intersectionResult= new ArrayList<GraphObject>();
	    List<GraphObject> tmp;
	    //SequenceFlow currentEdge;
	    GraphObject currentNode;


	    //System.out.println("CALLING RESOLVE EDGES");
	    int sz3 = query.nodes.size();
	    for (int i = 0; i < sz3 ;i++)
	    {

		currentNode = query.nodes.get(i);
		if (currentNode.type == GraphObjectType.GATEWAY && currentNode.type2.equals("GENERIC "+tt))
		{

		    predsFromQueryGraph = query.getPredecessorsFromQueryGraph(currentNode);
		    succsFromQueryGraph = query.getSuccessorsFromQueryGraph(currentNode);

		    if (predsFromQueryGraph.size()> 0)
		    {
			
			tmp = currentProcess.getSuccessorsFromGraph(predsFromQueryGraph.get(0), GraphObjectType.GATEWAY);
			intersectionResult = Utilities.intersect(tmp, tmp);

			//	iterate to find the common nodes
			// Optimization step added on 7th July 2007
			int sz4 = predsFromQueryGraph.size();
			for (int j =1; j < sz4 && intersectionResult.size() > 0;j++)
			    // end of optimization step
			{
			    tmp = currentProcess.getSuccessorsFromGraph(predsFromQueryGraph.get(j),  GraphObjectType.GATEWAY);
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			}
			// a variable node fails to resolve so terminate this query
			if (intersectionResult.size() == 0 && containsUnresolvedNode(predsFromQueryGraph))
			{
			    if (AllGateWayNodes == null) {
				AllGateWayNodes = currentProcess.getGateways(tt);
			    }
			    intersectionResult.clear();
			    intersectionResult.addAll(AllGateWayNodes);
			}
			else if (intersectionResult.size() == 0) // Query has no matches
			    return;
		    }
		    if (succsFromQueryGraph.size() >0)
		    {
			tmp = currentProcess.getPredecessorFromGraph(succsFromQueryGraph.get(0),  GraphObjectType.GATEWAY);
			if (intersectionResult.size() >0)
			    // as i reach here with intersection result empty, this means i didnt find predecessors in query graph
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			else
			    intersectionResult = Utilities.intersect(tmp, tmp);

			//	iterate to find the common nodes
			// optimization step added on 7th July 2007
			int sz5 = succsFromQueryGraph.size();
			for (int j =1; j < sz5 && intersectionResult.size() > 0;j++)
			    // End of optimization step
			{
			    tmp = currentProcess.getPredecessorFromGraph(succsFromQueryGraph.get(j),  GraphObjectType.GATEWAY);
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			}
			if (intersectionResult.size() == 0 && containsUnresolvedNode(succsFromQueryGraph))
			{
			    if (AllGateWayNodes == null) {
				AllGateWayNodes = currentProcess.getGateways(tt);

			    }
			    intersectionResult.clear();
			    intersectionResult.addAll(AllGateWayNodes);
			}
			else if (intersectionResult.size() == 0) // this means there are predecessors in the query graph that have not been resolved yet
			    return;
		    }
		    // no sequence flow edges at all either incoming or outgoing
		    if (predsFromQueryGraph.size()==0 && succsFromQueryGraph.size()==0)
			// we have to try out all activities
		    {
			if (AllGateWayNodes == null)
			{
			    AllGateWayNodes = currentProcess.getGateways(tt);

			}
			intersectionResult.clear();
			intersectionResult.addAll(AllGateWayNodes);
		    }
		    //return;
		    queryHasBeenRefined = true;
		    // Optimization step added on 7th July 2007
		    // Apply reduction of possibilities
		    intersectionResult = reducePossibilities(intersectionResult, query, currentNode);
		    int sz6 = intersectionResult.size();
		    for (int mm =0; mm< sz6;mm++)
			if (query.forbiddenGatewayIDs.toString().contains(intersectionResult.get(mm).getID()))
			
			    intersectionResult.get(mm).setName(intersectionResult.get(mm).getName() + "$$");

		    for ( int z = 0; z < sz6;z++)
		    {

			// for each of the suggested matched create a new 
			// instance of the query graph
			// 
			if (intersectionResult.get(z).getName().endsWith("$$")) 
			    continue;
			if (!intersectionResult.get(z).type2.endsWith(tt)) 
			    continue;

			refinement = (QueryGraph)query.clone();
			//refinement.removeEdgesWithDestination(currentNode);
			//refinement.removeEdgesWithSource(currentNode);
			refinement.addInfoLog("Generic "+ tt +" " + currentNode.getName() + " has been bound to" + intersectionResult.get(z).getName() + " " + intersectionResult.get(z).toString());
			refinement.remove(currentNode);
			//refinement.removePathsWithDestination(currentNode);
			//refinement.removePathsWithSource(currentNode);

			// we have to update negative edges and path links

			if (!intersectionResult.get(z).getName().endsWith("$$"))
			{
			    intersectionResult.get(z).setBoundQueryObjectID(currentNode.getID());
			    refinement.add(intersectionResult.get(z));
			    currentNode.setID(intersectionResult.get(z).getID());
			    //	currentNode.type1 = intersectionResult.get(z).type1;
			    //currentNode.type2 = intersectionResult.get(z).type2;
			    //					 update the forbidden ids
			    //if (intersectionResult.get(z).type1.equals("Activity"))
			    //	refinement.forbiddenActivityIDs.append(", " + intersectionResult.get(z).id); 
			    if (intersectionResult.get(z).type == GraphObjectType.GATEWAY)
			    	refinement.forbiddenGatewayIDs.append(", " + intersectionResult.get(z).getID());
			    if (tt.equals("JOIN"))
				refinement.genericJoinBindings.append(","+ intersectionResult.get(z).getID());
			    else
				refinement.genericSplitBindings.append(","+ intersectionResult.get(z).getID());
			    
			    //else if (intersectionResult.get(z).type1.equals("Event"))
			    //	refinement.forbiddenEventIDs.append(", " + intersectionResult.get(z).id);

			    refinement.updateNegativeEdgesWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updateNegativePathsWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updateNegativeEdgesWithSource(currentNode, intersectionResult.get(z));
			    refinement.updateNegativePathsWithSource(currentNode, intersectionResult.get(z));

			    refinement.updateEdgesWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updateEdgesWithSource(currentNode, intersectionResult.get(z));

			    refinement.updatePathsWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updatePathsWithSource(currentNode, intersectionResult.get(z));

			    refinement.updateUnDirectedAssociationPathSource(currentNode,intersectionResult.get(z));
			    refinement.updateUnDirectedAssociationPathDestination(currentNode,intersectionResult.get(z));
			}
//			added on 9th of July 2008 
			refinement.updateExcludeExpression(currentNode.getName(), currentNode.toString());
			refinedQueries.add(refinement);
		    }
		    if (refinedQueries.size()> 0)
			for (QueryGraph qg : refinedQueries)
			    if (!this.intermediateRefinements.contains(qg))
				this.intermediateRefinements.add(0, qg);
		    /*else
				{
					// at any time a variable node fails to be resolved
					// the whole query is terminated with failure.
					Utilities.intermediateRefinements.clear();
					Utilities.finalRefinements.clear();
					return;
				}*/
		    break;
		}
	    }
	    if (!queryHasBeenRefined)
		this.finalRefinements.add(query);
	}
	while (this.intermediateRefinements.size()> 0);

	
    }
    @Override
    protected void resolveAnonymousActivities(String modelID)
    {
	if (0 == this.intermediateRefinements.size()) 
	    return;
	refreshModel(modelID);

	List<GraphObject> AllActivities = null;

	do
	{
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	    boolean queryHasBeenRefined = false;
	    List<QueryGraph> refinedQueries;
	    //refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    refinedQueries = new ArrayList<QueryGraph>();
	    QueryGraph refinement;
	    List<GraphObject> succsFromQueryGraph = new ArrayList<GraphObject>(0);
	    List<GraphObject> predsFromQueryGraph = new ArrayList<GraphObject>(0);
	    //ArrayList<GraphObject> pathSuccsFromQueryGraph;
	    //ArrayList<GraphObject> pathPredsFromQueryGraph;
	    List<GraphObject> intersectionResult= new ArrayList<GraphObject>();
	    List<GraphObject> tmp;
	    List<DataObject> incomingAssociation;
	    List<DataObject> outgoingAssociation;

	    //log.debug("CALLING RESOLVE EDGES");
	    for (GraphObject currentNode : query.nodes)
	    {
		if (!currentNode.getName().startsWith("@")) continue;

//		predsFromQueryGraph = query.getPredecessorsFromQueryGraph(currentNode);
//		succsFromQueryGraph = query.getSuccessorsFromQueryGraph(currentNode);
		// added on 2.6.2010 to include data associaitons
		incomingAssociation = getIncomingNonAnonymousDataObjects(query, currentNode);
		outgoingAssociation = getOutgoingNonAnonymousDataObjects(query, currentNode);
		
//		if (predsFromQueryGraph.size()> 0)
//		{
//		    tmp = currentProcess.getSuccessorsFromGraph(predsFromQueryGraph.get(0), currentNode.type);
//		    intersectionResult = Utilities.intersect(tmp, tmp);
//
//		    //	iterate to find the common nodes
//		    // Optimization step added on 7th July 2007
//		    int sz4 = predsFromQueryGraph.size();
//		    for (int j =1; j < sz4 && intersectionResult.size() > 0;j++)
//			// end of optimization step
//		    {
//			tmp = currentProcess.getSuccessorsFromGraph(predsFromQueryGraph.get(j),  currentNode.type);
//			intersectionResult = Utilities.intersect(intersectionResult, tmp);
//		    }
//		    // a variable node fails to resolve so terminate this query
//		    if (intersectionResult.size()== 0) 
//			//return ;
//			resolveAnonymousActivities(modelID);
//		}
//		if (succsFromQueryGraph.size() >0)
//		{
//		    tmp = currentProcess.getPredecessorFromGraph(succsFromQueryGraph.get(0),  currentNode.type);
//		    if (intersectionResult.size() >0)
//			// as i reach here with intersection result empty, this means i didnt find predecessors in query graph
//			intersectionResult = Utilities.intersect(intersectionResult, tmp);
//		    else
//			intersectionResult = Utilities.intersect(tmp, tmp);
//
//		    //	iterate to find the common nodes
//		    // optimization step added on 7th July 2007
//		    int sz5 = succsFromQueryGraph.size();
//		    for (int j =1; j < sz5 && intersectionResult.size() > 0;j++)
//			// End of optimization step
//		    {
//			tmp = currentProcess.getPredecessorFromGraph(succsFromQueryGraph.get(j),  currentNode.type);
//			intersectionResult = Utilities.intersect(intersectionResult, tmp);
//		    }
//		    if (intersectionResult.size()== 0) 
//			//return;
//			resolveAnonymousActivities(modelID);
//		}
		// Association to data objects
		if (incomingAssociation.size() > 0)
		{
		    DataObject x = incomingAssociation.get(0);
		    tmp = currentProcess.getReadingActivities(x, x.getState());
		    if (intersectionResult.size() >0)
			// as i reach here with intersection result empty, this means i didnt find predecessors in query graph
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    else
			intersectionResult = Utilities.intersect(tmp, tmp);
		    
		    int sz5 = incomingAssociation.size();
		    for (int j =1; j < sz5 && intersectionResult.size() > 0;j++)
			// End of optimization step
		    {
			tmp = currentProcess.getReadingActivities(incomingAssociation.get(j),incomingAssociation.get(j).getState());
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    }
		    if (intersectionResult.size()== 0) 
			//return;
			resolveAnonymousActivities(modelID);
		}
		if (outgoingAssociation.size() > 0)
		{
		    DataObject x = outgoingAssociation.get(0);
		    tmp = currentProcess.getUpdatingActivities(x, x.getState());
		    if (intersectionResult.size() >0)
			// as i reach here with intersection result empty, this means i didnt find predecessors in query graph
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    else
			intersectionResult = Utilities.intersect(tmp, tmp);
		    
		    int sz5 = outgoingAssociation.size();
		    for (int j =1; j < sz5 && intersectionResult.size() > 0;j++)
			// End of optimization step
		    {
			tmp = currentProcess.getUpdatingActivities(outgoingAssociation.get(j),outgoingAssociation.get(j).getState());
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    }
		    if (intersectionResult.size()== 0) 
			//return;
			resolveAnonymousActivities(modelID);
		}
		// no sequence flow edges at all either incoming or outgoing
		if (predsFromQueryGraph.size()==0 && succsFromQueryGraph.size()==0 && incomingAssociation.size() == 0 && outgoingAssociation.size() == 0)
		    // we have to try out all activities
		{
		    if (AllActivities == null)
		    {
			AllActivities = pgad.getNodesByType(GraphObjectType.ACTIVITY);

		    }

		    intersectionResult.clear();
		    try
		    {
			for (GraphObject j : AllActivities)

			    intersectionResult.add((GraphObject) j.clone());
		    } catch (CloneNotSupportedException e)
		    {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
		//return;
		queryHasBeenRefined = true;
		intersectionResult = reducePossibilities(intersectionResult, query, currentNode);

		for (GraphObject currObj : intersectionResult)
		    if (query.forbiddenActivityIDs.toString().contains(currObj.getID()))
			currObj.setName(currObj.getName() + "$$");

		for (GraphObject currObj : intersectionResult)
		{
		    // for each of the suggested matched create a new 
		    // instance of the query graph

		    if (currObj.getName().endsWith("$$")) 
			continue;
		    refinement = (QueryGraph)query.clone();
		    //refinement.removeEdgesWithDestination(currentNode);
		    //refinement.removeEdgesWithSource(currentNode);
		    refinement.remove(currentNode);
		    //refinement.removePathsWithDestination(currentNode);
		    //refinement.removePathsWithSource(currentNode);

		    // we have to update negative edges and path links

		    if (!currObj.getName().endsWith("$$"))
		    {
			// Added on 25.8.2010
			currObj.setBoundQueryObjectID(currentNode.getID());

			refinement.add(currObj);
			refinement.addInfoLog("Variable node "+ currentNode.getName() + " was bound to node " + currObj);
//			currentNode.setID(intersectionResult.get(z).getID());
			//					 update the forbidden ids
			currentNode.setID(currObj.getID());
			refinement.forbiddenActivityIDs.append(", " + currObj.getID()); 
			refinement.updateNegativeEdgesWithDestination(currentNode, currObj.getName());
			refinement.updateNegativePathsWithDestination(currentNode, currObj.getName());
			refinement.updateNegativeEdgesWithSource(currentNode, currObj.getName());
			refinement.updateNegativePathsWithSource(currentNode, currObj.getName());

			refinement.updateEdgesWithDestination(currentNode, currObj.getName());
			refinement.updateEdgesWithSource(currentNode, currObj.getName());

			refinement.updatePathsWithDestination(currentNode, currObj.getName());
			refinement.updatePathsWithSource(currentNode, currObj.getName());
			
//			 update associations
			refinement.updateAssociationsFromFlowObject(currentNode,currObj);
			refinement.updateAssociationsToFlowObject(currentNode,currObj);

			refinement.updateUnDirectedAssociationPathSource(currentNode,currObj);
			refinement.updateUnDirectedAssociationPathDestination(currentNode,currObj);
		    }

		    // added on 9th of July 2008 
		    refinement.updateExcludeExpression(currentNode.getName(), currentNode.toString());
		    refinedQueries.add(refinement);
		} // for loop

		if (refinedQueries.size()> 0)
		{
		    for (QueryGraph qg : refinedQueries)
			if (!this.intermediateRefinements.contains(qg))
			    this.intermediateRefinements.add(0, qg);
		} else if (this.intermediateRefinements.size() == 0 && this.finalRefinements.size()==0)
		{
		    // at any time a variable node fails to be resolved
		    // the whole query is terminated with failure.
		    log.error("Variable node "+ currentNode.getName() +" failed to find a binding");
		    log.error("Terminating the query processing against model " + modelID + " due to unbound objects");
//		    this.intermediateRefinements.clear();
//		    this.finalRefinements.clear();
		    return;
		}
		break;
	    }
	    if (!queryHasBeenRefined)
		this.finalRefinements.add(query);
	}
	while (this.intermediateRefinements.size() > 0);

	
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
//	     nodeList=new StringBuffer();
//	    int pathIndexResult = lookupPathIndex(currentEdgep);
//	    if (pathIndexResult == NO_PATH_INDEX_ENTRY)
//	    {
//	    currentPath = findPath(currentEdgep, modelID);
		currentPath = pgad.evaluatePath(currentEdgep);
		
		// we have to update the index for the upcoming lookups
//	    }
//	    else if (pathIndexResult == PATH_ENTRY_NEGATIVE || pathIndexResult == PATH_ENTRY_NEGATIVE_SUBSET)
//	    {
//		currentPath = null;
//		// we have to update the index
//	    }
//	    else // there is an exact entry for the path
//	    {
//		currentPath = loadPrecomputedPath(currentEdgep);
//		// we have to load the answer from the index
//	    }
	    
	    if (currentPath == null || currentPath.nodes.size()==0)
	    {
		query.addErrorLog("Evaluation of "+currentEdgep.toString() +" failed !");
		// update the path index
//		updatePathIndex(currentEdgep,currentPath, pathIndexResult);
		return false;
	    }
	    else
	    {
		// we have to remove this matched edge
		// insert nodes and edges from the matched path
		// call it one more time
//		query.paths.remove(currente);
		pathFound = true;
		//update the Path index
		
		// Optimization step added on 7th July 2007
		handleEvaluatedPath(query, currentPath, currentEdgep);
	    }
	    // we have to update all exclude path statements
	    query.updateExcludeExpression(currentEdgep.label, currentPath.nodes.toString().replace("[", "").replace("]", ""));
//	    updatePathIndex(currentEdgep,currentPath, pathIndexResult);
	    break;
	}
	if (pathFound) 
	    return resolvePaths(query, modelID);
	
	return false;
    }
    protected void handleEvaluatedPath(QueryGraph query,
	    ProcessGraph currentPath, Path currentEdgep)
    {
	int sz8 = currentPath.nodes.size();
	for (int h = 0; h < sz8;h++)
	{
	    query.add(currentPath.nodes.get(h));

	}
	// here we handle the undirected Associations
	List<DataObject> data = query.getDataPathAssociation(currentEdgep);
	List<Association> ass;
	for (DataObject dob : data)
	{
	    for (GraphObject obj :currentPath.nodes)
	    {
		ass = currentProcess.getIncomingAssociation(obj);
		for (Association as : ass)
		{
		    if (as.frmDataObject.doID == dob.doID)
		    {
			query.associations.add(as);
		    }
		}
		ass = currentProcess.getOutgoingAssociation(obj);
		for (Association as : ass)
		{
		    if (as.toDataObject.doID == dob.doID)
		    {
			query.associations.add(as);
		    }
		}
	    }
	    GraphObject tmp = new GraphObject();
	    tmp.setID(dob.doID);
	    tmp.setName(dob.name);
	    tmp.type = GraphObjectType.DATAOBJECT;
	    tmp.type2 = dob.getState();
	    query.removeDataPathAssociation(tmp, currentEdgep);
	}

	for (SequenceFlow edge : currentPath.edges)
	    query.add(edge);
    }
    
    
    @Override
    protected boolean checkNegativePaths(QueryGraph query, String modelID)
    {
	// it will be the case that for a completley resolved query there is no chance that negative edges are not fulfilled.
	// TODO Auto-generated method stub
	return true;
    }
    @Override
    protected boolean resolveConcreteNodeID(QueryGraph query, String modelID)
    {
	refreshModel(modelID);
	GraphObject currentNode;
	this.intermediateRefinements.add(query);
	do
	{
	    
	// Optimization step added on 7th July 2007
	    QueryGraph refinement,refinement2= (QueryGraph) intermediateRefinements.remove(0).clone();    
	int sz11 = refinement2.nodes.size();
	boolean queryHasbeenRefined = false;
	for (int i = 0; i < sz11; i++)
	{
	    currentNode = refinement2.nodes.get(i);
	    
	    if ( currentNode.type != GraphObjectType.ACTIVITY || currentNode.type2.equals("GENERIC SHAPE")) 
		continue;
	    if (currentNode.isResolved())
		continue;
	    
	    if (!currentNode.getName().startsWith("@")) // this is a concrete node
	    {
		queryHasbeenRefined = true;
//		boolean nodeFound = false;
		List<GraphObject> candidates = new ArrayList<GraphObject>(10);
		for (GraphObject nd : pgad.getNodesByType(GraphObjectType.ACTIVITY))
		{
		    if (nd.type == GraphObjectType.ACTIVITY && Utilities.normalizeString(nd.getName()).equalsIgnoreCase(Utilities.normalizeString(currentNode.getName())))
		    {
			candidates.add(nd);
			
			
			// I have to break to give chance to other nodes? no, because u look for all possible matches
		    }
		    
		    
		}
		// optimization step depending on pgad
		candidates = reducePossibilities(candidates, refinement2, currentNode);
		if (candidates.size()==0)
		    return false;
		for (GraphObject nd : candidates)
		{
		    refinement = (QueryGraph) refinement2.clone();
		    refinement.addInfoLog("Concrete node "+ refinement2.nodes.get(i).getName() +" was bound to activity "+nd.getID());
//		    nodeFound = true;
		    refinement.nodes.get(i).setID(nd.getID());
		    refinement.nodes.get(i).setBoundQueryObjectID(currentNode.getID());
		    // added to track the binding from the query to the process graph object
		    currentNode.setBoundQueryObjectID(currentNode.getID());
//		    currentNode.setID(nd.getID());
		    // we have to update all paths, edges in which this node is incident
		    refinement.forbiddenActivityIDs.append("," + refinement.nodes.get(i).getID());
		    
//		    refinement.updateEdgesWithDestination(currentNode,currentNode.getName());
		    refinement.updateEdgesWithDestination(currentNode, nd);
		    
		    refinement.updateEdgesWithSource(currentNode, nd);
//		    refinement.updateEdgesWithSource(currentNode,currentNode.getName());
		    
		    
//		    refinement.updatePathsWithDestination(currentNode,currentNode.getName());
		    refinement.updatePathsWithDestination(currentNode, nd);
		    
//		    refinement.updatePathsWithSource(currentNode,currentNode.getName());
		    refinement.updatePathsWithSource(currentNode, nd);
		    // do the same thing with negative edges and paths
		    
//		    refinement.updateNegativeEdgesWithDestination(currentNode,currentNode.getName());
		    refinement.updateNegativeEdgesWithDestination(currentNode, nd);
		    
//		    refinement.updateNegativeEdgesWithSource(currentNode,currentNode.getName());
		    refinement.updateNegativeEdgesWithSource(currentNode, nd);
		    
//		    refinement.updateNegativePathsWithDestination(currentNode,currentNode.getName());
		    refinement.updateNegativePathsWithDestination(currentNode, nd);
		    
//		    refinement.updateNegativePathsWithSource(currentNode,currentNode.getName());
		    refinement.updateNegativePathsWithSource(currentNode, nd);

		    // update associations
		    refinement.updateAssociationsFromFlowObject(refinement.nodes.get(i),refinement.nodes.get(i));
		    refinement.updateAssociationsToFlowObject(refinement.nodes.get(i),refinement.nodes.get(i));

		    refinement.updateUnDirectedAssociationPathSource(refinement.nodes.get(i),refinement.nodes.get(i));
		    refinement.updateUnDirectedAssociationPathDestination(refinement.nodes.get(i),refinement.nodes.get(i));
		    this.intermediateRefinements.add(refinement);
		}
		
		    
	    }
	}
	if (!queryHasbeenRefined )
	    this.finalRefinements.add(refinement2);
	
	}while(intermediateRefinements.size() > 0);
	

	return true;

    }
    @Override
    protected void resolveEventNode(String modelID)
    {
	// We need to adjust the resolution technique, to reduce the number
	// of alternatives for the query graph. 25th May, 2007
	// the idea is to add something called the forbiden Id list

	//log.debug("Begin Resolve Event Node ID");
	if (this.intermediateRefinements.size() == 0) 
	    return ;
	refreshModel(modelID);
	do
	{
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	    boolean queryHasBeenRefined = false;
	    //ArrayList<ArrayList<QueryGraph>> refinedQueriesX;
	    List<QueryGraph> refinedQueries;
	    //refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    refinedQueries = new ArrayList<QueryGraph>();
	    QueryGraph refinement;
	    GraphObject currentNode;


	    List<GraphObject> preds, predsP;
	    List<GraphObject> succs, succsP;
	    List<GraphObject> resolvedEvents= new ArrayList<GraphObject>();

	    int sz12 = query.nodes.size();
	    for (int i = 0; i < sz12; i++)
	    {
		currentNode = query.nodes.get(i);
		if (currentNode.type != GraphObjectType.EVENT) 
		    continue;
		if (currentNode.isResolved()) 
		    continue;
		// reaching the following line means that there are still unresolved event nodes
		//refinedQueries = new ArrayList<QueryGraph>();
		queryHasBeenRefined = true;
		for (GraphObject nd : pgad.getEventsByType(currentNode.type2))
//		for (GraphObject nd : currentProcess.getEvents(Integer.valueOf(currentNode.type2)))
		{

		    boolean notThis = false;
		    if (query.forbiddenEventIDs.toString().contains(nd.getID()))
		    {
//			System.out.println("### The investigated node id belongs to the forbidden ones");
			continue;
		    }

		    if (!currentNode.getName().startsWith("$#"))
		    {
			if (!currentNode.getName().equals(nd.getName()))
			{
			    System.out.println("### query node and investigated node names do not match");
			    continue;
			}
		    }
//		    The following check was commented out because it is redundant
//		    // we are looking for a specific node type
//		    if (currentNode.type2.length() > 0)
////			if (currentNode.type2.length()==1 && !currentNode.type2.endsWith(currentNode.type2))
////			    continue;
//			if (!nd.type2.endsWith(currentNode.type2))
//			{
//			    System.out.println("### event types are not the same ");
//			    continue;
//			}
		    preds = query.getPredecessorsFromQueryGraph(currentNode);
//		    predsP = currentProcess.getPredecessorsFromGraph(nd);
		    predsP = pgad.getPredecessors(nd);
		    for (GraphObject pre :preds)
		    {
			//if (pre.getID() != 0 && !predsP.contains(pre))
			if (!pre.getID().equals("0") && !predsP.contains(pre))
			{
			    System.out.println("### Condition: !pre.getID().equals(\"0\") && !predsP.contains(pre) is true");
			    notThis = true;
			    break;
			}
//			for (GraphObject preP : predsP)
//			{
//			if (pre.type == )
//			}

		    }
		    succs = query.getSuccessorsFromQueryGraph(currentNode);
		    succsP = currentProcess.getSuccessorsFromGraph(nd);
		    for (GraphObject suc :succs)
		    {
			//if (suc.getId() != 0 && !succsP.contains(suc))
			if (!suc.getID().equals("0") && !succsP.contains(suc))
			{
			    System.out.println("### Condition: !suc.getID().equals(\"0\") && !succsP.contains(suc) is true");
			    notThis = true;
			    break;
			}
//			for (GraphObject preP : predsP)
//			{
//			if (pre.type == )
//			}

		    }
		    if (!notThis)
			resolvedEvents.add(nd);

		}
		// reduce possibilities based on the pgad
		resolvedEvents = reducePossibilities(resolvedEvents, query, currentNode);
		
		for (GraphObject nd :resolvedEvents)
		{
		    //currentNode.name = lrs.getString("name");
		    refinement = (QueryGraph)query.clone();
		    refinement.addInfoLog("Event " + currentNode.getName() + " was bound event " + nd.getID());
		    refinement.nodes.get(i).setID(nd.getID());
		    refinement.nodes.get(i).setName(nd.getName());
		    // added on 17.12.08 to handle Oryx processes quering
		    refinement.nodes.get(i).type2 = nd.type2;
		    refinement.nodes.get(i).setBoundQueryObjectID(currentNode.getID());
		    refinement.updateUnDirectedAssociationPathSource(currentNode,refinement.nodes.get(i));
		    refinement.updateUnDirectedAssociationPathDestination(currentNode,refinement.nodes.get(i));
		    refinement.updatePathsWithDestinationID(currentNode,refinement.nodes.get(i));
		    
		    refinement.updatePathsWithSourceID(currentNode,nd);
		    // added on 25.8.2010
//		    currentNode.setBoundQueryObjectID(currentNode.getID());
//		    currentNode.setID(nd.getID());
//		    currentNode.type2 = nd.type2;
		    
		    // Update the forbidden ids list
		    refinement.forbiddenEventIDs.append(", " + nd.getID());
		    //query.nodes.get(i).id = lrs.getInt("id");
		    // we have to update all paths, edges in which this node is incident
		    refinement.updateEdgesWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateEdgesWithSource(currentNode,refinement.nodes.get(i).getName());
		    
		    // DO the same thing with negatives
		    refinement.updateNegativeEdgesWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateNegativeEdgesWithSource(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateNegativePathsWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateNegativePathsWithSource(currentNode,refinement.nodes.get(i).getName());
		    
		    // do it also for associations
		    refinement.updateAssociationsFromFlowObject(currentNode, refinement.nodes.get(i));
		    refinement.updateAssociationsToFlowObject(currentNode, refinement.nodes.get(i));
		    
		    refinedQueries.add(refinement);

		}
		//for (int u =0; u < refinedQueries.size();u++)
		if (refinedQueries.size()> 0)
		    this.intermediateRefinements.addAll(0,refinedQueries);
		else if (this.intermediateRefinements.size() == 0 && this.finalRefinements.size()==0)
		{
		    // at any time an event fails to be resolved
		    // the whole query is terminated with failure.
		    System.out.println("##################### ERROR");
		    log.error("Event "+ currentNode.getName() + " " + currentNode.type2 + " failed to find a binding");
		    System.out.println("##################### ERROR");
		    log.error("Terminating the query processing against model " + modelID + " due to unbound objects");

//		    this.intermediateRefinements.clear();
//		    this.finalRefinements.clear();

		    return ;
		}

		//refinedQueriesX.add(refinedQueries);
		break;
	    }
	    // here we have a list of versions for each event node
	    // we have to have the cartesian product
	    // the order is inherint in the order of lists of refined queries

	    if (!queryHasBeenRefined)
		this.finalRefinements.add(query);
	}
	while (this.intermediateRefinements.size() > 0);
	
    }
    @Override
    protected boolean checkNegativeEdges(QueryGraph qry, String modelID)
    {
	// it will be the case that for a completley resolved query there is no chance that negative edges are not fulfilled.
	// TODO Auto-generated method stub
	return true;
    }
    @Override
    protected boolean resolveConcreteDataObjectID(QueryGraph qry, String modelID)
    {
	if (intermediateRefinements.size() == 0)
	    return true;
	refreshModel(modelID);

	DataObject currentDob;
	QueryGraph refinement;
	do
	{
	    QueryGraph query2 = (QueryGraph)this.intermediateRefinements.remove(0).clone();
	    boolean queryHasBeenRefined= false;
	    int sz11 = query2.dataObjs.size();
	    for (int i = 0; i < sz11; i++)
	    {
		currentDob = query2.dataObjs.get(i);
		if (currentDob.isResolved())
		    continue;
		boolean dobFound = false;
		if (!currentDob.name.startsWith("@")) // this is a concrete node
		{
		    queryHasBeenRefined =true;

		    for (DataObject nd : currentProcess.dataObjs)
		    {
			boolean checkStateMatch = false;
			if (!currentDob.getState().startsWith("?"))
			    checkStateMatch = true;
			if (nd.name.trim().equalsIgnoreCase(currentDob.name.trim()))
			{
			    if (checkStateMatch && !nd.getState().equalsIgnoreCase(currentDob.getState()))
				continue;
			    
			    refinement = (QueryGraph)query2.clone();
			    refinement.addInfoLog("Concrete dataobject "+ refinement.dataObjs.get(i).name +" was bound to Data Object "+nd.doID);
			    dobFound = true;
			    refinement.dataObjs.get(i).doID = nd.doID;
			    refinement.dataObjs.get(i).setState(nd.getState());
			    // we have to update all paths, edges in which this node is incident
			    refinement.forbiddenDataObjects.append("," + refinement.dataObjs.get(i).doID);

			    refinement.updateAssociationsFromDataObject(currentDob,refinement.dataObjs.get(i));
			    refinement.updateAssociationsToDataObject(currentDob,refinement.dataObjs.get(i));

//			    if (!validateDataObjectAssociations(refinement.dataObjs.get(i), refinement))
//				return false;
			    //TODO : Resolve ? states here

			    refinement.updateUndirectAssociationWithDataObject(currentDob,refinement.dataObjs.get(i));
			    this.intermediateRefinements.add(refinement);

			}
		    }
		    if (!dobFound)
			return false;
		}
	    }
	    if (!queryHasBeenRefined)
		this.finalRefinements.add(query2);
	}while (intermediateRefinements.size() > 0);
	return true;
    }
    @Override
    protected void resolveVariableDataObjects(String modelID)
    {
	if (0 == this.intermediateRefinements.size()) 
	    return;
	refreshModel(modelID);
	List<DataObject> AllDataObjects = null;

	do
	{
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	    boolean queryHasBeenRefined = false;
	    List<QueryGraph> refinedQueries;
	    //refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    refinedQueries = new ArrayList<QueryGraph>();
	    QueryGraph refinement;
	    List<GraphObject> readingFlowObjects;
	    List<GraphObject> updatingFlowObjects;

	    List<DataObject> intersectionResult= new ArrayList<DataObject>();
	    List<DataObject> tmp;

	    //log.debug("CALLING RESOLVE EDGES");
	    for (DataObject currentdob : query.dataObjs)
	    {
		if (!currentdob.name.startsWith("@")) continue;
		// preds is the set of activities that 
		readingFlowObjects = query.getReadingActivities(currentdob,"");
		updatingFlowObjects = query.getUpdatingActivities(currentdob,"");
		if (readingFlowObjects.size()> 0)
		{
		    tmp = currentProcess.getReadDataObjects(readingFlowObjects.get(0),"");
		    intersectionResult = Utilities.intersect(tmp, tmp);

		    //	iterate to find the common nodes
		    // Optimization step added on 7th July 2007
		    int sz4 = readingFlowObjects.size();
		    for (int j =1; j < sz4 && intersectionResult.size() > 0;j++)
			// end of optimization step
		    {
			tmp = currentProcess.getReadDataObjects(readingFlowObjects.get(j),"");
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    }
		    // a variable node fails to resolve so terminate this query
		    if (intersectionResult.size()== 0) 
			//return ;
			resolveVariableDataObjects(modelID);
		}
		if (updatingFlowObjects.size() >0)
		{
		    tmp = currentProcess.getUpdatedDataObjects(updatingFlowObjects.get(0),"");
		    if (intersectionResult.size() >0)
			// as i reach here with intersection result empty, this means i didnt find predecessors in query graph
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    else
			intersectionResult = Utilities.intersect(tmp, tmp);

		    //	iterate to find the common nodes
		    // optimization step added on 7th July 2007
		    int sz5 = updatingFlowObjects.size();
		    for (int j =1; j < sz5 && intersectionResult.size() > 0;j++)
			// End of optimization step
		    {
			tmp = currentProcess.getUpdatedDataObjects(updatingFlowObjects.get(j),"");
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    }
		    if (intersectionResult.size()== 0) 
			//return;
			//return resolveVariableDataObjects(modelID);
		    {
			queryHasBeenRefined = true;
			break;
		    }
		}
		// no sequence flow edges at all either incoming or outgoing
		if (readingFlowObjects.size()==0 && updatingFlowObjects.size()==0)
		    // we have to try out all activities
		{
		    if (AllDataObjects== null)
		    {
			AllDataObjects = currentProcess.dataObjs;
		    }

		    intersectionResult.clear();
		    intersectionResult.addAll(AllDataObjects);
		}
		//return;
		queryHasBeenRefined = true;

		for (DataObject currObj : intersectionResult)
		    if (query.forbiddenDataObjects.toString().contains(currObj.doID))
			currObj.name+= "$$";

		for (DataObject currObj : intersectionResult)
		{
		    // for each of the suggested matched create a new 
		    // instance of the query graph

		    if (currObj.name.endsWith("$$")) 
			continue;
		    refinement = (QueryGraph)query.clone();

		    refinement.remove(currentdob);



		    refinement.add(currObj);
		    refinement.addInfoLog("Variable dataObject  "+ currentdob.name + " was bound to dataObject " + currObj);
//		    currentNode.setID(intersectionResult.get(z).getID());
		    //					 update the forbidden ids
		    refinement.forbiddenDataObjects.append(", " + currObj.doID);

		    refinement.updateAssociationsFromDataObject(currentdob, currObj);
		    refinement.updateAssociationsToDataObject(currentdob, currObj);

		    refinement.updateUndirectAssociationWithDataObject(currentdob, currObj);

		    refinedQueries.add(refinement);
		} // for loop

		if (refinedQueries.size()> 0)
		{
		    for (QueryGraph qg : refinedQueries)
			if (!this.intermediateRefinements.contains(qg))
			    this.intermediateRefinements.add(0, qg);
		} else
		{
		    // at any time a variable node fails to be resolved
		    // the whole query is terminated with failure.
		    log.error("Variable dataObject "+ currentdob.name +" failed to find a binding");
		    log.error("Terminating the query processing against model " + modelID + " due to unbound objects");
		    this.intermediateRefinements.clear();
		    this.finalRefinements.clear();
		    return;
		}
		break;
	    }
	    if (!queryHasBeenRefined)
		this.finalRefinements.add(query);
	    
	}
	while (this.intermediateRefinements.size() > 0);

	
    }
    
    @Override
    public List<String> findRelevantProcessModels(QueryGraph query)
	    throws IOException
    {
	// This method will be subdivided
	// In the first case, if the pgad is available.
	// we will extend the knowledge to filter by paths and negative paths as well
		StringBuilder filterStatement = preparePocessGraphsFilterStatement(query);
		
		String currentModel;
		List<String> results = new ArrayList<String>();

		try
		{
    		    ResultSet matchingModels = Utilities.getDbStatemement().executeQuery(
		    	filterStatement.toString());
		    // for each returned model we have to resolve the query into the 
		    // maximal sub graph from that model that matches the query.
		    while (matchingModels.next())
		    {
		        // start from the query graph and refine

		        currentModel = matchingModels.getString("id");
		        results.add(currentModel);
		    }
		    log.info(String.valueOf(results.size()) +" process models are subject to checking.");	
		} catch (SQLException e)
		{
		    throw new IOException("A database error occurred...", e);
		}

		// log.debug("End Filter Database");
		return results;    }
    protected StringBuilder preparePocessGraphsFilterStatement(QueryGraph query)
    {
	StringBuilder filterStatement = new StringBuilder(1000);
	filterStatement.append("select \"ID\" from \"BPMN_GRAPH\".\"ORYX_MODEL\" where 1=1 ");
	// Start with known nodes

	for(GraphObject currentNode : query.nodes)
	{
	    if (!currentNode.getName().startsWith("@") 
		    && !currentNode.getName().startsWith("$#") 
		    && !currentNode.getName().startsWith("?"))
	    {
		// We can filter with this node  because it is known
		switch(currentNode.type) {
		case ACTIVITY:
		    filterStatement.append("and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" where trim(upper(\"NAME\")) =trim(upper('"+ currentNode.getName()+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //filterStatement += "and model.id in (select mod_id from activity where ucase(name) =ucase('"+ currentNode.name+"'))";
		    break;
		case EVENT:
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_EVENT\" where trim(upper(\"NAME\")) =trim(upper('"+ currentNode.getName()+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //filterStatement += "and model.id in (select model_id from event where ucase(name) =ucase('"+ currentNode.name+"'))";
		    break;
		case GATEWAY:
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_GATEWAY\" where trim(upper(\"NAME\")) =trim(upper('"+ ""+"'))"); 
		    //filterStatement += "and model.id in (select model_id from gateway where ucase(name) =ucase('"+ currentNode.name+"')" +
		    filterStatement.append(" and trim(upper(\"GATE_WAY_TYPE\")) = trim(upper('"+ currentNode.type2+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //" and ucase(gate_way_type) = ucase('"+ currentNode.type2+"'))";
		}
	    }
	    
	}
	// added on 7.08.08 to support filtering with data objects
	for (DataObject dob : query.dataObjs)
	{
	    if (!dob.name.startsWith("@"))
	    {
		filterStatement.append("and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\" where trim(upper(\"NAME\")) =trim(upper('"+ dob.name+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
	    }
	}
	// now filter with known edges
	// The following for loop was commented to test performance and results without it
	for(SequenceFlow currentEdge : query.edges)
	{
	    if (currentEdge.frmActivity != null && currentEdge.toActivity != null)
	    {
		if (!currentEdge.frmActivity.name.startsWith("@") && !currentEdge.toActivity.name.startsWith("@") 
			&& !currentEdge.frmActivity.name.startsWith("$#") && !currentEdge.toActivity.name.startsWith("$#"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination");
		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    // check self loop
		    
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
	    }
	}
	// Added on 10.05.2011 to support filtering by path/negative path and negative edges on the closure of sequence flow
	
	for(SequenceFlow currentEdge : query.negativeEdges)
	{
	    if (currentEdge.frmActivity != null && currentEdge.toActivity != null)
	    {
		if (!currentEdge.frmActivity.name.startsWith("@") && !currentEdge.toActivity.name.startsWith("@") 
			&& !currentEdge.frmActivity.name.startsWith("$#") && !currentEdge.toActivity.name.startsWith("$#"))
		{
		    filterStatement.append(" and not exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination");
		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
	    }
	}
	// path edges
	for(Path currentEdge : query.paths)
	{
	    // just handle pure path edges without any exclude statements
	    // in the version with path index, we can handle that
	    if (currentEdge.exclude !=null && currentEdge.exclude.length() > 0)
		continue;
	    
	    if (currentEdge.frmActivity != null && currentEdge.toActivity != null)
	    {
		if (!currentEdge.frmActivity.name.startsWith("@") && !currentEdge.toActivity.name.startsWith("@") 
			&& !currentEdge.frmActivity.name.startsWith("$#") && !currentEdge.toActivity.name.startsWith("$#")
			&& !currentEdge.frmActivity.name.startsWith("?")  && !currentEdge.toActivity.name.startsWith("?")) 
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination");
		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    if (currentEdge.frmActivity.actID.equals(currentEdge.toActivity.actID))
			filterStatement.append(" and source.\"ID\" = destination.\"ID\"");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		   
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
		else if (!currentEdge.frmActivity.name.startsWith("@") && !currentEdge.frmActivity.name.startsWith("$#") && !currentEdge.frmActivity.name.startsWith("?")
			&& currentEdge.toActivity.name.startsWith("@"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination");
		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
//			    filterStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		    
		}
		else if (!currentEdge.toActivity.name.startsWith("@") && !currentEdge.toActivity.name.startsWith("$#") && !currentEdge.toActivity.name.startsWith("?")
			&& currentEdge.frmActivity.name.startsWith("@"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination");
		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
//			    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		    
		}
	    }
	    else if (currentEdge.frmActivity != null && currentEdge.toEvent != null)
	    {
		if (!currentEdge.frmActivity.name.startsWith("@") && !currentEdge.frmActivity.name.startsWith("$#") && !currentEdge.frmActivity.name.startsWith("?"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_EVENT\" as destination");
		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_EVE_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(destination.\"EVE_POSITION\")= "+ currentEdge.toEvent.eventPosition+" and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
	    }
	    else if (currentEdge.frmActivity != null && currentEdge.toGateWay != null)
	    {
		if (!currentEdge.frmActivity.name.startsWith("@") && !currentEdge.frmActivity.name.startsWith("$#") && !currentEdge.frmActivity.name.startsWith("?"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_GATEWAY\" as destination");
		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_GAT_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(destination.\"GATE_WAY_TYPE\")= upper('"+ currentEdge.toGateWay.type+"') and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
	    }
	    else if (currentEdge.frmEvent != null && currentEdge.toActivity != null)
	    {
		if (!currentEdge.toActivity.name.startsWith("@") && !currentEdge.toActivity.name.startsWith("$#") && !currentEdge.toActivity.name.startsWith("?"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination, \"BPMN_GRAPH\".\"ORYX_EVENT\" as source");
		    filterStatement.append(" where \"TO_ACT_ID\" =destination.\"ID\" and \"FRM_EVE_ID\" = source.\"ID\"");
		    filterStatement.append(" and upper(destination.\"NAME\") = upper('"+ currentEdge.toActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(source.\"EVE_POSITION\")= "+ currentEdge.frmEvent.eventPosition+" and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
	    }
	    else if (currentEdge.frmEvent != null && currentEdge.toGateWay != null)
	    {
		filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_GATEWAY\" as destination, \"BPMN_GRAPH\".\"ORYX_EVENT\" as source");
		filterStatement.append(" where \"TO_GAT_ID\" =destination.\"ID\" and \"FRM_EVE_ID\" = source.\"ID\"");
		filterStatement.append(" and upper(destination.\"GATE_WAY_TYPE\")= upper('"+ currentEdge.toGateWay.type+"')");
		filterStatement.append(" and upper(source.\"EVE_POSITION\")= "+ currentEdge.frmEvent.eventPosition+" and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		//" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		
	    }
	    else if (currentEdge.frmEvent != null && currentEdge.toEvent != null)
	    {
		filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_EVENT\" as destination, \"BPMN_GRAPH\".\"ORYX_EVENT\" as source");
		filterStatement.append(" where \"TO_EVE_ID\" =destination.\"ID\" and \"FRM_EVE_ID\" = source.\"ID\"");
		filterStatement.append(" and upper(destination.\"EVE_POSITION\")= "+ currentEdge.toEvent.eventPosition+" ");
//			filterStatement.append(" and upper(destination.\"GATE_WAY_TYPE\")= upper('"+ currentEdge.toGateWay.type+"')");
		filterStatement.append(" and upper(source.\"EVE_POSITION\")= "+ currentEdge.frmEvent.eventPosition+" and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		//" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		
	    }
	    // there should be many cases here
	    else if (currentEdge.frmGateWay != null && currentEdge.toGateWay !=null)
	    {
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_GATEWAY\" as source, \"BPMN_GRAPH\".\"ORYX_GATEWAY\" as destination");
		    filterStatement.append(" where \"FRM_GAT_ID\" =source.\"ID\" and \"TO_GAT_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(source.\"GATE_WAY_TYPE\") = upper('"+ currentEdge.frmGateWay.type+"')");
		    if (currentEdge.frmGateWay.gateID.equals(currentEdge.toGateWay.gateID))
			filterStatement.append(" and source.\"ID\" = destination.\"ID\"");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(destination.\"GATE_WAY_TYPE\")= upper('"+ currentEdge.toGateWay.type+"') and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		   
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
		
	    }
	    else if (currentEdge.frmGateWay != null && currentEdge.toActivity != null)
	    {
		if (!currentEdge.toActivity.name.startsWith("@") && !currentEdge.toActivity.name.startsWith("$#"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination, \"BPMN_GRAPH\".\"ORYX_GATEWAY\" as source");
		    filterStatement.append(" where \"FRM_GAT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(destination.\"NAME\") = upper('"+ currentEdge.toActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(source.\"GATE_WAY_TYPE\")= upper('"+ currentEdge.frmGateWay.type+"') and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
	    }
	    else if (currentEdge.frmGateWay != null && currentEdge.toEvent != null)
	    {
		filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination, \"BPMN_GRAPH\".\"ORYX_GATEWAY\" as source");
		filterStatement.append(" where \"FRM_GAT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
//			filterStatement.append(" and upper(destination.\"NAME\") = upper('"+ currentEdge.toActivity.name+"')");
		filterStatement.append(" and upper(destination.\"EVE_POSITION\")= "+ currentEdge.toEvent.eventPosition+" ");
		//" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		filterStatement.append(" and upper(source.\"GATE_WAY_TYPE\")= upper('"+ currentEdge.frmGateWay.type+"') and \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW_TRANSITIVE_CLOSURE\".\"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
	    }
	}

	//	 added on 7.08.08 to support filtering with data objects
	for (Association ass : query.associations)
	{
		if (ass.frmActivity != null && ass.toDataObject != null)
		{
			if (!ass.frmActivity.name.startsWith("@") && 
					!ass.frmActivity.name.startsWith("$#") &&
					!ass.toDataObject.name.startsWith("@")&&
					!ass.toDataObject.getState().startsWith("?"))
			{
				filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
	        ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\"" +
	        " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
	        " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
	        " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
	        "\"TO_STATE_ID\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"ID\"" +
	        " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" = '" + ass.toDataObject.name +
	        "' and \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"NAME\" = '"+ ass.frmActivity.name+
	        "' and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.toDataObject.getState()+"')");
			}
		}
		else if (ass.frmEvent != null && ass.toDataObject !=null)
		{
			if (!ass.frmEvent.eventName.startsWith("@") && 
					!ass.frmEvent.eventName.startsWith("$#") &&
					!ass.toDataObject.name.startsWith("@")&&
					!ass.toDataObject.getState().startsWith("?"))
			{
				filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
	        ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_EVENT\"" +
	        " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
	        " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
	        " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
	        "\"TO_STATE_ID\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_EVENT\".\"ID\"" +
	        " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" = '" + ass.toDataObject.name +
	        "' and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"NAME\" = '"+ ass.frmEvent.eventName+
	        "' and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"EVENT_POSITION\" = "+ ass.frmEvent.eventPosition +
	        " and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"EVENT_TYPE\" = '"+ ass.frmEvent.eventType +
	        "' and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.toDataObject.getState()+"')");
			}
		}
		else if (ass.frmDataObject != null && ass.toActivity != null)
		{
			filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
	                ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\"" +
	                " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
	                "\"FROM_STATE_ID\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" = '" + ass.frmDataObject.name +
	                "' and \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"NAME\" = '"+ ass.toActivity.name+
	                "' and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.frmDataObject.getState()+"')");
		}
		else if (ass.frmDataObject != null && ass.toEvent != null)
		{
			filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
	                ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_EVENT\"" +
	                " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
	                "\"FROM_STATE_ID\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_EVENT\".\"ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" = '" + ass.frmDataObject.name +
	                "' and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"NAME\" = '"+ ass.toEvent.eventName+
	                "' and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"EVENT_POSITION\" = "+ ass.toEvent.eventPosition +
	                " and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"EVENT_TYPE\" = '"+ ass.toEvent.eventType +
	                "' and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.frmDataObject.getState()+"')");
		}
	}
	return filterStatement;
    }
    protected List<GraphObject> reducePossibilities(List<GraphObject> possibilities, QueryGraph query, GraphObject evaluatedNode)
    {
	// the reduce dependent possibilities should be invoked upon context
	//
	possibilities = eleminateNodesViolatingEdgeConstraints(possibilities, query, evaluatedNode);
	possibilities = eleminateNodesViolatingPathConstraints(possibilities, query, evaluatedNode);
	possibilities = eleminateNodesViolatingNegativeEdgeConstraints(possibilities, query, evaluatedNode);
	possibilities = eleminateNodesViolatingNegativePathConstraints(possibilities,query, evaluatedNode);
//	if (evaluatedNode.type == GraphObjectType.ACTIVITY)
//	    possibilities  = reduceDependentPossibilities(possibilities);
	return possibilities;
    }
    protected List<GraphObject> eleminateNodesViolatingPathConstraints(List<GraphObject> possibilities, QueryGraph query, GraphObject evaluatedNode)
    {
	// more efficient implementation
	// start from the evaluated node
	List<GraphObject> toRemove = new ArrayList<GraphObject>(possibilities.size());
	List<GraphObject> pathSuccessors = query.getPathSuccessorsFromQueryGraph(evaluatedNode);
	for (GraphObject g : pathSuccessors)
	{
	    if (!g.isResolved())
		continue;
	    String exl = query.getPathExcludeStatement(evaluatedNode, g);
	    if (exl != null && exl.length() > 0)
		continue;
	    for (GraphObject possibility : possibilities)
	    {
		if (!pgad.pathExists(possibility, g))
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
		continue;
	    for (GraphObject possibility : possibilities)
	    {
		if (!pgad.pathExists(g,possibility))
		    toRemove.add(possibility);
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	}
	return possibilities;
	
	
	
	
    }
    protected List<GraphObject> eleminateNodesViolatingEdgeConstraints(List<GraphObject> possibilities, QueryGraph query, GraphObject evaluatedNode)
    {
	List<GraphObject> resolvedNodes = new ArrayList<GraphObject>(query.nodes.size());
	for (GraphObject g: query.nodes)
	{
	    if (g.isResolved())
		resolvedNodes.add(g);
	}
	if (resolvedNodes.size()==0)
	    return possibilities;// there is no way to tell how to reduce possibilities
	
	// now get those nodes that are path predecessors or path successors of a resolved node
	List<GraphObject> toRemove = new ArrayList<GraphObject>(possibilities.size());
	
	for (GraphObject g : resolvedNodes)
	{
	    
	    List<GraphObject> predecessors = query.getPredecessorsFromGraph(g);
	    if (predecessors.contains(evaluatedNode))
	    {
		for (GraphObject possibility : possibilities)
		    if (!pgad.getNodesWithDistanceFromSource(possibility, 1).contains(g))
			toRemove.add(possibility);
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	    
	    List<GraphObject> successors = query.getSuccessorsFromGraph(g);
	    
//	    List<GraphObject> successors = query.getSuccessorsFromQueryGraph(g);
	    if (successors.contains(evaluatedNode))
	    {
		for (GraphObject possibility : possibilities)
		    if (!pgad.getNodesWithDistanceFromSource(g, 1).contains(possibility))
			toRemove.add(possibility);
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	}
	return possibilities;
    }
    protected List<GraphObject> eleminateNodesViolatingNegativeEdgeConstraints(List<GraphObject> possibilities, QueryGraph query, GraphObject evaluatedNode)
    {
	List<GraphObject> resolvedNodes = new ArrayList<GraphObject>(query.nodes.size());
	for (GraphObject g: query.nodes)
	{
	    if (g.isResolved())
		resolvedNodes.add(g);
	}
	if (resolvedNodes.size()==0)
	    return possibilities;// there is no way to tell how to reduce possibilities
	
	// now get those nodes that are path predecessors or path successors of a resolved node
	List<GraphObject> toRemove = new ArrayList<GraphObject>(possibilities.size());
	
	for (GraphObject g : resolvedNodes)
	{
	    
	    List<GraphObject> negativePredecessors = query.getNegativePredecessorsFromQueryGraph(g);
	    if (negativePredecessors.contains(evaluatedNode))
	    {
		for (GraphObject possibility : possibilities)
		    if (pgad.getNodesWithDistanceFromSource(possibility, 1).contains(g))
			toRemove.add(possibility);
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	    
	    
	    List<GraphObject> negativeSuccessors = query.getNegativeSuccessorsFromQueryGraph(g);
	    if (negativeSuccessors.contains(evaluatedNode))
	    {
		for (GraphObject possibility : possibilities)
		    if (pgad.getNodesWithDistanceFromSource(g, 1).contains(possibility))
			toRemove.add(possibility);
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	}
	return possibilities;
    }
    protected List<GraphObject> eleminateNodesViolatingNegativePathConstraints(List<GraphObject> possibilities, QueryGraph query, GraphObject evaluatedNode)
    {
	List<GraphObject> resolvedNodes = new ArrayList<GraphObject>(query.nodes.size());
	for (GraphObject g: query.nodes)
	{
	    if (g.isResolved())
		resolvedNodes.add(g);
	}
	if (resolvedNodes.size()==0)
	    return possibilities;// there is no way to tell how to reduce possibilities
	
	// now get those nodes that are path predecessors or path successors of a resolved node
	List<GraphObject> toRemove = new ArrayList<GraphObject>(possibilities.size());
	
	for (GraphObject g : resolvedNodes)
	{
	    
	    List<GraphObject> negativePathPredecessors = query.getNegativePathPredecessorsFromQueryGraph(g);
	    if (negativePathPredecessors.contains(evaluatedNode))
	    {
		for (GraphObject possibility : possibilities)
		    if (pgad.acyclicPathExists(possibility, g))
			toRemove.add(possibility);
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	    
	    
	    List<GraphObject> negativePathSuccessors = query.getNegativePathSuccessorsFromQueryGraph(g);
	    if (negativePathSuccessors.contains(evaluatedNode))
	    {
		for (GraphObject possibility : possibilities)
		    if (pgad.acyclicPathExists(g, possibility))
			toRemove.add(possibility);
	    }
	    possibilities.removeAll(toRemove);
	    toRemove.clear();
	    if (possibilities.size()==0)
		    return possibilities;
	}
	return possibilities;
    }  
    protected String getNodesIDsList(List<GraphObject> exclueds)
    {
	String listOfIDs ="";
	for (GraphObject o : exclueds)
	{
	    if (listOfIDs.length() ==0)
	    {
		listOfIDs = o.getID();
	    }
	    else
	    {
		listOfIDs = listOfIDs+","+o.getID();
	    }
	}
	if (listOfIDs.length() == 1)
	    return "";
	return listOfIDs;
    }
    protected ProcessGraph findPath(Path currentEdgep, String modelID) 
    {
	refreshModel(modelID);
	String[] execludes = getNodesIDsList(pgad.handleExcludeStatement(currentEdgep.exclude)).split(",");
	
	GraphObject source = new GraphObject();
	GraphObject destination = new GraphObject();
	if (currentEdgep.frmActivity != null) {
	    source = currentEdgep.frmActivity.originalNode();
	} else if (currentEdgep.frmEvent != null) {
	    source = currentEdgep.frmEvent.originalNode();
	} else if (currentEdgep.frmGateWay != null) {
	    source = currentEdgep.frmGateWay.originalNode();
	}

	if (currentEdgep.toActivity != null) {
	    destination = currentEdgep.toActivity.originalNode();
	} else if (currentEdgep.toEvent != null) {
	    destination = currentEdgep.toEvent.originalNode();
	} else if (currentEdgep.toGateWay != null) {
	    destination = currentEdgep.toGateWay.originalNode();
	}
	    
	ProcessGraph result= new ProcessGraph();
	List<ProcessGraph> rs;
	PathBuilder bp = new PathBuilder(currentEdgep.getPathEvaluation());
	rs = bp.buildPaths(source, destination, currentProcess);
	for (ProcessGraph p : rs)
	    p.modelURI = currentProcess.modelURI;
	
	List<GraphObject> andSplits = new ArrayList<GraphObject>();
	
	List<GraphObject> xorSplits = new ArrayList<GraphObject>();
	// get the enclosing and splits
	if (includeEnclosingAndSplits)
	{
	    for (ProcessGraph pg : rs)
	    {
		for (String exx : execludes)
		{
		    try
		    {
			andSplits.addAll(pg.getEnclosingANDSplit2(exx));
			
		    }
		    catch(Exception e)
		    {
//			System.out.println("Exception "+e.getMessage());
		    }
		    
		    try
		    {
//			 Added 19.8.09 
			// we need to make an extra check.
			xorSplits.addAll(pg.getEnclosingXORSplits(exx));
			GraphObject endEvent = new GraphObject();
			endEvent.setID("-1");
			endEvent.type = GraphObjectType.EVENT;
			endEvent.type2 = "3";
			MemoryQueryProcessor tqp;
			
			    tqp = new MemoryQueryProcessor(this.answerWriter);	
			for (GraphObject split : xorSplits)
			{
			    // construct a new query graph and a new query processor
			    QueryGraph tqg = new QueryGraph();
			    tqg.add(endEvent);
			    tqg.add(split);
			    Path p = new Path(split,endEvent,exx,TemporalType.NONE);
			    p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
			    tqg.add(p);
			    tqg.setStopAtFirstMatch(true);
			    tqg.setAllowIncludeEnclosingAndSplitDirective(false);
			    ProcessGraph resultt = tqp.runQueryAgainstModel(tqg, this.currentProcess.modelURI);
			    if (resultt.nodes.size() > 0)
			    {
				andSplits.clear();
				break;
			    }
			}
		    }
		    catch(Exception e)
		    {
			// Exception if the function returns null
			
		    }
		    
		}
		
	    }
	    
	}
	for (ProcessGraph pg :rs)
	{
	    boolean found = false;
	    for (String exx : execludes)
	    {
		if (exx.length() == 0) 
		    continue;
		// if the excluded node is either the source or the destination ignore it
		if (exx.equals(source.toString()))
		{
		    if (pg.nodes.lastIndexOf(source) != 0 )
		    {
			found = true;
			break;
		    }
		}
		else if (exx.equals(destination.toString()))
		{
		    if (pg.nodes.indexOf(destination) != pg.nodes.size() -1)
		    {
			found = true;
			break;
		    }
		}

		else if (pg.nodes.toString().contains(exx))
		{
		    found = true;
		    break;
		}
		
		
	    }
	    // added to handle And Splits
	    for (GraphObject ndSplit : andSplits)
	    {
		if (pg.nodes.contains(ndSplit))
		{
		    found = true;
		    break;
		}
	    }
	    if (!found)
		for (GraphObject nd :pg.nodes)
		    if (!result.nodes.contains(nd))
			result.add(nd);
	}
	result.constructEdges(currentProcess);
	return result;
    }
//    private List<GraphObject> reduceDependentPossibilities(List<GraphObject> possibilities)
//    {
//	// the major idea is to leave possible resolvant nodes that have no paths among each other.
//	List<GraphObject> toRemove = new ArrayList<GraphObject>(possibilities.size());
//	for (GraphObject out : possibilities)
//	    for(GraphObject in: possibilities)
//	    {
//		if (out.equals(in))
//		    continue;
//		if (pgad.pathExists(out, in))
//		    toRemove.add(in);
//	    }
//	possibilities.removeAll(toRemove);
//	
//	return possibilities;
//    }
   
}
