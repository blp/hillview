package org.hiero.table;

import org.hiero.sketches.ColumnSortOrientation;
import org.hiero.table.api.IRow;

/**
 * An abstract class that implements IRow, which is an interface for accessing rows in a table.
 * Concrete classes that extend it are RowSnapshot and VirtualRowSnapshot. The main methods this
 * class provides is equality testing. This for instance allows easy comparison between the
 * classes mentioned above.
 */
public abstract class BaseRowSnapshot implements IRow {
    /**
     * Compare this row to the other for equality.
     * Only the fields in the schema are compared.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean compareForEquality(BaseRowSnapshot other, Schema schema) {
        for (String cn: schema.getColumnNames()) {
            if (this.isMissing(cn) && other.isMissing(cn))
                continue;
            if (this.isMissing(cn) || other.isMissing(cn))
                return false;
            boolean same;
            switch (schema.getKind(cn)) {
                case Category:
                case String:
                case Json:
                    same = this.getString(cn).equals(other.getString(cn));
                    break;
                case Date:
                    same = this.getDate(cn).equals(other.getDate(cn));
                    break;
                case Integer:
                    same = this.getInt(cn) == other.getInt(cn);
                    break;
                case Double:
                    same = this.getDouble(cn) == other.getDouble(cn);
                    break;
                case Duration:
                    same = this.getDuration(cn).equals(other.getDuration(cn));
                    break;
                default:
                    throw new RuntimeException("Unexpected kind " + schema.getKind(cn));
            }
            if (!same)
                return false;
        }
        return true;
    }

    /**
     * Compare this row to the other for ordering.
     * Only the fields in the schema are compared.
     */
    @SuppressWarnings("ConstantConditions")
    public int compareTo(BaseRowSnapshot other, RecordOrder ro) {
        for (ColumnSortOrientation cso: ro) {
            String cn = cso.columnDescription.name;
            if (this.isMissing(cn) && other.isMissing(cn))
                continue;
            if (this.isMissing(cn))
                return -1;
            if (other.isMissing(cn))
                return 1;
            int c;
            switch (cso.columnDescription.kind) {
                case Category:
                case String:
                case Json:
                    c = this.getString(cn).compareTo(other.getString(cn));
                    break;
                case Date:
                    c = this.getDate(cn).compareTo(other.getDate(cn));
                    break;
                case Integer:
                    c = Integer.compare(this.getInt(cn), other.getInt(cn));
                    break;
                case Double:
                    c = Double.compare(this.getDouble(cn), other.getDouble(cn));
                    break;
                case Duration:
                    c = this.getDuration(cn).compareTo(other.getDuration(cn));
                    break;
                default:
                    throw new RuntimeException("Unexpected kind " + cso.columnDescription.kind);
            }
            if (!cso.isAscending)
                c = -c;
            if (c != 0)
                return c;
        }
        return 0;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String cn : this.getColumnNames()) {
            Object o = this.getObject(cn);
            if (!first)
                builder.append(",");
            if (o != null)
                builder.append(o.toString());
            first = false;
        }
        return builder.toString();
    }
}