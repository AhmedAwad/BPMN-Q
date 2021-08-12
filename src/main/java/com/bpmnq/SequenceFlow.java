package com.bpmnq;

import java.io.PrintStream;

import org.apache.log4j.Logger;



import static com.bpmnq.GraphObject.GraphObjectType.*;

/**
 * Representation of a directed arc in a graph.
 * 
 * A sequence flow can connect activities, gateways and events with any of 
 * these objects.
 *
 * @author Ahmed Awad
 */
public class SequenceFlow implements Cloneable
{
    public Activity frmActivity;
    public Activity toActivity;
    public GateWay frmGateWay;
    public GateWay toGateWay;
    public Event frmEvent;
    public Event toEvent;
    public String arcCondition;
    /** this is the id from the database */
    public int ID; 

    private Logger log = Logger.getLogger(SequenceFlow.class);
    
    public SequenceFlow() {
	frmActivity = null;
	toActivity = null;
	frmGateWay = null;
	toGateWay = null;
	frmEvent = null;
	toEvent = null;
    }

    public String toString()
    {
	String result="";
	if (frmActivity != null)
	    result = "From ACT" + frmActivity.actID;
	else if (frmEvent != null)
	    result = "From EVE" + frmEvent.eventID;
	else
	    result = "From GAT" + frmGateWay.gateID;

	if (toActivity != null)
	    result += " to ACT" + toActivity.actID;
	else if (toEvent != null)
	    result += " to EVE" + toEvent.eventID;
	else
	    result += " to GAT" + toGateWay.gateID;


	return result;
    }
    
    public Object clone() {
	try
	{
	    SequenceFlow clone = (SequenceFlow)super.clone();
	    // id is cloned automatically
	    if (this.frmActivity != null)
	        clone.frmActivity = (Activity)this.frmActivity.clone();
	    if (this.frmEvent != null)
	        clone.frmEvent = (Event)this.frmEvent.clone();
	    if (this.frmGateWay != null)
	        clone.frmGateWay = (GateWay)this.frmGateWay.clone();
	    if (this.toActivity != null)
	        clone.toActivity = (Activity)this.toActivity.clone();
	    if (this.toEvent != null)
	        clone.toEvent = (Event)this.toEvent.clone();
	    if (this.toGateWay != null)
	        clone.toGateWay = (GateWay)this.toGateWay.clone();
	    
	    return clone;
	} catch (CloneNotSupportedException e)
	{
	    return null;
	}

    }

