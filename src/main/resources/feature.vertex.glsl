#version 330 core
layout (location = 0) in vec3 aPos;   // the position variable has attribute position 0
layout (location = 1) in vec2 aUV; // the uv variable has attribute position 1
layout (location = 2) in mat4 aInstance; // the instance matrix variable has attribute position 2

uniform mat4 uCamera;
uniform mat4 uModel;

out vec2 outUV; // output a uv to the fragment shader

void main()
{
    gl_Position = uCamera * uModel * aInstance * vec4(aPos, 1.0);
    outUV = aUV;
}