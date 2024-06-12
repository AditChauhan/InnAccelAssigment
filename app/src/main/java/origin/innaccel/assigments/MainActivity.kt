package origin.innaccel.assigments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.content.ContextCompat
import origin.innaccel.assigments.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       val  videoView = binding.videoView

        val videoUri = "android.resource://" + packageName + "/" + R.raw.promotion_video
        videoView.setVideoURI(Uri.parse(videoUri))
        videoView.setOnPreparedListener { mp -> mp.isLooping = true }
        videoView.start()

        // Start the background service
        val intent = Intent(this, DataProcessingService::class.java)
        startService(intent)
        val filter = IntentFilter("assignment.UPDATE_CHANNEL_VALUES")
        ContextCompat.registerReceiver(this, updateChannelValuesReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
    }

    fun updateChannelValues(channelValues: List<Double>) {
        runOnUiThread {
            binding.channel1TextView.text = channelValues[0].toString()
            binding.channel2TextView.text = channelValues[1].toString()
            binding.channel3TextView.text = channelValues[2].toString()
            binding.channel4TextView.text = channelValues[3].toString()
        }
    }



    private val updateChannelValuesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val channelValues = intent?.getDoubleArrayExtra("channelValues")?.toList() ?: return
            updateChannelValues(channelValues)
        }
    }

}