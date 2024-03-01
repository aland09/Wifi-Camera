package com.example.myapplication.utils;

//import com.example.myapplication.easydarwin.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.app.FrameMetricsAggregator;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.common.base.Ascii;

import org.easydarwin.video.EasyPlayerClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/* loaded from: classes.dex */
public class UDPManager {
    public static final int BUFFER_SIZE = 1472;
    private static final String TAG = "UDPManager";
    private static volatile UDPManager mInstance;
    private TimerTask batteryTask;
    private TimerTask connectTask;
    private String deviceType;
    public boolean is9AConnect;
    public boolean is9BConnect;
    public boolean is9EConnect;
    public boolean is9SConnect;
    public boolean is9WConnect;
    public boolean isWF200Connect;
    public boolean isWF80DConnect;
    ArrayList<CameraListener> listeners;
    private int loseCount;
    private Timer mBatteryTimer;
    private CameraListCallBack mCameraListCallBack;
    private Timer mConnectTimer;
    private ResolutionCallBack mResolutionCallBack;
    public DatagramSocket socket;
    private byte[] picData = new byte[1024000];
    private final byte[] cmdStartImg = {35, 54};
    private final byte[] cmdStopImg = {35, 55};
    private final byte[] cmdCheckBattery = {102, 33};
    private final byte[] cmdCheckVersion = {102, Ascii.CAN};
    private final byte[] cmdCheckResolution = {102, Ascii.DC4};
    private final byte[] cmdGetCurrentResolution = {102, Ascii.ETB};
    private final byte[] cmdSetLightIntensity = {102, 37, 0};
    private final byte[] cmdChangeCamera = {102, 35, 0};
    private final byte[] cmdGetCurrentCamera = {102, 41};
    private final byte[] cmdGetDeviceCameraList = {102, 39};
    byte g_id = 0;
    byte g_pkt_cnt = 0;
    int g_jpeg_len = 0;
    int len = 0;

    private String tag = "UDPManager";

    /* loaded from: classes.dex */
    public interface CameraListCallBack {
        void cameraList(int i);
    }

    /* loaded from: classes.dex */
    public interface ResolutionCallBack {
        void resolutionList(List<String> list);
    }

    private int CONV_GS_INT(int i) {
        return (i & 512) == 512 ? -(i & FrameMetricsAggregator.EVERY_DURATION) : i & AnalyticsListener.EVENT_DRM_KEYS_LOADED;
    }

    private int byteToInt(byte b) {
        return b >= 0 ? b : b + 256;
    }

    private UDPManager() {
    }

    public static UDPManager getInstance(CameraListener cameraListener) {
        if (mInstance == null) {
            synchronized (UDPManager.class) {
                if (mInstance == null) {
                    mInstance = new UDPManager();
                    mInstance.init();
                    mInstance.listeners = new ArrayList<>();
                }
            }
        }
        mInstance.listeners.add(cameraListener);
        return mInstance;
    }

