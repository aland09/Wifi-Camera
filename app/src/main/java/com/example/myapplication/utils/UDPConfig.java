package com.example.myapplication.utils;

public class UDPConfig {
    public static final int CAMERA_BATTERY_PORT = 6090;
    public static final String CAMERA_IP = "192.168.1.101";
    public static final int CAMERA_VIDEO_PORT = 6080;
    public static final byte[] CMD_START_IMG = {35, 54};
    public static final byte[] CMD_STOP_IMG = {35, 55};
    public static final byte[] CMD_CHECK_BATTERY = {102, 33};
}