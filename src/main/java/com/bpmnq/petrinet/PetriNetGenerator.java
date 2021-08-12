package com.bpmnq.petrinet;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bpmnq.*;
import com.bpmnq.GraphObject.GraphObjectType;



public final class PetriNetGenerator {
    private ProcessGraph myPGraph;
    private PetriNet myPNet;
    //private
    private List<GraphObject> succs;
    private List<GraphObject> preds;
    private List<GraphObject> startNodes;
//    private List<GraphObject> processedNodes;
//    private List<String> visitedDataObjects;
    private GraphObject currentNode;
    private Place p1, p2 = null;
    private Transition t;
    private int cnt;
    
    private Logger log = Logger.getLogger(PetriNetGenerator.class);
    
    public PetriNetGenerator(ProcessGraph p)
    {
	myPGraph = p;
	myPNet = new PetriNet();
//	visitedDataObjects = new ArrayList<String>();
    }
    private List<Transition> handleDataObjects(GraphObject act)
    {
	List<Association> ass = myPGraph.getIncomingAssociation(act),oass;
	//ass.addAll(myPGraph.getOutgoingAssociation(act));
	List<Transition> result = new ArrayList<Transition>();
	Map<String,String> in,out;
	
	in = new HashMap<String, String>(); // the input conditions for an activity
	out = new HashMap<String, String>(); // the output conditions for an activity
	
	for(Association  a:ass )
	{
	    if (in.containsKey(a.getSource().getName()))
	    {
		String tmp = in.get(a.getSource().getName());
		tmp += " or "+a.getSource().getName()+"_"+a.getSource().type2;
		in.put(a.getSource().getName(), tmp);
	    }
	    else
	    {
		in.put(a.getSource().getName(), a.getSource().getName()+"_"+a.getSource().type2);
	    }
	}
	List<String> inCond= new ArrayList<String>(),inCondDNF;
	
	Iterator it = in.keySet().iterator();
	
	while (it.hasNext())
	{
	    inCond.add(in.get(it.next()));
	}
	inCondDNF = convertFromCNFtoDNF(inCond);
	
	// now handle output
	//oass.clear();
	oass = myPGraph.getOutgoingAssociation(act);
	for(Association  a:oass )
	{
	    if (out.containsKey(a.getDestination().getName()))
	    {
		String tmp = out.get(a.getDestination().getName());
		tmp += " or "+a.getDestination().getName()+"_"+a.getDestination().type2;
		out.put(a.getDestination().getName(), tmp);
	    }
	    else
	    {
		out.put(a.getDestination().getName(), a.getDestination().getName()+"_"+a.getDestination().type2);
	    }
	}
	List<String> outCond= new ArrayList<String>(),outCondDNF;
	
	Iterator ot = out.keySet().iterator();
	
	while (ot.hasNext())
	{
	    outCond.add(out.get(ot.next()));
	}
	outCondDNF = convertFromCNFtoDNF(outCond);
	
	if (inCondDNF.size() > 0 && outCondDNF.size() > 0) // both input and output conditions are set explicitly
	{    
	    result = generateTransitionsFromDataConditions(inCondDNF, outCondDNF,act);
	}
	else if (inCondDNF.size() > 0 && outCondDNF.size() == 0) // this is a read only case
	{
	    outCondDNF.addAll(inCondDNF);
	    result = generateTransitionsFromDataConditions(inCondDNF, outCondDNF,act);
	}
	else if (inCondDNF.size() == 0 && outCondDNF.size() > 0) // this is an unconditional update
	{
	    
	}
	
	return result;
	
    }
    private List<Transition> generateTransitionsFromDataConditions(List<String> inCondDNF,List<String> outCondDNF,GraphObject act)
    {
	List<Transition> result = new ArrayList<Transition>();
	List<Place> inPlaces = new ArrayList<Place>();
	List<Place> outPlaces = new ArrayList<Place>();
	for (String inC : inCondDNF)
	{
	    inPlaces.clear();
	    String[] inCs = inC.split(",");
	    for (int i = 0; i < inCs.length;i++) // prepare data places
	    {
		Place x;
		x = getPlace(inCs[i]);
		if (x == null)
		{
		    x = new Place(inCs[i]);
		    if (inCs[i].toUpperCase().contains("INITIAL"))
			x.setAsInitialPlace(true);
		    myPNet.addPlace(x);
		    x.appendToID(inCs[i]);
		}
		x.appendToID(","+act.toString());
		
		inPlaces.add(x);

	    }
	    // till this point all input places for a single transition are prepared
	    // now we start with parsin output condition and 
	    for (String outC : outCondDNF)
	    {
		outPlaces.clear();
		String[] outCs = outC.split(",");
		for (int i = 0; i < outCs.length;i++) // prepare data places
		{
		    Place x;
		    x = getPlace(outCs[i]);
		    if (x == null)
		    {
			x = new Place(outCs[i]);
			if (outCs[i].toUpperCase().contains("INITIAL"))
			    x.setAsInitialPlace(true);
			myPNet.addPlace(x);
			x.appendToID(outCs[i]);
		    }
		    x.appendToID(","+act.toString());
		    
		    outPlaces.add(x);

		}
		// after this cyle we are ready to generate the transition
		Transition t = new Transition(act.toString());
		for (Place p : inPlaces)
		    t.addInputPlace(p);
		for (Place p : outPlaces)
		    t.addOutputPlace(p);
		result.add(t);
	    }
	}
	return result;
    }
//    private List<Transition> handleInputDataObjects()
//    {
//    	visitedDataObjects.clear();
//	List<Transition> result = new ArrayList<Transition>();
//    	List<DataObject> dobs = myPGraph.dataObjs;
//    	List<Association> ins = myPGraph.getIncomingAssociation(currentNode);
//    	List<Association> tmp= new ArrayList<Association>();
//    	// we need to filter them
//    	for (DataObject d : dobs)
//    	{
//    	    if (visitedDataObjects.contains(d.name))
//		continue;
//    	    tmp.clear();
//    	    for (Association ass: ins)
//    	    {
//    		if (ass.getSource().getName().equals(d.name))
//    		    tmp.add(ass);
//    	    }
//    	    result = handleInputDataObject(tmp, result);
//    	    visitedDataObjects.add(d.name);
//    	}
//    	
//    	return result;
//    }
//    private List<Transition> handleOutputDataObjects(List<Transition> ts)
//    {
//    	visitedDataObjects.clear();
//	List<Transition> result = new ArrayList<Transition>();
//    	for (Transition t : ts)
//    		result.add(t);
//    	List<DataObject> dobs = myPGraph.dataObjs;
//    	List<Association> ous = myPGraph.getOutgoingAssociation(currentNode);
//    	List<Association> tmp= new ArrayList<Association>();
//    	// we need to filter them
//    	for (DataObject d : dobs)
//    	{
//    	    if (visitedDataObjects.contains(d.name))
//    		continue;
//    	    	
//    	    tmp.clear();
//    	    for (Association ass: ous)
//    	    {
//		if (ass.getDestination().getName().equals(d.name))
//			tmp.add(ass);
//    	    }
//    	    if (tmp.size() == 0) // this dataobject is not updated by the current node
//    	    {
//		for (Transition tt : result)
//		{
//		    Place p = tt.hasInputPlace(d.name); 
//		    if ( p != null)
//			tt.addOutputPlace(p);
//		}
//    	    }
//    	    else
//    		result = handleOutputDataObject(tmp, result);
//    	    visitedDataObjects.add(d.name);
//    	}
//    	
//    	return result;
//    }
//    private List<Transition> handleInputDataObject(List<Association> ass,List<Transition> ts)
//    {
//    	List<Transition> result= new ArrayList<Transition>();
//    	
//    	boolean transitionCreated = false;
//    	Place x;
//    	if (ass.size() == 1 && ass.get(0).getSource().type2.trim().equals(""))
//    	{
//    		GraphObject dob = ass.get(0).getSource();
//    		ass.clear();
//    		for(String st : myPGraph.getDataObjectStates(dob.getName()))
//    		{
//    			Association a = new Association(new GraphObject(dob.getID(),dob.getName(),dob.type,st),currentNode);
//    			ass.add(a);
//    		}
//    		
//    	}
//    	if (ass.size() == 0) return ts;
//    	for (Association a :ass)
//    	{
//    		transitionCreated = false;
//    		x = new Place(a.getSource().getName().replace(" ", "_")+"_"+a.getSource().type2);
//    		if (a.getSource().type2.toUpperCase().equals("INITIAL"))
//    		    x.setAsInitialPlace(true);
//    		
//    		x.appendToID(a.getSource().getName().replace(" ", "_")+"_"+a.getSource().type2 + "," +  currentNode.toString());
////    		for (Transition t : ts)
////    		{
////    			Transition tnew = new Transition(currentNode.toString());
////    			for(Place p : t.getInputPlaces())
////    				tnew.addInputPlace(p);
////    			tnew.addInputPlace(x);
////    			result.add(tnew);
////    			transitionCreated = true;
////    		}
//    		if (!transitionCreated)
//    		{
//    			Transition tnew = new Transition(currentNode.toString());
//    			tnew.addInputPlace(x);
//    			result.add(tnew);
//    		}
//    		myPNet.addPlace(x);
//    	}
//    	return result;
//    }
    private String normalizeObjectState(String dobState)
    {
	String normalFormOfState = dobState.substring(0, dobState.indexOf("="));
    	normalFormOfState+="_"+dobState.substring(dobState.indexOf("=")+1);
    	normalFormOfState = normalFormOfState.trim();
    	return normalFormOfState;
    }
    private Transition handleInputDataObjectXOR(String dobState)
    {
	Place x;
    	String[] states = dobState.split(",");
    	List<Place> xs = new ArrayList<Place>();
    	for (int ii = 0 ; ii < states.length;ii++)
    	{
    	    String st = normalizeObjectState(states[ii]);
    	    x = getPlace(st);
    	    if (x == null)
    	    {
    		x = new Place(states[ii].replace("=","_"));
    		myPNet.addPlace(x);
    	    }
    	    if (st.toUpperCase().contains("INITIAL"))
    		x.setAsInitialPlace(true);

    	    x.appendToID(st);

    	    xs.add(x);

    	}
    	Transition tnew = new Transition(currentNode.toString());

    	for (Place y :xs)
    	{
    	    tnew.addInputPlace(y);
    	    tnew.addOutputPlace(y);
    	}
    	return tnew;
    }
//    private List<Transition> handleOutputDataObject(List<Association> ass,List<Transition> ts)
//    {
//    	List<Transition> result= new ArrayList<Transition>();
//    	if (ass.size() == 0) return ts;
//    	boolean transitionCreated = false;
//    	Place x;
//    	for (Association a :ass)
//    	{
//    		transitionCreated = false;
//    		x = new Place(a.getDestination().getName().replace(" ", "_")+"_"+a.getDestination().type2);
//    		x.appendToID(a.getDestination().getName().replace(" ", "_")+"_"+a.getDestination().type2 + "," +  currentNode.toString());
//    		for (Transition t : ts)
//    		{
//    			Transition tnew = new Transition(currentNode.toString());
//    			for(Place p : t.getInputPlaces())
//    				tnew.addInputPlace(p);
//    			for(Place p : t.getOutputPlaces())
//    				tnew.addOutputPlace(p);
//    			tnew.addOutputPlace(x);
//    			result.add(tnew);
//    			transitionCreated = true;
//    		}
//    		if (!transitionCreated)
//    		{
//    			Transition tnew = new Transition(currentNode.toString());
//    			tnew.addOutputPlace(x);
//    			result.add(tnew);
//    		}
//    		myPNet.addPlace(x);
//    	}
//    	
//    	return result;
//    }
    
