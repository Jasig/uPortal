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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.jndi;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.jasig.portal.PortalException;
import org.jasig.portal.services.ExternalServices;
import org.jasig.portal.services.LogService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * JNDIManager.
 *
 * uPortal's JNDI tree has the following basic structure:
 * <tt>
 * root context
 *    |
 *    +--services--*[service name]*...
 *    |
 *    +--users--*[userID]*
 *    |             |
 *    |             +--layouts--*[layoutId]*
 *    +             |               |
 * sessions         |               +--channel-ids
 *    |             |               |      |
 * *[sessionId]*    |               |      +--*[fname]*--[chanId]
 *                  |               |
 *                  |               +--sessions--*[sessionId]*
 *                  |
 *                  |
 *                  +--sessions--*[sessionId]*
 *                                    |
 *                                    +--channel-obj--*[chanId]*...
 *                                    |
 *                                    +--[layoutId]
 * </tt>
 * Notation:
 *  [something] referes to a value of something
 *  *[something]* refers to a set of values
 *  ... refers to a subcontext
 *
 *
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @version $Revision$
 */
public class JNDIManager {

  /**
   * Empty constructor.
   */
  public JNDIManager () {
  }

  /**
   * Initializes root context node
   */
  public static void initializePortalContext () throws PortalException {
    try {
      Context context = getContext();

      // Create a subcontext for portal-wide services, initialize services 
      // Start any portal services configured in services.xml
      try {
          ExternalServices.startServices(context.createSubcontext("services"));
      } catch (Exception ex) {
        LogService.log(LogService.ERROR, ex);
        throw new PortalException ("Failed to start external portal services.",ex);
      }

      /*
      // Note: this should be moved into a common service init
      // Bind in the logger service
      LogService logger = LogService.instance();
      context.bind("/services/logger", logger);
      */

      // Create a subcontext for user specific bindings
      context.createSubcontext("users");

      // Create a subcontext for session listings
      context.createSubcontext("sessions");

      LogService.log(LogService.DEBUG, "JNDIManager::initializePortalContext() : initialized portal JNDI context");

    } catch (Exception e) {
      // Log the exception
      LogService.log(LogService.ERROR, e);
    }
  }

  /**
   * Create and populate contexts for a new user sesions
   * @param sessionID
   */
  public static void initializeSessionContext (HttpSession session, String userId,String layoutId, Document userLayout) throws PortalException {

      Context topContext=null;

      // get initial context
      try {
          topContext=(Context)getContext();
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): Unable to obtain initial context - " + ne.getMessage());
          return;
      }

