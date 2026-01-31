package com.example.recipegram.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipegram.data.Ingredient
import com.example.recipegram.repository.RecipeRepository
import com.example.recipegram.repository.StepDataUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewRecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    val ingredients = mutableStateListOf(IngredientData("", ""))
    val steps = mutableStateListOf(StepDataUI(name = "", description = "", imageUri = null))

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing

    var isStepModalVisible by mutableStateOf(false)
    var editingStepIndex by mutableStateOf<Int?>(null)

    var tempStep by mutableStateOf(StepDataUI("", "", null))

    private val _publishSuccess = MutableStateFlow(false)
    val publishSuccess: StateFlow<Boolean> = _publishSuccess

    fun addIngredient() = ingredients.add(IngredientData("", ""))
    fun removeIngredient(index: Int) = ingredients.removeAt(index)
    fun updateIngredient(index: Int, name: String, amount: String) {
        ingredients[index] = IngredientData(name, amount)
    }

    fun addStep() = steps.add(StepDataUI("", "", null))
    fun removeStep(index: Int) = steps.removeAt(index)
    fun updateStep(index: Int, step: StepDataUI) {
        steps[index] = step
    }

    fun openStepModal(index: Int? = null) {
        editingStepIndex = index
        tempStep = if (index != null) steps[index] else StepDataUI("", "", null)
        isStepModalVisible = true
    }

    fun saveStep() {
        if (tempStep.description.isBlank()) return

        val index = editingStepIndex
        if (index != null) {
            steps[index] = tempStep
        } else {
            steps.add(tempStep)
        }
        isStepModalVisible = false
    }

    fun publishRecipe(name: String, description: String, time: String, authorId: Long, coverUri: String?) {
        viewModelScope.launch {
            _isPublishing.value = true

            val ingredientEntities = ingredients.map {
                Ingredient(recipeId = 0, name = it.name, amount = it.amount)
            }

            val result = repository.createRecipe(
                title = name,
                description = description.ifBlank { null },
                time = time,
                authorId = authorId,
                coverUri = coverUri,
                ingredients = ingredientEntities,
                steps = steps.toList()
            )

            result.onSuccess { _publishSuccess.value = true }
            _isPublishing.value = false
        }
    }
}

data class IngredientData(val name: String, val amount: String)