package org.openl.rules.tableeditor.model;

public class CellEditorFactory implements ICellEditorFactory {

    public ICellEditor makeComboboxEditor(String[] choices) {
        return new ComboBoxCellEditor(choices, choices);
    }

    public ICellEditor makeIntEditor(int min, int max) {
        return new IntRangeCellEditor(min, max);
    }

    public ICellEditor makeMultilineEditor() {
        return new MultilineEditor();
    }

    public ICellEditor makeTextEditor() {
        return new TextCellEditor();
    }

    public ICellEditor makeFormulaEditor() {
        return new FormulaCellEditor();
    }
    
    public ICellEditor makeDateEditor() {
        return new DateCellEditor();
    }
    
    public ICellEditor makeBooleanEditor() {
        return new BooleanCellEditor();
    }

    public ICellEditor makeMultiSelectEditor(String[] choices) {        
        return new MultiSelectCellEditor(choices, choices);
    }

}
