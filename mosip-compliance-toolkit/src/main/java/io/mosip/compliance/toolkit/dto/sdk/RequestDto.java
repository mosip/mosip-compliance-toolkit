package io.mosip.compliance.toolkit.dto.sdk;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RequestDto {
    private String version;
    private String request;
}