    /**
     * Constructs a sequence flow object from the two GraphObjects that it connects
     * @param from
     * @param to
     */
    public SequenceFlow(GraphObject from, GraphObject to) {
	this();
	try
	{
	switch (from.type) {
	case ACTIVITY:
	    this.frmActivity = new Activity();
	    this.frmActivity.actID = from.getID();
	    this.frmActivity.name = from.getName();
	    // this.frmActivity.modelID = modelID;

	    // CASE 1: ACTIVITY AS SOURCE ACTIVITY AS DESTINATION
	    if (to.type == ACTIVITY) {
		this.toActivity = new Activity();
		this.toActivity.actID = to.getID();
		this.toActivity.name = to.getName();
		// this.toActivity.modelID = modelID;

		// edges.add(sq);

	    }
	    // CASE 2: ACTIVITY AS SOURCE EVENT AS DESTINATION
	    else if (to.type == EVENT) {
		this.toEvent = new Event();
		this.toEvent.eventID = to.getID();
		this.toEvent.eventName = to.getName();
		this.toEvent.eventPosition = Integer.parseInt(to.type2
			.substring(to.type2.length() - 1, to.type2.length()));
		if (to.type2.length() > 1)
		    this.toEvent.eventType = to.type2.substring(0, to.type2
			    .length() - 1);
		// this.toEvent.modelID = modelID;

		// edges.add(sq);

	    }
	    // CASE 3: ACTIVITY AS SOURCE GATEWAY AS DESTINATION
	    else if (to.type == GATEWAY) {
		this.toGateWay = new GateWay();
		this.toGateWay.gateID = to.getID();
		this.toGateWay.name = to.getName();
		this.toGateWay.type = to.type2;
		// this.toGateWay.modelID = modelID;

		// edges.add(sq);

	    }
	    break;
	case EVENT:
	    this.frmEvent = new Event();
	    this.frmEvent.eventID = from.getID();
	    this.frmEvent.eventName = from.getName();
	    this.frmEvent.eventPosition = Integer.parseInt(from.type2
		    .substring(from.type2.length() - 1, from.type2.length()));
	    if (from.type2.length() > 1)
		this.frmEvent.eventType = from.type2.substring(0, from.type2
			.length() - 1);
	    // this.frmEvent.modelID = modelID;

	    // CASE 4: EVENT AS SOURCE ACTIVITY AS DESTINATION
	    if (to.type == ACTIVITY) {
		this.toActivity = new Activity();
		this.toActivity.actID = to.getID();
		this.toActivity.name = to.getName();
		// this.toActivity.modelID = modelID;

		// edges.add(sq);

	    }
	    // CASE 5: EVENT AS SOURCE EVENT AS DESTINATION
	    else if (to.type == EVENT) {
		this.toEvent = new Event();
		this.toEvent.eventID = to.getID();
		this.toEvent.eventName = to.getName();
		this.toEvent.eventPosition = Integer.parseInt(to.type2
			.substring(to.type2.length() - 1, to.type2.length()));
		if (to.type2.length() > 1)
		    this.toEvent.eventType = to.type2.substring(0, to.type2
			    .length() - 1);
		// this.toEvent.modelID = modelID;

		// edges.add(sq);

	    }
	    // CASE 6: EVENT AS SOURCE GATEWAY AS DESTINATION
	    else if (to.type == GATEWAY) {
		this.toGateWay = new GateWay();
		this.toGateWay.gateID = to.getID();
		this.toGateWay.name = to.getName();
		this.toGateWay.type = to.type2;
		// this.toGateWay.modelID = modelID;

		// edges.add(sq);

	    }
	    break;
	case GATEWAY:
	    this.frmGateWay = new GateWay();
	    this.frmGateWay.gateID = from.getID();
	    this.frmGateWay.name = from.getName();
	    this.frmGateWay.type = from.type2;
	    // this.frmGateWay.modelID = modelID;

	    // CASE 7: GATEWAY AS SOURCE ACTIVITY AS DESTINATION
	    if (to.type == ACTIVITY) {
		this.toActivity = new Activity();
		this.toActivity.actID = to.getID();
		this.toActivity.name = to.getName();
		// this.toActivity.modelID = modelID;

		// edges.add(sq);

	    }
	    // CASE 8: EVENT AS SOURCE EVENT AS DESTINATION
	    else if (to.type == EVENT) {
		this.toEvent = new Event();
		this.toEvent.eventID = to.getID();
		this.toEvent.eventName = to.getName();
		this.toEvent.eventPosition = Integer.parseInt(to.type2
			.substring(to.type2.length() - 1, to.type2.length()));
		if (to.type2.length() > 1)
		    this.toEvent.eventType = to.type2.substring(0, to.type2
			    .length() - 1);
		// this.toEvent.modelID = modelID;

		// edges.add(sq);

	    }
	    // CASE 9: EVENT AS SOURCE GATEWAY AS DESTINATION
	    else if (to.type == GATEWAY) {
		this.toGateWay = new GateWay();
		this.toGateWay.gateID = to.getID();
		this.toGateWay.name = to.getName();
		this.toGateWay.type = to.type2;
		// this.toGateWay.modelID = modelID;

		// edges.add(sq);
	    }
	    break;
	default:
	    break;
	}
	}
	catch(Exception e)
	{
	    System.err.println(e.getMessage());
	    log.error("Sequence flow construction failed (from, to):" 
		    + from + ", " + to, e);
	}
    }

