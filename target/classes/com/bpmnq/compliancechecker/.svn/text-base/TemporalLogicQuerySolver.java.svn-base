package com.bpmnq.compliancechecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import com.bpmnq.DataObject;

public final class TemporalLogicQuerySolver
{
    //public 
    private BusinessContext bc;
    private ModelChecker mc;
    private Map<String, String> dobStateQueryResult;
    public TemporalLogicQuerySolver(BusinessContext bx,ModelChecker m)
    {
	bc = bx;
	mc = m;
	if (bc == null)
	{
	    System.out.println("??????????? NULL BUSINESS CONTEXT ????????");
	}
    }
    public Map<String,String> resolvePureDataFlowQuery(TemporalQueryGraph query,String modelID)
    {
	// query must be a pure data flow query having only one
	// activity and one or more states
	dobStateQueryResult = new HashMap<String, String>();
	String[] states;
	for (DataObject dob : query.dataObjs)
	{
	    if (bc == null)
	    {
		System.out.println("!!!!!!!!! NULL BUSINESS CONTEXT ????????");
	    }
	    if (dob.name == null)
	    {
		System.out.println("!!!!!!!!! Data Object Name ????????");
	    }
	    System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
	    System.out.println("DOB :"+dob.name);
	    System.out.println("DOB States");
	    bc.printDobStates(System.out);
	    System.out.println(bc.getDataObjectStates(dob.name));
	    System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
	    String sss =bc.getDataObjectStates(dob.name); 
	    if (sss == null)
	    {
		return dobStateQueryResult;
	    }
	    states = sss.split(",");
	    
	    for (int i = 0 ; i < states.length;i++)
	    {
		String formula = "CTLSPEC EF(enabled"+query.nodes.get(0).getTemporalExpressionName().replace("executed", "")+" & "+dob.name+"_"+states[i]+")";
		if (mc.checkModelAgainstFormula(modelID, formula) == ModelChecker.RET_NET_COMPLIES)
		{
		    if (!dobStateQueryResult.keySet().contains(dob.name))
		    {
			dobStateQueryResult.put(dob.name, states[i]);
		    }
		    else
		    {
			String tmp = dobStateQueryResult.get(dob.name);
			tmp += ","+states[i];
			dobStateQueryResult.put(dob.name, tmp);
		    }
		}
	    }
	}
	return dobStateQueryResult;
    }
    public Map<String,String> resolveConditionalPrecedesQuery(TemporalQueryGraph query, String modelID)
    {
	
	// the query is on the form 
	// the query contains two activities with a precedes path
	// the source node is associated with data conditions
	// G(ready(ActY) -> O(executed(ActX) and state condition  and G(! contradicting state condition))
	// the contradicting state condition is derived from the utilization of the  business context instance
	// calling the method to get contradicting state of some other state
	dobStateQueryResult = new HashMap<String, String>();
	String[] states;
	for (DataObject dob : query.dataObjs)
	{
	   
//	    else
//		formula+= "true";
	    
	    states = bc.getDataObjectStates(dob.name).split(",");
	    // we need to get rid of the initial state
	    String[] newStates = new String[states.length-1];
	    for (int i = 0,j=0 ; i < states.length;i++)
	    {
		if (!states[i].contains("initial"))
		{
		    newStates[j] = states[i];
		    j++;
		}
	    }
	    List<String> powerSet = powerset(newStates,dob.name);
	    
	    for (String stat : powerSet)
	    {
		if (stat.length() == 0)
		    continue;
		String formula = " G(enabled"+query.paths.get(0).getDestinationGraphObject().getTemporalExpressionName().replace("executed", "")+" -> O( ";
//		if (!query.paths.get(0).getSourceGraphObject().getName().startsWith("@"))
		formula+= query.paths.get(0).getSourceGraphObject().getTemporalExpressionName();
		formula += " &("+stat+ ")))";
		formula = formula.replace("true &", "");
		if (mc.checkModelAgainstFormula(modelID, formula) == ModelChecker.RET_NET_COMPLIES)
		{
//		    if (!dobStateQueryResult.keySet().contains(dob.name))
//		    {
			dobStateQueryResult.put(dob.name, stat);
//		    }
//		    else
//		    {
//			String tmp = dobStateQueryResult.get(dob.name);
//			tmp += ","+states[i];
//			dobStateQueryResult.put(dob.name, tmp);
//		    }
		    // we have enough states
		    break;
		}
	    }
	}
	
	
	return dobStateQueryResult;
    }
    private List<String> powerset(String[] set,String dobName) 
    {

	//create the empty power set
	List<String> power = new ArrayList<String>();

	//get the number of elements in the set
	int elements = set.length;

	//the number of members of a power set is 2^n
	int powerElements = (int) Math.pow(2,elements);

	//run a binary counter for the number of power elements
	for (int i = 0; i < powerElements; i++) 
	{

	    //convert the binary number to a string containing n digits
	    String binary = intToBinary(i, elements);

	    //create a new set
	    String innerSet = "";

	    //convert each digit in the current binary number to the corresponding element
	    //in the given set
	    for (int j = 0; j < binary.length(); j++) {
		if (binary.charAt(j) == '1')
		    innerSet+=dobName +"_"+set[j]+" | ";
	    }

	    //add the new set to the power set
	    if (innerSet.length()== 0)
		continue;
	    
	    innerSet = innerSet.substring(0,innerSet.length()-2);
	    power.add(innerSet);

	}

	return power;
    }
    private String intToBinary(int binary, int digits) 
    {

	String temp = Integer.toBinaryString(binary);
	int foundDigits = temp.length();
	String returner = temp;
	for (int i = foundDigits; i < digits; i++) {
	    returner = "0" + returner;
	}

	return returner;
    } 
}
