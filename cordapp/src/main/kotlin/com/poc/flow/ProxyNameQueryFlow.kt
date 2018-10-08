package com.poc.flow

import co.paralleluniverse.fibers.Suspendable
import com.poc.model.ProxyName
import com.poc.service.ProxyNameDatabaseService
import com.poc.service.ProxyNameOracle
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.utilities.unwrap

@InitiatingFlow
class ProxyNameQueryFlow(val oracle: Party, val identifier: String): FlowLogic<ProxyName>() {

    @Suspendable
    override fun call(): ProxyName {
        return initiateFlow(oracle).sendAndReceive<ProxyName>(identifier).unwrap { it }
    }
}

@InitiatedBy(ProxyNameQueryFlow::class)
class ProxyNameQueryFlowHandler(val counterPartySession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val identifier = counterPartySession.receive<String>().unwrap { it }
        val proxyNameDatabaseService = serviceHub.cordaService(ProxyNameOracle::class.java)
        val response = try {
            val proxy = proxyNameDatabaseService.query(identifier)
            proxy
        } catch(e:Exception) {
            throw FlowException(e)
        }

        counterPartySession.send(response)
    }
}