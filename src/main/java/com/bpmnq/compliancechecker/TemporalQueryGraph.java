package com.bpmnq.compliancechecker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.bpmnq.Association;
import com.bpmnq.DataObject;
import com.bpmnq.GraphObject;
import com.bpmnq.Path;
import com.bpmnq.QueryGraph;

import com.bpmnq.Utilities;
import com.bpmnq.Association.AssociaitonType;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.Path.PathEvaluation;
import com.bpmnq.Path.TemporalType;

public final class TemporalQueryGraph extends QueryGraph {

    private Logger log = Logger.getLogger(TemporalQueryGraph.class);
    private boolean startActivity = false;
    protected List<GraphObject> startupNodes;
    // These are added on 7.11.2007 to handle the generation of temporal formulae

    public void addLeadsToPath(GraphObject from, GraphObject to, String excl)
    {
	Path path = new Path(from, to, excl, TemporalType.LEADS_TO);
	add(path);
    }
    
    public void addLeadsToNegativePath(GraphObject from, GraphObject to, String excl)
    {
	Path negPath = new Path(from, to, excl, TemporalType.LEADS_TO);
	addNegativePath(negPath);
    }
    
    public void addPrecedesPath(GraphObject from, GraphObject to, String excl)
    {
	Path path = new Path(from, to, excl, TemporalType.PRECEDES);
	add(path);
    }
    
    public void addPrecedesNegativePath(GraphObject from, GraphObject to, String excl)
    {
	Path negPath = new Path(from, to, excl, TemporalType.PRECEDES);
	addNegativePath(negPath);
    }
    
    public List<GraphObject> getLeadsToPathSuccessorsFromQueryGraph(GraphObject node)
    {
	return getSuccessorWithTag(node, TemporalType.LEADS_TO);
    }
    
    public List<GraphObject> getPrecedesPathSuccessorsFromQueryGraph(GraphObject node)
    {
	return getSuccessorWithTag(node, TemporalType.PRECEDES);
    }
    
