package it.eng.idsa.dataapp.service;

import org.apache.http.HttpEntity;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing MultiPartMessage.
 */
public interface MultiPartMessageService {
	public String getHeader(String body);
	public String getPayload(String body);

	public Message getMessage(String body);
	public String convertMessageToString(Message message) throws JsonProcessingException, ParseException;
	public Message getMessage(Object header);
	public String addToken(Message message, String token);
	public HttpEntity createMultipartMessage(String header, String payload/*, String boundary, String contentType*/);
	public String getToken(String message);
	public String removeToken(Message message);
}
