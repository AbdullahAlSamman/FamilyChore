package org.aals.family.chore.feature.dashboard.domain.model

import org.aals.family.chore.core.domain.model.BehaviorItem

object BehaviorDefaults {
    val defaultItems = listOf(
        BehaviorItem("1", "", "Politeness", 10),
        BehaviorItem("2", "", "Helping others", 15),
        BehaviorItem("3", "", "Rudeness", -10),
        BehaviorItem("4", "", "Ignoring instructions", -20)
    )
}
