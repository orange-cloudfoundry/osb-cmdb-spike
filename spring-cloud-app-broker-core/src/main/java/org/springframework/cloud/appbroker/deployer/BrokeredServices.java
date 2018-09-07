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

package org.springframework.cloud.appbroker.deployer;

import java.util.ArrayList;
import java.util.List;

public class BrokeredServices extends ArrayList<BrokeredService> {
	private static final long serialVersionUID = 6303127383252611352L;

	public BrokeredServices() {
	}

	private BrokeredServices(List<BrokeredService> brokeredServices) {
		super.addAll(brokeredServices);
	}

	public static BrokeredServicesBuilder builder() {
		return new BrokeredServicesBuilder();
	}

	public static class BrokeredServicesBuilder {
		private List<BrokeredService> brokeredServices = new ArrayList<>();

		public BrokeredServicesBuilder service(BrokeredService brokeredService) {
			this.brokeredServices.add(brokeredService);
			return this;
		}

		public BrokeredServices build() {
			return new BrokeredServices(brokeredServices);
		}
	}
}