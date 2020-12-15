package jsf.lazymodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import core.Analysis.Result;

public class LazyResultDataModel extends LazyDataModel<Result> {

	 private List<Result> datasource;
	 
	    public LazyResultDataModel(List<Result> datasource) {
	        this.datasource = datasource;
	    }
	 
	    @Override
	    public Result getRowData(String rowKey) {
	        for (Result result : datasource) {
	            if (result.toString().equals(rowKey)) {
	                return result;
	            }
	        }
	 
	        return null;
	    }
	 
	    @Override
	    public Object getRowKey(Result result) {
	        return result.toString();
	    }
	 
	    @Override
	    public List<Result> load(int first, int pageSize, Map<String, SortMeta> sortMeta, Map<String, FilterMeta> filterMeta) {
	        List<Result> data = new ArrayList<>();
	 
	        //filter
	        for (Result result : datasource) {
	            boolean match = true;
	 
	            if (filterMeta != null) {
	                for (FilterMeta meta : filterMeta.values()) {
	                    try {
	                        String filterField = meta.getFilterField();
	                        Object filterValue = meta.getFilterValue();
	                        String fieldValue = String.valueOf(result.getClass().getField(filterField).get(result));
	 
	                        if (filterValue == null || fieldValue.startsWith(filterValue.toString())) {
	                            match = true;
	                        }
	                        else {
	                            match = false;
	                            break;
	                        }
	                    }
	                    catch (Exception e) {
	                        match = false;
	                    }
	                }
	            }
	 
	            if (match) {
	                data.add(result);
	            }
	        }
	 
	        //sort
	        if (sortMeta != null && !sortMeta.isEmpty()) {
	            for (SortMeta meta : sortMeta.values()) {
	                Collections.sort(data, new LazySorter(meta.getSortField(), meta.getSortOrder()));
	            }
	        }
	 
	        //rowCount
	        int dataSize = data.size();
	        this.setRowCount(dataSize);
	 
	        //paginate
	        if (dataSize > pageSize) {
	            try {
	                return data.subList(first, first + pageSize);
	            }
	            catch (IndexOutOfBoundsException e) {
	                return data.subList(first, first + (dataSize % pageSize));
	            }
	        }
	        else {
	            return data;
	        }
	    }
}
