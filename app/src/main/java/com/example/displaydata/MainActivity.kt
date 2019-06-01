package com.example.displaydata


import com.example.displaydata.R
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

import android.R.attr.label
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var myLabel: TextView
    lateinit var myTextbox: EditText
    private var mBluetoothAdapter: BluetoothAdapter? = null
    lateinit var mmSocket: BluetoothSocket
    lateinit var mmDevice: BluetoothDevice
    lateinit var mmOutputStream: OutputStream
    lateinit var mmInputStream: InputStream
    lateinit var workerThread: Thread
    lateinit var readBuffer: ByteArray
    var readBufferPosition: Int = 0
    internal var counter: Int = 0
    @Volatile
    internal var stopWorker: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val openButton = findViewById<Button>(R.id.open)
        val sendButton = findViewById<Button>(R.id.send)
        val closeButton = findViewById<Button>(R.id.close)
        myLabel = findViewById<TextView>(R.id.label)
        myTextbox = findViewById<EditText>(R.id.entry)

        //Open Button
        openButton.setOnClickListener {
            try {
                findBT()
                openBT()
            } catch (ex: IOException) {
                println(ex.message)
                print(ex.stackTrace)
            }
        }

        //Send Button
        sendButton.setOnClickListener {
            try {
                sendData()
            } catch (ex: IOException) {
            }
        }

        //Close button
        closeButton.setOnClickListener {
            try {
                closeBT()
            } catch (ex: IOException) {
            }
        }
    }

    private fun findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            myLabel.text = "No bluetooth adapter available"
        }

        if (!mBluetoothAdapter!!.isEnabled) {
            val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetooth, 0)
        }

        val pairedDevices = mBluetoothAdapter!!.bondedDevices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                println(device.name)
                if (device.name == "IMU2") {
                    mmDevice = device
                    println("Setting device name")
                    break
                }
            }
        }
        myLabel.text = "Bluetooth Device Found"
    }

    @Throws(IOException::class)
    internal fun openBT() {
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid)
        mmSocket.connect()
        mmOutputStream = mmSocket.outputStream
        mmInputStream = mmSocket.inputStream

        try {
            beginListenForData()
        } catch (ex: Exception) {

            println(ex.message)
            print(ex.stackTrace)

        }





        myLabel.text = "Bluetooth Opened"
    }

    internal fun beginListenForData() {
        val handler = Handler()
        val delimiter: Byte = 10 //This is the ASCII code for a newline character

        stopWorker = false
        readBufferPosition = 0
        readBuffer = ByteArray(1024)
        workerThread = Thread(Runnable {
            while (!Thread.currentThread().isInterrupted && !stopWorker) {
                try {
                    val bytesAvailable = mmInputStream.available()
                    if (bytesAvailable > 0) {
                        val packetBytes = ByteArray(bytesAvailable)
                        mmInputStream.read(packetBytes)
                        for (i in 0 until bytesAvailable) {
                            val b = packetBytes[i]
                            if (b == delimiter) {
                                val encodedBytes = ByteArray(readBufferPosition)
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.size)
                                val data = String(encodedBytes, charset("US-ASCII"))
                                readBufferPosition = 0

                                handler.post { myLabel.text = data }
                            } else {
                                readBuffer[readBufferPosition++] = b
                            }
                        }
                    }
                } catch (ex: IOException) {
                    stopWorker = true
                }

            }
        })

        workerThread.start()
    }

    @Throws(IOException::class)
    internal fun sendData() {
        var msg = myTextbox.text.toString()
        msg += "\n"
        mmOutputStream.write(msg.toByteArray())
        myLabel.text = "Data Sent"
    }

    @Throws(IOException::class)
    internal fun closeBT() {
        stopWorker = true
        mmOutputStream.close()
        mmInputStream.close()
        mmSocket.close()
        myLabel.text = "Bluetooth Closed"
    }
}