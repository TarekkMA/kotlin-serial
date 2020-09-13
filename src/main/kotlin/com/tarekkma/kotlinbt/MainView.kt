package com.tarekkma.kotlinbt

import com.fazecast.jSerialComm.SerialPort
import javafx.beans.property.*
import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import tornadofx.*


class MainViewState(
    connected: Boolean = false,
    ports: List<SerialPort> = emptyList(),
    communicationHistory: List<String> = emptyList()
) {
    val connectedProperty = SimpleBooleanProperty(connected)
    var connected by connectedProperty

    val serialPortsProperty = SimpleListProperty(ports.toObservable())
    var serialPorts by serialPortsProperty

    val communicationHistoryProperty = SimpleListProperty(communicationHistory.toObservable())
    var communicationHistory by communicationHistoryProperty


    val selectedSerialPortProperty = SimpleObjectProperty<SerialPort>()
    var selectedSerialPort by selectedSerialPortProperty

    val dataToSendProperty = SimpleStringProperty()
    var dataToSend by dataToSendProperty


    val baudRateProperty = SimpleIntegerProperty(115200)
    var baudRate by baudRateProperty

    val numDataBitsProperty = SimpleIntegerProperty(8)
    var numDataBits by numDataBitsProperty

    val parityProperty = SimpleIntegerProperty(0)
    var parity by parityProperty

    val numStopBitsProperty = SimpleIntegerProperty(1)
    var numStopBits by numStopBitsProperty

}

fun IntegerProperty.toStringProperty(): StringProperty {
    val stringProperty = SimpleStringProperty(this.value.toString())
    this.onChange {
        stringProperty.set(it.toString())
    }
    stringProperty.onChange {
        this.set(it?.trim()?.toIntOrNull() ?: -1)
    }
    return stringProperty
}


class MainView : View("Serial Port Terminal") {

    private val controller: MainController by inject();
    private val state: MainViewState
        get() = controller.state

    override val root = vbox {
        titledpane("Connection", collapsible = false) {
            hbox {
                pane {
                    useMaxWidth = true
                    hgrow = Priority.ALWAYS
                    maxWidth = Double.MAX_VALUE
                    enableWhen(state.connectedProperty.not())
                    vbox {
                        prefWidthProperty().bind(this@pane.widthProperty())
                        hbox {
                            button("Discover") {
                                action {
                                    controller.discoverPorts()
                                }
                            }
                            combobox<SerialPort>(state.selectedSerialPortProperty, state.serialPortsProperty) {
                                hgrow = Priority.ALWAYS
                                maxWidth = Double.MAX_VALUE
                            }
                        }
                        hbox {
                            vbox {
                                hgrow = Priority.ALWAYS
                                label("baud rate")
                                textfield {
                                    textProperty().bindBidirectional(state.baudRateProperty.toStringProperty())
                                }
                            }
                            vbox {
                                hgrow = Priority.ALWAYS
                                label("data bits")
                                textfield(state.numDataBitsProperty.toStringProperty())
                            }
                            vbox {
                                hgrow = Priority.ALWAYS
                                label("parity bits")
                                textfield(state.parityProperty.toStringProperty())
                            }
                            vbox {
                                hgrow = Priority.ALWAYS
                                label("stop bits")
                                textfield(state.numStopBitsProperty.toStringProperty())
                            }
                        }
                    }
                }
                button {
                    textProperty().bind(state.connectedProperty.stringBinding {
                        if (it == true) "Disconnect" else "Connect"
                    })
                    prefWidth = 100.0
                    action {
                        controller.connect()
                    }
                }
            }
        }
        titledpane("Communication History", collapsible = false) {
            useMaxSize = true
            vgrow = Priority.ALWAYS
            textarea {
                maxWidth = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
                useMaxSize = true
                isEditable = false
                textProperty().bind(state.communicationHistoryProperty.stringBinding { it?.joinToString("\n") }
                    .onChange {
                        runLater {
                            scrollTop = Double.MAX_VALUE
                        }
                    })

            }
        }
        titledpane("Send Data", collapsible = false) {
            enableWhen(state.connectedProperty)
            hbox {
                textfield(state.dataToSendProperty) {
                    hgrow = Priority.ALWAYS
                }
                button("send") {
                    action {
                        controller.send()
                    }
                }
            }
        }
        label("Serial communication test by TarekMA") {
            textAlignment = TextAlignment.CENTER
            vgrow = Priority.ALWAYS
            alignment = Pos.CENTER
            maxWidth = Double.MAX_VALUE
        }
    }

}
