package sd.client.ui;

import java.util.*;

public class Table<T> {
    public interface TableColumnFactory<T> {
        String getValue(T t);
    }

    private Map<String, TableColumnFactory<T>> columns;
    private List<T> values;

    public Table() {
        columns = new LinkedHashMap<>(); // LinkedHashMap para manter a ordem
        values = new ArrayList<>();
    }

    public void addColumn(String name, TableColumnFactory<T> factory) {
        this.columns.put(name, factory);
    }

    public void addItems(Collection<T> items) {
        values.addAll(items);
    }

    public void show() {
        int[] widths = new int[this.columns.size()];
        List<List<String>> lines = new ArrayList<>();

        int i = 0;
        for (String columnName : this.columns.keySet()) {
            widths[i] = columnName.length();
            i++;
        }

        for (T value : values) {
            i = 0;
            List<String> thisLine = new ArrayList<>();
            for (Map.Entry<String, TableColumnFactory<T>> entry : this.columns.entrySet()) {
                String asString = entry.getValue().getValue(value);
                if (widths[i] < asString.length()) {
                    widths[i] = asString.length();
                }
                thisLine.add(asString);
                i++;
            }
            lines.add(thisLine);
        }

        i = 0;
        System.out.print(ClientUI.ANSI_BOLD + ClientUI.ANSI_CYAN);
        for (String title : this.columns.keySet()) {
            System.out.printf("%1$-" + widths[i] + "s ", title);
            i++;
        }
        System.out.println(ClientUI.ANSI_RESET);

        for (List<String> line : lines) {
            i = 0;
            for (String val : line) {
                System.out.printf("%1$-" + widths[i] + "s ", val);
                i++;
            }
            System.out.println();
        }
    }
}
