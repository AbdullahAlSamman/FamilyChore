package org.aals.family.chore.feature.auth.presentation.create_family

sealed interface CreateFamilyEvent {
    data class FamilyCreated(val familyId: String, val userId: String) : CreateFamilyEvent
    data object NavigateBack : CreateFamilyEvent
}