      // bind userId to /sessions context
      try {
          Context tsessionContext=(Context)topContext.lookup("/sessions");
          tsessionContext.bind(session.getId(),userId);
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): Unable to obtain /sessions context - " + ne.getMessage());
      }

      // bind listener
      session.setAttribute("JNDISessionListener", new JNDISessionListener());

      // Get the ID of the session
      String sessionId = session.getId();
      Context usersContext = null;
      try {
          // get /users context
          usersContext = (Context)topContext.lookup("/users");
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): Could not find /users context - " + ne.getMessage());
          throw  new PortalException("JNDIManager.initializeSessionContext(): Could not find /users context",ne);
      }

      // get or create /users/[userId] context
      Context userIdContext=null;
      Context sessionsContext=null;
      Context layoutsContext=null;
      try {
          userIdContext=(Context)usersContext.lookup(userId);

          // lookup layouts and sessions contexts
          try {
              layoutsContext=(Context)userIdContext.lookup("layouts");
          } catch (NamingException ne) {
              LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): /users/"+userId+"/layouts - did not exist, even though /users/"+userId+" context did!");
              layoutsContext=userIdContext.createSubcontext("layouts");
          }

          try {
              sessionsContext=(Context)userIdContext.lookup("sessions");
          } catch (NamingException ne) {
              LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): context /users/"+userId+"/sessions - did not exist, even though /users/"+userId+" context did!");
              sessionsContext=userIdContext.createSubcontext("sessions");
          }

      } catch (NamingException ne) {
          // new user
          try {
              userIdContext=usersContext.createSubcontext(userId);
              // create layouts and sessions context
              layoutsContext=userIdContext.createSubcontext("layouts");
              sessionsContext=userIdContext.createSubcontext("sessions");
              LogService.log(LogService.DEBUG, "JNDIManager.initializeSessionContext(): initialized context for a userId=\""+userId+"\".");
          } catch (NamingException ne2) {
              LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): exception encountered while trying to create  /users/"+userId+" and layouts/sessions contexts ! "+ne2.getMessage());
              throw new PortalException("JNDIManager.initializeSessionContext(): exception encountered while trying to create  /users/"+userId+" and layouts/sessions contexts !",ne2);
          }
      }

      // bind sessions/[sessionId] context
      Context sessionIdContext=null;
      try {
          sessionIdContext=sessionsContext.createSubcontext(sessionId);
      } catch (NameAlreadyBoundException nabe) {
          LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): trying to initialize session twice. sessionId=\""+sessionId+"\"");
          //          sessionIdContext=(Context)sessionsContext.lookup(sessionId);
          throw new PortalException("JNDIManager.initializeSessionContext(): trying to initialize session twice. sessionId=\""+sessionId+"\"",nabe);
      } catch (Exception e) {
          LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): error encountered while trying to create context /users/"+userId+"/sessions/"+sessionId+" "+e.getMessage());
          throw new PortalException("JNDIManager.initializeSessionContext(): error encountered while trying to create context /users/"+userId+"/sessions/"+sessionId,e);
      }

      // bind layoutId
      try {
          sessionIdContext.bind("layoutId",layoutId);
      } catch (Exception e) {
          LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): error encountered while trying to bind /users/"+userId+"/sessions/"+sessionId+"/layoutId "+e.getMessage());
          throw new PortalException("JNDIManager.initializeSessionContext(): error encountered while trying to bind /users/"+userId+"/sessions/"+sessionId+"/layoutId",e);
      }

      // make sure channel-obj context exists
      try {
          sessionIdContext.createSubcontext("channel-obj");
      } catch (Exception e) {};

      try {
          // check if the layout id binding already exists
          try {
              Context layoutIdContext=(Context)layoutsContext.lookup(layoutId);
              // assume layouts/[layoutId]/ has already been populated

              // bind layouts/[layoutId]/sessions/[sessionId]
              try {
                  Context lsessionsContext=(Context)userIdContext.lookup("layouts/"+layoutId+"/sessions");
                  lsessionsContext.createSubcontext(sessionId);

                  LogService.log(LogService.DEBUG, "JNDIManager.initializeSessionContext(): created /users/"+userId+"/layouts/"+layoutId+"/sessions/"+sessionId);

              } catch (Exception e) {
                  LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): exception occured while looking up context /users/"+userId+"/layouts/"+layoutId+"/sessions , although /users/"+userId+"/layouts context already existed ! "+e.getMessage());
                  throw new PortalException("JNDIManager.initializeSessionContext(): exception occured while looking up context /users/"+userId+"/layouts/"+layoutId+"/sessions , although /users/"+userId+"/layouts context already existed !",e);
              }
          } catch (NamingException nne) {
              // given layout id has not been registered yet
              Context layoutIdContext=layoutsContext.createSubcontext(layoutId);

              // bind layouts/[layoutId]/sessions/[sessionId] context
              Context lsessionsContext=layoutIdContext.createSubcontext("sessions");
              lsessionsContext.createSubcontext(sessionId);

              LogService.log(LogService.DEBUG, "JNDIManager.initializeSessionContext(): created context /users/"+userId+"/layouts/"+layoutId);

              try {
                  Context channel_idsContext = (Context)layoutIdContext.createSubcontext("channel-ids");
                  // Get the list of channels in the user's layout
                  NodeList channelNodes = userLayout.getElementsByTagName("channel");
                  Node fname = null;
                  Node instanceid = null;
                  // Parse through the channels and populate the JNDI
                  for (int i = 0; i < channelNodes.getLength(); i++) {
                      // Attempt to get the fname and instance ID from the channel
                      fname = channelNodes.item(i).getAttributes().getNamedItem("fname");
                      instanceid = channelNodes.item(i).getAttributes().getNamedItem("ID");
                      if (fname != null && instanceid != null) {
                          //System.out.println("fname found -> " + fname);
                          // Create a new composite name from the fname
                          CompositeName cname = new CompositeName(fname.getNodeValue());
                          // Get a list of the name components
                          Enumeration e = cname.getAll();
                          // Get the root of the context
                          Context nextContext = channel_idsContext;
                          // Add all of the subcontexts in the fname
                          String subContextName = new String();
                          while (e.hasMoreElements()) {
                              subContextName = (String)e.nextElement();
                              if (e.hasMoreElements()) {
                                  // Bind a new sub context if the current name component is not the leaf
                                  nextContext = nextContext.createSubcontext(subContextName);
                              } else {
                                  //System.out.println("Binding " + instanceid.getNodeValue() + " to " + nextContext.getNameInNamespace() + "/" + subContextName);
                                  LogService.log(LogService.DEBUG, "JNDIManager.initializeSessionContext(): bound "+instanceid.getNodeValue() + " to " + nextContext.getNameInNamespace() + "/" + subContextName);

                                  nextContext.rebind(subContextName, instanceid.getNodeValue());
                              }
                          }
                      }
                  }
              } catch (NamingException ne) {
                  LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): exception occured while creating cahnnel-ids context. "+ne.getMessage());
                  throw new PortalException("JNDIManager.initializeSessionContext(): exception occured while creating cahnnel-ids context.",ne);
              }
          }
      } catch (Exception e) {
          LogService.log(LogService.ERROR, "JNDIManager.initializeSessionContext(): exception occured while pupulating context /users/"+userId+"/layouts/"+layoutId+"  "+e.getMessage());
          throw new PortalException("JNDIManager.initializeSessionContext(): exception occured while pupulating context /users/"+userId+"/layouts/"+layoutId,e);
      }
  }

  /**
   * Get the uPortal JNDI context
   * @return uPortal JNDI context
   * @exception NamingException
   */
  private static Context getContext() throws NamingException {
    Hashtable environment = new Hashtable(5);
    // Set up the path
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jasig.portal.jndi.PortalInitialContextFactory");
    Context ctx = new InitialContext(environment);
    return  (ctx);
  }

  /**
   * This class will be bound to the user's session when they log in. When the user's session is expired this
   * object should be unbound and will clean up all user specific objects in JNDI. Note: It's possible that
   * not all servlet containers properly unbind objects from the session when it expires!
   */
  private static class JNDISessionListener
      implements HttpSessionBindingListener {

 
    public void valueBound(HttpSessionBindingEvent bindingEvent) {
      LogService.log(LogService.INFO, "JNDISessionListener bound for: " + bindingEvent.getSession().getId());
    }

    /**
     * This method is called when the JNDISessionListener is unbound from a user's session. This should
     * only happen when the users session is either destroyed or expires. Note: This method may need synchronization!
     * If a user logs in and out quickly there may be problems with things not happening in the correct order.
     * @param     HttpSessionBindingEvent bindingEvent
     */
    public void valueUnbound (HttpSessionBindingEvent bindingEvent) {
      LogService.log(LogService.INFO, "JNDISessionListener unbound for: " + bindingEvent.getSession().getId());
      Context context = null;
      try {
        // Get the portal JNDI context
        context = getContext();
      } catch (NamingException ne) {
        LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Could not get portal context " +
            ne.getMessage());
        return;
      }

      Context usersContext = null;
      try {
          // get users context
        usersContext = (Context)context.lookup("/users");
      } catch (NamingException ne) {
        LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Could not get /users context "
            + ne.getMessage());
        return;
      }
      if (usersContext == null) {
        return;
      }

      String sessionId=bindingEvent.getSession().getId();

      // obtain /sessions context
      Context tsessionsContext=null;
      try {
        tsessionsContext = (Context)context.lookup("/sessions");
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Could not get /sessions context "+ne.getMessage());
          return;
      }

      String userId=null;
      // obtain userId by looking at /sessions bindings
      try {
          userId=(String)tsessionsContext.lookup(sessionId);
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Session "+sessionId+" does is not registered under /sessions context ! "+ne.getMessage());
          return;
      }
      if(userId==null) {
          // could do a /users/[userId]/sessions/* traversal here instead
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Unable to determine userId for a session "+sessionId+" ... giving up on JNDI cleanup.");
          return;
      }

      // unbind userId binding in /sessions
      try {
          tsessionsContext.unbind(sessionId);
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Problems unbinding /sessions/"+sessionId+" "+ne.getMessage());
      }

      Context userIdContext=null;
      try {
          userIdContext=(Context) usersContext.lookup(userId);
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): context /users/"+userId+" doesn't exist!");
          return;
      }

      Context sessionsContext=null;
      try {
          sessionsContext=(Context) userIdContext.lookup("sessions");
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): context /users/"+userId+"/sessions doesn't exist!");
          return;
      }

      Context sessionIdContext=null;
      try {
          sessionIdContext=(Context) sessionsContext.lookup(sessionId);
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): context /users/"+userId+"/sessions/"+sessionId+" doesn't exist!");
          return;
      }

      // determine layoutId
      String layoutId=null;
      try {
          layoutId=(String) sessionIdContext.lookup("layoutId");
      } catch (NamingException ne) {
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): binding /users/"+userId+"/sessions/"+sessionId+"/layoutId doesn't exist!");
      }

      // destroy sessionIdContext
      try {
          sessionsContext.unbind(sessionId);
          LogService.log(LogService.DEBUG, "JNDISessionListener.valueUnbound(): destroyed context /users/"+userId+"/sessions/"+sessionId);
      } catch (Exception e) {
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): exception occurred while trying to destroy context  /users/"+userId+"/sessions/"+sessionId+" "+e.getMessage());
      }

      // see if this was the only session
      try {
          NamingEnumeration list=userIdContext.list("sessions");
          if(!list.hasMore()) {
              // destroy userIdContext alltogether
              usersContext.unbind(userId);
              LogService.log(LogService.DEBUG, "JNDISessionListener.valueUnbound(): destroyed context /users/"+userId+" since the last remaining session has been unbound.");
          } else {
              // remove sessionId from the layouts/[layoutId]/sessions
              try {
                  Context layoutsContext=(Context) userIdContext.lookup("layouts");
                  try {
                      Context layoutIdContext=(Context) layoutsContext.lookup(layoutId);
                      try {
                          Context lsessionsContext=(Context) layoutIdContext.lookup("sessions");
                          // unbind sessionId
                          lsessionsContext.unbind(sessionId);
                          LogService.log(LogService.DEBUG, "JNDISessionListener.valueUnbound(): destroyed context /users/"+userId+"/layouts/"+layoutId+"/sessions/"+sessionId);

                          // see if the lsessionsContext is empty
                          NamingEnumeration slist=layoutIdContext.list("sessions");
                          if(!slist.hasMore()) {
                              // destroy the layoutId context
                              try {
                                  layoutsContext.unbind(layoutId);
                                  LogService.log(LogService.DEBUG, "JNDISessionListener.valueUnbound(): destroyed context /users/"+userId+"/layouts/"+layoutId+" since the last session using it has been unbound.");

                              } catch (Exception e) {
                                  LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): error destroying /users/"+userId+"/layouts/"+layoutId+" "+e.getMessage());
                              }
                          }
                      } catch (Exception e) {
                      LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): error looking up  /users/"+userId+"/layouts/"+layoutId+"/sesions "+e.getMessage());
                      }
                  } catch (Exception e) {
                      LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): error looking up  /users/"+userId+"/layouts/"+layoutId+" "+e.getMessage());
                  }
              } catch (Exception e) {
                  LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): error looking up  /users/"+userId+"/layouts "+e.getMessage());
              }
          }
      } catch (Exception e) {
          LogService.log(LogService.ERROR, "JNDISessionListener.valueUnbound(): error listing  /users/"+userId+"/sessions/ "+e.getMessage());
      }
    }
  }
}



