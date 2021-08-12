package com.bpmnq;


import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;



import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.Path.PathEvaluation;

public final class PathBuilder  {
    private Logger log = Logger.getLogger(PathBuilder.class);
    private PathEvaluation pe;
    public PathBuilder()
    {
	pe = PathEvaluation.CYCLIC; // default to the widest option
    }
    public PathBuilder(PathEvaluation e)
    {
	pe = e;
    }
    private ProcessGraph getSuccessorsAsPath(GraphObject elem,ProcessGraph process)
    {
	ProcessGraph succs;
	GraphObject succElem;
	succs = new ProcessGraph();
	//succs.addNode(elem);
	if (elem.type.equals(GraphObject.GraphObjectType.ACTIVITY) )
	{


	    for (SequenceFlow seq : process.edges )
	    {
		if (seq.frmActivity != null)
		{
		    // check that it is this activity
		    if (!seq.frmActivity.actID.equals(elem.getID())) continue;
		    // now check the to part

		    succElem = new GraphObject();
		    if (seq.toActivity != null)
		    {
			succElem.setID( seq.toActivity.actID);
			succElem.setName(seq.toActivity.name);
			succElem.type = GraphObject.GraphObjectType.ACTIVITY;
			succElem.type2 = "";
		    }
		    else if (seq.toEvent != null)
		    {
			succElem.setID(seq.toEvent.eventID);
			succElem.setName(seq.toEvent.eventName);
			succElem.type = GraphObject.GraphObjectType.EVENT;
			succElem.type2 = Integer.toString(seq.toEvent.eventPosition);
		    }
		    else if (seq.toGateWay != null)
		    {
			succElem.setID(seq.toGateWay.gateID);
			succElem.setName(seq.toGateWay.name);
			succElem.type = GraphObjectType.GATEWAY;
			succElem.type2 = seq.toGateWay.type;
		    }
		    succs.add(succElem);
		    succs.add(seq);

		}
	    }							
	}
	else if (elem.type.equals(GraphObjectType.EVENT) )
	{
	    // this will be changed to calls to the DB
	    // we have to resolve the id of the activity
	    for (SequenceFlow seq : process.edges )
	    {
		if (seq.frmEvent != null)
		{
		    // check that it is this activity
		    if (!seq.frmEvent.eventID.equals(elem.getID())) continue;
		    // now check the to part

		    succElem = new GraphObject();
		    if (seq.toActivity != null)
		    {
			succElem.setID( seq.toActivity.actID);
			succElem.setName(seq.toActivity.name);
			succElem.type = GraphObject.GraphObjectType.ACTIVITY;
			succElem.type2 = "";
		    }
		    else if (seq.toEvent != null)
		    {
			succElem.setID(seq.toEvent.eventID);
			succElem.setName(seq.toEvent.eventName);
			succElem.type = GraphObject.GraphObjectType.EVENT;
			succElem.type2 = Integer.toString(seq.toEvent.eventPosition);
		    }
		    else if (seq.toGateWay != null)
		    {
			succElem.setID(seq.toGateWay.gateID);
			succElem.setName(seq.toGateWay.name);
			succElem.type = GraphObjectType.GATEWAY;
			succElem.type2 = seq.toGateWay.type;
		    }
		    succs.add(succElem);
		    succs.add(seq);
		}
	    }
	}
	else if (elem.type.equals(GraphObjectType.GATEWAY) )
	{
	    // this will be changed to calls to the DB
	    // we have to resolve the id of the activity
	    //System.out.println("GateWay as source gateway id " + elem.id );
	    for (SequenceFlow seq : process.edges )
	    {
		if (seq.frmGateWay != null)
		{
		    // check that it is this activity
		    if (!seq.frmGateWay.gateID.equals(elem.getID())) continue;
		    // now check the to part
		    //System.out.println("Current index at which match is found " + i );
		    succElem = new GraphObject();
		    if (seq.toActivity != null)
		    {
			succElem.setID( seq.toActivity.actID);
			succElem.setName(seq.toActivity.name);
			succElem.type = GraphObject.GraphObjectType.ACTIVITY;
			succElem.type2 = "";
		    }
		    else if (seq.toEvent != null)
		    {
			succElem.setID(seq.toEvent.eventID);
			succElem.setName(seq.toEvent.eventName);
			succElem.type = GraphObject.GraphObjectType.EVENT;
			succElem.type2 = Integer.toString(seq.toEvent.eventPosition);
		    }
		    else if (seq.toGateWay != null)
		    {
			succElem.setID(seq.toGateWay.gateID);
			succElem.setName(seq.toGateWay.name);
			succElem.type = GraphObjectType.GATEWAY;
			succElem.type2 = seq.toGateWay.type;
		    }
		    succs.add(succElem);
		    succs.add(seq);

		}
	    }							
	}
	return succs;
    }

