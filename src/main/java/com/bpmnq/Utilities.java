package com.bpmnq;

import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.naming.ConfigurationException;

import org.apache.log4j.*;
import com.bpmnq.GraphObject.GraphObjectType;
import com.bpmnq.compliancechecker.BusinessContext;

public final class Utilities 
{
    public static Statement st;
    public static Connection connection;
    public static ResultSet rs;

    // return code constants
    public static final int ABNORMAL_TERMINATION = -1;
    public static final int INVALID_FILE_PATH = -5;
    public static final int FILE_IO_FAILURE = -4;

    // file path constants
    // actual initialization takes place in static constructor below
    public static String LOLA_PATH;
    public static String LOLA_PATH_DEADLOCK;
    public static String LOLA_PATH_BOUNDED;
    public static String NUSMV_PATH;
    public static String TEMP_DIRECTORY;
    public static String OUTPUT_DIRECTORY;
    public static Properties config;
    public static String QUERY_PROCESSOR_TYPE;

    //  public static boolean stillQueryEventNodeRefinement=false;
    //  public static boolean stillQueryGateWayNodeRefinement=false;
    //  public static ArrayList<QueryGraph> initialQueryGraphs = new ArrayList<QueryGraph>();
    //  public static ArrayList<QueryGraph> intermediateQueryGraphs = new ArrayList<QueryGraph>();
    private static Logger log; 

    private static final String propertiesFileName = "bpmnq.properties"; 
    private static boolean connectionIsOpen = false;
    private static int counter = 0;
    private static Utilities instance = null;

    private Utilities() throws ConfigurationException {
	log = Logger.getLogger(Utilities.class);

	config = new Properties();
	InputStream defaultPropertiesStr = Utilities.class.getResourceAsStream("/" + propertiesFileName);
	if (defaultPropertiesStr == null) {
	    log.fatal("You do not have a configuration file '" + propertiesFileName +"' in the current directory.");
	    log.fatal("Please rename the provided file 'bpmnq.templ.properties' to '" + propertiesFileName +"' and adapt this according to your specific setup.");
	    throw new ConfigurationException("No configuration file '" + propertiesFileName +"' was found");
	}
	try {
	    config.load(defaultPropertiesStr);
	} catch (IOException e) {
	    log.fatal("Cannot load configuration from properties file", e);
	} finally {
	    try
	    {
		defaultPropertiesStr.close();
	    } catch (IOException e) {}
	}
	
	// if a user-specific properties file exist, read it in and use the above 
	// loaded properties as default values
	InputStream userPropStr = null;
	try
	{
	    File userProperties = new File(System.getProperty("user.dir"), propertiesFileName);
	    if (userProperties.exists()) {
		log.info("Using user properties file " + userProperties.getAbsolutePath());
	        userPropStr = new BufferedInputStream(
	    	    new FileInputStream(userProperties));
	        Properties userConfig = new Properties(config);
	        userConfig.load(userPropStr);
	        
	        // replace config object with user-specific properties, but use old config as default value store
	        config = userConfig;
	    }
	} catch (IOException e)
	{
	    log.fatal("Loading user configuration properties file " 
		    + propertiesFileName + "failed.", e);
	} finally {
	    try
	    {
		if (userPropStr != null)
		    userPropStr.close();
	    } catch (IOException e) {}
	}

	// set Temp directory
	String tempDir = config.getProperty("bpmnq.tempdir", ".");
	if ("".equals(tempDir))
	    tempDir = ".";
	if (tempDir == null)
	    tempDir = System.getenv("TEMP");
	if (tempDir == null)
	    tempDir = System.getenv("TMP");
	if (tempDir == null) 
	    tempDir = ".";
	TEMP_DIRECTORY = tempDir;

	OUTPUT_DIRECTORY = config.getProperty("bpmnq.outputdir", ".");
	LOLA_PATH = config.getProperty("bpmnq.lola.full");
	LOLA_PATH_BOUNDED = config.getProperty("bpmnq.lola.bounded");
	LOLA_PATH_DEADLOCK = config.getProperty("bpmnq.lola.deadlock");
	NUSMV_PATH = config.getProperty("bpmnq.nusmv");
	QUERY_PROCESSOR_TYPE = config.getProperty("bpmnq.queryprocessor", "MEMORY");
	
    }
    
