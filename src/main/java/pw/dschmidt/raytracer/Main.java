package pw.dschmidt.raytracer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;


public class Main {


    public static void main(String[] args) {

        new Renderer(640, 480, 50, "test%03d.png")
                .render(List.of(
                        // the "ground"
                        new Sphere(new Vector3D(0, -20004, -20),
                                   20000,
                                   new Vector3D(0.2, 0.2, 0.2),
                                   Vector3D.ZERO,
                                   0,
                                   0),
                        // visible spheres
                        new Sphere(new Vector3D(0, 1, -20),
                                   4,
                                   new Vector3D(1, 0.32, 0.36), // red
                                   Vector3D.ZERO,
                                   1,
                                   0.5f),
                        new Sphere(new Vector3D(5.0, -1, -15),
                                   2,
                                   new Vector3D(0.90, 0.76, 0.46), // yellow
                                   Vector3D.ZERO,
                                   1,
                                   0),
                        new Sphere(new Vector3D(5.0, 0, -25),
                                   3,
                                   new Vector3D(0.65, 0.77, 0.97), // blue
                                   Vector3D.ZERO,
                                   1,
                                   0),
                        new Sphere(new Vector3D(-5.5, 0, -15),
                                   3,
                                   new Vector3D(1, 1, 1), // grey
                                   Vector3D.ZERO,
                                   1f,
                                   0.f),
                        new Sphere(new Vector3D(-4, 1, -25),
                                   3,
                                   new Vector3D(0.40, 0.90, 0.40), // green
                                   Vector3D.ZERO,
                                   1,
                                   0),
                        new Sphere(new Vector3D(0, 0, 20), // behind camera
                                   4,
                                   new Vector3D(0.90, 0.76, 0.46), // yellow
                                   Vector3D.ZERO,
                                   1, 0),
                        // light
                        new Sphere(new Vector3D(10.0, 20, -30),
                                   3,
                                   Vector3D.ZERO,
                                   new Vector3D(3, 3, 3),
                                   0,
                                   0),
                        new Sphere(new Vector3D(-10.0, 20, -30),
                                   5,
                                   Vector3D.ZERO,
                                   new Vector3D(3, 3, 2),
                                   0,
                                   0)
                ));
    }
}
