package com.bpmnq.pathanalyzer;

import java.io.PrintStream;
import java.util.ArrayList;

//import org.apache.log4j.Logger;

import com.bpmnq.GraphObject;
import com.bpmnq.ProcessGraph;
import com.bpmnq.SequenceFlow;

public class WeightedProcessGraph extends ProcessGraph {
    public ArrayList<WeightedSequenceFlow> edges;
    
//    private Logger log = Logger.getLogger(WeightedProcessGraph.class);
    
    public WeightedProcessGraph(ProcessGraph pGraph)
    {
	this.nodes = new ArrayList<GraphObject>(pGraph.nodes.size());
	this.edges = new ArrayList<WeightedSequenceFlow>(pGraph.edges.size());

	for (GraphObject node : pGraph.nodes)
	{
	    add(node);
	}
	for (SequenceFlow edge : pGraph.edges)
	{
	    WeightedSequenceFlow weigtedEdge = new WeightedSequenceFlow(edge);
	    add(weigtedEdge);
	}

    }
    
    public void add(WeightedSequenceFlow edge)
    {
	if(!this.edges.contains(edge))
	    this.edges.add(edge);
    }

    public void printWithWeights(PrintStream outStream)
    {
	for (int i = edges.size()-1; i >=0; i--)
	{
	    WeightedSequenceFlow edge = edges.get(i);
	    if (edge.frmActivity != null)
	    {
		outStream.print("From Activity: " +edge.frmActivity.name +" "+ edge.frmActivity.actID);
	    }
	    else if (edge.frmGateWay!= null)
	    {
		outStream.print("From Gateway: " +edge.frmGateWay.type +" "+ edge.frmGateWay.gateID);
	    } 
	    else if (edge.frmEvent!= null)
	    {
		outStream.print("From Event: " + edge.frmEvent.eventID);
	    }
	    outStream.print(" Min Tokens " + edge.minToken + " Max Tokens " + edge.maxToken +
		    " Optionality " + edge.optional);

	    if (edge.toActivity != null)
	    {
		outStream.println("...To Activity: "+edge.toActivity.name +" " + edge.toActivity.actID);
	    }
	    else if (edge.toGateWay!= null)
	    {
		outStream.println("...To Gatway: " +edge.toGateWay.type +" "+ edge.toGateWay.gateID);
	    } 
	    else if (edge.toEvent!= null)
	    {
		outStream.println("...To Event: " + edge.toEvent.eventID);
	    }
	}
    }

}
