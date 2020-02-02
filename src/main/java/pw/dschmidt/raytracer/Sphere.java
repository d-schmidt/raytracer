package pw.dschmidt.raytracer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


public class Sphere {
    private final Vector3D center;
    private final double radius;
    /**
     * square of radius
     */
    private final double radius2;

    private final Vector3D surfaceColor;
    private final Vector3D emissionColor;
    private final float reflection;
    private final float transparency;


    public Sphere(Vector3D center, double radius, Vector3D surfaceColor, Vector3D emissionColor,
                  float reflection, float transparency) {
        this.center = center;
        this.radius = radius;
        this.radius2 = radius * radius;
        this.surfaceColor = surfaceColor;
        this.emissionColor = emissionColor;
        this.transparency = transparency;
        this.reflection = reflection;
    }


    double[] intersect(Vector3D rayOrigin, Vector3D rayDirection) {
        Vector3D l = center.subtract(rayOrigin);
        double tca = l.dotProduct(rayDirection);
        if (tca < 0) return new double[0];
        double d2 = l.dotProduct(l) - tca * tca;
        if (d2 > radius2) return new double[0];
        double thc = Math.sqrt(radius2 - d2);

        return new double[]{tca - thc, tca + thc};
    }


    public Vector3D getCenter() {
        return center;
    }


    public Vector3D getSurfaceColor() {
        return surfaceColor;
    }


    public Vector3D getEmissionColor() {
        return emissionColor;
    }


    public float getTransparency() {
        return transparency;
    }


    public float getReflection() {
        return reflection;
    }


    @Override
    public String toString() {
        return "Sphere{" +
                "center=" + center +
                ", radius=" + radius +
                ", radius2=" + radius2 +
                ", surfaceColor=" + surfaceColor +
                ", emissionColor=" + emissionColor +
                ", transparency=" + transparency +
                ", reflection=" + reflection +
                '}';
    }
}