package com.plcoding.goals.presentation.goal_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.core.domain.util.Result
import com.plcoding.core.presentation.ui.asUiText
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalRepository
import com.plcoding.goals.domain.GoalTargetValidator
import com.plcoding.goals.domain.GoalType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives both New goal and Edit goal. Edit mode is selected by a `goalId` argument in
 * [savedStateHandle] (from the `EditGoal` route).
 */
class GoalEditorViewModel(
    private val goalRepository: GoalRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val goalId: String? = savedStateHandle.get<String>(KEY_GOAL_ID)

    private val _state = MutableStateFlow(restoreState())
    val state = _state.asStateFlow()

    private val _events = Channel<GoalEditorEvent>()
    val events = _events.receiveAsFlow()

    init {
        if (goalId != null && savedStateHandle.get<String>(KEY_TARGET) == null) {
            loadGoal(goalId)
        }
    }

    fun onAction(action: GoalEditorAction) {
        when (action) {
            is GoalEditorAction.OnTypeSelect -> selectType(action.type)
            is GoalEditorAction.OnPeriodSelect -> selectPeriod(action.period)
            is GoalEditorAction.OnTargetChange -> changeTarget(action.text)
            GoalEditorAction.OnSaveClick -> save()
            GoalEditorAction.OnDeleteClick -> {
                if (goalId != null) _state.update { it.copy(isDeleteDialogVisible = true) }
            }
            GoalEditorAction.OnDeleteDismiss -> _state.update { it.copy(isDeleteDialogVisible = false) }
            GoalEditorAction.OnDeleteConfirm -> delete()
            // Navigation is handled by the Root composable.
            GoalEditorAction.OnBackClick -> Unit
        }
    }

    private fun restoreState(): GoalEditorState {
        val type = savedStateHandle.get<String>(KEY_TYPE)?.let(GoalType::valueOf) ?: GoalType.DISTANCE
        val period = savedStateHandle.get<String>(KEY_PERIOD)?.let(GoalPeriod::valueOf) ?: GoalPeriod.WEEKLY
        val targetText = savedStateHandle.get<String>(KEY_TARGET).orEmpty()
        return GoalEditorState(
            isEditMode = goalId != null,
            isLoadingGoal = goalId != null && savedStateHandle.get<String>(KEY_TARGET) == null,
            type = type,
            period = period,
            targetText = targetText,
            canSave = isValid(targetText, type)
        )
    }

    private fun loadGoal(id: String) {
        viewModelScope.launch {
            when (val result = goalRepository.getGoal(id)) {
                is Result.Success -> {
                    val goal = result.data
                    updateForm(
                        type = goal.type,
                        period = goal.period,
                        targetText = GoalTargetValidator.toInputText(goal.target, goal.type)
                    )
                    _state.update { it.copy(isLoadingGoal = false) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoadingGoal = false) }
                    _events.send(GoalEditorEvent.Error(result.error.asUiText()))
                }
            }
        }
    }

    private fun selectType(type: GoalType) {
        val current = _state.value
        if (current.isEditMode || type == current.type) return
        val targetText = current.targetText.takeIf { GoalTargetValidator.isAllowedInput(it, type) }.orEmpty()
        updateForm(type = type, targetText = targetText)
    }

    private fun selectPeriod(period: GoalPeriod) {
        if (_state.value.isEditMode) return
        updateForm(period = period)
    }

    private fun changeTarget(text: String) {
        if (!GoalTargetValidator.isAllowedInput(text, _state.value.type)) return
        updateForm(targetText = text)
    }

    private fun updateForm(
        type: GoalType = _state.value.type,
        period: GoalPeriod = _state.value.period,
        targetText: String = _state.value.targetText
    ) {
        savedStateHandle[KEY_TYPE] = type.name
        savedStateHandle[KEY_PERIOD] = period.name
        savedStateHandle[KEY_TARGET] = targetText
        _state.update {
            it.copy(
                type = type,
                period = period,
                targetText = targetText,
                canSave = isValid(targetText, type)
            )
        }
    }

    private fun save() {
        val current = _state.value
        if (!current.canSave || current.isSaving) return
        val target = (GoalTargetValidator.parse(current.targetText, current.type) as? Result.Success)?.data
            ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = if (goalId != null) {
                goalRepository.updateTarget(goalId, target)
            } else {
                goalRepository.createGoal(current.type, current.period, target)
            }
            _state.update { it.copy(isSaving = false) }
            when (result) {
                is Result.Success -> _events.send(GoalEditorEvent.GoalSaved)
                is Result.Error -> _events.send(GoalEditorEvent.Error(result.error.asUiText()))
            }
        }
    }

    private fun delete() {
        val id = goalId ?: return
        if (_state.value.isDeleting) return
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            val result = goalRepository.deleteGoal(id)
            _state.update { it.copy(isDeleting = false, isDeleteDialogVisible = false) }
            when (result) {
                is Result.Success -> _events.send(GoalEditorEvent.GoalDeleted)
                is Result.Error -> _events.send(GoalEditorEvent.Error(result.error.asUiText()))
            }
        }
    }

    private fun isValid(text: String, type: GoalType): Boolean {
        return GoalTargetValidator.parse(text, type) is Result.Success
    }

    companion object {
        const val KEY_GOAL_ID = "goalId"
        private const val KEY_TYPE = "type"
        private const val KEY_PERIOD = "period"
        private const val KEY_TARGET = "targetText"
    }
}
