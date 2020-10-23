package org.openl.rules.project.resolving;

import org.openl.rules.project.model.Module;
import org.openl.rules.table.properties.ITableProperties;

public interface PropertiesFileNameProcessor {

    @Deprecated
    default ITableProperties process(Module module, String fileNamePattern) throws NoMatchFileNameException,
            InvalidFileNamePatternException {
        return process(module, new String[]{fileNamePattern});
    }

    default ITableProperties process(Module module, String... fileNamePatterns) throws NoMatchFileNameException,
                                                                    InvalidFileNamePatternException {
        return process(module, fileNamePatterns != null ? fileNamePatterns[0] : null);
    }

}
