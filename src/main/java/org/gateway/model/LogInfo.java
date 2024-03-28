package org.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogInfo {
	private RequestInfo requestInfo;
	private ResponseInfo responseInfo;
	private String logTime;
	private String logType;
	private String logMessage;
}
