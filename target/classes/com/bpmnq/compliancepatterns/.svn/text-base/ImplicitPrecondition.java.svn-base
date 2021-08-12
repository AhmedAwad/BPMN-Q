/**
 * 
 */
package com.bpmnq.compliancepatterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bpmnq.Association;
import com.bpmnq.DataObject;
import com.bpmnq.GraphObject;
import com.bpmnq.Path;
import com.bpmnq.QueryGraph;
import com.bpmnq.Utilities;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.Path.PathEvaluation;
import com.bpmnq.compliancechecker.BusinessContext;
import com.bpmnq.compliancechecker.ModelChecker;

/**
 * @author Blue
 *
 */
public class ImplicitPrecondition extends CompliancePattern
{
    private BusinessContext bc;
    private ModelChecker mc;
    private Map<String, String> dobStateQueryResult;
    private Map<String,String> queryObjectStates;
    public void setBusinessContext(BusinessContext bc)
    {
	this.bc = bc;
    }
    public void setModelChecker(ModelChecker mc)
    {
	this.mc = mc;
    }
    public ImplicitPrecondition()
    {
	this.vacuousQueryEvaluation = VacuousComplianceQueryInterpretation.NoMatchIsSuccess;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.CompliancePattern#getAntiPatterns()
     */
    @Override
    public List<QueryGraph> getAntiPatterns()
    {
	// Here we need to invlove the process model and do temporal logic query checking
	// TODO Auto-generated method stub
	return explainViolation();
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.CompliancePattern#getImpliedStructuralQueries()
     */
    @Override
    public List<QueryGraph> getImpliedStructuralQueries()
    {
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.CompliancePattern#getTemporalFormulaCTLLola()
     */
    @Override
    public String getTemporalFormulaCTLLola()
    {
//	 TODO Auto-generated method stub
	String formula = "ALLPATH ALWAYS(NOT("+conditionTask+") OR ("+consequentDataEffect+"))";
	// I need to replace the conditionTask and consequentTask wtih respective place names in the net
	return formula;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.CompliancePattern#getTemporalFormulaPLTL()
     */
    @Override
    public String getTemporalFormulaPLTL()
    {
	String conseqUnderlined = consequentDataEffect.replace(" ", "_").replace("AND", "&").replace("OR", "|");
	String conditionUnderlined = conditionTask.replace(" ", "_");
	return "PLTLSPEC G("+conditionUnderlined+" -> ("+conseqUnderlined+"))";
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.CompliancePattern#getVacuousComplianceCheckCTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckCTLFormula()
    {
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.CompliancePattern#getVacuousComplianceCheckLTLFormula()
     */
    @Override
    public String getVacuousComplianceCheckLTLFormula()
    {
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see com.bpmnq.compliancepatterns.CompliancePattern#getVacuousComplianceCheckQuery()
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
    private Map<String,String> resolvePureDataFlowQuery()
    {
	// query must be a pure data flow query having only one
	// activity and one or more states
	dobStateQueryResult = new HashMap<String, String>();
	String[] states;
	for (String dob : queryObjectStates.keySet())
	{
	    if (bc == null)
	    {
		System.out.println("!!!!!!!!! NULL BUSINESS CONTEXT ????????");
	    }
	    if (dob == null)
	    {
		System.out.println("!!!!!!!!! Data Object Name ????????");
	    }
	    System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
	    System.out.println("DOB :"+dob);
	    System.out.println("DOB States");
	    bc.printDobStates(System.out);
	    System.out.println(bc.getDataObjectStates(dob));
	    System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
	    String sss =bc.getDataObjectStates(dob); 
	    if (sss == null)
	    {
		return dobStateQueryResult;
	    }
	    states = sss.split(",");
	    
	    for (int i = 0 ; i < states.length;i++)
	    {
		String formula;
		if (this.modelCheckerType == ModelCheckerType.NUSMV)
		    formula = "CTLSPEC EF(enabled"+conditionTask.replace(" ","_").replace("executed", "")+" & "+dob+"_"+states[i]+")";
		else // this must be lola
		    formula = "EXPATH EVENTUALLY (enabled"+conditionTask.replace(" ","_").replace("executed", "")+" & "+dob+"_"+states[i]+")";
		if (mc.checkModelAgainstFormula(this.process.modelURI, formula) == ModelChecker.RET_NET_COMPLIES)
		{
		    if (!dobStateQueryResult.keySet().contains(dob))
		    {
			dobStateQueryResult.put(dob, states[i]);
		    }
		    else
		    {
			String tmp = dobStateQueryResult.get(dob);
			tmp += ","+states[i];
			dobStateQueryResult.put(dob, tmp);
		    }
		}
	    }
	}
	return dobStateQueryResult;
    }
    private List<QueryGraph> explainViolation()
    //(TemporalQueryGraph pDataFlow)
    {
	// call the temporal logic query solver to get the set of reachable data states for the studied
	// activity
	List<QueryGraph> result = new ArrayList<QueryGraph>();
	
	// The consequentDataEffect is on the form : (d1ef1 AND d2ef3) OR (d1ef2 AND d2ef4) OR ... 
	queryObjectStates = new HashMap<String, String>();
	String[] conjuncts = consequentDataEffect.split(" OR ");
	for (String con : conjuncts)
	{
	    String[] props = con.split(" AND ");
	    for (String prop : props)
	    {
		// we assume that data states on the form VARIABLE_value
		String object = prop.substring(0, prop.lastIndexOf("_")-1);
		String state = prop.substring(prop.lastIndexOf("_")+1);
		if (!queryObjectStates.keySet().contains(object))
		{
		    queryObjectStates.put(object, state);
		}
		else
		{
		    String tmp = queryObjectStates.get(object);
		    tmp = tmp + ","+state;
		    queryObjectStates.put(object, tmp);
		}
	    }
	}
	
	Map<String,String> violatingObjectStates = new HashMap<String, String>();
	
	Map<String,String> reachableDataObjectStates;
	reachableDataObjectStates = resolvePureDataFlowQuery();
	String req; String[]  given;
	for (String dob : queryObjectStates.keySet())
	{
	    req = queryObjectStates.get(dob);
	    String sss =reachableDataObjectStates.get(dob); 
	    if (sss == null)
	    {
		break;
	    }
	    given = sss.split(",");
	    
	    for (int i = 0; i < given.length;i++)
	    {
		if (!req.contains(given[i]))
		{
		    if (!violatingObjectStates.keySet().contains(dob))
		    {
			violatingObjectStates.put(dob, given[i]);
		    }
		    else
		    {
			String tmp = violatingObjectStates.get(dob);
			tmp = tmp + ","+given[i];
			violatingObjectStates.put(dob, tmp);
		    }
		}
	    }
	}
	// at this point we know exactly the unrequired states
	// for each data object & violating state combination generate a query graph
	// on the form @A produces the violating condition with a path to activity stated in the original query
	GraphObject dummy = new GraphObject();
	dummy.setName("@A");
	dummy.type = GraphObjectType.ACTIVITY;
	//DataObject d;
	GraphObject dObject = new GraphObject();
	dObject.type = GraphObjectType.DATAOBJECT;
	Association asss;
	Path p;
	QueryGraph q;
	for (String dob : queryObjectStates.keySet())
	{
	    DataObject another = new DataObject();
	    another.name = dob;
	    String dddd = violatingObjectStates.get(dob);
	    GraphObject oo = new GraphObject();
	    oo.setID("-1");
	    oo.setName(conditionTask.replace("_", " "));
	    oo.type = GraphObjectType.ACTIVITY;
	    if (dddd == null) // the process model is underspecified and does not provide data handling situations
	    {
		// we need to insert an anti pattern query with  path from the start node to the activity mentioned in the pattern
		q = new QueryGraph();
		q.setAllowIncludeEnclosingAndSplitDirective(true);
		
		q.add(oo);
		GraphObject start = new GraphObject();
		start.type = GraphObjectType.EVENT;
		start.type2 = "1";
		q.add(start);
		p = new Path(start,oo,"");
		p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
		q.add(p);
		result.add(q);
	    }
	    else
	    {
		given = dddd.split(",");
		for (int j = 0; j < given.length;j++)
		{
		    q = new QueryGraph();
		    q.setAllowIncludeEnclosingAndSplitDirective(true);
		    q.add(oo);
		    if (given[j].equalsIgnoreCase("initial"))
		    {
			DataObject dd=null;
			for (DataObject ddd : this.process.dataObjs)
			{
			    if (ddd.name.equals(dob) && ddd.getState().equalsIgnoreCase("initial"))
			    {
				dd = ddd;
				break;
			    }
			}
			// we have to find activities that read that state
			List<GraphObject> readingObjects = this.process.getReadingActivities(dd, dd.getState());
			List<GraphObject> updatingObjects = this.process.getUpdatingActivities(dd, "");
			List<GraphObject> intersection = Utilities.intersect(readingObjects, updatingObjects);
			GraphObject start = new GraphObject();
			start.type = GraphObjectType.EVENT;
			start.type2 = "1";
			q.add(start);
			String exclude="";
			for (GraphObject g : intersection)
			{
			    exclude += g.getName()+",";
			}
			p = new Path(start,oo,exclude);
			p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
		    }
		    else
		    {
			q.add(dummy);
			dObject.setID("-2");
			dObject.setName(another.name);
			dObject.type2 = given[j];
			another.setState(given[j]);
			another.doID = "-1";
			q.add(another);
			p = new Path(dummy,oo);
			asss = new Association(dummy,dObject);
			q.add(asss);

		    }



		    q.add(p);
//		    dob.setState(given[j]);




		    result.add(q);
		}
	    }
	    
	}
	return result;
    }

}
