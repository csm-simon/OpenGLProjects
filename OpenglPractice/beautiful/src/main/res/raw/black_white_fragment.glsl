precision mediump float;

varying vec2 textureCoordinate;

uniform sampler2D s_texture;

void main() {
    vec4 color = texture2D( s_texture, textureCoordinate );
    float value = (0.3 * color.r + 0.59 * color.g + 0.11 * color.g);
    gl_FragColor = vec4(value, value, value, 1);
}