#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 InSize;

uniform float hue;
uniform float Brightness;
uniform float Contrast;
uniform float Saturation;

// See https://forum.unity.com/threads/hue-saturation-brightness-contrast-shader.260649/ for inspiration.

void applyHue(inout vec4 color, float hue) {
    float angle = radians(hue);
    vec3 k = vec3(0.57735, 0.57735, 0.57735);
    float cosAngle = cos(angle);
    //Rodrigues' rotation formula
    color.rgb = color.rgb * cosAngle + cross(k, color.rgb) * sin(angle) + k * dot(k, color.rgb) * (1.0 - cosAngle);
}

void applySaturation(inout vec4 color, float saturation) {
    float dot = sqrt(color.r * color.r * 0.299 + color.g * color.g * 0.587 + color.b * color.b * 0.114);
    color.r = dot + (color.r - dot) * saturation;
    color.g = dot + (color.g - dot) * saturation;
    color.b = dot + (color.b - dot) * saturation;
}

vec4 applyHSBCEffect(vec4 startColor, vec4 hsbc) {
    float _Hue = 360 * hsbc.r;
    float _Brightness = hsbc.b * 2 - 1;
    float _Contrast = hsbc.a * 2;
    float _Saturation = hsbc.g * 2;

    vec4 outputColor = startColor;
    applyHue(outputColor, _Hue);
    // Contrast
    outputColor.rgb = (outputColor.rgb - 0.5) * _Contrast + 0.5;
    // Brightness
    outputColor.rgb = outputColor.rgb + vec3(_Brightness, _Brightness, _Brightness);
    // Saturation
    applySaturation(outputColor, _Saturation);

    outputColor = vec4(outputColor.rgb, 1.0);

    return outputColor;
}

void main() {
    fragColor = applyHSBCEffect(texture(DiffuseSampler, texCoord), vec4(hue, Saturation, Brightness, Contrast));
}
