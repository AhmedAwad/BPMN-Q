package com.bpmnq.petrinet;

import java.util.ArrayList;
import java.util.List;

public final class Transition {
	private String name;
	private List<Place> consumes;
	private List<Place> produces;
	private int id;
	public void setID(int x)
	{
		id = x;
	}
	public void setName(String x)
	{
		name = x;
	}
	public int getID()
	{
		return id;
	}
	public Transition (String s)
	{
		name = s;
		consumes = new ArrayList<Place>(5);
		produces = new ArrayList<Place>(5);
	}
	public void addInputPlace(Place x)
	{
		consumes.add(x);
	}
	public void addOutputPlace(Place x)
	{
		produces.add(x);
	}
	public boolean equals(Object other)
	{
		try
		{
			 return this.name.equals(((Transition) other).name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	public String toString()
	{
		StringBuffer reslt = new StringBuffer(50);
		reslt.append("TRANSITION ");
		reslt.append(this.name+"\n");
		reslt.append("CONSUME ");
		int cnt = this.consumes.size();
		for (int i = 0; i < cnt;i++)
			reslt.append(consumes.get(i).toString()+": 1,");
		if (reslt.charAt(reslt.length()-1) == ',')
			reslt.deleteCharAt(reslt.length()-1);
		reslt.append(";\n");
		
		reslt.append("PRODUCE ");
		cnt = this.produces.size();
		for (int i = 0; i < cnt;i++)
			reslt.append(produces.get(i).toString()+": 1,");
		if (reslt.charAt(reslt.length()-1) == ',')
			reslt.deleteCharAt(reslt.length()-1);
		reslt.append(";\n");
		return reslt.toString();
	}
	public String getName()
	{
		return this.name;
	}
	public boolean hasOutputPlace(Place x)
	{
		return this.produces.contains(x);
	}
	public boolean hasInputPlace(Place x)
	{
		return this.consumes.contains(x);
	}
	public Place hasInputPlace(String someName)
	{
		for (Place p : this.consumes)
			if (p.getID().contains(someName))
				return p;
		return null;
	}
	public List<Place> getInputPlaces()
	{
		return this.consumes;
		
	}
	public List<Place> getOutputPlaces()
	{
		return this.produces;
	}
	public String getTransitionAsAPNN()
	{
	    return "\\transition{T_"+getID()+"}{\\name{"+getName()+"}}";
	}
}
