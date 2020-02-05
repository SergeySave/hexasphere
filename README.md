# Hexasphere

This repository currently contains the code needed to generate and render the Dual of a Subdivided Icosahedron, a Goldberg Polyhedron, referred to as a hexasphere from now on.
These hexaspheres look like they are spheres tiled with hexagons. This is not entirely true as a hexasphere of any size will always include 12 pentagons.

As of now the functionality contained in this repository can do the following things:
- Generate the hexasphere by taking the dual of a subdivided icosahedron
- Generate height, temperature, moisture, and biome maps for the tiles on the hexasphere
- Render the hexasphere, both using a perspective projection and a simplified stereographic projection (explained below)

## Running the code
Run the main method in the class com.sergeysav.hexasphere.MainKt.
If you are on a macOS device you will need to include the VM option -XstartOnFirstThread
The buttons WASDQE rotate the sphere and R changes the current projection mode.

## Simplified Stereographic Projection
The simplified stereographic projection performs a stereographic projection for each vertex.
Each edge is then linearly interpolated between adjacent vertices. This is not mathematically accurate as the stereographic projection
is not a linear projection.  This ends up nearly as efficient as the normal perspective projection rendering as the
projection occurs on the GPU.
