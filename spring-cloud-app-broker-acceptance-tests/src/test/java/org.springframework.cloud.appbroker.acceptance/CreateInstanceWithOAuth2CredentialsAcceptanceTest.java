/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.appbroker.acceptance;

import com.jayway.jsonpath.DocumentContext;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.services.ServiceInstanceSummary;
import org.cloudfoundry.uaa.clients.GetClientResponse;
import org.cloudfoundry.uaa.tokens.GrantType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled("This test can only be run with a Cloud Foundry user or client that has 'client.write' authority, " +
	"so it should not be run in CI")
class CreateInstanceWithOAuth2CredentialsAcceptanceTest extends CloudFoundryAcceptanceTest {

	private static final String APP_NAME = "app-create-oauth2";
	private static final String SI_NAME = "si-create-oauth2";

	@Test
	@AppBrokerTestProperties({
		"spring.cloud.appbroker.services[0].service-name=example",
		"spring.cloud.appbroker.services[0].plan-name=standard",
		"spring.cloud.appbroker.services[0].apps[0].name=" + APP_NAME,
		"spring.cloud.appbroker.services[0].apps[0].path=classpath:demo.jar",
		
		"spring.cloud.appbroker.services[0].apps[0].credential-providers[0].name=SpringSecurityOAuth2",
		"spring.cloud.appbroker.services[0].apps[0].credential-providers[0].args.registration=sample-app-client",
		"spring.cloud.appbroker.services[0].apps[0].credential-providers[0].args.grant-types=[\"client_credentials\"]",
		"spring.cloud.appbroker.services[0].apps[0].credential-providers[0].args.authorities=[\"uaa.resource\"]",
		"spring.cloud.appbroker.services[0].apps[0].credential-providers[0].args.length=12",
		"spring.cloud.appbroker.services[0].apps[0].credential-providers[0].args.include-uppercase-alpha=false",
		"spring.cloud.appbroker.services[0].apps[0].credential-providers[0].args.include-lowercase-alpha=true",
		"spring.cloud.appbroker.services[0].apps[0].credential-providers[0].args.include-numeric=false",
		"spring.cloud.appbroker.services[0].apps[0].credential-providers[0].args.include-special=false"
	})
	void deployAppsWithOAuth2OnCreateService() {
		// when a service instance is created
		createServiceInstance(SI_NAME);

		Optional<ServiceInstanceSummary> serviceInstance = getServiceInstance(SI_NAME);
		assertThat(serviceInstance).hasValueSatisfying(value ->
			assertThat(value.getLastOperation()).contains("completed"));

		String serviceInstanceGuid = serviceInstance
			.map(ServiceInstanceSummary::getId)
			.orElse("unknown");

		// then a backing application is deployed
		Optional<ApplicationSummary> backingApplication = getApplicationSummaryByName(APP_NAME);
		assertThat(backingApplication).hasValueSatisfying(app ->
			assertThat(app.getRunningInstances()).isEqualTo(1));

		// and has the environment variables
		DocumentContext json = getSpringAppJsonByName(APP_NAME);
		assertThat(json).jsonPathAsString("$.spring.security.oauth2.client.registration.sample-app-client.client-id")
			.isEqualTo(uaaClientId(serviceInstanceGuid));
		assertThat(json).jsonPathAsString("$.spring.security.oauth2.client.registration.sample-app-client.client-secret")
			.matches("[a-zA-Z]{12}");

		// and a UAA client is created
		Optional<GetClientResponse> uaaClient = getUaaClient(uaaClientId(serviceInstanceGuid));
		assertThat(uaaClient).hasValueSatisfying(client -> {
			assertThat(client.getAuthorities()).contains("uaa.resource");
			assertThat(client.getAuthorizedGrantTypes()).contains(GrantType.CLIENT_CREDENTIALS);
		});

		// when the service instance is deleted
		deleteServiceInstance(SI_NAME);

		// then the backing application is deleted
		Optional<ApplicationSummary> backingApplicationAfterDeletion = getApplicationSummaryByName(APP_NAME);
		assertThat(backingApplicationAfterDeletion).isEmpty();

		// and the UAA client is deleted
		Optional<GetClientResponse> uaaClientAfterDeletion = getUaaClient(uaaClientId(serviceInstanceGuid));
		assertThat(uaaClientAfterDeletion).isEmpty();
	}

	private String uaaClientId(String serviceInstanceGuid) {
		return APP_NAME + "-" + serviceInstanceGuid;
	}
}