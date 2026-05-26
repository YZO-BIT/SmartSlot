package com.timetable.backend.service;

import com.timetable.backend.model.Configuration;
import com.timetable.backend.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
    @Autowired
    private ConfigurationRepository configurationRepository;

    public String getConfigValue(String key, String defaultValue) {
        return configurationRepository.findByConfigKey(key)
                .map(Configuration::getConfigValue)
                .orElse(defaultValue);
    }

    public int getIntConfigValue(String key, int defaultValue) {
        try {
            String value = getConfigValue(key, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
