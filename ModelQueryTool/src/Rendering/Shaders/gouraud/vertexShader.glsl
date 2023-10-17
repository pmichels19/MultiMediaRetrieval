#version 330 core

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform int wireframe;

out vec4 color;

void main() {
    gl_Position = projectionMatrix * viewMatrix * vec4(inPosition, 1.0);

    // Lighting calculations for flat shading
    vec3 lightDirection = normalize(vec3( inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0) ));
    vec3 norm = normalize(inNormal);
    float dotNL = dot(norm, lightDirection);
    if (dotNL < 0.0) dotNL = -dotNL;
    // Output the final color (apply shading)
    vec3 baseColor = vec3(0.5, 0.5, 0.5);
    if (wireframe == 1) baseColor = vec3(0, 0, 0);
    color = vec4(dotNL * baseColor, 1.0);
}
