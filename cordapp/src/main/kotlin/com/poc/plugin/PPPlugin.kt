package com.poc.plugin

import com.poc.api.PPAPI
import net.corda.core.messaging.CordaRPCOps
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

class PPPlugin: WebServerPluginRegistry {
    override val webApis: List<Function<CordaRPCOps, out Any>>
        get() = listOf(Function(::PPAPI))
    override val staticServeDirs: Map<String, String>
        get() = mapOf("pp" to javaClass.classLoader.getResource("templateWeb").toExternalForm())
}