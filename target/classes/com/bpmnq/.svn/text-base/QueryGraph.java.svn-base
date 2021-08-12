package com.bpmnq;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

import org.apache.log4j.Logger;

import com.bpmnq.Path.TemporalType;
import com.bpmnq.compliancechecker.TemporalQueryGraph;

import static com.bpmnq.GraphObject.GraphObjectType.*;

public  class QueryGraph extends ProcessGraph implements Cloneable
{
    public List<Path> paths;
    public List<SequenceFlow> negativeEdges;
    public List<SequenceFlow> negativePaths;
    /** added to handle querying with data */
    public List<UndirectedAssociation> dataPathAssociations;
    public StringBuilder forbiddenEventIDs;
    public StringBuilder forbiddenGatewayIDs;
    public StringBuilder forbiddenActivityIDs;
    public StringBuilder genericSplitBindings;
    public StringBuilder genericJoinBindings;
    /** a single string representing a data object along with its state */
    public StringBuilder forbiddenDataObjects;

    private List<CommonBinding> bindings;
    private int dummyNodeID;
    // these two properties are added for the processing of Partial Process models, 9.9.2010
    private String queryNodeID;
    private String queryNodeName;
    /** added specially for semantic expansion of queries */
    private float matchRate = 1.0f;
    public void setMatchRate(float matchRate)
    {
        this.matchRate = matchRate;
    }
    // added for the logging functionality
    private List<String> infoLogs;
    private List<String> errorLogs;
    
    private Logger log = Logger.getLogger(QueryGraph.class); 
    private QueryProcessorDirective directive;
    
    public QueryGraph() 
    {
	super();
	paths = new ArrayList<Path>();
	negativeEdges = new ArrayList<SequenceFlow>();
	negativePaths = new ArrayList<SequenceFlow>();
	dataPathAssociations = new ArrayList<UndirectedAssociation>();
	forbiddenEventIDs = new StringBuilder(20);
	forbiddenEventIDs.append("0");

	forbiddenGatewayIDs = new StringBuilder(20);
	forbiddenGatewayIDs.append("0");

	forbiddenActivityIDs = new StringBuilder(20);
	forbiddenActivityIDs.append("0");
	
	forbiddenDataObjects = new StringBuilder(20);
	forbiddenDataObjects.append("0");
	
	dummyNodeID = -1;
	infoLogs = new ArrayList<String>(10);
	errorLogs = new ArrayList<String>(10);
	bindings = new ArrayList<CommonBinding>();
	genericJoinBindings = new StringBuilder(20);
	genericSplitBindings = new StringBuilder(20);
	
    }

    public Object clone() {
	    // this copies all primitive fields already
	    QueryGraph clone = (QueryGraph)super.clone();
	    
	    clone.paths = new ArrayList<Path>(this.paths.size());
	    for (Path path : this.paths)
	    {
	        Path pClone = (Path)path.clone();
	        clone.paths.add(pClone);
	    }
	    
	    clone.negativeEdges = new ArrayList<SequenceFlow>(this.negativeEdges.size());
	    for (SequenceFlow negEdge : this.negativeEdges)
	    {
	        SequenceFlow neClone = (SequenceFlow)negEdge.clone();
	        clone.negativeEdges.add(neClone);
	    }

	    clone.negativePaths = new ArrayList<SequenceFlow>(this.negativePaths.size());
	    for (SequenceFlow negPath : this.negativePaths)
	    {
	        SequenceFlow npClone = (SequenceFlow)negPath.clone();
	        clone.negativePaths.add(npClone);
	    }
	    
	    clone.forbiddenActivityIDs = new StringBuilder(this.forbiddenActivityIDs);
	    clone.forbiddenEventIDs = new StringBuilder(this.forbiddenEventIDs);
	    clone.forbiddenGatewayIDs = new StringBuilder(this.forbiddenGatewayIDs);
	    clone.forbiddenDataObjects = new StringBuilder(this.forbiddenDataObjects);
	    clone.genericJoinBindings = new StringBuilder(this.genericJoinBindings);
	    clone.genericSplitBindings = new StringBuilder(this.genericSplitBindings);
	    clone.bindings = new ArrayList<CommonBinding>(this.bindings.size());
	    for (CommonBinding cb : this.getCommonBindings())
	    {
	        CommonBinding cbClone = (CommonBinding)cb.clone();
	        clone.bindings.add(cbClone);
	    }
	    
	    clone.infoLogs = new ArrayList<String>();
	    for (String logMsg : this.infoLogs)
	        clone.addInfoLog(logMsg);
	    clone.errorLogs = new ArrayList<String>();
	    for (String logMsg : this.errorLogs)
	        clone.addErrorLog(logMsg);
	    
	    clone.dataPathAssociations = new ArrayList<UndirectedAssociation>(this.dataPathAssociations.size());
	    for (UndirectedAssociation unia : this.dataPathAssociations)
	    {
	        UndirectedAssociation uaClone = (UndirectedAssociation)unia.clone();
	        clone.dataPathAssociations.add(uaClone);
	    }
	    if (this.getProcessorDirectives() != null)
	    {
		clone.setAllowGenericShapeToEvaluateToNone(this.directive.allowGenericShapeToEvaluateToNone);
		clone.setAllowIncludeEnclosingAndSplitDirective(this.directive.includeEnclosingANDSplit);
		clone.setStopAtFirstMatch(this.directive.stopAtFirstMatch);
	    }
	    return clone;
    }
    public TemporalQueryGraph getTemporalQueryGraph() {
	    // this copies all primitive fields already
	    TemporalQueryGraph clone = new TemporalQueryGraph();
	    try
	    {
		clone.nodes = new ArrayList<GraphObject>(this.nodes.size());
		for (GraphObject node : this.nodes)
		{
		    GraphObject nClone = (GraphObject)node.clone();
		    clone.add(nClone);
		}

		clone.edges = new ArrayList<SequenceFlow>(this.edges.size());
		for (SequenceFlow edge : this.edges)
		{
		    SequenceFlow eClone = (SequenceFlow)edge.clone();
		    clone.add(eClone);
		}

		clone.dataObjs = new ArrayList<DataObject>(this.dataObjs.size());
		for (DataObject dObj : this.dataObjs)
		{
		    DataObject doClone = (DataObject)dObj.clone();
		    clone.add(doClone);
		}

		clone.associations = new ArrayList<Association>(this.associations.size());
		for (Association assoc : this.associations)
		{
		    Association aClone = (Association)assoc.clone();
		    clone.add(aClone);
		}

		clone.paths = new ArrayList<Path>(this.paths.size());
		for (Path path : this.paths)
		{
		    Path pClone = (Path)path.clone();
		    clone.paths.add(pClone);
		}

		clone.negativeEdges = new ArrayList<SequenceFlow>(this.negativeEdges.size());
		for (SequenceFlow negEdge : this.negativeEdges)
		{
		    SequenceFlow neClone = (SequenceFlow)negEdge.clone();
		    clone.negativeEdges.add(neClone);
		}

		clone.negativePaths = new ArrayList<SequenceFlow>(this.negativePaths.size());
		for (SequenceFlow negPath : this.negativePaths)
		{
		    SequenceFlow npClone = (SequenceFlow)negPath.clone();
		    clone.negativePaths.add((Path)npClone);
		}




		clone.dataPathAssociations = new ArrayList<UndirectedAssociation>(this.dataPathAssociations.size());
		for (UndirectedAssociation unia : this.dataPathAssociations)
		{
		    UndirectedAssociation uaClone = (UndirectedAssociation)unia.clone();
		    clone.dataPathAssociations.add(uaClone);
		}
		return clone;
		
	    } 
	    catch (CloneNotSupportedException e)
	    {
		return null;
	    }
}
    public void updateMatchRate(float rate) {
	this.matchRate *= rate;
    }