    public List<ProcessGraph> buildPaths(GraphObject startElem, GraphObject endElem,ProcessGraph process)
    {
	if (startElem.equals(endElem))
	    return buildPathsCyclic(startElem, endElem, process);
	else
	{
	    List<ProcessGraph> result,result3,result2= new ArrayList<ProcessGraph>();
	    result = buildPathsAcyclicShortest(startElem, endElem, process);
	    // we need to add a step to include only new paths
	    // These were added according to the new directives on 19.08.2009
	    if (this.pe == PathEvaluation.ACYCLIC || this.pe == PathEvaluation.CYCLIC)
	    {
		result3=buildPathsAcyclic(startElem, endElem, process);
		for (ProcessGraph p2 : result3)
		{   
		    boolean found = false;
		    for(ProcessGraph p1 : result)
		    {
			
			if (p1.nodes.toString().equals(p2.nodes.toString()))
			{
			    found = true;
			}
		    }
		    if (!found )
			result.add(p2);
		}
	    }
	    if (this.pe == PathEvaluation.CYCLIC)
		result2 = buildPathsCyclic(endElem, endElem, process);
	    
	    for (ProcessGraph o : result)
	    {
		for (ProcessGraph i: result2)
		{
		    for (GraphObject ob : i.nodes)
		    {
			if (!o.nodes.contains(ob))
			    o.nodes.add(ob);
		    }
		}
	    }
	    return result;
	}
    }

