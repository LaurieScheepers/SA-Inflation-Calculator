package za.co.inflationcalc.comms.http;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * The REST API client that does the API requests
 * <p/>
 *
 * Created by Laurie on 9/14/2015.
 */
public class RestClient {
    public static final String BASE_URL = "http://inflationcalc.co.za/api.php";

    private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public static void get(Context context, String absoluteUrl, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        asyncHttpClient.get(context, absoluteUrl, params, responseHandler);
    }
}
