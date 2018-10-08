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
        class Transfer: TypeOnlyCommandData(), Commands
    }

    class OracleCommand(val identifier: String, val proxy: ProxyName): CommandData

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<AccountTransferContract.Commands>()
        when(command.value) {
            is Commands.Propose -> requireThat {

            }
            is Commands.Transfer -> requireThat {

            }
        }
    }
}