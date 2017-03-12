package com.fydp.uwaterloo.launchcam.Model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sahil on 3/12/2017.
 */
public class CameraStatusModel {
    StatusModel status;
    SettingsModel settings;

    public class StatusModel {
        @SerializedName("2")
        int battery;

        public int getBattery() {
            return battery;
        }
    }

    private class SettingsModel {
    }

    public StatusModel getStatusModel() {
        return status;
    }

    public SettingsModel getSettingsModel() {
        return settings;
    }
}
