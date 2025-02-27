package com.bpmnq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
//import java.sql.SQLException;
//import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;



public final class OryxMemoryQueryProcessor extends MemoryQueryProcessor{

    private String oryxBaseUri;
    private final String filterURIpostfix = "/filter?type=http%3A%2F%2Fb3mn.org%2Fstencilset%2Fbpmn1.1%23";
    private Logger log = Logger.getLogger(OryxMemoryQueryProcessor.class);

    public OryxMemoryQueryProcessor(PrintWriter answer)
    {
	super(answer);
	oryxBaseUri = Utilities.config.getProperty("bpmnq.oryx.baseuri", 
		"http://oryx-editor.org/backend/poem");
    }
    
    public OryxMemoryQueryProcessor(PrintWriter answer, String oryxBaseUri) {
	this(answer);
	this.oryxBaseUri = oryxBaseUri;
    }
    
    protected void refreshModel(String modelID)
    {
	if (!currentProcess.modelURI.equals(modelID))
	{
	    long startLoading = System.currentTimeMillis();
	    if (Utilities.QUERY_PROCESSOR_TYPE.toUpperCase().equals("DATABASE"))
		currentProcess.loadModelFromOryxRepository(modelID);
	    else    
		currentProcess.loadFromOryx(modelID);
	    long loadingTime = (System.currentTimeMillis() - startLoading);
//	    extraOverhead += loadingTime;
	    System.out.println("Loading model: "+modelID+" took "+(loadingTime) +" MS");
	}
	
    }
    
    /**
     * Current implementation ignores the actual query graph. Instead, it
     * retrieves a list of process models that are currently accessible in Oryx
     */
    public List<String> findRelevantProcessModels(QueryGraph query) throws IOException
    {
	if (Utilities.QUERY_PROCESSOR_TYPE.equals("DATABASE"))
	    return findRelevantProcessModelsDatabase(query);
	else
	    return findRelevantModelsMemory(query);
	
    }
    public List<String> findRelevantModelsMemory(QueryGraph query) throws IOException
    {
	List<String> result = new ArrayList<String>();
	List<String> temp = new ArrayList<String>();
	URL repository;
	try
	{
	    repository = new URL(oryxBaseUri + filterURIpostfix);
	    BufferedReader in = new BufferedReader(new InputStreamReader(
		    repository.openStream()));
	    String inputLine;
	    while ((inputLine = in.readLine()) != null)
	    {
		temp.add(inputLine);
	    }
            in.close();

            // stream contains list of accessible Oryx models, encoded as a JSON array
            // this encoding is parsed in the following
            String [] splits;
            int size = 0;
            for (String t : temp)
            {
        	splits = t.split(",");
        	size += splits.length;
        	for (int i = 0 ; i < splits.length;i++)
        	{
        	    // remove braces and quotation marks
        	    String urlPart = splits[i].replace("[", "").replace("]","").replace("\"", "");
        	    result.add(oryxBaseUri + urlPart + "/rdf");
        	}
            }
            log.info(String.valueOf(size) +" process models are subject to checking.");	
	} catch (IOException e)
	{
	    log.error("Could not read in list of relevant process model from Oryx repository", e);
	    throw e;
	}
	
	return result;
    }
//    public List<String> processQuery(QueryGraph qry)
//    {
//	List<String> matchedModels = new ArrayList<String>();
//	List<String> filterResult;
//	try
//	{
//	    long startTime = System.currentTimeMillis();
//	    filterResult = findRelevantProcessModels(qry);
//	    this.extraOverhead+= (System.currentTimeMillis() - startTime);
//	} catch (IOException e)
//	{
//	    log.error("Database error, Cannot retrieve model numbers. Cannot continue - Aborting!", e);
//	    return matchedModels;
//	}
//
//	answerWriter.println("<query-result>");
//	
//	for (String procModelNo : filterResult)
//	{
//	    this.intermediateRefinements.clear();
//	    this.finalRefinements.clear();
//	    QueryGraph rslt = (QueryGraph)qry.clone();
//	    //log.debug("Testing Model:" + procModelNo);
//	    ProcessGraph matchingProcGraph = null;
//	    if (testQueryAgainstModel(rslt, procModelNo.toString(), matchingProcGraph))
//		matchedModels.add(procModelNo);
////		matchedModels.put(procModelNo, matchingProcGraph);
//	
//	}
//	answerWriter.println("</query-result>");
//
//	return matchedModels;
//    }

