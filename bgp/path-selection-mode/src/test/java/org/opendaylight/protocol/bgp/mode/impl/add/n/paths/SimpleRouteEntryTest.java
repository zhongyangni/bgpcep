/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.mode.impl.add.n.paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.protocol.bgp.mode.impl.AbstractRouteEntryTest;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public final class SimpleRouteEntryTest extends AbstractRouteEntryTest {
    private static final long N_PATHS = 2;
    private SimpleRouteEntry testBARE;

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testSimpleRouteEntry() throws Exception {
        this.testBARE = (SimpleRouteEntry) new AddPathBestNPathSelection(N_PATHS).createRouteEntry(false);
        testWriteEmptyBestPath();
        testAddRouteSelectBestAndWriteOnDS();
        testRewriteSameRoute();
        testInitializePeerWithExistentRoute();
        testRemoveRoute();
    }

    @Test(expected = NullPointerException.class)
    public void testAddRouteSelectBestAndWriteOnDSs() {
        /** Add non Add Path Route **/
        this.testBARE.addRoute(ROUTER_ID, REMOTE_PATH_ID, this.ribSupport.routeAttributesIdentifier(), this.attributes);
    }

    private void testWriteEmptyBestPath() {
        this.testBARE.writeRoute(PEER_ID, ROUTE_ID_PA, PEER_YII2, this.peg, TABLES_KEY, this.peerPT, this.ribSupport, this.tx);
        assertEquals(0, this.yIIChanges.size());
    }

    private void testAddRouteSelectBestAndWriteOnDS() {
        this.testBARE.addRoute(ROUTER_ID, REMOTE_PATH_ID, this.ribSupport.routeAttributesIdentifier(), this.attributes);
        assertFalse(this.testBARE.isEmpty());
        assertTrue(this.testBARE.selectBest(AS));
        /** Add AddPath Route **/
        this.testBARE.updateRoute(TABLES_KEY, this.peerPT, LOC_RIB_TARGET, this.ribSupport, this.tx, ROUTE_ID_PA_ADD_PATH);
        Map<YangInstanceIdentifier, Long> yiiCount = this.yIIChanges.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(3, yiiCount.size());
        assertEquals(1, (long) yiiCount.get(this.routePaAddPathYii));
        assertEquals(1, (long) yiiCount.get(this.routeAddRiboutYii));
        assertEquals(1, (long) yiiCount.get(this.routeAddRiboutAttYii));
    }

    private void testRewriteSameRoute() {
        this.testBARE.addRoute(ROUTER_ID, REMOTE_PATH_ID, this.ribSupport.routeAttributesIdentifier(), this.attributes);
        assertFalse(this.testBARE.selectBest(AS));
    }

    private void testInitializePeerWithExistentRoute() {
        assertEquals(3, this.yIIChanges.size());
        this.testBARE.writeRoute(PEER_ID, ROUTE_ID_PA_ADD_PATH, PEER_YII2, this.peg, TABLES_KEY, this.peerPT, this.ribSupport, this.tx);
        assertEquals(5, this.yIIChanges.size());
        final Map<YangInstanceIdentifier, Long> yiiCount = this.yIIChanges.stream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(1, (long) yiiCount.get(this.routeAddRiboutYiiPeer2));
        assertEquals(1, (long) yiiCount.get(this.routeAddRiboutYiiPeer2));
    }

    private void testRemoveRoute() {
        Map<YangInstanceIdentifier, Long> yiiCount = this.yIIChanges.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(5, yiiCount.size());
        assertEquals(1, (long) yiiCount.get(this.routePaAddPathYii));
        assertTrue(this.testBARE.removeRoute(ROUTER_ID, REMOTE_PATH_ID));
        assertTrue(this.testBARE.selectBest(AS));
        this.testBARE.updateRoute(TABLES_KEY, this.peerPT, LOC_RIB_TARGET, this.ribSupport, this.tx, ROUTE_ID_PA_ADD_PATH);
        yiiCount = this.yIIChanges.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(2, yiiCount.size());
        assertFalse(yiiCount.containsKey(this.routePaAddPathYii));
        assertFalse(yiiCount.containsKey(this.routeAddRiboutYii));
    }
}
