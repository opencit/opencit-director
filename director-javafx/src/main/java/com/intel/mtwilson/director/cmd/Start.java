/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.director.cmd;

import com.intel.dcsg.cpg.console.AbstractCommand;
import com.intel.mtwilson.director.javafx.ui.ManifestTool;

/**
 *
 * @author rksavino
 */
public class Start extends AbstractCommand {
    public void execute(String[] args) {
        ManifestTool.main(args);
    }
}
