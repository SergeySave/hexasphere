#version 330 core
layout (location = 0) in vec3 aPos;   // the position variable has attribute position 0
layout (location = 1) in vec3 aNorm; // the normal variable has attribute position 1
layout (location = 2) in vec2 aUV; // the uv variable has attribute position 2
layout (location = 3) in vec3 aTangent; // the uv variable has attribute position 3
layout (location = 4) in vec3 aBiTangent; // the uv variable has attribute position 4
layout (location = 5) in vec4 aColor; // the color variable has attribute position 5
layout (location = 6) in mat4 aModel; // the model matrix variable has attribute position 6,7,8,9

uniform mat4 uCamera;
uniform mat4 uModel;
uniform vec3 lightDir;
uniform vec3 viewPos;

out VS_OUT {
    vec3 pos;
    vec2 uv;
    vec4 color;
    vec3 tanLightDir;
    vec3 tanViewPos;
    vec3 tanFragPos;
} vs_out;

void main()
{
    mat4 modelMatrix = uModel * aModel;
    vs_out.pos = vec3(modelMatrix * vec4(aPos, 1.0));
    vs_out.uv = aUV;
    vs_out.color = aColor;

    mat3 normalMatrix = transpose(inverse(mat3(modelMatrix)));
    vec3 T = normalize(normalMatrix * aTangent);
    vec3 N = normalize(normalMatrix * aNorm);
    T = normalize(T - dot(T, N) * N);
    vec3 B = cross(N, T);
    mat3 TBN = transpose(mat3(T, B, N));

    vs_out.tanLightDir = TBN * normalize(lightDir);
    vs_out.tanViewPos = TBN * viewPos;
    vs_out.tanFragPos = TBN * vs_out.pos;

    gl_Position = uCamera * vec4(vs_out.pos, 1.0);
}