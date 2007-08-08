/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package  org.jasig.portal.layout.simple;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.layout.IUserLayoutStore;


/**
 * Oracle optimized SQL implementation of the 2.x relational database model
 *
 * Sequence numbers have the form of {Table Name}_SEQ and, at the moment, they must
 * have been created by hand before uPortal is started. See UP_SEQUENCE in properties/data.xml
 * for the tables that expect sequence counters, and the expected starting value.
 * 
 * Prior to uPortal 2.5 this class existed in the package org.jasig.portal.  It was moved
 * to its present package to express that it is part of the Simple Layout Manager
 * implementation.
 * 
 * @author George Lindholm
 * @version $Revision$
 */
public final class OracleUserLayoutStore extends RDBMUserLayoutStore
    implements IUserLayoutStore {
    
  public OracleUserLayoutStore() throws Exception {
    super();
  }

  /* DBCounter */
  /*
   * get&increment method.
   */

   /**
    * Get the next incremental value
   * @param tableName
   * @exception Exception
    */
  public int getIncrementIntegerId (String tableName) throws Exception {
    int id;
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT " + tableName + "_SEQ.NEXTVAL FROM DUAL";
        if (log.isDebugEnabled())
            log.debug("OracleUserLayoutStore::getIncrementInteger(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          rs.next();            // If this doesn't work then the database is munged up
          id = rs.getInt(1);
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
    return  id;
  }

  /**
   * Create a sequence counter
   * @param tableName
   * @exception Exception
   */
  public void createCounter (String tableName) throws Exception {
    createCounter(tableName, 1);
  }

  /**
   * Create a sequence counter, starting with a specific value
   * @param tableName
   * @param startAt
   * @exception Exception
   */
  protected void createCounter (String tableName, int startAt) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sInsert = "CREATE SEQUENCE " + tableName + "_SEQ INCREMENT BY 1 START WITH " + startAt + " NOMAXVALUE NOCYCLE";
        log.debug("OracleUserLayoutStore::createCounter(): " + sInsert);
        stmt.executeUpdate(sInsert);
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  /**
   * Modify the current value of a counter
   * @param tableName
   * @param value
   * @exception Exception
   */
  public synchronized void setCounter (String tableName, int value) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {

        /* This is dangerous */
        String sUpdate = "DROP SEQUENCE " + tableName + "_SEQ";
        log.debug("OracleUserLayoutStore::setCounter(): " + sUpdate);
        stmt.executeUpdate(sUpdate);
        createCounter(tableName, value);
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }
}



