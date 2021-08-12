package com.bpmnq.petrinet;
import com.bpmnq.*;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.GraphObject.GateWayType;
//import com.bpmnq.GraphObject.EventType;

import java.util.*;

import org.apache.log4j.Logger;

public final class GraphReducer {
    // created on 15 december 07 to handle graph reduction
    private ProcessGraph myPGraph;
    private List<GraphObject> keepList;
    private List<GraphObject> startNodes;
    private List<GraphObject> preds, succs,processed;

    private Logger log = Logger.getLogger(GraphReducer.class);

    public GraphReducer(ProcessGraph pgrh, List<GraphObject> keepList)
    {
	myPGraph = pgrh;
	this.keepList = keepList;
	processed = new ArrayList<GraphObject>();

    }
    
    private void reduceLinearActivities()
    {

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);
	    //succs.clear();
	    succs = myPGraph.getSuccessorsFromGraph(currentNode);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    

	    if (keepList.contains(currentNode))
		continue;
	    // in this step we reduce activities and events only
	    if (currentNode.type == GraphObjectType.GATEWAY)
		continue;

	    preds = myPGraph.getPredecessorsFromGraph(currentNode);
	    myPGraph.remove(currentNode);
	    myPGraph.removeEdgesWithSource(currentNode);
	    myPGraph.removeEdgesWithDestination(currentNode);
	    // now reconnect those as preds to those as succs
	    int cnt1,cnt2;
	    cnt1 = preds.size();
	    cnt2 = succs.size();
	    for (int i = 0; i < cnt1;i++)
		for (int j = 0; j < cnt2;j++)
		    myPGraph.addEdge(preds.get(i), succs.get(j));
	}
    }
    
    private boolean reduceSelectionBlock()
    {
	boolean blockRemoved = false;
	List<GraphObject> startNodes;

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);

	    succs = myPGraph.getSuccessorsFromGraph(currentNode);

//	    startNodes.addAll(succs);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);


	    if (currentNode.type2.equals("XOR SPLIT"))
	    {
		if (succs.size() == 1 && succs.get(0).type2.equals("XOR JOIN") 
			&& myPGraph.getPredecessorsFromGraph(succs.get(0)).size() == 1) 
		    // we need to check that the xor join has only the current xor split as source
		{
		    log.info("Selection block found and will be removed...");
		    blockRemoved = true;
		    GraphObject sucNode = succs.get(0);
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);
		    succs = myPGraph.getSuccessorsFromGraph(sucNode);
		    myPGraph.remove(sucNode);
		    myPGraph.removeEdgesWithSource(sucNode);
		    myPGraph.removeEdgesWithDestination(sucNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1; i++)
			for (int j = 0; j < cnt2; j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));
		    
		    startNodes.addAll(succs);
		}
	    }
	    else if (currentNode.type2.equals("OR SPLIT"))
	    {
		if (succs.size()==1 && succs.get(0).type2.equals("OR JOIN") 
			&& myPGraph.getPredecessorsFromGraph(succs.get(0)).size() ==1) 
		    // we need to check that the xor join has only the current xor split as source
		{
		    log.info("Multiple Selection block found and will be removed...");
		    blockRemoved = true;
		    GraphObject sucNode = succs.get(0);
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);
		    succs = myPGraph.getSuccessorsFromGraph(sucNode);
		    myPGraph.remove(sucNode);
		    myPGraph.removeEdgesWithSource(sucNode);
		    myPGraph.removeEdgesWithDestination(sucNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));
		    startNodes.addAll(succs);
		}
	    }
	}
	return blockRemoved;
    }
    
    private boolean reduceSingleOutputANDSplit()
    {
	boolean blockRemoved = false;
	List<GraphObject> startNodes;

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);

	    succs = myPGraph.getSuccessorsFromGraph(currentNode);

//	    startNodes.addAll(succs);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);

	    if (currentNode.type2.equals("AND SPLIT"))
	    {
		if (succs.size()==1 && !succs.get(0).type2.equals("AND Join"))
		{
		    log.info("Single output AND Split found and will be removed...");
		    blockRemoved = true;
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));
		}
	    }
	}
	return blockRemoved;
    }
    
    private boolean reduceParallelBlock()
    {
	boolean blockRemoved = false;
	List<GraphObject> startNodes;

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);

	    succs = myPGraph.getSuccessorsFromGraph(currentNode);