    public List<Match> processMultiQuery(QueryGraph qry)
    {
	List<String> filterResult;
	try
	{
	    filterResult = findRelevantProcessModels(qry);
	} catch (IOException e)
	{
	    log.error("Database error, Cannot retrieve model numbers. Cannot continue - Aborting!", e);
	    return new ArrayList<Match>(0);
	}

	matches.clear();

	for (String modelNo : filterResult)
	{
	    this.intermediateRefinements.clear();
	    this.finalRefinements.clear();
	    QueryGraph rslt = (QueryGraph)qry.clone();
	    ProcessGraph pGraph = runQueryAgainstModel(rslt, modelNo.toString());
	    if (pGraph.nodes.size() > 0)
	    {
		Match mc = new Match();
		mc.matchGraph = pGraph;
		mc.matchGraph.modelURI = modelNo;
		mc.queryMatchRate = qry.getMatchRate();
		mc.matchedQuery = qry;
		matches.add(mc);

	    }
	}
	return matches;

    }
    protected List<String> handleExcludeStatement(String exec,String modelID)
    {
	List<String> results= new ArrayList<String>();
	StringTokenizer stk = new StringTokenizer(exec,",");
//	StringTokenizer stk2;
	String token;
	GraphObject and=null;
	while(stk.hasMoreTokens())
	{
	    token = stk.nextToken();
	    if (token.startsWith("\""))
		token = token.substring(1, token.length());
	    if (token.endsWith("\""))
		token = token.substring(0, token.length()-1);
	    token = token.trim();
	    if (token.equals("XORJOIN"))
	    {
		for (GraphObject nd:currentProcess.getGateways("XOR JOIN"))
		{
		    results.add(nd.toString());
		    if (includeEnclosingAndSplits)
			    and = currentProcess.getEnclosingANDSplit(nd);
		    if (and !=null && includeEnclosingAndSplits)
			 results.add(and.toString());
		    
//		    stk2 = new StringTokenizer(Utilities.getParallelsOfActivity(nd.toString()),",");
//		    while (stk2.hasMoreTokens())
//		    {
//			token2 =stk2.nextToken(); 
//			if (token2.trim().length() > 0)
//			    results.add(token2.trim());
//		    }
		}
	    }
	    else if (token.equals("XORSPLIT"))
	    {
		for (GraphObject nd:currentProcess.getGateways("XOR SPLIT"))
		{
		    results.add(nd.toString());
		    if (includeEnclosingAndSplits)
			    and = currentProcess.getEnclosingANDSplit(nd);
		    if (and !=null && includeEnclosingAndSplits)
			 results.add(and.toString());
		    
//		    stk2 = new StringTokenizer(Utilities.getParallelsOfActivity(nd.toString()),",");
//		    while (stk2.hasMoreTokens())
//		    {
//			token2 =stk2.nextToken(); 
//			if (token2.trim().length() > 0)
//			    results.add(token2.trim());
//		    }
		}

	    }
	    else if (token.equals("ANDJOIN"))
	    {
		for (GraphObject nd:currentProcess.getGateways("AND JOIN"))
		{
		    results.add(nd.toString());
		    if (includeEnclosingAndSplits)
			    and = currentProcess.getEnclosingANDSplit(nd);
		    if (and !=null && includeEnclosingAndSplits)
			 results.add(and.toString());
		    
//		    stk2 = new StringTokenizer(Utilities.getParallelsOfActivity(nd.toString()),",");
//		    while (stk2.hasMoreTokens())
//		    {
//			token2 =stk2.nextToken(); 
//			if (token2.trim().length() > 0)
//			    results.add(token2.trim());
//		    }
		}
	    }
	    else if (token.equals("ANDSPLIT"))
	    {
		for (GraphObject nd:currentProcess.getGateways("AND SPLIT"))
		{
		    results.add(nd.toString());
		    if (includeEnclosingAndSplits)
			and = currentProcess.getEnclosingANDSplit(nd);
		    if (and !=null && includeEnclosingAndSplits)
			 results.add(and.toString());
		    
//		    stk2 = new StringTokenizer(Utilities.getParallelsOfActivity(nd.toString()),",");
//		    while (stk2.hasMoreTokens())
//		    {
//			token2 =stk2.nextToken(); 
//			if (token2.trim().length() > 0)
//			    results.add(token2.trim());
//		    }
		}
	    }
	    else if (token.equals("ORJOIN"))
	    {
		for (GraphObject nd:currentProcess.getGateways("OR JOIN"))
		{
		    results.add(nd.toString());
		    if (includeEnclosingAndSplits)
			    and = currentProcess.getEnclosingANDSplit(nd);
		    if (and !=null && includeEnclosingAndSplits)
			results.add(and.toString());
//		    stk2 = new StringTokenizer(Utilities.getParallelsOfActivity(nd.toString()),",");
//		    while (stk2.hasMoreTokens())
//		    {
//			token2 =stk2.nextToken(); 
//			if (token2.trim().length() > 0)
//			    results.add(token2.trim());
//		    }
		}
	    }
	    else if (token.equals("ORSPLIT"))
	    {
		for (GraphObject nd:currentProcess.getGateways("OR SPLIT"))
		{
		    results.add(nd.toString());
		    if (includeEnclosingAndSplits)
			    and = currentProcess.getEnclosingANDSplit(nd);
		    if (and !=null && includeEnclosingAndSplits)
			results.add(and.toString());
//		    stk2 = new StringTokenizer(Utilities.getParallelsOfActivity(nd.toString()),",");
//		    while (stk2.hasMoreTokens())
//		    {
//			token2 =stk2.nextToken(); 
//			if (token2.trim().length() > 0)
//			    results.add(token2.trim());
//		    }
		}
	    }
	    else if (token.startsWith("GAT") || token.startsWith("EVE")|| token.startsWith("ACT"))
	    {
		results.add(token);
		GraphObject nd = new GraphObject();
		nd = currentProcess.getNode(token);
		if (includeEnclosingAndSplits)
		    and = currentProcess.getEnclosingANDSplit(nd);
        	if (and !=null && includeEnclosingAndSplits)
        	    results.add(and.toString());
//		stk2 = new StringTokenizer(Utilities.getParallelsOfActivity(token),",");
//		while (stk2.hasMoreTokens())
//		{
//		    token2 =stk2.nextToken(); 
//		    if (token2.trim().length() > 0)
//			results.add(token2.trim());
//		}
	    }
	    else // this is an activity
	    {
		GraphObject currentNode = new GraphObject();
//		currentNode.type = GraphObjectType.ACTIVITY;
//		currentNode.setName(token);
//		currentNode.setID(currentProcess.getActivity(token).getID());
		
//		currentNode.setName(token);
//		//currentNode.name = exclude;
//		currentNode.type = GraphObjectType.ACTIVITY;
//		String newId = "0";
//		newId =  currentProcess.get
//		try
//		{
//		    newId = Utilities.getActivityID(currentNode.getName(), modelID);
//		} catch (SQLException e)
//		{
//		    log.error("Database error: Could not retrieve an activity ID. Results may be incorrect!", e);
//		}
//		currentNode.setID(newId);
		currentNode = currentProcess.getActivity(token);
		
//		if (includeEnclosingAndSplits)
//		    and = currentProcess.getEnclosingANDSplit(currentNode);
//		if (and !=null && includeEnclosingAndSplits)
//		    results.add(and.toString());
		if (currentNode != null)
		{
		    results.add(currentNode.toString());
//		    stk2 = new StringTokenizer(Utilities.getParallelsOfActivity(currentNode.toString()),",");
//			while (stk2.hasMoreTokens())
//			{
//			    token2 =stk2.nextToken(); 
//			    if (token2.trim().length() > 0)
//				results.add(token2.trim());
//			}
		}
		
	    }
	}
	return results;
    }
    private List<String> findRelevantProcessModelsDatabase(QueryGraph query) throws IOException
    {
	// log.debug("Begin Filter Database");
	// First we have to filter graph database to the set of matching models
	//log.info("################### Find Relevant process models Database");
	StringBuilder filterStatement = new StringBuilder(100);
	filterStatement.append("select \"ID\" from \"BPMN_GRAPH\".\"ORYX_MODEL\" where 1=1 ");
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
		    filterStatement.append("and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" where trim(upper(\"NAME\")) =trim(upper('"+ currentNode.getName()+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //filterStatement += "and model.id in (select mod_id from activity where ucase(name) =ucase('"+ currentNode.name+"'))";
		    break;
		case EVENT:
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_EVENT\" where trim(upper(\"NAME\")) =trim(upper('"+ currentNode.getName()+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //filterStatement += "and model.id in (select model_id from event where ucase(name) =ucase('"+ currentNode.name+"'))";
		    break;
		case GATEWAY:
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_GATEWAY\" where trim(upper(\"NAME\")) =trim(upper('"+ ""+"'))"); 
		    //filterStatement += "and model.id in (select model_id from gateway where ucase(name) =ucase('"+ currentNode.name+"')" +
		    filterStatement.append(" and trim(upper(\"GATE_WAY_TYPE\")) = trim(upper('"+ currentNode.type2+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
		    //" and ucase(gate_way_type) = ucase('"+ currentNode.type2+"'))";
		}
	    }
	}
	// added on 7.08.08 to support filtering with data objects
	for (DataObject dob : query.dataObjs)
	{
	    if (!dob.name.startsWith("@"))
	    {
		filterStatement.append("and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\" where trim(upper(\"NAME\")) =trim(upper('"+ dob.name+"')) and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
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
		    filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_SEQUENCE_FLOW\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as source, \"BPMN_GRAPH\".\"ORYX_ACTIVITY\" as destination");
		    filterStatement.append(" where \"FRM_ACT_ID\" =source.\"ID\" and \"TO_ACT_ID\" = destination.\"ID\"");
		    filterStatement.append(" and upper(source.\"NAME\") = upper('"+ currentEdge.frmActivity.name+"')");
		    //" and ucase(source.name) = ucase('"+ currentEdge.frmActivity.actName+"')" +
		    filterStatement.append(" and upper(destination.\"NAME\")= upper('"+ currentEdge.toActivity.name+"') and \"MODEL_ID\" = \"BPMN_GRAPH\".\"ORYX_MODEL\".\"ID\")");
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
				filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
                ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\"" +
                " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
                "\"TO_STATE_ID\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" = '" + ass.toDataObject.name +
                "' and \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"NAME\" = '"+ ass.frmActivity.name+
                "' and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.toDataObject.getState()+"')");
			}
		}
		else if (ass.frmEvent != null && ass.toDataObject !=null)
		{
			if (!ass.frmEvent.eventName.startsWith("@") && 
					!ass.frmEvent.eventName.startsWith("$#") &&
					!ass.toDataObject.name.startsWith("@")&&
					!ass.toDataObject.getState().startsWith("?"))
			{
				filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
                ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_EVENT\"" +
                " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
                "\"TO_STATE_ID\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_EVENT\".\"ID\"" +
                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" = '" + ass.toDataObject.name +
                "' and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"NAME\" = '"+ ass.frmEvent.eventName+
                "' and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"EVENT_POSITION\" = "+ ass.frmEvent.eventPosition +
                " and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"EVENT_TYPE\" = '"+ ass.frmEvent.eventType +
                "' and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.toDataObject.getState()+"')");
			}
		}
		else if (ass.frmDataObject != null && ass.toActivity != null)
		{
			filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
	                ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_ACTIVITY\"" +
	                " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
	                "\"FROM_STATE_ID\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" = '" + ass.frmDataObject.name +
	                "' and \"BPMN_GRAPH\".\"ORYX_ACTIVITY\".\"NAME\" = '"+ ass.toActivity.name+
	                "' and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.frmDataObject.getState()+"')");
		}
		else if (ass.frmDataObject != null && ass.toEvent != null)
		{
			filterStatement.append(" and exists (select 1 from \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\"" +
	                ",\"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\",\"BPMN_GRAPH\".\"ORYX_EVENT\"" +
	                " where \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"DATA_OBJECT_ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"DATA_OBJECT_ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_ID\" = \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\"."+
	                "\"FROM_STATE_ID\" and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATE_TRANSITION\".\"ACTIVITY_ID\" = \"BPMN_GRAPH\".\"ORYX_EVENT\".\"ID\"" +
	                " and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT\".\"NAME\" = '" + ass.frmDataObject.name +
	                "' and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"NAME\" = '"+ ass.toEvent.eventName+
	                "' and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"EVENT_POSITION\" = "+ ass.toEvent.eventPosition +
	                " and \"BPMN_GRAPH\".\"ORYX_EVENT\".\"EVENT_TYPE\" = '"+ ass.toEvent.eventType +
	                "' and \"BPMN_GRAPH\".\"ORYX_DATA_OBJECT_STATES\".\"STATE_NAME\"='" + ass.frmDataObject.getState()+"')");
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
	    log.info(String.valueOf(results.size()) +" process models are subject to checking.");	
	} catch (SQLException e)
	{
	    throw new IOException("A database error occurred...", e);
	}

	// log.debug("End Filter Database");
	return results;
    }
}