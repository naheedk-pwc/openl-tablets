package org.openl.rules.serialization;

/*-
 * #%L
 * OpenL - STUDIO - Jackson
 * %%
 * Copyright (C) 2016 - 2020 OpenL Tablets
 * %%
 * See the file LICENSE.txt for copying permission.
 * #L%
 */

import java.lang.reflect.Field;
import java.util.Objects;

import org.openl.rules.calc.SpreadsheetCell;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;

public class SpreadsheetResultBeanPropertyNamingStrategy {

    protected static abstract class SpreadsheetResultBeanPropertyNamingStrategyBase extends PropertyNamingStrategy {

        protected abstract String transform(String name);

        protected abstract String transform(String column, String row);

        protected String toUpperCamelCase(String input) {
            if (input == null || input.length() == 0) {
                return input;
            }
            char c = input.charAt(0);
            char uc = Character.toUpperCase(c);
            if (c == uc) {
                return input;
            }
            StringBuilder sb = new StringBuilder(input);
            sb.setCharAt(0, uc);
            return sb.toString();
        }

        protected String toLowerCamelCase(String input) {
            if (input == null || input.length() == 0) {
                return input;
            }
            char c = input.charAt(0);
            char uc = Character.toLowerCase(c);
            if (c == uc) {
                return input;
            }
            StringBuilder sb = new StringBuilder(input);
            sb.setCharAt(0, uc);
            return sb.toString();
        }

        protected String transform(String defaultName, SpreadsheetCell spreadsheetCell) {
            if (spreadsheetCell.simpleRefByColumn()) {
                return transform(spreadsheetCell.column());
            } else if (spreadsheetCell.simpleRefByRow()) {
                return transform(spreadsheetCell.row());
            } else {
                return transform(spreadsheetCell.column(), spreadsheetCell.row());
            }
        }

        @Override
        public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
            if (field.hasAnnotation(SpreadsheetCell.class)) {
                SpreadsheetCell spreadsheetCell = field.getAnnotation(SpreadsheetCell.class);
                String ret = transform(defaultName, spreadsheetCell);
                int c = 0;
                int duplicates = 0;
                for (Field f : field.getDeclaringClass().getDeclaredFields()) {
                    SpreadsheetCell sc = f.getAnnotation(SpreadsheetCell.class);
                    if (sc != null) {
                        if (Objects.equals(ret, transform(defaultName, sc))) {
                            duplicates++;
                        }
                    }
                }
                return duplicates < 2 ? ret : defaultName;
            }
            return defaultName;
        }
    }

    public static class UpperCamelCaseStrategy extends SpreadsheetResultBeanPropertyNamingStrategyBase {
        @Override
        protected String transform(String name) {
            return toUpperCamelCase(name);
        }

        @Override
        protected String transform(String column, String row) {
            return toUpperCamelCase(column) + toUpperCamelCase(row);
        }
    }

    public static class LowerCamelCaseStrategy extends SpreadsheetResultBeanPropertyNamingStrategyBase {
        @Override
        protected String transform(String name) {
            return toLowerCamelCase(name);
        }

        @Override
        protected String transform(String column, String row) {
            return toLowerCamelCase(column) + toUpperCamelCase(row);
        }
    }

    public static class LowerCaseStrategy extends SpreadsheetResultBeanPropertyNamingStrategyBase {
        @Override
        protected String transform(String name) {
            if (name == null || name.length() == 0) {
                return name;
            }
            return name.toLowerCase();
        }

        @Override
        protected String transform(String column, String row) {
            return transform(column) + "_" + transform(row);
        }
    }
}