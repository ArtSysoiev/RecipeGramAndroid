package com.example.recipegram.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipegram.data.RecipeFull
import com.example.recipegram.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailsViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _recipe = MutableStateFlow<RecipeFull?>(null)
    val recipe: StateFlow<RecipeFull?> = _recipe

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRecipe(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _recipe.value = repository.getRecipeById(id)
            _isLoading.value = false
        }
    }
}