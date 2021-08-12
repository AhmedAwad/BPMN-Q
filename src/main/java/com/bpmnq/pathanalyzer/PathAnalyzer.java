package com.bpmnq.pathanalyzer;

import com.bpmnq.*;
import com.bpmnq.GraphObject.GraphObjectType;

import java.io.PrintStream;
import java.util.*;

import org.apache.log4j.Logger;

@Deprecated
public class PathAnalyzer 
{
    private WeightedProcessGraph myPGraph;
    private List<GraphObject> breadthFirst;
    // this holds the breadth first navigation result.
    private List<GraphObject> alreadyVisited;
    // this holds nodes that are rereachable due to cycles in the graph
    
    private Logger log = Logger.getLogger(PathAnalyzer.class);

    public PathAnalyzer(ProcessGraph matchSubGraph) {
	myPGraph = new WeightedProcessGraph(matchSubGraph);
	breadthFirst = new ArrayList<GraphObject>(myPGraph.nodes.size());
	alreadyVisited = new ArrayList<GraphObject>(myPGraph.nodes.size());
    }

    private List<WeightedSequenceFlow> getIncomingEdges(GraphObject currentNode)
    {
	List<WeightedSequenceFlow> edges = new ArrayList<WeightedSequenceFlow>(10);
	WeightedSequenceFlow currentEdge;
	int sz2 = myPGraph.edges.size();
	if (currentNode.type == GraphObjectType.ACTIVITY)
	{
	    for (int j = 0; j < sz2; j++)
	    {
		currentEdge = myPGraph.edges.get(j);
		if (currentEdge.toActivity != null && currentEdge.toActivity.actID == currentNode.getID())
		{
		    edges.add(currentEdge);
		}

	    }
	}
	else if (currentNode.type == GraphObjectType.EVENT)
	{
	    for (int j = 0; j < sz2; j++)
	    {
		currentEdge = myPGraph.edges.get(j);
		if (currentEdge.toEvent != null && currentEdge.toEvent.eventID == currentNode.getID())
		{
		    edges.add(currentEdge);
		}

	    }
	}
	else if (currentNode.type == GraphObjectType.GATEWAY)
	{
	    for (int j = 0; j < sz2; j++)
	    {
		currentEdge = myPGraph.edges.get(j);
		if (currentEdge.toGateWay != null && currentEdge.toGateWay.gateID == currentNode.getID())
		{
		    edges.add(currentEdge);
		}

	    }
	}
	return edges;
    }
    private int getMinMinTokens(List<WeightedSequenceFlow> edges)
    {
	// returns the minimum value in all incoming edges min token value
	if (edges.size()==0) return -1;
	int min = edges.get(0).minToken;
	for (int i = 1; i < edges.size();i++)
	    if (edges.get(i).minToken < min)
		min = edges.get(i).minToken;
	return min;

    }
//  private int getMaxMinTokens(ArrayList<SequenceFlow> edges)
//  {
//  // returns the minimum value in all incoming edges min token value
//  if (edges.size()==0) return -1;
//  int min = edges.get(0).minToken;
//  for (int i = 1; i < edges.size();i++)
//  if (edges.get(i).minToken > min)
//  min = edges.get(i).minToken;
//  return min;

//  }

    private boolean getOptionalityOfIncomingEdges(List<WeightedSequenceFlow> edges,GraphObject currentNode)
    {
	if (edges.size()==0) return true;
	if (currentNode.type == GraphObjectType.ACTIVITY)
	{
	    for (int i = 0; i < edges.size();i++)
		if (edges.get(i).optional == false)
		    return false;
	    return true;
	}
	else if (currentNode.type == GraphObjectType.EVENT)
	{
	    for (int i = 0; i < edges.size();i++)
		if (edges.get(i).optional == false)
		    return false;
	    return true;
	}
	else if (currentNode.type == GraphObjectType.GATEWAY)
	{
	    for (int i = 0; i < edges.size();i++)
		if (edges.get(i).optional == false)
		    return false;
	    return true;
	}
	else return true;
    }
    private boolean areAllMandatory(List<WeightedSequenceFlow> edges)
    {
	if (edges.size()== 0) return false;
	for (int i = 0; i < edges.size();i++)
	    if (edges.get(i).optional==true)
		return false;
	return true;

    }
    private boolean areAllOptional(List<WeightedSequenceFlow> edges)
    {

	if (edges.size()== 0) return false;
	for (int i = 0; i < edges.size();i++)
	    if (edges.get(i).optional==false)
		return false;
	return true;
    }
    private int getMinMaxTokens(List<WeightedSequenceFlow> edges)
    {
	// returns the minimum value in all incoming edges max tokens value.
	if (edges.size()==0) return -1;
	int max = edges.get(0).maxToken;
	for (int i = 1; i < edges.size();i++)
	    if (edges.get(i).maxToken < max)
		max = edges.get(i).maxToken;
	return max;

    }
    private int getMaxMaxTokens(List<WeightedSequenceFlow> edges)
    {
	// returns the minimum value in all incoming edges max tokens value.
	if (edges.size()==0) return -1;
	int max = edges.get(0).maxToken;
	for (int i = 1; i < edges.size();i++)
	    if (edges.get(i).maxToken > max)
		max = edges.get(i).maxToken;
	return max;

    }

