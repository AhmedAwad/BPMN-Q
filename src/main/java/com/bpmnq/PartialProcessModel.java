/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmnq;

import java.util.ArrayList;
import java.util.List;

import com.bpmnq.GraphObject.GraphObjectType;



/**
 *
 * @author nemo
 */
public class PartialProcessModel {

    private List<QueryGraph> queries;
    private List<GraphObject> nodes;
    private List<SequenceFlow> edges;
    private List<SequenceFlow> specialEdges;
    private final int PM = 1; // this is a process model
    private final int PPM = 2; // this is a partial process model
//    // Added by Ahmed on 22.08.2011 to enhance error reporting
//    private List<String> errorTrace=null;// this is used to track query evaluation failures through multi-lvel inheritance
    public PartialProcessModel() {

        queries = new ArrayList<QueryGraph>();
        nodes = new ArrayList<GraphObject>();
        edges = new ArrayList<SequenceFlow>();
        specialEdges = new ArrayList<SequenceFlow>();
    }
    private class EvaluationResult
    {
	ProcessGraph result;
	List<String> errorTrace;
    }
//    public PartialProcessModel(List<String> errTrace)
//    {
//	this();
//	this.errorTrace =  new ArrayList<String>(errTrace.size());
//	for(String s : errTrace)
//	    this.errorTrace.add(s);
//    }

    public void addQueryGraph(QueryGraph qg) {
        System.out.println("a querygraph has been added ");
        if (this.queries == null) {
            this.queries = new ArrayList<QueryGraph>();
            this.queries.add(qg);
        } else {
            this.queries.add(qg);
        }
    }

    public void addNode(GraphObject go) {
      //  System.out.println("a normal node has been added ");
        if(this.nodes==null){
            this.nodes=new ArrayList<GraphObject>();
            this.nodes.add(go);
        }else{
            this.nodes.add(go);
        }

    }

    public void addEdge(SequenceFlow sf) {
   // System.out.println("a normal edge has been added ");
    if(this.edges==null){
            this.edges=new ArrayList<SequenceFlow>();
            this.edges.add(sf);
        }else{
            this.edges.add(sf);
        }

    }

    public void addSpecialEdge(SequenceFlow sf) {
   // System.out.println("a special edge has been added");
    if(this.specialEdges==null){
            this.specialEdges=new ArrayList<SequenceFlow>();
            this.specialEdges.add(sf);
        }else{
            this.specialEdges.add(sf);
        }

    }
    
    // Added by Ahmed Awad on 9.8.2010
    public List<QueryGraph> getQueryGraphs()
    {
	return queries;
    }
    public List<SequenceFlow> getSpecialEdgeWithTarget(GraphObject target)
    {
	List<SequenceFlow> result = new ArrayList<SequenceFlow>();
	for (SequenceFlow seq : specialEdges)
	{
	    if (seq.getDestinationGraphObject().equals(target))
		result.add(seq);
	}
	return result;
    }

    public List<SequenceFlow> getSpecialEdgeWithSource(GraphObject source)
    {
	List<SequenceFlow> result = new ArrayList<SequenceFlow>();
	for (SequenceFlow seq : specialEdges)
	{
	    if (seq.getSourceGraphObject().equals(source))
		result.add(seq);
	}
	return result;
    }
    public List<GraphObject> getNodes()
    {
	return nodes;
    }
    public List<SequenceFlow> getEdges()
    {
	return edges;
    }
    
