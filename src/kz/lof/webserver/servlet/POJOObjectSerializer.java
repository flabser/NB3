package kz.lof.webserver.servlet;

import java.io.IOException;

import kz.lof.scripting._POJOListWrapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class POJOObjectSerializer extends JsonSerializer<_POJOListWrapper> {

	@Override
	public void serialize(_POJOListWrapper value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		System.out.println(value.getClass().getName());

		jgen.writeString(value.toJSON());

	}

}
