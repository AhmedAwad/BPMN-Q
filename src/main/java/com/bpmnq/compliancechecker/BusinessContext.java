package com.bpmnq.compliancechecker;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.bpmnq.DataObject;
import com.bpmnq.ProcessGraph;
import com.bpmnq.Utilities;

public final class BusinessContext
{
    private Map<String,String> dobStates; // key= data object name, value= string concat of different states
    private Map<String,String> contraStates;// key= a data object state value= string concat of contradicting states
    private List<String> activities;
    private Map<String,String> activitiesPreConditions;
    private Map<String,String> activitiesPostConditionsPositive;
    private Map<String,String> activitiesPostConditionsNegative;
    private Map<String,String> contraActivities;
    private ProcessGraph process;
    public BusinessContext()
    {
	reset();
    }
    public BusinessContext(ProcessGraph pp)
    {
	reset();
	process = (ProcessGraph) pp.clone();
    }
    private void reset()
    {
	dobStates = new HashMap<String, String>();
	contraStates = new HashMap<String, String>();
	activities = new ArrayList<String>();
	activitiesPreConditions = new HashMap<String, String>();
	activitiesPostConditionsPositive = new HashMap<String, String>();
	activitiesPostConditionsNegative = new HashMap<String, String>();
	contraActivities = new HashMap<String, String>();
	process = new ProcessGraph();
    }
    public void loadDataObjectStates(String modelURI)
    {
	if (modelURI.startsWith("http"))
	    loadDataObjectStatesURI(modelURI);
	else
	    loadDataObjectStatesDB(modelURI);
    }
    public void loadContradictingStates()
    {
	if (!Utilities.isConnectionOpen())
	{
	    loadDefaultContradictingStates();
	    return;
	}
	
	String selStatement ="SELECT \"STATE\", \"CONTRA_STATE\" FROM \"BPMN_GRAPH\".\"CONTRADICTING_STATE\"";
	String key,value,temp;
	try
	{
	    ResultSet rs = Utilities.getDbStatemement().executeQuery(selStatement);
	    while(rs.next())
	    {
		key = rs.getString("STATE");
		value = rs.getString("CONTRA_STATE");
		if (contraStates.keySet().contains(key))
		{
		    temp = contraStates.get(key);
		    temp += ","+value;
		    contraStates.put(key, temp);
		}
		else
		    contraStates.put(key, value);
	    }
	} catch (SQLException e)
	{
	    
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
    public void printDobStates(PrintStream p)
    {
	p.println(dobStates.toString());
    }
    public void loadContradictingActivities()
    {
	String selStatement ="SELECT \"ACTIVITY\", \"CONTRA_ACTIVITY\" FROM \"BPMN_GRAPH\".\"CONTRADICTING_ACTIVITIES\"";
	String key,value,temp;
	try
	{
	    ResultSet rs = Utilities.getDbStatemement().executeQuery(selStatement);
	    while(rs.next())
	    {
		key = rs.getString("ACTIVITY").toLowerCase();
		value = rs.getString("CONTRA_ACTIVITY");
		if (contraActivities.keySet().contains(key))
		{
		    temp = contraActivities.get(key);
		    temp += ","+value;
		    contraActivities.put(key, temp);
		}
		else
		    contraActivities.put(key, value);
	    }
	} catch (SQLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
    public void loadDataPreConditionsOfActivities()
    {
	String selStatement ="SELECT \"ACT_NAME\", \"PRE_CONDITION\" FROM \"BPMN_GRAPH\".\"ACTIVITY_PRE_CONDITION_DATA\"";
	String key,value,temp;
	try
	{
	    ResultSet rs = Utilities.getDbStatemement().executeQuery(selStatement);
	    while(rs.next())
	    {
		key = rs.getString("ACT_NAME").toLowerCase();
		if (!activities.contains(key))
		    activities.add(key);
		value = rs.getString("PRE_CONDITION");
		insertDataObjectState(value);
		
	        
		
		if (activitiesPreConditions.keySet().contains(key))
		{
		    temp = activitiesPreConditions.get(key);
		    temp += ","+value;
		    activitiesPreConditions.put(key, temp);
		}
		else
		    activitiesPreConditions.put(key, value);
	    }
	} catch (SQLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
    public void loadDataPostConditionsOfActivities()
    {
	String selStatement ="SELECT \"ACT_NAME\", \"POST_CONDITION\", \"CONDITION_TYPE\" FROM \"BPMN_GRAPH\".\"ACTIVITY_POST_CONDITION_DATA\"";
	String key,value,temp,condType;
	try
	{
	    ResultSet rs = Utilities.getDbStatemement().executeQuery(selStatement);
	    while(rs.next())
	    {
		condType = rs.getString("CONDITION_TYPE");
		key = rs.getString("ACT_NAME").toLowerCase();
		if (!activities.contains(key))
		    activities.add(key);
		value = rs.getString("POST_CONDITION");
		insertDataObjectState(value);
		if (condType.equals("p"))
		{
		    if (activitiesPostConditionsPositive.keySet().contains(key))
		    {
			temp = activitiesPostConditionsPositive.get(key);
			temp += ","+value;
			activitiesPostConditionsPositive.put(key, temp);
		    }
		    else
			activitiesPostConditionsPositive.put(key, value);
		}
		else
		{
		    if (activitiesPostConditionsNegative.keySet().contains(key))
		    {
			temp = activitiesPostConditionsNegative.get(key);
			temp += ","+value;
			activitiesPostConditionsNegative.put(key, temp);
		    }
		    else
			activitiesPostConditionsNegative.put(key, value);
		}
	    }
	} catch (SQLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
    private void insertDataObjectState(String dobState)
    {
	String valueA[] = dobState.split("and");
	String temp;
	for (String ss : valueA)
	{
	    String dob = Utilities.getDataObjectName(ss.trim());
	    String st = Utilities.getDataObjectState(ss.trim());
	    if (dobStates.keySet().contains(dob))
	        {
	            temp = dobStates.get(dob);
	            if (!temp.contains(st))
	            {
	        	temp += ","+st;
	        	dobStates.put(dob, temp);
	            }
	        }
	        else
	            dobStates.put(dob, st);
	}
    }
    private void loadDataObjectStatesURI(String modelURI)
    {
	if (!process.modelURI.equals(modelURI))
	    process.loadFromOryx(modelURI);
	String key,value,temp;
	for (DataObject dob : process.dataObjs)
	{
	    key = dob.name;
	    value = dob.getState();
	    if (dobStates.keySet().contains(key))
	    {
		temp = dobStates.get(key);
		temp += ","+value;
		dobStates.put(key, temp);
	    }
	    else
		dobStates.put(key, value);
	    System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
	    System.out.println("Loading Data Object "+dob.toString());
	    System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
	}
    }
    private void loadDataObjectStatesDB(String modelURI)
    {
	String selStatement = "SELECT replace(\"BPMN_GRAPH\".\"DATA_OBJECT\".\"NAME\",'\n','') as \"NAME\",\"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_NAME\" " +
	" FROM \"BPMN_GRAPH\".\"DATA_OBJECT\", \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\" WHERE \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
			"AND \"MODEL_ID\" ="+modelURI;
        String key,value,temp;
        try
        {
        ResultSet rs = Utilities.getDbStatemement().executeQuery(selStatement);
        while(rs.next())
        {
        key = rs.getString("NAME");
        value = rs.getString("STATE_NAME");
        if (dobStates.keySet().contains(key))
        {
            temp = dobStates.get(key);
            temp += ","+value;
            dobStates.put(key, temp);
        }
        else
            dobStates.put(key, value);
        }
        } catch (SQLException e)
        {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
    }
    public String getContradictingState(String state)
    {
	return contraStates.get(state);
    }
    public String getContradictingActivities(String act)
    {
	return contraActivities.get(act);
    }
    public String getDataObjectStates(String dob)
    {
	return dobStates.get(dob);
    }
    public void fullLoad()
    {
	if (Utilities.QUERY_PROCESSOR_TYPE.equals("DATABASE") )
	{
	    loadContradictingActivities();
	    loadContradictingStates();
	    loadDataPostConditionsOfActivities();
	    loadDataPreConditionsOfActivities();
	}
	else
	{
	    loadDefaultContradictingActivities();
	    loadDefaultContradictingStates();
	}
    }
    private void loadDefaultContradictingActivities()
    {
	// not needed so far
    }
    
    private void loadDefaultContradictingStates()
    {
	
	this.contraStates.put("RISK_high","RISK_low");
	this.contraStates.put("EXTRA EVALUATION_yes","EXTRA EVALUATION_no");
	this.contraStates.put("RATING_accepted","RATING_rejected");
	this.contraStates.put("EVALUATION_passed","EVALUATION_failed");
	this.contraStates.put("CERTIFICATE_valid","CERTIFICATE_invalid");
	this.contraStates.put("RISK_low","RISK_high");
	this.contraStates.put("EXTRA EVALUATION_no","EXTRA EVALUATION_yes");
	this.contraStates.put("RATING_rejected","RATING_accepted");
	this.contraStates.put("EVALUATION_failed","EVALUATION_passed");
	this.contraStates.put("CERTIFICATE_invalid","CERTIFICATE_valid");
	this.contraStates.put("PAYMENT_METHOD_bank","PAYMENT_METHOD_credit");
	this.contraStates.put("PAYMENT_METHOD_credit","PAYMENT_METHOD_bank");
	
    }
    public String getActivityDataPostConditionPositive(String actname)
    {
	return activitiesPostConditionsPositive.get(actname.toLowerCase());
    }
    public String getActivityDataPostConditionNegative(String actname)
    {
	return activitiesPostConditionsNegative.get(actname.toLowerCase());
    }
    public String getActivityDataPreCondition(String actname)
    {
	return activitiesPreConditions.get(actname.toLowerCase());
    }
    public List<String> getBusinessContextActivities()
    {
	return activities;
    }
    public List<String> getAllDataObjectStates()
    {
	List<String> dobs = new ArrayList<String>();
	Iterator<String> it = dobStates.keySet().iterator();
	while (it.hasNext())
	{
	    String dobName = it.next();
	    dobName = dobName.trim();
	    if (dobName.endsWith("_"))
		dobName = dobName.substring(0,dobName.length() -1);
	    if (dobStates.get(dobName) != null)
		for (String s: dobStates.get(dobName).split(","))
		{
		    if (!dobs.contains(dobName+"_"+s))
			dobs.add(dobName+"_"+s);
		}
	}
	return dobs;
    }
}
