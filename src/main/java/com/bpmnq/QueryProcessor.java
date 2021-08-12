package com.bpmnq;

import java.util.List;

public interface QueryProcessor 
{
    /**
     * Evaluates a query against one specific model, identified by <code>modelID</code>.
     * @param rslt the Query that is to be tested
     * @param modelID ID of the model to check against
     * @param resultGraph is used to return the matching result graph. Any value passed in will be discarded; after the method returned, the graph can be inspected. Hence, do not pass a constant value.
     * @return <code>true</code> if specified model matches, otherwise <code>false</code>
     */
    public boolean testQueryAgainstModel(QueryGraph rslt, String modelID, ProcessGraph resultGraph);
    
    /**
     * 
     * @param rslt
     * @param modelID
     * @return
     */
    public ProcessGraph runQueryAgainstModel(QueryGraph rslt, String modelID);
    public ProcessGraph runQueryAgainstModel(QueryGraph q, ProcessGraph p);
    
    /**
     * 
     * @param qry
     * @return
     */
    public List<String> processQuery(QueryGraph qry);
    
    /**
     * 
     * @param qry
     * @return
     */
    public List<Match> processMultiQuery(QueryGraph qry);

}