    /**
     * finds cyclic paths between source and destination nodes in a process
     * 
     * graph
     * 
     * @param startElem a graph object start node
     * @param endElem a graph object end node
     * @param process the process graph in which a path is searched for
     * @return returns a list of process subgraphs each consisting of a match 
     */
    public List<ProcessGraph> buildPathsCyclic(GraphObject startElem, GraphObject endElem,ProcessGraph process) // returns a vector of paths
    {
	GraphObject currentElem;

	List<ProcessGraph> paths =  new ArrayList<ProcessGraph>(); // vector of paths
	ProcessGraph newPath = new ProcessGraph();

	VisitedListElement vlelem,prevelem;
	currentElem = startElem;
	Stack<GraphObject> toVisitObjects;
	List<VisitedListElement> visitedObjects = new ArrayList<VisitedListElement>(); // a list of visited list element
	List<SequenceFlow> VisitedEdges = new ArrayList<SequenceFlow>(); // a list of current edges.
	toVisitObjects = new Stack<GraphObject>();
	List<GraphObject> visitedNodes = new ArrayList<GraphObject>();

	ProcessGraph succs;
	succs = getSuccessorsAsPath(currentElem,process);

	// add the current element to the visited list element
	vlelem = new VisitedListElement();
	vlelem.elem = currentElem;
	vlelem.successorsAll.addAll(succs.nodes);
	vlelem.successorsVisited = new ArrayList<GraphObject>();
	visitedObjects.add(vlelem);
	VisitedEdges.addAll(succs.edges);

	for (int j = 0; j < succs.nodes.size();j++)
	{

	    toVisitObjects.push(succs.nodes.get(j));
	}

	visitedNodes.add(currentElem);
	while (!toVisitObjects.empty())
	{

	    // get the element on top of the stack,
	    // get its successors
	    // put it at the end of the visited list
	    // and check if this element is the target node

	    currentElem = (GraphObject) toVisitObjects.pop();

	    succs = getSuccessorsAsPath(currentElem,process);

	    // add the element popped to the visited list
	    vlelem = new VisitedListElement();
	    vlelem.elem = currentElem;
	    vlelem.successorsAll = succs.nodes;
	    vlelem.successorsVisited = new ArrayList<GraphObject>();
	    visitedObjects.add(vlelem);// add to the visited list
	    VisitedEdges.addAll(succs.edges);



	    if (visitedObjects.size() >= 2)
	    {
		prevelem = visitedObjects.get(visitedObjects.size()-2); // it was -2

		if (!prevelem.successorsVisited.contains(vlelem.elem) && prevelem.successorsAll.contains(vlelem.elem))
		    prevelem.successorsVisited.add(vlelem.elem);
		// update the previous element info
		visitedObjects.set(visitedObjects.size()-2,prevelem);
	    }
	    if (currentElem.toString().equals(endElem.toString()))
	    {
		// a complete path has been found
		for (int x = 0; x < visitedObjects.size();x++)
		    newPath.nodes.add(visitedObjects.get(x).elem);
		if (!newPath.nodes.contains(endElem))
		    newPath.nodes.add(endElem);
		boolean pathfound = false;
		for (ProcessGraph p :paths)
		{
		    if (p.nodes.containsAll(newPath.nodes))
		    {
			pathfound = true;
			break;
		    }
		}
		if (!pathfound)
		    paths.add(newPath);

		newPath = new ProcessGraph();
		// we have to delete the last node because it is the target
		visitedObjects.remove(visitedObjects.size()-1);

		// delete from the visited object vectors all those that have equal size of sub vectors
		int maxCnt = visitedObjects.size();
		int cnt = 0;
		while(cnt < maxCnt)
		{
		    // this should be true with the last found path
		    if (visitedObjects.size()==0) 
			break;

		    try
		    {
//			log.debug("Current Visited Object Vector Size "+VisitedObjects.size());
			vlelem = visitedObjects.get(visitedObjects.size()-1-cnt);
//			if (vlelem.successorsAll.size()==vlelem.successorsVisited.size())
			if (vlelem.successorsVisited.containsAll(vlelem.successorsAll))
			    visitedNodes.remove(visitedObjects.remove(visitedObjects.size()-1).elem);
//			else
//			break;
		    }
		    catch (Exception e)
		    {
			System.err.println("Something is seriously flawed here (PathBuilder.java:279), and catch Exception can't be the solution! Please write a JUnit test case and then fix the issue.");
			System.err.println(e.getMessage());
			break;
		    }
		    cnt++;
		}

		// we have to add the successors in case of a loop path
		for (int j = 0; j < succs.nodes.size();j++)
		{
		    boolean found = false;
		    for (int m= 0; m < visitedObjects.size(); m++ )

		    {

			if (((GraphObject)(visitedObjects.get(m)).elem).getID().equals(succs.nodes.get(j).getID()) &&
				((GraphObject)(visitedObjects.get(m)).elem).getName().equals(succs.nodes.get(j).getName()) &&
				((GraphObject)(visitedObjects.get(m)).elem).type.equals(succs.nodes.get(j).type))
			{
			    // we have also to update the visited successors list
			    if (m > 1)
			    {	
				prevelem = (VisitedListElement) visitedObjects.get(m-1);
				//System.out.println("Previous element name " +prevelem.elem.name);
				if (!prevelem.successorsVisited.contains(succs.nodes.get(j)) && prevelem.successorsAll.contains(succs.nodes.get(j)))
				    prevelem.successorsVisited.add(succs.nodes.get(j));// update the previous element info
				visitedObjects.set(visitedObjects.size()-1,prevelem);
			    }
			    found = true;
			}

		    }

		    if (!found  && !toVisitObjects.contains(succs.nodes.get(j)) && !alreadyOnAPath(paths, succs.nodes.get(j))) //&& !visitedNodes.contains(succs.nodes.get(j)))
			toVisitObjects.push(succs.nodes.get(j));
		    // anyway we have to update the visited edges

		}

	    }
	    else if (succs.nodes.size() == 0) // we reached a dead end
	    {

		// backtrack to a node from which we can start a new search.
		// delete from the visited object vectors all those that have equal size of sub vectors
		visitedObjects.remove(visitedObjects.size()-1);
		visitedNodes.remove(currentElem);
		int maxCnt = visitedObjects.size();
		int cnt = 0;
		while(cnt < maxCnt)
		{
		    // this should be true with the last found path
		    if (visitedObjects.size()==0) break;

		    try
		    {

			vlelem = (VisitedListElement) visitedObjects.get(visitedObjects.size()-1-cnt);
			if (vlelem.successorsVisited.containsAll(vlelem.successorsAll))
			    visitedNodes.remove(visitedObjects.remove(visitedObjects.size()-1).elem);
			else
			    cnt++;
//			break;
		    }
		    catch (Exception e)
		    {
//			System.err.println("Something is seriously flawed here (PathBuilder.java:336), and catch Exception can't be the solution! Please write a JUnit test case and then fix the issue.");
			break;
		    }
		}
	    }
	    else // no match
	    {
		for (int j = 0; j < succs.nodes.size();j++)
		{
		    // we have to be sure that the added object to the stack does not have a copy in the currently visited objects
		    boolean found = false;
		    for (int m= 0; m < visitedObjects.size(); m++ )
		    {
			if (((GraphObject)(visitedObjects.get(m)).elem).getID().equals(succs.nodes.get(j).getID()) &&
				((GraphObject)(visitedObjects.get(m)).elem).getName().equals(succs.nodes.get(j).getName()) &&
				((GraphObject)(visitedObjects.get(m)).elem).type.equals(succs.nodes.get(j).type))
			{
			    // we have also to update the visited successors list
			    prevelem = (VisitedListElement) visitedObjects.get(visitedObjects.size()-1);
			    //System.out.println("Previous element name " +prevelem.elem.name);
			    if (!prevelem.successorsVisited.contains(succs.nodes.get(j)) && prevelem.successorsAll.contains(succs.nodes.get(j)))
				prevelem.successorsVisited.add(succs.nodes.get(j));// update the previous element info
			    visitedObjects.set(visitedObjects.size()-1,prevelem);
			    found = true;
			    break;
			}

		    }

		    if (!found && !visitedNodes.contains(succs.nodes.get(j)) && !toVisitObjects.contains(succs.nodes.get(j)) && !alreadyOnAPath(paths, succs.nodes.get(j)))
			toVisitObjects.push(succs.nodes.get(j));
		    if (found && paths.size() > 0) // this means there is another path that shares parts of an old path
		    {

			for (int x = 0; x < visitedObjects.size();x++)
			    newPath.nodes.add(visitedObjects.get(x).elem);
			if (!newPath.nodes.contains(endElem))
			    newPath.nodes.add(endElem);
			boolean pathfound = false;
			for (ProcessGraph p :paths)
			{
			    if (p.nodes.containsAll(newPath.nodes))
			    {
				pathfound = true;
				break;
			    }
			}
			if (!pathfound)
			    paths.add(newPath);
			newPath = new ProcessGraph();
		    }
		    else if (found && succs.nodes.get(j).equals(endElem))
		    {
			for (int x = 0; x < visitedObjects.size();x++)
			    newPath.nodes.add(visitedObjects.get(x).elem);
			if (!newPath.nodes.contains(endElem))
			    newPath.nodes.add(endElem);
			boolean pathfound = false;
			for (ProcessGraph p :paths)
			{
			    if (p.nodes.containsAll(newPath.nodes))
			    {
				pathfound = true;
				break;
			    }
			}
			if (!pathfound)
			    paths.add(newPath);
			newPath = new ProcessGraph();
		    }
		    // anyway we have to update the visited edges

		}
	    }

	    if (!currentElem.toString().equals(endElem.toString()))
		visitedNodes.add(currentElem);
	}
	return paths;
    }

