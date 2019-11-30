package com.serialport;

import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.serialport.serialportlib.DLCSerialPortUtil;
import com.serialport.serialportlib.ReceiveCallback;
import com.serialport.serialportlib.SerialPortManager;

import java.util.ArrayList;


public class MultiSerialPortModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static final String onSerialPortOpenStatus  = "onSerialPortOpenStatus";//判断串口是否打开成功通知
    private static final String onSerialPortRecevieData  = "onSerialPortRecevieData";//判断串口是否打开成功通知

    private DLCSerialPortUtil portutil;
    private ArrayList<SerialPortManager> mPortManagers = new ArrayList<>();



    public MultiSerialPortModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        portutil = DLCSerialPortUtil.getInstance();

    }

    @Override
    public String getName() {
        return "MultiSerialPort";
    }



    @ReactMethod
    public void show(String message, int duration) {
        Toast.makeText(getReactApplicationContext(), message, duration).show();
    }

    /*自定义原生和RN通讯方法*/
    /**
     * 获取串口列表 方式
     * rn调用Native,并获取返回值
     *
     * @param callback
     */
    @ReactMethod
    public void getAllDevicesPath(Callback callback) {
        String[] paths = DLCSerialPortUtil.getInstance().getAllDevicesPath();
        // .回调RN,即将处理串口列表返回给RN
//    Toast.makeText(this.reactContext ,result,Toast.LENGTH_SHORT).show();
        WritableArray pathArray = Arguments.createArray();
        for (int i = 0; i <paths.length ; i++) {
            String path = paths[i];
            pathArray.pushString(path);

        }
        callback.invoke(pathArray);
    }

    /**
     * 打开串口
     * rn调用Native,并获取返回值
     * @param portStr 串口路径
     * @param Baudrates 波特率
     */
    @ReactMethod
    public  void openSerialPort(String portStr,String Baudrates ){
        WritableMap params = Arguments.createMap();
        params.putString("linuxDevPath",portStr);

        for (SerialPortManager portManager : mPortManagers) {
            String devicePath = portManager.getDevicePath();
            if (devicePath != null && portStr.equals(devicePath)) {
                params.putBoolean("isSucess",false);
                params.putString("msg","找不到串口:"+portStr);

                this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(onSerialPortOpenStatus,params);
                return;
            }
        }

        SerialPortManager manager = DLCSerialPortUtil.getInstance().open(portStr,Baudrates);
        if (manager == null){
            params.putBoolean("isSucess",false);
            params.putString("msg","找不到串口:"+portStr);
            this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(onSerialPortOpenStatus,params);
            return;
        }
        boolean isSucess = manager.isOpenSuccess();
        if ( isSucess){
            //打开串口成功
            params.putBoolean("isSucess",true);
            params.putString("msg","打开串口成功:"+portStr);
            this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(onSerialPortOpenStatus,params);            manager.setReceiveCallback(new ReceiveCallback() {
                @Override
                public void onReceive(String devicePath, String baudrateString, byte[] received, int size) {
                    WritableMap params = Arguments.createMap();
                    WritableArray receiveArray = Arguments.createArray();
                    for (int i = 0; i < size; i++) {
                        byte cmdSingle = received[i];
                        int singleInt = cmdSingle & 0xFF;
                        receiveArray.pushInt(singleInt);
                    }
                    params.putString("linuxDevPath", devicePath);
                    params.putArray("valueArray", receiveArray);
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(onSerialPortRecevieData, params);
                }
            });
            mPortManagers.add(manager);
        }else {
            //打开串口失败
            params.putBoolean("isSucess",false);
            params.putString("msg","打开串口失败:"+portStr);
            this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(onSerialPortOpenStatus,params);
        }


    }





    // 数据接收回调
    private ReceiveCallback mPortManagerCallback = new ReceiveCallback() {
        @Override
        public void onReceive(String devicePath, String baudrateString, byte[] received, int size) {
            WritableMap params = Arguments.createMap();
            WritableArray receiveArray = Arguments.createArray();
            for (int i = 0; i < size; i++) {
                byte cmdSingle = received[i];
                int singleInt = cmdSingle & 0xFF;
                receiveArray.pushInt(singleInt);
            }
            params.putString("linuxDevPath", devicePath);
            params.putArray("valueArray", receiveArray);
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(onSerialPortRecevieData, params);
        }
    };



    private SerialPortManager pickPortManager(String portStr) {
        for (SerialPortManager portManager : mPortManagers) {
            String devicePath = portManager.getDevicePath();
            if (devicePath != null && portStr.equals(devicePath)) {
                return portManager;
            }
        }
        return null;
    }


    /**
     * 发送byte 字节 方式
     * rn调用Native,
     * @param msg
     *
     */
    @ReactMethod
    public void sendByteData(String portStr, ReadableArray msg) throws Exception {
        int length = msg.size();
        byte [] cmd = new byte [length];
        for (int i = 0; i < length; i++) {
            int number =  msg.getInt(i);
            cmd[i] =(byte)number ;
        }
        SerialPortManager pickedPortManager = pickPortManager(portStr);
        pickedPortManager.sendData(cmd);
    }



    /**
     * 根据业务逻辑，增加移除监听
     * rn调用Native,
     *
     *
     */
    @ReactMethod
    public void removeReceiveCallback(String portStr) {
        SerialPortManager pickedPortManager = pickPortManager(portStr);
        if (pickedPortManager != null) {
            pickedPortManager.removeReceiveCallback();
        }
    }


    /**
     * 关闭串口
     * rn调用Native,
     *
     *
     */
    @ReactMethod
    public void close(String portStr) {
        SerialPortManager pickedPortManager = pickPortManager(portStr);
        if (pickedPortManager != null) {
            pickedPortManager.removeReceiveCallback();
            pickedPortManager.close();
            mPortManagers.remove(pickedPortManager);
        }
    }

    @ReactMethod
    public void doDestroy() {
        for (SerialPortManager portManager : mPortManagers) {
            String devicePath = portManager.getDevicePath();
            if (devicePath != null) {
                portManager.removeReceiveCallback();
                portManager.close();
                mPortManagers.remove(portManager);
            }
        }
    }


    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        this.doDestroy();
    }
}