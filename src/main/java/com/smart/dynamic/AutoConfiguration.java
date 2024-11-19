package com.smart.dynamic;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.smart.dynamic.service.DynamicLogicServiceImpl;

@Configuration
@Import({ DynamicLogicServiceImpl.class })
public class AutoConfiguration {
}