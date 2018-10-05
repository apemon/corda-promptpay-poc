package com.poc.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
class BroadcastTransaction(val stx: SignedTransaction): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        var everyone = serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities }
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        everyone = everyone - notary - ourIdentity
        val sessions = everyone.map { initiateFlow(it)}
        sessions.forEach{ subFlow(SendTransactionFlow(it, stx))}
    }
}

@InitiatedBy(BroadcastTransaction::class)
class BroardcastTransactionResponder(val otherSession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call(){
        val flow = ReceiveTransactionFlow(
                otherSideSession = otherSession,
                checkSufficientSignatures = true,
                statesToRecord = StatesToRecord.ALL_VISIBLE
        )

        subFlow(flow)
    }
}