#version 400

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texcoord;

uniform vec3 pos_a;
uniform vec3 pos_b;
uniform vec2 scl_a;
uniform vec2 scl_b;
uniform float rot_a;
uniform float rot_b;
uniform float delta_t;
uniform mat4 vp;

out vec2 texcoord_final;

void main()
{
	vec3 pos = mix (pos_a, pos_b, delta_t);
	vec2 scl = mix (scl_a, scl_b, delta_t);
	float rot = mix (rot_a, rot_b, delta_t);
	mat4 m = mat4(scl.x * cos(rot),  scl.x * sin(rot), 0,     0,
				  -scl.y * sin(rot), scl.y * cos(rot), 0,     0,
				  0,                0,                 1,     0,
				  pos.x,            pos.y,             pos.z, 1);
	vec4 pos_f = m * vec4(position, 1.0);
	pos_f *= vp;
	texcoord_final = texcoord;
    gl_Position = vec4 (pos_f.x, pos_f.y, pos_f.z, pos_f.w);
}