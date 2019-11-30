
# 可同时支持多个串口 Android和硬件通信使用的串口库 react-native-multi-serial-port

## 原生库 使用的是 志勇大神写的串口工具库 [项目地址]:[https://github.com/licheedev/Android-SerialPort-API]

## Getting started

1. `$ npm install react-native-multi-serial-port --save`
2. `$ react-native link react-native-multi-serial-port`
3. 在Android 目录下 的`build.gradle`文件里面的 `repositories`
   增加 `maven { url 'https://jitpack.io' }` 如下
   ```sh
    allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven {
            // All of React Native (JS, Obj-C sources, Android binaries) is installed from npm
            url "$rootDir/../node_modules/react-native/android"
            }
      maven { url 'https://jitpack.io' }
      }
	}
   ```
4. 在AndraidMainifest.xml 文件中 将  ` android:allowBackup="false"` 改成`android:allowBackup="true"`

## 使用方法
```javascript
import MultiSerialPort from 'react-native-multi-serial-port';

// TODO: What to do with the module?
MultiSerialPort;
```

1. 获取可用的软件编程节点
```javascript
import MultiSerialPort from 'react-native-multi-serial-port';

// TODO: 获取设备的路径列表
 MultiSerialPort.getAllDevicesPath((result) => {
                console.log(result); 
          });
 ```
2. 打开串口 可同时打开多个串口
```javascript
import MultiSerialPort from 'react-native-multi-serial-port';

// TODO: 打开串口1
MultiSerialPort.openSerialPort('/dev/ttySO', '9600');

// TODO: 打开串口2
MultiSerialPort.openSerialPort('/dev/ttyS1', '115200');

```
3. 指定串口发送数据 
```javascript
import MultiSerialPort from 'react-native-multi-serial-port';

let byteData1 = [0x00,0x01,0x02,0x03,0x05]
let byteData2 = [0x00,0x01,0x02,0x03,0x05]

// TODO: 串口1 发送数据
MultiSerialPort.sendByteData('/dev/ttyS1',byteData1);

// TODO: 串口2 发送数据
MultiSerialPort.sendByteData('/dev/ttyS2',byteData2);

```

3. 监听串口的状态 和 监听串口回传数据
```javascript
import MultiSerialPort from 'react-native-multi-serial-port';

     DeviceEventEmitter.addListener('onSerialPortRecevieData', this.onSerialPortRecevieData, this)
    //监听接收串口开关的状态
	DeviceEventEmitter.addListener('onSerialPortOpenStatus'，this.onSerialPortOpenStatus, this)

	    //监听串口的状态
    onSerialPortOpenStatus(resStatus) {
        let isSucess = resStatus.isSucess; // 是否开启成功
        let linuxDevPath = resStatus.linuxDevPath; //开启的串口
        //处理逻辑
	}
	
    // 监听串口回传数据
    onSerialPortRecevieData(receiveData) {
        console.log("onSerialPortRecevieData");
        let linuxDevPath = receiveData.linuxDevPath;// 可以根据这个来判断是哪个串口返回来的数据
        let serialPortReceiveData = receiveData.valueArray // 指定串口返回的数据
        // 处理接收的数据
    }

```