    public static Utilities getInstance() throws ConfigurationException {
	if (instance == null) {
	    instance = new Utilities();
	}
	return instance;
    }

    /**
     * Computes the intersection list of both parameters. 
     * 
     * Every element in <code>v1</code> that is present in <code>v2</code> will be 
     * contained in the result. Duplicate elements are preserved. Ordering of 
     * elements depends on the order in <code>v1</code>.
     */
    public static <T> List<T> intersect(List<T> v1, List<T> v2) {
	List<T> result = new ArrayList<T>();

	for (int i = 0; i < v1.size(); i++)
	    for (int j = 0; j < v2.size(); j++)
		if (v1.get(i).equals(v2.get(j))) {
		    result.add(v2.get(j));
		    break;
		}
	return result;
    }

    /**
     * Computes the union list of both parameters. 
     * 
     * Every element in <code>v1</code> or in <code>v2</code> will be 
     * contained in the result. Duplicate elements in <code>v1</code>are preserved, 
     * duplicates in <code>v2</code> are not copied. Elements contained in both lists
     * will be present only once in the result.  
     */
    public static <T> List<T> union(List<T> v1, List<T> v2) {
	List<T> result = new ArrayList<T>(v1.size() + v2.size());
	result.addAll(v1);

	for (int j = 0; j < v2.size(); j++)
	    if (!result.contains(v2.get(j))) {
		result.add(v2.get(j));
	    }
	return result;
    }

    public static int getNextVal()
    {
	return ++counter;
    }
    
    public static void openConnection() throws Exception {
	if (connectionIsOpen)
	    return;

	Class.forName("org.postgresql.Driver").newInstance();

	String hostname = config.getProperty("bpmnq.database.hostname", "localhost");
	String databaseName = config.getProperty("bpmnq.database.databasename"); 
	String username = config.getProperty("bpmnq.database.username");
	String password = config.getProperty("bpmnq.database.password");
	String url = "jdbc:postgresql://" + hostname + "/" + databaseName;

	//	con = DriverManager.getConnection(url,"postgres", "postgres");
	//	st1 = con.createStatement(  ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	//Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
	//String url = "jdbc:odbc:BPMN_GRAPH_CON";

	try {
	    connection = DriverManager.getConnection(url, username, password);//DriverManager.getConnection(url,"", "");
	    st = getDbStatemement(); 
	}
	catch(SQLException e) {
	    if (e.getErrorCode() == 17) {
		log.fatal("Repository unavailable, closing...", e);
//		System.exit(-1);
	    } 
//	    else {
//		log.fatal(e.getMessage());
	    	System.out.println(e.getMessage());
		throw e;
//	    }
	}
	if (connection != null && st != null) {
	    connectionIsOpen = true;
	}
    }

