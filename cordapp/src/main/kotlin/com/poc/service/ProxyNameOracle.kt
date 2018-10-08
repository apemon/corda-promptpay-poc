package com.poc.service

import com.poc.contract.AccountTransferContract
import com.poc.model.ProxyName
import net.corda.core.contracts.Command
import net.corda.core.crypto.TransactionSignature
import net.corda.core.flows.FlowException
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.transactions.FilteredTransaction

@CordaService
class ProxyNameOracle(val services: ServiceHub): SingletonSerializeAsToken() {
    private val myKey = services.myInfo.legalIdentities.first().owningKey

    fun query(identifier: String): ProxyName {
        val databaseService = services.cordaService(ProxyNameDatabaseService::class.java)
        return databaseService.queryProxyName(identifier)
    }

    fun sign(ftx: FilteredTransaction): TransactionSignature {
        ftx.verify()

        val proxyNameDatabaseService = services.cordaService(ProxyNameDatabaseService::class.java)

        fun isCorrectCommand(elem: Any) = when {
            elem is Command<*> && elem.value is AccountTransferContract.OracleCommand -> {
                val command = elem.value as AccountTransferContract.OracleCommand
                myKey in elem.signers && proxyNameDatabaseService.queryProxyName(command.identifier).equals(command.proxy)
            }
            else -> false
        }

        val isValid = ftx.checkWithFun(::isCorrectCommand)

        if (isValid) {
            return services.createSignature(ftx, myKey)
        } else {
            throw IllegalArgumentException("Oracle signature requested over invalid transaction.")
        }
    }
}