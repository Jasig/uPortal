/**
 * WSRP_v1_PortletManagement_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.intf;

public interface WSRP_v1_PortletManagement_PortType extends java.rmi.Remote {
    public void getPortletDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, java.lang.String[] desiredLocales, org.jasig.portal.wsrp.types.holders.PortletDescriptionHolder portletDescription, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault;
    public void clonePortlet(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, javax.xml.rpc.holders.StringHolder portletHandle, javax.xml.rpc.holders.ByteArrayHolder portletState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault;
    public void destroyPortlets(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] portletHandles, org.jasig.portal.wsrp.types.holders.DestroyFailedArrayHolder destroyFailed, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault;
    public void setPortletProperties(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, org.jasig.portal.wsrp.types.PropertyList propertyList, javax.xml.rpc.holders.StringHolder portletHandle, javax.xml.rpc.holders.ByteArrayHolder portletState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault;
    public void getPortletProperties(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, java.lang.String[] names, org.jasig.portal.wsrp.types.holders.PropertyArrayHolder properties, org.jasig.portal.wsrp.types.holders.ResetPropertyArrayHolder resetProperties, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault;
    public void getPortletPropertyDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, java.lang.String[] desiredLocales, org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder modelDescription, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault;
}
