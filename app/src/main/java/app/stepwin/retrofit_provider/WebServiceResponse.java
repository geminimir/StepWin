package app.stepwin.retrofit_provider;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Response;

public class WebServiceResponse {

    public static void handleResponse(Response<?> response, WebResponse webResponse) {
        if (response.isSuccessful()) {
            if (response.body() != null) {
                webResponse.onResponseSuccess(response);
            } else {
                webResponse.onResponseFailed(response.message());
            }
        } else if (response.code() == 403) {
            webResponse.onResponseFailed("Invalid UserName or Password");
        } else if (response.code() == 500) {
            webResponse.onResponseFailed("An error has occurred. 'Internal Server Error' !!!");
        } else {
            try {
                if (response.errorBody() != null) {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    webResponse.onResponseFailed(jObjError.getString("error"));
                } else {
                    webResponse.onResponseFailed(response.message());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}