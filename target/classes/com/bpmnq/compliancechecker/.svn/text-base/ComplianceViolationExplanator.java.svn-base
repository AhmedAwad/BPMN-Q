package com.bpmnq.compliancechecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bpmnq.Association;
import com.bpmnq.DataObject;
import com.bpmnq.GraphObject;
import com.bpmnq.OryxMemoryQueryProcessor;
import com.bpmnq.Path;
import com.bpmnq.ProcessGraph;
import com.bpmnq.QueryGraph;
import com.bpmnq.Utilities;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.Path.PathEvaluation;


public final class ComplianceViolationExplanator
{
    private BusinessContext bc;
    private ModelChecker mc;
    private TemporalLogicQuerySolver tlqs;
    private TemporalQueryGraph tq;
    private String modelURI;
    private ProcessGraph InspectedProcess;
    private void reset(String model)
    {
	modelURI = model;
	InspectedProcess = new ProcessGraph();
	if (modelURI.startsWith("http"))
	    InspectedProcess.loadFromOryx(modelURI);
	else
	    InspectedProcess.loadModelFromRepository(modelURI);
	d();
    }
    private void d()
    {
	bc = new BusinessContext(InspectedProcess);
	bc.loadContradictingStates();
	
	bc.loadDataObjectStates(modelURI);
	mc = new ModelChecker();
	mc.setProcessGraph(InspectedProcess);
	mc.queryProc = new OryxMemoryQueryProcessor(null); // just for the sake of testing
	tlqs = new TemporalLogicQuerySolver(bc,mc);
    }
    public ComplianceViolationExplanator(String model)
    {
	reset(model);
    }
    public ComplianceViolationExplanator(ProcessGraph process)
    {
	modelURI = process.modelURI;
	InspectedProcess = (ProcessGraph) process.clone();
	System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
	System.out.println("Inspected process uri "+InspectedProcess.modelURI);
	System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
	d();
    }
    public void setModel(String modelURI)
    {
	reset(modelURI);
    }
    private List<QueryGraph> explainViolationPureDataFlow(TemporalQueryGraph pDataFlow)
    {
	// call the temporal logic query solver to get the set of reachable data states for the studied
	// activity
	List<QueryGraph> result = new ArrayList<QueryGraph>();
	
	Map<String,String> queryObjectStates = new HashMap<String, String>();
	List<Association> ass = pDataFlow.getIncomingAssociation(pDataFlow.nodes.get(0));
	for (Association a : ass)
	{
	    if (!queryObjectStates.keySet().contains(a.getSource().getName()))
	    {
		queryObjectStates.put(a.getSource().getName(), a.getSource().type2);
	    }
	    else
	    {
		String tmp = queryObjectStates.get(a.getSource().getName());
		tmp = tmp + ","+a.getSource().type2;
		queryObjectStates.put(a.getSource().getName(), tmp);
	    }
	}
	Map<String,String> violatingObjectStates = new HashMap<String, String>();
	
	Map<String,String> reachableDataObjectStates;
	reachableDataObjectStates = tlqs.resolvePureDataFlowQuery(pDataFlow,modelURI);
	String req; String[]  given;
	for (DataObject dob : pDataFlow.dataObjs)
	{
	    req = queryObjectStates.get(dob.name);
	    String sss =reachableDataObjectStates.get(dob.name); 
	    if (sss == null)
	    {
		break;
	    }
	    given = sss.split(",");
	    
	    for (int i = 0; i < given.length;i++)
	    {
		if (!req.contains(given[i]))
		{
		    if (!violatingObjectStates.keySet().contains(dob.name))
		    {
			violatingObjectStates.put(dob.name, given[i]);
		    }
		    else
		    {
			String tmp = violatingObjectStates.get(dob.name);
			tmp = tmp + ","+given[i];
			violatingObjectStates.put(dob.name, tmp);
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
	for (DataObject dob : pDataFlow.dataObjs)
	{
	    DataObject another = new DataObject();
	    another.name = dob.name;
	    String dddd = violatingObjectStates.get(dob.name);
	    if (dddd == null) // the process model is underspecified and does not provide data handling situations
	    {
		// we need to insert an anti pattern query with  path from the start node to the activity mentioned in the pattern
		q = new QueryGraph();
		q.setAllowIncludeEnclosingAndSplitDirective(true);
		q.add(pDataFlow.nodes.get(0));
		GraphObject start = new GraphObject();
		start.type = GraphObjectType.EVENT;
		start.type2 = "1";
		q.add(start);
		p = new Path(start,pDataFlow.nodes.get(0),"");
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
		    q.add(pDataFlow.nodes.get(0));
		    if (given[j].equalsIgnoreCase("initial"))
		    {
			DataObject dd=null;
			for (DataObject ddd : InspectedProcess.dataObjs)
			{
			    if (ddd.name.equals(dob.name) && ddd.getState().equalsIgnoreCase("initial"))
			    {
				dd = ddd;
				break;
			    }
			}
			// we have to find activities that read that state
			List<GraphObject> readingObjects = InspectedProcess.getReadingActivities(dd, dd.getState());
			List<GraphObject> updatingObjects = InspectedProcess.getUpdatingActivities(dd, "");
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
			p = new Path(start,pDataFlow.nodes.get(0),exclude);
			p.setPathEvaluaiton(PathEvaluation.ACYCLIC);
		    }
		    else
		    {
			q.add(dummy);
			dObject.setID("-1");
			dObject.setName(another.name);
			dObject.type2 = given[j];
			another.setState(given[j]);
			another.doID = "-1";
			q.add(another);
			p = new Path(dummy,pDataFlow.nodes.get(0));
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
    
    private List<QueryGraph> explainViolationPureControlFlow(TemporalQueryGraph pControlFlow)
    {
	
	return pControlFlow.generateAntiPatternQueries();
    }
    private List<QueryGraph> explainViolationConditionalLeadsTo(TemporalQueryGraph pCondLT)
    {
	List<QueryGraph> result = new ArrayList<QueryGraph>();
	result = pCondLT.generateAntiPatternQueries();
	List<Association> ass = pCondLT.getOutgoingAssociation(pCondLT.paths.get(0).getSourceGraphObject());
	List<DataObject> dobs = pCondLT.dataObjs;
	for(QueryGraph qg : result)
	{
	    for (DataObject d : dobs)
	    {
		qg.add(d);
	    }
	    for (Association a : ass)
	    {
		qg.add(a);
	    }
	}
	return result;
    }
    private List<QueryGraph> explainViolationConditionalPrecedes(TemporalQueryGraph tp)
    {
	// get the temporal expression of the query and remove the contra condition part
	List<QueryGraph> result = new ArrayList<QueryGraph>();
	
	String[] excludes = tp.paths.get(0).exclude.split(",");
	tp.paths.get(0).exclude = "";
	String temporalFormula = tp.generateTemporalExpression(tp.paths.get(0), bc);
	// remove the contra condition
	String temporalFormulaWithoutContra;
	temporalFormula = temporalFormula.replace("  ", " ");
	temporalFormula = temporalFormula.replace("TRUE &", "");
	temporalFormulaWithoutContra = temporalFormula.substring(0,temporalFormula.indexOf("& G( !"));
	temporalFormulaWithoutContra +="))";
	
	if (mc.checkModelAgainstFormula(modelURI,temporalFormulaWithoutContra) == ModelChecker.RET_NET_COMPLIES)
	{
	    // we know that the reason is that the contra conditions occurred.
	    // so for each contradicting condition for the condition mentioned in the query
	    // generate an anti pattern query
	    result.addAll(handleViolationOfContraConditionOccurrence(tp));
	    
	}
	else // either preceding activity is not executed or the condition is not always resulting
	{
	    if (temporalFormulaWithoutContra.contains("exec"))
	    {
		String temporalFormulaWithoutCondition = temporalFormulaWithoutContra.substring(0,temporalFormulaWithoutContra.indexOf("&"));
		temporalFormulaWithoutCondition +=")";
		if (mc.checkModelAgainstFormula(modelURI, temporalFormulaWithoutCondition) == ModelChecker.RET_NET_COMPLIES)
		{
		    // we know that some of the required states in the condition do not always occur
		    result.addAll(handleViolationofPrecedesWithOtherConditionResult(tp));
		}
		else // the source activity is not always executed before the destination
		{
		    TemporalQueryGraph other = (TemporalQueryGraph) tp.clone();
		    other.associations.clear();
		    other.dataObjs.clear();
		    result.addAll(other.generateAntiPatternQueries());
		}
	    }
	    else // here we use anonymous activity to hold the data condition
	    {
		result.addAll(handleViolationofPrecedesWithOtherConditionResult(tp));
	    }
	}
	if (excludes.length > 0)
	{
	    result.addAll(handleConditionalPrecedesExclude(tp,excludes));
	}
	
	
	
	return result;
    }
    
    private List<QueryGraph> handleConditionalPrecedesExclude(TemporalQueryGraph tp, String[] excludes)
    {
	// TODO Auto-generated method stub
	List<QueryGraph> result = new ArrayList<QueryGraph>();
	for (int i = 0; i < excludes.length;i++)
	{
	    if (excludes[i].length() == 0)
		continue;
	    // here generate the anti pattern query
	    QueryGraph q = new QueryGraph();
	    // take the common parts from the original compliance query
	    for (GraphObject g : tp.nodes)
		q.add(g);
	    // create the dummy activity that will procude the contra condition
	    GraphObject dummy = new GraphObject();
	    dummy.type = GraphObjectType.ACTIVITY;
	    dummy.setName(excludes[i]);
	    q.add(dummy);
	    q.add(new Path(tp.paths.get(0).getSourceGraphObject(),dummy));
	    q.add(new Path(dummy,tp.paths.get(0).getDestinationGraphObject()));
	    result.add(q);

	}
	return result;
    }
    private List<QueryGraph> handleViolationofPrecedesWithOtherConditionResult(TemporalQueryGraph tp)
    {
	List<QueryGraph> result = new ArrayList<QueryGraph>();
	Map<String,String> queryObjectStates = new HashMap<String, String>();
	List<Association> ass ;
	
	ass = tp.getOutgoingAssociation(tp.paths.get(0).getSourceGraphObject());
	    for (Association a : ass)
		{
		    if (!queryObjectStates.keySet().contains(a.getDestination().getName()))
		    {
			queryObjectStates.put(a.getDestination().getName(), a.getDestination().type2);
		    }
		    else
		    {
			String tmp = queryObjectStates.get(a.getDestination().getName());
			tmp = tmp + ","+a.getDestination().type2;
			queryObjectStates.put(a.getDestination().getName(), tmp);
		    }
		}
	     
	    Map<String,String> reachableDataObjectStates;
	    reachableDataObjectStates = tlqs.resolveConditionalPrecedesQuery(tp, modelURI);
	    String req;
	    String[] given;
	    Map<String,String> violatingObjectStates = new HashMap<String, String>();
	    for (DataObject dob : tp.dataObjs)
	    {
		req = queryObjectStates.get(dob.name);
		String tmp2 = reachableDataObjectStates.get(dob.name); 
		given = tmp2.replace("|", ",").split(",");
		for (int i = 0; i < given.length;i++)
		{
		    if (given[i].contains("_"))
		    {
			given[i]= given[i].substring(given[i].indexOf("_")+1).toLowerCase().trim();
		    }
		    if (!req.contains(given[i]))
		    {
			if (!violatingObjectStates.keySet().contains(dob.name))
			{
			    violatingObjectStates.put(dob.name, given[i]);
			}
			else
			{
			    String tmp = violatingObjectStates.get(dob.name);
			    tmp = tmp + ","+given[i];
			    queryObjectStates.put(dob.name, tmp);
			}
		    }
		}
	    }
	    //GraphObject dummy = new GraphObject();
	    //dummy.setName("@A");
	    //dummy.type = GraphObjectType.ACTIVITY;
	    //DataObject d;
	    GraphObject dObject = new GraphObject();
	    dObject.type = GraphObjectType.DATAOBJECT;
	    Association asss;
	    Path p;
	    QueryGraph q;
	    for (DataObject dob : tp.dataObjs)
	    {
		given = violatingObjectStates.get(dob.name).split(",");
		for (int j = 0; j < given.length;j++)
		{
		    q = new QueryGraph();
		    q.add(tp.paths.get(0).getSourceGraphObject());
		    q.add(tp.paths.get(0).getDestinationGraphObject());
		    p = new Path(tp.paths.get(0).getSourceGraphObject(),tp.paths.get(0).getDestinationGraphObject());
		    q.add(p);
		    dob.setState(given[j]);
		    dObject.setName(dob.name);
		    dObject.type2 = given[j];
		    q.add(dob);
		    asss = new Association(tp.paths.get(0).getSourceGraphObject(),dObject);
		    q.add(asss);
		    result.add(q);
		}

	    }
	    return result;
    }
    private List<QueryGraph> handleViolationOfContraConditionOccurrence(TemporalQueryGraph tp)
    {
	List<QueryGraph> result = new ArrayList<QueryGraph>();
	Map<String,String> queryObjectStates = new HashMap<String, String>();
	List<Association> ass ;
	
	ass = tp.getOutgoingAssociation(tp.paths.get(0).getSourceGraphObject());
	for (Association a : ass)
	{
	    if (!queryObjectStates.keySet().contains(a.getDestination().getName()))
	    {
		queryObjectStates.put(a.getDestination().getName(), a.getDestination().type2);
	    }
	    else
	    {
		String tmp = queryObjectStates.get(a.getDestination().getName());
		tmp = tmp + ","+a.getDestination().type2;
		queryObjectStates.put(a.getDestination().getName(), tmp);
	    }
	}
	// for each state get the contradicting ones
	Iterator it = queryObjectStates.keySet().iterator();
	while(it.hasNext())
	{
	    String key = (String) it.next();
	    String[] reqStat = queryObjectStates.get(key).split(",");
	    for (int i = 0; i < reqStat.length;i++)
	    {
		String[] contraStates = bc.getContradictingState(key.toUpperCase()+"_"+  reqStat[i]).split(",");
		for (int j = 0; j < contraStates.length; j++)
		{
		    // here generate the anti pattern query
		    QueryGraph q = new QueryGraph();
		    // take the common parts from the original compliance query
		    for (GraphObject g : tp.nodes)
			q.add(g);

		    for(DataObject dd : tp.dataObjs)
			q.add(dd);
		    for (Association a : tp.associations)
			q.add(a);

		    DataObject d = new DataObject();
		    d.setState(contraStates[j]);
		    d.name = key;
		    q.add(d);

		    GraphObject dobObject = new GraphObject();
		    dobObject.type = GraphObjectType.DATAOBJECT;
		    dobObject.setName(key);
		    dobObject.type2 = contraStates[j];

		    // create the dummy activity that will procude the contra condition
		    GraphObject dummy = new GraphObject();
		    dummy.type = GraphObjectType.ACTIVITY;
		    dummy.setName("@A");
		    q.add(dummy);
		    q.add(new Association(dummy,dobObject));
		    q.add(new Path(tp.paths.get(0).getSourceGraphObject(),dummy));
		    q.add(new Path(dummy,tp.paths.get(0).getDestinationGraphObject()));
		    result.add(q);

		}
	    }
	}
	return result;
    }
    public List<QueryGraph> explainViolation(TemporalQueryGraph t)
    {
	tq = t;
	List<QueryGraph> result = new ArrayList<QueryGraph>();
	// first the pure data flow
	List<TemporalQueryGraph> tqss = tq.getPureDataFlowQueries();
	for (TemporalQueryGraph tqg : tqss)
	{
	    result.addAll(explainViolationPureDataFlow(tqg));
	}
	tqss.clear();
	// second the pure control flow
	tqss = tq.getLeadsToQueries(); // the pure control flow leads to queries
	tqss.addAll(tq.getPrecedesQueries());
	for (TemporalQueryGraph tqg : tqss)
	{
	    result.addAll(explainViolationPureControlFlow(tqg));
	}
	// third the mixed rules
	// Conditional leads to
	tqss.clear();
	tqss = tq.getConditionalLeadsToQueries();
	for (TemporalQueryGraph tqg : tqss)
	{
	    result.addAll(explainViolationConditionalLeadsTo(tqg));
	}
	// Conditional precedes
	tqss.clear();
	tqss = tq.getConditionalPrecedesQueries();
	for (TemporalQueryGraph tqg : tqss)
	{
	    result.addAll(explainViolationConditionalPrecedes(tqg));
	}
	return result;
    }

}
