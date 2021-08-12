package com.bpmnq.petrinet;

public final class Place {
	private String name;
	private String id;
	private boolean isInit;
	private boolean isEnd;
//	private boolean isDataPlace;
	
	public Place(String s)
	{
		name = s;
		id="";
		isInit = false;
		isEnd = true;
	}
	public void appendToID(String x)
	{
		id += x;
	}
	public boolean isItInitialPlace()
	{
		return isInit;
	}
	public void setAsInitialPlace(boolean v)
	{
		isInit = v;
	}
	public String getID()
	{
		return id;
	}
	public boolean isInIDs(String v)
	{
		return id.contains(v);
	}
	public String getName()
	{
		return name;
	}
	public boolean equals (Object other)
	{
		try
		{
			 return this.name.equals(((Place) other).name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	public String toString()
	{
		return this.name;
	}
	public void setAsNotEndPlace()
	{
		isEnd = false;
	}
	public boolean isEndPlace()
	{
		return isEnd;
	}
	public void setAsEndPlace()
	{
	    isEnd = true;
	}
	public String getPlaceAsAPNN()
	{
	    return "\\place{"+ getID().toUpperCase() +"}{\\name{"+ getName()+"}\\capacity{1}\\init{"+(isItInitialPlace()? 1 :0)+"}}";
	}
	public void setID(String newID)
	{
	    id = newID;
	}
	public void setName(String newName)
	{
	    name = newName;
	}
}
