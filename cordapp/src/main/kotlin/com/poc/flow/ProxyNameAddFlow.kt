package com.poc.flow

import co.paralleluniverse.fibers.Suspendable
import com.poc.model.ProxyName
import com.poc.service.ProxyNameDatabaseService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

@InitiatingFlow
@StartableByRPC
class ProxyNameAddFlow(val proxy: ProxyName): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val databaseService = serviceHub.cordaService(ProxyNameDatabaseService::class.java)
        databaseService.insertProxyName(proxy)
    }
}