//	    startNodes.addAll(succs);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);

	    if (currentNode.type2.equals("AND SPLIT"))
	    {
		if (succs.size()==1 && succs.get(0).type2.equals("AND JOIN")&&  myPGraph.getPredecessorsFromGraph(succs.get(0)).size() ==1) 
		    // we need to check that the xor join has only the current xor split as source
		{
		    log.info("Parallel block found and will be removed...");
		    blockRemoved = true;
		    GraphObject sucNode = succs.get(0);
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);
		    succs = myPGraph.getSuccessorsFromGraph(sucNode);
		    myPGraph.remove(sucNode);
		    myPGraph.removeEdgesWithSource(sucNode);
		    myPGraph.removeEdgesWithDestination(sucNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));
		    startNodes.addAll(succs);
		}
	    }
	}
	return blockRemoved;
    }

    private boolean reduceSingleInputJoinNode()
    {
	boolean blockRemoved = false;
	List<GraphObject> startNodes;

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);

	    succs = myPGraph.getSuccessorsFromGraph(currentNode);

//	    startNodes.addAll(succs);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);

	    if (currentNode.type2.contains("JOIN"))
	    {
		if (preds.size() == 1)
		{
		    log.info("Single input join node found and will be removed...");
		    blockRemoved = true;
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));
		}
	    }
	}
	return blockRemoved;
    }
    
    private boolean reduceUnncessaryNestedHomogenousSplits()
    {
	boolean blockRemoved = false;
	List<GraphObject> startNodes;

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);

	    succs = myPGraph.getSuccessorsFromGraph(currentNode);

//	    startNodes.addAll(succs);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);

	    if (currentNode.type2.equals(GateWayType.AND_SPLIT.asType2String()))
	    {
		if (preds.size() == 1 && preds.get(0).type2.equals(GateWayType.AND_SPLIT.asType2String()))
		    // remove this node and link its succs to its pred
		{
		    log.info("Unncessary successive AND Splits found and will be removed...");
		    blockRemoved = true;
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));

		}
	    }
	    else if (currentNode.type2.equals(GateWayType.XOR_SPLIT.asType2String()))
	    {
		if (preds.size() == 1 && preds.get(0).type2.equals(GateWayType.XOR_SPLIT.asType2String()))
		    // remove this node and link its succs to its pred
		{
		    log.info("Unncessary successive XOR Splits found and will be removed...");
		    blockRemoved = true;
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));

		}
	    }
	    else if (currentNode.type2.equals(GateWayType.OR_SPLIT.asType2String()))
	    {
		if (preds.size() == 1 && preds.get(0).type2.equals(GateWayType.OR_SPLIT.asType2String()))
		    // remove this node and link its succs to its pred
		{
		    log.info("Unncessary successive OR Split found and will be removed...");
		    blockRemoved = true;
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));

		}
	    }
	    // the same applies for successive join nodes of the same type
	    if (currentNode.type2.equals(GateWayType.AND_JOIN.asType2String()))
	    {
		if (succs.size() == 1 && succs.get(0).type2.equals(GateWayType.AND_JOIN.asType2String()))
		{
		    log.info("Unncessary successive AND Joins found and will be removed...");
		    blockRemoved = true;
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));
		}
	    }
	    else if (currentNode.type2.equals(GateWayType.XOR_JOIN.asType2String()))
	    {
		if (succs.size() == 1 && succs.get(0).type2.equals(GateWayType.XOR_JOIN.asType2String()))
		{
		    log.info("Unncessary successive XOR Joins found and will be removed...");
		    blockRemoved = true;
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));
		}
	    }
	    else if (currentNode.type2.equals(GateWayType.OR_JOIN.asType2String()))
	    {
		if (succs.size()==1 && succs.get(0).type2.equals(GateWayType.OR_JOIN.asType2String()))
		{
		    log.info("Unncessary successive OR Joins found and will be removed...");
		    blockRemoved = true;
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);

		    int cnt1 = preds.size();
		    int cnt2 = succs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), succs.get(j));
		}
	    }
	}
	return blockRemoved;	
    }
    
    private boolean reduceSingleActivityParallelBlock()
    {
	boolean blockRemoved = false;
	List<GraphObject> startNodes;

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);

	    succs = myPGraph.getSuccessorsFromGraph(currentNode);

