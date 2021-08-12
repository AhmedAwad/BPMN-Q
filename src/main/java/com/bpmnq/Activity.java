package com.bpmnq;

import com.bpmnq.GraphObject.GraphObjectType;

public final class Activity implements Cloneable
{
    public String actID;
    public String name;
    public String modelID;
    
    public GraphObject originalNode() {
        GraphObject rslt = new GraphObject();
        rslt.setID(actID);
        rslt.setName(name);
        rslt.type = GraphObjectType.ACTIVITY;
        rslt.type2 = "";
        return rslt;
    }

    public Activity() {
        name = "$#ACTIVITY#$" + Utilities.getNextVal();
        modelID = "";
        actID = "";
    }

    public Object clone() {
        try
	{
	    Activity clone = (Activity)super.clone();
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
	result = prime * result + ((actID == null) ? 0 : actID.hashCode());
	result = prime * result + ((modelID == null) ? 0 : modelID.hashCode());
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof Activity))
	    return false;
	final Activity other = (Activity) obj;
	if (actID == null)
	{
	    if (other.actID != null)
		return false;
	} else if (!actID.equals(other.actID))
	    return false;
	if (modelID == null)
	{
	    if (other.modelID != null)
		return false;
	} else if (!modelID.equals(other.modelID))
	    return false;
	if (name == null)
	{
	    if (other.name != null)
		return false;
	} else if (!Utilities.normalizeString(name).equals(Utilities.normalizeString(other.name)))
	    return false;
	return true;
    }
    public String toString()
    {
	return "Activity("+this.actID+","+(this.name.startsWith("$#")? "" : this.name)+")";
    }

}
