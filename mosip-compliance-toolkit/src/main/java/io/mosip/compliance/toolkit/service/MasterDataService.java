package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.events.Event.ID;

import com.google.gson.Gson;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.MasterDataDto;
import io.mosip.compliance.toolkit.dto.MasterDataValueDto;
import io.mosip.compliance.toolkit.entity.MasterDataEntity;
import io.mosip.compliance.toolkit.repository.MasterDataRepository;
import io.mosip.kernel.core.http.ResponseWrapper;

@Service
public class MasterDataService {

	@Autowired
	private MasterDataRepository masterDataRepository;

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

	public ResponseWrapper<MasterDataDto> addMasterData(String owner, MasterDataDto masterDataDto) {
		ResponseWrapper<MasterDataDto> responseWrapper = new ResponseWrapper<>();
		MasterDataDto savedMasterDataDto = null;
		if(null != masterDataDto) {

			MasterDataEntity inputMDE = new MasterDataEntity();
			inputMDE.setId(masterDataDto.getId());
			inputMDE.setName(masterDataDto.getName());
			inputMDE.setDescription(masterDataDto.getDescription());
			inputMDE.setValueJson(new Gson().toJson(masterDataDto.getData()));
			List<MasterDataEntity> mdeList = masterDataRepository.findAllByName(masterDataDto.getName());
			if(null != mdeList && !mdeList.isEmpty()) {
				inputMDE.setCrBy(mdeList.get(0).getCrBy());
				inputMDE.setCrDate(mdeList.get(0).getCrDate());
				inputMDE.setUpBy(owner);
				inputMDE.setUpdDate(LocalDateTime.now());
			}else {
				inputMDE.setCrBy(owner);
				inputMDE.setCrDate(LocalDateTime.now());
			}
			MasterDataEntity savedMDE = masterDataRepository.save(inputMDE);
			savedMasterDataDto = new MasterDataDto();
			savedMasterDataDto.setId(savedMDE.getId());
			savedMasterDataDto.setName(savedMDE.getName());
			savedMasterDataDto.setDescription(savedMDE.getDescription());
			MasterDataValueDto[] masterDataArray = new Gson().fromJson(savedMDE.getValueJson(), MasterDataValueDto[].class);
			savedMasterDataDto.setData(Arrays.asList(masterDataArray));
		}
		responseWrapper.setResponse(savedMasterDataDto);
		responseWrapper.setVersion(AppConstants.VERSION);		
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
}
