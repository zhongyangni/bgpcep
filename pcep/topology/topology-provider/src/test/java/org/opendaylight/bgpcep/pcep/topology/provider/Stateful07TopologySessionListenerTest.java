/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bgpcep.pcep.topology.provider;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.protocol.pcep.pcc.mock.spi.MsgBuilderUtil.createLspTlvs;
import static org.opendaylight.protocol.util.CheckTestUtil.checkEquals;
import static org.opendaylight.protocol.util.CheckTestUtil.checkNotPresentOperational;
import static org.opendaylight.protocol.util.CheckTestUtil.readDataOperational;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.protocol.pcep.PCEPCloseTermination;
import org.opendaylight.protocol.pcep.TerminationReason;
import org.opendaylight.protocol.pcep.impl.PCEPSessionImpl;
import org.opendaylight.protocol.pcep.pcc.mock.spi.MsgBuilderUtil;
import org.opendaylight.protocol.pcep.spi.AbstractMessageParser;
import org.opendaylight.protocol.pcep.spi.PCEPErrors;
import org.opendaylight.protocol.util.CheckTestUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.topology.rev140113.NetworkTopologyRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev181109.Pcinitiate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev181109.Stateful1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev181109.Stateful1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev181109.pcinitiate.message.pcinitiate.message.Requests;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Arguments1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Arguments1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Arguments2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Arguments2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Arguments3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Arguments3Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.OperationalStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Pcrpt;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.PcrptBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Pcupd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.PlspId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.SymbolicPathName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Tlvs1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.Tlvs1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.lsp.identifiers.tlv.LspIdentifiersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.lsp.object.LspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.lsp.object.lsp.Tlvs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.pcrpt.message.PcrptMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.pcrpt.message.pcrpt.message.Reports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.pcrpt.message.pcrpt.message.ReportsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.pcrpt.message.pcrpt.message.reports.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.pcupd.message.pcupd.message.Updates;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.stateful.capability.tlv.StatefulBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev181109.symbolic.path.name.tlv.SymbolicPathNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.message.rev181109.Close;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.stateful.stats.rev181109.StatefulCapabilitiesStatsAug;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.stateful.stats.rev181109.StatefulMessagesStatsAug;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.stats.rev171113.PcepSessionState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.stats.rev171113.pcep.session.state.LocalPref;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.stats.rev171113.pcep.session.state.PeerPref;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.stats.rev171113.reply.time.grouping.ReplyTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.endpoints.address.family.Ipv4CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.endpoints.address.family.ipv4._case.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.endpoints.object.EndpointsObjBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.explicit.route.object.EroBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.open.object.Open;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.open.object.OpenBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.open.object.open.TlvsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev181109.pcep.error.object.ErrorObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev150820.LspId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.AddLspInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.AddLspInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.AddLspOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.EnsureLspOperationalInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.EnsureLspOperationalInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.FailureType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.OperationResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.RemoveLspInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.RemoveLspInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.UpdateLspInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.UpdateLspInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.UpdateLspOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.add.lsp.args.ArgumentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.pcep.client.attributes.path.computation.client.ReportedLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.pcep.client.attributes.path.computation.client.reported.lsp.Path;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint32;

