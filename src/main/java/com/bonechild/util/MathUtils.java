package com.bonechild.util;

/**
 * Utility class for math operations
 */
public class MathUtils {
    
    /**
     * Clamp a value between min and max
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Linear interpolation
     */
    public static float lerp(float start, float end, float alpha) {
        return start + alpha * (end - start);
    }
    
    /**
     * Calculate distance between two points
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Convert degrees to radians
     */
    public static float toRadians(float degrees) {
        return degrees * (float) Math.PI / 180f;
    }
    
    /**
     * Convert radians to degrees
     */
    public static float toDegrees(float radians) {
        return radians * 180f / (float) Math.PI;
    }
    
    /**
     * Check if value is within range
     */
    public static boolean inRange(float value, float min, float max) {
        return value >= min && value <= max;
    }
}
