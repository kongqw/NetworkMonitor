package com.kongqw.networkmonitor

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kongqw.network.monitor.NetworkMonitorManager
import com.kongqw.network.monitor.enums.NetworkState
import com.kongqw.network.monitor.interfaces.NetworkMonitor
import kotlinx.android.synthetic.main.activity_main.*

open class BaseActivity: AppCompatActivity() {

    companion object {
        private val TAG = BaseActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkMonitorManager.getInstance().register(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        NetworkMonitorManager.getInstance().unregister(this)
    }



    @NetworkMonitor
    fun onNetWorkStateChange(networkState: NetworkState) {
        Log.i(TAG, "onNetWorkStateChange  networkState = $networkState")
        when (networkState) {
            NetworkState.NONE -> {
                Toast.makeText(applicationContext, "暂无网络", Toast.LENGTH_SHORT).show()
                tv_network_state?.text = "当前网络类型：暂无网络"
            }
            NetworkState.WIFI -> {
                Toast.makeText(applicationContext, "WIFI网络", Toast.LENGTH_SHORT).show()
                tv_network_state?.text = "当前网络类型：WIFI网络"
            }
            NetworkState.CELLULAR -> {
                Toast.makeText(applicationContext, "蜂窝网络", Toast.LENGTH_SHORT).show()
                tv_network_state?.text = "当前网络类型：蜂窝网络"
            }
        }
    }
}