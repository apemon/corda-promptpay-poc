package com.poc.flow

import co.paralleluniverse.fibers.Suspendable
import com.poc.contract.ProxyNameContract
import com.poc.state.ProxyNameState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class ProxyNameIssueFlow(val state: ProxyNameState): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val issueCommand = Command(ProxyNameContract.Commands.Issue(), state.participants.map { it.owningKey })
        val builder = TransactionBuilder(notary = notary)
        builder.addOutputState(state, ProxyNameContract.PROXYNAME_CONTRACT_ID)
        builder.addCommand(issueCommand)
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)
        val ftx = subFlow(FinalityFlow(ptx))
        subFlow(BroadcastTransaction(ftx))
        return ftx
    }
}