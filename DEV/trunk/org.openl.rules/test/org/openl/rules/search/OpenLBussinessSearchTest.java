package org.openl.rules.search;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openl.conf.UserContext;
import org.openl.impl.OpenClassJavaWrapper;
import org.openl.rules.lang.xls.binding.XlsMetaInfo;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.XlsModuleSyntaxNode;
import org.openl.rules.table.properties.ITableProperties;

public class OpenLBussinessSearchTest {
    
    private String __src = "test/rules/Tutorial_4_Test.xls";
    
    private OpenLBussinessSearch search = new OpenLBussinessSearch();
    
    private XlsModuleSyntaxNode getTables() {        
        UserContext ucxt = new UserContext(Thread.currentThread().getContextClassLoader(), ".");
        OpenClassJavaWrapper wrapper = OpenClassJavaWrapper.createWrapper("org.openl.xls", ucxt, __src);
        XlsMetaInfo xmi = (XlsMetaInfo) wrapper.getOpenClass().getMetaInfo();
        XlsModuleSyntaxNode xsn = xmi.getXlsModuleNode();
        return xsn;
    }
    
    private void initSearchCondition(Map<String, Object> propList) {
        BussinessSearchCondition searchCondition = new BussinessSearchCondition();        
        searchCondition.setPropToSearch(propList);
        search.setBusSearchCondit(searchCondition);
    }
    
    @Test 
    public void testTableByName() {
        Map<String, Object> propList = new HashMap<String, Object>();
        propList.put("name", "Vehicle Discounts");
        initSearchCondition(propList);
        Object searchResult = search.search(getTables());
        if((searchResult != null) && (searchResult instanceof OpenLBussinessSearchResult)) {
            List<TableSyntaxNode> foundTables = ((OpenLBussinessSearchResult) searchResult).getFoundTables();
            assertTrue("There is only one table for this cryteria",foundTables.size()==1);
            assertEquals("Display names are identical", "Rules DoubleValue vehicleDiscount(Vehicle vehicle, String vehicleTheftRating)" ,
                    foundTables.get(0).getDisplayName());            
        } else {
            fail();
        }        
    }
    
    @Test 
    public void testTableByNameAndCategory() {        
        Map<String, Object> propList = new HashMap<String, Object>();
        propList.put("name", "Vehicle Score Processing Sequence");
        propList.put("category", "Auto-Scoring");
        initSearchCondition(propList);
        Object searchResult = search.search(getTables());
        if((searchResult != null) && (searchResult instanceof OpenLBussinessSearchResult)) {
            List<TableSyntaxNode> foundTables = ((OpenLBussinessSearchResult) searchResult).getFoundTables();
            assertTrue("There is only one table for this cryteria",foundTables.size()==1);
            assertEquals("Display names are identical", "Rules void vehicleScore1(VehicleCalc vc)" ,
                    foundTables.get(0).getDisplayName());
            //System.out.println("name: "+ foundTables.get(0).getDisplayName());
        } else {
            fail();
        }        
    }
    
    @Test 
    public void testWithConsists() {
        XlsModuleSyntaxNode xls = getTables();        
        Map<String, Object> propList = new HashMap<String, Object>();
        propList.put("name", "Vehicle Score Processing Sequence");
        initSearchCondition(propList);
        search.getBusSearchCondit().setTablesContains(getTableConsists(xls, "Vehicle Score Processing Sequence"));
        Object searchResult = search.search(xls);
        if((searchResult != null) && (searchResult instanceof OpenLBussinessSearchResult)) {
            List<TableSyntaxNode> foundTables = ((OpenLBussinessSearchResult) searchResult).getFoundTables();
            assertTrue("There is only one table for this cryteria",foundTables.size()==1);
            assertEquals("Display names are identical", "Rules void vehicleScore(VehicleCalc vc)" ,
                    foundTables.get(0).getDisplayName());            
        } else {
            fail();
        }        
    }
    
    private TableSyntaxNode[] getTableConsists(XlsModuleSyntaxNode xls, String nameProp) {
        TableSyntaxNode[] listTables = new TableSyntaxNode[1];
        TableSyntaxNode result = null;
        
        for(TableSyntaxNode table : xls.getXlsTableSyntaxNodes()) {
            ITableProperties tableProp = table.getTableProperties();
            if(tableProp != null) {
                if(tableProp.getPropertyValue("name") != null && tableProp.getPropertyValue("name").equals(nameProp) && 
                        tableProp.getPropertyValue("category") == null) {
                    listTables[0] = table;
                }
            }
            
        }
        return listTables;
    }
                                               

}
