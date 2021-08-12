package com.bpmnq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
//import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.bpmnq.Association.AssociaitonType;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.ProcessGraphBuilderRDF.RdfSyntax;




import static com.bpmnq.GraphObject.GraphObjectType.*;

public class ProcessGraph implements Cloneable
{
    public List<GraphObject> nodes;
    public List<SequenceFlow> edges;
    
    /** Support for data objects 
     * @since 31.07.08
     */
    public List<DataObject> dataObjs;
    public List<Association> associations; 
    //public int modelID;
    public String modelURI;
    
    private Logger log = Logger.getLogger(ProcessGraph.class);

    public ProcessGraph() {
	nodes = new ArrayList<GraphObject>();
	edges = new ArrayList<SequenceFlow>();
	dataObjs = new ArrayList<DataObject>();
	associations = new ArrayList<Association> ();
	modelURI = "####";
    }
    
    public Object clone()
    {
	try
	{
	    ProcessGraph clone = (ProcessGraph)super.clone();
	    
	    clone.nodes = new ArrayList<GraphObject>(this.nodes.size());
	    for (GraphObject node : this.nodes)
	    {
		GraphObject nClone = (GraphObject)node.clone();
		clone.add(nClone);
	    }
	    
	    clone.edges = new ArrayList<SequenceFlow>(this.edges.size());
	    for (SequenceFlow edge : this.edges)
	    {
		SequenceFlow eClone = (SequenceFlow)edge.clone();
		clone.add(eClone);
	    }
	    
	    clone.dataObjs = new ArrayList<DataObject>(this.dataObjs.size());
	    for (DataObject dObj : this.dataObjs)
	    {
		DataObject doClone = (DataObject)dObj.clone();
		clone.add(doClone);
	    }
	    
	    clone.associations = new ArrayList<Association>(this.associations.size());
	    for (Association assoc : this.associations)
	    {
		Association aClone = (Association)assoc.clone();
		clone.add(aClone);
	    }
	    clone.modelURI = this.modelURI;
	    return clone;
	} catch (CloneNotSupportedException e)
	{
	    return null;
	}
    }

    /**
     * Add a graph object node to this process graph.
     * 
     * If this graph object is already part of the graph, this method does nothing.
     * 
     * @param go The graph object to be added.
     * @return TODO
     */
    public boolean add(GraphObject go) {
	if (!nodes.contains(go)) {
	    nodes.add(go);
	    return true;
	}
	return false;
    }

    /**
     * Add a data object node to this process graph.
     * 
     * If this data object is already part of the graph, this method does nothing.
     * 
     * @param neu The data object to be added.
     */
    public void add(DataObject neu)
    {
    	if (!dataObjs.contains(neu))
    		dataObjs.add(neu);
    }
    
