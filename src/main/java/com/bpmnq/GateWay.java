package com.bpmnq;

import com.bpmnq.GraphObject.GraphObjectType;

public final class GateWay implements Cloneable
{
    public String gateID;
    // TODO document variable meanings; rewrite type as an enum
    /** Encodes the actual gateway type 
     * Possible values are: "XOR SPLIT", "XOR JOIN", "AND SPLIT", "AND JOIN" */ 
    public String type;
    public String name;
    public String modelID;

    public GraphObject originalNode() {
        GraphObject rslt = new GraphObject();
        rslt.setName(name);
        rslt.setID(gateID);
        rslt.type = GraphObjectType.GATEWAY;
        rslt.type2 = type;
        return rslt;
    }

    public GateWay() {
        type = "";
        name = "$#GATEWAY#$" + Utilities.getNextVal();
        modelID = "";
        gateID = "";
    }

    public Object clone() {
	try
	{
	    GateWay clone = (GateWay)super.clone();
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
	result = prime * result + ((gateID == null) ? 0 : gateID.hashCode());
	result = prime * result + ((modelID == null) ? 0 : modelID.hashCode());
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof GateWay))
	    return false;
	final GateWay other = (GateWay) obj;
	if (gateID == null)
	{
	    if (other.gateID != null)
		return false;
	} else if (!gateID.equals(other.gateID))
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
	} else if (!name.equals(other.name))
	    return false;
	if (type == null)
	{
	    if (other.type != null)
		return false;
	} else if (!type.equals(other.type))
	    return false;
	return true;
    }
    public String toString()
    {
	return "Gateway ("+gateID+","+type+")";
    }

}
