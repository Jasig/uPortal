package org.jasig.portal.security.provider;

/**
 * Copyright �  2001, 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.groups.EntityTypes;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.RDBMPreparedStatement;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SqlTransaction;

/**
 * Reference implementation of IPermissionStore.  Performs CRUD operations
 * on the UP_Permission table.
 * @author Dan Ellentuck (de3@columbia.edu)
 * @version $Revision$
 */
public class RDBMPermissionImpl implements IPermissionStore {

    private static RDBMPermissionImpl singleton;

    // sql Strings:
    private static String PERMISSION_TABLE = "UP_PERMISSION";
    private static String OWNER_COLUMN =     "OWNER";
    private static String PRINCIPAL_COLUMN = "PRINCIPAL";
    private static String ACTIVITY_COLUMN =  "ACTIVITY";
    private static String TARGET_COLUMN =    "TARGET";
    private static String TYPE_COLUMN =      "PERMISSION_TYPE";
    private static String EFFECTIVE_COLUMN = "EFFECTIVE";
    private static String EXPIRES_COLUMN =   "EXPIRES";
    private static String deletePermissionSql;
    private static String findPermissionSql;
    private static String insertPermissionSql;
    private static String selectPermissionSql;
    private static String updatePermissionSql;
/**
 * RDBMReferencePermission constructor comment.
 */
public RDBMPermissionImpl() {
    super();
}
/**
 * Add the IPermissions to the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void add(IPermission[] perms) throws AuthorizationException
{
    if ( perms.length > 0 )
    {
        try
        {
            primAdd(perms);
        }
        catch (SQLException sqle)
        {
            LogService.log (LogService.ERROR, sqle);
            throw new AuthorizationException(sqle.getMessage());
        }
    }
}
/**
 * Add the IPermission to the store.
 * @param perm org.jasig.portal.security.IPermission
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void add(IPermission perm) throws AuthorizationException

{
    Connection conn = null;
    int rc = 0;

    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getInsertPermissionSql();
        RDBMPreparedStatement ps = new RDBMPreparedStatement(conn, sQuery);
        try
        {
            primAdd(perm, ps);
            LogService.instance().log(LogService.DEBUG, "RDBMPermissionImpl.add(): " + ps);
            rc = ps.executeUpdate();
            if ( rc != 1 )
                { throw new AuthorizationException("Problem adding Permission " + perm); }
        }
        finally
            { ps.close(); }
    }
    catch (SQLException sqle)
    {
        LogService.log(LogService.ERROR, sqle.getMessage());
        throw new AuthorizationException("Problem adding Permission " + perm);
    }
    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * Delete the IPermissions from the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void delete(IPermission[] perms) throws AuthorizationException
{
    if ( perms.length > 0 )
    {
        try
        {
            primDelete(perms);
        }
        catch (SQLException sqle)
        {
            LogService.log (LogService.ERROR, sqle);
            throw new AuthorizationException(sqle.getMessage());
        }
    }
}
/**
 * Delete a single IPermission from the store.
 * @param perm org.jasig.portal.security.IPermission
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void delete(IPermission perm) throws AuthorizationException

{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getDeletePermissionSql();
        RDBMPreparedStatement ps = new RDBMPreparedStatement(conn, sQuery);
        try
            { primDelete(perm, ps); }
        finally
            { ps.close(); }
    }
    catch (SQLException sqle)
    {
        LogService.log(LogService.ERROR, sqle.getMessage());
        throw new AuthorizationException("Problem deleting Permission " + perm);
    }
    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * Answer if this entity exists in the database.
 * @return boolean
 * @param perm org.jasig.portal.security.IPermission
 * @exception java.sql.SQLException
 */
public boolean existsInDatabase(IPermission perm) throws SQLException
{
    Connection conn = RDBMServices.getConnection();
    try
    {
        String sQuery = getFindPermissionSql();
        RDBMPreparedStatement ps = new RDBMPreparedStatement(conn, sQuery);
        try
        {
            ps.setString(1, perm.getOwner());
            ps.setString(2, perm.getPrincipal());
            ps.setString(3, perm.getActivity());
            ps.setString(4, perm.getTarget());
            LogService.instance().log(LogService.DEBUG, "RDBMPermissionImpl.existsInDatabase(): " + ps);
            ResultSet rs = ps.executeQuery();
            try {
              return ( rs.next() );
            } finally {
               rs.close();
            }
        } finally {
          ps.close();
        }
    }
    catch (SQLException sqle)
    {
        LogService.log (LogService.ERROR, sqle);
        throw sqle;
    }
    finally
    {
        RDBMServices.releaseConnection(conn);
    }
}
/**
 * @return java.lang.String
 */
private static String getDeletePermissionSql()
{
    if ( deletePermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(200);
        sqlBuff.append("DELETE FROM ");
        sqlBuff.append(PERMISSION_TABLE);
        sqlBuff.append(" WHERE ");
        sqlBuff.append(OWNER_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(PRINCIPAL_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(ACTIVITY_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(TARGET_COLUMN);
        sqlBuff.append(" = ? ");
        deletePermissionSql = sqlBuff.toString();
    }
    return deletePermissionSql;
}
/**
 * Insert the method's description here.
 * Creation date: (11/6/01 5:19:57 PM)
 * @return java.lang.String
 */
private static java.lang.String getFindPermissionSql()
{
    if ( findPermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(getSelectPermissionSql());
        sqlBuff.append("WHERE ");
        sqlBuff.append(OWNER_COLUMN);
        sqlBuff.append(" = ? AND ");
	    sqlBuff.append(PRINCIPAL_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(ACTIVITY_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(TARGET_COLUMN);
        sqlBuff.append(" = ? ");
        sqlBuff.append(TYPE_COLUMN);
        sqlBuff.append(" = ? ");
        findPermissionSql = sqlBuff.toString();
    }
    return findPermissionSql;
}
/**
 * @return java.lang.String
 */
private static String getInsertPermissionSql()
{
    if ( insertPermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(200);
        sqlBuff.append("INSERT INTO ");
        sqlBuff.append(PERMISSION_TABLE);
        sqlBuff.append(" (");
        sqlBuff.append(OWNER_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(PRINCIPAL_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(ACTIVITY_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(TARGET_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(TYPE_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(EFFECTIVE_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(EXPIRES_COLUMN);
        sqlBuff.append(") VALUES (?, ?, ?, ?, ?, ?, ?)");
        insertPermissionSql = sqlBuff.toString();
    }
    return insertPermissionSql;
}
/**
 * @return java.lang.String
 */
private static String getSelectPermissionSql()
{
    if ( selectPermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(200);
        sqlBuff.append("SELECT ");
        sqlBuff.append(OWNER_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(PRINCIPAL_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(ACTIVITY_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(TARGET_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(TYPE_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(EFFECTIVE_COLUMN);
        sqlBuff.append(", ");
        sqlBuff.append(EXPIRES_COLUMN);
        sqlBuff.append(" FROM ");
        sqlBuff.append(PERMISSION_TABLE);
        sqlBuff.append(" ");
        selectPermissionSql = sqlBuff.toString();
    }
    return selectPermissionSql;
}
/**
 * @return java.lang.String
 */
private static String getUpdatePermissionSql()
{
    if ( updatePermissionSql == null )
    {
        StringBuffer sqlBuff = new StringBuffer(300);
        sqlBuff.append("UPDATE ");
        sqlBuff.append(PERMISSION_TABLE);
        sqlBuff.append(" SET ");
        sqlBuff.append(TYPE_COLUMN);
        sqlBuff.append(" = ?, ");
        sqlBuff.append(EFFECTIVE_COLUMN);
        sqlBuff.append(" = ?, ");
        sqlBuff.append(EXPIRES_COLUMN);
        sqlBuff.append(" = ? WHERE ");
        sqlBuff.append(OWNER_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(PRINCIPAL_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(ACTIVITY_COLUMN);
        sqlBuff.append(" = ? AND ");
        sqlBuff.append(TARGET_COLUMN);
        sqlBuff.append(" = ? ");
        updatePermissionSql = sqlBuff.toString();
    }
    return updatePermissionSql;
}
/**
 * @return org.jasig.portal.security.IPermission
 * @param rs java.sql.ResultSet
 */
private IPermission instanceFromResultSet(ResultSet rs) throws  SQLException
{
    IPermission perm = newInstance(rs.getString(OWNER_COLUMN));
    perm.setPrincipal(rs.getString(PRINCIPAL_COLUMN));
    perm.setActivity(rs.getString(ACTIVITY_COLUMN));
    perm.setTarget(rs.getString(TARGET_COLUMN));
    perm.setType(rs.getString(TYPE_COLUMN));
    perm.setEffective(rs.getDate(EFFECTIVE_COLUMN));
    perm.setExpires(rs.getDate(EXPIRES_COLUMN));

    return perm;
}
/**
 * Factory method for IPermissions
 */
public IPermission newInstance(String owner)
{
    return new PermissionImpl(owner);
}
/**
 * Add the IPermissions to the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception java.sql.Exception
 */
private void primAdd(IPermission[] perms) throws SQLException, AuthorizationException
{
    Connection conn = null;
    int rc = 0;

    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getInsertPermissionSql();
        RDBMPreparedStatement ps = new RDBMPreparedStatement(conn, sQuery);
        try
        {
            RDBMServices.setAutoCommit(conn, false);

            for ( int i=0; i<perms.length; i++ )
            {
                primAdd(perms[i], ps);
                LogService.instance().log(LogService.DEBUG, "RDBMPermissionImpl.primAdd(): " + ps);
                rc = ps.executeUpdate();

                if ( rc != 1 )
                {
    	            String errMsg = "Problem adding " + perms[i] + " RC: " + rc;
                    LogService.log (LogService.ERROR, errMsg);
                    RDBMServices.rollback(conn);
                    throw new AuthorizationException(errMsg);
                }
            }
        }
        finally
            { ps.close(); }

        RDBMServices.commit(conn);

    }
    catch (SQLException sqle)
    {
        LogService.log (LogService.ERROR, sqle);
        RDBMServices.rollback(conn);
        throw sqle;
    }
    finally
    {
        RDBMServices.setAutoCommit(conn, true);
        RDBMServices.releaseConnection(conn);
    }
}
/**
 * Set the params on the PreparedStatement and execute the insert.
 * @param perm org.jasig.portal.security.IPermission
 * @param ps java.sql.PreparedStatement - the PreparedStatement for inserting a Permission row.
 * @return int - the return code from the PreparedStatement
 * @exception java.sql.Exception
 */
private void primAdd(IPermission perm, RDBMPreparedStatement ps) throws SQLException
{
    java.sql.Date date = null;

    // NON-NULL COLUMNS:
    ps.clearParameters();
    ps.setString(1, perm.getOwner());
    ps.setString(2, perm.getPrincipal());
    ps.setString(3, perm.getActivity());
    ps.setString(4, perm.getTarget());
    // TYPE:
    if ( perm.getType() == null )
    	{ ps.setNull(5, Types.VARCHAR); }
    else
        { ps.setString(5, perm.getType()); }
    // EFFECTIVE:
    if ( perm.getEffective() == null )
   	    { ps.setNull(6, Types.DATE); }
	else
        {
            date = new java.sql.Date(perm.getEffective().getTime());
            ps.setDate(6, date);
        }
    // EXPIRES:
    if ( perm.getExpires() == null )
        { ps.setNull(7, Types.DATE); }
    else
        {
            date = new java.sql.Date(perm.getExpires().getTime());
            ps.setDate(7, date);
        }
}
/**
 * Delete the IPermissions from the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception java.sql.Exception
 */
private void primDelete(IPermission[] perms) throws SQLException, AuthorizationException
{
    Connection conn = null;

    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getDeletePermissionSql();
        RDBMPreparedStatement ps = new RDBMPreparedStatement(conn, sQuery);
        try
        {
            RDBMServices.setAutoCommit(conn, false);

            for ( int i=0; i<perms.length; i++ )
                { primDelete(perms[i], ps); }
        }
        finally
            { ps.close(); }

        RDBMServices.commit(conn);

    }
    catch (SQLException sqle)
    {
        LogService.log (LogService.ERROR, sqle);
        RDBMServices.rollback(conn);
        throw sqle;
    }
    finally
    {
        RDBMServices.setAutoCommit(conn, true);
        RDBMServices.releaseConnection(conn);
    }
}
/**
 * Set the params on the PreparedStatement and execute the delete.
 * @param perm org.jasig.portal.security.IPermission
 * @param ps java.sql.PreparedStatement - the PreparedStatement for deleting a Permission row.
 * @return int - the return code from the PreparedStatement
 * @exception java.sql.Exception
 */
private int primDelete(IPermission perm, RDBMPreparedStatement ps) throws SQLException
{
    ps.clearParameters();
    ps.setString(1, perm.getOwner());
    ps.setString(2, perm.getPrincipal());
    ps.setString(3, perm.getActivity());
    ps.setString(4, perm.getTarget());
    LogService.instance().log(LogService.DEBUG, "RDBMPermissionImpl.primDelete(): " + ps);

    return ps.executeUpdate();
}
/**
 * Update the IPermissions in the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception java.sql.Exception
 */
private void primUpdate(IPermission[] perms) throws SQLException, AuthorizationException
{
    Connection conn = null;

    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getUpdatePermissionSql();
        RDBMPreparedStatement ps = new RDBMPreparedStatement(conn, sQuery);
        try
        {
            RDBMServices.setAutoCommit(conn, false);

            for ( int i=0; i<perms.length; i++ )
                { primUpdate(perms[i], ps); }
        }
        finally
            { ps.close(); }

        RDBMServices.commit(conn);

    }
    catch (SQLException sqle)
    {
        LogService.log (LogService.ERROR, sqle);
        RDBMServices.rollback(conn);
        throw sqle;
    }
    finally
    {
        RDBMServices.setAutoCommit(conn, true);
        RDBMServices.releaseConnection(conn);
    }
}
/**
 * Set the params on the PreparedStatement and execute the update.
 * @param perm org.jasig.portal.security.IPermission
 * @param ps java.sql.PreparedStatement - the PreparedStatement for updating a Permission row.
 * @return int - the return code from the PreparedStatement
 * @exception java.sql.Exception
 */
private int primUpdate(IPermission perm, RDBMPreparedStatement ps) throws SQLException
{
    java.sql.Date date = null;

    // UPDATE COLUMNS:

    ps.clearParameters();
    // TYPE:
    if ( perm.getType() == null )
        { ps.setNull(1, Types.VARCHAR); }
    else
        { ps.setString(1, perm.getType()); }
    // EFFECTIVE:
    if ( perm.getEffective() == null )
        { ps.setNull(2, Types.DATE); }
    else
    {
        date = new java.sql.Date(perm.getEffective().getTime());
        ps.setDate(2, date);
    }
    // EXPIRES:
    if ( perm.getExpires() == null )
        { ps.setNull(3, Types.DATE); }
    else
    {
        date = new java.sql.Date(perm.getExpires().getTime());
        ps.setDate(3, date);
    }
    // WHERE COLUMNS:
    ps.setString(4, perm.getOwner());
    ps.setString(5, perm.getPrincipal());
    ps.setString(6, perm.getActivity());
    ps.setString(7, perm.getTarget());
    LogService.instance().log(LogService.DEBUG, "RDBMPermissionImpl.primUpdate(): " + ps);


    return ps.executeUpdate();
}
/**
 * Select the Permissions from the store.
 * @param owner String - the Permission owner
 * @param principal String - the Permission principal
 * @param activity String - the Permission activity
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public IPermission[] select
    (String owner,
    String principal,
    String activity,
    String target,
    String type)
throws AuthorizationException
{
    Connection conn = null;
    Statement stmnt = null;
    ResultSet rs = null;
    List perms = new ArrayList();

    StringBuffer sqlQuery = new StringBuffer(getSelectPermissionSql());
    sqlQuery.append(" WHERE ");

    if ( owner != null )
    {
        sqlQuery.append(OWNER_COLUMN);
        sqlQuery.append(" = '");
        sqlQuery.append(owner);
        sqlQuery.append("' ");
    }
    else
    {
        sqlQuery.append("1 = 1 ");
    }

    if ( principal != null )
        {
            sqlQuery.append("AND ");
            sqlQuery.append(PRINCIPAL_COLUMN);
            sqlQuery.append(" = '");
            sqlQuery.append(principal);
            sqlQuery.append("' ");
        }

    if ( activity != null )
        {
            sqlQuery.append("AND ");
            sqlQuery.append(ACTIVITY_COLUMN);
            sqlQuery.append(" = '");
            sqlQuery.append(activity);
            sqlQuery.append("' ");
        }

    if ( target != null )
        {
            sqlQuery.append("AND ");
            sqlQuery.append(TARGET_COLUMN);
            sqlQuery.append(" = '");
            sqlQuery.append(target);
            sqlQuery.append("' ");
        }

    if ( type != null )
        {
            sqlQuery.append("AND ");
            sqlQuery.append(TYPE_COLUMN);
            sqlQuery.append(" = '");
            sqlQuery.append(type);
            sqlQuery.append("' ");
        }

    LogService.instance().log(LogService.DEBUG, "RDBMPermissionImpl.select(): " + sqlQuery.toString());

    try
    {
        conn = RDBMServices.getConnection();
        stmnt = conn.createStatement();
        try
        {
            rs = stmnt.executeQuery(sqlQuery.toString());
            try
            {
                while ( rs.next() )
                    { perms.add(instanceFromResultSet(rs)); }
            }
            finally
                { rs.close(); }
        }
        finally
            { stmnt.close(); }
    }
    catch (SQLException sqle)
    {
        LogService.log (LogService.ERROR, sqle);
        throw new AuthorizationException("Problem retrieving Permissions " + sqle.getMessage());
    }
    finally
        { RDBMServices.releaseConnection(conn); }

    return ((IPermission[])perms.toArray(new IPermission[perms.size()]));
}
/**
 * @return org.jasig.portal.security.provider.RDBMPermissionImpl
 */
public static synchronized RDBMPermissionImpl singleton()
{
    if ( singleton == null )
        { singleton = new RDBMPermissionImpl(); }
    return singleton;
}
/**
 * Update the IPermissions in the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void update(IPermission[] perms) throws AuthorizationException
{
    if ( perms.length > 0 )
    {
        try
        {
            primUpdate(perms);
        }
        catch (SQLException sqle)
        {
            LogService.log (LogService.ERROR, sqle);
            throw new AuthorizationException(sqle.getMessage());
        }
    }
}
/**
 * Update a single IPermission in the store.
 * @param perm org.jasig.portal.security.IPermission
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
public void update(IPermission perm) throws AuthorizationException

{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        String sQuery = getUpdatePermissionSql();
        LogService.instance().log(LogService.DEBUG, "RDBMPermissionImpl.update(): " + sQuery);
        RDBMPreparedStatement ps = new RDBMPreparedStatement(conn, sQuery);
        try
            { primUpdate(perm, ps); }
        finally
            { ps.close(); }
    }
    catch (SQLException sqle)
    {
        LogService.log(LogService.ERROR, sqle.getMessage());
        throw new AuthorizationException("Problem updating Permission " + perm);
    }
    finally
        { RDBMServices.releaseConnection(conn); }
}
}
