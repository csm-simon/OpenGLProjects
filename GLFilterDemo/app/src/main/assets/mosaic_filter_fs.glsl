precision highp float;

varying vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform float imageWidthFactor;
uniform float imageHeightFactor;
uniform float pixel;

void main() {
    vec2 uv = textureCoordinate.xy;
    float dx = pixel * imageWidthFactor;
    float dy = pixel * imageHeightFactor;
    vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));
    vec3 textureColor = texture2D(inputImageTexture, coord).xyz;
    gl_FragColor = vec4(textureColor, 1.0);
}