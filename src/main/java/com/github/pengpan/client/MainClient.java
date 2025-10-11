package com.github.pengpan.client;

import java.util.List;
import java.util.Map;

import com.github.pengpan.common.proxy.Proxy;
import com.github.pengpan.common.retry.Retry;
import com.github.pengpan.entity.BrushSch;
import com.github.pengpan.entity.CheckUser;
import com.github.pengpan.entity.CheckUserV3;
import com.github.pengpan.entity.DoctorSch;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * @author pengpan
 */
public interface MainClient {

    @GET
    String htmlPage(@Url String url);

    @FormUrlEncoded
    @POST
    CheckUser checkUser(@Url String url, @FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST
    CheckUserV3 checkUserV3(@Url String url, @FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST
    Response<Void> doLogin(@Url String url, @FieldMap Map<String, String> fields);

    @GET
    Response<String> loginRedirect(@Url String url);

    @FormUrlEncoded
    @POST("/ajax/getunitbycity.html")
    List<Map<String, Object>> getUnit(@Field("c") String cityId);

    @POST("/home/ajaxgetguahaourl.html")
    @Headers("X-Requested-With: XMLHttpRequest")
    Map<String, Object> getLocalUnit();

    @FormUrlEncoded
    @POST("/ajax/getdepbyunit.html")
    List<Map<String, Object>> getDept(@Field("keyValue") String unitId);

    @Retry(maxRetries = 10)
    @Proxy
    @GET
    BrushSch dept(@Url String url,
                  @Query("unit_id") String unitId,
                  @Query("dep_id") String deptId,
                  @Query("date") String date,
                  @Query("p") int page,
                  @Query("user_key") String userKey);

    @Retry(maxRetries = 10)
    @Proxy
    @GET
    DoctorSch doctor(@Url String url,
                     @Query("user_key") String userKey,
                     @Query("docid") String docid,
                     @Query("doc_id") String doc_id,
                     @Query("unit_id") String unitId,
                     @Query("dep_id") String deptId,
                     @Query("date") String date,
                     @Query("days") int days);

    @GET("/guahao/ystep1/uid-{unitId}/depid-{deptId}/schid-{schId}.html")
    String orderPage(@Path("unitId") String unitId,
                     @Path("deptId") String deptId,
                     @Path("schId") String schId);

    @FormUrlEncoded
    @POST("/guahao/ysubmit.html")
    Response<Void> doSubmit(@Field("sch_data") String schData,
                            @Field("unit_id") String unitId,
                            @Field("dep_id") String depId,
                            @Field("doctor_id") String doctorId,
                            @Field("schedule_id") String schId,
                            @Field("mid") String memberId,
                            @Field("accept") String accept,
                            @Field("time_type") String time_type,
                            @Field("detlid") String detlid,
                            @Field("detlid_realtime") String detlid_realtime,
                            @Field("level_code") String level_code,
                            @Field("addressId") String addressId,
                            @Field("address") String address,
                            @Field("hisMemId") String hisMemId);

    @GET("/favicon.ico")
    Response<Void> serverTime();

    @GET
    Response<ResponseBody> captcha(@Url String url);

    @FormUrlEncoded
    @POST
    Response<String> doLoginV2(@Url String url, @FieldMap Map<String, String> fields);

    @GET("/dep/show/depid-{deptId}.html")
    String deptPage(@Path("deptId") String deptId);
}
