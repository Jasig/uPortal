package org.jasig.portal.portlets.favorites;


import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("VIEW")
public class FavoritesController {
	
	//TODO : Make this a preference
	private static final String FAVORITE_TAB_NAME = "_favorite";
	
	private IUserInstanceManager userInstanceManager;
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
	
	@Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
	
	@RenderMapping
	public String initializeView(Model model) {
        IUserInstance ui = userInstanceManager.getUserInstance(portalRequestUtils.getCurrentPortalRequest());
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserLayoutManager ulm = upm.getUserLayoutManager();
        
        IUserLayout userLayout = ulm.getUserLayout();
        List<IUserLayoutNodeDescription> favorites = getFavoritePortlets(FAVORITE_TAB_NAME, userLayout);
        model.addAttribute("favorites",favorites);
        model.addAttribute("tabs",userLayout.getFragmentNames());
		return "jsp/Favorites/view";
	}
	
	
	@SuppressWarnings("unchecked")
	public List<IUserLayoutNodeDescription> getFavoritePortlets(String favoriteTabName, IUserLayout userLayout) {
		List<IUserLayoutNodeDescription> favorites = new LinkedList<IUserLayoutNodeDescription>();
		
		Enumeration<String> tabs = userLayout.getChildIds(userLayout.getRootId());
		while(tabs.hasMoreElements()) { //loop over tabs
			String node = tabs.nextElement();
			IUserLayoutNodeDescription tabDescription = userLayout.getNodeDescription(node);
			if(favoriteTabName.equalsIgnoreCase(tabDescription.getName())) {
				Enumeration<String> columns = userLayout.getChildIds(node);
				//loop through columns to get a list of portlets
				while(columns.hasMoreElements()) {
					String column = (String) columns.nextElement();
					Enumeration<String> portlets = userLayout.getChildIds(column);
					while(portlets.hasMoreElements()) {
						String portlet = (String) portlets.nextElement();
						IUserLayoutNodeDescription portletDescription = userLayout.getNodeDescription(portlet);
						favorites.add(portletDescription);
					}
				}
			}
			
		}
		return favorites;
	}

}
