package com.podevs.android.poAndroid.teambuilder;

public class PickerUtils {
    public static boolean isValidColor(int color) {
        int green = green(color);
        if (green > 200) {
            return false;
        }

        float luma = luma(color);
        if (luma > 140) {
            return false;
        }

        float lightness = HSLColor.lightness(color) * 100;
        if (lightness > 140) {
            return false;
        }
        return true;
    }

    /*
    public static float brightness(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        int V = Math.max(b, Math.max(r, g));

        return (V / 255.f);
    }
    */

    public static int luma(int color) {
        // This is a quick approximation
        // real Y = 0.2126 R + 0.7152 G + 0.0722 B
        int r = red(color);
        int g = green(color);
        int b = blue(color);
        return (g*3 + b + r*2)/6;
    }

    static class HSLColor {
        public static float lightness(int rgb) {
            return HSL(rgb)[2];
        }

        public static float hue(int rgb) {
            return HSL(rgb)[0];
        }

        public static float saturation(int rgb) {
            return HSL(rgb)[1];
        }

        public static float[] HSL(int rgb) {
            final float rf = red(rgb) / 255f;
            final float gf  = green(rgb) / 255f;
            final float bf = blue(rgb) / 255f;

            final float max = Math.max(rf, Math.max(gf, bf));
            final float min = Math.min(rf, Math.min(gf, bf));
            final float deltaMaxMin = max - min;

            float h, s;
            float l = (max + min) / 2f;

            if (max == min) {
                h = s = 0f;
            } else {
                if (max == rf) {
                    h = ((gf - bf) / deltaMaxMin) % 6f;
                } else if (max == gf) {
                    h = ((bf - rf) / deltaMaxMin) + 2f;
                } else {
                    h = ((rf - gf) / deltaMaxMin) + 4f;
                }

                s = deltaMaxMin / (1f - Math.abs(2f * l - 1f));
            }

            float[] hsl = new float[3];
            hsl[0] = (h * 60f) % 360f;
            hsl[1] = s;
            hsl[2] = l;
            return hsl;
        }
    }

    public static int[] RGBfromInt(int color) {
        return new int[]{red(color), green(color), blue(color)};
    }

    /*
    private static class XYZ {
        private static int X = 0;
        private static int Y = 1;
        private static int Z = 2;

        public static float[] xyz(int rgb) {
            float rf = red(rgb) / 255f;
            float gf  = green(rgb) / 255f;
            float bf = blue(rgb) / 255f;

            if (rf <= 0.04045) rf /= 12f;
            else rf = (float) Math.pow((rf + 0.055f), 2.4f);

            if (gf <= 0.04045) gf /= 12f;
            else gf = (float) Math.pow((gf + 0.055f), 2.4f);

            if (bf <= 0.04045) bf /= 12f;
            else bf = (float) Math.pow((bf + 0.055f), 2.4f);

            rf *= 100f;
            gf *= 100f;
            bf *= 100f;

            float[] xyz = new float[3];
            xyz[X] = rf * 0.4124f + gf * 0.3576f + bf * 0.1805f;
            xyz[Y] = rf * 0.2126f + gf * 0.7152f + bf * 0.0722f;
            xyz[Z] = rf * 0.0193f + gf * 0.1192f + bf * 0.9505f;

            return xyz;
        }
    }

    private static class CIELab {
        private static int L = 0;
        private static int a = 1;
        private static int b = 2;

        public static float[] Lab(float[] XYZ) {
            float x = XYZ[0] / 95.047f;
            float y = XYZ[1] / 100.000f;
            float z = XYZ[2] / 108.883f;

            if (x > 0.008856)   x = (float) Math.pow(x,1/3f);
            else                x = (7.787f * x) + (16 / 116);

            if (y > 0.008856)   y = (float) Math.pow(y,1/3f);
            else                y = (7.787f * y) + (16 / 116);

            if (z > 0.008856)   z = (float) Math.pow(z,1/3f);
            else                z = (7.787f * z) + (16 / 116);

            float[] Lab = new float[3];
            Lab[L] = (116 * y) - 16;
            Lab[a] = 500 * (x - y);
            Lab[b] = 200 * (y - z);


            return Lab;
        }
    }
    */

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Return the green component of a color int. This is the same as saying
     * (color >> 8) & 0xFF
     */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Return the blue component of a color int. This is the same as saying
     * color & 0xFF
     */
    public static int blue(int color) {
        return color & 0xFF;
    }
}