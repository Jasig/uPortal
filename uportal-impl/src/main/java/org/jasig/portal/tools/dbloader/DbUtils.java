/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.tools.dbloader;

import java.io.PrintWriter;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Hashtable;
import java.util.Map;

import org.jasig.portal.RDBMServices;

/**
 * Utility class for centralizing various functions performed on the database.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
class DbUtils
{
 
    static void logDbInfo (Configuration config) throws SQLException
    {
        PrintWriter out = config.getLog();
      DatabaseMetaData dbMetaData = config.getConnection().getMetaData();
      String dbName = dbMetaData.getDatabaseProductName();
      String dbVersion = dbMetaData.getDatabaseProductVersion();
      String driverName = dbMetaData.getDriverName();
      String driverVersion = dbMetaData.getDriverVersion();
      String driverClass = RDBMServices.getJdbcDriver();
      String url = RDBMServices.getJdbcUrl();
      String user = RDBMServices.getJdbcUser();
      out.println("Starting DbLoader...");
      out.println("Database name: '" + dbName + "'");
      out.println("Database version: '" + dbVersion + "'");
      out.println("Driver name: '" + driverName + "'");
      out.println("Driver version: '" + driverVersion + "'");
      out.println("Driver class: '" + driverClass + "'");
      out.println("Connection URL: '" + url + "'");
      out.println("User: '" + user + "'");
    }

    static String getLocalDataTypeName (Configuration config,
      String genericDataTypeName)
    {


      String localDataTypeName = null;

      try
      {
        DatabaseMetaData dbmd = config.getConnection().getMetaData();
        String dbName = dbmd.getDatabaseProductName();
        String dbVersion = dbmd.getDatabaseProductVersion();
        String driverName = dbmd.getDriverName();
        String driverVersion = dbmd.getDriverVersion();

        // Check for a mapping in DbLoader.xml
        localDataTypeName = config.getMappedDataTypeName(dbName, dbVersion, driverName, driverVersion, genericDataTypeName);

        // Find the type code for this generic type name
        int dataTypeCode = DbUtils.getJavaSqlType(genericDataTypeName);
        
        if (localDataTypeName != null)
              return localDataTypeName;

        if (config.getLocalTypeMap() == null) {

            Map localTypeMap = new Hashtable();
            config.setLocalTypeMap(localTypeMap);

              try {
                  
                  ResultSet rs = dbmd.getTypeInfo();
                  try {
                      while (rs.next())
                      {
                          Integer dbTypeCode = new Integer(rs.getInt("DATA_TYPE"));
                          String dbTypeName = rs.getString("TYPE_NAME");

                          // Add only first occurence of each type code
                          // See Bugzilla for a detailed explanation                              
                          if (!localTypeMap.containsKey(dbTypeCode)) {                            
                              localTypeMap.put(dbTypeCode, dbTypeName);
                          }                          
                      }
                  }
                  finally {
                      rs.close();
                  }
              }
              catch (Exception e)
              {
                  config.getLog().println("Problem loading Database Meta Data. "
                  + e.getMessage());
                e.printStackTrace(config.getLog());
                DbLoader.exit(config);
              }
          }

        Integer dataTypeCodeObj = new Integer(dataTypeCode);
        localDataTypeName = (String)config.getLocalTypeMap().get(dataTypeCodeObj);

        if (localDataTypeName != null)
          {
            return localDataTypeName;
          }

        // No matching type found, report an error
        config.getLog().println(
            "Your database driver, '"
                + driverName
                + "', version '"
                + driverVersion
                + "', was unable to find a local type name that matches the generic type name, '"
                + genericDataTypeName
                + "'.");
        config.getLog().println(
            "Please add a mapped type for database '"
                + dbName
                + "', version '"
                + dbVersion
                + "' inside '"
                + config.getPropertiesURL()
                + "' and run this program again.");
        config.getLog().println("Exiting...");
        DbLoader.exit(config);
      }
      catch (Exception e)
      {
        e.printStackTrace(config.getLog());
        DbLoader.exit(config);
      }

      return null;
    }
    
    static void dumpTableAction(Configuration config, String tableStatement) {
        if (config.getScriptWriter() != null)
            config.getScriptWriter().println(tableStatement +
                config.getStatementTerminator());
    }
    
    static void dropTable (Configuration config, String dropTableStatement)
    {
        Statement stmt = null;


      try
      {
        stmt = config.getConnection().createStatement();
        try { stmt.executeUpdate(dropTableStatement); } 
        catch (SQLException sqle) {/*Table didn't exist*/}
      }
      catch (Exception e)
      {
          config.getLog().println(dropTableStatement);
        e.printStackTrace(config.getLog());
      }
      finally
      {
        try { if (stmt != null) stmt.close(); } catch (Exception e) { }
      }
    }

    static int getJavaSqlType (String genericDataTypeName)
    {
      // Find the type code for this generic type name
      int dataTypeCode = 0;

      if (genericDataTypeName.equalsIgnoreCase("BIT"))
        dataTypeCode = Types.BIT; // -7
      else if (genericDataTypeName.equalsIgnoreCase("TINYINT"))
        dataTypeCode = Types.TINYINT; // -6
      else if (genericDataTypeName.equalsIgnoreCase("SMALLINT"))
        dataTypeCode = Types.SMALLINT; // 5
      else if (genericDataTypeName.equalsIgnoreCase("INTEGER"))
        dataTypeCode = Types.INTEGER; // 4
      else if (genericDataTypeName.equalsIgnoreCase("BIGINT") ||
          genericDataTypeName.equalsIgnoreCase("BIGINTEGER"))
        dataTypeCode = Types.BIGINT; // -5
      else if (genericDataTypeName.equalsIgnoreCase("FLOAT"))
        dataTypeCode = Types.FLOAT; // 6
      else if (genericDataTypeName.equalsIgnoreCase("REAL"))
        dataTypeCode = Types.REAL; // 7
      else if (genericDataTypeName.equalsIgnoreCase("DOUBLE"))
        dataTypeCode = Types.DOUBLE; // 8
      else if (genericDataTypeName.equalsIgnoreCase("NUMERIC"))
        dataTypeCode = Types.NUMERIC; // 2
      else if (genericDataTypeName.equalsIgnoreCase("DECIMAL"))
        dataTypeCode = Types.DECIMAL; // 3

      else if (genericDataTypeName.equalsIgnoreCase("CHAR"))
        dataTypeCode = Types.CHAR; // 1
      else if (genericDataTypeName.equalsIgnoreCase("VARCHAR"))
        dataTypeCode = Types.VARCHAR; // 12
      else if (genericDataTypeName.equalsIgnoreCase("LONGVARCHAR"))
        dataTypeCode = Types.LONGVARCHAR; // -1

      else if (genericDataTypeName.equalsIgnoreCase("DATE"))
        dataTypeCode = Types.DATE; // 91
      else if (genericDataTypeName.equalsIgnoreCase("TIME"))
        dataTypeCode = Types.TIME; // 92
      else if (genericDataTypeName.equalsIgnoreCase("TIMESTAMP"))
        dataTypeCode = Types.TIMESTAMP; // 93

      else if (genericDataTypeName.equalsIgnoreCase("BINARY"))
        dataTypeCode = Types.BINARY; // -2
      else if (genericDataTypeName.equalsIgnoreCase("VARBINARY"))
        dataTypeCode = Types.VARBINARY; // -3
      else if (genericDataTypeName.equalsIgnoreCase("LONGVARBINARY"))
        dataTypeCode = Types.LONGVARBINARY;  // -4

      else if (genericDataTypeName.equalsIgnoreCase("NULL"))
        dataTypeCode = Types.NULL; // 0

      else if (genericDataTypeName.equalsIgnoreCase("OTHER"))
        dataTypeCode = Types.OTHER; // 1111

      else if (genericDataTypeName.equalsIgnoreCase("JAVA_OBJECT"))
        dataTypeCode = Types.JAVA_OBJECT; // 2000
      else if (genericDataTypeName.equalsIgnoreCase("DISTINCT"))
        dataTypeCode = Types.DISTINCT; // 2001
      else if (genericDataTypeName.equalsIgnoreCase("STRUCT"))
        dataTypeCode = Types.STRUCT; // 2002

      else if (genericDataTypeName.equalsIgnoreCase("ARRAY"))
        dataTypeCode = Types.ARRAY; // 2003
      else if (genericDataTypeName.equalsIgnoreCase("BLOB"))
        dataTypeCode = Types.BLOB; // 2004
      else if (genericDataTypeName.equalsIgnoreCase("CLOB"))
        dataTypeCode = Types.CLOB; // 2005
      else if (genericDataTypeName.equalsIgnoreCase("REF"))
        dataTypeCode = Types.REF; // 2006
    
    else if (genericDataTypeName.equalsIgnoreCase("DATALINK"))
        dataTypeCode = 70; // Use Types.DATALINK when JDK 1.3 support is no longer needed
    else if (genericDataTypeName.equalsIgnoreCase("BOOLEAN"))
        dataTypeCode = 16; // Use Types.BOOLEAN when JDK 1.3 support is no longer needed

      return dataTypeCode;
    }

    static void createTable (Configuration config, String createTableStatement)
    {
        Statement stmt = null;

      try
      {
        stmt = config.getConnection().createStatement();
        stmt.executeUpdate(createTableStatement);
      }
      catch (Exception e)
      {
          config.getLog().println(createTableStatement);
        e.printStackTrace(config.getLog());
      }
      finally
      {
        try { if (stmt != null) stmt.close(); } 
        catch (Exception e) { }
      }
    }


}
