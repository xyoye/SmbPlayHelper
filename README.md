# SmbPlayHelper #

Android端通过创建本地服务实现局域网内视频播放，相关技术实现的介绍可查看[《关于实现局域网内视频播放》](https://xyoye.github.io/2019/07/25/2019-7-25-%E5%85%B3%E4%BA%8E%E5%AE%9E%E7%8E%B0%E5%B1%80%E5%9F%9F%E7%BD%91%E5%86%85%E8%A7%86%E9%A2%91%E6%92%AD%E6%94%BE/)

1. 从[弹弹Player 概念版](https://github.com/xyoye/DanDanPlayForAndroid)中抽离出来的局域网播放功能，通过创建本地服务，将Smb文件转换成Http协议传输文件流形式实现局域网内视频的播放。

2. 项目中使用[smbj-rpc](https://github.com/rapid7/smbj-rpc)、[smbj](https://github.com/hierynomus/smbj)、[jcifs-ng](https://github.com/AgNO3/jcifs-ng)、[jcifs](https://www.jcifs.org/)实现局域网文件浏览，理论上支持SmbV1、SmbV2、SmbV3，项目没有播放器的实现，仅作为服务端，可选取手机上已安装的视频播放播放视频。

## 截图 ##

<div>
	<img src="https://github.com/xyoye/SmbPlayHelper/blob/master/Screenshot/Screenshot_01.jpg" width="200px">
	<img src="https://github.com/xyoye/SmbPlayHelper/blob/master/Screenshot/Screenshot_02.jpg" width="200px">
	<img src="https://github.com/xyoye/SmbPlayHelper/blob/master/Screenshot/Screenshot_03.jpg" align="top" height="200px">
</div>
