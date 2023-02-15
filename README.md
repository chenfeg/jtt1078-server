# jtt1078-server

java版本的1078音视频服务

----
## 说明

* 1 基于springboot的websocket服务，对接手机终端；
* 2 基于netty的JT/T1078协议的tcp服务，用于视频机设备的接入与音视频数据传输与控制；
* 3 将接收到的h264音视频流解析并转为http-flv，提供给http服务；
* 4 通过ffmpeg将音视频流推到srs4服务器上；
* 5 利用开源srs4为手机和web提供音视频直播与点播服务；