    private List<GraphObject> getSuccessorWithTag(GraphObject node, TemporalType tag)
    {
	List<GraphObject> succs = new ArrayList<GraphObject>();

	for (Path currentEdge : paths)
	{
	    if (currentEdge.getTemporalTag() == tag)
	    {
		GraphObject succ = getSuccessorObject(currentEdge, node);
		if (succ.type != GraphObjectType.UNDEFINED)
		    succs.add(succ);
	    }
	}
	return succs;

    }
    
//    private String generateTemporalExpressionStartingAt(GraphObject node)
//    {
//	StringBuffer exp = new StringBuffer(100);
//	List<GraphObject> succs ;
//
//	exp.append("G( ");
//	if (isActivityOrEvent(node)) {
//	    startActivity = true;
//	}
//	else
//	{
//	    //exp.append("( ");
//	    if (node.type2.equals("XOR SPLIT"))
//		exp.append(generateSPLITTemporalExpression(node, "XOR"));
//	    else if (node.type2.equals("OR SPLIT"))
//		exp.append(generateSPLITTemporalExpression(node, "OR"));
//	    else if (node.type2.equals("AND SPLIT"))
//		exp.append(generateSPLITTemporalExpression(node, "AND"));
//
//	}
//	// we need to get successors from the graph
//	// start by edge successors
//	if (startActivity)
//	{
//	    succs = getSuccessorsFromQueryGraph(node);
//	    GraphObject sucNode;
//	    int sz = succs.size();
//	    for (int i = 0; i < sz; i++)
//	    {
//		sucNode = succs.get(i);
//		if (isActivityOrEvent(sucNode)) {
//		    if (i == sz -1)
//			exp.append("( "+ node.getTemporalExpressionName()+ " implies X("+ sucNode.getTemporalExpressionName()+"))");
//		    if (!startupNodes.contains(sucNode))
//			startupNodes.add(sucNode);
//		    //exp.append(generateTemporalExpressionStartingAt(sucNode));
//		}
//		else 
//		{
//		    if (sucNode.type2.equals("XOR SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> ("+ generateSPLITTemporalExpression(sucNode,"XOR")+"))");
//		    else if (sucNode.type2.equals("OR SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> ("+ generateSPLITTemporalExpression(sucNode,"OR")+"))");
//		    else if (sucNode.type2.equals("AND SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> ("+ generateSPLITTemporalExpression(sucNode,"AND")+"))");
//		}
//		if (i < sz -1)
//		    exp.append(" and ");
//	    }
//	    succs = getNegativeSuccessorsFromQueryGraph(node);
//	    sz = succs.size();
//	    for (int i = 0; i < sz; i++)
//	    {
//		sucNode = succs.get(i);
//		if (isActivityOrEvent(sucNode)) {
//		    exp.append("( "+ node.getTemporalExpressionName()+ " -> NOT X("+ sucNode.getTemporalExpressionName()+"))");
//		    //exp.append(generateTemporalExpressionStartingAt(sucNode));
//		}
//		else 
//		{
//		    if (sucNode.type2.equals("XOR SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> NOT("+ generateSPLITTemporalExpression(sucNode,"XOR")+"))");
//		    else if (sucNode.type2.equals("OR SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> NOT("+ generateSPLITTemporalExpression(sucNode,"OR")+"))");
//		    else if (sucNode.type2.equals("AND SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> NOT("+ generateSPLITTemporalExpression(sucNode,"AND")+"))");
//		}
//
//		if (i < sz -1)
//		    exp.append(" and ");
//	    }
//	    succs = getLeadsToPathSuccessorsFromQueryGraph(node);
//	    sz = succs.size();
//	    for (int i = 0; i < sz; i++)
//	    {
//		sucNode = succs.get(i);
//		if (isActivityOrEvent(sucNode)) {
//		    exp.append("( "+ node.getTemporalExpressionName()+ " ->  F("+ sucNode.getTemporalExpressionName()+"))");
//		    //exp.append(generateTemporalExpressionStartingAt(sucNode));
//		} else {
//		    if (sucNode.type2.equals("XOR SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> F("+ generateSPLITTemporalExpression(sucNode,"XOR")+"))");
//		    else if (sucNode.type2.equals("OR SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> F("+ generateSPLITTemporalExpression(sucNode,"OR")+"))");
//		    else if (sucNode.type2.equals("AND SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> F("+ generateSPLITTemporalExpression(sucNode,"AND")+"))");
//		}
//		if (i < sz -1)
//		    exp.append(" and ");
//	    }
//
//	    succs = getPrecedesPathSuccessorsFromQueryGraph(node);
//	    sz = succs.size();
//	    for (int i = 0; i < sz; i++)
//	    {
//		sucNode = succs.get(i);
//		if (isActivityOrEvent(sucNode)) {
//		    exp.append("( "+ sucNode.getTemporalExpressionName()+ " ->  O("+ node.getTemporalExpressionName()+"))");
//		    //exp.append(generateTemporalExpressionStartingAt(sucNode));
//		} else {
//		    if (sucNode.type2.equals("XOR SPLIT"))
//			exp.append("( "+ generateSPLITTemporalExpression(sucNode,"XOR")+ " -> O("+ node.getTemporalExpressionName() +"))");
//		    else if (sucNode.type2.equals("OR SPLIT"))
//			exp.append("( "+generateSPLITTemporalExpression(sucNode,"OR") + " -> O("+ node.getTemporalExpressionName()+"))");
//		    else if (sucNode.type2.equals("AND SPLIT"))
//			exp.append("( "+ generateSPLITTemporalExpression(sucNode,"AND")+ " -> O("+ node.getTemporalExpressionName()+"))");
//		}
//		if (i < sz -1)
//		    exp.append(" and ");
//	    }
//	    succs = getNegativePathSuccessorsFromQueryGraph(node);
//	    sz = succs.size();
//	    for (int i = 0; i < sz; i++)
//	    {
//		sucNode = succs.get(i);
//		if (isActivityOrEvent(sucNode)) {
//		    exp.append("( "+ node.getTemporalExpressionName()+ " -> ( NOT F("+ sucNode.getTemporalExpressionName()+")))");
//		    //exp.append(generateTemporalExpressionStartingAt(sucNode));
//		} else 				{
//		    if (sucNode.type2.equals("XOR SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> NOT F("+ generateSPLITTemporalExpression(sucNode,"XOR")+"))");
//		    else if (sucNode.type2.equals("OR SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> NOT F("+ generateSPLITTemporalExpression(sucNode,"OR")+"))");
//		    else if (sucNode.type2.equals("AND SPLIT"))
//			exp.append("( "+ node.getTemporalExpressionName()+ " -> NOT F("+ generateSPLITTemporalExpression(sucNode,"AND")+"))");
//		}
//		if (i < sz -1)
//		    exp.append(" and ");
//	    }
//	}
//	exp.append(" )");
//	return exp.toString();
//    }

//    private String generateSPLITTemporalExpression(GraphObject node, String SplitType)
//    {
//	StringBuffer exp = new StringBuffer(100);
//	ArrayList<GraphObject> succs ;
//	succs = getSuccessorsFromQueryGraph(node);
//	GraphObject sucNode;
//	String temporalQuantifier="";
//	int sz = succs.size();
//	boolean onecycle=false;
//	if (startActivity)
//	    temporalQuantifier = "X";
//	for (int i = 0; i < sz; i++)
//	{
//	    onecycle = true;
//
//	    sucNode = succs.get(i);
//	    if (isActivityOrEvent(sucNode))
//	    {
//		if(i == sz -1)
//		    exp.append(" "+ temporalQuantifier+"("+ sucNode.getName()+") ");
//		else
//		    exp.append(" "+temporalQuantifier+"("+ sucNode.getName()+") "+SplitType+" ");
//		if (!startupNodes.contains(sucNode))
//		    startupNodes.add(sucNode);
//	    }
//	    else
//	    {
//		if (sucNode.type2.equals("XOR SPLIT"))
//		    exp.append("("+generateSPLITTemporalExpression(sucNode,"XOR")+")");
//		else if (sucNode.type2.equals("OR SPLIT"))
//		    exp.append("("+generateSPLITTemporalExpression(sucNode,"OR")+")");
//		else if (sucNode.type2.equals("AND SPLIT"))
//		    exp.append("("+generateSPLITTemporalExpression(sucNode,"AND")+")");
//	    }
//	}
//
//	// negative edges
//	succs = getNegativeSuccessorsFromQueryGraph(node);
//	sz = succs.size();
//	if (sz > 0 && onecycle)
//	{
//	    exp.append(" "+SplitType+" ");
//	    onecycle=false;
//	}
//	for (int i = 0; i < sz; i++)
//	{
//	    onecycle = true;
//	    sucNode = succs.get(i);
//	    if (isActivityOrEvent(sucNode))
//	    {
//		if(i == sz -1)
//		    exp.append(" (NOT "+temporalQuantifier+"("+ sucNode.getName()+")) ");
//		else
//		    exp.append(" (NOT "+temporalQuantifier+"("+ sucNode.getName()+")) "+SplitType+" ");
//	    }
//	    else
//	    {
//		if (sucNode.type2.equals("XOR SPLIT"))
//		    exp.append("NOT("+generateSPLITTemporalExpression(sucNode,"XOR")+")");
//		else if (sucNode.type2.equals("OR SPLIT"))
//		    exp.append("NOT("+generateSPLITTemporalExpression(sucNode,"OR")+")");
//		else if (sucNode.type2.equals("AND SPLIT"))
//		    exp.append("NOT("+generateSPLITTemporalExpression(sucNode,"AND")+")");
//	    }
//	}
//
//	// paths
//	if (startActivity)
//	    temporalQuantifier = "F";
//	succs = getPathSuccessorsFromQueryGraph(node);
//	sz = succs.size();
//	if (sz > 0 && onecycle)
//	{
//	    exp.append(" "+SplitType+" ");
//	    onecycle=false;
//	}
//	for (int i = 0; i < sz; i++)
//	{
//	    onecycle=true;
//	    sucNode = succs.get(i);
//	    if (isActivityOrEvent(sucNode))
//	    {
//		if(i == sz -1)
//		    exp.append("  "+temporalQuantifier+"("+ sucNode.getName()+") ");
//		else
//		    exp.append(" "+temporalQuantifier+"("+ sucNode.getName()+") "+SplitType+" ");
//	    }
//	    else
//	    {
//		if (sucNode.type2.equals("XOR SPLIT"))
//		    exp.append("("+generateSPLITTemporalExpression(sucNode,"XOR")+")");
//		else if (sucNode.type2.equals("OR SPLIT"))
//		    exp.append("("+generateSPLITTemporalExpression(sucNode,"OR")+")");
//		else if (sucNode.type2.equals("AND SPLIT"))
//		    exp.append("("+generateSPLITTemporalExpression(sucNode,"AND")+")");
//	    }
//	}
//
//	// negative paths
//	succs = getNegativePathSuccessorsFromQueryGraph(node);
//	sz = succs.size();
//	if (sz > 0 && onecycle)
//	{
//	    exp.append(" "+SplitType+" ");
//	    onecycle=false;
//	}
//	for (int i = 0; i < sz; i++)
//	{
//	    onecycle=true;
//	    sucNode = succs.get(i);
//	    if (isActivityOrEvent(sucNode))
//	    {
//		if(i == sz -1)
//		    exp.append("  (NOT "+temporalQuantifier+"("+ sucNode.getName()+")) ");
//		else
//		    exp.append(" (NOT "+temporalQuantifier+"("+ sucNode.getName()+")) "+SplitType+" ");
//	    }
//	    else
//	    {
//		if (sucNode.type2.equals("XOR SPLIT"))
//		    exp.append("NOT("+generateSPLITTemporalExpression(sucNode,"XOR")+")");
//		else if (sucNode.type2.equals("OR SPLIT"))
//		    exp.append("NOT("+generateSPLITTemporalExpression(sucNode,"OR")+")");
//		else if (sucNode.type2.equals("AND SPLIT"))
//		    exp.append("NOT("+generateSPLITTemporalExpression(sucNode,"AND")+")");
//	    }
//	}
//	return exp.toString();
//    }
//    private String generateTemporalExpression(Association a)
//    {
//	StringBuffer result = new StringBuffer();
//	if (a.frmDataObject !=null && a.toActivity != null)
//	{	
//	    result.append("G("+"enabled_"+getTemporalName(a.toActivity.name)+"-> "+a.frmDataObject.name.toUpperCase()+"_"+a.frmDataObject.getState().toLowerCase()+")");
//	}
//	return result.toString();
//    }
    public String generateTemporalExpression(Path p, BusinessContext bc)
    {
	StringBuffer exp = new StringBuffer(100);
	GraphObject source = p.getSourceGraphObject();
	List<Association> asses = this.getOutgoingAssociation(source);
	if (asses.size() == 0)
	{
	    if (p.getTemporalTag() == TemporalType.LEADS_TO)
	    {
		if (source.type == GraphObjectType.ACTIVITY)
		{
		    exp.append("G(");
		    exp.append(source.getTemporalExpressionName());
		    exp.append(" -> ");
		}
		else if (source.type == GraphObjectType.EVENT)
		{
		    exp.append("(");
		}
		if (p.exclude.length() == 0)
		{
		    exp.append("F(");
		    exp.append(p.getDestinationGraphObject().getTemporalExpressionName());
		    exp.append("))");
		}
		else
		{
		    String[] acts = p.exclude.split(",");
		    exp.append("(!"+getTemporalName(acts[0]));
		    for (int i = 1 ; i < acts.length;i++)
		    {
			exp.append(" & !"+getTemporalName(acts[i]));
		    }
		    
		    exp.append(") U (");
		    exp.append(p.getDestinationGraphObject().getTemporalExpressionName());
		    exp.append("))");
		}
		
	    }
	    else if (p.getTemporalTag() == TemporalType.PRECEDES)
	    {
		exp.append("G(");
		exp.append(p.getDestinationGraphObject().getTemporalExpressionName().replace("executed", "enabled"));
		exp.append(" -> ");
		if (p.exclude.length() == 0)
		{
		    
		    exp.append("O(");
		    exp.append(source.getTemporalExpressionName());
		    exp.append("))");
		}
		else
		{
		    String[] acts = p.exclude.split(",");
		    exp.append("(!"+getTemporalName(acts[0]));
		    for (int i = 1 ; i < acts.length;i++)
		    {
			exp.append(" & !"+getTemporalName(acts[i]));
		    }
		    
		    exp.append(") S (");
		    exp.append(source.getTemporalExpressionName());
		    exp.append("))");
		}
	    }
	    else // this is a sufficience edge
	    {
		exp.append("F(");
		    exp.append(source.getTemporalExpressionName());
		    exp.append(" -> ");
		    
		if (p.exclude.length() == 0)
		{
		    exp.append("F(");
		    exp.append(p.getDestinationGraphObject().getTemporalExpressionName());
		    exp.append("))");
		}
		else
		{
		    String[] acts = p.exclude.split(",");
		    exp.append("(!"+getTemporalName(acts[0]));
		    for (int i = 1 ; i < acts.length;i++)
		    {
			exp.append(" & !"+getTemporalName(acts[i]));
		    }
		    
		    exp.append(") U (");
		    exp.append(p.getDestinationGraphObject().getTemporalExpressionName());
		    exp.append("))");
		}
	    }
	}
	else // there are data conditions
	{
	    String dataCondition = generateEffectDataConditions(asses);
	    if (p.getTemporalTag() == TemporalType.LEADS_TO)
	    {
		if (source.type == GraphObjectType.ACTIVITY)
		{
		    exp.append("G(");
		    exp.append(source.getTemporalExpressionName());
		    exp.append(" & ");
		    exp.append(dataCondition + " & ");

		    exp.append("G("+generateContraDataCondition(asses, bc)+") & ");
		    exp.append("!O(");
		    exp.append(source.getTemporalExpressionName());
		    exp.append(" & ");
		    exp.append(dataCondition + " & ");

		    exp.append("G("+generateContraDataCondition(asses,bc)+") )");
		    exp.append(" -> ");
		}
		else if (source.type == GraphObjectType.EVENT)
		{
		    exp.append("(");
		}
		
		if (p.exclude.length()==0)
		{
		    exp.append("F(");
		    exp.append(p.getDestinationGraphObject().getTemporalExpressionName());
		    exp.append("))");
		}
		else
		{
		    String[] acts = p.exclude.split(",");
		    exp.append("(!"+getTemporalName(acts[0]));
		    for (int i = 1 ; i < acts.length;i++)
		    {
			exp.append(" & !"+getTemporalName(acts[i]));
		    }
		    
		    exp.append(") U (");
		    exp.append(p.getDestinationGraphObject().getTemporalExpressionName());
		    exp.append("))");
		}
	    }
	    else if (p.getTemporalTag() == TemporalType.PRECEDES)
	    {
		exp.append("G(");
		exp.append(p.getDestinationGraphObject().getTemporalExpressionName().replace("executed", "enabled"));
		exp.append(" -> ");
		if (p.exclude.length() == 0)
		{
		    exp.append("O(");
		    exp.append(source.getTemporalExpressionName());
		    exp.append(" & ");
		    exp.append(dataCondition + " & ");
		    exp.append("G("+generateContraDataCondition(asses,bc)+")");
		    exp.append("))"); 
		}
		else
		{
		    String[] acts = p.exclude.split(",");
		    exp.append("(!"+getTemporalName(acts[0]));
		    for (int i = 1 ; i < acts.length;i++)
		    {
			exp.append(" & !"+getTemporalName(acts[i]));
		    }
		    
		    exp.append(") S (");
		    exp.append(source.getTemporalExpressionName());
		    exp.append(" & ");
		    exp.append(dataCondition + " & ");
		    exp.append("G("+generateContraDataCondition(asses,bc)+")");
		    exp.append("))");
		}
		
	    }
	    else // this is a sufficience edge
	    {
		exp.append("F(");
		exp.append(source.getTemporalExpressionName());
    	    	exp.append(" & ");
        	exp.append(dataCondition + " & ");
        	    
        	exp.append("G("+generateContraDataCondition(asses,bc)+") & ");
        	exp.append("!O(");
        	exp.append(source.getTemporalExpressionName());
        	exp.append(" & ");
        	exp.append(dataCondition + " & ");
        	    
        	exp.append("G("+generateContraDataCondition(asses,bc)+") )");
        	exp.append(" -> ");
		if (p.exclude.length()==0)
		{
		    exp.append("F(");
		    exp.append(p.getDestinationGraphObject().getTemporalExpressionName());
		    exp.append("))");
		}
		else
		{
		    String[] acts = p.exclude.split(",");
		    exp.append("(!"+getTemporalName(acts[0]));
		    for (int i = 1 ; i < acts.length;i++)
		    {
			exp.append(" & !"+getTemporalName(acts[i]));
		    }
		    
		    exp.append(") U (");
		    exp.append(p.getDestinationGraphObject().getTemporalExpressionName());
		    exp.append("))");
		}
	    }
	}
	return exp.toString();
    }
    private String generateContraDataCondition(List<Association> asses, BusinessContext bc)
    {
	String result="TRUE",temp;
	for (Association a:asses)
	{
	    
	    if (a.toDataObject != null)
	    {
		
		temp = bc.getContradictingState(a.toDataObject.name.toUpperCase()+"_"+a.toDataObject.getState().toLowerCase());
		if (temp != null)
		{    
		    String[] sts = temp.split(",");
		    for (int i = 0; i < sts.length;i++)
		    {
			String ini = sts[i].substring(0,1);
//			ini = ini.toLowerCase();
			sts[i] = sts[i].substring(1).toLowerCase();
			sts[i] = ini + sts[i].trim();
			result += " & !"+sts[i].replace(" ", "_");
		    }
		}
		
	    }
	}
	return result;
    }