    private void handleTask()
    {
	List<Transition> ts;
	ts = handleDataObjects(currentNode);
	if (ts.size() == 0)
	    ts.add( new Transition(currentNode.toString()));
	// Control flow places
	if (preds.size()==0)
	{
	    p1 = new Place("Input_place_of_"+currentNode.toString());
	    p1.appendToID(currentNode.toString());
	    p1.setAsInitialPlace(true);
	    p1.setAsNotEndPlace();
	    myPNet.addPlace(p1);
	}
	else if (preds.size()==1)
	{
	    p1 = getPlace(preds.get(0),currentNode);
	    if (p1 == null)
	    {
		p1 = new Place("Out_place_"+preds.get(0).toString()+"_in_place_"+currentNode.toString());

		p1.appendToID(preds.get(0).toString());
		p1.setAsInitialPlace(false);
		p1.setAsNotEndPlace();
		p1.appendToID(","+currentNode.toString());
		myPNet.addPlace(p1);
	    }
	   
	}
	for (Transition t : ts)
	{
	    t.addInputPlace(p1);
	    
	    for (GraphObject nd : succs)
	    {
		p2 = getPlace(currentNode, nd);
		if (p2 == null)
		{
		    p2 = new Place("Out_place_"+currentNode.toString()+"_in_place_"+nd.toString());
		    p2.appendToID(currentNode.toString());
		    p2.appendToID(","+nd.toString());
		    p2.setAsNotEndPlace();
		    myPNet.addPlace(p2);
		}

		t.addOutputPlace(p2);
	    }
	    if (succs.size()==0)
		//if (currentNode.type1.equals("Event") && currentNode.type2.equals("3"))
	    {
		p2= new Place("out_place_"+currentNode.toString());
		p2.appendToID(currentNode.toString());
		myPNet.addPlace(p2);
		t.addOutputPlace(p2);
	    }
	    myPNet.addTransition(t);
	}
	    
	
	// now nexts
	
	
    }
    private void handleXORSplit()
    {
	List<Transition> ts;
	ts = handleDataObjects(currentNode);
	if (preds.size()==0)
	{
	    p1 = new Place("Input_place_of_"+currentNode.toString());
	    p1.appendToID(currentNode.toString());
	    p1.setAsInitialPlace(true);
	    p1.setAsNotEndPlace();
	    myPNet.addPlace(p1);
	}
	else if (preds.size()==1)
	{
	    p1 = getPlace(preds.get(0),currentNode);
	    if (p1 == null)
	    {
		p1 = new Place("Out_place_"+preds.get(0).toString()+"_in_place_"+currentNode.toString());

		p1.appendToID(preds.get(0).toString());
		p1.setAsInitialPlace(false);
		p1.setAsNotEndPlace();
		myPNet.addPlace(p1);
	    }
	    p1.appendToID(","+currentNode.toString());
	}
	
    }
    private void handleOnyToManyTransitions()
    {
		//t = new Transition(currentNode.toString());
	List<Transition> ts;
//	if (currentNode.type == GraphObjectType.ACTIVITY)
//	{ 
	    ts = handleDataObjects(currentNode);
//	}
//	else
//	    ts = new ArrayList<Transition>();
	
	if (ts.size() == 0)
	    ts.add( new Transition(currentNode.toString()));
	    
	if (preds.size()==0)
	{
	    p1 = new Place("Input_place_of_"+currentNode.toString());
	    p1.appendToID(currentNode.toString());
	    p1.setAsInitialPlace(true);
	    p1.setAsNotEndPlace();
	    myPNet.addPlace(p1);
	}
	else if (preds.size()==1)
	{
	    p1 = getPlace(preds.get(0),currentNode);
	    if (p1 == null)
	    {
		p1 = new Place("Out_place_"+preds.get(0).toString()+"_in_place_"+currentNode.toString());

		p1.appendToID(preds.get(0).toString());
		p1.setAsInitialPlace(false);
		p1.setAsNotEndPlace();
		myPNet.addPlace(p1);
	    }
	    p1.appendToID(","+currentNode.toString());
	}
	cnt = succs.size();
	for (Transition t :ts)
	{
	    for (int i =0; i < cnt; i++)
	    {
		p2 = getPlace(currentNode, succs.get(i));
		if (p2 == null)
		{
		    p2 = new Place("Out_place_"+currentNode.toString()+"_in_place_"+succs.get(i).toString());
		    p2.appendToID(currentNode.toString());
		    p2.appendToID(","+succs.get(i).toString());
		    p2.setAsNotEndPlace();
		    myPNet.addPlace(p2);
		}

		t.addOutputPlace(p2);
	    }
	    if (succs.size()==0)
		//if (currentNode.type1.equals("Event") && currentNode.type2.equals("3"))
	    {
		p2= new Place("out_place_"+currentNode.toString());
		p2.appendToID(currentNode.toString());
		myPNet.addPlace(p2);
		t.addOutputPlace(p2);
	    }

	    t.addInputPlace(p1);

	    myPNet.addTransition(t);
	}
    }
    
//    public void generatePTNetFromProcessGraph()
//    {
//	// we will follow the work from Remco etal.
//
//
//	startNodes = myPGraph.getStartupNodes();
//	processedNodes = new ArrayList<GraphObject>(myPGraph.nodes.size());
//
//	while (startNodes.size() > 0)
//	{
//	    currentNode = startNodes.remove(0);
//	    succs = myPGraph.getSuccessorsFromGraph(currentNode);
//	    preds = myPGraph.getPredecessorsFromGraph(currentNode);
//	    // the processed nodes list is used to han8idle loops in the process graph
//	    for (int i = 0 ; i < succs.size(); i++)
//		if (!processedNodes.contains(succs.get(i)) && !startNodes.contains(succs.get(i)))
//		    startNodes.add(succs.get(i));
//	    if (!allPredPlacesExist())
//	    {
//		startNodes.add(currentNode);
//		continue;
//	    }
//	    if (!processedNodes.contains(currentNode))
//		processedNodes.add(currentNode);
//
//	    if (currentNode.type == GraphObjectType.EVENT)
//	    {
//		handleOnyToManyTransitions();
//
//	    }
//	    else if (currentNode.type == GraphObjectType.ACTIVITY)
//	    {
//		handleOnyToManyTransitions();
//	    }
//	    else if (currentNode.type == GraphObjectType.GATEWAY)
//	    {
//		if (currentNode.type2.equals("AND SPLIT"))
//		{
//		    handleOnyToManyTransitions();
//		}
//		else if (currentNode.type2.equals("XOR SPLIT"))
//		{
//		    if (preds.size()==0)
//		    {
//			p1 = new Place("In_place_of_"+currentNode.toString());
//			p1.setAsInitialPlace(true);
//			p1.setAsNotEndPlace();
//			myPNet.addPlace(p1);
//		    }
//		    else if (preds.size() == 1)
//		    {
//			p1 = getPlace(preds.get(0),currentNode);
//		    }
//		    p1.appendToID(currentNode.toString());// we need to comment this line
//		    for (int i =0; i < succs.size();i++)
//		    {
//			t = new Transition(currentNode.toString()+"_"+i);
//			p2 = new Place("Out_place_"+currentNode.toString()+"_in_place_"+succs.get(i).toString());
//			p2.appendToID(currentNode.toString());
//			p2.appendToID(","+succs.get(i).toString());
//			p2.setAsNotEndPlace();
//			t.addInputPlace(p1);
//			t.addOutputPlace(p2);
//			myPNet.addPlace(p2);
//			myPNet.addTransition(t);
//		    }
//		    // special case for single output xor split due to query results
//		    if (succs.size() == 1) // we add a another path
//		    {
//			t = new Transition(currentNode.toString()+"_1");
//			p2 = new Place("Out_place_"+currentNode.toString()+"_in_place_dummy");
//			p2.appendToID(currentNode.toString());
//			p2.appendToID(",dummy");
//			p2.setAsNotEndPlace();
//			t.addInputPlace(p1);
//			t.addOutputPlace(p2);
//			myPNet.addPlace(p2);
//			myPNet.addTransition(t);
//		    }
//
//		}
//		else if (currentNode.type2.equals("AND JOIN"))
//		{
//		    //multiple input places single transition single output place
//		    t = new Transition(currentNode.toString());
//		    p2 = new Place("Out_place_"+currentNode.toString());
//		    p2.appendToID(currentNode.toString());
//		    if (succs.size()==1)
//		    {
//			p2.appendToID(","+succs.get(0).toString());
//			p2.setAsNotEndPlace();
//		    }
//
//		    t.addOutputPlace(p2);
//		    myPNet.addPlace(p2);
//		    for (int i = 0;i < preds.size();i++)
//		    {
//			p1 = getPlace(preds.get(i),currentNode);
//			if (p1 == null)// this is a case in a loop
//			{
//			    p1 = new Place("Out_place_"+preds.get(i).toString()+"_in_place_"+currentNode.toString());
//			    p1.appendToID(preds.get(i).toString());
//			    p1.appendToID(","+currentNode.toString());
//			    myPNet.addPlace(p1);
//			}
//			else
//			    p1.appendToID(","+currentNode.toString());
//
//			//p1.appendToID(","+currentNode.toString());
//			myPNet.addPlace(p1);
//			t.addInputPlace(p1);
//		    }
//		    myPNet.addTransition(t);
//		}
//		else if (currentNode.type2.equals("XOR JOIN"))
//		{
//		    //multiple input places single transition single output place
//
//		    p2 = new Place("Out_place_"+currentNode.toString());
//		    p2.appendToID(currentNode.toString());
//		    if (succs.size()==1)
//		    {
//			p2.appendToID(","+succs.get(0).toString());
//			p2.setAsNotEndPlace();
//		    }
//
//		    myPNet.addPlace(p2);
//		    for (int i = 0;i < preds.size();i++)
//		    {
//			t = new Transition(currentNode.toString()+"_"+i);
//			t.addOutputPlace(p2);
//			p1 = getPlace(preds.get(i),currentNode);
//			if (p1 == null)// this is a case in a loop
//			{
//			    p1 = new Place("Out_place_"+preds.get(i).toString()+"_in_place_"+currentNode.toString());
//			    p1.appendToID(preds.get(i).toString());
//			    p1.appendToID(","+currentNode.toString());
//			    myPNet.addPlace(p1);
//			}
//			else
//			    p1.appendToID(","+currentNode.toString());
//			p1.setAsNotEndPlace();
//			t.addInputPlace(p1);
//			myPNet.addTransition(t);
//		    }
//
//		}
//	    }
//	}
//    }
    public void generatePTNetFromProcessGraph()
    {
	// we will follow the work from Remco etal.
	startNodes = new ArrayList<GraphObject>(myPGraph.nodes.size());
	for (GraphObject nd : myPGraph.nodes)
	{
	    try
	    {
		startNodes.add(nd.clone());
	    } catch (CloneNotSupportedException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	//processedNodes = new ArrayList<GraphObject>(myPGraph.nodes.size());

	while (startNodes.size() > 0)
	{
	    currentNode = startNodes.remove(0);
//	    System.out.println("Evaluating node "+currentNode.toString());
	    succs = myPGraph.getSuccessorsFromGraph(currentNode);
	    preds = myPGraph.getPredecessorsFromGraph(currentNode);
	    // the processed nodes list is used to han8idle loops in the process graph
	    
	    
	    if (currentNode.type == GraphObjectType.EVENT)
	    {
		handleOnyToManyTransitions();

	    }
	    else if (currentNode.type == GraphObjectType.ACTIVITY)
	    {
//		handleOnyToManyTransitions();
		handleTask();
	    }
	    else if (currentNode.type == GraphObjectType.GATEWAY)
	    {
		if (currentNode.type2.equals("AND SPLIT"))
		{
		    handleOnyToManyTransitions();
		}
		else if (currentNode.type2.equals("XOR SPLIT"))
		{
		    if (preds.size()==0)
		    {
			p1 = new Place("In_place_of_"+currentNode.toString());
			p1.setAsInitialPlace(true);
			p1.setAsNotEndPlace();
			myPNet.addPlace(p1);
		    }
		    else if (preds.size() == 1)
		    {
			p1 = getPlace(preds.get(0),currentNode);
			if (p1 == null)
			{
			   p1 = new Place("Out_place_"+preds.get(0).toString()+"_in_place_"+currentNode.toString());
			   p1.appendToID(preds.get(0).toString());
			   p1.setAsNotEndPlace();
			   p1.setAsInitialPlace(false);
			   myPNet.addPlace(p1);
			}
		    }
		    p1.appendToID(", "+ currentNode.toString());// we need to comment this line
		    handleXORDataBased();
//		    handleOnyToManyTransitions();
		}
		else if (currentNode.type2.equals("AND JOIN"))
		{
		    //multiple input places single transition single output place
		    t = new Transition(currentNode.toString());
		   
		    
		    if (succs.size()==1)
		    {
			p2 = getPlace(currentNode, succs.get(0));
			if (p2 == null)
			{
			    p2 = new Place("Out_place_"+currentNode.toString());
			
			    p2.appendToID(currentNode.toString());
			    p2.appendToID(","+succs.get(0).toString());
			    p2.setAsNotEndPlace();
			    myPNet.addPlace(p2);
			}
			else
			    p2.appendToID(", "+currentNode.toString());
		    }

		    t.addOutputPlace(p2);
		    
		    for (int i = 0;i < preds.size();i++)
		    {
			p1 = getPlace(preds.get(i),currentNode);
			if (p1 == null)// this is a case in a loop
			{
			    p1 = new Place("Out_place_"+preds.get(i).toString()+"_in_place_"+currentNode.toString());
			    p1.appendToID(preds.get(i).toString());
			    p1.appendToID(","+currentNode.toString());
			    myPNet.addPlace(p1);
			}
			else
			    p1.appendToID(","+currentNode.toString());

			//p1.appendToID(","+currentNode.toString());
//			myPNet.addPlace(p1);
			t.addInputPlace(p1);
		    }
		    myPNet.addTransition(t);
		}
		else if (currentNode.type2.equals("XOR JOIN"))
		{
		    //multiple input places single transition single output place
		    
			
//		    p2 = new Place("Out_place_"+currentNode.toString());
//		    p2.appendToID(currentNode.toString());
		    if (succs.size()==1)
		    {
			p2 = getPlace(currentNode, succs.get(0));
			if (p2 == null)
			{
			    p2 = new Place("Out_place_"+currentNode.toString());
			
			    p2.appendToID(currentNode.toString());
			    p2.appendToID(","+succs.get(0).toString());
			    p2.setAsNotEndPlace();
			    myPNet.addPlace(p2);
			}
			else
			    p2.appendToID(", "+currentNode.toString());
			
//			p2.appendToID(","+succs.get(0).toString());
//			p2.setAsNotEndPlace();
		    }

		    
		    for (int i = 0;i < preds.size();i++)
		    {
			t = new Transition(currentNode.toString()+"_"+i);
			t.addOutputPlace(p2);
			p1 = getPlace(preds.get(i),currentNode);
			if (p1 == null)// this is a case in a loop
			{
			    p1 = new Place("Out_place_"+preds.get(i).toString()+"_in_place_"+currentNode.toString());
			    p1.appendToID(preds.get(i).toString());
			    p1.appendToID(","+currentNode.toString());
			    myPNet.addPlace(p1);
			}
			else
			    p1.appendToID(","+currentNode.toString());
			p1.setAsNotEndPlace();
			t.addInputPlace(p1);
			myPNet.addTransition(t);
		    }

		}
	    }
	}
	
    }

//    private boolean allPredPlacesExist()
//    {
//	Place p;
//	if (preds.size() == 0)
//	    return true;
//	for (int i = 0; i < preds.size(); i++)
//	{
//	    p = getPlace(preds.get(i), currentNode);
//	    if (p == null)
//		return false;
//	}
//	return true;
//    }

    private Place getPlace(GraphObject pred, GraphObject suc)
    {
	return myPNet.getPlace(pred.toString(), suc.toString());
    }

    private Place getPlace(String dobState)
    {
	return myPNet.getPlace(dobState);
    }
    
    public void printPetriNet(PrintStream outStream)
    {
	myPNet.print(outStream);
    }

    public void writePetriNetToLOLAFile(String folder) throws IOException
    {
	try
	{
	    myPNet.writeLoLANetFile(folder);
	}
	catch(IOException e)
	{
	    throw e;
	}
    }

    public void printProcess(PrintStream outStream)
    {
	myPGraph.print(outStream);
    }

    public PetriNet getPetriNet()
    {
	return myPNet;
    }
    private void handleXORDataBased()
    {
	//List<SequenceFlow> seqs = myPGraph.getOutgoingFlow(currentNode);
	String exp;
	List<String> ls;
	Transition ts=null;
	
	for (int i =0; i < succs.size();i++)
	{
	    SequenceFlow sq = myPGraph.getFlowEdge(currentNode, succs.get(i));
	    if (sq != null)
	    {
		exp = sq.arcCondition;
		if (exp != null && exp.length() > 0)
		{
		    exp = exp.replace("\n", " ");

		    try
		    {
			ls = parseConditionExpression(exp);
			for (String s:ls)
			{
			    if (s.trim().length()==0)
				continue;

//			    System.out.println("Cond: "+s);
			    ts = handleInputDataObjectXOR(s);
			    if (ts==null)
			    {
				ts = new Transition(currentNode.toString()+"_"+i);
			    }
			    p2 = getPlace(currentNode, succs.get(i));
			    if (p2 == null)
			    {
				p2 = new Place("Out_place_"+currentNode.toString()+"_in_place_"+succs.get(i).toString());
				p2.appendToID(currentNode.toString());
				p2.appendToID(","+succs.get(i).toString());
				p2.setAsNotEndPlace();
				myPNet.addPlace(p2);
			    }
			    ts.addInputPlace(p1);
			    ts.addOutputPlace(p2);
			    myPNet.addTransition(ts);
			}
		    } 
		    catch (NotConjunctiveNormalFormException e)
		    {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println(e.getMessage());
		    }
		}
		else // there is no arc condition
		{
		    t = new Transition(currentNode.toString()+"_"+i);
		    p2 = getPlace(succs.get(i),currentNode);
		    if (p2 == null)
		    {
			p2 = new Place("Out_place_"+currentNode.toString()+"_in_place_"+succs.get(i).toString());

			p2.appendToID(succs.get(i).toString());
			p2.setAsInitialPlace(false);
			p2.setAsNotEndPlace();
			myPNet.addPlace(p2);
		    }
		    t.addInputPlace(p1);
		    t.addOutputPlace(p2);
		    p2.appendToID(","+currentNode.toString());
		    myPNet.addTransition(t);
		}
	    }
	    
	    
	}
	    // special case for single output xor split due to query results
	if (succs.size() == 1) // we add a another path
	{
	    t = new Transition(currentNode.toString()+"_1");
	    p2 = new Place("Out_place_"+currentNode.toString()+"_in_place_dummy");
	    p2.appendToID(currentNode.toString());
	    p2.appendToID(",dummy");
	    p2.setAsNotEndPlace();
	    t.addInputPlace(p1);
	    t.addOutputPlace(p2);
	    myPNet.addPlace(p2);
	    myPNet.addTransition(t);
	    
	    

	}
	
//	int cnt = 0;
//	for (GraphObject nd : succs)
//	{
//	    t = new Transition(currentNode.toString()+"_"+cnt);
//	    p2 = getPlace(nd,currentNode);
//	    if (p2 == null)
//	    {
//		p2 = new Place("Out_place_"+currentNode.toString()+"_in_place_"+nd.toString());
//
//		p2.appendToID(nd.toString());
//		p2.setAsInitialPlace(false);
//		p2.setAsNotEndPlace();
//		myPNet.addPlace(p2);
//	    }
//	    t.addInputPlace(p1);
//	    t.addOutputPlace(p2);
//	    p2.appendToID(","+currentNode.toString());
//	    myPNet.addTransition(t);
//	    cnt++;
//	}
	//return null;
    }
    private List<String> parseConditionExpression(String exp) throws NotConjunctiveNormalFormException
    {
	List<String> parse = new ArrayList<String>();
	List<String> CNF = new ArrayList<String>();
	String term="";
	// determine whether it is disjunctive or conjunctive normal form

	int numOpenBracket = 0;
	for (int i = 0 ; i < exp.length() ; i++)
	{
	    if (exp.charAt(i)== '(')
		numOpenBracket++;
	    else if (exp.charAt(i)==')')
		numOpenBracket--;
	    else if (exp.charAt(i) == ' ')
		continue;
	    else if (exp.toLowerCase().charAt(i)=='o' ) // this might be an or
	    {
		if (i+2 < exp.length())
		{
		    if (exp.toLowerCase().charAt(i+1) == 'r' && exp.toLowerCase().charAt(i+2) == ' ' ) // we are about to find an OR
		    {

			if (numOpenBracket <= 0)
			{
			    NotConjunctiveNormalFormException ne = new NotConjunctiveNormalFormException();
			    throw ne;
			}
			// we have found the or so replace it with ,
			//term+=",";
			term+=" ";
			term+=exp.toLowerCase().charAt(i);
			term+=exp.toLowerCase().charAt(i+1);
			term+=" ";
			i+=2;

		    }
		    else
			term+=exp.toLowerCase().charAt(i);
		}
		else // this is a normal character
		    term+=exp.toLowerCase().charAt(i);
	    }
	    else if (exp.toLowerCase().charAt(i)=='a')
	    {
		if (i+2 < exp.length())
		{
		    if (exp.toLowerCase().charAt(i+1) == 'n' && exp.toLowerCase().charAt(i+2) == 'd' ) // we found an AND
		    {
			// an AND must be out of all brackets
			if (numOpenBracket > 0)
			{
			    NotConjunctiveNormalFormException ne = new NotConjunctiveNormalFormException();
			    throw ne;
			}
			// add the so far term as an OR term and reset it
			CNF.add(term);
			term = "";
			i+=2;
		    }
		    else
			term+=exp.toLowerCase().charAt(i);
		}
		else
		    term+=exp.toLowerCase().charAt(i);

	    }
	    else // this is another character
		term+=exp.charAt(i);
	}
	// add the last term

	CNF.add(term);
	//		 now change to DNF
	parse = convertFromCNFtoDNF(CNF);
	return parse;
    }
private List<String> convertFromCNFtoDNF(List<String> CNF)
{
    List<String> result = new ArrayList<String>();
    String term1;
    if (CNF.size() == 0) // this case must not occur
	return result;
    if (CNF.size() == 1)
    {
	String t = CNF.remove(0);
	String[] r = t.split(" or ");
	for (int m = 0; m < r.length;m++)
	    CNF.add(r[m].trim());
	//t = t.replace(" = ", "_");
	//t = t.replace("=", "_");
	//CNF.add(t);
	return CNF;
    }
    term1 = CNF.get(0);
    for (int i = 1; i < CNF.size();i++)
    {
	term1 = convertPairCNFtoDNF(term1, CNF.get(i));

    }
    String[] r = term1.split("or");
    for (int h = 0 ; h < r.length; h++)
    {
	String tm =r[h].replace(" = ", "_");
	tm = tm.replace(" and ", ",");
	result.add(tm);
    }
    return result;

}
private String convertPairCNFtoDNF(String term1, String term2)
{
    String[] literals1 = term1.split(" or ");
    String[] literals2 = term2.split(" or ");
    String result = "";
    for (int i = 0; i < literals1.length; i++)
    {	for(int j = 0; j < literals2.length;j++)
    {
	result+= literals1[i] + " and "+literals2[j];
    }
    result += " or ";
    }
    return result;
}
public PetriNet getReadableNet()
{
    // This method is used to replace activity ID with readable text
    try
    {
	PetriNet result = this.myPNet.clone();
	int cnt = 0;
	for (Place p : result.myPlaces)
	{
	    String name = p.getName();
	    p.setID("P_"+cnt);
	    cnt++;
	    // do some checing
	    int beginIndex,endInedx;
	    beginIndex = 10;
	    endInedx = name.indexOf("_in_place_");
	    String executedID,executedName,enabledID,enabledName;
	    GraphObject nd;
	    if (name.startsWith("Out_place") && name.contains("_in_place")) // an inner place
	    {
		
		executedID = name.substring(10,endInedx);
		enabledID = name.substring(endInedx+10);
		
		nd = myPGraph.getNode(executedID);
//		executedName = "";
		if (nd != null)
		{
		    if (nd.type == GraphObjectType.EVENT && nd.type2.endsWith("1"))
		    {
			executedName ="start";
		    }
		    else if (nd.type == GraphObjectType.ACTIVITY)
			executedName = nd.getName();
		    else
			executedName = executedID;
		}
		else
		{
		    executedName = executedID;
		}
		name = name.replace("Out_place_", "");
		executedName = executedName.replace(" ", "_");
		name = name.replace(executedID,"executed_"+executedName);
		
		nd = myPGraph.getNode(enabledID);
		
		if (nd != null)
		{
		    if (nd.type == GraphObjectType.ACTIVITY)
			enabledName = nd.getName();
		    else
			enabledName = enabledID;
		}
		else
		{
		    enabledName = enabledID;
		}
		name = name.replace("in_place_", "");
		enabledName = enabledName.replace(" ", "_");
		name = name.replace(enabledID,"enabled_"+enabledName);
	    }
	    else if (name.startsWith("Out_place") || name.startsWith("out_place")) // terminal place
	    {
		//executedID = name.substring(11,endInedx-1);
		executedID = name.substring(10);
		if (executedID.startsWith("EVE"))
		{
		    nd = myPGraph.getNode(executedID);
		    if (nd != null)
		    {
			if (nd.type == GraphObjectType.EVENT && nd.type2.endsWith("3"))
			{
			    executedName = "end";
			}
			else
			    executedName = executedID;
		    }
		    else
			executedName = executedID;
		    name = executedName;
		}
		
	    }
	    else if (name.startsWith("Input_place_of"))
	    {}
	    name = name.replace("#", "");
	    name = name.replace("-", "_");
	    p.setName(name);
	    
	    
	}
	
	cnt = 0;
	for (Transition t : result.myTransitions)
	{
	    t.setID(cnt);
	    cnt++;
	    if (t.getName().startsWith("ACT"))
	    {
		GraphObject nd = myPGraph.getNode(t.getName());
		if (nd != null)
		    t.setName(nd.getName().replace(" ", "_"));
	    }
	    t.setName(t.getName().replace("#", "").replace("-", "_"));
	}
	
	// Now arcs
	
	return result;
    }
    catch(CloneNotSupportedException cnse)
    {
	return null;
    }
}
}