    public boolean equals(Object another) {
	if (! (another instanceof SequenceFlow))
	    return false;

	SequenceFlow anOther = (SequenceFlow) another;

	boolean frmEqual = false, toEqual = false,conditionEqual = false;

	if (this.frmActivity != null && anOther.frmActivity != null)
	    frmEqual = this.frmActivity.equals(anOther.frmActivity);
	else if (this.frmEvent != null && anOther.frmEvent != null)
	    frmEqual = this.frmEvent.equals(anOther.frmEvent);
	else if (this.frmGateWay != null && anOther.frmGateWay != null)
	    frmEqual = this.frmGateWay.equals(anOther.frmGateWay);
	
	if (this.arcCondition != null && anOther.arcCondition != null)
	    conditionEqual = this.arcCondition.equalsIgnoreCase(anOther.arcCondition);
	else if (this.arcCondition != null && anOther.arcCondition == null)
	    conditionEqual = false;
	else if (this.arcCondition == null && anOther.arcCondition != null)
	    conditionEqual = false;
	else
	    conditionEqual = true;
	

	if (this.toActivity != null && anOther.toActivity != null)
	    toEqual = this.toActivity.equals(anOther.toActivity);
	else if (this.toEvent != null && anOther.toEvent != null)
	    toEqual = this.toEvent.equals(anOther.toEvent);
	else if (this.toGateWay != null && anOther.toGateWay != null)
	    toEqual = this.toGateWay.equals(anOther.toGateWay);

	return (frmEqual && toEqual && conditionEqual);
    }

    public boolean equalsIgnoreArcCondition(Object another) {
	if (! (another instanceof SequenceFlow))
	    return false;

	SequenceFlow anOther = (SequenceFlow) another;

	boolean frmEqual = false, toEqual = false,conditionEqual = true;

	if (this.frmActivity != null && anOther.frmActivity != null)
	    frmEqual = this.frmActivity.equals(anOther.frmActivity);
	else if (this.frmEvent != null && anOther.frmEvent != null)
	    frmEqual = this.frmEvent.equals(anOther.frmEvent);
	else if (this.frmGateWay != null && anOther.frmGateWay != null)
	    frmEqual = this.frmGateWay.equals(anOther.frmGateWay);
	
	
	

	if (this.toActivity != null && anOther.toActivity != null)
	    toEqual = this.toActivity.equals(anOther.toActivity);
	else if (this.toEvent != null && anOther.toEvent != null)
	    toEqual = this.toEvent.equals(anOther.toEvent);
	else if (this.toGateWay != null && anOther.toGateWay != null)
	    toEqual = this.toGateWay.equals(anOther.toGateWay);

	return (frmEqual && toEqual && conditionEqual);
    }

    /*public void setNegative(boolean newVal)
	{
		this.isNegative = newVal;
	}
	public boolean isItNegative()
	{
		return this.isNegative;
	}*/

