/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.api.util;

import com.intel.dcsg.cpg.crypto.file.PemKeyEncryption;
import com.intel.kms.api.KeyDescriptor;
import com.intel.mtwilson.util.crypto.key2.CipherKeyAttributes;
import com.intel.mtwilson.util.crypto.key2.IntegrityKeyAttributes;

/**
 * An adapter KeyDescriptor that wraps a PemKeyEncryption object
 * 
 * @author jbuhacoff
 */
public class PemKeyEncryptionKeyDescriptor extends KeyDescriptor {
    public PemKeyEncryptionKeyDescriptor(PemKeyEncryption keyEnvelope) {
        CipherKeyAttributes contentAttributes = new CipherKeyAttributes();
        contentAttributes.setKeyId(keyEnvelope.getContentKeyId());
        contentAttributes.setAlgorithm(keyEnvelope.getContentAlgorithm());
        contentAttributes.setKeyLength(keyEnvelope.getContentKeyLength());
        contentAttributes.setMode(keyEnvelope.getContentMode());
        contentAttributes.setPaddingMode(keyEnvelope.getContentPaddingMode());

        CipherKeyAttributes encryptionAttributes = new CipherKeyAttributes();
        encryptionAttributes.setAlgorithm(keyEnvelope.getEncryptionAlgorithm());
        encryptionAttributes.setKeyId(keyEnvelope.getEncryptionKeyId());

        IntegrityKeyAttributes integrityAttributes = new IntegrityKeyAttributes();

        setContent(contentAttributes);
        setEncryption(encryptionAttributes);
        setIntegrity(integrityAttributes);
    }
}
