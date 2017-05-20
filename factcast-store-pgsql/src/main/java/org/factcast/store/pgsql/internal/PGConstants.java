package org.factcast.store.pgsql.internal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

/**
 * String constants mainly used in SQL-Statement creation
 * 
 * @author uwe.schaefer@mercateo.com
 *
 */
@UtilityClass
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
class PGConstants {

    String NEXT_FROM_CATCHUP_SEQ = "SELECT nextval('catchup_seq')";

    String TABLE_CATCHUP = "catchup";

    String TABLE_FACT = "fact";

    String CHANNEL_NAME = "fact_insert";

    String COLUMN_PAYLOAD = "payload";

    String COLUMN_HEADER = "header";

    String COLUMN_SER = "ser";

    String COLUMN_CID = "cid";

    String ALIAS_ID = "id";

    String ALIAS_NS = "ns";

    String ALIAS_TYPE = "type";

    String ALIAS_AGGID = "aggIds";

    String PROJECTION_FACT = String.join(", ", COLUMN_SER, COLUMN_HEADER, COLUMN_PAYLOAD,
            fromHeader(ALIAS_ID), fromHeader(ALIAS_AGGID), fromHeader(ALIAS_NS), fromHeader(
                    ALIAS_TYPE));

    // FIXME still needed?
    String PROJECTION_ID = String.join(", ", COLUMN_SER, empty(COLUMN_HEADER), empty(
            COLUMN_PAYLOAD), fromHeader(ALIAS_ID), fromHeader(ALIAS_AGGID), fromHeader(ALIAS_NS),
            fromHeader(ALIAS_TYPE));

    String INSERT_FACT = "INSERT INTO " + TABLE_FACT + "(" + COLUMN_HEADER + "," + COLUMN_PAYLOAD
            + ") VALUES (cast(? as jsonb),cast (? as jsonb))";

    String SELECT_BY_ID = "SELECT " + PROJECTION_FACT + " FROM " + TABLE_FACT + " WHERE "
            + COLUMN_HEADER + " @> cast (? as jsonb)";

    String SELECT_LATEST_SER = "SELECT max(" + COLUMN_SER + ") FROM " + TABLE_FACT;

    String SELECT_ID_FROM_CATCHUP = "SELECT " + PROJECTION_ID + " FROM " + TABLE_FACT + " WHERE ("
            + COLUMN_SER + " IN ( SELECT " + COLUMN_SER + " FROM " + TABLE_CATCHUP + " WHERE "
            + COLUMN_CID + "=? AND " + COLUMN_SER + ">? LIMIT ? ) ORDER BY " + COLUMN_SER + " ASC";

    String SELECT_FACT_FROM_CATCHUP = //
            "SELECT " + PROJECTION_FACT + //
                    " FROM " + TABLE_FACT + //
                    " WHERE " + COLUMN_SER + " IN ( " + //
                    "   SELECT " + COLUMN_SER + " FROM " + TABLE_CATCHUP + //
                    "   WHERE ( " + COLUMN_CID + "=? AND " + COLUMN_SER + ">? ) LIMIT ? " + //
                    ") ORDER BY " + COLUMN_SER + " ASC";

    String DELETE_CATCH_BY_CID = "DELETE FROM " + TABLE_CATCHUP + //
            " WHERE cid=?";

    private String fromHeader(String attributeName) {
        return PGConstants.COLUMN_HEADER + "->>'" + attributeName + "' AS " + attributeName;
    }

    private String empty(String attributeName) {
        return "'{}' AS " + attributeName;
    }

}
