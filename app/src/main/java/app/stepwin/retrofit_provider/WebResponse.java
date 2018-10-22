package app.stepwin.retrofit_provider;

import retrofit2.Response;

/**
 * Created by pc6 on 3/20/2017.
 */

public interface WebResponse {

    void onResponseSuccess(Response<?> result);

    void onResponseFailed(String error);
}