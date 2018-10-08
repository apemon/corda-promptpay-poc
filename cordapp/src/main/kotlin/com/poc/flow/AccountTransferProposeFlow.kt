package com.poc.flow

import co.paralleluniverse.fibers.Suspendable
import com.poc.contract.AccountTransferContract
import com.poc.state.AccountTransferState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.function.Predicate

@InitiatingFlow
@StartableByRPC
class AccountTransferProposeFlow(val state: AccountTransferState):FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val oracle = state.creditor
        // get information from oracle
        val proxy = subFlow(ProxyNameQueryFlow(oracle, state.identifier))
        // update state
        val updatedState = AccountTransferState(ourIdentity, oracle, state.debtorAcct, proxy.acctNo, proxy.acctName,state.amount,state.identifier,"PROPOSE")
        // get notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // build command
        val proposeCommand = Command(AccountTransferContract.Commands.Propose(), ourIdentity.owningKey)
        val oracleCommand = Command(AccountTransferContract.OracleCommand(state.identifier, proxy), oracle.owningKey)
        // build transaction
        val builder = TransactionBuilder(notary)
        builder.addCommand(proposeCommand)
        builder.addCommand(oracleCommand)
        builder.addOutputState(updatedState, AccountTransferContract.ACCOUNT_TRANSFER_CONTRACT_ID)
        builder.verify(serviceHub)
        // sign initial transaction
        val ptx = serviceHub.signInitialTransaction(builder)
        // build filtered transaction for oracle
        val ftx = ptx.buildFilteredTransaction(Predicate {
            when (it) {
                is Command<*> -> oracle.owningKey in it.signers && it.value is AccountTransferContract.OracleCommand
                else -> false
            }
        })
        // send transaction to oracle for signing
        val oracleSignature = subFlow(ProxyNameSignFlow(oracle, ftx))
        val stx = ptx.withAdditionalSignature(oracleSignature)
        // finality flow
        return subFlow(FinalityFlow(stx))
    }
}