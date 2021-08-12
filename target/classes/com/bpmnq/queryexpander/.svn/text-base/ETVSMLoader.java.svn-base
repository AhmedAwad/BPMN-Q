package com.bpmnq.queryexpander;

import java.sql.ResultSet;
import java.sql.SQLException;


import org.themis.ir.eTVSM;
import org.themis.ir.eTVSMOntology;
import org.themis.util.LETTERCASE;
import org.themis.util.PREPROCESS;
import org.themis.util.STEMMER;


import com.bpmnq.DataObject;
import com.bpmnq.GraphObject;
import com.bpmnq.ProcessGraph;
import com.bpmnq.Utilities;
import com.bpmnq.GraphObject.GraphObjectType;

public final class ETVSMLoader {
    public eTVSM etvsm;
    public eTVSMOntology ontology;

    public ETVSMLoader()
    {
	try {
	    etvsm = new eTVSM("localhost","themis","postgres","postgres");
	    ontology = new eTVSMOntology("localhost","themis","postgres","postgres");

//	    etvsm.clear();
//	    ontology.clear();
	    etvsm.setParameter(PREPROCESS.STEMMER.toString(), STEMMER.PORTER.toString());
	    etvsm.setParameter(PREPROCESS.LETTERCASE.toString(), LETTERCASE.LOWER.toString());
	    etvsm.clear();
	    ontology.clear();
	    ontology.autoISims(true);
	} catch (SQLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	} catch (ClassNotFoundException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
    }

    public void loadDoc(String doc) throws SQLException
    {
	ontology.dumpWNSynsetMF(doc);
	etvsm.addDocument("DOC:"+doc, doc, false);
    }

    public void loadModel(int modelID) throws SQLException
    {
	String selExp = "Select \"NAME\" from \"BPMN_GRAPH\".\"ACTIVITY\" where \"MOD_ID\"=" + modelID;
	ResultSet lrs = Utilities.st.executeQuery(selExp);
	while (lrs.next())
	{
	    loadDoc(lrs.getString("\"NAME\""));
	}
	//for (int u =0; u < refinedQueries.size();u++)
    }

    public void loadTestData()
    {
	try
	{
	    loadDoc("Obtain Customer Information");
	    loadDoc("Retrieve Full Customer Details");
	    loadDoc("Analyze Customer Relation");
	    loadDoc("Identify Customer Information");
	    loadDoc("Select Deposit Service");
	    loadDoc("Submit Deposit");
	    loadDoc("Prepare Prop. Document");
	    loadDoc("Record Customer Information");
	    loadDoc("Propose Accountount Opening");
	    loadDoc("Schedule Status Review");
	    loadDoc("Open Account Status Review");
	    loadDoc("Verify Customer ID");
	    loadDoc("Open Accountount");
	    loadDoc("Validate Account Information");
	    loadDoc("Apply Account Policy");
	    loadDoc("Close Account");
	    loadDoc("Record Account Information");
	    loadDoc("Activate Account");
	    loadDoc("Evaluate Deposit Value");
	    loadDoc("Do Deposit");
	    loadDoc("Report Large Deposit");
	    loadDoc("Notify Customer");

	    loadDoc("Process Order");
	    loadDoc("Check Credit");
	    loadDoc("Check Order");
	    loadDoc("Arrange Payment");
	    loadDoc("Notify Cancel");
	    loadDoc("Pick");
	    loadDoc("Cancel Order");
	    loadDoc("Wrap");
	    loadDoc("Deliver");

	    loadDoc("Receive Order");
	    loadDoc("Report order");
	    loadDoc("Report rejected order");
	    loadDoc("Fill order");
	    loadDoc("Send invoice");
	    loadDoc("Produce");
	    loadDoc("Receive payment");
	    loadDoc("Test quality");
	    loadDoc("Report payment");
	    loadDoc("Ship");
	    loadDoc("Close order");

	    loadDoc("Order Parts");
	    loadDoc("Transfer parts to Factory");
	    loadDoc("Perform paint preparation");
	    loadDoc("Assemble monocoque");
	    loadDoc("Paint the bodywork Metalic");
	    loadDoc("Paint the bodywork non Metalic");
	    loadDoc("Transport other parts to assembly");
	    loadDoc("Transport the bodywork parts");
	    loadDoc("Assemble the car");
	    loadDoc("Ship the car");

	    loadDoc("Payment request");
	    loadDoc("Get approval from finance director");
	    loadDoc("Prepare cheque for ANZ bank");
	    loadDoc("Inform employee about rejection");
	    loadDoc("Prepare cheque for citi bank");
	    loadDoc("Update account database");
	    loadDoc("Get signature from finance director");
	    loadDoc("Issue cheque");
	    loadDoc("File payment request");

	    loadDoc("Receive issue list");
	    loadDoc("Review issue list");
	    loadDoc("Discussion Cycle");
	    loadDoc("Announce issue");
	    loadDoc("Collect votes");
	    loadDoc("Prepare result");
	    loadDoc("Post result on website");
	    loadDoc("E-mail to voters");
	    loadDoc("Reduce");
	    loadDoc("Reduce and recalc votes");
	    loadDoc("Reannounce");
	    loadDoc("accounting change");
	}
	catch (SQLException ex)
	{
	    ex.printStackTrace();
	}
    }
    public void loadOryxModel(String url)
    {
	ProcessGraph pg = new ProcessGraph();
	pg.loadFromOryx(url);
	
	for (GraphObject nd : pg.nodes)
	{
	    if (nd.type == GraphObjectType.ACTIVITY)
	    {
		try
		{
		    loadDoc(nd.getName());
		} 
		catch (SQLException e)
		{
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
	
	for (DataObject dob : pg.dataObjs)
	{
	    
		try
		{
		    loadDoc(dob.name);
		} 
		catch (SQLException e)
		{
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    
	}
    }
}
