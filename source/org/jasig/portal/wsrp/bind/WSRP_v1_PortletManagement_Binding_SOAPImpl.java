/**
 * WSRP_v1_PortletManagement_Binding_SOAPImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.jasig.portal.wsrp.bind;

public class WSRP_v1_PortletManagement_Binding_SOAPImpl implements org.jasig.portal.wsrp.intf.WSRP_v1_PortletManagement_PortType{
    public void getPortletDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, java.lang.String[] desiredLocales, org.jasig.portal.wsrp.types.holders.PortletDescriptionHolder portletDescription, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault {
        portletDescription.value = new org.jasig.portal.wsrp.types.PortletDescription();
        resourceList.value = new org.jasig.portal.wsrp.types.ResourceList();
        extensions.value = new org.jasig.portal.wsrp.types.Extension[0];
    }

    public void clonePortlet(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, javax.xml.rpc.holders.StringHolder portletHandle, javax.xml.rpc.holders.ByteArrayHolder portletState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault {
        portletHandle.value = new java.lang.String();
        portletState.value = new byte[0];
        extensions.value = new org.jasig.portal.wsrp.types.Extension[0];
    }

    public void destroyPortlets(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, java.lang.String[] portletHandles, org.jasig.portal.wsrp.types.holders.DestroyFailedArrayHolder destroyFailed, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault {
        destroyFailed.value = new org.jasig.portal.wsrp.types.DestroyFailed[0];
        extensions.value = new org.jasig.portal.wsrp.types.Extension[0];
    }

    public void setPortletProperties(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, org.jasig.portal.wsrp.types.PropertyList propertyList, javax.xml.rpc.holders.StringHolder portletHandle, javax.xml.rpc.holders.ByteArrayHolder portletState, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault {
        portletHandle.value = new java.lang.String();
        portletState.value = new byte[0];
        extensions.value = new org.jasig.portal.wsrp.types.Extension[0];
    }

    public void getPortletProperties(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, java.lang.String[] names, org.jasig.portal.wsrp.types.holders.PropertyArrayHolder properties, org.jasig.portal.wsrp.types.holders.ResetPropertyArrayHolder resetProperties, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault {
        properties.value = new org.jasig.portal.wsrp.types.Property[0];
        resetProperties.value = new org.jasig.portal.wsrp.types.ResetProperty[0];
        extensions.value = new org.jasig.portal.wsrp.types.Extension[0];
    }

    public void getPortletPropertyDescription(org.jasig.portal.wsrp.types.RegistrationContext registrationContext, org.jasig.portal.wsrp.types.PortletContext portletContext, org.jasig.portal.wsrp.types.UserContext userContext, java.lang.String[] desiredLocales, org.jasig.portal.wsrp.types.holders.ModelDescriptionHolder modelDescription, org.jasig.portal.wsrp.types.holders.ResourceListHolder resourceList, org.jasig.portal.wsrp.types.holders.ExtensionArrayHolder extensions) throws java.rmi.RemoteException, org.jasig.portal.wsrp.types.InvalidUserCategoryFault, org.jasig.portal.wsrp.types.InconsistentParametersFault, org.jasig.portal.wsrp.types.InvalidRegistrationFault, org.jasig.portal.wsrp.types.OperationFailedFault, org.jasig.portal.wsrp.types.MissingParametersFault, org.jasig.portal.wsrp.types.AccessDeniedFault, org.jasig.portal.wsrp.types.InvalidHandleFault {
        modelDescription.value = new org.jasig.portal.wsrp.types.ModelDescription();
        resourceList.value = new org.jasig.portal.wsrp.types.ResourceList();
        extensions.value = new org.jasig.portal.wsrp.types.Extension[0];
    }

}
