package com.bpmnq.queryexpander;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.themis.ir.eTVSM;
import org.themis.ir.eTVSMOntology;

import com.bpmnq.AbstractQueryProcessor;
import com.bpmnq.GraphObject;
import com.bpmnq.Match;
import com.bpmnq.OryxMemoryQueryProcessor;
import com.bpmnq.QueryGraph;
import com.bpmnq.DatabaseQueryProcessor;
import com.bpmnq.Utilities;
import com.bpmnq.GraphObject.GraphObjectType;

public final class QueryExpander {
    
    private Logger log = Logger.getLogger(QueryGraph.class); 
    protected PrintWriter answerWriter;
    protected AbstractQueryProcessor qProcessor;
    public eTVSM e;
    public eTVSMOntology o;
//  private static final float THRESHOLD = 0.4f;

    private class SynMatch {
	String synonym;
	float rate;
	public boolean equals(Object other)
	{
	    if (!(other instanceof SynMatch))
		return false;
	    
	    SynMatch another = (SynMatch) other;
	    return another.synonym.equalsIgnoreCase(this.synonym) && another.rate == this.rate;
	}
    }

    public QueryExpander(PrintWriter answer) {
	this.answerWriter = answer;
	try {
	    // FIXME make database credentials configurable!!
	    e = new eTVSM("localhost","themis","postgres","postgres");
	    o = new eTVSMOntology("localhost","themis","postgres","postgres");
//	    e.clear();
//	    ontology.clear();

	    //ontology.autoISims(true);
	} catch (SQLException e1) {
	    e1.printStackTrace();
	} catch (ClassNotFoundException e1) {
	    e1.printStackTrace();
	}
    }
    public QueryExpander(PrintWriter answer, AbstractQueryProcessor qp)
    {
	this(answer);
	this.qProcessor = qp;
    }

    public List<SynMatch> findSynonyms(String par, float threshold)
    {
//	ArrayList<String> result = null;
	List<SynMatch> matches = null;
	try
	{
	    ResultSet res = e.searchFull(par, 0, 100);
//	    result = new ArrayList<String>();
	    matches = new ArrayList<SynMatch>();
	    SynMatch sm;

	    while (res.next())
	    {
		if (res.getFloat(3) >= threshold && !res.getString(4).toLowerCase().trim().equals(par.toLowerCase().trim()))
		{
		    sm = new SynMatch();
		    sm.synonym = res.getString(4);
		    sm.rate = res.getFloat(3);
		    if (!matches.contains(sm))
			matches.add(sm);
		}
		//System.out.println(res.getInt(1)+"\t|\t"+res.getFloat(3)+"\t|\t"+res.getString(4));
	    }
	    return matches;
	}
	catch(SQLException ex)
	{
	    ex.printStackTrace();
	}
	return matches;
    }

    public List<QueryGraph> expandQuery(QueryGraph qry, float threshold)
    {
	List<QueryGraph> tmpEquivalent = new ArrayList<QueryGraph>(10);
	tmpEquivalent.add(qry);
//	ArrayList<GraphObject> succs, preds,negsuccs,negpreds,pathsuccs,negpathsuccs,pathpreds,negpathpreds;
//	ArrayList<String> syns;
	List<SynMatch> syns;
	QueryGraph newQry=null;
//	boolean found = false;
	GraphObject newNode;
	int sz = qry.nodes.size();
	for (int i = 0 ; i < sz;i++)
	{
	    GraphObject currentNode = qry.nodes.get(i);
	    if (currentNode.type == GraphObjectType.ACTIVITY && currentNode.type2.equals("")
		    && !currentNode.getName().startsWith("@"))
	    { // this is an activity node with a name
		syns = findSynonyms(currentNode.getName(), threshold);
		for (int j = 0 ; j < syns.size(); j++)
		{
//		    found = false;

		    // remove the node with all incoming outgoing edges
		    newNode = new GraphObject();
		    newNode.setID(currentNode.getID());
		    newNode.setName(syns.get(j).synonym);
		    newNode.type = currentNode.type;
		    newNode.type2 = currentNode.type2;

		    int tmpsize = tmpEquivalent.size();
		    for (int k = 0 ; k < tmpsize;k++)
		    {
			if (tmpEquivalent.get(k).nodes.contains(currentNode))
			{
			    newQry = (QueryGraph)tmpEquivalent.get(k).clone();
			    newQry.remove(currentNode);
			    newQry.add(newNode);
			    newQry.updateMatchRate(syns.get(j).rate);
			    newQry.updateEdgesWithDestination(currentNode, syns.get(j).synonym);
			    newQry.updateEdgesWithSource(currentNode, syns.get(j).synonym);

			    newQry.updateNegativeEdgesWithDestination(currentNode, syns.get(j).synonym);
			    newQry.updateNegativeEdgesWithSource(currentNode,syns.get(j).synonym);

			    newQry.updatePathsWithDestination(currentNode, syns.get(j).synonym);
			    newQry.updatePathsWithSource(currentNode, syns.get(j).synonym);

			    newQry.updateNegativePathsWithDestination(currentNode, syns.get(j).synonym);
			    newQry.updateNegativePathsWithSource(currentNode, syns.get(j).synonym);
			    tmpEquivalent.add(newQry);
//			    found = true;
			}
		    }
		    // no match found in the tmpequivalent
//		    if (!found)
//		    {
//		    // just create a new query from the original query

//		    }
		}
	    }

	}
	return tmpEquivalent;
    }