    public boolean hasSubGraph(ProcessGraph other) {
	return (this.nodes.containsAll(other.nodes)
		&& this.edges.containsAll(other.edges));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     * (Eclipse-generated)
     */
    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((associations == null) ? 0 : associations.hashCode());
	result = prime * result
		+ ((dataObjs == null) ? 0 : dataObjs.hashCode());
	result = prime * result + ((edges == null) ? 0 : edges.hashCode());
	result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
	return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     * (Eclipse-generated)
     */
    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof ProcessGraph))
	    return false;
	final ProcessGraph other = (ProcessGraph) obj;
	if (associations == null)
	{
	    if (other.associations != null)
		return false;
	} else if (!associations.equals(other.associations))
	    return false;
	if (dataObjs == null)
	{
	    if (other.dataObjs != null)
		return false;
	} else if (!dataObjs.equals(other.dataObjs))
	    return false;
	if (edges == null)
	{
	    if (other.edges != null)
		return false;
	} else if (!edges.equals(other.edges))
	    return false;
	if (nodes == null)
	{
	    if (other.nodes != null)
		return false;
	} else if (!nodes.equals(other.nodes))
	    return false;
	return true;
    }
    
    /**
     * Adds an edge (directed arc) to this process graph, connecting the graph 
     * nodes <code>from</code> and <code>to</code>.
     * 
     * If such an edge exists already, this method does nothing.
     * 
     * @param from Source of the arc.
     * @param to End of the arc.
     */
    public void addEdge(GraphObject from, GraphObject to) {
	SequenceFlow sq = new SequenceFlow(from, to);
	add(sq);
    }

    /**
     * Adds an edge (directed arc) to this process graph.
     * 
     * If such an edge exists already, this method does nothing.
     * 
     * @param edge SequenceFlow object describing the edge
     */
    public void add(SequenceFlow edge) {
	if (!edges.contains(edge)) {
	    edges.add(edge);
	}
    }
    
    /**
     * Adds an association edge (directed arc) to this process graph, connecting the graph 
     * nodes <code>from</code> and <code>to</code>.
     * 
     * If such an association edge exists already, this method does nothing.
     * 
     * @param from Source of the arc.
     * @param to End of the arc.
     */
    public void addAssociation(GraphObject from, GraphObject to) {
	Association ass = new Association(from, to);
	if (!associations.contains(ass))
		add(ass);
    }

    /**
     * Adds an association edge (directed arc) to this process graph.
     * 
     * If such an association edge exists already, this method does nothing.
     * 
     * @param edge Association object
     */
    public void add(Association edge) {
	if (!associations.contains(edge)) {
	    associations.add(edge);
	}
    }

    public void removeEdge(GraphObject frm,GraphObject too)
    {
	int cnt = this.edges.size();
	SequenceFlow sq;
	if (frm.type == ACTIVITY) {

	    if (too.type == ACTIVITY) {
		for (int i = 0; i < cnt; i++) {
		    sq = edges.get(i);
		    if (sq.frmActivity != null && sq.toActivity != null)
			if (sq.frmActivity.actID.equals(frm.getID())
				&& sq.toActivity.actID.equals(too.getID())) {
			    edges.remove(i);
			    return;
			}
		}
	    } else if (too.type == EVENT) {
		for (int i = 0; i < cnt; i++) {
		    sq = edges.get(i);
		    if (sq.frmActivity != null && sq.toEvent != null)
			if (sq.frmActivity.actID.equals(frm.getID())
				&& sq.toEvent.eventID.equals(too.getID())) {
			    edges.remove(i);
			    return;
			}
		}
	    } else if (too.type == GATEWAY) {
		for (int i = 0; i < cnt; i++) {
		    sq = edges.get(i);
		    if (sq.frmActivity != null && sq.toGateWay != null)
			if (sq.frmActivity.actID.equals(frm.getID())
				&& sq.toGateWay.gateID.equals(too.getID())) {
			    edges.remove(i);
			    return;
			}
		}
	    }
	} // end of (frm.type == ACTIVITY)
	else if (frm.type == EVENT) {
	    if (too.type == ACTIVITY) {
		for (int i = 0; i < cnt; i++) {
		    sq = edges.get(i);
		    if (sq.frmEvent != null && sq.toActivity != null)
			if (sq.frmEvent.eventID.equals(frm.getID())
				&& sq.toActivity.actID.equals(too.getID())) {
			    edges.remove(i);
			    return;
			}
		}
	    } else if (too.type == EVENT) {
		for (int i = 0; i < cnt; i++) {
		    sq = edges.get(i);
		    if (sq.frmEvent != null && sq.toEvent != null)
			if (sq.frmEvent.eventID.equals(frm.getID())
				&& sq.toEvent.eventID.equals(too.getID())) {
			    edges.remove(i);
			    return;
			}
		}
	    } else if (too.type == GATEWAY) {
		for (int i = 0; i < cnt; i++) {
		    sq = edges.get(i);
		    if (sq.frmEvent != null && sq.toGateWay != null)
			if (sq.frmEvent.eventID.equals(frm.getID())
				&& sq.toGateWay.gateID.equals(too.getID())) {
			    edges.remove(i);
			    return;
			}
		}
	    }
	} // end of (frm.type == EVENT)
	else if (frm.type == GATEWAY) {
	    if (too.type == ACTIVITY) {
		for (int i = 0; i < cnt; i++) {
		    sq = edges.get(i);
		    if (sq.frmGateWay != null && sq.toActivity != null)
			if (sq.frmGateWay.gateID.equals(frm.getID())
				&& sq.toActivity.actID.equals(too.getID())) {
			    edges.remove(i);
			    return;
			}
		}
	    } else if (too.type == EVENT) {
		for (int i = 0; i < cnt; i++) {
		    sq = edges.get(i);
		    if (sq.frmGateWay != null && sq.toEvent != null)
			if (sq.frmGateWay.gateID.equals(frm.getID())
				&& sq.toEvent.eventID.equals(too.getID())) {
			    edges.remove(i);
			    return;
			}
		}
	    } else if (too.type == GATEWAY) {
		for (int i = 0; i < cnt; i++) {
		    sq = edges.get(i);
		    if (sq.frmGateWay != null && sq.toGateWay != null)
			if (sq.frmGateWay.gateID.equals(frm.getID())
				&& sq.toGateWay.gateID.equals(too.getID())) {
			    edges.remove(i);
			    return;
			}
		}
	    }
	}

    }
    
    /**
     * Removes an association edge (directed arc) from this process graph.
     * 
     * @param from GraphObject object describing the source of the association
     * @param to GraphObject object describing the destination of the association
     */
    public void removeAssociation(GraphObject from, GraphObject to)
    {
    int cnt = this.associations.size();
	Association sq;
	if (from.type == ACTIVITY) 
	{

	    if (to.type == DATAOBJECT) 
	    {
		for (int i = 0; i < cnt; i++) 
		{
		    sq = associations.get(i);
		    if (sq.frmActivity != null && sq.toDataObject != null)
			if (sq.frmActivity.actID.equals(from.getID())
				&& sq.toDataObject.doID.equals(to.getID())) 
			{
			    associations.remove(i);
			    return;
			}
		}
	     
	    } 
	} // end of (from.type == ACTIVITY)
	else if (from.type == EVENT) 
	{
	    if (to.type == DATAOBJECT) {
		for (int i = 0; i < cnt; i++) {
		    sq = associations.get(i);
		    if (sq.frmEvent != null && sq.toDataObject != null)
			if (sq.frmEvent.eventID.equals(from.getID())
				&& sq.toDataObject.doID.equals(to.getID())) {
			    associations.remove(i);
			    return;
			}
		}
	    }
	} // end of (from.type == EVENT)
	else if (from.type == DATAOBJECT) {
	    if (to.type == ACTIVITY) {
		for (int i = 0; i < cnt; i++) {
		    sq = associations.get(i);
		    if (sq.frmDataObject != null && sq.toActivity != null)
			if (sq.frmDataObject.doID.equals(from.getID())
				&& sq.toActivity.actID.equals(to.getID())) {
			    associations.remove(i);
			    return;
			}
		}
	    } else if (to.type == EVENT) {
		for (int i = 0; i < cnt; i++) {
		    sq = associations.get(i);
		    if (sq.frmDataObject != null && sq.toEvent != null)
			if (sq.frmDataObject.doID.equals(from.getID())
				&& sq.toEvent.eventID.equals(to.getID())) {
			    associations.remove(i);
			    return;
			}
		}
	    } 
	}

    }
    
    /**
     * Removes <code>node</code> from this process graph, if it exists there.
     * Edges going from/to this node will remain in the graph, thus potentially
     * ending nowhere.
     *  
     * @param node
     */
    public void remove(GraphObject node) {
	if (nodes.size() == 0)
	    return;
	for (int i = 0; i < nodes.size(); i++)
	    if (nodes.get(i).equals(node)) {
		nodes.remove(i);
		break;
	    }
    }
    
    public List<Association> getIncomingAssociation(GraphObject dest)
    {
    	List<Association> result = new ArrayList<Association>();
    	for (Association assoc : associations)
    		if (assoc.getDestination().equals(dest))
    			result.add(assoc);
    	return result;
    }
    
    public List<Association> getOutgoingAssociation(GraphObject src)
    {
    	List<Association> result = new ArrayList<Association>();
    	for (Association assoc : associations)
    		if (assoc.getSource().equals(src))
    			result.add(assoc);
    	return result;
    }
    
    public void removeEdgesWithDestination(GraphObject node) {
	if (edges.size() == 0) 
	    return;
	
	boolean matchFound = false;
	SequenceFlow currentEdge;
	for (int i = 0; i < edges.size(); i++)
	{
	    currentEdge = edges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.actID.equals(node.getID()) && currentEdge.toActivity.name.equals(node.getName()))
		    {
			matchFound = true;
			edges.remove(i);
			break;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventID.equals(node.getID()) && currentEdge.toEvent.eventName.equals(node.getName()))
		    {
			matchFound = true;
			edges.remove(i);
			break;
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.gateID.equals(node.getID()) && currentEdge.toGateWay.name.equals(node.getName()))
		    {
			matchFound = true;
			edges.remove(i);
			break;
		    }
	    }
	}
	if (matchFound) 
	    removeEdgesWithDestination(node);
    }
    
    public void removeEdgesWithSource(GraphObject node)
    {
	if (edges.size() == 0) return;
	boolean matchFound = false;
	SequenceFlow currentEdge;
	for (int i =0; i < edges.size();i++)
	{
	    currentEdge = edges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.actID.equals(node.getID()) && currentEdge.frmActivity.name.equals(node.getName()))
		    {
			matchFound = true;
			edges.remove(i);
			break;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventID.equals(node.getID()) && currentEdge.frmEvent.eventName.equals(node.getName()))
		    {
			matchFound = true;
			edges.remove(i);
			break;
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.gateID.equals(node.getID()) && currentEdge.frmGateWay.name.equals(node.getName()))
		    {
			matchFound = true;
			edges.remove(i);
			break;
		    }
	    }
	}
	if (matchFound) removeEdgesWithSource(node);
    }

    public void print(PrintStream outStream)
    {
	try
	{
	    for (int i = edges.size()-1; i >= 0; i--)
	    {
		SequenceFlow edge = edges.get(i);
		if (edge.frmActivity != null)
		{
		    if (!edge.frmActivity.name.startsWith("$#"))
			outStream.print("From Activity: " + edge.frmActivity.name);
		    else
			outStream.print("From Activity: " + edge.frmActivity.actID);

		}
		else if (edge.frmGateWay!= null)
		{
		    //if (!edges.get(i).frmGateWay.gateWayName.startsWith("$#"))
		    //	outStream.print("From Gatway: " + edges.get(i).frmGateWay.gateWayName);
		    //else
		    outStream.print("From Gatway: " + edge.frmGateWay.type+ " " + edge.frmGateWay.gateID);

		} 
		else if (edge.frmEvent!= null)
		{
		    if (!edge.frmEvent.eventName.startsWith("$#"))
			outStream.print("From Event: " + edge.frmEvent.eventName);
		    else
			outStream.print("From Event: " + edge.frmEvent.eventID);
		}

		if (edge.toActivity != null)
		{
		    if (!edge.toActivity.name.startsWith("$#"))
			outStream.println("...To Activity: " + edge.toActivity.name);
		    else
			outStream.println("...To Activity: " + edge.toActivity.actID);

		}
		else if (edge.toGateWay!= null)
		{
		    //if (!edges.get(i).toGateWay.gateWayName.startsWith("$#"))
		    //	outStream.println("...To Gatway: " + edges.get(i).toGateWay.gateWayName);
		    //else
		    outStream.println("...To Gatway: "+ edge.toGateWay.type + " " + edge.toGateWay.gateID);

		} 
		else if (edge.toEvent!= null)
		{
		    if (!edge.toEvent.eventName.startsWith("$#"))
			outStream.println("...To Event: " + edge.toEvent.eventName);
		    else
			outStream.println("...To Event: " + edge.toEvent.eventID);
		}

	    }
	    for (Association ass : associations)
	    {
		if (ass.assType == null)
		    ass.assType = AssociaitonType.Structural;
		if (ass.frmActivity != null)
		{
		    outStream.println(ass.assType.toString()+ " association From Activity "+ass.frmActivity.name + " to Data Object "+ass.toDataObject.name +"("+ass.toDataObject.getState()+")");

		}
		else if (ass.frmEvent != null)
		{
		    outStream.println(ass.assType.toString()+ " association From Event "+ass.frmEvent.eventName + " to Data Object "+ass.toDataObject.name +"("+ass.toDataObject.getState()+")");

		}
		else if (ass.frmDataObject != null)
		{
		    if (ass.toActivity != null)
			outStream.println(ass.assType.toString()+ " association From Data Object "+ass.frmDataObject.name +"("+ass.frmDataObject.getState()+") "+ " to Activity "+ass.toActivity.name );
		    else
			outStream.println(ass.assType.toString()+ " association From Data Object "+ass.frmDataObject.name +"("+ass.frmDataObject.getState()+") "+ " to event "+ass.toEvent.eventName );


		}


	    }
	    // Nodes
	    for(GraphObject nd : nodes)
	    {
		outStream.println(nd.toString());
	    }
	} catch(NullPointerException npe)
	{
	    System.out.println(npe.getMessage());
	}
    }

    public void union(ProcessGraph otherPath) {
	// first add the nodes
	for (int j = 0; j < otherPath.nodes.size(); j++) {
	    /* TODO remove old code
	     * if (!this.nodes.contains(otherPath.nodes.get(j)))
	     * //get(i).equals(otherPath.nodes.get(j))) {
	     * this.nodes.add(otherPath.nodes.get(j)); }
	     */
	    this.add(otherPath.nodes.get(j));
	}

	for (int i = 0; i < otherPath.edges.size(); i++) {
	    /* TODO remove old code
	     * if (!this.edges.contains(otherPath.edges.get(i)))
	     * //get(i).equals(otherPath.nodes.get(j))) {
	     * this.edges.add(otherPath.edges.get(i)); }
	     */
	    this.union(otherPath.edges.get(i));
	}
    }

    public void union(SequenceFlow s) {
	// first add the nodes
	boolean isAlreadyInGraph = false;
	for (int i = 0; i < this.edges.size(); i++) {
	    // match = false;
	    if (this.edges.get(i).equals(s)) {
		isAlreadyInGraph = true;
		break;
	    }
	}

	if (!isAlreadyInGraph)
	    this.edges.add(s);
	/*// now the edges
		for (int i = 0; i < this.edges.size();i++)
			{
			match = false;

			for (j = 0; j < otherPath.edges.size();j++)
			{
				if (this.edges.get(i).equals(otherPath.edges.get(j)))
				{
					match = true;
					break;
				}
			}
			if (!match)
				this.nodes.add(otherPath.nodes.get(j));
		}*/

    }
    
    public boolean hasNode(GraphObject nd)
    {
	return nodes.contains(nd);
    }
    public void constructEdges(ProcessGraph other)
    {
	for (GraphObject src :this.nodes)
	    for(GraphObject dst: this.nodes)
		{
			SequenceFlow sq = new SequenceFlow(src,dst);
			for (SequenceFlow s : other.edges)
			{
			    if (s.equalsIgnoreArcCondition(sq))
				    this.edges.add(sq);
			}
			
		}
	
	
    }
    
    public void constructEdges(String modelID, Statement st)
    {
	if (nodes.size() == 0) 
	    return;
	SequenceFlow sq;
	String frmGat, frmEve, frmAct, toGat, toEve, toAct;
	String basicSelect = "Select coalesce(\"FRM_GAT_ID\",0) as frm_gat_id,coalesce(\"FRM_EVE_ID\",0)as frm_eve_id," +
	"coalesce(\"FRM_ACT_ID\",0) as frm_act_id, coalesce(\"TO_ACT_ID\",0) as to_act_id," +
	"coalesce(\"TO_EVE_ID\",0) as to_eve_id,coalesce(\"TO_GAT_ID\",0) as to_gat_id,\"CONDITION\" as condition" +
	" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\"=" + modelID;
	
	try
	{
	    ResultSet rs = st.executeQuery(basicSelect);
	    while(rs.next())
	    {
		sq = new SequenceFlow();
		frmGat = rs.getString("frm_gat_id");
		frmEve = rs.getString("frm_eve_id");
		frmAct = rs.getString("frm_act_id");

		toAct = rs.getString("to_act_id");
		toEve = rs.getString("to_eve_id");
		toGat = rs.getString("to_gat_id");


		if (!frmGat.equals("0"))
		{
		    sq.frmGateWay = new GateWay();
		    sq.frmGateWay.gateID = frmGat;
		    sq.frmGateWay.type = Utilities.getGatewayType(Integer.parseInt(frmGat));
		    sq.frmGateWay.name = Utilities.getGatewayName(Integer.parseInt(frmGat));

		}
		else if (!frmAct.equals("0"))
		{
		    sq.frmActivity = new Activity();
		    sq.frmActivity.actID = frmAct;
		    sq.frmActivity.name = getActivityName(sq.frmActivity.actID);
		}
		else if (!frmEve.equals("0"))
		{
		    sq.frmEvent = new Event();
		    sq.frmEvent.eventID = frmEve;
		    sq.frmEvent.eventName = Utilities.getEventName(Integer.parseInt(frmEve));
		    sq.frmEvent.eventPosition = Utilities.getEventPosition(Integer.parseInt(frmEve));
		}

		if (!toGat.equals("0"))
		{
		    sq.toGateWay = new GateWay();
		    sq.toGateWay.gateID = toGat;
		    sq.toGateWay.type = Utilities.getGatewayType(Integer.parseInt(toGat));
		    sq.toGateWay.name = Utilities.getGatewayName(Integer.parseInt(toGat));
		}
		else if (!toAct.equals("0"))
		{
		    sq.toActivity = new Activity();
		    sq.toActivity.actID = toAct;
		    sq.toActivity.name = getActivityName(sq.toActivity.actID);
		}
		else if (!toEve.equals("0"))
		{
		    sq.toEvent = new Event();
		    sq.toEvent.eventID = toEve;
		    sq.toEvent.eventName = Utilities.getEventName(Integer.parseInt(toEve));
		    sq.toEvent.eventPosition = Utilities.getEventPosition(Integer.parseInt(toEve));
		}
		sq.arcCondition = rs.getString("condition");
		boolean sourceF = false, targetF = false;
		GraphObject currentNode;
		for (int i = 0 ; i < nodes.size() && (!targetF || !sourceF); i++)
		{
		    currentNode = nodes.get(i);
		    if (currentNode.type == ACTIVITY)
		    {
			if (sq.frmActivity != null && !sourceF)
			{
			    sourceF = (currentNode.getID().equals(sq.frmActivity.actID));
			}
			if (sq.toActivity != null && !targetF)
			{
			    targetF = (currentNode.getID().equals(sq.toActivity.actID));
			}
		    }
		    else if (currentNode.type == EVENT)
		    {
			if (sq.frmEvent != null && !sourceF)
			{
			    sourceF = (currentNode.getID().equals(sq.frmEvent.eventID));
			}
			if (sq.toEvent!= null && !targetF)
			{
			    targetF = (currentNode.getID().equals(sq.toEvent.eventID));
			}
		    }
		    else if (currentNode.type == GATEWAY)
		    {
			if (sq.frmGateWay != null && !sourceF)
			{
			    sourceF = (currentNode.getID().equals(sq.frmGateWay.gateID));
			}
			if (sq.toGateWay!= null && !targetF)
			{
			    targetF = (currentNode.getID().equals(sq.toGateWay.gateID));
			}
		    }
		}

		if (targetF && sourceF)
		    edges.add(sq);
		//else
		    //System.out.println("Edge from ")
	    }
	}
	catch(SQLException ex)
	{
	    log.error("Database error. Could not read out graph edges. Results will be incorrect.", ex);
	}
    }
    public void constructOryxEdges(String modelID, Statement st)
    {
	if (nodes.size() == 0) 
	    return;
	SequenceFlow sq;
	String frmGat, frmEve, frmAct, toGat, toEve, toAct;
	String basicSelect = "Select case when \"FRM_GAT_ID\" isnull then '0' else \"FRM_GAT_ID\" end as frm_gat_id, case when \"FRM_EVE_ID\" isnull then '0' else \"FRM_EVE_ID\" end as frm_eve_id," +
	"case when \"FRM_ACT_ID\" isnull then '0' else \"FRM_ACT_ID\" end as frm_act_id, case when \"TO_ACT_ID\" isnull then '0' else \"TO_ACT_ID\" end as to_act_id," +
	"case when \"TO_EVE_ID\" isnull then '0' else \"TO_EVE_ID\" end as to_eve_id, case when \"TO_GAT_ID\" isnull then '0' else \"TO_GAT_ID\" end as to_gat_id,\"CONDITION\" as condition" +
	" from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW\" where \"MODEL_ID\"='" + modelID+"'";
	
	try
	{
	    ResultSet rs = st.executeQuery(basicSelect);
	    while(rs.next())
	    {
		sq = new SequenceFlow();
		frmGat = rs.getString("frm_gat_id");
		frmEve = rs.getString("frm_eve_id");
		frmAct = rs.getString("frm_act_id");

		toAct = rs.getString("to_act_id");
		toEve = rs.getString("to_eve_id");
		toGat = rs.getString("to_gat_id");


		if (!frmGat.equals("0"))
		{
		    sq.frmGateWay = new GateWay();
		    sq.frmGateWay.gateID = frmGat;
		    sq.frmGateWay.type = Utilities.getOryxGatewayType(frmGat);
		    sq.frmGateWay.name = Utilities.getOryxGatewayName(frmGat);

		}
		else if (!frmAct.equals("0"))
		{
		    sq.frmActivity = new Activity();
		    sq.frmActivity.actID = frmAct;
		    sq.frmActivity.name = getActivityName(sq.frmActivity.actID);
		}
		else if (!frmEve.equals("0"))
		{
		    sq.frmEvent = new Event();
		    sq.frmEvent.eventID = frmEve;
		    sq.frmEvent.eventName = Utilities.getOryxEventName(frmEve);
		    sq.frmEvent.eventPosition = Utilities.getOryxEventPosition(frmEve);
		}

		if (!toGat.equals("0"))
		{
		    sq.toGateWay = new GateWay();
		    sq.toGateWay.gateID = toGat;
		    sq.toGateWay.type = Utilities.getOryxGatewayType(toGat);
		    sq.toGateWay.name = Utilities.getOryxGatewayName(toGat);
		}
		else if (!toAct.equals("0"))
		{
		    sq.toActivity = new Activity();
		    sq.toActivity.actID = toAct;
		    sq.toActivity.name = getActivityName(sq.toActivity.actID);
		}
		else if (!toEve.equals("0"))
		{
		    sq.toEvent = new Event();
		    sq.toEvent.eventID = toEve;
		    sq.toEvent.eventName = Utilities.getOryxEventName(toEve);
		    sq.toEvent.eventPosition = Utilities.getOryxEventPosition(toEve);
		}
		sq.arcCondition = rs.getString("condition");
		boolean sourceF = false, targetF = false;
		GraphObject currentNode;
		for (int i = 0 ; i < nodes.size() && (!targetF || !sourceF); i++)
		{
		    currentNode = nodes.get(i);
		    if (currentNode.type == ACTIVITY)
		    {
			if (sq.frmActivity != null && !sourceF)
			{
			    sourceF = (currentNode.getID().equals(sq.frmActivity.actID));
			}
			if (sq.toActivity != null && !targetF)
			{
			    targetF = (currentNode.getID().equals(sq.toActivity.actID));
			}
		    }
		    else if (currentNode.type == EVENT)
		    {
			if (sq.frmEvent != null && !sourceF)
			{
			    sourceF = (currentNode.getID().equals(sq.frmEvent.eventID));
			}
			if (sq.toEvent!= null && !targetF)
			{
			    targetF = (currentNode.getID().equals(sq.toEvent.eventID));
			}
		    }
		    else if (currentNode.type == GATEWAY)
		    {
			if (sq.frmGateWay != null && !sourceF)
			{
			    sourceF = (currentNode.getID().equals(sq.frmGateWay.gateID));
			}
			if (sq.toGateWay!= null && !targetF)
			{
			    targetF = (currentNode.getID().equals(sq.toGateWay.gateID));
			}
		    }
		}

		if (targetF && sourceF)
		    edges.add(sq);
		//else
		    //System.out.println("Edge from ")
	    }
	}
	catch(SQLException ex)
	{
	    log.error("Database error. Could not read out graph edges. Results will be incorrect.", ex);
	}
    }
