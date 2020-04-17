package com.orange.oss.osbcmdb.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

import org.springframework.cloud.servicebroker.model.Context;
import org.springframework.cloud.servicebroker.model.ServiceBrokerRequest;

public abstract class BaseMetadataFormatter {

	protected abstract void setLabelsAndAnnotations(Map<String, Object> properties, Map<String, String> annotations,
			Map<String, String> labels, String prefix);

	protected Mono<List<MetaData>> setMetadata(List<MetaData> metaDatas,
		ServiceBrokerRequest request, String serviceInstanceId,
		Context context) {

		Map<String, String> annotations = new HashMap<>();
		Map<String, String> labels = new HashMap<>();

		setBrokeredServiceGuidLabel(serviceInstanceId, labels);
		setApiInfoLocationAnnotation(request, annotations);
		setContextMetadata(context, annotations, labels);
		setOriginatingIdentityMetadata(request, annotations, labels);

		for (MetaData metaData : metaDatas) {
			metaData.setAnnotations(annotations);
			metaData.setLabels(labels);
		}
		return Mono.justOrEmpty(metaDatas);
	}

	private void setApiInfoLocationAnnotation(ServiceBrokerRequest request, Map<String, String> annotations) {
		String apiInfoLocation = request.getApiInfoLocation();
		if (apiInfoLocation !=null) {
			annotations.put("brokered_service_api_info_location", apiInfoLocation);
		}
	}

	private void setBrokeredServiceGuidLabel(String serviceInstanceId, Map<String, String> labels) {
		labels.put("brokered_service_instance_guid", serviceInstanceId);
	}

	private void setContextMetadata(Context context, Map<String, String> annotations, Map<String, String> labels) {
		Map<String, Object> contextProperties =
			context == null ? new HashMap<>() : context.getProperties();
		setLabelsAndAnnotations(contextProperties, annotations, labels, "context");
	}

	private void setOriginatingIdentityMetadata(ServiceBrokerRequest request, Map<String, String> annotations,
		Map<String, String> labels) {
		Context originatingIdentity = request.getOriginatingIdentity();
		Map<String, Object> originatingIdentityProperties =
			originatingIdentity == null ? new HashMap<>() : originatingIdentity.getProperties();
		setLabelsAndAnnotations(originatingIdentityProperties, annotations, labels, "originating_identity");
	}

}