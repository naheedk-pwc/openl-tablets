package org.openl.config;

/**
 * @author Aleh Bykhavets
 */
public class ConfigPropertyString extends ConfigProperty<String> {
    public ConfigPropertyString(String name, String defValue) {
        super(name, defValue);
    }

    protected void setTextValue(String s) {
        setValue(s);
    }
}
