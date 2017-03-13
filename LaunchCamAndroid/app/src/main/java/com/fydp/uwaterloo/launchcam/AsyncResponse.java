package com.fydp.uwaterloo.launchcam;

import java.util.List;

/**
 * Created by stephen on 3/11/17.
 */

public interface AsyncResponse {
    void processFinish(List<String> videoFileNames, List<String> pictureFileNames);
}