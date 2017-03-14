package com.fydp.uwaterloo.launchcam.Service;
import com.fydp.uwaterloo.launchcam.Model.CameraModel;
import com.fydp.uwaterloo.launchcam.Model.CameraStatusModel;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface CameraService {
    String SERVICE_ENDPOINT = "http://10.5.5.9/gp/gpControl";

    @GET("/execute")
    Observable<CameraModel> findSession(@Query("p1") String gpStream, @Query("a1") String proto_v2, @Query("c1") String restart);
    @GET("/status")
    Observable<CameraStatusModel> getStatus();
    @GET("/command/shutter")
    Observable<CameraModel> record(@Query("p") int isRecord);
    @GET("/command/mode")
    Observable<CameraModel> primaryMode(@Query("p") int primaryMode);
    @GET("/setting/2/{res}")
    Observable<CameraModel> setResolution(@Path("res") String resCode);
    @GET("/setting/3/{fr}")
    Observable<CameraModel> setFrameRate(@Path("fr") String frCode);
    @GET("/setting/4/{fov}")
    Observable<CameraModel> setFOV(@Path("fov") String fovCode);
}