package com.poc.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Column

// family of schemas
object PublicProxyNameSchema

// ProxyName Schema
object PublicProxyNameSchemaV1: MappedSchema(
        schemaFamily = PublicProxyNameSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentProxyName::class.java)
){
    @Entity
    @Table(name = "proxyname_public_states")
    class PersistentProxyName(
            @Column(name = "identifier")
            var identifier: String,

            @Column(name = "issuer")
            var issuerName: String,

            @Column(name = "linear_id")
            var linearId: UUID
    ): PersistentState(){
        constructor(): this("","",UUID.randomUUID())
    }
}