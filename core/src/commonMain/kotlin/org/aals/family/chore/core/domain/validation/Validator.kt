package org.aals.family.chore.core.domain.validation

import org.aals.family.chore.core.domain.util.Error

interface Validator<T, E : Error> {
    fun validate(input: T): E?
}
