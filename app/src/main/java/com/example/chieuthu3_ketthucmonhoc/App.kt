package com.example.chieuthu3_ketthucmonhoc

import android.app.Application
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator

/** Cấp application context cho tầng dữ liệu (SQLite/Room) trước khi Activity nào chạy. */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AppServiceLocator.init(this)
    }
}
