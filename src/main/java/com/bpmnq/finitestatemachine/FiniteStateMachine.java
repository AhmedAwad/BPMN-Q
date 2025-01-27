package com.bpmnq.finitestatemachine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bpmnq.GraphObject;
import com.bpmnq.ProcessGraph;
import com.bpmnq.Utilities;

public final class FiniteStateMachine 
{
    private Logger log = Logger.getLogger(FiniteStateMachine.class);
    private List<State> myStates;
    private List<StateTransition> myTransitions;
    private List<String> myActions;
    private HashMap<String, String> enablement;
    private ProcessGraph myProcess;

    public FiniteStateMachine()
    {
	myStates = new ArrayList<State>(10);
	myTransitions = new ArrayList<StateTransition>(50);
	myActions = new ArrayList<String>(50);
	enablement = new HashMap<String, String>();
	myProcess = null;
    }
    public FiniteStateMachine(ProcessGraph p)
    {
	this();
	myProcess = p; 
	
    }

    private List<State> getActionNextState(String act)
    {
	List<State> result = new ArrayList<State>(10);
	for (int i =0; i < myTransitions.size();i++)
	{
	    if (myTransitions.get(i).getAction().equals(act))
		result.add(myTransitions.get(i).getDestinationState());
	}
	return result;
    }

    private List<State> getStateNextState(State st)
    {
	List<State> result = new ArrayList<State>(10);
	for (int i =0; i < myTransitions.size();i++)
	{
	    if (myTransitions.get(i).getSourceState().equals(st))
		result.add(myTransitions.get(i).getDestinationState());
	}
	return result;
    }
    /**
     * 
     * @param file : the .graph file output by LoLA containg the search space.
     * The file must be created with option -m to the LoLA 
     * @throws IOException
     */
     public void loadStateFromLOLAStateFile(String file) throws IOException
    {
	myStates.clear();
	myTransitions.clear();
	myActions.clear();
	
	try
	{
	    BufferedReader breader = new BufferedReader(new FileReader(file));
	    String line;
	    State from, to=null;
	    StateTransition tr;
	    StringBuilder fromid, toid, action;
	    int transid = 0;
	    while (true)
	    {
		line = breader.readLine();
		if (line == null)
		    break;
		if (line.trim().length()==0)
		    continue;

		if (line.contains("FROM"))
		{
		    fromid = new StringBuilder(5);
		    for(int i = line.indexOf("FROM ")+5; line.charAt(i) != ' ';i++)
			fromid.append(line.charAt(i));
		    from = new State(Integer.parseInt(fromid.toString()));
		    toid = new StringBuilder(5);
		    for(int i = line.indexOf("STATE ")+6; line.charAt(i) != ' ';i++)
			toid.append(line.charAt(i));
		    to = new State(Integer.parseInt(toid.toString()));
		    action = new StringBuilder(10);
		    boolean underscorefound = false;
		    for(int i = line.indexOf("BY ")+3; line.charAt(i) != ';'/*&& line.charAt(i) != '_'*/;i++)
		    {
			if (line.charAt(i) == '_' && !underscorefound)
			{
			    underscorefound= true;
			}
			else if (line.charAt(i) == '_' && underscorefound)
			    break;
			    
			action.append(line.charAt(i));
		    }
//		    if (action.lastIndexOf("_") != action.indexOf("_")) // this is a renaming of an action due to nondeterminism
//		    {
//			action = action.substring(0, action.lastIndexOf("_")-1);
//		    }
		    transid++;
		    tr = new StateTransition(transid,from,to,action.toString());
		    if (!myStates.contains(from))
			myStates.add(from);
		    if (!myStates.contains(to))
			myStates.add(to);
		    if (!myActions.contains(action.toString()))
			myActions.add(action.toString());

		    myTransitions.add(tr);
		}
		else if (line.contains("STATE ")) // this is an initial state
		{
		    toid = new StringBuilder(5);
		    for(int i = line.indexOf("STATE ")+6; line.charAt(i) != ';';i++)
			toid.append(line.charAt(i));
		    to = new State(Integer.parseInt(toid.toString()));
		    if (!myStates.contains(to))
			myStates.add(to);
		}
		else if (line.contains(":"))
		{
			if (to != null)
				to.addMarkedPlace(line.substring(0, line.indexOf(":")));
		}
		else if (line.trim().length() > 0)
		{
			if (to != null)
			{
			    // Do some checks for multiple enablements
			    to.addAction(line.trim());
			}
		}
	    }
	    breader.close();
	}
	catch(FileNotFoundException fnf)
	{
	    log.fatal("Invalid file path...");
	    throw fnf;

	}
	catch(IOException ioe)
	{
	    log.fatal("Reading file terminated unexpectedly...", ioe);
	    throw ioe;

	}
    }

