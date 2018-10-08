package com.poc.flow

import co.paralleluniverse.fibers.Suspendable
import com.poc.model.ProxyName
import com.poc.service.ProxyNameDatabaseService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.utilities.unwrap

@InitiatingFlow
@StartableByRPC
class ProxyNameQueryUtil(val identifier: String): FlowLogic<ProxyName>() {

    @Suspendable
    override fun call(): ProxyName {
        val proxynameDatabaseService = serviceHub.cordaService(ProxyNameDatabaseService::class.java)
        return proxynameDatabaseService.queryProxyName(identifier)
    }
}