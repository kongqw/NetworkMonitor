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

class NetworkMonitorManager private constructor() : ConnectivityManager.NetworkCallback() {

    companion object {

        private const val ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"

        @JvmStatic
        private var INSTANCE: NetworkMonitorManager? = null

        @JvmStatic
        fun getInstance(): NetworkMonitorManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: NetworkMonitorManager().also { INSTANCE = it }
        }
    }

    private var mApplication: Application? = null
    private var mNetworkBroadcastReceiver = NetworkBroadcastReceiver()
    private var netWorkStateChangedMethodMap: HashMap<Any, ArrayList<NetworkStateReceiverMethod>> = HashMap()
    private val mUiHandler = Handler(Looper.getMainLooper())

    fun init(application: Application) {
        mApplication = application
        initMonitor(application)
    }

    private fun initMonitor(application: Application) {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager?

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                connectivityManager?.registerDefaultNetworkCallback(this)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                connectivityManager?.registerNetworkCallback(NetworkRequest.Builder().build(), this)
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

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        postNetworkState(NetworkStateUtils.getNetworkState(mApplication?.applicationContext))
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        postNetworkState(NetworkState.NONE)
        // postNetworkState(NetworkStateUtils.getNetworkState(mApplication?.applicationContext))
    }


    inner class NetworkBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ANDROID_NET_CHANGE_ACTION -> {
                    postNetworkState(NetworkStateUtils.getNetworkState(mApplication?.applicationContext))
                }
            }
        }
    }

    private fun postNetworkState(networkState: NetworkState) {
        for ((_, methods) in netWorkStateChangedMethodMap) {
            methods.forEach { networkStateReceiverMethod ->
                if (true == networkStateReceiverMethod.monitorFilter?.contains(networkState)) {
                    mUiHandler.post { networkStateReceiverMethod.method?.invoke(networkStateReceiverMethod.any, networkState) }
                }
            }
        }
    }
}