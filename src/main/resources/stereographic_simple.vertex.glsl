#version 330 core
layout (location = 0) in vec3 aPos;   // the position variable has attribute position 0
layout (location = 1) in vec3 aColor; // the color variable has attribute position 1

uniform mat3 uCamera;
uniform mat4 uModel;

out vec3 ourColor; // output a color to the fragment shader

void main()
{
	// Rotate the sphere
	vec4 sphereCoord = uModel * vec4(aPos, 1.0);
	// Back to non-homeographic coordinates
	sphereCoord.xyzw /= sphereCoord.w;
	// Normalize (now xyz is a location on the unit sphere)
	sphereCoord.xyz /= length(sphereCoord.xyz);
	// Compute the stereographic projection and apply the camera (scaling) matrix
	vec3 projection = vec3(sphereCoord.x / (1 - sphereCoord.z), sphereCoord.y / (1 - sphereCoord.z), 1.0);
//	// Output this position
//    gl_Position = vec4(projection.xy / projection.z, sphereCoord.z, 1.0);
//    ourColor = aColor; // set ourColor to the input color we got from the vertex data

	vec4 result = vec4(uCamera * projection, 1.0);
	result.z = sphereCoord.z;
    gl_Position = result;
    ourColor = aColor; // set ourColor to the input color we got from the vertex data
}