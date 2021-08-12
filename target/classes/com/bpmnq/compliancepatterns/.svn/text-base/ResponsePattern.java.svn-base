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
 * @since  19.10.2010
 */
public class ResponsePattern extends ControlFlowCompliancePattern
{

    public ResponsePattern()
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
	GraphObject start, end;
	start = new GraphObject();
	start.type = GraphObjectType.ACTIVITY;
	
	start.setID("-1");
	String nameS = conditionTask.replace("_", " ");
	start.setName(nameS);
	
	end = new GraphObject();
	end.type = GraphObjectType.EVENT;
	end.type2 = "3";
	end.setID("-2");
	String exclude = this.consequentTask;
	exclude = exclude.replace("_", " ");
	Path p = new Path(start,end,exclude);
	p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	p.setTemporalTag(TemporalType.NONE);
	QueryGraph q = new QueryGraph();
	q.add(start);
	q.add(end);
	q.add(p);
	result.add(q);
	return result;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getImpliedStructuralQuery()
     */
    @Override
    public List<QueryGraph> getImpliedStructuralQueries()
    {
	// no match means structural nonCompliance
	// order of queries is important
	List<QueryGraph> result = new ArrayList<QueryGraph>(2);
	GraphObject start, act;
	start = new GraphObject();
	start.type = GraphObjectType.ACTIVITY;
	start.setID("-1");
	String nameS = conditionTask.replace("_", " ");
	start.setName(nameS);
	
	act = new GraphObject();
	act.type = GraphObjectType.ACTIVITY;
	act.setID("-2");
	String name = this.consequentTask;
	name = name.replace("_", " ");
	act.setName(name);
	QueryGraph s = new QueryGraph();
	s.add(act);
	result.add(s);
	
	Path p = new Path(start,act,"");
	p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	p.setTemporalTag(TemporalType.NONE);
	QueryGraph q = new QueryGraph();
	q.add(start);
	q.add(act);
	q.add(p);
	result.add(q);
	
	return result;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getTemporalFormulaCTLLola()
     */
    @Override
    public String getTemporalFormulaCTLLola()
    {
	// TODO Auto-generated method stub
	String formula = "ALLPATH ALWAYS(NOT("+conditionTask+") OR ALLPATH EVENTUALLY("+consequentTask+"))";
	// I need to replace the conditionTask and consequentTask wtih respective place names in the net
	return formula;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getTemporalFormulaPLTL()
     */
    @Override
    public String getTemporalFormulaPLTL()
    {
	String conseqUnderlined = consequentTask.replace(" ", "_");
	String conditionUnderlined = conditionTask.replace(" ", "_");
	return "PLTLSPEC G("+conditionUnderlined+" -> F("+conseqUnderlined+"))";
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
