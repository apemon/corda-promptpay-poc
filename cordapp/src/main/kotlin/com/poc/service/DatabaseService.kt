package com.poc.service

import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.loggerFor
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * this code is copied from
 * https://github.com/corda/flow-db/blob/release-V3/src/main/kotlin/com/flowdb/DatabaseService.kt
 */
@CordaService
open class DatabaseService(private val services: ServiceHub): SingletonSerializeAsToken() {

    companion object {
        var log = loggerFor<DatabaseService>()
    }

    protected fun <T: Any> executeQuery(query: String, params: Map<Int, Any>, transformer: (ResultSet) -> T): List<T> {
        val preparedStatement = prepareStatement(query, params)
        val results = mutableListOf<T>()

        return try {
            val resultSet = preparedStatement.executeQuery()
            while(resultSet.next()) {
                results.add(transformer(resultSet))
            }
            results
        } catch(e: SQLException) {
            log.error(e.message)
            throw e
        } finally {
            preparedStatement.close()
        }
    }

    protected fun executeUpdate(query: String, params: Map<Int, Any>) {
        val preparedStatement = prepareStatement(query, params)

        try {
            preparedStatement.executeUpdate()
        } catch(e:SQLException) {
            log.error(e.message)
            throw e
        } finally {
            preparedStatement.close()
        }
    }

    private fun prepareStatement(query: String, params: Map<Int, Any>): PreparedStatement {
        val session = services.jdbcSession()
        val preparedStatement = session.prepareStatement(query)

        params.forEach { (key, value) ->
            when (value) {
                is String -> preparedStatement.setString(key, value)
                is Int -> preparedStatement.setInt(key, value)
                is Long -> preparedStatement.setLong(key, value)
                else -> throw IllegalArgumentException("Unsupported type.")
            }
        }

        return preparedStatement
    }
}