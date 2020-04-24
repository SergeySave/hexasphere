#version 330 core
in VS_OUT {
    vec3 pos;
    vec2 uv;
    vec4 color;
    vec3 tanLightDir;
    vec3 tanViewPos;
    vec3 tanFragPos;
} fs_in;

out vec4 FragColor;

uniform sampler2D texture_diffuse1;
uniform sampler2D texture_specular1;
uniform sampler2D texture_normal1;
uniform float ambientStrength;

void main()
{
    // obtain normal from normal map in range [0,1]
    vec3 normal = texture(texture_normal1, fs_in.uv).rgb;
    // transform normal vector to range [-1,1]
    normal = normalize(normal * 2.0 - 1.0);  // this normal is in tangent space

    // get diffuse color
    vec3 color = texture(texture_diffuse1, fs_in.uv).rgb;
    // ambient
    vec3 ambient = ambientStrength * color;
    // diffuse
    vec3 lightDir = fs_in.tanLightDir;
    float diff = max(dot(lightDir, normal), 0.0);
    vec3 diffuse = diff * color;
    // specular
    vec3 viewDir = normalize(fs_in.tanViewPos - fs_in.tanFragPos);
    vec3 reflectDir = reflect(-lightDir, normal);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);

    vec3 specular = vec3(0.0) * spec * texture(texture_specular1, fs_in.uv).r;
    FragColor = vec4(ambient + diffuse + specular, 1.0) * fs_in.color;
}