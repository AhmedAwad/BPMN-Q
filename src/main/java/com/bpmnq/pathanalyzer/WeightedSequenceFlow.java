package com.bpmnq.pathanalyzer;

import java.util.ArrayList;
import java.util.List;

import com.bpmnq.GateWay;
import com.bpmnq.SequenceFlow;

public class WeightedSequenceFlow extends SequenceFlow {
//  the next three attributes added for the path analyzer class
    public int minToken;
    public int maxToken;
    public boolean optional;
    public List<GateWay> passedAndSplits;
    public List<GateWay> passedOrSplits;
    public boolean fromANDSplit;
    /** this is to hold the ID of the nearest XOR-Split, or OR-Split ID to the edge */
    public String nearestSelectionGateID; 

    public WeightedSequenceFlow()
    {
	super();
	minToken = -1;
	maxToken = -1;
	optional = false;
	passedAndSplits = new ArrayList<GateWay>();
	passedOrSplits = new ArrayList<GateWay>();
	fromANDSplit = false;
	nearestSelectionGateID = "0";
    }

    public WeightedSequenceFlow(SequenceFlow other)
    {
	this();
	this.frmActivity = other.frmActivity;
	this.frmEvent = other.frmEvent;
	this.frmGateWay = other.frmGateWay;
	this.toActivity = other.toActivity;
	this.toEvent = other.toEvent;
	this.toGateWay = other.toGateWay;

    }
}
