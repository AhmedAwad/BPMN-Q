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
 * @since  19.10.2010
 *
 */
public class AfterAbsence extends ControlFlowCompliancePattern
{

    public AfterAbsence()
    {
	vacuousQueryEvaluation = VacuousComplianceQueryInterpretation.NoMatchIsSuccess;
    }
    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getAntiPatterns()
     */
    @Override
    public List<QueryGraph> getAntiPatterns()
    {
	List<QueryGraph> result = new ArrayList<QueryGraph>(1);
	GraphObject start, act;
	start = new GraphObject();
	start.type = GraphObjectType.ACTIVITY;
	start.setID("-1");
	String nameS = conditionTask.replace("_", " ");
	start.setName(nameS);
	
	String[] props = this.negatedPropositions.split(",");
	// the case they are in sequence
	for (String prop : props)
	{
	    act = new GraphObject();
	    act.type = GraphObjectType.ACTIVITY;
	    act.setID("-2");
	    String name = prop.trim();
	    name = name.replace("_", " ");
	    act.setName(name);
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
	Path p = new Path(andSplit,start,"");
	p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	p.setTemporalTag(TemporalType.NONE);
	for (String prop : props)
	{
	    act = new GraphObject();
	    act.type = GraphObjectType.ACTIVITY;
	    act.setID("-2");
	    String name = prop.trim();
	    name = name.replace("_", " ");
	    act.setName(name);
	    
	    Path p2 = new Path(andSplit,act,"");
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
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getImpliedStructuralQuery()
     */
    @Override
    public List<QueryGraph> getImpliedStructuralQueries()
    {
	// There is no implied structural query
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getTemporalFormulaCTLLola()
     */
    @Override
    public String getTemporalFormulaCTLLola()
    {
//	 TODO Auto-generated method stub
	// AG(condition -> !exclude U end)
	// ALLPATH ALWAYS( !condition OR (!exclude U end))
	String[] props = negatedPropositions.split(",");
	String absentPart = "NOT "+" "+props[0].replace(" ", "_");
	for (int i = 1; i < props.length; i++)
	{
	    absentPart = absentPart + " AND "+" NOT "+" "+props[i].replace(" ", "_");
	}
	String formula = "ALLPATH ALWAYS(NOT "+conditionTask+" OR("+absentPart+" UNTIL "+consequentTask+"))";
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
	
	
	return "PLTLSPEC G("+conditionTask.replace(" ", "_")+" -> ("+absentPart+" U end))";
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckCTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckCTLFormula()
    {
	// We can decide vacuousity structurally
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckLTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckLTLFormula()
    {
	// We can decide vacuousity structurally
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
