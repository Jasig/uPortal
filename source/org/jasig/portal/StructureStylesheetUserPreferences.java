/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
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

 package org.jasig.portal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.jasig.portal.services.LogService;

/**
 * User preferences for stylesheets performing structure transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */


// structure stylesheet preferences will remain to be more complex then
// preferences of the second stylesheet, hence the derivation
public class StructureStylesheetUserPreferences extends ThemeStylesheetUserPreferences {
    protected Hashtable folderAttributeNumbers;
    protected Hashtable folderAttributeValues;
    protected ArrayList defaultFolderAttributeValues;

    public StructureStylesheetUserPreferences() {
        super();
        folderAttributeNumbers=new Hashtable();
        folderAttributeValues=new Hashtable();
        defaultFolderAttributeValues=new ArrayList();
    }

    public StructureStylesheetUserPreferences( StructureStylesheetUserPreferences fsup) {
        super(fsup);
        this.folderAttributeNumbers=new Hashtable(fsup.folderAttributeNumbers);
        this.folderAttributeValues=new Hashtable(fsup.folderAttributeValues);
        this.defaultFolderAttributeValues=new ArrayList(fsup.defaultFolderAttributeValues);
    }

    public String getFolderAttributeValue(String folderID,String attributeName) {
        Integer attributeNumber=(Integer)folderAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            LogService.log(LogService.ERROR,"StructureStylesheetUserPreferences::getFolderAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
            return null;
        }
        String value=null;
        List l=(List) folderAttributeValues.get(folderID);
        if(l==null) {
	    //            LogService.log(LogService.ERROR,"StructureStylesheetUserPreferences::getFolderAttributeValue() : Attempting to obtain an attribute for a non-existing folder \""+folderID+"\".");
	    //            return null;
	    return (String) defaultFolderAttributeValues.get(attributeNumber.intValue());
        } else {
            if(attributeNumber.intValue()<l.size()) {
                value=(String) l.get(attributeNumber.intValue());
            }
            if(value==null) {
                try {
                    value=(String) defaultFolderAttributeValues.get(attributeNumber.intValue());
                } catch (IndexOutOfBoundsException e) {
                    LogService.log(LogService.ERROR,"StructureStylesheetUserPreferences::getFolderAttributeValue() : internal error - attribute name is registered, but no default value is provided.");
                    return null;
                }
            }
        }
        return value;
    }

    /**
     * Returns folder attribute value only if it has been assigned specifically.
     * @folderID folder id
     * @attributeName name of the attribute
     * @return attribute value or null if the value is determined by the attribute default
     */
    String getDefinedFolderAttributeValue(String folderID,String attributeName) {
        Integer attributeNumber=(Integer)folderAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            LogService.log(LogService.ERROR,"ThemeStylesheetUserPreferences::hasDefinedFolderAttributeValue() : Attempting to obtain a non-existing attribute \""+attributeName+"\".");
            return null;
        }
        List l=(List) folderAttributeValues.get(folderID);
        if(l==null) {
	    return null;
	} else {
	    if(attributeNumber.intValue()<l.size())
		return (String) l.get(attributeNumber.intValue());
	    else
		return null;
	}
    }

    // this should be modified to throw exceptions
    public void setFolderAttributeValue(String folderID,String attributeName,String attributeValue) {
        Integer attributeNumber=(Integer)folderAttributeNumbers.get(attributeName);
        if(attributeNumber==null) {
            LogService.log(LogService.ERROR,"StructureStylesheetUserPreferences::setFolderAttribute() : Attempting to set a non-existing folder attribute \""+attributeName+"\".");
            return;
        }
        List l=(List) folderAttributeValues.get(folderID);
        if(l==null)
            l=this.createFolder(folderID);
        try {
            l.set(attributeNumber.intValue(),attributeValue);
        } catch (IndexOutOfBoundsException e) {
            // bring up the array to the right size
            for(int i=l.size();i<attributeNumber.intValue();i++) {
                l.add((String)null);
            }
            l.add(attributeValue);
        }
    }

    public void addFolderAttribute(String attributeName, String defaultValue) {
        if(folderAttributeNumbers.get(attributeName)!=null) {
            LogService.log(LogService.ERROR,"StructureStylesheetUserPreferences::addFolderAttribute() : Attempting to re-add an existing folder attribute \""+attributeName+"\".");
        } else {
            folderAttributeNumbers.put(attributeName,new Integer(defaultFolderAttributeValues.size()));
            // append to the end of the default value array
            defaultFolderAttributeValues.add(defaultValue);
        }
    }

    public void setFolderAttributeDefaultValue(String attributeName, String defaultValue) {
        Integer attributeNumber=(Integer)folderAttributeNumbers.get(attributeName);
        defaultFolderAttributeValues.set(attributeNumber.intValue(),defaultValue);
    }

    public void removeFolderAttribute(String attributeName) {
        Integer attributeNumber;
        if((attributeNumber=(Integer)folderAttributeNumbers.get(attributeName))==null) {
            LogService.log(LogService.ERROR,"StructureStylesheetUserPreferences::removeFolderAttribute() : Attempting to remove a non-existing folder attribute \""+attributeName+"\".");
        } else {
            folderAttributeNumbers.remove(attributeName);
            // do not touch the arraylists
        }
    }

    public Enumeration getFolderAttributeNames() {
        return folderAttributeNumbers.keys();
    }

    public void addFolder(String folderID) {
        // check if the folder is there. In general it might be ok to use this functon to default
        // all of the folder's parameters

        ArrayList l=new ArrayList(defaultFolderAttributeValues.size());

        if(folderAttributeValues.put(folderID,l)!=null)
            LogService.log(LogService.DEBUG,"StructureStylesheetUserPreferences::addFolder() : Readding an existing folder (folderID=\""+folderID+"\"). All values will be set to default.");
    }

    public void removeFolder(String folderID) {
        if(folderAttributeValues.remove(folderID)==null)
            LogService.log(LogService.ERROR,"StructureStylesheetUserPreferences::removeFolder() : Attempting to remove an non-existing folder (folderID=\""+folderID+"\").");
    }


    public Enumeration getFolders() {
        return folderAttributeValues.keys();
    }

    public boolean hasFolder(String folderID) {
        return folderAttributeValues.containsKey(folderID);
    }

    private ArrayList createFolder(String folderID) {
        ArrayList l=new ArrayList(defaultFolderAttributeValues.size());
        folderAttributeValues.put(folderID,l);
        return l;
    }

    private Hashtable copyFolderAttributeNames() {
        return folderAttributeNumbers;
    }

    public String getCacheKey() {
        StringBuffer sbKey = new StringBuffer();
        for(Enumeration e=folderAttributeValues.keys();e.hasMoreElements();) {
            String folderId=(String)e.nextElement();
            sbKey.append("(folder:").append(folderId).append(':');
            List l=(List)folderAttributeValues.get(folderId);
            for(int i=0;i<l.size();i++) {
                String value=(String)l.get(i);
                if(value==null) value=(String)defaultFolderAttributeValues.get(i);
                sbKey.append(value).append(",");
            }
            sbKey.append(")");
        }
        return super.getCacheKey().concat(sbKey.toString());
    }

}
