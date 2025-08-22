package com.notenest.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)



    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes_table ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE id = :id")
    fun getNoteById(id: Int): Flow<Note>
}