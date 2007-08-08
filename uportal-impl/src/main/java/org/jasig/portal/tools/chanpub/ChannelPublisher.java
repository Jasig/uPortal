/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.chanpub;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jasig.portal.ChannelCategory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelType;
import org.jasig.portal.Constants;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This is a Channel Publisher tool to install uPortal channels from outside of
 * the portal or from within a channel archive.
 * Currently configured to be executed via Jakarta Ant or via a
 * channel-definition block within a CAR deployment descriptor.
 *
 * Sample of command line arguments:
 * ant publish -Dchannel=all  (this will publish all channels that have a corresponding xml file)
 * ant publish -Dchannel=webmail.xml  (this will publish the specified channels)
 *
 * @author Freddy Lopez, flopez@unicon.net
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd, mboyd@sct.com
 * @version $Revision$
 */
public class ChannelPublisher implements ErrorHandler
{
    private static final Log log = LogFactory.getLog(ChannelPublisher.class);

    private static final String FRAMEWORK_OWNER = IPermission.PORTAL_FRAMEWORK;
    private static final String SUBSCRIBER_ACTIVITY =
        IPermission.CHANNEL_SUBSCRIBER_ACTIVITY;
    private static final String GRANT_PERMISSION_TYPE =
        IPermission.PERMISSION_TYPE_GRANT;

    private static final int LOAD_ALL_FILES = 0;
    private static final int LOAD_ONE_FILE = 1;

    private IPerson systemUser;
    private DocumentBuilder domParser;
    private IChannelRegistryStore crs;
    private Map chanTypesNamesToIds;
    private boolean mOnCommandLine = false;
    private boolean mOverrideExisting = false;

    private static final String chanDefsLocation = "/properties/chanpub";

    /**
     * @param args
     * @throws Exception We let Exceptions bubble up so that ant will know
     * that the publishing failed and can report the error message and stack
     * trace to the user.
     */
    public static void main(String[] args)throws Exception{
        try{
            RDBMServices.setGetDatasourceFromJndi(false); /*don't try jndi when not in web app */
            /*

             Channel Publisher Tool Workflow.
             1) read all or specified channel.xml file

             ant publish -Dchannel=all or -Dchannel=webmail.xml

             2) validate each against the channelDefinition.dtd file

             3) publish one channel at a time

             */

            // determine whether user wants to publish one or all of the channels in current directory
            int mode = LOAD_ALL_FILES;

            if (args[1] != null && args[1].length() > 0) {
                // MODE = 0 for all channels in directory
                // MODE = 1 for individual channel
                if (args[1].equals("all"))
                    mode = LOAD_ALL_FILES;
                else
                    mode = LOAD_ONE_FILE;

            }

            ChannelPublisher publisher = getCommandLineInstance();

            // determine what mode we are in
            if (mode == LOAD_ONE_FILE) {
                System.out.println(
                "You have chosen to publish one channel.....");
                System.out.print("Publishing channel " + args[1] + ".....");
                // lets publish one channel only
                publisher.publishChannel(args[1]);
                System.out.println("Done");
            } else {
                // lets publish all channels in directory
                System.out.println ("You have chosen to publish all channels.....");

                // user has selected to publish all channel in the /channels directory
                // lets publish all channel one by one that is
                // create InputStream object to pass to next method
                File f =
                    ResourceLoader.getResourceAsFile(
                            ChannelPublisher.class,
                            chanDefsLocation + "/");
                if (f.isDirectory()) {

                    // Consider only files that end in .xml
                    class ChannelDefFileFilter implements FileFilter {
                        public boolean accept(File file) {
                            return file.getName().endsWith(".xml");
                        }
                    }
                    File[] files = f.listFiles(new ChannelDefFileFilter());

                    for (int j = 0; j < files.length; j++) {
                        String name = files[j].getName();
                        // lets publish one at a time
                        try{
                            publisher.publishChannel(name);
                        }catch(Exception e){
                            // Add file name into exception so we will know which
                            // file has the problem.
                            throw new Exception("Unable to publish file: "+name,e);
                        }
                        System.out.println("Published channel " + name);
                    }
                }
            }
            System.out.println("Publishing finished.");
            System.exit(0);
        }catch(Exception e){
            // signal failure to ant and log
            log.error(e, e);
            throw e;
        }
    }

