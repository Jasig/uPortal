package org.apereo.portal.url;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/** Customizer to replace a text by a serverName. */
public class UrlMultiServerNameCustomizer implements IAuthUrlCustomizer {

    private static final String LOGIN_URL_QUERYSTRING_PARAMETER = "service";
    private static final String LOGOUT_REDIRECT_QUERYSTRING_PARAMETER = "url";

    /** Logger. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Set<String> allServerNames = new HashSet<>();

    @Required
    public void setAllServerNames(final Set<String> serverNames) {
        this.allServerNames = Sets.newHashSet(serverNames);
        Assert.notEmpty(this.allServerNames, "The attribute serverNames should not be empty");
    }

    public boolean supports(final HttpServletRequest request, final String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        final MultiValueMap<String, String> parameters =
                UriComponentsBuilder.fromUriString(url).build().getQueryParams();
        return parameters.containsKey(LOGIN_URL_QUERYSTRING_PARAMETER)
                || parameters.containsKey(LOGOUT_REDIRECT_QUERYSTRING_PARAMETER);
    }

    public String customizeUrl(final HttpServletRequest request, final String url) {
        if (supports(request, url)) {

            final String matchingServerName = findMatchingServerName(request);
            if (StringUtils.isNotBlank(matchingServerName)) {

                final String matchingHostnamePart =
                        matchingServerName.contains(":")
                                ? matchingServerName.split(":")[0]
                                : matchingServerName;
                final String matchingPortPart =
                        matchingServerName.contains(":") ? matchingServerName.split(":")[1] : null;

                final UriComponentsBuilder uriComponentsBuilder =
                        UriComponentsBuilder.fromUriString(url);
                final MultiValueMap<String, String> parameters =
                        uriComponentsBuilder.build().getQueryParams();

                try {

                    // Replace LOGIN_URL_QUERYSTRING_PARAMETER
                    if (parameters.containsKey(LOGIN_URL_QUERYSTRING_PARAMETER)) {
                        final List<String> originalList =
                                parameters.get(LOGIN_URL_QUERYSTRING_PARAMETER);
                        final List<String> copyList = new ArrayList<>();
                        for (String s : originalList) {
                            final UriComponents originalUri =
                                    UriComponentsBuilder.fromUriString(s).build();
                            final UriComponentsBuilder copyUriBuilder =
                                    UriComponentsBuilder.newInstance()
                                            .scheme(originalUri.getScheme())
                                            .host(matchingHostnamePart)
                                            .path(originalUri.getPath())
                                            .query(originalUri.getQuery());
                            if (StringUtils.isNotBlank(matchingPortPart)) {
                                copyUriBuilder.port(matchingPortPart);
                            }
                            copyList.add(copyUriBuilder.toUriString());
                        }
                        uriComponentsBuilder.replaceQueryParam(
                                LOGIN_URL_QUERYSTRING_PARAMETER, copyList.toArray());
                    }

                    // Replace LOGIN_URL_QUERYSTRING_PARAMETER
                    if (parameters.containsKey(LOGOUT_REDIRECT_QUERYSTRING_PARAMETER)) {
                        final List<String> originalList =
                                parameters.get(LOGOUT_REDIRECT_QUERYSTRING_PARAMETER);
                        final List<String> copyList = new ArrayList<>();
                        for (String s : originalList) {
                            final UriComponents originalUri =
                                    UriComponentsBuilder.fromUriString(s).build();
                            final UriComponentsBuilder copyUriBuilder =
                                    UriComponentsBuilder.newInstance()
                                            .scheme(originalUri.getScheme())
                                            .host(matchingHostnamePart)
                                            .path(originalUri.getPath())
                                            .query(originalUri.getQuery());
                            if (StringUtils.isNotBlank(matchingPortPart)) {
                                copyUriBuilder.port(matchingPortPart);
                            }
                            copyList.add(copyUriBuilder.toUriString());
                        }
                        uriComponentsBuilder.replaceQueryParam(
                                LOGOUT_REDIRECT_QUERYSTRING_PARAMETER, copyList.toArray());
                    }

                } catch (Exception e) {
                    logger.warn("Failed to customize the specified URL:  {}", url);
                    throw new RuntimeException(e);
                }

                final String rslt = uriComponentsBuilder.build().toUriString();
                logger.debug("Customizing URL from [{}] to [{}]", url, rslt);
                return rslt;
            }
        }

        return url; // No change
    }

    private String findMatchingServerName(final HttpServletRequest request) {
        if (request != null) {
            final String comparisonHost = request.getHeader("Host");
            if (comparisonHost != null && !comparisonHost.isEmpty()) {
                for (final String server : this.allServerNames) {
                    if (server.contains(comparisonHost)) {
                        return server;
                    }
                }
            }
        }

        return null;
    }
}
