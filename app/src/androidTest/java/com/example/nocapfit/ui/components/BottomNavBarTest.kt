package com.example.nocapfit.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nocapfit.setThemedContent
import com.example.nocapfit.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BottomNavBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersAllNavItems() {
        composeTestRule.setThemedContent {
            BottomNavBar(
                currentRoute = Screen.WorkoutHistory.route,
                onNavigate = {}
            )
        }

        composeTestRule.onNodeWithText("History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Programs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exercises").assertIsDisplayed()
    }

    @Test
    fun historyRoute_selectsHistoryItem() {
        composeTestRule.setThemedContent {
            BottomNavBar(
                currentRoute = Screen.WorkoutHistory.route,
                onNavigate = {}
            )
        }

        composeTestRule.onNodeWithText("History").assertIsSelected()
    }

    @Test
    fun programsRoute_selectsProgramsItem() {
        composeTestRule.setThemedContent {
            BottomNavBar(
                currentRoute = Screen.ProgramList.route,
                onNavigate = {}
            )
        }

        composeTestRule.onNodeWithText("Programs").assertIsSelected()
    }

    @Test
    fun exercisesRoute_selectsExercisesItem() {
        composeTestRule.setThemedContent {
            BottomNavBar(
                currentRoute = Screen.ExerciseList.route,
                onNavigate = {}
            )
        }

        composeTestRule.onNodeWithText("Exercises").assertIsSelected()
    }

    @Test
    fun clickingItem_triggersNavigateCallback() {
        var navigatedTo: Screen? = null
        composeTestRule.setThemedContent {
            BottomNavBar(
                currentRoute = Screen.WorkoutHistory.route,
                onNavigate = { navigatedTo = it }
            )
        }

        composeTestRule.onNodeWithText("Programs").performClick()
        assertEquals(Screen.ProgramList, navigatedTo)
    }

    @Test
    fun clickingExercises_navigatesToExerciseList() {
        var navigatedTo: Screen? = null
        composeTestRule.setThemedContent {
            BottomNavBar(
                currentRoute = Screen.WorkoutHistory.route,
                onNavigate = { navigatedTo = it }
            )
        }

        composeTestRule.onNodeWithText("Exercises").performClick()
        assertEquals(Screen.ExerciseList, navigatedTo)
    }
}
