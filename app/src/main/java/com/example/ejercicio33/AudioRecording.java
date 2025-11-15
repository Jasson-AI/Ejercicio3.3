package com.example.ejercicio33;

public class AudioRecording {
    public long id;
    public String name;
    public int durationSeconds;
    public String recordDate;

    public AudioRecording(long id, String name, int durationSeconds, String recordDate) {
        this.id = id;
        this.name = name;
        this.durationSeconds = durationSeconds;
        this.recordDate = recordDate;
    }
}
