/**
 * generated by http://RDFReactor.semweb4j.org ($Id: CodeGenerator.java 1535 2008-09-09 15:44:46Z max.at.xam.de $) on 1/3/11 6:01 PM
 */
package de.hpi.bpmn.rdf;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdfreactor.runtime.Base;
import org.ontoware.rdfreactor.runtime.ReactorResult;

/**
 * This class manages access to these properties:
 * <ul>
 *   <li> Activitytype </li>
 *   <li> Name </li>
 * </ul>
 *
 * This class was generated by <a href="http://RDFReactor.semweb4j.org">RDFReactor</a> on 1/3/11 6:01 PM
 */
public class Activity extends Node {

    /** http://b3mn.org/stencilset/bpmn1.1#Activity */
    @SuppressWarnings("hiding")
	public static final URI RDFS_CLASS = new URIImpl("http://b3mn.org/stencilset/bpmn1.1#Activity", false);

    /** http://oryx-editor.org/activitytype */
    @SuppressWarnings("hiding")
	public static final URI ACTIVITYTYPE = new URIImpl("http://oryx-editor.org/activitytype",false);

    /** http://oryx-editor.org/name */
    @SuppressWarnings("hiding")
	public static final URI NAME = new URIImpl("http://oryx-editor.org/name",false);

    /** 
     * All property-URIs with this class as domain.
     * All properties of all super-classes are also available. 
     */
    @SuppressWarnings("hiding")
    public static final URI[] MANAGED_URIS = {
      new URIImpl("http://oryx-editor.org/activitytype",false),
      new URIImpl("http://oryx-editor.org/name",false) 
    };


	// protected constructors needed for inheritance
	
	/**
	 * Returns a Java wrapper over an RDF object, identified by URI.
	 * Creating two wrappers for the same instanceURI is legal.
	 * @param model RDF2GO Model implementation, see http://rdf2go.semweb4j.org
	 * @param classURI URI of RDFS class
	 * @param instanceIdentifier Resource that identifies this instance
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 *
	 * [Generated from RDFReactor template rule #c1] 
	 */
	protected Activity ( Model model, URI classURI, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
		super(model, classURI, instanceIdentifier, write);
	}

	// public constructors

	/**
	 * Returns a Java wrapper over an RDF object, identified by URI.
	 * Creating two wrappers for the same instanceURI is legal.
	 * @param model RDF2GO Model implementation, see http://rdf2go.ontoware.org
	 * @param instanceIdentifier an RDF2Go Resource identifying this instance
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 *
	 * [Generated from RDFReactor template rule #c2] 
	 */
	public Activity ( Model model, org.ontoware.rdf2go.model.node.Resource instanceIdentifier, boolean write ) {
		super(model, RDFS_CLASS, instanceIdentifier, write);
	}


	/**
	 * Returns a Java wrapper over an RDF object, identified by a URI, given as a String.
	 * Creating two wrappers for the same URI is legal.
	 * @param model RDF2GO Model implementation, see http://rdf2go.ontoware.org
	 * @param uriString a URI given as a String
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 * @throws ModelRuntimeException if URI syntax is wrong
	 *
	 * [Generated from RDFReactor template rule #c7] 
	 */
	public Activity ( Model model, String uriString, boolean write) throws ModelRuntimeException {
		super(model, RDFS_CLASS, new URIImpl(uriString,false), write);
	}

	/**
	 * Returns a Java wrapper over an RDF object, identified by a blank node.
	 * Creating two wrappers for the same blank node is legal.
	 * @param model RDF2GO Model implementation, see http://rdf2go.ontoware.org
	 * @param bnode BlankNode of this instance
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 *
	 * [Generated from RDFReactor template rule #c8] 
	 */
	public Activity ( Model model, BlankNode bnode, boolean write ) {
		super(model, RDFS_CLASS, bnode, write);
	}

