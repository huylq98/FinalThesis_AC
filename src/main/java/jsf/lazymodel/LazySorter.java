package jsf.lazymodel;

import java.util.Comparator;

import org.primefaces.model.SortOrder;

import core.Analysis.Result;;

public class LazySorter implements Comparator<Result> {
	 
    private String sortField;
     
    private SortOrder sortOrder;
     
    public LazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }
 
    public int compare(Result result1, Result result2) {
        try {
            Object value1 = Result.class.getField(this.sortField).get(result1);
            Object value2 = Result.class.getField(this.sortField).get(result2);
 
            int value = ((Comparable<Object>)value1).compareTo(value2);
             
            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        }
        catch(Exception e) {
            throw new RuntimeException();
        }
    }
}
