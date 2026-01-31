package com.example.recipegram.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
//import androidx.hilt.navigation.compose.hiltViewModel
import com.example.recipegram.ui.components.AuthInput
import com.example.recipegram.ui.components.RecipeCard
import com.example.recipegram.ui.viewmodel.RecipeViewModel

@Composable
fun HomeScreen(
    onRecipeClick: (Long) -> Unit,
    viewModel: RecipeViewModel = hiltViewModel<RecipeViewModel>()
) {
    val recipes by viewModel.allRecipes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .statusBarsPadding()
    ) {
        Text(
            text = "RecipeGram",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            AuthInput(
                label = "Search recipes...",
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                icon = Icons.Default.Search
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recipes.isEmpty()) {
            EmptyFeedState(isSearching = searchQuery.isNotBlank())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(recipes) { recipeFull ->
                    RecipeCard(
                        recipeFull = recipeFull,
                        onPress = { onRecipeClick(recipeFull.recipe.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyFeedState(isSearching: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSearching) "No recipes found" else "No recipes yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearching) "Try changing your search query" else "It's time to cook something! Your feed is empty",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}