    private boolean allHaveANDSplits(List<WeightedSequenceFlow> edges)
    {
	if (edges.size() == 0 ) return false;
	for (int i = 0; i < edges.size();i++)
	    if (edges.get(i).fromANDSplit == false)
		return false;
	return true;
    }
    private boolean anyHaveANDSplits(List<WeightedSequenceFlow> edges)
    {
	if (edges.size() == 0 ) return false;
	for (int i = 0; i < edges.size();i++)
	    if (edges.get(i).fromANDSplit == true)
		return true;
	return false;
    }
    private boolean anyMandatory(List<WeightedSequenceFlow> edges)
    {
	if (edges.size() == 0 ) return false;
	for (int i = 0; i < edges.size();i++)
	    if (edges.get(i).optional == false)
		return true;
	return false;
    }

    private boolean anyCommonXORSplits(List<WeightedSequenceFlow> edges)
    {
	if (edges.size() == 0) return false;
	List<GateWay> result = new ArrayList<GateWay>(10);
	result.addAll(edges.get(0).passedOrSplits);
	for (int i = 1; i < edges.size();i++)
	{
	    result = Utilities.intersect(result, edges.get(i).passedOrSplits);
	    if (result.size() == 0) return false;
	}
	return true;
    }

    private List<GateWay> union(List<WeightedSequenceFlow> edges)
    {
	List<GateWay> result = new ArrayList<GateWay>(10);
	if (edges.size()== 0)
	    return result;

	try {
	    result.addAll(edges.get(0).passedOrSplits);
	    for (int i = 1; i < edges.size(); i++) {
		result = Utilities.union(result, edges.get(i).passedOrSplits);
	    }
	} catch (Exception ex) {
	    log.error(" Utilities.union " + ex.getMessage(), ex);
	    return null;
	}
	return result;
    }

    private String getNearestSelectionGateID(List<WeightedSequenceFlow> edges)
    {
	if (edges.size() == 0 ) return "0";
	for (int i = 0; i < edges.size();i++)
	    if (!edges.get(i).nearestSelectionGateID.equals("0"))
		return edges.get(i).nearestSelectionGateID;
	return "0";
    }
    