    /**
     * Sets up the system user for use during publishing.
     *
     */
    private void setupSystemUser()
    {
        systemUser = PersonFactory.createSystemPerson();
    }

    /**
     * Publishes the channel represented by the XML located in the file
     * represented by the passed in filename and returns the resultant
     * ChannelDefinition object.
     *
     * @param filename the name of a file containing the channel XML definition
     * @return org.jasig.portal.ChannelDefinition the published channel definition
     * @throws Exception
     */
    public ChannelDefinition publishChannel(String filename) throws Exception
    {
        ChannelInfo ci = getChannelInfo(filename);
        return publishChannel(ci);
    }

    /**
     * Publishes the channel represented by the XML accessed via the passed in
     * InputStream object and returns the resultant ChannelDefinition object.
     *
     * @param is and InputStream containing the channel XML definition
     * @return org.jasig.portal.ChannelDefinition the published channel definition
     * @throws Exception
     */
    public ChannelDefinition publishChannel(InputStream is) throws Exception
    {
        ChannelInfo ci = getChannelInfo(is);
        return publishChannel(ci);
    }

    /**
     * Publishes the channel represented by the passed in ChannelDefinition
     * object and returns the resultant ChannelDefinition object.
     *
     * @param ci
     * @return
     * @throws Exception
     */
    private ChannelDefinition publishChannel(ChannelInfo ci) throws Exception
    {

        if (ci == null)
            return null;

        try {
            if (ci.chanDef.getTypeId() != -1)
            {
                ChannelType type = crs.getChannelType(ci.chanDef.getTypeId());
                ci.chanDef.setJavaClass(type.getJavaClass());
            }
            crs.saveChannelDefinition(ci.chanDef);

            // Permission for everyone to subscribe to channel
            AuthorizationService authService = AuthorizationService.instance();
            String target = "CHAN_ID." + ci.chanDef.getId();
            IUpdatingPermissionManager upm = authService.newUpdatingPermissionManager(FRAMEWORK_OWNER);

            // Remove old permissions
            IPermission[] oldPermissions = upm.getPermissions(SUBSCRIBER_ACTIVITY, target);
            upm.removePermissions(oldPermissions);

          // Add new permissions for this channel based on both groups and users
          if (ci.groups != null) {
            IPermission[] newGroupPermissions = new IPermission[ci.groups.length];
            for (int j = 0; j < ci.groups.length; j++) {
                IAuthorizationPrincipal authPrincipal = authService.newPrincipal(ci.groups[j]);
                newGroupPermissions[j] = upm.newPermission(authPrincipal);
                newGroupPermissions[j].setType(GRANT_PERMISSION_TYPE);
                newGroupPermissions[j].setActivity(SUBSCRIBER_ACTIVITY);
                newGroupPermissions[j].setTarget(target);
            }
            upm.addPermissions(newGroupPermissions);
	  }
          if (ci.users != null) {
              IPermission[] newUserPermissions = new IPermission[ci.users.length];
              for (int j=0; j < ci.users.length; j++) {
                    IAuthorizationPrincipal authPrincipal = authService.newPrincipal(ci.users[j]);
                    newUserPermissions[j] = upm.newPermission(authPrincipal);
                    newUserPermissions[j].setType(GRANT_PERMISSION_TYPE);
                    newUserPermissions[j].setActivity(SUBSCRIBER_ACTIVITY);
                    newUserPermissions[j].setTarget(target);
              }
              upm.addPermissions(newUserPermissions);
          }

            // Categories
            // First, remove channel from its categories
            ChannelCategory[] categories = crs.getParentCategories(ci.chanDef);
            for (int i = 0; i < categories.length; i++) {
                crs.removeChannelFromCategory(ci.chanDef, categories[i]);
            }
            // Now add channel to assigned categories
            if (ci.categories != null)
            {
                for (int k = 0; k < ci.categories.length; k++) {
                    crs.addChannelToCategory(ci.chanDef, ci.categories[k]);
                }
            }

            // Need to approve channel
            crs.approveChannelDefinition(ci.chanDef, systemUser, new Date());

        } catch (Exception e) {
            log.error("publishChannel() :: Exception while attempting to publish channel to database. Channel name = " + ci.chanDef.getName());
            throw e;
        }
        return ci.chanDef;
    }

