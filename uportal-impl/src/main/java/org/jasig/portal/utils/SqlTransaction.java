/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.sql.Connection;
import java.sql.SQLException;

import org.jasig.portal.RDBMServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This type is a place to centralize the portal's sql transaction code.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class SqlTransaction {
    private static final Log log = LogFactory.getLog(SqlTransaction.class);
/**
 * SqlTransaction constructor comment.
 */
public SqlTransaction() {
	super();
}
/**
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
public static void begin(Connection conn) throws java.sql.SQLException {
	/*
	 * Previously this method was invoked in a try-catch SQLException block.
	 * Prior to 2.5.1, RDBMS.setAutoCommit didn't actually throw SQLException, and
	 * as of 2.5.1 its method declaration was corrected to reflect this.
	 */
    RDBMServices.setAutoCommit(conn, false);
}
/**
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
public static void commit(Connection conn) throws java.sql.SQLException {
	/*
	 * Previously this method was invoked in a try-catch SQLException block.
	 * Prior to 2.5.1, RDBMS.commit() and RDBMS.setAutoCommit didn't actually 
	 * throw SQLException, and as of 2.5.1 its method declaration was corrected 
	 * to reflect this.
	 */
    RDBMServices.commit(conn);
    RDBMServices.setAutoCommit(conn, true);
}
/**
 *
 */
protected static void logNoTransactionWarning()
{
	String msg = "You are running the portal on a database that does not support transactions.  " +
				 "This is not a supported production environment for uPortal.  " +
				 "Sooner or later, your database will become corrupt.";
	log.warn(msg);
}
/**
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
public static void rollback(Connection conn) throws java.sql.SQLException
{
	try
	{
			RDBMServices.rollback(conn);
			RDBMServices.setAutoCommit(conn, true);
	}
	catch (SQLException sqle)
	{
		log.error("Error rolling back connection.", sqle);
		throw sqle;
	}
}
/**
 * @param conn java.sql.Connection
 * @param newValue boolean
 * @exception java.sql.SQLException
 */
public static void setAutoCommit(Connection conn, boolean newValue) throws java.sql.SQLException
{
	/*
	 * Previously this method was invoked in a try-catch SQLException block.
	 * Prior to 2.5.1, RDBMS.setAutocommit() didn't actually 
	 * throw SQLException, and as of 2.5.1 its method declaration was corrected 
	 * to reflect this.
	 */
    RDBMServices.setAutoCommit(conn, newValue);
}

}