    private boolean allFromNearestSelectionGate(List<WeightedSequenceFlow> edges)
    {
	if (edges.size() == 0 ) return false;
	if (getNearestSelectionGateID(edges).equals("0")) return false;
	String gat = edges.get(0).nearestSelectionGateID;
	for (int i = 1; i < edges.size();i++)
	    if (!edges.get(i).nearestSelectionGateID.equals(gat))
		return false;
	return true;
    }
    private List<GateWay> filterXORSplits(List<WeightedSequenceFlow> edges)
    {
	List<GateWay> result = union(edges);
	GateWay gt = new GateWay();

	for (int i = 0; i < edges.size();i++)

	{
	    gt.gateID = edges.get(i).nearestSelectionGateID;
	    gt.name ="";
	    gt.type = "XOR SPLIT";
	    //for(int j = 0; j < edges.get(i).passedOrSplits.size();j++)
	    for(int j = 0; j < result.size();j++)
		if (result.get(j).gateID == gt.gateID)
		{
		    result.remove(j);
		    //System.out.println("XOR REmoved");
		    break;
		}
	}
	return result;
    }
    /*private boolean anyOptional(ArrayList<WeightedSequenceFlow> edges)
	{
		if (edges.size() == 0 ) return false;
		for (int i = 0; i < edges.size();i++)
			if (edges.get(i).optional == true)
				return true;
		return false;
	}*/
    private int sumMinTokens(List<WeightedSequenceFlow> edges)
    {
	if (edges.size() == 0 ) return -1;
	int sum = 0;
	for (int i = 0; i < edges.size();i++)
	    sum += edges.get(i).minToken;

	return sum;
    }
    private int sumMaxTokens(List<WeightedSequenceFlow> edges)
    {
	if (edges.size() == 0 ) return -1;
	int sum = 0;
	for (int i = 0; i < edges.size();i++)
	    sum += edges.get(i).maxToken;

	return sum;
    }
    private void forwardTokens(GraphObject currentNode, boolean init)
    {
	WeightedSequenceFlow currentEdge;

	int sz2 = myPGraph.edges.size();
	if (init)
	{

	    if (currentNode.type == GraphObjectType.ACTIVITY)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = myPGraph.edges.get(j);
		    if (currentEdge.frmActivity != null && currentEdge.frmActivity.actID == currentNode.getID())
		    {
			currentEdge.minToken = 1;
			currentEdge.maxToken = 1;
			currentEdge.optional = false;

		    }
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).optional = currentEdge.optional;
//		    myPGraph.edges.get(j).fromANDSplit = currentEdge.fromANDSplit;
//		    myPGraph.edges.get(j).nearestSelectionGateID = currentEdge.nearestSelectionGateID;

		}
	    }
	    else if (currentNode.type == GraphObjectType.EVENT)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = myPGraph.edges.get(j);
		    if (currentEdge.frmEvent != null && currentEdge.frmEvent.eventID == currentNode.getID())
		    {
			currentEdge.minToken = 1;
			currentEdge.maxToken = 1;
			currentEdge.optional = false;
		    }
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).optional = currentEdge.optional;
//		    myPGraph.edges.get(j).fromANDSplit = currentEdge.fromANDSplit;
//		    myPGraph.edges.get(j).nearestSelectionGateID = currentEdge.nearestSelectionGateID;
		}
	    }
	    else if (currentNode.type == GraphObjectType.GATEWAY)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = myPGraph.edges.get(j);
		    if (currentEdge.frmGateWay != null && currentEdge.frmGateWay.gateID == currentNode.getID())
		    {
			currentEdge.minToken = 1;
			currentEdge.maxToken = 1;
			currentEdge.optional = false;
			if (currentNode.type2.equals("AND SPLIT"))
			{	
			    /*if (currentEdge.passedAndSplits == null)
								currentEdge.passedAndSplits = new ArrayList<GateWay>(10);
							GateWay tmp = new GateWay();
							tmp.gateID = currentNode.id;
							tmp.gateWayType = currentNode.type2;
							tmp.gateWayName = currentNode.name;
							currentEdge.passedAndSplits.add(tmp);*/
			    currentEdge.fromANDSplit = true;
			}
			else if (currentNode.type2.equals("XOR SPLIT"))
			{	
			    if (currentEdge.passedOrSplits == null)
				currentEdge.passedOrSplits = new ArrayList<GateWay>(10);
			    GateWay tmp = new GateWay();
			    tmp.gateID = currentNode.getID();
			    tmp.type = currentNode.type2;
			    tmp.name = currentNode.getName();
			    currentEdge.passedOrSplits.add(tmp);
			    currentEdge.nearestSelectionGateID = currentNode.getID();
			}
			else if (currentNode.type2.equals("OR SPLIT"))
			{	
			    /*if (currentEdge.passedAndSplits == null)
								currentEdge.passedAndSplits = new ArrayList<GateWay>(10);
							GateWay tmp = new GateWay();
							tmp.gateID = currentNode.id;
							tmp.gateWayType = currentNode.type2;
							tmp.gateWayName = currentNode.name;
							currentEdge.passedAndSplits.add(tmp);*/
			    currentEdge.nearestSelectionGateID = currentNode.getID();
			}
			/*else if (currentNode.type2.equals("OR SPLIT"))
						{	
							if (currentEdge.passedOrSplits == null)
								currentEdge.passedOrSplits = new ArrayList<GateWay>(10);
							GateWay tmp = new GateWay();
							tmp.gateID = currentNode.id;
							tmp.gateWayType = currentNode.type2;
							tmp.gateWayName = currentNode.name;
							currentEdge.passedOrSplits.add(tmp);
						}
						else if (currentNode.type2.equals("XOR SPLIT"))
						{	
							if (currentEdge.passedOrSplits == null)
								currentEdge.passedOrSplits = new ArrayList<GateWay>(10);
							GateWay tmp = new GateWay();
							tmp.gateID = currentNode.id;
							tmp.gateWayType = currentNode.type2;
							tmp.gateWayName = currentNode.name;
							currentEdge.passedOrSplits.add(tmp);
						}*/
		    }
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).optional = currentEdge.optional;
//		    myPGraph.edges.get(j).fromANDSplit = currentEdge.fromANDSplit;
//		    myPGraph.edges.get(j).nearestSelectionGateID = currentEdge.nearestSelectionGateID;
		}
	    }
	    // prepare for the next call
	    List<GraphObject> succs = myPGraph.getSuccessorsFromGraph(currentNode);
	    breadthFirst.addAll(succs);
	    alreadyVisited.add(currentNode);
	    //if (breadthFirst.size() > 0)
	    //	forwardTokens(breadthFirst.remove(0), false);
	    if (breadthFirst.size() > 0)
	    {
		GraphObject next = breadthFirst.remove(0); 
		List<WeightedSequenceFlow> in = getIncomingEdges(next);

		while (anyUnmarkedEdges(in))
		{
		    //breadthFirst.add(next);
		    next = breadthFirst.remove(0);
		    in = getIncomingEdges(next);
		}
		forwardTokens(next, false);
	    }
	} else { // we track the forwarding of the tokens in the graph
	    // we need to get the incoming edges to the node first
	    // then based on the type of each node we distribute the tokens forward
	    List<WeightedSequenceFlow> inEdges = getIncomingEdges(currentNode);
	    int minMinToken = getMinMinTokens(inEdges);
	    //int maxMinToken = getMaxMinTokens(inEdges);
	    int maxMaxToken= getMaxMaxTokens(inEdges);
	    int minMaxToken= getMinMaxTokens(inEdges);
	    boolean opt;
	    if (currentNode.type == GraphObjectType.ACTIVITY)
	    {
		// we assume that activities have at most one incoming edge.
		// at most one outgoing edge.
		opt = getOptionalityOfIncomingEdges(inEdges, currentNode);
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = myPGraph.edges.get(j);
		    if (currentEdge.frmActivity != null && currentEdge.frmActivity.actID == currentNode.getID())
		    {
			if (inEdges.size() <= 1)
			{
			    currentEdge.minToken = minMinToken;
			    currentEdge.maxToken = maxMaxToken;
			    currentEdge.optional = opt;
			    currentEdge.fromANDSplit = anyHaveANDSplits(inEdges);

			    currentEdge.passedOrSplits = union(inEdges);
			}
			else if (allFromNearestSelectionGate(inEdges))
			{
			    currentEdge.optional = false;// this needs to be looked upon in more details.
			    currentEdge.minToken = maxMaxToken;
			    currentEdge.maxToken = maxMaxToken;

			}
			else if (anyMandatory(inEdges))
			{
			    currentEdge.optional = false;
			    currentEdge.minToken = sumMinTokens(inEdges);
			    currentEdge.maxToken = sumMaxTokens(inEdges);
			}

			else if (allHaveANDSplits(inEdges))
			{
			    currentEdge.optional = true;
			    currentEdge.minToken = sumMinTokens(inEdges);
			    currentEdge.maxToken = sumMaxTokens(inEdges);
			}

			else
			{
			    currentEdge.optional = true;
			    currentEdge.minToken = minMinToken;
			    currentEdge.maxToken = maxMaxToken;
			}
			currentEdge.fromANDSplit = false;
			//currentEdge.nearestSelectionGateID = 0;
			currentEdge.nearestSelectionGateID = getNearestSelectionGateID(inEdges);
			currentEdge.passedOrSplits = filterXORSplits(inEdges);
		    }

		}
	    }
	    else if (currentNode.type == GraphObjectType.EVENT)
	    {
		opt = getOptionalityOfIncomingEdges(inEdges, currentNode);
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = myPGraph.edges.get(j);
		    if (currentEdge.frmEvent != null && currentEdge.frmEvent.eventID == currentNode.getID())
		    {
			currentEdge.minToken = minMinToken;
			currentEdge.maxToken = maxMaxToken;
			currentEdge.optional = opt;
			currentEdge.fromANDSplit = anyHaveANDSplits(inEdges);
			currentEdge.nearestSelectionGateID = getNearestSelectionGateID(inEdges);
			currentEdge.passedOrSplits = union(inEdges);
		    }
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).optional = currentEdge.optional;
//		    myPGraph.edges.get(j).fromANDSplit = currentEdge.fromANDSplit;
//		    myPGraph.edges.get(j).nearestSelectionGateID = currentEdge.nearestSelectionGateID;	
		}
	    }
	    else if (currentNode.type == GraphObjectType.GATEWAY)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = myPGraph.edges.get(j);
		    if (currentEdge.frmGateWay != null && currentEdge.frmGateWay.gateID == currentNode.getID())
		    {
			// different routing capabilities
			// we assume that each node as only one incoming
			// edge.
			if (currentNode.type2.equals("AND SPLIT"))
			{
			    // any and split must have at most one incoming edge.
			    opt = getOptionalityOfIncomingEdges(inEdges, currentNode);
			    currentEdge.minToken = minMinToken;
			    currentEdge.maxToken = maxMaxToken;
			    currentEdge.optional = opt;
			    currentEdge.fromANDSplit = true;
			    currentEdge.nearestSelectionGateID = getNearestSelectionGateID(inEdges);
			    currentEdge.passedOrSplits = union(inEdges);
			}
			else if(currentNode.type2.equals("XOR SPLIT"))
			{
			    //opt = getOptionalityOfIncomingEdges(inEdges, currentNode);
			    currentEdge.minToken = 0;
			    currentEdge.maxToken = maxMaxToken;
			    currentEdge.optional = true;
			    currentEdge.fromANDSplit = anyHaveANDSplits(inEdges);
			    currentEdge.nearestSelectionGateID = currentNode.getID();
			    currentEdge.passedOrSplits = union(inEdges);
			    if (currentEdge.passedOrSplits == null)
				currentEdge.passedOrSplits = new ArrayList<GateWay>(10);
			    GateWay tmp = new GateWay();
			    tmp.gateID = currentNode.getID();
			    tmp.type = currentNode.type2;
			    tmp.name = currentNode.getName();
			    currentEdge.passedOrSplits.add(tmp);
			} 
			else if(currentNode.type2.equals("OR SPLIT"))
			{
			    //opt = getOptionalityOfIncomingEdges(inEdges, currentNode);
			    currentEdge.minToken = 0;
			    currentEdge.maxToken = maxMaxToken;
			    currentEdge.optional = true;
			    currentEdge.fromANDSplit = anyHaveANDSplits(inEdges);
			    currentEdge.nearestSelectionGateID = currentNode.getID();
			}
			else if (currentNode.type2.equals("AND JOIN"))
			{
			    //opt = getOptionalityOfIncomingEdges(inEdges, currentNode);
			    if (areAllMandatory(inEdges))
			    {
				currentEdge.optional = false;
				currentEdge.minToken = minMinToken;
				currentEdge.maxToken = minMinToken;

			    }
			    else if (areAllOptional(inEdges))
			    {
				if (allHaveANDSplits(inEdges))
				{
				    currentEdge.optional = true;
				    currentEdge.minToken = minMinToken;
				    currentEdge.maxToken = minMaxToken; // the minimum value of incoming max tokens

				}
				else if (anyCommonXORSplits(inEdges))
				{
				    currentEdge.optional = true;
				    currentEdge.minToken = 0;
				    currentEdge.maxToken = 0;
				    // a deadlock situation.
				}
				else
				{
				    currentEdge.optional = true;
				    currentEdge.minToken = minMinToken;
				    currentEdge.maxToken = minMaxToken;

				}
			    }
			    else // there is a mix
			    {
				currentEdge.minToken = minMinToken;
				currentEdge.maxToken = minMaxToken;
				currentEdge.optional = true;
				// a deadlock situation.
			    }
			    currentEdge.fromANDSplit = false;
			    currentEdge.nearestSelectionGateID = getNearestSelectionGateID(inEdges);
			    currentEdge.passedOrSplits = union(inEdges);
			}
			else if (currentNode.type2.endsWith("XOR JOIN"))
			{
			    // determining optionality of an edge needs to be focused on
			    // because it is not accurate.
			    if (allFromNearestSelectionGate(inEdges))
			    {
				currentEdge.optional = false;// this needs to be looked upon in more details.
				currentEdge.minToken = maxMaxToken;
				currentEdge.maxToken = maxMaxToken;

			    }
			    else if (anyMandatory(inEdges))
			    {
				currentEdge.optional = false;
				currentEdge.minToken = sumMinTokens(inEdges);
				currentEdge.maxToken = sumMaxTokens(inEdges);
			    }

			    else if (allHaveANDSplits(inEdges))
			    {
				currentEdge.optional = true;
				currentEdge.minToken = sumMinTokens(inEdges);
				currentEdge.maxToken = sumMaxTokens(inEdges);
			    }

			    else
			    {
				currentEdge.optional = true;
				currentEdge.minToken = minMinToken;
				currentEdge.maxToken = maxMaxToken;
			    }
			    currentEdge.fromANDSplit = false;
			    currentEdge.nearestSelectionGateID = "0";
			    currentEdge.passedOrSplits = filterXORSplits(inEdges);
			    if (currentEdge.passedOrSplits.size() ==0)
				currentEdge.optional = false;
			    else
				currentEdge.optional = true;
			}
			else if (currentNode.type2.endsWith("OR JOIN"))
			{
			    /*if (allFromNearestSelectionGate(inEdges))
							{

							}
							else if (anyMandatory(inEdges))
							{
								currentEdge.optional = false;
								currentEdge.minToken = sumMinTokens(inEdges);
								currentEdge.maxToken = sumMaxTokens(inEdges);
							}

							else if (allHaveANDSplits(inEdges))
							{
								currentEdge.optional = true;
								currentEdge.minToken = sumMinTokens(inEdges);
								currentEdge.maxToken = sumMaxTokens(inEdges);
							}

							else
							{
								currentEdge.optional = true;
								currentEdge.minToken = minMinToken;
								currentEdge.maxToken = maxMaxToken;
							}*/
			    currentEdge.minToken = minMaxToken;
			    currentEdge.maxToken = maxMaxToken;
			    currentEdge.optional = true;
			    currentEdge.fromANDSplit = false;
			    currentEdge.nearestSelectionGateID = "0";
			    currentEdge.passedOrSplits = filterXORSplits(inEdges);

			    if (currentEdge.passedOrSplits.size()== 0)
			    {
				currentEdge.optional = false;
			    }
			}
		    }
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).maxToken = currentEdge.maxToken;
//		    myPGraph.edges.get(j).optional = currentEdge.optional;
//		    myPGraph.edges.get(j).fromANDSplit = currentEdge.fromANDSplit;
//		    myPGraph.edges.get(j).nearestSelectionGateID = currentEdge.nearestSelectionGateID;
		}
	    }

