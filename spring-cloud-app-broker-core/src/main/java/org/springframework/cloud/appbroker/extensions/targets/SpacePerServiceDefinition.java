/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.appbroker.extensions.targets;

import org.springframework.cloud.appbroker.deployer.DeploymentProperties;

import java.util.Map;

public class SpacePerServiceDefinition extends TargetFactory<SpacePerServiceDefinition.Config> {

	public SpacePerServiceDefinition() {
		super(Config.class);
	}

	@Override
	public Target create(Config config) {
		return this::apply;
	}

	protected ArtifactDetails apply(Map<String, String> properties, String name,
		String brokeredServiceInstanceId,
		String backingServiceName, String backingServicePlanName) {
		properties.put(DeploymentProperties.TARGET_PROPERTY_KEY, backingServiceName);
		//space names are unlimited in CF
		properties.put(DeploymentProperties.KEEP_TARGET_ON_DELETE_PROPERTY_KEY, "true");

		return ArtifactDetails.builder()
			.name(ServiceInstanceNameHelper.truncateNameToCfMaxSize(brokeredServiceInstanceId))
			.properties(properties)
			.build();
	}

	static class Config {
	}

}
