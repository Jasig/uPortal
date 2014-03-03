package org.jasig.portal.portlets.marketplace;

import org.jasig.portal.portlet.dao.jpa.MarketplaceRatingPK;

public interface IMarketplaceRating{
    public static final int MAX_RATING = 5;
    public static final int MIN_RATING = 0;
    
    public int getRating();

    public void setRating(int rating);

    public MarketplaceRatingPK getMarketplaceRatingPK();

    public void setMarketplaceRatingPK(MarketplaceRatingPK marketplaceRatingPK);
    
}
