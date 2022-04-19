package com.kongqw.network.monitor.util

import com.kongqw.network.monitor.NetworkStateReceiverMethod
import com.kongqw.network.monitor.enums.NetworkState
import com.kongqw.network.monitor.interfaces.NetworkMonitor

internal object AnnotationUtils {

    fun findAnnotationMethod(any: Any): ArrayList<NetworkStateReceiverMethod> {
        return findRegisterMethods(any, any.javaClass)
    }

    private fun findRegisterMethods(any: Any, clazz: Class<Any>): ArrayList<NetworkStateReceiverMethod> {
        val receiverMethodList = ArrayList<NetworkStateReceiverMethod>()

        // 获取注册类中所有的方法
        val declaredMethods = clazz.declaredMethods
        // 遍历注册类中的所有方法
        declaredMethods.forEach { method ->
            kotlin.run method@{
                // 获取方法参数
                val parameterTypes = method.parameterTypes
                if (1 != parameterTypes.size) {
                    return@method
                }
                if (parameterTypes[0].name != NetworkState::class.java.name) {
                    return@method
                }

                method.annotations.forEach {
                    if (it.annotationClass == NetworkMonitor::class) {
                        val networkMonitor: NetworkMonitor? = method.getAnnotation(NetworkMonitor::class.java)
                        val monitorFilter = networkMonitor?.monitorFilter
                        receiverMethodList.add(NetworkStateReceiverMethod(any, method, monitorFilter))
                    }
                }
            }
        }
        // 检索父类
        clazz.superclass?.apply {
            // 检索父类中注册方法
            val findAnnotationMethod = findRegisterMethods(any, this)
            receiverMethodList.addAll(findAnnotationMethod)
        }
        return receiverMethodList
    }
}