public class Stateful07TopologySessionListenerTest
        extends AbstractPCEPSessionTest<Stateful07TopologySessionListenerFactory> {

    private final String tunnelName = "pcc_" + this.testAddress + "_tunnel_0";

    private Stateful07TopologySessionListener listener;

    private PCEPSessionImpl session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.listener = (Stateful07TopologySessionListener) getSessionListener();
        this.session = getPCEPSession(getLocalPref(), getRemotePref());
    }

    @Test
    public void testStateful07TopologySessionListener() throws Exception {
        this.listener.onSessionUp(this.session);
        final PcepSessionState listenerState = this.listener.listenerState;
        assertEquals(this.testAddress, listenerState.getPeerPref().getIpAddress());
        final LocalPref state = this.listener.listenerState.getLocalPref();
        assertNotNull(state);
        assertEquals(DEAD_TIMER, state.getDeadtimer().shortValue());
        assertEquals(KEEP_ALIVE, state.getKeepalive().shortValue());
        assertEquals(0, state.getSessionId().intValue());
        assertEquals(this.testAddress, state.getIpAddress());

        final PeerPref peerState = listenerState.getPeerPref();

        assertEquals(DEAD_TIMER, peerState.getDeadtimer().shortValue());
        assertEquals(KEEP_ALIVE, peerState.getKeepalive().shortValue());
        assertEquals(0, peerState.getSessionId().intValue());
        assertEquals(this.testAddress, peerState.getIpAddress());

        // add-lsp
        this.topologyRpcs.addLsp(createAddLspInput());
        assertEquals(1, this.receivedMsgs.size());
        assertTrue(this.receivedMsgs.get(0) instanceof Pcinitiate);
        final Pcinitiate pcinitiate = (Pcinitiate) this.receivedMsgs.get(0);
        final Requests req = pcinitiate.getPcinitiateMessage().getRequests().get(0);
        final Uint32 srpId = req.getSrp().getOperationId().getValue();
        final Tlvs tlvs = createLspTlvs(req.getLsp().getPlspId().getValue(), true,
                this.testAddress, this.testAddress, this.testAddress, Optional.empty());
        final Pcrpt pcRpt = MsgBuilderUtil.createPcRtpMessage(new LspBuilder(req.getLsp())
                        .setTlvs(tlvs).setPlspId(new PlspId(Uint32.ONE)).setSync(FALSE).setRemove(FALSE)
                        .setOperational(OperationalStatus.Active).build(), Optional.of(MsgBuilderUtil.createSrp(srpId)),
                MsgBuilderUtil.createPath(req.getEro().getSubobject()));
        final Pcrpt esm = MsgBuilderUtil.createPcRtpMessage(new LspBuilder().setSync(FALSE).build(),
                Optional.of(MsgBuilderUtil.createSrp(Uint32.ZERO)), null);
        this.listener.onMessage(this.session, esm);
        readDataOperational(getDataBroker(), this.pathComputationClientIId, pcc -> {
            assertEquals(this.testAddress, pcc.getIpAddress().getIpv4AddressNoZone().getValue());
            // reported lsp so far empty, has not received response (PcRpt) yet
            assertTrue(pcc.getReportedLsp().isEmpty());
            return pcc;
        });

        this.listener.onMessage(this.session, pcRpt);
        // check created lsp
        readDataOperational(getDataBroker(), this.pathComputationClientIId, pcc -> {
            assertEquals(1, pcc.getReportedLsp().size());
            final ReportedLsp reportedLsp = pcc.getReportedLsp().get(0);
            assertEquals(this.tunnelName, reportedLsp.getName());
            assertEquals(1, reportedLsp.getPath().size());
            final Path path = reportedLsp.getPath().get(0);
            assertEquals(1, path.getEro().getSubobject().size());
            assertEquals(this.eroIpPrefix, getLastEroIpPrefix(path.getEro()));
            return pcc;
        });

        // check stats
        checkEquals(() -> assertEquals(1, listenerState.getDelegatedLspsCount().intValue()));
        checkEquals(() -> assertTrue(this.listener.isSessionSynchronized()));
        checkEquals(() -> assertTrue(listenerState.getMessages()
                .augmentation(StatefulMessagesStatsAug.class).getLastReceivedRptMsgTimestamp().toJava() > 0));
        checkEquals(() -> assertEquals(2, listenerState.getMessages()
                .augmentation(StatefulMessagesStatsAug.class).getReceivedRptMsgCount().intValue()));
        checkEquals(() -> assertEquals(1, listenerState.getMessages()
                .augmentation(StatefulMessagesStatsAug.class).getSentInitMsgCount().intValue()));
        checkEquals(() -> assertEquals(0, listenerState.getMessages()
                .augmentation(StatefulMessagesStatsAug.class).getSentUpdMsgCount().intValue()));

        // update-lsp
        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.update.lsp.args
                .ArgumentsBuilder updArgsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang
                .topology.pcep.rev200120.update.lsp.args.ArgumentsBuilder();
        updArgsBuilder.setEro(createEroWithIpPrefixes(Lists.newArrayList(this.eroIpPrefix, this.dstIpPrefix)));
        updArgsBuilder.addAugmentation(Arguments3.class, new Arguments3Builder().setLsp(new LspBuilder()
                .setDelegate(TRUE).setAdministrative(FALSE).build()).build());
        final UpdateLspInput update = new UpdateLspInputBuilder().setArguments(updArgsBuilder.build())
                .setName(this.tunnelName).setNetworkTopologyRef(new NetworkTopologyRef(TOPO_IID))
                .setNode(this.nodeId).build();
        this.topologyRpcs.updateLsp(update);
        assertEquals(2, this.receivedMsgs.size());
        assertTrue(this.receivedMsgs.get(1) instanceof Pcupd);
        final Pcupd updateMsg = (Pcupd) this.receivedMsgs.get(1);
        final Updates upd = updateMsg.getPcupdMessage().getUpdates().get(0);
        final Uint32 srpId2 = upd.getSrp().getOperationId().getValue();
        final Tlvs tlvs2 = createLspTlvs(upd.getLsp().getPlspId().getValue(), false,
                this.newDestinationAddress, this.testAddress, this.testAddress, Optional.empty());
        final Pcrpt pcRpt2 = MsgBuilderUtil.createPcRtpMessage(new LspBuilder(upd.getLsp()).setTlvs(tlvs2)
                        .setSync(TRUE).setRemove(FALSE).setOperational(OperationalStatus.Active).build(),
                Optional.of(MsgBuilderUtil.createSrp(srpId2)), MsgBuilderUtil.createPath(upd.getPath()
                        .getEro().getSubobject()));
        this.listener.onMessage(this.session, pcRpt2);

        //check updated lsp
        readDataOperational(getDataBroker(), this.pathComputationClientIId, pcc -> {
            assertEquals(1, pcc.getReportedLsp().size());
            final ReportedLsp reportedLsp = pcc.getReportedLsp().get(0);
            assertEquals(this.tunnelName, reportedLsp.getName());
            assertEquals(1, reportedLsp.getPath().size());
            final Path path = reportedLsp.getPath().get(0);
            assertEquals(2, path.getEro().getSubobject().size());
            assertEquals(this.dstIpPrefix, getLastEroIpPrefix(path.getEro()));
            assertEquals(1, listenerState.getDelegatedLspsCount().intValue());
            assertTrue(this.listener.isSessionSynchronized());
            final StatefulMessagesStatsAug statefulstate = listenerState.getMessages()
                    .augmentation(StatefulMessagesStatsAug.class);
            assertTrue(statefulstate.getLastReceivedRptMsgTimestamp().toJava() > 0);
            assertEquals(3, statefulstate.getReceivedRptMsgCount().intValue());
            assertEquals(1, statefulstate.getSentInitMsgCount().intValue());
            assertEquals(1, statefulstate.getSentUpdMsgCount().intValue());
            final ReplyTime replyTime = listenerState.getMessages().getReplyTime();
            assertTrue(replyTime.getAverageTime().toJava() > 0);
            assertTrue(replyTime.getMaxTime().toJava() > 0);
            final StatefulCapabilitiesStatsAug statefulCapabilities = listenerState
                    .getPeerCapabilities().augmentation(StatefulCapabilitiesStatsAug.class);
            assertFalse(statefulCapabilities.isActive());
            assertTrue(statefulCapabilities.isInstantiation());
            assertTrue(statefulCapabilities.isStateful());
            return pcc;
        });

        // ensure-operational
        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.ensure.lsp
                .operational.args.ArgumentsBuilder ensureArgs = new org.opendaylight.yang.gen.v1.urn.opendaylight.params
                .xml.ns.yang.topology.pcep.rev200120.ensure.lsp.operational.args.ArgumentsBuilder();
        ensureArgs.addAugmentation(Arguments1.class, new Arguments1Builder().setOperational(OperationalStatus.Active)
                .build());
        final EnsureLspOperationalInput ensure = new EnsureLspOperationalInputBuilder().setArguments(ensureArgs.build())
                .setName(this.tunnelName).setNetworkTopologyRef(new NetworkTopologyRef(TOPO_IID))
                .setNode(this.nodeId).build();
        final OperationResult result = this.topologyRpcs.ensureLspOperational(ensure).get().getResult();
        //check result
        assertNull(result.getFailure());

        // remove-lsp
        final RemoveLspInput remove = new RemoveLspInputBuilder().setName(this.tunnelName)
                .setNetworkTopologyRef(new NetworkTopologyRef(TOPO_IID)).setNode(this.nodeId).build();
        this.topologyRpcs.removeLsp(remove);
        assertEquals(3, this.receivedMsgs.size());
        assertTrue(this.receivedMsgs.get(2) instanceof Pcinitiate);
        final Pcinitiate pcinitiate2 = (Pcinitiate) this.receivedMsgs.get(2);
        final Requests req2 = pcinitiate2.getPcinitiateMessage().getRequests().get(0);
        final Uint32 srpId3 = req2.getSrp().getOperationId().getValue();
        final Tlvs tlvs3 = createLspTlvs(req2.getLsp().getPlspId().getValue(), false,
                this.testAddress, this.testAddress, this.testAddress, Optional.empty());
        final Pcrpt pcRpt3 = MsgBuilderUtil.createPcRtpMessage(new LspBuilder(req2.getLsp()).setTlvs(tlvs3)
                        .setRemove(TRUE).setSync(TRUE).setOperational(OperationalStatus.Down).build(),
                Optional.of(MsgBuilderUtil.createSrp(srpId3)), MsgBuilderUtil.createPath(Collections.emptyList()));
        this.listener.onMessage(this.session, pcRpt3);

        // check if lsp was removed
        readDataOperational(getDataBroker(), this.pathComputationClientIId, pcc -> {
            assertNull(pcc.getReportedLsp());
            return pcc;
        });
        // check stats
        checkEquals(() -> assertEquals(0, listenerState.getDelegatedLspsCount().intValue()));
        checkEquals(() -> assertTrue(this.listener.isSessionSynchronized()));
        checkEquals(() -> assertTrue(listenerState.getMessages()
                .augmentation(StatefulMessagesStatsAug.class).getLastReceivedRptMsgTimestamp().toJava() > 0));
        checkEquals(() -> assertEquals(4, listenerState.getMessages()
                .augmentation(StatefulMessagesStatsAug.class).getReceivedRptMsgCount().intValue()));
        checkEquals(() -> assertEquals(2, listenerState.getMessages()
                .augmentation(StatefulMessagesStatsAug.class).getSentInitMsgCount().intValue()));
        checkEquals(() -> assertEquals(1, listenerState.getMessages()
                .augmentation(StatefulMessagesStatsAug.class).getSentUpdMsgCount().intValue()));
    }

    @Test
    public void testOnUnhandledErrorMessage() {
        final Message errorMsg = AbstractMessageParser.createErrorMsg(PCEPErrors.NON_ZERO_PLSPID, Optional.empty());
        this.listener.onSessionUp(this.session);
        assertTrue(this.listener.onMessage(Optional.<AbstractTopologySessionListener.MessageContext>empty()
                .orElse(null),
            errorMsg));
    }

    @Test
    public void testOnErrorMessage() throws InterruptedException, ExecutionException {
        final Message errorMsg = MsgBuilderUtil.createErrorMsg(PCEPErrors.NON_ZERO_PLSPID, Uint32.ONE);
        this.listener.onSessionUp(this.session);
        final Future<RpcResult<AddLspOutput>> futureOutput = this.topologyRpcs.addLsp(createAddLspInput());
        this.listener.onMessage(this.session, errorMsg);

        final AddLspOutput output = futureOutput.get().getResult();
        assertEquals(FailureType.Failed, output.getFailure());
        assertEquals(1, output.getError().size());
        final ErrorObject err = output.getError().get(0).getErrorObject();
        assertEquals(PCEPErrors.NON_ZERO_PLSPID.getErrorType(), err.getType());
        assertEquals(PCEPErrors.NON_ZERO_PLSPID.getErrorValue(), err.getValue());
    }

    @Test
    public void testOnSessionDown() throws InterruptedException, ExecutionException {
        this.listener.onSessionUp(this.session);
        // send request
        final Future<RpcResult<AddLspOutput>> futureOutput = this.topologyRpcs.addLsp(createAddLspInput());
        assertFalse(this.session.isClosed());
        this.listener.onSessionDown(this.session, new IllegalArgumentException());
        assertTrue(this.session.isClosed());
        final AddLspOutput output = futureOutput.get().getResult();
        // deal with unsent request after session down
        assertEquals(FailureType.Unsent, output.getFailure());
    }

    /**
     * All the pcep session registration should be closed when the session manager is closed.
     */
    @Test
    public void testOnServerSessionManagerDown() throws InterruptedException, ExecutionException {
        this.listener.onSessionUp(this.session);
        // the session should not be closed when session manager is up
        assertFalse(this.session.isClosed());
        // send request
        final Future<RpcResult<AddLspOutput>> futureOutput = this.topologyRpcs.addLsp(createAddLspInput());
        stopSessionManager();
        final AddLspOutput output = futureOutput.get().getResult();
        // deal with unsent request after session down
        assertEquals(FailureType.Unsent, output.getFailure());
        // verify the session is closed after server session manager is closed
        assertTrue(this.session.isClosed());
    }

    /**
     * Verify the PCEP session should not be up when server session manager is down,
     * otherwise it would be a problem when the session is up while it's not registered with session manager.
     */
    @Test
    public void testOnServerSessionManagerUnstarted() throws InterruptedException, ExecutionException {
        stopSessionManager();
        assertFalse(this.session.isClosed());
        this.listener.onSessionUp(this.session);
        // verify the session was NOT added to topology
        checkNotPresentOperational(getDataBroker(), TOPO_IID);
        // verify the session is closed due to server session manager is closed
        assertTrue(this.session.isClosed());
        // send request
        final Future<RpcResult<AddLspOutput>> futureOutput = this.topologyRpcs.addLsp(createAddLspInput());
        final AddLspOutput output = futureOutput.get().getResult();
        // deal with unsent request after session down
        assertEquals(FailureType.Unsent, output.getFailure());
    }

    @Test
    public void testOnServerSessionManagerRestartAndSessionRecovery() throws Exception {
        // close server session manager first
        stopSessionManager();
        assertFalse(this.session.isClosed());
        this.listener.onSessionUp(this.session);
        // verify the session was NOT added to topology
        checkNotPresentOperational(getDataBroker(), TOPO_IID);
        // verify the session is closed due to server session manager is closed
        assertTrue(this.session.isClosed());
        // send request
        final Future<RpcResult<AddLspOutput>> futureOutput = this.topologyRpcs.addLsp(createAddLspInput());
        final AddLspOutput output = futureOutput.get().getResult();
        // deal with unsent request after session down
        assertEquals(FailureType.Unsent, output.getFailure());
        // PCC client is not there
        checkNotPresentOperational(getDataBroker(), this.pathComputationClientIId);

        // reset received message queue
        this.receivedMsgs.clear();
        // now we restart the session manager
        startSessionManager();
        // try to start the session again
        // notice since the session was terminated before, it is not usable anymore.
        // we need to get a new session instance. the new session will have the same local / remote preference
        this.session = getPCEPSession(getLocalPref(), getRemotePref());
        assertFalse(this.session.isClosed());
        this.listener.onSessionUp(this.session);
        assertFalse(this.session.isClosed());

        // create node
        this.topologyRpcs.addLsp(createAddLspInput());
        final Pcinitiate pcinitiate = (Pcinitiate) this.receivedMsgs.get(0);
        final Requests req = pcinitiate.getPcinitiateMessage().getRequests().get(0);
        final Uint32 srpId = req.getSrp().getOperationId().getValue();
        final Tlvs tlvs = createLspTlvs(req.getLsp().getPlspId().getValue(), true,
                this.testAddress, this.testAddress, this.testAddress, Optional.empty());
        final Pcrpt pcRpt = MsgBuilderUtil.createPcRtpMessage(new LspBuilder(req.getLsp()).setTlvs(tlvs).setSync(TRUE)
                        .setRemove(FALSE).setOperational(OperationalStatus.Active).build(),
                Optional.of(MsgBuilderUtil.createSrp(srpId)), MsgBuilderUtil.createPath(req.getEro().getSubobject()));
        this.listener.onMessage(this.session, pcRpt);
        readDataOperational(getDataBroker(), TOPO_IID, topology -> {
            assertEquals(1, topology.getNode().size());
            return topology;
        });
    }

    /**
     * When a session is somehow duplicated in controller, the controller should drop existing session.
     */
    @Test
    public void testDuplicatedSession() throws ExecutionException, InterruptedException {
        this.listener.onSessionUp(this.session);

        // create node
        this.topologyRpcs.addLsp(createAddLspInput());
        final Pcinitiate pcinitiate = (Pcinitiate) this.receivedMsgs.get(0);
        final Requests req = pcinitiate.getPcinitiateMessage().getRequests().get(0);
        final Uint32 srpId = req.getSrp().getOperationId().getValue();
        final Tlvs tlvs = createLspTlvs(req.getLsp().getPlspId().getValue(), true,
                this.testAddress, this.testAddress, this.testAddress, Optional.empty());
        final Pcrpt pcRpt = MsgBuilderUtil.createPcRtpMessage(new LspBuilder(req.getLsp()).setTlvs(tlvs).setSync(TRUE)
                        .setRemove(FALSE).setOperational(OperationalStatus.Active).build(),
                Optional.of(MsgBuilderUtil.createSrp(srpId)), MsgBuilderUtil.createPath(req.getEro().getSubobject()));
        this.listener.onMessage(this.session, pcRpt);
        readDataOperational(getDataBroker(), TOPO_IID, topology -> {
            assertEquals(1, topology.getNode().size());
            return topology;
        });

        // now we do session up again
        this.listener.onSessionUp(this.session);
        assertTrue(this.session.isClosed());
        // node should be removed after termination
        checkNotPresentOperational(getDataBroker(), this.pathComputationClientIId);
        assertFalse(this.receivedMsgs.isEmpty());
        // the last message should be a Close message
        assertTrue(this.receivedMsgs.get(this.receivedMsgs.size() - 1) instanceof Close);
    }

    @Test
    public void testConflictingListeners() {
        this.listener.onSessionUp(this.session);
        assertFalse(this.session.isClosed());
        Stateful07TopologySessionListener conflictingListener =
                (Stateful07TopologySessionListener) getSessionListener();
        conflictingListener.onSessionUp(this.session);
        assertTrue(this.session.isClosed());
    }

    @Test
    public void testOnSessionTermination() throws Exception {
        this.listener.onSessionUp(this.session);
        // create node
        this.topologyRpcs.addLsp(createAddLspInput());
        final Pcinitiate pcinitiate = (Pcinitiate) this.receivedMsgs.get(0);
        final Requests req = pcinitiate.getPcinitiateMessage().getRequests().get(0);
        final Uint32 srpId = req.getSrp().getOperationId().getValue();
        final Tlvs tlvs = createLspTlvs(req.getLsp().getPlspId().getValue(), true,
                this.testAddress, this.testAddress, this.testAddress, Optional.empty());
        final Pcrpt pcRpt = MsgBuilderUtil.createPcRtpMessage(new LspBuilder(req.getLsp()).setTlvs(tlvs).setSync(TRUE)
                        .setRemove(FALSE).setOperational(OperationalStatus.Active).build(),
                Optional.of(MsgBuilderUtil.createSrp(srpId)), MsgBuilderUtil.createPath(req.getEro().getSubobject()));
        this.listener.onMessage(this.session, pcRpt);
        readDataOperational(getDataBroker(), TOPO_IID, topology -> {
            assertEquals(1, topology.getNode().size());
            return topology;
        });

        assertFalse(this.session.isClosed());
        // node should be removed after termination
        this.listener.onSessionTerminated(this.session, new PCEPCloseTermination(TerminationReason.UNKNOWN));
        assertTrue(this.session.isClosed());
        checkNotPresentOperational(getDataBroker(), this.pathComputationClientIId);
    }

    @Test
    public void testUnknownLsp() throws Exception {
        final List<Reports> reports = Lists.newArrayList(new ReportsBuilder()
            .setPath(new PathBuilder()
                .setEro(new EroBuilder().build())
                .build())
            .setLsp(new LspBuilder()
                .setPlspId(new PlspId(Uint32.valueOf(5)))
                .setSync(FALSE).setRemove(FALSE)
                .setTlvs(new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful
                    .rev181109.lsp.object.lsp.TlvsBuilder().setLspIdentifiers(new LspIdentifiersBuilder()
                        .setLspId(new LspId(Uint32.ONE))
                        .build())
                    .setSymbolicPathName(new SymbolicPathNameBuilder()
                        .setPathName(new SymbolicPathName(new byte[]{22, 34}))
                        .build())
                    .build())
                .build())
            .build());
        final Pcrpt rptmsg = new PcrptBuilder().setPcrptMessage(new PcrptMessageBuilder().setReports(reports).build())
                .build();
        this.listener.onSessionUp(this.session);
        this.listener.onMessage(this.session, rptmsg);
        readDataOperational(getDataBroker(), TOPO_IID, node -> {
            assertFalse(node.getNode().isEmpty());
            return node;
        });
    }

    @Test
    public void testUpdateUnknownLsp() throws InterruptedException, ExecutionException {
        this.listener.onSessionUp(this.session);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev200120.update.lsp.args
                .ArgumentsBuilder updArgsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang
                .topology.pcep.rev200120.update.lsp.args.ArgumentsBuilder();
        updArgsBuilder.setEro(createEroWithIpPrefixes(Lists.newArrayList(this.eroIpPrefix, this.dstIpPrefix)));
        updArgsBuilder.addAugmentation(Arguments3.class, new Arguments3Builder().setLsp(new LspBuilder()
                .setDelegate(TRUE).setAdministrative(TRUE).build()).build());
        final UpdateLspInput update = new UpdateLspInputBuilder().setArguments(updArgsBuilder.build())
                .setName(this.tunnelName).setNetworkTopologyRef(new NetworkTopologyRef(TOPO_IID)).setNode(this.nodeId)
                .build();
        final UpdateLspOutput result = this.topologyRpcs.updateLsp(update).get().getResult();
        assertEquals(FailureType.Unsent, result.getFailure());
        assertEquals(1, result.getError().size());
        final ErrorObject errorObject = result.getError().get(0).getErrorObject();
        assertNotNull(errorObject);
        assertEquals(PCEPErrors.UNKNOWN_PLSP_ID, PCEPErrors.forValue(errorObject.getType(), errorObject.getValue()));
    }

    @Test
    public void testRemoveUnknownLsp() throws InterruptedException, ExecutionException {
        this.listener.onSessionUp(this.session);
        final RemoveLspInput remove = new RemoveLspInputBuilder().setName(this.tunnelName).setNetworkTopologyRef(
                new NetworkTopologyRef(TOPO_IID)).setNode(this.nodeId).build();
        final OperationResult result = this.topologyRpcs.removeLsp(remove).get().getResult();
        assertEquals(FailureType.Unsent, result.getFailure());
        assertEquals(1, result.getError().size());
        final ErrorObject errorObject = result.getError().get(0).getErrorObject();
        assertNotNull(errorObject);
        assertEquals(PCEPErrors.UNKNOWN_PLSP_ID, PCEPErrors.forValue(errorObject.getType(), errorObject.getValue()));
    }

    @Test
    public void testAddAlreadyExistingLsp() throws InterruptedException, ExecutionException {
        this.listener.onSessionUp(this.session);
        this.topologyRpcs.addLsp(createAddLspInput());
        assertEquals(1, this.receivedMsgs.size());
        assertTrue(this.receivedMsgs.get(0) instanceof Pcinitiate);
        final Pcinitiate pcinitiate = (Pcinitiate) this.receivedMsgs.get(0);
        final Requests req = pcinitiate.getPcinitiateMessage().getRequests().get(0);
        final Uint32 srpId = req.getSrp().getOperationId().getValue();
        final Tlvs tlvs = createLspTlvs(req.getLsp().getPlspId().getValue(), true,
                this.testAddress, this.testAddress, this.testAddress, Optional.empty());
        final Pcrpt pcRpt = MsgBuilderUtil.createPcRtpMessage(new LspBuilder(req.getLsp()).setTlvs(tlvs)
                .setPlspId(new PlspId(Uint32.ONE))
                .setSync(FALSE)
                .setRemove(FALSE)
                .setOperational(OperationalStatus.Active)
                .build(), Optional.of(MsgBuilderUtil.createSrp(srpId)), MsgBuilderUtil.createPath(req.getEro()
                .getSubobject()));
        this.listener.onMessage(this.session, pcRpt);

        //try to add already existing LSP
        final AddLspOutput result = this.topologyRpcs.addLsp(createAddLspInput()).get().getResult();
        assertEquals(FailureType.Unsent, result.getFailure());
        assertEquals(1, result.getError().size());
        final ErrorObject errorObject = result.getError().get(0).getErrorObject();
        assertNotNull(errorObject);
        assertEquals(PCEPErrors.USED_SYMBOLIC_PATH_NAME, PCEPErrors.forValue(errorObject.getType(),
                errorObject.getValue()));
    }

    @Test
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void testPccResponseTimeout() throws Exception {
        this.listener.onSessionUp(this.session);
        final Future<RpcResult<AddLspOutput>> addLspResult = this.topologyRpcs.addLsp(createAddLspInput());
        try {
            addLspResult.get(2, TimeUnit.SECONDS);
            fail();
        } catch (final Exception e) {
            assertTrue(e instanceof TimeoutException);
        }
        Thread.sleep(AbstractPCEPSessionTest.RPC_TIMEOUT);
        CheckTestUtil.checkEquals(() -> {
            final RpcResult<AddLspOutput> rpcResult = addLspResult.get();
            assertNotNull(rpcResult);
            assertEquals(rpcResult.getResult().getFailure(), FailureType.Unsent);
        });
    }

    @Test
    public void testDelegatedLspsCountWithDelegation() throws Exception {
        this.listener.onSessionUp(this.session);
        this.topologyRpcs.addLsp(createAddLspInput());
        assertEquals(1, this.receivedMsgs.size());
        assertTrue(this.receivedMsgs.get(0) instanceof Pcinitiate);
        final Pcinitiate pcinitiate = (Pcinitiate) this.receivedMsgs.get(0);
        final Requests req = pcinitiate.getPcinitiateMessage().getRequests().get(0);
        final Uint32 srpId = req.getSrp().getOperationId().getValue();
        final Tlvs tlvs = createLspTlvs(req.getLsp().getPlspId().getValue(), true,
                this.testAddress, this.testAddress, this.testAddress, Optional.empty());
        //delegate set to true
        final Pcrpt pcRpt = MsgBuilderUtil.createPcRtpMessage(new LspBuilder(req.getLsp()).setTlvs(tlvs)
                .setPlspId(new PlspId(Uint32.ONE))
                .setSync(FALSE)
                .setRemove(FALSE)
                .setOperational(OperationalStatus.Active)
                .setDelegate(TRUE)
                .build(), Optional.of(MsgBuilderUtil.createSrp(srpId)), MsgBuilderUtil.createPath(
                req.getEro().getSubobject()));
        this.listener.onMessage(this.session, pcRpt);
        checkEquals(() -> assertEquals(1, this.listener.listenerState.getDelegatedLspsCount().intValue()));
    }

    @Test
    public void testDelegatedLspsCountWithoutDelegation() throws Exception {
        this.listener.onSessionUp(this.session);
        this.topologyRpcs.addLsp(createAddLspInput());
        assertEquals(1, this.receivedMsgs.size());
        assertTrue(this.receivedMsgs.get(0) instanceof Pcinitiate);
        final Pcinitiate pcinitiate = (Pcinitiate) this.receivedMsgs.get(0);
        final Requests req = pcinitiate.getPcinitiateMessage().getRequests().get(0);
        final Uint32 srpId = req.getSrp().getOperationId().getValue();
        final Tlvs tlvs = createLspTlvs(req.getLsp().getPlspId().getValue(), true,
                this.testAddress, this.testAddress, this.testAddress, Optional.empty());
        //delegate set to false
        final Pcrpt pcRpt = MsgBuilderUtil.createPcRtpMessage(new LspBuilder(req.getLsp()).setTlvs(tlvs)
                        .setPlspId(new PlspId(Uint32.ONE))
                        .setSync(FALSE)
                        .setRemove(FALSE)
                        .setOperational(OperationalStatus.Active)
                        .setDelegate(FALSE)
                        .build(), Optional.of(MsgBuilderUtil.createSrp(srpId)),
                MsgBuilderUtil.createPath(req.getEro().getSubobject()));
        this.listener.onMessage(this.session, pcRpt);
        checkEquals(() -> assertEquals(0, this.listener.listenerState.getDelegatedLspsCount().intValue()));
    }

    @Override
    protected Open getLocalPref() {
        return new OpenBuilder(super.getLocalPref()).setTlvs(new TlvsBuilder().addAugmentation(Tlvs1.class,
                new Tlvs1Builder().setStateful(new StatefulBuilder()
                        .addAugmentation(Stateful1.class, new Stateful1Builder().setInitiation(TRUE).build())
                        .addAugmentation(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller
                                .pcep.sync.optimizations.rev181109.Stateful1.class, new org.opendaylight.yang.gen.v1
                                .urn.opendaylight.params.xml.ns.yang.controller.pcep.sync.optimizations.rev181109
                                .Stateful1Builder().setTriggeredInitialSync(TRUE).build())
                        .build()).build()).build()).build();
    }

    @Override
    protected Open getRemotePref() {
        return getLocalPref();
    }

    private AddLspInput createAddLspInput() {
        final ArgumentsBuilder argsBuilder = new ArgumentsBuilder();
        final Ipv4CaseBuilder ipv4Builder = new Ipv4CaseBuilder();
        ipv4Builder.setIpv4(new Ipv4Builder().setSourceIpv4Address(new Ipv4AddressNoZone(this.testAddress))
                .setDestinationIpv4Address(new Ipv4AddressNoZone(this.testAddress)).build());
        argsBuilder.setEndpointsObj(new EndpointsObjBuilder().setAddressFamily(ipv4Builder.build()).build());
        argsBuilder.setEro(createEroWithIpPrefixes(Lists.newArrayList(this.eroIpPrefix)));
        argsBuilder.addAugmentation(Arguments2.class, new Arguments2Builder().setLsp(new LspBuilder()
                .setDelegate(TRUE).setAdministrative(TRUE).build()).build());
        return new AddLspInputBuilder().setName(this.tunnelName).setArguments(argsBuilder.build())
                .setNetworkTopologyRef(new NetworkTopologyRef(TOPO_IID)).setNode(this.nodeId).build();
    }
}
