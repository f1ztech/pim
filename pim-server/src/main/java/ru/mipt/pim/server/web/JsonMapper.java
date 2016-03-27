package ru.mipt.pim.server.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class JsonMapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	public class DateSerializer extends StdSerializer<Date> {

		public DateSerializer() {
			super(Date.class);
		}

		@Override
		public void serialize(Date date, JsonGenerator json, SerializerProvider provider) throws IOException, JsonGenerationException {
			// The client side will handle presentation, we just want it accurate
			DateFormat df = StdDateFormat.getBlueprintISO8601Format();
			String out = df.format(date);
			json.writeString(out);
		}

	}

	public class DateDeserializer extends StdDeserializer<Date> {

		private static final long serialVersionUID = 6056425840960642203L;

		public DateDeserializer() {
			super(Date.class);
		}

		@Override
		public Date deserialize(JsonParser json, DeserializationContext context) throws IOException, JsonProcessingException {
			try {
				DateFormat df = StdDateFormat.getBlueprintISO8601Format();
				return df.parse(json.getText());
			} catch (ParseException e) {
				return null;
			}
		}

	}

	public JsonMapper() {
		SimpleModule module = new SimpleModule("JSONModule", new Version(2, 0, 0, null, null, null));
		module.addSerializer(Date.class, new DateSerializer());
		module.addDeserializer(Date.class, new DateDeserializer());
		// Add more here ...
		registerModule(module);
	}

}
