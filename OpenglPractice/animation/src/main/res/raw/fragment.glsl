precision mediump float;

varying float vElapseTime;
varying vec3 vColor;

uniform sampler2D uTextureUnit;

void main() {
    gl_FragColor = vec4(vColor / vElapseTime, 1) * texture2D(uTextureUnit, gl_PointCoord);
}