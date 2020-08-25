package com.kyle.random.other.helpers;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
public class AudioRecorder {
    final MediaRecorder recorder = new MediaRecorder();
    public final String path;
    public AudioRecorder(String path) {
        this.path = sanitizePath(path);
    }
    private String sanitizePath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.contains(".")) {
            path += ".3gp";
        }
        return path ;
    }
    public boolean start() {
        try {
            String state = android.os.Environment.getExternalStorageState();
            if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
                throw new IOException("SD Card is not mounted.  It is " + state
                        + ".");
            }
            // make sure the directory we plan to store the recording in exists
            File directory = new File(path).getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Path to file could not be created.");
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(path);
            recorder.prepare();
            recorder.start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean stop() {
        try {

            recorder.stop();
            recorder.release();
            return true;
        }catch (Exception e){
            return false;
        }

    }
    public void playarcoding(String path) throws IOException {
        MediaPlayer mp = new MediaPlayer();
        mp.setDataSource(path);
        mp.prepare();
        mp.start();
        mp.setVolume(10, 10);
    }
}