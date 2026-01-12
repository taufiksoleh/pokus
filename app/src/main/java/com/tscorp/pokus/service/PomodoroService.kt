package com.tscorp.pokus.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tscorp.pokus.MainActivity
import com.tscorp.pokus.PokusApplication
import com.tscorp.pokus.R
import com.tscorp.pokus.data.preferences.PreferencesManager
import com.tscorp.pokus.domain.model.PomodoroPhase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that manages the Pomodoro timer.
 * Handles timer countdown, phase transitions, and notifications.
 */
@AndroidEntryPoint
class PomodoroService : Service() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    // Pomodoro settings (loaded from preferences)
    private var workDurationMillis = 25 * 60 * 1000L
    private var shortBreakMillis = 5 * 60 * 1000L
    private var longBreakMillis = 15 * 60 * 1000L
    private var pomodorosUntilLongBreak = 4

    // Current timer state
    private var currentPhase = PomodoroPhase.IDLE
    private var remainingTimeMillis = 0L
    private var totalTimeMillis = 0L
    private var completedPomodoros = 0
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        loadSettings()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val phase = intent.getStringExtra(EXTRA_PHASE)?.let {
                    PomodoroPhase.valueOf(it)
                } ?: PomodoroPhase.WORK
                startTimer(phase)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
            ACTION_SKIP -> skipPhase()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }

    /**
     * Loads Pomodoro settings from preferences.
     */
    private fun loadSettings() {
        serviceScope.launch {
            workDurationMillis = preferencesManager.pomodoroWorkDuration.first() * 60 * 1000L
            shortBreakMillis = preferencesManager.pomodoroShortBreak.first() * 60 * 1000L
            longBreakMillis = preferencesManager.pomodoroLongBreak.first() * 60 * 1000L
            pomodorosUntilLongBreak = preferencesManager.pomodorosUntilLongBreak.first()
        }
    }

    /**
     * Starts the timer for the given phase.
     */
    private fun startTimer(phase: PomodoroPhase) {
        currentPhase = phase
        totalTimeMillis = when (phase) {
            PomodoroPhase.WORK -> workDurationMillis
            PomodoroPhase.SHORT_BREAK -> shortBreakMillis
            PomodoroPhase.LONG_BREAK -> longBreakMillis
            PomodoroPhase.IDLE -> 0L
        }
        remainingTimeMillis = totalTimeMillis
        isPaused = false

        // Start foreground notification
        val notification = createTimerNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Cancel existing timer
        timerJob?.cancel()

        // Start countdown
        timerJob = serviceScope.launch {
            while (isActive && remainingTimeMillis > 0) {
                if (!isPaused) {
                    delay(1000)
                    remainingTimeMillis -= 1000

                    // Update notification every second
                    updateNotification()

                    // Broadcast state update
                    broadcastState()
                }
            }

            // Timer completed
            if (remainingTimeMillis <= 0) {
                onTimerComplete()
            }
        }

        // Broadcast initial state
        broadcastState()
    }

    /**
     * Pauses the timer.
     */
    private fun pauseTimer() {
        isPaused = true
        updateNotification()
        broadcastState()
    }

    /**
     * Resumes the timer.
     */
    private fun resumeTimer() {
        isPaused = false
        updateNotification()
        broadcastState()
    }

    /**
     * Stops the timer and removes the service.
     */
    private fun stopTimer() {
        timerJob?.cancel()
        currentPhase = PomodoroPhase.IDLE
        remainingTimeMillis = 0L
        isPaused = false
        broadcastState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Skips the current phase and moves to the next one.
     */
    private fun skipPhase() {
        timerJob?.cancel()
        onTimerComplete()
    }

    /**
     * Called when the timer completes a phase.
     */
    private fun onTimerComplete() {
        // Show completion notification
        showCompletionNotification()

        // Determine next phase
        val nextPhase = when (currentPhase) {
            PomodoroPhase.WORK -> {
                completedPomodoros++
                if (completedPomodoros % pomodorosUntilLongBreak == 0) {
                    PomodoroPhase.LONG_BREAK
                } else {
                    PomodoroPhase.SHORT_BREAK
                }
            }
            PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> {
                PomodoroPhase.WORK
            }
            PomodoroPhase.IDLE -> PomodoroPhase.IDLE
        }

        // Check auto-start settings
        serviceScope.launch {
            val autoStartBreaks = preferencesManager.pomodoroAutoStartBreaks.first()
            val autoStartPomodoros = preferencesManager.pomodoroAutoStartPomodoros.first()

            val shouldAutoStart = when (nextPhase) {
                PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> autoStartBreaks
                PomodoroPhase.WORK -> autoStartPomodoros
                PomodoroPhase.IDLE -> false
            }

            if (shouldAutoStart) {
                startTimer(nextPhase)
            } else {
                // Wait for user to start
                currentPhase = nextPhase
                remainingTimeMillis = 0L
                isPaused = false
                broadcastState()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    /**
     * Creates the timer notification.
     */
    private fun createTimerNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val phaseTitle = when (currentPhase) {
            PomodoroPhase.WORK -> "Focus Time"
            PomodoroPhase.SHORT_BREAK -> "Short Break"
            PomodoroPhase.LONG_BREAK -> "Long Break"
            PomodoroPhase.IDLE -> "Pomodoro"
        }

        val timeText = formatTime(remainingTimeMillis)
        val pauseResumeAction = if (isPaused) {
            NotificationCompat.Action(
                0,
                "Resume",
                createActionPendingIntent(ACTION_RESUME)
            )
        } else {
            NotificationCompat.Action(
                0,
                "Pause",
                createActionPendingIntent(ACTION_PAUSE)
            )
        }

        return NotificationCompat.Builder(this, PokusApplication.CHANNEL_POMODORO)
            .setContentTitle(phaseTitle)
            .setContentText(timeText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(pauseResumeAction)
            .addAction(
                NotificationCompat.Action(
                    0,
                    "Skip",
                    createActionPendingIntent(ACTION_SKIP)
                )
            )
            .addAction(
                NotificationCompat.Action(
                    0,
                    "Stop",
                    createActionPendingIntent(ACTION_STOP)
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(
                totalTimeMillis.toInt(),
                (totalTimeMillis - remainingTimeMillis).toInt(),
                false
            )
            .build()
    }

    /**
     * Updates the notification with current timer state.
     */
    private fun updateNotification() {
        val notification = createTimerNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Shows a notification when a phase completes.
     */
    private fun showCompletionNotification() {
        val message = when (currentPhase) {
            PomodoroPhase.WORK -> "Work session complete! Time for a break."
            PomodoroPhase.SHORT_BREAK -> "Break complete! Ready to focus?"
            PomodoroPhase.LONG_BREAK -> "Long break complete! Ready for more work?"
            PomodoroPhase.IDLE -> ""
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, PokusApplication.CHANNEL_POMODORO)
            .setContentTitle("Pomodoro Timer")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager
        notificationManager.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    /**
     * Creates a PendingIntent for notification actions.
     */
    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, PomodoroService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Broadcasts the current timer state.
     */
    private fun broadcastState() {
        val intent = Intent(ACTION_TIMER_UPDATE).apply {
            setPackage(packageName)
            putExtra(EXTRA_IS_RUNNING, timerJob?.isActive == true)
            putExtra(EXTRA_IS_PAUSED, isPaused)
            putExtra(EXTRA_PHASE, currentPhase.name)
            putExtra(EXTRA_REMAINING_TIME, remainingTimeMillis)
            putExtra(EXTRA_TOTAL_TIME, totalTimeMillis)
            putExtra(EXTRA_COMPLETED_POMODOROS, completedPomodoros)
        }
        sendBroadcast(intent)
    }

    /**
     * Formats time in milliseconds to MM:SS string.
     */
    private fun formatTime(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val COMPLETION_NOTIFICATION_ID = 1002

        const val ACTION_START = "com.tscorp.pokus.POMODORO_START"
        const val ACTION_PAUSE = "com.tscorp.pokus.POMODORO_PAUSE"
        const val ACTION_RESUME = "com.tscorp.pokus.POMODORO_RESUME"
        const val ACTION_STOP = "com.tscorp.pokus.POMODORO_STOP"
        const val ACTION_SKIP = "com.tscorp.pokus.POMODORO_SKIP"
        const val ACTION_TIMER_UPDATE = "com.tscorp.pokus.POMODORO_TIMER_UPDATE"

        const val EXTRA_PHASE = "phase"
        const val EXTRA_IS_RUNNING = "is_running"
        const val EXTRA_IS_PAUSED = "is_paused"
        const val EXTRA_REMAINING_TIME = "remaining_time"
        const val EXTRA_TOTAL_TIME = "total_time"
        const val EXTRA_COMPLETED_POMODOROS = "completed_pomodoros"

        /**
         * Starts the Pomodoro timer service.
         */
        fun start(context: Context, phase: PomodoroPhase) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PHASE, phase.name)
            }
            context.startForegroundService(intent)
        }

        /**
         * Pauses the Pomodoro timer.
         */
        fun pause(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        /**
         * Resumes the Pomodoro timer.
         */
        fun resume(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        /**
         * Stops the Pomodoro timer service.
         */
        fun stop(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        /**
         * Skips the current phase.
         */
        fun skip(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_SKIP
            }
            context.startService(intent)
        }
    }
}
