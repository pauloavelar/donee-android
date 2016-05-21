package me.avelar.donee.web;

import java.util.ArrayList;

import me.avelar.donee.model.Collection;
import me.avelar.donee.model.Form;
import me.avelar.donee.model.LoginRequest;
import me.avelar.donee.model.Session;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;

public interface DoneeService {

    String HEADER_SESSION = "X-Donee-Session-Id";

    enum RequestStatus { SUCCEEDED, NO_CONNECTION, SERVER_ERROR, UNKNOWN_ERROR }

    @POST("/login")
    void performLogin(@Body LoginRequest credentials, Callback<Session> cb);

    @GET("/session")
    void validateSession(@Header(HEADER_SESSION) String sessionId, Callback<Session> cb);

    @GET("/forms")
    void listForms(@Header(HEADER_SESSION) String sessionId, Callback<ArrayList<Form>> cb);

    @POST("/forms")
    SenderResponse submitForm(@Header(HEADER_SESSION) String sessionId, @Body Collection collection);
    // no need for asynchrony because this will be run in a service

}
