package io.mosip.compliance.toolkit.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.constants.SdkPurpose;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.AddBioTestDataResponseDto;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.dto.TestDataValidationDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto.ValidatorDef;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.CryptoUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;

@Component
public class BiometricTestDataService {

    private static final String BLANK_SPACE = " ";

    private static final String ZIP_EXT = ".zip";

    private static final String UNDERSCORE = "_";

    private static final String PROBE_XML = "probe.xml";

    private static final String GALLERY = "gallery";

    private static final String DOT_XML = ".xml";

    /**
     * Autowired reference for {@link #VirusScanner}
     */
    @Autowired
    VirusScanner<Boolean, InputStream> virusScan;

    @Autowired
    ResourceCacheService resourceCacheService;

    @Value("$(mosip.toolkit.api.id.biometric.testdata.get)")
    private String getBiometricTestDataId;

    @Value("${mosip.toolkit.api.id.biometric.testdata.post}")
    private String postBiometricTestDataId;

    @Value("$(mosip.toolkit.api.id.biometric.testdata.filenames.get)")
    private String getBioTestDataFileNames;

    @Value("${mosip.toolkit.document.scan}")
    private Boolean scanDocument;
    private Logger log = LoggerConfiguration.logConfig(BiometricTestDataService.class);

    @Autowired
    private ObjectMapperConfig objectMapperConfig;

    @Value("${mosip.kernel.objectstore.account-name}")
    private String objectStoreAccountName;

    @Value("${mosip.toolkit.sample.testdata.sdk.specversion}")
    private String sdkSampleTestdataSpecVer;

    @Value("${mosip.toolkit.sample.testdata.sdk.readme.text}")
    private String readmeIntro;
	
	@Value("${mosip.toolkit.sample.testdata.sdk.outer.readme.text.head}")
	private String outerReadmeIntro;
	
	@Value("${mosip.toolkit.sample.testdata.sdk.outer.readme.text.body}")
	private String outerReadmeBody;

    @Value("${mosip.toolkit.sdk.testcases.ignore.list}")
    private String ignoreTestcases;

    @Value("${mosip.toolkit.abis.testcases.ignore.list}")
    private String ignoreAbisTestcases;

    @Value("${mosip.toolkit.max.allowed.gallery.files}")
    private String maxAllowedGalleryFiles;

    @Qualifier("S3Adapter")
    @Autowired
    private ObjectStoreAdapter objectStore;

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getPartnerId() {
        String partnerId = authUserDetails().getUsername();
        return partnerId;
    }

    private String getUserBy() {
        String crBy = authUserDetails().getMail();
        return crBy;
    }

    @Autowired
    private TestCaseCacheService testCaseCacheService;

    @Autowired
    private BiometricTestDataRepository biometricTestDataRepository;