    public void buildAllPaths(ProcessGraph process)
    {
	List<ProcessGraph> result;
	for (GraphObject go :  process.getStartupNodes())
	    for(GraphObject go2 : process.getEndNodes())
	    {
		result = buildPaths(go,go2, process);
		for (ProcessGraph p : result)
		{
//		    p.nodes.remove(p.nodes.size()-1);
		    log.info(p.nodes.toString());
		    log.info("Path length " + p.nodes.size());
		}
	    }
    }

    private boolean alreadyOnAPath(List<ProcessGraph> rs, GraphObject nd)
    {
	for (ProcessGraph p : rs)
	    if (p.nodes.contains(nd))
		return true;
	return false;
    }

//  private boolean alreadyOnAPath(List<ProcessGraph> rs, List<GraphObject> nds)
//  {
//  for (ProcessGraph p : rs)
//  if (p.nodes.toString().equals(nds.toString()))
//  return true;
//  return false;
//  }
    /**
     * finds acyclic paths between source and destination nodes in a process
     * 
     * graph
     * 
     * @param startElem a graph object start node
     * @param endElem a graph object end node
     * @param process the process graph in which a path is searched for
     * @return returns a list of process subgraphs each consisting of a match 
     */
    public List<ProcessGraph> buildPathsAcyclic(GraphObject startElem, GraphObject endElem,ProcessGraph process)
    {
	GraphObject currentElem;

	List<ProcessGraph> paths =  new ArrayList<ProcessGraph>(); // vector of paths
	ProcessGraph newPath = new ProcessGraph();

	VisitedListElement vlelem;
	currentElem = startElem;

	Stack<GraphObject> toVisitObjects;
	List<VisitedListElement> VisitedObjects = new ArrayList<VisitedListElement>(); // a list of visited list element

	toVisitObjects = new Stack<GraphObject>();


	ProcessGraph succs;
	succs = getSuccessorsAsPath(currentElem,process);

	// add the current element to the visited list element
	vlelem = new VisitedListElement();
	vlelem.elem = currentElem;
	vlelem.successorsAll.addAll(succs.nodes);
	vlelem.successorsVisited = new ArrayList<GraphObject>();
	VisitedObjects.add(vlelem);



	for (int j = 0; j < succs.nodes.size();j++)
	    toVisitObjects.push(succs.nodes.get(j));

	while (!toVisitObjects.empty())
	{
	    currentElem = (GraphObject) toVisitObjects.pop();

	    succs = getSuccessorsAsPath(currentElem,process);

	    // add the element popped to the visited list
	    vlelem = new VisitedListElement();
	    vlelem.elem = currentElem;
	    vlelem.successorsAll = succs.nodes;
	    vlelem.successorsVisited = new ArrayList<GraphObject>();
	    VisitedObjects.add(vlelem);// add to the visited list

	    updatePrevVisitedElement(VisitedObjects, currentElem);
	    if (currentElem.toString().equals(endElem.toString()))
	    {
		// a complete path has been found
		for (int x = 0; x < VisitedObjects.size();x++)
		    newPath.nodes.add(VisitedObjects.get(x).elem);
		if (!newPath.nodes.contains(endElem))
		    newPath.nodes.add(endElem);
		boolean pathfound = false;
		for (ProcessGraph p :paths)
		{
		    if (p.nodes.containsAll(newPath.nodes))
		    {
			pathfound = true;
			break;
		    }
		}
		if (!pathfound)
		    paths.add(newPath);

		newPath = new ProcessGraph();
		// we have to delete the last node because it is the target
		VisitedObjects.remove(VisitedObjects.size()-1);
		VisitedObjects = removeNodesWithAllVisitedChildren(VisitedObjects);
	    }
	    else if (succs.nodes.size()==0) // we reached a dead end
	    {
		// backtrack to a node from which we can start a new search.
		// delete from the visited object vectors all those that have equal size of sub vectors
		VisitedObjects.remove(VisitedObjects.size()-1);
		VisitedObjects = removeNodesWithAllVisitedChildren(VisitedObjects);
	    }
	    else // no match
	    {
		for (int j = 0; j < succs.nodes.size();j++)
		{
		    // we have to be sure that the added object to the stack does not have a copy in the currently visited objects
		    boolean found = false;
		    for (int m= 0; m < VisitedObjects.size(); m++ )
		    {
			if (((GraphObject)(VisitedObjects.get(m)).elem).getID().equals(succs.nodes.get(j).getID()) &&
				((GraphObject)(VisitedObjects.get(m)).elem).getName().equals(succs.nodes.get(j).getName()) &&
				((GraphObject)(VisitedObjects.get(m)).elem).type.equals(succs.nodes.get(j).type))
			{
			    updatePrevVisitedElement(VisitedObjects, succs.nodes.get(j));
			    found = true;
			    break;
			}

		    }
		    // I am not sure about this following step
//		    if (toVisitObjects.contains(succs.nodes.get(j)))
//			updatePrevVisitedElement(VisitedObjects, succs.nodes.get(j));
		    
		    if (!found) // && !toVisitObjects.contains(succs.nodes.get(j)) )
			toVisitObjects.push(succs.nodes.get(j));

		}
	    }
	}
	return paths;
    }
    /**
     * finds shortest acyclic paths between source and destination nodes in a process
     * 
     * graph
     * 
     * @param startElem a graph object start node
     * @param endElem a graph object end node
     * @param process the process graph in which a path is searched for
     * @return returns a list of process subgraphs each consisting of a match 
     */
    public List<ProcessGraph> buildPathsAcyclicShortest(GraphObject startElem, GraphObject endElem,ProcessGraph process)
    {
	GraphObject currentElem;

	List<ProcessGraph> paths =  new ArrayList<ProcessGraph>(); // vector of paths
	ProcessGraph newPath = new ProcessGraph();

	VisitedListElement vlelem;
	currentElem = startElem;

	Stack<GraphObject> toVisitObjects;
	List<VisitedListElement> visitedObjects = new ArrayList<VisitedListElement>(); // a list of visited list element

	toVisitObjects = new Stack<GraphObject>();


	ProcessGraph succs;
	succs = getSuccessorsAsPath(currentElem,process);

	// add the current element to the visited list element
	vlelem = new VisitedListElement();
	vlelem.elem = currentElem;
	vlelem.successorsAll.addAll(succs.nodes);
	vlelem.successorsVisited = new ArrayList<GraphObject>();
	visitedObjects.add(vlelem);



	for (int j = 0; j < succs.nodes.size();j++)
	    toVisitObjects.push(succs.nodes.get(j));

	while (!toVisitObjects.empty())
	{

	    currentElem = (GraphObject) toVisitObjects.pop();

	    succs = getSuccessorsAsPath(currentElem,process);

	    // add the element popped to the visited list
	    vlelem = new VisitedListElement();
	    vlelem.elem = currentElem;
	    vlelem.successorsAll = succs.nodes;
	    vlelem.successorsVisited = new ArrayList<GraphObject>();
	    visitedObjects.add(vlelem);// add to the visited list


	    updatePrevVisitedElement(visitedObjects, currentElem);
	    if (currentElem.toString().equals(endElem.toString()))
	    {
		// a complete path has been found
		for (int x = 0; x < visitedObjects.size();x++)
		    newPath.nodes.add(visitedObjects.get(x).elem);
		if (!newPath.nodes.contains(endElem))
		    newPath.nodes.add(endElem);
//		boolean pathfound = false;
//		for (ProcessGraph p :paths)
//		{
//		if (p.nodes.containsAll(newPath.nodes))
//		{
//		pathfound = true;
//		break;
//		}
//		}
//		if (!pathfound)
		boolean copyFound = false;
		for (ProcessGraph crnt : paths)
		{
		    if (crnt.nodes.toString().equals(newPath.nodes.toString()))
		    {
			copyFound = true;
			break;
		    }
		}
		if (!copyFound)
		    paths.add(newPath);

		//return paths;
		// The next commented part has something wrong leads to infinite loops
		newPath = new ProcessGraph();
		// we have to delete the last node because it is the target
		visitedObjects.remove(visitedObjects.size()-1);
		visitedObjects = removeNodesWithAllVisitedChildren(visitedObjects);
	    }
	    else if (succs.nodes.size()==0) // we reached a dead end
	    {

		// backtrack to a node from which we can start a new search.
		// delete from the visited object vectors all those that have equal size of sub vectors
		visitedObjects.remove(visitedObjects.size()-1);

		visitedObjects = removeNodesWithAllVisitedChildren(visitedObjects);
	    }
	    else // no match
	    {
		for (int j = 0; j < succs.nodes.size();j++)
		{
		    // we have to be sure that the added object to the stack does not have a copy in the currently visited objects
		    boolean found = false;
		    for (int m= 0; m < visitedObjects.size(); m++ )
		    {
			if (((GraphObject)(visitedObjects.get(m)).elem).getID().equals(succs.nodes.get(j).getID()) &&
				((GraphObject)(visitedObjects.get(m)).elem).getName().equals(succs.nodes.get(j).getName()) &&
				((GraphObject)(visitedObjects.get(m)).elem).type.equals(succs.nodes.get(j).type))
			{

			    List<ProcessGraph> pp = buildPathsCyclic(succs.nodes.get(j), succs.nodes.get(j), process);
			    if (pp.size() == 0) // there is no cycle
				toVisitObjects.push(succs.nodes.get(j));
			    else
			    {
				updatePrevVisitedElement(visitedObjects, succs.nodes.get(j));
				visitedObjects = removeNodesWithAllVisitedChildren(visitedObjects);
			    }

			    found = true;
			    break;
			}

		    }



		    if (!found )// there is a cycle and we need to have non cyclic paths
			toVisitObjects.push(succs.nodes.get(j));
		}
	    }
	}
	return paths;
    }
//  private List<GraphObject> getElemNodes(List<VisitedListElement> in)
//  {
//  List<GraphObject> result = new ArrayList<GraphObject>();
//  for (VisitedListElement vle : in)
//  result.add(vle.elem);
//  return result;
//  }
    private List<VisitedListElement> removeNodesWithAllVisitedChildren(List<VisitedListElement> in)
    {
	VisitedListElement vlelem;
	while(in.size() > 0 )
	{
	    // this should be true with the last found path

	    vlelem = in.get(in.size()-1);

	    if (vlelem.successorsVisited.containsAll(vlelem.successorsAll))
		in.remove(vlelem);

	    else
		break ; // 
	}
	return in;
    }

//  private List<VisitedListElement> getPrevVisitedElement(List<VisitedListElement> visited,GraphObject currentNode)
//  {
//  List<VisitedListElement> prev = new ArrayList<VisitedListElement>();
//  for (VisitedListElement vle : visited)
//  {
//  if (vle.successorsAll.contains(currentNode))
//  prev.add(vle);
//  }
//  return prev;

//  }
    private void updatePrevVisitedElement(List<VisitedListElement> visited, GraphObject currentNode)
    {
	int sz = visited.size();
	VisitedListElement vle;
	for (int i = sz -1; i >=0; i--)
//	    for(VisitedListElement vle :visited)
	{
	    vle  = visited.get(i);
	    if (vle.successorsAll.contains(currentNode) && !vle.successorsVisited.contains(currentNode))
	    {
		vle.successorsVisited.add(currentNode);
		break;

	    }



	}
    }
}
