package com.bpmnq;

public final class GraphObject implements Cloneable
{
    public enum GraphObjectType {
        UNDEFINED { public String xmlEncodedName() {return "";} },
        ACTIVITY { public String xmlEncodedName() {return "Activity";} },
        GATEWAY { public String xmlEncodedName() {return "GateWay";} },
        EVENT { public String xmlEncodedName() {return "Event";} },
        DATAOBJECT { public String xmlEncodedName() {return "DataObject";} }, // added on 30.06.2008
        ;
        
        public abstract String xmlEncodedName();
    }
    
    /**
     * Acceptable type values for activities. 
     * In the long run, this should be used as a property of
     * Activity, but so far this information is stored as a string in GraphObject.type2.
     * Hence, each type constant has a method that converts it to the currently used string.
     * 
     * But again, these converter methods should be considered temporary, as this type2-solution 
     * provides no type-safety at all :(
     *
     * @author Steffen Ryll
     */
    public enum ActivityType {
        TASK {public String asType2String() {return "";} },
        VARIABLE_ACTIVITY {public String asType2String() {return "";} },
        GENERIC_SHAPE {public String asType2String() {return "GENERIC SHAPE";} },
        ;
        
        public abstract String asType2String();
    }
    
    /**
     * Acceptable type values for gateways. 
     * In the long run, this should be used as a property of
     * GateWay, but so far this information is stored as a string in GraphObject.type2.
     * Hence, each type constant has a method that converts it to the currently used string.
     * 
     * But again, these converter methods should be considered temporary, as this type2-solution 
     * provides no type-safety at all :(
     *
     * @author Steffen Ryll
     */
    public enum GateWayType {
        XOR_SPLIT {public String asType2String() {return "XOR SPLIT";} },
        XOR_JOIN {public String asType2String() {return "XOR JOIN";} },
        OR_SPLIT {public String asType2String() {return "OR SPLIT";} },
        OR_JOIN {public String asType2String() {return "OR JOIN";} },
        AND_SPLIT {public String asType2String() {return "AND SPLIT";} },
        AND_JOIN {public String asType2String() {return "AND JOIN";} },
        GENERIC_SPLIT {public String asType2String() {return "GENERIC SPLIT";} },
        GENERIC_JOIN {public String asType2String() {return "GENERIC JOIN";} },
        ;
        
        public abstract String asType2String();
    }
    
    /**
     * Acceptable type values for events. 
     * In the long run, this should be used as a property of
     * Events, but so far this information is stored as a string in GraphObject.type2.
     * Hence, each type constant has a method that converts it to the currently used string.
     * 
     * But again, these converter methods should be considered temporary, as this type2-solution 
     * provides no type-safety at all :(
     * 
     * Note: In some places, the type of an event is also referred to as position 
     * (like positioned in the start/ end).
     *
     * @author Steffen Ryll
     */
    public enum EventType {
        START {public String asType2String() {return "1";} },
        INTERMEDIATE {public String asType2String() {return "2";} },
        END {public String asType2String() {return "3";} },
        ;

        public abstract String asType2String();
    }
    private String ID; // this is for the new processing in Oryx
    private String boundTo; // added on 25.08.2010 to track the query graphobject to which it was bound from the query graph
    private String name;
    /** this is to distinguish either it is an activity, gateway, event, data object */
    public GraphObjectType type;
    /** this is to distinguish further details within <code>type</code>
     * <p>(currently known) Possible values are:</p>
     * <ul> <li>"1" for start events</li>
     * <li>"2" for intermediate events</li>
     * <li>"3" for end events</li>
     * <li>"XOR SPLIT"</li>
     * <li>"XOR JOIN"</li>
     * <li>"OR JOIN"</li>
     * <li>"OR SPLIT"</li>
     * <li>"AND JOIN"</li>
     * <li>"AND SPLIT"</li>
     * <li>"GENERIC SPLIT"</li>
     * <li>"GENERIC JOIN"</li>
     * <li>"GENERIC SHAPE"</li>
     * <li>arbitrary values if <code>type</code>==DATAOBJECT. Contains the data object state in this case.</li>
     * </ul>
     * */
    public String type2;

    public GraphObject() 
    {
	this.name = "$#GO#$" + Utilities.getNextVal();
	this.type = GraphObjectType.UNDEFINED;
	this.type2 = "";
	this.ID = "0";
	this.boundTo = "";
    }
    
    public GraphObject(String ID, String name, GraphObjectType type, String type2)
    {
	this();
    	this.ID = ID;
    	this.name = name;
    	this.type = type;
    	this.type2 = type2;
    }
    
    public GraphObject clone() throws CloneNotSupportedException
    {
	GraphObject clone = (GraphObject)super.clone();
	clone.ID = this.ID;
	clone.name = this.name;
	clone.type = this.type;
	clone.type2 = this.type2;
	
	return clone;
    }
    
    public String toString() {
	switch(type) {
	case ACTIVITY:
	    return "ACT" + getID();
	case EVENT:
	    return "EVE" + getID();
	case GATEWAY:
	    return "GAT" + getID();
	case DATAOBJECT: // added on 30.06.2008
	    return "DOB" + getID();
	default:
	    return "???" + getID();
	}
    }
  
    /**
     * returns a whitespace-free name if <code>type</code> is ACTIVITY, 
     * otherwise an empty string
     * @return
     */
    public String getTemporalExpressionName() {
	if (type == GraphObjectType.ACTIVITY && type2.equals("")) {
	    String ret = getName().replace(" ", "_");
	    ret = ret.replace("\n", "_");
	    ret = "executed_" + ret;
	    if (name.startsWith("@"))
		return "true";
	    return ret;
	}
	else if (type == GraphObjectType.EVENT && type2.endsWith("1"))
	{
	    return "true";
	}
	return "";
    }

    /**
     * @param id the id to set
     */
    public void setID(String id) {
        this.ID= id;
    }
    
    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Checks whether this node has been resolved to a specific model node already 
     * (<code>true</code>) or if it's still unbound (<code>false</code>)  
     * @return
     */
    public boolean isResolved()
    {
	return !ID.startsWith("-");
//	boolean result;
//	try
//	{
//	    int id = Integer.parseInt(ID);
//	    result = (id > 0);
//	}
//	catch(Exception e)
//	{
//	    // the ID is not convertable to String
//	    result = ID.length() > 0;
//	}
//	return result;
	
    }
    
//    public void setResolved(boolean alreadyResolved)
//    {
//	if ((alreadyResolved && (id < 0)) 
//		|| (!alreadyResolved && (id > 0)))
//	    id = -1 * id;
//    }
//    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the name
     */
    public String getName() {
    	return name;
    }

    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((ID == null) ? 0 : ID.hashCode());
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	result = prime * result + ((type2 == null) ? 0 : type2.hashCode());
	return result;
    }


    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof GraphObject))
	    return false;
	final GraphObject other = (GraphObject) obj;
	if (ID == null)
	{
	    if (other.ID != null)
		return false;
	} else if (!ID.equals(other.ID))
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
	if (type2 == null)
	{
	    if (other.type2 != null)
		return false;
	} 
	else if (!type2.equals(other.type2))
	    return false;
	return true;
    }
    
    public String getBoundQueryObjectID()
    {
	return boundTo;
    }
    public void setBoundQueryObjectID(String id)
    {
	boundTo = id;
    }
    
}
	