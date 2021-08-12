/**
 * 
 */
package com.bpmnq.compliancepatterns;

import java.util.ArrayList;
import java.util.List;

import com.bpmnq.GraphObject;
import com.bpmnq.Path;
import com.bpmnq.QueryGraph;
import com.bpmnq.GraphObject.GateWayType;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.Path.PathEvaluation;
import com.bpmnq.Path.TemporalType;

/**
 * @author Ahmed Awad
 * @sine   19.10.2010
 */
public class BeforeAbsence extends ControlFlowCompliancePattern
{

    public BeforeAbsence()
    {
	this.vacuousQueryEvaluation = VacuousComplianceQueryInterpretation.NoMatchIsSuccess;
    }
    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getAntiPatterns()
     */
    @Override
    public List<QueryGraph> getAntiPatterns()
    {
	List<QueryGraph> result = new ArrayList<QueryGraph>(1);
	GraphObject start, act;
	act = new GraphObject();
	act.type = GraphObjectType.ACTIVITY;
	act.setID("-1");
	String nameS = conditionTask.replace("_", " ");
	act.setName(nameS);
	
	String[] props = this.negatedPropositions.split(",");
	// the case they are in sequence
	for (String prop : props)
	{
	    start = new GraphObject();
	    start.type = GraphObjectType.ACTIVITY;
	    start.setID("-2");
	    String name = prop.trim();
	    name = name.replace("_", " ");
	    start.setName(name);
	    Path p = new Path(start,act,"");
	    p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	    p.setTemporalTag(TemporalType.NONE);
	    QueryGraph q = new QueryGraph();
	    q.add(start);
	    q.add(act);
	    q.add(p);
	    result.add(q);
	}
	// the case they are in parallel
	GraphObject andSplit = new GraphObject();
	andSplit.type = GraphObjectType.GATEWAY;
	andSplit.type2 = GateWayType.AND_SPLIT.asType2String();
	andSplit.setID("-3");
	Path p = new Path(andSplit,act,"");
	p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	p.setTemporalTag(TemporalType.NONE);
	for (String prop : props)
	{
	    start = new GraphObject();
	    start.type = GraphObjectType.ACTIVITY;
	    start.setID("-2");
	    String name = prop.trim();
	    name = name.replace("_", " ");
	    start.setName(name);
	    
	    Path p2 = new Path(andSplit,start,"");
	    p2.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	    p2.setTemporalTag(TemporalType.NONE);
	    QueryGraph q = new QueryGraph();
	    q.add(start);
	    q.add(act);
	    q.add(p);
	    q.add(p2);
	    result.add(q);
	}
	return result;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getImpliedStructuralQueries()
     */
    @Override
    public List<QueryGraph> getImpliedStructuralQueries()
    {
	// There is no implied structure query
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getTemporalFormulaCTLLola()
     */
    @Override
    public String getTemporalFormulaCTLLola()
    {
	String[] props = negatedPropositions.split(",");
	String absentPart = "("+props[0].replace(" ", "_");
	for (int i = 1; i < props.length; i++)
	{
	    absentPart = absentPart + " OR "+props[i].replace(" ", "_");
	}
	absentPart = absentPart+")";
	
	String formula = "NOT(EXPATH EVENTUALLY("+consequentTask+" AND EXPATH EVENTUALLY ("+absentPart+" AND EXPATH EVENTUALLY("+conditionTask+"))) )";
	// do some post processing of formula
	return formula;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getTemporalFormulaPLTL()
     */
    @Override
    public String getTemporalFormulaPLTL()
    {
	String[] props = negatedPropositions.split(",");
	String absentPart = "!"+" "+props[0].replace(" ", "_");
	for (int i = 1; i < props.length; i++)
	{
	    absentPart = absentPart + " & "+" !"+" "+props[i].replace(" ", "_");
	}
	
	
	return "PLTLSPEC G("+conditionTask.replace(" ", "_")+" -> ("+absentPart+" S start))";
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckCTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckCTLFormula()
    {
	// There is no vacuous compliance formula. It can be decided structurally
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckLTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckLTLFormula()
    {
	// There is no vacuous compliance formula. It can be decided structurally
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckQuery()
     */
    @Override
    public QueryGraph getVacuousComplianceCheckQuery()
    {
	GraphObject start;
	start = new GraphObject();
	start.type = GraphObjectType.ACTIVITY;
	start.setID("-1");
	String nameS = conditionTask.replace("_", " ");
	start.setName(nameS);
	
	QueryGraph q = new QueryGraph();
	q.add(start);
	return q;
    }

}
