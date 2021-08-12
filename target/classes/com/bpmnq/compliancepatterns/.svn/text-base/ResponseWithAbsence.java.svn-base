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
public class ResponseWithAbsence extends ControlFlowCompliancePattern
{

    public ResponseWithAbsence()
    {
	this.vacuousQueryEvaluation = VacuousComplianceQueryInterpretation.NoMatchIsSuccess;
    }
    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getAntiPatterns()
     */
    @Override
    public List<QueryGraph> getAntiPatterns()
    {
//	 one anti pattern is similar to the precedence sequential anti pattern
	List<QueryGraph> result = new ArrayList<QueryGraph>(1);
	GraphObject start, end;
	end = new GraphObject();
	end.type = GraphObjectType.EVENT;
	end.type2 = "3";
	end.setID("-1");
	
	
	
	
	start = new GraphObject();
	start.type = GraphObjectType.ACTIVITY;
	start.setID("-2");
	String nameS = conditionTask.replace("_", " ");
	start.setName(nameS);
	
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
	
	GraphObject condO, conseqO;
	condO = new GraphObject();
	condO.type = GraphObjectType.ACTIVITY;
	condO.setName(nameS);
	condO.setID("-2");
	
	conseqO = new GraphObject();
	conseqO.type = GraphObjectType.ACTIVITY;
	String nameSS = consequentTask.replace("_", " ");
	conseqO.setName(nameSS);
	conseqO.setID("-1");
	// now the other cases where the to-be-absent propositions exits
	// first possibility consequent, to-be-absent,condition are in sequence
	String[] props = this.negatedPropositions.split(",");
	// the case they are in sequence
	GraphObject act;
	for (String prop : props)
	{
	    act = new GraphObject();
	    act.type = GraphObjectType.ACTIVITY;
	    act.setID("-3");
	    String name = prop.trim();
	    name = name.replace("_", " ");
	    act.setName(name);
	    Path pp = new Path(condO,act,"");
	    pp.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	    pp.setTemporalTag(TemporalType.NONE);
	    
	    Path ppp = new Path(act,conseqO,"");
	    ppp.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	    ppp.setTemporalTag(TemporalType.NONE);
	    QueryGraph qq = new QueryGraph();
	    qq.add(condO);
	    qq.add(conseqO);
	    qq.add(act);
	    qq.add(pp);
	    qq.add(ppp);
	    result.add(qq);
	}
	// second possibility cond, conseq in secquence, to-be-absent, conseq in parallel
	GraphObject andSplit = new GraphObject();
	andSplit.type = GraphObjectType.GATEWAY;
	andSplit.type2 = GateWayType.AND_SPLIT.asType2String();
	andSplit.setID("-4");
	
	Path pp = new Path(condO,conseqO,"");
	pp.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	pp.setTemporalTag(TemporalType.NONE);
	
	Path pppp = new Path(andSplit,conseqO,"");
	pppp.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	pppp.setTemporalTag(TemporalType.NONE);
	
	for (String prop : props)
	{
	    act = new GraphObject();
	    act.type = GraphObjectType.ACTIVITY;
	    act.setID("-3");
	    String name = prop.trim();
	    name = name.replace("_", " ");
	    act.setName(name);
	    
	    
	    Path ppp = new Path(andSplit,act,"");
	    ppp.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	    ppp.setTemporalTag(TemporalType.NONE);
	    
	    
	    
	    QueryGraph qq = new QueryGraph();
	    qq.add(condO);
	    qq.add(conseqO);
	    qq.add(act);
	    qq.add(pp);
	    qq.add(ppp);
	    qq.add(pppp);
	    result.add(qq);
	}
	// third and last possibility: cond and conseq are in sequence, cond and to-be-absent in parallel
	pppp = new Path(andSplit,condO,"");
	pppp.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	pppp.setTemporalTag(TemporalType.NONE);
	for (String prop : props)
	{
	    act = new GraphObject();
	    act.type = GraphObjectType.ACTIVITY;
	    act.setID("-3");
	    String name = prop.trim();
	    name = name.replace("_", " ");
	    act.setName(name);
	    
	    
	    Path ppp = new Path(andSplit,act,"");
	    ppp.setPathEvaluaiton(PathEvaluation.ACYCLIC);
	    ppp.setTemporalTag(TemporalType.NONE);
	    
	    
	    
	    QueryGraph qq = new QueryGraph();
	    qq.add(condO);
	    qq.add(conseqO);
	    qq.add(act);
	    qq.add(pp);
	    qq.add(ppp);
	    qq.add(pppp);
	    result.add(qq);
	}
	return result;

    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getImpliedStructuralQueries()
     */
    @Override
    public List<QueryGraph> getImpliedStructuralQueries()
    {
	List<QueryGraph> result = new ArrayList<QueryGraph>(2);
	GraphObject start, act;
	start = new GraphObject();
	start.type = GraphObjectType.ACTIVITY;
	start.setID("-1");
	String nameS = consequentTask.replace("_", " ");
	start.setName(nameS);
	QueryGraph s = new QueryGraph();
	s.add(start);
	result.add(s);
	
	act = new GraphObject();
	act.type = GraphObjectType.ACTIVITY;
	act.setID("-2");
	String name = this.conditionTask;
	name = name.replace("_", " ");
	act.setName(name);
	// this is a forward path
	Path p = new Path(act,start,"");
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
	
	
	return "PLTLSPEC G("+conditionTask.replace(" ", "_")+" -> ("+absentPart+" U "+consequentTask.replace(" ", "_")+"))";
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckCTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckCTLFormula()
    {
	// no formula, can be decided structurally
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.ControlFlowCompliancePattern#getVacuousComplianceCheckLTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckLTLFormula()
    {
	// no formula, can be decided structurally
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
