package app.stepwin.retrofit_provider;

import app.stepwin.constants.Constants;
import app.stepwin.model.UserHistory;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitApiClient {

    @FormUrlEncoded
    @POST(Constants.USER_PROGRESS_URL)
    Call<ResponseBody> postUserProgress(@Field("step") String step,
                                        @Field("distant") String distant,
                                        @Field("calories") String calories,
                                        @Field("points") String points,
                                        @Field("minute") String minute,
                                        @Field("time_milli") long time_milli,
                                        @Field("unlocl_counter") String unlocl_counter,
                                        @Field("date") String date,
                                        @Field("username") String username,
                                        @Field("email") String email);

    @FormUrlEncoded
    @POST(Constants.USER_HISTORY_URL)
    Call<UserHistory> userHistoryL(@Field("username") String username,
                                   @Field("email") String email);

    @FormUrlEncoded
    @POST(Constants.USER_PREVIOUS_URL)
    Call<ResponseBody> userPreviousL(@Field("username") String username,
                                     @Field("email") String email,@Field("date") String date);
    @FormUrlEncoded
    @POST(Constants.FB_SHARE_Count_URL)
    Call<ResponseBody> fbSharecount(@Field("username") String username,
                                     @Field("email") String email);

    @FormUrlEncoded
    @POST(Constants.LOGIN_URL)
    Call<ResponseBody> login(@Query("mFullname") String mFullname,
                          @Field("Email") String Email);


    @GET(Constants.FACEBOOK_LOGIN_URL)
    Call<ResponseBody> loginFb();



   /*
   @FormUrlEncoded
    @POST(Constant.LOGIN_URL)
    Call<LoginFormPageData> logiPageLabel(@Field("mch_id") String mch_id,
                                          @Field("op") String op,
                                          @Field("lang") String lang);


   @GET(Constant.LOGIN_URL)
    Call<StoreAdmin> login(@Query("username") String username,
                           @Query("password") String password);


    @GET(Constant.USER_SEARCH_URL)
    Call<UserSearch> search(@Query("Username") String Username,
                            @Query("password") String password,
                            @Query("Mobile") String Mobile);

    @Headers("Content-Type: application/json")
    @POST(Constant.FEEDBACK_SUBMISSION_URL)
    Observable<ResponseBody> sendFeedback(@Body RequestBody body);


   @Headers("Content-Type: application/json")
    @POST(Constant.ADD_USER_DETAILS_URL)
   Call<ResponseBody> addUser(@Body RequestBody body);*/
}


/*@Headers("Content-Type: application/json")
    @POST(Constant.FEEDBACK_SUBMISSION_URL)
    Observable<ResponseBody> sendFeedback(@Body String jsonObject);*/























/*@Query("Username") String Username,
                               @Query("password") String password,*/


 /*   @FormUrlEncoded
    @POST(Constant.USER_LOGIN)
    Call<LoginUserModal> userLogin(@Field("type") String type,
                                   @Field("user_id") String user_id,
                                   @Field("device_token") String device_token,
                                   @Field("password") String password);

    @GET(Constant.DEPARTMENT_LIST)
    Call<DepartmentDetailModal> departmentList();


    @Multipart
    @POST(Constant.SEND_IMAGE_DETAIL)
    Call<ResponseBody> sendReportConcern(@Part MultipartBody.Part image,
                                         @Part("date") RequestBody date,
                                         @Part("time") RequestBody time,
                                         @Part("user_id") RequestBody emp_id,
                                         @Part("department_id") RequestBody dept,
                                         @Part("to") RequestBody sendTo,
                                         @Part("user_type") RequestBody user_type,
                                         @Part("caption") RequestBody caption);

*/
