precision highp float;

varying vec2 textureCoordinate;
uniform sampler2D inputImageTexture;
// const highp vec3 GRAY = vec3(0.2125, 0.7154, 0.0721);
const highp vec3 GRAY = vec3(0.299, 0.587, 0.114);

void main() {
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    float grayColor = dot(textureColor.rgb, GRAY);
    gl_FragColor = vec4(vec3(grayColor), textureColor.a);
}