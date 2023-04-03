package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.abis.InsertRequestDto;
import io.mosip.compliance.toolkit.dto.abis.QueueRequest;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class ABISQueueService {

	private Logger log = LoggerConfiguration.logConfig(ABISQueueService.class);

	@Autowired
	private ApplicationContext context;

	@Value("${mosip.toolkit.api.id.abis.post}")
	private String postSendToQueue;

	@Value("${mosip.toolkit.api.id.abis.get}")
	private String getReadFromQueue;
	
	@Value("${spring.activemq.inboundQueueName}")
	private String inboundQueueName;
	
	@Value("${spring.activemq.outboundQueueName}")
	private String outboundQueueName;

	Gson gson = new GsonBuilder().create();

	public ResponseWrapper<Boolean> sendToQueue(QueueRequest queueRequest) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		Boolean resp = false;
		try {
			JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
			// Send a message with a POJO - the template reuse the message converter
			System.out.println("Sending to queue");
			String abisMethodName = queueRequest.getMethodName();
			if (MethodName.INSERT.getCode().equals(abisMethodName)) {
				Object data = gson.fromJson(queueRequest.getRequestJson(), InsertRequestDto.class);
				System.out.print(data);
				jmsTemplate.convertAndSend(outboundQueueName, data);
			}
			resp = true;
		} catch (Exception ex) {
			resp = false;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In sendToQueue method of ABISQueueService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.ABIS_QUEUE_SEND_EXCEPTION.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.ABIS_QUEUE_SEND_EXCEPTION.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postSendToQueue);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(resp);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<List<String>> readFromQueue() {
		ResponseWrapper<List<String>> responseWrapper = new ResponseWrapper<>();
		List<String> msgsList = new ArrayList<String>();
		
		try {
			JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
			System.out.println("Reading from queue");
			jmsTemplate.browse(inboundQueueName, new BrowserCallback<Integer>() {
				public Integer doInJms(final Session session, final QueueBrowser browser) throws JMSException {
					Enumeration<?> enumeration = browser.getEnumeration();
					int counter = 0;
					while (enumeration.hasMoreElements()) {
						Message msg = (Message) enumeration.nextElement();
						String text2 = ((TextMessage)msg).getText();
						System.out.println(String.format("\tFound : %s", text2));
						msgsList.add(text2);
						counter += 1;
					}
					return counter;
				}
			});
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In readFromQueue method of ABISQueueService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.ABIS_QUEUE_READ_EXCEPTION.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.ABIS_QUEUE_READ_EXCEPTION.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getReadFromQueue);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(msgsList);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
}
