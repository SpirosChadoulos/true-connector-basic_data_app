package it.eng.idsa.dataapp.service.impl;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.TokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.serialization.SerializationHelper;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */


/**
 * Service Implementation for managing MultiPartMessage.
 */
@Service
@Transactional
public class MultiPartMessageServiceImpl implements MultiPartMessageService {

	@Override
	public String getHeader(String body) {
		MultiPartMessage deserializedMultipartMessage = MultiPart.parseString(body);
		return deserializedMultipartMessage.getHeaderString();
	}

	@Override
	public String getPayload(String body) {
		MultiPartMessage deserializedMultipartMessage = MultiPart.parseString(body);
		return deserializedMultipartMessage.getPayload();
	}

	@Override
	public Message getMessage(String body) {
		MultiPartMessage deserializedMultipartMessage = MultiPart.parseString(body);
		return deserializedMultipartMessage.getHeader();
	}
	
	@Override
	public String convertMessageToString(Message message) throws JsonProcessingException, ParseException {
		String msgSerialized = new Serializer().serializePlainJson(message);
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
		String messageString =new Serializer().serializePlainJson(jsonObject);
		return messageString;
	}

	@Override
	public String addToken(Message message, String token) {
		String output = null;
		try {
			String msgSerialized = new Serializer().serializePlainJson(message);
			Token tokenJsonValue = new TokenBuilder()
					._tokenFormat_(TokenFormat.JWT)
					._tokenValue_(token).build();
			String tokenValueSerialized=new Serializer().serializePlainJson(tokenJsonValue);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			JSONObject jsonObjectToken = (JSONObject) parser.parse(tokenValueSerialized);
			jsonObject.put("authorizationToken",jsonObjectToken);
			output=new Serializer().serializePlainJson(jsonObject);
		} catch (JsonProcessingException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	@Override
	public String removeToken(Message message) {
		String output = null;
		try {
			String msgSerialized = new Serializer().serializePlainJson(message);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			jsonObject.remove("authorizationToken");
			output=new Serializer().serializePlainJson(jsonObject);
		} catch (JsonProcessingException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}


	@Override
	public Message getMessage(Object header) {
		Message message = null;
		try {
			message = SerializationHelper.getInstance().fromJsonLD(String.valueOf(header), Message.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;

	}

	@Override
	public HttpEntity createMultipartMessage(String header, String payload/*, String boundary, String contentType*/) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder.addTextBody("header", header);
		multipartEntityBuilder.addTextBody("payload", payload);

		// multipartEntityBuilder.setBoundary(boundary)
		HttpEntity multipart = multipartEntityBuilder.build();

		//return multipart;
		InputStream streamHeader = new ByteArrayInputStream(header.getBytes(StandardCharsets.UTF_8));
		InputStream streamPayload = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));




		multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.STRICT);
		try {
			FormBodyPart bodyHeaderPart;

			/*
			 * bodyHeaderPart = FormBodyPartBuilder.create() .addField("Content-Lenght",
			 * ""+header.length()) .setName("header") .setBody(new StringBody(header))
			 * .build();
			 */
			bodyHeaderPart = new FormBodyPart("header", new StringBody(header, ContentType.DEFAULT_TEXT)) {
				@Override
				protected void generateContentType(ContentBody body) {
				}
				@Override
				protected void generateTransferEncoding(ContentBody body){
				}
			};
			bodyHeaderPart.addField("Content-Lenght", ""+header.length());

			FormBodyPart bodyPayloadPart;
			bodyPayloadPart=new FormBodyPart("payload", new StringBody(payload, ContentType.DEFAULT_TEXT)) {
				@Override
				protected void generateContentType(ContentBody body) {
				}
				@Override
				protected void generateTransferEncoding(ContentBody body){
				}
			};
			bodyPayloadPart.addField("Content-Lenght", ""+payload.length());

			/*
			 * = FormBodyPartBuilder.create() .addField("Content-Lenght",
			 * ""+payload.length()) .setName("payload") .setBody(new StringBody(payload))
			 * .build();
			 */


			multipartEntityBuilder.addPart (bodyHeaderPart);
			multipartEntityBuilder.addPart(bodyPayloadPart);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return multipartEntityBuilder.build();
	}

	@Override
	public String getToken(String message) {
		JsonObject messageObject = new JsonParser().parse(message).getAsJsonObject();
		JsonObject authorizationTokenObject = messageObject.get("authorizationToken").getAsJsonObject();
		String token = authorizationTokenObject.get("tokenValue").getAsString();
		return token;
	}




}
