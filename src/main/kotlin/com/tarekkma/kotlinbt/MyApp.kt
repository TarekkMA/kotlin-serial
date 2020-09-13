package com.tarekkma.kotlinbt

import com.fazecast.jSerialComm.SerialPort
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import tornadofx.*
import kotlin.concurrent.thread

fun main() = launch<MyApp>()

class MyApp : App(MainView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        with(stage) {
            isResizable = true
            width = 800.0
            height = 600.0
        }
    }

}