package iuliiaponomareva.evroscudo.db;

import android.provider.BaseColumns;


public class RatesContract {
    public RatesContract() {}

    public static abstract class ExchangeRate implements BaseColumns {
        public static final String TABLE_NAME = "rates";
        public static final String COLUMN_NAME_BANK = "bank";
        public static final String COLUMN_NAME_CURRENCY_CODE = "code";
        public static final String COLUMN_NAME_RATE = "rate";

    }

    public static abstract class Nominals implements BaseColumns {
        public static final String TABLE_NAME = "nominals";
        public static final String COLUMN_NAME_CURRENCY_CODE = "code";
        public static final String COLUMN_NAME_BANK = "bank";
        public static final String COLUMN_NAME_NOMINAL = "nominal";

    }

    public static abstract class Dates implements BaseColumns {
        public static final String TABLE_NAME = "dates";
        public static final String COLUMN_NAME_BANK = "bank";
        public static final String COLUMN_NAME_DATE = "date";
    }
}
