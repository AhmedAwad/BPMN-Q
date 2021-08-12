package com.bpmnq.petrinet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.bpmnq.Utilities;

public final class PetriNet implements Cloneable{
    public List<Place> myPlaces; // control flow places
    public List<Place> myDataPlaces;
    public List<Transition> myTransitions;
    private String name;

    public PetriNet(String n) {
	this.name = n;
	myPlaces = new ArrayList<Place>(50);
	myTransitions = new ArrayList<Transition>(20);
    }

    public PetriNet() {
	this("net" + Utilities.getNextVal());
    }

    public PetriNet clone() throws CloneNotSupportedException {
	PetriNet newNet = (PetriNet) super.clone();
//	for (int i = 0 ; i < myPlaces.size();i++)
//	    newNet.addPlace(myPlaces.get(i));
//	int x = myTransitions.size();
//	for (int i = 0 ; i < x;i++)
//	    newNet.addTransition(myTransitions.get(i));

	return newNet;
    }

    public void addPlace(Place x) {
	if (!this.myPlaces.contains(x))
	    this.myPlaces.add(x);
    }

    public void addTransition(Transition x) {
//	if (!this.myTransitions.contains(x))
	this.myTransitions.add(x);
    }

    public String toString() {
	return this.name;
    }

    public Place getPlace(String prd, String suc) {
	for (int i = 0 ; i < myPlaces.size();i++)
	    if (myPlaces.get(i).getID().contains(prd)  
		    && myPlaces.get(i).getID().contains(suc))
		return myPlaces.get(i);

	return null;
    }
    public Place getPlace(String dobState) {
	for (int i = 0 ; i < myPlaces.size();i++)
	    if (myPlaces.get(i).getID().contains(dobState))
		return myPlaces.get(i);

	return null;
    }
    public Transition getTransition(String x) {
	for (int i = 0 ; i < myTransitions.size();i++)
	    if (myTransitions.get(i).getName().equals(x))
		return myTransitions.get(i);

	return null;
    }

    public void writeLoLANetFile(String filePath)  throws IOException {
	PrintWriter outFile;
	StringBuffer txt = new StringBuffer(200);
	try 
	{  // Create the output stream.
	    outFile = new PrintWriter(new BufferedWriter(
		    new FileWriter(filePath)));
	    txt.append("PLACE ");
	    for (int i = 0 ; i < myPlaces.size();i++)
		txt.append(myPlaces.get(i).toString()+",\n");
	    txt.deleteCharAt(txt.length()-2);
	    txt.append(";");
	    outFile.println(txt);
	    // this resets the txt 
	    txt.delete(0, txt.length());
	    txt.append("MARKING ");
	    for (int i = 0 ; i < myPlaces.size();i++)
		if (myPlaces.get(i).isItInitialPlace())
		    txt.append(myPlaces.get(i).toString()+": 1,\n");
	    txt.deleteCharAt(txt.length()-2);
	    txt.append(";");
	    outFile.println(txt);
	    txt = new StringBuffer();
	    int cnt=0;
	    for (int i =0; i < myTransitions.size();i++)
	    {
		if (!txt.toString().contains(myTransitions.get(i).getName()))
		    {
		    	outFile.println(myTransitions.get(i).toString());
		    	txt.append(myTransitions.get(i).getName());
		    }
		else
		{
		    myTransitions.get(i).setName(myTransitions.get(i).getName()+"_"+cnt);
		    outFile.println(myTransitions.get(i).toString());
		    	//txt.append(myTransitions.get(i).getName());
		    	cnt++;
		}
	    }
	    outFile.flush();
	    outFile.close();
	}
	catch (IOException e) {
	    // TODO: handle exception
	    
	    e.printStackTrace();
	    throw e;
	}

    }

