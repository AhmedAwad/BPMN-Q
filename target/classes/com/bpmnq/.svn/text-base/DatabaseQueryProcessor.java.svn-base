package com.bpmnq;
import java.util.*;
import java.util.Date;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.*;

import org.apache.log4j.Logger;

import com.bpmnq.GraphObject.GraphObjectType;


public final class DatabaseQueryProcessor extends AbstractQueryProcessor {

    private Logger log = Logger.getLogger(DatabaseQueryProcessor.class);
    // Test

    public DatabaseQueryProcessor(PrintWriter answer)
    {
	super(answer);
	log.debug("Logging started at "+ getDateTime());
    }

    private String getDateTime() {
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	Date date = new Date();
	return dateFormat.format(date);
    }

    private String getGraphObjectName(String id, String model, GraphObjectType type)
    {
	try
	{
	    Statement stt = Utilities.connection.createStatement(
		    ResultSet.TYPE_SCROLL_INSENSITIVE,
		    ResultSet.CONCUR_UPDATABLE);
	    ResultSet tmp;

	    switch (type) {
	    case ACTIVITY:
		// tmp = stt.executeQuery("Select Name From Activity where id
		// ="+id + " and mod_id ="+model);
		tmp = stt.executeQuery("Select \"NAME\" From \"BPMN_GRAPH\".\"ACTIVITY\" where \"ID\" =" + id);// + " and mod_id ="+model);
		break;
	    case EVENT:
		// tmp = stt.executeQuery("Select Name From Event where id ="+id
		// + " and model_id ="+model);
		tmp = stt.executeQuery("Select \"NAME\" From \"BPMN_GRAPH\".\"EVENT\" where \"ID\" =" + id);// + " and model_id ="+model);
		break;
	    case GATEWAY:
		tmp = stt.executeQuery("Select \"NAME\" From \"BPMN_GRAPH\".\"GATEWAY\" where \"ID\" =" + id);// + " and model_id ="+model);
		break;
	    default:
		// unknown GraphObject type
		return "NULL";
	    }
	    if (tmp.next())
		return tmp.getString(1);
	    else
		return "NULL";
	} catch (SQLException e) {
	    log.error(e.getMessage(), e);
	    return "DBERROR";
	}
    }

