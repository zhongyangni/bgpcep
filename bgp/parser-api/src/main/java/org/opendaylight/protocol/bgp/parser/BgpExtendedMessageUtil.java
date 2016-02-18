/*
 * Copyright (c) 2016 AT&T Services, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.parser;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.Open;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.open.message.BgpParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.open.message.bgp.parameters.OptionalCapabilities;

public final class BgpExtendedMessageUtil {

    private BgpExtendedMessageUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Looks for Bgp Extended Message Capability in capabilities .
     *
     * @param open remote BGP open message
     * @return flag
     */
    public static boolean advertizedBgpExtendedMessageCapability(final Open open) {
        // Look for Bgp Extended Message capability very defensively
        final List<BgpParameters> params = open.getBgpParameters();
        if (params != null) {
            for (final BgpParameters p : params) {
                for (final OptionalCapabilities oc : p.getOptionalCapabilities()) {
                    if (oc.getCParameters() != null && oc.getCParameters().getBgpExtendedMessageCapability() != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}