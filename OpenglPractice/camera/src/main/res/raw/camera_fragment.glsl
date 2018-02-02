#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 textureCoordinate;

uniform samplerExternalOES inputImageTexture;


void main() {
    vec4 color = texture2D(inputImageTexture, textureCoordinate);

    gl_FragColor = color + vec4(0.3, 0.3, 0.3, 0);
}