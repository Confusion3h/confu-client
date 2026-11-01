#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 InSize;

uniform float BlurFactor;

void main() {
    fragColor = mix(texture(DiffuseSampler, texCoord), texture(PrevSampler, texCoord), BlurFactor);
    fragColor.w = 1.0;
}