    protected void writeAnswer(List<Match> matches)
    {
	log.debug("Calling write answer");
	answerWriter.println("<query-result>");
	for (Match match : matches)
	{
	    if (match.skip) 
		continue;

	    
		log.info("Match Rate " + match.queryMatchRate + " " 
			+ match.matchGraph.modelURI);
		log.info("Matched Query Graph");

		// following is a tweak to print the query to the log instead of System.out or a file
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream queryPrint = new PrintStream(bos);
		match.matchedQuery.print(queryPrint);
		log.info(bos.toString());
	    

	    match.matchGraph.exportXMLDetailed(answerWriter);
	}
	answerWriter.println("</query-result>");

    }

    public void runSemanticQuery(QueryGraph qry, float threshold)
    {
	List<Match> result = doWork(qry, threshold);
	writeAnswer(result);
    }
    public List<Match> runSemanticQueryReturnResult(QueryGraph qry, float threshold)
    {
	List<Match> result = doWork(qry, threshold);
	return result;
    }
    private List<Match> doWork(QueryGraph qry, float threshold)
    {
	List<QueryGraph> semanticEquivalent = expandQuery(qry, threshold);
	if (qProcessor == null)
	    qProcessor = new OryxMemoryQueryProcessor(answerWriter);//new DatabaseQueryProcessor(answerWriter);
	
	log.info("Total Expanded Queries :" + semanticEquivalent.size());

	List<Match> result = new ArrayList<Match>(10);
	List<Match> resultp = new ArrayList<Match>(10);
//	try {
//	
//	    PrintStream ps = new PrintStream(new File("/Users/ahmedawad/Downloads/PPM Experiments/QueriesForThreshold0.5.txt"));
//	    for (int i = 0 ; i < semanticEquivalent.size(); i++)
//		{
//		    ps.println("########################## Query "+ i + "##########################");
//		    semanticEquivalent.get(i).print(ps);
//		}
//	    	ps.close();
//	
//	} catch (IOException ioex) {
//	    ioex.printStackTrace();
//	} 
	
	for (int i = 0 ; i < semanticEquivalent.size(); i++)
	{
	    resultp = qProcessor.processMultiQuery(semanticEquivalent.get(i));
	    if (resultp.size() == 0)
		
		log.info("query #" + i +" did not find a match");
	    else 
		log.info("query #" + i +" found a match with value "+ semanticEquivalent.get(i).getMatchRate());
	    
//	    boolean supGraphFound = false;
	    for (int j = 0; j < resultp.size();j++)
		if (!result.contains(resultp.get(j)))
		{
		    result.add(resultp.get(j));
//		    for (int ii = 0 ; ii < result.size();ii++)
//		    {
////			supGraphFound = false;
//			Match currMatch = result.get(ii);
//			if (currMatch.matchGraph.hasSubGraph(resultp.get(j).matchGraph))
//			{
////			    supGraphFound = true;
//			    break;
//			}
//			if (currMatch.queryMatchRate < resultp.get(j).queryMatchRate)
//			{
//			    result.add(ii, resultp.get(j));
//			    break;
//			}
//
//		    }
		}
	}
	//compactAnswerSet(result);
	return result;
    }
}