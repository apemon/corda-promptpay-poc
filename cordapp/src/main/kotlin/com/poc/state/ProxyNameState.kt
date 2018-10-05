package com.poc.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

data class ProxyNameState(val identifier: String,
                          val issuer: Party,
                          val hash: String,
                          override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    override val participants: List<AbstractParty>
        get() = listOf(issuer)
}