//	    startNodes.addAll(succs);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);

	    if (currentNode.type2.equals("AND SPLIT"))
	    {
		ArrayList<GraphObject> act=new ArrayList<GraphObject>(10);
		GraphObject andjoin=null;
		for (int i = 0;i < succs.size();i++)
		{
		    if (succs.get(i).type != GraphObjectType.GATEWAY)

			act.add(succs.get(i));

		    else if (succs.get(i).type2.equals("AND JOIN"))
		    {
			andjoin = succs.get(i);

		    }
		}
		if (act.size() ==1 && andjoin != null) 
		    // link the pred of the and split to be the pred of the activity
		{
		    List<GraphObject> jsuccs;
		    jsuccs = myPGraph.getSuccessorsFromGraph(andjoin);
		    myPGraph.remove(currentNode);
		    myPGraph.removeEdgesWithDestination(currentNode);
		    myPGraph.removeEdgesWithSource(currentNode);

		    myPGraph.remove(andjoin);
		    myPGraph.removeEdgesWithDestination(andjoin);
		    myPGraph.removeEdgesWithSource(andjoin);


		    int cnt1,cnt2;
		    cnt1 = preds.size();
		    cnt2 = act.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(preds.get(i), act.get(j));
		    cnt1 = act.size();
		    cnt2 = jsuccs.size();
		    for (int i = 0; i < cnt1;i++)
			for (int j = 0; j < cnt2;j++)
			    myPGraph.addEdge(act.get(i), jsuccs.get(j));
		    blockRemoved = true;
		}
	    }
	}
	return blockRemoved;

    }
    private boolean reduceXORDeltaComponent()
    {
	boolean blockRemoved = false;
	List<GraphObject> startNodes;

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);

	    succs = myPGraph.getSuccessorsFromGraph(currentNode);

//	    startNodes.addAll(succs);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);

	    if (currentNode.type2.equals("XOR SPLIT"))
	    {
		GraphObject succ1,succ2;
		if (succs.size()==2) // only two successors one is or/xor split the other is join
		{
		    succ1 = succs.get(0);
		    succ2 = succs.get(1);
		    if ((succ1.type2.equals("OR Split") && succ2.type2.equals("XOR Join"))
			    || (succ1.type2.equals("XOR Join") && succ2.type2.equals("OR Split")))
		    {
			log.info("XOR Delta component found and will be removed...");
			blockRemoved = true;
			if (succ1.type2.equals("XOR Join"))
			    myPGraph.removeEdge(currentNode, succ1);
			else
			    myPGraph.removeEdge(currentNode, succ2);
		    }
		    else if ((succ1.type2.equals("XOR Split") && succ2.type2.equals("OR Join"))
			    || (succ1.type2.equals("OR Join") && succ2.type2.equals("XOR Split")))
		    {
			log.info("XOR Delta component found and will be removed...");
			blockRemoved = true;
			if (succ1.type2.equals("OR Join"))
			    myPGraph.removeEdge(currentNode, succ1);
			else
			    myPGraph.removeEdge(currentNode, succ2);
		    }	
		    else if ((succ1.type2.equals("OR Split") && succ2.type2.equals("OR Join"))
			    || (succ1.type2.equals("OR Join") && succ2.type2.equals("OR Split")))
		    {
			log.info("XOR Delta component found and will be removed...");
			blockRemoved = true;
			if (succ1.type2.equals("OR Join"))
			    myPGraph.removeEdge(currentNode, succ1);
			else
			    myPGraph.removeEdge(currentNode, succ2);
		    }
		}
	    }
	}
	return blockRemoved;
    }
    
    private boolean reduceORDeltaComponent()
    {
	boolean blockRemoved = false;
	List<GraphObject> startNodes;

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);

	    succs = myPGraph.getSuccessorsFromGraph(currentNode);

