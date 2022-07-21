package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.MasterDataValueDto;
import io.mosip.compliance.toolkit.dto.MasterDataDto;
import io.mosip.compliance.toolkit.entity.MasterDataEntity;
import io.mosip.compliance.toolkit.repository.MasterDataRepository;
import io.mosip.kernel.core.http.ResponseWrapper;

@Service
public class MasterDataService {

	@Autowired
	private MasterDataRepository masterDataRepository;

	public ResponseWrapper<MasterDataDto> getDataById(String id){
		ResponseWrapper<MasterDataDto> responseWrapper = new ResponseWrapper<>(); 
		MasterDataDto masterDataDto = new MasterDataDto();

		Optional<MasterDataEntity> mdeOptional = masterDataRepository.findById(id);
		if(null != mdeOptional && !mdeOptional.isEmpty()) {
			MasterDataEntity masterDataEntity = mdeOptional.get();
			masterDataDto.setId(masterDataEntity.getId());
			masterDataDto.setName(masterDataEntity.getName());
			masterDataDto.setDescription(masterDataEntity.getDescription());
			MasterDataValueDto[] masterDataArray = new Gson().fromJson(masterDataEntity.getValueJson(), MasterDataValueDto[].class);
			masterDataDto.setData(Arrays.asList(masterDataArray));
		}
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(masterDataDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<MasterDataDto> getDataByName(String name){
		ResponseWrapper<MasterDataDto> responseWrapper = new ResponseWrapper<>(); 
		//List<MasterDataDto> masterDataDtoList = new ArrayList<>();
		MasterDataDto masterDataDto = new MasterDataDto();

		List<MasterDataEntity> mdeList = masterDataRepository.findAllByName(name);
		if(null != mdeList && !mdeList.isEmpty()) {
			MasterDataEntity masterDataEntity = mdeList.get(0);
			masterDataDto.setId(masterDataEntity.getId());
			masterDataDto.setName(masterDataEntity.getName());
			masterDataDto.setDescription(masterDataEntity.getDescription());
			MasterDataValueDto[] masterDataArray = new Gson().fromJson(masterDataEntity.getValueJson(), MasterDataValueDto[].class);
			masterDataDto.setData(Arrays.asList(masterDataArray));
		}	
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(masterDataDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private Map<String, String> prepareDataFromJson(MasterDataEntity masterDataEntity){
		Map<String, String> map = new HashMap<>();
		if(null != masterDataEntity && null != masterDataEntity.getValueJson()) {
			JSONArray dataArray = new JSONArray(masterDataEntity.getValueJson());
			for (int i = 0; i < dataArray.length(); i++) {
				map.put(dataArray.getJSONObject(i).getString("code"), dataArray.getJSONObject(i).getString("value"));
			}
		}
		return map;
	}
}
