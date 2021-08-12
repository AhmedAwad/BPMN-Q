package com.complianceviolationresolution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bpmnq.Utilities;
import com.bpmnq.compliancechecker.BusinessContext;
import com.pst.Node;
import com.pst.ProcessStructureTree;
import com.pst.Node.NodeType;


public final class Planner
{
    private BusinessContext bc;
    private String plannerApplication = "C:\\Applications\\BlackboxPlanner";//\\blackbox.exe";
    public Planner(BusinessContext b)
    {
	bc = b;
    }
    public ProcessStructureTree findPlan(List<String> initState, List<String> finalState)
    {
	String domain = prepareDomainDescriptionFile();
	String problem = prepareProblemDescriptionFile(initState,finalState);
	PrintWriter domFile, probFile;
	try{
	    domFile =  new PrintWriter(new BufferedWriter(
		    new FileWriter(plannerApplication+"\\domain.pddl")));
	    probFile = new PrintWriter(new BufferedWriter(
		    new FileWriter(plannerApplication+"\\problem.pddl")));
	    domFile.print(domain);
	    probFile.print(problem);
	    
	    domFile.flush();
	    domFile.close();
	    
	    probFile.flush();
	    probFile.close();
	    
	    //every thing is ready, call the blackbox planner
	    List<String> result = new ArrayList<String>();
	    Process plannerProcess = Runtime.getRuntime().exec(
		    plannerApplication+"\\blackbox"+ " -o "+plannerApplication+"\\domain.pddl -f "+plannerApplication+"\\problem.pddl -g "+plannerApplication+"\\output.txt");
	    BufferedReader in = new BufferedReader(new InputStreamReader(
		    plannerProcess.getInputStream()));
	    String currentLine = null;
	    while ((currentLine = in.readLine()) != null)
		result.add(currentLine);
	    
	    plannerProcess.waitFor();
	    
	    // check for an answer
	    boolean planFound=false;
	    List<String> plan = new ArrayList<String>();
	    for (String s : result)
	    {
		if (!planFound && !s.startsWith("Begin plan"))
		    continue;
		else
		{
		    planFound = true;
		    //continue;
		}
		if (planFound && s.startsWith("Begin plan"))
		    continue;
		if (planFound && !s.startsWith("End plan"))
		{
		    plan.add(s);
		}
		if (s.startsWith("End plan"))
		    break;
	    }
	    if (planFound)
		return constructTreeFromPlannerOutput(plan);
	    return null;
	}
	catch (InterruptedException ie) {
	    System.out.println(ie.getMessage());
	    // exit is an inappropriate reaction
	    // System.exit(Utilities.ABNORMAL_TERMINATION); // abnormal
	    throw new RuntimeException("External tool Lola terminated unexpectedly, cannot continue.", ie);
	}
	catch(IOException ioe)
	{
	    ioe.printStackTrace();
	}
	
	return null;
    }
    private ProcessStructureTree constructTreeFromPlannerOutput(List<String> plan)
    {
	Map<String,Integer> stepCount = new HashMap<String, Integer>();
	Map<String,String> steps = new HashMap<String, String>();
	for (String step: plan)
	{
	    String stepNumber = step.substring(0, step.indexOf(' '));
	    if (stepCount.keySet().contains(stepNumber))
	    {
		int stpCnt = stepCount.get(stepNumber).intValue();
		stpCnt++;
		stepCount.put(stepNumber, new Integer(stpCnt));
	    }
	    else
	    {
		stepCount.put(stepNumber, new Integer(1));
	    }
	    
	    if (steps.keySet().contains(stepNumber))
	    {
		String tmp = steps.get(stepNumber);
		tmp+= ","+restoreActivityName(step);
		steps.put(stepNumber, tmp);
	    }
	    else
	    {
		steps.put(stepNumber, restoreActivityName(step));
	    }
	    
	}
	// now construct tree
	Node root = new Node();
	root.setNodeType(NodeType.Sequence);
	root.setCondition("true");
	root.setParent(null);
	Node dummy;
	Node currentParent;
	ProcessStructureTree pst = new ProcessStructureTree(root);
	
	int stpCnt;
	List<String> added = new ArrayList<String>();
	for (String stepnum : plan)
	{
	    
	    currentParent = root;
	    stepnum = stepnum.substring(0, stepnum.indexOf(' '));
	    if (added.contains(stepnum))
		continue;
	    else
	    {
		stpCnt = stepCount.get(stepnum).intValue();

		if (stpCnt == 1)
		{
		    dummy = new Node();
		    dummy.setNodeType(NodeType.Activity);
		    dummy.setLabel(steps.get(stepnum));
		    if (currentParent.getNodeType() == NodeType.Sequence)
			pst.insertNode(dummy, currentParent, currentParent.getChildren().size()+1);
		    else
			pst.insertNode(dummy, currentParent);
		}
		else
		{
		    Node parallel = new Node();
		    Node dummy2;
		    parallel.setNodeType(NodeType.Parallel);
		    pst.insertNode(parallel, currentParent, currentParent.getChildren().size()+1);
		    // we have to create sequence block nodes exactly as the number of the parallel steps -- currently ignore
		    String sss = steps.get(stepnum);
		    String sssA[] = sss.split(",");
		    for(String m: sssA)
		    {
			dummy2 = new Node();
			dummy2.setNodeType(NodeType.Sequence);
			dummy2.setCondition("true");
			pst.insertNode(dummy2, parallel);
			dummy = new Node();
			dummy.setNodeType(NodeType.Activity);
			dummy.setLabel(m.trim());
			pst.insertNode(dummy, dummy2,dummy2.getChildren().size()+1);
		    }
		}
		added.add(stepnum);
	    } 
	}
	return pst;
    }
    private String restoreActivityName(String plannerOutput)
    {
	String result = plannerOutput.substring(11, plannerOutput.indexOf(' ',2)-1);
	String res2="";
	for (int i = 0 ; i < result.length();i++)
	{
	    if (Character.isDigit(result.charAt(i)))
		continue;
	    else if (result.charAt(i) == '-')
		res2 += " ";
	    else
		res2 += result.charAt(i);
	}
	return res2;
    }
    private String prepareDomainDescriptionFile()
    {
	String domain="(define (domain business-context)\n(:predicates\n ";
	domain+="(EXECUTED ?act) ";
	for (String s : bc.getAllDataObjectStates())
	{
	    if (!domain.contains(s.toUpperCase()))
	    {
		domain+= "("+s.toUpperCase()+" ?state) ";
	    }
	}
//	for (String s : bc.getBusinessContextActivities())
//	{
//	    domain+= "("+s.replace(" ", "-")+" ?act)";
//	}
	domain+=")\n;ACTIONS\n";
	// now write the actions all with its pre and post conditions
	for (String s : bc.getBusinessContextActivities())
	{
	    int cnt = 1;
	    String preCond,postCondP,postCondN;
	    String preCondA[]= new String[0],postCondPA[]= new String[0],postCondNA[]= new String[0];
	    
	    preCond = bc.getActivityDataPreCondition(s);
	    if (preCond != null)
		preCondA = preCond.split(",");
	    
	    postCondP = bc.getActivityDataPostConditionPositive(s);
	    if (postCondP != null)
		postCondPA = postCondP.split(",");
	    
	    postCondN = bc.getActivityDataPostConditionNegative(s);
	    if (postCondN != null)
		postCondNA = postCondN.split(",");
	    List<String> added = new ArrayList<String>();
	    // now all combinations
	    for (int i = 0 ; i < preCondA.length;i++)
		for(int j = 0 ; j < postCondNA.length;j++)
		    for(int k = 0; k < postCondPA.length;k++)
		    {
			String parameters =":parameters(";
			added.clear();
			for (String c : getSeparateDataStates(preCondA[i]))
			{
			    if (!added.contains(Utilities.getDataObjectName(c).toLowerCase()))
			    {
				parameters+= "?"+Utilities.getDataObjectName(c).toLowerCase()+" ";
				added.add(Utilities.getDataObjectName(c).toLowerCase());
			    }
			}
			for (String c : getSeparateDataStates(postCondNA[j]))
			{
			    if (!added.contains(Utilities.getDataObjectName(c).toLowerCase()))
			    {
				parameters+= "?"+Utilities.getDataObjectName(c).toLowerCase()+" ";
				added.add(Utilities.getDataObjectName(c).toLowerCase());
			    }
			}
			for (String c : getSeparateDataStates(postCondPA[k]))
			{
			    if (!added.contains(Utilities.getDataObjectName(c).toLowerCase()))
			    {
				parameters+= "?"+Utilities.getDataObjectName(c).toLowerCase()+" ";
				added.add(Utilities.getDataObjectName(c).toLowerCase());
			    }
			}
			parameters+=")";
			String prCondPDDL = generateConjunctiveStatementofDataState(preCondA[i]);
			String postCondPDDL = generateConjunctiveStatementPostCondition(postCondPA[k],postCondNA[j],s);
			domain+="(:action execute_"+s.replace(" ", "_")+cnt+"\n";
			domain+=parameters+"\n";
			domain+=":precondition "+prCondPDDL+"\n";
			domain+=":effect "+postCondPDDL+"\n"+")\n";
			cnt++;
		    }
	}
	return domain+")";
    }
    private String generateConjunctiveStatementPostCondition(String s1, String s2, String actname)
    {
	String result = "(and (EXECUTED "+ actname.replace(" ", "-")+") ";
	
	String a[] = s1.split("and");
	for (String ss : a)
	{
	    result += " ("+ss.toUpperCase().trim()+ " "+"?"+Utilities.getDataObjectName(ss.trim()).toLowerCase()+") ";
	}
	//result +=")";
	a = s2.split("and");
	for (String ss : a)
	{
	    result += " (not ("+ss.toUpperCase().trim()+ " "+"?"+Utilities.getDataObjectName(ss.trim()).toLowerCase()+")) ";
	}
	result +=")";
	return result;
    }
    private List<String> getSeparateDataStates(String s)
    {
	List<String> result = new ArrayList<String>();
	String y[] = s.split("and");
	for (String c :y)
	{
	    result.add(c.trim());
	    
	}
	
	return result;
    }
    private String generateConjunctiveStatementofDataState(String s)
    {
	String result = "(and ";
	String a[] = s.split("and");
	for (String ss : a)
	{
	    result += " ("+ss.toUpperCase().trim()+ " "+"?"+Utilities.getDataObjectName(ss.trim()).toLowerCase()+") ";
	}
	result +=")";
	return result;
    }
    private String prepareProblemDescriptionFile(List<String> initialState, List<String> targetState)
    {
	String problem = "(define (problem planning-problem)\n(:domain business-context)\n";
	// now, define objects
	problem +="(:objects \n";
	for (String s : bc.getBusinessContextActivities())
	{
	    problem+= s.replace(" ", "-")+" \n";
	}
	List<String> added = new ArrayList<String>();
	for (String s : bc.getAllDataObjectStates())
	{
	    if (!added.contains(Utilities.getDataObjectName(s).toLowerCase().trim()))
	    {
		problem+= Utilities.getDataObjectName(s).toLowerCase().trim()+" \n";
		added.add(Utilities.getDataObjectName(s).toLowerCase().trim());
	    }
	}
	problem +=")\n";
	// writing the initial state
	problem += "(:init \n";
	for(String i : initialState)
	{
	    if (i.startsWith("EXECUTED"))
	    {
		problem+= "("+i.toLowerCase()+") \n";
	    }
	    else 
	    {
		problem+= "("+i.toUpperCase() +" "+ Utilities.getDataObjectName(i).toLowerCase().trim()+")\n";
	    }
	}
	problem +=")\n";
	// the final state, or the goal state
	problem += "(:goal (and ";
	for (String i : targetState)
	{
	    if (i.startsWith("EXECUTED"))
	    {
		problem+= "("+i.toLowerCase()+") ";
	    }
	    else if (i.startsWith("!EXECUTED"))
	    {
		problem+="(not ("+i.substring(1).toLowerCase()+")) ";
	    }
	    else 
	    {
		problem+= "("+i.toUpperCase() +" "+ Utilities.getDataObjectName(i).toLowerCase().trim()+")";
	    }
	}
	problem +=")))";
	return problem;
    }
    
    
}