    public List<SequenceFlow> getSpecialEdges()
    {
	return specialEdges;
    }
    private void cleanConfigurationNodes(ProcessGraph result)
    {
	List<GraphObject> xorNodes = new ArrayList<GraphObject>();
	for (GraphObject nd : result.nodes)
	{
	    // generalized the condition to any gateway nodes
//	    if (nd.type == GraphObjectType.GATEWAY && nd.type2.contains("XOR"))
	    if (nd.type == GraphObjectType.GATEWAY)
	    {
		List<GraphObject> preds, succs;
		preds = result.getPredecessorsFromGraph(nd);
		
		succs = result.getSuccessorsFromGraph(nd);
		if (preds.size() == 1 && succs.size() == 1)
		{
		    xorNodes.add(nd);
		}
	    }
	}
	for (GraphObject xor : xorNodes)
	{
	    List<GraphObject> preds, succs;
	    preds = result.getPredecessorsFromGraph(xor);
	    succs = result.getSuccessorsFromGraph(xor);
	    result.remove(xor);
	    result.removeEdgesWithDestination(xor);
	    result.removeEdgesWithSource(xor);
	    result.addEdge(preds.get(0), succs.get(0));
	}
    }
//    private void resetErrorTrace()
//    {
//	errorTrace = new ArrayList<String>();
//    }
    private void copyErrorTrace(List<String> src, List<String> dst)
    {
	dst =  new ArrayList<String>(src.size());
	for(String s : src)
	    dst.add(s);
    }
    public ProcessGraph evaluatePPM(QueryProcessor qProcessor)
    {
	List<String> errorTrace = new ArrayList<String>();
//	resetErrorTrace();, er
	EvaluationResult er = evaluatePPMRec(qProcessor,0);
//	ProcessGraph result = evaluatePPMRec(qProcessor, errorTrace);
	// do some analysis then return the modified reulst
//	System.out.println("+++++++++++++ PRINTING ERROR TRACE");
//	for (String s : errorTrace)
//	    System.out.println(s);
//	errorTrace = null;
	return er.result;
    }
    private EvaluationResult evaluatePPMRec(QueryProcessor qProcessor, int recDpth)
    {
	int recDepth = recDpth;
	EvaluationResult er = new EvaluationResult();
	List<String> errorTrace = new ArrayList<String>();
	er.result = evaluatePPMRec(qProcessor, errorTrace,recDepth);
	er.errorTrace = errorTrace;
	return er;
    }
    private ProcessGraph evaluatePPMRec(QueryProcessor qProcessor, List<String> errorTrace, int rcDepth)
    {
	int recDepth = rcDepth;
	System.out.println("############ Printing queries");
	 List<QueryGraph> queries = this.getQueryGraphs();
	 // Initialize the final resulting process graph
	 ProcessGraph result = new ProcessGraph();
	 
	 result.nodes.addAll(this.getNodes());
	 result.edges.addAll(this.getEdges());
	 for (QueryGraph q : queries)
	 { 
	     
	     System.out.println("PRINTING QUERY");
	     q.print(System.out);

//	     we need to add a preprocessing here.
	     // all nodes that have their labels as $# have to be assigned a unique label
//	     for (int i = 0 ; i < q.nodes.size();i++)
//	     {
//		 if (q.nodes.get(i).getName().equals("$#"))
//		 {
//		     q.nodes.get(i).setName("$#"+q.nodes.get(i).getID()+"$#");
//		     // also all edges with source or destination have to be updated
//		 }
//	     }
	     
	     // now start processing each query
	     // on 14.01.2010: we need to traverse up the hierarchy to evaluate each intermediate PPM.
	     
	     // we have to check whether the parent is an actual process or another PPM
	     ProcessGraph parent = new ProcessGraph();
	     if (isPMOrPPM(q.modelURI)==PM)
	     {
		 parent.loadFromOryx(q.modelURI);
	     }
	     else
	     {
		 ORYXModelXMLParser xp=new ORYXModelXMLParser();
		 
		 xp.createModel(q.modelURI);
//		 xp.createModel("http://adage.cse.unsw.edu.au:9090/backend/poem/model/48/rdf");
//		 xp.createModel("http://oryx-project.org/backend/poem/model/11745/rdf");
		 PartialProcessModel ppm = xp.getPartialProcessModel();
//		 ppm.setErrorTrace(this.errorTrace);
		 parent = ppm.evaluatePPMRec(qProcessor, errorTrace,recDepth+1);
		 parent.modelURI = q.modelURI;
	     }
	     ProcessGraph match = qProcessor.runQueryAgainstModel(q, parent);
	     if (match.nodes.size() > 0)
	     {
		 System.out.println("---------- Match to the query");

		 match.print(System.out);
		 
	     }
	     else // some query didn't find a match
	     {
		 GraphObject dummy = new GraphObject();
		 dummy.type = GraphObjectType.ACTIVITY;
		 
		 
		 // here loop on errortrace so far and add the explanation to the nodes text
		 StringBuffer errTxt = new StringBuffer();
		 errTxt.append("View "+q.getQueryNodeName()+ " didn't find a match within model "+ q.modelURI);
		 if (errorTrace.size() > 0)
		     errTxt.append(" possibly because:\n");
		 for (String s: errorTrace)
		     errTxt.append(s+"\n");
		 errorTrace.add("View "+q.getQueryNodeName()+ " didn't find a match within model "+ q.modelURI);
		 dummy.setName(errTxt.toString());
		 dummy.setID(q.getQueryNodeID());
		 dummy.setBoundQueryObjectID(q.getQueryNodeID());
		 result.add(dummy);
		 //now look for all special edges incoming or out going from that unmatched query
		 // and update their references to the dummy node
		 for (GraphObject nd : q.nodes)
		 {
		     List<SequenceFlow> target = this.getSpecialEdgeWithTarget(nd);
		     
		     for (SequenceFlow sff : target)
		     {
			 this.removeSpecialEdge(sff);
			 SequenceFlow updated = new SequenceFlow(sff.getSourceGraphObject(),dummy);
			 this.addSpecialEdge(updated);
		     }
		     
		     target.clear();
		     target = this.getSpecialEdgeWithSource(nd);
		     for (SequenceFlow sff : target)
		     {
			 this.removeSpecialEdge(sff);
			 SequenceFlow updated = new SequenceFlow(dummy,sff.getDestinationGraphObject());
			 this.addSpecialEdge(updated);
		     }
		 }
	     }
	     for (GraphObject o : match.nodes)
	     {
		if (! result.add(o))
		{
		    // there is an overlap with the result of another query
		    for (GraphObject other : result.nodes)
		    {
			if (other.equals(o))
			{
			    other.setBoundQueryObjectID(o.getBoundQueryObjectID());
			}
		    }
		    
		}
	     }
	     for (SequenceFlow sf : match.edges)
		 result.add(sf);
	     if (recDepth == 0)
		 errorTrace.clear();
//	     System.out.println("---------- End match to the query");
	 }
	 
	 // finally, resolve special edges
	 for (SequenceFlow sf : this.getSpecialEdges())
	 {
	     GraphObject bSource,bDestination;
	     bSource = result.getBoundNodeToID(sf.getSourceGraphObject().getID());
	     bDestination = result.getBoundNodeToID(sf.getDestinationGraphObject().getID());
	     
	     if (bSource == null)
		 bSource = sf.getSourceGraphObject();
	     if (bDestination == null)
		 bDestination = sf.getDestinationGraphObject();
	     
	     result.addEdge(bSource, bDestination);
	 }
//	 System.out.println("Final process graph is:");
	 // Finaly clean all negative IDS for nodes on the root level
	 for (GraphObject ob : result.nodes)
	 {
	     if (ob.getID().startsWith("-"))
	     {
		 ob.setID(ob.getID().substring(1));
		 
	     }
	 }
	 for (SequenceFlow seq: result.edges)
	 {
	     if (seq.frmActivity != null && seq.frmActivity.actID.startsWith("-"))
		 seq.frmActivity.actID = seq.frmActivity.actID.substring(1);
	     
	     if (seq.frmGateWay != null && seq.frmGateWay.gateID.startsWith("-"))
		 seq.frmGateWay.gateID = seq.frmGateWay.gateID.substring(1);
	     
	     if (seq.frmEvent != null && seq.frmEvent.eventID.startsWith("-"))
		 seq.frmEvent.eventID = seq.frmEvent.eventID.substring(1);
	     
	     if (seq.toActivity != null && seq.toActivity.actID.startsWith("-"))
		 seq.toActivity.actID = seq.toActivity.actID.substring(1);
	     
	     if (seq.toGateWay != null && seq.toGateWay.gateID.startsWith("-"))
		 seq.toGateWay.gateID = seq.toGateWay.gateID.substring(1);
	     
	     if (seq.toEvent != null && seq.toEvent.eventID.startsWith("-"))
		 seq.toEvent.eventID = seq.toEvent.eventID.substring(1);
	 }
	 normalizeEdgeNodeIDs(result);
	 cleanConfigurationNodes(result);
	 return result;
    }
    private void normalizeEdgeNodeIDs(ProcessGraph result)
    {
	for (GraphObject o : result.nodes)
	{
	    for (SequenceFlow seq :result.edges)
	    {
		if (o.type == GraphObjectType.ACTIVITY)
		{
		    if (seq.frmActivity != null && seq.frmActivity.actID.equals(o.getID()))
			seq.frmActivity.name = o.getName();
		    
		    if (seq.toActivity != null && seq.toActivity.actID.equals(o.getID()))
			seq.toActivity.name = o.getName();
		}
		else if (o.type == GraphObjectType.EVENT)
		{
		    if (seq.frmEvent != null && seq.frmEvent.eventID.equals(o.getID()))
			seq.frmEvent.eventName = o.getName();
		    
		    if (seq.toEvent != null && seq.toEvent.eventID.equals(o.getID()))
			seq.toEvent.eventName = o.getName();
		}
		else if (o.type == GraphObjectType.GATEWAY)
		{
		    if (seq.frmGateWay != null && seq.frmGateWay.gateID.equals(o.getID()))
			seq.frmGateWay.name = o.getName();
		    
		    if (seq.toGateWay != null && seq.toGateWay.gateID.equals(o.getID()))
			seq.toGateWay.name = o.getName();
		}
	    }
	}
    }
    public void removeSpecialEdge(SequenceFlow s)
    {
	this.specialEdges.remove(s);
    }
    public GraphObject getNodeByID(String id)
    {
	for (GraphObject g : nodes)
	{
	    if (g.getID().equals(id))
		return g;
	}
	return null;
    }
    public void print()
    {
	System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
	
	for (GraphObject nd : nodes)
	{
	    System.out.println(nd.toString());
	}
	System.out.print("Edges");
	for (SequenceFlow sq : edges)
	{
	    System.out.println(sq.toString());
	}
	System.out.print("Special Edges");
	for (SequenceFlow sq : specialEdges)
	{
	    System.out.println(sq.toString());
	}
	
	System.out.print("Queries");
	for (QueryGraph q: queries)
	{
	    q.print(System.out);
	}
	
    }
    private int isPMOrPPM(String modelURI)
    {
	ORYXModelXMLParser xp=new ORYXModelXMLParser();

	xp.createModel(modelURI);
	//	 xp.createModel("http://adage.cse.unsw.edu.au:9090/backend/poem/model/48/rdf");
	//	 xp.createModel("http://oryx-project.org/backend/poem/model/11745/rdf");
	PartialProcessModel ppm = xp.getPartialProcessModel();
	if (ppm.getQueryGraphs().size()==0)
	    return PM;
	else
	    return PPM;
    }
}
