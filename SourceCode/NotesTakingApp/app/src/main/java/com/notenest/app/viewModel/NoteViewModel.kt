package com.notenest.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.notenest.app.data.Note
import com.notenest.app.data.NoteDatabase
import com.notenest.app.data.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    private val _allNotes = MutableStateFlow<List<Note>>(emptyList())
    val allNotes: StateFlow<List<Note>> = _allNotes

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredNotes: StateFlow<List<Note>> =
        searchQuery.combine(_allNotes) { query, notes ->
            if (query.isBlank()) {
                notes
            } else {
                notes.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(500), emptyList())


    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)

        viewModelScope.launch {
            repository.allNotes.collect { notesList ->
                _allNotes.value = notesList
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addNote(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }

    fun getNoteById(id: Int): Flow<Note> {
        return repository.getNoteById(id)
    }
}