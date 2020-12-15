package jsf.lazymodel;

import java.util.Comparator;

import org.primefaces.model.SortOrder;

import core.Analysis;

public class LazySorter implements Comparator<Analysis.Result> {
	 
    private String sortField;
     
    private SortOrder sortOrder;
     
    public LazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }
 
    public int compare(Analysis.Result car1, Analysis.Result car2) {
        try {
            Object value1 = Analysis.Result.class.getField(this.sortField).get(car1);
            Object value2 = Analysis.Result.class.getField(this.sortField).get(car2);
 
            int value = ((Comparable<Object>)value1).compareTo(value2);
             
            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        }
        catch(Exception e) {
            throw new RuntimeException();
        }
    }
}
