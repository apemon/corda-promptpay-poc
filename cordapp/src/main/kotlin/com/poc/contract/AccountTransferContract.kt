package com.poc.contract

import com.poc.model.ProxyName
import com.poc.state.AccountTransferState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.finance.contracts.asset.Cash

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

            }
        }
    }
}