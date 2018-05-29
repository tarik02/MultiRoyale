package com.tarik02.multiroyale

import android.os.AsyncTask
import java.io.InputStreamReader
import kotlin.system.exitProcess

class StartProfileTask(private val script: String, private val profile: String) : AsyncTask<Unit, Unit, Unit>() {
    override fun doInBackground(vararg p0: Unit) {
        val rt = Runtime.getRuntime()
        val process = rt.exec(arrayOf("su", "-c", "sh", script, profile))

        process.waitFor()

        val reader = InputStreamReader(process.inputStream)
        println(reader.readText())
    }

    override fun onPostExecute(result: Unit?) {
        exitProcess(0)
    }
}