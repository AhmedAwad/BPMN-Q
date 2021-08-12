package com.bpmnq;

import static com.bpmnq.GraphObject.GraphObjectType.*;

/**
 * Representation of a directed association arc in a graph.
 * 
 * An Association can either connect activities and events with a data object in any direction  
 *
 * @author Ahmed Awad
 */
public class Association implements Cloneable
{
    public DataObject frmDataObject;
    public Activity frmActivity;
    public Event frmEvent;

    public DataObject toDataObject;
    public Activity toActivity;
    public Event toEvent; 
    public enum AssociaitonType {Structural, Behavioral};
    public AssociaitonType assType;
    public Association()
    {
	frmDataObject = null;
	toDataObject = null;

	frmActivity = null;
	toActivity = null;

	frmEvent = null;
	toEvent = null;
	assType = AssociaitonType.Structural;
    }
    
    public Association(GraphObject from, GraphObject to)
    {
	switch (from.type) {
	case ACTIVITY:
	    frmActivity = new Activity();
	    frmActivity.actID = from.getID();
	    frmActivity.name = from.getName();
	    break;
	case EVENT:
	    frmEvent = new Event();
	    frmEvent.eventID = from.getID();
	    frmEvent.eventName = from.getName();
	    frmEvent.eventPosition = Integer.parseInt(from.type2.substring(from.type2.length()-1));
	    break;
	case DATAOBJECT:
	    frmDataObject = new DataObject();
	    frmDataObject.doID = from.getID();
	    frmDataObject.setState(from.type2.replace("\"", ""));
	    if (from.getName().contains("["))
	    {
		frmDataObject.name = from.getName().substring(0,from.getName().indexOf("[")-1);
		if (frmDataObject.getState().length() == 0)
		{
		    frmDataObject.setState(from.getName().substring(from.getName().indexOf("[")+1,from.getName().indexOf("]")-1));
		}
	    }
	    else
		frmDataObject.name = from.getName();
	    
	    break;

	default:
	    throw new IllegalArgumentException("Association can only start at an activity, event or data object.");
	}

	switch (to.type) {
	case ACTIVITY:
	    toActivity = new Activity();
	    toActivity.actID = to.getID();
	    toActivity.name = to.getName();
	    break;
	case EVENT:
	    toEvent = new Event();
	    toEvent.eventID = to.getID();
	    toEvent.eventName = to.getName();
	    toEvent.eventPosition = Integer.parseInt(to.type2.substring(to.type2.length()-1));
	    break;
	case DATAOBJECT:
	    toDataObject = new DataObject();
	    toDataObject.doID = to.getID();
	    toDataObject.setState(to.type2.replace("\"", ""));
	    if (to.getName().contains("["))
	    {
		toDataObject.name = to.getName().substring(0,to.getName().indexOf("[")-1);
		if (toDataObject.getState().length() == 0)
		{
		    toDataObject.setState(to.getName().substring(to.getName().indexOf("[")+1,to.getName().indexOf("]")-1));
		}
	    }
	    else
		toDataObject.name = to.getName();
	    break;

	default:
	    throw new IllegalArgumentException("Association can only end at an activity, event or data object.");
	}

    }

    public Object clone()
    {
	try
	{
	    Association clone = (Association)super.clone();
	    
	    if (this.frmDataObject != null)
	    	clone.frmDataObject = (DataObject)this.frmDataObject.clone();
	    if (this.frmActivity != null)
	    	clone.frmActivity = (Activity)this.frmActivity.clone();
	    if (this.frmEvent != null)
	    	clone.frmEvent = (Event)this.frmEvent.clone();
	    if (this.toDataObject != null)
	    	clone.toDataObject = (DataObject)this.toDataObject.clone();
	    if (this.toActivity != null)
	    	clone.toActivity = (Activity)this.toActivity.clone();
	    if (this.toEvent != null)
	    	clone.toEvent = (Event)this.toEvent.clone();
	    clone.assType = this.assType;
	    return clone;
	} catch (CloneNotSupportedException e)
	{
	    return null;
	}
    }

    public boolean equals(Object other)
    {
	if (!(other instanceof Association))
	    return false;
	Association anOther = (Association) other;

	boolean frmEqual = false, toEqual = false;

	if (this.frmActivity != null && anOther.frmActivity != null)
	    frmEqual = this.frmActivity.equals(anOther.frmActivity);
	else if (this.frmEvent != null && anOther.frmEvent != null)
	    frmEqual = this.frmEvent.equals(anOther.frmEvent);
	else if (this.frmDataObject != null && anOther.frmDataObject != null)
	    frmEqual = this.frmDataObject.equals(anOther.frmDataObject);

	if (this.toActivity != null && anOther.toActivity != null)
	    toEqual = this.toActivity.equals(anOther.toActivity);
	else if (this.toEvent != null && anOther.toEvent != null)
	    toEqual = this.toEvent.equals(anOther.toEvent);
	else if (this.toDataObject != null && anOther.toDataObject != null)
	    toEqual = this.toDataObject.equals(anOther.toDataObject);

	return (frmEqual && toEqual && this.assType == anOther.assType);
    }

    public GraphObject getSource()
    {
	GraphObject result = new GraphObject();
	if (this.frmActivity != null)
	{
	    result.type = ACTIVITY;
	    result.type2 = "";
	    result.setID(this.frmActivity.actID);
	    result.setName(this.frmActivity.name);
	}
	else if (this.frmEvent != null)
	{
	    result.type = EVENT;
	    result.type2 = this.frmEvent.eventType+this.frmEvent.eventPosition;
	    result.setID(this.frmEvent.eventID);
	    result.setName(this.frmEvent.eventName);
	}
	else// if (this.frmEvent != null)
	{
	    result.type = DATAOBJECT;
	    result.type2 = this.frmDataObject.getState();
	    result.setID(this.frmDataObject.doID);
	    result.setName(this.frmDataObject.name);
	}
	return result;
    }

    public GraphObject getDestination()
    {
	GraphObject result = new GraphObject();
	if (this.toActivity != null)
	{
	    result.type = ACTIVITY;
	    result.type2 = "";
	    result.setID(this.toActivity.actID);
	    result.setName(this.toActivity.name);
	}
	else if (this.toEvent != null)
	{
	    result.type = EVENT;
	    result.type2 = this.toEvent.eventType+this.toEvent.eventPosition;
	    result.setID(this.toEvent.eventID);
	    result.setName(this.toEvent.eventName);
	}
	else// if (this.frmEvent != null)
	{
	    result.type = DATAOBJECT;
	    result.type2 = this.toDataObject.getState();
	    result.setID(this.toDataObject.doID);
	    result.setName(this.toDataObject.name);
	}
	return result;
    }
}
