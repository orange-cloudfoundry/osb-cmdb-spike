package com.orange.oss.osbcmdb.testfixtures;

import com.orange.oss.osbcmdb.serviceinstance.ServiceInstanceInterceptor;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.instance.OperationState;

/**
 * Simulates a failed async backing service requested in backing space: create always fails, delete always succeeds
 * (synchronously)
 *
 * Only accept OSB calls when space is a backing space, i.e. not the default space
 */
public class ASyncFailedCreateBackingSpaceInstanceInterceptor extends BaseServiceInstanceBackingSpaceInstanceInterceptor
	implements ServiceInstanceInterceptor {

	private static final Logger LOG = Loggers.getLogger(ASyncFailedCreateBackingSpaceInstanceInterceptor.class);

	public ASyncFailedCreateBackingSpaceInstanceInterceptor(String defaultSpaceName) {
		super(defaultSpaceName);
	}

	@Override
	public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
		provisionnedInstanceGuids.add(request.getServiceInstanceId());
		provisionnedInstanceParams.put(request.getServiceInstanceId(), request.getParameters());
		return Mono.just(CreateServiceInstanceResponse.builder()
			.async(true)
			.build());
	}

	@Override
	public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
		return Mono.just(GetLastServiceOperationResponse.builder()
			.operationState(OperationState.FAILED)
			.description(this.getClass().getSimpleName())
			.build());
	}

}
