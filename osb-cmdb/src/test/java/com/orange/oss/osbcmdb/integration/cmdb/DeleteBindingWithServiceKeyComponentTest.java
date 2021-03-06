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

package com.orange.oss.osbcmdb.integration.cmdb;

import com.orange.oss.osbcmdb.integration.WiremockComponentTest;
import com.orange.oss.osbcmdb.integration.fixtures.CloudControllerStubFixture;
import com.orange.oss.osbcmdb.integration.fixtures.OpenServiceBrokerApiFixture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import static com.orange.oss.osbcmdb.integration.CreateInstanceWithServicesComponentTest.BACKING_SERVICE_NAME;
import static com.orange.oss.osbcmdb.integration.CreateInstanceWithServicesComponentTest.BACKING_SI_NAME;
import static io.restassured.RestAssured.given;

@TestPropertySource(properties = {
	"spring.cloud.appbroker.services[0].service-name=example",
	"spring.cloud.appbroker.services[0].plan-name=standard",
	"spring.cloud.appbroker.services[0].services[0].service-instance-name=" + BACKING_SI_NAME,
	"spring.cloud.appbroker.services[0].services[0].name=" + BACKING_SERVICE_NAME,
	"spring.cloud.appbroker.services[0].services[0].plan=standard",
	"service-bindings-as-service-keys=true"
})
@Disabled("Still need stub adaptation to osb-redesign")
class DeleteBindingWithServiceKeyComponentTest extends WiremockComponentTest {

	private static final String SERVICE_INSTANCE_ID = "instance-id";
	private static final String BINDING_ID = "binding-id";

	protected static final String APP_NAME = "app-with-new-services";

	protected static final String BACKING_SI_NAME = "my-db-service";

	protected static final String BACKING_SERVICE_NAME = "db-service";

	@Autowired
	private CloudControllerStubFixture cloudControllerFixture;

	@Autowired
	private OpenServiceBrokerApiFixture brokerFixture;

	@Value("${spring.cloud.openservicebroker.catalog.services[0].id}")
	String serviceDefinitionId;


	@Test
	void deleteServiceBindingDeletesServiceKey() {

		// given services are available in the marketplace
		cloudControllerFixture.stubServiceInstanceExists(BACKING_SI_NAME);

		//given service key guid gets properly looked up by name
		cloudControllerFixture.stubListServiceKey(BACKING_SI_NAME, BINDING_ID);

		//given service key deletion by guid gets accepted
		cloudControllerFixture.stubDeleteServiceKey(BACKING_SI_NAME, BINDING_ID);

		// when a service binding is created
		given(brokerFixture.serviceBrokerSpecification())
//			.filter(new RequestLoggingFilter())
//			.filter(new ResponseLoggingFilter())
			.when()
			.delete(brokerFixture.deleteBindingUrl(), SERVICE_INSTANCE_ID, BINDING_ID)
			.then()
			.statusCode(HttpStatus.OK.value());

	}

}