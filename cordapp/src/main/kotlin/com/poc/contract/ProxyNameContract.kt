package com.poc.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class ProxyNameContract: Contract {

    companion object {
        @JvmStatic
        val PROXYNAME_CONTRACT_ID = "com.poc.contract.ProxyNameContract"
    }

    interface Commands: CommandData {
        class Issue: TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {

    }
}