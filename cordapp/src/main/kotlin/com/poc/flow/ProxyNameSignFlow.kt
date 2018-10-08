package com.poc.flow

import co.paralleluniverse.fibers.Suspendable
import com.poc.contract.AccountTransferContract
import com.poc.service.ProxyNameDatabaseService
import com.poc.service.ProxyNameOracle
import net.corda.core.contracts.Command
import net.corda.core.crypto.TransactionSignature
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.FilteredTransaction
import net.corda.core.utilities.unwrap
import javax.annotation.processing.SupportedSourceVersion

@InitiatingFlow
class ProxyNameSignFlow(val oracle: Party, val ftx: FilteredTransaction): FlowLogic<TransactionSignature>() {

    @Suspendable
    override fun call(): TransactionSignature {
        return initiateFlow(oracle).sendAndReceive<TransactionSignature>(ftx).unwrap{ it }
    }
}

@InitiatedBy(ProxyNameSignFlow::class)
class ProxyNameSignFlowHandler(val counterPartySession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val request = counterPartySession.receive<FilteredTransaction>().unwrap { it }

        val oracleService = serviceHub.cordaService(ProxyNameOracle::class.java)
        val response = try {
            oracleService.sign(request)
        } catch (e:Exception) {
            throw FlowException(e)
        }

        counterPartySession.send(response)
    }
}