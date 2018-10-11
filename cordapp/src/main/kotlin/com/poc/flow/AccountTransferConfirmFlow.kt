package com.poc.flow

import co.paralleluniverse.fibers.Suspendable
import com.poc.contract.AccountTransferContract
import com.poc.state.AccountTransferState
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.contracts.getCashBalance

@InitiatingFlow
@StartableByRPC
class AccountTransferConfirmFlow(val linearId: UniqueIdentifier): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // get state
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val input = serviceHub.vaultService.queryBy<AccountTransferState>(queryCriteria).states.single()
        // get notary
        val notary = input.state.notary
        // get counter party
        val creditor = input.state.data.creditor
        // check the party running this flow is debtor
        if(ourIdentity != input.state.data.debtor) {
            throw IllegalArgumentException("confirm flow must be initiated by the debtor.")
        }
        // check cash balance
        val amount = input.state.data.amount
        val cashBalance = serviceHub.getCashBalance(amount.token)
        if(cashBalance < amount) {
            throw IllegalArgumentException("You has only $cashBalance but attempt to transfer $amount")
        }
        // create transacion builder
        val builder = TransactionBuilder(notary)
        // get some cash from vault and add a spend to transaction builder
        val (_, cashKeys) = Cash.generateSpend(serviceHub, builder, amount, ourIdentityAndCert, creditor)
        // create output state
        val output = input.state.data.copy(status = "CONFIRM", participants = listOf(ourIdentity, creditor))
        // build command
        val command = Command(AccountTransferContract.Commands.Confirm(), output.participants.map { it.owningKey })
        // add state and command to builder
        builder.addCommand(command)
        builder.addInputState(input)
        builder.addOutputState(output, AccountTransferContract.ACCOUNT_TRANSFER_CONTRACT_ID)
        // verify and sign
        builder.verify(serviceHub)
        // sign for cash
        val myKeysToSign = (cashKeys.toSet() + ourIdentity.owningKey).toList()
        val ptx = serviceHub.signInitialTransaction(builder, myKeysToSign)
        // initial session
        val counterPartySession = initiateFlow(creditor)
        // send to counterparty
        subFlow(IdentitySyncFlow.Send(counterPartySession, ptx.tx))
        // collect signature
        val stx = subFlow(CollectSignaturesFlow(ptx, listOf(counterPartySession), myOptionalKeys = myKeysToSign))
        // finality
        val ftx = subFlow(FinalityFlow(stx))
        return ftx
    }
}

@InitiatedBy(AccountTransferConfirmFlow::class)
class AccountTransferConfirmFlowHandler(val flowSession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        subFlow(IdentitySyncFlow.Receive(flowSession))
        val signedTransactionFlow = object: SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {

            }
        }
        subFlow(signedTransactionFlow)
    }
}