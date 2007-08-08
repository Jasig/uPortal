/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.legacy;

import java.util.Map;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

import junit.framework.TestCase;

/**
 * Testcase for the adaptor from PersonDirInfo JavaBeans to IPersonAttributeDao.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PersonDirInfoAdaptorTest extends TestCase {

    /**
     * Test adapting from a PersonDirInfo instance that represents a
     * directly-configured LDAP source.
     * 
     * This testcase will stop working on that fateful day when Susan Bramhall
     * is no longer listed in Yale University's LDAP.
     */
    public void testLdap() {
        PersonDirInfo pdi = new PersonDirInfo();
        
        pdi.setUrl("ldap://mrfrumble.its.yale.edu:389/o=yale.edu");
        pdi.setUidquery("(uid={0})");
        pdi.setUsercontext("");
        pdi.setAttributenames(new String[] {"mail"});
        pdi.setAttributealiases(new String[] {"emailfromldap"});

        
        IPersonAttributeDao dao = PersonDirInfoAdaptor.adapt(pdi);
        
        Map attributes = dao.getUserAttributes("susan");
        
        assertFalse(attributes.isEmpty());
        assertEquals("susan.bramhall@yale.edu", attributes.get("emailfromldap"));
        
    }
}

