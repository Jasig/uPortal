package org.jasig.portal.rest.layout;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.jasig.portal.portlet.marketplace.PortletReleaseNotes;
import org.jasig.portal.portlet.marketplace.ScreenShot;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.PortletCategory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MarketplaceEntry  implements Serializable {
    
    private Set<String> getPortletCategories(MarketplacePortletDefinition pdef) {
        Set<PortletCategory> categories = pdef.getCategories();
        Set<String> rslt = new HashSet<String>();
        for (PortletCategory category : categories) {
            String lowerCase = category.getName().toLowerCase();
            if(!"all categories".equals(lowerCase)) {
                rslt.add(StringUtils.capitalize(category.getName().toLowerCase()));
            }
        }
        return rslt;
    }

    private static final long serialVersionUID = 1L;

    private final MarketplacePortletDefinition pdef;
    private String maxURL;
    private Set<MarketplaceEntry> relatedEntries;
    private boolean generateRelatedPortlets = true;
    private boolean canAdd;
    
    public MarketplaceEntry(MarketplacePortletDefinition pdef) {
        this.pdef = pdef;
        this.maxURL = pdef.getRenderUrl();
    }

    public MarketplaceEntry(MarketplacePortletDefinition pdef, String maxURL) {
        this.pdef = pdef;
        this.maxURL = maxURL;
    }
    
    public MarketplaceEntry(MarketplacePortletDefinition pdef, boolean generateRelatedPortlets) {
        this.pdef = pdef;
        this.maxURL = pdef.getRenderUrl();
        this.generateRelatedPortlets = generateRelatedPortlets;
    }

    public String getId() {
        return pdef.getPortletDefinitionId().getStringId();
    }

    public String getTitle() {
        return pdef.getTitle();
    }

    public String getName() {
        return pdef.getName();
    }

    public String getFname() {
        return pdef.getFName();
    }

    public String getDescription() {
        return pdef.getDescription();
    }

    public String getType() {
        return pdef.getType().getName();
    }

    public String getLifecycleState() {
        return pdef.getLifecycleState().toString();
    }

    public Set<String> getCategories() {
        return getPortletCategories(pdef);
    }

    @JsonIgnore
    public MarketplacePortletDefinition getMarketplacePortletDefinition() {
        return pdef;
    }
    
    public String getFaIcon() {
        IPortletDefinitionParameter parameter = pdef.getParameter("faIcon");
        return parameter != null ? parameter.getValue() : null;
    }
    
    public String getMaxUrl() {
        return maxURL;
    }

    public void setMaxUrl(String urlString) {
        this.maxURL = urlString;
    }
    
    public String getShortUrl() {
        return pdef.getShortURL();
    }
    
    public List<ScreenShot> getMarketplaceScreenshots() {
        return pdef.getScreenShots();
    }
    
    public PortletReleaseNotes getPortletReleaseNotes() {
        return pdef.getPortletReleaseNotes();
    }
    
    public Set<MarketplaceEntry> getRelatedPortlets() {
        
        if(!generateRelatedPortlets) {
            //disabled so we don't have infinite related portlets.
            return null;
        }
        if(relatedEntries==null) {
            relatedEntries = new HashSet<MarketplaceEntry>(MarketplacePortletDefinition.QUANTITY_RELATED_PORTLETS_TO_SHOW);
            Set<MarketplacePortletDefinition> randomSamplingRelatedPortlets = pdef.getRandomSamplingRelatedPortlets();
            for(MarketplacePortletDefinition def : randomSamplingRelatedPortlets) {
                relatedEntries.add(new MarketplaceEntry(def, false));
            }
        }
        return relatedEntries;
    }
    
    public Double getRating() {
        return pdef.getRating() == null ? 0 : pdef.getRating();
    }

    public String getRenderUrl() {
        return pdef.getRenderUrl();
    }

    public Long getUserRated() {
        return pdef.getUsersRated();
    }

    public boolean isCanAdd() {
        return canAdd;
    }

    public void setCanAdd(boolean canAdd) {
        this.canAdd = canAdd;
    }
    
    public String getTarget() {
      return pdef.getTarget();
    }

    @Override
    public boolean equals(Object other) {

        if (other == null) { return false; }
        if (other == this) { return true; }
        if (other.getClass() != getClass()) {
            return false;
        }
        MarketplaceEntry rhs = (MarketplaceEntry) other;

        return new EqualsBuilder()
            .append(getMarketplacePortletDefinition(), rhs.getMarketplacePortletDefinition())
            .append(isCanAdd(), rhs.isCanAdd())
            .append(generateRelatedPortlets, rhs.generateRelatedPortlets)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getMarketplacePortletDefinition())
            .append(isCanAdd())
            .append(generateRelatedPortlets)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("fname", getFname())
            .toString();
    }

}