	/**
	 * Returns a Java wrapper over an RDF object, identified by 
	 * a randomly generated URI.
	 * Creating two wrappers results in different URIs.
	 * @param model RDF2GO Model implementation, see http://rdf2go.ontoware.org
	 * @param write if true, the statement (this, rdf:type, TYPE) is written to the model
	 *
	 * [Generated from RDFReactor template rule #c9] 
	 */
	public Activity ( Model model, boolean write ) {
		super(model, RDFS_CLASS, model.newRandomUniqueURI(), write);
	}

    ///////////////////////////////////////////////////////////////////
    // typing

	/**
	 * Return an existing instance of this class in the model. No statements are written.
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 * @return an instance of Activity  or null if none existst
	 *
	 * [Generated from RDFReactor template rule #class0] 
	 */
	public static Activity  getInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getInstance(model, instanceResource, Activity.class);
	}

	/**
	 * Create a new instance of this class in the model. 
	 * That is, create the statement (instanceResource, RDF.type, http://b3mn.org/stencilset/bpmn1.1#Activity).
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #class1] 
	 */
	public static void createInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.createInstance(model, RDFS_CLASS, instanceResource);
	}

	/**
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 * @return true if instanceResource is an instance of this class in the model
	 *
	 * [Generated from RDFReactor template rule #class2] 
	 */
	public static boolean hasInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.hasInstance(model, RDFS_CLASS, instanceResource);
	}

	/**
	 * @param model an RDF2Go model
	 * @return all instances of this class in Model 'model' as RDF resources
	 *
	 * [Generated from RDFReactor template rule #class3] 
	 */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Resource> getAllInstances(Model model) {
		return Base.getAllInstances(model, RDFS_CLASS, org.ontoware.rdf2go.model.node.Resource.class);
	}

	/**
	 * @param model an RDF2Go model
	 * @return all instances of this class in Model 'model' as a ReactorResult,
	 * which can conveniently be converted to iterator, list or array.
	 *
	 * [Generated from RDFReactor template rule #class3-as] 
	 */
	public static ReactorResult<? extends Activity> getAllInstances_as(Model model) {
		return Base.getAllInstances_as(model, RDFS_CLASS, Activity.class );
	}

    /**
	 * Remove rdf:type Activity from this instance. Other triples are not affected.
	 * To delete more, use deleteAllProperties
	 * @param model an RDF2Go model
	 * @param instanceResource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #class4] 
	 */
	public static void deleteInstance(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.deleteInstance(model, RDFS_CLASS, instanceResource);
	}

	/**
	 * Delete all (this, *, *), i.e. including rdf:type
	 * @param model an RDF2Go model
	 * @param resource
	 */
	public static void deleteAllProperties(Model model,	org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.deleteAllProperties(model, instanceResource);
	}

    ///////////////////////////////////////////////////////////////////
    // property access methods


    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1d97406 has at least one value set 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-static] 
     */
	public static boolean hasBpmnActivitytype(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.has(model, instanceResource, ACTIVITYTYPE);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1d97406 has at least one value set 
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-dynamic] 
     */
	public boolean hasBpmnActivitytype() {
		return Base.has(this.model, this.getResource(), ACTIVITYTYPE);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1d97406 has the given value (maybe among other values).  
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-static] 
     */
	public static boolean hasBpmnActivitytype(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(model, instanceResource, ACTIVITYTYPE);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1d97406 has the given value (maybe among other values).  
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-dynamic] 
     */
	public boolean hasBpmnActivitytype( org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(this.model, this.getResource(), ACTIVITYTYPE);
	}

     /**
     * Get all values of property Activitytype as an Iterator over RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static] 
     */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllBpmnActivitytype_asNode(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_asNode(model, instanceResource, ACTIVITYTYPE);
	}
	
    /**
     * Get all values of property Activitytype as a ReactorResult of RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static-reactor-result] 
     */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllBpmnActivitytype_asNode_(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, ACTIVITYTYPE, org.ontoware.rdf2go.model.node.Node.class);
	}

    /**
     * Get all values of property Activitytype as an Iterator over RDF2Go nodes 
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic] 
     */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllBpmnActivitytype_asNode() {
		return Base.getAll_asNode(this.model, this.getResource(), ACTIVITYTYPE);
	}

    /**
     * Get all values of property Activitytype as a ReactorResult of RDF2Go nodes 
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic-reactor-result] 
     */
	public ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllBpmnActivitytype_asNode_() {
		return Base.getAll_as(this.model, this.getResource(), ACTIVITYTYPE, org.ontoware.rdf2go.model.node.Node.class);
	}
     /**
     * Get all values of property Activitytype     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get11static] 
     */
	public static ClosableIterator<java.lang.String> getAllBpmnActivitytype(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll(model, instanceResource, ACTIVITYTYPE, java.lang.String.class);
	}
	
    /**
     * Get all values of property Activitytype as a ReactorResult of java.lang.String 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get11static-reactorresult] 
     */
	public static ReactorResult<java.lang.String> getAllBpmnActivitytype_as(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, ACTIVITYTYPE, java.lang.String.class);
	}

    /**
     * Get all values of property Activitytype     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get12dynamic] 
     */
	public ClosableIterator<java.lang.String> getAllBpmnActivitytype() {
		return Base.getAll(this.model, this.getResource(), ACTIVITYTYPE, java.lang.String.class);
	}

    /**
     * Get all values of property Activitytype as a ReactorResult of java.lang.String 
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get12dynamic-reactorresult] 
     */
	public ReactorResult<java.lang.String> getAllBpmnActivitytype_as() {
		return Base.getAll_as(this.model, this.getResource(), ACTIVITYTYPE, java.lang.String.class);
	}
 
    /**
     * Adds a value to property Activitytype as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1static] 
     */
	public static void addBpmnActivitytype( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.add(model, instanceResource, ACTIVITYTYPE, value);
	}
	
    /**
     * Adds a value to property Activitytype as an RDF2Go node 
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1dynamic] 
     */
	public void addBpmnActivitytype( org.ontoware.rdf2go.model.node.Node value) {
		Base.add(this.model, this.getResource(), ACTIVITYTYPE, value);
	}
    /**
     * Adds a value to property Activitytype from an instance of java.lang.String 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #add3static] 
     */
	public static void addBpmnActivitytype(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.String value) {
		Base.add(model, instanceResource, ACTIVITYTYPE, value);
	}
	
    /**
     * Adds a value to property Activitytype from an instance of java.lang.String 
	 *
	 * [Generated from RDFReactor template rule #add4dynamic] 
     */
	public void addBpmnActivitytype(java.lang.String value) {
		Base.add(this.model, this.getResource(), ACTIVITYTYPE, value);
	}
  

    /**
     * Sets a value of property Activitytype from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be set
	 *
	 * [Generated from RDFReactor template rule #set1static] 
     */
	public static void setBpmnActivitytype( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.set(model, instanceResource, ACTIVITYTYPE, value);
	}
	
    /**
     * Sets a value of property Activitytype from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set1dynamic] 
     */
	public void setBpmnActivitytype( org.ontoware.rdf2go.model.node.Node value) {
		Base.set(this.model, this.getResource(), ACTIVITYTYPE, value);
	}
    /**
     * Sets a value of property Activitytype from an instance of java.lang.String 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set3static] 
     */
	public static void setBpmnActivitytype(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.String value) {
		Base.set(model, instanceResource, ACTIVITYTYPE, value);
	}
	
    /**
     * Sets a value of property Activitytype from an instance of java.lang.String 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set4dynamic] 
     */
	public void setBpmnActivitytype(java.lang.String value) {
		Base.set(this.model, this.getResource(), ACTIVITYTYPE, value);
	}
  


    /**
     * Removes a value of property Activitytype as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1static] 
     */
	public static void removeBpmnActivitytype( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(model, instanceResource, ACTIVITYTYPE, value);
	}
	
    /**
     * Removes a value of property Activitytype as an RDF2Go node
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1dynamic] 
     */
	public void removeBpmnActivitytype( org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(this.model, this.getResource(), ACTIVITYTYPE, value);
	}
    /**
     * Removes a value of property Activitytype given as an instance of java.lang.String 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove3static] 
     */
	public static void removeBpmnActivitytype(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.String value) {
		Base.remove(model, instanceResource, ACTIVITYTYPE, value);
	}
	
    /**
     * Removes a value of property Activitytype given as an instance of java.lang.String 
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove4dynamic] 
     */
	public void removeBpmnActivitytype(java.lang.String value) {
		Base.remove(this.model, this.getResource(), ACTIVITYTYPE, value);
	}
  
    /**
     * Removes all values of property Activitytype     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #removeall1static] 
     */
	public static void removeAllBpmnActivitytype( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.removeAll(model, instanceResource, ACTIVITYTYPE);
	}
	
    /**
     * Removes all values of property Activitytype	 *
	 * [Generated from RDFReactor template rule #removeall1dynamic] 
     */
	public void removeAllBpmnActivitytype() {
		Base.removeAll(this.model, this.getResource(), ACTIVITYTYPE);
	}
     /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1f3b292 has at least one value set 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-static] 
     */
	public static boolean hasBpmnName(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.has(model, instanceResource, NAME);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1f3b292 has at least one value set 
     * @return true if this property has at least one value
	 *
	 * [Generated from RDFReactor template rule #get0has-dynamic] 
     */
	public boolean hasBpmnName() {
		return Base.has(this.model, this.getResource(), NAME);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1f3b292 has the given value (maybe among other values).  
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-static] 
     */
	public static boolean hasBpmnName(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(model, instanceResource, NAME);
	}

    /**
     * Check if org.ontoware.rdfreactor.generator.java.JProperty@1f3b292 has the given value (maybe among other values).  
	 * @param value the value to be checked
     * @return true if this property contains (maybe among other) the given value
	 *
	 * [Generated from RDFReactor template rule #get0has-value-dynamic] 
     */
	public boolean hasBpmnName( org.ontoware.rdf2go.model.node.Node value ) {
		return Base.hasValue(this.model, this.getResource(), NAME);
	}

     /**
     * Get all values of property Name as an Iterator over RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static] 
     */
	public static ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllBpmnName_asNode(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_asNode(model, instanceResource, NAME);
	}
	
    /**
     * Get all values of property Name as a ReactorResult of RDF2Go nodes 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get7static-reactor-result] 
     */
	public static ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllBpmnName_asNode_(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, NAME, org.ontoware.rdf2go.model.node.Node.class);
	}

    /**
     * Get all values of property Name as an Iterator over RDF2Go nodes 
     * @return a ClosableIterator of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic] 
     */
	public ClosableIterator<org.ontoware.rdf2go.model.node.Node> getAllBpmnName_asNode() {
		return Base.getAll_asNode(this.model, this.getResource(), NAME);
	}

    /**
     * Get all values of property Name as a ReactorResult of RDF2Go nodes 
     * @return a List of RDF2Go Nodes
	 *
	 * [Generated from RDFReactor template rule #get8dynamic-reactor-result] 
     */
	public ReactorResult<org.ontoware.rdf2go.model.node.Node> getAllBpmnName_asNode_() {
		return Base.getAll_as(this.model, this.getResource(), NAME, org.ontoware.rdf2go.model.node.Node.class);
	}
     /**
     * Get all values of property Name     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get11static] 
     */
	public static ClosableIterator<java.lang.String> getAllBpmnName(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll(model, instanceResource, NAME, java.lang.String.class);
	}
	
    /**
     * Get all values of property Name as a ReactorResult of java.lang.String 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get11static-reactorresult] 
     */
	public static ReactorResult<java.lang.String> getAllBpmnName_as(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		return Base.getAll_as(model, instanceResource, NAME, java.lang.String.class);
	}

    /**
     * Get all values of property Name     * @return a ClosableIterator of $type
	 *
	 * [Generated from RDFReactor template rule #get12dynamic] 
     */
	public ClosableIterator<java.lang.String> getAllBpmnName() {
		return Base.getAll(this.model, this.getResource(), NAME, java.lang.String.class);
	}

    /**
     * Get all values of property Name as a ReactorResult of java.lang.String 
     * @return a ReactorResult of $type which can conveniently be converted to iterator, list or array
	 *
	 * [Generated from RDFReactor template rule #get12dynamic-reactorresult] 
     */
	public ReactorResult<java.lang.String> getAllBpmnName_as() {
		return Base.getAll_as(this.model, this.getResource(), NAME, java.lang.String.class);
	}
 
    /**
     * Adds a value to property Name as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1static] 
     */
	public static void addBpmnName( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.add(model, instanceResource, NAME, value);
	}
	
    /**
     * Adds a value to property Name as an RDF2Go node 
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #add1dynamic] 
     */
	public void addBpmnName( org.ontoware.rdf2go.model.node.Node value) {
		Base.add(this.model, this.getResource(), NAME, value);
	}
    /**
     * Adds a value to property Name from an instance of java.lang.String 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #add3static] 
     */
	public static void addBpmnName(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.String value) {
		Base.add(model, instanceResource, NAME, value);
	}
	
    /**
     * Adds a value to property Name from an instance of java.lang.String 
	 *
	 * [Generated from RDFReactor template rule #add4dynamic] 
     */
	public void addBpmnName(java.lang.String value) {
		Base.add(this.model, this.getResource(), NAME, value);
	}
  

    /**
     * Sets a value of property Name from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be set
	 *
	 * [Generated from RDFReactor template rule #set1static] 
     */
	public static void setBpmnName( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.set(model, instanceResource, NAME, value);
	}
	
    /**
     * Sets a value of property Name from an RDF2Go node.
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set1dynamic] 
     */
	public void setBpmnName( org.ontoware.rdf2go.model.node.Node value) {
		Base.set(this.model, this.getResource(), NAME, value);
	}
    /**
     * Sets a value of property Name from an instance of java.lang.String 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set3static] 
     */
	public static void setBpmnName(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.String value) {
		Base.set(model, instanceResource, NAME, value);
	}
	
    /**
     * Sets a value of property Name from an instance of java.lang.String 
     * First, all existing values are removed, then this value is added.
     * Cardinality constraints are not checked, but this method exists only for properties with
     * no minCardinality or minCardinality == 1.
	 * @param value the value to be added
	 *
	 * [Generated from RDFReactor template rule #set4dynamic] 
     */
	public void setBpmnName(java.lang.String value) {
		Base.set(this.model, this.getResource(), NAME, value);
	}
  


    /**
     * Removes a value of property Name as an RDF2Go node 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1static] 
     */
	public static void removeBpmnName( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(model, instanceResource, NAME, value);
	}
	
    /**
     * Removes a value of property Name as an RDF2Go node
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove1dynamic] 
     */
	public void removeBpmnName( org.ontoware.rdf2go.model.node.Node value) {
		Base.remove(this.model, this.getResource(), NAME, value);
	}
    /**
     * Removes a value of property Name given as an instance of java.lang.String 
     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove3static] 
     */
	public static void removeBpmnName(Model model, org.ontoware.rdf2go.model.node.Resource instanceResource, java.lang.String value) {
		Base.remove(model, instanceResource, NAME, value);
	}
	
    /**
     * Removes a value of property Name given as an instance of java.lang.String 
	 * @param value the value to be removed
	 *
	 * [Generated from RDFReactor template rule #remove4dynamic] 
     */
	public void removeBpmnName(java.lang.String value) {
		Base.remove(this.model, this.getResource(), NAME, value);
	}
  
    /**
     * Removes all values of property Name     * @param model an RDF2Go model
     * @param resource an RDF2Go resource
	 *
	 * [Generated from RDFReactor template rule #removeall1static] 
     */
	public static void removeAllBpmnName( Model model, org.ontoware.rdf2go.model.node.Resource instanceResource) {
		Base.removeAll(model, instanceResource, NAME);
	}
	
    /**
     * Removes all values of property Name	 *
	 * [Generated from RDFReactor template rule #removeall1dynamic] 
     */
	public void removeAllBpmnName() {
		Base.removeAll(this.model, this.getResource(), NAME);
	}
 }