package com.bpmnq.compliancepatterns;

import java.util.List;

import com.bpmnq.ProcessGraph;
import com.bpmnq.QueryGraph;
import com.bpmnq.petrinet.PetriNet;

public abstract class CompliancePattern
{
    public enum Relation {Response,Precedence,ImplicitDataCondition};
    public enum VacuousComplianceQueryInterpretation {MatchIsSuccess,NoMatchIsSuccess};
    public enum ModelCheckerType {LOLA,NUSMV};
    // MatchIsSuccess = if the vacuous compliance query finds a match -> process is vacuously compliant
    // NoMatchIsSuccess = if the vacuous compliance query does not find a match -> process is vacuously compliant
    protected ProcessGraph process;
    protected PetriNet net;
    protected String conditionTask;
    protected String consequentTask;
    protected String conditionDataEffect;
    protected String consequentDataEffect;
    protected String negatedPropositions;
    protected Relation temporalRelation;
    protected VacuousComplianceQueryInterpretation vacuousQueryEvaluation;
    protected ModelCheckerType modelCheckerType;
    
    public void setInvestigatedProcess(ProcessGraph p)
    {
	process = (ProcessGraph) p.clone();
    }
    public void setConditionTask(String c)
    {
	conditionTask = c;
    }
    public void setConditionDataEffect(String c)
    {
	/**
	 * the effect c must be in a disjunctive normal form
	 * 
	 */
	conditionDataEffect = c;
    }
    public void setConsequentDataEffect(String c)
    {
	/**
	 * the effect c must be in a disjunctive normal form
	 * 
	 */
	consequentDataEffect = c;
    }
    public void setConsequentTask(String conseq)
    {
	consequentTask = conseq;
    }
    public void setAbsentTerm(String term)
    {
	negatedPropositions = term;
    }
    public void setTemporalRelation(Relation x)
    {
	temporalRelation = x;
    }
    public PetriNet getNet()
    {
        return net;
    }
    public void setNet(PetriNet net)
    {
        this.net = net;
    }
    public void setModelCheckerType(ModelCheckerType mct)
    {
	this.modelCheckerType = mct;
    }
    public abstract String getTemporalFormulaPLTL();
    public abstract String getTemporalFormulaCTLLola();
    public abstract List<QueryGraph> getAntiPatterns();
    public abstract QueryGraph getVacuousComplianceCheckQuery();
    public abstract String getVacuousComplianceCheckCTLFormula();
    public abstract String getVacuousComplianceCheckLTLFormula();
    public abstract List<QueryGraph> getImpliedStructuralQueries();
    
    
}
