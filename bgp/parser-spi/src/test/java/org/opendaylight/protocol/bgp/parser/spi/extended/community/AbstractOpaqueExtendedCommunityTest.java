/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.parser.spi.extended.community;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev200120.extended.community.ExtendedCommunity;

public class AbstractOpaqueExtendedCommunityTest {

    @Test
    public void testGetType() {
        final AbstractOpaqueExtendedCommunity abstractOpaqueExtendedCommunity = new AbstractOpaqueExtendedCommunity() {
            @Override
            public void serializeExtendedCommunity(final ExtendedCommunity extendedCommunity,
                    final ByteBuf byteAggregator) {
            }

            @Override
            public int getSubType() {
                return 0;
            }

            @Override
            public ExtendedCommunity parseExtendedCommunity(final ByteBuf buffer) {
                return null;
            }
        };
        Assert.assertEquals(3, abstractOpaqueExtendedCommunity.getType(true));
        Assert.assertEquals(67, abstractOpaqueExtendedCommunity.getType(false));
    }
}
