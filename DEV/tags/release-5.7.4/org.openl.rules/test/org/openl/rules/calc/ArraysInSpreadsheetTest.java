package org.openl.rules.calc;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openl.rules.BaseOpenlBuilderHelper;

public class ArraysInSpreadsheetTest extends BaseOpenlBuilderHelper {
    
    private static String __src = "test/rules/calc1/ArrayInSpreadsheet.xlsx";

    public ArraysInSpreadsheetTest() {
        super(__src);
    }
    
    @Test
    public void testWorkingWithArrayInSpreadsheet() throws ClassNotFoundException {
        Object result = invokeMethod("start");
        assertTrue(result.getClass().isArray());
        Class<?> clazz = Class.forName("org.openl.generated.beans.Driver");        
        assertTrue(result.getClass().getComponentType().equals(clazz));        
    }

}
