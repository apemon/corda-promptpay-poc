package com.poc.api

import com.poc.flow.*
import com.poc.model.ProxyName
import com.poc.schema.PublicProxyNameSchemaV1
import com.poc.state.AccountTransferState
import com.poc.state.ProxyNameState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.internal.x500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.utilities.OpaqueBytes
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.contracts.getCashBalances
import net.corda.finance.flows.CashIssueFlow
import net.corda.finance.flows.CashPaymentFlow
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("pp")
class PPAPI (val rpcOps: CordaRPCOps) {

    private val me = rpcOps.nodeInfo().legalIdentities.first()
    private val myLegalName = me.name.x500Name;

    fun X500Name.toDisplayString() : String  = BCStyle.INSTANCE.toString(this)

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoamo() = mapOf("me" to myLegalName.toDisplayString());

    @GET
    @Path("balance")
    @Produces(MediaType.APPLICATION_JSON)
            // Display cash balances.
    fun getCashBalances() = rpcOps.getCashBalances()

    @GET
    @Path("cash")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCash(): List<StateAndRef<ContractState>> {
        // Filter by state type: Cash.
        return rpcOps.vaultQueryBy<Cash.State>().states
    }

    @POST
    @Path("issue")
    fun issue(request: RequestParam): Response {
        val issueAmount = Amount(request.amount.toLong() * 100, Currency.getInstance(request.currency))

        val issuerBankPartyRef = OpaqueBytes.of(0)
        val notaryParty = rpcOps.notaryIdentities().first()
        try {
            val cashState = rpcOps.startFlow(::CashIssueFlow, issueAmount, issuerBankPartyRef, notaryParty).returnValue.get()
            return Response
                    .status(Response.Status.OK)
                    .entity((cashState.stx.tx.outputs.single().data as Cash.State).toString())
                    .build()
        } catch(e: Exception){
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.printStackTrace())
                    .build()
        }
    }

    @POST
    @Path("transfer")
    fun transfer(request: RequestParam): Response {
        val transferAmount = Amount(request.amount.toLong() * 100, Currency.getInstance(request.currency))

        val targetParty = rpcOps.partiesFromName(request.to, true).first()
        val issuerParty = rpcOps.partiesFromName("CentralBank", true)
        val paymentRequest = CashPaymentFlow.PaymentRequest(
                transferAmount, targetParty, true, issuerParty
        )
        try {
            val cashState = rpcOps.startFlow(::CashPaymentFlow, paymentRequest).returnValue.get()
            return Response
                    .status(Response.Status.OK)
                    .entity((cashState.stx.tx.outputs.single().data as Cash.State).toString())
                    .build()
        } catch(e: Exception){
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.printStackTrace())
                    .build()
        }
    }

    @POST
    @Path("names/issue")
    fun issueProxyName(request: IssueRequest): Response {
        try{
            val identifier = SecureHash.sha256(request.namespace + ":" + request.value).toString()
            val hash = SecureHash.sha256(request.account + ":" + request.accountName).toString()
            val proxy = ProxyName(identifier, hash, request.namespace, request.value, request.accountName, request.account)
            rpcOps.startFlow(::ProxyNameAddFlow, proxy)
            val state = ProxyNameState(identifier, me, hash)
            val result = rpcOps.startFlow(::ProxyNameIssueFlow, state).returnValue.get()
            return Response
                    .status(Response.Status.OK)
                    .entity(result.tx.outputs.single().data.toString())
                    .build()
        } catch (e: Exception){
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.printStackTrace())
                    .build()
        }
    }

    @GET
    @Path("names")
    @Produces(MediaType.APPLICATION_JSON)
    fun getNames(): List<StateAndRef<ContractState>> {
        return rpcOps.vaultQueryBy<ProxyNameState>().states
    }

    @GET
    @Path("name/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getName(@PathParam(value = "identifier") identifier: String): Response {
        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
        val results = builder {
            val identifierType = PublicProxyNameSchemaV1.PersistentProxyName::identifier.equal(identifier)
            val customIdentifierCriteria = QueryCriteria.VaultCustomQueryCriteria(identifierType)
            val criteria = generalCriteria.and(customIdentifierCriteria)
            val results = rpcOps.vaultQueryBy<ProxyNameState>(criteria).states
            return Response.ok(results).build()
        }
    }

    @GET
    @Path("proxy/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getProxyName(@PathParam(value = "identifier") identifier: String): Response {
        val proxy = rpcOps.startFlow(::ProxyNameQueryUtil, identifier).returnValue.get()
        return Response.ok(proxy).build()
    }

    @GET
    @Path("accttrans")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAccountTransfer(): List<StateAndRef<ContractState>> {
        // Filter by state type: Cash.
        return rpcOps.vaultQueryBy<AccountTransferState>().states
    }

    @POST
    @Path("acct/propose")
    fun accountTransfer(request: AccountTransferRequest): Response {
        val transferAmount = Amount(request.amount.toLong() * 100, Currency.getInstance(request.currency))

        val targetParty = rpcOps.partiesFromName(request.creditor, true).first()
        val state = AccountTransferState(
                debtor = me,
                debtorAcct = request.debtorAcct,
                creditor = targetParty,
                identifier = request.identifier,
                amount = transferAmount,
                creditorAcct = "",
                creditorName = "",
                status = ""
        )
        try {
            val trx = rpcOps.startFlow(::AccountTransferProposeFlow, state).returnValue.get()
            return Response
                    .status(Response.Status.OK)
                    .entity((trx.tx.outputs.single().data as AccountTransferState).toString())
                    .build()
        } catch(e: Exception){
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.printStackTrace())
                    .build()
        }
    }

    @GET
    @Path("acct/confirm/{linearid}")
    fun accountConfirm(@PathParam(value = "linearid") linearId: String): Response {
        val id = UniqueIdentifier.fromString(linearId)
        try {
            val trx = rpcOps.startFlow(::AccountTransferConfirmFlow, id).returnValue.get()
            return Response
                    .status(Response.Status.OK)
                    .entity(trx.tx.outputsOfType<AccountTransferState>().single().toString())
                    .build()
        } catch(e: Exception){
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.printStackTrace())
                    .build()
        }
    }

    data class RequestParam(
            val to: String,
            val amount: Int,
            val currency: String
    )

    data class IssueRequest(
            val namespace: String,
            val value: String,
            val account: String,
            val accountName: String
    )

    data class AccountTransferRequest(
            val debtor: String,
            val debtorAcct: String,
            val creditor: String,
            val creditorAcct: String,
            val amount: Int,
            val currency: String,
            val identifier: String
    )
}