//	    startNodes.addAll(succs);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);

	    if (currentNode.type2.equals("OR SPLIT"))
	    {
		if (succs.size() == 2) // only two successors one is or/xor split the other is join
		{
		    GraphObject succ1 = succs.get(0);
		    GraphObject succ2 = succs.get(1);
		    if ((succ1.type2.equals("XOR Split") && succ2.type2.equals("OR Join"))
			    || (succ1.type2.equals("OR Join") && succ2.type2.equals("XOR Split")))
		    {
			log.info("OR Delta component found and will be removed...");
			blockRemoved = true;
			if (succ1.type2.equals("OR Join"))
			    myPGraph.removeEdge(succ2, succ1);
			else
			    myPGraph.removeEdge(succ1, succ2);
		    }	
		    else if ((succ1.type2.equals("OR Split") && succ2.type2.equals("OR Join"))
			    || (succ1.type2.equals("OR Join") && succ2.type2.equals("OR Split")))
		    {
			log.info("OR Delta component found and will be removed...");
			blockRemoved = true;
			if (succ1.type2.equals("OR Join"))
			    myPGraph.removeEdge(currentNode, succ1);
			else
			    myPGraph.removeEdge(currentNode, succ2);
		    }
		}
	    }
	}
	return blockRemoved;
    }
    
//  private boolean reduceANDDeltaComponent()
//  {
//  boolean blockRemoved = false;
//  ArrayList<GraphObject> startNodes;

//  GraphObject currentNode;
//  startNodes = myPGraph.getStartupNodes();
//  while(startNodes.size() != 0)
//  {
//  // iterate on each one 
//  currentNode = startNodes.remove(0);

//  succs = myPGraph.getSuccessorsFromGraph(currentNode);

//  startNodes.addAll(succs);

//  preds = myPGraph.getPredecessorsFromGraph(currentNode);

//  if (currentNode.type2.equals("AND SPLIT"))
//  {
//  GraphObject succ1,succ2;
//  if (succs.size()==2) // only two successors one is or/xor split the other is join
//  {
//  succ1 = succs.get(0);
//  succ2 = succs.get(1);
//  if ((succ1.type2.equals("AND Split") && succ2.type2.equals("OR Join"))
//  || (succ1.type2.equals("OR Join") && succ2.type2.equals("AND Split")))
//  {
//  System.out.println("AND Delta component found and will be removed...");
//  blockRemoved = true;
//  if (succ1.type2.equals("OR Join"))
//  myPGraph.removeEdge(currentNode, succ1);
//  else
//  myPGraph.removeEdge(currentNode, succ2);
//  }	
//  else if ((succ1.type2.equals("OR Split") && succ2.type2.equals("OR Join"))
//  || (succ1.type2.equals("OR Join") && succ2.type2.equals("OR Split")))
//  {
//  System.out.println("OR Delta component found and will be removed...");
//  blockRemoved = true;
//  if (succ1.type2.equals("OR Join"))
//  myPGraph.removeEdge(currentNode, succ1);
//  else
//  myPGraph.removeEdge(currentNode, succ2);
//  }
//  }
//  }
//  }
//  return blockRemoved;
//  }

    private boolean reduceSingleActivitySelectionBlock()
    {
	// the application of this rule needs to investigate first the involvement of the single activity in a precedes expression
	// if it is not involved in any precedes expression at all we can safely remove it.
	return false;
    }

    private void reduceBlocks()
    {
	// check for balanced un necessary selection blocks
	boolean more = true;
	while (more)
	{
	    more = false;
	    processed.clear();
	    more = more || reduceSelectionBlock();
	    processed.clear();
	    more = more || reduceParallelBlock();
	    processed.clear();
	    more = more || reduceUnncessaryNestedHomogenousSplits();
	    processed.clear();
	    more = more || reduceSingleActivityParallelBlock();
	    processed.clear();
	    more = more || reduceSingleInputJoinNode();
	    processed.clear();
	    more = more || reduceSingleActivitySelectionBlock();
	    processed.clear();
	    more = more || reduceXORDeltaComponent();
	    processed.clear();
	    more = more || reduceORDeltaComponent();
	    processed.clear();
	    more = more || reduceSingleOutputANDSplit();
	    processed.clear();
	    more = more || cleanupEdges();
	}
    }

    private boolean cleanupEdges()
    {
	int cnt = myPGraph.edges.size();
	boolean cleaned=false;
	SequenceFlow currentEdge;
	for (int i =cnt-1; i >= 0; i--)
	{
	    currentEdge = myPGraph.edges.get(i);
	    if (currentEdge.frmGateWay != null && currentEdge.toGateWay != null  )
	    {

//		if (currentEdge.frmGateWay.gateWayType.equals("XOR SPLIT") && currentEdge.toGateWay.gateWayType.equals("XOR JOIN") )
//		{
//		myPGraph.edges.remove(i);
//		cleaned = true;
//		//cleanupEdges();
//		}
//		if (currentEdge.frmGateWay.gateWayType.equals("OR SPLIT") && currentEdge.toGateWay.gateWayType.equals("OR JOIN") )
//		{
//		myPGraph.edges.remove(i);
//		cleaned = true;
//		//cleanupEdges();
//		}
		if (currentEdge.frmGateWay.type.equals("AND SPLIT") && currentEdge.toGateWay.type.equals("AND JOIN") )
		{
		    myPGraph.edges.remove(i);
		    cleaned = true;
		    //cleanupEdges();
		}

	    }
	}
	return cleaned;
    }

    //	private void cleanupNoedes()
