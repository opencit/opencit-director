package com.intel.director.service;

import com.intel.director.api.GenericResponse;
import com.intel.director.api.MountWilsonSetting;
import com.intel.director.api.ValidateKMSConfigurationRequest;

public interface Setting {

    public GenericResponse validateKMS(ValidateKMSConfigurationRequest request);

    public GenericResponse validateMTW(MountWilsonSetting request);
}