    public ResponseWrapper<List<BiometricTestDataDto>> getListOfBiometricTestData() {
        ResponseWrapper<List<BiometricTestDataDto>> responseWrapper = new ResponseWrapper<>();
        List<BiometricTestDataDto> biometricTestDataList = new ArrayList<>();
        try {
            List<BiometricTestDataEntity> entities = biometricTestDataRepository.findAllByPartnerId(getPartnerId());
            if (Objects.nonNull(entities) && !entities.isEmpty()) {
                ObjectMapper mapper = objectMapperConfig.objectMapper();
                for (BiometricTestDataEntity entity : entities) {
                    BiometricTestDataDto testData = mapper.convertValue(entity, BiometricTestDataDto.class);
                    biometricTestDataList.add(testData);
                }
            } else {
                String errorCode = ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorCode();
                String errorMessage = ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorMessage();
                responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
            }
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In getBiometricTestdata method of BiometricTestDataService Service - " + ex.getMessage());
            String errorCode = ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorCode();
            String errorMessage = ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorMessage() + BLANK_SPACE
                    + ex.getMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
        }
        responseWrapper.setId(getBiometricTestDataId);
        responseWrapper.setResponse(biometricTestDataList);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    public ResponseWrapper<AddBioTestDataResponseDto> addBiometricTestdata(
            BiometricTestDataDto inputBiometricTestDataDto,
            MultipartFile file) {
        ResponseWrapper<AddBioTestDataResponseDto> responseWrapper = new ResponseWrapper<>();
        AddBioTestDataResponseDto addBioTestDataResponseDto = null;
        try {
            if (validInputRequest(inputBiometricTestDataDto, file)) {
                String fileName = file.getOriginalFilename();
                if (fileName.contains(".")) {
                    if (!(fileName.split("\\.").length > 2)) {
                        if (!scanDocument || (scanDocument && isVirusScanSuccess(file))) {
                            if (Objects.nonNull(inputBiometricTestDataDto) && Objects.nonNull(file) && !file.isEmpty()
                                    && file.getSize() > 0) {

                                String requestPurpose = inputBiometricTestDataDto.getPurpose();
                                String purpose = "";
                                if (!requestPurpose.equals(AppConstants.ABIS)) {
                                    SdkPurpose sdkPurpose = SdkPurpose.fromCode(requestPurpose);
                                    purpose = sdkPurpose.getCode();
                                } else {
                                    purpose = requestPurpose;
                                }
                                TestDataValidationDto testDataValidation = validateTestData(purpose, file);

                                String encodedHash = CryptoUtil.getEncodedHash(file.getBytes());

                                ObjectMapper mapper = objectMapperConfig.objectMapper();
                                BiometricTestDataEntity inputEntity = mapper.convertValue(inputBiometricTestDataDto,
                                        BiometricTestDataEntity.class);
                                inputEntity.setId(RandomIdGenerator.generateUUID("btd", "", 36));
                                inputEntity.setPartnerId(getPartnerId());
                                inputEntity.setFileId(file.getOriginalFilename());
                                inputEntity.setFileHash(encodedHash);
                                inputEntity.setOrgName(resourceCacheService.getOrgName(getPartnerId()));
                                inputEntity.setCrBy(getUserBy());
                                inputEntity.setCrDate(LocalDateTime.now());
                                inputEntity.setUpBy(null);
                                inputEntity.setUpdDate(null);
                                inputEntity.setDeleted(false);
                                inputEntity.setDelTime(null);

                                String container = AppConstants.PARTNER_TESTDATA + "/" + inputEntity.getPartnerId() + "/"
                                        + inputEntity.getPurpose();
                                if (!objectStore.exists(objectStoreAccountName, container, null, null, inputEntity.getFileId())) {
                                    BiometricTestDataEntity entity = biometricTestDataRepository.save(inputEntity);

                                    boolean status = false;
                                    InputStream is = file.getInputStream();
                                    try {
                                        status = putInObjectStore(container, inputEntity.getFileId(), is);
                                    } catch (Exception ex) {
                                        log.debug("sessionId", "idType", "id", ex.getStackTrace());
                                        log.error("sessionId", "idType", "id",
                                                "In addBiometricTestdata method of BiometricTestDataService Service - "
                                                        + ex.getMessage());
                                    }
                                    is.close();
                                    if (status) {
                                        BiometricTestDataDto biometricTestData = mapper.convertValue(entity,
                                                BiometricTestDataDto.class);
                                        addBioTestDataResponseDto = new AddBioTestDataResponseDto();
                                        addBioTestDataResponseDto.setBiometricTestDataDto(biometricTestData);
                                        if (Objects.nonNull(testDataValidation)) {
                                            Set<String> set = null;
                                            if (!testDataValidation.getFolders().isEmpty()) {
                                                set = new HashSet<>(testDataValidation.getFolders());
                                                if (!requestPurpose.equals(AppConstants.ABIS)) {
                                                    set.addAll(testDataValidation.getProbeFolders());
                                                    set.addAll(testDataValidation.getGalleryFolders());
                                                }
                                                String msg = "For testcases " + set + " we will use data from MOSIP_DEFAULT";
                                                addBioTestDataResponseDto.setInfo(msg);
                                            }
                                        }
                                    } else {
                                        biometricTestDataRepository.delete(entity);
                                        String errorCode = ToolkitErrorCodes.OBJECT_STORE_UNABLE_TO_ADD_FILE.getErrorCode();
                                        String errorMessage = ToolkitErrorCodes.OBJECT_STORE_UNABLE_TO_ADD_FILE.getErrorMessage();
                                        responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                                    }

                                } else {
                                    String errorCode = ToolkitErrorCodes.OBJECT_STORE_FILE_EXISTS.getErrorCode();
                                    String errorMessage = ToolkitErrorCodes.OBJECT_STORE_FILE_EXISTS.getErrorMessage() + inputEntity.getFileId();
                                    responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                                }

                            } else {
                                String errorCode = ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode();
                                String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage();
                                responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                            }
                        } else {
                            String errorCode = ToolkitErrorCodes.VIRUS_FOUND.getErrorCode();
                            String errorMessage = ToolkitErrorCodes.VIRUS_FOUND.getErrorMessage();
                            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                        }
                    } else {
                        String errorCode = ToolkitErrorCodes.FILE_WITH_MULTIPLE_EXTENSIONS.getErrorCode();
                        String errorMessage = ToolkitErrorCodes.FILE_WITH_MULTIPLE_EXTENSIONS.getErrorMessage();
                        responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                    }
                } else {
                    String errorCode = ToolkitErrorCodes.FILE_WITHOUT_EXTENSIONS.getErrorCode();
                    String errorMessage = ToolkitErrorCodes.FILE_WITHOUT_EXTENSIONS.getErrorMessage();
                    responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                }
            }
        } catch (ToolkitException ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In addBiometricTestdata method of BiometricTestDataService - " + ex.getMessage());
            String errorCode = ex.getErrorCode();
            String errorMessage = ex.getMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
        } catch (DataIntegrityViolationException ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In addBiometricTestdata method of BiometricTestDataService Service - " + ex.getMessage());
            String bioTestName = BLANK_SPACE;
            if (inputBiometricTestDataDto != null) {
                bioTestName = inputBiometricTestDataDto.getName();
            }
            String errorCode = ToolkitErrorCodes.BIO_TEST_DATA_FILE_EXISTS.getErrorCode();
            String errorMessage = ToolkitErrorCodes.BIO_TEST_DATA_FILE_EXISTS.getErrorMessage() + BLANK_SPACE
                    + bioTestName;
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In addBiometricTestdata method of BiometricTestDataService Service - " + ex.getMessage());
            String errorCode = ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorCode();
            String errorMessage = ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorMessage() + BLANK_SPACE
                    + ex.getMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
        }
        responseWrapper.setId(postBiometricTestDataId);
        responseWrapper.setResponse(addBioTestDataResponseDto);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    private boolean validInputRequest(BiometricTestDataDto biometricTestDataDto, MultipartFile file) {
        String testDataName = biometricTestDataDto.getName();
        if (!Pattern.matches(AppConstants.REGEX_PATTERN, testDataName)) {
            String exceptionErrorCode = ToolkitErrorCodes.INVALID_CHARACTERS.getErrorCode()
                    + AppConstants.COMMA_SEPARATOR
                    + ToolkitErrorCodes.TEST_DATA_NAME.getErrorCode();
            throw new ToolkitException(exceptionErrorCode, "Invalid characters are not allowed in test data name");
        }
        String fileName = file.getOriginalFilename();
        if (!Pattern.matches(AppConstants.FILE_NAME_REGEX_PATTERN, fileName)) {
            String exceptionErrorCode = ToolkitErrorCodes.INVALID_CHARACTERS.getErrorCode()
                    + AppConstants.COMMA_SEPARATOR
                    + ToolkitErrorCodes.FILE_NAME.getErrorCode();
            throw new ToolkitException(exceptionErrorCode, "Invalid characters are not allowed in file name");
        }
        return true;
    }

	private TestDataValidationDto validateTestData(String purpose, MultipartFile file) throws IOException {
		TestDataValidationDto testDataValidation = new TestDataValidationDto();
		testDataValidation.setPurpose(purpose);
		ZipInputStream zis = null;
		try {
			List<TestCaseEntity> testcases = null;
			List<String> validGalleryXmls = new ArrayList<>();
			List<String> ignoreTestcaseList = Arrays.asList(ignoreTestcases.split(","));
			if (!purpose.equals(AppConstants.ABIS)) {
				for (int i = 1; i <= Integer.valueOf(maxAllowedGalleryFiles); i++) {
					validGalleryXmls.add(GALLERY + i + DOT_XML);
				}
				testcases = testCaseCacheService.getSdkTestCases(AppConstants.SDK, sdkSampleTestdataSpecVer);
				if (Objects.nonNull(testcases)) {
					List<String> folders = new ArrayList<>();
					List<String> probeFolders = new ArrayList<>();
					List<String> galleryFolders = new ArrayList<>();
					for (TestCaseEntity testcase : testcases) {
						String testcaseJson = testcase.getTestcaseJson();
						TestCaseDto testCaseDto = objectMapperConfig.objectMapper().readValue(testcaseJson,
								TestCaseDto.class);
						if (!ignoreTestcaseList.contains(testCaseDto.getTestId())
								&& testCaseDto.getOtherAttributes().getSdkPurpose().contains(purpose)) {
							String folderName = testCaseDto.getTestId();
							folders.add(folderName);
							probeFolders.add(folderName);
							if (testCaseDto.getOtherAttributes().getSdkPurpose()
									.contains(SdkPurpose.MATCHER.getCode())) {
								galleryFolders.add(folderName);
							}
							if (testCaseDto.getMethodName().size() > 1
									&& testCaseDto.getMethodName().get(1).equals(MethodName.MATCH.getCode())) {
								folderName += "/" + testCaseDto.getMethodName().get(1);
								folders.add(folderName);
								probeFolders.add(folderName);
							}
						}
					}
					testDataValidation.setFolders(folders);
					testDataValidation.setProbeFolders(probeFolders);
					testDataValidation.setGalleryFolders(galleryFolders);
				}
				if (testDataValidation.getFolders().size() == 0 || testDataValidation.getProbeFolders().size() == 0) {
                    String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                            + AppConstants.COMMA_SEPARATOR
                            + ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorCode();
                    throw new ToolkitException(errorCode,
                            ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorMessage());
                }
			} else {
				testcases = testCaseCacheService.getAbisTestCases(AppConstants.ABIS, sdkSampleTestdataSpecVer);
				if (Objects.nonNull(testcases)) {
					List<String> folders = new ArrayList<>();
					for (TestCaseEntity testcase : testcases) {
						String testcaseJson = testcase.getTestcaseJson();
						TestCaseDto testCaseDto = objectMapperConfig.objectMapper().readValue(testcaseJson,
								TestCaseDto.class);
						String folderName = testCaseDto.getTestId();
						folders.add(folderName);

					}
					testDataValidation.setFolders(folders);
				}
				if (testDataValidation.getFolders().size() == 0) {
                    String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                            + AppConstants.COMMA_SEPARATOR
                            + ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorCode();
                    throw new ToolkitException(errorCode,
                            ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorMessage());
                }
			}

			if (Objects.nonNull(file)) {
				InputStream zipFileIs = file.getInputStream();
				zis = new ZipInputStream(zipFileIs);
				ZipEntry zipEntry = null;

				if (!file.getOriginalFilename().endsWith(ZIP_EXT)) {
                    String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                            + AppConstants.COMMA_SEPARATOR
                            + ToolkitErrorCodes.TESTDATA_INVALID_FILE.getErrorCode();
					throw new ToolkitException(errorCode,
							ToolkitErrorCodes.TESTDATA_INVALID_FILE.getErrorMessage());
				}

				double THRESHOLD_RATIO = 10;
				int THRESHOLD_ENTRIES = 10000;
				int THRESHOLD_SIZE = 1000000000; // 1 GB
				int totalSizeArchive = 0;
				int totalEntryArchive = 0;
				while ((zipEntry = zis.getNextEntry()) != null) {
					totalEntryArchive++;
					int nBytes = -1;
					byte[] buffer = new byte[2048];
					double totalSizeEntry = 0;
					while ((nBytes = zis.read(buffer)) > 0) { // Compliant
						totalSizeEntry += nBytes;
						totalSizeArchive += nBytes;
						double compressionRatio = totalSizeEntry / zipEntry.getCompressedSize();
						if (compressionRatio > THRESHOLD_RATIO) {
							// ratio between compressed and uncompressed data is highly suspicious, looks
							// like a Zip Bomb Attack
                            String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                                    + AppConstants.COMMA_SEPARATOR
                                    + ToolkitErrorCodes.ZIP_HIGH_COMPRESSION_RATIO_ERROR.getErrorCode();
							throw new ToolkitException(errorCode,
									ToolkitErrorCodes.ZIP_HIGH_COMPRESSION_RATIO_ERROR.getErrorMessage());
						}
					}

					if (totalSizeArchive > THRESHOLD_SIZE) {
                        String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                                + AppConstants.COMMA_SEPARATOR
                                + ToolkitErrorCodes.ZIP_SIZE_TOO_LARGE_ERROR.getErrorCode();
						throw new ToolkitException(errorCode,
								ToolkitErrorCodes.ZIP_SIZE_TOO_LARGE_ERROR.getErrorMessage());
					}

					if (totalEntryArchive > THRESHOLD_ENTRIES) {
                        String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                                + AppConstants.COMMA_SEPARATOR
                                + ToolkitErrorCodes.ZIP_ENTRIES_TOO_MANY_ERROR.getErrorCode();
						throw new ToolkitException(errorCode,
								ToolkitErrorCodes.ZIP_ENTRIES_TOO_MANY_ERROR.getErrorMessage());
					}

					String entryName = zipEntry.getName();
					if (!purpose.equals(AppConstants.ABIS)) {
						if (!entryName.startsWith(purpose)) {
                            entryName = entryName.charAt(entryName.length() - 1) != '/' ? entryName
                                    : entryName.substring(0, entryName.length() - 1);
                            String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                                    + AppConstants.COMMA_SEPARATOR
                                    + ToolkitErrorCodes.TESTDATA_WRONG_PURPOSE.getErrorCode()
                                    + AppConstants.COMMA_SEPARATOR
                                    + entryName;
							throw new ToolkitException(errorCode,
									ToolkitErrorCodes.TESTDATA_WRONG_PURPOSE.getErrorMessage() + " " + entryName);
						} else {
							entryName = entryName.replace(purpose + "/", "");
						}
					} else {
						if (!entryName.split("/")[0].equals(AppConstants.ABIS)) {
                            entryName = entryName.charAt(entryName.length() - 1) != '/' ? entryName
                                    : entryName.substring(0, entryName.length() - 1);
                            String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                                    + AppConstants.COMMA_SEPARATOR
                                    + ToolkitErrorCodes.TESTDATA_WRONG_PURPOSE.getErrorCode()
                                    + AppConstants.COMMA_SEPARATOR
                                    + entryName;
							throw new ToolkitException(errorCode,
									ToolkitErrorCodes.TESTDATA_WRONG_PURPOSE.getErrorMessage() + " " + entryName);
						} else {
							entryName = entryName.replace(purpose + "/", "");
						}
					}
					if (!entryName.isBlank()) {
						if (!purpose.equals(AppConstants.ABIS)) {
                                                    
							if (zipEntry.isDirectory()) {
								String testcaseId = entryName.substring(0, entryName.length() - 1);
								if (testDataValidation.getFolders().contains(testcaseId)) {
									testDataValidation.getFolders().remove(testcaseId);
								} else {
                                    String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                                            + AppConstants.COMMA_SEPARATOR
                                            + ToolkitErrorCodes.TESTDATA_INVALID_FOLDER.getErrorCode()
                                            + AppConstants.COMMA_SEPARATOR
                                            + testcaseId;
									throw new ToolkitException(errorCode,
											ToolkitErrorCodes.TESTDATA_INVALID_FOLDER.getErrorMessage()
                                                    + " "
													+ testcaseId);
								}
							} else if (entryName.endsWith(PROBE_XML)) {
								String testcaseId = entryName.substring(entryName.indexOf(AppConstants.SDK),
										entryName.indexOf(PROBE_XML) - 1);
								testDataValidation.getProbeFolders().remove(testcaseId);
							} else if (entryName.contains(GALLERY) && purpose.equals(SdkPurpose.MATCHER.getCode())) {
								String testcaseId = entryName.substring(entryName.indexOf(AppConstants.SDK),
										entryName.indexOf(GALLERY) - 1);
								testDataValidation.getGalleryFolders().remove(testcaseId);
								String galleryXml = entryName.substring(entryName.indexOf(GALLERY));
								if (!validGalleryXmls.contains(galleryXml)) {
                                    String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                                            + AppConstants.COMMA_SEPARATOR
                                            + ToolkitErrorCodes.TESTDATA_INVALID_GALLERY.getErrorCode()
                                            + AppConstants.ARGUMENTS_DELIMITER
                                            + galleryXml
                                            + AppConstants.ARGUMENTS_SEPARATOR
                                            + testcaseId;
									throw new ToolkitException(
											errorCode,
											ToolkitErrorCodes.TESTDATA_INVALID_GALLERY.getErrorMessage() + " "
													+ galleryXml + " in " + testcaseId);
								}
							}
						} else {
							if (zipEntry.isDirectory()) {
								String testcaseId = entryName.substring(0, entryName.length() - 1);
								if (testDataValidation.getFolders().contains(testcaseId)) {
									testDataValidation.getFolders().remove(testcaseId);
								} else {
                                    String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                                            + AppConstants.COMMA_SEPARATOR
                                            + ToolkitErrorCodes.TESTDATA_INVALID_FOLDER.getErrorCode()
                                            + AppConstants.COMMA_SEPARATOR
                                            + testcaseId;
									throw new ToolkitException(errorCode,
											ToolkitErrorCodes.TESTDATA_INVALID_FOLDER.getErrorMessage() + " "
													+ testcaseId);
								}
							}
						}
					}
				}

				if (0 == totalEntryArchive) {
                    String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                            + AppConstants.COMMA_SEPARATOR
                            + ToolkitErrorCodes.TESTDATA_INVALID_FILE.getErrorCode();
					throw new ToolkitException(errorCode,
							ToolkitErrorCodes.TESTDATA_INVALID_FILE.getErrorMessage());
				}

				testDataValidation.setValidated(true);
			} else {
                String errorCode = ToolkitErrorCodes.TESTDATA_VALIDATION_UNSUCCESSFULL.getErrorCode()
                        + AppConstants.COMMA_SEPARATOR
                        + ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode();
				throw new ToolkitException(errorCode,
						ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
			}
		} catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In validateTestData method of BiometricTestDataService Service - " + ex.getMessage());
			throw ex;
		} finally {
			if (zis != null) {
				zis.closeEntry();
				zis.close();
			}
		}
		return testDataValidation;
	}

    public ResponseWrapper<List<String>> getBioTestDataNames(String purpose) {
        ResponseWrapper<List<String>> responseWrapper = new ResponseWrapper<>();
        List<String> testDataNames = new ArrayList<>();
        try {
            String partnerId = getPartnerId();
            String container = AppConstants.PARTNER_TESTDATA + "/" + partnerId + "/" + purpose;
            List<ObjectDto> objects = objectStore.getAllObjects(objectStoreAccountName, container);
            if (Objects.nonNull(objects) && !objects.isEmpty()) {
                List<String> fileNames = new ArrayList<>();
                for (ObjectDto objectDto : objects) {
                    fileNames.add(objectDto.getObjectName());
                }
                String[] inputFileNames = fileNames.toArray(new String[0]);
                testDataNames = biometricTestDataRepository.findTestDataNamesByFileIds(inputFileNames, purpose,
                        partnerId);
            } else {
                String errorCode = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode();
                String errorMessage = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage();
                responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
            }
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In getBioTestDataFileNames method of BiometricTestDataService Service - " + ex.getMessage());
            String errorCode = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode();
            String errorMessage = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage() + BLANK_SPACE
                    + ex.getMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
        }
        responseWrapper.setId(getBioTestDataFileNames);
        responseWrapper.setResponse(testDataNames);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    public ResponseEntity<Resource> getSampleBioTestDataFile(String purpose) {
        Resource resource = null;
        try {
            byte[] bytes;
            List<TestCaseEntity> sdkTestCaseEntities = testCaseCacheService.getSdkTestCases(AppConstants.SDK,
                    sdkSampleTestdataSpecVer);
            List<TestCaseEntity> abisTestCaseEntities = testCaseCacheService.getAbisTestCases(AppConstants.ABIS,
                    sdkSampleTestdataSpecVer);
            if (!purpose.equals(AppConstants.ABIS)) {
                bytes = generateSampleTestData(purpose,sdkTestCaseEntities,ignoreTestcases);
            } else {
                bytes = generateSampleTestData(purpose,abisTestCaseEntities,ignoreAbisTestcases);
            }
            if (Objects.nonNull(bytes)) {
                String defaultFileName;
                if (!purpose.equals(AppConstants.ABIS)) {
                    SdkPurpose sdkPurpose = SdkPurpose.fromCode(purpose);
                    defaultFileName = AppConstants.SAMPLE + UNDERSCORE + sdkPurpose.toString().toUpperCase()
                            + ZIP_EXT;
                } else {
                    defaultFileName = AppConstants.SAMPLE + UNDERSCORE + purpose.toUpperCase() + ZIP_EXT;
                }

                resource = new ByteArrayResource(bytes);

                HttpHeaders header = new HttpHeaders();
                header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + defaultFileName);
                header.add("Cache-Control", "no-cache, no-store, must-revalidate");
                header.add("Pragma", "no-cache");
                header.add("Expires", "0");

                return ResponseEntity.ok().headers(header).contentLength(resource.contentLength())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
            }
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In getDefaultBioTestData method of BiometricTestDataService Service - " + ex.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    private byte[] generateSampleTestData(String purpose,List<TestCaseEntity> testCaseEntities, String ignoretestcases) {
        byte[] response = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        try {
            if (Objects.nonNull(testCaseEntities) && testCaseEntities.size() > 0) {
                String folderName = purpose;
                String fileName = "Readme.txt";

                byteArrayOutputStream = new ByteArrayOutputStream();
                bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
                zipOutputStream = new ZipOutputStream(bufferedOutputStream);

                List<String> ignoreTestcaseList = Arrays.asList(ignoretestcases.split(","));

                // adding Readme file outside testcases
                StringBuilder builder = new StringBuilder();
                builder.append(outerReadmeIntro + "\n\n");
                builder.append("Method - " + purpose + "\n\n");
                builder.append(outerReadmeBody);

                zipOutputStream.putNextEntry(new ZipEntry(folderName + "/" + fileName));
                zipOutputStream.write(builder.toString().getBytes());
                zipOutputStream.closeEntry();
                if (!purpose.equals(AppConstants.ABIS)) {
                    generateSampleSdkTestData(purpose,testCaseEntities, ignoreTestcaseList, zipOutputStream, fileName);
                } else {
                    generateSampleAbisTestData(purpose,testCaseEntities, ignoreTestcaseList, zipOutputStream, fileName);
                }
                if (null != zipOutputStream) {
                    zipOutputStream.finish();
                    zipOutputStream.flush();
                    zipOutputStream.close();
                    zipOutputStream = null;
                }

                response = byteArrayOutputStream.toByteArray();

                if (Objects.nonNull(bufferedOutputStream)) {
                    bufferedOutputStream.close();
                    bufferedOutputStream = null;
                }
                if (Objects.nonNull(byteArrayOutputStream)) {
                    byteArrayOutputStream.close();
                    byteArrayOutputStream = null;
                }
            }
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In generateSampleTestData method of BiometricTestDataService Service - " + ex.getMessage());
        } finally {
            try {
                if (null != zipOutputStream) {
                    zipOutputStream.finish();
                    zipOutputStream.flush();
                    zipOutputStream.close();
                }
                if (null != bufferedOutputStream) {
                    bufferedOutputStream.close();
                }
                if (null != byteArrayOutputStream) {
                    byteArrayOutputStream.close();
                }
            } catch (Exception e) {
                log.debug("sessionId", "idType", "id", e.getStackTrace());
                log.error("sessionId", "idType", "id",
                        "In generateSampleTestData method of BiometricTestDataService Service - " + e.getMessage());
            }
        }
        return response;
    }
    private void generateSampleSdkTestData(String purpose, List<TestCaseEntity> testCaseEntities,List<String> ignoreTestcasesList,
                                             ZipOutputStream zipOutputStream, String fileName) throws IOException {

        for (final TestCaseEntity testCaseEntity : testCaseEntities) {
            String testcaseJson = testCaseEntity.getTestcaseJson();
            TestCaseDto testCaseDto = objectMapperConfig.objectMapper().readValue(testcaseJson,
                    TestCaseDto.class);
            if (testCaseDto.getSpecVersion() != null
                    && testCaseDto.getSpecVersion().equals(sdkSampleTestdataSpecVer)
                    && !ignoreTestcasesList.contains(testCaseDto.getTestId())
                    && testCaseDto.getOtherAttributes().getSdkPurpose().contains(purpose)) {

                String folderName = purpose + "/" + testCaseDto.testId;

                String content = prepareReadme(testCaseDto);

                zipOutputStream.putNextEntry(new ZipEntry(folderName + "/" + fileName));
                zipOutputStream.write(content.getBytes());
                zipOutputStream.closeEntry();

                if (testCaseDto.getMethodName().size() > 1
                        && testCaseDto.getMethodName().get(1).equals(MethodName.MATCH.getCode())) {
                    folderName = purpose + "/" + testCaseDto.testId + "/" + testCaseDto.getMethodName().get(1);
                    fileName = "Readme.txt";

                    content = prepareReadme(testCaseDto);

                    zipOutputStream.putNextEntry(new ZipEntry(folderName + "/" + fileName));
                    zipOutputStream.write(content.getBytes());
                    zipOutputStream.closeEntry();
                }
            }
        }
    }

    private void generateSampleAbisTestData(String purpose, List<TestCaseEntity> testCaseEntities,List<String> ignoreTestcasesList,
                                              ZipOutputStream zipOutputStream, String fileName) throws IOException {
        for (final TestCaseEntity testCaseEntity : testCaseEntities) {
            String testcaseJson = testCaseEntity.getTestcaseJson();
            TestCaseDto testCaseDto = objectMapperConfig.objectMapper().readValue(testcaseJson,
                    TestCaseDto.class);
            if (testCaseDto.getSpecVersion() != null
                    && testCaseDto.getSpecVersion().equals(sdkSampleTestdataSpecVer)
                    && purpose.equals(AppConstants.ABIS)
                    && !ignoreTestcasesList.contains(testCaseDto.getTestId())) {
                String folderName = purpose + "/" + testCaseDto.testId;
                String content = prepareReadme(testCaseDto);

                zipOutputStream.putNextEntry(new ZipEntry(folderName + "/" + fileName));
                zipOutputStream.write(content.getBytes());
                zipOutputStream.closeEntry();
            }
        }
    }

    private String prepareReadme(TestCaseDto testCaseDto) {
        String readMeFileData = "";
        try {
            if (Objects.nonNull(testCaseDto)) {
                StringBuilder builder = new StringBuilder();
                builder.append(readmeIntro + "\n\n");
                builder.append("Name - " + testCaseDto.testName + "\n");
                builder.append("ID - " + testCaseDto.testId + "\n");
                builder.append("Description - " + testCaseDto.testDescription + "\n");
                builder.append("Spec Version - " + testCaseDto.specVersion + "\n");
                if (testCaseDto.getTestCaseType().equals(AppConstants.SDK)) {
                    builder.append("Purpose - " + testCaseDto.getOtherAttributes().getSdkPurpose().toString() + "\n");
                    builder.append(
                            "Modality - " + testCaseDto.getOtherAttributes().getModalities().toString() + "\n\n");
                } else {
                    builder.append("\n");
                }
                if (testCaseDto.negativeTestcase) {
                    builder.append("This is Negative Testcase\n\n");
                }

                List<List<ValidatorDef>> validatorDefsList = testCaseDto.getValidatorDefs();
                for (int i = 0; i < validatorDefsList.size(); i++) {
                    if (testCaseDto.getMethodName().size() > i) {
                        builder.append("Validators for " + testCaseDto.getMethodName().get(i) + "\n");
                        for (ValidatorDef validatorDef : validatorDefsList.get(i)) {
                            builder.append("Name - " + validatorDef.getName() + "\n");
                            builder.append("Description - " + validatorDef.getDescription() + "\n");
                        }
                        builder.append("\n");
                    }
                }
                readMeFileData = builder.toString();
            }
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In prepareReadme method of BiometricTestDataService Service - " + ex.getMessage());
        }
        return readMeFileData;
    }

    public ResponseEntity<Resource> getBiometricTestDataFile(String bioTestDataId) {
        ByteArrayResource resource = null;
        try {
            String partnerId = getPartnerId();
            BiometricTestDataEntity biometricTestDataEntity = biometricTestDataRepository.findById(bioTestDataId,
                    partnerId);
            if (Objects.nonNull(biometricTestDataEntity)) {
                String fileName = biometricTestDataEntity.getFileId();
                String purpose = biometricTestDataEntity.getPurpose();
                if (Objects.nonNull(fileName) && Objects.nonNull(purpose)) {
                    String container = AppConstants.PARTNER_TESTDATA + "/" + partnerId + "/" + purpose;
                    if (existsInObjectStore(container, fileName)) {
                        InputStream inputStream = getFromObjectStore(container, fileName);
                        resource = new ByteArrayResource(inputStream.readAllBytes());
                        inputStream.close();

                        HttpHeaders header = new HttpHeaders();
                        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
                        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
                        header.add("Pragma", "no-cache");
                        header.add("Expires", "0");

                        return ResponseEntity.ok().headers(header).contentLength(resource.contentLength())
                                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In getBiometricTestData method of BiometricTestDataService Service - " + ex.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    private boolean existsInObjectStore(String container, String objectName) {
        return objectStore.exists(objectStoreAccountName, container, null, null, objectName);
    }

    private InputStream getFromObjectStore(String container, String objectName) {
        return objectStore.getObject(objectStoreAccountName, container, null, null, objectName);
    }

    private boolean putInObjectStore(String container, String objectName, InputStream data) {
        return objectStore.putObject(objectStoreAccountName, container, null, null, objectName, data);
    }

    private boolean isVirusScanSuccess(MultipartFile file) {
        try {
            log.info("sessionId", "idType", "id", "In isVirusScanSuccess method of BiometricTestDataService");
            return virusScan.scanDocument(file.getBytes());
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(e));
            log.error("sessionId", "idType", "id",
                    "In isVirusScanSuccess method of BiometricTestDataService Service - " + e.getMessage());
            throw new VirusScannerException(ToolkitErrorCodes.OBJECT_STORE_UNABLE_TO_ADD_FILE.getErrorCode(),
                    ToolkitErrorCodes.OBJECT_STORE_UNABLE_TO_ADD_FILE.getErrorMessage() + e.getMessage());
        }
    }
}
