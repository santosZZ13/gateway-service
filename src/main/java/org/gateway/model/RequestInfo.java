package org.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestInfo {
	/**
	 * A unique identifier for the request
	 */
	private String requestId;
	/**
	 * The timestamp when the request was made
	 */
	private String timestamp;
	/**
	 *  The HTTP method used (GET, POST, PUT, DELETE, etc.).
	 */
	private String method;
	/**
	 * The URI of the request
	 */
	private String uri;
	/**
	 * The HTTP headers sent with the request.
	 */
	private Headers<String, String> headers;
	/**
	 * Any query parameters included in the request.
	 */
	private Map<String, String> queryParams;
	/**
	 * The body of the request, if applicable.
	 */
	private String body;
	/**
	 * The IP address of the client making the request.
	 */
	private String remoteAddress;
	/**
	 *  Information about the client making the request,
	 *  usually including the browser name, version, and the operating system.
	 */
	private String userAgent;
	public static class Headers<K, V> {

	}
}
