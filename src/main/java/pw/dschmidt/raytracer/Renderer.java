package pw.dschmidt.raytracer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;


public class Renderer {

    private final int MAX_RAY_DEPTH = 5;

    private final int width;
    private final int height;

    private final double fov;

    private String output;


    /**
     * @param width  of resulting image
     * @param height of resulting image
     * @param fov    in degree (1-360)
     * @param output file name format string to write result to
     */
    public Renderer(int width, int height, double fov, String output) {
        // super scale for smoother edges
        this.width = width * 2;
        this.height = height * 2;

        this.fov = fov;
        this.output = output;
    }


    public void render(List<Sphere> spheres) {

        // render from different angles
        for (int degree = 0; degree < 360; degree+=90) {
            double rad = FastMath.toRadians(degree);
            Vector3D origin = new Vector3D(5 * FastMath.sin(rad), 0, 5 * FastMath.cos(rad) - 5);
            render(spheres, origin, degree);
        }
    }


    private void getFuture(Future<?> future) {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException ignore) {
        }
    }


    private void render(List<Sphere> spheres, Vector3D origin, int imageId) {
        Vector3D[] image = new Vector3D[this.width * this.height];
        double inverseWidth = 1. / this.width;
        double inverseHeight = 1. / this.height;
        double aspectRatio = this.width / (double) this.height;
        double angle = FastMath.tan(FastMath.toRadians(0.5 * this.fov));

        List<Future<?>> rows = new ArrayList<>(this.width * this.height);
        for (int y = 0; y < height; y++) {
            final int fy = y;
            rows.add(CompletableFuture.runAsync(() -> {
                for (int x = 0; x < width; x++) {
                    double xx = (2 * ((x + 0.5) * inverseWidth) - 1) * angle * aspectRatio;
                    double yy = (1 - 2 * ((fy + 0.5) * inverseHeight)) * angle;
                    Vector3D rayDirection = new Vector3D(xx, yy, -1).normalize();
                    Vector3D result = trace(origin, rayDirection, spheres, 0);
                    image[fy * width + x] = result;
                }
            }));
        }

        rows.forEach(this::getFuture);

        writeImage(image, imageId);
    }


    private Vector3D trace(Vector3D rayOrigin, Vector3D rayDirection, List<Sphere> spheres,
                           int depth) {

        double distanceNearest = Double.POSITIVE_INFINITY;
        Sphere hitSphere = null;
        // find intersection of this ray with the sphere in the scene
        for (Sphere sphere : spheres) {
            double[] intersect = sphere.intersect(rayOrigin, rayDirection);
            if (intersect.length > 0) {
                if (intersect[0] < 0) {
                    intersect[0] = intersect[1];
                }
                if (intersect[0] < distanceNearest) {
                    distanceNearest = intersect[0];
                    hitSphere = sphere;
                }
            }
        }
        // if there's no intersection return black or background color
        if (hitSphere == null) {
            return new Vector3D(2, 2, 2);
        }

        // color of the ray/surface of the object intersected by the ray
        Vector3D surfaceColor = hitSphere.getSurfaceColor();
        // point of intersection
        Vector3D phit = rayOrigin.add(rayDirection.scalarMultiply(distanceNearest));
        // normalized normal direction at the intersection point
        Vector3D nhit = phit.subtract(hitSphere.getCenter()).normalize();

        // If the normal and the view direction are not opposite to each other
        // reverse the normal direction. That also means we are inside the sphere so set
        // the inside bool to true. Finally reverse the sign of IdotN which we want
        // positive.
        double bias = 1e-4; // add some bias to the point from which we will be tracing
        boolean inside = false;

        if (rayDirection.dotProduct(nhit) > 0) {
            nhit = nhit.negate();
            inside = true;
        }

        if ((hitSphere.getTransparency() > 0 || hitSphere.getReflection() > 0)
                && depth < MAX_RAY_DEPTH) {
            double fresnelEffect = getFresnelEffect(rayDirection, nhit);
            // compute reflection direction (not need to normalize because all vectors
            // are already normalized)
            Vector3D reflectionDirection = rayDirection.subtract(
                    nhit.scalarMultiply(2)
                            .scalarMultiply(rayDirection.dotProduct(nhit)));
            reflectionDirection = reflectionDirection.normalize();
            Vector3D reflection = trace(phit.add(nhit.scalarMultiply(bias)),
                                        reflectionDirection,
                                        spheres,
                                        depth + 1);
            Vector3D refraction = Vector3D.ZERO;
            // if the sphere is also transparent compute refraction ray (transmission)
            if (hitSphere.getTransparency() > 0) {
                double ior = 1.1;
                double eta = inside ? ior : 1 / ior; // are we inside or outside the surface?
                double cosi = -nhit.dotProduct(rayDirection);
                double k = 1 - eta * eta * (1 - cosi * cosi);
                Vector3D refrdir = rayDirection.scalarMultiply(eta)
                        .add(nhit.scalarMultiply(eta * cosi - FastMath.sqrt(k)));
                refrdir = refrdir.normalize();
                refraction = trace(phit.subtract(nhit.scalarMultiply(bias)),
                                   refrdir,
                                   spheres,
                                   depth + 1);
            }
            // the result is a mix of reflection and refraction (if the sphere is transparent)
            surfaceColor = product(reflection.scalarMultiply(fresnelEffect)
                                           .add(refraction.scalarMultiply(1 - fresnelEffect)
                                                        .scalarMultiply(hitSphere.getTransparency())),
                                   hitSphere.getSurfaceColor());
        } else {
            // it's a diffuse object, no need to raytrace any further
            for (int i = 0; i < spheres.size(); i++) {
                if (!spheres.get(i).getEmissionColor().equals(Vector3D.ZERO)) {
                    // this is a light
                    Vector3D transmission = new Vector3D(1, 1, 1);
                    Vector3D lightDirection = spheres.get(i).getCenter().subtract(phit).normalize();
                    for (int j = 0; j < spheres.size(); j++) {
                        if (i != j) {
                            double[] intersect = spheres.get(j)
                                    .intersect(phit.add(nhit.scalarMultiply(bias)), lightDirection);
                            if (intersect.length > 0) {
                                transmission = Vector3D.ZERO;
                                break;
                            }
                        }
                    }

                    surfaceColor = surfaceColor.add(product(product(hitSphere.getSurfaceColor(),
                                                                    transmission)
                                                                    .scalarMultiply(FastMath.max(0,
                                                                                                 nhit.dotProduct(
                                                                                                         lightDirection))),
                                                            spheres.get(i).getEmissionColor()));
                }
            }
        }

        return surfaceColor.add(hitSphere.getEmissionColor());
    }


    private double getFresnelEffect(Vector3D rayDirection, Vector3D nhit) {
        double facingRatio = -rayDirection.dotProduct(nhit);
        // change the mix value to tweak the effect
        return mix(FastMath.pow(1 - facingRatio, 3), 1, 0.1);
    }


    private double mix(final double a, final double b, final double mix) {
        return b * mix + a * (1 - mix);
    }


    /**
     * https://en.wikipedia.org/wiki/Hadamard_product_(matrices)
     */
    private Vector3D product(Vector3D a, Vector3D b) {
        return new Vector3D(a.getX() * b.getX(), a.getY() * b.getY(), a.getZ() * b.getZ());
    }


    private void writeImage(Vector3D[] image, int imageId) {
        BufferedImage superScaleImage = new BufferedImage(width,
                                                          height,
                                                          BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Vector3D color = image[y * width + x];
                superScaleImage.setRGB(x, y, getVectorAsRGB(color));
            }
        }

        try {
            BufferedImage outputImage = new BufferedImage(width / 2,
                                                          height / 2,
                                                          BufferedImage.TYPE_INT_RGB);

            Image targetSizeImage = superScaleImage.getScaledInstance(width / 2,
                                                                      height / 2,
                                                                      Image.SCALE_SMOOTH);

            Graphics2D outputGraphics = outputImage.createGraphics();
            outputGraphics.drawImage(targetSizeImage, 0, 0, null);
            outputGraphics.dispose();

            ImageIO.write(outputImage, "png", new File(String.format(output, imageId)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private int getVectorAsRGB(Vector3D color) {
        return (getColorAsInt(color.getX()) << 16) |
                (getColorAsInt(color.getY()) << 8) |
                getColorAsInt(color.getZ());
    }


    /**
     * @param color around 0.0 .. 1.0
     * @return color 0 - 255
     */
    private int getColorAsInt(double color) {
        double minMax = FastMath.max(0, FastMath.min(1, color));
        return ((int) (minMax * 255 + 0.5) & 0xFF);
    }
}
