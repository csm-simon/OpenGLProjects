
attribute vec4 position;
attribute vec4 inputTextureCoordinate;
uniform mat4 mvpMatrix;
varying vec2 textureCoordinate;

void main() {
    gl_Position = mvpMatrix * position;
    textureCoordinate = inputTextureCoordinate.xy;
}