    private String generateEffectDataConditions(List<Association> asses)
    {
	Map<String,String> dobState = new HashMap<String, String>();
	
	for (Association as : asses)
	{
	    if (as.toDataObject != null)
		
	    {
		as.toDataObject.normalize();
		if (!dobState.keySet().contains(as.toDataObject.name))
		    dobState.put(as.toDataObject.name, as.toDataObject.name+"_"+as.toDataObject.getState().toLowerCase());
		else
		{
		    String tmp = dobState.get(as.toDataObject.name);
		    tmp = tmp.concat(" | " +as.toDataObject.name+"_"+as.toDataObject.getState().toLowerCase());
		    dobState.put(as.toDataObject.name, tmp);
		}
	    }
	    
		
	}
	Set<String>keys = dobState.keySet();
	Iterator<String> it = keys.iterator();
	String key;
	StringBuffer result = new StringBuffer(100);
	result.append("TRUE ");
	while(it.hasNext())
	{
	    key = it.next();
	    
	    result.append(" & ( "+dobState.get(key)+" )");
	}
	return result.toString();
    }
    private String generatePreDataConditions(List<Association> asses)
    {
	
	Map<String,String> dobState = new HashMap<String, String>();
	
	for (Association as : asses)
	{
	    as.frmDataObject.normalize();
	    if (as.frmDataObject != null && as.toActivity != null)
	    {
		if (!dobState.keySet().contains(as.frmDataObject.name))
		    dobState.put(as.frmDataObject.name, as.frmDataObject.name+"_"+as.frmDataObject.getState().toLowerCase());
		else
		{
		    String tmp = dobState.get(as.frmDataObject.name);
		    tmp.concat(" | " + as.frmDataObject.name+"_"+as.frmDataObject.getState().toLowerCase());
		    dobState.put(as.frmDataObject.name,tmp );
		}
	    }
	    
		
	}
	Set<String>keys = dobState.keySet();
	Iterator<String> it = keys.iterator();
	String key;
	StringBuffer result = new StringBuffer(100);
	result.append("TRUE ");
	while(it.hasNext())
	{
	    key = it.next();
	    
	    result.append(" & ( "+dobState.get(key)+" )");
	}
	return result.toString();
    }
    public  List<String> getTemporalExpressions(BusinessContext bc)
    {
	List<String> result = new ArrayList<String>(paths.size()+associations.size());
	for (Path p: this.paths)
	{
	    result.add(generateTemporalExpression(p,bc));
	}
	List<Association> preCond ;
	String dataCond;
	for (GraphObject  a: this.nodes)
	{
	    if (a.type == GraphObjectType.ACTIVITY )
	    {
		preCond = this.getIncomingAssociation(a);
		dataCond = generatePreDataConditions(preCond);
		if (preCond.size() > 0)
		    result.add("G("+"enabled_"+getTemporalName(a.getName()).replace("executed_", "")+" -> "+dataCond+")");
	    }
	}
	return result;
    }
    public String getTemporalExpression()
    {
	List<String> res = getTemporalExpressions(Utilities.getBusinssContext());
	String result = res.toString();
	result = result.replace("[", "");
	result = result.replace("]", "");
	result = result.replace(",", " & ");
	return result;
    }
//    private String generateTemporalExpression()
//    {
//	StringBuffer exp =new StringBuffer(100);
//	startupNodes = getStartupNodes();
//	GraphObject currentNode;
//	String tmp;
//	int sz = startupNodes.size();
//	//exp.append("True and ");
//	if (sz== 0)
//	    return "False";
//	while (startupNodes.size() !=0)
////	    for (int i = 0; i < startupNodes.size(); i++)
//	{
//	    currentNode = startupNodes.remove(0);
//	    tmp = generateTemporalExpressionStartingAt(currentNode);
//	    startupNodes.addAll(getLeadsToPathSuccessorsFromQueryGraph(currentNode));
//	    startupNodes.addAll(getPrecedesPathSuccessorsFromQueryGraph(currentNode));
//
//	    exp.append(tmp);
//	    if (startupNodes.size() != 0)
//		//if (i < startupNodes.size() -1)
//		exp.append(" AND \n ");
//	    startActivity = false;
//	}
//	//exp.append(" True ");
//
//	// Post processing step
//	while (exp.toString().contains("AND \n G(  )"))
//	{
//	    int startIndex =exp.indexOf("AND \n G(  )"); 
//	    exp.replace(startIndex , startIndex+ "AND \n G(  )".length(), "");
//	}
//	return exp.toString();
//    }
    
