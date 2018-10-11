package com.poc.contract

import com.poc.model.ProxyName
import com.poc.state.AccountTransferState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.utils.sumCash

class AccountTransferContract: Contract {
    companion object {
        @JvmStatic
        val ACCOUNT_TRANSFER_CONTRACT_ID = "com.poc.contract.AccountTransferContract"
    }

    interface Commands: CommandData {
        class Propose: TypeOnlyCommandData(), Commands
        class Confirm: TypeOnlyCommandData(), Commands
    }

    class OracleCommand(val identifier: String, val proxy: ProxyName): CommandData

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<AccountTransferContract.Commands>()
        when(command.value) {
            is Commands.Propose -> requireThat {
                val propose = tx.groupStates<AccountTransferState, UniqueIdentifier> { it.linearId }.single()
                "No inputs should be consumed when transfer" using (propose.inputs.isEmpty())
                "Only one output state should be created" using (propose.outputs.size == 1)
                val output = propose.outputs.single()
                "Only participants must sign transaction" using (command.signers.toSet() == listOf(output.debtor.owningKey).toSet())
            }
            is Commands.Confirm -> requireThat {
                val confirm = tx.groupStates<AccountTransferState, UniqueIdentifier>{it.linearId}.single()
                "Only one input should be consumed when confirm" using (confirm.inputs.size == 1)
                "Only one output state should be created" using (confirm.outputs.size == 1)
                // Check there are output cash states.
                val cash = tx.outputsOfType<Cash.State>()
                "There must be output cash" using (cash.isNotEmpty())
                val input = confirm.inputs.single()
                val output = confirm.outputs.single() as AccountTransferState
                "Debtor and Creditor must not be the same party" using (output.debtor != output.creditor)
                val acceptableCash = cash.filter { it.owner == input.creditor }
                "There must be output cash paid to the recipient." using (acceptableCash.isNotEmpty())
                val sumAcceptableCash = acceptableCash.sumCash().withoutIssuer()
                "Must have enough cash" using (sumAcceptableCash == output.amount)
                "Input Status must PURPOSE" using (input.status == "PURPOSE")
                "Output Status must CONFIRM" using (output.status == "CONFIRM")
                "Only participants must sign transaction" using (command.signers.toSet() == output.participants.map { it.owningKey }.toSet())
            }
        }
    }
}