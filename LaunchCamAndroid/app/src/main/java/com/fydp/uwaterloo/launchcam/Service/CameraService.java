package com.fydp.uwaterloo.launchcam.Service;
import com.fydp.uwaterloo.launchcam.Model.CameraModel;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface CameraService {
    String SERVICE_ENDPOINT = "http://10.5.5.9/gp/gpControl";

    @GET("/users/{login}")
    Observable<CameraModel> getUser(@Path("login") String login);
}