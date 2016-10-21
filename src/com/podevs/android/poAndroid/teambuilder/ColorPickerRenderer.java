package com.podevs.android.poAndroid.teambuilder;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ColorPickerRenderer implements GLSurfaceView.Renderer {

    private static String vertex =
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";


    public static String fragment =
                    "uniform sampler2D u_sampler2D;" +
                    "uniform vec2 u_size;" +
                    "uniform float u_value;" +
                    "" +
                    "vec3 hsv2rgb(vec3 c) {" +
                    "    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);" +
                    "    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);" +
                    "    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);" +
                    "}" +
                    "" +
                    "bool colorCheck(vec3 c) {" +
                    "    if (c.g > 0.78431) return true;" +
                    "    float luma = (c.g + c.g + c.g + c.b + c.r + c.r)/6.;" +
                    "    if (luma > 0.549019) return true;" +
                    "    return false;" +
                    "}" +
                    "" +
                    "void main() {" +
                    "    gl_FragColor = vec4(1, 0, 1, 1);" +
                    "}";

    float value;
    Square mSquare;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0,0,0,0);
        mSquare = new Square();
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0,0,1f,0);
        mSquare.draw(value);
    }
}
