#version 330 core
layout (location = 0) in vec3 aPos;   // the position variable has attribute position 0
layout (location = 1) in vec3 aNorm; // the normal variable has attribute position 1
layout (location = 2) in vec2 aUV; // the uv variable has attribute position 2
layout (location = 3) in vec4 aColor; // the color variable has attribute position 3
layout (location = 4) in mat4 aModel; // the model matrix variable has attribute position 4

uniform mat4 uCamera;

out vec3 outPos; // output a position to the fragment shader
out vec3 outNorm; // output a norm to the fragment shader
out vec2 outUV; // output a uv to the fragment shader
out vec4 outColor; // output a color to the fragment shader

void main()
{
    outPos = vec3(aModel * vec4(aPos, 1.0));
    outNorm = mat3(transpose(inverse(aModel))) * aNorm;
    outUV = aUV;
    outColor = aColor;

    gl_Position = uCamera * vec4(outPos, 1.0);
}