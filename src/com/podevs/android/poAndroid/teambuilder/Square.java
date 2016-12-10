package com.podevs.android.poAndroid.teambuilder;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] =
                    {-1.0f, 1.0f,0.0f,
                     -1.0f,-1.0f,0.0f,
                      1.0f,-1.0f,0.0f,
                      1.0f, 1.0f,0.0f};



    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices


    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    public final String fragment =
                    "precision mediump float;" +
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
                    "    vec3 c = vec3(gl_FragCoord.x / u_size.x, gl_FragCoord.y / u_size.y, u_value);" +
                    "    vec3 color = hsv2rgb(c);" +
                    "    if (colorCheck(color)) color = vec3(0, 0, 0);" +
                    "    gl_FragColor = vec4(color, 1);" +
                    "}";

    int mProgram;

    static final int vertexStride = COORDS_PER_VERTEX * 4;
    static final int vertexCount = squareCoords.length/COORDS_PER_VERTEX;

    public Square() {
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void draw(final float value) {
        GLES20.glUseProgram(mProgram);

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        int mSizeHandle = GLES20.glGetUniformLocation(mProgram, "u_size");

        GLES20.glUniform2f(mSizeHandle, ColorPickerActivity.width, ColorPickerActivity.height);

        int mValueHandle = GLES20.glGetUniformLocation(mProgram, "u_value");

        GLES20.glUniform1f(mValueHandle, value);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}