//	    prepare for the next call
	    List<GraphObject> succs = myPGraph.getSuccessorsFromGraph(currentNode);
	    //breadthFirst.addAll(succs);
	    for (int i = 0 ; i < succs.size();i++)
		if (breadthFirst.contains(succs.get(i)))
		{	if( !alreadyVisited.contains(succs.get(i)))
		    alreadyVisited.add(succs.get(i));
		}
		else
		    breadthFirst.add(succs.get(i));

	    alreadyVisited.add(currentNode);
	    if (breadthFirst.size() > 0)
	    {
		GraphObject next = breadthFirst.remove(0); 
		List<WeightedSequenceFlow> in = getIncomingEdges(next);

		while (anyUnmarkedEdges(in))
		{
		    //breadthFirst.add(next);
		    next = breadthFirst.remove(0);
		    in = getIncomingEdges(next);
		}
		forwardTokens(next, false);
	    }
	}
    }

    private boolean anyUnmarkedEdges(List<WeightedSequenceFlow> edges) {
	for (int i = 0; i < edges.size(); i++)
	    if (edges.get(i).maxToken == -1)
		return true;
	return false;
    }

    public void distributeTokens()
    {
	// First of all identify start up nodes in the graph
	// i.e nodes with no input sequence flow
	for (GraphObject graphObject : myPGraph.getStartupNodes())
	{
	    forwardTokens(graphObject, true);
	}

    }
    
    public void print(PrintStream outStream)
    {
	myPGraph.printWithWeights(outStream);
    }

}