    /**
     * Produces the SELECT statement to find the model id corresponding to this 
     * sequence flow arc.
     */
    public String getSelectStatement(String modelID)
    {
	StringBuilder selStatement = new StringBuilder(); 
	selStatement.append("Select \"ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where ");

	// Activity as source
	if (this.frmActivity != null) {
	    selStatement.append(" \"FRM_ACT_ID\" =" + this.frmActivity.actID);

	    if (this.toActivity != null)
		selStatement.append(" and \"TO_ACT_ID\" =" + this.toActivity.actID);
	    else if (this.frmActivity != null && this.toEvent != null)
		selStatement.append(" and \"TO_EVE_ID\" =" + this.toEvent.eventID);
	    else if (this.frmActivity != null && this.toGateWay != null)
		selStatement.append(" and \"TO_GATE_ID\" =" + this.toGateWay.gateID);
	}

	// Event as source
	else if (this.frmEvent != null) {
	    selStatement.append("\"FRM_EVE_ID\" =" + this.frmEvent.eventID);

	    if (this.toActivity != null)
		selStatement.append(" and \"TO_ACT_ID\" =" + this.toActivity.actID);
	    else if (this.frmEvent != null && this.toEvent != null)
		selStatement.append(" and \"TO_EVE_ID\" =" + this.toEvent.eventID);
	    else if (this.frmEvent != null && this.toGateWay != null)
		selStatement.append(" and \"TO_GAT_ID\" =" + this.toGateWay.gateID);
	}

	//	Gateway as source
	else if (this.frmGateWay != null) {
	    selStatement.append(" \"FRM_GAT_ID\" =" + this.frmGateWay.gateID);

	    if (this.toActivity != null)
		selStatement.append(" and \"TO_ACT_ID\" =" + this.toActivity.actID);
	    else if (this.frmGateWay != null && this.toEvent != null)
		selStatement.append(" and \"TO_EVE_ID\" =" + this.toEvent.eventID);
	    else //if (this.frmEvent != null && this.toGateWay != null)
		selStatement.append(" and \"TO_GAT_ID\" =" + this.toGateWay.gateID);
	}

	return selStatement.toString();
    }

    /**
     * Returns the graph object where this sequence flow starts.
     *  
     * @return The graph object at the start of the sequence flow arc,
     *      representing either an activity, an event or a gateway
     */
    public GraphObject getSourceGraphObject() {
	if (this.frmActivity != null)
	    return this.frmActivity.originalNode();
	else if (this.frmEvent != null)
	    return this.frmEvent.originalNode();
	else
	    return this.frmGateWay.originalNode();
    }

    /**
     * Returns the graph object where this sequence flow ends (the arrow points to).
     *  
     * @return The graph object at the end of the sequence flow arc,
     *      representing either an activity, an event or a gateway
     */
    public GraphObject getDestinationGraphObject() {
	if (this.toActivity != null)
	    return this.toActivity.originalNode();
	else if (this.toEvent != null)
	    return this.toEvent.originalNode();
	else
	    return this.toGateWay.originalNode();
    }

    public void print(PrintStream outStream)
    {
	if (frmActivity != null)
	{
	    if (frmActivity.name.startsWith("$#"))
		outStream.print("From Activity: " + frmActivity.name);
	    else
		outStream.print("From Activity: " + frmActivity.actID);

	}
	else if (frmGateWay!= null)
	{
	    //if (!edges.get(i).frmGateWay.gateWayName.startsWith("$#"))
	    //	outStream.print("From Gatway: " + edges.get(i).frmGateWay.gateWayName);
	    //else
	    outStream.print("From Gatway: " + frmGateWay.type+ " " + frmGateWay.gateID);

	} 
	else if (frmEvent!= null)
	{
	    if (frmEvent.eventName.startsWith("$#"))
		outStream.print("From Event: " + frmEvent.eventName);
	    else
		outStream.print("From Event: " + frmEvent.eventID);
	}

	if (toActivity != null)
	{
	    if (toActivity.name.startsWith("$#"))
		outStream.println("...To Activity: " + toActivity.name);
	    else
		outStream.println("...To Activity: " + toActivity.actID);

	}
	else if (toGateWay!= null)
	{
	    //if (!edges.get(i).toGateWay.gateWayName.startsWith("$#"))
	    //	outStream.println("...To Gatway: " + edges.get(i).toGateWay.gateWayName);
	    //else
	    outStream.println("...To Gatway: "+ toGateWay.type + " " + toGateWay.gateID);

	} 
	else if (toEvent!= null)
	{
	    if (toEvent.eventName.startsWith("$#"))
		outStream.println("...To Event: " +toEvent.eventName);
	    else
		outStream.println("...To Event: " + toEvent.eventID);
	}
    }

}
