package com.poc.flow

import co.paralleluniverse.fibers.Suspendable
import com.poc.model.ProxyName
import com.poc.service.ProxyNameDatabaseService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

@InitiatingFlow
@StartableByRPC
class ProxyNameQueryFlow(val identifier: String): FlowLogic<ProxyName>() {

    @Suspendable
    override fun call(): ProxyName {
        val databaseService = serviceHub.cordaService(ProxyNameDatabaseService::class.java)
        return databaseService.queryProxyName(identifier)
    }
}