package com.bpmnq;

import java.io.PrintStream;

public class SequenceFlowTransitiveClosure extends SequenceFlow implements Cloneable
{
    private int distance=0;
    public SequenceFlowTransitiveClosure()
    {
	super();
	distance=0;
    }
    public SequenceFlowTransitiveClosure(GraphObject sourceGraphObject,
	    GraphObject destinationGraphObject)
    {
	super(sourceGraphObject,destinationGraphObject);
	distance = 0;
	// TODO Auto-generated constructor stub
    }
    public Object clone() {
	try
	{
	    SequenceFlowTransitiveClosure clone = (SequenceFlowTransitiveClosure)super.clone();
	    clone.distance = this.distance;
	    
	    return clone;
	} catch (Exception e)
	{
	    return null;
	}

    }
    public int getDistance()
    {
	return distance;
    }
    public void setDistance(int d)
    {
	distance = d;
    }
    @Override
    public void print(PrintStream outStream)
    {
	// TODO Auto-generated method stub
	super.print(outStream);
	outStream.println("Distance is "+distance);
    }
    public String toString()
    {
	String result = super.toString();
	result = result + " distance "+distance;
	
	return result;
    }
}