    public String toString()
    {
	//return this.generateTemporalExpression();
	return this.getTemporalExpression();
    }
    
    public List<GraphObject> generateKeepList(String model)
    {
	int nsize = this.nodes.size();
	List<GraphObject> rslt = new ArrayList<GraphObject>(nsize);
	List<String> ids;
	try
	{
	    ids = Utilities.getEventID(1, model);
	} catch (SQLException e)
	{
	    log.error("Database error. Could not retrieve an event's porperties. Result may be incorrect", e);
	    ids = new ArrayList<String>();
	}

	boolean startEventFound = false;
	for (int i = 0 ; i < nsize ; i++)
	{
	    GraphObject currentNode = nodes.get(i);
	    if (currentNode.type == GraphObjectType.ACTIVITY)
	    {
		GraphObject copiedNode = null;
		try
		{
		    copiedNode = currentNode.clone();
		    copiedNode.setID(Utilities.getActivityID(copiedNode.getName(), model));
		} catch (SQLException e)
		{
		    copiedNode.setID("0");
		    log.error("Database error. Could not get an activity's ID. Results may be incorrect", e);
		} catch (CloneNotSupportedException e) { }
		
		rslt.add(copiedNode);
	    }
	    else if (currentNode.type == GraphObjectType.EVENT 
		    && currentNode.type2.endsWith("1")) // currentNode is a start event
	    {
		startEventFound = true;
		if (ids.size() > 1)
		{
		    currentNode.setID(ids.get(0));
		    for (int j = 1 ; j < ids.size(); j++)
		    {
			GraphObject copiedNode = new GraphObject();
			copiedNode.setName(rslt.get(i).getName());
			copiedNode.type = GraphObjectType.EVENT;
			copiedNode.type2 = rslt.get(i).type2;
			copiedNode.setID(ids.get(j));
			rslt.add(copiedNode);
		    }
		}
	    }	

	} // end for loop
	
	if (!startEventFound)
	{
	    for (int j = 0 ; j < ids.size(); j++)
	    {
		GraphObject copiedNode = new GraphObject();
		copiedNode.setName("");
		copiedNode.type = GraphObjectType.EVENT;
		copiedNode.type2 = GraphObject.EventType.START.asType2String();
		copiedNode.setID(ids.get(j));
		rslt.add(copiedNode);
	    }

	}
	return rslt;
    }

