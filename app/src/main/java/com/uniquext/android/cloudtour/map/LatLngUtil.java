package com.uniquext.android.cloudtour.map;

import com.amap.api.maps.model.LatLng;

/**
 * 　 　　   へ　　　 　／|
 * 　　    /＼7　　　 ∠＿/
 * 　     /　│　　 ／　／
 * 　    │　Z ＿,＜　／　　   /`ヽ
 * 　    │　　　 　　ヽ　    /　　〉
 * 　     Y　　　　　   `　  /　　/
 * 　    ｲ●　､　●　　⊂⊃〈　　/
 * 　    ()　 へ　　　　|　＼〈
 * 　　    >ｰ ､_　 ィ　 │ ／／      去吧！
 * 　     / へ　　 /　ﾉ＜| ＼＼        比卡丘~
 * 　     ヽ_ﾉ　　(_／　 │／／           消灭代码BUG
 * 　　    7　　　　　　　|／
 * 　　    ＞―r￣￣`ｰ―＿
 * ━━━━━━感觉萌萌哒━━━━━━
 *
 * @author UniqueXT
 * @version 1.0
 * @date 2019/4/19  13:27
 * 大地坐标系资料WGS-84
 */
public class LatLngUtil {
    /**
     * 长半径
     */
    private static final double RADIUS_LONG = 6378137.0;
    /**
     * 短半径
     */
    private static final double RADIUS_SHORT = 6356752.3142;
    /**
     * 扁率
     */
    private static final double FATE_RATE = 1 / 298.257223565;

    /**
     * 计算另一点经纬度
     *
     * @param latitude  维度
     * @param longitude 经度
     * @param Az        Azimuth angle,方位角
     * @param distance  距离（米）
     */
    public static LatLng computerLatLng(double latitude, double longitude, double Az, double distance) {
        double alpha1 = Math.toRadians(Az);
        double sinAlpha1 = Math.sin(alpha1);
        double cosAlpha1 = Math.cos(alpha1);

        double tanU1 = (1 - FATE_RATE) * Math.tan(Math.toRadians(latitude));
        double cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1));
        double sinU1 = tanU1 * cosU1;
        double sigma1 = Math.atan2(tanU1, cosAlpha1);
        double sinAlpha = cosU1 * sinAlpha1;
        double cosSqAlpha = 1 - sinAlpha * sinAlpha;
        double uSq = cosSqAlpha * (RADIUS_LONG * RADIUS_LONG - RADIUS_SHORT * RADIUS_SHORT) / (RADIUS_SHORT * RADIUS_SHORT);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double cos2SigmaM = 0;
        double sinSigma = 0;
        double cosSigma = 0;
        double sigma = distance / (RADIUS_SHORT * A), sigmaP = 2 * Math.PI;
        while (Math.abs(sigma - sigmaP) > 1e-12) {
            cos2SigmaM = Math.cos(2 * sigma1 + sigma);
            sinSigma = Math.sin(sigma);
            cosSigma = Math.cos(sigma);
            double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)
                    - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
            sigmaP = sigma;
            sigma = distance / (RADIUS_SHORT * A) + deltaSigma;
        }
        double tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1;
        double lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
                (1 - FATE_RATE) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp));
        double lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
        double C = FATE_RATE / 16 * cosSqAlpha * (4 + FATE_RATE * (4 - 3 * cosSqAlpha));
        double L = lambda - (1 - C) * FATE_RATE * sinAlpha
                * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        double revAz = Math.atan2(sinAlpha, -tmp); // final bearing

        return new LatLng(Math.toDegrees(lat2), longitude + Math.toDegrees(L));
    }

}