    /**
     * Set up a DOM parser for handling the XML channel-definition data.
     *
     * @throws Exception
     */
    private void setupDomParser() throws Exception {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            domParser = dbf.newDocumentBuilder();
            domParser.setEntityResolver(new ChannelDefDtdResolver());
            domParser.setErrorHandler(this);
        } catch (Exception e) {
            log.error( "setupDomParser() :: creating Dom Parser. ", e);
            throw e;
        }
    }

    /**
     * Populates and returns a ChannelInfo object based on the passed in
     * file name containing XML data structured according to the
     * channel-definition dtd.
     *
     * @param chanDefFile
     * @return
     * @throws Exception
     */
    private ChannelInfo getChannelInfo(String chanDefFile) throws Exception {
        InputStream is =
            ResourceLoader.getResourceAsStream(
                ChannelPublisher.class,
                chanDefsLocation + "/" + chanDefFile);
        return getChannelInfo(is);
    }

    /**
     * Populates and returns a ChannelInfo object based on the input stream
     * containing XML data structured according to the channel-definition dtd.
     *
     * @param is
     * @return
     * @throws Exception
     */
    private ChannelInfo getChannelInfo(InputStream is) throws Exception {
        ChannelInfo ci = new ChannelInfo();
        Document doc = null;

        // Build a DOM tree out of Channel_To_Publish.xml
        doc = domParser.parse(is);

        Element chanDefE = doc.getDocumentElement();
        final String id = getId(chanDefE);
        if (id != null) {
            ci.chanDef = crs.newChannelDefinition(Integer.parseInt(id));
        } else {
            String fname = getFname(chanDefE);

            // Use existing channel definition if it exists,
            // otherwise make a new one with a new ID
            ci.chanDef = crs.getChannelDefinition(fname);

            if (ci.chanDef != null && !mOverrideExisting)
            {
                log.error(
                        "chanDef with fname "
                        + fname
                        + " already exists "
                        + "and override is false. Terminating publication.");
                return null;
            }

            if (ci.chanDef == null) {
                ci.chanDef = crs.newChannelDefinition();
            }
        }

        for (Node param = chanDefE.getFirstChild(); param != null; param = param.getNextSibling()) {
            if (!(param instanceof Element))
                continue; // whitespace (typically \n) between tags
            Element pele = (Element) param;
            String tagname = pele.getTagName();
            String value = XML.getElementText(pele).trim();

            // each tagname corresponds to an object data field
            if (tagname.equals("title"))
                ci.chanDef.setTitle(value);
            else if (tagname.equals("name"))
                ci.chanDef.setName(value);
            else if (tagname.equals("makeFNameAccessibleOnly"))
                ci.fNameAccessibleOnly = true;
            else if (tagname.equals("fname"))
                ci.chanDef.setFName(value);
            else if (tagname.equals("desc"))
                ci.chanDef.setDescription(value);
            else if (tagname.equals("type"))
                getType(ci, value);
            else if (tagname.equals("class"))
                ci.chanDef.setJavaClass(value);
            else if (tagname.equals("timeout"))
                ci.chanDef.setTimeout(Integer.parseInt(value));
            else if (tagname.equals("hasedit"))
                ci.chanDef.setEditable((value != null && value.equals("Y")) ? true : false);
            else if (tagname.equals("hashelp"))
                ci.chanDef.setHasHelp((value != null && value.equals("Y")) ? true : false);
            else if (tagname.equals("hasabout"))
                ci.chanDef.setHasAbout((value != null && value.equals("Y")) ? true : false);
                else if (tagname.equals("secure"))
                  ci.chanDef.setIsSecure((value != null && value.equals("Y")) ? true : false);
            else if (mOnCommandLine && tagname.equals("categories"))
                getCategories(ci, pele);
            else if (mOnCommandLine && tagname.equals("groups"))
                getGroups(ci, pele);
            else if (mOnCommandLine && tagname.equals("users"))
                getUsers(ci, pele);
            else if (tagname.equals("parameters"))
                getParameters(ci, pele);

            ci.chanDef.setPublisherId(0); // system user
            ci.chanDef.setPublishDate(new Date());
        }
        if (ci.groups == null && ! mOnCommandLine)
            ci.groups = getAdminGroup();
        if (ci.categories == null && ! mOnCommandLine)
            ci.categories = getDefaultCategory(ci.fNameAccessibleOnly);
        return ci;
    }

    /**
     * Determines the functional name of the channel.
     *
     * @param chanDefE
     * @return
     * @throws Exception
     */
    private String getFname(Element chanDefE) throws Exception
    {
        String fname = null;
        for (Node n = chanDefE.getFirstChild();
            n != null;
            n = n.getNextSibling())
        {
            if (n.getNodeType() == Node.ELEMENT_NODE)
            {
                if (n.getNodeName().equals("fname"))
                {
                    fname = XML.getElementText((Element) n).trim();
                }
            }
        }

        // Complain if we don't find an fname
        if (fname == null)
            throw new Exception("Missing required fname element");
        return fname;
    }

    /**
     * Find the channel id.
     *
     * @param chanDefE
     * @return possible channel id
     * @throws Exception
     */
    private String getId(Element chanDefE) throws Exception
    {
        String id = null;
        for (Node n = chanDefE.getFirstChild();
            n != null;
            n = n.getNextSibling())
        {
            if (n.getNodeType() == Node.ELEMENT_NODE)
            {
                if (n.getNodeName().equals("channelId"))
                {
                    id = XML.getElementText((Element) n).trim();
                }
            }
        }
        return id;
    }

    /**
     * Translate the declared channel type name into a channel type id.
     *
     * @param ci
     * @param value
     * @throws Exception
     */
    private void getType(ChannelInfo ci, String value)
        throws Exception
    {
        Integer typeId = (Integer) chanTypesNamesToIds.get(value);
        if (typeId != null)
        {
            ci.chanDef.setTypeId(typeId.intValue());
        }
        else
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = null;
            for (Iterator itr = chanTypesNamesToIds.keySet().iterator();
            itr.hasNext();)
            {
                if (pw == null)
                {
                    pw = new PrintWriter(sw);
                    pw.print("['");
                    pw.print((String) itr.next());
                    pw.print("'");
                }
                else
                {
                    pw.print(" | '");
                    pw.print((String) itr.next());
                    pw.print("'");
                }
            }
            pw.print("]");
            pw.flush();
            throw new Exception(
                "Invalid entry '"
                    + value
                    + "' for Channel Type. Must be one of "
                    + sw.toString());
        }
    }

    /**
     * Translate channel category names into category ids.
     *
     * @param ci The ChannelInfo object being populated.
     * @param pele The Element containing the category elements.
     * @throws Exception
     */
    private void getCategories(ChannelInfo ci, Element pele) throws Exception
    {
        NodeList anodes = pele.getElementsByTagName("category");
        if (anodes.getLength() != 0)
        {
            ci.categories = new ChannelCategory[anodes.getLength()];
            for (int j = 0; j < anodes.getLength(); j++)
            {
                Element anode = (Element) anodes.item(j);
                String catString = XML.getElementText(anode).trim();
                // need to look up corresponding category id
                // ie: Applications = local.50
                //     Entertainment = local.51
                IEntityGroup cat = getGroup(catString, ChannelDefinition.class);

                if (cat != null)
                    ci.categories[j] = crs.getChannelCategory(cat.getKey());
                else
                    throw new Exception(
                        "Invalid entry '" + catString + "' for category.");
            }
        }
    }


    /**
     * Load the declared parameters.
     *
     * @param ci The ChannelInfo object being populated.
     * @param pele The Element containing the parameter elements.
     */
    private void getParameters(ChannelInfo ci, Element pele)
    {
        NodeList anodes = pele.getElementsByTagName("parameter");
        if (anodes.getLength() > 0)
        {
            for (int j = 0; j < anodes.getLength(); j++)
            {
                String pname = null;
                String pvalue = null;
                String povrd = null;
                String pdescr = null;
                Element anode = (Element) anodes.item(j);
                NodeList namenodes = anode.getElementsByTagName("name");
                if (namenodes.getLength() > 0)
                {
                    pname = XML.getElementText((Element) namenodes.item(0)).trim();
                }
                NodeList valuenodes = anode.getElementsByTagName("value");
                if (valuenodes.getLength() > 0)
                {
                    pvalue = XML.getElementText((Element) valuenodes.item(0)).trim();
                }
                NodeList descnodes = anode.getElementsByTagName("description");
                if (descnodes.getLength() > 0)
                {
                    pdescr = XML.getElementText((Element) descnodes.item(0)).trim();
                }
                NodeList ovrdnodes = anode.getElementsByTagName("ovrd");
                if (ovrdnodes.getLength() > 0)
                {
                    povrd = XML.getElementText((Element) ovrdnodes.item(0)).trim();
                }
                ChannelParameter chanParam =
                    new ChannelParameter(pname, pvalue, RDBMServices.dbFlag(povrd));
                chanParam.setDescription(pdescr);
                ci.chanDef.addParameter(chanParam);
            }
        }
    }

    /**
     * Translate access group names into group ids.
     *
     * @param ci The ChannelInfo object being populated.
     * @param pele The Element containing the group elements.
     * @throws Exception
     */
    private void getGroups(ChannelInfo ci, Element pele) throws Exception
    {
        NodeList anodes = pele.getElementsByTagName("group");
        if (anodes.getLength() != 0)
        {
            ci.groups = new IEntityGroup[anodes.getLength()];
            for (int j = 0; j < anodes.getLength(); j++)
            {
                Element anode = (Element) anodes.item(j);
                String groupStr = XML.getElementText(anode).trim();
                // need to look up corresponding group id
                // ie: Everyone = local.0
                //     Developers = local.4
                IEntityGroup group = getGroup(groupStr, IPerson.class);

                if (group != null)
                    ci.groups[j] = group;
                else
                    throw new Exception(
                        "Invalid entry '" + groupStr + "' for group.");
            }
        }
    }

    /**
     * Translate access user ids into the user entity objects.
     *
     * @param ci The ChannelInfo object being populated.
     * @param pele The Element containing the user elements.
     * @throws Exception
     */
    private void getUsers(ChannelInfo ci, Element pele) throws Exception
    {
        NodeList anodes = pele.getElementsByTagName("user");
        if (anodes.getLength() != 0)
        {
            ci.users = new IEntity[anodes.getLength()];
            for (int j = 0; j < anodes.getLength(); j++)
            {
                Element anode = (Element) anodes.item(j);
                String userStr = XML.getElementText(anode).trim();
                // need to look up corresponding user
                IEntity user = GroupService.getEntity(userStr, IPerson.class);

                if (user != null)
                    ci.users[j] = user;
                else
                    throw new Exception(
                        "Invalid entry '" + userStr + "' for user.");
            }
        }
    }

    /**
     * Gets the portal administrators group.
     */
    private IEntityGroup[] getAdminGroup() throws Exception
    {
        String dg = GroupService.PORTAL_ADMINISTRATORS;
        IEntityGroup e = GroupService.getDistinguishedGroup(dg);
        return new IEntityGroup[] { e };
    }

    /**
     * Obtains an auto-publish category located in the root category or creates
     * if not found.
     */
    private ChannelCategory[] getDefaultCategory(boolean fNameAccessibleOnly)
        throws Exception
    {
        ChannelCategory rootCat = crs.getTopLevelChannelCategory();

        if (fNameAccessibleOnly)
            return new ChannelCategory[] { rootCat };

        ChannelCategory[] topCats = crs.getChildCategories(rootCat);
        ChannelCategory autoCat = null;

        for (int i = 0; i < topCats.length; i++)
        {
            if (topCats[i].getName().equals(Constants.AUTO_PUBLISH_CATEGORY))
            {
                autoCat = topCats[i];
                break;
            }
        }
        if (autoCat == null) // target category not created. create.
        {
            autoCat =
                crs.newChannelCategory(
                    Constants.AUTO_PUBLISH_CATEGORY,
                    "Holds Auto-published " + "Channels.",
                    "system");
            crs.addCategoryToCategory(autoCat, rootCat);
        }
        return new ChannelCategory[] { autoCat };
    }

    /**
     * Attempts to determine group key based on a group name or fully qualifed
     * group key.
     * @param groupName a <code>String</code> value
     * @param entityType the kind of entity the group contains
     * @return a group key
     */
    private static IEntityGroup getGroup(String groupName, Class entityType) throws Exception {
        IEntityGroup group = null;
        EntityIdentifier[] groups = GroupService.searchForGroups(groupName, IGroupConstants.IS, entityType);
        if (groups != null && groups.length > 0) {
            group = GroupService.findGroup(groups[0].getKey());
        } else {
            // An actual group key might be specified, so try looking up group directly
            group = GroupService.findGroup(groupName);
        }
        return group;
    }

    /**
     * Load the lookup table to translate channel type names into ids.
     *
     * @throws Exception
     */
    private void initChanTypeMap()
    throws Exception
    {
        if (chanTypesNamesToIds == null) {
            chanTypesNamesToIds = new HashMap();
            chanTypesNamesToIds.put("Custom", new Integer(-1));

            ChannelType[] types = crs.getChannelTypes();
            for (int i = 0; i < types.length; i++)
                chanTypesNamesToIds.put( types[i].getName(),
                new Integer(types[i].getId()));
        }
    }

    private static class ChannelInfo
    {
        ChannelDefinition chanDef;
        IEntityGroup[] groups;
        ChannelCategory[] categories;
        boolean fNameAccessibleOnly = false;
	    IEntity[] users;
    }

    private ChannelPublisher(boolean onCommandLine) throws Exception
    {
        mOnCommandLine = onCommandLine;
        // initialize channel registry store
        crs = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
        // read in channel types
        initChanTypeMap();

        // create IPerson object for the portal's system user
        setupSystemUser();
        // setup DOM Parser with dtd validation
        setupDomParser();
    }

    public void setOverride(boolean b)
    {
        mOverrideExisting = b;
    }

    public static ChannelPublisher getCommandLineInstance() throws Exception
    {
        ChannelPublisher publisher = new ChannelPublisher(true);
        publisher.setOverride(true);
        return publisher;
    }

    public static ChannelPublisher getChannelArchiveInstance() throws Exception
    {
        return new ChannelPublisher(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException arg0) throws SAXException
    {
        if (log.isInfoEnabled())
            log.info("Warning occurred while parsing channel definition.",
                            arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException arg0) throws SAXException
    {
        throw new SAXException(
                "Error occurred while parsing channel definition.", arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException arg0) throws SAXException
    {
        throw new SAXException(
                "Fatal Error occurred while parsing channel definition.", arg0);
    }
}
