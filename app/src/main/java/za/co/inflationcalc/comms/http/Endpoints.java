package za.co.inflationcalc.comms.http;

/**
 * Created by Laurie on 9/14/2015.
 */
public interface Endpoints {

    /**
     * GET http://inflationcalc.co.za/api.php?date1={date1}&date2={date2}&amount={amount}
     * @see za.co.inflationcalc.utils.RequestBuilderUtil#buildGetResultQuery(String, String, int)
     */

    String GET_RESULT = "/?$s";
}
