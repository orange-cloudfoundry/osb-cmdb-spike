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

package org.springframework.cloud.appbroker.extensions.credentials;

import java.util.Collections;
import java.util.HashMap;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import org.springframework.cloud.appbroker.deployer.BackingApplication;

public class SpringSecurityBasicAuthCredentialProviderFactory extends
	CredentialProviderFactory<CredentialGenerationConfig> {

	private static final String CREDENTIAL_DESCRIPTOR = "basic";

	static final String SPRING_KEY = "spring";
	static final String SPRING_SECURITY_KEY = "security";
	static final String SPRING_SECURITY_USER_KEY = "user";
	static final String SPRING_SECURITY_USER_NAME_KEY = "name";
	static final String SPRING_SECURITY_USER_PASSWORD_KEY = "password";

	private final CredentialGenerator credentialGenerator;

	public SpringSecurityBasicAuthCredentialProviderFactory(CredentialGenerator credentialGenerator) {
		super(CredentialGenerationConfig.class);
		this.credentialGenerator = credentialGenerator;
	}

	@Override
	public CredentialProvider create(CredentialGenerationConfig config) {
		return new CredentialProvider() {
			@Override
			public Mono<BackingApplication> addCredentials(BackingApplication backingApplication,
														   String serviceInstanceGuid) {
				return generateCredentials(config, backingApplication, serviceInstanceGuid)
					.flatMap(user -> addUserToEnvironment(backingApplication, user))
					.thenReturn(backingApplication);
			}

			@Override
			public Mono<BackingApplication> deleteCredentials(BackingApplication backingApplication,
															  String serviceInstanceGuid) {
				return credentialGenerator.deleteUser(backingApplication.getName(), serviceInstanceGuid, CREDENTIAL_DESCRIPTOR)
										  .thenReturn(backingApplication);
			}
		};
	}

	private Mono<Tuple2<String, String>> generateCredentials(CredentialGenerationConfig config,
															 BackingApplication backingApplication,
															 String serviceInstanceGuid) {
		return credentialGenerator.generateUser(backingApplication.getName(), serviceInstanceGuid, CREDENTIAL_DESCRIPTOR,
			config.getLength(), config.isIncludeUppercaseAlpha(), config.isIncludeLowercaseAlpha(),
			config.isIncludeNumeric(), config.isIncludeSpecial());
	}

	private Mono<Void> addUserToEnvironment(BackingApplication backingApplication, Tuple2<String, String> user) {
		return Mono.just(new HashMap<>(2))
				   .flatMap(userProperties -> {
					   userProperties.put(SPRING_SECURITY_USER_NAME_KEY, user.getT1());
					   userProperties.put(SPRING_SECURITY_USER_PASSWORD_KEY, user.getT2());
					   backingApplication.addEnvironment(SPRING_KEY,
						   Collections.singletonMap(SPRING_SECURITY_KEY,
							   Collections.singletonMap(SPRING_SECURITY_USER_KEY, userProperties)));
					   return Mono.empty();
				   });
	}
}
