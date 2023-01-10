package com.example.mapper.apis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.mapper.data.Messages;
import com.example.mapper.models.AccountManagement;
import com.example.mapper.models.Settings;
import com.example.mapper.models.Subject;
import com.example.mapper.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper API library.
 */
public class Mapper {
    private static RequestQueue requestQueue = null;
    public static String ACCESS_TOKEN_KEY = "token";

    public static RequestQueue initiateRequestQueueInstance(Context context){
        if(requestQueue == null) {
            synchronized (Mapper.class) {
                if (requestQueue == null) {
                    // Instantiate the cache
                    Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap

                    // Set up the network to use HttpURLConnection as the HTTP client.
                    Network network = new BasicNetwork(new HurlStack());

                    // Instantiate the RequestQueue with the cache and network.
                    requestQueue = new RequestQueue(cache, network);

                    // Start the queue
                    requestQueue.start();
                }
            }
        }
        return requestQueue;
    }
    private static RequestQueue getRequestQueueInstance(){
        return requestQueue;
    }

    private static String baseUrl = null;

    private static User authenticatedUser = null;

    /**
     * Set rest api's host name & base query
     * @param url
     */
    public static void setBaseUrl(String url){
        baseUrl = url + "/api";
    }

    /**
     * Sign in the user
     * @param user
     * @param taskTransition
     * @param responseCallBack
     */
    public static void SignIn(User user, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/login";

        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("email", user.email);
        params.put("password", user.password);
        params.put("device_name", getDeviceName());


        taskTransition.onBefore();


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
            (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                try {
                    if(response.has("status") && response.getBoolean("status")){
                        responseCallBack.onSuccess(response);
                    }else{
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                } catch (JSONException e) {
                    responseCallBack.onError(getLocalizedErrorMessage(null), null);
                }
                taskTransition.onAfter();

            }, error -> {
                String errorMessage = null;
                JSONObject errorObj = null;

                try{
                    errorMessage = getLocalizedErrorMessage(error);

                    //parse response json
                    if(error.networkResponse != null) {
                        String json = null;
                        json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                        errorObj = new JSONObject(json);
                    }else if(errorMessage == null){
                        errorMessage = "Something was wrong! Please try again.";
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    errorMessage = "Something was wrong! Please try again.";
                }

                responseCallBack.onError(errorMessage, errorObj);
                taskTransition.onAfter();
            }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Sign up the user
     * @param user
     * @param taskTransition
     * @param responseCallBack
     */
    public static void SignUp(User user, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/register";

        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("email", user.email);
        params.put("first_name", user.firstname);
        params.put("last_name", user.lastname);
        params.put("password", user.password);
        params.put("device_name", getDeviceName());


        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Sign out the authenticated user
     */
    public static void SignOut(Context context, TaskTransition taskTransition, ResponseCallBack responseCallBack) {
        String endpoint = baseUrl + "/logout";

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, null, response -> {
                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            RemoveAuthenticatedUser();
                            RemoveAuthenticationToken(context);

                            responseCallBack.onSuccess(null);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {

                    String errorMessage;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                            Log.d("SDSD", errorObj.toString());


                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }


    /**
     * Forgot password - step 1
     * This
     * @param email
     * @param taskTransition
     * @param responseCallBack
     */
    public static void ForgotPasswordStep1(String email, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/forgot-password/step-1";

        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);


        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                    try {
                        if(response.has("success") && response.getBoolean("success")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }
    /**
     * Forgot password - step 2
     * @param email
     * @param otp
     * @param taskTransition
     * @param responseCallBack
     */
    public static void ForgotPasswordStep2(String email, String otp, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/forgot-password/step-2";

        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("otp", otp);


        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                    try {
                        if(response.has("success") && response.getBoolean("success")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }
    /**
     * Forgot password - step 2
     * @param email
     * @param otp
     * @param newPassword
     * @param taskTransition
     * @param responseCallBack
     */
    public static void ForgotPasswordStep3(String email, String otp, String newPassword, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/forgot-password/step-3";

        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("otp", otp);
        params.put("password", newPassword);

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                    try {
                        if(response.has("success") && response.getBoolean("success")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        if(error instanceof AuthFailureError){
                            responseCallBack.NotAuthenticated();
                        }

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);

                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }


    /**
     * Change the password
     * @param taskTransition
     * @param responseCallBack
     */
    public static void ChangePassword(Context context, String currentPassword, String newPassword, String newPasswordConfirmation, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/account/new-password";

        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("current_password", currentPassword);
        params.put("new_password", newPassword);
        params.put("new_password_confirmation", newPasswordConfirmation);

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {
                    try {
                        if(response.has("success") && response.getBoolean("success")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);



                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);

                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }


    /**
     * Fetch & Store authenticated user
     */
    public static void TryAuthenticate(Context context, TaskTransition taskTransition, ResponseCallBack responseCallBack) {
        String endpoint = baseUrl + "/fetch-user";
        Log.d("sdd", endpoint);
        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {

                    try {
                        if(response.has("status")
                                && response.getBoolean("status")
                                && response.has("user")){

                            JSONObject jUser = response.getJSONObject("user");

                            User user = new User();
                            user.email = jUser.getString("email");
                            user.firstname = jUser.getString("first_name");
                            user.lastname = jUser.getString("last_name");

                            SetAuthenticatedUser(user);

                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    Log.d("Sds", error.toString());
                    error.printStackTrace();

                    String errorMessage = null;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);


                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }


    /**
     * Update the settings
     * @param settings
     * @param taskTransition
     * @param responseCallBack
     */
    public static void UpdateSettings(Context context, Settings settings, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/account/settings";

        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("prioritization", String.valueOf(settings.prioritisation));

        params.put("duration_between_activities", String.valueOf(settings.durationBetweenActivities));

        params.put("activity_max_duration", String.valueOf(settings.activityMaxDuration));

        params.put("day_starts_at", settings.dayStartsAt);
        params.put("day_ends_at", settings.dayEndsAt);

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);



                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);

                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * get settings
     * @param context
     * @param taskTransition
     * @param responseCallBack
     */
    public static void GetSettings(Context context, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/account/settings";

        taskTransition.onBefore();

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, new JSONObject(), response -> {
                    Log.i("Sdsdsd", response.toString());
                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();
                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);

                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }


    /**
     * get account managment data
     * @param context
     * @param taskTransition
     * @param responseCallBack
     */
    public static void GetAccountManagement(Context context, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/account/management";

        taskTransition.onBefore();

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, new JSONObject(), response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();
                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);

                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Update the account management data
     * @param accountManagement
     * @param taskTransition
     * @param responseCallBack
     */
    public static void UpdateAccountManagement(Context context, AccountManagement accountManagement, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/account/management";

        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("first_name", String.valueOf(accountManagement.firstName));
        params.put("last_name", String.valueOf(accountManagement.lastName));

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);



                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);

                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Fetch Subjects
     * @param taskTransition
     * @param responseCallBack
     */
    public static void fetchSubject(Context context, int perPage, int page, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/subjects?" + "per_page=" + perPage + "&page=" + page;


        taskTransition.onBefore();

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }
    /**
     * Create new subject
     * @param name
     * @param taskTransition
     * @param responseCallBack
     */
    public static void createSubject(Context context, String name, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/subjects/new";

        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("subject_name", name);

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);



                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);

                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }



    /**
     * Update subject
     */
    public static void updateSubject(Context context, Integer id, String name, TaskTransition taskTransition, ResponseCallBack responseCallBack) {
        String endpoint = baseUrl + "/subjects/" + id + "/update";

        HashMap<String, String> params = new HashMap<>();
        params.put("subject_name", name);

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {
                    try {
                        if(response.has("status") && response.getBoolean("status")){
                           responseCallBack.onSuccess(null);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {

                    String errorMessage;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                            Log.d("SDSD", errorObj.toString());


                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Delete subject
     */
    public static void deleteSubject(Context context, Integer id, TaskTransition taskTransition, ResponseCallBack responseCallBack) {
        String endpoint = baseUrl + "/subjects/" + id + "/delete";

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, null, response -> {
                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(null);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {

                    String errorMessage;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                            Log.d("SDSD", errorObj.toString());


                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Fetch modules
     * @param taskTransition
     * @param responseCallBack
     */
    public static void fetchModule(Context context, int subjectId, int perPage, int page, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/subjects/" + subjectId + "/modules?" + "per_page=" + perPage + "&page=" + page;


        taskTransition.onBefore();



        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }


    /**
     * Create new module
     * @param subjectId
     * @param data
     * @param taskTransition
     * @param responseCallBack
     */
    public static void createModule(Context context, int subjectId, HashMap<String, String> data, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/subjects/" + subjectId + "/modules/new";

        taskTransition.onBefore();



        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(data), response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {

                    String errorMessage = null;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);



                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);

                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Update module
     * @param subjectId
     * @param data
     * @param taskTransition
     * @param responseCallBack
     */
    public static void updateModule(Context context, int subjectId, int moduleId, HashMap<String, String> data, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/subjects/" + subjectId + "/modules/" + moduleId + "/update";

        taskTransition.onBefore();



        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(data), response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {

                    String errorMessage = null;
                    JSONObject errorObj = null;

                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);



                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);

                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }


    /**
     * Delete subject
     */
    public static void deleteModule(Context context, int subjectId, int moduleId, TaskTransition taskTransition, ResponseCallBack responseCallBack) {
        String endpoint = baseUrl + "/subjects/" + subjectId + "/modules/" + moduleId + "/delete";
        Log.i("sdsdsd", endpoint);
        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, null, response -> {
                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(null);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {

                    String errorMessage;
                    JSONObject errorObj = null;
                    Log.e("sdsdsd", error.toString());
                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                            Log.d("SDSD", errorObj.toString());


                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Generate new schedule
     */
    public static void generateSchedule(Context context, TaskTransition taskTransition, ResponseCallBack responseCallBack) {
        String endpoint = baseUrl + "/schedule/generate-new";

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, null, response -> {
                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {

                    String errorMessage;
                    JSONObject errorObj = null;
                    Log.e("sdsdsd", error.toString());
                    if(error instanceof AuthFailureError){
                        responseCallBack.NotAuthenticated();
                    }

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                            Log.d("SDSD", errorObj.toString());


                            if(errorObj.getString("message") == "Unauthenticated"){
                                responseCallBack.NotAuthenticated();
                            }

                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(25 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }
    /**
     * Fetch all scheduled  slots
     * @param taskTransition
     * @param responseCallBack
     */
    public static void fetchSchedule(Context context, int filterStatus, int perPage, int page, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/schedule?" + "per_page=" + perPage + "&page=" + page + "&filter=" + filterStatus ;


        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        Log.e("SDDSD", String.valueOf(jsonObjectRequest.toString()));

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Fetch all scheduled  slots
     * @param taskTransition
     * @param responseCallBack
     */
    public static void fetchScheduleOfModule(Context context, int moduleId, int perPage, int page, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/modules/" + moduleId + "/schedule?" + "per_page=" + perPage + "&page=" + page;


        taskTransition.onBefore();


        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Fetch all scheduled  slots
     * @param taskTransition
     * @param responseCallBack
     */
    public static void fetchTodayActivities(Context context, int filterStatus, int perPage, int page, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/schedule/today-activities?" + "per_page=" + perPage + "&page=" + page + "&filter=" + filterStatus ;



        taskTransition.onBefore();

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }


    /**
     * Fetch schedule
     * @param taskTransition
     * @param responseCallBack
     */
    public static void toggleFinishScheduleSlot(Context context, int scheduleId, boolean isFinished, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/schedule/" + scheduleId +  "/toggle-finish";


        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("is_finished", isFinished ? "1" : "0");

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }

    /**
     * Fetch schedule
     * @param taskTransition
     * @param responseCallBack
     */
    public static void reScheduleSlot(Context context, int scheduleId, String date, String startAt, String endAt, TaskTransition taskTransition, ResponseCallBack responseCallBack){
        String endpoint = baseUrl + "/schedule/" + scheduleId +  "/reschedule";


        taskTransition.onBefore();

        HashMap<String, String> params = new HashMap<>();
        params.put("date", date);
        params.put("start_at", startAt);
        params.put("end_at", endAt);

        taskTransition.onBefore();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, new JSONObject(params), response -> {

                    try {
                        if(response.has("status") && response.getBoolean("status")){
                            responseCallBack.onSuccess(response);
                        }else{
                            responseCallBack.onError(getLocalizedErrorMessage(null), null);
                        }
                    } catch (JSONException e) {
                        responseCallBack.onError(getLocalizedErrorMessage(null), null);
                    }
                    taskTransition.onAfter();

                }, error -> {
                    String errorMessage = null;
                    JSONObject errorObj = null;

                    try{
                        errorMessage = getLocalizedErrorMessage(error);

                        //parse response json
                        if(error.networkResponse != null) {
                            String json = null;
                            json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                            errorObj = new JSONObject(json);
                        }else if(errorMessage == null){
                            errorMessage = "Something was wrong! Please try again.";
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        errorMessage = "Something was wrong! Please try again.";
                    }

                    responseCallBack.onError(errorMessage, errorObj);
                    taskTransition.onAfter();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + GetAuthenticationToken(context));
                return headers;
            }
        };

        // Access the RequestQueue through singleton.
        getRequestQueueInstance().add(jsonObjectRequest);
    }


    /**
     * Get authentication token
     * @param context
     * @return
     */
    public static String GetAuthenticationToken(Context context){
        try {
            SharedPreferences sharedPreferences = SecureSharedPreferences
                    .getEncryptedSharedPreferences(context);

            return  sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Store authentication token in shared preferences.
     * @param context
     * @param token
     */
    public static void SetAuthenticationToken(Context context, String token){
        try {
            SharedPreferences sharedPreferences = SecureSharedPreferences
                    .getEncryptedSharedPreferences(context);

             sharedPreferences.edit()
                    .putString(ACCESS_TOKEN_KEY, token)
                    .apply();

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Remove authentication token in shared preferences.
     * @param context
     */
    public static void RemoveAuthenticationToken(Context context){
        try {
            SharedPreferences sharedPreferences = SecureSharedPreferences
                    .getEncryptedSharedPreferences(context);

            sharedPreferences.edit()
                    .remove(ACCESS_TOKEN_KEY)
                    .apply();

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void SetAuthenticatedUser(User user){
        authenticatedUser = user;
    }
    public static User GetAuthenticatedUser(){
        return authenticatedUser;
    }
    private static void RemoveAuthenticatedUser(){
        authenticatedUser = null;
    }
    /**
     * Check whether the user has authentication token
     * @param context
     */
    public static boolean IsAuthenticated(Context context){
        try {
            SharedPreferences sharedPreferences = SecureSharedPreferences
                    .getEncryptedSharedPreferences(context);

            return sharedPreferences
                    .getString(ACCESS_TOKEN_KEY, null) != null;

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the device name
     * @return
     */
    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }

    /**
     * Get localized Http error message
     * @param error
     * @return
     */
    private static String getLocalizedErrorMessage(VolleyError error){
        if (error instanceof NetworkError) {
            return Messages.NO_CONNECTION_ERROR_MESSAGE;
        } else if (error instanceof ServerError) {
            return Messages.UNKNOWN_ERROR_MESSAGE;
        } else if (error instanceof AuthFailureError) {
            return Messages.AUTH_FAILURE_ERROR_MESSAGE;
        } else if (error instanceof ParseError) {
            return Messages.PARSE_ERROR_MESSAGE;
        } else if (error instanceof NoConnectionError) {
            return Messages.NO_CONNECTION_ERROR_MESSAGE;
        } else if (error instanceof TimeoutError) {
            return Messages.CONNECTION_TIMEOUT_ERROR_MESSAGE;
        }else{
            return Messages.UNKNOWN_ERROR_MESSAGE;
        }
    }

    public interface TaskTransition{
        default void onBefore(){};
        default void onAfter(){};
    }

    public interface ResponseCallBack{
        default void onSuccess(JSONObject response){};
        default void onError(String message, @Nullable JSONObject error){};
        default void NotAuthenticated(){};
    }
}
