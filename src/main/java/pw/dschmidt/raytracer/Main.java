package pw.dschmidt.raytracer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.io.File;
import java.util.List;


public class Main {


    public static void main(String[] args) {


//        spheres.push_back(Sphere(Vec3f( 0.0, -10004, -20), 10000, Vec3f(0.20, 0.20, 0.20), 0, 0.0));
//        spheres.push_back(Sphere(Vec3f( 0.0,      0, -20),     4, Vec3f(1.00, 0.32, 0.36), 1, 0.5));
//        spheres.push_back(Sphere(Vec3f( 5.0,     -1, -15),     2, Vec3f(0.90, 0.76, 0.46), 1, 0.0));
//        spheres.push_back(Sphere(Vec3f( 5.0,      0, -25),     3, Vec3f(0.65, 0.77, 0.97), 1, 0.0));
//        spheres.push_back(Sphere(Vec3f(-5.5,      0, -15),     3, Vec3f(0.90, 0.90, 0.90), 1, 0.0));
//        // light
//        spheres.push_back(Sphere(Vec3f( 0.0,     20, -30),     3, Vec3f(0.00, 0.00, 0.00), 0, 0.0, Vec3f(3)));

//        new Renderer(640, 480, 30, new File("test.png"))
//                .render(List.of(
//                        new Sphere(new Vector3D(0, 0, -30),
//                                   3,
//                                   new Vector3D(1, 0.32, 0.36),
//                                   Vector3D.ZERO,
//                                   0,
//                                   0.0f),
//                        // light
//                        new Sphere(new Vector3D(10.0, 20, -30),
//                                   10000,
//                                   Vector3D.ZERO,
//                                   new Vector3D(3, 3, 3),
//                                   0,
//                                   0)
//                ));
//
//        System.exit(0);
        new Renderer(1920, 1080, 50, "test%03d.png")
                .render(List.of(
                        // the "ground"
                        new Sphere(new Vector3D(0, -20004, -20),
                                   20000,
                                   new Vector3D(0.2, 0.2, 0.2),
                                   Vector3D.ZERO,
                                   0,
                                   0),
                        new Sphere(new Vector3D(0, 1, -20),
                                   3,
                                   new Vector3D(1, 0.32, 0.36), // rot
                                   Vector3D.ZERO,
                                   1, 0.f),
                        new Sphere(new Vector3D(5.0, -1, -15),
                                   2,
                                   new Vector3D(0.90, 0.76, 0.46), // gelb
                                   Vector3D.ZERO,
                                   1,
                                   0),
                        new Sphere(new Vector3D(5.0, 0, -25),
                                   3,
                                   new Vector3D(0.65, 0.77, 0.97), // blau
                                   Vector3D.ZERO,
                                   1,
                                   0),
                        new Sphere(new Vector3D(-5.5, 0, -15),
                                   3,
                                   new Vector3D(1, 1, 1), // grau
                                   Vector3D.ZERO,
                                   1f,
                                   0.f),
                        new Sphere(new Vector3D(-4, 1, -25),
                                   3,
                                   new Vector3D(0.40, 0.90, 0.40), // grün
                                   Vector3D.ZERO,
                                   1,
                                   0),
                        new Sphere(new Vector3D(0, 0, 20),
                                   4,
                                   new Vector3D(0.90, 0.76, 0.46), // rot
                                   Vector3D.ZERO,
                                   1, 0),
                        // light
                        new Sphere(new Vector3D(10.0, 20, -30),
                                   3,
                                   Vector3D.ZERO,
                                   new Vector3D(3, 3, 3),
                                   0,
                                   0),
                        // light
                        new Sphere(new Vector3D(-10.0, 20, -30),
                                   5,
                                   Vector3D.ZERO,
                                   new Vector3D(3, 3, 2),
                                   0,
                                   0)
                ));
    }
}
