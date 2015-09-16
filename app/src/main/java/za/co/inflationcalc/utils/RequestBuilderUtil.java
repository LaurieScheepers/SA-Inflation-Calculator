package za.co.inflationcalc.utils;

import java.net.URLEncoder;

/**
 * Created by Laurie on 9/14/2015.
 */
public class RequestBuilderUtil {
    public static String buildGetResultQuery(String date1, String date2, int amount) throws IllegalArgumentException {

        if (StringUtil.isNullOrEmpty(date1)) {
            throw new IllegalArgumentException("Start date can't be empty");
        }

        if (StringUtil.isNullOrEmpty(date2)) {
            throw new IllegalArgumentException("End date can't be empty");
        }

        if (amount < 0) {
            throw new IllegalArgumentException("Amount can't be less than 0");
        }

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("date1=");
        queryBuilder.append(date1);

        queryBuilder.append("&date2=");
        queryBuilder.append(date2);

        queryBuilder.append("&amount=");
        queryBuilder.append(amount);

        return queryBuilder.toString();
    }
}
