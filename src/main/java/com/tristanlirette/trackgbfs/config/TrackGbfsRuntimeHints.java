package com.tristanlirette.trackgbfs.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

public class TrackGbfsRuntimeHints implements RuntimeHintsRegistrar {

    // jOOQ's SQLDataType static initializer reflectively materialises array types for every
    // built-in mapping; without these hints jOOQ throws an NPE during native-image startup.
    // See https://github.com/spring-projects/spring-boot/issues/33552
    private static final List<String> JOOQ_REFLECTION_TYPES = List.of(
            "java.time.LocalDate[]",
            "java.time.LocalDateTime[]",
            "java.time.LocalTime[]",
            "java.time.ZonedDateTime[]",
            "java.time.OffsetDateTime[]",
            "java.time.OffsetTime[]",
            "java.time.Instant[]",
            "java.time.Year[]",
            "java.sql.Timestamp[]",
            "java.sql.Date[]",
            "java.sql.Time[]",
            "java.math.BigInteger[]",
            "java.math.BigDecimal[]",
            "org.jooq.types.UNumber[]",
            "org.jooq.types.UByte[]",
            "org.jooq.types.UInteger[]",
            "org.jooq.types.ULong[]",
            "org.jooq.types.Unsigned[]",
            "org.jooq.types.UShort[]",
            "java.lang.Boolean[]",
            "java.lang.Byte[]",
            "java.lang.Short[]",
            "java.lang.Integer[]",
            "java.lang.Long[]",
            "java.lang.Float[]",
            "java.lang.Double[]",
            "java.lang.String[]",
            "java.lang.Object[]",
            "org.jooq.types.YearToMonth[]",
            "org.jooq.types.YearToSecond[]",
            "org.jooq.types.DayToSecond[]",
            "org.jooq.RowId[]",
            "org.jooq.Result[]",
            "org.jooq.Record[]",
            "org.jooq.JSON[]",
            "org.jooq.JSONB[]",
            "org.jooq.XML[]",
            "org.jooq.Geography[]",
            "org.jooq.Geometry[]",
            "java.util.UUID[]",
            "byte[]",
            "org.jooq.impl.SQLDataType",
            "org.jooq.util.cubrid.CUBRIDDataType",
            "org.jooq.util.derby.DerbyDataType",
            "org.jooq.util.firebird.FirebirdDataType",
            "org.jooq.util.h2.H2DataType",
            "org.jooq.util.hsqldb.HSQLDBDataType",
            "org.jooq.util.ignite.IgniteDataType",
            "org.jooq.util.mariadb.MariaDBDataType",
            "org.jooq.util.mysql.MySQLDataType",
            "org.jooq.util.postgres.PostgresDataType",
            "org.jooq.util.sqlite.SQLiteDataType",
            "org.jooq.util.oracle.OracleDataType",
            "org.jooq.util.sqlserver.SQLServerDataType",
            "org.jooq.impl.DefaultBinding$Mdsys",
            "org.jooq.impl.DefaultBinding$SdoElemInfoArray",
            "org.jooq.impl.DefaultBinding$SdoOrdinateArray",
            "org.jooq.impl.DefaultBinding$SdoGeometry",
            "org.jooq.impl.DefaultBinding$SdoGeometryRecord",
            "org.jooq.impl.DefaultBinding$SdoPointType",
            "org.jooq.impl.DefaultBinding$SdoPointTypeRecord");

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
                .registerType(TrackGbfsProperties.class,
                        MemberCategory.ACCESS_DECLARED_FIELDS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TrackGbfsProperties.Feed.class,
                        MemberCategory.ACCESS_DECLARED_FIELDS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TrackGbfsProperties.Poll.class,
                        MemberCategory.ACCESS_DECLARED_FIELDS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TrackGbfsProperties.Api.class,
                        MemberCategory.ACCESS_DECLARED_FIELDS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS);

        for (String type : JOOQ_REFLECTION_TYPES) {
            hints.reflection().registerType(TypeReference.of(type),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        }

        // Thymeleaf's StandardJavaScriptSerializer.Jackson3StandardJavaScriptSerializer uses a long
        // reflective chain to set up Jackson 3 for JavaScript serialization. It calls getMethod() which
        // resolves against the declaring class, so each class in the hierarchy must be registered
        // separately. tryBuildViaJsonMapperBuilder uses JsonMapper + MapperBuilder; the constructor
        // calls safeInvokeVoid (catches Exception only, not Error) on the built ObjectMapper.
        hints.reflection()
                .registerType(TypeReference.of("tools.jackson.databind.json.JsonMapper"),
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TypeReference.of("tools.jackson.databind.json.JsonMapper$Builder"),
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TypeReference.of("tools.jackson.databind.cfg.MapperBuilder"),
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TypeReference.of("tools.jackson.databind.ObjectMapper"),
                        MemberCategory.INVOKE_DECLARED_METHODS);

        // Caffeine resolves cache implementation classes by name at runtime via Class.forName.
        // Each name encodes enabled features: S=Strong key, S=Strong value, M=Maximum, W=Write expiry.
        // Classes needed for our CacheConfig: expireAfterWrite only (SSW), maximumSize+expireAfterWrite
        // (SSMW), and maximumSize only (SSM) used for the zero-TTL test path.
        for (String caffeineClass : List.of(
                "com.github.benmanes.caffeine.cache.SSW",
                "com.github.benmanes.caffeine.cache.SSMW",
                "com.github.benmanes.caffeine.cache.SSM")) {
            hints.reflection().registerType(TypeReference.of(caffeineClass),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.ACCESS_DECLARED_FIELDS);
        }
    }
}
