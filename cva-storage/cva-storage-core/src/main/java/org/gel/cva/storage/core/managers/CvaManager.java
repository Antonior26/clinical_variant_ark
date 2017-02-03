package org.gel.cva.storage.core.managers;

import org.gel.cva.storage.core.config.CvaConfiguration;

/**
 * Created by priesgo on 19/01/17.
 */
public abstract class CvaManager {

    protected CvaConfiguration cvaConfiguration;

    public CvaManager(CvaConfiguration cvaConfiguration) {
        this.cvaConfiguration = cvaConfiguration;
    }
}
