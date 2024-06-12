package origin.innaccel.assigments

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class DataProcessingService : Service() {

    private val executor = Executors.newFixedThreadPool(4)
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var lineNumber = 0
    private var sampleCounter = 0
    private var latestChannelValues: List<Double> = listOf(0.0, 0.0, 0.0, 0.0)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            readFile()
        }.start()
        scheduler.scheduleAtFixedRate({
            sendChannelValues()
        }, 0, 100, TimeUnit.MILLISECONDS)

        return START_STICKY
    }

    private fun readFile() {
        val inputStream = resources.openRawResource(R.raw.input_data)
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.useLines { lines ->
            lines.forEach { line ->
                lineNumber++
                val samples = parseLine(line)
                processSamples(samples)
            }
        }
    }

    private fun parseLine(line: String): List<String> {
        return line.split("!")
            .filter { it.isNotBlank() }
            .map { it.trim() }
    }

    private fun processSamples(samples: List<String>) {
        samples.forEach { sample ->
            executor.execute {
                try {
                    val channelValues = decodeSample(sample)
                    synchronized(this) {
                        latestChannelValues = channelValues
                        sampleCounter++
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun decodeSample(sample: String): List<Double> {
        val channelValues = mutableListOf<Double>()
        val pattern = Regex("([0-9A-Fa-f]{6})")

        val matches = pattern.findAll(sample)
        if (matches.count() < 4) {
            while (channelValues.size < 4) {
                channelValues.add(0.0)
            }
        } else {
            matches.forEach { match ->
                channelValues.add(hexToDouble(match.value))
            }
        }
        while (channelValues.size < 4) {
            channelValues.add(0.0)
        }

        return channelValues
    }

    private fun hexToDouble(hex: String): Double {
        return try {
            java.lang.Long.parseLong(hex, 16).toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    private fun sendChannelValues() {
        synchronized(this) {
            if (sampleCounter > 0) {
                val intent = Intent("assignment.UPDATE_CHANNEL_VALUES")
                intent.putExtra("channelValues", latestChannelValues.toDoubleArray())
                sendBroadcast(intent)
                sampleCounter = 0
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
