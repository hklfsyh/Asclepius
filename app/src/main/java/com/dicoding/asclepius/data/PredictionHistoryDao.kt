package com.dicoding.asclepius.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.dicoding.asclepius.model.PredictionHistory

@Dao
interface PredictionHistoryDao {

    @Insert
    suspend fun insertPrediction(predictionHistory: PredictionHistory)

    @Query("SELECT * FROM prediction_history ORDER BY timestamp DESC")
    suspend fun getAllPredictions(): List<PredictionHistory>

    @Delete
    suspend fun deletePrediction(predictionHistory: PredictionHistory)
}