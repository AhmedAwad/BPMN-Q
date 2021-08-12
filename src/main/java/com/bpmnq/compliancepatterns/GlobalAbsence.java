/**
 * 
 */
package com.bpmnq.compliancepatterns;

import java.util.ArrayList;
import java.util.List;

import com.bpmnq.GraphObject;
import com.bpmnq.Path;
import com.bpmnq.QueryGraph;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.Path.PathEvaluation;
import com.bpmnq.Path.TemporalType;

/**
 * @author Ahmed Awad
 * @since 19.10.2010
 */
public class GlobalAbsence extends ControlFlowCompliancePattern
{

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getAntiPatterns()
     */
    @Override
    public List<QueryGraph> getAntiPatterns()
    {
	List<QueryGraph> result = new ArrayList<QueryGraph>(1);
	GraphObject start, act;
	start = new GraphObject();
	start.type = GraphObjectType.EVENT;
	start.type2 = "1";
	start.setID("-1");
	String[] props = this.negatedPropositions.split(",");
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
	
	
	
	
	return result;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getImpliedStructuralQuery()
     */
    @Override
    public List<QueryGraph> getImpliedStructuralQueries()
    {
	
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getTemporalFormulaCTLLola()
     */
    @Override
    public String getTemporalFormulaCTLLola()
    {
	// TODO Auto-generated method stub
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
	
	
	return "PLTLSPEC G(start -> ("+absentPart+" U end))";
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckCTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckCTLFormula()
    {
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckLTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckLTLFormula()
    {
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckQuery()
     */
    @Override
    public QueryGraph getVacuousComplianceCheckQuery()
    {
	// TODO Auto-generated method stub
	return null;
    }

}