    /**
     * Returns a statement object on the database connection.
     * @throws SQLException
     */
    public static Statement getDbStatemement() throws SQLException
    {
	return Utilities.connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
	    ResultSet.CONCUR_UPDATABLE);
    }


    public static void closeConnection() throws SQLException {
	if (!connectionIsOpen)
	    return;

	st.close();
	connection.close();

	st = null;
	connection = null;
	connectionIsOpen = false;
    }

    public static boolean isConnectionOpen() {
	return connectionIsOpen;
    }

    public static String getActivityID(String actName, String modelId) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"ID\" from \"BPMN_GRAPH\".\"ACTIVITY\" where \"MOD_ID\" =? and upper(\"NAME\")=upper(?)",
		Integer.parseInt(modelId), actName);
	if (lrs.next())
	{
	    return lrs.getString("ID");
	}

	return "0";
    }

    public static String getGatewayType(int gateID) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"NAME\", \"GATE_WAY_TYPE\" from \"BPMN_GRAPH\".\"GATEWAY\" where \"ID\" =?",
		gateID);
	if (lrs.next())
	{
	    return lrs.getString("GATE_WAY_TYPE");
	}

	return "";
    }

    public static String getOryxGatewayType(String gateID) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"NAME\", \"GATE_WAY_TYPE\" from \"BPMN_GRAPH\".\"ORYX_GATEWAY\" where \"ID\" =?",
		gateID);
	if (lrs.next())
	{
	    return lrs.getString("GATE_WAY_TYPE");
	}

	return "";
    }
    
    public static String getGatewayName(int gateID) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"NAME\", \"GATE_WAY_TYPE\" from \"BPMN_GRAPH\".\"GATEWAY\" where \"ID\" =?",
		gateID);
	if (lrs.next())
	{
	    return lrs.getString("NAME");
	}

	return "";
    }

    public static String getOryxGatewayName(String gateID) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"NAME\", \"GATE_WAY_TYPE\" from \"BPMN_GRAPH\".\"ORYX_GATEWAY\" where \"ID\" =?",
		gateID);
	if (lrs.next())
	{
	    return lrs.getString("NAME");
	}

	return "";
    }
    public static String getActivityName(int actID) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"NAME\" from \"BPMN_GRAPH\".\"ACTIVITY\" where \"ID\" =?",
		actID);
	if (lrs.next())
	{
	    return lrs.getString("NAME");
	}
	
	return "";
    }

    public static List<String> getEventID(int eventPosition, String modelId) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"ID\" from \"BPMN_GRAPH\".\"EVENT\" where \"MODEL_ID\" =? and \"EVE_POSITION\"= ?", 
		Integer.parseInt(modelId), eventPosition);
	List<String> rslt = new ArrayList<String>(10);
	while (lrs.next())
	{
	    rslt.add( lrs.getString("ID"));
	}

	return rslt;
    }

    public static String getEventName(int eveID) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"NAME\", \"EVE_POSITION\" from \"BPMN_GRAPH\".\"EVENT\" where \"ID\" =?", 
		eveID);
	if (lrs.next())
	{
	    return lrs.getString("NAME");
	}

	return "";
    }
    public static String getOryxEventName(String eveID) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"NAME\", \"EVE_POSITION\" from \"BPMN_GRAPH\".\"ORYX_EVENT\" where \"ID\" =?", 
		eveID);
	if (lrs.next())
	{
	    return lrs.getString("NAME");
	}

	return "";
    }
    public static int getEventPosition(int eveID) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"NAME\", \"EVE_POSITION\" from \"BPMN_GRAPH\".\"EVENT\" where \"ID\" =?", 
		eveID);
	if (lrs.next())
	{
	    return lrs.getInt("EVE_POSITION");
	}

	return -1;
    }
    public static int getOryxEventPosition(String eveID) throws SQLException
    {
	ResultSet lrs = executePrepQuery("Select \"NAME\", \"EVE_POSITION\" from \"BPMN_GRAPH\".\"ORYX_EVENT\" where \"ID\" =?", 
		eveID);
	if (lrs.next())
	{
	    return lrs.getInt("EVE_POSITION");
	}

	return -1;
    }
    /**
     * Executes a prepared statement SQL string. Before executing, placeholders are replaced with
     * supplied query arguments. 
     * @param prepQuery
     * 		An SQL prepared statement string. "?" acts as a placeholder for the supplied values.
     * @param args
     * 		Variable-length argument list. Currently supported argument types are int and String.
     * @return
     * 		The result set retrieved for this query.
     * @throws SQLException
     */
    protected static ResultSet executePrepQuery(String prepQuery, Object... args) throws SQLException {
	PreparedStatement stmt = connection.prepareStatement(prepQuery);
	int i = 0;
	for (Object arg : args)
	{
	    i++;
	    if (arg instanceof Integer)
	    {
		stmt.setInt(i, (Integer) arg);
	    } else if (arg instanceof String)
	    {
		stmt.setString(i, (String) arg);
	    } else
		throw new IllegalArgumentException("Argument type " + arg.getClass().toString() + "is not supported for prepared statements.");
	}
	ResultSet result = stmt.executeQuery();
	return result;
    }

    public static String prepareSQLExcludeStatement(String src, String dst,String ModelID,String type) throws SQLException
    {
	ResultSet lrs;

	String selExp = "SELECT 'GAT' + CAST (ID AS VARCHAR) AS ND FROM GATEWAY WHERE MODEL_ID = " + ModelID +" AND GATE_WAY_TYPE = '"+type +"' " +
	"       AND 'GAT' + CAST (ID AS VARCHAR) <> '"+src+"' and 'GAT' + CAST (ID AS VARCHAR) <> '"+dst+"'";

	StringBuffer result = new StringBuffer(500);
//	result.append(" AND CHARINDEX('''+'GAT0'+''',PATH+'''+','+'''+ENE,1) =0");
	result.append(" AND \"BPMN_GRAPH\".charindex(''GAT0'',\"PATH\"||'',''||\"ENE\",1) =0");

	lrs = Utilities.st.executeQuery(selExp);
	while (lrs.next())
	{
	    result.append(" AND ");
//	    result.append(" CHARINDEX('''+'"+lrs.getString("ND")+"'+''',PATH+'''+','+'''+ENE,1) =0");
	    result.append(" \"BPMN_GRAPH\".charindex(''"+lrs.getString("ND")+"'',\"PATH\"||'',''||\"ENE\",1) =0");
	}
	//String selExp2 = "SELECT path+'''+','+'''+ene FROM newp WHERE MODEL_ID ="+ModelID +" AND  bni ='''+'"+src+"'+'''  and ene ='''+'"+dst+"'+'''";
	//selExp2 += result;
	return result.toString();
    }

    public static String prepareSQLExcludeMultiple(String src, String dst,
	    String modelId, String excludeStr) throws SQLException {
	StringBuffer selExp2 = new StringBuffer(500);
//	selExp2.append("SELECT path+'''+','+'''+ene FROM newp WHERE MODEL_ID ="+ModelID +" AND  bni ='''+'"+src+"'+'''  and ene ='''+'"+dst+"'+'''");
	selExp2.append("SELECT \"PATH\" || '',''||\"ENE\" FROM \"BPMN_GRAPH\".\"NEWP\" WHERE \"Model_Id\" ="+modelId +" AND  \"BNI\" =''"+src+"'' AND \"ENE\" =''"+dst+"''");
	StringTokenizer strTok = new StringTokenizer(excludeStr,",");
	String token;
	String result;
	while(strTok.hasMoreTokens())
	{
	    token = strTok.nextToken();
	    if (token.startsWith("\""))
		token = token.substring(1, token.length());
	    if (token.endsWith("\""))
		token = token.substring(0, token.length()-1);

	    if (token.equals("XORJOIN"))
	    {
		result = Utilities.prepareSQLExcludeStatement(src, dst, modelId, "XOR JOIN");
		selExp2.append( result);
	    }
	    else if (token.equals("XORSPLIT"))
	    {
		result = Utilities.prepareSQLExcludeStatement(src, dst, modelId, "XOR SPLIT");
		selExp2.append( result);

	    }
	    else if (token.equals("ANDJOIN"))
	    {
		result = Utilities.prepareSQLExcludeStatement(src, dst, modelId, "AND JOIN");
		selExp2.append( result);
	    }
	    else if (token.equals("ANDSPLIT"))
	    {
		result = Utilities.prepareSQLExcludeStatement(src, dst, modelId, "AND SPLIT");
		selExp2.append( result);
	    }
	    else if (token.equals("ORJOIN"))
	    {
		result = Utilities.prepareSQLExcludeStatement(src, dst, modelId, "OR JOIN");
		selExp2.append( result);
	    }
	    else if (token.equals("ORSPLIT"))
	    {
		result = Utilities.prepareSQLExcludeStatement(src, dst, modelId, "OR SPLIT");
		selExp2.append( result);
	    }
	    else if (token.startsWith("GAT") || token.startsWith("EVE")|| token.startsWith("ACT"))
	    {
		selExp2.append(" AND \"BPMN_GRAPH\".charindex(''"+ token+"'',\"PATH\"||'',''||\"ENE\",1) =0 ");
//		ResultSet rs;
//		try
//		{
//		// we have first to check that the node has a path to destination or not
//		int reslt;
//		rs = Utilities.st.executeQuery("exec pathExists '"+ token +"','"+ dst +"'");
//		if(rs.next())
//		{
//		reslt = rs.getInt("cnt");
//		if (reslt > 1)
//		{
//		rs = Utilities.st.executeQuery("exec get_parallels_of_activity "+ token.substring(3));
//		while(rs.next())
//		{
//		selExp2.append(" AND CHARINDEX(''"+ rs.getString("id")+"'',PATH+'',''+ENE,1) =0 ");
//		}
//		}
//		}


//		}
//		catch (SQLException sqe)
//		{
//		System.out.println("get_parallels_of_activity "+ sqe.getMessage());
//		}
	    }
	    else // this is an activity
	    {
		GraphObject currentNode = new GraphObject();
		currentNode.setName(token);
		//currentNode.name = exclude;
		currentNode.type = GraphObjectType.ACTIVITY;
		currentNode.setID(Utilities.getActivityID(currentNode.getName(), modelId));
//		selExp2.append(" AND CHARINDEX(''"+ currentNode.toString()+"'',PATH+'',''+ENE,1) =0 ");
		selExp2.append(" AND \"BPMN_GRAPH\".charindex(''"+ currentNode.toString()+"'',\"PATH\"||'',''||\"ENE\",1) =0 ");
		// get all parallels
		// we have first to check that the node has a path to destination or not
		int reslt;
		ResultSet rs = st.executeQuery("exec pathExists '"+ currentNode.toString() +"','"+ dst +"'");
		if(rs.next())
		{
		    reslt = rs.getInt("cnt");
		    if (reslt > 1)
		    {
			rs = st.executeQuery("exec get_parallels_of_activity "+ currentNode.getID());
			while(rs.next())
			{
			    selExp2.append(" AND CHARINDEX(''"+ rs.getString("id")+"'',PATH+'',''+ENE,1) =0 ");
			}
		    }
		}
	    }
	}
	return selExp2.toString();
    }

    public static String makeTempfilePath(String filename) {
	if (Utilities.TEMP_DIRECTORY.equals("."))
	{
	    return filename;
	}
	else
	    return Utilities.TEMP_DIRECTORY + File.separator + filename;
    }

    public static String makeOutputfilePath(String filename) {
	return Utilities.OUTPUT_DIRECTORY + File.separator + filename;
    }

    /**
     * Runs a LoLa instance, passing it the name of the net file.
     * 
     * @param lolaPath One of the path constants defined in <code>Utilities</code>. 
     *     Intended to allow selection of a specialized LoLa version 
     * @param netFilename Path name of the file to be passed to LoLa. The file should describe 
     *     a net in LoLa's syntax. If the file name is not an absolute one, it will be treated 
     *     as relative to <code>Utilities.TEMP_DIRECTORY</code>  
     * @return Complete output of the LoLa run. Each line from stdout is put into 
     *     a separate list element. 
     */
    public static ArrayList<String> callLoLA(String lolaPath, String netFilename) {
	ArrayList<String> result = new ArrayList<String>(19);
	log.debug("### Calling lola with the following command " + lolaPath + " "+ netFilename+ " -m");
	try {
	    // check for netFilename being absolute
//	    File file = new File(netFilename);
//	    if (!file.isAbsolute())
//	    {
//		netFilename = makeTempfilePath(netFilename);
//	    }
	    // Workaround for Unix systems
	    String cmd;
//	    String env = System.getProperty("os.name");
	    
	    if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
	    {
		cmd = lolaPath + " " + netFilename + " -m";
	    }
	    else
	    {
		// check that the file exists
		File f = new File("lolarun.sh");
		if (f.exists())
		{
		    f.delete();
		}
		PrintWriter shellFile = new PrintWriter(new BufferedWriter(
		    new FileWriter("lolarun.sh")));
		shellFile.println(lolaPath + " "+ netFilename +" -m");
		shellFile.flush();
		shellFile.close();
		
		Runtime.getRuntime().exec("chmod a+x lolarun.sh");
		cmd = "./lolarun.sh";
	    }
	    Process lolaProc = Runtime.getRuntime().exec(cmd);
	    BufferedReader in = new BufferedReader(new InputStreamReader(
		    lolaProc.getInputStream()));
	    String currentLine = null;
	    while ((currentLine = in.readLine()) != null)
		result.add(currentLine);

	    lolaProc.waitFor();
	} catch (InterruptedException ie) {
	    log.error("Lola terminated unexpectedly : "
		    + ie.getMessage());
	    // exit is an inappropriate reaction
	    // System.exit(Utilities.ABNORMAL_TERMINATION); // abnormal
	    throw new RuntimeException("External tool Lola terminated unexpectedly, cannot continue.", ie);
	    // termination
	} catch (IOException ioe) {
	    log.error("Failed to call lola : " + ioe.getMessage());
	    //System.exit(Utilities.INVALID_FILE_PATH);
	    throw new RuntimeException("External tool Lola caused an I/O exception, cannot continue.", ioe);
	}
	catch (Exception e)
	{
	    log.error(e.getMessage());
	}
	return result;
    }

    /**
     * Runs a NuSMV instance, passing it the name of the state file.
     * 
     * @param nuSMVPath One of the path constants defined in <code>Utilities</code>. 
     *     Intended to allow selection of a specialized NuSMV version 
     * @param stateFile Name of the file to be passed to NuSMV. The file should describe 
     *     a net in NuSMV's syntax. The file name will be treated as relative to <code>Utilities.TEMP_DIRECTORY</code>  
     * @return Complete output of the NuSMV run. Each line from stdout is put into 
     *     a separate list element. 
     */
    public static ArrayList<String> callNuSMV(String nuSMVPath, String stateFile) {
	ArrayList<String> result = new ArrayList<String>(19);
	String fullStateFilePath = stateFile;
	try {
	    String cmd;
	    if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
	    {
		cmd = nuSMVPath + " " + fullStateFilePath;
	    }
	    else
	    {
		// check that the file exists
		File f = new File("nusmvrun.sh");
		if (f.exists())
		{
		    f.delete();
		}
		PrintWriter shellFile = new PrintWriter(new BufferedWriter(
		    new FileWriter("nusmvrun.sh")));
		shellFile.println(nuSMVPath + " "+ fullStateFilePath);
		shellFile.flush();
		shellFile.close();
		
		Runtime.getRuntime().exec("chmod a+x nusmvrun.sh");
		cmd = "./nusmvrun.sh";
	    }

	    Process NuSMVProc = Runtime.getRuntime().exec(
		    cmd);
	    BufferedReader in = new BufferedReader(new InputStreamReader(
		    NuSMVProc.getInputStream()));
	    String currentLine = null;
	    while ((currentLine = in.readLine()) != null)
		result.add(currentLine);
	    int result2 = NuSMVProc.waitFor();
	    log.debug("NuSMV return code is :" + result2);
	} catch (IOException ioe) {
	    log.error("Failed to call NuSMV : " + ioe.getMessage());
	    // System.exit(Utilities.INVALID_FILE_PATH);
	    throw new RuntimeException("External tool NuSMV caused an I/O exception, cannot continue.", ioe);
	} catch (InterruptedException ie) {
	    log.error("NuSMV terminated unexpectedly : "
		    + ie.getMessage());
	    // System.exit(Utilities.ABNORMAL_TERMINATION); // abnormal termination
	    throw new RuntimeException("External tool NuSMV terminated unexpectedly, cannot continue.", ie);
	}
	return result;
    }

    public static String getModelFilePath(String model) throws SQLException {
	ResultSet rslt;
	rslt = executePrepQuery("Select \"Model_file_path\" From \"BPMN_GRAPH\".\"MODEL\" where \"ID\" =?", model);

	if (rslt.next())
	    return rslt.getString(1);
	else
	    return "NULL";	
    }

    /**
     * this method returns an sql statements string looking 
     * for predecessors of an Activity 
     */
    private static String getQryActStatement(String currentNode)
    {
    	String qryTxtACT = "Select 'ACT' ||\"FRM_ACT_ID\" AS PRED FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"FRM_ACT_ID\" IS NOT NULL AND \"TO_ACT_ID\" ="+currentNode.substring(3) +
    	" UNION Select 'GAT'||\"FRM_GAT_ID\" AS PRED FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"FRM_GAT_ID\" IS NOT NULL AND \"TO_ACT_ID\" ="+currentNode.substring(3) +
    	" UNION Select 'EVE'||\"FRM_EVE_ID\" AS PRED FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"FRM_EVE_ID\" IS NOT NULL AND \"TO_ACT_ID\" ="+currentNode.substring(3);
    	
    	return qryTxtACT;
    }
    
    private static String getQryGaTStatement(String currentNode)
    {
    	String qryTxtGAT = "Select 'ACT' ||\"FRM_ACT_ID\" AS PRED FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"FRM_ACT_ID\" IS NOT NULL AND \"TO_GAT_ID\" ="+currentNode.substring(3) +
    	" UNION Select 'GAT'||\"FRM_GAT_ID\" AS PRED FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"FRM_GAT_ID\" IS NOT NULL AND \"TO_GAT_ID\" ="+currentNode.substring(3) +
    	" UNION Select 'EVE'||\"FRM_EVE_ID\" AS PRED FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"FRM_EVE_ID\" IS NOT NULL AND \"TO_GAT_ID\" ="+currentNode.substring(3);
    	
    	return qryTxtGAT;
    	
    }
    
    private static String getQryEveStatement(String currentNode)
    {
    	String qryTxtEVE = "Select 'ACT' ||\"FRM_ACT_ID\" AS PRED FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"FRM_ACT_ID\" IS NOT NULL AND \"TO_EVE_ID\" ="+currentNode.substring(3) +
    	" UNION Select 'GAT'||\"FRM_GAT_ID\" AS PRED FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"FRM_GAT_ID\" IS NOT NULL AND \"TO_EVE_ID\" ="+currentNode.substring(3) +
    	" UNION Select 'EVE'||\"FRM_EVE_ID\" AS PRED FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"FRM_EVE_ID\" IS NOT NULL AND \"TO_EVE_ID\" ="+currentNode.substring(3);
    	
    	return qryTxtEVE;
    }
    
    /**
     * 
     * @param act
     * 		always encodes an activity node not any other type at the beginning
     * @return
     * @throws SQLException 
     */
    private static String getEnclosingANDSplit(String act) throws SQLException
    {
	StringBuilder result = new StringBuilder(100);
	ResultSet rss;
	String currentNode = act;
	String lastVisitedANDSplit = "";
	int visitedANDJoins = 0;
	int visitedSelectionJoins = 0;
	List<String> visited = new ArrayList<String>();
	visited.add(currentNode);

	boolean more = true;

	while (more)
	{
	    if (currentNode.startsWith("ACT"))
	    {
		rss = st.executeQuery(getQryActStatement(currentNode));
	    } else if (currentNode.startsWith("EVE"))
	    {
		rss = st.executeQuery(getQryEveStatement(currentNode));
	    } else
	    {
		rss = st.executeQuery(getQryGaTStatement(currentNode));
	    }

	    boolean predFound = false;

	    while (rss.next())
	    {
		predFound = true;
		currentNode = rss.getString("PRED");
		if (visited.contains(currentNode)) 
		{
		    if (visitedANDJoins > 0 ) 
			return lastVisitedANDSplit;
		    else 
			return "";
		}

		if (currentNode.startsWith("GAT"))
		{
		    if (getGatewayType(Integer.parseInt(currentNode.substring(3))).contains("OR JOIN"))
		    {
			visitedSelectionJoins++;
		    } else if(getGatewayType(Integer.parseInt(currentNode.substring(3))).contains("OR SPLIT"))
		    {
			if (visitedSelectionJoins==0)
			    return "";
			visitedSelectionJoins--;
		    }

		    if ("AND JOIN".equals(getGatewayType(Integer.parseInt(currentNode.substring(3)))))
		    {
			visitedANDJoins++;
		    } else if ("AND SPLIT".equals(getGatewayType(Integer.parseInt(currentNode.substring(3)))))
		    {
			lastVisitedANDSplit = currentNode;
			if (visitedANDJoins == 0)
			{
			    return lastVisitedANDSplit;
			} else
			    visitedANDJoins--;
		    }

		}
		visited.add(currentNode);
	    }

	    if (!predFound)// no more predecessors
	    {
		if (visitedANDJoins > 0 ) 
		    result.append(lastVisitedANDSplit);
		break;
	    }
	}

	return result.toString();
    }
    
    public static String getParallelsOfActivity(String act)
    {
	StringBuilder ANDSplit = new StringBuilder(10);
	try
	{
	    ANDSplit.append(getEnclosingANDSplit(act));
	} catch (SQLException e)
	{
	    log.error("Database error. Results may be incorrect!", e);
	}
	
	return ANDSplit.toString();
	
//	StringBuilder result = new StringBuilder(100);
//	if (ANDSplit.length() == 0 ) return "";
//	String qryTxtGAT = "Select 'ACT' ||\"TO_ACT_ID\" AS SUCC FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"TO_ACT_ID\" IS NOT NULL AND \"FRM_GAT_ID\" ="+ANDSplit.substring(3) +
//	" UNION Select 'GAT'||\"TO_GAT_ID\" AS SUCC FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"TO_GAT_ID\" IS NOT NULL AND \"FRM_GAT_ID\" ="+ANDSplit.substring(3) +
//	" UNION Select 'EVE'||\"TO_EVE_ID\" AS SUCC FROM \"BPMN_GRAPH\".\"SEQUENCE_FLOW\" WHERE \"TO_EVE_ID\" IS NOT NULL AND \"FRM_GAT_ID\" ="+ANDSplit.substring(3);
//
//	try
//	{
//	    ResultSet rss = st.executeQuery(qryTxtGAT);
//	    while(rss.next())
//		result.append(rss.getString("SUCC")+',');
//
//	}
//	catch (SQLException e) 
//	{
//	    System.err.println(e.getMessage());
//	    System.out.println("Method Utilities.getParallelsOfActivity");
//	}
//
//
//	return result.toString();
    }

    /**
     * Takes the vendor-shipped properties file and copies it to the current directory
     * 
     * Intended for users that run BPMNQ from the big JAR file, but still want to set 
     * their own configuration properties. If that file exists, it is automatically read
     * ({@link Utilities#Utilities()})
     */
    public void writeProperties()
    {
	File userProperties = new File(System.getProperty("user.dir"), propertiesFileName);
	if (userProperties.exists()) {
	    log.fatal("A file '" + propertiesFileName + "' exists already in the current directory. Refusing to overwrite it!");
	    return;
	}
	InputStream defaultPropertiesStr = Utilities.class.getResourceAsStream("/" + propertiesFileName);
	if (defaultPropertiesStr == null) {
	    log.fatal("Cannot read vendor-shipped properies file. Aborting.");
	    return;
	}
	
	BufferedWriter userPropWriter = null;
	BufferedReader defaultPropReader = null;
	try
	{
	    userPropWriter = new BufferedWriter(new FileWriter(userProperties));
	    defaultPropReader = new BufferedReader(
		    new InputStreamReader(defaultPropertiesStr));
	    
	    String line; 
	    while ((line = defaultPropReader.readLine()) != null)
	    {
		userPropWriter.write(line);
		userPropWriter.write("\n");
	    }
	    System.out.println("Wrote default property values into file " + propertiesFileName 
		    + ". You may change that file to make personal settings.");
	} catch (IOException e)
	{
	    log.fatal("Writing properties file failed", e);
	} finally // close readers and writers
	{
	    if (defaultPropReader != null)
	    {
		try
		{
		    defaultPropReader.close();
		} catch (IOException e) { }
	    }
	    if (userPropWriter != null)
	    {
		try
		{
		    userPropWriter.close();
		} catch (IOException e) { }
	    }
	}
    }
    public static String getDataObjectName(String dobState)
    {
	String result="";
	
	for (int i = 0; i < dobState.length();i++)
	{
	    
	    if (!Character.isLowerCase(dobState.charAt(i)))
	    {
		result+=dobState.charAt(i);
	    }
	}
	result = result.substring(0, result.length()-1);
	return result;
    }
    
    public static String getDataObjectState(String dobState)
    {
	String result="";
	
	for (int i = 0; i < dobState.length();i++)
	{
	    
	    if (!Character.isUpperCase(dobState.charAt(i)))
	    {
		result+=dobState.charAt(i);
	    }
	}
	//now remove the leading underscores
	result = result.replace("_", " ");
	result = result.trim();
	result.replace(" ", "_");
	return result;
    }
    public static BusinessContext getBusinssContext()
    {
	if (isConnectionOpen())
	{	BusinessContext bc = new BusinessContext();
		bc.fullLoad();
		return bc;
	}
	return null;
    }
    public static String normalizeString(String in)
    {
	String out;
	out = in.replace("\n", "");
	out = out.replace("\t", "");
	out = out.toLowerCase();
	return out;
    }
    public static boolean isSubset(String superSet, String subSet)
    {
	if (subSet.length() == 0)
	    return true;
	
	String[] superList = superSet.split(",");
	String[] subsetList = subSet.split(",");
	
	for (String s :subsetList)
	{
	    boolean found = false;
	    for (String t : superList)
	    {
		if (s.equals(t))
		{
		    found = true;
		    break; 
		}
		
	    }
	    if (!found)
		return false;
	}
	return true;

    }
}
