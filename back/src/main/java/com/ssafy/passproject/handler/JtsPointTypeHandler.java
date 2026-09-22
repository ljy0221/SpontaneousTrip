package com.ssafy.passproject.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.geom.GeometryFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JtsPointTypeHandler extends BaseTypeHandler<Point> {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final WKBReader wkbReader = new WKBReader(geometryFactory);
    private final WKBWriter wkbWriter = new WKBWriter();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Point parameter, JdbcType jdbcType)
            throws SQLException {
        // MySQL expects WKB format for POINT data
        byte[] wkb = wkbWriter.write(parameter);
        ps.setBytes(i, wkb);
    }

    @Override
    public Point getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return readPoint(rs.getBytes(columnName));
    }

    @Override
    public Point getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return readPoint(rs.getBytes(columnIndex));
    }

    @Override
    public Point getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return readPoint(cs.getBytes(columnIndex));
    }

    private Point readPoint(byte[] wkb) throws SQLException {
        if (wkb == null || wkb.length == 0) {
            return null;
        }
        try {
            return (Point) wkbReader.read(wkb);
        } catch (Exception e) {
            throw new SQLException("Error reading JTS Point from DB (WKB format)", e);
        }
    }
}