    public float getMatchRate() {
	return this.matchRate;
    }
//    public boolean isEquivalentTo(QueryGraph other)
//    {
//	if (this.associations.size() != other.associations.size())
//	    return false;
//	
//	if (this.dataObjs.size() != other.dataObjs.size())
//	    return false;
//	
//	if (this.dataPathAssociations.size() != other.dataPathAssociations.size())
//	    return false;
//	if (this.edges.size() != other.edges.size())
//	    return false;
//	if (this.negativeEdges.size() != other.negativeEdges.size())
//	    return false;
//	if (this.negativePaths.size() != other.negativePaths.size())
//	    return false;
//	if (this.nodes.size() != other.nodes.size())
//	    return false;
//	if (this.paths.size() != other.paths.size())
//	    return false;
//	
//	for (GraphObject g : this.nodes)
//	{
//	    for (GraphObject gg : other.nodes)
//	    {
//		
//	    }
//	}
//	
//	return true;
//    }
    public List<String> getSignature()
    {
	List<String> result = new ArrayList<String>(this.nodes.size() +this.dataObjs.size() + this.associations.size()
		+this.dataPathAssociations.size()+this.edges.size()+this.negativeEdges.size()+
		this.negativePaths.size()+this.paths.size());
	for (GraphObject o : this.nodes)
	{
	    result.add(o.toString());
	}
	for (Path p : this.paths)
	{
	    result.add(p.toString());
	}
	return result;
    }
    public boolean equals(Object other) {
	if (!(other instanceof QueryGraph))
	    return false;

	QueryGraph anOther = (QueryGraph) other;
	if (this.associations.size() != anOther.associations.size())
	    return false;
	
	if (this.dataObjs.size() != anOther.dataObjs.size())
	    return false;
	
	if (this.dataPathAssociations.size() != anOther.dataPathAssociations.size())
	    return false;
	if (this.edges.size() != anOther.edges.size())
	    return false;
	if (this.negativeEdges.size() != anOther.negativeEdges.size())
	    return false;
	if (this.negativePaths.size() != anOther.negativePaths.size())
	    return false;
	if (this.nodes.size() != anOther.nodes.size())
	    return false;
	if (this.paths.size() != anOther.paths.size())
	    return false;
//	
//	boolean result = true;
	//if (this.nodes.size() > 0 && anOther.nodes.size() > 0)
	//    result = result && this.nodes.containsAll(anOther.nodes);
	if (!this.nodes.containsAll(anOther.nodes))
	    return false;
	if (!this.edges.containsAll(anOther.edges))
	    return false;
	if (!this.negativeEdges.containsAll(anOther.negativeEdges))
	    return false;
	if(!this.paths.containsAll(anOther.paths))
	    return false;
	if(!this.negativePaths.containsAll(anOther.negativePaths))
	    return false;
	if(!this.associations.containsAll(anOther.associations))
	    return false;
	if(!this.dataPathAssociations.containsAll(anOther.dataPathAssociations))
	    return false;
	if(!this.dataObjs.containsAll(anOther.dataObjs))
	    return false;
	
//	if (this.edges.size() > 0 && anOther.edges.size() > 0)
//	    result = result && this.edges.containsAll(anOther.edges);
//	if (this.negativeEdges.size() > 0 && anOther.negativeEdges.size() > 0)
//	    result = result && this.negativeEdges.containsAll(anOther.negativeEdges);
//	if (this.paths.size() > 0 && anOther.paths.size() > 0)
//	    result = result && this.paths.containsAll(anOther.paths);
//	if (this.negativePaths.size() > 0 && anOther.negativePaths.size() > 0)
//	    result = result && this.negativePaths.containsAll(anOther.negativePaths);
//	if (this.associations.size() > 0 && anOther.associations.size() > 0)
//	    result = result && this.associations.containsAll(anOther.associations);
//	if (this.dataPathAssociations.size() > 0 && anOther.dataPathAssociations.size() > 0)
//	    result = result && this.dataPathAssociations.containsAll(anOther.dataPathAssociations);
//	if (this.dataObjs.size() > 0 && anOther.dataObjs.size() > 0)
//	    result = result && this.dataObjs.containsAll(anOther.dataObjs);
	
	return true;
    }

    public boolean add(GraphObject go)
    {
	if (!nodes.contains(go))
	{
	    if (go.getID().equals("0"))
		go.setID(String.valueOf(dummyNodeID));
	    nodes.add(go);
	    dummyNodeID--;
	    return true;
	}
	return false;
    }

    public void add(SequenceFlow sq) {
	if (!negativeEdges.contains(sq)) {
	    super.add(sq);
	}
    }

    public void addNegativeEdge(GraphObject from, GraphObject to) {
	SequenceFlow sq = new SequenceFlow(from, to);
	addNegativeEdge(sq);
    }

    public void addNegativeEdge(SequenceFlow sq) {
	if (!edges.contains(sq) && !negativeEdges.contains(sq)) {
	    negativeEdges.add(sq);
	}
    }

    public void add(Path sq) {
	if (!paths.contains(sq) && !negativePaths.contains(sq)) {
	    paths.add(sq);
	}
    }

    public void addNegativePath(GraphObject from, GraphObject to) {
	SequenceFlow sq = new SequenceFlow(from, to);
	addNegativePath(sq);
    }

    public void addNegativePath(SequenceFlow sq) {
	if (!paths.contains(sq) && !negativePaths.contains(sq)) {
	    negativePaths.add(sq);
	}
    }

//  private GraphObject getSuccessorObject(SequenceFlow currentEdge, GraphObject node)
//{
//  GraphObject temp = new GraphObject();
//if (node.type1.equals("Activity"))
//  {
//  if (currentEdge.frmActivity != null)
//  {
//  if (currentEdge.frmActivity.actName.equals(node.name))
//  {

//  if (currentEdge.toActivity != null)
//  {
//  temp.id = currentEdge.toActivity.actID;
//  temp.name = currentEdge.toActivity.actName;
//  temp.type1 = "Activity";
//  if (currentEdge.toActivity.actName.startsWith("$#"))
//  temp.type2 ="GENERIC SHAPE";
//  }
//  else if (currentEdge.toEvent != null)
//  {
//  temp.id = currentEdge.toEvent.eventID;
//  temp.name = currentEdge.toEvent.eventName;
//  temp.type1 = "Event";
//  temp.type2 = currentEdge.toEvent.eventType + currentEdge.toEvent.eventPosition;
//  }
//  else// if (currentEdge.toGateWay != null)
//  {
//  temp.id = currentEdge.toGateWay.gateID;
//  temp.name = currentEdge.toGateWay.gateWayName;
//  temp.type1 = "GateWay";
//  temp.type2 = currentEdge.toGateWay.gateWayType;
//  }
//  //succs.add(temp);
//  }
//  }
//  }
//  else if (node.type1.equals("Event"))
//  {
//  if (currentEdge.frmEvent != null)
//  {
//  if (currentEdge.frmEvent.eventName.equals(node.name))
//  {

//  if (currentEdge.toActivity != null)
//  {
//  temp.id = currentEdge.toActivity.actID;
//  temp.name = currentEdge.toActivity.actName;
//  temp.type1 = "Activity";
//  if (currentEdge.toActivity.actName.startsWith("$#"))
//  temp.type2 ="GENERIC SHAPE";
//  }
//  else if (currentEdge.toEvent != null)
//  {
//  temp.id = currentEdge.toEvent.eventID;
//  temp.name = currentEdge.toEvent.eventName;
//  temp.type1 = "Event";
//  temp.type2 = currentEdge.toEvent.eventType + currentEdge.toEvent.eventPosition;
//  }
//  else //if (currentEdge.toGateWay != null)
//  {
//  temp.id = currentEdge.toGateWay.gateID;
//  temp.name = currentEdge.toGateWay.gateWayName;
//  temp.type1 = "GateWay";
//  temp.type2 = currentEdge.toGateWay.gateWayType;
//  }
//  //succs.add(temp);
//  }
//  }
//  }
//  else //if (node.type1.equals("GateWay"))
//  {
//  if (currentEdge.frmGateWay != null)
//  {
//  if (currentEdge.frmGateWay.gateWayName.equals(node.name))
//  {

//  if (currentEdge.toActivity != null)
//  {
//  temp.id = currentEdge.toActivity.actID;
//  temp.name = currentEdge.toActivity.actName;
//  temp.type1 = "Activity";
//  if (currentEdge.toActivity.actName.startsWith("$#"))
//  temp.type2 ="GENERIC SHAPE";
//  }
//  else if (currentEdge.toEvent != null)
//  {
//  temp.id = currentEdge.toEvent.eventID;
//  temp.name = currentEdge.toEvent.eventName;
//  temp.type1 = "Event";
//  temp.type2 = currentEdge.toEvent.eventType + currentEdge.toEvent.eventPosition;
//  }
//  else //if (currentEdge.toGateWay != null)
//  {
//  temp.id = currentEdge.toGateWay.gateID;
//  temp.name = currentEdge.toGateWay.gateWayName;
//  temp.type1 = "GateWay";
//  temp.type2 = currentEdge.toGateWay.gateWayType;
//  }
//  //succs.add(temp);
//  }
//  }
//  }
//  return temp;
//  }