//    private String getNodeID(String name)
//    {
//	for (int i = 0; i < nodes.size(); i++)
//	    if (nodes.get(i).getName().equals(name))
//		return nodes.get(i).getID();
//	return "";
//    }
    

    public void loadModelFromRepository(String modelID)
    {
	clear();
	this.modelURI = modelID;
	// first load the nodes
	GraphObject tmp;
	try
	{
	    ResultSet rs = Utilities.getDbStatemement().executeQuery("select \"ID\", \"NAME\" from \"BPMN_GRAPH\".\"ACTIVITY\" where \"MOD_ID\" ="+ modelID);
	    while (rs.next())
	    {
		tmp = new GraphObject();
		tmp.setID(rs.getString("id"));
		tmp.setName(rs.getString("name"));
		tmp.type = ACTIVITY;
		tmp.type2 ="";
		nodes.add(tmp);
	    }
	    // events
	    rs = Utilities.getDbStatemement().executeQuery("SELECT \"ID\", \"NAME\", \"EVE_TYPE\" || CAST(\"EVE_POSITION\" AS varchar(10)) AS evep FROM \"BPMN_GRAPH\".\"EVENT\" where \"MODEL_ID\" ="+ modelID);
	    while (rs.next())
	    {
		tmp = new GraphObject();
		tmp.setID(rs.getString("id"));
		tmp.setName(rs.getString("name"));
		tmp.type = EVENT;
		tmp.type2 = rs.getString("evep");
		nodes.add(tmp);
	    }
	    //	gateways
	    rs = Utilities.getDbStatemement().executeQuery("SELECT \"ID\", \"NAME\", \"GATE_WAY_TYPE\" FROM \"BPMN_GRAPH\".\"GATEWAY\" where \"MODEL_ID\" ="+ modelID);
	    while (rs.next())
	    {
		tmp = new GraphObject();
		tmp.setID(rs.getString("id"));
		tmp.setName(rs.getString("name"));
		tmp.type = GATEWAY;
		tmp.type2 =rs.getString("gate_way_type");
		nodes.add(tmp);
	    }
	    constructEdges(modelID, Utilities.getDbStatemement());
	}
	catch (SQLException sqle)
	{
	    log.error("Database error while loading model. Cannot continue.", sqle);
	}
	
	// a new part to load data objects and their associations with activities if any
	DataObject tmp2;
	GraphObject tmp3;
	try
	{
//	    rs = Utilities.getDbStatemement().executeQuery("select \"ID\", \"NAME\" from \"BPMN_GRAPH\".\"DATA_OBJECT\" where \"MODEL_ID\" ="+ modelID);
//	    while (rs.next())
//	    {
//		tmp2 = new DataObject();
//		tmp2.doID =rs.getInt("id");
//		tmp2.modelID=  modelID;
//		tmp2.name = rs.getString("name");
//		
//		dataObjs.add(tmp2);
//	    }
	    // now get the read transitions
	    String queryStr = "select distinct \"BPMN_GRAPH\".\"DATA_OBJECT\".\"NAME\" as ObjName,\"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\"" +
	    		" as ObjID,\"ACTIVITY_ID\",\"BPMN_GRAPH\".\"ACTIVITY\".\"NAME\" as ActName," +
                "\"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_NAME\",\"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\""+
                ".\"FROM_STATE_ID\" from \"BPMN_GRAPH\".\"DATA_OBJECT\",\"BPMN_GRAPH\".\"DATA_OBJECT_STATES\"" +
                ",\"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ACTIVITY\"" +
                " where \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\"."+
                "\"FROM_STATE_ID\" and \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ACTIVITY\".\"ID\"" +
                " and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"MODEL_ID\" = " + modelID +" and \"BPMN_GRAPH\".\"ACTIVITY\".\"MOD_ID\" = "+ modelID;
	    log.trace(queryStr);
	    ResultSet rs = Utilities.getDbStatemement().executeQuery(queryStr);
	    Association ass;
	    while (rs.next())
	    {
	    	
	    	// tmp = from ie the data object
	    	tmp = new GraphObject();
	    	tmp2 = new DataObject();
	    	
	    	tmp.setID(rs.getString("objid"));
	    	tmp2.doID =rs.getString("objid");
	    	
	    	tmp.setName(rs.getString("objname").replace("\n",""));
	    	tmp2.name = rs.getString("objname").replace("\n", "");
	    	
	    	tmp.type = DATAOBJECT;
	    	tmp.type2 = rs.getString("state_name");
	    	tmp2.setState(rs.getString("state_name"));
	    	if (!dataObjs.contains(tmp2))
	    		dataObjs.add(tmp2);
	    	
	    	// tmp3 = to ie the activity
	    	tmp3 = new GraphObject();
	    	tmp3.type = ACTIVITY;
	    	tmp3.setID(rs.getString("activity_id"));
	    	tmp3.setName(rs.getString("actname"));
	    	tmp3.type2= "";
	    	ass = new Association(tmp,tmp3);
	    	ass.assType = AssociaitonType.Structural;
	    	associations.add(ass);
	    	
		}
	    
	    //	  now get the update transitions
	    rs = Utilities.getDbStatemement().executeQuery("select distinct \"BPMN_GRAPH\".\"DATA_OBJECT\".\"NAME\" as ObjName,\"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\"" +
	    		" as ObjID,\"ACTIVITY_ID\",\"BPMN_GRAPH\".\"ACTIVITY\".\"NAME\" as ActName," +
                "\"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_NAME\",\"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\""+
                ".\"TO_STATE_ID\" from \"BPMN_GRAPH\".\"DATA_OBJECT\",\"BPMN_GRAPH\".\"DATA_OBJECT_STATES\"" +
                ",\"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ACTIVITY\"" +
                " where \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\"."+
                "\"TO_STATE_ID\" and \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ACTIVITY\".\"ID\"" +
                " and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"MODEL_ID\" = " + modelID +" and \"BPMN_GRAPH\".\"ACTIVITY\".\"MOD_ID\" = "+ modelID );
	 
	    while (rs.next())
	    {
		// tmp = to ie the data object
		tmp = new GraphObject();
		tmp2 = new DataObject();

		tmp.setID(rs.getString("objid"));
		tmp2.doID =rs.getString("objid");

		tmp.setName(rs.getString("objname").replace("\n", ""));
		tmp2.name = rs.getString("objname").replace("\n", "");

		tmp.type = DATAOBJECT;
		tmp.type2 = rs.getString("state_name");
		tmp2.setState(rs.getString("state_name"));
		if (!dataObjs.contains(tmp2))
		    dataObjs.add(tmp2);
		// tmp3 = from ie the activity
		tmp3 = new GraphObject();
		tmp3.type = ACTIVITY;
		tmp3.setID(rs.getString("activity_id"));;
		tmp3.setName(rs.getString("actname"));
		tmp3.type2= "";
		ass = new Association(tmp3,tmp);
		ass.assType = AssociaitonType.Structural;

		associations.add(ass);

	    }
	} catch (SQLException sqle)
	{
	    log.error("Database error while loading model. Cannot continue.", sqle);
	}
    }
    public void loadModelFromOryxRepository(String modelID)
    {
	clear();
	this.modelURI = modelID;
	// first load the nodes
	GraphObject tmp;
	try
	{
	    ResultSet rs = Utilities.getDbStatemement().executeQuery("select \"ID\", \"NAME\" from \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" where \"MODEL_ID\" ='"+ modelID+"'");
	    while (rs.next())
	    {
		tmp = new GraphObject();
		tmp.setID(rs.getString("id"));
		tmp.setName(rs.getString("name"));
		tmp.type = ACTIVITY;
		tmp.type2 ="";
		nodes.add(tmp);
	    }
	    // events
	    rs = Utilities.getDbStatemement().executeQuery("SELECT \"ID\", \"NAME\", \"EVE_TYPE\" || CAST(\"EVE_POSITION\" AS varchar(10)) AS evep FROM \"BPMN_GRAPH\".\"ORYX_EVENT\" where \"MODEL_ID\" ='"+ modelID+"'");
	    while (rs.next())
	    {
		tmp = new GraphObject();
		tmp.setID(rs.getString("id"));
		tmp.setName(rs.getString("name"));
		tmp.type = EVENT;
		tmp.type2 = rs.getString("evep");
		nodes.add(tmp);
	    }
	    //	gateways
	    rs = Utilities.getDbStatemement().executeQuery("SELECT \"ID\", \"NAME\", \"GATE_WAY_TYPE\" FROM \"BPMN_GRAPH\".\"ORYX_GATEWAY\" where \"MODEL_ID\" ='"+ modelID+ "'");
	    while (rs.next())
	    {
		tmp = new GraphObject();
		tmp.setID(rs.getString("id"));
		tmp.setName(rs.getString("name"));
		tmp.type = GATEWAY;
		tmp.type2 =rs.getString("gate_way_type");
		nodes.add(tmp);
	    }
	    constructOryxEdges(modelID, Utilities.getDbStatemement());
	}
	catch (SQLException sqle)
	{
	    log.error("Database error while loading model. Cannot continue.", sqle);
	}
	
	// a new part to load data objects and their associations with activities if any
	DataObject tmp2;
	GraphObject tmp3;
	try
	{
//	    rs = Utilities.getDbStatemement().executeQuery("select \"ID\", \"NAME\" from \"BPMN_GRAPH\".\"DATA_OBJECT\" where \"MODEL_ID\" ="+ modelID);
//	    while (rs.next())
//	    {
//		tmp2 = new DataObject();
//		tmp2.doID =rs.getInt("id");
//		tmp2.modelID=  modelID;
//		tmp2.name = rs.getString("name");
//		
//		dataObjs.add(tmp2);
//	    }
	    // now get the read transitions
	    String queryStr = "select distinct \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" as ObjName,\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\"" +
	    		" as ObjID,\"ACTIVITY_ID\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"NAME\" as ActName," +
                "\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\""+
                ".\"FROM_STATE\" from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
                ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\"" +
                " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
                "\"FROM_STATE\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"MODEL_ID\" ='" + modelID +"' and \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"MODEL_ID\" ='"+ modelID+"'";
	    log.trace(queryStr);
	    ResultSet rs = Utilities.getDbStatemement().executeQuery(queryStr);
	    Association ass;
	    while (rs.next())
	    {
	    	
	    	// tmp = from ie the data object
	    	tmp = new GraphObject();
	    	tmp2 = new DataObject();
	    	
	    	tmp.setID(rs.getString("objid"));
	    	tmp2.doID =rs.getString("objid");
	    	
	    	tmp.setName(rs.getString("objname").replace("\n",""));
	    	tmp2.name = rs.getString("objname").replace("\n", "");
	    	
	    	tmp.type = DATAOBJECT;
	    	tmp.type2 = rs.getString("state_name");
	    	tmp2.setState(rs.getString("state_name"));
	    	if (!dataObjs.contains(tmp2))
	    		dataObjs.add(tmp2);
	    	
	    	// tmp3 = to ie the activity
	    	tmp3 = new GraphObject();
	    	tmp3.type = ACTIVITY;
	    	tmp3.setID(rs.getString("activity_id"));
	    	tmp3.setName(rs.getString("actname"));
	    	tmp3.type2= "";
	    	ass = new Association(tmp,tmp3);
	    	ass.assType = AssociaitonType.Structural;
	    	associations.add(ass);
	    	
		}
	    
	    //	  now get the update transitions
	    rs = Utilities.getDbStatemement().executeQuery("select distinct \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" as ObjName,\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\"" +
	    		" as ObjID,\"ACTIVITY_ID\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"NAME\" as ActName," +
                "\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\""+
                ".\"TO_STATE\" from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
                ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\"" +
                " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
                "\"TO_STATE\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"MODEL_ID\" ='" + modelID +"' and \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"MODEL_ID\" ='"+ modelID +"'");
	 
	    while (rs.next())
	    {
		// tmp = to ie the data object
		tmp = new GraphObject();
		tmp2 = new DataObject();

		tmp.setID(rs.getString("objid"));
		tmp2.doID =rs.getString("objid");

		tmp.setName(rs.getString("objname").replace("\n", ""));
		tmp2.name = rs.getString("objname").replace("\n", "");

		tmp.type = DATAOBJECT;
		tmp.type2 = rs.getString("state_name");
		tmp2.setState(rs.getString("state_name"));
		if (!dataObjs.contains(tmp2))
		    dataObjs.add(tmp2);
		// tmp3 = from ie the activity
		tmp3 = new GraphObject();
		tmp3.type = ACTIVITY;
		tmp3.setID(rs.getString("activity_id"));;
		tmp3.setName(rs.getString("actname"));
		tmp3.type2= "";
		ass = new Association(tmp3,tmp);
		ass.assType = AssociaitonType.Structural;

		associations.add(ass);

	    }
	} catch (SQLException sqle)
	{
	    log.error("Database error while loading model. Cannot continue.", sqle);
	}
    }
    
    public void exportXML(PrintWriter writer)
    {
	
	if (!modelURI.toLowerCase().startsWith("http"))
	//
	{
	    try
	    {
	    writer.println("<ProcessGraph modelID=\"" + modelURI 
	    	+ "\" location=\"" + Utilities.getModelFilePath(modelURI) + "\">");
	    }
	    catch(SQLException sqe)
	    {
		System.out.println(sqe.getMessage());
	    }
	} 
	else
	//catch (SQLException e)
	{
	    //log.error("Database error. Cannot look up location of a matching model. Results may be incorrect", e);
	    writer.println("<ProcessGraph modelID=\"" + modelURI 
		    	+ "\" location=\"" + modelURI + "\">");
	}
	for (GraphObject node : this.nodes)
	{
	    String outt = null;
	    switch (node.type) {
	    case ACTIVITY:
		outt = "ACT";
		break;
	    case EVENT:
		outt = "EVE";
		break;
	    case GATEWAY:
		outt = "GAT";
		break;
	    default:
		break;
	    }
	    writer.println("<"+ outt + " id=\"" + node.getID() + "\"/>");
	}
	for(DataObject dat :dataObjs)
	{
	    writer.println("<DAT id=\"" + dat.doID + "\"/>");
	}
	for (SequenceFlow edge : this.edges)
	{
	    StringBuilder outt = new StringBuilder();
	    if (edge.frmActivity != null)
		outt.append("<SequenceFlow from=\"ACT" + edge.frmActivity.actID + "\"");
	    if (edge.frmEvent != null)
		outt.append("<SequenceFlow from=\"EVE" + edge.frmEvent.eventID + "\"");
	    if (edge.frmGateWay != null)
		outt.append("<SequenceFlow from=\"GAT" + edge.frmGateWay.gateID + "\"");
	    
	    if (edge.toActivity != null)
		outt.append(" to=\"ACT" + edge.toActivity.actID + "\"/>");
	    if (edge.toEvent != null)
		outt.append(" to=\"EVE" + edge.toEvent.eventID + "\"/>");
	    if (edge.toGateWay != null)
		outt.append(" to=\"GAT" + edge.toGateWay.gateID + "\"/>");

	    writer.println(outt);
	}
	
	for(Association ass :associations)
	{
	    StringBuilder outt = new StringBuilder();
	    if (ass.frmActivity != null)
		outt.append("<Association from=\"ACT" + ass.frmActivity.actID + "\"");
	    else if (ass.frmDataObject != null)
		outt.append("<Association from=\"DAT" + ass.frmDataObject.doID + "\"");
	    else if (ass.frmEvent != null)
		outt.append("<Association from=\"EVE" + ass.frmEvent.eventID + "\"");

	    if (ass.toActivity != null)
		outt.append(" to=\"ACT" + ass.toActivity.actID + "\"/>");
	    else if (ass.toDataObject != null)
		outt.append(" to=\"DAT" + ass.toDataObject.doID + "\"/>");
	    else if (ass.toEvent != null)
		outt.append(" to=\"EVE" + ass.toEvent.eventID + "\"/>");

	    writer.println(outt);

	}
	writer.println("</ProcessGraph>");
    }
    public void exportXMLDetailed(PrintWriter writer)
    {
	removeDuplicates();
	
	if (!modelURI.equals("####") && !modelURI.toLowerCase().startsWith("http"))
	//
	{
	    try
	    {
	    writer.println("<ProcessGraph modelID=\"" + modelURI 
	    	+ "\" location=\"" + Utilities.getModelFilePath(modelURI) + "\">");
	    }
	    catch(SQLException sqe)
	    {
		System.out.println(sqe.getMessage());
	    }
	} 
	else
	//catch (SQLException e)
	{
	    //log.error("Database error. Cannot look up location of a matching model. Results may be incorrect", e);
	    writer.println("<ProcessGraph modelID=\"" + modelURI 
		    	+ "\" location=\"" + modelURI + "\">");
	}
	for (GraphObject node : this.nodes)
	{
	    String out = "<";
	    out = out+node.type.toString().substring(0, 1)+node.type.toString().substring(1).toLowerCase()+" ";
	    out = out+"id=\""+node.getID()+"\" ";
	    out = out+"label=\""+node.getName()+"\" ";
	    out = out+"type2=\""+node.type2+"\" />";
//	    out = out+">";
//	    out = out+"\n</"+node.type.toString()+">";
	    writer.println(out);
	}
	for(DataObject dat :dataObjs)
	{
	    writer.println("<DataObject id=\"" + dat.doID + "label=\""+ dat.name + "label=\"\""+"/>");
	}
	for (SequenceFlow edge : this.edges)
	{
	    StringBuilder outt = new StringBuilder();
	    if (edge.frmActivity != null)
		outt.append("<SequenceFlow from=\"ACT" + edge.frmActivity.actID + "\"");
	    if (edge.frmEvent != null)
		outt.append("<SequenceFlow from=\"EVE" + edge.frmEvent.eventID + "\"");
	    if (edge.frmGateWay != null)
		outt.append("<SequenceFlow from=\"GAT" + edge.frmGateWay.gateID + "\"");
	    
	    if (edge.toActivity != null)
		outt.append(" to=\"ACT" + edge.toActivity.actID + "\"/>");
	    if (edge.toEvent != null)
		outt.append(" to=\"EVE" + edge.toEvent.eventID + "\"/>");
	    if (edge.toGateWay != null)
		outt.append(" to=\"GAT" + edge.toGateWay.gateID + "\"/>");

	    writer.println(outt);
	}
	
	for(Association ass :associations)
	{
	    StringBuilder outt = new StringBuilder();
	    if (ass.frmActivity != null)
		outt.append("<Association from=\"ACT" + ass.frmActivity.actID + "\"");
	    else if (ass.frmDataObject != null)
		outt.append("<Association from=\"DAT" + ass.frmDataObject.doID + "\"");
	    else if (ass.frmEvent != null)
		outt.append("<Association from=\"EVE" + ass.frmEvent.eventID + "\"");

	    if (ass.toActivity != null)
		outt.append(" to=\"ACT" + ass.toActivity.actID + "\"/>");
	    else if (ass.toDataObject != null)
		outt.append(" to=\"DAT" + ass.toDataObject.doID + "\"/>");
	    else if (ass.toEvent != null)
		outt.append(" to=\"EVE" + ass.toEvent.eventID + "\"/>");

	    writer.println(outt);

	}
	writer.println("</ProcessGraph>");
    }
    public void exportXML(PrintWriter writer,String diagnosis)
    {
	
	if (!modelURI.toLowerCase().startsWith("http"))
	//
	{
	    try
	    {
	    writer.println("<ProcessGraph modelID=\"" + modelURI 
	    	+ "\" location=\"" + Utilities.getModelFilePath(modelURI) + "\">");
	    }
	    catch(SQLException sqe)
	    {
		System.out.println(sqe.getMessage());
	    }
	} 
	else
	//catch (SQLException e)
	{
	    //log.error("Database error. Cannot look up location of a matching model. Results may be incorrect", e);
	    writer.println("<ProcessGraph modelID=\"" + modelURI 
		    	+ "\" location=\"" + modelURI + "\">");
	}
	for (GraphObject node : this.nodes)
	{
	    String outt = null;
	    String name="";
	    switch (node.type) {
	    case ACTIVITY:
		outt = "ACT";
		name = node.getName();
		break;
	    case EVENT:
		outt = "EVE";
		if (node.type2.endsWith("1"))
		    name = "StartEvent";
		else if (node.type2.endsWith("2"))
		    name = "IntermediateEvent";
		else
		    name = "EndEvent";
		break;
	    case GATEWAY:
		outt = "GAT";
		name = node.type2;
		break;
	    default:
		break;
	    }
	    writer.println("<"+ outt + " id=\"" + node.getID() + "\" name=\"" + name+ "\"/>");
	}
	for(DataObject dat :dataObjs)
	{
	    writer.println("<DAT id=\"" + dat.doID + "\" name=\"" + dat.name+ "\"/>");
	}
	for (SequenceFlow edge : this.edges)
	{
	    StringBuilder outt = new StringBuilder();
	    if (edge.frmActivity != null)
		outt.append("<SequenceFlow from=\"ACT" + edge.frmActivity.actID + "\"");
	    if (edge.frmEvent != null)
		outt.append("<SequenceFlow from=\"EVE" + edge.frmEvent.eventID + "\"");
	    if (edge.frmGateWay != null)
		outt.append("<SequenceFlow from=\"GAT" + edge.frmGateWay.gateID + "\"");
	    
	    if (edge.toActivity != null)
		outt.append(" to=\"ACT" + edge.toActivity.actID + "\"/>");
	    if (edge.toEvent != null)
		outt.append(" to=\"EVE" + edge.toEvent.eventID + "\"/>");
	    if (edge.toGateWay != null)
		outt.append(" to=\"GAT" + edge.toGateWay.gateID + "\"/>");

	    writer.println(outt);
	}
	
	for(Association ass :associations)
	{
	    StringBuilder outt = new StringBuilder();
	    if (ass.frmActivity != null)
		outt.append("<Association from=\"ACT" + ass.frmActivity.actID + "\"");
	    else if (ass.frmDataObject != null)
		outt.append("<Association from=\"DAT" + ass.frmDataObject.doID + "\"");
	    else if (ass.frmEvent != null)
		outt.append("<Association from=\"EVE" + ass.frmEvent.eventID + "\"");

	    if (ass.toActivity != null)
		outt.append(" to=\"ACT" + ass.toActivity.actID + "\"/>");
	    else if (ass.toDataObject != null)
		outt.append(" to=\"DAT" + ass.toDataObject.doID + "\"/>");
	    else if (ass.toEvent != null)
		outt.append(" to=\"EVE" + ass.toEvent.eventID + "\"/>");

	    writer.println(outt);

	}
	
	writer.println(diagnosis);
	writer.println("</ProcessGraph>");
    }
    protected GraphObject getPredecessorObject(SequenceFlow currentEdge, GraphObject node)
    {
	GraphObject temp = new GraphObject();

	if (node.type == ACTIVITY)
	{
	    if (currentEdge.toActivity != null)
	    {
		if (currentEdge.toActivity.actID.equals(node.getID()) && currentEdge.toActivity.name.equals(node.getName()))
		{

		    if (currentEdge.frmActivity != null)
		    {
			temp.setID(currentEdge.frmActivity.actID);
			temp.setName(currentEdge.frmActivity.name);
			temp.type = ACTIVITY;
			if (currentEdge.frmActivity.name.startsWith("$#") || currentEdge.frmActivity.name.startsWith("?"))
			    temp.type2 ="GENERIC SHAPE";
		    }
		    else if (currentEdge.frmEvent != null)
		    {
			temp.setID(currentEdge.frmEvent.eventID);
			temp.setName(currentEdge.frmEvent.eventName);
			temp.type = EVENT;
			temp.type2 = currentEdge.frmEvent.eventType + currentEdge.frmEvent.eventPosition;
		    }
		    else //if (currentEdge.frmGateWay != null)
		    {
			temp.setID(currentEdge.frmGateWay.gateID);
			temp.setName(currentEdge.frmGateWay.name);
			temp.type = GATEWAY;
			temp.type2= currentEdge.frmGateWay.type;
		    }
		    //preds.add(temp);
		}
	    }
	}
	else if (node.type == EVENT)
	{
	    if (currentEdge.toEvent != null)
	    {
		if (currentEdge.toEvent.eventID.equals(node.getID()) && currentEdge.toEvent.eventName.equals(node.getName()))
		{

		    if (currentEdge.frmActivity != null)
		    {
			temp.setID(currentEdge.frmActivity.actID);
			temp.setName(currentEdge.frmActivity.name);
			temp.type = ACTIVITY;
			if (currentEdge.frmActivity.name.startsWith("$#") || currentEdge.frmActivity.name.startsWith("?"))
			    temp.type2 ="GENERIC SHAPE";
		    }
		    else if (currentEdge.frmEvent != null)
		    {
			temp.setID(currentEdge.frmEvent.eventID);
			temp.setName(currentEdge.frmEvent.eventName);
			temp.type = EVENT;
			temp.type2 = currentEdge.frmEvent.eventType + currentEdge.frmEvent.eventPosition;
		    }
		    else //if (currentEdge.frmGateWay != null)
		    {
			temp.setID(currentEdge.frmGateWay.gateID);
			temp.setName(currentEdge.frmGateWay.name);
			temp.type = GATEWAY;
			temp.type2 = currentEdge.frmGateWay.type;
		    }
		    //preds.add(temp);
		}
	    }
	}
	else //if (node.type == GATEWAY)
	{
	    if (currentEdge.toGateWay != null)
	    {
		if (currentEdge.toGateWay.gateID.equals(node.getID()) && currentEdge.toGateWay.name.equals(node.getName()))
		{

		    if (currentEdge.frmActivity != null)
		    {
			temp.setID(currentEdge.frmActivity.actID);
			temp.setName(currentEdge.frmActivity.name);
			temp.type = ACTIVITY;
			if (currentEdge.frmActivity.name.startsWith("$#") || currentEdge.frmActivity.name.startsWith("?"))
			    temp.type2 ="GENERIC SHAPE";
		    }
		    else if (currentEdge.frmEvent != null)
		    {
			temp.setID(currentEdge.frmEvent.eventID);
			temp.setName(currentEdge.frmEvent.eventName);
			temp.type = EVENT;
			temp.type2 = currentEdge.frmEvent.eventType + currentEdge.frmEvent.eventPosition;
		    }
		    else //if (currentEdge.frmGateWay != null)
		    {
			temp.setID(currentEdge.frmGateWay.gateID);
			temp.setName(currentEdge.frmGateWay.name);
			temp.type = GATEWAY;
			temp.type2 = currentEdge.frmGateWay.type;
		    }
		    //preds.add(temp);
		}
	    }
	}
	return temp;
    }

    protected GraphObject getSuccessorObject(SequenceFlow currentEdge, GraphObject node)
    {
	GraphObject temp = new GraphObject();
	if (node.type == ACTIVITY)
	{
	    if (currentEdge.frmActivity != null)
	    {
		if (currentEdge.frmActivity.actID.equals(node.getID()) 
			&& currentEdge.frmActivity.name.equals(node.getName()))
		{
		    if (currentEdge.toActivity != null)
		    {
			temp.setID(currentEdge.toActivity.actID);
			temp.setName(currentEdge.toActivity.name);
			temp.type = ACTIVITY;
			if (currentEdge.toActivity.name.startsWith("$#")
				|| currentEdge.toActivity.name.startsWith("?"))
			    temp.type2 ="GENERIC SHAPE";
		    } else if (currentEdge.toEvent != null)
		    {
			temp.setID(currentEdge.toEvent.eventID);
			temp.setName(currentEdge.toEvent.eventName);
			temp.type = EVENT;
			temp.type2 = currentEdge.toEvent.eventType + currentEdge.toEvent.eventPosition;
		    } else if (currentEdge.toGateWay != null)
		    {
			temp.setID(currentEdge.toGateWay.gateID);
			temp.setName(currentEdge.toGateWay.name);
			temp.type = GATEWAY;
			temp.type2 = currentEdge.toGateWay.type;
		    }
		    //succs.add(temp);
		}
	    }
	} else if (node.type == EVENT)
	{
	    if (currentEdge.frmEvent != null)
	    {
		if (currentEdge.frmEvent.eventID.equals(node.getID()) && currentEdge.frmEvent.eventName.equals(node.getName()))
		{

		    if (currentEdge.toActivity != null)
		    {
			temp.setID(currentEdge.toActivity.actID);
			temp.setName(currentEdge.toActivity.name);
			temp.type = ACTIVITY;
			if (currentEdge.toActivity.name.startsWith("$#") || currentEdge.toActivity.name.startsWith("$#"))
			    temp.type2 ="GENERIC SHAPE";
		    }
		    else if (currentEdge.toEvent != null)
		    {
			temp.setID(currentEdge.toEvent.eventID);
			temp.setName(currentEdge.toEvent.eventName);
			temp.type = EVENT;
			temp.type2 = currentEdge.toEvent.eventType + currentEdge.toEvent.eventPosition;
		    }
		    else if (currentEdge.toGateWay != null)
		    {
			temp.setID(currentEdge.toGateWay.gateID);
			temp.setName(currentEdge.toGateWay.name);
			temp.type = GATEWAY;
			temp.type2 = currentEdge.toGateWay.type;
		    }
		    //succs.add(temp);
		}
	    }
	}
	else //if (node.type == GATEWAY)
	{
	    if (currentEdge.frmGateWay != null)
	    {
		if (currentEdge.frmGateWay.gateID.equals(node.getID()) && currentEdge.frmGateWay.name.equals(node.getName()))
		{

		    if (currentEdge.toActivity != null)
		    {
			temp.setID(currentEdge.toActivity.actID);
			temp.setName(currentEdge.toActivity.name);
			temp.type = ACTIVITY;
			if (currentEdge.toActivity.name.startsWith("$#") || currentEdge.toActivity.name.startsWith("?")) 
			    temp.type2 ="GENERIC SHAPE";
		    }
		    else if (currentEdge.toEvent != null)
		    {
			temp.setID(currentEdge.toEvent.eventID);
			temp.setName(currentEdge.toEvent.eventName);
			temp.type = EVENT;
			temp.type2 = currentEdge.toEvent.eventType + currentEdge.toEvent.eventPosition;
		    }
		    else if (currentEdge.toGateWay != null)
		    {
			temp.setID(currentEdge.toGateWay.gateID);
			temp.setName(currentEdge.toGateWay.name);
			temp.type = GATEWAY;
			temp.type2 = currentEdge.toGateWay.type;
		    }
		    //succs.add(temp);
		}
	    }
	}
	return temp;
    }

    public List<GraphObject> getSuccessorsFromGraph(GraphObject node)
    {
	List<GraphObject> successors = new ArrayList<GraphObject>();
	GraphObject succ;
	for (SequenceFlow currentEdge : edges)
	{
	    succ = getSuccessorObject(currentEdge, node);
	    if (!succ.getID().equals("0"))
		successors.add(succ);
	}

	return successors;
    }
    
    public List<GraphObject> getSuccessorsFromGraph(GraphObject node, GraphObjectType tp)
    {
	List<GraphObject> succs = new ArrayList<GraphObject>();
	for(GraphObject nd : getSuccessorsFromGraph(node))
	{
	    if (nd.type == tp)
		succs.add(nd);
	}
	return succs;
    }
    
    public List<GraphObject> getPredecessorsFromGraph(GraphObject node)
    {
	List<GraphObject> preds = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < edges.size();i++)
	{
	    currentEdge = edges.get(i);
	    temp = getPredecessorObject(currentEdge, node);
	    if (temp.getName().startsWith("@"))
		continue;
	    if (temp.type2.equals("GENERIC SHAPE"))
		continue;
	    if (temp.type2.equals("GENERIC SPLIT"))
		continue;
	    if (temp.type != UNDEFINED)
		preds.add(temp);

	}
	return preds;
    }
    
    public List<GraphObject> getPredecessorFromGraph(GraphObject node, GraphObjectType tp)
    {
	ArrayList<GraphObject> preds = new ArrayList<GraphObject>();
	for(GraphObject nd : getPredecessorsFromGraph(node))
	{
	    if (nd.type == tp)
		preds.add(nd);
	}
	return preds;
    }
    
    private String getActivityName(String actID) {
	for (int i = 0; i < this.nodes.size(); i++) {
	    GraphObject currentNode = nodes.get(i);
	    if (currentNode.type == ACTIVITY)
		if (currentNode.getID().equals(actID))
		    return currentNode.getName();
	}
	return "";
    }

    public List<GraphObject> getStartupNodes() {
	GraphObject currentNode;
	SequenceFlow currentEdge;
	int sz = nodes.size();
	int sz2 = edges.size();
	boolean found;
	List<GraphObject> strtNodes = new ArrayList<GraphObject>(4);
	for(int i = 0; i < sz;i++)
	{	
	    currentNode = nodes.get(i);
	    found = false;
	    if (currentNode.type == ACTIVITY)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = edges.get(j);
		    if (currentEdge.toActivity != null && currentEdge.toActivity.actID.equals(currentNode.getID()))
		    {
			found = true;
			break;
		    }

		}
	    }
	    else if (currentNode.type == EVENT)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = edges.get(j);
		    if (currentEdge.toEvent != null && currentEdge.toEvent.eventID.equals(currentNode.getID()))
		    {
			found = true;
			break;
		    }

		}
	    }
	    else if (currentNode.type == GATEWAY)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = edges.get(j);
		    if (currentEdge.toGateWay != null && currentEdge.toGateWay.gateID.equals(currentNode.getID()))
		    {
			found = true;
			break;
		    }

		}
	    }
	    if (!found)
		strtNodes.add(currentNode);
	}
	return strtNodes;
    }
    
    public List<GraphObject> getEndNodes() {
	GraphObject currentNode;
	SequenceFlow currentEdge;
	int sz = nodes.size();
	int sz2 = edges.size();
	boolean found;
	ArrayList<GraphObject> strtNodes = new ArrayList<GraphObject>(4);
	for(int i = 0; i < sz;i++)
	{	
	    currentNode = nodes.get(i);
	    found = false;
	    if (currentNode.type == ACTIVITY)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = edges.get(j);
		    if (currentEdge.frmActivity != null && currentEdge.frmActivity.actID.equals(currentNode.getID()))
		    {
			found = true;
			break;
		    }

		}
	    }
	    else if (currentNode.type == EVENT)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = edges.get(j);
		    if (currentEdge.frmEvent != null && currentEdge.frmEvent.eventID.equals(currentNode.getID()))
		    {
			found = true;
			break;
		    }

		}
	    }
	    else if (currentNode.type == GATEWAY)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = edges.get(j);
		    if (currentEdge.frmGateWay != null && currentEdge.frmGateWay.gateID.equals(currentNode.getID()))
		    {
			found = true;
			break;
		    }

		}
	    }
	    if (!found)
		strtNodes.add(currentNode);
	}
	return strtNodes;
    }
    
    public List<GraphObject> getActivities()
    {
	List<GraphObject> result = new ArrayList<GraphObject>();
	for (GraphObject nd : nodes)
	{
	    if (nd.type == ACTIVITY)
		result.add(nd);
	}
	return result;

    }

    public List<GraphObject> getGateways(String gateWayType)
    {
	List<GraphObject> result = new ArrayList<GraphObject>();
	for (GraphObject nd : nodes)
	{
	    if (nd.type == GATEWAY 
		    && (nd.type2.contains(gateWayType) || gateWayType.equals("") ))
		result.add(nd);
	}
	return result;

    }
    
    public List<GraphObject> getEvents(int position)
    {
	List<GraphObject> result = new ArrayList<GraphObject>();
	for (GraphObject nd : nodes)
	{
	    if (nd.type == EVENT 
		    && nd.type2.endsWith(String.valueOf(position)))
		result.add(nd);
	}
	return result;

    }
    // added on 2nd of July 2008 to test how we can generate all possible execution paths

    /**
     * Generates a new process graph which is a transitive closure (in a limited sense)
     * of this object.  
     */
    public ProcessGraph calculateTransitiveClosure()
    {
        ProcessGraph rStar = new ProcessGraph();
    
        rStar.nodes.addAll(this.nodes);
        rStar.edges.addAll(this.edges);
        for (GraphObject currentNodeK : this.nodes)
        {
            for (GraphObject currentNodeI : this.nodes)
            {
        	for (GraphObject currentNodeJ : this.nodes)
        	{
        	    if (	// direct connection
        		    rStar.edges.contains(new SequenceFlow(currentNodeI, currentNodeJ))
        		    || // indirect connection via node K
        		    (rStar.edges.contains(new SequenceFlow(currentNodeI, currentNodeK))
        			    &&
        			    rStar.edges.contains(new SequenceFlow(currentNodeK, currentNodeJ)))
        	    )
        		rStar.addEdge(currentNodeI, currentNodeJ);
        	}
            }
        }
    
        return rStar;
    }

    public List<String> getDataObjectStates(String dObjName)
    {
	List<String> result = new ArrayList<String>();
	for (Association ass : associations)
	{
	    if (ass.frmDataObject.name.equals(dObjName))
	    {
		if (!ass.frmDataObject.getState().equals(""))
		    result.add(ass.frmDataObject.getState());
	    }
	}
	return result;
    }

    public List<GraphObject> getReadingActivities(DataObject d, String state)
    {
	List<GraphObject> result = new ArrayList<GraphObject>();
	for (Association ass : associations)
	{
	    if (ass.assType == null)
		ass.assType = AssociaitonType.Structural;
	    if (ass.assType != AssociaitonType.Structural)
		continue;
	    if (ass.frmDataObject != null && ass.frmDataObject.name.equals(d.name) )
//		    && ass.frmDataObject.doID.equals(d.doID))
	    {
		if (state.length() == 0 ||  ass.frmDataObject.getState().equalsIgnoreCase(state))
		{    
		    if (ass.toActivity != null)
			result.add(ass.toActivity.originalNode());
		    else
			result.add(ass.toEvent.originalNode());
		}
	    }
	}

	return result;
    }

    public List<GraphObject> getUpdatingActivities(DataObject d, String state)
    {
	List<GraphObject> result = new ArrayList<GraphObject>();
	for (Association ass : associations)
	{
	    if (ass.assType == null)
		ass.assType = AssociaitonType.Structural;
	    if (ass.assType != AssociaitonType.Structural)
		continue;
	    if (ass.toDataObject != null && ass.toDataObject.name.equals(d.name)) 
//		    && ass.toDataObject.doID.equals(d.doID))
	    {
		if (state.length() == 0 ||  ass.toDataObject.getState().equalsIgnoreCase(state))
		{
		    if (ass.frmActivity != null)
			result.add(ass.frmActivity.originalNode());
		    else
			result.add(ass.frmEvent.originalNode());
		}
	    }
	}

	return result;
    }
    
    public List<DataObject> getReadDataObjects(GraphObject obj, String state)
    {
	List<DataObject> result = new ArrayList<DataObject>();

	for (Association ass : associations)
	{
	    if (obj.type == ACTIVITY)
	    {
		if (ass.toActivity != null && ass.toActivity.name.equals(obj.getName()) 
			&& ass.toActivity.actID.equals(obj.getID()))
		{
		    if (ass.frmDataObject != null )
			if (state.length() == 0 ||  ass.frmDataObject.getState().equalsIgnoreCase(state))
			    result.add(ass.frmDataObject);
			
			
		}
	    }
	    else if (obj.type == EVENT)
	    {
		if (ass.toEvent != null && ass.toEvent.eventName.equals(obj.getName()) 
			&& ass.toEvent.eventID.equals(obj.getID())) // FIXME: Isn't Events.equals() more appropriate?
		{
		    if (ass.frmDataObject != null)
			if (state.length() == 0 ||  ass.frmDataObject.getState().equalsIgnoreCase(state))
			    result.add(ass.frmDataObject);
		}
	    }
	}
	return result;
    }

    public List<DataObject> getUpdatedDataObjects(GraphObject obj, String state)
    {
	List<DataObject> result = new ArrayList<DataObject>();

	for (Association ass : associations)
	{
	    if (obj.type == ACTIVITY)
	    {
		if (ass.frmActivity != null && ass.frmActivity.name.equals(obj.getName()) 
			&& ass.frmActivity.actID.equals(obj.getID()))
		{
		    if (ass.toDataObject != null)
			if (state.length() == 0 ||  ass.toDataObject.getState().equalsIgnoreCase(state))
			    result.add(ass.toDataObject);
		}
	    }
	    else if (obj.type == EVENT)
	    {
		if (ass.frmEvent != null && ass.frmEvent.eventName.equals(obj.getName()) 
			&& ass.frmEvent.eventID.equals(obj.getID()))
		{
		    if (ass.toDataObject != null)
			if (state.length() == 0 ||  ass.toDataObject.getState().equalsIgnoreCase(state))
			    result.add(ass.toDataObject);
		}
	    }
	}
	return result;
    }

    public void remove(DataObject d)
    {
	dataObjs.remove(d);
    }
    
    /**
     * for Oryx
     */ 
    public void loadFromOryx(String processURI)
    {
	clear();
	this.modelURI = processURI;
	BufferedReader in = null;
	try
	{
	    URL processuri = new URL(processURI);
	    in = new BufferedReader(new InputStreamReader(processuri.openStream()));
	    ProcessGraphBuilderRDF parser = new ProcessGraphBuilderRDF();
	    String baseUri = processURI;
	    
	    parser.setRdfInput(in, RdfSyntax.RDF_XML, baseUri);
	    
	    try
	    {
		ProcessGraph tt = parser.buildGraph();
		
		this.nodes.addAll(tt.nodes);
		this.edges.addAll(tt.edges);
		this.dataObjs.addAll(tt.dataObjs);
		this.associations.addAll(tt.associations);
		this.modelURI = processURI;
	    } catch (FileFormatException e)
	    {
		log.error("Could not parse process graph, invalid file format", e);
		System.out.println("Could not parse process graph, invalid file format:"+e.getMessage());
	    }
	    
	} catch (IOException e)
	{
	    if (e.getMessage().contains("403"))
		log.error("Access to model " + processURI +" is forbidden");
	    else
		System.out.println(e.getMessage());
	} finally
	{
	    if (in != null)
	    {
		try
		{
		    in.close();
		} catch (IOException e) { }
	    }
	}
    }

    private void clear()
    {
	this.associations.clear();
	this.dataObjs.clear();
	this.edges.clear();
	this.nodes.clear() ;
	this.modelURI = "";
    }
    private void removeDuplicates()
    {
	// This method should be called upon completion of evaluation of BPMN-Q queries to remove duplicate nodes
	String visitedIDs="";
	List<GraphObject> toRemove = new ArrayList<GraphObject>();
	for (GraphObject nd : nodes)
	{
	    if (visitedIDs.contains(nd.getID()))
	    {
		toRemove.add(nd);
	    }
	    else
	    {
		visitedIDs +=","+nd.getID();
	    }
		
	}
	for (GraphObject rm : toRemove)
	{
	    nodes.remove(rm);
	}
    }
    public GraphObject getActivity(String actname)
    {
	for (GraphObject obj : this.nodes)
	    if (obj.type == GraphObjectType.ACTIVITY 
		    && obj.getName().equals(actname))
		return obj;
	return null;
    }
    public List<GraphObject> getEnclosingANDSplit2(String ndID)
    {
	//	this method must be called on process graphs resulting
	// from evaluating path edges
	// in each evaluation the nodes are inserted in a way that implies the control flow execution order
	int cnt = nodes.size();
	if (cnt == 0)
	    return null;
	
	int pos= -1;
	for (int i = 0 ; i < cnt; i ++)
	{
	    if (nodes.get(i).toString().equals(ndID))
	    {
		pos = i;
		break;
	    }
	}
	if (pos == -1)
	    return null;
	Stack<GraphObject> openSplits= new Stack<GraphObject>();
//	Stack<GraphObject> openChoiceSplits = new Stack<GraphObject>();
	
	for (int i = 0 ; i < pos ; i++)
	{
	    if (nodes.get(i).type2.toUpperCase().contains("JOIN"))
	    {
		if (openSplits.size() > 0)
		    openSplits.pop();
	    }
	    else if (nodes.get(i).type2.toUpperCase().contains("SPLIT"))
	    {
		openSplits.push(nodes.get(i));
	    }
	    
	    
	}
	List<GraphObject> result = new ArrayList<GraphObject>();
	while (openSplits.size() > 0)
	{
	    GraphObject top = openSplits.pop();
	    if (top.type2.equals("AND SPLIT"))
	    {
		result.add(top);
	    }
	    else // once we find a choice split we stop 
		break;
	}
	return result;
    }
    
    public List<GraphObject> getEnclosingXORSplits(String ndID)
    {
	// this method must be called on process graphs resulting
	// from evaluating path edges
	// in each evaluation the nodes are inserted in a way that implies the control flow execution order
	int cnt = nodes.size();
	if (cnt == 0)
	    return null;
	
	int pos= -1;
	for (int i = 0 ; i < cnt; i ++)
	{
	    if (nodes.get(i).toString().equals(ndID))
	    {
		pos = i;
		break;
	    }
	}
	if (pos == -1)
	    return null;
	List<GraphObject> openSplits= new ArrayList<GraphObject>();
//	Stack<GraphObject> openChoiceSplits = new Stack<GraphObject>();
	int jnCnt = 0;
	for (int i = pos-1 ; i >= 0; i--)
	{
	    if (nodes.get(i).type2.toUpperCase().contains("AND JOIN"))
	    {
		jnCnt++;
	    }
	    if (nodes.get(i).type2.toUpperCase().contains("AND SPLIT"))
	    {
		jnCnt--;
		if (jnCnt < 0)
		    break;
	    }
	    if (nodes.get(i).type2.toUpperCase().contains("OR SPLIT"))
	    {
		openSplits.add(nodes.get(i));
	    }
	    
	    
	    
	}
//	log.info("Process ID "+this.modelURI);
//	log.info("#### Open XOR Splits "+openSplits.toString());
	return openSplits;
    }
    public GraphObject getEnclosingANDSplit(GraphObject nd)
    {
	
	
	List<GraphObject> preds2 = getPredecessorsFromGraph(nd);
	List<GraphObject> visitedNodes = new ArrayList<GraphObject>();
	List<GraphObject> preds=new ArrayList<GraphObject>();
	preds.addAll(preds2);
	int visitedSelectionJoins = 0;
	int visitedAndJoins = 0;
	GraphObject result=null;
	
	while (preds.size() > 0)
	{
	    // check if any of the direct predecessors is a gateway
	    GraphObject g = preds.remove(0);
	    
            if (visitedNodes.contains(g))
            {
        	if (visitedAndJoins > 0 ) 
			return result;
//		    else 
//			return null;
            }
            if (g.type == GraphObjectType.GATEWAY)
    	    {
    	    	if (g.type2.endsWith("OR JOIN"))// || g.type2.equals("XOR JOIN"))
    	    	{
    	    	    visitedSelectionJoins++;
    	    	}
    	    	if (g.type2.endsWith("OR SPLIT"))
    	    	{
    	    	    if (visitedSelectionJoins > 0)
    	    		visitedSelectionJoins--;
//    	    	    else
//    	    		return null;
    	    	}
    	    	if (g.type2.equals("AND JOIN"))
    	    	{
    	    	    visitedAndJoins++;
    	    	}
    	    	if (g.type2.equals("AND SPLIT"))
    	    	{
    	    	    result = g;
    	    	    if (visitedAndJoins> 0 )
    	    		visitedAndJoins--;
    	    	    else
    	    		return g;
    	    	}
    	    }
//            else // this is no gateway then get its predecessors
//            {
        	preds2 = getPredecessorsFromGraph(g);
        	preds.addAll(preds2);
        	
//            }
	    visitedNodes.add(g);
	}
	return result;
    }
    
    public GraphObject getNode(String nodeID)
    {
	for (GraphObject g : nodes)
	{
	    if (g.toString().equals(nodeID))
		return g;
	}
	return null;
    }
    public GraphObject getNodeByID(String id)
    {
	for (GraphObject g : nodes)
	{
	    if (g.getID().equals(id))
		return g;
	}
	return null;
    }
    public List<SequenceFlow> getOutgoingFlow(GraphObject nd)
    {
	List<SequenceFlow> result = new ArrayList<SequenceFlow>();
    	for (SequenceFlow seq : edges)
    		if (seq.getSourceGraphObject().equals(nd))
    			result.add(seq);
    	return result;
    }
    public SequenceFlow getFlowEdge(GraphObject src, GraphObject dst)
    {
	for (SequenceFlow sq : this.edges)
	{
	    if (sq.getSourceGraphObject().equals(src) && sq.getDestinationGraphObject().equals(dst))
		return sq;
	}
	return null;
    }
    public boolean saveToDB()
    {
	// saving the model
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
	    // we have to get rid of the model first
	    Utilities.getDbStatemement().execute("select \"BPMN_GRAPH\".\"ORYX_GET_RID_OF_MODEL\"('"+this.modelURI+"')");
	    Utilities.getDbStatemement().execute("INSERT INTO \"BPMN_GRAPH\".\"ORYX_MODEL\"(\"ID\",\"NAME\",\"SUPER_MODEL_ID\",\"LOADED_TVSM\") VALUES('"+this.modelURI+"','','','N')");
	    // Insert nodes
	    for (GraphObject nd : this.nodes)
	    {
		if (nd.type== GraphObjectType.ACTIVITY)
		{
		    Utilities.getDbStatemement().execute("INSERT INTO \"BPMN_GRAPH\".\"ORYX_ACTIVITY\"(\"ID\", \"MODEL_ID\", \"NAME\") VALUES ('"+nd.getID()+"', '"+this.modelURI+"', '"+nd.getName()+"')");
		}
		else if (nd.type == GraphObjectType.GATEWAY)
		{
		    Utilities.getDbStatemement().execute("INSERT INTO \"BPMN_GRAPH\".\"ORYX_GATEWAY\"(\"ID\", \"MODEL_ID\", \"GATE_WAY_TYPE\", \"NAME\") VALUES ('"+nd.getID()+"', '"+this.modelURI+"','"+nd.type2+"', '')");
		}
		else if (nd.type == GraphObjectType.EVENT)
		{
		    Utilities.getDbStatemement().execute("INSERT INTO \"BPMN_GRAPH\".\"ORYX_EVENT\"(\"ID\", \"MODEL_ID\", \"NAME\", \"EVE_TYPE\", \"EVE_POSITION\") VALUES ('"+nd.getID()+"', '"+this.modelURI+"', '', '', "+nd.type2.charAt(nd.type2.length()-1)+")");
		}
	    }
	    for (SequenceFlow s : this.edges)
	    {
		String sqlStatement="INSERT INTO \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW\"(\"FRM_GAT_ID\", \"FRM_EVE_ID\", \"FRM_ACT_ID\", \"TO_GAT_ID\", \"TO_EVE_ID\", \"TO_ACT_ID\", \"MODEL_ID\", \"CONDITION\") VALUES(";
		
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
		sqlStatement += FRM_GAT_ID+","+FRM_EVE_ID+","+FRM_ACT_ID+","+TO_GAT_ID+","+TO_EVE_ID+","+TO_ACT_ID+",'"+this.modelURI+"','"+s.arcCondition+"')";
		
		Utilities.getDbStatemement().execute(sqlStatement);
	    }
	    
	    // now data objects
	    for (DataObject dob : this.dataObjs)
	    {
		Utilities.getDbStatemement().execute("INSERT INTO \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\"(\"ID\", \"NAME\", \"MODEL_ID\", \"DESCRIPTION\") VALUES ('"+dob.doID+"', '"+dob.name+"', '"+this.modelURI+"', '')");
                // data object states
		if (dob.getState() != null && !dob.getState().equals(""))
		{
		    Utilities.getDbStatemement().execute("INSERT INTO \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"(\"DATA_OBJECT_ID\", \"STATE_NAME\", \"IS_INIT_STATE\") VALUES ('"+dob.doID+"', '"+dob.getState()+"','"+ (dob.getState().toLowerCase().startsWith("init")? "Y" : "N")  +"')");
		}
	    }
//	  data objects state transitions
	    for (Association ass : this.associations)
	    {
		String actID="null",dobID="null",operation="R",fromState="null",toState="null";
		if (ass.frmActivity != null)
		{
		    operation = "W";
		    actID  = ass.frmActivity.actID;
		    if (ass.toDataObject != null)
		    {
			dobID = ass.toDataObject.doID;
			if (ass.toDataObject.getState() != null && ass.toDataObject.getState().length() > 0)
			{
			    toState = ass.toDataObject.getState();
			}
		    }
		    
		}
		else if (ass.toActivity != null)
		{
		    operation = "R";
		    actID  = ass.toActivity.actID;
		    if (ass.frmDataObject != null)
		    {
			dobID = ass.frmDataObject.doID;
			if (ass.frmDataObject.getState() != null && ass.frmDataObject.getState().length() > 0)
			{
			    fromState = ass.frmDataObject.getState();
			}
		    }
		}
		else
		    // this is an invalid association
		    continue;
		
		Utilities.getDbStatemement().execute("INSERT INTO \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"(\"DATA_OBJECT_ID\", \"ACTIVITY_ID\", \"FROM_STATE\", \"TO_STATE\", \"OPERATION\") VALUES ('"+dobID+"', '"+ actID+"', '"+fromState+"', '"+toState+"', '"+operation+"')");

	    }
	    
	    
	    
	} catch (SQLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return false;
	}
	
	return true;
	
    }
    public GraphObject getBoundNodeToID(String id)
    {
	for (GraphObject ob : nodes)
	    if (ob.getBoundQueryObjectID().equals(id))
		return ob;
	return null;
    }
    // Added on 20.04.2011 to support efficient query processing
   
}
