package com.poc.state

import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.util.*

data class AccountTransferState(val debtor: Party,
                                val creditor: Party,
                                val debtorAcct: String,
                                val creditorAcct: String,
                                val creditorName: String,
                                val amount: Amount<Currency>,
                                val identifier: String,
                                val status: String,
                                override val participants: List<AbstractParty> = listOf(debtor),
                                override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

}