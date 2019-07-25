# SmbPlayHelper #

Android端通过创建本地服务实现局域网内视频播放，相关技术实现的介绍可查看[《关于实现局域网内视频播放》](https://xyoye.github.io/2019/07/25/关于实现局域网内视频播放.html)

1. 从[弹弹Player 概念版](https://github.com/xyoye/DanDanPlayForAndroid)中抽离出来的局域网播放功能，通过创建本地服务，将Smb文件转换成Http协议传输文件流形式实现局域网内视频的播放。

2. 项目中没有播放器的实现，仅作为服务端，可选取手机上已安装的视频播放播放视频。

## 截图 ##

<div>
	<img src="https://github.com/xyoye/SmbPlayHelper/blob/master/Screenshot/Screenshot_01.jpg" width="200px">
	<img src="https://github.com/xyoye/SmbPlayHelper/blob/master/Screenshot/Screenshot_02.jpg" width="200px">
	<img src="https://github.com/xyoye/SmbPlayHelper/blob/master/Screenshot/Screenshot_03.jpg" align="top" height="200px">
</div>
