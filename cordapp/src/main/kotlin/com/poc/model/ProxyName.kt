package com.poc.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class ProxyName (val identifier: String,
                      val hash: String,
                      val namespace: String,
                      val value: String,
                      val acctName: String,
                      val acctNo: String)