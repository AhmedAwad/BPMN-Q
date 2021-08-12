package com.bpmnq.finitestatemachine;

public final class StateTransition {
	private int transitionID;
	private State sourceState;
	private State destinationState;
	private String transitionAction;
	public StateTransition(int id, State src, State dst, String act)
	{
		this.transitionID= id;
		this.sourceState = src;
		this.destinationState = dst;
		this.transitionAction = act;
	}
	public String toString()
	{
		return "TRANSITION"+this.transitionID+" FROM "+sourceState.toString()+" TO "+destinationState.toString()+" BY "+
		transitionAction;
	}
	public State getSourceState()
	{
		return sourceState;
	}
	public State getDestinationState()
	{
		return destinationState;
	}
	public String getAction()
	{
		return transitionAction;
	}
	public boolean equals(Object other)
	{
		try
		{
			return ((StateTransition) other).toString().equals(this.toString());
		}
		catch(Exception e)
		{
			return false;
		}
		
	}

}
