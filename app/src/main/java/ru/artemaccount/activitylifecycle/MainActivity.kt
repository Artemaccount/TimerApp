package ru.artemaccount.activitylifecycle

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import ru.artemaccount.activitylifecycle.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var timer: CountDownTimer
    private var isTicking = false
    private lateinit var shared: SharedPreferences
    private var defaultValue = 5


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shared = PreferenceManager.getDefaultSharedPreferences(this)
        defaultValue =
            getIntervalFromSettings(shared)

        initSeekbar(defaultValue)
        binding.timerValueTv.text = DateUtils.formatElapsedTime(defaultValue.toLong())

        binding.startButton.setOnClickListener {
            binding.apply {
                if (isTicking) {
                    seekbar.isEnabled = true
                    startButton.text = "Start!"
                    val time = DateUtils.formatElapsedTime(seekbar.progress.toLong())
                    timerValueTv.text = time
                    Log.d("myTag", "isTicking true")
                    timer.cancel()
                    isTicking = false
                } else {
                    seekbar.isEnabled = false
                    Log.d("myTag", "isTicking false")
                    val timeValue = seekbar.progress
                    Log.d("myTag", "timeValue $timeValue")
                    startButton.text = "Stop!"
                    startTimer(timeValue.toLong())
                    isTicking = true
                }
            }
        }
        shared.registerOnSharedPreferenceChangeListener(this)
    }


    private fun startTimer(value: Long) {
        timer = object : CountDownTimer(value * 1_000, 1_000) {
            override fun onTick(p0: Long) {
                Log.d("myTag", "p0 = $p0")
                val time = DateUtils.formatElapsedTime(p0 / 1000)
                binding.timerValueTv.text = time
                Log.d("myTag", "1 seconds passed...")
            }

            override fun onFinish() {
                binding.apply {
                    startButton.text = "Start!"
                    val time = DateUtils.formatElapsedTime(seekbar.progress.toLong())
                    timerValueTv.text = time
                    backImage.shakeImage()
                    seekbar.isEnabled = true
                    isTicking = false
                    val soundEnabled =
                        PreferenceManager.getDefaultSharedPreferences(applicationContext)
                            .getBoolean("enable_sound", true)

                    if (soundEnabled) playAlarmSound()
                }
            }
        }
        timer.start()

    }

    private fun playAlarmSound() {
        val audio = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val melody = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getString("timer_melody", "bell")

        val melodyResource = when (melody) {
            "bell" -> R.raw.alarm
            "alarm" -> R.raw.alarm2
            "yeah" -> R.raw.alarm3
            else -> R.raw.alarm
        }

        val soundPool = SoundPool.Builder().setAudioAttributes(audio).build()
        soundPool.load(this, melodyResource, 1)

        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            soundPool
                .play(sampleId, 0.5f, 0.5f, 1, 0, 1f)
        }
    }

    private fun ImageView.shakeImage() {
        val rotate = ObjectAnimator.ofFloat(
            this,
            "rotation",
            0f,
            20f,
            0f,
            -20f,
            0f
        )
        rotate.duration = 170
        rotate.repeatCount = 4
        rotate.start()
    }

    private fun initSeekbar(oldValue: Int) {
        Log.d("myTag", "Seekbar init")
        Log.d("myTag", "oldValue $oldValue")
        binding.seekbar.max = 999
        binding.seekbar.progress = oldValue
        binding.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    Log.d("myTag", "onProgressChanged")
                    val seconds = p1.toLong()
                    val time = DateUtils.formatElapsedTime(seconds)
                    binding.timerValueTv.text = time
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.timer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> true
        }

    private fun getIntervalFromSettings(shared: SharedPreferences): Int {
        return try {
            shared.getString("default_interval", "5")!!.toInt()
        } catch (e: java.lang.Exception) {
            5
        }
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p1 == "default_interval") {
            defaultValue = getIntervalFromSettings(shared)
            initSeekbar(defaultValue)
            binding.timerValueTv.text = DateUtils.formatElapsedTime(defaultValue.toLong())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shared.unregisterOnSharedPreferenceChangeListener(this)
    }
}



