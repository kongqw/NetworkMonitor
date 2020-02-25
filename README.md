# AndroidNetworkMonitor

[![](https://jitpack.io/v/kongqw/NetworkMonitor.svg)](https://jitpack.io/#kongqw/NetworkMonitor)

> Android 全局网络变化监听

##  How to
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

``` glide
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

``` glide
dependencies {
        implementation 'com.github.kongqw:NetworkMonitor:1.0.0'
}
```

## 使用
### 初始化

在`Application`中初始化

``` kotlin
NetworkMonitorManager.getInstance().init(this)
```

### 注册

在需要监听网络状态的页面进行注册

``` kotlin
NetworkMonitorManager.getInstance().register(this)
```

### 反注册

``` kotlin
NetworkMonitorManager.getInstance().unregister(this)
```

### 监听网络状态变化

``` kotlin
@NetworkMonitor
fun onNetWorkStateChange(networkState: NetworkState) {
    when (networkState) {
        NetworkState.NONE -> {
            // TODO 暂无网络 
        }
        NetworkState.WIFI -> {
            // TODO WIFI网络 
        }
        NetworkState.CELLULAR -> {
            // TODO 蜂窝网络 
        }
    }
}
```

也可以监听指定网络状态，例如

``` kotlin
@NetworkMonitor(monitorFilter = [NetworkState.NONE])
fun onNetWorkStateChangeNONE(networkState: NetworkState) {
    // TODO 网络断开时回调
}
```

``` kotlin
@NetworkMonitor(monitorFilter = [NetworkState.WIFI])
fun onNetWorkStateChange1(networkState: NetworkState) {
    // TODO WIFI连接上的时候回调
}
```

``` kotlin
@NetworkMonitor(monitorFilter = [NetworkState.WIFI, NetworkState.CELLULAR])
fun onNetWorkStateChange2(networkState: NetworkState) {
    // TODO 连接上WIFI或蜂窝网络的时候回调
}
```
