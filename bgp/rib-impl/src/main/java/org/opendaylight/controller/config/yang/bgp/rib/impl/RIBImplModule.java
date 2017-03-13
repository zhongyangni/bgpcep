/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Generated file

 * Generated from: yang module name: bgp-rib-impl  yang module local name: rib-impl
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Wed Nov 06 13:02:32 CET 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.bgp.rib.impl;

import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opendaylight.controller.config.api.JmxAttributeValidationException;
import org.opendaylight.controller.config.api.osgi.WaitingServiceTracker;
import org.opendaylight.protocol.bgp.mode.api.PathSelectionMode;
import org.opendaylight.protocol.bgp.rib.impl.config.OpenConfigMappingUtil;
import org.opendaylight.protocol.bgp.rib.impl.spi.BGPBestPathSelection;
import org.opendaylight.protocol.bgp.rib.impl.spi.BgpDeployer;
import org.opendaylight.protocol.bgp.rib.impl.spi.InstanceType;
import org.opendaylight.protocol.bgp.rib.impl.spi.RIB;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev151018.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev151018.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev151018.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.policy.types.rev151009.BGP;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.openconfig.extensions.rev160614.Protocol1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.TablesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;

/**
 *
 */
@Deprecated
public final class RIBImplModule extends org.opendaylight.controller.config.yang.bgp.rib.impl.AbstractRIBImplModule {

    private static final String IS_NOT_SET = "is not set.";
    private BundleContext bundleContext;

    public RIBImplModule(final org.opendaylight.controller.config.api.ModuleIdentifier name,
            final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(name, dependencyResolver);
    }

    public RIBImplModule(final org.opendaylight.controller.config.api.ModuleIdentifier name,
            final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, final RIBImplModule oldModule,
            final java.lang.AutoCloseable oldInstance) {
        super(name, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        JmxAttributeValidationException.checkNotNull(getExtensions(), IS_NOT_SET, extensionsJmxAttribute);
        JmxAttributeValidationException.checkNotNull(getRibId(), IS_NOT_SET, ribIdJmxAttribute);
        JmxAttributeValidationException.checkNotNull(getLocalAs(), IS_NOT_SET, localAsJmxAttribute);
        JmxAttributeValidationException.checkNotNull(getBgpRibId(), IS_NOT_SET, bgpRibIdJmxAttribute);
        JmxAttributeValidationException.checkNotNull(getLocalTable(), IS_NOT_SET, localTableJmxAttribute);
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final WaitingServiceTracker<BgpDeployer> bgpDeployerTracker =
                WaitingServiceTracker.create(BgpDeployer.class, this.bundleContext);
        final BgpDeployer bgpDeployer = bgpDeployerTracker.waitForService(WaitingServiceTracker.FIVE_MINUTES);
        //map configuration to OpenConfig BGP
        final Global global = OpenConfigMappingUtil.fromRib(getBgpRibId(), getClusterId(), getRibId(),
            new AsNumber(getLocalAs()), getLocalTableDependency(),
                mapBestPathSelectionStrategyByFamily(getRibPathSelectionModeDependency()), bgpDeployer.getTableTypeRegistry());
        final InstanceIdentifier<Bgp> bgpIID = bgpDeployer.getInstanceIdentifier().child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(BGP.class, getRibId().getValue())).augmentation(Protocol1.class)
            .child(Bgp.class);
        bgpDeployer.onGlobalModified(bgpIID, global, () -> bgpDeployer.writeConfiguration(global, bgpIID.child(Global.class)));

        //get rib instance service, use filter
        final WaitingServiceTracker<RIB> ribTracker = WaitingServiceTracker.create(RIB.class,
            this.bundleContext, "(" + InstanceType.RIB.getBeanName() + "=" + getRibId().getValue() + ")");
        final RIB rib = ribTracker.waitForService(WaitingServiceTracker.FIVE_MINUTES);
        final RIBImplRuntimeRegistration register = getRootRuntimeBeanRegistratorWrapper().register(rib.getRenderStats());

        return Reflection.newProxy(AutoCloseableRIB.class, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(final Object proxy, final Method method, final Object[] args) throws Throwable {
                if (method.getName().equals("close")) {
                    bgpDeployer.onGlobalRemoved(bgpIID);
                    register.close();
                    bgpDeployerTracker.close();
                    ribTracker.close();
                    return null;
                } else {
                    return method.invoke(rib, args);
                }
            }
        });
    }

    public void setBundleContext(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private Map<TablesKey, PathSelectionMode> mapBestPathSelectionStrategyByFamily(final List<BGPBestPathSelection> bestPathSelectionDependency) {
        return Collections.unmodifiableMap(bestPathSelectionDependency.stream().collect(
                Collectors.toMap(st -> new TablesKey(st.getAfi(), st.getSafi()), BGPBestPathSelection::getStrategy)));
    }

    private static interface AutoCloseableRIB extends RIB, AutoCloseable {
    }


}