    public void init() {
        Log.d(tag, "init: Start");
        this.mBatteryTimer = new Timer();
        this.mConnectTimer = new Timer();
        this.loseCount = 0;
        try {
            DatagramSocket datagramSocket = new DatagramSocket((int) UDPConfig.CAMERA_BATTERY_PORT);
            this.socket = datagramSocket;
            datagramSocket.setSoTimeout(EasyPlayerClient.FrameInfoQueue.CAPACITY);
            openReceiver();
            Log.d(tag, "init: Success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(tag, "init: Error : " + e);
        }
        initTimerTask();
        Log.d(tag, "init: End");
    }

//    public void connectToCamera(Activity activity){
//        activity.startActivity(new Intent(activity, ));
//    }

    private void initTimerTask() {
        Log.d(tag, "initTimerTask: Start");
        this.batteryTask = new TimerTask() { // from class: io.grus.kycamera.Utils.socket.UDPManager.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                UDPManager.this.getBatteryData();
            }
        };
        this.connectTask = new TimerTask() { // from class: io.grus.kycamera.Utils.socket.UDPManager.2
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                if (UDPManager.this.deviceType != null) {
                    if (UDPManager.this.loseCount > 4) {
                        if (UDPManager.this.deviceType != "SA39A") {
                            if (UDPManager.this.deviceType != "SA39E") {
                                if (UDPManager.this.deviceType != "SA39W") {
                                    if (UDPManager.this.deviceType != "SA39S") {
                                        if (UDPManager.this.deviceType != "WF200") {
                                            if (UDPManager.this.deviceType != "SA39B") {
                                                if (UDPManager.this.deviceType == "WF80D") {
                                                    UDPManager.this.isWF80DConnect = false;
                                                }
                                            } else {
                                                UDPManager.this.is9BConnect = false;
                                            }
                                        } else {
                                            UDPManager.this.isWF200Connect = false;
                                        }
                                    } else {
                                        UDPManager.this.is9SConnect = false;
                                    }
                                } else {
                                    UDPManager.this.is9WConnect = false;
                                }
                            } else {
                                UDPManager.this.is9EConnect = false;
                            }
                        } else {
                            UDPManager.this.is9AConnect = false;
                        }
                        Iterator<CameraListener> it = UDPManager.this.listeners.iterator();
                        while (it.hasNext()) {
                            it.next().connectStateChanged(false, UDPManager.this.deviceType);
                        }
                    } else {
                        if (UDPManager.this.deviceType != "SA39A") {
                            if (UDPManager.this.deviceType != "SA39E") {
                                if (UDPManager.this.deviceType != "SA39W") {
                                    if (UDPManager.this.deviceType != "SA39S") {
                                        if (UDPManager.this.deviceType != "WF200") {
                                            if (UDPManager.this.deviceType != "SA39B") {
                                                if (UDPManager.this.deviceType == "WF80D") {
                                                    UDPManager.this.isWF80DConnect = true;
                                                }
                                            } else {
                                                UDPManager.this.is9BConnect = true;
                                            }
                                        } else {
                                            UDPManager.this.isWF200Connect = true;
                                        }
                                    } else {
                                        UDPManager.this.is9SConnect = true;
                                    }
                                } else {
                                    UDPManager.this.is9WConnect = true;
                                }
                            } else {
                                UDPManager.this.is9EConnect = true;
                            }
                        } else {
                            UDPManager.this.is9AConnect = true;
                        }
                        Iterator<CameraListener> it2 = UDPManager.this.listeners.iterator();
                        while (it2.hasNext()) {
                            it2.next().connectStateChanged(true, UDPManager.this.deviceType);
                        }
                    }
                }
                UDPManager.this.loseCount++;
                UDPManager.this.getDeviceVersion();
                Log.d(tag, "initTimerTask: run() Success");
            }
        };
        this.mBatteryTimer.schedule(this.batteryTask, 0L, C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS);
        this.mConnectTimer.schedule(this.connectTask, 0L, 1000L);
        Log.d(tag, "initTimerTask: End");
    }

    public void removeListener(CameraListener cameraListener) {
        this.listeners.remove(cameraListener);
    }

    public void startPreview() {
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.3
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCmd(uDPManager.cmdStartImg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void stopPreview() {
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.4
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCmd(uDPManager.cmdStopImg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void getBatteryData() {
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.5
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCheckCmd(uDPManager.cmdCheckBattery);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void getDeviceVersion() {
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.6
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCheckCmd(uDPManager.cmdCheckVersion);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void setLightIntensity(int i) {
        this.cmdSetLightIntensity[2] = (byte) i;
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.7
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCheckCmd(uDPManager.cmdSetLightIntensity);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void changeCameraWithId(int i) {
        this.cmdChangeCamera[2] = (byte) i;
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.8
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCheckCmd(uDPManager.cmdChangeCamera);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void getDeviceCameraList(CameraListCallBack cameraListCallBack) {
        this.mCameraListCallBack = cameraListCallBack;
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.9
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCheckCmd(uDPManager.cmdGetDeviceCameraList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void getCurrentCamera() {
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.10
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCheckCmd(uDPManager.cmdGetCurrentCamera);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void receiveCameraList(byte[] bArr) {
        this.mCameraListCallBack.cameraList(bArr[2]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void receiveCameraChange(byte[] bArr) {
        byte b = bArr[2];
        Iterator<CameraListener> it = this.listeners.iterator();
        while (it.hasNext()) {
            it.next().cameraChanged(b);
        }
    }

    public void getCurrentResolution() {
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.11
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCheckCmd(uDPManager.cmdGetCurrentResolution);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void getResolutionList(ResolutionCallBack resolutionCallBack) {
        this.mResolutionCallBack = resolutionCallBack;
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.12
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager uDPManager = UDPManager.this;
                        uDPManager.sendCheckCmd(uDPManager.cmdCheckResolution);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void setResolution(String str) {
        final byte[] bArr;
        if (str.contains("Front Camera")) {
            bArr = new byte[]{102, Ascii.SYN, 0};
        } else if (str.contains("Side Camera")) {
            bArr = new byte[]{102, Ascii.SYN, 1};
        } else {
            bArr = str.contains("Dual View") ? new byte[]{102, Ascii.SYN, 2} : new byte[]{102, Ascii.SYN, 0};
        }
        try {
            new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.13
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        UDPManager.this.sendCheckCmd(bArr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception unused) {
        }
    }

    public void openReceiver() {
        new Thread(new Runnable() { // from class: io.grus.kycamera.Utils.socket.UDPManager.14
            @Override // java.lang.Runnable
            public void run() {
                byte[] bArr = new byte[UDPManager.BUFFER_SIZE];
                DatagramPacket datagramPacket = new DatagramPacket(bArr, UDPManager.BUFFER_SIZE);
                while (true) {
                    try {
                        UDPManager.this.socket.receive(datagramPacket);
                        if (datagramPacket.getPort() == 6080) {
                            UDPManager.this.addPicData(bArr, UDPManager.BUFFER_SIZE);
                        } else if (bArr[1] == 34) {
                            UDPManager.this.updateBattery(bArr);
                        } else if (bArr[1] == 25) {
                            UDPManager.this.updateConnectState(bArr);
                        } else if (bArr[1] == 32) {
                            Log.d("🔥 🔥 🔥", "按键点击 ");
                        } else if (bArr[1] == 21) {
                            UDPManager.this.receiveResolutionList(bArr);
                        } else if (bArr[1] == 23) {
                            UDPManager.this.updateResolution(bArr);
                        } else if (bArr[1] != 38) {
                            if (bArr[1] == 36) {
                                UDPManager.this.receiveCameraChange(bArr);
                                Log.d("TAG", "-----run: " + ((int) bArr[2]));
                            } else if (bArr[1] == 40) {
                                UDPManager.this.receiveCameraList(bArr);
                            }
                        }
                    } catch (IOException unused) {
                    }
                }
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendCmd(byte[] bArr) throws IOException {
        if (this.socket != null) {
            this.socket.send(new DatagramPacket(bArr, bArr.length, InetAddress.getByName(UDPConfig.CAMERA_IP), (int) UDPConfig.CAMERA_VIDEO_PORT));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendCheckCmd(byte[] bArr) throws IOException {
        if (this.socket != null) {
            this.socket.send(new DatagramPacket(bArr, bArr.length, InetAddress.getByName(UDPConfig.CAMERA_IP), (int) UDPConfig.CAMERA_BATTERY_PORT));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void receiveResolutionList(byte[] bArr) {
        Log.d("TAG", "receiveResolutionList: " + bArr);
        ArrayList arrayList = new ArrayList();
        byte b = bArr[2];
        if (b == 1) {
            arrayList.add("Front Camera");
        } else if (b == 2) {
            arrayList.add("Front Camera");
            arrayList.add("Side Camera");
        } else if (b == 3) {
            arrayList.add("Front Camera");
            arrayList.add("Side Camera");
            arrayList.add("Dual View");
        }
        this.mResolutionCallBack.resolutionList(arrayList);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBattery(byte[] bArr) {
        byte b = bArr[2];
        Iterator<CameraListener> it = this.listeners.iterator();
        while (it.hasNext()) {
            it.next().receiveBatteryData(b);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateResolution(byte[] bArr) {
        byte b = bArr[2];
        String str = "Front Camera";
        if (b != 0) {
            if (b == 1) {
                str = "Side Camera";
            } else if (b == 2) {
                str = "Dual View";
            }
        }
        Iterator<CameraListener> it = this.listeners.iterator();
        while (it.hasNext()) {
            it.next().resolutionChanged(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConnectState(byte[] bArr) {
        this.loseCount = 0;
        if (bArr[3] == 69) {
            this.deviceType = "SA39E";
        } else if (bArr[3] == 65) {
            this.deviceType = "SA39A";
        } else if (bArr[3] == 87) {
            this.deviceType = "SA39W";
        } else if (bArr[3] == 83) {
            this.deviceType = "SA39S";
        } else if (bArr[3] == 77) {
            this.deviceType = "WF200";
        } else if (bArr[3] == 66) {
            this.deviceType = "SA39B";
        } else if (bArr[2] == 54 && bArr[3] == 48) {
            this.deviceType = "WF80D";
        } else {
            this.deviceType = "Unknown";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addPicData(byte[] bArr, int i) {
        int i2 = 0;
        byte b = bArr[0];
        byte b2 = bArr[1];
        byte b3 = bArr[2];
        int i3 = this.g_jpeg_len;
        int i4 = i - 8;
        this.len = i4;
        if (this.g_id == b) {
            System.arraycopy(bArr, 8, this.picData, i3, i4);
            int i5 = i3 + this.len;
            byte b4 = (byte) (this.g_pkt_cnt + 1);
            this.g_pkt_cnt = b4;
            if (b2 == 1) {
                if (b4 == b3) {
                    drawBitmap(i5, getAngle(bArr));
                }
                this.g_id = (byte) (this.g_id + 1);
                this.g_pkt_cnt = (byte) 0;
                i5 = 0;
            }
            if (i5 > this.picData.length - 1) {
                this.g_pkt_cnt = (byte) 0;
                this.g_id = (byte) 0;
            } else {
                i2 = i5;
            }
        } else {
            this.g_id = b;
            this.g_pkt_cnt = (byte) 0;
        }
        this.g_jpeg_len = i2;
    }

    private void drawBitmap(int i, int i2) {
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(this.picData, 0, i);
        if (decodeByteArray != null) {
            Iterator<CameraListener> it = this.listeners.iterator();
            while (it.hasNext()) {
                it.next().receiveImageData(decodeByteArray, i2);
            }
        }
        if (decodeByteArray == null) {
            Log.e("TAG", "localBitmap is null");
        }
    }

    private String intToString(int i) {
        return new StringBuffer().append((char) i).toString();
    }

    private int getAngle(byte[] bArr) {
        int i;
        int i2 = 0;
        for (int i3 = 0; i3 < 4; i3++) {
            i2 = (i2 << 8) | (bArr[i3 + 3] & 255);
        }
        int CONV_GS_INT = CONV_GS_INT(i2 >> 20);
        int CONV_GS_INT2 = CONV_GS_INT(i2 >> 10);
        int CONV_GS_INT3 = CONV_GS_INT(i2 >> 0);
        String str = this.deviceType;
        if (str == "SA39W" || str == "SA39S") {
            i = -((int) ((Math.atan2(CONV_GS_INT, CONV_GS_INT2) / 3.141592653589793d) * 1800.0d));
        } else {
            i = (int) ((Math.atan2(CONV_GS_INT3, CONV_GS_INT2) / 3.141592653589793d) * 1800.0d);
        }
        int atan2 = (int) ((Math.atan2(CONV_GS_INT, Math.sqrt((CONV_GS_INT2 * CONV_GS_INT2) + (CONV_GS_INT3 * CONV_GS_INT3))) / 3.141592653589793d) * 1800.0d);
        String str2 = this.deviceType;
        if (str2 == "SA39W") {
            return i / 10;
        }
        if (str2 == "SA39S") {
            return i / 10;
        }
        int i4 = atan2 / 10;
        if (i4 < -70 || i4 > 70) {
            return 0;
        }
        return i / 10;
    }
}