    /**
     * Checks <code>node</code> for being an activity or an event
     * @param node The graph object to check
     * @return
     */
    private boolean isActivityOrEvent(GraphObject node) {
	return (node.type == GraphObjectType.ACTIVITY 
		|| node.type == GraphObjectType.EVENT);
    }
    public List<QueryGraph> generateAntiPatternQueries()
    {
	List<QueryGraph> result = new ArrayList<QueryGraph>(this.paths.size());
	
	for (Path p : this.paths)
	{
	    if (p.getTemporalTag() == TemporalType.LEADS_TO)
	    
		result.addAll(generateLeadsToAntiPattern(p));
	    
	    else if (p.getTemporalTag() == TemporalType.PRECEDES)
		result.addAll(generatePrecedesAntiPattern(p));
	}
	return result;
    }

    private List<QueryGraph> generateLeadsToAntiPattern(Path p)
    {
	if (p.getSourceGraphObject().type == GraphObjectType.EVENT && 
		p.getDestinationGraphObject().type == GraphObjectType.EVENT)
	    
	    return generateGlobalScopeAbsenceAntiPattern(p);
	
	else 
	    
	    return generateBeforeScopeAntiPattern(p);

    }
    private List<QueryGraph> generatePrecedesAntiPattern(Path p)
    {
	List<QueryGraph> result;
	QueryGraph tmp;
	result = new ArrayList<QueryGraph>();
	GraphObject s= new GraphObject(),d= new GraphObject();
	Path ep = new Path();
	//ep.exclude = p.getSourceGraphObject().getName();
	s.type = GraphObjectType.EVENT;
	s.type2= "1";
	try
	{
	    d = p.getDestinationGraphObject().clone();
	}
	catch (CloneNotSupportedException cnse)
	{
	    System.err.println(cnse.getMessage());
	    return null;
	}
	tmp = new QueryGraph();
	tmp.add(s);
	tmp.add(d);
	ep = new Path(s,d,p.getSourceGraphObject().getName(),TemporalType.NONE);
	ep.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	tmp.add(ep);
	tmp.setAllowIncludeEnclosingAndSplitDirective(true);
	result.add(tmp);
	
	if (p.exclude.length() > 0)
	{
	    
	    s = new GraphObject();
	    
	    
	    try
	    {
		s = p.getSourceGraphObject().clone();
	    }
	    catch (CloneNotSupportedException cnse)
	    {
    	    	System.err.println(cnse.getMessage());
    	    	return null;
	    }
	    
	    
	    String[] elems = p.exclude.split(",");
	    for (int i = 0 ; i < elems.length; i++)
	    {
		tmp = new QueryGraph();
		GraphObject m = new GraphObject();
		m.setName(elems[i]);
		m.type = GraphObjectType.ACTIVITY;
		
		ep = new Path(s,m,"",TemporalType.NONE);
		ep.setPathEvaluaiton(PathEvaluation.ACYCLIC);
//		if (s.type == GraphObjectType.ACTIVITY)
//		{   
		Path ep2;
		ep2 = new Path(s,d,"",TemporalType.NONE);
		ep2.setPathEvaluaiton(PathEvaluation.ACYCLIC);
		tmp.add(ep2);
//		}
		tmp.add(s);
		tmp.add(d);
		tmp.add(ep);
		tmp.setAllowIncludeEnclosingAndSplitDirective(true);
		result.add(tmp);
		
	    }
	}
	
	return result;
    }
//    private List<QueryGraph> generateAfterScopeAntiPattern(Path p)
//    {
//	// TODO Auto-generated method stub
//	return null;
//    }

//    private List<QueryGraph> generateBetweenScopeAntiPattern(Path p)
//    {
//	
//    }

    

