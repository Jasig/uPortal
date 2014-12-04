/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlet.marketplace;

import com.google.common.collect.ImmutableSet;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.Validate;
import org.jasig.portal.concurrency.caching.RequestCache;
import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.rest.layout.MarketplaceEntry;
import org.jasig.portal.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Service layer implementation for Marketplace.
 * @since uPortal 4.1
 */
@Service
public class MarketplaceService implements IMarketplaceService, ApplicationListener<LoginEvent> {

    public static String FEATURED_CATEGORY_NAME="Featured";
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IPortletDefinitionRegistry portletDefinitionRegistry;

    private IPortletCategoryRegistry portletCategoryRegistry;
    
    private IAuthorizationService authorizationService;
    private boolean enableMarketplacePreloading = false;

    @Autowired
    @Qualifier(value = "org.jasig.portal.portlet.marketplace.MarketplaceService.marketplacePortletDefinitionCache")
    private Cache marketplacePortletDefinitionCache;
    
    @Autowired
    public void setAuthorizationService(IAuthorizationService service) {
        this.authorizationService = service;
    }

    /**
     * Cache of Username -> Future<Set<MarketplaceEntry>
     */
    @Autowired
    @Qualifier(value = "org.jasig.portal.portlet.marketplace.MarketplaceService.marketplaceUserPortletDefinitionCache")
    private Cache marketplaceUserPortletDefinitionCache;

    @Value("${org.jasig.portal.portlets.marketplacePortlet.loadMarketplaceOnLogin:false}")
    public void setLoadMarketplaceOnLogin(final boolean enableMarketplacePreloading) {
        this.enableMarketplacePreloading = enableMarketplacePreloading;
    }


    /**
     * Handle the portal LoginEvent.   If marketplace caching is enabled, will preload
     * marketplace entries for the currently logged in user.
     *
     * @param loginEvent the login event.
     */
    @Override
    public void onApplicationEvent(LoginEvent loginEvent) {
        if (enableMarketplacePreloading) {
            IPerson person = loginEvent.getPerson();
            loadMarketplaceEntriesFor(person);
        }
    }

    @Async
    private  Future<ImmutableSet<MarketplacePortletDefinition>>
      loadMarketplacePortletDefinitionsFor(final IPerson user) {

        final List<IPortletDefinition> allPortletDefinitions =
            this.portletDefinitionRegistry.getAllPortletDefinitions();

        final Set<MarketplacePortletDefinition> browseablePortletDefinitions = new HashSet<>();

        for (final IPortletDefinition portletDefinition : allPortletDefinitions) {

            if (mayBrowsePortlet(user, portletDefinition)) {
                final MarketplacePortletDefinition marketplacePortletDefinition = getOrCreateMarketplacePortletDefinition(portletDefinition);
                browseablePortletDefinitions.add(marketplacePortletDefinition);
            }

        }

        logger.trace("These portlet definitions {} are browseable by {}.",
            browseablePortletDefinitions, user.getUserName());

        Future<ImmutableSet<MarketplacePortletDefinition>> future = new AsyncResult<>(ImmutableSet
            .copyOf(browseablePortletDefinitions));

        // TODO: cache

        return future;
    }

    @Async
    public Future<ImmutableSet<MarketplaceEntry>> loadMarketplaceEntriesFor(final IPerson user) {
        final List<IPortletDefinition> allPortletDefinitions =
                this.portletDefinitionRegistry.getAllPortletDefinitions();

        final Set<MarketplaceEntry> visiblePortletDefinitions = new HashSet<>();

        for (final IPortletDefinition portletDefinition : allPortletDefinitions) {

            if (mayBrowsePortlet(user, portletDefinition)) {
                final MarketplacePortletDefinition marketplacePortletDefinition = getOrCreateMarketplacePortletDefinition(portletDefinition);
                MarketplaceEntry entry = new MarketplaceEntry(marketplacePortletDefinition);

                // flag whether this use can add the portlet...
                boolean canAdd = mayAddPortlet(user, portletDefinition);
                entry.setCanAdd(canAdd);

                visiblePortletDefinitions.add(entry);
            }
        }

        logger.trace("These portlet definitions {} are browseable by {}.", visiblePortletDefinitions, user);

        Future<ImmutableSet<MarketplaceEntry>> result = new AsyncResult<>(ImmutableSet.copyOf(visiblePortletDefinitions));
        Element cacheElement = new Element(user.getUserName(), result);
        marketplaceUserPortletDefinitionCache.put(cacheElement);

        return result;
    }

