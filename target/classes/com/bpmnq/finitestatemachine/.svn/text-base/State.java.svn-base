package com.bpmnq.finitestatemachine;

import java.util.ArrayList;
import java.util.List;

public final class State {
	private int stateID; // Assigned
	private ArrayList<String> enabledActions;
	private List<String> markedPlaces;
	public State(int id)
	{
		this.stateID = id;
		this.enabledActions = new ArrayList<String>(5);
		this.markedPlaces = new ArrayList<String>();
		
	}
	public void addAction(String act)
	{
		if (!this.enabledActions.contains(act))
			this.enabledActions.add(act);
	}
	public String toString()
	{
		return "STATE"+this.stateID;
	}
	public void addMarkedPlace(String neu)
	{
		if (!markedPlaces.contains(neu))
			markedPlaces.add(neu);
	}
	public boolean equals(Object other)
	{
		try
		{
			return ((State) other).toString().equals(this.toString());
		}
		catch(Exception e)
		{
			return false;
		}
		
	}
	public List<String> getEnabledActions()
	{
		return enabledActions;
	}
	public List<String> getMarkedPlaces()
	{
		return markedPlaces;
	}

}
