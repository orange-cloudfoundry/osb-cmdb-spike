package com.orange.oss.osbcmdb;

import com.orange.oss.osbcmdb.metadata.CreateServiceMetadataFormatterServiceImpl;
import com.orange.oss.osbcmdb.metadata.UpdateServiceMetadataFormatterService;
import com.orange.oss.osbcmdb.testfixtures.ASyncFailedBackingSpaceInstanceInterceptor;
import com.orange.oss.osbcmdb.testfixtures.SyncFailedCreateBackingSpaceInstanceInterceptor;
import com.orange.oss.osbcmdb.testfixtures.SyncFailedUpdateBackingSpaceInstanceInterceptor;
import com.orange.oss.osbcmdb.testfixtures.SyncSuccessfullBackingSpaceInstanceInterceptor;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!offline-test-without-scab")
@Configuration
public class OsbCmdbBrokerConfiguration {


	/**
	 * Provide a {@link OsbCmdbServiceInstance} bean
	 *
	 * @param cloudFoundryOperations the CloudFoundryOperations bean
	 * @param cloudFoundryClient the CloudFoundryClient bean
	 * @param targetProperties the CloudFoundryTargetProperties bean
	 * @param serviceInstanceInterceptor
	 * @return the bean
	 */
	@Bean
	public OsbCmdbServiceInstance osbCmdbServiceInstance(CloudFoundryOperations cloudFoundryOperations,
		CloudFoundryClient cloudFoundryClient,
		CloudFoundryTargetProperties targetProperties,
		@Autowired(required = false)
			ServiceInstanceInterceptor serviceInstanceInterceptor) {
		return new OsbCmdbServiceInstance(cloudFoundryOperations, cloudFoundryClient,
			targetProperties.getDefaultOrg(), targetProperties.getDefaultSpace(), targetProperties.getUsername(),
			serviceInstanceInterceptor, new CreateServiceMetadataFormatterServiceImpl(),
			new UpdateServiceMetadataFormatterService());
	}

	@Bean
	@Profile("acceptanceTests & ASyncFailedBackingSpaceInstanceInterceptor")
	public ServiceInstanceInterceptor acceptanceTestFailedAsyncBackingServiceInstanceInterceptor(CloudFoundryTargetProperties targetProperties) {
		return new ASyncFailedBackingSpaceInstanceInterceptor(targetProperties.getDefaultSpace());
	}

	@Bean
	@Profile("acceptanceTests & SyncFailedCreateBackingSpaceInstanceInterceptor")
	public ServiceInstanceInterceptor acceptanceTestSyncFailedCreateBackingServiceInstanceInterceptor(CloudFoundryTargetProperties targetProperties) {
		return new SyncFailedCreateBackingSpaceInstanceInterceptor(targetProperties.getDefaultSpace());
	}

	@Bean
	@Profile("acceptanceTests & SyncFailedUpdateBackingSpaceInstanceInterceptor")
	public ServiceInstanceInterceptor acceptanceTestSyncFailedUpdateBackingServiceInstanceInterceptor(CloudFoundryTargetProperties targetProperties) {
		return new SyncFailedUpdateBackingSpaceInstanceInterceptor(targetProperties.getDefaultSpace());
	}

	@Bean
	@ConditionalOnMissingBean //other methods declaring beans must be declare before in the class!!
	@Profile("acceptanceTests")
	//	@Profile("SyncSuccessfullBackingSpaceInstanceInterceptor") // Default impl unless another profile is enabled
	//	another bean
	public ServiceInstanceInterceptor acceptanceTestBackingServiceInstanceInterceptor(CloudFoundryTargetProperties targetProperties) {
		return new SyncSuccessfullBackingSpaceInstanceInterceptor(targetProperties.getDefaultSpace());
	}

	@Bean
	@Profile("acceptanceTests")
	@ConditionalOnMissingBean
	public ServiceBindingInterceptor noopServiceBindingInterceptor(CloudFoundryTargetProperties targetProperties) {
		return new BackingServiceBindingInterceptor(targetProperties.getDefaultSpace());
	}

	/**
	 * Provide a {@link OsbCmdbServiceBinding} bean
	 *
	 * @param cloudFoundryOperations the CloudFoundryOperations bean
	 * @param cloudFoundryClient the CloudFoundryClient bean
	 * @param targetProperties the CloudFoundryTargetProperties bean
	 * @param serviceBindingInterceptor
	 * @return the bean
	 */
	@Bean
	public OsbCmdbServiceBinding osbCmdbServiceBinding(
		CloudFoundryOperations cloudFoundryOperations,
		CloudFoundryClient cloudFoundryClient,
		CloudFoundryTargetProperties targetProperties,
		@Autowired(required = false)
			ServiceBindingInterceptor serviceBindingInterceptor) {
		return new OsbCmdbServiceBinding(cloudFoundryClient, targetProperties.getDefaultOrg(),
			targetProperties.getUsername(), cloudFoundryOperations, serviceBindingInterceptor);
	}


}
