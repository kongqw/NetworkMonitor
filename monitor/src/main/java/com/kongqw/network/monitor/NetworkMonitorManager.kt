package com.kongqw.network.monitor

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.kongqw.network.monitor.enums.NetworkState
import com.kongqw.network.monitor.util.AnnotationUtils
import com.kongqw.network.monitor.util.NetworkStateUtils
import java.util.*

class NetworkMonitorManager private constructor() {

    companion object {

        private const val ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"

        // 默认网络防抖时间
        private const val DEFAULT_JITTER_TIME: Long = 1_500

        @JvmStatic
        private var INSTANCE: NetworkMonitorManager? = null

        @JvmStatic
        fun getInstance(): NetworkMonitorManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: NetworkMonitorManager().also { INSTANCE = it }
        }
    }

    private var mApplication: Application? = null
    private var mJitterTime: Long = DEFAULT_JITTER_TIME
    private var mNetworkBroadcastReceiver = NetworkBroadcastReceiver()
    private var mNetworkCallback = NetworkCallback()
    private var netWorkStateChangedMethodMap: HashMap<Any, ArrayList<NetworkStateReceiverMethod>> = HashMap()
    private val mUiHandler = Handler(Looper.getMainLooper())

    private var mLastNetworkState: NetworkState? = null
    private var mRunnable: Runnable? = null

    /**
     * 初始化
     * @param application 上下文
     * @param jitterTime 设置抖动时间
     */
    fun init(application: Application, jitterTime: Long = DEFAULT_JITTER_TIME) {
        mApplication = application
        mJitterTime = jitterTime.let {
            if (it < 0) {
                return@let 0
            }
            return@let it
        }
        initMonitor(application)
    }

    private fun initMonitor(application: Application) {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager?

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                connectivityManager?.registerDefaultNetworkCallback(mNetworkCallback)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                connectivityManager?.registerNetworkCallback(NetworkRequest.Builder().build(), mNetworkCallback)
            }
            else -> {
                val intentFilter = IntentFilter().apply { addAction(ANDROID_NET_CHANGE_ACTION) }
                application.registerReceiver(mNetworkBroadcastReceiver, intentFilter)
            }
        }
    }

    fun register(any: Any?) {
        any?.apply { netWorkStateChangedMethodMap[this] = AnnotationUtils.findAnnotationMethod(this) }
    }

    fun unregister(any: Any?) {
        any?.apply { netWorkStateChangedMethodMap.remove(this) }
    }

    internal inner class NetworkCallback : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postNetworkState(NetworkStateUtils.getNetworkState(mApplication?.applicationContext))
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postNetworkState(NetworkState.NONE)
        }
    }

    internal inner class NetworkBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ANDROID_NET_CHANGE_ACTION -> {
                    postNetworkState(NetworkStateUtils.getNetworkState(mApplication?.applicationContext))
                }
            }
        }
    }


    private fun postNetworkState(networkState: NetworkState) {
        // Log.i("NetworkMonitorManager", "postNetworkState($networkState)")
        if (mLastNetworkState == networkState) {
            // Log.i("NetworkMonitorManager", "已经回调过该状态，不再多次回调")
            return
        }

        mRunnable?.also { mUiHandler.removeCallbacks(it) }


        mRunnable = Runnable {
            for ((_, methods) in netWorkStateChangedMethodMap) {

                methods.forEach { networkStateReceiverMethod ->
                    if (true == networkStateReceiverMethod.monitorFilter?.contains(networkState)) {
                        networkStateReceiverMethod.method?.invoke(networkStateReceiverMethod.any, networkState)
                        // 记录最后一次回调的网络状态
                        mLastNetworkState = networkState
                    }
                }
            }
        }

        when (networkState) {
            NetworkState.NONE -> {
                mRunnable?.also {
                    mUiHandler.postDelayed(it, mJitterTime)
                }
            }
            else -> {
                mRunnable?.also {
                    mUiHandler.postDelayed(it, mJitterTime)
                }
            }
        }
    }
}