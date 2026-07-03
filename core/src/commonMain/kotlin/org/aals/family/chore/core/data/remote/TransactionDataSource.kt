package org.aals.family.chore.core.data.remote

import io.ktor.client.HttpClient
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

open class TransactionDataSource(
    private val httpClient: HttpClient
) {
    open suspend fun getTransactions(
        familyId: String,
        userId: String? = null
    ): Result<List<Transaction>, DataError.Network> {
        return httpClient.get(
            route = "$familyId/transactions",
            queryParameters = buildMap {
                userId?.let { put("userId", it) }
            }
        )
    }

    open suspend fun addTransaction(
        transaction: Transaction
    ): Result<Unit, DataError.Network> {
        return httpClient.post(
            route = "${transaction.familyId}/transactions",
            body = transaction
        )
    }
}
