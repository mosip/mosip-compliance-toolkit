package io.mosip.compliance.toolkit.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.MasterDataDto;
import io.mosip.compliance.toolkit.dto.MasterDataValueDto;
import io.mosip.compliance.toolkit.entity.MasterDataEntity;
import io.mosip.compliance.toolkit.repository.MasterDataRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class MasterDataService {

	@Value("${mosip.toolkit.api.id.masterdata.get}")
	private String getMasterDataId;

	@Value("${mosip.toolkit.api.id.masterdata.post}")
	private String saveMasterDataId;

	@Autowired
	private MasterDataRepository masterDataRepository;

	@Autowired
	private ObjectMapper objectMapper;
	
	private Logger log = LoggerConfiguration.logConfig(MasterDataService.class);

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUserId();
		return partnerId;
	}

	public ResponseWrapper<MasterDataDto> getDataByName(String name){
		ResponseWrapper<MasterDataDto> responseWrapper = new ResponseWrapper<>(); 
		MasterDataDto masterDataDto = new MasterDataDto();

		List<MasterDataEntity> mdeList = masterDataRepository.findAllByName(name);
		if(null != mdeList && !mdeList.isEmpty()) {
			try {
			MasterDataEntity masterDataEntity = mdeList.get(0);
			masterDataDto.setName(masterDataEntity.getName());
			masterDataDto.setDescription(masterDataEntity.getDescription());
			MasterDataValueDto[] masterDataArray = objectMapper.readValue(masterDataEntity.getValueJson(), MasterDataValueDto[].class);
			masterDataDto.setData(Arrays.asList(masterDataArray));
			}catch (IOException ex) {
				log.error(ex.getMessage());
			}
		}	
		responseWrapper.setId(getMasterDataId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(masterDataDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<MasterDataDto> addMasterData(MasterDataDto masterDataDto) {
		ResponseWrapper<MasterDataDto> responseWrapper = new ResponseWrapper<>();
		MasterDataDto savedMasterDataDto = null;
		if(null != masterDataDto) {
			try {
				MasterDataEntity inputMDE = new MasterDataEntity();
				inputMDE.setName(masterDataDto.getName());
				inputMDE.setDescription(masterDataDto.getDescription());
				inputMDE.setValueJson(objectMapper.writeValueAsString(masterDataDto.getData()));
				inputMDE.setCrBy(getPartnerId());
				inputMDE.setCrDate(LocalDateTime.now());

				MasterDataEntity savedMDE = masterDataRepository.save(inputMDE);

				savedMasterDataDto = new MasterDataDto();
				savedMasterDataDto.setName(savedMDE.getName());
				savedMasterDataDto.setDescription(savedMDE.getDescription());
				MasterDataValueDto[] masterDataArray = new Gson().fromJson(savedMDE.getValueJson(), MasterDataValueDto[].class);
				savedMasterDataDto.setData(Arrays.asList(masterDataArray));
			} catch (JsonProcessingException ex) {
				log.error(ex.getMessage());
			}
		}
		responseWrapper.setId(saveMasterDataId);
		responseWrapper.setResponse(savedMasterDataDto);
		responseWrapper.setVersion(AppConstants.VERSION);		
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
}