    @Override
    public ImmutableSet<MarketplaceEntry> browseableMarketplaceEntriesFor(final IPerson user) {
        Element cacheElement = marketplaceUserPortletDefinitionCache.get(user.getUserName());
        Future<ImmutableSet<MarketplaceEntry>> future = null;
        if (cacheElement == null) {
            // not in cache, load it and cache the results...
            future = loadMarketplaceEntriesFor(user);
        } else {
            future = (Future<ImmutableSet<MarketplaceEntry>>)cacheElement.getObjectValue();
        }

        try {
            return future.get();

        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return ImmutableSet.of();
        }
    }

    @Override
    public ImmutableSet<MarketplacePortletDefinition> marketplacePortletDefinitionsBrowseableBy(
        final IPerson user) {

        final Future<ImmutableSet<MarketplacePortletDefinition>> future =
            loadMarketplacePortletDefinitionsFor(user);

        try {
            return future.get();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read asynch load MarketplacePortletDefinitions "
                + "for this user.", e);
        }
    }

    @Override
    public ImmutableSet<MarketplacePortletDefinition>
        marketplacePortletDefinitionsRelatedTo(final MarketplacePortletDefinition definition) {

        final Set<MarketplacePortletDefinition> allRelatedPortlets = new HashSet<>();

        for (final PortletCategory parentCategory:
            portletCategoryRegistry.getParentCategories(definition)) {

            final Set<IPortletDefinition> portletsInCategory =
                portletCategoryRegistry.getAllChildPortlets(parentCategory);

            for (final IPortletDefinition portletDefinition : portletsInCategory) {
                allRelatedPortlets.add(
                    new MarketplacePortletDefinition(portletDefinition,
                        this, portletCategoryRegistry));
            }
        }

        allRelatedPortlets.remove(definition);
        return ImmutableSet.copyOf(allRelatedPortlets);
    }

    @Override
    public Set<PortletCategory> browseableNonEmptyPortletCategoriesFor(final IPerson user) {
        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        final Set<MarketplaceEntry> browseablePortlets = browseableMarketplaceEntriesFor(user);

        final Set<PortletCategory> browseableCategories = new HashSet<PortletCategory>();

        // by considering only the parents of portlets browseable by this user,
        // categories containing zero browseable portlets are excluded.
        for (final MarketplaceEntry entry : browseablePortlets) {
            IPortletDefinition portletDefinition = entry.getMarketplacePortletDefinition();
            for (final PortletCategory category : this.portletCategoryRegistry.getParentCategories(portletDefinition)) {

                final String categoryId = category.getId();

                if ( mayBrowse(principal, categoryId) ) {
                    browseableCategories.add(category);
                } else {
                    logger.trace("Portlet {} is browseable by {} but it is in category {} " +
                            "which is not browseable by that user.  " +
                            "This may be as intended, " +
                            "or it may be that that portlet category ought to be more widely browseable.",
                            portletDefinition, user, category);
                }
            }
        }

        logger.trace("These categories {} are browseable by {}.", browseableCategories, user);

        return browseableCategories;

    }

    @Override
    public boolean mayBrowsePortlet(final IPerson user, final IPortletDefinition portletDefinition) {
        Validate.notNull(user, "Cannot determine if null users can browse portlets.");
        Validate.notNull(portletDefinition, "Cannot determine whether a user can browse a null portlet definition.");

        final IAuthorizationPrincipal principal = AuthorizationPrincipalHelper.principalFromUser(user);

        final String portletPermissionEntityId = PermissionHelper.permissionTargetIdForPortletDefinition(portletDefinition);

        return mayBrowse(principal, portletPermissionEntityId);

    }

    @Override
    public Set<MarketplacePortletDefinition> featuredPortletsForUser(IPerson user) {
        Validate.notNull(user, "Cannot determine relevant featured portlets for null user.");

        final Set<MarketplaceEntry> browseablePortlets = browseableMarketplaceEntriesFor(user);
        final Set<MarketplacePortletDefinition> featuredPortlets = new HashSet<>();

        for (final MarketplaceEntry entry : browseablePortlets) {
            IPortletDefinition portletDefinition = entry.getMarketplacePortletDefinition();
            for (final PortletCategory category : this.portletCategoryRegistry.getParentCategories(portletDefinition)) {

                if ( FEATURED_CATEGORY_NAME.equalsIgnoreCase(category.getName())){
                    featuredPortlets.add(getOrCreateMarketplacePortletDefinition(portletDefinition));
                }

            }
        }

        return featuredPortlets;
    }

    @Override
    public MarketplacePortletDefinition getOrCreateMarketplacePortletDefinition(IPortletDefinition portletDefinition) {
        Element element = marketplacePortletDefinitionCache.get(portletDefinition.getFName());
        if (element == null) {
            MarketplacePortletDefinition mpd = new MarketplacePortletDefinition(portletDefinition, this, portletCategoryRegistry);
            element = new Element(portletDefinition.getFName(), mpd);
            this.marketplacePortletDefinitionCache.put(element);
        }
        return (MarketplacePortletDefinition) element.getObjectValue();
    }
    
    @Override
    public MarketplacePortletDefinition getOrCreateMarketplacePortletDefinitionIfTheFnameExists(String fname) {
        IPortletDefinition portletDefinition = portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        if(portletDefinition != null) {
            return getOrCreateMarketplacePortletDefinition(portletDefinition);
        }
        return null;
    }

    // Private stateless static utility methods below here

    /**
     * True if the principal has UP_PORTLET_SUBSCRIBE.BROWSE or UP_PORTLET_PUBLISH.MANAGE on the target id.
     * The target ID must be fully resolved.  This method will not e.g. prepend the portlet prefix to target ids
     * that seem like they might be portlet IDs.
     * Implementation note: technically this method is not stateless since asking an AuthorizationPrincipal about
     * its permissions has caching side effects in the permissions system, but it's stateless as far as this Service
     * is concerned.
     * @param principal non-null IAuthorizationPrincipal who might have permission
     * @param targetId non-null identifier of permission target
     * @return true if has BROWSE or MANAGE permissions, false otherwise.
     */
    private static boolean mayBrowse(final IAuthorizationPrincipal principal, final String targetId) {
        Validate.notNull(principal, "Cannot determine permissions for a null user.");
        Validate.notNull(targetId, "Cannot determine permissions on a null target.");

        return (principal.hasPermission(IPermission.PORTAL_SUBSCRIBE,
                IPermission.PORTLET_BROWSE_ACTIVITY, targetId)
                || principal.hasPermission(IPermission.PORTAL_PUBLISH,
                IPermission.PORTLET_MANAGER_ACTIVITY, targetId));

    }
    
    @Override
    @RequestCache
    public boolean mayAddPortlet(final IPerson user, final IPortletDefinition portletDefinition) {
        Validate.notNull(user, "Cannot determine if null users can browse portlets.");
        Validate.notNull(portletDefinition, "Cannot determine whether a user can browse a null portlet definition.");
        //short-cut for guest user, it will always be false for guest, otherwise evaluate
        return user.isGuest() ? false : authorizationService.canPrincipalSubscribe(AuthorizationPrincipalHelper.principalFromUser(user), portletDefinition.getPortletDefinitionId().getStringId());
    }

    // JavaBean property setters below here.
    // getters omitted because no use cases for reading the properties

    @Autowired
    public void setPortletDefinitionRegistry(final IPortletDefinitionRegistry portletDefinitionRegistry) {
        Validate.notNull(portletDefinitionRegistry, "Portlet definition registry must not be null.");
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPortletCategoryRegistry(final IPortletCategoryRegistry portletCategoryRegistry) {
        Validate.notNull(portletCategoryRegistry);
        this.portletCategoryRegistry = portletCategoryRegistry;
    }

}