    public ArrayList<GraphObject> getPredecessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> preds = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < edges.size();i++)
	{
	    currentEdge = edges.get(i);
	    temp = getPredecessorObject(currentEdge, node);
	    if (temp.getName().startsWith("@"))
		continue;
	    if (temp.type2.equals("GENERIC SHAPE"))
		continue;
	    if (temp.type2.equals("GENERIC SPLIT"))
		continue;
	    if (temp.type != UNDEFINED)
		preds.add(temp);

	}
	return preds;
    }
    
    public ArrayList<GraphObject> getGenericPredecessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> preds = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < edges.size();i++)
	{
	    currentEdge = edges.get(i);
	    temp = getPredecessorObject(currentEdge, node);
	    
	    if (temp.type2.equals("GENERIC SHAPE"))
			preds.add(temp);
	    

	}
	return preds;
    }

    public ArrayList<GraphObject> getGenericSuccessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> preds = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < edges.size();i++)
	{
	    currentEdge = edges.get(i);
	    temp = getSuccessorObject(currentEdge, node);
	    
	    if (temp.type2.equals("GENERIC SHAPE"))
			preds.add(temp);
	    

	}
	return preds;
    }
    public ArrayList<GraphObject> getNegativePredecessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> preds = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < negativeEdges.size();i++)
	{
	    currentEdge = negativeEdges.get(i);
	    temp = getPredecessorObject(currentEdge, node);
	    if (temp.type != UNDEFINED)
		preds.add(temp);
	}
	return preds;
    }

    public ArrayList<GraphObject> getPathPredecessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> preds = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < paths.size();i++)
	{
	    currentEdge = paths.get(i);
	    temp = getPredecessorObject(currentEdge, node);
	    if (temp.type != UNDEFINED)
		preds.add(temp);
	}
	return preds;
    }

    public ArrayList<GraphObject> getNegativePathPredecessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> preds = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < negativePaths.size();i++)
	{
	    currentEdge = negativePaths.get(i);
	    temp = getPredecessorObject(currentEdge, node);
	    if (temp.type != UNDEFINED)
		preds.add(temp);
	}
	return preds;
    }

    public ArrayList<GraphObject> getSuccessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> succs = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < edges.size();i++)
	{
	    currentEdge = edges.get(i);
	    temp = getSuccessorObject(currentEdge, node);
	    if (temp.getName().startsWith("@"))
		continue;
	    if (temp.type2.equals("GENERIC SHAPE"))
		continue;
	    if (temp.type2.equals("GENERIC SPLIT"))
		continue;
	    if (temp.type != UNDEFINED)
		succs.add(temp);
	}
	return succs;
    }

    public ArrayList<GraphObject> getNegativeSuccessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> succs = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < negativeEdges.size();i++)
	{
	    currentEdge = negativeEdges.get(i);
	    temp = getSuccessorObject(currentEdge, node);
	    if (temp.type != UNDEFINED)
		succs.add(temp);
	}
	return succs;
    }

    public ArrayList<GraphObject> getPathSuccessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> succs = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < paths.size();i++)
	{
	    currentEdge = paths.get(i);
	    temp = getSuccessorObject(currentEdge, node);
	    if (temp.type != UNDEFINED)
		succs.add(temp);
	}
	return succs;
    }
    public String getPathExcludeStatement(GraphObject frm, GraphObject too)
    {
    	String result;
    	
    	SequenceFlow seq = new SequenceFlow(frm, too);
    	int indx = paths.indexOf(seq);
    	if (indx < 0)
    		return "";
    	result = paths.get(indx).exclude;
    	
    	return result;
    }
    
    public String getPathLabel(GraphObject frm, GraphObject too)
    {
    	String result;
    	
    	SequenceFlow seq = new SequenceFlow(frm, too);
    	int indx = paths.indexOf(seq);
    	if (indx < 0)
    		return "";
    	result = paths.get(indx).label;
    	
    	return result;
    }
    
    public ArrayList<GraphObject> getNegativePathSuccessorsFromQueryGraph(GraphObject node)
    {
	ArrayList<GraphObject> succs = new ArrayList<GraphObject>();
	GraphObject temp ;
	SequenceFlow currentEdge;
	for(int i =0; i < negativePaths.size();i++)
	{
	    currentEdge = negativePaths.get(i);
	    temp = getSuccessorObject(currentEdge, node);
	    if (temp.type != UNDEFINED)
		succs.add(temp);
	}
	return succs;
    }

    public void updateEdgesWithDestination(GraphObject node,String newName)
    {
	if (edges.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < edges.size();i++)
	{
	    currentEdge = edges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.name.equals(node.getName()))
		    {

			edges.get(i).toActivity.actID = node.getID();
			
			edges.get(i).toActivity.name = newName;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventName.equals(node.getName()))
		    {

			edges.get(i).toEvent.eventID = node.getID();
			edges.get(i).toEvent.eventName = newName;
			edges.get(i).toEvent.eventType = node.type2.substring(0, node.type2.length()-1);
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.name.equals(node.getName()))
		    {

			edges.get(i).toGateWay.gateID = node.getID();
			edges.get(i).toGateWay.name = newName;
		    }
	    }
	}

    }

    /**
     * Insert a undirected association into the graph.
     *@param undirAssoc the undirected association to insert
     */
    public void addDataPathAssociation(UndirectedAssociation undirAssoc)
    {
    	if (!dataPathAssociations.contains(undirAssoc))
    		dataPathAssociations.add(undirAssoc);
    }
    
    public void removeDataPathAssociation(GraphObject data, Path p)
    {
    	UndirectedAssociation unia = new UndirectedAssociation(data,p);
    	dataPathAssociations.remove(unia);
    }
    
    public List<GraphObject> getDataObjecsAssociatedWithPath(Path p)
    {
    	List<GraphObject> result = new ArrayList<GraphObject>();
    	for (UndirectedAssociation unia :dataPathAssociations)
    	{
    		if (unia.path.equals(p))
    			result.add(unia.frmDataObject.originalNode());
    	}
    	return result;
    }
    
    public void updateNegativeEdgesWithDestination(GraphObject node,String newName)
    {
	if (negativeEdges.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < negativeEdges.size();i++)
	{
	    currentEdge = negativeEdges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.name.equals(node.getName()))
		    {

			currentEdge.toActivity.actID = node.getID();

			currentEdge.toActivity.name = newName;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventName.equals(node.getName()))
		    {

			currentEdge.toEvent.eventID = node.getID();
			currentEdge.toEvent.eventName = newName;
			edges.get(i).toEvent.eventType = node.type2.substring(0, node.type2.length()-1);
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.name.equals(node.getName()))
		    {

			currentEdge.toGateWay.gateID = node.getID();
			currentEdge.toGateWay.name = newName;
		    }
	    }
	}

    }

    public void updateNegativeEdgesWithDestination(GraphObject node,GraphObject newNode)
    {
	// this is used specially for generic shapes
	if (negativeEdges.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < negativeEdges.size();i++)
	{
	    currentEdge = negativeEdges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toActivity.actID = newNode.getID();

			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.toActivity = null;
			    currentEdge.toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.toActivity = null;
			    currentEdge.toEvent = new Event();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventName.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toEvent = null;
			    currentEdge.toActivity = new Activity();
			    currentEdge.toActivity.actID = newNode.getID();
			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.toEvent = null;
			    currentEdge.toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toEvent = new Events();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toGateWay = null;
			    currentEdge.toActivity=new Activity();
			    currentEdge.toActivity.actID = newNode.getID();
			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.toGateWay = null;
			    currentEdge.toEvent = new Event();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	}

    }

    public void updateEdgesWithDestination(GraphObject node,GraphObject newNode)
    {
	// this is used specially for generic shapes
	if (edges.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < edges.size();i++)
	{
	    currentEdge = edges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toActivity.actID = newNode.getID();

			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.toActivity = null;
			    currentEdge.toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.toActivity = null;
			    currentEdge.toEvent = new Event();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventName.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toEvent = null;
			    currentEdge.toActivity = new Activity();
			    currentEdge.toActivity.actID = newNode.getID();
			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.toEvent = null;
			    currentEdge.toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toEvent = new Events();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toGateWay = null;
			    currentEdge.toActivity=new Activity();
			    currentEdge.toActivity.actID = newNode.getID();
			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.toGateWay = null;
			    currentEdge.toEvent = new Event();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	}

    }

    public void updatePathsWithDestination(GraphObject node,GraphObject newNode)
    {
	// this is used specially for generic shapes
	if (paths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < paths.size();i++)
	{
	    currentEdge = paths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toActivity.actID = newNode.getID();

			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.toActivity = null;
			    currentEdge.toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.toActivity = null;
			    currentEdge.toEvent = new Event();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventName.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toEvent = null;
			    currentEdge.toActivity = new Activity();
			    currentEdge.toActivity.actID = newNode.getID();
			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.toEvent = null;
			    currentEdge.toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toEvent = new Events();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toGateWay = null;
			    currentEdge.toActivity=new Activity();
			    currentEdge.toActivity.actID = newNode.getID();
			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.toGateWay = null;
			    currentEdge.toEvent = new Event();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	}

    }

    public void updatePathsWithDestination(GraphObject node, String newName)
    {
	if (paths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < paths.size();i++)
	{
	    currentEdge = paths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.name.equals(node.getName()))
		    {

			paths.get(i).toActivity.actID = node.getID();
			paths.get(i).toActivity.name = newName;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventName.equals(node.getName()))
		    {

			paths.get(i).toEvent.eventID= node.getID();
			paths.get(i).toEvent.eventName= newName;
			paths.get(i).toEvent.eventPosition = Integer.parseInt(node.type2.substring(node.type2.length()-1,node.type2.length()));
			paths.get(i).toEvent.eventType = node.type2.substring(0, node.type2.length()-1);
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.name.equals(node.getName()))
		    {

			paths.get(i).toGateWay.gateID = node.getID();
			paths.get(i).toGateWay.name = newName;
		    }
	    }
	}

    }

    public void updatePathsWithDestinationID(GraphObject oldNode, GraphObject newNode)
    {
	if (paths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < paths.size();i++)
	{
	    currentEdge = paths.get(i);
	    if (oldNode.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.actID.equals(oldNode.getID()))
		    {

			paths.get(i).toActivity.actID = newNode.getID();
			paths.get(i).toActivity.name = newNode.getName();
		    }
	    }
	    else if (oldNode.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventID.equals(oldNode.getID()))
		    {

			paths.get(i).toEvent.eventID= newNode.getID();
			paths.get(i).toEvent.eventName= newNode.getName();
			paths.get(i).toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			paths.get(i).toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
		    }
	    }
	    else if (oldNode.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.gateID.equals(oldNode.getID()))
		    {

			paths.get(i).toGateWay.gateID = newNode.getID();
			paths.get(i).toGateWay.name = newNode.getName();
		    }
	    }
	}

    }
    public void updateNegativePathsWithDestination(GraphObject node,GraphObject newNode)
    {
	// this is used specially for generic shapes
	if (negativePaths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < negativePaths.size();i++)
	{
	    currentEdge = negativePaths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toActivity.actID = newNode.getID();

			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.toActivity = null;
			    currentEdge.toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.toActivity = null;
			    currentEdge.toEvent = new Event();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventName.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toEvent = null;
			    currentEdge.toActivity = new Activity();
			    currentEdge.toActivity.actID = newNode.getID();
			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.toEvent = null;
			    currentEdge.toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toEvent = new Events();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.toGateWay = null;
			    currentEdge.toActivity=new Activity();
			    currentEdge.toActivity.actID = newNode.getID();
			    currentEdge.toActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toGateWay = new GateWay();
			    currentEdge.toGateWay.gateID = newNode.getID();
			    currentEdge.toGateWay.name = newNode.getName();
			    currentEdge.toGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.toGateWay = null;
			    currentEdge.toEvent = new Event();
			    currentEdge.toEvent.eventID = newNode.getID();
			    currentEdge.toEvent.eventName= newNode.getName();
			    currentEdge.toEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.toEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	}

    }

    public void updateNegativePathsWithDestination(GraphObject node,String newName)
    {
	if (negativePaths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < negativePaths.size();i++)
	{
	    currentEdge = negativePaths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.toActivity != null)
		    if (currentEdge.toActivity.name.equals(node.getName()))
		    {

			currentEdge.toActivity.actID = node.getID();
			currentEdge.toActivity.name = newName;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.toEvent != null)
		    if (currentEdge.toEvent.eventName.equals(node.getName()))
		    {

			currentEdge.toEvent.eventID= node.getID();
			currentEdge.toEvent.eventName= newName;
			currentEdge.toEvent.eventPosition = Integer.parseInt(node.type2.substring(node.type2.length()-1,node.type2.length()));
			currentEdge.toEvent.eventType = node.type2.substring(0, node.type2.length()-1);
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.toGateWay != null)
		    if (currentEdge.toGateWay.name.equals(node.getName()))
		    {

			currentEdge.toGateWay.gateID = node.getID();
			currentEdge.toGateWay.name = newName;
		    }
	    }
	}

    }

    public void updateEdgesWithSource(GraphObject node, GraphObject newNode)
    {
	// this is used specially for generic shapes
	if (edges.size() == 0) return;

	//SequenceFlow currentEdge;
	for (int i =0; i < edges.size();i++)
	{
	    //currentEdge = edges.get(i);
	    SequenceFlow currentEdge = edges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmActivity.actID = newNode.getID();

			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventName.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmActivity = new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toEvent = new Events();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmActivity=new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	}

    }

    public void updateEdgesWithSource(GraphObject node,String newName)
    {
	if (edges.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < edges.size();i++)
	{
	    currentEdge = edges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.name.equals(node.getName()))
		    {

			currentEdge.frmActivity.actID = node.getID();
			currentEdge.frmActivity.name = newName;

		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventName.equals(node.getName()))
		    {

			currentEdge.frmEvent.eventID = node.getID();
			currentEdge.frmEvent.eventName = newName;
			currentEdge.frmEvent.eventPosition = Integer.parseInt(node.type2.substring(node.type2.length()-1,node.type2.length()));
			currentEdge.frmEvent.eventType = node.type2.substring(0, node.type2.length()-1);
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.name.equals(node.getName()))
		    {

			currentEdge.frmGateWay.gateID = node.getID();
			currentEdge.frmGateWay.name = newName;
		    }
	    }
	}

    }

    public void updateNegativeEdgesWithSource(GraphObject node,GraphObject newNode)
    {
	// this is used specially for generic shapes
	if (negativeEdges.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < negativeEdges.size();i++)
	{
	    currentEdge = negativeEdges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmActivity.actID = newNode.getID();

			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventName.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmActivity = new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toEvent = new Events();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmActivity=new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	}

    }

    public void updateNegativeEdgesWithSource(GraphObject node, String newName)
    {
	if (negativeEdges.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < negativeEdges.size();i++)
	{
	    currentEdge = negativeEdges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.name.equals(node.getName()))
		    {

			currentEdge.frmActivity.actID = node.getID();
			currentEdge.frmActivity.name = newName;

		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventName.equals(node.getName()))
		    {

			currentEdge.frmEvent.eventID = node.getID();
			currentEdge.frmEvent.eventName = newName;
			currentEdge.frmEvent.eventPosition = Integer.parseInt(node.type2.substring(node.type2.length()-1,node.type2.length()));
			currentEdge.frmEvent.eventType = node.type2.substring(0, node.type2.length()-1);
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.name.equals(node.getName()))
		    {

			currentEdge.frmGateWay.gateID = node.getID();
			currentEdge.frmGateWay.name = newName;
		    }
	    }
	}

    }

    public void updatePathsWithSource(GraphObject node, GraphObject newNode)
    {
	// this is used specially for generic shapes
	if (paths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < paths.size();i++)
	{
	    currentEdge = paths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmActivity.actID = newNode.getID();

			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventName.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmActivity = new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toEvent = new Events();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmActivity=new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	}

    }

    public void updatePathsWithSourceID(GraphObject node, GraphObject newNode)
    {
	// this is used specially for generic shapes
	if (paths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < paths.size();i++)
	{
	    currentEdge = paths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.actID.equals(node.getID()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmActivity.actID = newNode.getID();

			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventID.equals(node.getID()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmActivity = new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toEvent = new Events();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.gateID.equals(node.getID()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmActivity=new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	}

    }
    public void updatePathsWithSource(GraphObject node, String newName)
    {
	if (paths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < paths.size();i++)
	{
	    currentEdge = paths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.name.equals(node.getName()))
		    {

			currentEdge.frmActivity.actID = node.getID();
			currentEdge.frmActivity.name = newName;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventName.equals(node.getName()))
		    {

			currentEdge.frmEvent.eventID = node.getID();
			currentEdge.frmEvent.eventName = newName;
			currentEdge.frmEvent.eventPosition = Integer.parseInt(node.type2.substring(node.type2.length()-1,node.type2.length()));
			currentEdge.frmEvent.eventType = node.type2.substring(0, node.type2.length()-1);
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.name.equals(node.getName()))
		    {

			currentEdge.frmGateWay.name = newName;
			currentEdge.frmGateWay.gateID = node.getID();
		    }
	    }
	}

    }

    public void updateNegativePathsWithSource(GraphObject node,GraphObject newNode)
    {
	// this is used specially for generic shapes
	if (negativePaths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < negativePaths.size();i++)
	{
	    currentEdge = negativePaths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmActivity.actID = newNode.getID();

			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmActivity = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventName.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmActivity = new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    currentEdge.frmEvent = null;
			    currentEdge.frmGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toEvent = new Events();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.name.equals(node.getName()))
		    {

			if (newNode.type == ACTIVITY)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmActivity=new Activity();
			    currentEdge.frmActivity.actID = newNode.getID();
			    currentEdge.frmActivity.name = newNode.getName();
			}
			else if (newNode.type == GATEWAY)
			{
			    //negativeEdges.get(i).toActivity = null;
			    //negativeEdges.get(i).toGateWay = new GateWay();
			    currentEdge.frmGateWay.gateID = newNode.getID();
			    currentEdge.frmGateWay.name = newNode.getName();
			    currentEdge.frmGateWay.type = newNode.type2;
			}
			else if (newNode.type == EVENT)
			{
			    currentEdge.frmGateWay = null;
			    currentEdge.frmEvent = new Event();
			    currentEdge.frmEvent.eventID = newNode.getID();
			    currentEdge.frmEvent.eventName= newNode.getName();
			    currentEdge.frmEvent.eventPosition = Integer.parseInt(newNode.type2.substring(newNode.type2.length()-1,newNode.type2.length()));
			    currentEdge.frmEvent.eventType = newNode.type2.substring(0, newNode.type2.length()-1);
			}
		    }
	    }
	}

    }

    public void updateNegativePathsWithSource(GraphObject node,String newName)
    {
	if (negativePaths.size() == 0) return;

	SequenceFlow currentEdge;
	for (int i =0; i < negativePaths.size();i++)
	{
	    currentEdge = negativePaths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.name.equals(node.getName()))
		    {

			currentEdge.frmActivity.actID = node.getID();
			currentEdge.frmActivity.name = newName;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventName.equals(node.getName()))
		    {

			currentEdge.frmEvent.eventID = node.getID();
			currentEdge.frmEvent.eventName = newName;
			currentEdge.frmEvent.eventPosition = Integer.parseInt(node.type2.substring(node.type2.length()-1,node.type2.length()));
			currentEdge.frmEvent.eventType = node.type2.substring(0, node.type2.length()-1);
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.name.equals(node.getName()))
		    {

			currentEdge.frmGateWay.name = newName;
			currentEdge.frmGateWay.gateID = node.getID();
		    }
	    }
	}

    }

    private boolean isResolvableNode(GraphObject node, ArrayList<GraphObject> prevChecked)
    {
	if ( node.type == EVENT || node.type == GATEWAY)
	    return true;
	if (!node.getName().startsWith("@"))
	    return true;
	ArrayList<GraphObject> allPredsSuccs = new ArrayList<GraphObject>();
	allPredsSuccs = this.getPredecessorsFromQueryGraph(node);
	allPredsSuccs.addAll(this.getSuccessorsFromQueryGraph(node));
	allPredsSuccs.removeAll(prevChecked);
	if (allPredsSuccs.size() ==0) return false;
	prevChecked.add(node);
	for (int i =0; i < allPredsSuccs.size();i++)
	    if (isResolvableNode(allPredsSuccs.get(i),prevChecked))
		return true;
	log.info("Node: " + node.getName() + " is unresolvable");
	return false;

    }

    public boolean isResolvableQuery() {
	GraphObject currentNode;
	boolean qryOk = true;
	ArrayList<GraphObject> prev;
	for (int i = 0; i < this.nodes.size();i++)
	{
	    currentNode = this.nodes.get(i);
	    prev = new ArrayList<GraphObject>();
	    prev.add(currentNode);
	    if (!isResolvableNode(currentNode, prev))
		qryOk =false;
	}

	return qryOk;
    }

    public boolean anyNegativeConnections(GraphObject src, GraphObject dst) {
	SequenceFlow currentEdge;
	boolean result=false;
	for (int i = 0; i < this.negativeEdges.size();i++)
	{
	    currentEdge = negativeEdges.get(i);
	    if (src.type == ACTIVITY) {
		if (dst.type == ACTIVITY) {
		    if (currentEdge.frmActivity != null && currentEdge.toActivity != null)
		    {
			result = currentEdge.frmActivity.name.equals(src.getName()) 
			&& currentEdge.toActivity.name.equals(dst.getName());
		    }
		}
		else if (dst.type == GATEWAY)
		{
		    if (currentEdge.frmActivity != null && currentEdge.toGateWay != null)
		    {
			result = currentEdge.frmActivity.name.equals(src.getName()) 
			&& currentEdge.toGateWay.name.equals(dst.getName()) 
			&& currentEdge.toGateWay.type.equals(dst.type2);
		    }
		}
		else if (dst.type == EVENT)
		{
		    if (currentEdge.frmActivity != null && currentEdge.toEvent != null)
		    {
			result = currentEdge.frmActivity.name.equals(src.getName()) 
			&& currentEdge.toEvent.eventName.equals(dst.getName()) 
			&& currentEdge.toEvent.eventType.startsWith(dst.type2);
		    }
		}
	    }
	    else if (src.type == GATEWAY) {
		if (dst.type == ACTIVITY) {
		    if (currentEdge.frmGateWay != null && currentEdge.toActivity != null)
		    {
			result = currentEdge.frmGateWay.name.equals(src.getName()) 
			&& currentEdge.frmGateWay.type.equals(src.type2)
			&& currentEdge.toActivity.name.equals(dst.getName());
		    }
		}
		else if (dst.type == GATEWAY)
		{
		    if (currentEdge.frmGateWay != null && currentEdge.toGateWay != null)
		    {
			result = currentEdge.frmGateWay.name.equals(src.getName()) 
			&& currentEdge.frmGateWay.type.equals(src.type2) 
			&& currentEdge.toGateWay.name.equals(dst.getName()) 
			&& currentEdge.toGateWay.type.equals(dst.type2);
		    }
		}
		else if (dst.type == EVENT)
		{
		    if (currentEdge.frmGateWay != null && currentEdge.toEvent != null)
		    {
			result = currentEdge.frmGateWay.name.equals(src.getName()) 
			&& currentEdge.frmGateWay.type.equals(src.type2) 
			&& currentEdge.toEvent.eventName.equals(dst.getName()) 
			&& currentEdge.toEvent.eventType.startsWith(dst.type2);
		    }
		}
	    }
	    else if (src.type == EVENT) {
		if (dst.type == ACTIVITY) {
		    if (currentEdge.frmEvent != null && currentEdge.toActivity != null)
		    {
			result = currentEdge.frmEvent.eventName.equals(src.getName()) 
			&& currentEdge.frmEvent.eventType.startsWith(src.type2)
			&& currentEdge.toActivity.name.equals(dst.getName());
		    }
		}
		else if (dst.type == GATEWAY)
		{
		    if (currentEdge.frmEvent != null && currentEdge.toGateWay != null)
		    {
			result = currentEdge.frmEvent.eventName.equals(src.getName()) 
			&& currentEdge.frmEvent.eventType.startsWith(src.type2) 
			&& currentEdge.toGateWay.name.equals(dst.getName()) 
			&& currentEdge.toGateWay.type.equals(dst.type2);
		    }
		}
		else if (dst.type == EVENT)
		{
		    if (currentEdge.frmEvent != null && currentEdge.toEvent != null)
		    {
			result = currentEdge.frmEvent.eventName.equals(src.getName()) 
			&& currentEdge.frmEvent.eventType.startsWith(src.type2) 
			&& currentEdge.toEvent.eventName.equals(dst.getName()) 
			&& currentEdge.toEvent.eventType.startsWith(dst.type2);
		    }
		}
	    }
	    if (result)
		return true;
	}

	for (int i = 0; i < this.negativePaths.size();i++)
	{
	    currentEdge = negativePaths.get(i);
	    if (src.type == ACTIVITY) {
		if (dst.type == ACTIVITY) {
		    if (currentEdge.frmActivity != null && currentEdge.toActivity != null)
		    {
			result = currentEdge.frmActivity.name.equals(src.getName()) 
			&& currentEdge.toActivity.name.equals(dst.getName());
		    }
		}
		else if (dst.type == GATEWAY)
		{
		    if (currentEdge.frmActivity != null && currentEdge.toGateWay != null)
		    {
			result = currentEdge.frmActivity.name.equals(src.getName()) 
			&& currentEdge.toGateWay.name.equals(dst.getName()) 
			&& currentEdge.toGateWay.type.equals(dst.type2);
		    }
		}
		else if (dst.type == EVENT)
		{
		    if (currentEdge.frmActivity != null && currentEdge.toEvent != null)
		    {
			result = currentEdge.frmActivity.name.equals(src.getName()) 
			&& currentEdge.toEvent.eventName.equals(dst.getName()) 
			&& currentEdge.toEvent.eventType.startsWith(dst.type2);
		    }
		}
	    }
	    else if (src.type == GATEWAY) {
		if (dst.type == ACTIVITY)
		{
		    if (currentEdge.frmGateWay != null && currentEdge.toActivity != null)
		    {
			result = currentEdge.frmGateWay.name.equals(src.getName()) 
			&& currentEdge.frmGateWay.type.equals(src.type2)
			&& currentEdge.toActivity.name.equals(dst.getName());
		    }
		}
		else if (dst.type == GATEWAY)
		{
		    if (currentEdge.frmGateWay != null && currentEdge.toGateWay != null)
		    {
			result = currentEdge.frmGateWay.name.equals(src.getName()) 
			&& currentEdge.frmGateWay.type.equals(src.type2) 
			&& currentEdge.toGateWay.name.equals(dst.getName()) 
			&& currentEdge.toGateWay.type.equals(dst.type2);
		    }
		}
		else if (dst.type == EVENT)
		{
		    if (currentEdge.frmGateWay != null && currentEdge.toEvent != null)
		    {
			result = currentEdge.frmGateWay.name.equals(src.getName()) 
			&& currentEdge.frmGateWay.type.equals(src.type2) 
			&& currentEdge.toEvent.eventName.equals(dst.getName()) 
			&& currentEdge.toEvent.eventType.startsWith(dst.type2);
		    }
		}
	    }
	    else if (src.type == EVENT) {
		if (dst.type == ACTIVITY) {
		    if (currentEdge.frmEvent != null && currentEdge.toActivity != null)
		    {
			result = currentEdge.frmEvent.eventName.equals(src.getName()) 
			&& currentEdge.frmEvent.eventType.startsWith(src.type2)
			&& currentEdge.toActivity.name.equals(dst.getName());
		    }
		}
		else if (dst.type == GATEWAY)
		{
		    if (currentEdge.frmEvent != null && currentEdge.toGateWay != null)
		    {
			result = currentEdge.frmEvent.eventName.equals(src.getName()) 
			&& currentEdge.frmEvent.eventType.startsWith(src.type2) 
			&& currentEdge.toGateWay.name.equals(dst.getName()) 
			&& currentEdge.toGateWay.type.equals(dst.type2);
		    }
		}
		else if (dst.type == EVENT)
		{
		    if (currentEdge.frmEvent != null && currentEdge.toEvent != null)
		    {
			result = currentEdge.frmEvent.eventName.equals(src.getName()) 
			&& currentEdge.frmEvent.eventType.startsWith(src.type2) 
			&& currentEdge.toEvent.eventName.equals(dst.getName()) 
			&& currentEdge.toEvent.eventType.startsWith(dst.type2);
		    }
		}
	    }
	    if (result)
		return true;
	}
	return false;
    }

    public List<GraphObject> getStartupNodes() {

	GraphObject currentNode;
	SequenceFlow currentEdge;
	ArrayList<SequenceFlow> alledges=new ArrayList<SequenceFlow>(10);
	alledges.addAll(this.edges);
	alledges.addAll(this.negativeEdges);
	alledges.addAll(this.paths);
	alledges.addAll(this.negativePaths);

	int sz = nodes.size();
	int sz2 = alledges.size();
	boolean found;
	ArrayList<GraphObject> strtNodes = new ArrayList<GraphObject>(4);
	for(int i = 0; i < sz;i++)
	{	
	    currentNode = nodes.get(i);
	    found = false;
	    if (currentNode.type == ACTIVITY)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = alledges.get(j);
		    if (currentEdge.toActivity != null && currentEdge.toActivity.name.equals(currentNode.getName()))
		    {
			found = true;
			break;
		    }

		}
	    }
	    else if (currentNode.type == EVENT)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = alledges.get(j);
		    if (currentEdge.toEvent != null && currentEdge.toEvent.eventName.equals(currentNode.getName()))
		    {
			found = true;
			break;
		    }

		}
	    }
	    else if (currentNode.type == GATEWAY)
	    {
		for (int j = 0; j < sz2; j++)
		{
		    currentEdge = alledges.get(j);
		    if (currentEdge.toGateWay != null && currentEdge.toGateWay.gateID.equals(currentNode.getID()))
		    {
			found = true;
			break;
		    }

		}
	    }
	    if (!found)
		strtNodes.add(currentNode);
	}
	return strtNodes;
    }

    public void print(PrintStream outStream)
    {
	super.print(outStream);
	outStream.println("MatchRate: "+this.matchRate);
	for (int i = negativeEdges.size()-1; i >=0; i--)
	{
	    SequenceFlow negEdge = negativeEdges.get(i);
	    if (negEdge.frmActivity != null)
	    {
		if (!negEdge.frmActivity.name.startsWith("$#"))
		    outStream.print("Negative Sequence Flow From Activity: " + negEdge.frmActivity.name);
		else
		    outStream.print("Negative Sequence Flow From Activity: " + negEdge.frmActivity.actID);

	    }
	    else if (negEdge.frmGateWay!= null)
	    {
		//if (!edges.get(i).frmGateWay.gateWayName.startsWith("$#"))
		//	outStream.print("From Gatway: " + edges.get(i).frmGateWay.gateWayName);
		//else
		outStream.print("Negative Sequence Flow From Gatway: " + negEdge.frmGateWay.type+ " " + negEdge.frmGateWay.gateID);

	    } 
	    else if (negEdge.frmEvent!= null)
	    {
		if (!negEdge.frmEvent.eventName.startsWith("$#"))
		    outStream.print("Negative Sequence Flow From Event: " + negEdge.frmEvent.eventName);
		else
		    outStream.print("Negative Sequence Flow From Event: " + negEdge.frmEvent.eventID);
	    }

	    if (negEdge.toActivity != null)
	    {
		if (!negEdge.toActivity.name.startsWith("$#"))
		    outStream.println("...To Activity: " + negEdge.toActivity.name);
		else
		    outStream.println("...To Activity: " + negEdge.toActivity.actID);

	    }
	    else if (negEdge.toGateWay!= null)
	    {
		//if (!edges.get(i).toGateWay.gateWayName.startsWith("$#"))
		//	outStream.println("...To Gatway: " + edges.get(i).toGateWay.gateWayName);
		//else
		outStream.println("...To Gatway: "+ negEdge.toGateWay.type + " " + negEdge.toGateWay.gateID);

	    } 
	    else if (negEdge.toEvent!= null)
	    {
		if (!negEdge.toEvent.eventName.startsWith("$#"))
		    outStream.println("...To Event: " + negEdge.toEvent.eventName);
		else
		    outStream.println("...To Event: " + negEdge.toEvent.eventID);
	    }

	}
	// paths
	for (int i = paths.size()-1; i >=0; i--)
	{
	    Path path = paths.get(i);
	    if (path.frmActivity != null)
	    {
		if (!path.frmActivity.name.startsWith("$#"))
		    if (path.temporalTag == TemporalType.NONE)
			outStream.print("Path From Activity: " + path.frmActivity.name);
		    else if (path.temporalTag == TemporalType.LEADS_TO)
			outStream.print("\"Leads to\" path From Activity: " + path.frmActivity.name);
		    else
			outStream.print("\"Precedes\" path From Activity: " + path.frmActivity.name);
		else
		    outStream.print("Path From Activity: " + path.frmActivity.actID);

	    }
	    else if (path.frmGateWay!= null)
	    {
		//if (!edges.get(i).frmGateWay.gateWayName.startsWith("$#"))
		//	System.out.print("From Gatway: " + edges.get(i).frmGateWay.gateWayName);
		//else
		outStream.print("Path From Gatway: " + path.frmGateWay.type+ " " + path.frmGateWay.gateID);

	    } 
	    else if (path.frmEvent!= null)
	    {
		if (!path.frmEvent.eventName.startsWith("$#"))
		    outStream.print("Path From Event: " + path.frmEvent.eventName);
		else
		    outStream.print("Path From Event: " + path.frmEvent.eventID);
	    }

	    if (path.toActivity != null)
	    {
		if (!path.toActivity.name.startsWith("$#"))
		    outStream.println("...To Activity: " + path.toActivity.name);
		else
		    outStream.println("...To Activity: " + path.toActivity.actID);

	    }
	    else if (path.toGateWay!= null)
	    {
		//if (!edges.get(i).toGateWay.gateWayName.startsWith("$#"))
		//	outStream.println("...To Gatway: " + edges.get(i).toGateWay.gateWayName);
		//else
		outStream.println("...To Gatway: "+ path.toGateWay.type + " " + path.toGateWay.gateID);

	    } 
	    else if (path.toEvent!= null)
	    {
		if (!path.toEvent.eventName.startsWith("$#"))
		    outStream.println("...To Event: " + path.toEvent.eventName);
		else
		    outStream.println("...To Event: " + path.toEvent.eventID);
	    }
	    if (path.exclude.length() > 0)
		outStream.println("excluding "+path.exclude);
	}

//	Negative paths
	for (int i = negativePaths.size()-1; i >=0; i--)
	{
	    SequenceFlow negPath = negativePaths.get(i);
	    if (negPath.frmActivity != null)
	    {
		if (!negPath.frmActivity.name.startsWith("$#"))
		    outStream.print("Negative Path From Activity: " + negPath.frmActivity.name);
		else
		    outStream.print("Negative Path From Activity: " + negPath.frmActivity.actID);

	    }
	    else if (negPath.frmGateWay!= null)
	    {
		//if (!edges.get(i).frmGateWay.gateWayName.startsWith("$#"))
		//	System.out.print("From Gatway: " + edges.get(i).frmGateWay.gateWayName);
		//else
		outStream.print("Negative Path From Gatway: " + negPath.frmGateWay.type+ " " + negPath.frmGateWay.gateID);

	    } 
	    else if (negPath.frmEvent!= null)
	    {
		if (!negPath.frmEvent.eventName.startsWith("$#"))
		    outStream.print("Negative Path From Event: " + negPath.frmEvent.eventName);
		else
		    outStream.print("Negative Path From Event: " + negPath.frmEvent.eventID);
	    }

	    if (negPath.toActivity != null)
	    {
		if (!negPath.toActivity.name.startsWith("$#"))
		    outStream.println("...To Activity: " + negPath.toActivity.name);
		else
		    outStream.println("...To Activity: " + negPath.toActivity.actID);

	    }
	    else if (negPath.toGateWay!= null)
	    {
		//if (!edges.get(i).toGateWay.gateWayName.startsWith("$#"))
		//	outStream.println("...To Gatway: " + edges.get(i).toGateWay.gateWayName);
		//else
		outStream.println("...To Gatway: "+ negPath.toGateWay.type + " " + negPath.toGateWay.gateID);

	    } 
	    else if (negPath.toEvent!= null)
	    {
		if (!negPath.toEvent.eventName.startsWith("$#"))
		    outStream.println("...To Event: " + negPath.toEvent.eventName);
		else
		    outStream.println("...To Event: " + negPath.toEvent.eventID);
	    }

	}
	for (int i = 0 ; i < nodes.size();i++) {
	    outStream.println("Node " + nodes.get(i).type +" "+ nodes.get(i).type2+ " " + nodes.get(i).getName());
	}
	for (int i = 0 ; i < dataObjs.size();i++) {
	    outStream.println("Data Object " + dataObjs.get(i).name+" "+ dataObjs.get(i).getState());
	}
    }
    
    public void updateExcludeExpression(String oldExpr, String newExpr)
    {
	if (oldExpr.length() == 0) 
	    return;
	for (Path p : this.paths)
	{
	    p.exclude = p.exclude.replace(oldExpr, newExpr);
	}
    }
    
    public void addInfoLog(String info)
    {
	infoLogs.add(info);
    }
    
    public void addErrorLog(String error)
    {
	errorLogs.add(error);
    }

    public List<String> getInfoLogs()
    {
	return infoLogs;
    }

    public List<String> getErrorLogs()
    {
	return errorLogs;
    }
    
    public void removeNegativeEdgesWithDestination(GraphObject node) {
    	if (negativeEdges.size() == 0) 
    	    return;
    	boolean matchFound = false;
    	SequenceFlow currentEdge;
    	for (int i =0; i < negativeEdges.size();i++)
    	{
    	    currentEdge = negativeEdges.get(i);
    	    if (node.type == ACTIVITY)
    	    {
    		if (currentEdge.toActivity != null)
    		    if (currentEdge.toActivity.actID.equals(node.getID()) && currentEdge.toActivity.name.equals(node.getName()))
    		    {
    		    	matchFound = true;
    		    	negativeEdges.remove(i);
    		    	break;
    		    }
    	    }
    	    else if (node.type == EVENT)
    	    {
    		if (currentEdge.toEvent != null)
    		    if (currentEdge.toEvent.eventID.equals(node.getID()) && currentEdge.toEvent.eventName.equals(node.getName()))
    		    {
	    			matchFound = true;
	    			negativeEdges.remove(i);
	    			break;
    		    }
    	    }
    	    else if (node.type == GATEWAY)
    	    {
    		if (currentEdge.toGateWay != null)
    		    if (currentEdge.toGateWay.gateID.equals(node.getID()) && currentEdge.toGateWay.name.equals(node.getName()))
    		    {
    		    	matchFound = true;
	    			negativeEdges.remove(i);
	    			break;
    		    }
    	    }
    	}
    	if (matchFound) removeNegativeEdgesWithDestination(node);
    }

    public void removeNegativeEdgesWithSource(GraphObject node)
    {
	if (negativeEdges.size() == 0) 
	    return;
	boolean matchFound = false;
	SequenceFlow currentEdge;
	for (int i =0; i < negativeEdges.size();i++)
	{
	    currentEdge = negativeEdges.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.actID.equals(node.getID()) && currentEdge.frmActivity.name.equals(node.getName()))
		    {
		    	matchFound = true;
		    	negativeEdges.remove(i);
		    	break;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventID.equals(node.getID()) && currentEdge.frmEvent.eventName.equals(node.getName()))
		    {
		    	matchFound = true;
		    	negativeEdges.remove(i);
		    	break;
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.gateID.equals(node.getID()) && currentEdge.frmGateWay.name.equals(node.getName()))
		    {
		    	matchFound = true;
				negativeEdges.remove(i);
				break;
		    }
	    }
	}
	if (matchFound) removeNegativeEdgesWithSource(node);
    }
    
    public void removeNegativePathsWithDestination(GraphObject node) {
    	if (negativePaths.size() == 0) 
    	    return;
    	boolean matchFound = false;
    	SequenceFlow currentEdge;
    	for (int i =0; i < negativePaths.size();i++)
    	{
    	    currentEdge = negativePaths.get(i);
    	    if (node.type == ACTIVITY)
    	    {
    		if (currentEdge.toActivity != null)
    		    if (currentEdge.toActivity.actID.equals(node.getID()) && currentEdge.toActivity.name.equals(node.getName()))
    		    {
    		    	matchFound = true;
    		    	negativePaths.remove(i);
    		    	break;
    		    }
    	    }
    	    else if (node.type == EVENT)
    	    {
    		if (currentEdge.toEvent != null)
    		    if (currentEdge.toEvent.eventID.equals(node.getID()) && currentEdge.toEvent.eventName.equals(node.getName()))
    		    {
	    			matchFound = true;
	    			negativePaths.remove(i);
	    			break;
    		    }
    	    }
    	    else if (node.type == GATEWAY)
    	    {
    		if (currentEdge.toGateWay != null)
    		    if (currentEdge.toGateWay.gateID.equals(node.getID()) && currentEdge.toGateWay.name.equals(node.getName()))
    		    {
    		    	matchFound = true;
    		    	negativePaths.remove(i);
	    			break;
    		    }
    	    }
    	}
    	if (matchFound) removeNegativePathsWithDestination(node);
    }

    public void removeNegativePathsWithSource(GraphObject node)
    {
	if (negativePaths.size() == 0) return;
	boolean matchFound = false;
	SequenceFlow currentEdge;
	for (int i =0; i < negativePaths.size();i++)
	{
	    currentEdge = negativePaths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.actID.equals(node.getID()) && currentEdge.frmActivity.name.equals(node.getName()))
		    {
		    	matchFound = true;
		    	negativePaths.remove(i);
		    	break;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventID.equals(node.getID()) && currentEdge.frmEvent.eventName.equals(node.getName()))
		    {
		    	matchFound = true;
		    	negativePaths.remove(i);
		    	break;
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.gateID.equals(node.getID()) && currentEdge.frmGateWay.name.equals(node.getName()))
		    {
		    	matchFound = true;
		    	negativePaths.remove(i);
				break;
		    }
	    }
	}
	if (matchFound) removeNegativePathsWithSource(node);
    }
    
    public void removePathsWithDestination(GraphObject node) {
    	if (paths.size() == 0) return;
    	boolean matchFound = false;
    	SequenceFlow currentEdge;
    	for (int i =0; i < paths.size();i++)
    	{
    	    currentEdge = paths.get(i);
    	    if (node.type == ACTIVITY)
    	    {
    		if (currentEdge.toActivity != null)
    		    if (currentEdge.toActivity.actID.equals(node.getID()) && currentEdge.toActivity.name.equals(node.getName()))
    		    {
    		    	matchFound = true;
    		    	paths.remove(i);
    		    	break;
    		    }
    	    }
    	    else if (node.type == EVENT)
    	    {
    		if (currentEdge.toEvent != null)
    		    if (currentEdge.toEvent.eventID.equals(node.getID()) && currentEdge.toEvent.eventName.equals(node.getName()))
    		    {
	    			matchFound = true;
	    			paths.remove(i);
	    			break;
    		    }
    	    }
    	    else if (node.type == GATEWAY)
    	    {
    		if (currentEdge.toGateWay != null)
    		    if (currentEdge.toGateWay.gateID.equals(node.getID()) && currentEdge.toGateWay.name.equals(node.getName()))
    		    {
    		    	matchFound = true;
    		    	paths.remove(i);
	    			break;
    		    }
    	    }
    	}
    	if (matchFound) removePathsWithDestination(node);
    }

    public void removePathsWithSource(GraphObject node)
    {
	if (paths.size() == 0) return;
	boolean matchFound = false;
	SequenceFlow currentEdge;
	for (int i =0; i < paths.size();i++)
	{
	    currentEdge = paths.get(i);
	    if (node.type == ACTIVITY)
	    {
		if (currentEdge.frmActivity != null)
		    if (currentEdge.frmActivity.actID.equals(node.getID()) && currentEdge.frmActivity.name.equals(node.getName()))
		    {
		    	matchFound = true;
		    	paths.remove(i);
		    	break;
		    }
	    }
	    else if (node.type == EVENT)
	    {
		if (currentEdge.frmEvent != null)
		    if (currentEdge.frmEvent.eventID.equals(node.getID()) && currentEdge.frmEvent.eventName.equals(node.getName()))
		    {
		    	matchFound = true;
		    	paths.remove(i);
		    	break;
		    }
	    }
	    else if (node.type == GATEWAY)
	    {
		if (currentEdge.frmGateWay != null)
		    if (currentEdge.frmGateWay.gateID.equals(node.getID()) && currentEdge.frmGateWay.name.equals(node.getName()))
		    {
		    	matchFound = true;
		    	paths.remove(i);
				break;
		    }
	    }
	}
	if (matchFound) removePathsWithSource(node);
    }
    
    public void establishCommonBinding()
    {
    	for (GraphObject nd : nodes)
    	{
    		if (nd.type == GATEWAY)
    		{
    			CommonBinding cb = new CommonBinding();
    			cb.commonNode = nd.toString();
    			for (GraphObject nd2 : getGenericSuccessorsFromQueryGraph(nd))
    			{
    				cb.succs += nd2.toString()+",";
    			}
    			for (GraphObject nd2 : getGenericPredecessorsFromQueryGraph(nd))
    			{
    				cb.preds += nd2.toString()+",";
    			}
    			bindings.add(cb);
    		}
    	}
    }

    public boolean lookupSuccBindings(String sucNode,String bindNode)
    {
    	for (CommonBinding cb : bindings)
    	{
    		if (cb.succs.contains(sucNode)
    				&& cb.succsCommonBinding.contains(bindNode))
    			return true;
    	}
    	return false;
    }
    
    public boolean lookupPredBindings(String predNode,String bindNode)
    {
    	for (CommonBinding cb : bindings)
    	{
    		if (cb.preds.contains(predNode)
    				&& cb.predsCommonBinding.contains(bindNode))
    			return true;
    	}
    	return false;
    }

    public void updateSuccCommonBinding(String commNode, String sucNode, String binNode)
    {
    	for (CommonBinding cb : bindings)
    	{
    		if (cb.commonNode.equals(commNode)
    				&& cb.succs.contains(sucNode))
    			if (!cb.succsCommonBinding.contains(binNode))
    			{
    				cb.succsCommonBinding += binNode + ",";
    				break;
    			}
    			
    	}
    }
    
    public void updatePredCommonBinding(String commNode,String predNode,String binNode)
    {
    	for (CommonBinding cb : bindings)
    	{
    		if (cb.commonNode.equals(commNode)
    				&& cb.preds.contains(predNode))
    			if (!cb.predsCommonBinding.contains(binNode))
    			{
    				cb.predsCommonBinding += binNode + ",";
    				break;
    			}
    			
    	}
    }

    public List<CommonBinding> getCommonBindings()
    {
    	return bindings;
    }
    
    public void updateAssociationsToDataObject(DataObject old, DataObject neu)
    {
	for (Association ass : associations)
	{
	    if (ass.toDataObject != null)
	    {
		if (ass.toDataObject.name.equals(old.name) && (ass.toDataObject.getState().equals("?") || ass.toDataObject.getState().equals(neu.getState())))
		{
		    ass.toDataObject.doID = neu.doID;
		    ass.toDataObject.name = neu.name;
		    ass.toDataObject.setState(neu.getState());
		}
	    }
	}
    }
    
    public void updateAssociationsFromDataObject(DataObject old, DataObject neu)
    {
	for (Association ass : associations)
	{
	    if (ass.frmDataObject != null)
	    {
		if (ass.frmDataObject.name.equals(old.name)&& (ass.frmDataObject.getState().equals("?") || ass.frmDataObject.getState().equals(neu.getState())))
		{
		    ass.frmDataObject.doID = neu.doID;
		    ass.frmDataObject.name = neu.name;
		    ass.frmDataObject.setState(neu.getState());
		}
	    }
	}
    }

    public void updateAssociationsToFlowObject(GraphObject old, GraphObject neu)
    {
	for (Association ass : associations)
	{
	    if (old.type == ACTIVITY && ass.toActivity != null)
	    {
		if (ass.toActivity.name.equals(old.getName()))
		{
		    ass.toActivity.actID = neu.getID();
		    ass.toActivity.name = neu.getName();
		}
	    }
	    else if (old.type == EVENT && ass.toEvent != null)
	    {
		if (ass.toEvent.eventName.equals(old.getName()))
		{
		    ass.toEvent.eventID = neu.getID();
		    ass.toEvent.eventName = neu.getName();
		}
	    } 
	}
    }

    public void updateAssociationsFromFlowObject(GraphObject old, GraphObject neu)
    {
	for (Association ass : associations)
	{
	    if (old.type == ACTIVITY && ass.frmActivity != null)
	    {
		if (ass.frmActivity.name.equals(old.getName()))
		{
		    ass.frmActivity.actID = neu.getID();
		    ass.frmActivity.name = neu.getName();
		}
	    }
	    else if (old.type == EVENT && ass.toEvent != null)
	    {
		if (ass.frmEvent.eventName.equals(old.getName()))
		{
		    ass.frmEvent.eventID = neu.getID();
		    ass.frmEvent.eventName = neu.getName();
		}
	    } 
	}
    }
    public void updateUndirectAssociationWithDataObject(DataObject old, DataObject neu)
    {
	for (UndirectedAssociation unia : dataPathAssociations)
	{
	    if (unia.frmDataObject.name.equals(old.name) && (unia.frmDataObject.getState().equals("?") || unia.frmDataObject.getState().equals(neu.getState())))
	    {
		unia.frmDataObject.doID = neu.doID;
		unia.frmDataObject.name = neu.name;
		unia.frmDataObject.setState(neu.getState());
	    }
	}
    }

    public void removeAssociationToFlowObject(GraphObject obj)
    {
	int assSize = associations.size();
	Association ass;
	for (int i = assSize-1; i >= 0; i--)
	{
	    ass = associations.get(i);
	    if (obj.type == ACTIVITY && ass.toActivity != null)
	    {
		if (ass.toActivity.name.equals(obj.getName()) && ass.toActivity.actID.equals(obj.getID()))
		    associations.remove(i);
	    }
	}
    }

    public void removeAssociationFromFlowObject(GraphObject obj)
    {
	int assSize = associations.size();
	Association ass;
	for (int i = assSize -1; i >= 0; i--)
	{
	    ass = associations.get(i);
	    if (obj.type == ACTIVITY && ass.frmActivity != null)
	    {
		if (ass.frmActivity.name.equals(obj.getName()) && ass.frmActivity.actID.equals(obj.getID()))
		    associations.remove(i);
	    }
	}
    }

    public List<DataObject> getDataPathAssociation(Path path)
    {
	List<DataObject> result = new ArrayList<DataObject>();
	for(UndirectedAssociation unia : dataPathAssociations)
	{
	    if (unia.path.getSourceGraphObject().equals(path.getSourceGraphObject()) &&
		    unia.path.getDestinationGraphObject().equals(path.getDestinationGraphObject()))
	    {
		result.add(unia.frmDataObject);
	    }
	}
	return result;
    }
    public void updateUnDirectedAssociationPathSource(GraphObject old,GraphObject neu)
    {
	for (UndirectedAssociation unia : dataPathAssociations)
	{
	    if (unia.path.frmActivity != null && old.type == ACTIVITY 
		    && unia.path.frmActivity.actID.equals(old.getID()) && unia.path.frmActivity.name.equals(old.getName()))
	    {
		unia.path.frmActivity.actID = neu.getID();
		unia.path.frmActivity.name = neu.getName();
	    }

	    else if (unia.path.frmEvent != null && old.type == EVENT  && unia.path.frmEvent.eventID.equals(old.getID()) )
	    {
		unia.path.frmEvent.eventID = neu.getID();
		unia.path.frmEvent.eventName = neu.getName();
	    }
	    else if (unia.path.frmGateWay != null && old.type == GATEWAY && unia.path.frmGateWay.gateID.equals(old.getID()) )
	    {
		unia.path.frmGateWay.gateID = neu.getID();
		unia.path.frmGateWay.name = neu.getName();
	    }
	}
    }
    public void updateUnDirectedAssociationPathDestination(GraphObject old,GraphObject neu)
    {
	for (UndirectedAssociation unia : dataPathAssociations)
	{
	    if (unia.path.toActivity != null && old.type == ACTIVITY 
		    && unia.path.toActivity.actID.equals(old.getID()) && unia.path.toActivity.name.equals(old.getName()))
	    {
		unia.path.toActivity.actID = neu.getID();
		unia.path.toActivity.name = neu.getName();
	    }

	    else if (unia.path.toEvent != null && old.type == EVENT  && unia.path.toEvent.eventID.equals(old.getID()))
	    {
		unia.path.toEvent.eventID = neu.getID();
		unia.path.toEvent.eventName = neu.getName();
	    }
	    else if (unia.path.toGateWay != null && old.type == GATEWAY && unia.path.toGateWay.gateID.equals(old.getID()))
	    {
		unia.path.toGateWay.gateID = neu.getID();
		unia.path.toGateWay.name = neu.getName();
	    }
	}
    }
    public boolean pathEdgeHasDependency(Path x)
    {
    	for (Path y : this.paths)
    	{
    		if (y.label.trim().length() > 0 && x.exclude.contains(y.label))
    			return true;
    	}
    	return false;
    }
    public  QueryProcessorDirective getProcessorDirectives()
    {
	return directive;
    }
    public void setAllowIncludeEnclosingAndSplitDirective(boolean flag)
    {
	if (this.directive == null)
	{
	    this.directive = new QueryProcessorDirective();
	    this.directive.includeEnclosingANDSplit = flag;
	    
	}
    }
    
    public void setStopAtFirstMatch(boolean flag)
    {
	if (this.directive == null)
	{
	    this.directive = new QueryProcessorDirective();
	    this.directive.stopAtFirstMatch = flag;
	    
	}
    }
    
    public void setAllowGenericShapeToEvaluateToNone(boolean flag)
    {
	if (this.directive == null)
	{
	    this.directive = new QueryProcessorDirective();
	    this.directive.allowGenericShapeToEvaluateToNone = flag;
	}
    }
    public void setQueryNodeID(String id)
    {
	queryNodeID = id;
    }
    public String getQueryNodeID()
    {
	return queryNodeID;
    }
    public void setQueryNodeName(String n)
    {
	queryNodeName = n;
    }
    public String getQueryNodeName()
    {
	return queryNodeName;
    }

    @Override
    public void exportXML(PrintWriter writer)
    {
	writer.println("<QueryGraph>");
	
	
	for (GraphObject node : this.nodes)
	{
	    String outt = null;
	    String name="";
	    switch (node.type) {
	    case ACTIVITY:
		outt = "ACT";
		name = node.getName();
		break;
	    case EVENT:
		outt = "EVE";
		if (node.type2.endsWith("1"))
		    name = "StartEvent";
		else if (node.type2.endsWith("2"))
		    name = "IntermediateEvent";
		else
		    name = "EndEvent";
		break;
	    case GATEWAY:
		outt = "GAT";
		name = node.type2;
		break;
	    default:
		break;
	    }
	    writer.println("<"+ outt + " id=\"" + node.getID() + "\" name=\"" + name+ "\"/>");
	}
	for(DataObject dat :dataObjs)
	{
	    writer.println("<DAT id=\"" + dat.doID + "\"/>");
	}
	for (SequenceFlow edge : this.edges)
	{
	    StringBuilder outt = new StringBuilder();
	    if (edge.frmActivity != null)
		outt.append("<SequenceFlow from=\"ACT" + edge.frmActivity.actID + "\"");
	    if (edge.frmEvent != null)
		outt.append("<SequenceFlow from=\"EVE" + edge.frmEvent.eventID + "\"");
	    if (edge.frmGateWay != null)
		outt.append("<SequenceFlow from=\"GAT" + edge.frmGateWay.gateID + "\"");
	    
	    if (edge.toActivity != null)
		outt.append(" to=\"ACT" + edge.toActivity.actID + "\"/>");
	    if (edge.toEvent != null)
		outt.append(" to=\"EVE" + edge.toEvent.eventID + "\"/>");
	    if (edge.toGateWay != null)
		outt.append(" to=\"GAT" + edge.toGateWay.gateID + "\"/>");

	    writer.println(outt);
	}
	
	for(Association ass :associations)
	{
	    StringBuilder outt = new StringBuilder();
	    if (ass.frmActivity != null)
		outt.append("<Association from=\"ACT" + ass.frmActivity.actID + "\"");
	    else if (ass.frmDataObject != null)
		outt.append("<Association from=\"DAT" + ass.frmDataObject.doID + "\"");
	    else if (ass.frmEvent != null)
		outt.append("<Association from=\"EVE" + ass.frmEvent.eventID + "\"");

	    if (ass.toActivity != null)
		outt.append(" to=\"ACT" + ass.toActivity.actID + "\"/>");
	    else if (ass.toDataObject != null)
		outt.append(" to=\"DAT" + ass.toDataObject.doID + "\"/>");
	    else if (ass.toEvent != null)
		outt.append(" to=\"EVE" + ass.toEvent.eventID + "\"/>");

	    writer.println(outt);

	}
	for (Path edge : this.paths)
	{
	    StringBuilder outt = new StringBuilder();
	    if (edge.frmActivity != null)
		outt.append("<Path from=\"ACT" + edge.frmActivity.actID + "\" exclude=\""+edge.exclude+"\"");
	    if (edge.frmEvent != null)
		outt.append("<SequenceFlow from=\"EVE" + edge.frmEvent.eventID + "\" exclude=\""+edge.exclude+"\"");
	    if (edge.frmGateWay != null)
		outt.append("<SequenceFlow from=\"GAT" + edge.frmGateWay.gateID + "\" exclude=\""+edge.exclude+"\"");
	    
	    if (edge.toActivity != null)
		outt.append(" to=\"ACT" + edge.toActivity.actID + "\"/>");
	    if (edge.toEvent != null)
		outt.append(" to=\"EVE" + edge.toEvent.eventID + "\"/>");
	    if (edge.toGateWay != null)
		outt.append(" to=\"GAT" + edge.toGateWay.gateID + "\"/>");

	    writer.println(outt);
	}
	writer.println("</QueryGraph>");
	
	
    }
    public void getResolvedVersion()
    {
	for (GraphObject nd : nodes)
	{
	    if (nd.getName().startsWith("#"))
	    {
		try
		{
		    GraphObject oldNode = nd.clone();
		    nd.setID(nd.getName());
		    nd.setName("");
		    this.updateEdgesWithDestination(oldNode,nd);
		    this.updateEdgesWithSource(oldNode,nd);
		    // updated on 6.1.2011 to fix a big all update methods should be replaced with ID counterparts
		    this.updatePathsWithDestinationID(oldNode,nd);
		    this.updatePathsWithSourceID(oldNode,nd);

//		    DO the same thing with negatives
		    this.updateNegativeEdgesWithDestination(oldNode,nd);
		    this.updateNegativeEdgesWithSource(oldNode,nd);
		    this.updateNegativePathsWithDestination(oldNode,nd);
		    this.updateNegativePathsWithSource(oldNode,nd);


		    // added on 9th of July 2008 
//		    this.updateExcludeExpression(nd.getName(), nd.toString());

		    this.updateUnDirectedAssociationPathSource(oldNode,nd);
		    this.updateUnDirectedAssociationPathDestination(oldNode,nd);

		} catch (CloneNotSupportedException e)
		{
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
		
	    }
	}
    }
}
