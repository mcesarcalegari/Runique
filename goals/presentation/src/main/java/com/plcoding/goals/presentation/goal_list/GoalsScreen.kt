@file:OptIn(ExperimentalMaterial3Api::class)

package com.plcoding.goals.presentation.goal_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plcoding.core.presentation.designsystem.PlusIcon
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.core.presentation.designsystem.components.RuniqueFloatingActionButton
import com.plcoding.core.presentation.designsystem.components.RuniqueScaffold
import com.plcoding.core.presentation.designsystem.components.RuniqueToolbar
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.goal_list.components.GoalCard
import com.plcoding.goals.presentation.goal_list.components.GoalCardPreviewData
import com.plcoding.goals.presentation.goal_list.components.GoalsEmptyState
import com.plcoding.goals.presentation.goal_list.components.GoalsTabRow
import org.koin.androidx.compose.koinViewModel

/** Space kept free below the content for the FAB (Figma body bottom padding). */
private val BODY_BOTTOM_PADDING = 140.dp

@Composable
fun GoalsScreenRoot(
    onBackClick: () -> Unit,
    onAddGoalClick: () -> Unit,
    onEditGoalClick: (goalId: String) -> Unit,
    viewModel: GoalsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GoalsScreen(
        state = state,
        onAction = { action ->
            when (action) {
                GoalsAction.OnBackClick -> onBackClick()
                GoalsAction.OnAddGoalClick -> onAddGoalClick()
                is GoalsAction.OnGoalClick -> onEditGoalClick(action.goalId)
                is GoalsAction.OnTabSelect -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

/** Goals list from Figma frames 02 (Active), 03 (empty), 04 (Completed) and 05 (Missed). */
@Composable
fun GoalsScreen(
    state: GoalsState,
    onAction: (GoalsAction) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    RuniqueScaffold(
        topAppBar = {
            RuniqueToolbar(
                showBackButton = true,
                title = stringResource(R.string.goals),
                scrollBehavior = scrollBehavior,
                onBackClick = { onAction(GoalsAction.OnBackClick) }
            )
        },
        floatingActionButton = {
            RuniqueFloatingActionButton(
                icon = PlusIcon,
                contentDescription = stringResource(R.string.add_goal),
                iconSize = 24.dp,
                onClick = { onAction(GoalsAction.OnAddGoalClick) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            GoalsTabRow(
                selectedTab = state.selectedTab,
                onTabSelect = { onAction(GoalsAction.OnTabSelect(it)) }
            )
            when {
                state.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = BODY_BOTTOM_PADDING),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO(design): no Figma frame for the loading state yet.
                    CircularProgressIndicator()
                }
                state.visibleGoals.isEmpty() -> GoalsEmptyState(
                    tab = state.selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = BODY_BOTTOM_PADDING)
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(top = 8.dp, bottom = BODY_BOTTOM_PADDING),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = state.visibleGoals,
                        key = { it.id }
                    ) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = if (goal.isEditable) {
                                { onAction(GoalsAction.OnGoalClick(goal.id)) }
                            } else null,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsScreenPreview(state: GoalsState) {
    RuniqueTheme {
        GoalsScreen(state = state, onAction = {})
    }
}

private val previewState = GoalsState(
    isLoading = false,
    activeGoals = listOf(
        GoalCardPreviewData.activeDistance,
        GoalCardPreviewData.activeFrequency,
        GoalCardPreviewData.activeDuration
    ),
    completedGoals = listOf(
        GoalCardPreviewData.completedEnded,
        GoalCardPreviewData.completedMonthlyFrequency
    ),
    missedGoals = listOf(
        GoalCardPreviewData.missedDuration,
        GoalCardPreviewData.missedMonthlyDistance
    )
)

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalsScreenLoadingPreview() {
    GoalsScreenPreview(GoalsState())
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalsScreenActivePreview() {
    GoalsScreenPreview(previewState)
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalsScreenActiveEmptyPreview() {
    GoalsScreenPreview(GoalsState(isLoading = false))
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalsScreenCompletedPreview() {
    GoalsScreenPreview(previewState.copy(selectedTab = GoalsTab.COMPLETED))
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalsScreenCompletedEmptyPreview() {
    GoalsScreenPreview(GoalsState(isLoading = false, selectedTab = GoalsTab.COMPLETED))
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalsScreenMissedPreview() {
    GoalsScreenPreview(previewState.copy(selectedTab = GoalsTab.MISSED))
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalsScreenMissedEmptyPreview() {
    GoalsScreenPreview(GoalsState(isLoading = false, selectedTab = GoalsTab.MISSED))
}
