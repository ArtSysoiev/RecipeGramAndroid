package com.example.recipegram.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
//import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.recipegram.data.RecipeFull
import com.example.recipegram.ui.components.RecipeCard
import com.example.recipegram.ui.viewmodel.AuthViewModel
import com.example.recipegram.ui.viewmodel.RecipeViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun ProfileScreen(
    onRecipeClick: (Long) -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel,
    recipeViewModel: RecipeViewModel = hiltViewModel<RecipeViewModel>()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    val myRecipes by remember(currentUser) {
        if (currentUser != null) {
            recipeViewModel.getUserRecipes(currentUser!!.id)
        } else {
            flowOf(emptyList<RecipeFull>())
        }
    }.collectAsState(initial = emptyList())

    var recipeToDelete by remember { mutableStateOf<RecipeFull?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                if (currentUser?.profileImage != null) {
                    Image(
                        painter = rememberAsyncImagePainter(currentUser!!.profileImage),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = currentUser?.username ?: "Guest",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Master Chef",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.error, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

        Text(
            text = "My Recipes (${myRecipes.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (myRecipes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't posted any recipes yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(myRecipes) { recipeFull ->
                    RecipeCard(
                        recipeFull = recipeFull,
                        onPress = { onRecipeClick(recipeFull.recipe.id) },
                        onDelete = { recipeToDelete = recipeFull }
                    )
                }
            }
        }
    }

    if (recipeToDelete != null) {
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete \"${recipeToDelete?.recipe?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recipeViewModel.deleteRecipe(recipeToDelete!!.recipe.id)
                        recipeToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}