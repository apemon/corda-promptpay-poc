package com.poc.state

import com.poc.schema.PublicProxyNameSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

data class ProxyNameState(val identifier: String,
                          val issuer: Party,
                          val hash: String,
                          override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState, QueryableState {

    override val participants: List<AbstractParty>
        get() = listOf(issuer)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is PublicProxyNameSchemaV1 -> PublicProxyNameSchemaV1.PersistentProxyName(
                    this.identifier,
                    this.issuer.name.toString(),
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(PublicProxyNameSchemaV1)
}