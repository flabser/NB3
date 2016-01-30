package kz.flabs.users;

import kz.flabs.appenv.AppEnv;
import kz.flabs.runtimeobj.constants.SortingType;
import kz.flabs.webrule.constants.FieldType;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;


public class RunTimeParameters {
    public HashMap<String, Sorting> sortingMap = new HashMap<String, Sorting>();
    public HashMap<String, Filter> filtersMap = new HashMap<String, Filter>();

    public HashSet<Sorting> sorting = new HashSet<Sorting>();
    public HashSet<Filter> filters = new HashSet<Filter>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

    public RunTimeParameters() {
    }

    public RunTimeParameters(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    public void parseParameters(String[] strings) {
        boolean isSorting = false, isFilter = false, resetSorting = false, resetFilter = false;
        String sortingColumnName = "", sortingDirection = "", filterColumnName = "", keyWord = "";
        for (String val : strings) {
            StringTokenizer t = new StringTokenizer(val, "~");
            String fieldName = t.nextToken();
            String value = t.nextToken();
            if (fieldName.equalsIgnoreCase("sorting_mode")) {
                if (value.equalsIgnoreCase("on")) {
                    isSorting = true;
                } else if (value.equalsIgnoreCase("reset_all")) {
                    resetSorting = true;
                }
            } else if (fieldName.equalsIgnoreCase("sorting_column")) {
                sortingColumnName = value;
            } else if (fieldName.equalsIgnoreCase("sorting_direction")) {
                sortingDirection = value;
            } else if (fieldName.equalsIgnoreCase("filter_mode")) {
                if (value.equalsIgnoreCase("on")) {
                    isFilter = true;
                } else if (value.equalsIgnoreCase("resetFilter")) {
                    resetFilter = true;
                }
            } else if (fieldName.equalsIgnoreCase("filtered_column")) {
                filterColumnName = value;
            } else if (fieldName.equalsIgnoreCase("key_word")) {
                try {
                    keyWord = new String(((String) value).getBytes("ISO-8859-1"), "UTF-8").replace("'", "\"");
                } catch (UnsupportedEncodingException e) {
                    AppEnv.logger.errorLogEntry(e);
                    keyWord = value;
                }
            }
        }

        if (isSorting) {
            Sorting s = new Sorting(sortingColumnName);
            s.sortingDirection = SortingType.valueOf(sortingDirection.toUpperCase());
            sorting.add(s);
            sortingMap.put(sortingColumnName, s);
        } else if (resetSorting) {
            sorting.clear();
            sortingMap.clear();
        }

        if (isFilter) {
            Filter f = new Filter(filterColumnName);
            f.keyWord = keyWord;
            if (filterColumnName.equalsIgnoreCase("viewnumber")) {
                f.fieldType = FieldType.NUMBER;
            } else if (filterColumnName.equalsIgnoreCase("viewdate")) {
                f.fieldType = FieldType.DATE;
            } else {
                f.fieldType = FieldType.TEXT;
            }
            filters.add(f);
            filtersMap.put(filterColumnName, f);
        } else if (resetFilter) {
            filters.clear();
            filtersMap.clear();
        }

    }

    public String toString() {
        String val = "";

        for (Sorting s : sorting) {
            val += s;
        }

        for (Sorting s : sorting) {
            val += s;
        }

        return val;
    }

    public HashSet<Sorting> getSorting() {
        return sorting;
    }

    public HashSet<Filter> getFilters() {
        return filters;
    }

    public SimpleDateFormat getDateFormat() {
        return simpleDateFormat;
    }

    class Parameter {
        protected String columnName;

        Parameter(String columnName) {
            if (columnName != null && !"".equalsIgnoreCase(columnName)) {
                this.columnName = columnName;
            }
        }

        public String getName() {
            return columnName.toUpperCase();
        }

        public boolean isValid() {
            return (columnName != null && !"".equalsIgnoreCase(columnName));
        }


    }

    public class Sorting extends Parameter {
        public SortingType sortingDirection;

        public Sorting(String c) {
            super(c);
        }

        public String toString() {
            return columnName + " sorting:" + sortingDirection;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Sorting)) return false;

            Sorting sorting = (Sorting) o;

            if (columnName != sorting.columnName) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return columnName != null ? columnName.hashCode() : 0;
        }

        @Override
        public boolean isValid() {
            return (super.isValid() && sortingDirection != null);
        }
    }

    public class Filter extends Parameter {
        public FieldType fieldType;
        public String keyWord;

        Filter(String c) {
            super(c);
        }

        public String toString() {
            return columnName + "=" + keyWord;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Filter)) return false;

            Filter filter = (Filter) o;

            if (columnName != null ? !columnName.equals(filter.columnName) : filter.columnName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return columnName != null ? columnName.hashCode() : 0;
        }

        @Override
        public boolean isValid(){
            return (super.isValid() && keyWord != null);
        }
    }


    public static void main(String[] args) {
        String viewTextFileds[] = {"sorting_mode~on", "sorting_column~viewtext1", "sorting_direction~asc", "filter_mode~on", "filtered_column~viewtext1", "key_word~труба"};

        RunTimeParameters r = new RunTimeParameters();
        r.parseParameters(viewTextFileds);


    }

}