    private String getGraphObjectType2(String id, String model, GraphObjectType type)
    {
	try
	{
	    Statement stt = Utilities.connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);	    
	    ResultSet tmp;
	    if (type == GraphObjectType.ACTIVITY)
	    {
		tmp = stt.executeQuery("Select '' as type2 From \"BPMN_GRAPH\".\"ACTIVITY\" where \"ID\" ="+id + " and \"MOD_ID\" ="+model);
	    }
	    else if (type == GraphObjectType.EVENT)
	    {
		tmp = stt.executeQuery("Select coalesce(\"EVE_TYPE\",'') || cast(\"EVE_POSITION\" as char) as type2 From \"BPMN_GRAPH\".\"EVENT\" where \"ID\" ="+id + " and \"MODEL_ID\" ="+model);
	    }
	    else // (type == GraphObjectType.GATEWAY)
	    {
		tmp = stt.executeQuery("Select \"GATE_WAY_TYPE\" as type2 From \"BPMN_GRAPH\".\"GATEWAY\" where \"ID\" ="+id + " and \"MODEL_ID\" ="+model);

	    }
	    if (tmp.next())
		return tmp.getString(1);
	    else
		return "NULL";	

	} catch(SQLException e)
	{
	    return "DBERROR";
	}
    }

    private List<GraphObject> getSuccessorsFromDB(GraphObject elem, 
	    String modelID, GraphObjectType successorType)
	    {
	List<GraphObject> succs = new ArrayList<GraphObject>();
	GraphObject succElem = new GraphObject();
//	String basicSelect = "Select isnull(to_act_id,0) as to_act_id ,isnull(to_eve_id,0) as to_eve_id,isnull(to_gat_id,0) as to_gat_id from SEQUENCE_FLOW where model_id="+modelID;
	String basicSelect = "Select coalesce(\"TO_ACT_ID\",0) as to_act_id ,coalesce(\"TO_EVE_ID\",0) as to_eve_id,coalesce(\"TO_GAT_ID\",0) as to_gat_id from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" ="+modelID;
	//String basicSelect = "Select to_act_id,to_eve_id,to_gat_id from SEQUENCE_FLOW where model_id="+modelID;

	switch(successorType) {
	case ACTIVITY:
	    basicSelect += " and \"TO_ACT_ID\" is not null";
	    break;
	case EVENT:
	    basicSelect += " and \"TO_EVE_ID\" is not null";
	    break;
	case GATEWAY:
	    basicSelect += " and \"TO_GAT_ID\" is not null";
	    break;
	}

	try
	{
	    if (elem.type == GraphObjectType.ACTIVITY )
	    {
		// added on 11 6 to handle unresolvable queries
		if (!elem.getID().equals("0"))
		    basicSelect += (" and \"FRM_ACT_ID\"="+elem.getID());
		else
		    basicSelect += (" and \"FRM_ACT_ID\" is not null");


		Utilities.rs = Utilities.st.executeQuery(basicSelect);
		// this will be changed to calls to the DB
		// we have to resolve the id of the activity
		while(Utilities.rs.next())
		{
		    String result1,result2,result3;
		    result1 = Utilities.rs.getString("to_act_id");
		    result2 = Utilities.rs.getString("to_eve_id");
		    result3 = Utilities.rs.getString("to_gat_id");

		    succElem = new GraphObject();
		    if (!result1.equals("0")) // an activity as target
		    {
			succElem.setID(result1);
			succElem.setName(getGraphObjectName(result1, modelID, GraphObjectType.ACTIVITY));
			succElem.type = GraphObjectType.ACTIVITY;
			succElem.type2 = getGraphObjectType2(result1, modelID, GraphObjectType.ACTIVITY);
		    }
		    else if (!result2.equals("0"))
		    {
			succElem.setID(result2);
			succElem.setName(getGraphObjectName(result2,modelID,GraphObjectType.EVENT));
			succElem.type = GraphObjectType.EVENT;
			succElem.type2 = getGraphObjectType2(result2,modelID,GraphObjectType.EVENT);;
		    }
		    else if (!result3.equals("0"))
		    {
			succElem.setID(result3);
			succElem.setName(getGraphObjectName(result3,modelID,GraphObjectType.GATEWAY));
			succElem.type = GraphObjectType.GATEWAY;
			succElem.type2 = getGraphObjectType2(result3,modelID,GraphObjectType.GATEWAY);//seqFlowList[i].toGateWay.gateWayType;
		    }
		    succs.add(succElem);

		}
	    }							
	    else if (elem.type == GraphObjectType.EVENT )
	    {
		if (!elem.getID().equals("0"))
		    basicSelect += (" and \"FRM_EVE_ID\"="+elem.getID());
		else
		    basicSelect += (" and \"FRM_EVE_ID\" is not null");
		//System.out.println(basicSelect);
		Utilities.rs = Utilities.st.executeQuery(basicSelect);

		while(Utilities.rs.next())
		{
		    String result1,result2,result3;
		    result1 = Utilities.rs.getString("to_act_id");
		    result2 = Utilities.rs.getString("to_eve_id");
		    result3 = Utilities.rs.getString("to_gat_id");
		    succElem = new GraphObject();
		    if (!result1.equals("0")) // an activity as target
		    {

			succElem.setID(result1);
			succElem.setName(getGraphObjectName(result1,modelID,GraphObjectType.ACTIVITY));
			succElem.type = GraphObjectType.ACTIVITY;
			succElem.type2 = getGraphObjectType2(result1,modelID,GraphObjectType.ACTIVITY);
		    }
		    else if (!result2.equals("0"))
		    {
			succElem.setID(result2);
			succElem.setName(getGraphObjectName(result2,modelID,GraphObjectType.EVENT));
			succElem.type = GraphObjectType.EVENT;
			succElem.type2 = getGraphObjectType2(result2,modelID,GraphObjectType.EVENT);
		    }
		    else if (!result3.equals("0"))
		    {
			succElem.setID(result3);
			succElem.setName(getGraphObjectName(result3,modelID,GraphObjectType.GATEWAY));
			succElem.type = GraphObjectType.GATEWAY;
			succElem.type2 = getGraphObjectType2(result3,modelID,GraphObjectType.GATEWAY);//seqFlowList[i].toGateWay.gateWayType;
		    }
		    succs.add(succElem);

		}
	    } else if (elem.type == GraphObjectType.GATEWAY )
	    {
		if (!elem.getID().equals("0"))
		    basicSelect += (" and \"FRM_GAT_ID\"="+elem.getID());
		else
		    basicSelect +=(" and \"FRM_GAT_ID\" is not null");

		Utilities.rs = Utilities.st.executeQuery(basicSelect);
		succElem = new GraphObject();
		while(Utilities.rs.next())
		{
		    String result1,result2,result3;
		    result1 = Utilities.rs.getString("to_act_id");
		    result2 = Utilities.rs.getString("to_eve_id");
		    result3 = Utilities.rs.getString("to_gat_id");
		    succElem = new GraphObject();
		    if (!result1.equals("0")) // an activity as target
		    {

			succElem.setID(result1);
			succElem.setName(getGraphObjectName(result1,modelID,GraphObjectType.ACTIVITY));
			succElem.type = GraphObjectType.ACTIVITY;
			succElem.type2 = getGraphObjectType2(result1,modelID,GraphObjectType.ACTIVITY);
		    }
		    else if (!result2.equals("0"))
		    {
			succElem.setID(result2);
			succElem.setName(getGraphObjectName(result2,modelID,GraphObjectType.EVENT));
			succElem.type = GraphObjectType.EVENT;
			succElem.type2 = getGraphObjectType2(result2,modelID,GraphObjectType.EVENT);
		    }
		    else if (!result3.equals("0"))
		    {
			succElem.setID(result3);
			succElem.setName(getGraphObjectName(result3,modelID,GraphObjectType.GATEWAY));
			succElem.type = GraphObjectType.GATEWAY;
			succElem.type2 = getGraphObjectType2(result3,modelID,GraphObjectType.GATEWAY);//seqFlowList[i].toGateWay.gateWayType;
		    }
		    succs.add(succElem);

		}
	    }

	} catch(SQLException e)	
	{
	    log.error("Method : getSuccessorsFromDB");
	    log.error(e.getMessage(), e);
	    log.error(e.getErrorCode());

	    log.error(basicSelect);
	}
	return succs;
	    }

    private List<GraphObject> getPredecessorsFromDB(GraphObject elem, 
	    String modelID, GraphObjectType predType)
	    {
	List<GraphObject> preds = new ArrayList<GraphObject>();
	GraphObject predElem = new GraphObject();
//	String basicSelect = "Select isnull(frm_act_id,0) as frm_act_id ,isnull(frm_eve_id,0) as frm_eve_id,isnull(frm_gat_id,0) as frm_gat_id from SEQUENCE_FLOW where model_id="+modelID;
	String basicSelect = "Select coalesce(\"FRM_ACT_ID\",0) as frm_act_id ,coalesce(\"FRM_EVE_ID\",0) as frm_eve_id,coalesce(\"FRM_GAT_ID\",0) as frm_gat_id from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" ="+modelID;
	//String basicSelect = "Select frm_act_id,frm_eve_id,frm_gat_id from SEQUENCE_FLOW where model_id="+modelID;
	try
	{

	    if (elem.type == GraphObjectType.ACTIVITY )
	    {
		if (!elem.getID().equals("0"))
//		    basicSelect += (" and to_act_id="+elem.id);
		    basicSelect += (" and \"TO_ACT_ID\"="+elem.getID());
		else
//		    basicSelect +=(" and to_act_id is not null");
		    basicSelect +=(" and \"TO_ACT_ID\" is not null");

		switch(predType) {
		case ACTIVITY:
//		    basicSelect += " and frm_act_id is not null";
		    basicSelect += " and \"FRM_ACT_ID\" is not null";
		    break;
		case EVENT:
//		    basicSelect += " and frm_eve_id is not null";
		    basicSelect += " and \"FRM_EVE_ID\" is not null";
		    break;
		case GATEWAY:
//		    basicSelect += " and frm_gat_id is not null";
		    basicSelect += " and \"FRM_GAT_ID\" is not null";
		    break;
		}

		Utilities.rs = Utilities.st.executeQuery(basicSelect);
		// this will be changed to calls to the DB
		// we have to resolve the id of the activity
		while(Utilities.rs.next())
		{
		    String result1,result2,result3;
		    result1 = Utilities.rs.getString("frm_act_id");
		    result2 = Utilities.rs.getString("frm_eve_id");
		    result3 = Utilities.rs.getString("frm_gat_id");

		    predElem = new GraphObject();
		    if (!result1.equals("0")) // an activity as target
		    {
			predElem.setID(result1);
			predElem.setName(getGraphObjectName(result1,modelID, GraphObjectType.ACTIVITY));
			predElem.type = GraphObjectType.ACTIVITY;
			predElem.type2 = getGraphObjectType2(result1,modelID, GraphObjectType.ACTIVITY);
		    }
		    else if (!result2.equals("0"))
		    {
			predElem.setID(result2);
			predElem.setName(getGraphObjectName(result2,modelID,GraphObjectType.EVENT));
			predElem.type = GraphObjectType.EVENT;
			predElem.type2 = getGraphObjectType2(result2,modelID,GraphObjectType.EVENT);
		    }
		    else if (!result3.equals("0"))
		    {
			predElem.setID(result3);
			predElem.setName(getGraphObjectName(result3,modelID,GraphObjectType.GATEWAY));
			predElem.type = GraphObjectType.GATEWAY;
			predElem.type2 = getGraphObjectType2(result2,modelID,GraphObjectType.EVENT);//seqFlowList[i].toGateWay.gateWayType;
		    }
		    preds.add(predElem);

		}
	    }							
	    else if (elem.type == GraphObjectType.EVENT )
	    {
		if (!elem.getID().equals("0"))
		    basicSelect += (" and \"TO_EVE_ID\"="+elem.getID());
		else
		    basicSelect +=(" and \"TO_EVE_ID\" is not null");

		switch(predType) {
		case ACTIVITY:
		    basicSelect += " and \"FRM_ACT_ID\" is not null";
		    break;
		case EVENT:
		    basicSelect += " and \"FRM_EVE_ID\" is not null";
		    break;
		case GATEWAY:
		    basicSelect += " and \"FRM_GAT_ID\" is not null";
		    break;
		}

		//System.out.println(basicSelect);
		Utilities.rs = Utilities.st.executeQuery(basicSelect);

		while(Utilities.rs.next())
		{
		    String result1,result2,result3;
		    result1 = Utilities.rs.getString("frm_act_id");
		    result2 = Utilities.rs.getString("frm_eve_id");
		    result3 = Utilities.rs.getString("frm_gat_id");
		    predElem = new GraphObject();
		    if (!result1.equals("0")) // an activity as target
		    {

			predElem.setID(result1);
			predElem.setName(getGraphObjectName(result1,modelID,GraphObjectType.ACTIVITY));
			predElem.type = GraphObjectType.ACTIVITY;
			predElem.type2 = getGraphObjectType2(result1,modelID,GraphObjectType.ACTIVITY);
		    }
		    else if (!result2.equals("0"))
		    {
			predElem.setID(result2);
			predElem.setName(getGraphObjectName(result2,modelID,GraphObjectType.EVENT));
			predElem.type = GraphObjectType.EVENT;
			predElem.type2 = getGraphObjectType2(result2,modelID,GraphObjectType.EVENT);
		    }
		    else if (!result3.equals("0"))
		    {
			predElem.setID(result3);
			predElem.setName(getGraphObjectName(result3,modelID,GraphObjectType.GATEWAY));
			predElem.type = GraphObjectType.GATEWAY;
			predElem.type2 = getGraphObjectType2(result3,modelID,GraphObjectType.GATEWAY);//seqFlowList[i].toGateWay.gateWayType;
		    }
		    preds.add(predElem);

		}
	    }

	    else if (elem.type == GraphObjectType.GATEWAY )
	    {
		if (!elem.getID().equals("0"))
		    basicSelect += (" and \"TO_GAT_ID\"="+elem.getID());
		else
		    basicSelect +=(" and \"TO_GAT_ID\" is not null");

		switch(predType) {
		case ACTIVITY:
		    basicSelect += " and \"FRM_ACT_ID\" is not null";
		    break;
		case EVENT:
		    basicSelect += " and \"FRM_EVE_ID\" is not null";
		    break;
		case GATEWAY:
		    basicSelect += " and \"FRM_GAT_ID\" is not null";
		    break;
		}

		Utilities.rs = Utilities.st.executeQuery(basicSelect);

		while(Utilities.rs.next())
		{
		    String result1,result2,result3;
		    result1 = Utilities.rs.getString("frm_act_id");
		    result2 = Utilities.rs.getString("frm_eve_id");
		    result3 = Utilities.rs.getString("frm_gat_id");
		    predElem = new GraphObject();
		    if (!result1.equals("0")) // an activity as target
		    {

			predElem.setID(result1);
			predElem.setName(getGraphObjectName(result1,modelID,GraphObjectType.ACTIVITY));
			predElem.type = GraphObjectType.ACTIVITY;
			predElem.type2 = getGraphObjectType2(result1,modelID,GraphObjectType.ACTIVITY);
		    }
		    else if (!result2.equals("0"))
		    {
			predElem.setID(result2);
			predElem.setName(getGraphObjectName(result2,modelID,GraphObjectType.EVENT));
			predElem.type = GraphObjectType.EVENT;
			predElem.type2 = getGraphObjectType2(result2,modelID,GraphObjectType.EVENT);
		    }
		    else if (!result3.equals("0"))
		    {
			predElem.setID(result3);
			predElem.setName(getGraphObjectName(result3,modelID,GraphObjectType.GATEWAY));
			predElem.type = GraphObjectType.GATEWAY;
			predElem.type2 = getGraphObjectType2(result3,modelID,GraphObjectType.GATEWAY);//seqFlowList[i].toGateWay.gateWayType;
		    }
		    preds.add(predElem);

		}
	    }

	}
	catch(SQLException e)	
	{
	    log.error("Database error. Could not find node predecessors. Results may be incorrect.", e);
//	    System.err.println("Method : getSuccessorsFromDB");
//	    System.err.println(e.getMessage());
//	    System.err.println(basicSelect);
	}
	return preds;
	    }

    private ProcessGraph findPathFromDB(GraphObject startElem, 
	    GraphObject endElem, String modelId, String exclude)
    {
	String selStatement = "";

	if (exclude.length() > 0)
	{
	    try
	    {
		String selStat = Utilities.prepareSQLExcludeMultiple(
			startElem.toString(), endElem.toString(), modelId, exclude);
		Utilities.getDbStatemement().execute("Select \"BPMN_GRAPH\".\"ProcCreator\"('"+selStat+"')");
	    }
	    catch(SQLException sqlex)
	    {
		log.error("Database error. Results may be incorrect!", sqlex);
	    }
	    selStatement = "select \"BPMN_GRAPH\".\"DO_AS_TOLD\"()";
	}
	else
	{
	    selStatement= "select \"BPMN_GRAPH\".get_path('"+startElem.toString() +"','"+ endElem.toString()+"',"+ modelId +",'*')";
	}

	ProcessGraph result = new ProcessGraph();
	GraphObject currentNode;
	int cnt = 0;
	try
	{
	    String token;
	    ResultSet rs = Utilities.getDbStatemement().executeQuery(selStatement);
	    while (rs.next())
	    {
		cnt++;
		//qry1matched = true;
		//strToken = new StringTokenizer(rs.getString("path"),",");
		StringTokenizer strToken = new StringTokenizer(rs.getString(1),",");
		while(strToken.hasMoreTokens())
		{
		    token = strToken.nextToken();
		    if (token.length() > 3)
		    {
			currentNode = new GraphObject();
			if (token.substring(0, 3).equals("ACT"))
			{
			    currentNode.type = GraphObjectType.ACTIVITY;
			    currentNode.setID(token.substring(3,token.length()));
			    currentNode.setName(getGraphObjectName(currentNode.getID(),modelId, GraphObjectType.ACTIVITY));
			    currentNode.type2 = getGraphObjectType2(currentNode.getID(),modelId, GraphObjectType.ACTIVITY);

			}
			else if (token.substring(0, 3).equals("EVE"))
			{
			    currentNode.type = GraphObjectType.EVENT;
			    currentNode.setID(token.substring(3,token.length()));
			    currentNode.setName(getGraphObjectName(currentNode.getID(),modelId, GraphObjectType.EVENT));
			    currentNode.type2 = getGraphObjectType2(currentNode.getID(),modelId, GraphObjectType.EVENT);
			} 
			else 
			{
			    currentNode.type = GraphObjectType.GATEWAY;
			    currentNode.setID(token.substring(3,token.length()));
			    currentNode.setName(getGraphObjectName(currentNode.getID(),modelId, GraphObjectType.GATEWAY));
			    currentNode.type2 = getGraphObjectType2(currentNode.getID(),modelId, GraphObjectType.GATEWAY);
			}
			result.add(currentNode);
		    }
		}
	    }
	}
	catch(SQLException ex)
	{
	    log.error("Database error. Could not retrieve a path. Results may be incorrect. count is " + cnt +" " +selStatement, ex);
	}

	result.constructEdges(modelId, Utilities.st);
	return result;
    }

    /**
     * 
     * @param startElem
     * @param endElem
     * @param ModelID
     * @return <code>false</code> when a path exists. <code>true</code> when no path exists.
     */
    protected boolean checkNegativePathFromDB(GraphObject startElem,
	    GraphObject endElem, String ModelID) // returns a ArrayList of paths
    {
	// selStatement = "EXEC GET_PATH '" + startElem.toString() + "',
	// '"+endElem.toString() + "' , " + ModelID ;
//	selStatement = "SELECT \"PATH\" ||',' || \"ENE\" FROM \"BPMN_GRAPH\".\"NEWP\" WHERE \"Model_Id\" = _MODEL_ID    AND \"BNI\" ="
//	+ startElem.toString()
//	+ "   and \"ENE\" ="
//	+ endElem.toString();

	String selStatement= "select \"BPMN_GRAPH\".get_path('"+startElem.toString() +"','"+ endElem.toString()+"',"+ ModelID +",'*')";
	try 
	{
	    ResultSet rs = Utilities.getDbStatemement().executeQuery(selStatement);
	    rs.next();
	    String result = rs.getString(1);
	    if (result.length() > 0)
		return false;
	    else
		return true;
	} catch (SQLException ex) {
	    log.error("Database error. Could not check a negative path. Results may be incorrect.", ex);
	}

	return false;
    }

    /**
     * this is a recursive function that keeps going as long as there 
     * are unknown nodes or paths
     */
    protected void resolveAnonymousActivities (String modelID)
    {
	if (this.intermediateRefinements.size()==0) return;
	List<GraphObject> AllActivities = null;

	do
	{
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	    boolean queryHasBeenRefined = false;
	    List<QueryGraph> refinedQueries;
	    //refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    refinedQueries = new ArrayList<QueryGraph>();
	    QueryGraph refinement;
	    List<GraphObject> succsFromQueryGraph;
	    List<GraphObject> predsFromQueryGraph;
	    //List<GraphObject> pathSuccsFromQueryGraph;
	    //List<GraphObject> pathPredsFromQueryGraph;
	    List<GraphObject> intersectionResult= new ArrayList<GraphObject>();
	    List<GraphObject> tmp;

	    //log.debug("CALLING RESOLVE EDGES");
	    for (GraphObject currentNode : query.nodes)
	    {
		if (!currentNode.getName().startsWith("@")) {
		    // this is not a variable activity, so simply proceed to the next node
		    continue;
		}

		predsFromQueryGraph = query.getPredecessorsFromQueryGraph(currentNode);
		succsFromQueryGraph = query.getSuccessorsFromQueryGraph(currentNode);
		if (predsFromQueryGraph.size()> 0)
		{
		    tmp = getSuccessorsFromDB(predsFromQueryGraph.get(0), modelID, currentNode.type);
		    intersectionResult = Utilities.intersect(tmp, tmp);

		    //	iterate to find the common nodes
		    for (GraphObject pred : predsFromQueryGraph)
		    {
			if (intersectionResult.size() == 0)
			    break;
			tmp = getSuccessorsFromDB(pred, modelID, currentNode.type);
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    }
		    // a variable node fails to resolve so terminate this query
		    if (intersectionResult.size() == 0) 
			//return ;
			resolveAnonymousActivities(modelID);
		}
		if (succsFromQueryGraph.size() > 0)
		{
		    tmp = getPredecessorsFromDB(succsFromQueryGraph.get(0), modelID, currentNode.type);
		    if (intersectionResult.size() > 0)
			// as i reach here with intersection result empty, this means i didnt find predecessors in query graph
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    else
			intersectionResult = Utilities.intersect(tmp, tmp);

		    //	iterate to find the common nodes
		    for (GraphObject succ : predsFromQueryGraph)
		    {
			if (intersectionResult.size() == 0)
			    break;
			tmp = getPredecessorsFromDB(succ, modelID, currentNode.type);
			intersectionResult = Utilities.intersect(intersectionResult, tmp);
		    }
		    if (intersectionResult.size() == 0) 
			//return;
			resolveAnonymousActivities(modelID);
		}
		// no sequence flow edges at all either incoming or outgoing
		if (predsFromQueryGraph.size()==0 && succsFromQueryGraph.size()==0)
		{ // we have to try out all activities
		    if (AllActivities == null)
		    {
			AllActivities = getAllActivityNodesOfAModel(modelID);
		    }

		    intersectionResult.clear();
		    intersectionResult.addAll(AllActivities);
		}

		queryHasBeenRefined = true;

		// the big cost testing path expressions
		//pathPredsFromQueryGraph = query.getPathPredecessorsFromQueryGraph(currentNode);
		//pathSuccsFromQueryGraph = query.getPathSuccessorsFromQueryGraph(currentNode);

		// Arriving here means we have to replace the unknown node with those nodes in the intersection result
		// in the edge we remove the ones that have the unknown node as either source or desitnation
		// and add corresponding resolved edges

		// we have to filter the intersection result to ensure the 
		// removal of ids that are in the forbidden list

		for (GraphObject currObj : intersectionResult)
		    if (query.forbiddenActivityIDs.toString().contains(currObj.getID()))
			currObj.setName(currObj.getName() + "$$");

		for (GraphObject currObj : intersectionResult)
		{
		    // for each of the suggested matched create a new 
		    // instance of the query graph
		    // 
		    if (currObj.getName().endsWith("$$")) 
			continue;
		    refinement = (QueryGraph)query.clone();
		    //refinement.removeEdgesWithDestination(currentNode);
		    //refinement.removeEdgesWithSource(currentNode);
		    refinement.remove(currentNode);
		    //refinement.removePathsWithDestination(currentNode);
		    //refinement.removePathsWithSource(currentNode);

		    // we have to update negative edges and path links

		    if (!currObj.getName().endsWith("$$"))
		    {
			currObj.setBoundQueryObjectID(currentNode.getID());
			refinement.add(currObj);
			refinement.addInfoLog("Variable node "+ currentNode.getName() + " was bound to node " + currObj);
			currentNode.setID(currObj.getID());
//			update the forbidden ids
			refinement.forbiddenActivityIDs.append(", " + currObj.getID()); 
			refinement.updateNegativeEdgesWithDestination(currentNode, currObj.getName());
			refinement.updateNegativePathsWithDestination(currentNode, currObj.getName());
			refinement.updateNegativeEdgesWithSource(currentNode, currObj.getName());
			refinement.updateNegativePathsWithSource(currentNode, currObj.getName());

			refinement.updateEdgesWithDestination(currentNode, currObj.getName());
			refinement.updateEdgesWithSource(currentNode, currObj.getName());

			refinement.updatePathsWithDestination(currentNode, currObj.getName());
			refinement.updatePathsWithSource(currentNode, currObj.getName());
		    }

//		    for (int n = 0;n < predsFromQueryGraph.size();n++ )
//		    if (!intersectionResult.get(z).name.endsWith("$$"))

//		    refinement.addEdge(predsFromQueryGraph.get(n), intersectionResult.get(z));

//		    for (int n = 0;n < succsFromQueryGraph.size();n++ )

//		    if (!intersectionResult.get(z).name.endsWith("$$"))
//		    refinement.addEdge(intersectionResult.get(z),succsFromQueryGraph.get(n));
//		    // now we have to add edges and nodes that are matched from the path expressions
//		    for (int n = 0;n < pathPredsFromQueryGraph.size();n++ )

//		    if (!intersectionResult.get(z).name.endsWith("$$"))
//		    if (pathPredsFromQueryGraph.get(n).name.equals(currentNode.name))
//		    refinement.addPath(intersectionResult.get(z), intersectionResult.get(z));
//		    else
//		    refinement.addPath(pathPredsFromQueryGraph.get(n), intersectionResult.get(z));

//		    for (int n = 0;n < pathSuccsFromQueryGraph.size();n++ )

//		    if (!intersectionResult.get(z).name.endsWith("$$"))
//		    if (pathSuccsFromQueryGraph.get(n).name.equals(currentNode.name))
//		    refinement.addPath(intersectionResult.get(z),intersectionResult.get(z));
//		    else
//		    refinement.addPath(intersectionResult.get(z),pathSuccsFromQueryGraph.get(n));

		    // added on 9th of July 2008 
		    refinement.updateExcludeExpression(currentNode.getName(), currentNode.toString());
		    refinedQueries.add(refinement);
		}
		if (refinedQueries.size()> 0)
		    this.intermediateRefinements.addAll(0,refinedQueries);
		else
		{
		    // at any time a variable node fails to be resolved
		    // the whole query is terminated with failure.
		    log.error("Variable node "+ currentNode.getName() +" failed to find a binding");
		    log.error("Terminating the query processing against model " + modelID + " due to unbound objects");
		    this.intermediateRefinements.clear();
		    this.finalRefinements.clear();
		    return;
		}
		break;
	    }
	    if (!queryHasBeenRefined)
		this.finalRefinements.add(query);
	}
	while (this.intermediateRefinements.size() > 0);
//	resolveVariableNodes(modelID);
    }

    private GraphObject getOriginalSourceNode(SequenceFlow currentEdge)
    {
	GraphObject source = null;
	if (currentEdge.frmActivity != null) {
	    source = currentEdge.frmActivity.originalNode();
	} else if (currentEdge.frmEvent != null) {
	    source = currentEdge.frmEvent.originalNode();
	} else if (currentEdge.frmGateWay != null) {
	    source = currentEdge.frmGateWay.originalNode();
	}
	return source;
    }

    private GraphObject getOriginalDestNode(SequenceFlow currentEdge)
    {
	GraphObject destination = null;
	if (currentEdge.toActivity != null) {
	    destination = currentEdge.toActivity.originalNode();
	} else if (currentEdge.toEvent != null) {
	    destination = currentEdge.toEvent.originalNode();
	} else if (currentEdge.toGateWay != null) {
	    destination = currentEdge.toGateWay.originalNode();
	}
	return destination;
    }

    /**
     * this method must be called after the resolve edges method
     * (for whatever reasons?!)
     */
    protected boolean resolvePaths(QueryGraph query, String modelID)
    {
	if (query.paths.size() ==0) 
	    return true;
	//System.out.println("CALLING RESOLVE PATHS");
	boolean pathFound = false;
	Path currentEdge;
	ProcessGraph currentPath;

	int sz7 = query.paths.size();
	for (int i = 0; i < sz7;i++) {
	    currentEdge = query.paths.get(i);
	    GraphObject source = getOriginalSourceNode(currentEdge);
	    GraphObject destination = getOriginalDestNode(currentEdge);

	    // Now test the existence of a path
	    currentPath = findPathFromDB(source, destination, modelID, currentEdge.exclude);
	    if (currentPath.nodes.size()==0)
	    {
		query.addErrorLog("A path edge checking between "+ source.getName() + " and " + destination.getName() + " failed!");
		return false;
	    } else
	    {
		// we have to remove this matched edge
		// insert nodes and edges from the matched path
		// call it one more time
		query.paths.remove(i);
		pathFound = true;
		// Optimization step added on 7th July 2007
		int sz8 = currentPath.nodes.size();
		for (int h = 0; h < sz8;h++)
		{
		    query.add(currentPath.nodes.get(h));

		}
		int sz9 = currentPath.edges.size();
		for (int g=0; g < sz9; g++)
		{
		    query.add(currentPath.edges.get(g));
		}
	    }
	    break;
	}
	if (pathFound) return resolvePaths(query, modelID);
	return false;
    }

    /**
     * this method must be called after the resolve edges method
     * (for whatever reasons?!)
     */
    protected boolean checkNegativePaths(QueryGraph query, String modelID)
    {
	if (query.negativePaths.size() ==0) 
	    return true;
	//System.out.println("CALLING RESOLVE PATHS");
	GraphObject source = new GraphObject();
	GraphObject destination = new GraphObject();
	SequenceFlow currentEdge;
	int sz10 = query.negativePaths.size();
	for (int i = 0; i < sz10; i++) {
	    currentEdge = query.negativePaths.get(i);
	    source = getOriginalSourceNode(currentEdge);
	    destination = getOriginalDestNode(currentEdge);

	    // Now test the existence of a path
	    if (checkNegativePathFromDB(source, destination, modelID) == false)
	    {
		query.addErrorLog("A negative path checking between " + source.getName() + " and "+ destination.getName() + " failed!");
		return false;
	    }
	}

	return true;
    }

    protected boolean resolveConcreteNodeID(QueryGraph query, String modelID)
    {
	//log.debug("Begin Resolve Concrete Node ID");
	// This is used to resolve the ID of known activity nodes
	// i.e activity nodes whose names are not in the form  @*

	for (GraphObject currentNode : query.nodes)
	{
	    if ( currentNode.type != GraphObjectType.ACTIVITY) 
		continue;

	    if (!currentNode.getName().startsWith("@")) // this is a concrete node
	    {
//		selExp = "Select id from activity where mod_id =" + modelID + " and upper(name)=upper('" + currentNode.name + "')";
		String selExp = "Select \"ID\" from \"BPMN_GRAPH\".\"ACTIVITY\" where \"MOD_ID\" =" + modelID + " and trim(upper(\"NAME\"))=trim(upper('" + currentNode.getName() + "'))";
		try
		{
		    ResultSet lrs = Utilities.st.executeQuery(selExp);
		    while (lrs.next())
		    {
			query.addInfoLog("Concrete node " + currentNode.getName() + " was bound to activity " + lrs.getString("id"));
			// added to track the binding from the query to the process graph object
			currentNode.setBoundQueryObjectID(currentNode.getID());
			currentNode.setID(lrs.getString("id"));

			// we have to update all paths, edges in which this node is incident
			query.forbiddenActivityIDs.append("," + currentNode.getID());
			query.updateEdgesWithDestination(currentNode,currentNode.getName());
			query.updateEdgesWithSource(currentNode,currentNode.getName());
			query.updatePathsWithDestination(currentNode,currentNode.getName());
			query.updatePathsWithSource(currentNode,currentNode.getName());

			// do the same thing with negative edges and paths
			query.updateNegativeEdgesWithDestination(currentNode,currentNode.getName());
			query.updateNegativeEdgesWithSource(currentNode,currentNode.getName());
			query.updateNegativePathsWithDestination(currentNode,currentNode.getName());
			query.updateNegativePathsWithSource(currentNode,currentNode.getName());
			this.finalRefinements.add(query);

		    }
		}
		catch (SQLException ex)
		{
		    log.error("Database error. Could not resolve a node ID. Results may be incorrect.", ex);
		}
	    }
	}
	//log.debug("End Resolve Concrete Node ID");
	return true;
    }

    //public ArrayList<QueryGraph> resolveEventNode(QueryGraph query, int modelID)
    protected void resolveEventNode(String modelID)
    {
	// We need to adjust the resolution technique, to reduce the number
	// of alternatives for the query graph. 25th May, 2007
	// the idea is to add something called the forbiden Id list

	//log.debug("Begin Resolve Event Node ID");
	if (0 == this.intermediateRefinements.size()) 
	    return ;
	QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	boolean queryHasBeenRefined = false;
	//ArrayList<ArrayList<QueryGraph>> refinedQueriesX;
	List<QueryGraph> refinedQueries;
	//refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	refinedQueries = new ArrayList<QueryGraph>();
	QueryGraph refinement;
	GraphObject currentNode;
	String selExp;
	ResultSet lrs;
	List<GraphObject> preds;
	List<GraphObject> succs;

	// Optimization step added on 7th July 2007
	int sz12 = query.nodes.size();
	for (int i = 0; i < sz12; i++)
	{
	    currentNode = query.nodes.get(i);
	    if (currentNode.type != GraphObjectType.EVENT) 
		continue;
	    if (currentNode.isResolved() ) 
		continue;
	    // reaching the following line means that there are still unresolved event nodes
	    //refinedQueries = new ArrayList<QueryGraph>();
	    queryHasBeenRefined = true;
//	    selExp = "Select id,name from event where model_id =" + modelID;// + ""
	    selExp = "Select \"ID\",\"NAME\" from \"BPMN_GRAPH\".\"EVENT\" where \"MODEL_ID\"=" + modelID;// + ""
//	    selExp += " and id not in (" + query.forbiddenEventIDs +") ";
	    selExp += " and \"ID\" not in (" + query.forbiddenEventIDs +") ";

	    if (!currentNode.getName().startsWith("$#"))
//		selExp += " and upper(name) = upper('" + currentNode.name+ "') ";
		selExp += " and upper(\"NAME\") = upper('" + currentNode.getName()+ "') ";
	    //selExp += " and ucase(name) = ucase('" + currentNode.name+ "') ";
	    if (currentNode.type2.endsWith("1"))
//		selExp += " and eve_position=1 ";
		selExp += " and \"EVE_POSITION\"=1 ";
	    else if (currentNode.type2.endsWith("2"))
		selExp += " and \"EVE_POSITION\"=2 ";
	    else if (currentNode.type2.endsWith("3"))
		selExp += " and \"EVE_POSITION\"=3 ";

	    if (currentNode.type2.length() >1)
		selExp +=" and upper(\"EVE_TYPE\")=upper('" + currentNode.type2.substring(0, currentNode.type2.length()-1)+ "') ";
	    //selExp +=" and ucase(eve_type)=ucase('" + currentNode.type2.substring(0, currentNode.type2.length()-1)+ "') ";

	    /*if (currentNode.name.length() >0)
				selExp += " and upper(name)= upper('" + currentNode.name + "')";
	     */
	    // use edges and paths to reduce the matching ids
	    preds = query.getPredecessorsFromQueryGraph(currentNode);
	    // Optimization step added on 7th July 2007
	    int sz13 = preds.size();
	    for (int j=0; j < sz13;j++)
	    {
		if (preds.get(j).type == GraphObjectType.ACTIVITY)
		{
		    if (preds.get(j).isResolved())
//			selExp += " and \"ID\" in (select \"TO_EVE_ID\" from sequence_flow where model_id =" + modelID + " and frm_act_id ="+ preds.get(j).id+ ")";
			selExp += " and \"ID\" in (select \"TO_EVE_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"FRM_ACT_ID\" ="+ preds.get(j).getID()+ ")";
		    else
//			selExp += " and id in (select to_eve_id from sequence_flow where model_id =" + modelID + " and frm_act_id is not null)";
			selExp += " and \"ID\" in (select \"TO_EVE_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"FRM_ACT_ID\" is not null)";
		}
		else if (preds.get(j).type == GraphObjectType.GATEWAY)
		{
//		    selExp += " and id in (select to_eve_id from sequence_flow where model_id =" + modelID + " and frm_gat_id is not null";
		    selExp += " and \"ID\" in (select \"TO_EVE_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"FRM_GAT_ID\" is not null";
//		    selExp += " and frm_gat_id in (select id from gateway where upper(gate_way_type)=upper('"+ preds.get(j).type2 +"')))";
		    selExp += " and \"FRM_GAT_ID\" in (select \"ID\" from \"BPMN_GRAPH\".\"GATEWAY\" where upper(\"GATE_WAY_TYPE\")=upper('"+ preds.get(j).type2 +"')))";
		    //selExp += " and frm_gat_id in (select id from gateway where ucase(gate_way_type)=ucase('"+ preds.get(j).type2 +"'))";
		}
		// I didnt want to put another else here as it is meaning less to connect event to event
	    }
	    succs = query.getSuccessorsFromQueryGraph(currentNode);
	    // Optimization step added on 7th July 2007
	    int sz14 = succs.size();
	    for (int j=0; j < sz14;j++)
	    {
		if (succs.get(j).type == GraphObjectType.ACTIVITY)
		{
		    if (succs.get(j).isResolved())
//			selExp += " and id in (select frm_eve_id from sequence_flow where model_id =" + modelID + " and to_act_id ="+ succs.get(j).id+ ")";
			selExp += " and \"ID\" in (select \"FRM_EVE_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"TO_ACT_ID\" ="+ succs.get(j).getID()+ ")";
		    else
//			selExp += " and id in (select frm_eve_id from sequence_flow where model_id =" + modelID + " and to_act_id is not null)";
			selExp += " and \"ID\" in (select \"FRM_EVE_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"TO_ACT_ID\" is not null)";
		}
		else if (succs.get(j).type == GraphObjectType.GATEWAY)
		{
//		    selExp += " and id in (select frm_eve_id from sequence_flow where model_id =" + modelID + " and to_gat_id is not null";
		    selExp += " and \"ID\" in (select \"FRM_EVE_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"TO_GAT_ID\" is not null";
//		    selExp += " and to_gat_id in (select id from gateway where upper(gate_way_type)=upper('"+ succs.get(j).type2 +"')))";
		    selExp += " and \"TO_GAT_ID\" in (select \"ID\" from \"BPMN_GRAPH\".\"GATEWAY\" where upper(\"GATE_WAY_TYPE\")=upper('"+ succs.get(j).type2 +"')))";
		    //selExp += " and to_gat_id in (select id from gateway where ucase(gate_way_type)=ucase('"+ preds.get(j).type2 +"'))";
		}
		// I didnt want to put another else here as it is meaning less to connect event to event
	    }
	    try
	    {
		lrs = Utilities.st.executeQuery(selExp);
		while (lrs.next())
		{
		    // we have to create a refinement for each event id
//		    added to track the binding from the query to the process graph object
		    currentNode.setBoundQueryObjectID(currentNode.getID());
		    currentNode.setID(lrs.getString("id"));
		    //currentNode.name = lrs.getString("name");
		    refinement = (QueryGraph)query.clone();
		    refinement.addInfoLog("Intermediate event " + currentNode.getName() + " was bound event " + lrs.getString("id"));
		    refinement.nodes.get(i).setID(lrs.getString("ID"));
		    refinement.nodes.get(i).setName(lrs.getString("NAME"));
		    // Update the forbidden ids list
		    refinement.forbiddenEventIDs.append(", " + currentNode.getID());
		    //query.nodes.get(i).id = lrs.getString("id");
		    // we have to update all paths, edges in which this node is incident
		    refinement.updateEdgesWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateEdgesWithSource(currentNode,refinement.nodes.get(i).getName());
		    refinement.updatePathsWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updatePathsWithSource(currentNode,refinement.nodes.get(i).getName());

		    // DO the same thing with negatives
		    refinement.updateNegativeEdgesWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateNegativeEdgesWithSource(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateNegativePathsWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateNegativePathsWithSource(currentNode,refinement.nodes.get(i).getName());
		    refinedQueries.add(refinement);
		}
		//for (int u =0; u < refinedQueries.size();u++)
		if (refinedQueries.size()> 0)
		    this.intermediateRefinements.addAll(0,refinedQueries);
		else
		{
		    // at any time an event fails to be resolved
		    // the whole query is terminated with failure.
		    log.error("Intermediate event "+ currentNode.getName() +" failed to find a binding");
		    log.error("Terminating the query processing against model " + modelID + " due to unbound objects");

		    this.intermediateRefinements.clear();
		    this.finalRefinements.clear();

		    return ;
		}
	    }
	    catch (SQLException ex)
	    {
		log.error(ex.getMessage());
		log.error(selExp);
	    }
	    //refinedQueriesX.add(refinedQueries);
	    break;
	}
	// here we have a list of versions for each event node
	// we have to have the cartesian product
	// the order is inherint in the order of lists of refined queries

	if (!queryHasBeenRefined)
	    this.finalRefinements.add(query);
	resolveEventNode(modelID);
	//log.debug("End Resolve Event Node ID");
    }

    private List<GraphObject> getAllActivityNodesOfAModel(String modelID) 
    {
	List<GraphObject> result = new ArrayList<GraphObject>();
	try {
	    ResultSet lrs = Utilities.executePrepQuery("Select \"ID\",\"NAME\" from \"BPMN_GRAPH\".\"ACTIVITY\" where \"MOD_ID\" = ?", 
		    modelID);
	    while (lrs.next()) {
		GraphObject node = new GraphObject();
		node.setID(lrs.getString("ID"));
		node.setName(lrs.getString("NAME"));
		node.type = GraphObjectType.ACTIVITY;
		node.type2 = "";
		result.add(node);
	    }
	    // for (int u =0; u < refinedQueries.size();u++)

	} catch (SQLException ex) {
	    log.error("Database error. Could not get activity info. Results may be incorrect.", ex);
	}
	return result;
    }

    private List<GraphObject> getAllGateWayNodesOfAModel(String modelID, String NodeType) {
	List<GraphObject> result = new ArrayList<GraphObject>();
	String selExp = "Select \"ID\",\"NAME\" ,\"GATE_WAY_TYPE\" from \"BPMN_GRAPH\".\"GATEWAY\" where \"GATE_WAY_TYPE\" like '%" + NodeType +"' AND \"MODEL_ID\" ="
	+ modelID;
	try {
	    ResultSet lrs = Utilities.st.executeQuery(selExp);
	    while (lrs.next()) {
		GraphObject node = new GraphObject();
		node.setID(lrs.getString("ID"));
		node.setName(lrs.getString("NAME"));
		node.type = GraphObjectType.GATEWAY;
		node.type2 = lrs.getString("GATE_WAY_TYPE");
		result.add(node);

	    }
	    // for (int u =0; u < refinedQueries.size();u++)

	} catch (SQLException ex) {
	    log.error("Database error. Could not get gateway info. Results may be incorrect.", ex);
	}
	return result;
    }

    private List<GraphObject> getAllIntermediateEventNodesOfAModel(String modelID) {
	List<GraphObject> result = new ArrayList<GraphObject>();
	String selExp = "Select \"ID\",\"NAME\" from \"BPMN_GRAPH\".\"EVENT\" where \"EVE_POSITION\"= 2 and \"MODEL_ID\" ="
	    + modelID;
	try
	{
	    ResultSet lrs = Utilities.getDbStatemement().executeQuery(selExp);
	    while (lrs.next()) {
		GraphObject node = new GraphObject();
		node.setID(lrs.getString("ID"));
		node.setName(lrs.getString("NAME"));
		node.type = GraphObjectType.EVENT;
		node.type2 = "2";
		result.add(node);
	    }
	    // for (int u =0; u < refinedQueries.size();u++)
	} catch (SQLException ex) {
	    log.error("Database error. Could not get event info. Results may be incorrect.", ex);
	}
	return result;
    }

    protected void resolveGateWayNode(String modelID)
    {
	//log.debug("Begin Resolve Gateway Node ID");
	if (this.intermediateRefinements.size()==0) return;
	QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	boolean queryHasBeenRefined = false;
	//ArrayList<ArrayList<QueryGraph>> refinedQueriesX;
	List<QueryGraph> refinedQueries;
	//refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	refinedQueries = new ArrayList<QueryGraph>();
	QueryGraph refinement;
	GraphObject currentNode;
	String selExp;
	ResultSet lrs;
	List<GraphObject> preds;
	List<GraphObject> succs;

//	Optimization step added on 7th July 2007
	int sz71 = query.nodes.size();
	for (int i = 0; i < sz71; i++)
	{
	    currentNode = query.nodes.get(i);
	    if (currentNode.type != GraphObjectType.GATEWAY) 
		continue;
	    if (currentNode.isResolved() ) 
		continue;
	    if (currentNode.type2.startsWith("GENERIC")) 
		continue;
	    // reaching the following line means that there are still unresolved event nodes
	    //refinedQueries = new ArrayList<QueryGraph>();
	    queryHasBeenRefined = true;
//	    selExp = "Select id,name from gateway where model_id =" + modelID;// + ""
	    selExp = "Select \"ID\",\"NAME\" from \"BPMN_GRAPH\".\"GATEWAY\" where \"MODEL_ID\" =" + modelID;// + ""
//	    selExp += " and id not in (" + query.forbiddenGatewayIDs +") ";
	    selExp += " and \"ID\" not in (" + query.forbiddenGatewayIDs +") ";
//	    if (!currentNode.getName().startsWith("$#"))
////	    selExp += " and upper(name) =upper('" + currentNode.name+ "') ";
//	    selExp += " and upper(\"NAME\") =upper('" + currentNode.getName()+ "') ";
//	    //selExp += " and ucase(name) =ucase('" + currentNode.name+ "') ";
	    if (currentNode.type2.length() >1)
//		selExp +=" and upper(gate_way_type)=upper('" + currentNode.type2+ "') ";
		selExp +=" and upper(\"GATE_WAY_TYPE\")=upper('" + currentNode.type2+ "') ";
	    //selExp +=" and ucase(gate_way_type)=ucase('" + currentNode.type2+ "') ";
	    /*if (currentNode.name.length() >0)
				selExp += " and upper(name)= upper('" + currentNode.name + "')";
	     */
	    // use edges and paths to reduce the matching ids
	    preds = query.getPredecessorsFromQueryGraph(currentNode);
	    // Optimization step added on 7th July 2007
	    int sz72 = preds.size();
	    for (int j=0; j < sz72;j++)
	    {
		if (preds.get(j).type == GraphObjectType.ACTIVITY)
		{
		    if (preds.get(j).isResolved())
//			selExp += " and id in (select to_gat_id from sequence_flow where model_id =" + modelID + " and frm_act_id ="+ preds.get(j).id+ ")";
			selExp += " and \"ID\" in (select \"TO_GAT_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"FRM_ACT_ID\" ="+ preds.get(j).getID()+ ")";
		    else
//			selExp += " and id in (select to_gat_id from sequence_flow where model_id =" + modelID + " and frm_act_id is not null)";
			selExp += " and \"ID\" in (select \"TO_GAT_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"FRM_ACT_ID\" is not null)";
		}
		else if (preds.get(j).type == GraphObjectType.GATEWAY)
		{
		    // some gatways might have been resolved
		    if (preds.get(j).isResolved())
//			selExp += " and id in (select to_gat_id from sequence_flow where model_id =" + modelID + " and frm_gat_id ="+ preds.get(j).id+ ")";
			selExp += " and \"ID\" in (select \"TO_GAT_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"FRM_GAT_ID\" ="+ preds.get(j).getID()+ ")";
		    else
		    {
//			selExp += " and id in (select to_gat_id from sequence_flow where model_id =" + modelID + " and frm_gat_id is not null";
			selExp += " and \"ID\" in (select \"TO_GAT_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"FRM_GAT_ID\" is not null";
//			selExp += " and frm_gat_id in (select id from gateway where upper(gate_way_type)=upper('"+ preds.get(j).type2 +"')))";
			selExp += " and \"FRM_GAT_ID\" in (select \"ID\" from \"BPMN_GRAPH\".\"GATEWAY\" where upper(\"GATE_WAY_TYPE\")=upper('"+ preds.get(j).type2 +"')))";
			//selExp += " and frm_gat_id in (select id from gateway where ucase(gate_way_type)=ucase('"+ preds.get(j).type2 +"'))";
		    }
		}
		else if (preds.get(j).type == GraphObjectType.EVENT)
		{
		    // Now all event nodes have been resolved
//		    selExp += " and id in (select to_gat_id from sequence_flow where model_id =" + modelID + " and frm_eve_id ="+ preds.get(j).id+ ")";
		    selExp += " and \"ID\" in (select \"TO_GAT_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"FRM_EVE_ID\"="+ preds.get(j).getID()+ ")";
		}

	    }

	    succs = query.getSuccessorsFromQueryGraph(currentNode);
	    // Optimization step added on 7th July 2007
	    int sz73 = succs.size();
	    for (int j=0; j < sz73;j++)
	    {
		if (succs.get(j).type == GraphObjectType.ACTIVITY)
		{
		    if (succs.get(j).isResolved())
//			selExp += " and id in (select frm_gat_id from sequence_flow where model_id =" + modelID + " and to_act_id ="+ succs.get(j).id+ ")";
			selExp += " and \"ID\" in (select \"FRM_GAT_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"TO_ACT_ID\" ="+ succs.get(j).getID()+ ")";
		    else
//			selExp += " and id in (select frm_gat_id from sequence_flow where model_id =" + modelID + " and to_act_id is not null)";
			selExp += " and \"ID\" in (select \"FRM_GAT_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"TO_ACT_ID\" is not null)";
		}
		else if (succs.get(j).type == GraphObjectType.GATEWAY)
		{
		    if (succs.get(j).isResolved())
//			selExp += " and id in (select frm_gat_id from sequence_flow where model_id =" + modelID + " and to_gat_id ="+ succs.get(j).id+ ")";
			selExp += " and \"ID\" in (select \"FRM_GAT_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"TO_GAT_ID\" ="+ succs.get(j).getID()+ ")";
		    else
		    {
//			selExp += " and id in (select frm_gat_id from sequence_flow where model_id =" + modelID + " and to_gat_id is not null";
			selExp += " and \"ID\" in (select \"FRM_GAT_ID\" from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" where \"MODEL_ID\" =" + modelID + " and \"TO_GAT_ID\" is not null";
//			selExp += " and to_gat_id in (select id from gateway where upper(gate_way_type)=upper('"+ succs.get(j).type2 +"')))";
			selExp += " and \"TO_GAT_ID\" in (select \"ID\" from \"BPMN_GRAPH\".\"GATEWAY\" where upper(\"GATE_WAY_TYPE\")=upper('"+ succs.get(j).type2 +"')))";
			//selExp += " and to_gat_id in (select id from gateway where ucase(gate_way_type)=ucase('"+ succs.get(j).type2 +"'))";
		    }
		}
		// I didnt want to put another else here as it is meaning less to connect event to event
	    }
	    try
	    {
		lrs = Utilities.st.executeQuery(selExp);
		while (lrs.next())
		{
//		    added to track the binding from the query to the process graph object
		    currentNode.setBoundQueryObjectID(currentNode.getID());
		    // we have to create a refinement for each event id
		    currentNode.setID(lrs.getString("id"));
		    //currentNode.name = lrs.getString("name");
		    refinement = (QueryGraph)query.clone();
		    refinement.addInfoLog("GateWay " + currentNode.getName() + " was bound to gateway ID " + lrs.getString("id"));
		    refinement.nodes.get(i).setID(lrs.getString("id"));
		    refinement.nodes.get(i).setName(lrs.getString("name"));

		    refinement.forbiddenGatewayIDs.append(", " + currentNode.getID());

		    //query.nodes.get(i).id = lrs.getString("id");
		    // we have to update all paths, edges in which this node is incident
		    refinement.updateEdgesWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateEdgesWithSource(currentNode,refinement.nodes.get(i).getName());
		    refinement.updatePathsWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updatePathsWithSource(currentNode,refinement.nodes.get(i).getName());

//		    DO the same thing with negatives
		    refinement.updateNegativeEdgesWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateNegativeEdgesWithSource(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateNegativePathsWithDestination(currentNode,refinement.nodes.get(i).getName());
		    refinement.updateNegativePathsWithSource(currentNode,refinement.nodes.get(i).getName());


		    // added on 9th of July 2008 
		    refinement.updateExcludeExpression(currentNode.getName(), currentNode.toString());
		    refinedQueries.add(refinement);
		}
		//for (int u =0; u < refinedQueries.size();u++)
		if (refinedQueries.size()> 0)
		    this.intermediateRefinements.addAll(0,refinedQueries);
		else
		{
		    // at any time an event fails to be resolved
		    // the whole query is terminated with failure.
		    log.error("GateWay "+ currentNode.getName() +" failed to find a binding");
		    log.error("Terminating the query processing against model " + modelID + " due to unbound objects");
		    this.intermediateRefinements.clear();
		    this.finalRefinements.clear();
		    return;
		}
	    }
	    catch (SQLException ex)
	    {
		log.error(ex.getErrorCode());
		log.error(ex.getMessage());
		log.error(selExp);
	    }
	    //refinedQueriesX.add(refinedQueries);
	    break;
	}
	// here we have a list of versions for each event node
	// we have to have the cartesian product
	// the order is inherint in the order of lists of refined queries

	if (!queryHasBeenRefined)
	    this.finalRefinements.add(query);
	resolveGateWayNode(modelID);
	//log.debug("End Resolve Event Node ID");
    }

    protected boolean checkNegativeEdges(QueryGraph qry, String modelID)
    {
	boolean allOk = true;

	// TODO potential for optimization: instead of looping over all edges and
	// issueing an SQL query for each, create one SQL statement covering all edges.
	// rationale: the most time is spent with communication to the DB now
	for (SequenceFlow negEdge : qry.negativeEdges)
	{
	    String selStmt = negEdge.getSelectStatement(modelID);
	    try
	    {
		ResultSet rs = Utilities.getDbStatemement().executeQuery(selStmt);
		while (rs.next())
		{
		    qry.addErrorLog("A negative edge checking between " 
			    + negEdge.getSourceGraphObject().getName() + " and "
			    + negEdge.getDestinationGraphObject().getName() + " failed!");
		    return false;
		}
	    } catch(SQLException ex)
	    {
		log.error("Database error. Could not check a negative edge. Results may be incorrect.", ex);
		return false;
	    }
	}
	return allOk;
    }

    protected void resolveGenericShape (String modelID)
    {
	//*****************************************************************************************
	// WE NEED TO HANDLE A CASE WHERE A DUMMY SUBSTITUTION OF ALL POSSIBILITIES, WHEN THE NODE IS NOT RESOLVALBE
	// this is a recursive function that keeps going as long as there are unknown nodes or paths
	if (this.intermediateRefinements.size()==0) return;
	List<GraphObject> AllInnerNodes=null;

	do
	{
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	    boolean queryHasBeenRefined = false;
	    List<QueryGraph> refinedQueries;
	    //refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    refinedQueries = new ArrayList<QueryGraph>();
	    QueryGraph refinement;
	    List<GraphObject> succsFromQueryGraph;
	    List<GraphObject> predsFromQueryGraph;
	    //ArrayList<GraphObject> pathSuccsFromQueryGraph;
	    //ArrayList<GraphObject> pathPredsFromQueryGraph;
	    List<GraphObject> intersectionResult= new ArrayList<GraphObject>();
	    List<GraphObject> tmp;
	    //SequenceFlow currentEdge;
	    GraphObject currentNode;


	    //System.out.println("CALLING RESOLVE EDGES");
	    int i;
	    // Optimization step added on 7th of July 2007
	    int sz3 = query.nodes.size();
	    for ( i = 0; i < sz3 ;i++)
		// End of optimization step
	    {

		currentNode = query.nodes.get(i);
		if (currentNode.type == GraphObjectType.ACTIVITY && currentNode.type2.equals("GENERIC SHAPE"))
		{

		    predsFromQueryGraph = query.getPredecessorsFromQueryGraph(currentNode);
		    succsFromQueryGraph = query.getSuccessorsFromQueryGraph(currentNode);
		    if (predsFromQueryGraph.size()> 0)
		    {
			tmp = getSuccessorsFromDB(predsFromQueryGraph.get(0), modelID, GraphObjectType.UNDEFINED);
			intersectionResult = Utilities.intersect(tmp, tmp);

			//	iterate to find the common nodes
			// Optimization step added on 7th July 2007
			int sz4 = predsFromQueryGraph.size();
			for (int j =1; j < sz4 && intersectionResult.size() > 0;j++)
			    // end of optimization step
			{
			    tmp = getSuccessorsFromDB(predsFromQueryGraph.get(j), modelID, GraphObjectType.UNDEFINED);
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			}
			// a variable node fails to resolve so terminate this query
			if (intersectionResult.size()== 0) return ;
		    }
		    if (succsFromQueryGraph.size() >0)
		    {
			tmp = getPredecessorsFromDB(succsFromQueryGraph.get(0), modelID, GraphObjectType.UNDEFINED);
			if (intersectionResult.size() >0)
			    // as i reach here with intersection result empty, this means i didnt find predecessors in query graph
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			else
			    intersectionResult = Utilities.intersect(tmp, tmp);

			//	iterate to find the common nodes
			// optimization step added on 7th July 2007
			int sz5 = succsFromQueryGraph.size();
			for (int j =1; j < sz5 && intersectionResult.size() > 0;j++)
			    // End of optimization step
			{
			    tmp = getPredecessorsFromDB(succsFromQueryGraph.get(j), modelID, GraphObjectType.UNDEFINED);
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			}
			if (intersectionResult.size()== 0) return;
		    }
		    // no sequence flow edges at all either incoming or outgoing
		    if (predsFromQueryGraph.size()==0 && succsFromQueryGraph.size()==0)
			// we have to try out all activities
		    { // this parent needs to be calculated once for all invocations
			// as it is a constant per instance
			if (AllInnerNodes == null)
			{
			    AllInnerNodes = getAllActivityNodesOfAModel(modelID);
			    AllInnerNodes.addAll(getAllGateWayNodesOfAModel(modelID,""));
			    AllInnerNodes.addAll(getAllIntermediateEventNodesOfAModel(modelID));
			}


			intersectionResult.clear();
			intersectionResult.addAll(AllInnerNodes);
		    }


		    //return;
		    queryHasBeenRefined = true;
		    // the big cost testing path expressions
		    //pathPredsFromQueryGraph = query.getPathPredecessorsFromQueryGraph(currentNode);
		    //pathSuccsFromQueryGraph = query.getPathSuccessorsFromQueryGraph(currentNode);

		    // Arriving here means we have to replace the unknown node with those nodes in the intersection result
		    // in the edge we remove the ones that have the unknown node as either source or desitnation
		    // and add corresponding resoved edges

		    // we have to filter the intersection result to ensure the 
		    // removal of ids that are in the forbidden list

		    // Optimization step added on 7th July 2007
		    int sz6 = intersectionResult.size();
		    for (int mm = 0; mm < sz6; mm++)
		    {	
			if ((query.forbiddenActivityIDs.toString().contains(intersectionResult.get(mm).getID()) && intersectionResult.get(mm).type == GraphObjectType.ACTIVITY) ||
				//(query.forbiddenGatewayIDs.toString().contains(Integer.toString(intersectionResult.get(mm).id)) && intersectionResult.get(mm).type1.equals("GateWay")) ||
				(query.forbiddenEventIDs.toString().contains(intersectionResult.get(mm).getID()) && intersectionResult.get(mm).type == GraphObjectType.EVENT)
			)
			    intersectionResult.get(mm).setName(intersectionResult.get(mm).getName() + "$$");
			// Special emphasis on gateways
			if (query.forbiddenGatewayIDs.toString().contains(intersectionResult.get(mm).getID()) && intersectionResult.get(mm).type == GraphObjectType.GATEWAY)
			{
			    // If any relation in query graph between the already existing and the generic shape dont add it
			    GraphObject dst = new GraphObject();
			    dst = intersectionResult.get(mm);
			    if (query.anyNegativeConnections(currentNode, dst)|| query.anyNegativeConnections(dst, currentNode ))
				intersectionResult.get(mm).setName(intersectionResult.get(mm).getName() + "$$");
			}

		    }

		    for ( int z = 0; z < sz6; z++)
		    {

			// for each of the suggested matched create a new 
			// instance of the query graph
			// 
			if (intersectionResult.get(z).getName().endsWith("$$")) continue;
			refinement = (QueryGraph)query.clone();
			//refinement.removeEdgesWithDestination(currentNode);
			//refinement.removeEdgesWithSource(currentNode);
			refinement.remove(currentNode);
			refinement.addInfoLog("Generic Node "+ currentNode.getName() + "has been bound to " + intersectionResult.get(z).getName() );
			//refinement.removePathsWithDestination(currentNode);
			//refinement.removePathsWithSource(currentNode);

			// we have to update negative edges and path links

			if (!intersectionResult.get(z).getName().endsWith("$$"))
			{
			    intersectionResult.get(z).setBoundQueryObjectID(currentNode.getID());
			    refinement.add(intersectionResult.get(z));
			    currentNode.setID(intersectionResult.get(z).getID());
			    //	currentNode.type1 = intersectionResult.get(z).type1;
			    //currentNode.type2 = intersectionResult.get(z).type2;
			    //					 update the forbidden ids
			    if (intersectionResult.get(z).type == GraphObjectType.ACTIVITY)
				refinement.forbiddenActivityIDs.append(", " + intersectionResult.get(z).getID()); 
			    else if (intersectionResult.get(z).type == GraphObjectType.GATEWAY)
				refinement.forbiddenGatewayIDs.append(", " + intersectionResult.get(z).getID());
			    else if (intersectionResult.get(z).type == GraphObjectType.EVENT)
				refinement.forbiddenEventIDs.append(", " + intersectionResult.get(z).getID());

			    refinement.updateNegativeEdgesWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updateNegativePathsWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updateNegativeEdgesWithSource(currentNode, intersectionResult.get(z));
			    refinement.updateNegativePathsWithSource(currentNode, intersectionResult.get(z));

			    refinement.updateEdgesWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updateEdgesWithSource(currentNode, intersectionResult.get(z));

			    refinement.updatePathsWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updatePathsWithSource(currentNode, intersectionResult.get(z));
			}
//			added on 9th of July 2008 
			refinement.updateExcludeExpression(currentNode.getName(), currentNode.toString());
			refinedQueries.add(refinement);
		    }
		    if (refinedQueries.size()> 0)
			this.intermediateRefinements.addAll(0,refinedQueries);
		    /*else
				{
					// at any time a variable node fails to be resolved
					// the whole query is terminated with failure.
					Utilities.intermediateRefinements.clear();
					Utilities.finalRefinements.clear();
					return;
				}*/
		    break;
		}
	    }
	    if (!queryHasBeenRefined)
		this.finalRefinements.add(query);
//	    System.out.println("Total intermediate queries:"+Utilities.intermediateRefinements.size());
//	    System.out.println("Total final queries:"+Utilities.finalRefinements.size());
//	    System.out.println("Calling once more");
	}
	while (this.intermediateRefinements.size() > 0);

//	System.out.println("Total intermediate queries:"+Utilities.intermediateRefinements.size());
//	System.out.println("Total final queries:"+Utilities.finalRefinements.size());

//	resolveGenericShape(modelID);
    }

    /**
     * this is a recursive function that keeps going as long as there are 
     * unknown nodes or paths 
     */
    protected void resolveGenericSplit(String modelID) 
    {
	// System.out.println("Calling resolve generic split");
	if (this.intermediateRefinements.size() == 0)
	    return;
	List<GraphObject> AllGateWayNodes = null;
	do {
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	    boolean queryHasBeenRefined = false;
	    List<QueryGraph> refinedQueries;
	    // refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    refinedQueries = new ArrayList<QueryGraph>();
	    QueryGraph refinement;
	    List<GraphObject> succsFromQueryGraph;
	    List<GraphObject> predsFromQueryGraph;
	    // ArrayList<GraphObject> pathSuccsFromQueryGraph;
	    // ArrayList<GraphObject> pathPredsFromQueryGraph;
	    List<GraphObject> intersectionResult = new ArrayList<GraphObject>();
	    List<GraphObject> tmp;
	    // SequenceFlow currentEdge;
	    GraphObject currentNode;

	    // System.out.println("CALLING RESOLVE EDGES");
	    int sz3 = query.nodes.size();
	    for (int i = 0; i < sz3; i++)
	    {
		currentNode = query.nodes.get(i);
		if (currentNode.type == GraphObjectType.GATEWAY
			&& currentNode.type2.equals("GENERIC SPLIT")) {

		    predsFromQueryGraph = query
		    .getPredecessorsFromQueryGraph(currentNode);
		    succsFromQueryGraph = query
		    .getSuccessorsFromQueryGraph(currentNode);
		    if (predsFromQueryGraph.size() > 0) {
			tmp = getSuccessorsFromDB(predsFromQueryGraph.get(0),
				modelID, GraphObjectType.GATEWAY);
			intersectionResult = Utilities.intersect(tmp, tmp);

			// iterate to find the common nodes
			// Optimization step added on 7th July 2007
			int sz4 = predsFromQueryGraph.size();
			for (int j = 1; j < sz4
			&& intersectionResult.size() > 0; j++)
			    // end of optimization step
			{
			    tmp = getSuccessorsFromDB(predsFromQueryGraph
				    .get(j), modelID, GraphObjectType.GATEWAY);
			    intersectionResult = Utilities.intersect(
				    intersectionResult, tmp);
			}
			// a variable node fails to resolve so terminate this
			// query
			if (intersectionResult.size() == 0)
			    return;
		    }
		    if (succsFromQueryGraph.size() > 0) {
			tmp = getPredecessorsFromDB(succsFromQueryGraph.get(0),
				modelID, GraphObjectType.GATEWAY);
			if (intersectionResult.size() > 0)
			    // as i reach here with intersection result empty,
			    // this means i didnt find predecessors in query
			    // graph
			    intersectionResult = Utilities.intersect(
				    intersectionResult, tmp);
			else
			    intersectionResult = Utilities.intersect(tmp, tmp);

			// iterate to find the common nodes
			// optimization step added on 7th July 2007
			int sz5 = succsFromQueryGraph.size();
			for (int j = 1; j < sz5
			&& intersectionResult.size() > 0; j++)
			    // End of optimization step
			{
			    tmp = getPredecessorsFromDB(succsFromQueryGraph
				    .get(j), modelID, GraphObjectType.GATEWAY);
			    intersectionResult = Utilities.intersect(
				    intersectionResult, tmp);
			}
			if (intersectionResult.size() == 0)
			    return;
		    }
		    // no sequence flow edges at all either incoming or outgoing
		    if (predsFromQueryGraph.size() == 0
			    && succsFromQueryGraph.size() == 0)
			// we have to try out all activities
		    {
			if (AllGateWayNodes == null) {
			    AllGateWayNodes = getAllGateWayNodesOfAModel(
				    modelID, "SPLIT");

			}
			intersectionResult.clear();
			intersectionResult.addAll(AllGateWayNodes);
		    }

		    // return;
		    queryHasBeenRefined = true;
		    // Optimization step added on 7th July 2007
		    int sz6 = intersectionResult.size();
		    for (int mm = 0; mm < sz6; mm++)
			if (query.forbiddenGatewayIDs.toString().contains(
				intersectionResult.get(mm)
				.getID()))
			    intersectionResult.get(mm)
			    .setName(
				    intersectionResult.get(mm)
				    .getName()
				    + "$$");

		    for (int z = 0; z < sz6; z++) {

			// for each of the suggested matched create a new
			// instance of the query graph
			// 
			if (intersectionResult.get(z).getName().endsWith("$$"))
			    continue;
			if (intersectionResult.get(z).type2.endsWith("JOIN"))
			    continue;

			refinement = (QueryGraph)query.clone();
			refinement.addInfoLog("Generic Split "
				+ currentNode.getName() + " has been bound to"
				+ intersectionResult.get(z).getName());
			// refinement.removeEdgesWithDestination(currentNode);
			// refinement.removeEdgesWithSource(currentNode);
			refinement.remove(currentNode);
			// refinement.removePathsWithDestination(currentNode);
			// refinement.removePathsWithSource(currentNode);

			// we have to update negative edges and path links

			if (!intersectionResult.get(z).getName().endsWith("$$")) {
			    intersectionResult.get(z).setBoundQueryObjectID(currentNode.getID());
			    refinement.add(intersectionResult.get(z));
			    currentNode
			    .setID(intersectionResult.get(z).getID());
			    // currentNode.type1 =
			    // intersectionResult.get(z).type1;
			    // currentNode.type2 =
			    // intersectionResult.get(z).type2;
			    // update the forbidden ids
			    // if
			    // (intersectionResult.get(z).type1.equals("Activity"))
			    // refinement.forbiddenActivityIDs.append(", " +
			    // intersectionResult.get(z).id);
			    if (intersectionResult.get(z).type == GraphObjectType.GATEWAY)
				refinement.forbiddenGatewayIDs.append(", "
					+ intersectionResult.get(z).getID());
			    // else if
			    // (intersectionResult.get(z).type1.equals("Event"))
			    // refinement.forbiddenEventIDs.append(", " +
			    // intersectionResult.get(z).id);

			    refinement.updateNegativeEdgesWithDestination(
				    currentNode, intersectionResult.get(z));
			    refinement.updateNegativePathsWithDestination(
				    currentNode, intersectionResult.get(z));
			    refinement.updateNegativeEdgesWithSource(
				    currentNode, intersectionResult.get(z));
			    refinement.updateNegativePathsWithSource(
				    currentNode, intersectionResult.get(z));

			    refinement.updateEdgesWithDestination(currentNode,
				    intersectionResult.get(z));
			    refinement.updateEdgesWithSource(currentNode,
				    intersectionResult.get(z));

			    refinement.updatePathsWithDestination(currentNode,
				    intersectionResult.get(z));
			    refinement.updatePathsWithSource(currentNode,
				    intersectionResult.get(z));
			}
//			added on 9th of July 2008 
			refinement.updateExcludeExpression(currentNode.getName(), currentNode.toString());
			refinedQueries.add(refinement);
		    }
		    if (refinedQueries.size() > 0)
			this.intermediateRefinements.addAll(0,
				refinedQueries);
		    /*
		     * else { // at any time a variable node fails to be
		     * resolved // the whole query is terminated with failure.
		     * Utilities.intermediateRefinements.clear();
		     * Utilities.finalRefinements.clear(); return; }
		     */
		    break;
		}
	    }
	    if (!queryHasBeenRefined)
		this.finalRefinements.add(query);
	} while (this.intermediateRefinements.size() > 0);
	//resolveGenericSplit(modelID);
    }

    /**
     * this is a recursive function that keeps going as long as there are unknown 
     * nodes or paths 
     */
    protected void resolveGenericJoin (String modelID)
    {
	if (this.intermediateRefinements.size()==0) return;
	List<GraphObject> AllGateWayNodes=null;
	do
	{
	    QueryGraph query = (QueryGraph)this.intermediateRefinements.remove(0).clone();

	    boolean queryHasBeenRefined = false;
	    List<QueryGraph> refinedQueries;
	    //refinedQueriesX = new ArrayList<ArrayList<QueryGraph>>();
	    refinedQueries = new ArrayList<QueryGraph>();
	    QueryGraph refinement;
	    List<GraphObject> succsFromQueryGraph;
	    List<GraphObject> predsFromQueryGraph;
	    //ArrayList<GraphObject> pathSuccsFromQueryGraph;
	    //ArrayList<GraphObject> pathPredsFromQueryGraph;
	    List<GraphObject> intersectionResult= new ArrayList<GraphObject>();
	    List<GraphObject> tmp;
	    //SequenceFlow currentEdge;
	    GraphObject currentNode;


	    //System.out.println("CALLING RESOLVE EDGES");
	    int i;
	    // Optimization step added on 7th of July 2007
	    int sz3 = query.nodes.size();
	    for ( i = 0; i < sz3 ;i++)
		// End of optimization step
	    {

		currentNode = query.nodes.get(i);
		if (currentNode.type == GraphObjectType.GATEWAY && currentNode.type2.equals("GENERIC JOIN"))
		{

		    predsFromQueryGraph = query.getPredecessorsFromQueryGraph(currentNode);
		    succsFromQueryGraph = query.getSuccessorsFromQueryGraph(currentNode);
		    if (predsFromQueryGraph.size()> 0)
		    {
			tmp = getSuccessorsFromDB(predsFromQueryGraph.get(0), modelID, GraphObjectType.GATEWAY);
			intersectionResult = Utilities.intersect(tmp, tmp);

			//	iterate to find the common nodes
			// Optimization step added on 7th July 2007
			int sz4 = predsFromQueryGraph.size();
			for (int j =1; j < sz4 && intersectionResult.size() > 0;j++)
			    // end of optimization step
			{
			    tmp = getSuccessorsFromDB(predsFromQueryGraph.get(j), modelID, GraphObjectType.GATEWAY);
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			}
			// a variable node fails to resolve so terminate this query
			if (intersectionResult.size()== 0) return ;
		    }
		    if (succsFromQueryGraph.size() >0)
		    {
			tmp = getPredecessorsFromDB(succsFromQueryGraph.get(0), modelID, GraphObjectType.GATEWAY);
			if (intersectionResult.size() >0)
			    // as i reach here with intersection result empty, this means i didnt find predecessors in query graph
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			else
			    intersectionResult = Utilities.intersect(tmp, tmp);

			//	iterate to find the common nodes
			// optimization step added on 7th July 2007
			int sz5 = succsFromQueryGraph.size();
			for (int j =1; j < sz5 && intersectionResult.size() > 0;j++)
			    // End of optimization step
			{
			    tmp = getPredecessorsFromDB(succsFromQueryGraph.get(j), modelID, GraphObjectType.GATEWAY);
			    intersectionResult = Utilities.intersect(intersectionResult, tmp);
			}
			if (intersectionResult.size()== 0) return;
		    }
		    // no sequence flow edges at all either incoming or outgoing
		    if (predsFromQueryGraph.size()==0 && succsFromQueryGraph.size()==0)
			// we have to try out all activities
		    {
			if (AllGateWayNodes == null)
			{
			    AllGateWayNodes = getAllGateWayNodesOfAModel(modelID,"JOIN");

			}
			intersectionResult.clear();
			intersectionResult.addAll(AllGateWayNodes);
		    }
		    //return;
		    queryHasBeenRefined = true;
		    // Optimization step added on 7th July 2007
		    int sz6 = intersectionResult.size();
		    for (int mm =0; mm< sz6;mm++)
			if (query.forbiddenGatewayIDs.toString().contains(intersectionResult.get(mm).getID()))
			    intersectionResult.get(mm).setName(intersectionResult.get(mm).getName() + "$$");

		    for ( int z = 0; z < sz6;z++)
		    {

			// for each of the suggested matched create a new 
			// instance of the query graph
			// 
			if (intersectionResult.get(z).getName().endsWith("$$")) 
			    continue;
			if (intersectionResult.get(z).type2.endsWith("SPLIT")) 
			    continue;

			refinement = (QueryGraph)query.clone();
			//refinement.removeEdgesWithDestination(currentNode);
			//refinement.removeEdgesWithSource(currentNode);
			refinement.addInfoLog("Generic Join " + currentNode.getName() + " has been bound to" + intersectionResult.get(z).getName());
			refinement.remove(currentNode);
			//refinement.removePathsWithDestination(currentNode);
			//refinement.removePathsWithSource(currentNode);

			// we have to update negative edges and path links

			if (!intersectionResult.get(z).getName().endsWith("$$"))
			{
			    intersectionResult.get(z).setBoundQueryObjectID(currentNode.getID());
			    refinement.add(intersectionResult.get(z));
			    currentNode.setID(intersectionResult.get(z).getID());
			    //	currentNode.type1 = intersectionResult.get(z).type1;
			    //currentNode.type2 = intersectionResult.get(z).type2;
			    //					 update the forbidden ids
			    //if (intersectionResult.get(z).type1.equals("Activity"))
			    //	refinement.forbiddenActivityIDs.append(", " + intersectionResult.get(z).id); 
			    if (intersectionResult.get(z).type == GraphObjectType.GATEWAY)
				refinement.forbiddenGatewayIDs.append(", " + intersectionResult.get(z).getID());
			    //else if (intersectionResult.get(z).type1.equals("Event"))
			    //	refinement.forbiddenEventIDs.append(", " + intersectionResult.get(z).id);

			    refinement.updateNegativeEdgesWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updateNegativePathsWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updateNegativeEdgesWithSource(currentNode, intersectionResult.get(z));
			    refinement.updateNegativePathsWithSource(currentNode, intersectionResult.get(z));

			    refinement.updateEdgesWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updateEdgesWithSource(currentNode, intersectionResult.get(z));

			    refinement.updatePathsWithDestination(currentNode, intersectionResult.get(z));
			    refinement.updatePathsWithSource(currentNode, intersectionResult.get(z));
			}
//			added on 9th of July 2008 
			refinement.updateExcludeExpression(currentNode.getName(), currentNode.toString());
			refinedQueries.add(refinement);
		    }
		    if (refinedQueries.size()> 0)
			this.intermediateRefinements.addAll(0,refinedQueries);
		    /*else
				{
					// at any time a variable node fails to be resolved
					// the whole query is terminated with failure.
					Utilities.intermediateRefinements.clear();
					Utilities.finalRefinements.clear();
					return;
				}*/
		    break;
		}
	    }
	    if (!queryHasBeenRefined)
		this.finalRefinements.add(query);
	}
	while (this.intermediateRefinements.size()> 0);
	//resolveGenericJoin(modelID);
    }

    @Override
    protected boolean resolveConcreteDataObjectID(QueryGraph qry, String modelID)
    {
	// TODO Auto-generated method stub
	return true;

    }

    @Override
    protected void resolveVariableDataObjects(String modelID)
    {
	// TODO Auto-generated method stub

    }

    public List<String> findRelevantProcessModels(QueryGraph query) throws IOException
    {
	// log.debug("Begin Filter Database");
	// First we have to filter graph database to the set of matching models

	StringBuilder filterStatement = new StringBuilder(100);
	filterStatement.append("select \"ID\" from \"BPMN_GRAPH\".\"MODEL\" where 1=1 ");
	// Start with known nodes

	for(GraphObject currentNode : query.nodes)
	{
	    if (!currentNode.getName().startsWith("@") 
		    && !currentNode.getName().startsWith("$#") 
		    && !currentNode.getName().startsWith("?"))
	    {
		// We can filter with this node  because it is known
		switch(currentNode.type) {
		case ACTIVITY:
		    filterStatement.append("and exists (select 1 from \"BPMN_GRAPH\".\"ACTIVITY\" where trim(upper(\"NAME\")) =trim(upper('"+ currentNode.getName()+"')) and \"MOD_ID\" = \"BPMN_GRAPH\".\"MODEL\".\"ID\")");
		    //filterStatement += "and model.id in (select mod_id from activity where ucase(name) =ucase('"+ currentNode.name+"'))";
		    break;
		case EVENT:
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"EVENT\" where trim(upper(\"NAME\")) =trim(upper('"+ currentNode.getName()+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"MODEL\".\"ID\")");
		    //filterStatement += "and model.id in (select model_id from event where ucase(name) =ucase('"+ currentNode.name+"'))";
		    break;
		case GATEWAY:
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"GATEWAY\" where trim(upper(\"NAME\")) =trim(upper('"+ ""+"'))"); 
		    //filterStatement += "and model.id in (select model_id from gateway where ucase(name) =ucase('"+ currentNode.name+"')" +
		    filterStatement.append(" and trim(upper(\"GATE_WAY_TYPE\")) = trim(upper('"+ currentNode.type2+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"MODEL\".\"ID\")");
		    //" and ucase(gate_way_type) = ucase('"+ currentNode.type2+"'))";
		}
	    }
	}
	// added on 7.08.08 to support filtering with data objects
	for (DataObject dob : query.dataObjs)
	{
	    if (!dob.name.startsWith("@"))
	    {
		filterStatement.append("and exists (select 1 from \"BPMN_GRAPH\".\"DATA_OBJECT\" where trim(upper(\"NAME\")) =trim(upper('"+ dob.name+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"MODEL\".\"ID\")");
	    }
	}
	// now filter with known edges
	// The following for loop was commented to test performance and results without it
	for(SequenceFlow currentEdge : query.edges)
	{
	    if (currentEdge.frmActivity != null && currentEdge.toActivity != null)
	    {
		if (!currentEdge.frmActivity.name.startsWith("@") && !currentEdge.toActivity.name.startsWith("@") 
			&& !currentEdge.frmActivity.name.startsWith("$#") && !currentEdge.toActivity.name.startsWith("$#"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"SEQUENCE_FLOW\",\"BPMN_GRAPH\".\"ACTIVITY\" as source, \"BPMN_GRAPH\".\"ACTIVITY\" as destination");
		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"MODEL_ID\" = \"BPMN_GRAPH\".\"MODEL\".\"ID\")");
		    //" and ucase(destination.name)= ucase('"+ currentEdge.toActivity.actName+"'))";
		}
	    }
	}
	//	 added on 7.08.08 to support filtering with data objects
	for (Association ass : query.associations)
	{
	    if (ass.frmActivity != null && ass.toDataObject != null)
	    {
		if (!ass.frmActivity.name.startsWith("@") && 
			!ass.frmActivity.name.startsWith("$#") &&
			!ass.toDataObject.name.startsWith("@")&&
			!ass.toDataObject.getState().startsWith("?"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"DATA_OBJECT\",\"BPMN_GRAPH\".\"DATA_OBJECT_STATES\"" +
			    ",\"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ACTIVITY\"" +
			    " where \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
			    " and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
			    " and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\"."+
			    "\"TO_STATE_ID\" and \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ACTIVITY\".\"ID\"" +
			    " and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"NAME\" = '" + ass.toDataObject.name +
			    "' and \"BPMN_GRAPH\".\"ACTIVITY\".\"NAME\" = '"+ ass.frmActivity.name+
			    "' and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.toDataObject.getState()+"'");
		}
	    }
	    else if (ass.frmEvent != null && ass.toDataObject !=null)
	    {
		if (!ass.frmEvent.eventName.startsWith("@") && 
			!ass.frmEvent.eventName.startsWith("$#") &&
			!ass.toDataObject.name.startsWith("@")&&
			!ass.toDataObject.getState().startsWith("?"))
		{
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"DATA_OBJECT\",\"BPMN_GRAPH\".\"DATA_OBJECT_STATES\"" +
			    ",\"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"EVENT\"" +
			    " where \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
			    " and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
			    " and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\"."+
			    "\"TO_STATE_ID\" and \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"EVENT\".\"ID\"" +
			    " and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"NAME\" = '" + ass.toDataObject.name +
			    "' and \"BPMN_GRAPH\".\"EVENT\".\"NAME\" = '"+ ass.frmEvent.eventName+
			    "' and \"BPMN_GRAPH\".\"EVENT\".\"EVENT_POSITION\" = "+ ass.frmEvent.eventPosition +
			    " and \"BPMN_GRAPH\".\"EVENT\".\"EVENT_TYPE\" = '"+ ass.frmEvent.eventType +
			    "' and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.toDataObject.getState()+"'");
		}
	    }
	    else if (ass.frmDataObject != null && ass.toActivity != null)
	    {
		filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"DATA_OBJECT\",\"BPMN_GRAPH\".\"DATA_OBJECT_STATES\"" +
			",\"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ACTIVITY\"" +
			" where \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
			" and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
			" and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\"."+
			"\"FROM_STATE_ID\" and \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ACTIVITY\".\"ID\"" +
			" and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"NAME\" = '" + ass.toDataObject.name +
			"' and \"BPMN_GRAPH\".\"ACTIVITY\".\"NAME\" = '"+ ass.frmActivity.name+
			"' and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.toDataObject.getState()+"'");
	    }
	    else if (ass.frmDataObject != null && ass.toEvent != null)
	    {
		filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"DATA_OBJECT\",\"BPMN_GRAPH\".\"DATA_OBJECT_STATES\"" +
			",\"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"EVENT\"" +
			" where \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
			" and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
			" and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\"."+
			"\"FROM_STATE_ID\" and \"BPMN_GRAPH\".\"DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"EVENT\".\"ID\"" +
			" and \"BPMN_GRAPH\".\"DATA_OBJECT\".\"NAME\" = '" + ass.toDataObject.name +
			"' and \"BPMN_GRAPH\".\"EVENT\".\"NAME\" = '"+ ass.frmEvent.eventName+
			"' and \"BPMN_GRAPH\".\"EVENT\".\"EVENT_POSITION\" = "+ ass.frmEvent.eventPosition +
			" and \"BPMN_GRAPH\".\"EVENT\".\"EVENT_TYPE\" = '"+ ass.frmEvent.eventType +
			"' and \"BPMN_GRAPH\".\"DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.toDataObject.getState()+"'");
	    }
	}
	String currentModel;
	List<String> results = new ArrayList<String>();

	try
	{
	    ResultSet matchingModels = Utilities.getDbStatemement().executeQuery(
		    filterStatement.toString());
	    // for each returned model we have to resolve the query into the 
	    // maximal sub graph from that model that matches the query.
	    while (matchingModels.next())
	    {
		// start from the query graph and refine

		currentModel = matchingModels.getString("id");
		results.add(currentModel);
	    }
	} catch (SQLException e)
	{
	    throw new IOException(e);
	}

	// log.debug("End Filter Database");
	return results;
    }

    @Override
    protected ProcessGraph updateSequenceFlowConditions(ProcessGraph result, ProcessGraph matchingProcess)
    {
	// TODO Auto-generated method stub
	return null;
    }
   
}