    public void writeAPNNNFile(String filePath)  throws IOException {
	PrintWriter outFile;
	StringBuffer txt = new StringBuffer(200);
	try 
	{  // Create the output stream.
	    outFile = new PrintWriter(new BufferedWriter(
		    new FileWriter(filePath)));
	    txt.append("\\beginnet{dummy}\n");
	    
	    
	    
	    for (int i = 0 ; i < myPlaces.size();i++)
		txt.append(myPlaces.get(i).getPlaceAsAPNN()+"\n");
//	    txt.deleteCharAt(txt.length()-2);
//	    txt.append(";");
	    outFile.println(txt);
	    // this resets the txt 
	    txt.delete(0, txt.length());
	    
	    
	    txt = new StringBuffer();
	    int cnt=0;
	    for (int i =0; i < myTransitions.size();i++)
	    {
		if (!txt.toString().contains(myTransitions.get(i).getName()))
		    {
		    	outFile.println(myTransitions.get(i).getTransitionAsAPNN());
		    	txt.append(myTransitions.get(i).getName());
		    }
		else
		{
		    String outtt = myTransitions.get(i).getTransitionAsAPNN();
		    outtt = outtt.replace(myTransitions.get(i).getName(), myTransitions.get(i).getName()+"_"+cnt );
//		    setName(myTransitions.get(i).getName()+"_"+cnt);
		    outFile.println(outtt);
		    	//txt.append(myTransitions.get(i).getName());
		    	cnt++;
		}
	    }
	    cnt = 0;
	    for (Transition t: myTransitions)
	    {
		for (Place i : t.getInputPlaces())
		{
		    outFile.print("\\arc{A_"+cnt+"}{\\from{"+i.getID()+"} \\to{T_"+t.getID()+"} \\weight{1} \\type{ordinary}}\n");
		    cnt++;
		}
		for (Place o : t.getOutputPlaces())
		{
		    outFile.print("\\arc{A_"+cnt+"}{\\from{T_"+t.getID()+"} \\to{"+o.getID()+"} \\weight{1} \\type{ordinary}}\n");
		    cnt++;
		}
	    }
	    
	    outFile.println("\n\\endnet");
	    outFile.flush();
	    outFile.close();
	}
	catch (IOException e) {
	    // TODO: handle exception
	    
	    e.printStackTrace();
	    throw e;
	}

    }
    public void print(PrintStream outStream) {
	for (Place place : myPlaces)
	    outStream.println(place.toString());

	for (Transition trans : myTransitions)
	    outStream.println(trans.toString());
    }
    private List<Place> getEndPlaces()
    {
	List<Place> outPlaces = new ArrayList<Place>();
	for (Place p : myPlaces)
	{
	    boolean isInput = false;
	    for (Transition t : myTransitions)
	    {
		if (t.getInputPlaces().contains(p))
		{
		    isInput = true;
		    break;
		}
	    }
	    if (!isInput)
	    {
		p.setAsEndPlace();
		outPlaces.add(p);
	    }
	}
	return outPlaces;
    }
    private List<Place> getStartPlaces()
    {
	List<Place> outPlaces = new ArrayList<Place>();
	for (Place p : myPlaces)
	{
	    
	    if (p.isItInitialPlace())
		outPlaces.add(p);
	    
	}
	return outPlaces;
    }
    public void shortCircuit() {
	
	List<Place> outPlaces = getEndPlaces();
	
	List<Place> inPlaces = getStartPlaces();
	int j = 0;
	for (Place out : outPlaces)
	{
	    Transition shortTransition;
	    shortTransition = new Transition("shortCircuit"+j);
	    for (Place in : inPlaces)
	    {
		shortTransition.addInputPlace(out);
		shortTransition.addOutputPlace(in);
		addTransition(shortTransition);
	    }
	    j++;
	}
	
	
    }
    public void printEndPlaces()
    {
	for (Place c : myPlaces)
	{
	    boolean found = false;
	    for (Transition t : myTransitions)
	    {
		if (t.hasInputPlace(c))
		{
		    found = true;
		    break;
		}
	    }
	    if (!found)
		System.out.println(c.toString());
	}
    }
}
