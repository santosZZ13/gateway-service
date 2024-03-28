package org.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseInfo {
	/**
	 * The unique identifier for the request this response is associated with.
	 */
	private String requestId;
	/**
	 * The timestamp when the response was sent.
	 */
	private String timestamp;
	/**
	 * The HTTP status code of the response.
	 */
	private Integer status;

	private Headers<String, String> headers;
	/**
	 * The body of the response, if applicable.
	 */
	private String body;
	/**
	 * The time taken to process the request and generate the response.
	 */
	private int duration;
	public static class Headers<K, V> {

	}
}