//  {
//  int cnt = myPGraph.nodes.size();
//  GraphObject currentNode;
//  for (int i =0; i < cnt; i++)
//  {
//  currentNode = myPGraph.nodes.get(i);
//  if (myPGraph.getPredecessorsFromGraph(currentNode).size() ==0 && myPGraph.getSuccessorsFromGraph(currentNode).size()==0)
//  {
//  myPGraph.nodes.remove(i);
//  cleanupNoedes();
//  }
//  }
//  }
    
    private boolean reduceLoops()
    {
	boolean blockRemoved = false;
	List<GraphObject> startNodes;

	GraphObject currentNode;
	startNodes = myPGraph.getStartupNodes();
	while(startNodes.size() != 0)
	{
	    // iterate on each one 
	    currentNode = startNodes.remove(0);

	    succs = myPGraph.getSuccessorsFromGraph(currentNode);

//	    startNodes.addAll(succs);
	    processed.add(currentNode);
	    // repeat the operation for the successors
	    for (GraphObject g :succs)
	    {
		if (!processed.contains(g) && !startNodes.contains(g))
		    startNodes.add(g);
	    }
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);

	    if (currentNode.type2.equals("XOR SPLIT"))
	    {
		for (GraphObject succ : succs)
		    if (succ.type2.equals("XOR JOIN") &&  preds.contains(succ)) // structured loop found 
			// we need to check that the xor join has only the current xor split as source
		    {
			log.info("Structured loop found and will be removed...");
			blockRemoved = true;
			myPGraph.removeEdge(currentNode, succ);
		    }
	    }
	    else if (currentNode.type2.equals("OR SPLIT"))
	    {
		for (GraphObject succ : succs)
		    if (succ.type2.equals("OR JOIN") &&  preds.contains(succ)) // structured loop found 
			// we need to check that the xor join has only the current xor split as source
		    {
			log.info("Structured loop found and will be removed...");
			blockRemoved = true;
			myPGraph.removeEdge(currentNode, succ);
		    }
	    }
	}
	return blockRemoved;

    }

    public ProcessGraph getReducedGraph()
    {
	long startTime, endTime;
	startTime = System.currentTimeMillis();
	processed.clear();
	// call reduction steps here
//	log.debug("Reducing non necessary activities and events");
	reduceLinearActivities();
	//cleanupEdges();
	//cleanupNodes();
	reduceBlocks();
	//cleanupEdges();
	//cleanupNodes();
	processed.clear();
	
	if (reduceLoops())
	{
	    processed.clear();
	    reduceSingleInputJoinNode(); // calling reduce again because single input join nodes might appear.
	}
	    
	//reduceLinearActivities();

	endTime = System.currentTimeMillis();
	log.info("Reduction time: "+ (endTime - startTime));
	ProcessGraph reducedGraph = myPGraph;
//	log.debug("Reduction complete");
	return reducedGraph;
    }

}
