/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.rsvp.parser.impl.subobject.ero;

import static com.google.common.base.Preconditions.checkArgument;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.opendaylight.protocol.rsvp.parser.spi.EROSubobjectParser;
import org.opendaylight.protocol.rsvp.parser.spi.EROSubobjectSerializer;
import org.opendaylight.protocol.rsvp.parser.spi.EROSubobjectUtil;
import org.opendaylight.protocol.rsvp.parser.spi.RSVPParsingException;
import org.opendaylight.protocol.util.ByteArray;
import org.opendaylight.protocol.util.Ipv6Util;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev150820.IpPrefixSubobject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev150820.basic.explicit.route.subobjects.subobject.type.IpPrefixCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev150820.basic.explicit.route.subobjects.subobject.type.IpPrefixCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev150820.basic.explicit.route.subobjects.subobject.type.ip.prefix._case.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev150820.explicit.route.subobjects.list.SubobjectContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev150820.explicit.route.subobjects.list.SubobjectContainerBuilder;

/**
 * Parser for {@link IpPrefixCase}.
 */
public class EROIpv6PrefixSubobjectParser implements EROSubobjectParser, EROSubobjectSerializer {
    public static final int TYPE = 2;

    private static final int PREFIX_F_OFFSET = Ipv6Util.IPV6_LENGTH;
    private static final int RESERVED = 1;
    private static final int CONTENT_LENGTH = PREFIX_F_OFFSET + RESERVED + 1;

    @Override
    public SubobjectContainer parseSubobject(final ByteBuf buffer, final boolean loose) throws RSVPParsingException {
        checkArgument(buffer != null && buffer.isReadable(), "Array of bytes is mandatory. Can't be null or empty.");
        final SubobjectContainerBuilder builder = new SubobjectContainerBuilder();
        builder.setLoose(loose);
        if (buffer.readableBytes() != CONTENT_LENGTH) {
            throw new RSVPParsingException("Wrong length of array of bytes. Passed: " + buffer.readableBytes() + ";");
        }
        final int length = buffer.getUnsignedByte(PREFIX_F_OFFSET);
        final IpPrefixBuilder prefix = new IpPrefixBuilder().setIpPrefix(
            new IpPrefix(Ipv6Util.prefixForBytes(ByteArray.readBytes(buffer, Ipv6Util.IPV6_LENGTH), length)));
        builder.setSubobjectType(new IpPrefixCaseBuilder().setIpPrefix(prefix.build()).build());
        return builder.build();
    }

    @Override
    public void serializeSubobject(final SubobjectContainer subobject, final ByteBuf buffer) {
        checkArgument(subobject.getSubobjectType() instanceof IpPrefixCase,
            "Unknown subobject instance. Passed %s. Needed IpPrefixCase.", subobject.getSubobjectType().getClass());
        final IpPrefixSubobject specObj = ((IpPrefixCase) subobject.getSubobjectType()).getIpPrefix();
        final Ipv6Prefix ipv6prefix = specObj.getIpPrefix().getIpv6Prefix();
        checkArgument(ipv6prefix != null, "Ipv6Prefix is mandatory.");
        serializeSubobject(buffer, subobject, ipv6prefix);
    }

    static void serializeSubobject(final ByteBuf buffer, final SubobjectContainer subobject,
            final Ipv6Prefix ipv6prefix) {
        final ByteBuf body = Unpooled.buffer();
        Ipv6Util.writeIpv6Prefix(ipv6prefix, body);
        body.writeZero(RESERVED);
        EROSubobjectUtil.formatSubobject(TYPE, subobject.isLoose(), body, buffer);
    }
}
