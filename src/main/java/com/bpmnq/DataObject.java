/**
 * Added on 30.06.08 to begin support of queries including data objects
 * 
 * @author Ahmed Awad
 */
package com.bpmnq;

import com.bpmnq.GraphObject.GraphObjectType;

public final class DataObject implements Cloneable
{
    private String state;
    public String doID;
    public String name;
    public String modelID;
    public void normalize()
    {
	try
	{
	    String st = name.substring(name.indexOf('['));
	    name = name.substring(0, name.indexOf('[')-1);
	    name.trim();
	    st = st.replace("[", "");
	    st = st.replace("]", "");
	    state = st;
	}
	catch (Exception e) {
	    // TODO: handle exception
	}
	
    }
    public DataObject()
    {
	doID = "";
	name = "$#DATAOBJECT#$"+Utilities.getNextVal();
	modelID = "";
    }
    public boolean isResolved()
    {
	boolean result;
	try
	{
	    int id = Integer.parseInt(doID);
	    result = (id > 0);
	}
	catch(Exception e)
	{
	    // the ID is not convertable to String
	    result = doID.length() > 0;
	}
	return result;
	
    }
    public DataObject(String newSate)
    {
	this();
	state = newSate;
    }

    public DataObject(String id, String name, String state)
    {
	this();
	this.doID = id;
	this.name = name;
	this.state = state;
    }

    public Object clone()
    {
	try
	{
	    DataObject clone = (DataObject)super.clone();
	    return clone;
	} catch (CloneNotSupportedException e)
	{
	    return null;
	}
    }

    public String getState()
    {
	return state.trim();
    }
    
    public void setState(String newSate)
    {
	state = newSate;
    }

    public GraphObject originalNode() {
	GraphObject rslt = new GraphObject();
	rslt.setID(this.doID);
	rslt.setName(name);
	rslt.type = GraphObjectType.DATAOBJECT;
	//type2 attribute is used to carry the state information of the data object
	rslt.type2 = this.getState();
	return rslt;
    }

    public String toString()
    {
	return "DAT"+doID+"_"+state;
    }

    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((doID == null) ? 0 : doID.hashCode());
	result = prime * result + ((modelID == null) ? 0 : modelID.hashCode());
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + ((state == null) ? 0 : state.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof DataObject))
	    return false;
	final DataObject other = (DataObject) obj;
	if (doID == null)
	{
	    if (other.doID != null)
		return false;
	} else if (!doID.equals(other.doID))
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
	if (state == null)
	{
	    if (other.state != null)
		return false;
	} else if (!state.equals(other.state))
	    return false;
	return true;
    }


}
