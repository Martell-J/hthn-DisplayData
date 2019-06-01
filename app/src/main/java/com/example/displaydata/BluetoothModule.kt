package com.example.displaydata

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import kotlin.concurrent.fixedRateTimer

class BluetoothModule {

    var m_bluetoothAdapter: BluetoothAdapter
    val TICK_RATE = 2000
    lateinit var m_pairedDevices: Set<BluetoothDevice>

    val REQUEST_ENABLE_BLUETOOTH = 1

    companion object {
        val EXTRA_aDDRESS: String = "Device Address"

    }

    private fun handleConnectivity() {

        println("Test!")

    }




    constructor(context: AppCompatActivity) {

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter === null) {
            println("No bluetooth available")
            return

        }
        if(!m_bluetoothAdapter!!.isEnabled) {

            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        fixedRateTimer("default", false, 0L, TICK_RATE.toLong()){

            handleConnectivity()

        }

    }





}