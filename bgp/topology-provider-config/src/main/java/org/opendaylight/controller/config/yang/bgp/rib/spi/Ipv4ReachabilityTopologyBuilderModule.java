/**
 * Generated file

 * Generated from: yang module name: config-bgp-topology-provider  yang module local name: bgp-reachability-ipv4
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Tue Nov 19 15:13:57 CET 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.bgp.rib.spi;

import org.opendaylight.bgpcep.topology.DefaultTopologyReference;
import org.opendaylight.bgpcep.topology.provider.bgp.AbstractTopologyBuilder;
import org.opendaylight.bgpcep.topology.provider.bgp.Ipv4ReachabilityTopologyBuilder;
import org.opendaylight.controller.config.api.JmxAttributeValidationException;
import org.opendaylight.controller.sal.binding.api.data.DataChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.Tables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.Ipv4AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.UnicastSubsequentAddressFamily;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


/**
 *
 */
public final class Ipv4ReachabilityTopologyBuilderModule extends org.opendaylight.controller.config.yang.bgp.rib.spi.AbstractIpv4ReachabilityTopologyBuilderModule
{

	public Ipv4ReachabilityTopologyBuilderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier, final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
		super(identifier, dependencyResolver);
	}

	public Ipv4ReachabilityTopologyBuilderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier, final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, final Ipv4ReachabilityTopologyBuilderModule oldModule, final java.lang.AutoCloseable oldInstance) {
		super(identifier, dependencyResolver, oldModule, oldInstance);
	}

	@Override
	public void validate(){
		super.validate();
		JmxAttributeValidationException.checkNotNull(getTopologyId(),
				"is not set.", topologyIdJmxAttribute);
	}

	@Override
	public java.lang.AutoCloseable createInstance() {
		final Ipv4ReachabilityTopologyBuilder b = new Ipv4ReachabilityTopologyBuilder(getDataProviderDependency(), getTopologyId());
		final InstanceIdentifier<Tables> i = AbstractTopologyBuilder.tableInstanceIdentifier(Ipv4AddressFamily.class, UnicastSubsequentAddressFamily.class);
		final ListenerRegistration<DataChangeListener> r = getDataProviderDependency().registerDataChangeListener(i, b);

		final class TopologyReferenceAutocloseable extends DefaultTopologyReference implements AutoCloseable {
			public TopologyReferenceAutocloseable(final InstanceIdentifier<Topology> instanceIdentifier) {
				super(instanceIdentifier);
			}

			@Override
			public void close() throws Exception {
				try {
					r.close();
				} finally {
					b.close();
				}
			}
		}

		return new TopologyReferenceAutocloseable(b.getInstanceIdentifier());
	}
}
