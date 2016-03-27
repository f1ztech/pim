package ru.mipt.pim.server.web;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//@Configuration
public class WebConfiguration /*extends WebMvcConfigurerAdapter */ {

//    @Override
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
//        builder.indentOutput(true).dateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"));
//        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
//    }
	
//	@Bean
//	public Jackson2ObjectMapperBuilder jacksonBuilder() {
//		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
//		builder.indentOutput(true);
//		builder.dateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"));
//		return builder;
//	}
}