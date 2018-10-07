package com.poc.service

import com.poc.model.ProxyName
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService

val TABLE_NAME = "proxyname"

@CordaService
class ProxyNameDatabaseService(services: ServiceHub): DatabaseService(services) {
    init {
        setupStorage()
    }

    fun insertProxyName(proxy: ProxyName) {
        val query = "Insert into $TABLE_NAME values(?,?,?,?,?,?)"

        val params = mapOf(1 to proxy.identifier,
                2 to proxy.namespace,
                3 to proxy.value,
                4 to proxy.acctName,
                5 to proxy.acctNo,
                6 to proxy.hash)

        executeUpdate(query, params)
        log.info("Proxy add $TABLE_NAME")
    }

    fun queryProxyName(identifier: String): ProxyName {
        val query = "select * from $TABLE_NAME where identifier = ?"
        val params = mapOf(1 to identifier)

        val results = executeQuery(query, params, {
            val iden = it.getString(1)
            val proxyName = it.getString(2)
            val proxyValue = it.getString(3)
            val acctName = it.getString(4)
            val acctNo = it.getString(5)
            val hash = it.getString(6)
            val proxy = ProxyName(iden,proxyName,proxyValue, acctName, acctNo, hash)
            proxy
        })

        if(results.isEmpty()) {
            throw IllegalArgumentException("$identifier is not in database.")
        }

        val value = results.single()

        return value
    }

    private fun setupStorage() {
        val query = """
        create table if not exists $TABLE_NAME(
            identifier varchar(64),
            proxy_name varchar(64),
            proxy_value varchar(256),
            account_name varchar(2048),
            account_no varchar(32),
            hash varchar(64),
            PRIMARY KEY (identifier))
        """

        executeUpdate(query, emptyMap())
        log.info("Create $TABLE_NAME table")
    }
}