package com.bpmnq;

import com.bpmnq.GraphObject.GraphObjectType;

public final class Event implements Cloneable
{
    private String modelID;
    public String eventName;
    public String eventID;
    // TODO document variable meanings
    public String eventType;
    /** mystical value coming from the database. Probably it indicates start/end/intermediate events */
    public int eventPosition;

    public GraphObject originalNode() {
        GraphObject rslt = new GraphObject();
        rslt.setName(eventName);
        rslt.setID(eventID);
        rslt.type = GraphObjectType.EVENT;
        rslt.type2 = eventType + eventPosition;
        return rslt;
    }
    
    public Event() {
        modelID = "";
        eventID = "";
        eventPosition = 1;
        eventName = "$#EVENT#$" + Utilities.getNextVal();
        eventType = "";
    }

    public Object clone() {
	try
	{
	    Event clone = (Event)super.clone();
	    // as long this class contains only primitive or immutable fields, calling super.clone() is enough
	    return clone;
	} catch (CloneNotSupportedException e)
	{
	    return null;
	}    
    }


    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((eventID == null) ? 0 : eventID.hashCode());
	result = prime * result
		+ ((eventName == null) ? 0 : eventName.hashCode());
	result = prime * result + eventPosition;
	result = prime * result
		+ ((eventType == null) ? 0 : eventType.hashCode());
	result = prime * result + ((modelID == null) ? 0 : modelID.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof Event))
	    return false;
	final Event other = (Event) obj;
	if (eventID == null)
	{
	    if (other.eventID != null)
		return false;
	} else if (!eventID.equals(other.eventID))
	    return false;
	if (eventName == null)
	{
	    if (other.eventName != null)
		return false;
	} else if (!eventName.equals(other.eventName))
	    return false;
	if (eventPosition != other.eventPosition)
	    return false;
	if (eventType == null)
	{
	    if (other.eventType != null)
		return false;
	} else if (!eventType.equals(other.eventType))
	    return false;
	if (modelID == null)
	{
	    if (other.modelID != null)
		return false;
	} else if (!modelID.equals(other.modelID))
	    return false;
	return true;
    }

    /**
     * @return the eventID
     */
    public String getID() {
        return eventID;
    }

    /**
     * @param eventID the eventID to set
     */
    public void setID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * @return the modelID
     */
    public String getModelID() {
        return modelID;
    }

    /**
     * @param modelID the modelID to set
     */
    public void setModelID(String modelID) {
        this.modelID = modelID;
    }

    /**
     * @return the eventName
     */
    public String getName() {
        return eventName;
    }

    /**
     * @param eventName the eventName to set
     */
    public void setName(String eventName) {
        this.eventName = eventName;
    }
    public String toString()
    {
	StringBuffer result = new StringBuffer(50);
	if (eventPosition == 1)
	    result.append("Start event (");
	else if (eventPosition == 2)
	    result.append("Intermediate event (");
	else
	    result.append("End event (");
	
	result.append(eventID+","+(eventName.startsWith("$#")? "": eventName)+")");
	
	return result.toString();
	    
    }
}
