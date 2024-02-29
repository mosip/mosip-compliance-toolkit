package io.mosip.compliance.toolkit.dto.testrun;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class DecryptDataRequestDto {
    String applicationId;
    String referenceId;
    LocalDateTime timeStamp;
    String data;
    String salt;
    String aad;
}
