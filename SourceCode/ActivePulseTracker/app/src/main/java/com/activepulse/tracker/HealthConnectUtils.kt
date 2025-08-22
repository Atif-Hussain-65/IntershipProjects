
// HealthConnectUtils.kt
package com.activepulse.tracker

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

object HealthConnectUtils {
    private var healthConnectClient: HealthConnectClient? = null
    private const val TAG = "HealthConnectUtils"

    /**
     * Initializes the Health Connect client.
     * @return true if initialization is successful, false otherwise.
     */
    fun init(context: Context): Boolean {
        try {
            healthConnectClient = HealthConnectClient.getOrCreate(context)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize HealthConnectClient.", e)
            healthConnectClient = null
            Toast.makeText(
                context,
                "Health Connect is not supported on this device.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    suspend fun checkPermissions(): Boolean {
        if (healthConnectClient == null) return false
        return try {
            val granted = healthConnectClient?.permissionController?.getGrantedPermissions() ?: setOf()
            granted.containsAll(PERMISSIONS)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check permissions.", e)
            false
        }
    }

    suspend fun readDailySteps(startTime: Instant, endTime: Instant): Long {
        if (healthConnectClient == null) return 0L
        return try {
            val response = healthConnectClient?.aggregate(
                AggregateRequest(
                    setOf(StepsRecord.COUNT_TOTAL),
                    TimeRangeFilter.between(startTime, endTime)
                )
            )
            response?.get(StepsRecord.COUNT_TOTAL) ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read daily steps.", e)
            0L
        }
    }

    suspend fun readDailyWorkoutMinutes(startTime: Instant, endTime: Instant): Long {
        if (healthConnectClient == null) return 0L
        return try {
            val response = healthConnectClient?.readRecords(
                ReadRecordsRequest(
                    ExerciseSessionRecord::class,
                    TimeRangeFilter.between(startTime, endTime)
                )
            )
            var totalMinutes = 0L
            response?.records?.forEach { session ->
                val duration = ChronoUnit.MINUTES.between(session.startTime, session.endTime)
                totalMinutes += duration
            }
            totalMinutes
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read daily workout minutes.", e)
            0L
        }
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }
}