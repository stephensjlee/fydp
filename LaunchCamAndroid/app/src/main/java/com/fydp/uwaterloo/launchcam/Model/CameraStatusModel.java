package com.fydp.uwaterloo.launchcam.Model;

import com.google.gson.annotations.SerializedName;

import static com.fydp.uwaterloo.launchcam.R.id.battery;

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

    public class SettingsModel {
        @SerializedName("2")
        int videoResolution;
        @SerializedName("3")
        int frameRate;
        @SerializedName("4")
        int fieldOfView;

        public int getVideoResolution() {
            return videoResolution;
        }
        public int getFrameRate() {
            return frameRate;
        }
        public int getFieldOfView() {
            return fieldOfView;
        }
    }

    public StatusModel getStatusModel() {
        return status;
    }

    public SettingsModel getSettingsModel() {
        return settings;
    }
}
