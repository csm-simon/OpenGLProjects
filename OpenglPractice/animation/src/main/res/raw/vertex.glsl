uniform mat4 uMatrix;
uniform float uTime;

attribute vec3 aPosition;
attribute vec3 aColor;
attribute vec3 aDirection;
attribute float aCreateTime;

varying float vElapseTime;
varying vec3 vColor;

void main() {
    vColor = aColor;
    vElapseTime = uTime - aCreateTime;

    vec3 position = aPosition + (aDirection * vElapseTime);

    gl_Position = uMatrix * vec4(position, 1.0);

    gl_PointSize = 25.0;
}