package com.orange.oss.osbcmdb.metadata;

import java.util.List;

import reactor.core.publisher.Mono;

import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;

public interface CreateServiceMetadataFormatterService {

	Mono<List<MetaData>> setMetadata(List<MetaData> metaData,
		CreateServiceInstanceRequest request);

}