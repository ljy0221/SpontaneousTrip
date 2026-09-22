package com.ssafy.passproject.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class GeoUtil {
	private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326); // WGS84 SRID 4326

    public static Point createJtsPoint(double lon, double lat) {
        // Point 객체는 (경도, 위도) 순서로 생성됩니다.
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }
    
    
}
