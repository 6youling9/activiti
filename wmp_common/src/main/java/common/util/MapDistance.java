package common.util;

import java.util.HashMap;

public class MapDistance {  
	  
    private static double EARTH_RADIUS = 6378.137;  
  
    private static double rad(double d) {  
        return d * Math.PI / 180.0;  
    }
    
    /**
     * 根据两个位置的经纬度，来计算两地的距离（单位为KM）
     * 参数为String类型
     * @param lat1 用户经度
     * @param lng1 用户纬度
     * @param lat2 商家经度
     * @param lng2 商家纬度
     * @return
     */
    public static String getDistance(String lat1Str, String lng1Str, String lat2Str, String lng2Str) {
    	Double lat1 = Double.parseDouble(lat1Str);
    	Double lng1 = Double.parseDouble(lng1Str);
    	Double lat2 = Double.parseDouble(lat2Str);
    	Double lng2 = Double.parseDouble(lng2Str);
    	double patm = 2;
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double difference = radLat1 - radLat2;
        double mdifference = rad(lng1) - rad(lng2);
        double distance = patm * Math.asin(Math.sqrt(Math.pow(Math.sin(difference / patm), patm)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(mdifference / patm), patm)));
        distance = distance * EARTH_RADIUS;
        String distanceStr = String.valueOf(distance);
        return distanceStr;
    }
    
    /**
	 * lon 经度  lat 纬度
	 * @param raidus 单位米
	 * return minLng, maxLng, minLat, maxLat 
	 *        最小经度       最大经度    最小纬度      最大纬度
	*/
	 public static HashMap<String,Object> getAround(double lon,double lat, int raidus) {

		 Double latitude = lat;
		 Double longitude = lon;
	
		 Double degree = (24901 * 1609) / 360.0;
		 double raidusMile = raidus;
	
		 Double dpmLat = 1 / degree;
		 Double radiusLat = dpmLat * raidusMile;
		 Double minLat = latitude - radiusLat;
		 Double maxLat = latitude + radiusLat;
	
		 Double mpdLng = degree * Math.cos(latitude * (Math.PI / 180));
		 Double dpmLng = 1 / mpdLng;
		 Double radiusLng = dpmLng * raidusMile;
		 Double minLng = longitude - radiusLng;
		 Double maxLng = longitude + radiusLng;
		 
		 HashMap<String, Object> result= new HashMap<String, Object>();
			 result.put("minLng", minLng);
			 result.put("maxLng", maxLng);
			 result.put("minLat", minLat);
			 result.put("maxLat", maxLat);
		 return result;
	}
    
    public static void main(String[] args) {
    	//济南国际会展中心经纬度：117.11811  36.68484
    	//趵突泉：117.00999000000002  36.66123
    	//System.out.println(getDistance("116.97265","36.694514","116.597805","36.738024"));
    	
    	System.out.println(getAround(117.11811, 36.68484, 13000));
    	//117.01028712333508(Double), 117.22593287666493(Double), 
    	//36.44829619896034(Double), 36.92138380103966(Double)
    	
	}
    
}
