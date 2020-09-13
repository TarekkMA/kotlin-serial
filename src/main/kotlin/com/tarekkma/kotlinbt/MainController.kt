package com.tarekkma.kotlinbt

import com.fazecast.jSerialComm.SerialPort
import com.sun.javafx.collections.ObservableListWrapper
import javafx.concurrent.Task
import tornadofx.*

class MainController : Controller() {
    var state = MainViewState()
    var readTask: Task<Unit>? = null;


    init {
        state.selectedSerialPortProperty
    }

    fun discoverPorts() {
        val ports = SerialPort.getCommPorts()
        state.serialPortsProperty.set(ObservableListWrapper(ports.toList()))
        addToCommHistory("DISCOVERY", "Found ${ports.size} available serial ports.")
    }

    fun addToCommHistory(tag: String, message: String) {
        state.communicationHistoryProperty.add("[${tag}] $message")
    }


    fun connect() {
        if (!state.connected) {
            if (state.selectedSerialPort == null) {
                addToCommHistory("ERROR", "You have to select a port first")
                return;
            }
            if (state.baudRate == -1) {
                addToCommHistory("ERROR", "baudRate is not valid")
                return;
            }
            if (state.numDataBits == -1) {
                addToCommHistory("ERROR", "numDataBits is not valid")
                return;
            }
            if (state.parity == -1) {
                addToCommHistory("ERROR", "parity is not valid")
                return;
            }
            if (state.numStopBits == -1) {
                addToCommHistory("ERROR", "numStopBits is not valid")
                return;
            }
            connectToPort(
                state.selectedSerialPort,
                state.baudRate,
                state.numDataBits,
                state.parity,
                state.numStopBits
            )
        } else {
            closePort(state.selectedSerialPort)
        }
    }


    private fun connectToPort(
        port: SerialPort,
        baudRate: Int,
        numDataBits: Int,
        parity: Int,
        numStopBits: Int
    ) {
        addToCommHistory("CONNECTION", "Connected to port $port")
        state.connected = true;
        readTask = task {
            runLater {
                addToCommHistory("TASK", "Read task for $port was created")
            }
            try {
                port.openPort()
                port.baudRate = baudRate;
                port.numDataBits = numDataBits
                port.parity = parity
                port.numStopBits = numStopBits
                mainl@ while (isCancelled.not()) {
                    while (port.bytesAvailable() == 0) {
                        Thread.sleep(20)
                        if (isCancelled) break@mainl;
                    }
                    val readBuffer = ByteArray(port.bytesAvailable())
                    val numRead: Int = port.readBytes(readBuffer, readBuffer.size.toLong())
                    runLater {
                        addToCommHistory("<<<", "Received $numRead bytes.")
                        addToCommHistory("<<<", "UTF-8: ${readBuffer.decodeToString()}")
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                runLater {
                    addToCommHistory("ERROR", "Exception in read task:${e}")
                    addToCommHistory("ERROR Details", e.stackTraceToString())
                }

            }
            runLater {
                closePort(port)
            }
        }
    }

    private fun closePort(port: SerialPort) {
        if (readTask != null && !readTask!!.isCancelled) {
            readTask!!.cancel()
            readTask!!.cancel {
                addToCommHistory("TASK", "Read task for $port was canceled")
                if (port.isOpen) {
                    port.closePort()
                }
                addToCommHistory("CONNECTION", "Disconnected from port $port")
                state.connected = false;
            }
        }
    }

    fun send() {
        if (!state.connected) {
            addToCommHistory("ERROR", "Can't send port is closed or not yet connected.")
            return
        }
        val bytesToSend = state.dataToSend.toByteArray();
        state.selectedSerialPort.writeBytes(bytesToSend, bytesToSend.size.toLong())
        addToCommHistory(">>>", "sent ${bytesToSend.size} bytes.")
        addToCommHistory(">>>", "UTF-8: ${bytesToSend.decodeToString()}")
        state.dataToSend = ""
    }
}