    private List<QueryGraph> generateGlobalScopeAbsenceAntiPattern(Path p)
    {
	QueryGraph result = null; 
	
	List<QueryGraph> r = new ArrayList<QueryGraph>();
	
	if (p.exclude.length() > 0)
	{
	    GraphObject dest;
	    Path ep;
		
	    
	    String[] elems = p.exclude.split(",");
	    for (int i = 0 ; i < elems.length; i++)
	    {
		result = new QueryGraph();
		dest = new GraphObject();
		dest.type = GraphObjectType.ACTIVITY;
		dest.setName(elems[i].trim());
		ep = new Path(p.getSourceGraphObject(),dest,"",TemporalType.NONE);
		result.add(p.getSourceGraphObject());
		result.add(dest);
		// Added on 19.08.2009
		ep.setPathEvaluaiton(PathEvaluation.ACYCLIC);
		result.add(ep);
		// Added on 19.08.2009
		result.setAllowIncludeEnclosingAndSplitDirective(true);
		r.add(result);
	    }
	}
	return r;
    }

    

    private List<QueryGraph> generateBeforeScopeAntiPattern(Path p)
    {
	List<QueryGraph> result;
	QueryGraph tmp;
	result = new ArrayList<QueryGraph>();
	GraphObject s= new GraphObject(),d= new GraphObject();
	Path ep;
	
//	s.type = GraphObjectType.EVENT;
//	s.type2= "1";
	try
	{
	    s = p.getSourceGraphObject().clone();
//	    d = p.getDestinationGraphObject().clone();
	}
	catch (CloneNotSupportedException cnse)
	{
	    System.err.println(cnse.getMessage());
	    return null;
	}
	tmp = new QueryGraph();
	tmp.add(s);
	
	d.type = GraphObjectType.EVENT;
        d.type2="3";
        ep = new Path(s,d,p.getDestinationGraphObject().getName(),TemporalType.NONE);
        // Added on 19.08.2009 
        ep.setPathEvaluaiton(PathEvaluation.ACYCLIC);
        tmp.add(d);
	tmp.add(ep);
	tmp.setAllowIncludeEnclosingAndSplitDirective(true);
	result.add(tmp);
	
	if (p.exclude.length() > 0)
	{
	    
	    d = new GraphObject();
	    
	    
	    try
	    {
		d = p.getDestinationGraphObject().clone();
	    }
	    catch (CloneNotSupportedException cnse)
	    {
    	    	System.err.println(cnse.getMessage());
    	    	return null;
	    }
	    
	    // Modeified on 19.08.2009
	    String[] elems = p.exclude.split(",");
	    for (int i = 0 ; i < elems.length; i++)
	    {
		tmp = new QueryGraph();
		GraphObject m = new GraphObject();
		m.setName(elems[i].trim());
		m.type = GraphObjectType.ACTIVITY;
		
		ep = new Path(s,m,"",TemporalType.NONE);
		ep.setPathEvaluaiton(PathEvaluation.ACYCLIC);
		if (d.type == GraphObjectType.ACTIVITY)
		{
		    Path ep2;
		    ep2 = new Path(s,d,"",TemporalType.NONE);
		    ep2.setPathEvaluaiton(PathEvaluation.ACYCLIC);
		    tmp.add(ep2);
		}
		tmp.add(s);
		tmp.add(d);
		tmp.add(m);
		tmp.add(ep);
		tmp.setAllowIncludeEnclosingAndSplitDirective(true);
		result.add(tmp);
		
	    }
	}
	
	return result;
    }
    private String getTemporalName(String s)
    {
	String ret = s.replace(" ", "_");
	    ret = ret.replace("\n", "_");
	    ret = "executed_" + ret;

	    return ret;
    }
    //This method should return the set of pure data flow queries
    // within the querys
    public List<TemporalQueryGraph> getPureDataFlowQueries()
    {
	List<TemporalQueryGraph> result=new ArrayList<TemporalQueryGraph>();
	TemporalQueryGraph tmp;
	for (GraphObject nd : this.nodes)
	{
	    if (nd.type != GraphObjectType.ACTIVITY)
		continue;
	    List<Association> ass = this.getIncomingAssociation(nd);

	    tmp = new TemporalQueryGraph();
	    tmp.add(nd);
	    boolean behavioralAssFound = false;
	    for (Association as : ass)
	    {
		if (as.assType == AssociaitonType.Behavioral)
        	{
		    behavioralAssFound = true;
		    if (as.getSource().type == GraphObjectType.DATAOBJECT)
		    {
    		    	DataObject neu = new DataObject();
        		neu.doID = as.getSource().getID();
        		neu.name = as.getSource().getName();
        		neu.setState(as.getSource().type2);
        		tmp.add(neu);
        		tmp.add(as);
        		    
		    }
        	}
	    }
	    if (behavioralAssFound)
		result.add(tmp);
	}
	return result;
    }
    private List<TemporalQueryGraph> getConditionalControlFlowQueries(TemporalType t)
    {
	List<TemporalQueryGraph> result=new ArrayList<TemporalQueryGraph>();
	TemporalQueryGraph tmp;
	GraphObject src,dst;
	for (Path p : this.paths)
	{
	    if (p.getTemporalTag() != t)
		continue;
	    src = p.getSourceGraphObject();
	    dst = p.getDestinationGraphObject();
	    
	    List<Association> ass = this.getOutgoingAssociation(src);

	    tmp = new TemporalQueryGraph();
	    tmp.add(src);
	    tmp.add(dst);
	    tmp.add(p);
	    boolean structuralAssFound = false;
	    for (Association as : ass)
	    {
		if (as.assType == AssociaitonType.Structural)
        	{
		    structuralAssFound = true;
		    if (as.getDestination().type == GraphObjectType.DATAOBJECT)
		    {
    		    	DataObject neu = new DataObject();
        		neu.doID = as.getDestination().getID();
        		neu.name = as.getDestination().getName();
        		neu.setState(as.getDestination().type2);
        		tmp.add(neu);
        		tmp.add(as);
        		    
		    }
        	}
	    }
	    if (structuralAssFound)
		result.add(tmp);
	}
	return result;
    }
    public List<TemporalQueryGraph> getConditionalLeadsToQueries()
    {
	return getConditionalControlFlowQueries(TemporalType.LEADS_TO);
    }
    public List<TemporalQueryGraph> getConditionalPrecedesQueries()
    {
	return getConditionalControlFlowQueries(TemporalType.PRECEDES);
    }
    private List<TemporalQueryGraph> getPureControlFlowQueries(TemporalType t)
    {
	List<TemporalQueryGraph> result = new ArrayList<TemporalQueryGraph>();
	TemporalQueryGraph tmp;
	GraphObject src,dst;
	for (Path p : this.paths)
	{
	    if (p.getTemporalTag() != t)
		continue;
	    src = p.getSourceGraphObject();
	    dst = p.getDestinationGraphObject();
	    
	    List<Association> ass = this.getOutgoingAssociation(src);
	    if (ass.size() > 0)
		continue;
	    
	    tmp = new TemporalQueryGraph();
	    tmp.add(src);
	    tmp.add(dst);
	    tmp.add(p);
//	    for (Association as : ass)
//	    {
//		if (as.assType == AssociaitonType.Structural)
//        	{
//		    
//		    if (as.getDestination().type == GraphObjectType.DATAOBJECT)
//		    {
//    		    	DataObject neu = new DataObject();
//        		neu.doID = as.getDestination().getID();
//        		neu.name = as.getDestination().getName();
//        		neu.setState(as.getDestination().type2);
//        		tmp.add(neu);
//        		tmp.add(as);
//        		    
//		    }
//        	}
//	    }
	   
	    result.add(tmp);
	}
	return result;
    }
    public List<TemporalQueryGraph> getLeadsToQueries()
    {
	return getPureControlFlowQueries(TemporalType.LEADS_TO);
    }
    public List<TemporalQueryGraph> getPrecedesQueries()
    {
	return getPureControlFlowQueries(TemporalType.PRECEDES);
    }
    public QueryGraph getQuerableQueryGraph() throws TemporalQueryMalFormedException
    {
	boolean errFound = false;
	// we have to check for improper constructs
	// No variable data objects or states allowed
	for (DataObject dob : this.dataObjs)
	{
	    if (dob.name.contains("@") ||  dob.getState().contains("?") || dob.getState().contains("@"))
	    {
		TemporalQueryMalFormedException e = new TemporalQueryMalFormedException();
		throw e;
	    }
	    
	}
	// Variable nodes are allowed only if they are associated with data conditions
	for (GraphObject ob : this.nodes)
	{
	    if (ob.getName().contains("@"))
	    {
		if (this.getOutgoingAssociation(ob).size() == 0)
		{
		    TemporalQueryMalFormedException e = new TemporalQueryMalFormedException();
		    throw e;
		}
	    }
	    // no generic nodes allowed of any kind
	    if (ob.type == GraphObjectType.ACTIVITY && ob.type2.equals("GENERIC SHAPE"))
	    {
		TemporalQueryMalFormedException e = new TemporalQueryMalFormedException();
		throw e;
	    }
	    
	    if (ob.type == GraphObjectType.GATEWAY && ob.type2.contains("GENERIC"))
	    {
		TemporalQueryMalFormedException e = new TemporalQueryMalFormedException();
		throw e;
	    }
		
	}
	if (dataPathAssociations.size() > 0)
	{
	    TemporalQueryMalFormedException e = new TemporalQueryMalFormedException();
	    throw e;
	}
	QueryGraph result=null;
	// do the work;
	
	return result;
    }
}