    public void print(PrintStream outStream)
    {
	for (StateTransition trans : myTransitions)
	    outStream.println(trans.toString());
    }
    /**
     * 
     * @param file : the target .smv file on which the search space of NuSMV will be written
     * @param temporalExpression : the expression that needs to checked by the MC
     * @throws IOException
     */
    public void writeNuSMVSpecToFile(String file, String temporalExpression) throws IOException
    {
	Map<String,String> execStates = new HashMap<String, String>();
//	Map<String,String> enabledStates = new HashMap<String, String>();
	
	if (myStates.size() == 0)
	{
	    log.warn("State machine is empty...");
	    return;
	}
	
	StringBuilder state = new StringBuilder(100);
	state.append("state : {");
	boolean isFirst = true;
	for (State currState : myStates)
	{
	    if (!isFirst)
		state.append(",");
	    else 
		isFirst = false;

	    state.append(currState.toString());
	    for (String s : currState.getEnabledActions())
	    {
		String actName="";
//		StringBuilder df = new StringBuilder(50);
		if (s.startsWith("ACT"))
		{


		    try
		    {
			int actId ;
			if (s.contains("_"))
			    actId = Integer.parseInt(s.substring(3,s.indexOf("_")));
			else
			    actId = Integer.parseInt(s.substring(3));

			actName = Utilities.getActivityName(actId);
		    } catch (SQLException e)
		    {
			log.error("Databse error. Could not get an activity's name. Results may be incorrect.", e);
			actName = "";
		    }
		    catch(NumberFormatException nfe)
		    {
			// this would occur in case of processing oryx models
			int underScorePos = s.substring(9).indexOf("_");
			String find;
			if (underScorePos == -1)
			    find = s;
			else
			    find = s.substring(0, underScorePos+9);
			// 26.4.2010 : cleaning s
			GraphObject nd = myProcess.getNode(find);
			if (nd != null)
			    actName = nd.getName();
		    }
		    actName = actName.replace(' ', '_');
		    if (enablement.containsKey(actName))
		    {
			enablement.put(actName, enablement.get(actName)+","+currState.toString());
		    }
		    else
		    {
			enablement.put(actName, "enabled_"+actName.trim().replace(' ', '_')+" := state in {" + currState.toString());
		    }
		}
//		else
//		{
//		    actName = s;
//		}
	    	
	    }
	    for (String s : currState.getMarkedPlaces())
	    {
	    	if (s.startsWith("out_place_") || s.startsWith("Out_place_") || s.startsWith("Input_place_"))
	    	    continue;
	    	
		if (enablement.containsKey(s))
	    	{
	    		enablement.put(s, enablement.get(s)+","+currState.toString());
	    	}
	    	else
	    	{
	    		enablement.put(s, s.trim().replace(' ', '_')+" := state in {" + currState.toString());
	    	}
	    }
	}
	state.append("};");
	
	StringBuilder init = new StringBuilder(20);
	init.append("init(state) := " + myStates.get(0).toString() + ";");

	StringBuilder next = new StringBuilder(500);
	next.append("next(state) := case\n");
	for (int i = 0 ; i < myStates.size(); i++)
	{
	    List<State> nx = getStateNextState(myStates.get(i));
	    if (nx.size() > 0)
	    {
		next.append("state = " + myStates.get(i).toString()+" :{");
		for (int j = 0 ; j < nx.size(); j++)
		{
		    next.append(nx.get(j).toString());
		    if (j != nx.size()-1)
			next.append(",");
		}
		next.append("};\n");
	    }
	}
	next.append(" 1 : state;");
	next.append("esac;");
	List<String> define = new ArrayList<String>(10);
	
	// definition used in expressing PLTL
	for (String action : myActions)
	{
	    String actName="";
	    StringBuilder df = new StringBuilder(50);
	    if (action.startsWith("ACT"))
	    {
		
		
		try
		{
		    int actId ;
		    if (action.contains("_"))
			actId = Integer.parseInt(action.substring(3,action.indexOf("_")));
		    else
			actId = Integer.parseInt(action.substring(3));
		    
		    actName = Utilities.getActivityName(actId);
		} catch (SQLException e)
		{
		    log.error("Databse error. Could not get an activity's name. Results may be incorrect.", e);
		    actName = "";
		}
		 catch(NumberFormatException nfe)
		 {
		     // this would occur in case of processing oryx models
		     int underScorePos = action.substring(9).indexOf("_");
			String find;
			if (underScorePos == -1)
			    find = action;
			else
			    find = action.substring(0, underScorePos+9);
		     GraphObject nd = myProcess.getNode(find);
		     
		     if (nd != null)
			 actName = nd.getName();
		 }
		actName = actName.replace(' ', '_');
		List<State> nx = getActionNextState(action);
		if (nx.size() > 0)
		{
		    df.append("executed_"+actName+" := state in {");
		    for (int j = 0 ; j < nx.size(); j++)
		    {
			if (execStates.containsKey(actName))
			{
			    execStates.put(actName, execStates.get(actName)+","+nx.get(j).toString());
			}
			else
			{
			    execStates.put(actName, "executed_"+actName.trim().replace(' ', '_')+" := state in {" + nx.get(j).toString());
			}
		    }
//		    df.append("};\n");
//		    define.add(df.toString());
		}
		
		
	    }
//	    else
//	    {
//		actName = action;
//	    }
//		
	   
	}
	
	for (String s : execStates.values())
	{
		define.add(s+"};\n");
	}
	for (String s : enablement.values())
	{
		define.add(s+"};\n");
	}
	
	// all are ready write them to file
	try
	{
	    PrintWriter outFile = new PrintWriter(new BufferedWriter(
		    new FileWriter(file)));
	    outFile.println("MODULE main");
	    outFile.println("VAR");
	    outFile.println(state.toString());
	    outFile.println("ASSIGN");
	    outFile.println(init.toString());
	    outFile.println(next.toString());
	    outFile.println("DEFINE");
	    for (int i = 0 ; i < define.size(); i++)
	    {
		outFile.println(define.get(i));

	    }
	    // this command is hard coded and will be removed after this unit testing is successful
	    if (temporalExpression.length() > 0)
	    {
		temporalExpression=temporalExpression.replace("AND", "\nLTLSPEC ");
		if (temporalExpression.startsWith("CTLSPEC"))
		    outFile.println(temporalExpression);
		else
		    outFile.println("LTLSPEC "+temporalExpression);
	    }
	    outFile.close();
	}
	catch(IOException ioe)
	{
	    log.fatal("Error writing to file " + file, ioe);
	    throw ioe;

	}
    }
    public void writeDotFile(String fileName)
    {
	 FileWriter fstream;
	try
	{
	    fstream = new FileWriter(fileName);
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write("digraph processTemplate {\n");
	    // write nodes
	    for (State s: this.myStates)
	    {
		String props = s.getMarkedPlaces().toString().replace("[", "").replace("]", "");
		
		out.write(s.toString()+ " [label=\""+props+"\",shape=\"circle\"]\n");
	    }
	    
	    // write edges
	    for (StateTransition st : myTransitions)
	    {
		out.write(st.getSourceState().toString() +" -> "+ st.getDestinationState().toString()+"\n");
	    }
	    out.write("}\n");
	    out.flush();
	    out.close();
	} catch (IOException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	       
    }
}
