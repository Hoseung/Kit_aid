/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tensorflow.lite.examples.objectdetection
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * A ViewModel for encapsulating the data for a QR Code, including the encoded data, the bounding
 * box, and the touch behavior on the QR Code.
 *
 * As is, this class only handles displaying the QR Code data if it's a URL. Other data types
 * can be handled by adding more cases of Barcode.TYPE_URL in the init block.
 */
class QrCodeViewModel(barcode: Barcode) {
    var boundingRect: Rect = barcode.boundingBox!!
    var qrContent: String = ""
    var qrCodeTouchCallback = { v: View, e: MotionEvent -> false} //no-op

    init {
        println("barcode.valueType ${barcode.valueType} + ${Barcode.TYPE_TEXT}")
        when (barcode.valueType) {
            Barcode.TYPE_TEXT -> {
                qrContent = barcode.displayValue.toString()
                println("qrcontent: ${qrContent}")
                qrCodeTouchCallback = { v: View, e: MotionEvent ->

                    if (e.action == MotionEvent.ACTION_DOWN && boundingRect.contains(e.getX().toInt(), e.getY().toInt())) {
                        val words = qrContent.split(" ")
                        println("234234234")
                        if(words.size == 5 && words[0].lowercase() == "proteometech"){
                            MyEntryPoint.prefs.setString("prodName", words[1])
                            MyEntryPoint.prefs.setString("lotNum", words[2].replace("BIG", ""))
                            MyEntryPoint.prefs.setString("Date", words[3])
                            MyEntryPoint.prefs.setString("hash", words[4])

                            println("xxxxxxxxxx ${words[0]} ${words[1]} ${words[2]} ${words[3]} ${words[4]}")

                            // download if model is missing
                            MyEntryPoint.prefs.setString("removeQR", "true")
                            val cameraIntent = Intent(v.context, CameraActivity::class.java)
                            v.context.startActivity(cameraIntent)
                        }
                        else{
                            qrContent = "Cannot parse: $qrContent"
                        }
//                        val openBrowserIntent = Intent(Intent.ACTION_VIEW)
//                        openBrowserIntent.data = Uri.parse(qrContent)
//                        v.context.startActivity(openBrowserIntent)
                    }
                    true // return true from the callback to signify the event was handled
                }
            }
            // Not needed. let me temporarily keep it for debugging purpose.
//            Barcode.TYPE_URL -> {
//                qrContent = barcode.url!!.url!!
//                qrCodeTouchCallback = { v: View, e: MotionEvent ->
//                    if (e.action == MotionEvent.ACTION_DOWN && boundingRect.contains(e.getX().toInt(), e.getY().toInt())) {
//                        val openBrowserIntent = Intent(Intent.ACTION_VIEW)
//                        openBrowserIntent.data = Uri.parse(qrContent)
//                        v.context.startActivity(openBrowserIntent)
//                    }
//
//
//                    true // return true from the callback to signify the event was handled
//                }
//            }
            // Add other QR Code types here to handle other types of data,
            // like Wifi credentials.
            else -> {
                qrContent = "Unsupported data type: ${barcode.rawValue.toString()}"
            }
        }
    }
}